package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.tests.extensions.loadCandlesAllMarkets
import exchange.dydx.abacus.tests.extensions.loadCandlesFirst
import exchange.dydx.abacus.tests.extensions.loadCandlesSecond
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class V4CandlesTests : V3BaseTests() {
    private val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")

    @Test
    fun testCandles() {
        loadMarkets()
        loadMarketsConfigurations()

        print("--------First round----------\n")

        testCandlesOnce()
    }

    private fun testCandlesOnce() {
        var time = ServerTime.now()
        testCandlesAllMarkets()
        time = perp.log("Candles All Markets", time)

        testCandlesFirstCall()
        time = perp.log("Candles First Call", time)

        testCandlesSecondCall()
        perp.log("Candles Second Call", time)

        testCandlesSubscribed()
        perp.log("Candles Subscribed", time)

        testCandlesChannelData()
        perp.log("Candles Channel Data", time)

        testCandlesChannelBatchData()
        perp.log("Candles Channel Batch Data", time)
    }

    private fun testCandlesAllMarkets() {
        perp.loadCandlesAllMarkets(mock)
        val market = perp.internalState.marketsSummary.markets["ETH-USD"]
        assertEquals(1, market?.candles?.size)
        val firstItem = market?.candles?.get("1HOUR")?.first()
        assertEquals(1785.7, firstItem?.open)
        assertEquals(1797.4, firstItem?.close)
    }

    private fun testCandlesFirstCall() {
        perp.loadCandlesFirst(mock)
        val market = perp.internalState.marketsSummary.markets["ETH-USD"]
        val firstItem = market?.candles?.get("15MINS")?.first()
        assertEquals(1780.6, firstItem?.open)
        assertEquals(1782.3, firstItem?.close)
    }

    private fun testCandlesSecondCall() {
        perp.loadCandlesSecond(mock)
        val market = perp.internalState.marketsSummary.markets["ETH-USD"]
        val firstItem = market?.candles?.get("15MINS")?.first()
        assertEquals(1709.7, firstItem?.open)
        assertEquals(1709.7, firstItem?.close)
    }

    private fun testCandlesSubscribed() {
        perp.socket(testWsUrl, mock.candles.v4_subscribed, 0, null)
        val market = perp.internalState.marketsSummary.markets["ETH-USD"]
        val firstItem = market?.candles?.get("1HOUR")?.first()
        assertEquals(1785.7, firstItem?.open)
        assertEquals(1797.4, firstItem?.close)
    }

    private fun testCandlesChannelData() {
        perp.socket(testWsUrl, mock.candles.v4_channel_data, 0, null)
        val market = perp.internalState.marketsSummary.markets["ETH-USD"]
        val firstItem = market?.candles?.get("15MINS")?.first()
        assertEquals(1709.7, firstItem?.open)
        assertEquals(1709.7, firstItem?.close)

        val candles = perp.internalState.marketsSummary.markets["ETH-USD"]?.candles?.get("1HOUR")
        assertEquals(125, candles?.size)
        val lastCandle = candles?.last()
        assertEquals(1582.8, lastCandle?.close)
        assertEquals(1577.7, lastCandle?.open)
    }

    private fun testCandlesChannelBatchData() {
        perp.socket(testWsUrl, mock.candles.v4_channel_batch_data, 0, null)
        var candles = perp.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")
        assertEquals(126, candles?.size)
        var lastCandle = candles?.last()
        assertEquals(1590.8, lastCandle?.close)
        assertEquals(1598.0, lastCandle?.open)

        perp.socket(testWsUrl, mock.candles.v4_channel_batch_data_2, 0, null)
        candles = perp.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")
        assertEquals(126, candles?.size)
        lastCandle = candles?.last()
        assertEquals(1592.7, lastCandle?.close)
        assertEquals(1598.0, lastCandle?.open)
    }
}
