package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.tests.extensions.loadv4MarketsBatchChanged
import exchange.dydx.abacus.tests.extensions.loadv4MarketsChanged
import exchange.dydx.abacus.tests.extensions.loadv4MarketsSubscribed
import exchange.dydx.abacus.tests.extensions.loadv4TradesBatchChanged
import exchange.dydx.abacus.tests.extensions.loadv4TradesChanged
import exchange.dydx.abacus.tests.extensions.loadv4TradesSubscribed
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class V4MarketsTests : V4BaseTests() {
    override fun setup() {
        loadMarketsConfigurations()
    }

    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testMarketsOnce()

        reset()

        print("--------Second round----------\n")

        testMarketsOnce()

        testMarketNotOnline()
    }

    @Test
    fun testDataFeedWithV4Data() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        var time = ServerTime.now()
        testMarketsV4r1Subscribed()
        time = perp.log("Markets Subscribed", time)
    }

    private fun testMarketsOnce() {
        var time = ServerTime.now()
        testMarketsSubscribed()
        time = perp.log("Markets Subscribed", time)

        testMarketsSparklinesChanged()
        time = perp.log("Sparklines Changed", time)

        testMarketsSubscribed()
        time = perp.log("Markets Subscribed", time)

        testMarketsChanged()
        time = perp.log("Markets Changed", time)

        testMarketsBatchChanged()
        time = perp.log("Markets Batch Changed", time)

        testTradesSubscribed()
        time = perp.log("Trades Subscribed", time)

        testTradesChanged()
        time = perp.log("Trades Changed", time)

        testTradesBatchChanged()
        time = perp.log("Trades Batch Changed", time)

        testEffectiveIMF()
        time = perp.log("Calculate Effective IMF", time)
    }

    private fun testMarketsSubscribed() {
        perp.loadv4MarketsSubscribed(mock, testWsUrl)

        val markets = perp.internalState.marketsSummary.markets

        val btcMarket = markets["BTC-USD"]
        assertEquals(btcMarket?.perpetualMarket?.status?.canTrade, true)
        assertEquals(btcMarket?.perpetualMarket?.status?.canReduce, true)
        assertEquals(btcMarket?.perpetualMarket?.priceChange24H, 0.0)
        assertEquals(btcMarket?.perpetualMarket?.oraclePrice, 0.0)
        assertEquals(btcMarket?.perpetualMarket?.configs?.clobPairId, "0")
        assertEquals(btcMarket?.perpetualMarket?.configs?.maintenanceMarginFraction, 0.03)
        assertEquals(btcMarket?.perpetualMarket?.configs?.incrementalInitialMarginFraction, 0.0)
        assertEquals(btcMarket?.perpetualMarket?.configs?.incrementalPositionSize, 0.0)
        assertEquals(btcMarket?.perpetualMarket?.configs?.initialMarginFraction, 0.05)
        assertEquals(btcMarket?.perpetualMarket?.configs?.baselinePositionSize, 0.0)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.volume24H, 4.936082546194518E8)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.openInterest, 3530.502834378)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.nextFundingRate, 0.0)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.trades24H, 922707.0)

        val ethMarket = markets["ETH-USD"]
        assertEquals(ethMarket?.perpetualMarket?.status?.canTrade, true)
        assertEquals(ethMarket?.perpetualMarket?.status?.canReduce, true)
        assertEquals(ethMarket?.perpetualMarket?.priceChange24H, 0.0)
        assertEquals(ethMarket?.perpetualMarket?.oraclePrice, 1000.0)
        assertEquals(ethMarket?.perpetualMarket?.configs?.clobPairId, "1")
        assertEquals(ethMarket?.perpetualMarket?.configs?.maintenanceMarginFraction, 0.03)
        assertEquals(ethMarket?.perpetualMarket?.configs?.incrementalInitialMarginFraction, 0.0)
        assertEquals(ethMarket?.perpetualMarket?.configs?.incrementalPositionSize, 0.0)
        assertEquals(ethMarket?.perpetualMarket?.configs?.initialMarginFraction, 0.05)
        assertEquals(ethMarket?.perpetualMarket?.configs?.baselinePositionSize, 0.0)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.volume24H, 4.931478367879293E8)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.openInterest, 46115.049878)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.nextFundingRate, 0.0)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.trades24H, 939311.0)

        val marketIds = perp.state?.marketIds()
        assertNotNull(marketIds)
        val first = marketIds.firstOrNull()
        assertNotNull(first)
        val market = perp.state?.market(first)
        assertNotNull(first)
    }

    private fun testMarketsV4r1Subscribed() {
        perp.socket(testWsUrl, mock.marketsChannel.v4_subscribed_r1, 0, null)

        val markets = perp.internalState.marketsSummary.markets
        val btcMarket = markets["BTC-USD"]
        assertEquals(btcMarket?.perpetualMarket?.configs?.clobPairId, "0")
        assertEquals(btcMarket?.perpetualMarket?.configs?.stepSize, 1.0E-9)
        assertEquals(btcMarket?.perpetualMarket?.configs?.minOrderSize, 1.0E-9)
        val ethMarket = markets["ETH-USD"]
        assertEquals(ethMarket?.perpetualMarket?.configs?.clobPairId, "1")

        val marketIds = perp.state?.marketIds()
        assertNotNull(marketIds)
        val first = marketIds.firstOrNull()
        assertNotNull(first)
        val market = perp.state?.market(first)
        assertNotNull(first)
    }

    private fun testMarketsSparklinesChanged() {
        perp.rest(
            url = AbUrl.fromString("$testRestUrl/v4/sparklines?timePeriod=ONE_DAY"),
            payload = mock.candles.v4SparklinesFirstCall,
            subaccountNumber = 0,
            height = null,
        )
        val btcLine =
            perp.internalState.marketsSummary.markets["BTC-USD"]?.perpetualMarket?.perpetual?.line
        assertEquals(btcLine?.get(0), 29308.0)
        assertEquals(btcLine?.get(1), 29373.0)
    }

    private fun testMarketsChanged() {
        perp.loadv4MarketsChanged(mock, testWsUrl)
        val markets = perp.internalState.marketsSummary.markets
        val btcMarket = markets["BTC-USD"]
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.openInterest, 3531.250439547)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.volume24H, 493681565.92757831256)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.trades24H, 922900.0)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.nextFundingRate, 0.0)
        val lines = btcMarket?.perpetualMarket?.perpetual?.line
        assertEquals(lines?.get(0), 29308.0)
        assertEquals(lines?.get(1), 29373.0)

        val ethMarket = markets["ETH-USD"]
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.openInterest, 46115.767606)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.volume24H, 493203231.416110155)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.trades24H, 939491.0)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.nextFundingRate, 0.0)
        val ethLines = ethMarket?.perpetualMarket?.perpetual?.line
        assertEquals(ethLines?.get(0), 1900.3)
        assertEquals(ethLines?.get(1), 1902.6)
    }

    private fun testMarketsBatchChanged() {
        perp.loadv4MarketsBatchChanged(mock, testWsUrl)

        var markets = perp.internalState.marketsSummary.markets
        var btcMarket = markets["BTC-USD"]
        assertEquals(btcMarket?.perpetualMarket?.oraclePrice, 0.0)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.openInterest, 5082.297882905)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.volume24H, 626611363.85036835076)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.trades24H, 1205976.0)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.nextFundingRate, 0.0)

        var ethMarket = markets["ETH-USD"]
        assertEquals(ethMarket?.perpetualMarket?.oraclePrice, 1000.0)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.openInterest, 67603.376057)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.volume24H, 626131271.611287094)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.trades24H, 1214631.0)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.nextFundingRate, 0.0)

        val mlnMarket = markets["MLN-USD"]
        assertEquals(mlnMarket?.perpetualMarket?.oraclePrice, null)
        assertEquals(mlnMarket?.perpetualMarket?.perpetual?.openInterest, null)
        assertEquals(mlnMarket?.perpetualMarket?.configs?.stepSize, 0.1)
        assertEquals(mlnMarket?.perpetualMarket?.configs?.tickSize, 0.01)

        perp.socket(
            url = testWsUrl,
            jsonString = mock.marketsChannel.v4_channel_batch_data_oracle_prices,
            subaccountNumber = 0,
            height = null,
        )

        markets = perp.internalState.marketsSummary.markets
        btcMarket = markets["BTC-USD"]
        assertEquals(btcMarket?.perpetualMarket?.oraclePrice, 21000.0)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.openInterest, 5082.297882905)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.volume24H, 626611363.85036835076)
        assertEquals(btcMarket?.perpetualMarket?.perpetual?.trades24H, 1205976.0)

        ethMarket = markets["ETH-USD"]
        assertEquals(ethMarket?.perpetualMarket?.oraclePrice, 1000.0)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.openInterest, 67603.376057)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.volume24H, 626131271.611287094)
        assertEquals(ethMarket?.perpetualMarket?.perpetual?.trades24H, 1214631.0)
    }

    private fun testTradesSubscribed() {
        perp.loadv4TradesSubscribed(mock, testWsUrl)
        val trades = perp.state?.trades?.get("ETH-USD")
        assertEquals(trades?.size, 100)
        val firstItem = trades?.firstOrNull()
        assertEquals(firstItem?.side?.rawValue, "SELL")
        assertEquals(firstItem?.size, 9.5E-4)
        assertEquals(firstItem?.price, 1255.98)
    }

    private fun testTradesChanged() {
        perp.loadv4TradesChanged(mock, testWsUrl)
        val trades = perp.state?.trades?.get("ETH-USD")
        assertEquals(trades?.size, 101)
        val firstItem = trades?.firstOrNull()
        assertEquals(firstItem?.side?.rawValue, "BUY")
        assertEquals(firstItem?.size, 1.593707)
        assertEquals(firstItem?.price, 1255.949)
    }

    private fun testTradesBatchChanged() {
        perp.loadv4TradesBatchChanged(mock, testWsUrl)
        val trades = perp.state?.trades?.get("ETH-USD")
        assertEquals(trades?.size, 240)
        val firstItem = trades?.firstOrNull()
        assertEquals(firstItem?.side?.rawValue, "SELL")
        assertEquals(firstItem?.size, 1.02E-4)
        assertEquals(firstItem?.price, 1291.255)
    }

    private fun testMarketNotOnline() {
        perp.loadv4MarketsSubscribed(mock, testWsUrl)
        val trades = perp.state?.trades?.get("ETH-USD")
        assertEquals(trades?.size, 240)
    }

    @Test
    fun testInitializingMarkets() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        perp.socket(
            url = testWsUrl,
            jsonString = mock.marketsChannel.v4_subscribed_with_initializing_status,
            subaccountNumber = 0,
            height = null,
        )
        var markets = perp.internalState.marketsSummary.markets
        var market = markets["MATIC-USD"]
        assertEquals(market?.perpetualMarket?.status?.canTrade, false)
        assertEquals(market?.perpetualMarket?.status?.canReduce, false)

        var marketIds = perp.state?.marketIds()
        assertNotNull(marketIds)
        var first = marketIds.firstOrNull()
        assertNotNull(first)
        var marketState = perp.state?.market(first)
        assertNotNull(marketState)

        perp.socket(
            url = testWsUrl,
            jsonString = mock.marketsChannel.v4_channel_batch_data_oracle_prices_for_initializing_status,
            subaccountNumber = 0,
            height = null,
        )

        markets = perp.internalState.marketsSummary.markets
        market = markets["MATIC-USD"]
        assertEquals(market?.perpetualMarket?.status?.canTrade, false)
        assertEquals(market?.perpetualMarket?.status?.canReduce, false)
        assertEquals(market?.perpetualMarket?.oraclePrice, 0.5648517536)

        marketIds = perp.state?.marketIds()
        assertNotNull(marketIds)
        first = marketIds.firstOrNull()
        assertNotNull(first)
        marketState = perp.state?.market(first)
        assertNotNull(marketState)
    }

    private fun testEffectiveIMF() {
        perp.socket(
            url = testWsUrl,
            jsonString = mock.marketsChannel.v4_subscribed_for_effective_imf_calculation,
            subaccountNumber = 0,
            height = null,
        )

        val markets = perp.internalState.marketsSummary.markets
        val btcMarket = markets["BTC-USD"]
        assertEquals(btcMarket?.perpetualMarket?.configs?.effectiveInitialMarginFraction, 0.0523)

        val ethMarket = markets["ETH-USD"]
        assertEquals(
            ethMarket?.perpetualMarket?.configs?.effectiveInitialMarginFraction,
            0.6464285714285715,
        )

        val maticMarket = markets["MATIC-USD"]
        assertEquals(maticMarket?.perpetualMarket?.configs?.effectiveInitialMarginFraction, 1.0)

        val enjMarket = markets["ENJ-USD"]
        assertEquals(enjMarket?.perpetualMarket?.configs?.effectiveInitialMarginFraction, 0.05)
    }
}
