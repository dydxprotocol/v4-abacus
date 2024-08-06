package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalOrderbook
import exchange.dydx.abacus.state.internalstate.InternalOrderbookTick
import exchange.dydx.abacus.tests.mock.calculator.OrderbookCalculatorMock
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerOrderbookResponseObject
import indexer.codegen.IndexerOrderbookResponsePriceLevel
import indexer.models.IndexerWsOrderbookUpdateResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderbookProcessorTests {
    companion object {
        internal val orderbookPayloadMock = IndexerOrderbookResponseObject(
            asks = arrayOf(
                IndexerOrderbookResponsePriceLevel(price = "1000.1", size = "1.0"),
                IndexerOrderbookResponsePriceLevel(price = "1000.5", size = "2.0"),
                IndexerOrderbookResponsePriceLevel(price = "1060.0", size = "5.0"),
                IndexerOrderbookResponsePriceLevel(price = "1800.0", size = "10.0"),
            ),
            bids = arrayOf(
                IndexerOrderbookResponsePriceLevel(price = "999.9", size = "1.0"),
                IndexerOrderbookResponsePriceLevel(price = "999.5", size = "2.0"),
                IndexerOrderbookResponsePriceLevel(price = "990.5", size = "5.0"),
                IndexerOrderbookResponsePriceLevel(price = "900.0", size = "10.0"),
            ),
        )

        internal val rawOrderbookResult = InternalOrderbook(
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
    }

    private val calculator = OrderbookCalculatorMock()
    private val orderbookProcessor = OrderbookProcessor(
        parser = Parser(),
        calculator = calculator,
    )

    @Test
    fun testProcessSubscribed() {
        val state = InternalMarketState()
        val result = orderbookProcessor.processSubscribed(
            existing = state,
            tickSize = 0.1,
            content = orderbookPayloadMock,
        )
        assertEquals(rawOrderbookResult, result.rawOrderbook)
        assertEquals(calculator.consolidateCallCount, 1)
        assertEquals(calculator.calculateCallCount, 1)
    }

    @Test
    fun testProcessChannelBatchData() {
        val state = InternalMarketState()
        var result = orderbookProcessor.processSubscribed(
            existing = state,
            tickSize = 0.1,
            content = orderbookPayloadMock,
        )
        assertEquals(calculator.consolidateCallCount, 1)
        assertEquals(calculator.calculateCallCount, 1)

        result = orderbookProcessor.processChannelBatchData(
            existing = result,
            tickSize = 0.1,
            content = listOf(
                IndexerWsOrderbookUpdateResponse(
                    asks = listOf(
                        listOf("1000.2", "1.0"),
                    ),
                    bids = null,
                ),
            ),
        )
        val asks = rawOrderbookResult.asks?.toMutableList()
        asks?.add(
            1,
            InternalOrderbookTick(
                price = 1000.2,
                size = 1.0,
            ),
        )
        assertEquals(
            rawOrderbookResult.copy(
                asks = asks,
            ),
            result.rawOrderbook,
        )
        assertEquals(calculator.consolidateCallCount, 2)
        assertEquals(calculator.calculateCallCount, 2)
    }

    @Test
    fun testProcessGrouping() {
        val state = InternalMarketState()
        val result = orderbookProcessor.processSubscribed(
            existing = state,
            tickSize = 0.1,
            content = orderbookPayloadMock,
        )
        assertEquals(calculator.consolidateCallCount, 1)
        assertEquals(calculator.calculateCallCount, 1)

        orderbookProcessor.processGrouping(
            existing = result,
            tickSize = 0.1,
            groupingMultiplier = 10,
        )
        assertEquals(calculator.calculateCallCount, 2)
    }
}
