package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.tests.extensions.*
import exchange.dydx.abacus.utils.ServerTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class V3CandlesTests : V3BaseTests() {
    @Test
    fun testCandles() {
        loadMarkets()
        loadMarketsConfigurations()

        print("--------First round----------\n")

        testCandlesOnce()
    }


    private fun testCandlesOnce() {
        var time = ServerTime.now()
        testCandlesAllMarkets()
        time = perp.log("Candles All Markets", time)

        testCandlesFirstCall()
        time = perp.log("Candles First Call", time)

        testCandlesSecondCall()
        perp.log("Candles Second Call", time)
    }

    private fun testCandlesAllMarkets() {
        test(
            {
                perp.loadCandlesAllMarkets(mock)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "candles": {
                                    "1HOUR": [
                                        {
                                            "low": 1785.7,
                                            "high": 1797.4,
                                            "open": 1785.7,
                                            "close": 1797.4,
                                            "baseTokenVolume": 152.181,
                                            "usdVolume": 272650.369,
                                            "startedAt": "2022-08-08T20:00:00.000Z",
                                            "updatedAt": "2022-08-08T20:59:35.718Z"
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testCandlesFirstCall() {
        test(
            {
                perp.loadCandlesFirst(mock)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "candles": {
                                    "15MINS": [
                                        {
                                            "low": 1780.6,
                                            "high": 1782.3,
                                            "open": 1780.6,
                                            "close": 1782.3,
                                            "baseTokenVolume": 35.402,
                                            "usdVolume": 63058.9366,
                                            "startedAt": "2022-08-08T19:30:00.000Z",
                                            "updatedAt": "2022-08-08T19:34:16.371Z"
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testCandlesSecondCall() {
        test(
            {
                perp.loadCandlesSecond(mock)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "candles": {
                                    "15MINS": [
                                        {
                                            "low": 1709.7,
                                            "high": 1709.7,
                                            "open": 1709.7,
                                            "close": 1709.7,
                                            "baseTokenVolume": 0.012,
                                            "usdVolume": 20.5164,
                                            "startedAt": "2022-08-07T18:30:00.000Z",
                                            "updatedAt": "2022-08-07T18:44:10.633Z"
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }
}