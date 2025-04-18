package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.state.machine.setOrderbookGrouping
import exchange.dydx.abacus.tests.extensions.socket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class V3OrderbookTests : V3BaseTests() {
    @Test
    fun testOverlappedOrderbook() {
        loadMarkets()
        loadMarketsConfigurations()

        perp.socket(
            url = mock.socketUrl,
            jsonString = mock.orderbookChannel.subscribed_overlapped,
            subaccountNumber = 0,
            height = null,
        )

        val orderbook = perp.internalState.marketsSummary.markets["ETH-USD"]?.groupedOrderbook
        val asks = orderbook?.asks!!

        assertEquals(25.138, asks[0].size)
        assertEquals(1655.8, asks[0].price)
        assertEquals(25.138, asks[0].depth)

        assertEquals(11.891, asks[1].size)
        assertEquals(1656.0, asks[1].price)
        assertEquals(37.029, asks[1].depth)

        val bids = orderbook.bids!!

        assertEquals(13.363, bids[0].size)
        assertEquals(1654.3, bids[0].price)
        assertEquals(13.363, bids[0].depth)

        assertEquals(19.55, bids[1].size)
        assertEquals(1653.0, bids[1].price)
        assertEquals(32.913, bids[1].depth)

        assertEquals(34.435, bids[2].size)
        assertEquals(1652.9, bids[2].price)
        assertEquals(67.348, bids[2].depth)

        assertEquals(1655.05, orderbook.midPrice)
        assertEquals(9.06317029696988E-4, orderbook.spreadPercent)

        test(
            {
                perp.setOrderbookGrouping("ETH-USD", 100)
            },
            null,
        )
    }

    @Test
    fun testOrderbook() {
        loadMarkets()
        loadMarketsConfigurations()

        print("--------First round----------\n")

        testOrderbookOnce()
    }

    fun testOrderbookOnce() {
        val payloads = mock.orderbookChannel.orderbook_test_data
        for (i in payloads.indices) {
            val payload = payloads[i]
            test({
                perp.socket(mock.socketUrl, payload, 0, null)
            }, null, {
                val orderbook = it.state?.marketOrderbook("ETH-USD")
                val bids = orderbook?.bids
                val asks = orderbook?.asks
                val highestBid = bids?.firstOrNull()?.price
                val lowestAsk = asks?.firstOrNull()?.price
                val bidSize = bids?.firstOrNull()?.size
                val askSize = asks?.firstOrNull()?.size
                val bidOffset = bids?.firstOrNull()?.offset
                val askOffset = asks?.firstOrNull()?.offset
                assertNotNull(highestBid)
                assertNotNull(lowestAsk)
//                assertTrue { highestBid <= lowestAsk }
            })
        }
    }
}
