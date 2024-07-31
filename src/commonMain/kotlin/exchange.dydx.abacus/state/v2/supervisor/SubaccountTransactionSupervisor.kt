package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.account.TransferRecordType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.state.manager.CancelOrderRecord
import exchange.dydx.abacus.state.manager.FaucetRecord
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.IsolatedPlaceOrderRecord
import exchange.dydx.abacus.state.manager.PlaceOrderRecord
import exchange.dydx.abacus.state.manager.TransactionParams
import exchange.dydx.abacus.state.manager.TransactionQueue
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.findOrder
import exchange.dydx.abacus.state.model.orderCanceled
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CONDITIONAL_ORDER_FLAGS
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_FLAGS
import exchange.dydx.abacus.utils.iMapOf
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIMap
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class SubaccountTransactionSupervisor(
    private val stateMachine: TradingStateMachine,
    private val helper: NetworkHelper,
    private val analyticsUtils: AnalyticsUtils,
    private val accountAddress: String,
    private val subaccountNumber: Int,
    private val payloadProvider: SubaccountTransactionPayloadProviderProtocol,
) {
    internal val transactionQueue = TransactionQueue(helper::transaction)

    private val transactionTracker: SubaccountTransactionTrackerProtocol = SubaccountTransactionTracker(helper)

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

    private val pendingIsolatedOrderRecords: IMutableList<IsolatedPlaceOrderRecord> = iMutableListOf()

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

    private val cancelingOrphanedTriggerOrders = mutableSetOf<String>()
    private val reclaimingChildSubaccountNumbers = mutableSetOf<Int>()

    private fun fromSlTpDialogParams(fromSlTpDialog: Boolean): IMap<String, Any> {
        return iMapOf(
            "fromSlTpDialog" to fromSlTpDialog,
        )
    }
    private fun didSetPlaceOrderRecords() {
        parseOrdersToMatchPlaceOrdersAndCancelOrders()
    }

    private fun didSetCancelOrderRecords() {
        parseOrdersToMatchPlaceOrdersAndCancelOrders()
    }

    internal fun commitPlaceOrder(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadablePlaceOrderPayload {
        val orderPayload = payloadProvider.placeOrderPayload(currentHeight)
        val midMarketPrice = stateMachine.state?.marketOrderbook(orderPayload.marketId)?.midPrice
        val analyticsPayload = analyticsUtils.placeOrderAnalyticsPayload(orderPayload, midMarketPrice, fromSlTpDialog = false, isClosePosition = false)
        val transferPayload = payloadProvider.transferPayloadForIsolatedMarginTrade(orderPayload)
        val uiClickTimeMs = transactionTracker.trackOrderClick(analyticsPayload, AnalyticsEvent.TradePlaceOrderClick)

        return submitPlaceOrder(callback, orderPayload, analyticsPayload, uiClickTimeMs, false, transferPayload)
    }

    internal fun commitClosePosition(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadablePlaceOrderPayload {
        val payload = payloadProvider.closePositionPayload(currentHeight)
        val midMarketPrice = stateMachine.state?.marketOrderbook(payload.marketId)?.midPrice
        val analyticsPayload = analyticsUtils.placeOrderAnalyticsPayload(payload, midMarketPrice, fromSlTpDialog = false, isClosePosition = true)
        val uiClickTimeMs = transactionTracker.trackOrderClick(analyticsPayload, AnalyticsEvent.TradePlaceOrderClick)

        return submitPlaceOrder(callback, payload, analyticsPayload, uiClickTimeMs)
    }

    internal fun cancelOrder(orderId: String, isOrphanedTriggerOrder: Boolean = false, callback: TransactionCallback): HumanReadableCancelOrderPayload {
        val payload = payloadProvider.cancelOrderPayload(orderId)
        val subaccount = stateMachine.state?.subaccount(subaccountNumber)
        val existingOrder = subaccount?.orders?.firstOrNull { it.id == orderId } ?: throw ParsingException(
            ParsingErrorType.MissingRequiredData,
            "no existing order to be cancelled for $orderId",
        )
        val marketId = existingOrder.marketId
        val analyticsPayload = analyticsUtils.cancelOrderAnalyticsPayload(payload, existingOrder, fromSlTpDialog = false, isOrphanedTriggerOrder)
        val uiClickTimeMs = transactionTracker.trackOrderClick(analyticsPayload, AnalyticsEvent.TradeCancelOrderClick)

        return submitCancelOrder(orderId, marketId, callback, payload, analyticsPayload, uiClickTimeMs)
    }

    internal fun commitTriggerOrders(
        currentHeight: Int?,
        callback: TransactionCallback
    ): HumanReadableTriggerOrdersPayload {
        val payload = payloadProvider.triggerOrdersPayload(currentHeight)

        // this is a diff payload that summarizes the actions to be taken
        val analyticsPayload = analyticsUtils.triggerOrdersAnalyticsPayload(payload)
        val uiClickTimeMs = transactionTracker.trackOrderClick(analyticsPayload, AnalyticsEvent.TriggerOrderClick)

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
                orderId = cancelPayload.orderId,
                marketId = marketId,
                callback = callback,
                payload = cancelPayload,
                analyticsPayload = cancelOrderAnalyticsPayload,
                uiClickTimeMs = uiClickTimeMs,
                fromSlTpDialog = true,
            )
        }

        payload.placeOrderPayloads.forEach { placePayload ->
            val midMarketPrice = stateMachine.state?.marketOrderbook(placePayload.marketId)?.midPrice
            val placeOrderAnalyticsPayload = analyticsUtils.placeOrderAnalyticsPayload(
                payload = placePayload,
                midMarketPrice = midMarketPrice,
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
        val payload = payloadProvider.adjustIsolatedMarginPayload()
        val transferPayloadString = Json.encodeToString(payload)

        submitTransaction(
            transactionType = TransactionType.SubaccountTransfer,
            transactionPayloadString = transferPayloadString,
            onSubmitTransaction = null,
            transactionCallback = { response: String? ->
                val error = parseTransactionResponse(response)
                helper.send(
                    error = error,
                    callback = callback,
                    data = payload,
                )
            },
            useTransactionQueue = false,
        )

        return payload
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

    internal fun cancelTriggerOrdersWithClosedOrFlippedPositions() {
        val subaccount = stateMachine.state?.subaccount(subaccountNumber) ?: return
        val cancelableTriggerOrders = subaccount.orders?.filter { order ->
            val isConditionalOrder = order.orderFlags == CONDITIONAL_ORDER_FLAGS
            val isReduceOnly = order.reduceOnly
            val isActiveOrder =
                (order.status == OrderStatus.Untriggered || order.status == OrderStatus.Open)
            isConditionalOrder && isReduceOnly && isActiveOrder
        } ?: return

        cancelableTriggerOrders.forEach { order ->
            if (order.id !in this.cancelingOrphanedTriggerOrders) {
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

    internal fun parseOrdersToMatchPlaceOrdersAndCancelOrders() {
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
                        transactionTracker.trackingParams(interval),
                        fromSlTpDialogParams(placeOrderRecord.fromSlTpDialog),
                    )
                    val analyticsPayload = ParsingHelper.merge(extraParams, orderAnalyticsPayload)?.toIMap()

                    if (placeOrderRecord.lastOrderStatus != order.status) {
                        // when order is first indexed
                        if (placeOrderRecord.lastOrderStatus == null) {
                            transactionTracker.tracking(
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

                        transactionTracker.tracking(orderStatusChangeEvent.rawValue, analyticsPayload)

                        when (order.status) {
                            // order reaches final state, can remove / skip further tracking
                            OrderStatus.Canceled, OrderStatus.PartiallyCanceled, OrderStatus.Filled -> {
                                placeOrderRecords.remove(placeOrderRecord)
                            }
                            else -> {}
                        }
                        placeOrderRecord.lastOrderStatus = order.status
                    }
                }

                val cancelOrderRecord = cancelOrderRecords.firstOrNull {
                    it.clientId == order.clientId
                }
                if (cancelOrderRecord != null) {
                    val interval = Clock.System.now().toEpochMilliseconds()
                        .toDouble() - cancelOrderRecord.timestampInMilliseconds
                    val extraParams = ParsingHelper.merge(
                        transactionTracker.trackingParams(interval),
                        fromSlTpDialogParams(cancelOrderRecord.fromSlTpDialog),
                    )
                    transactionTracker.tracking(
                        AnalyticsEvent.TradeCancelOrderConfirmed.rawValue,
                        ParsingHelper.merge(
                            extraParams,
                            orderAnalyticsPayload,
                        )?.toIMap(),
                    )
                    cancelOrderRecords.remove(cancelOrderRecord)
                }
            }
        }
    }

    /**
     * @description Loop through all subaccounts to find childSubaccounts that have funds but no open positions or orders. Initiate a transfer to parentSubaccount.
     */
    internal fun reclaimUnutilizedFundsFromChildSubaccounts() {
        val subaccounts = stateMachine.state?.account?.subaccounts ?: return

        val subaccountQuoteBalanceMap = subaccounts.mapValues { subaccount ->
            val currentSubaccountNumber = subaccount.value.subaccountNumber
            // If the current subaccount is the parent, or if it's is not a child of parent subaccount 0 (reserved for FE), skip
            if (currentSubaccountNumber == subaccountNumber || currentSubaccountNumber < NUM_PARENT_SUBACCOUNTS || currentSubaccountNumber % NUM_PARENT_SUBACCOUNTS != 0) {
                return@mapValues 0.0
            }

            val quoteBalance = subaccount.value.quoteBalance?.current ?: 0.0
            val openPositions = subaccount.value.openPositions
            val hasIndexedOpenOrder = subaccount.value.orders?.any { order ->
                val status = helper.parser.asString(order.status)
                iListOf(
                    OrderStatus.Open.name,
                    OrderStatus.Pending.name,
                    OrderStatus.Untriggered.name,
                    OrderStatus.PartiallyFilled.name,
                ).contains(status)
            } ?: false

            // pendingIsolatedOrderRecords hold isolated orders that have been placed (i.e. transferring funds to child subaccount -> confirmed)
            // placeOrderRecords hold orders that have been placed and not indexed
            // checking both records to guard against transferring funds out of child subaccount when there's a pending isolated order in that subaccount
            val isTransferringToChildSubaccount = this.pendingIsolatedOrderRecords.any {
                it.destinationSubaccountNumber == subaccount.value.subaccountNumber
            }
            val isPlacingOrderForSubaccount = this.placeOrderRecords.any {
                it.destinationSubaccountNumber == subaccount.value.subaccountNumber &&
                    it.lastOrderStatus == null // i.e. not indexed, we let `hasIndexedOpenOrder` be source of truth once order is indexed
            }
            val isPlacingIsolatedOrderInChildSubaccount = isTransferringToChildSubaccount || isPlacingOrderForSubaccount

            // Only return a quoteBalance if the subaccount has no open positions or orders
            if (openPositions.isNullOrEmpty() && !hasIndexedOpenOrder && !isPlacingIsolatedOrderInChildSubaccount && quoteBalance > 0.0) {
                quoteBalance
            } else {
                0.0
            }
        }.filter {
            it.value > 0.0
        }

        val transferPayloads = iMutableListOf<HumanReadableSubaccountTransferPayload>()

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

            if (childSubaccountNumber !in this.reclaimingChildSubaccountNumbers) {
                this.reclaimingChildSubaccountNumbers.add(childSubaccountNumber)
                transferPayloads.add(transferPayload)
            }
        }

        transferPayloads.forEach { transferPayload ->
            val transferPayloadString = Json.encodeToString(transferPayload)
            val transactionCallback = { response: String? ->
                this.reclaimingChildSubaccountNumbers.remove(transferPayload.subaccountNumber)
                val error = parseTransactionResponse(response)
                if (error != null) {
                    emitError(error)
                }
            }
            submitTransaction(
                transactionType = TransactionType.SubaccountTransfer,
                transactionPayloadString = transferPayloadString,
                onSubmitTransaction = null,
                transactionCallback = transactionCallback,
                useTransactionQueue = true,
            )
        }
    }

    internal fun parseTransfersToMatchFaucetRecords(faucetRecords: IMutableList<FaucetRecord>) {
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
                                transactionTracker.tracking(
                                    AnalyticsEvent.TransferFaucetConfirmed.rawValue,
                                    transactionTracker.trackingParams(interval),
                                )
                                faucetRecords.remove(faucet)
                                break
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    internal fun stopWatchingLastOrder() {
        lastOrderClientId = null
    }

    internal fun updateLastOrder() {
        val lastOrderClientId = this.lastOrderClientId
        if (lastOrderClientId != null) {
            lastOrder = stateMachine.findOrder(lastOrderClientId, subaccountNumber)
        }
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
                    type = transactionType,
                    payload = transactionPayloadString,
                    callback = transactionCallback,
                    onSubmit = onSubmitTransaction,
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
            val submitTimeMs = transactionTracker.trackOrderSubmit(uiClickTimeMs, analyticsPayload)
            helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                this.placeOrderRecords.add(
                    PlaceOrderRecord(
                        subaccountNumber = subaccountNumber,
                        clientId = clientId,
                        timestampInMilliseconds = submitTimeMs,
                        fromSlTpDialog = isTriggerOrder,
                        lastOrderStatus = null,
                        destinationSubaccountNumber = transferPayload?.destinationSubaccountNumber,
                    ),
                )
            }
        }

        val orderTransactionCallback = { response: String? ->
            val error = parseTransactionResponse(response)
            transactionTracker.trackOrderSubmitted(error, analyticsPayload)
            if (error == null) {
                lastOrderClientId = clientId
            } else {
                val placeOrderRecord = this.placeOrderRecords.firstOrNull {
                    it.clientId == clientId
                }
                this.placeOrderRecords.remove(placeOrderRecord)
            }
            // stop tracking pending isolated order from records since it's been confirmed
            val isolatedOrderRecord = this.pendingIsolatedOrderRecords.firstOrNull {
                it.clientId == clientId
            }
            this.pendingIsolatedOrderRecords.remove(isolatedOrderRecord)

            helper.send(
                error,
                callback,
                if (isTriggerOrder) {
                    HumanReadableTriggerOrdersPayload(
                        marketId = marketId,
                        positionSize = positionSize,
                        placeOrderPayloads = iListOf(payload),
                        cancelOrderPayloads = iListOf(),
                    )
                } else {
                    payload
                },
            )
        }

        val onSubmitIsolatedTransferTransaction = {
            // track the pending isolated order, which is watched by reclaimUnutilizedFundsFromChildSubaccounts
            transferPayload?.destinationSubaccountNumber?.let {
                helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.pendingIsolatedOrderRecords.add(
                        IsolatedPlaceOrderRecord(
                            subaccountNumber = subaccountNumber,
                            clientId = clientId,
                            destinationSubaccountNumber = it,
                        ),
                    )
                }
            }
        }

        // If the transfer is successful, place the order
        val isolatedMarginTransactionCallback = { response: String? ->
            val error = parseTransactionResponse(response)
            if (error == null) {
                submitTransaction(
                    transactionType = TransactionType.PlaceOrder,
                    transactionPayloadString = string,
                    onSubmitTransaction = onSubmitOrderTransaction,
                    transactionCallback = orderTransactionCallback,
                    useTransactionQueue = useTransactionQueue,
                )
            } else {
                // remove pending isolated order since it will not be placed
                val isolatedOrderRecord = this.pendingIsolatedOrderRecords.firstOrNull {
                    it.clientId == clientId
                }
                this.pendingIsolatedOrderRecords.remove(isolatedOrderRecord)
                // callback with order payload instead of transfer payload since
                // client shows it as a place order error and needs order client id
                helper.send(error, callback, payload)
            }
        }

        stopWatchingLastOrder()

        if (transferPayloadString != null) {
            // isolated margin order
            submitTransaction(
                transactionType = TransactionType.SubaccountTransfer,
                transactionPayloadString = transferPayloadString,
                onSubmitTransaction = onSubmitIsolatedTransferTransaction,
                transactionCallback = isolatedMarginTransactionCallback,
                useTransactionQueue = true,
            )
        } else {
            submitTransaction(
                transactionType = TransactionType.PlaceOrder,
                transactionPayloadString = string,
                onSubmitTransaction = onSubmitOrderTransaction,
                transactionCallback = orderTransactionCallback,
                useTransactionQueue = useTransactionQueue,
            )
        }

        return payload
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
            transactionType = TransactionType.CancelOrder,
            transactionPayloadString = string,
            onSubmitTransaction = {
                val submitTimeMs = transactionTracker.trackOrderSubmit(uiClickTimeMs, analyticsPayload, true)
                helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.cancelOrderRecords.add(
                        CancelOrderRecord(
                            subaccountNumber = subaccountNumber,
                            clientId = clientId,
                            timestampInMilliseconds = submitTimeMs,
                            fromSlTpDialog = fromSlTpDialog,
                        ),
                    )
                }
            },
            transactionCallback = { response: String? ->
                val error = parseTransactionResponse(response)
                transactionTracker.trackOrderSubmitted(error, analyticsPayload, true)
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
                            marketId = marketId,
                            positionSize = positionSize,
                            placeOrderPayloads = iListOf(),
                            cancelOrderPayloads = iListOf(payload),
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

    private fun cancelTriggerOrder(orderId: String) {
        this.cancelingOrphanedTriggerOrders.add(orderId)
        cancelOrder(
            orderId = orderId,
            isOrphanedTriggerOrder = true,
            callback = { _, _, _ -> this.cancelingOrphanedTriggerOrders.remove(orderId) },
        )
    }

    private fun emitError(error: ParsingError) {
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.stateNotification?.errorsEmitted(iListOf(error))
            helper.dataNotification?.errorsEmitted(iListOf(error))
        }
    }

    private fun parseTransactionResponse(response: String?): ParsingError? {
        return helper.parseTransactionResponse(response)
    }
}
