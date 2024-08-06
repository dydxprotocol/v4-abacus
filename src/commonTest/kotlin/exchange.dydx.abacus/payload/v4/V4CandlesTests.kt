package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.tests.extensions.loadCandlesAllMarkets
import exchange.dydx.abacus.tests.extensions.loadCandlesFirst
import exchange.dydx.abacus.tests.extensions.loadCandlesSecond
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class V4CandlesTests : V3BaseTests() {
    private val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")

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

        testCandlesSubscribed()
        perp.log("Candles Subscribed", time)

        testCandlesChannelData()
        perp.log("Candles Channel Data", time)

        testCandlesChannelBatchData()
        perp.log("Candles Channel Batch Data", time)
    }

    private fun testCandlesAllMarkets() {
        if (perp.staticTyping) {
            perp.loadCandlesAllMarkets(mock)
            val market = perp.internalState.marketsSummary.markets["ETH-USD"]
            assertEquals(1, market?.candles?.size)
            val firstItem = market?.candles?.get("1HOUR")?.first()
            assertEquals(1785.7, firstItem?.open)
            assertEquals(1797.4, firstItem?.close)
        } else {
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
                """.trimIndent(),
            )
        }
    }

    private fun testCandlesFirstCall() {
        if (perp.staticTyping) {
            perp.loadCandlesFirst(mock)
            val market = perp.internalState.marketsSummary.markets["ETH-USD"]
            val firstItem = market?.candles?.get("15MINS")?.first()
            assertEquals(1780.6, firstItem?.open)
            assertEquals(1782.3, firstItem?.close)
        } else {
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
                """.trimIndent(),
            )
        }
    }

    private fun testCandlesSecondCall() {
        if (perp.staticTyping) {
            perp.loadCandlesSecond(mock)
            val market = perp.internalState.marketsSummary.markets["ETH-USD"]
            val firstItem = market?.candles?.get("15MINS")?.first()
            assertEquals(1709.7, firstItem?.open)
            assertEquals(1709.7, firstItem?.close)
        } else {
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
                """.trimIndent(),
            )
        }
    }

    private fun testCandlesSubscribed() {
        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.candles.v4_subscribed, 0, null)
            val market = perp.internalState.marketsSummary.markets["ETH-USD"]
            val firstItem = market?.candles?.get("1HOUR")?.first()
            assertEquals(1785.7, firstItem?.open)
            assertEquals(1797.4, firstItem?.close)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.candles.v4_subscribed, 0, null)
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
                """.trimIndent(),
                { response ->
                    assertEquals(
                        125,
                        response.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")?.size,
                    )
                },
            )
        }
    }

    private fun testCandlesChannelData() {
        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.candles.v4_channel_data, 0, null)
            val market = perp.internalState.marketsSummary.markets["ETH-USD"]
            val firstItem = market?.candles?.get("15MINS")?.first()
            assertEquals(1709.7, firstItem?.open)
            assertEquals(1709.7, firstItem?.close)

            val candles = perp.internalState.marketsSummary.markets["ETH-USD"]?.candles?.get("1HOUR")
            assertEquals(125, candles?.size)
            val lastCandle = candles?.last()
            assertEquals(1582.8, lastCandle?.close)
            assertEquals(1577.7, lastCandle?.open)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.candles.v4_channel_data, 0, null)
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
                """.trimIndent(),
                { response ->
                    assertEquals(
                        125,
                        response.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")?.size,
                    )
                    val lastCandle =
                        response.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")?.last()
                    assertEquals(1582.8, lastCandle?.close)
                    assertEquals(1577.7, lastCandle?.open)
                },
            )
        }
    }

    private fun testCandlesChannelBatchData() {
        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.candles.v4_channel_batch_data, 0, null)
            val candles = perp.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")
            assertEquals(126, candles?.size)
            val lastCandle = candles?.last()
            assertEquals(1590.8, lastCandle?.close)
            assertEquals(1598.0, lastCandle?.open)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.candles.v4_channel_batch_data, 0, null)
                },
                """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "candles": {
                                    "1HOUR": [
                                        {
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
                { response ->
                    assertEquals(
                        126,
                        response.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")?.size,
                    )
                    val lastCandle =
                        response.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")?.last()
                    assertEquals(1590.8, lastCandle?.close)
                    assertEquals(1598.0, lastCandle?.open)
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.candles.v4_channel_batch_data_2, 0, null)
            val candles = perp.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")
            assertEquals(126, candles?.size)
            val lastCandle = candles?.last()
            assertEquals(1592.7, lastCandle?.close)
            assertEquals(1598.0, lastCandle?.open)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.candles.v4_channel_batch_data_2, 0, null)
                },
                """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "candles": {
                                    "1HOUR": [
                                        {
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
                { response ->
                    assertEquals(
                        126,
                        response.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")?.size,
                    )
                    val lastCandle =
                        response.state?.candles?.get("ETH-USD")?.candles?.get("1HOUR")?.last()
                    assertEquals(1592.7, lastCandle?.close)
                    assertEquals(1598.0, lastCandle?.open)
                },
            )
        }
    }
}
