package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.payload.v3.V3BaseTests
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.state.machine.setOrderbookGrouping
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class V4OrderbookTests : V3BaseTests() {
    private val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")

    @Test
    fun testOrderbook() {
        loadMarkets()
        loadMarketsConfigurations()

        print("--------First round----------\n")

        testOrderbookOnce()
    }

    private fun testOrderbookOnce() {
        var time = ServerTime.now()
        testReceivedOrderbook()
        time = perp.log("Orderbook", time)
    }

    private fun testReceivedOrderbook() {
        perp.socket(testWsUrl, mock.orderbookChannel.halted_chain_test, 0, null)

        var orderbook = perp.internalState.marketsSummary.markets["ETH-USD"]?.groupedOrderbook
        var asks = orderbook?.asks!!
        assertEquals(3, asks.size)

        assertEquals(0.907, asks[0].size)
        assertEquals(1800.0, asks[0].price)
        assertEquals(0.907, asks[0].depth)

        assertEquals(1.0, asks[1].size)
        assertEquals(1801.0, asks[1].price)
        assertEquals(1.907, asks[1].depth)

        assertEquals(1.0, asks[2].size)
        assertEquals(1901.0, asks[2].price)
        assertEquals(2.907, asks[2].depth)

        var bids = orderbook?.bids!!

        assertEquals(0.221, bids[0].size)
        assertEquals(1619.0, bids[0].price)
        assertEquals(0.221, bids[0].depth)

        assertEquals(2.742, bids[1].size)
        assertEquals(1520.0, bids[1].price)
        assertEquals(2.963, bids[1].depth)

        assertEquals(0.02, bids[2].size)
        assertEquals(1505.0, bids[2].price)
        assertEquals(2.983, bids[2].depth)

        assertEquals(0.01, bids[3].size)
        assertEquals(1200.0, bids[3].price)
        assertEquals(2.993, bids[3].depth)

        assertEquals(1.02, bids[4].size)
        assertEquals(1025.0, bids[4].price)
        assertEquals(4.013, bids[4].depth)

        assertEquals(5.027, bids[5].size)
        assertEquals(1000.0, bids[5].price)
        assertEquals(9.04, bids[5].depth)

        assertEquals(0.075, bids[6].size)
        assertEquals(120.0, bids[6].price)
        assertEquals(9.114999999999998, bids[6].depth)

        assertEquals(1.0, bids[7].size)
        assertEquals(111.0, bids[7].price)
        assertEquals(10.114999999999998, bids[7].depth)

        perp.setOrderbookGrouping("ETH-USD", 10)

        orderbook = perp.internalState.marketsSummary.markets["ETH-USD"]?.groupedOrderbook
        asks = orderbook?.asks!!

        assertEquals(3, asks.size)

        assertEquals(0.907, asks[0].size)
        assertEquals(1800.0, asks[0].price)
        assertEquals(0.907, asks[0].depth)

        assertEquals(1.0, asks[1].size)
        assertEquals(1801.0, asks[1].price)
        assertEquals(1.907, asks[1].depth)

        assertEquals(1.0, asks[2].size)
        assertEquals(1901.0, asks[2].price)
        assertEquals(2.907, asks[2].depth)

        bids = orderbook?.bids!!

        assertEquals(0.221, bids[0].size)
        assertEquals(1619.0, bids[0].price)
        assertEquals(0.221, bids[0].depth)

        assertEquals(2.742, bids[1].size)
        assertEquals(1520.0, bids[1].price)
        assertEquals(2.963, bids[1].depth)

        assertEquals(0.02, bids[2].size)
        assertEquals(1505.0, bids[2].price)
        assertEquals(2.983, bids[2].depth)

        assertEquals(0.01, bids[3].size)
        assertEquals(1200.0, bids[3].price)
        assertEquals(2.993, bids[3].depth)

        assertEquals(1.02, bids[4].size)
        assertEquals(1025.0, bids[4].price)
        assertEquals(4.013, bids[4].depth)

        assertEquals(5.027, bids[5].size)
        assertEquals(1000.0, bids[5].price)
        assertEquals(9.04, bids[5].depth)

        assertEquals(0.075, bids[6].size)
        assertEquals(120.0, bids[6].price)
        assertEquals(9.114999999999998, bids[6].depth)

        assertEquals(1.0, bids[7].size)
        assertEquals(111.0, bids[7].price)
        assertEquals(10.114999999999998, bids[7].depth)

        perp.setOrderbookGrouping("ETH-USD", 100)

        orderbook = perp.internalState.marketsSummary.markets["ETH-USD"]?.groupedOrderbook
        asks = orderbook?.asks!!

        assertEquals(3, asks.size)

        assertEquals(0.907, asks[0].size)
        assertEquals(1800.0, asks[0].price)
        assertEquals(0.907, asks[0].depth)

        assertEquals(1.0, asks[1].size)
        assertEquals(1810.0, asks[1].price)
        assertEquals(1.907, asks[1].depth)

        assertEquals(1.0, asks[2].size)
        assertEquals(1910.0, asks[2].price)
        assertEquals(2.907, asks[2].depth)

        bids = orderbook?.bids!!

        assertEquals(0.221, bids[0].size)
        assertEquals(1610.0, bids[0].price)
        assertEquals(0.221, bids[0].depth)

        assertEquals(2.742, bids[1].size)
        assertEquals(1520.0, bids[1].price)
        assertEquals(2.963, bids[1].depth)

        assertEquals(0.02, bids[2].size)
        assertEquals(1500.0, bids[2].price)
        assertEquals(2.983, bids[2].depth)

        assertEquals(0.01, bids[3].size)
        assertEquals(1200.0, bids[3].price)
        assertEquals(2.993, bids[3].depth)

        assertEquals(1.02, bids[4].size)
        assertEquals(1020.0, bids[4].price)
        assertEquals(4.013, bids[4].depth)

        assertEquals(5.027, bids[5].size)
        assertEquals(1000.0, bids[5].price)
        assertEquals(9.04, bids[5].depth)

        assertEquals(0.075, bids[6].size)
        assertEquals(120.0, bids[6].price)
        assertEquals(9.114999999999998, bids[6].depth)

        assertEquals(1.0, bids[7].size)
        assertEquals(110.0, bids[7].price)
        assertEquals(10.114999999999998, bids[7].depth)

        assertEquals(0.1, bids[8].size)
        assertEquals(100.0, bids[8].price)
        assertEquals(10.214999999999998, bids[8].depth)

        assertEquals(0.007, bids[9].size)
        assertEquals(20.0, bids[9].price)
        assertEquals(10.221999999999998, bids[9].depth)

        assertEquals(2.338, bids[10].size)
        assertEquals(10.0, bids[10].price)
        assertEquals(12.559999999999999, bids[10].depth)

        perp.setOrderbookGrouping("ETH-USD", 10)

        orderbook = perp.internalState.marketsSummary.markets["ETH-USD"]?.groupedOrderbook
        asks = orderbook?.asks!!
        assertEquals(3, asks.size)

        assertEquals(0.907, asks[0].size)
        assertEquals(1800.0, asks[0].price)
        assertEquals(0.907, asks[0].depth)

        assertEquals(1.0, asks[1].size)
        assertEquals(1801.0, asks[1].price)
        assertEquals(1.907, asks[1].depth)

        assertEquals(1.0, asks[2].size)
        assertEquals(1901.0, asks[2].price)
        assertEquals(2.907, asks[2].depth)

        bids = orderbook?.bids!!

        assertEquals(0.221, bids[0].size)
        assertEquals(1619.0, bids[0].price)
        assertEquals(0.221, bids[0].depth)

        assertEquals(2.742, bids[1].size)
        assertEquals(1520.0, bids[1].price)
        assertEquals(2.963, bids[1].depth)

        assertEquals(0.02, bids[2].size)
        assertEquals(1505.0, bids[2].price)
        assertEquals(2.983, bids[2].depth)

        assertEquals(0.01, bids[3].size)
        assertEquals(1200.0, bids[3].price)
        assertEquals(2.993, bids[3].depth)

        assertEquals(1.02, bids[4].size)
        assertEquals(1025.0, bids[4].price)
        assertEquals(4.013, bids[4].depth)

        assertEquals(5.027, bids[5].size)
        assertEquals(1000.0, bids[5].price)
        assertEquals(9.04, bids[5].depth)

        assertEquals(0.075, bids[6].size)
        assertEquals(120.0, bids[6].price)
        assertEquals(9.114999999999998, bids[6].depth)

        assertEquals(1.0, bids[7].size)
        assertEquals(111.0, bids[7].price)
        assertEquals(10.114999999999998, bids[7].depth)

        perp.setOrderbookGrouping("ETH-USD", 1)

        orderbook = perp.internalState.marketsSummary.markets["ETH-USD"]?.groupedOrderbook
        asks = orderbook?.asks!!
        assertEquals(3, asks.size)

        assertEquals(0.907, asks[0].size)
        assertEquals(1800.0, asks[0].price)
        assertEquals(0.907, asks[0].depth)

        assertEquals(1.0, asks[1].size)
        assertEquals(1801.0, asks[1].price)
        assertEquals(1.907, asks[1].depth)

        assertEquals(1.0, asks[2].size)
        assertEquals(1901.0, asks[2].price)
        assertEquals(2.907, asks[2].depth)

        bids = orderbook?.bids!!

        assertEquals(0.221, bids[0].size)
        assertEquals(1619.0, bids[0].price)
        assertEquals(0.221, bids[0].depth)

        assertEquals(2.742, bids[1].size)
        assertEquals(1520.0, bids[1].price)
        assertEquals(2.963, bids[1].depth)

        assertEquals(0.02, bids[2].size)
        assertEquals(1505.0, bids[2].price)
        assertEquals(2.983, bids[2].depth)

        assertEquals(0.01, bids[3].size)
        assertEquals(1200.0, bids[3].price)
        assertEquals(2.993, bids[3].depth)

        assertEquals(1.02, bids[4].size)
        assertEquals(1025.0, bids[4].price)
        assertEquals(4.013, bids[4].depth)

        assertEquals(5.027, bids[5].size)
        assertEquals(1000.0, bids[5].price)
        assertEquals(9.04, bids[5].depth)

        assertEquals(0.075, bids[6].size)
        assertEquals(120.0, bids[6].price)
        assertEquals(9.114999999999998, bids[6].depth)

        assertEquals(1.0, bids[7].size)
        assertEquals(111.0, bids[7].price)
        assertEquals(10.114999999999998, bids[7].depth)
    }
}
