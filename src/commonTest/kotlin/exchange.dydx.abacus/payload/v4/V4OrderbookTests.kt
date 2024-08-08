package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.payload.v3.V3BaseTests
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.setOrderbookGrouping
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class V4OrderbookTests : V3BaseTests() {
    private val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")

    @Test
    fun testOrderbook() {
        loadMarkets()
        loadMarketsConfigurations()

        print("--------First round----------\n")

        testOrderbookOnce()
    }

    private fun testOrderbookOnce() {
        var time = ServerTime.now()
        testReceivedOrderbook()
        time = perp.log("Orderbook", time)
    }

    private fun testReceivedOrderbook() {
        test(
            {
                perp.socket(testWsUrl, mock.orderbookChannel.halted_chain_test, 0, null)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                    "asks": [
                                        {
                                            "size": 0.907,
                                            "price": 1800.0,
                                            "depth": 0.907
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 1801.0,
                                            "depth": 1.907
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 1901.0,
                                            "depth": 2.907
                                        }
                                    ],
                                    "bids": [
                                        {
                                            "size": 0.221,
                                            "price": 1619.0,
                                            "depth": 0.221
                                        },
                                        {
                                            "size": 2.742,
                                            "price": 1520.0,
                                            "depth": 2.963
                                        },
                                        {
                                            "size": 0.02,
                                            "price": 1505.0,
                                            "depth": 2.983
                                        },
                                        {
                                            "size": 0.01,
                                            "price": 1200.0,
                                            "depth": 2.993
                                        },
                                        {
                                            "size": 1.02,
                                            "price": 1025.0,
                                            "depth": 4.013
                                        },
                                        {
                                            "size": 5.027,
                                            "price": 1000.0,
                                            "depth": 9.04
                                        },
                                        {
                                            "size": 0.075,
                                            "price": 120.0,
                                            "depth": 9.115
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 111.0,
                                            "depth": 10.115
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.setOrderbookGrouping("ETH-USD", 10)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                    "asks": [
                                        {
                                            "size": 0.907,
                                            "price": 1800.0,
                                            "depth": 0.907
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 1801.0,
                                            "depth": 1.907
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 1901.0,
                                            "depth": 2.907
                                        }
                                    ],
                                    "bids": [
                                        {
                                            "size": 0.221,
                                            "price": 1619.0,
                                            "depth": 0.221
                                        },
                                        {
                                            "size": 2.742,
                                            "price": 1520.0,
                                            "depth": 2.963
                                        },
                                        {
                                            "size": 0.02,
                                            "price": 1505.0,
                                            "depth": 2.983
                                        },
                                        {
                                            "size": 0.01,
                                            "price": 1200.0,
                                            "depth": 2.993
                                        },
                                        {
                                            "size": 1.02,
                                            "price": 1025.0,
                                            "depth": 4.013
                                        },
                                        {
                                            "size": 5.027,
                                            "price": 1000.0,
                                            "depth": 9.04
                                        },
                                        {
                                            "size": 0.075,
                                            "price": 120.0,
                                            "depth": 9.115
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 111.0,
                                            "depth": 10.115
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.setOrderbookGrouping("ETH-USD", 100)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                    "asks": [
                                        {
                                            "size": 0.907,
                                            "price": 1800.0,
                                            "depth": 0.907
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 1810.0,
                                            "depth": 1.907
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 1910.0,
                                            "depth": 2.907
                                        }
                                    ],
                                    "bids": [
                                        {
                                            "size": 0.221,
                                            "price": 1610.0,
                                            "depth": 0.221
                                        },
                                        {
                                            "size": 2.742,
                                            "price": 1520.0,
                                            "depth": 2.963
                                        },
                                        {
                                            "size": 0.02,
                                            "price": 1500.0,
                                            "depth": 2.983
                                        },
                                        {
                                            "size": 0.01,
                                            "price": 1200.0,
                                            "depth": 2.993
                                        },
                                        {
                                            "size": 1.02,
                                            "price": 1020.0,
                                            "depth": 4.013
                                        },
                                        {
                                            "size": 5.027,
                                            "price": 1000.0,
                                            "depth": 9.04
                                        },
                                        {
                                            "size": 0.075,
                                            "price": 120.0,
                                            "depth": 9.115
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 110.0,
                                            "depth": 10.115
                                        },
                                        {
                                            "size": 0.1,
                                            "price": 100.0,
                                            "depth": 10.215
                                        },
                                        {
                                            "size": 0.007,
                                            "price": 20.0,
                                            "depth": 10.222
                                        },
                                        {
                                            "size": 2.338,
                                            "price": 10.0,
                                            "depth": 12.56
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.setOrderbookGrouping("ETH-USD", 10)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                    "bids": [
                                        {
                                            "size": 0.221,
                                            "price": 1619.0,
                                            "depth": 0.221
                                        },
                                        {
                                            "size": 2.742,
                                            "price": 1520.0,
                                            "depth": 2.963
                                        },
                                        {
                                            "size": 0.02,
                                            "price": 1505.0,
                                            "depth": 2.983
                                        },
                                        {
                                            "size": 0.01,
                                            "price": 1200.0,
                                            "depth": 2.993
                                        },
                                        {
                                            "size": 1.02,
                                            "price": 1025.0,
                                            "depth": 4.013
                                        },
                                        {
                                            "size": 5.027,
                                            "price": 1000.0,
                                            "depth": 9.04
                                        },
                                        {
                                            "size": 0.075,
                                            "price": 120.0,
                                            "depth": 9.115
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 111.0,
                                            "depth": 10.115
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.setOrderbookGrouping("ETH-USD", 1)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                    "bids": [
                                        {
                                            "size": 0.221,
                                            "price": 1619.0,
                                            "depth": 0.221
                                        },
                                        {
                                            "size": 2.742,
                                            "price": 1520.0,
                                            "depth": 2.963
                                        },
                                        {
                                            "size": 0.02,
                                            "price": 1505.0,
                                            "depth": 2.983
                                        },
                                        {
                                            "size": 0.01,
                                            "price": 1200.0,
                                            "depth": 2.993
                                        },
                                        {
                                            "size": 1.02,
                                            "price": 1025.0,
                                            "depth": 4.013
                                        },
                                        {
                                            "size": 5.027,
                                            "price": 1000.0,
                                            "depth": 9.04
                                        },
                                        {
                                            "size": 0.075,
                                            "price": 120.0,
                                            "depth": 9.115
                                        },
                                        {
                                            "size": 1.0,
                                            "price": 111.0,
                                            "depth": 10.115
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
