package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.ParsingHelper.Companion.transform
import indexer.codegen.IndexerCandleResponseObject

/*
    {
			"startedAt": "2022-08-09T20:00:00.000Z",
			"updatedAt": "2022-08-09T20:00:00.000Z",
			"market": "1INCH-USD",
			"resolution": "1HOUR",
			"low": "0.8",
			"high": "0.8",
			"open": "0.8",
			"close": "0.8",
			"baseTokenVolume": "0",
			"trades": "0",
			"usdVolume": "0",
			"startingOpenInterest": "2265081"
		}

		to
            {
              "id": "1HOUR",
              "startedAtMilliseconds": 9809024589345,
              "updatedAtMilliseconds": 9809024589356,
              "low": 0.715,
              "high": 0.735,
              "open": 0.715,
              "close": 0.729,
              "baseTokenVolume": 311861,
              "usdVolume": 226946.195
            }
 */

internal interface CandleProcessorProtocol {
    fun process(payload: IndexerCandleResponseObject?): MarketCandle?
}

internal class CandleProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), CandleProcessorProtocol {
    private val candleKeyMap = mapOf(
        "double" to mapOf(
            "low" to "low",
            "high" to "high",
            "open" to "open",
            "close" to "close",
            "baseTokenVolume" to "baseTokenVolume",
            "usdVolume" to "usdVolume",
            "startingOpenInterest" to "startingOpenInterest",
        ),
        "datetime" to mapOf(
            "startedAt" to "startedAt",
            "updatedAt" to "updatedAt",
        ),
        "int" to mapOf(
            "trades" to "trades",
        ),
    )

    override fun process(
        payload: IndexerCandleResponseObject?
    ): MarketCandle? {
        if (payload == null) {
            return null
        }
        val low = parser.asDouble(payload.low)
        val high = parser.asDouble(payload.high)
        val open = parser.asDouble(payload.open)
        val close = parser.asDouble(payload.close)
        val baseTokenVolume = parser.asDouble(payload.baseTokenVolume)
        val usdVolume = parser.asDouble(payload.usdVolume)
        val startedAtMilliseconds = parser.asDatetime(payload.startedAt)?.toEpochMilliseconds()
            ?.toDouble()
        if (low == null || high == null || open == null || close == null || baseTokenVolume == null || usdVolume == null || startedAtMilliseconds == null) {
            return null
        }
        return MarketCandle(
            startedAtMilliseconds = startedAtMilliseconds,
            updatedAtMilliseconds = null,
            low = low,
            high = high,
            open = open,
            close = close,
            baseTokenVolume = baseTokenVolume,
            usdVolume = usdVolume,
            trades = parser.asInt(payload.trades),
        )
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, candleKeyMap)
    }
}
