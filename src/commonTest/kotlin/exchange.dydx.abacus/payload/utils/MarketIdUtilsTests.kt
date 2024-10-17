package exchange.dydx.abacus.payload.utils

import exchange.dydx.abacus.processor.utils.MarketId
import kotlin.test.Test
import kotlin.test.assertEquals

class MarketIdUtilsTests {

    @Test
    fun getDisplayIdDefault() {
        val marketId = "ETH-USD"
        val displayId = MarketId.getDisplayId(marketId)
        assertEquals("ETH-USD", displayId)
    }

    @Test
    fun getDisplayIdLong() {
        val marketId = "AART,raydium,F3nefJBcejYbtdREjui1T9DPh5dBgpkKq7u2GAAMXs5B-USD"
        val displayId = MarketId.getDisplayId(marketId)
        assertEquals("AART-USD", displayId)
    }

    @Test
    fun getAssetIdDefault() {
        val marketId = "ETH-USD"
        val assetId = MarketId.getAssetId(marketId)
        assertEquals("ETH", assetId)
    }

    @Test
    fun getAssetIdLong() {
        val marketId = "AART,raydium,F3nefJBcejYbtdREjui1T9DPh5dBgpkKq7u2GAAMXs5B-USD"
        val assetId = MarketId.getAssetId(marketId)
        assertEquals("AART,raydium,F3nefJBcejYbtdREjui1T9DPh5dBgpkKq7u2GAAMXs5B", assetId)
    }
}
