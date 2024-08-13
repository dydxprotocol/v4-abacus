package exchange.dydx.abacus.processor.utils

internal object MarketId {
    internal fun getAssetId(
        marketId: String,
    ): String? {
        val elements = marketId.split("-")
        val baseAssetLongForm = elements.first()
        val baseAssetElements = baseAssetLongForm.split(",")

        return if (baseAssetElements.isNotEmpty()) {
            baseAssetElements.first()
        } else {
            null
        }
    }

    internal fun getDisplayId(
        marketId: String,
    ): String {
        val elements = marketId.split("-")
        val baseAssetLongForm = elements.first()
        val quoteAsset = elements.last()
        val baseAssetElements = baseAssetLongForm.split(",")

        return if (baseAssetElements.isNotEmpty()) {
            "fake" + baseAssetElements.first() + "-" + quoteAsset
        } else {
            marketId
        }
    }
}
