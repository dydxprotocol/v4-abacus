package exchange.dydx.abacus.state.v2.supervisor

import abs
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.calculator.TriggerOrdersConstants.TRIGGER_ORDER_DEFAULT_DURATION_DAYS
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PositionSide
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.TransferRecordType
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputGoodUntil
import exchange.dydx.abacus.output.input.TriggerOrder
import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.LocalTimerProtocol
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
import exchange.dydx.abacus.state.manager.CancelOrderRecord
import exchange.dydx.abacus.state.manager.FaucetRecord
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadableFaucetPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.PlaceOrderMarketInfo
import exchange.dydx.abacus.state.manager.PlaceOrderRecord
import exchange.dydx.abacus.state.manager.TransactionParams
import exchange.dydx.abacus.state.manager.TransactionQueue
import exchange.dydx.abacus.state.manager.notification.NotificationsProvider
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.adjustIsolatedMargin
import exchange.dydx.abacus.state.model.closePosition
import exchange.dydx.abacus.state.model.findOrder
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
import exchange.dydx.abacus.utils.CONDITIONAL_ORDER_FLAGS
import exchange.dydx.abacus.utils.GoodTil
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.MAX_SUBACCOUNT_NUMBER
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_DURATION
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_FLAGS
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.values
import kollections.iListOf
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
    internal val subaccountNumber: Int,
) : DynamicNetworkSupervisor(stateMachine, helper, analyticsUtils) {
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
        val url = helper.configs.privateApiUrl("historical-pnl") ?: return
        val params = subaccountParams()
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
            url,
            historicalPnl,
            "createdAt",
            1.days,
            90.days,
            "createdBeforeOrAt",
            "createdOnOrAfter",
            params,
            previousUrl,
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

        if (parent) {
            pollReclaimUnutilizedFunds()
        }
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
                    val extraParams = ParsingHelper.merge(
                        trackingParams(interval),
                        fromSlTpDialogParams(placeOrderRecord.fromSlTpDialog),
                    )
                    val analyticsPayload = ParsingHelper.merge(extraParams, orderAnalyticsPayload)?.toIMap()

                    if (placeOrderRecord.lastOrderStatus != order.status) {
                        // when order is first indexed
                        if (placeOrderRecord.lastOrderStatus == null) {
                            tracking(
                                AnalyticsEvent.TradePlaceOrderConfirmed.rawValue,
                                analyticsPayload,
                            )
                        }

                        val orderStatusChangeEvent = when (order.status) {
                            OrderStatus.Canceled -> AnalyticsEvent.TradePlaceOrderStatusCanceled
                            OrderStatus.Canceling -> AnalyticsEvent.TradePlaceOrderStatusCanceling
                            OrderStatus.Filled -> AnalyticsEvent.TradePlaceOrderStatusFilled
                            OrderStatus.Open -> AnalyticsEvent.TradePlaceOrderStatusOpen
                            OrderStatus.Pending -> AnalyticsEvent.TradePlaceOrderStatusPending
                            OrderStatus.Untriggered -> AnalyticsEvent.TradePlaceOrderStatusUntriggered
                            OrderStatus.PartiallyFilled -> AnalyticsEvent.TradePlaceOrderStatusPartiallyFilled
                            OrderStatus.PartiallyCanceled -> AnalyticsEvent.TradePlaceOrderStatusPartiallyCanceled
                        }

                        tracking(orderStatusChangeEvent.rawValue, analyticsPayload)

                        when (order.status) {
                            // order reaches final state, can remove / skip further tracking
                            OrderStatus.Canceled, OrderStatus.PartiallyCanceled, OrderStatus.Filled -> {
                                placeOrderRecords.remove(placeOrderRecord)
                            }
                            else -> {}
                        }
                        placeOrderRecord.lastOrderStatus = order.status
                    }
                    break
                }

                val cancelOrderRecord = cancelOrderRecords.firstOrNull {
                    it.clientId == order.clientId
                }
                if (cancelOrderRecord != null) {
                    val interval = Clock.System.now().toEpochMilliseconds()
                        .toDouble() - cancelOrderRecord.timestampInMilliseconds
                    val extraParams = ParsingHelper.merge(
                        trackingParams(interval),
                        fromSlTpDialogParams(cancelOrderRecord.fromSlTpDialog),
                    )
                    tracking(
                        AnalyticsEvent.TradeCancelOrderConfirmed.rawValue,
                        ParsingHelper.merge(
                            extraParams,
                            orderAnalyticsPayload,
                        )?.toIMap(),
                    )
                    cancelOrderRecords.remove(cancelOrderRecord)
                    break
                }
            }
        }
    }

    private var cancelingOrphanedTriggerOrders = mutableSetOf<String>()

    private fun cancelTriggerOrder(orderId: String) {
        cancelingOrphanedTriggerOrders.add(orderId)
        cancelOrder(
            orderId = orderId,
            isOrphanedTriggerOrder = true,
            callback = { _, _, _ -> cancelingOrphanedTriggerOrders.remove(orderId) },
        )
    }

    private fun cancelTriggerOrdersWithClosedOrFlippedPositions() {
        val subaccount = stateMachine.state?.subaccount(subaccountNumber) ?: return
        val cancelableTriggerOrders = subaccount.orders?.filter { order ->
            val isConditionalOrder = order.orderFlags == CONDITIONAL_ORDER_FLAGS
            val isReduceOnly = order.reduceOnly
            val isActiveOrder =
                (order.status == OrderStatus.Untriggered || order.status == OrderStatus.Open)
            isConditionalOrder && isReduceOnly && isActiveOrder
        } ?: return

        cancelableTriggerOrders.forEach { order ->
            if (order.id !in cancelingOrphanedTriggerOrders) {
                val marketPosition = subaccount.openPositions?.find { position -> position.id == order.marketId }
                val hasPositionFlippedOrClosed = marketPosition?.let { position ->
                    when (position.side.current) {
                        PositionSide.LONG -> order.side == OrderSide.Buy
                        PositionSide.SHORT -> order.side == OrderSide.Sell
                        else -> true
                    }
                } ?: true
                if (hasPositionFlippedOrClosed) {
                    cancelTriggerOrder(order.id)
                }
            }
        }
    }

    private fun fromSlTpDialogParams(fromSlTpDialog: Boolean): IMap<String, Any> {
        return iMapOf(
            "fromSlTpDialog" to fromSlTpDialog,
        )
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

    val transactionQueue = TransactionQueue(helper::transaction)

    private fun uiTrackingParams(interval: Double): IMap<String, Any> {
        return iMapOf(
            "clickToSubmitOrderDelayMs" to interval,
        )
    }

    private fun errorTrackingParams(error: ParsingError): IMap<String, Any> {
        return if (error.stringKey != null) {
            iMapOf(
                "errorType" to error.type.rawValue,
                "errorMessage" to error.message,
                "errorStringKey" to error.stringKey,
            )
        } else {
            iMapOf(
                "errorType" to error.type.rawValue,
                "errorMessage" to error.message,
            )
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

    /**
     * @description Get the childSubaccount number that is available for the given marketId
     * @param marketId
     */
    internal fun getChildSubaccountNumberForIsolatedMarginTrade(marketId: String): Int {
        val subaccounts = stateMachine.state?.account?.subaccounts

        val utilizedSubaccountsMarketIdMap = subaccounts?.mapValues { (_, subaccount) ->
            val openPositions = subaccount.openPositions
            val openOrders = subaccount.orders?.filter { order ->
                val status = helper.parser.asString(order.status)

                iListOf(
                    OrderStatus.Open.name,
                    OrderStatus.Pending.name,
                    OrderStatus.Untriggered.name,
                    OrderStatus.PartiallyFilled.name,
                ).contains(status)
            }

            val positionMarketIds = openPositions?.map { position ->
                val positionMarketId = helper.parser.asString(position.id)
                positionMarketId
            }?.filterNotNull() ?: iListOf()

            val openOrderMarketIds = openOrders?.map { order ->
                val orderMarketId = helper.parser.asString(order.marketId)
                orderMarketId
            }?.filterNotNull() ?: iListOf()

            // Return the combined list of marketIds w/o duplicates
            (positionMarketIds + openOrderMarketIds).toSet()
        }

        // Check if an existing childSubaccount is available to use for Isolated Margin Trade
        var availableSubaccountNumber = subaccountNumber
        utilizedSubaccountsMarketIdMap?.forEach { (key, marketIds) ->
            val subaccountNumberToCheck = key.toInt()
            if (subaccountNumberToCheck != subaccountNumber) {
                if (marketIds.contains(marketId) && marketIds.size <= 1) {
                    return subaccountNumberToCheck
                } else if (marketIds.isEmpty()) {
                    if (availableSubaccountNumber == subaccountNumber) {
                        availableSubaccountNumber = subaccountNumberToCheck
                    }
                }
            }
        }

        if (availableSubaccountNumber != subaccountNumber) {
            return availableSubaccountNumber
        }

        // Find new childSubaccount number available for Isolated Margin Trade
        val existingSubaccountNumbers = utilizedSubaccountsMarketIdMap?.keys ?: iListOf(subaccountNumber.toString())
        for (offset in NUM_PARENT_SUBACCOUNTS..MAX_SUBACCOUNT_NUMBER step NUM_PARENT_SUBACCOUNTS) {
            val tentativeSubaccountNumber = offset + subaccountNumber
            if (!existingSubaccountNumbers.contains(tentativeSubaccountNumber.toString())) {
                return tentativeSubaccountNumber
            }
        }

        // User has reached the maximum number of childSubaccounts for their current parentSubaccount
        error("No available subaccount number")
    }

    internal fun getTransferPayloadForIsolatedMarginTrade(orderPayload: HumanReadablePlaceOrderPayload): HumanReadableSubaccountTransferPayload? {
        val trade = stateMachine.state?.input?.trade ?: return null
        val childSubaccountNumber = orderPayload.subaccountNumber
        val childSubaccount = stateMachine.state?.subaccount(childSubaccountNumber) ?: return null
        val market = stateMachine.state?.market(orderPayload.marketId) ?: return null

        val isolatedMarginTransferAmount = MarginCalculator.getIsolatedMarginTransferInAmountForTradeTyped(
            trade,
            subaccount = childSubaccount,
            market,
        )

        if (isolatedMarginTransferAmount != null && isolatedMarginTransferAmount > 0.0) {
            val transferAmount = isolatedMarginTransferAmount.abs().toString()

            return HumanReadableSubaccountTransferPayload(
                senderAddress = accountAddress,
                subaccountNumber = subaccountNumber,
                amount = transferAmount,
                destinationAddress = accountAddress,
                destinationSubaccountNumber = childSubaccountNumber,
            )
        }

        return null
    }

    private fun submitTransaction(
        transactionType: TransactionType,
        transactionPayloadString: String,
        onSubmitTransaction: (() -> Unit?)?,
        transactionCallback: (String?) -> Unit,
        useTransactionQueue: Boolean,
    ) {
        if (useTransactionQueue) {
            transactionQueue.enqueue(
                TransactionParams(
                    transactionType,
                    transactionPayloadString,
                    transactionCallback,
                    onSubmitTransaction,
                ),
            )
        } else {
            onSubmitTransaction?.invoke()
            helper.transaction(transactionType, transactionPayloadString, transactionCallback)
        }
    }

    private fun submitPlaceOrder(
        callback: TransactionCallback,
        payload: HumanReadablePlaceOrderPayload,
        analyticsPayload: IMap<String, Any>?,
        uiClickTimeMs: Double,
        isTriggerOrder: Boolean = false,
        transferPayload: HumanReadableSubaccountTransferPayload? = null,
    ): HumanReadablePlaceOrderPayload {
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)
        val transferPayloadString =
            if (transferPayload != null) Json.encodeToString(transferPayload) else null

        val marketId = payload.marketId
        val position = stateMachine.state?.subaccount(subaccountNumber)?.openPositions?.find { it.id == marketId }
        val positionSize = position?.size?.current

        val isShortTermOrder = when (payload.type) {
            "MARKET" -> true
            "LIMIT" -> {
                when (helper.parser.asString(payload.timeInForce)) {
                    "GTT" -> false
                    else -> true
                }
            }

            else -> false
        }

        val useTransactionQueue = !isShortTermOrder

        val onSubmitOrderTransaction = {
            val submitTimeMs = trackOrderSubmit(uiClickTimeMs, analyticsPayload)
            helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                this.placeOrderRecords.add(
                    PlaceOrderRecord(
                        subaccountNumber,
                        clientId,
                        submitTimeMs,
                        fromSlTpDialog = isTriggerOrder,
                        lastOrderStatus = null,
                    ),
                )
            }
        }

        val orderTransactionCallback = { response: String? ->
            val error = parseTransactionResponse(response)
            trackOrderSubmitted(error, analyticsPayload)
            if (error == null) {
                lastOrderClientId = clientId
            } else {
                val placeOrderRecord = this.placeOrderRecords.firstOrNull {
                    it.clientId == clientId
                }
                this.placeOrderRecords.remove(placeOrderRecord)
            }
            helper.send(
                error,
                callback,
                if (isTriggerOrder) {
                    HumanReadableTriggerOrdersPayload(
                        marketId,
                        positionSize,
                        iListOf(payload),
                        iListOf(),
                    )
                } else {
                    payload
                },
            )
        }

        // If the transfer is successful, place the order
        val isolatedMarginTransactionCallback = { response: String? ->
            val error = parseTransactionResponse(response)
            if (error == null) {
                submitTransaction(
                    TransactionType.PlaceOrder,
                    string,
                    onSubmitOrderTransaction,
                    orderTransactionCallback,
                    useTransactionQueue,
                )
            } else {
                // callback with order payload instead of transfer payload since
                // client shows it as a place order error and needs order client id
                helper.send(error, callback, payload)
            }
        }

        stopWatchingLastOrder()

        if (transferPayloadString != null) {
            // isolated margin order
            submitTransaction(
                TransactionType.SubaccountTransfer,
                transferPayloadString,
                null,
                isolatedMarginTransactionCallback,
                useTransactionQueue = true,
            )
        } else {
            submitTransaction(
                TransactionType.PlaceOrder,
                string,
                onSubmitOrderTransaction,
                orderTransactionCallback,
                useTransactionQueue,
            )
        }

        return payload
    }

    private fun trackOrderClick(
        analyticsPayload: IMap<String, Any?>?,
        analyticsEvent: AnalyticsEvent,
    ): Double {
        val uiClickTimeMs = Clock.System.now().toEpochMilliseconds().toDouble()
        tracking(
            analyticsEvent.rawValue,
            analyticsPayload,
        )
        return uiClickTimeMs
    }

    private fun trackOrderSubmit(
        uiClickTimeMs: Double,
        analyticsPayload: IMap<String, Any>?,
        isCancel: Boolean = false
    ): Double {
        val submitTimeMs = Clock.System.now().toEpochMilliseconds().toDouble()
        val uiDelayTimeMs = submitTimeMs - uiClickTimeMs

        tracking(
            if (isCancel) AnalyticsEvent.TradeCancelOrder.rawValue else AnalyticsEvent.TradePlaceOrder.rawValue,
            ParsingHelper.merge(uiTrackingParams(uiDelayTimeMs), analyticsPayload)?.toIMap(),
        )

        return submitTimeMs
    }

    private fun trackOrderSubmitted(
        error: ParsingError?,
        analyticsPayload: IMap<String, Any>?,
        isCancel: Boolean = false,
    ) {
        if (error != null) {
            tracking(
                if (isCancel) AnalyticsEvent.TradeCancelOrderSubmissionFailed.rawValue else AnalyticsEvent.TradePlaceOrderSubmissionFailed.rawValue,
                ParsingHelper.merge(errorTrackingParams(error), analyticsPayload)?.toIMap(),
            )
        } else {
            tracking(
                if (isCancel) AnalyticsEvent.TradeCancelOrderSubmissionConfirmed.rawValue else AnalyticsEvent.TradePlaceOrderSubmissionConfirmed.rawValue,
                analyticsPayload,
            )
        }
    }

    private fun submitCancelOrder(
        orderId: String,
        marketId: String,
        callback: TransactionCallback,
        payload: HumanReadableCancelOrderPayload,
        analyticsPayload: IMap<String, Any>?,
        uiClickTimeMs: Double,
        fromSlTpDialog: Boolean = false,
    ): HumanReadableCancelOrderPayload {
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)

        val position = stateMachine.state?.subaccount(subaccountNumber)?.openPositions?.find { it.id == marketId }
        val positionSize = position?.size?.current

        stopWatchingLastOrder()

        val isShortTermOrder = payload.orderFlags == SHORT_TERM_ORDER_FLAGS

        submitTransaction(
            TransactionType.CancelOrder,
            string,
            onSubmitTransaction = {
                val submitTimeMs = trackOrderSubmit(uiClickTimeMs, analyticsPayload, true)
                helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.cancelOrderRecords.add(
                        CancelOrderRecord(
                            subaccountNumber,
                            clientId,
                            submitTimeMs,
                            fromSlTpDialog,
                        ),
                    )
                }
            },
            transactionCallback = { response: String? ->
                val error = parseTransactionResponse(response)
                trackOrderSubmitted(error, analyticsPayload, true)
                if (error == null) {
                    this.orderCanceled(orderId)
                } else {
                    val cancelOrderRecord = this.cancelOrderRecords.firstOrNull {
                        it.clientId == clientId
                    }
                    this.cancelOrderRecords.remove(cancelOrderRecord)
                }
                helper.send(
                    error,
                    callback,
                    if (fromSlTpDialog) {
                        HumanReadableTriggerOrdersPayload(
                            marketId,
                            positionSize,
                            iListOf(),
                            iListOf(payload),
                        )
                    } else {
                        payload
                    },
                )
            },
            useTransactionQueue = !isShortTermOrder,
        )

        return payload
    }

    internal fun commitPlaceOrder(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadablePlaceOrderPayload {
        val orderPayload = placeOrderPayload(currentHeight)
        val midMarketPrice = stateMachine.state?.marketOrderbook(orderPayload.marketId)?.midPrice
        val analyticsPayload = analyticsUtils.placeOrderAnalyticsPayload(orderPayload, midMarketPrice, fromSlTpDialog = false, isClosePosition = false)
        val transferPayload = getTransferPayloadForIsolatedMarginTrade(orderPayload)
        val uiClickTimeMs = trackOrderClick(analyticsPayload, AnalyticsEvent.TradePlaceOrderClick)

        return submitPlaceOrder(callback, orderPayload, analyticsPayload, uiClickTimeMs, false, transferPayload)
    }

    internal fun commitClosePosition(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadablePlaceOrderPayload {
        val payload = closePositionPayload(currentHeight)
        val midMarketPrice = stateMachine.state?.marketOrderbook(payload.marketId)?.midPrice
        val analyticsPayload = analyticsUtils.placeOrderAnalyticsPayload(payload, midMarketPrice, fromSlTpDialog = false, isClosePosition = true)
        val uiClickTimeMs = trackOrderClick(analyticsPayload, AnalyticsEvent.TradePlaceOrderClick)

        return submitPlaceOrder(callback, payload, analyticsPayload, uiClickTimeMs)
    }

    internal fun cancelOrder(orderId: String, isOrphanedTriggerOrder: Boolean = false, callback: TransactionCallback): HumanReadableCancelOrderPayload {
        val payload = cancelOrderPayload(orderId)
        val subaccount = stateMachine.state?.subaccount(subaccountNumber)
        val existingOrder = subaccount?.orders?.firstOrNull { it.id == orderId } ?: throw ParsingException(
            ParsingErrorType.MissingRequiredData,
            "no existing order to be cancelled for $orderId",
        )
        val marketId = existingOrder.marketId
        val analyticsPayload = analyticsUtils.cancelOrderAnalyticsPayload(payload, existingOrder, fromSlTpDialog = false, isOrphanedTriggerOrder)
        val uiClickTimeMs = trackOrderClick(analyticsPayload, AnalyticsEvent.TradeCancelOrderClick)

        return submitCancelOrder(orderId, marketId, callback, payload, analyticsPayload, uiClickTimeMs)
    }

    internal fun commitTriggerOrders(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadableTriggerOrdersPayload {
        val payload = triggerOrdersPayload(currentHeight)

        // this is a diff payload that summarizes the actions to be taken
        val analyticsPayload = analyticsUtils.triggerOrdersAnalyticsPayload(payload)
        val uiClickTimeMs = trackOrderClick(analyticsPayload, AnalyticsEvent.TriggerOrderClick)

        payload.cancelOrderPayloads.forEach { cancelPayload ->
            val subaccount = stateMachine.state?.subaccount(subaccountNumber)
            val existingOrder = subaccount?.orders?.firstOrNull { it.id == cancelPayload.orderId }
                ?: throw ParsingException(
                    ParsingErrorType.MissingRequiredData,
                    "no existing order to be cancelled for $cancelPayload.orderId",
                )
            val marketId = existingOrder.marketId
            val cancelOrderAnalyticsPayload = analyticsUtils.cancelOrderAnalyticsPayload(
                cancelPayload,
                existingOrder,
                fromSlTpDialog = true,
            )
            submitCancelOrder(
                cancelPayload.orderId,
                marketId,
                callback,
                cancelPayload,
                cancelOrderAnalyticsPayload,
                uiClickTimeMs,
                true,
            )
        }

        payload.placeOrderPayloads.forEach { placePayload ->
            val midMarketPrice = stateMachine.state?.marketOrderbook(placePayload.marketId)?.midPrice
            val placeOrderAnalyticsPayload = analyticsUtils.placeOrderAnalyticsPayload(
                placePayload,
                midMarketPrice,
                fromSlTpDialog = true,
                isClosePosition = false,
            )
            submitPlaceOrder(callback, placePayload, placeOrderAnalyticsPayload, uiClickTimeMs, true)
        }

        if (payload.cancelOrderPayloads.isEmpty() && payload.placeOrderPayloads.isEmpty()) {
            helper.send(null, callback, payload)
        }

        return payload
    }

    internal fun commitAdjustIsolatedMargin(
        callback: TransactionCallback
    ): HumanReadableSubaccountTransferPayload {
        val payload = adjustIsolatedMarginPayload()
        val transferPayloadString = Json.encodeToString(payload)

        submitTransaction(
            TransactionType.SubaccountTransfer,
            transferPayloadString,
            null,
            transactionCallback = { response: String? ->
                val error = parseTransactionResponse(response)
                helper.send(
                    error,
                    callback,
                    payload,
                )
            },
            false,
        )

        return payload
    }

    internal fun stopWatchingLastOrder() {
        lastOrderClientId = null
    }

    private fun isShortTermOrder(type: String, timeInForce: String?): Boolean {
        return when (type) {
            "MARKET" -> true
            "LIMIT" -> {
                when (timeInForce) {
                    "GTT" -> false
                    else -> true
                }
            }
            else -> false
        }
    }

    @Throws(Exception::class)
    fun placeOrderPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload {
        val trade = stateMachine.state?.input?.trade
        val marketId = trade?.marketId ?: error("marketId is null")
        val summary = trade.summary ?: error("summary is null")
        val clientId = Random.nextInt(0, Int.MAX_VALUE)
        val marginMode = trade.marginMode.rawValue
        val type = trade.type?.rawValue ?: error("type is null")
        val side = trade.side?.rawValue ?: error("side is null")
        val price = summary.payloadPrice ?: error("price is null")
        val triggerPrice =
            if (trade.options?.needsTriggerPrice == true) trade.price?.triggerPrice else null

        val size = summary.size ?: throw Exception("size is null")
        val reduceOnly = if (trade.options?.needsReduceOnly == true) trade.reduceOnly else null
        val postOnly = if (trade.options?.needsPostOnly == true) trade.postOnly else null

        val timeInForce = if (trade.options?.timeInForceOptions != null) {
            when (trade.type) {
                OrderType.Market -> "IOC"
                else -> trade.timeInForce ?: "IOC"
            }
        } else {
            null
        }

        val execution = if (trade.options?.executionOptions != null) {
            trade.execution ?: "DEFAULT"
        } else {
            null
        }

        val goodTilTimeInSeconds = (
            (
                if (trade.options?.goodTilUnitOptions != null) {
                    val timeInterval =
                        GoodTil.duration(trade.goodTil)
                            ?: throw Exception("goodTil is null")
                    timeInterval / 1.seconds
                } else {
                    null
                }
                )
            )?.toInt()

        val goodTilBlock =
            if (isShortTermOrder(trade.type.rawValue, trade.timeInForce)) currentHeight?.plus(SHORT_TERM_ORDER_DURATION) else null

        val marketInfo = marketInfo(marketId)

        val subaccountNumberForOrder = if (marginMode == "ISOLATED") {
            getChildSubaccountNumberForIsolatedMarginTrade(marketId)
        } else {
            subaccountNumber
        }

        return HumanReadablePlaceOrderPayload(
            subaccountNumberForOrder,
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
            goodTilBlock,
            marketInfo,
            currentHeight,
        )
    }

    private fun triggerOrderPayload(triggerOrder: TriggerOrder, marketId: String, currentHeight: Int?): HumanReadablePlaceOrderPayload {
        val clientId = Random.nextInt(0, Int.MAX_VALUE)
        val type = triggerOrder.type?.rawValue ?: error("type is null")
        val side = triggerOrder.side?.rawValue ?: error("side is null")
        val size = triggerOrder.summary?.size ?: error("size is null")

        val price = triggerOrder.summary.price ?: error("summary.price is null")
        val triggerPrice = triggerOrder.price?.triggerPrice ?: error("triggerPrice is null")

        val reduceOnly = true
        val postOnly = false

        // TP/SL orders always have a null timeInForce. IOC/PostOnly/GTD is distinguished by the execution field.
        val timeInForce = null;

        /**
         * TP/SL market orders default to IOC execution.
         * TP/SL limit orders default to GTD (default) execution.
         */
        val execution = when (triggerOrder.type) {
            OrderType.StopMarket, OrderType.TakeProfitMarket -> "IOC"
            OrderType.StopLimit, OrderType.TakeProfitLimit -> "DEFAULT"
            else -> error("invalid triggerOrderType")
        }

        val duration = GoodTil.duration(TradeInputGoodUntil(TRIGGER_ORDER_DEFAULT_DURATION_DAYS, "D")) ?: throw Exception("invalid duration")
        val goodTilTimeInSeconds = (duration / 1.seconds).toInt()
        val goodTilBlock = null

        val marketInfo = marketInfo(marketId)
        val position = stateMachine.state?.subaccount(subaccountNumber)?.openPositions?.find { it.id == marketId } ?: error("no existing position")

        val subaccountNumberForOrder = if (position.marginMode == MarginMode.Isolated) {
            getChildSubaccountNumberForIsolatedMarginTrade(marketId)
        } else {
            subaccountNumber
        }

        return HumanReadablePlaceOrderPayload(
            subaccountNumberForOrder,
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
            goodTilBlock,
            marketInfo,
            currentHeight,
        )
    }

    private fun isTriggerOrderEqualToExistingOrder(triggerOrder: TriggerOrder, existingOrder: SubaccountOrder): Boolean {
        val limitPriceCheck = when (triggerOrder.type) {
            OrderType.StopLimit, OrderType.TakeProfitLimit -> triggerOrder.price?.limitPrice == existingOrder.price
            else -> true
        }
        val size = triggerOrder.summary?.size

        return size == existingOrder.size &&
            triggerOrder.type == existingOrder.type &&
            triggerOrder.side == existingOrder.side &&
            triggerOrder.price?.triggerPrice == existingOrder.triggerPrice &&
            limitPriceCheck
    }

    fun triggerOrdersPayload(currentHeight: Int?): HumanReadableTriggerOrdersPayload {
        val placeOrderPayloads = iMutableListOf<HumanReadablePlaceOrderPayload>()
        val cancelOrderPayloads = iMutableListOf<HumanReadableCancelOrderPayload>()
        val triggerOrders = requireNotNull(stateMachine.state?.input?.triggerOrders) { "triggerOrders input was null" }

        val marketId = requireNotNull(triggerOrders.marketId) { "triggerOrders.marketId was null" }
        val subaccount = stateMachine.state?.subaccount(subaccountNumber)
        val position = subaccount?.openPositions?.find { it.id == marketId }
        val positionSize = position?.size?.current

        fun updateTriggerOrder(triggerOrder: TriggerOrder) {
            // Cases
            // 1. Existing order -> update
            // 2. Existing order -> nothing should be done
            // 3. Existing order -> should delete
            // 4. No existing order -> create a new one
            // 5. No existing order -> nothing should be done

            if (triggerOrder.orderId != null) {
                val existingOrder = subaccount?.orders?.firstOrNull { it.id == triggerOrder.orderId }
                    ?: throw Exception("order is null")
                if (triggerOrder.price?.triggerPrice != null) {
                    if (!isTriggerOrderEqualToExistingOrder(triggerOrder, existingOrder)) {
                        // (1) Existing order -> update
                        cancelOrderPayloads.add(cancelOrderPayload(triggerOrder.orderId))
                        placeOrderPayloads.add(triggerOrderPayload(triggerOrder, marketId, currentHeight))
                    } // (2) Existing order -> nothing changed
                } else {
                    // (3) Existing order -> should delete
                    cancelOrderPayloads.add(cancelOrderPayload(triggerOrder.orderId))
                }
            } else {
                if (triggerOrder.price?.triggerPrice != null) {
                    // (4) No existing order -> create a new one
                    placeOrderPayloads.add(triggerOrderPayload(triggerOrder, marketId, currentHeight))
                } // (5)
            }
        }

        if (triggerOrders.stopLossOrder != null) {
            updateTriggerOrder(triggerOrders.stopLossOrder)
        }

        if (triggerOrders.takeProfitOrder != null) {
            updateTriggerOrder(triggerOrders.takeProfitOrder)
        }

        return HumanReadableTriggerOrdersPayload(marketId, positionSize, placeOrderPayloads, cancelOrderPayloads)
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
        val execution = "DEFAULT"
        val reduceOnly = true
        val postOnly = false
        val goodTilTimeInSeconds = null
        val goodTilBlock = currentHeight?.plus(SHORT_TERM_ORDER_DURATION)
        val marketInfo = marketInfo(marketId)
        val subaccountNumberForPosition = helper.parser.asInt(helper.parser.value(stateMachine.data, "wallet.account.groupedSubaccounts.$subaccountNumber.openPositions.$marketId.childSubaccountNumber")) ?: subaccountNumber

        return HumanReadablePlaceOrderPayload(
            subaccountNumberForPosition,
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
            goodTilBlock,
            marketInfo,
            currentHeight,
        )
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
        val order = subaccount.orders?.firstOrNull { it.id == orderId }
            ?: throw Exception("order is null")
        val type = order.type.rawValue
        val clientId = order.clientId ?: error("clientId is null")
        val orderFlags = order.orderFlags ?: error("orderFlags is null")
        val clobPairId = order.clobPairId ?: error("clobPairId is null")
        val orderSubaccountNumber = order.subaccountNumber ?: error("order subaccountNumber is null")
        val goodTilBlock = order.goodTilBlock
        val goodTilBlockTime = order.goodTilBlockTime

        return HumanReadableCancelOrderPayload(
            orderSubaccountNumber,
            type,
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
            amount,
        )
    }

    @Throws(Exception::class)
    fun withdrawPayload(): HumanReadableWithdrawPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.usdcSize ?: throw Exception("usdcSize is null")
        return HumanReadableWithdrawPayload(
            subaccountNumber,
            amount,
        )
    }

    @Throws(Exception::class)
    fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val size = transfer.size?.size ?: throw Exception("size is null")
        val destinationAddress = transfer.address ?: throw Exception("destination address is null")

        return HumanReadableSubaccountTransferPayload(
            senderAddress = accountAddress,
            subaccountNumber,
            amount = size,
            destinationAddress,
            destinationSubaccountNumber = 0,
        )
    }

    @Throws(Exception::class)
    fun adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload {
        val isolatedMarginAdjustment = stateMachine.state?.input?.adjustIsolatedMargin ?: error("AdjustIsolatedMarginInput is null")
        val amount = isolatedMarginAdjustment.amount ?: error("amount is null")
        val childSubaccountNumber = isolatedMarginAdjustment.childSubaccountNumber ?: error("childSubaccountNumber is null")
        val type = isolatedMarginAdjustment.type

        val recipientSubaccountNumber = if (type == IsolatedMarginAdjustmentType.Add) {
            childSubaccountNumber
        } else {
            subaccountNumber
        }

        val sourceSubaccountNumber = if (type == IsolatedMarginAdjustmentType.Add) {
            subaccountNumber
        } else {
            childSubaccountNumber
        }

        return HumanReadableSubaccountTransferPayload(
            senderAddress = accountAddress,
            subaccountNumber = sourceSubaccountNumber,
            amount,
            destinationAddress = accountAddress,
            destinationSubaccountNumber = recipientSubaccountNumber,
        )
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
            if (firstIndexTransferAfter != -1) {
                val transfersAfter = transfers.subList(firstIndexTransferAfter, transfers.size)
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
                                    trackingParams(interval),
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
    }

    override fun updateTracking(changes: StateChanges) {
        if (changes.changes.contains(Changes.transfers)) {
            parseTransfersToMatchFaucetRecords()
        }
        if (changes.changes.contains(Changes.subaccount)) {
            parseOrdersToMatchPlaceOrdersAndCancelOrders()
            cancelTriggerOrdersWithClosedOrFlippedPositions()
        }
    }

    /**
     * @description Loop through all subaccounts to find childSubaccounts that have funds but no open positions or orders. Initiate a transfer to parentSubaccount.
     */
    private fun reclaimUnutilizedFundsFromChildSubaccounts() {
        val subaccounts = stateMachine.state?.account?.subaccounts ?: return

        val subaccountQuoteBalanceMap = subaccounts.mapValues { subaccount ->
            // If the subaccount is the parentSubaccount, skip
            if (subaccount.value.subaccountNumber == subaccountNumber) {
                return@mapValues 0.0
            }

            val quoteBalance = subaccount.value.quoteBalance?.current ?: 0.0
            val openPositions = subaccount.value.openPositions

            val openOrders = subaccount.value.orders?.filter { order ->
                val status = helper.parser.asString(order.status)
                iListOf(
                    OrderStatus.Open.name,
                    OrderStatus.Pending.name,
                    OrderStatus.Untriggered.name,
                    OrderStatus.PartiallyFilled.name,
                ).contains(status)
            }

            // Only return a quoteBalance if the subaccount has no open positions or orders
            if (openPositions.isNullOrEmpty() && openOrders.isNullOrEmpty() && quoteBalance > 0.0) {
                quoteBalance
            } else {
                0.0
            }
        }.filter {
            it.value > 0.0
        }

        val transferPayloadStrings = iMutableListOf<String>()

        subaccountQuoteBalanceMap.forEach {
            val childSubaccountNumber = helper.parser.asInt(it.key)
            val amountToTransfer = helper.parser.asString(it.value)

            if (childSubaccountNumber == null || amountToTransfer == null) {
                Logger.e { "Child Subaccount Number or Amount to Transfer is null" }
                return@forEach
            }

            val transferPayload = HumanReadableSubaccountTransferPayload(
                senderAddress = accountAddress,
                subaccountNumber = childSubaccountNumber,
                amount = amountToTransfer,
                destinationAddress = accountAddress,
                destinationSubaccountNumber = subaccountNumber,
            )

            val transferPayloadString = Json.encodeToString(transferPayload)
            transferPayloadStrings.add(transferPayloadString)
        }

        recursivelyReclaimChildSubaccountFunds(transferPayloadStrings)
    }

    private fun recursivelyReclaimChildSubaccountFunds(transferPayloadStrings: MutableList<String>) {
        if (transferPayloadStrings.isNotEmpty()) {
            val transferPayloadString = transferPayloadStrings.removeAt(0)
            helper.transaction(TransactionType.SubaccountTransfer, transferPayloadString) { response ->
                val error = parseTransactionResponse(response)
                if (error != null) {
                    emitError(error)
                } else {
                    recursivelyReclaimChildSubaccountFunds(transferPayloadStrings)
                }
            }
        }
    }

    private var reclaimUnutilizedFundsTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private fun pollReclaimUnutilizedFunds() {
        reclaimUnutilizedFundsTimer = null
        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
            this.reclaimUnutilizedFundsTimer = helper.ioImplementations.timer?.schedule(
                (10.seconds).inWholeSeconds.toDouble(),
                null,
            ) {
                reclaimUnutilizedFundsFromChildSubaccounts()
                pollReclaimUnutilizedFunds()
                false
            }
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

            val lastOrderClientId = this.lastOrderClientId
            if (lastOrderClientId != null) {
                lastOrder = stateMachine.findOrder(lastOrderClientId, subaccountNumber)
            }
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
