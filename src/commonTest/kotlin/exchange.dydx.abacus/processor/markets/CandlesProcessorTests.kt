package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.tests.mock.processor.markets.CandleProcessorMock
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerCandleResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class CandlesProcessorTests {

    private val itemProcessor = CandleProcessorMock()
    private val processor = CandlesProcessor(parser = Parser(), itemProcessor = itemProcessor)

    @Test
    fun testProcessSubscribed() {
        // Given
        val existing = InternalMarketState()
        val resolution = "1HOUR"
        val payload = IndexerCandleResponse(
            candles = arrayOf(CandleProcessorTests.candlePayloadMock),
        )
        itemProcessor.processAction = { it?.let { CandleProcessorTests.resultMock } }

        // When
        val result = processor.processSubscribed(existing, resolution, payload)

        // Then
        val expected = mapOf(
            resolution to listOf(CandleProcessorTests.resultMock),
        )
        assertEquals(expected, result.candles?.toMap())
    }

    @Test
    fun testProcessBatchUpdate() {
        // Given
        val existing = InternalMarketState()
        val resolution = "1HOUR"
        val payload = IndexerCandleResponse(
            candles = arrayOf(CandleProcessorTests.candlePayloadMock),
        )
        itemProcessor.processAction = { it?.let { CandleProcessorTests.resultMock } }

        var result = processor.processSubscribed(existing, resolution, payload)
        assertEquals(result.candles?.get(resolution)?.size, 1)

        val updatePayload = listOf(CandleProcessorTests.candlePayloadMock)
        itemProcessor.processAction = {
            it?.let {
                CandleProcessorTests.resultMock.copy(
                    startedAtMilliseconds = CandleProcessorTests.startedAt.plus(2.hours)
                        .toEpochMilliseconds().toDouble(),
                )
            }
        }

        // When
        result = processor.processBatchUpdate(result, resolution, updatePayload)

        // Then
        assertEquals(result.candles?.get(resolution)?.size, 2)
    }

    @Test
    fun testProcessUpdate() {
        // Given
        val existing = InternalMarketState()
        val resolution = "1HOUR"
        val payload = IndexerCandleResponse(
            candles = arrayOf(CandleProcessorTests.candlePayloadMock),
        )
        itemProcessor.processAction = { it?.let { CandleProcessorTests.resultMock } }

        var result = processor.processSubscribed(existing, resolution, payload)
        assertEquals(result.candles?.get(resolution)?.size, 1)

        val updatePayload = CandleProcessorTests.candlePayloadMock
        itemProcessor.processAction = {
            it?.let {
                CandleProcessorTests.resultMock.copy(
                    startedAtMilliseconds = CandleProcessorTests.startedAt.plus(2.hours)
                        .toEpochMilliseconds().toDouble(),
                )
            }
        }

        // When
        result = processor.processUpdate(result, resolution, updatePayload)

        // Then
        assertEquals(result.candles?.get(resolution)?.size, 2)
    }
}
