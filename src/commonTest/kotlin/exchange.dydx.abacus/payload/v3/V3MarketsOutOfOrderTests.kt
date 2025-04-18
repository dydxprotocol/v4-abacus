package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.tests.extensions.loadMarketsChanged
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

/*
Test if we receive REST payload from markets configurations first, then the socket
 */

class V3MarketsOutOfOrderTests : V3BaseTests() {
    @Test
    fun testOutOfOrder() {
        testMarketsOnce()
    }

    @Test
    fun control() {
        testMarketsOnce(false)
    }

    private fun test(map: MutableMap<String, String>) {
        map["test"] = "1"
    }

    private fun testMarketsOnce(outOfOrder: Boolean = true) {
        var time = ServerTime.now()

        if (outOfOrder) {
            loadMarketsConfigurations()
            time = perp.log("Markets Configurations", time)
            testMarketsSubscribed()
            time = perp.log("Markets Subscribed", time)
        } else {
            testMarketsSubscribed()
            time = perp.log("Markets Subscribed", time)
            loadMarketsConfigurations()
            time = perp.log("Markets Configurations", time)
        }

        testMarketsChanged()
        perp.log("Markets Changed", time)
    }

    private fun testMarketsSubscribed() {
        loadMarkets()

        val markets = perp.internalState.marketsSummary.markets
        val ethMarket = markets["ETH-USD"]!!
        assertEquals(1753.2932, ethMarket.perpetualMarket?.oraclePrice)
        assertEquals(14.47502, ethMarket.perpetualMarket?.priceChange24H)
        assertEquals("ETH", ethMarket.perpetualMarket?.assetId)
        assertEquals("ETH-USD", ethMarket.perpetualMarket?.market)
        assertEquals(0.001, ethMarket.perpetualMarket?.configs?.stepSize)
        assertEquals(0.03, ethMarket.perpetualMarket?.configs?.maintenanceMarginFraction)
        assertEquals(0.05, ethMarket.perpetualMarket?.configs?.initialMarginFraction)
        assertEquals(0.1, ethMarket.perpetualMarket?.configs?.tickSize)
        assertEquals(true, ethMarket.perpetualMarket?.status?.canTrade)
        assertEquals(true, ethMarket.perpetualMarket?.status?.canReduce)
    }

    private fun testMarketsChanged() {
        perp.loadMarketsChanged(mock)

        val markets = perp.internalState.marketsSummary.markets
        val ethMarket = markets["ETH-USD"]!!
        assertEquals(1753.2932, ethMarket.perpetualMarket?.oraclePrice)
        assertEquals(14.47502, ethMarket.perpetualMarket?.priceChange24H)
    }
}
