package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerCandleResolution
import indexer.codegen.IndexerCandleResponseObject
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class CandleProcessorTests {
    companion object {
        val startedAt = Instant.parse("2022-08-09T20:00:00.000Z")
        val candlePayloadMock = IndexerCandleResponseObject(
            high = "0.91",
            low = "0.6",
            startedAt = startedAt.toString(),
            ticker = null,
            resolution = IndexerCandleResolution._1HOUR,
            open = "0.8",
            close = "0.9",
            baseTokenVolume = "200",
            usdVolume = "180",
            trades = 10.0,
            startingOpenInterest = "1000",
            id = null,
        )

        val resultMock = MarketCandle(
            startedAtMilliseconds = startedAt.toEpochMilliseconds().toDouble(),
            updatedAtMilliseconds = null,
            low = 0.6,
            high = 0.91,
            open = 0.8,
            close = 0.9,
            baseTokenVolume = 200.0,
            usdVolume = 180.0,
            trades = 10,
        )
    }

    private val processor = CandleProcessor(parser = Parser())

    @Test
    fun testProcess() {
        val result = processor.process(candlePayloadMock)
        assertEquals(resultMock, result)
    }
}
