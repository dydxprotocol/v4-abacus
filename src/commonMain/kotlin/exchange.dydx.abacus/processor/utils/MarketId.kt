package exchange.dydx.abacus.processor.utils

internal class MarketId {
    companion object {
        internal fun assetid(
            marketId: String,
        ): String? {
            val elements = marketId.split("-")
            return if (elements.size == 2) {
                elements.first()
            } else {
                null
            }
        }
    }
}
