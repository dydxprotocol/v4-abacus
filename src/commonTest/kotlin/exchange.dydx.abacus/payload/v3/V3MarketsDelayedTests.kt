package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.tests.extensions.loadTrades
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class V3MarketsDelayedTests : V3BaseTests() {
    @Test
    fun testOrderbookFirst() {
        test(
            {
                loadOrderbook()
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
            {
                assertNull(perp.state?.marketsSummary)
            },
        )

        if (perp.staticTyping) {
            perp.loadTrades(mock)
            assertNull(perp.state?.marketsSummary)
        } else {
            test(
                {
                    perp.loadTrades(mock)
                },
                """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                },
                                "trades": [
                                ]
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                    assertNull(perp.state?.marketsSummary)
                },
            )
        }

        test(
            {
                loadMarkets()
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
            {
                assertNotNull(perp.state?.marketsSummary)
            },
        )
    }

    @Test
    fun testTradesFirst() {
        if (perp.staticTyping) {
            perp.loadTrades(mock)
            val market = perp.internalState.marketsSummary.markets.get("ETH-USD")
            assertTrue { market?.trades?.isNotEmpty() == true }
            assertNull(perp.state?.marketsSummary)
        } else {
            test(
                {
                    perp.loadTrades(mock)
                },
                """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "trades": [
                                ]
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                    assertNull(perp.state?.marketsSummary)
                },
            )
        }

        if (perp.staticTyping) {
            loadOrderbook()
            val market = perp.internalState.marketsSummary.markets.get("ETH-USD")
            assertTrue { market?.trades?.isNotEmpty() == true }
            assertNull(perp.state?.marketsSummary)
        } else {
            test(
                {
                    loadOrderbook()
                },
                """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                },
                                "trades": [
                                ]
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                    assertNull(perp.state?.marketsSummary)
                },
            )
        }

        if (perp.staticTyping) {
            loadMarkets()
            val market = perp.internalState.marketsSummary.markets.get("ETH-USD")
            assertTrue { market?.trades?.isNotEmpty() == true }
            assertNotNull(perp.state?.marketsSummary)
        } else {
            test(
                {
                    loadMarkets()
                },
                """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "trades": [
                                ]
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                    assertNotNull(perp.state?.marketsSummary)
                },
            )
        }
    }
}
