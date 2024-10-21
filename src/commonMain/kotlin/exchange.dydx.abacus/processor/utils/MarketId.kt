package exchange.dydx.abacus.processor.utils

internal object MarketId {
    /**
     * Get the human readable displayId from the unique market id (ticker).
     */
    internal fun getDisplayId(
        marketId: String,
    ): String {
        val elements = marketId.split("-")
        val baseAssetLongForm = elements.first()
        val quoteAsset = elements.last()
        val baseAssetElements = baseAssetLongForm.split(",")

        return if (baseAssetElements.isNotEmpty()) {
            baseAssetElements.first() + "-" + quoteAsset
        } else {
            marketId
        }
    }

    /**
     * Get the asset id from the market id.
     */
    internal fun getAssetId(
        marketId: String,
    ): String? {
        val elements = marketId.split("-")

        return if (elements.isNotEmpty()) {
            elements.first()
        } else {
            null
        }
    }
}
