package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.mutable

object MarginModeCalculator {

    private val parser = Parser()

    internal fun updateTradeInputMarginMode(
        markets: Map<String, Any>?,
        account: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
        subaccountNumber: Int,
    ): MutableMap<String, Any>? {
        val modified = tradeInput?.mutable() ?: return null
        val marketId = parser.asString(tradeInput["marketId"])
        val market = parser.asMap(markets?.get(marketId))

        val imf = parser.asDouble(parser.value(market, "configs.initialMarginFraction")) ?: Numeric.double.ZERO
        val effectiveImf = parser.asDouble(parser.value(market, "configs.effectiveInitialMarginFraction")) ?: Numeric.double.ZERO
        val maxMarketLeverage = if (effectiveImf > Numeric.double.ZERO) {
            Numeric.double.ONE / effectiveImf
        } else if (imf > Numeric.double.ZERO) {
            Numeric.double.ONE / imf
        } else {
            Numeric.double.ONE
        }

        val existingMarginMode =
            MarginCalculator.findExistingMarginModeDeprecated(
                parser,
                account,
                marketId,
                subaccountNumber,
            )
        // If there is an existing position or order, we have to use the same margin mode
        if (existingMarginMode != null) {
            modified["marginMode"] = existingMarginMode
            if (
                existingMarginMode == "ISOLATED" &&
                parser.asDouble(tradeInput["targetLeverage"]) == null
            ) {
                val existingPosition = MarginCalculator.findExistingPositionDeprecated(
                    parser,
                    account,
                    marketId,
                    subaccountNumber,
                )
                val existingPositionLeverage = parser.asDouble(parser.value(existingPosition, "leverage.current"))
                modified["targetLeverage"] = existingPositionLeverage ?: maxMarketLeverage
            }
        } else {
            val marketMarginMode = MarginCalculator.findMarketMarginModeDeprecated(
                parser,
                market,
            )
            when (marketMarginMode) {
                "ISOLATED" -> {
                    modified["marginMode"] = marketMarginMode
                    if (parser.asDouble(tradeInput["targetLeverage"]) == null) {
                        modified["targetLeverage"] = maxMarketLeverage
                    }
                }

                "CROSS" -> {
                    if (modified["marginMode"] == null) {
                        modified["marginMode"] = marketMarginMode
                    }
                }
            }
        }
        return modified
    }
}
