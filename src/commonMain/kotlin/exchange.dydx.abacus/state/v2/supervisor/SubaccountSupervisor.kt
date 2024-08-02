package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.output.Notification
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
import exchange.dydx.abacus.state.manager.ApiData
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.FaucetRecord
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadableFaucetPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.TransactionQueue
import exchange.dydx.abacus.state.manager.notification.NotificationsProvider
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.adjustIsolatedMargin
import exchange.dydx.abacus.state.model.closePosition
import exchange.dydx.abacus.state.model.historicalPnl
import exchange.dydx.abacus.state.model.orderCanceled
import exchange.dydx.abacus.state.model.receivedBatchSubaccountsChanges
import exchange.dydx.abacus.state.model.receivedFills
import exchange.dydx.abacus.state.model.receivedSubaccountSubscribed
import exchange.dydx.abacus.state.model.receivedSubaccountsChanges
import exchange.dydx.abacus.state.model.receivedTransfers
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.triggerOrders
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.values
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIList
import kollections.toIMap
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days

internal class SubaccountSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    private val configs: SubaccountConfigs,
    private val accountAddress: String,
    internal val subaccountNumber: Int,
) : DynamicNetworkSupervisor(stateMachine, helper, analyticsUtils) {

    private val payloadProvider: SubaccountTransactionPayloadProviderProtocol = SubaccountTransactionPayloadProvider(
        stateMachine = stateMachine,
        subaccountNumber = subaccountNumber,
        helper = helper,
        accountAddress = accountAddress,
    )
    private val transactionSupervisor = SubaccountTransactionSupervisor(
        stateMachine = stateMachine,
        helper = helper,
        analyticsUtils = analyticsUtils,
        accountAddress = accountAddress,
        subaccountNumber = subaccountNumber,
        payloadProvider = payloadProvider,
    )

    internal val transactionQueue: TransactionQueue
        get() =
            transactionSupervisor.transactionQueue

    /*
    Because faucet is done at subaccount level, we need SubaccountSupervisor even
    before the subaccount is realized on protocol/indexer.

    The realized flag indicates whether the subaccount contains payload from the indexer.
    Only when it is true, we send REST requests and subscribe to the subaccount channel.
     */
    internal var realized: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    didSetRealized()
                }
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
        stateMachine,
        helper.uiImplementations,
        helper.environment,
        helper.parser,
        helper.jsonEncoder,
        configs.useParentSubaccount,
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
        if (indexerConnected && realized) {
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
        if (configs.subscribeToSubaccount && realized) {
            subaccountChannelSubscription(
                configs.useParentSubaccount,
                socketConnected,
            )
        }
    }

    private fun retrieveFills() {
        val oldState = stateMachine.state
        val url =
            helper.configs.privateApiUrl(if (configs.useParentSubaccount) "parent-fills" else "fills")
        val params = if (configs.useParentSubaccount) parentSubaccountParams() else subaccountParams()
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

    private fun parentSubaccountParams(): IMap<String, String> {
        val accountAddress = accountAddress
        val subaccountNumber = subaccountNumber
        return iMapOf(
            "address" to accountAddress,
            "parentSubaccountNumber" to "$subaccountNumber",
        )
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
        val url = helper.configs.privateApiUrl(if (configs.useParentSubaccount) "parent-transfers" else "transfers")
        val params = if (configs.useParentSubaccount) parentSubaccountParams() else subaccountParams()
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
        val url = helper.configs.privateApiUrl(if (configs.useParentSubaccount) "parent-historical-pnl" else "historical-pnl") ?: return
        val params = if (configs.useParentSubaccount) parentSubaccountParams() else subaccountParams()

        if (stateMachine.staticTyping) {
            val historicalPNLs = stateMachine.internalState.wallet.account.subaccounts[subaccountNumber]?.historicalPNLs
            // TODO: remove last if calculated
//              if (historicalPNLs != null) {
//                val last = historicalPNLs.lastOrNull()
//                if (last != null && last.calculated) {
//                    historicalPNLs.removeLast()
//                }
//            }

            helper.retrieveTimed(
                url = url,
                items = historicalPNLs,
                timeField = { item ->
                    item?.createdAtMilliseconds?.toLong()?.let {
                        Instant.fromEpochMilliseconds(it)
                    }
                },
                sampleDuration = 1.days,
                maxDuration = 90.days,
                beforeParam = "createdBeforeOrAt",
                afterParam = "createdOnOrAfter",
                additionalParams = params,
                previousUrl = previousUrl,
            ) { url, response, httpCode, _ ->
                val oldState = stateMachine.state
                if (helper.success(httpCode) && !response.isNullOrEmpty()) {
                    val changes = stateMachine.historicalPnl(
                        payload = response,
                        subaccountNumber = subaccountNumber,
                    )
                    update(changes, oldState)
                    if (changes.changes.contains(Changes.historicalPnl)) {
                        retrieveHistoricalPnls(url)
                    }
                }
            }
        } else {
            val historicalPnl = helper.parser.asNativeList(
                helper.parser.value(
                    stateMachine.data,
                    "wallet.account.subaccounts.$subaccountNumber.historicalPnl",
                ),
            )?.mutable()

            if (historicalPnl != null) {
                val last = helper.parser.asMap(historicalPnl.lastOrNull())
                if (helper.parser.asBool(last?.get("calculated")) == true) {
                    historicalPnl.removeLast()
                }
            }

            helper.retrieveTimed(
                url = url,
                items = historicalPnl,
                timeField = { item ->
                    helper.parser.asDatetime(helper.parser.asMap(item)?.get("createdAt"))
                },
                sampleDuration = 1.days,
                maxDuration = 90.days,
                beforeParam = "createdBeforeOrAt",
                afterParam = "createdOnOrAfter",
                additionalParams = params,
                previousUrl = previousUrl,
            ) { url, response, httpCode, _ ->
                val oldState = stateMachine.state
                if (helper.success(httpCode) && !response.isNullOrEmpty()) {
                    val changes = stateMachine.historicalPnl(
                        payload = response,
                        subaccountNumber = subaccountNumber,
                    )
                    update(changes, oldState)
                    if (changes.changes.contains(Changes.historicalPnl)) {
                        retrieveHistoricalPnls(url)
                    }
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun subaccountChannelSubscription(
        parent: Boolean,
        subscribe: Boolean,
    ) {
        val channel = helper.configs.subaccountChannel(parent)
            ?: throw Exception("subaccount channel is null")
        helper.socket(
            helper.socketAction(subscribe),
            channel,
            subaccountChannelParams(accountAddress, subaccountNumber, subscribe),
        )
    }

    private fun subaccountChannelParams(
        accountAddress: String,
        subaccountNumber: Int,
        subscribe: Boolean,
    ): IMap<String, Any> {
        return if (subscribe) {
            iMapOf("id" to "$accountAddress/$subaccountNumber", "batched" to "true")
        } else {
            iMapOf("id" to "$accountAddress/$subaccountNumber")
        }
    }

    internal fun placeOrderPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload {
        return payloadProvider.placeOrderPayload(currentHeight)
    }

    internal fun closePositionPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload {
        return payloadProvider.closePositionPayload(currentHeight)
    }

    internal fun triggerOrdersPayload(currentHeight: Int?): HumanReadableTriggerOrdersPayload {
        return payloadProvider.triggerOrdersPayload(currentHeight)
    }

    internal fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload {
        return payloadProvider.cancelOrderPayload(orderId)
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
                        "closePosition.marketId",
                    ),
                )
            var stateResponse = stateMachine.closePosition(data, type, subaccountNumber)
            if (type == ClosePositionInputField.market && currentMarket != data) {
                val nextResponse = stateMachine.closePosition(
                    "1",
                    ClosePositionInputField.percent,
                    subaccountNumber,
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

    fun triggerOrders(
        data: String?,
        type: TriggerOrdersInputField?,
    ) {
        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse = stateMachine.triggerOrders(data, type, subaccountNumber)
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                helper.stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    fun adjustIsolatedMargin(
        data: String?,
        type: AdjustIsolatedMarginInputField?,
    ) {
        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse = stateMachine.adjustIsolatedMargin(data, type, subaccountNumber)
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                helper.stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    internal fun getTransferPayloadForIsolatedMarginTrade(orderPayload: HumanReadablePlaceOrderPayload): HumanReadableSubaccountTransferPayload? {
        return payloadProvider.transferPayloadForIsolatedMarginTrade(orderPayload)
    }

    internal fun commitPlaceOrder(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadablePlaceOrderPayload {
        return transactionSupervisor.commitPlaceOrder(currentHeight, callback)
    }

    internal fun commitClosePosition(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadablePlaceOrderPayload {
        return transactionSupervisor.commitClosePosition(currentHeight, callback)
    }

    internal fun cancelOrder(orderId: String, isOrphanedTriggerOrder: Boolean = false, callback: TransactionCallback): HumanReadableCancelOrderPayload {
        return transactionSupervisor.cancelOrder(orderId, isOrphanedTriggerOrder, callback)
    }

    internal fun commitTriggerOrders(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadableTriggerOrdersPayload {
        return transactionSupervisor.commitTriggerOrders(currentHeight, callback)
    }

    internal fun commitAdjustIsolatedMargin(
        callback: TransactionCallback
    ): HumanReadableSubaccountTransferPayload {
        return transactionSupervisor.commitAdjustIsolatedMargin(callback)
    }

    internal fun stopWatchingLastOrder() {
        transactionSupervisor.stopWatchingLastOrder()
    }

    internal fun orderCanceled(orderId: String) {
        return transactionSupervisor.orderCanceled(orderId)
    }

    @Throws(Exception::class)
    fun depositPayload(): HumanReadableDepositPayload {
        return payloadProvider.depositPayload()
    }

    @Throws(Exception::class)
    fun withdrawPayload(): HumanReadableWithdrawPayload {
        return payloadProvider.withdrawPayload()
    }

    @Throws(Exception::class)
    fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload {
        return payloadProvider.subaccountTransferPayload()
    }

    @Throws(Exception::class)
    fun adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload {
        return payloadProvider.adjustIsolatedMarginPayload()
    }

    private fun faucetBody(amount: Double): String? {
        return if (accountAddress != null) {
            val params = iMapOf(
                "address" to accountAddress,
                "subaccountNumber" to subaccountNumber,
                "amount" to amount,
            )
            helper.jsonEncoder.encode(params)
        } else {
            null
        }
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

    private fun parseFaucetResponse(
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
                    ),
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
        transactionSupervisor.parseTransfersToMatchFaucetRecords(faucetRecords)
    }

    override fun updateTracking(changes: StateChanges) {
        if (changes.changes.contains(Changes.transfers)) {
            transactionSupervisor.parseTransfersToMatchFaucetRecords(faucetRecords)
        }
        if (changes.changes.contains(Changes.subaccount)) {
            transactionSupervisor.parseOrdersToMatchPlaceOrdersAndCancelOrders()
            transactionSupervisor.cancelTriggerOrdersWithClosedOrFlippedPositions()
            transactionSupervisor.reclaimUnutilizedFundsFromChildSubaccounts()
        }
    }

    override fun updateNotifications() {
        val notifications = notificationsProvider.buildNotifications(subaccountNumber)
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
                            payload.toString(),
                        )
                    changes = stateMachine.receivedSubaccountSubscribed(content, height)
                }

                "unsubscribed" -> {}

                "channel_data" -> {
                    val content = helper.parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = stateMachine.receivedSubaccountsChanges(content, info, height)
                }

                "channel_batch_data" -> {
                    val content =
                        helper.parser.asList(payload["contents"]) as? IList<IMap<String, Any>>
                            ?: throw ParsingException(
                                ParsingErrorType.MissingContent,
                                payload.toString(),
                            )
                    changes = stateMachine.receivedBatchSubaccountsChanges(content, info, height)
                }

                else -> {
                    throw ParsingException(
                        ParsingErrorType.Unhandled,
                        "Type [ ${info.type} ] is not handled",
                    )
                }
            }
            update(changes, oldState)

            transactionSupervisor.updateLastOrder()
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

    private fun didSetRealized() {
        if (realized) {
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
            if (socketConnected) {
                if (configs.subscribeToSubaccount) {
                    subaccountChannelSubscription(configs.useParentSubaccount, true)
                }
            }
        }
    }
}
