package exchange.dydx.abacus.calculator

import abs
import exchange.dydx.abacus.protocols.ParserProtocol
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

    fun findExistingMarginMode(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        marketId: String?,
        subaccountNumber: Int,
    ): String? {
        val position = findExistingPosition(parser, account, marketId, subaccountNumber)
        if (position != null) {
            return if (position["equity"] != null) "ISOLATED" else "CROSS"
        }

        val openOrder = findExistingOrder(parser, account, marketId, subaccountNumber)
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
        parser: ParserProtocol,
        account: Map<String, Any>?,
        market: Map<String, Any>?,
        subaccountNumber: Int,
    ): Boolean {
        val marketId = parser.asString(market?.get("id"))
        val existingMarginMode =
            findExistingMarginMode(parser, account, marketId, subaccountNumber)
        return if (existingMarginMode != null) {
            false
        } else if (marketId != null) {
            findMarketMarginMode(parser, market) == "CROSS"
        } else {
            true
        }
    }

    /**
     * @description Get the childSubaccount number that is available for the given marketId
     * @param parser ParserProtocol
     * @param account Account data (data.wallet.account)
     * @param subaccountNumber Parent subaccount number
     * @param tradeInput Trade input data (data.input.trade)
     */
    fun getChildSubaccountNumberForIsolatedMarginTrade(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        subaccountNumber: Int,
        tradeInput: Map<String, Any>?
    ): Int {
        val marginMode = parser.asString(tradeInput?.get("marginMode"))
        if (marginMode != "ISOLATED") {
            return subaccountNumber
        }
        val marketId = parser.asString(tradeInput?.get("marketId")) ?: return subaccountNumber
        val subaccounts = parser.asNativeMap(account?.get("subaccounts")) ?: return subaccountNumber

        val utilizedSubaccountsMarketIdMap = subaccounts.mapValues {
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

    private fun getIsIncreasingPositionSize(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
    ): Boolean {
        return getPositionSizeDifference(parser, subaccount, tradeInput)?.let {
            it.compareTo(Numeric.double.ZERO) > 0 // whether it's positive
        } ?: true
    }

    /**
     * @description Determine if collateral should be transferred in for an isolated margin trade
     */
    fun getShouldTransferCollateral(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
    ): Boolean {
        val isIncreasingPositionSize = getIsIncreasingPositionSize(parser, subaccount, tradeInput)
        val isIsolatedMarginOrder = parser.asString(tradeInput?.get("marginMode")) == "ISOLATED"
        val isReduceOnly = parser.asBool(tradeInput?.get("reduceOnly")) ?: false

        return isIncreasingPositionSize && isIsolatedMarginOrder && !isReduceOnly
    }

    /**
     * @description Helper to determine how much collateral needs to be transferred in for an isolated margin trade
     */
    private fun getPositionSizeDifference(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
    ): Double? {
        return parser.asString(tradeInput?.get("marketId"))?.let { marketId ->
            val position = parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId"))
            val currentSize = parser.asDouble(parser.value(position, "size.current")) ?: 0.0
            val postOrderSize = parser.asDouble(parser.value(position, "size.postOrder")) ?: 0.0
            return postOrderSize.abs() - currentSize.abs()
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

    /**
     * @description Calculate the amount of collateral to transfer for an isolated margin trade.
     * Max leverage is capped at 98% of the the market's max leverage and takes the oraclePrice into account in order to pass collateral checks.
     */
    fun calculateIsolatedMarginTransferAmount(
        parser: ParserProtocol,
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        subaccount: Map<String, Any>?,
    ): Double? {
        val targetLeverage = parser.asDouble(trade["targetLeverage"]) ?: 1.0
        val side = parser.asString(parser.value(trade, "side")) ?: return null
        val oraclePrice = parser.asDouble(parser.value(market, "oraclePrice")) ?: return null
        val price = parser.asDouble(parser.value(trade, "summary.price")) ?: return null
        val initialMarginFraction = parser.asDouble(parser.value(market, "configs.initialMarginFraction")) ?: 0.0
        val effectiveImf = parser.asDouble(parser.value(market, "configs.effectiveInitialMarginFraction")) ?: 0.0

        val maxLeverageForMarket = if (effectiveImf != 0.0) {
            1.0 / effectiveImf
        } else if (initialMarginFraction != 0.0) {
            1.0 / initialMarginFraction
        } else {
            null
        }

        // Cap targetLeverage to 98% of max leverage
        val adjustedTargetLeverage = if (maxLeverageForMarket != null) {
            val cappedLeverage = maxLeverageForMarket * MAX_LEVERAGE_BUFFER_PERCENT
            min(targetLeverage, cappedLeverage)
        } else {
            null
        }

        // Get the position size difference
        val positionSizeDifference = getPositionSizeDifference(parser, subaccount, trade) ?: return null

        return if (adjustedTargetLeverage == 0.0 || adjustedTargetLeverage == null) {
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
}
