package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.tests.mock.processor.markets.MarketProcessorMock
import exchange.dydx.abacus.utils.Parser
import indexer.models.IndexerWsMarketUpdateResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class MarketsProcessorTests {
    private val marketProcessor = MarketProcessorMock()
    private val processor = MarketsProcessor(
        parser = Parser(),
        marketProcessor = marketProcessor,
        calculateSparklines = false,
    )

    @Test
    fun testProcessSubscribed() {
        val state = InternalMarketSummaryState()
        marketProcessor.processAction = { _, _ ->
            MarketProcessorTests.outputMock
        }

        val payload = mapOf(
            "BTC-USD" to MarketProcessorTests.marketPayloadMock,
            "ETH-USD" to MarketProcessorTests.marketPayloadMock,
        )
        val result = processor.processSubscribed(state, payload)
        assertEquals(2, result.markets.size)
    }

    @Test
    fun testProcessChannelData() {
        val state = InternalMarketSummaryState()
        marketProcessor.processAction = { _, _ ->
            MarketProcessorTests.outputMock
        }

        val payload = IndexerWsMarketUpdateResponse(
            trading = mapOf(
                "BTC-USD" to MarketProcessorTests.marketPayloadMock,
                "ETH-USD" to MarketProcessorTests.marketPayloadMock,
            ),
        )
        val result = processor.processChannelData(state, payload)
        assertEquals(2, result.markets.size)

        val oraclePricePayload = IndexerWsMarketUpdateResponse(
            oraclePrices = mapOf(
                "BTC-USD" to MarketProcessorTests.oraclePricePayloadMock,
                "ETH-USD" to MarketProcessorTests.oraclePricePayloadMock,
            ),
        )
        marketProcessor.processOraclePriceAction = { _, _ ->
            MarketProcessorTests.outputMock
        }
        val updatedResult = processor.processChannelData(state, oraclePricePayload)
        assertEquals(2, updatedResult.markets.size)
        assertEquals(20000.0, updatedResult.markets["BTC-USD"]?.perpetualMarket?.oraclePrice)
    }

    @Test
    fun testProcessBatchChannelData() {
        val state = InternalMarketSummaryState()
        val payload = listOf(
            IndexerWsMarketUpdateResponse(
                trading = mapOf(
                    "BTC-USD" to MarketProcessorTests.marketPayloadMock,
                    "ETH-USD" to MarketProcessorTests.marketPayloadMock,
                ),
            ),
            IndexerWsMarketUpdateResponse(
                oraclePrices = mapOf(
                    "BTC-USD" to MarketProcessorTests.oraclePricePayloadMock,
                    "ETH-USD" to MarketProcessorTests.oraclePricePayloadMock,
                ),
            ),
        )
        val result = processor.processChannelBatchData(state, payload)
        assertEquals(2, marketProcessor.processCallCount)
        assertEquals(2, marketProcessor.processOraclePriceCallCount)
    }

    @Test
    fun testProcessSparklines() {
        val state = InternalMarketSummaryState()
        marketProcessor.processAction = { _, _ ->
            MarketProcessorTests.outputMock
        }

        val payload = mapOf(
            "BTC-USD" to MarketProcessorTests.marketPayloadMock,
            "ETH-USD" to MarketProcessorTests.marketPayloadMock,
        )
        processor.processSubscribed(state, payload)

        val sparklines = mapOf(
            "BTC-USD" to listOf("1", "2", "3"),
            "ETH-USD" to listOf("1", "2", "3"),
        )
        val result = processor.processSparklines(state, sparklines)
        assertEquals(2, marketProcessor.processSparklinesCallCount)
        assertEquals(2, result.markets.size)
    }
}
