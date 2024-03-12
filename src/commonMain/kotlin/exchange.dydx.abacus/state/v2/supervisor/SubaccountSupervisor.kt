package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.TransferRecordType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.NotificationsProvider
import exchange.dydx.abacus.state.manager.ApiData
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.CancelOrderRecord
import exchange.dydx.abacus.state.manager.FaucetRecord
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadableFaucetPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.PlaceOrderMarketInfo
import exchange.dydx.abacus.state.manager.PlaceOrderRecord
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.closePosition
import exchange.dydx.abacus.state.model.findOrder
import exchange.dydx.abacus.state.model.historicalPnl
import exchange.dydx.abacus.state.model.orderCanceled
import exchange.dydx.abacus.state.model.receivedAccountsChanges
import exchange.dydx.abacus.state.model.receivedBatchAccountsChanges
import exchange.dydx.abacus.state.model.receivedFills
import exchange.dydx.abacus.state.model.receivedSubaccountSubscribed
import exchange.dydx.abacus.state.model.receivedTransfers
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.GoodTil
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.values
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIList
import kollections.toIMap
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

internal class SubaccountSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    private val configs: SubaccountConfigs,
    private val accountAddress: String,
    internal val subaccountNumber: Int
) : DynamicNetworkSupervisor(stateMachine, helper, analyticsUtils) {
    private var placeOrderRecords: IMutableList<PlaceOrderRecord> = iMutableListOf()
        set(value) {
            if (field !== value) {
                field = value
                didSetPlaceOrderRecords()
            }
        }

    private var cancelOrderRecords: IMutableList<CancelOrderRecord> = iMutableListOf()
        set(value) {
            if (field !== value) {
                field = value
                didSetCancelOrderRecords()
            }
        }

    private var lastOrderClientId: Int? = null
        set(value) {
            if (field != value) {
                field = value
                lastOrder = if (value != null) {
                    stateMachine.findOrder(value, subaccountNumber)
                } else {
                    null
                }
            }
        }


    private var lastOrder: SubaccountOrder? = null
        set(value) {
            if (field !== value) {
                field = value
                helper.stateNotification?.lastOrderChanged(lastOrder)
                helper.dataNotification?.lastOrderChanged(lastOrder)
            }
        }

    private var faucetRecords: IMutableList<FaucetRecord> = iMutableListOf()
        set(value) {
            if (field !== value) {
                field = value
                didSetFaucetRecords()
            }
        }

    private val notificationsProvider = NotificationsProvider(
        helper.uiImplementations,
        helper.environment,
        helper.parser,
        helper.jsonEncoder
    )

    internal var notifications: IMap<String, Notification> = iMapOf()
        set(value) {
            if (field !== value) {
                field = value
                sendNotifications()
            }
        }


    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)
        if (indexerConnected) {
            if (configs.retrieveFills) {
                retrieveFills()
            }
            if (configs.retrieveTransfers) {
                retrieveTransfers()
            }
            if (configs.retrieveHistoricalPnls) {
                retrieveHistoricalPnls()
            }
        }
    }

    override fun didSetSocketConnected(socketConnected: Boolean) {
        super.didSetSocketConnected(socketConnected)
        if (configs.subscribeToSubaccount) {
            subaccountChannelSubscription(socketConnected)
        }
    }

    private fun retrieveFills() {
        val oldState = stateMachine.state
        val url = helper.configs.privateApiUrl("fills")
        val params = subaccountParams()
        if (url != null) {
            helper.get(url, params, null, callback = { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    val fills = helper.parser.decodeJsonObject(response)?.toIMap()
                    if (fills != null && fills.size != 0) {
                        update(stateMachine.receivedFills(fills, subaccountNumber), oldState)
                    }
                }
            })
        }
    }

    private fun subaccountParams(): IMap<String, String> {
        val accountAddress = accountAddress
        val subaccountNumber = subaccountNumber
        return iMapOf(
            "address" to accountAddress,
            "subaccountNumber" to "$subaccountNumber",
        )
    }

    private fun retrieveTransfers() {
        val oldState = stateMachine.state
        val url = helper.configs.privateApiUrl("transfers")
        val params = subaccountParams()
        if (url != null) {
            helper.get(url, params, null, callback = { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    val tranfers = helper.parser.decodeJsonObject(response)
                    if (tranfers != null && tranfers.size != 0) {
                        update(stateMachine.receivedTransfers(tranfers, subaccountNumber), oldState)
                    }
                }
            })
        }
    }

    internal fun retrieveHistoricalPnls(previousUrl: String? = null) {
        val url = helper.configs.privateApiUrl("historical-pnl") ?: return
        val params = subaccountParams()
        val historicalPnl = helper.parser.asNativeList(
            helper.parser.value(
                stateMachine.data,
                "wallet.account.subaccounts.$subaccountNumber.historicalPnl"
            )
        )?.mutable()

        if (historicalPnl != null) {
            val last = helper.parser.asMap(historicalPnl.lastOrNull())
            if (helper.parser.asBool(last?.get("calculated")) == true) {
                historicalPnl.removeLast()
            }
        }

        helper.retrieveTimed(
            url,
            historicalPnl,
            "createdAt",
            1.days,
            180.days,
            "createdBeforeOrAt",
            "createdAtOrAfter",
            params,
            previousUrl
        ) { url, response, httpCode, _ ->
            val oldState = stateMachine.state
            if (helper.success(httpCode) && !response.isNullOrEmpty()) {
                val changes = stateMachine.historicalPnl(
                    payload = response,
                    subaccountNumber = subaccountNumber
                )
                update(changes, oldState)
                if (changes.changes.contains(Changes.historicalPnl)) {
                    retrieveHistoricalPnls(url)
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun subaccountChannelSubscription(
        subscribe: Boolean = true,
    ) {
        val channel =
            helper.configs.subaccountChannel() ?: throw Exception("subaccount channel is null")
        helper.socket(
            helper.socketAction(subscribe),
            channel,
            subaccountChannelParams(accountAddress, subaccountNumber)
        )
    }

    private fun subaccountChannelParams(
        accountAddress: String,
        subaccountNumber: Int,
    ): IMap<String, Any> {
        return iMapOf("id" to "$accountAddress/$subaccountNumber")
    }

    private fun didSetPlaceOrderRecords() {
        parseOrdersToMatchPlaceOrdersAndCancelOrders()
    }

    private fun parseOrdersToMatchPlaceOrdersAndCancelOrders() {
        if (placeOrderRecords.isNotEmpty() || cancelOrderRecords.isNotEmpty()) {
            val subaccount = stateMachine.state?.subaccount(subaccountNumber) ?: return
            val orders = subaccount.orders ?: return
            for (order in orders) {
                val orderAnalyticsPayload = analyticsUtils.formatOrder(order)
                val placeOrderRecord = placeOrderRecords.firstOrNull {
                    it.clientId == order.clientId
                }
                if (placeOrderRecord != null) {
                    val interval = Clock.System.now().toEpochMilliseconds()
                        .toDouble() - placeOrderRecord.timestampInMilliseconds
                    tracking(
                        AnalyticsEvent.TradePlaceOrderConfirmed.rawValue,
                        ParsingHelper.merge(
                            trackingParams(interval), orderAnalyticsPayload
                        )?.toIMap()
                    )
                    placeOrderRecords.remove(placeOrderRecord)
                    break
                }
                val cancelOrderRecord = cancelOrderRecords.firstOrNull {
                    it.clientId == order.clientId
                }
                if (cancelOrderRecord != null) {
                    val interval = Clock.System.now().toEpochMilliseconds()
                        .toDouble() - cancelOrderRecord.timestampInMilliseconds
                    tracking(
                        AnalyticsEvent.TradeCancelOrderConfirmed.rawValue,
                        ParsingHelper.merge(
                            trackingParams(interval), orderAnalyticsPayload
                        )?.toIMap()
                    )
                    cancelOrderRecords.remove(cancelOrderRecord)
                    break
                }
            }
        }
    }

    private fun trackingParams(interval: Double): IMap<String, Any> {
        return iMapOf(
            "roundtripMs" to interval,
        )
    }

    private fun didSetCancelOrderRecords() {
        parseOrdersToMatchPlaceOrdersAndCancelOrders()
    }

    fun trade(
        data: String?,
        type: TradeInputField?,
    ) {
        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse = stateMachine.trade(data, type, subaccountNumber)
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                helper.stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    fun closePosition(
        data: String?,
        type: ClosePositionInputField,
    ) {
        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
            val currentMarket =
                helper.parser.asString(
                    helper.parser.value(
                        stateMachine.input,
                        "closePosition.marketId"
                    )
                )
            var stateResponse = stateMachine.closePosition(data, type, subaccountNumber)
            if (type == ClosePositionInputField.market && currentMarket != data) {
                val nextResponse = stateMachine.closePosition(
                    "1",
                    ClosePositionInputField.percent,
                    subaccountNumber
                )
                stateResponse = nextResponse.merge(stateResponse)
            }
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                helper.stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    fun cancelOrder(orderId: String, callback: TransactionCallback) {
        val submitTimeInMilliseconds = Clock.System.now().toEpochMilliseconds().toDouble()
        val payload = cancelOrderPayload(orderId)
        val string = Json.encodeToString(payload)

        helper.transaction(TransactionType.CancelOrder, string) { response ->
            val error = helper.parseTransactionResponse(response)
            if (error == null) {
                tracking(
                    AnalyticsEvent.TradeCancelOrder.rawValue,
                    analyticsUtils.formatCancelOrderPayload(payload)
                )
                helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.orderCanceled(orderId)
                    this.cancelOrderRecords.add(
                        CancelOrderRecord(
                            subaccountNumber,
                            payload.clientId,
                            submitTimeInMilliseconds,
                        )
                    )
                }
            }
            helper.send(error, callback, payload)
        }
    }


    internal fun commitPlaceOrder(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadablePlaceOrderPayload {
        val submitTimeInMilliseconds = Clock.System.now().toEpochMilliseconds().toDouble()
        val payload = placeOrderPayload(currentHeight)
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)

        val analyticsPayload = analyticsUtils.formatPlaceOrderPayload(
            payload,
            false
        )

        lastOrderClientId = null
        helper.transaction(TransactionType.PlaceOrder, string) { response ->
            val error = helper.parseTransactionResponse(response)
            if (error == null) {
                tracking(
                    AnalyticsEvent.TradePlaceOrder.rawValue,
                    analyticsPayload,
                )
                helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.placeOrderRecords.add(
                        PlaceOrderRecord(
                            subaccountNumber,
                            payload.clientId,
                            submitTimeInMilliseconds,
                        )
                    )
                    lastOrderClientId = clientId
                }
            }
            helper.send(error, callback, payload)
        }
        return payload
    }

    internal fun stopWatchingLastOrder() {
        lastOrderClientId = null
    }

    internal fun commitClosePosition(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadablePlaceOrderPayload {
        val submitTimeInMilliseconds = Clock.System.now().toEpochMilliseconds().toDouble()
        val payload = closePositionPayload(currentHeight)
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)
        val analyticsPayload = analyticsUtils.formatPlaceOrderPayload(
            payload,
            true
        )

        lastOrderClientId = null
        helper.transaction(TransactionType.PlaceOrder, string) { response ->
            val error = helper.parseTransactionResponse(response)
            if (error == null) {
                tracking(
                    AnalyticsEvent.TradePlaceOrder.rawValue,
                    analyticsPayload,
                )
                helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.placeOrderRecords.add(
                        PlaceOrderRecord(
                            subaccountNumber,
                            payload.clientId,
                            submitTimeInMilliseconds,
                        )
                    )
                    lastOrderClientId = clientId
                }
            }
            helper.send(error, callback, payload)
        }
        return payload
    }

    @Throws(Exception::class)
    fun placeOrderPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload {
        val trade = stateMachine.state?.input?.trade
        val marketId = trade?.marketId ?: throw Exception("marketId is null")
        val summary = trade.summary ?: throw Exception("summary is null")
        val clientId = Random.nextInt(0, Int.MAX_VALUE)
        val type = trade.type?.rawValue ?: throw Exception("type is null")
        val side = trade.side?.rawValue ?: throw Exception("side is null")
        val price = summary.payloadPrice ?: throw Exception("price is null")
        val triggerPrice =
            if (trade.options?.needsTriggerPrice == true) trade.price?.triggerPrice else null

        val size = summary.size ?: throw Exception("size is null")
        val reduceOnly = if (trade.options?.needsReduceOnly == true) trade.reduceOnly else null
        val postOnly = if (trade.options?.needsPostOnly == true) trade.postOnly else null

        val timeInForce = if (trade.options?.timeInForceOptions != null) {
            when (trade.type) {
                OrderType.market -> "FOK"
                else -> trade.timeInForce ?: "FOK"
            }
        } else null

        val execution = if (trade.options?.executionOptions != null) {
            trade.execution ?: "Default"
        } else null

        val goodTilTimeInSeconds = ((if (trade.options?.goodTilUnitOptions != null) {
            val timeInterval =
                GoodTil.duration(trade.goodTil) ?: throw Exception("goodTil is null")
            timeInterval / 1.seconds
        } else null))?.toInt()

        val marketInfo = marketInfo(marketId)
        return HumanReadablePlaceOrderPayload(
            subaccountNumber,
            marketId,
            clientId,
            type,
            side,
            price,
            triggerPrice,
            size,
            reduceOnly,
            postOnly,
            timeInForce,
            execution,
            goodTilTimeInSeconds,
            marketInfo,
            currentHeight
        )
    }


    @Throws(Exception::class)
    fun closePositionPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload {
        val closePosition = stateMachine.state?.input?.closePosition
        val marketId = closePosition?.marketId ?: throw Exception("marketId is null")
        val summary = closePosition.summary ?: throw Exception("summary is null")
        val clientId = Random.nextInt(0, Int.MAX_VALUE)
        val side = closePosition.side?.rawValue ?: throw Exception("side is null")
        val price = summary.payloadPrice ?: throw Exception("price is null")
        val size = summary.size ?: throw Exception("size is null")
        val timeInForce = "IOC"
        val execution = "Default"
        val reduceOnly = helper.environment.featureFlags.reduceOnlySupported
        val postOnly = false
        val goodTilTimeInSeconds = null
        val marketInfo = marketInfo(marketId)
        return HumanReadablePlaceOrderPayload(
            subaccountNumber,
            marketId,
            clientId,
            "MARKET",
            side,
            price,
            null,
            size,
            reduceOnly,
            postOnly,
            timeInForce,
            execution,
            goodTilTimeInSeconds,
            marketInfo,
            currentHeight,
        )
    }

    fun closePositionPayloadJson(currentHeight: Int?): String {
        return Json.encodeToString(closePositionPayload(currentHeight))
    }


    private fun marketInfo(marketId: String): PlaceOrderMarketInfo? {
        val market = stateMachine.state?.market(marketId) ?: return null
        val v4config = market.configs?.v4 ?: return null

        return PlaceOrderMarketInfo(
            v4config.clobPairId,
            v4config.atomicResolution,
            v4config.stepBaseQuantums,
            v4config.quantumConversionExponent,
            v4config.subticksPerTick,
        )
    }

    @Throws(Exception::class)
    fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload {
        val subaccount = stateMachine.state?.subaccount(subaccountNumber)
            ?: throw Exception("subaccount is null")
        val order = subaccount.orders?.firstOrNull() { it.id == orderId }
            ?: throw Exception("order is null")
        val clientId = order.clientId ?: throw Exception("clientId is null")
        val orderFlags = order.orderFlags ?: throw Exception("orderFlags is null")
        val clobPairId = order.clobPairId ?: throw Exception("clobPairId is null")
        val goodTilBlock = order.goodTilBlock
        val goodTilBlockTime = order.goodTilBlockTime

        return HumanReadableCancelOrderPayload(
            subaccountNumber,
            orderId,
            clientId,
            orderFlags,
            clobPairId,
            goodTilBlock,
            goodTilBlockTime,
        )
    }

    internal fun orderCanceled(orderId: String) {
        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
            val changes = stateMachine.orderCanceled(orderId, subaccountNumber)
            if (changes.changes.size != 0) {
                helper.ioImplementations.threading?.async(ThreadingType.main) {
                    helper.stateNotification?.stateChanged(
                        stateMachine.state,
                        changes,
                    )
                }
            }
        }
    }


    @Throws(Exception::class)
    fun depositPayload(): HumanReadableDepositPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.size ?: throw Exception("size is null")
        return HumanReadableDepositPayload(
            subaccountNumber,
            amount
        )
    }

    @Throws(Exception::class)
    fun withdrawPayload(): HumanReadableWithdrawPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.usdcSize ?: throw Exception("usdcSize is null")
        return HumanReadableWithdrawPayload(
            subaccountNumber,
            amount
        )
    }

    @Throws(Exception::class)
    fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val size = transfer.size?.size ?: throw Exception("size is null")
        val destinationAddress = transfer.address ?: throw Exception("destination address is null")

        return HumanReadableSubaccountTransferPayload(
            subaccountNumber,
            size,
            destinationAddress,
            0,
        )
    }

    private fun faucetBody(amount: Double): String? {
        return if (accountAddress != null) {
            val params = iMapOf(
                "address" to accountAddress,
                "subaccountNumber" to subaccountNumber,
                "amount" to amount
            )
            helper.jsonEncoder.encode(params)
        } else null
    }

    internal fun faucet(amount: Double, callback: TransactionCallback) {
        val payload = faucetPayload(subaccountNumber, amount)
        val string = Json.encodeToString(payload)
        val submitTimeInMilliseconds = Clock.System.now().toEpochMilliseconds().toDouble()

        helper.transaction(TransactionType.Faucet, string) { response ->
            val error =
                parseFaucetResponse(response, amount, submitTimeInMilliseconds)
            helper.send(error, callback, payload)
        }
    }

    private fun faucetPayload(subaccountNumber: Int, amount: Double): HumanReadableFaucetPayload {
        return HumanReadableFaucetPayload(subaccountNumber, amount)
    }

    internal fun parseFaucetResponse(
        response: String,
        amount: Double,
        submitTimeInMilliseconds: Double
    ): ParsingError? {
        val result = helper.parser.decodeJsonObject(response)
        val status = helper.parser.asInt(result?.get("status"))
        return if (status == 202) {
            helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                this.faucetRecords.add(
                    FaucetRecord(
                        subaccountNumber,
                        amount,
                        submitTimeInMilliseconds,
                    )
                )
            }
            null
        } else if (status != null) {
            V4TransactionErrors.error(null, "API error: $status")
        } else {
            val resultError = helper.parser.asMap(result?.get("error"))
            val message = helper.parser.asString(resultError?.get("message"))
            V4TransactionErrors.error(null, message ?: "Unknown error")
        }
    }

    private fun didSetFaucetRecords() {
        parseTransfersToMatchFaucetRecords()
    }

    private fun parseTransfersToMatchFaucetRecords() {
        val subaccountFaucetRecords =
            faucetRecords.filter { it.subaccountNumber == subaccountNumber }
        if (subaccountFaucetRecords.isNotEmpty()) {
            val transfers = stateMachine.state?.subaccountTransfers(subaccountNumber) ?: return
            val earliest = subaccountFaucetRecords.first().timestampInMilliseconds
            val firstIndexTransferAfter = transfers.indexOfFirst {
                it.updatedAtMilliseconds < earliest
            }
            val transfersAfter = transfers.subList(firstIndexTransferAfter ?: 0, transfers.size)
            // Now, try to match transfers with faucet records
            for (transfer in transfersAfter) {
                when (transfer.type) {
                    TransferRecordType.TRANSFER_IN -> {
                        val faucet = subaccountFaucetRecords.firstOrNull {
                            it.amount == transfer.amount && it.timestampInMilliseconds < transfer.updatedAtMilliseconds
                        }
                        if (faucet != null) {
                            val interval = Clock.System.now().toEpochMilliseconds()
                                .toDouble() - faucet.timestampInMilliseconds
                            tracking(
                                AnalyticsEvent.TransferFaucetConfirmed.rawValue,
                                trackingParams(interval)
                            )
                            faucetRecords.remove(faucet)
                            break
                        }
                    }

                    else -> {}
                }
                val transferType = transfer.type
            }
        }
    }

    override fun updateTracking(changes: StateChanges) {
        if (changes.changes.contains(Changes.transfers)) {
            parseTransfersToMatchFaucetRecords()
        }
        if (changes.changes.contains(Changes.subaccount)) {
            parseOrdersToMatchPlaceOrdersAndCancelOrders()
        }
    }

    override fun updateNotifications() {
        val notifications = notificationsProvider.buildNotifications(stateMachine, subaccountNumber)
        consolidateNotifications(notifications)
    }

    private fun consolidateNotifications(notifications: IMap<String, Notification>) {
        val consolidated = iMutableMapOf<String, Notification>()
        var modified = false
        for ((key, notification) in notifications) {
            val existing = this.notifications[key]
            if (existing != null) {
                if (existing.updateTimeInMilliseconds != notification.updateTimeInMilliseconds ||
                    existing.text != notification.text
                ) {
                    consolidated[key] = notification
                    modified = true
                } else {
                    consolidated[key] = existing
                }
            } else {
                consolidated[key] = notification
                modified = true
            }
        }
        if (modified) {
            this.notifications = consolidated
        }
    }

    private fun sendNotifications() {
        val notifications = notifications.values().sortedWith { notification1, notification2 ->
            val comparison = notification1.priority.compareTo(notification2.priority)
            if (comparison == 0) {
                notification1.updateTimeInMilliseconds.compareTo(notification2.updateTimeInMilliseconds) * -1
            } else {
                comparison * -1
            }
        }.toIList()

        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.stateNotification?.notificationsChanged(notifications)
            helper.dataNotification?.notificationsChanged(notifications)
        }
    }

    internal fun receiveSubaccountChannelSocketData(
        info: SocketInfo,
        payload: IMap<String, Any>,
        height: BlockAndTime?,
    ) {
        val oldState = stateMachine.state
        var changes: StateChanges? = null
        try {
            when (info.type) {
                "subscribed" -> {
                    val content = helper.parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString()
                        )
                    changes = stateMachine.receivedSubaccountSubscribed(content, height)
                }

                "unsubscribed" -> {}

                "channel_data" -> {
                    val content = helper.parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString()
                        )
                    changes = stateMachine.receivedAccountsChanges(content, info, height)
                }

                "channel_batch_data" -> {
                    val content =
                        helper.parser.asList(payload["contents"]) as? IList<IMap<String, Any>>
                            ?: throw ParsingException(
                                ParsingErrorType.MissingContent,
                                payload.toString()
                            )
                    changes = stateMachine.receivedBatchAccountsChanges(content, info, height)
                }

                else -> {
                    throw ParsingException(
                        ParsingErrorType.Unhandled,
                        "Type [ ${info.type} ] is not handled"
                    )
                }
            }
            update(changes, oldState)
        } catch (e: ParsingException) {
            val error = ParsingError(
                e.type,
                e.message ?: "Unknown error",
            )
            emitError(error)
        }
    }

    internal fun refresh(data: ApiData) {
        when (data) {
            ApiData.HISTORICAL_PNLS -> {
                retrieveHistoricalPnls()
            }

            ApiData.HISTORICAL_TRADING_REWARDS -> {}
        }
    }
}