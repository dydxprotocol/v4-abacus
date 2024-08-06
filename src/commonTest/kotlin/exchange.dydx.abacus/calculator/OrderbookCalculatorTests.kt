package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.output.MarketOrderbookGrouping
import exchange.dydx.abacus.output.OrderbookLine
import exchange.dydx.abacus.state.internalstate.InternalOrderbook
import exchange.dydx.abacus.state.internalstate.InternalOrderbookTick
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.utils.Parser
import kollections.iListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderbookCalculatorTests {

    companion object {
        internal val rawOrderbook = InternalOrderbook(
            asks = listOf(
                InternalOrderbookTick(
                    price = 1000.1,
                    size = 1.0,
                ),
                InternalOrderbookTick(
                    price = 1000.5,
                    size = 2.0,
                ),
                InternalOrderbookTick(
                    price = 1060.0,
                    size = 5.0,
                ),
                InternalOrderbookTick(
                    price = 1800.0,
                    size = 10.0,
                ),
            ),
            bids = listOf(
                InternalOrderbookTick(
                    price = 999.9,
                    size = 1.0,
                ),
                InternalOrderbookTick(
                    price = 999.5,
                    size = 2.0,
                ),
                InternalOrderbookTick(
                    price = 990.5,
                    size = 5.0,
                ),
                InternalOrderbookTick(
                    price = 900.0,
                    size = 10.0,
                ),
            ),
        )

        val marketOrderbook = MarketOrderbook(
            midPrice = 1000.0,
            spread = 0.2,
            spreadPercent = 0.0002,
            asks = iListOf(
                OrderbookLine(
                    price = 1000.1,
                    size = 1.0,
                    sizeCost = 1000.1,
                    depth = 1.0,
                    depthCost = 1000.1,
                ),
                OrderbookLine(
                    price = 1000.5,
                    size = 2.0,
                    sizeCost = 2001.0,
                    depth = 3.0,
                    depthCost = 3001.1,
                ),
                OrderbookLine(
                    price = 1060.0,
                    size = 5.0,
                    sizeCost = 5300.0,
                    depth = 8.0,
                    depthCost = 8301.1,
                ),
                OrderbookLine(
                    price = 1800.0,
                    size = 10.0,
                    sizeCost = 18000.0,
                    depth = 18.0,
                    depthCost = 26301.1,
                ),
            ),
            bids = iListOf(
                OrderbookLine(
                    price = 999.9,
                    size = 1.0,
                    sizeCost = 999.9,
                    depth = 1.0,
                    depthCost = 999.9,
                ),
                OrderbookLine(
                    price = 999.5,
                    size = 2.0,
                    sizeCost = 1999.0,
                    depth = 3.0,
                    depthCost = 2998.9,
                ),
                OrderbookLine(
                    price = 990.5,
                    size = 5.0,
                    sizeCost = 4952.5,
                    depth = 8.0,
                    depthCost = 7951.4,
                ),
                OrderbookLine(
                    price = 900.0,
                    size = 10.0,
                    sizeCost = 9000.0,
                    depth = 18.0,
                    depthCost = 16951.4,
                ),
            ),
            grouping = MarketOrderbookGrouping(
                multiplier = OrderbookGrouping.none,
                tickSize = 0.1,
            ),
        )
    }

    private val calculator = OrderbookCalculator(
        parser = Parser(),
    )

    @Test
    fun testCalculate() {
        val result = calculator.calculate(
            rawOrderbook = rawOrderbook,
            tickSize = 0.1,
            groupingMultiplier = 1,
        )
        requireNotNull(result)
        assertEquals(marketOrderbook.asks, result.asks)
        assertEquals(marketOrderbook.bids, result.bids)
        assertEquals(marketOrderbook.midPrice!!, result.midPrice!!, 0.0001)
        assertEquals(marketOrderbook.spread!!, result.spread!!, 0.0001)
        assertEquals(marketOrderbook.spreadPercent!!, result.spreadPercent!!, 0.0001)
    }

    @Test
    fun testConsolidate() {
        val crossedItem = InternalOrderbookTick(
            price = 999.1,
            size = 1.0,
        )
        val asks = listOf(crossedItem) + rawOrderbook.asks!!
        val result = calculator.consolidate(
            rawOrderbook = rawOrderbook.copy(
                asks = asks,
            ),
        )
        requireNotNull(result)
        assertEquals(result.bids?.size, 3)
    }
}
