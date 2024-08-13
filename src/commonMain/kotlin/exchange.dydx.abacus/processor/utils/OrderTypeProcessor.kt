package exchange.dydx.abacus.processor.utils

internal class OrderTypeProcessor {
    companion object {
        internal fun orderType(type: String?, clientMetadata: Int?): String? {
            // hacky fix until indexer fix is in (CT-916)
            if (type == "LIQUIDATION") return "LIMIT"

            if (type == "DELEVERAGED") return "LIQUIDATED" // CT-1118
            if (type == "OFFSETTING") return "DELEVERAGED" // CT-1118

            return if (clientMetadata == 1) {
                when (type) {
                    "LIMIT" -> "MARKET"

                    "STOP_LIMIT" -> "STOP_MARKET"

                    "TAKE_PROFIT" -> "TAKE_PROFIT_MARKET"

                    else -> type
                }
            } else {
                type
            }
        }
    }
}
