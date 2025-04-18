package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.tests.extensions.loadTrades
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class V3MarketsDelayedTests : V3BaseTests() {
    @Test
    fun testOrderbookFirst() {
        loadOrderbook()

        var market = perp.internalState.marketsSummary.markets.get("ETH-USD")
        assertTrue { market?.groupedOrderbook != null }

        perp.loadTrades(mock)
        assertNull(perp.state?.marketsSummary)

        loadMarkets()
        market = perp.internalState.marketsSummary.markets.get("ETH-USD")
        assertTrue { market?.groupedOrderbook != null }
    }

    @Test
    fun testTradesFirst() {
        perp.loadTrades(mock)
        var market = perp.internalState.marketsSummary.markets.get("ETH-USD")
        assertTrue { market?.trades?.isNotEmpty() == true }
        assertNull(perp.state?.marketsSummary)

        loadOrderbook()
        market = perp.internalState.marketsSummary.markets.get("ETH-USD")
        assertTrue { market?.trades?.isNotEmpty() == true }
        assertNull(perp.state?.marketsSummary)

        loadMarkets()
        market = perp.internalState.marketsSummary.markets.get("ETH-USD")
        assertTrue { market?.trades?.isNotEmpty() == true }
        assertNotNull(perp.state?.marketsSummary)
    }
}
