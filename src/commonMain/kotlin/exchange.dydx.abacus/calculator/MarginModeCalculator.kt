package exchange.dydx.abacus.calculator

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
        val existingMarginMode =
            MarginCalculator.findExistingMarginMode(
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
                val existingPosition = MarginCalculator.findExistingPosition(
                    parser,
                    account,
                    marketId,
                    subaccountNumber,
                )
                val existingPositionLeverage = parser.asDouble(parser.value(existingPosition, "leverage.current"))
                modified["targetLeverage"] = existingPositionLeverage ?: 1.0
            }
        } else {
            val marketMarginMode = MarginCalculator.findMarketMarginMode(
                parser,
                parser.asMap(markets?.get(marketId)),
            )
            when (marketMarginMode) {
                "ISOLATED" -> {
                    modified["marginMode"] = marketMarginMode
                    if (parser.asDouble(tradeInput["targetLeverage"]) == null) {
                        modified["targetLeverage"] = 1.0
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
