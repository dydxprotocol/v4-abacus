package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.tests.extensions.loadTrades
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class V3MarketsDelayedTests: V3BaseTests() {
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
            }
        )
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
            }
        )
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
            }
        )
    }

    @Test
    fun testTradesFirst() {
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
            }
        )
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
            }
        )
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
            }
        )
    }
}