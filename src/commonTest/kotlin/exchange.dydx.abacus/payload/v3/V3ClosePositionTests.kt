package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.state.modal.ClosePositionInputField
import exchange.dydx.abacus.state.modal.closePosition
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class V3ClosePositionTests : V3BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        var time = ServerTime.now()
        testClosePositionInput()
        time = perp.log("Close Position", time)

        testCloseShortPositionInput()
        time = perp.log("Close Position", time)
    }

    override fun setup() {
        loadMarkets()
        loadMarketsConfigurations()
        // do not load account
        loadOrderbook()
        loadAccounts()
    }

    private fun testClosePositionInput() {
        /*
        Initial setup
         */
        test(
            {
                perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)
            }, """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "SELL",
                        "reduceOnly": true
                    }
                }
            }
        """.trimIndent()
        )

        test(
            {
                perp.closePosition("0.25", ClosePositionInputField.percent, 0)
            }, """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "SELL",
                        "size": {
                            "percent": 0.25,
                            "input": "size.percent",
                            "size": 2.3392E+1,
                            "usdcSize": 38684.3479
                        },
                        "reduceOnly": true,
                        "summary": {
                            "price": 1653.74,
                            "size": 23.392,
                            "usdcSize": 38684.3479,
                            "total": 38684.3479
                        }
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": -62697.28,
                                    "postOrder": -24012.93
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": 93.57,
                                            "postOrder": 70.178
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()
        )


        test(
            {
                perp.closePosition("15", ClosePositionInputField.size, 0)
            }, """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "SELL",
                        "size": {
                            "percent": null,
                            "input": "size.size",
                            "size": 15,
                            "usdcSize": 24812.3719
                        },
                        "reduceOnly": true,
                        "summary": {
                            "price": 1654.16,
                            "size": 15.0,
                            "usdcSize": 24812.37,
                            "total": 24812.37
                        }
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": -62697.28,
                                    "postOrder": -37884.91
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": 93.57,
                                            "postOrder": 78.57
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()
        )
    }

    private fun testCloseShortPositionInput() {
        test(
            {
                perp.socket(mock.socketUrl, mock.accountsChannel.v4_subscribed, 0, null)
            }, """
                {
                }
            """.trimIndent()
        )
        /*
        Initial setup
         */
        test(
            {
                perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)
            }, """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "BUY",
                        "reduceOnly": true
                    }
                }
            }
        """.trimIndent()
        )

        test(
            {
                perp.closePosition("0.25", ClosePositionInputField.percent, 0)
            }, """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "BUY",
                        "size": {
                            "percent": 0.25,
                            "input": "size.percent",
                            "size": 2.6544E+1
                        },
                        "reduceOnly": true
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": 68257.22,
                                    "postOrder": 24308.31
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -106.18,
                                            "postOrder": -79.64
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()
        )


        test(
            {
                perp.closePosition("15", ClosePositionInputField.size, 0)
            }, """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "BUY",
                        "size": {
                            "percent": null,
                            "input": "size.size",
                            "size": 15,
                            "usdcSize": 24835.5
                        },
                        "reduceOnly": true,
                        "summary": {
                            "price": 1655.7,
                            "size": 15.0,
                            "usdcSize": 24835.5,
                            "total": -24835.5
                        }
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": 68257.22,
                                    "postOrder": 43421.72
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -106.18,
                                            "postOrder": -91.18
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()
        )
    }
}