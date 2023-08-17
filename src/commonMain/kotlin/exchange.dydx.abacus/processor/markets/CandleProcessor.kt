package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

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
internal class CandleProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val candleKeyMap = iMapOf(
        "double" to iMapOf(
            "low" to "low",
            "high" to "high",
            "open" to "open",
            "close" to "close",
            "baseTokenVolume" to "baseTokenVolume",
            "usdVolume" to "usdVolume",
        ),
        "datetime" to iMapOf(
            "startedAt" to "startedAt",
            "updatedAt" to "updatedAt",
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        return transform(existing, payload, candleKeyMap)
    }
}