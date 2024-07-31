package exchange.dydx.abacus.state.v2.supervisor

import abs
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.calculator.TriggerOrdersConstants.TRIGGER_ORDER_DEFAULT_DURATION_DAYS
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputGoodUntil
import exchange.dydx.abacus.output.input.TriggerOrder
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.PlaceOrderMarketInfo
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.utils.GoodTil
import exchange.dydx.abacus.utils.MAX_SUBACCOUNT_NUMBER
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_DURATION
import kollections.iListOf
import kollections.iMutableListOf
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Suppress("TooGenericExceptionThrown")
internal interface SubaccountTransactionPayloadProviderProtocol {
    @Throws(Exception::class)
    fun placeOrderPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload

    @Throws(Exception::class)
    fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload

    fun triggerOrdersPayload(currentHeight: Int?): HumanReadableTriggerOrdersPayload

    @Throws(Exception::class)
    fun closePositionPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload

    @Throws(Exception::class)
    fun adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload

    fun transferPayloadForIsolatedMarginTrade(orderPayload: HumanReadablePlaceOrderPayload): HumanReadableSubaccountTransferPayload?

    @Throws(Exception::class)
    fun depositPayload(): HumanReadableDepositPayload

    @Throws(Exception::class)
    fun withdrawPayload(): HumanReadableWithdrawPayload

    @Throws(Exception::class)
    fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload
}

@Suppress("TooGenericExceptionThrown")
internal class SubaccountTransactionPayloadProvider(
    private val stateMachine: TradingStateMachine,
    private val subaccountNumber: Int,
    private val helper: NetworkHelper,
    private val accountAddress: String,
) : SubaccountTransactionPayloadProviderProtocol {

    @Throws(Exception::class)
    override fun placeOrderPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload {
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
        val sizeInput = trade.size?.input
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
            if (isShortTermOrder(trade.type.rawValue, trade.timeInForce)) {
                currentHeight?.plus(
                    SHORT_TERM_ORDER_DURATION,
                )
            } else {
                null
            }

        val marketInfo = marketInfo(marketId)

        val subaccountNumberForOrder = if (marginMode == "ISOLATED") {
            getChildSubaccountNumberForIsolatedMarginTrade(marketId)
        } else {
            subaccountNumber
        }

        return HumanReadablePlaceOrderPayload(
            subaccountNumber = subaccountNumberForOrder,
            marketId = marketId,
            clientId = clientId,
            type = type,
            side = side,
            price = price,
            triggerPrice = triggerPrice,
            size = size,
            sizeInput = sizeInput,
            reduceOnly = reduceOnly,
            postOnly = postOnly,
            timeInForce = timeInForce,
            execution = execution,
            goodTilTimeInSeconds = goodTilTimeInSeconds,
            goodTilBlock = goodTilBlock,
            marketInfo = marketInfo,
            currentHeight = currentHeight,
        )
    }

    @Throws(Exception::class)
    override fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload {
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
            subaccountNumber = orderSubaccountNumber,
            type = type,
            orderId = orderId,
            clientId = clientId,
            orderFlags = orderFlags,
            clobPairId = clobPairId,
            goodTilBlock = goodTilBlock,
            goodTilBlockTime = goodTilBlockTime,
        )
    }

    override fun transferPayloadForIsolatedMarginTrade(orderPayload: HumanReadablePlaceOrderPayload): HumanReadableSubaccountTransferPayload? {
        val trade = stateMachine.state?.input?.trade ?: return null
        val childSubaccountNumber = orderPayload.subaccountNumber
        val childSubaccount = stateMachine.state?.subaccount(childSubaccountNumber) ?: return null
        val market = stateMachine.state?.market(orderPayload.marketId) ?: return null

        val isolatedMarginTransferAmount = MarginCalculator.getIsolatedMarginTransferInAmountForTrade(
            trade = trade,
            subaccount = childSubaccount,
            market = market,
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

    override fun triggerOrdersPayload(currentHeight: Int?): HumanReadableTriggerOrdersPayload {
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
    override fun closePositionPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload {
        val closePosition = stateMachine.state?.input?.closePosition
        val marketId = closePosition?.marketId ?: throw Exception("marketId is null")
        val summary = closePosition.summary ?: throw Exception("summary is null")
        val clientId = Random.nextInt(0, Int.MAX_VALUE)
        val side = closePosition.side?.rawValue ?: throw Exception("side is null")
        val price = summary.payloadPrice ?: throw Exception("price is null")
        val size = summary.size ?: throw Exception("size is null")
        val sizeInput = null
        val timeInForce = "IOC"
        val execution = "DEFAULT"
        val reduceOnly = true
        val postOnly = false
        val goodTilTimeInSeconds = null
        val goodTilBlock = currentHeight?.plus(SHORT_TERM_ORDER_DURATION)
        val marketInfo = marketInfo(marketId)
        val subaccountNumberForPosition = helper.parser.asInt(helper.parser.value(stateMachine.data, "wallet.account.groupedSubaccounts.$subaccountNumber.openPositions.$marketId.childSubaccountNumber")) ?: subaccountNumber

        return HumanReadablePlaceOrderPayload(
            subaccountNumber = subaccountNumberForPosition,
            marketId = marketId,
            clientId = clientId,
            type = "MARKET",
            side = side,
            price = price,
            triggerPrice = null,
            size = size,
            sizeInput = sizeInput,
            reduceOnly = reduceOnly,
            postOnly = postOnly,
            timeInForce = timeInForce,
            execution = execution,
            goodTilTimeInSeconds = goodTilTimeInSeconds,
            goodTilBlock = goodTilBlock,
            marketInfo = marketInfo,
            currentHeight = currentHeight,
        )
    }

    @Throws(Exception::class)
    override fun adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload {
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
            amount = amount,
            destinationAddress = accountAddress,
            destinationSubaccountNumber = recipientSubaccountNumber,
        )
    }

    @Throws(Exception::class)
    override fun depositPayload(): HumanReadableDepositPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.size ?: throw Exception("size is null")
        return HumanReadableDepositPayload(
            subaccountNumber = subaccountNumber,
            amount = amount,
        )
    }

    @Throws(Exception::class)
    override fun withdrawPayload(): HumanReadableWithdrawPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.usdcSize ?: throw Exception("usdcSize is null")
        return HumanReadableWithdrawPayload(
            subaccountNumber = subaccountNumber,
            amount = amount,
        )
    }

    @Throws(Exception::class)
    override fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val size = transfer.size?.size ?: throw Exception("size is null")
        val destinationAddress = transfer.address ?: throw Exception("destination address is null")

        return HumanReadableSubaccountTransferPayload(
            senderAddress = accountAddress,
            subaccountNumber = subaccountNumber,
            amount = size,
            destinationAddress = destinationAddress,
            destinationSubaccountNumber = 0,
        )
    }

    private fun triggerOrderPayload(triggerOrder: TriggerOrder, marketId: String, currentHeight: Int?): HumanReadablePlaceOrderPayload {
        val clientId = Random.nextInt(0, Int.MAX_VALUE)
        val type = triggerOrder.type?.rawValue ?: error("type is null")
        val side = triggerOrder.side?.rawValue ?: error("side is null")
        val size = triggerOrder.summary?.size ?: error("size is null")
        // TP/SL orders always have a null sizeInput. Users can only input by asset size.
        val sizeInput = null

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
            subaccountNumber = subaccountNumberForOrder,
            marketId = marketId,
            clientId = clientId,
            type = type,
            side = side,
            price = price,
            triggerPrice = triggerPrice,
            size = size,
            sizeInput = sizeInput,
            reduceOnly = reduceOnly,
            postOnly = postOnly,
            timeInForce = timeInForce,
            execution = execution,
            goodTilTimeInSeconds = goodTilTimeInSeconds,
            goodTilBlock = goodTilBlock,
            marketInfo = marketInfo,
            currentHeight = currentHeight,
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

    private fun marketInfo(marketId: String): PlaceOrderMarketInfo? {
        val market = stateMachine.state?.market(marketId) ?: return null
        val v4config = market.configs?.v4 ?: return null

        return PlaceOrderMarketInfo(
            clobPairId = v4config.clobPairId,
            atomicResolution = v4config.atomicResolution,
            stepBaseQuantums = v4config.stepBaseQuantums,
            quantumConversionExponent = v4config.quantumConversionExponent,
            subticksPerTick = v4config.subticksPerTick,
        )
    }

    /**
     * @description Get the childSubaccount number that is available for the given marketId
     * @param marketId
     */
    private fun getChildSubaccountNumberForIsolatedMarginTrade(marketId: String): Int {
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
                    val equity = subaccounts.get("subaccountNumberToCheck")?.equity?.current ?: 0.0
                    if (availableSubaccountNumber == subaccountNumber && equity == 0.0) {
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
}
