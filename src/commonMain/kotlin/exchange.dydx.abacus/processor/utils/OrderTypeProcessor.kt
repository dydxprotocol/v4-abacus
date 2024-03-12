package exchange.dydx.abacus.processor.utils

internal class OrderTypeProcessor {
    companion object {
        internal fun orderType(type: String?, clientMetadata: Int?): String? {
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
