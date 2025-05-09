package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.Logger
import kotlin.test.Test
import kotlin.test.assertNotNull

class V3OrderbookLoadTests : V3BaseTests() {
    @Test
    fun testOverlappedOrderbook() {
        loadMarkets()
        loadMarketsConfigurations()

        perp.socket(
            url = mock.socketUrl,
            jsonString = mock.orderbookChannel.load_test_2_subscribed,
            subaccountNumber = 0,
            height = null,
        )

        val orderbook = perp.internalState.marketsSummary.markets["ETH-USD"]?.groupedOrderbook
        assertNotNull(orderbook)

        for (i in 0 until mock.orderbookChannel.load_test_2_channel_batch_data_list.count()) {
            testChannelBatchData(mock.orderbookChannel.load_test_2_channel_batch_data_list[i], i)
        }
    }

    fun testChannelBatchData(text: String, index: Int) {
        perp.socket(mock.socketUrl, text, 0, null)

        val orderbookState = perp.internalState.marketsSummary.markets["ETH-USD"]?.groupedOrderbook
        assertNotNull(orderbookState)

        val orderbook = perp.state?.marketOrderbook("ETH-USD")
        assertNotNull(orderbook)
        val asks = orderbook.asks
        assertNotNull(asks)
        val bids = orderbook.bids
        assertNotNull(bids)
        val asksCount = asks.count()
        val bidsCount = bids.count()
        Logger.d { "Asks: $asksCount, Bids: $bidsCount" }
    }
}
