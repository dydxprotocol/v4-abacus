package exchange.dydx.abacus.calculator

import abs
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.PerpetualMarketType
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.TradeInput
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.MAX_LEVERAGE_BUFFER_PERCENT
import exchange.dydx.abacus.utils.MAX_SUBACCOUNT_NUMBER
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.Numeric
import kollections.iListOf
import kotlin.math.max
import kotlin.math.min

internal object MarginCalculator {
    fun findExistingPosition(
        account: InternalAccountState?,
        marketId: String?,
        subaccountNumber: Int,
    ): InternalPerpetualPosition? {
        val position = account?.groupedSubaccounts?.get(subaccountNumber)?.openPositions?.get(marketId)
        return if (
            (position?.size ?: 0.0) != 0.0
        ) {
            position
        } else {
            null
        }
    }

    fun findExistingPositionDeprecated(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        marketId: String?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val position = parser.asNativeMap(
            parser.value(
                account,
                "groupedSubaccounts.$subaccountNumber.openPositions.$marketId",
            ),
        )

        return if (
            position != null &&
            (parser.asDouble(parser.value(position, "size.current")) ?: 0.0) != 0.0
        ) {
            position
        } else {
            null
        }
    }

    fun findExistingOrder(
        account: InternalAccountState,
        marketId: String?,
        subaccountNumber: Int,
    ): SubaccountOrder? {
        val orders = account.groupedSubaccounts[subaccountNumber]?.orders
        return orders?.firstOrNull {
            it.marketId == marketId && it.status.isOpen
        }
    }

    fun findExistingOrderDeprecated(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        marketId: String?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val orders = parser.asNativeMap(
            parser.value(
                account,
                "groupedSubaccounts.$subaccountNumber.orders",
            ),
        )
        val order = orders?.entries?.firstOrNull {
            val orderMarketId = parser.asString(parser.value(it.value, "marketId"))
            val orderStatus = parser.asString(parser.value(it.value, "status"))
            orderMarketId == marketId && listOf("OPEN", "PENDING", "UNTRIGGERED", "PARTIALLY_FILLED").contains(orderStatus)
        }

        return if (order != null) order.value as Map<String, Any> else null
    }

    private fun hasExistingOrder(
        subaccount: InternalSubaccountState?,
        marketId: String?
    ): Boolean {
        return subaccount?.orders?.any { order ->
            order.marketId == marketId && order.status.isOpen
        } ?: false
    }

    private fun hasExistingOrderDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        marketId: String?
    ): Boolean {
        val orders = parser.asNativeMap(parser.value(subaccount, "orders"))
        return orders?.entries?.any {
            val orderMarketId = parser.asString(parser.value(it.value, "marketId"))
            val orderStatus = parser.asString(parser.value(it.value, "status"))
            orderMarketId == marketId && listOf("OPEN", "PENDING", "UNTRIGGERED", "PARTIALLY_FILLED").contains(orderStatus)
        } ?: false
    }

    fun findExistingMarginMode(
        account: InternalAccountState,
        marketId: String?,
        subaccountNumber: Int,
    ): MarginMode? {
        val position = findExistingPosition(account, marketId, subaccountNumber)
        if (position != null) {
            // return if (position.equity != 0.0) "ISOLATED" else "CROSS"
            return position.marginMode
        }

        val openOrder = findExistingOrder(account, marketId, subaccountNumber)
        return if (openOrder != null) {
            if (openOrder.subaccountNumber != subaccountNumber) {
                MarginMode.Isolated
            } else {
                MarginMode.Cross
            }
        } else {
            null
        }
    }

    fun findExistingMarginModeDeprecated(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        marketId: String?,
        subaccountNumber: Int,
    ): String? {
        val position = findExistingPositionDeprecated(parser, account, marketId, subaccountNumber)
        if (position != null) {
            return if (position["equity"] != null) "ISOLATED" else "CROSS"
        }

        val openOrder = findExistingOrderDeprecated(parser, account, marketId, subaccountNumber)
        if (openOrder != null) {
            return if ((
                    parser.asInt(
                        parser.value(
                            openOrder,
                            "subaccountNumber",
                        ),
                    ) ?: subaccountNumber
                    ) != subaccountNumber
            ) {
                "ISOLATED"
            } else {
                "CROSS"
            }
        }

        return null
    }

    fun findMarketMarginMode(
        market: PerpetualMarket?,
    ): MarginMode {
        val marketType = market?.configs?.perpetualMarketType
        return when (marketType) {
            PerpetualMarketType.ISOLATED -> return MarginMode.Isolated
            PerpetualMarketType.CROSS -> return MarginMode.Cross
            else -> MarginMode.Cross
        }
    }

    fun findMarketMarginModeDeprecated(
        parser: ParserProtocol,
        market: Map<String, Any>?,
    ): String {
        val marginMode = parser.asString(
            parser.value(
                market,
                "configs.perpetualMarketType",
            ),
        )
        return marginMode ?: "CROSS"
    }

    fun selectableMarginModes(
        account: InternalAccountState,
        market: InternalMarketState?,
        subaccountNumber: Int,
    ): Boolean {
        val marketId = market?.perpetualMarket?.id
        val existingMarginMode = findExistingMarginMode(account, marketId, subaccountNumber)
        return if (existingMarginMode != null) {
            false
        } else if (marketId != null) {
            findMarketMarginMode(market.perpetualMarket) == MarginMode.Cross
        } else {
            true
        }
    }

    fun selectableMarginModesDeprecated(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        market: Map<String, Any>?,
        subaccountNumber: Int,
    ): Boolean {
        val marketId = parser.asString(market?.get("id"))
        val existingMarginMode =
            findExistingMarginModeDeprecated(parser, account, marketId, subaccountNumber)
        return if (existingMarginMode != null) {
            false
        } else if (marketId != null) {
            findMarketMarginModeDeprecated(parser, market) == "CROSS"
        } else {
            true
        }
    }

    fun getChildSubaccountNumberForIsolatedMarginTrade(
        parser: ParserProtocol,
        subaccounts: Map<Int, InternalSubaccountState>,
        subaccountNumber: Int,
        marketId: String,
    ): Int {
        // FE only supports subaccounts that are related to the "main" account (i.e. subaccount 0) and its children
        // If there are other utilized subaccounts (e.g. subaccount 1 or 129), ignore them as candidates
        val relevantSubaccounts = subaccounts.filterKeys {
                key ->
            parser.asInt(key)?.let { it % NUM_PARENT_SUBACCOUNTS == 0 } ?: false
        }
        val utilizedSubaccountsMarketIdMap = relevantSubaccounts.mapValues {
            val subaccount = it.value
            val openPositions = subaccount.openPositions
            val openOrders = subaccount.orders?.filter { order ->
                order.status.isOpen
            }

            val positionMarketIds = openPositions?.values?.mapNotNull { position ->
                position.market
            } ?: iListOf()

            val openOrderMarketIds = openOrders?.map { order ->
                order.marketId
            } ?: iListOf()

            // Return the combined list of marketIds w/o duplicates
            (positionMarketIds + openOrderMarketIds).toSet()
        }

        // Check if an existing childSubaccount is available to use for Isolated Margin Trade
        var availableSubaccountNumber = subaccountNumber
        utilizedSubaccountsMarketIdMap.forEach { (key, marketIds) ->
            val subaccountNumberToCheck = parser.asInt(key)
            if (subaccountNumberToCheck == null) {
                Logger.e { "Invalid subaccount number: $key" }
                return@forEach
            }
            if (subaccountNumberToCheck != subaccountNumber) {
                if (marketIds.contains(marketId) && marketIds.size <= 1) {
                    return subaccountNumberToCheck
                } else if (marketIds.isEmpty()) {
                    // Check if subaccount equity is 0 so that funds are moved to a clean account if reclaimUnutilizedChildSubaccountFunds has not been called yet
                    val equity = subaccounts[subaccountNumberToCheck]?.calculated?.get(CalculationPeriod.current)?.equity ?: 0.0
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
        val existingSubaccountNumbers = utilizedSubaccountsMarketIdMap.keys
        for (offset in NUM_PARENT_SUBACCOUNTS..MAX_SUBACCOUNT_NUMBER step NUM_PARENT_SUBACCOUNTS) {
            val tentativeSubaccountNumber = offset + subaccountNumber
            if (!existingSubaccountNumbers.contains(tentativeSubaccountNumber)) {
                return tentativeSubaccountNumber
            }
        }

        // User has reached the maximum number of childSubaccounts for their current parentSubaccount
        error("No available subaccount number")
    }

    /**
     * @description Get the childSubaccount number that is available for the given marketId
     * @param parser ParserProtocol
     * @param account Account data (data.wallet.account)
     * @param subaccountNumber Parent subaccount number
     * @param tradeInput Trade input data (data.input.trade)
     */
    fun getChildSubaccountNumberForIsolatedMarginTradeDeprecated(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        subaccountNumber: Int,
        tradeInput: Map<String, Any>?
    ): Int? {
        val marginMode = parser.asString(tradeInput?.get("marginMode")) ?: return null
        if (marginMode != "ISOLATED") {
            return subaccountNumber
        }
        val marketId = parser.asString(tradeInput?.get("marketId")) ?: return null
        val subaccounts = parser.asNativeMap(account?.get("subaccounts")) ?: return null

        // FE only supports subaccounts that are related to the "main" account (i.e. subaccount 0) and its children
        // If there are other utilized subaccounts (e.g. subaccount 1 or 129), ignore them as candidates
        val relevantSubaccounts = subaccounts.filterKeys { key -> parser.asInt(key)?.let { it % NUM_PARENT_SUBACCOUNTS == 0 } ?: false }
        val utilizedSubaccountsMarketIdMap = relevantSubaccounts.mapValues {
            val openPositions = parser.asNativeMap(parser.value(it.value, "openPositions"))
            val openOrders = parser.asNativeMap(parser.value(it.value, "orders"))?.filter {
                val order = parser.asMap(it.value)
                val status = parser.asString(parser.value(order, "status"))
                status == "OPEN" || status == "PENDING" || status == "UNTRIGGERED" || status == "PARTIALLY_FILLED"
            }

            val positionMarketIds = openPositions?.map { position ->
                val positionObj = parser.asMap(position.value)
                val positionMarketId = parser.asString(parser.value(positionObj, "id"))
                positionMarketId
            }?.filterNotNull() ?: iListOf()

            val openOrderMarketIds = openOrders?.map { order ->
                val orderObj = parser.asMap(order.value)
                val orderMarketId = parser.asString(parser.value(orderObj, "marketId"))
                orderMarketId
            }?.filterNotNull() ?: iListOf()

            // Return the combined list of marketIds w/o duplicates
            (positionMarketIds + openOrderMarketIds).toSet()
        }

        // Check if an existing childSubaccount is available to use for Isolated Margin Trade
        var availableSubaccountNumber = subaccountNumber
        utilizedSubaccountsMarketIdMap.forEach { (key, marketIds) ->
            val subaccountNumberToCheck = parser.asInt(key)
            if (subaccountNumberToCheck == null) {
                Logger.e { "Invalid subaccount number: $key" }
                return@forEach
            }
            if (subaccountNumberToCheck != subaccountNumber) {
                if (marketIds.contains(marketId) && marketIds.size <= 1) {
                    return subaccountNumberToCheck
                } else if (marketIds.isEmpty()) {
                    // Check if subaccount equity is 0 so that funds are moved to a clean account if reclaimUnutilizedChildSubaccountFunds has not been called yet
                    val equity = parser.asDouble(parser.value(subaccounts, "$subaccountNumberToCheck.equity.current")) ?: 0.0
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
        val existingSubaccountNumbers = utilizedSubaccountsMarketIdMap.keys
        for (offset in NUM_PARENT_SUBACCOUNTS..MAX_SUBACCOUNT_NUMBER step NUM_PARENT_SUBACCOUNTS) {
            val tentativeSubaccountNumber = offset + subaccountNumber
            if (!existingSubaccountNumbers.contains(tentativeSubaccountNumber.toString())) {
                return tentativeSubaccountNumber
            }
        }

        // User has reached the maximum number of childSubaccounts for their current parentSubaccount
        error("No available subaccount number")
    }

    fun getChangedSubaccountNumbers(
        parser: ParserProtocol,
        subaccounts: Map<Int, InternalSubaccountState>,
        subaccountNumber: Int,
        tradeInput: InternalTradeInputState?
    ): IList<Int> {
        val marketId = tradeInput?.marketId
        if (tradeInput?.marginMode != MarginMode.Isolated || marketId == null) {
            return iListOf(subaccountNumber)
        }
        val childSubaccountNumber = getChildSubaccountNumberForIsolatedMarginTrade(
            parser = parser,
            subaccounts = subaccounts,
            subaccountNumber = subaccountNumber,
            marketId = marketId,
        )
        if (subaccountNumber != childSubaccountNumber) {
            return iListOf(subaccountNumber, childSubaccountNumber)
        }

        return iListOf(subaccountNumber)
    }

    fun getChangedSubaccountNumbersDeprecated(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        subaccountNumber: Int,
        tradeInput: Map<String, Any>?
    ): IList<Int> {
        val childSubaccountNumber = getChildSubaccountNumberForIsolatedMarginTradeDeprecated(parser, account, subaccountNumber, tradeInput)
        if (childSubaccountNumber != null && subaccountNumber != childSubaccountNumber) {
            return iListOf(subaccountNumber, childSubaccountNumber)
        }

        return iListOf(subaccountNumber)
    }

    fun getChildSubaccountNumberForIsolatedMarginClosePosition(
        account: InternalAccountState?,
        subaccountNumber: Int,
        tradeInput: InternalTradeInputState?
    ): Int {
        val marketId = tradeInput?.marketId ?: return subaccountNumber
        val position = findExistingPosition(account, marketId, subaccountNumber)
        return position?.subaccountNumber ?: subaccountNumber
    }

    fun getChildSubaccountNumberForIsolatedMarginClosePositionDeprecated(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        subaccountNumber: Int,
        tradeInput: Map<String, Any>?
    ): Int {
        val marketId = parser.asString(tradeInput?.get("marketId")) ?: return subaccountNumber
        val position = findExistingPositionDeprecated(parser, account, marketId, subaccountNumber)
        return parser.asInt(position?.get("subaccountNumber")) ?: subaccountNumber
    }

    /**
     * @description Calculate the amount of collateral to transfer into child subaccount for an isolated margin trade.
     */
    fun getIsolatedMarginTransferInAmountForTrade(
        trade: TradeInput,
        subaccount: Subaccount,
        market: PerpetualMarket
    ): Double? {
        return if (getShouldTransferInCollateral(trade, subaccount)) {
            calculateIsolatedMarginTransferAmount(trade, market, subaccount)?.takeIf { it > 0.0 }
        } else {
            null
        }
    }

    /**
     * @description Determine if collateral should be transferred into child subaccount for an isolated margin trade
     */
    internal fun getShouldTransferInCollateral(
        subaccount: InternalSubaccountState?,
        tradeInput: InternalTradeInputState?,
    ): Boolean {
        val isIncreasingPositionSize = getIsIncreasingPositionSize(subaccount, tradeInput)
        val isIsolatedMarginOrder = tradeInput?.marginMode == MarginMode.Isolated
        val isReduceOnly = tradeInput?.reduceOnly ?: false

        return isIncreasingPositionSize && isIsolatedMarginOrder && !isReduceOnly
    }

    /**
     * @description Determine if collateral should be transferred into child subaccount for an isolated margin trade
     */
    internal fun getShouldTransferInCollateralDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
    ): Boolean {
        val isIncreasingPositionSize = getIsIncreasingPositionSizeDeprecated(parser, subaccount, tradeInput)
        val isIsolatedMarginOrder = parser.asString(tradeInput?.get("marginMode")) == "ISOLATED"
        val isReduceOnly = parser.asBool(tradeInput?.get("reduceOnly")) ?: false

        return isIncreasingPositionSize && isIsolatedMarginOrder && !isReduceOnly
    }

    /**
     * @description Determine if collateral should be transferred into child subaccount for an isolated margin trade
     */
    private fun getShouldTransferInCollateral(
        trade: TradeInput,
        subaccount: Subaccount,
    ): Boolean {
        val isIsolatedMarginOrder = trade.marginMode == MarginMode.Isolated
        val isIncreasingPositionSize = getIsIncreasingPositionSize(subaccount, trade)
        val isReduceOnly = trade.reduceOnly
        return isIncreasingPositionSize && isIsolatedMarginOrder && !isReduceOnly
    }

    /**
     * @description Determine if collateral should be transferred out of child subaccount for an isolated margin trade
     */
    internal fun getShouldTransferOutRemainingCollateral(
        subaccount: InternalSubaccountState?,
        tradeInput: InternalTradeInputState?,
    ): Boolean {
        val isPositionFullyClosed = getIsPositionFullyClosed(subaccount, tradeInput)
        val isIsolatedMarginOrder = tradeInput?.marginMode == MarginMode.Isolated
        val hasOpenOrder = tradeInput?.marketId?.let { marketId ->
            hasExistingOrder(subaccount, marketId)
        } ?: false

        return isPositionFullyClosed && isIsolatedMarginOrder && !hasOpenOrder
    }

    /**
     * @description Determine if collateral should be transferred out of child subaccount for an isolated margin trade
     */
    internal fun getShouldTransferOutRemainingCollateralDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
    ): Boolean {
        val isPositionFullyClosed = getIsPositionFullyClosedDeprecated(parser, subaccount, tradeInput)
        val isIsolatedMarginOrder = parser.asString(tradeInput?.get("marginMode")) == "ISOLATED"
        val hasOpenOrder = parser.asString(tradeInput?.get("marketId"))?.let { marketId ->
            hasExistingOrderDeprecated(parser, subaccount, marketId)
        } ?: false

        return isPositionFullyClosed && isIsolatedMarginOrder && !hasOpenOrder
    }

    internal fun getEstimateRemainingCollateralAfterClosePosition(
        subaccount: InternalSubaccountState?,
        tradeInput: InternalTradeInputState?,
    ): Double? {
        val quoteBalance = subaccount?.calculated?.get(CalculationPeriod.current)?.quoteBalance ?: return null
        val total = tradeInput?.summary?.total ?: return null
        return quoteBalance + total
    }

    internal fun getEstimateRemainingCollateralAfterClosePositionDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
    ): Double? {
        val quoteBalance = parser.asDouble(parser.value(subaccount, "quoteBalance.current")) ?: return null
        val tradeSummary = parser.asNativeMap(parser.value(tradeInput, "summary")) ?: return null
        val total = parser.asDouble(tradeSummary["total"]) ?: return null
        return quoteBalance + total
    }

    private fun getIsPositionFullyClosed(
        subaccount: InternalSubaccountState?,
        tradeInput: InternalTradeInputState?,
    ): Boolean {
        return tradeInput?.marketId?.let { marketId ->
            val position = subaccount?.openPositions?.get(marketId)
            val currentSize = position?.calculated?.get(CalculationPeriod.current)?.size ?: 0.0
            val postOrderSize = getPositionPostOrderSizeFromTrade(tradeInput, currentSize)
            val isReduceOnly = tradeInput.reduceOnly
            val hasFlippedSide = currentSize * postOrderSize < 0
            return postOrderSize == 0.0 || (isReduceOnly && hasFlippedSide)
        } ?: false
    }

    private fun getIsPositionFullyClosedDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
    ): Boolean {
        return parser.asString(tradeInput?.get("marketId"))?.let { marketId ->
            val position = parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId"))
            val currentSize = parser.asDouble(parser.value(position, "size.current")) ?: 0.0
            val postOrderSize = tradeInput?.let { getPositionPostOrderSizeFromTradeDeprecated(parser, tradeInput, currentSize) } ?: 0.0
            val isReduceOnly = parser.asBool(tradeInput?.get("reduceOnly")) ?: false
            val hasFlippedSide = currentSize * postOrderSize < 0
            return postOrderSize == 0.0 || (isReduceOnly && hasFlippedSide)
        } ?: false
    }

    private fun getIsIncreasingPositionSize(
        subaccount: InternalSubaccountState?,
        tradeInput: InternalTradeInputState?,
    ): Boolean {
        return getPositionSizeDifference(subaccount, tradeInput)?.let { it > 0 } ?: true
    }

    private fun getIsIncreasingPositionSizeDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
    ): Boolean {
        return getPositionSizeDifferenceDeprecated(parser, subaccount, tradeInput)?.let { it > 0 } ?: true
    }

    private fun getIsIncreasingPositionSize(
        subaccount: Subaccount,
        trade: TradeInput,
    ): Boolean {
        return getPositionSizeDifference(subaccount, trade)?.let { it > 0 } ?: true
    }

    private fun getPositionSizeDifference(
        subaccount: InternalSubaccountState?,
        tradeInput: InternalTradeInputState?,
    ): Double? {
        val marketId = tradeInput?.marketId ?: return null
        val position = subaccount?.openPositions?.get(marketId)
        val currentSize = position?.calculated?.get(CalculationPeriod.current)?.size ?: 0.0
        val postOrderSize = getPositionPostOrderSizeFromTrade(tradeInput, currentSize)
        return postOrderSize.abs() - currentSize.abs()
    }

    private fun getPositionSizeDifferenceDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
    ): Double? {
        return parser.asString(tradeInput?.get("marketId"))?.let { marketId ->
            val position = parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId"))
            val currentSize = parser.asDouble(parser.value(position, "size.current")) ?: 0.0
            val postOrderSize = tradeInput?.let { getPositionPostOrderSizeFromTradeDeprecated(parser, tradeInput, currentSize) } ?: 0.0
            return postOrderSize.abs() - currentSize.abs()
        }
    }

    /**
     * @description Helper to determine post-order position size from current trade input instead of position.size.postOrder
     * We need this estimate before the trade delta is applied to the position, as position post-order size may not be updated yet.
     */
    private fun getPositionPostOrderSizeFromTrade(
        trade: InternalTradeInputState,
        currentPositionSize: Double,
    ): Double {
        val tradeSize = trade.summary?.takeIf {
            trade.marketId != null && trade.side != null && it.filled
        }?.let { summary ->
            val multiplier = if (trade.side == OrderSide.Buy) Numeric.double.POSITIVE else Numeric.double.NEGATIVE
            (summary.size ?: Numeric.double.ZERO) * multiplier
        } ?: 0.0

        return currentPositionSize + tradeSize
    }

    /**
     * @description Helper to determine post-order position size from current trade input instead of position.size.postOrder
     * We need this estimate before the trade delta is applied to the position, as position post-order size may not be updated yet.
     */
    private fun getPositionPostOrderSizeFromTradeDeprecated(
        parser: ParserProtocol,
        trade: Map<String, Any>,
        currentPositionSize: Double,
    ): Double {
        val tradeSize = parser.asNativeMap(trade["summary"])?.takeIf {
            parser.asString(trade["marketId"]) != null &&
                parser.asString(trade["side"]) != null &&
                parser.asBool(it["filled"]) == true
        }?.let { summary ->
            val multiplier = if (parser.asString(trade["side"]) == "BUY") Numeric.double.POSITIVE else Numeric.double.NEGATIVE
            (parser.asDouble(summary["size"]) ?: Numeric.double.ZERO) * multiplier
        } ?: 0.0

        return currentPositionSize + tradeSize
    }

    /**
     * @description Since position is already typed, we can calculate difference with position size diff directly
     */
    private fun getPositionSizeDifference(
        subaccount: Subaccount,
        trade: TradeInput,
    ): Double? {
        return subaccount.openPositions?.find { it.id == trade.marketId }?.let {
            (it.size.postOrder ?: 0.0).abs() - (it.size.current ?: 0.0).abs()
        } ?: return null
    }

    private fun getMaxMarketLeverageDeprecated(
        effectiveImf: Double,
        imf: Double,
    ): Double {
        return if (effectiveImf > Numeric.double.ZERO) {
            Numeric.double.ONE / effectiveImf
        } else if (imf > Numeric.double.ZERO) {
            Numeric.double.ONE / imf
        } else {
            Numeric.double.ONE
        }
    }

    /**
     * @description Calculate the amount of collateral to transfer for an isolated margin trade.
     * Max leverage is capped at 98% of the the market's max leverage and takes the oraclePrice into account in order to pass collateral checks.
     */
    internal fun calculateIsolatedMarginTransferAmount(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?
    ): Double? {
        val side = trade.side ?: return null
        val oraclePrice = market?.perpetualMarket?.oraclePrice ?: return null
        val price = trade.summary?.price ?: return null
        val maxMarketLeverage = market.perpetualMarket?.configs?.maxMarketLeverage ?: return null
        val targetLeverage = trade.targetLeverage ?: maxMarketLeverage
        val positionSizeDifference = getPositionSizeDifference(subaccount, trade) ?: return null

        return calculateIsolatedMarginTransferAmountFromValues(
            targetLeverage = targetLeverage,
            side = side.rawValue,
            oraclePrice = oraclePrice,
            price = price,
            maxMarketLeverage = maxMarketLeverage,
            positionSizeDifference = positionSizeDifference,
        )
    }

    /**
     * @description Calculate the amount of collateral to transfer for an isolated margin trade.
     * Max leverage is capped at 98% of the the market's max leverage and takes the oraclePrice into account in order to pass collateral checks.
     */
    internal fun calculateIsolatedMarginTransferAmountDeprecated(
        parser: ParserProtocol,
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        subaccount: Map<String, Any>?
    ): Double? {
        val side = parser.asString(parser.value(trade, "side")) ?: return null
        val oraclePrice = parser.asDouble(parser.value(market, "oraclePrice")) ?: return null
        val price = parser.asDouble(parser.value(trade, "summary.price")) ?: return null
        val initialMarginFraction = parser.asDouble(parser.value(market, "configs.initialMarginFraction")) ?: Numeric.double.ZERO
        val effectiveImf = parser.asDouble(parser.value(market, "configs.effectiveInitialMarginFraction")) ?: Numeric.double.ZERO
        val maxMarketLeverage = getMaxMarketLeverageDeprecated(effectiveImf = effectiveImf, imf = initialMarginFraction)

        val targetLeverage = parser.asDouble(trade["targetLeverage"]) ?: maxMarketLeverage
        val positionSizeDifference = getPositionSizeDifferenceDeprecated(parser, subaccount, trade) ?: return null

        return calculateIsolatedMarginTransferAmountFromValues(
            targetLeverage = targetLeverage,
            side = side,
            oraclePrice = oraclePrice,
            price = price,
            maxMarketLeverage = maxMarketLeverage,
            positionSizeDifference = positionSizeDifference,
        )
    }

    private fun calculateIsolatedMarginTransferAmount(
        trade: TradeInput,
        market: PerpetualMarket,
        subaccount: Subaccount,
    ): Double? {
        val targetLeverage = trade.targetLeverage
        val side = trade.side?.rawValue ?: return null
        val oraclePrice = market.oraclePrice ?: return null
        val price = trade.summary?.price ?: return null
        val maxMarketLeverage = market.configs?.maxMarketLeverage ?: return null
        val positionSizeDifference = getPositionSizeDifference(subaccount, trade) ?: return null

        return calculateIsolatedMarginTransferAmountFromValues(
            targetLeverage,
            side,
            oraclePrice,
            price,
            maxMarketLeverage,
            positionSizeDifference,
        )
    }

    private fun calculateIsolatedMarginTransferAmountFromValues(
        targetLeverage: Double,
        side: String,
        oraclePrice: Double,
        price: Double,
        maxMarketLeverage: Double?,
        positionSizeDifference: Double,
    ): Double? {
        val maxLeverageForMarket = maxMarketLeverage ?: Numeric.double.ONE
        // Cap targetLeverage to 98% of max leverage
        val adjustedTargetLeverage = min(targetLeverage, maxLeverageForMarket * MAX_LEVERAGE_BUFFER_PERCENT)

        return if (adjustedTargetLeverage == 0.0) {
            null
        } else {
            getTransferAmountFromTargetLeverage(
                price,
                oraclePrice,
                side,
                positionSizeDifference,
                targetLeverage = adjustedTargetLeverage,
            )
        }
    }

    internal fun getTransferAmountFromTargetLeverage(
        price: Double,
        oraclePrice: Double,
        side: String,
        size: Double,
        targetLeverage: Double,
    ): Double {
        if (targetLeverage == 0.0) {
            return 0.0
        }

        val naiveTransferAmount = (price * size) / targetLeverage

        // Calculate the difference between the oracle price and the ask/bid price in order to determine immediate PnL impact that would affect collateral checks
        val priceDiff = if (side == "BUY") {
            price - oraclePrice
        } else {
            oraclePrice - price
        }

        return max((oraclePrice * size) / targetLeverage + priceDiff * size, naiveTransferAmount)
    }
}
