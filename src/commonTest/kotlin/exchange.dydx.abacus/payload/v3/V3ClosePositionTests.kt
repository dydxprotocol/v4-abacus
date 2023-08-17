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
                        "reduceOnly": false
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
                        "reduceOnly": false,
                        "summary": {
                            "price": 1653.742642783858,
                            "size": 23.392,
                            "usdcSize": 38684.34790000001,
                            "total": 38684.34790000001
                        }
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": -62697.279528,
                                    "postOrder": -24012.93162799999
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
                        "reduceOnly": false,
                        "summary": {
                            "price": 1654.158126666667,
                            "size": 15.0,
                            "usdcSize": 24812.371900000006,
                            "total": 24812.371900000006
                        }
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": -62697.279528,
                                    "postOrder": -37884.90762799999
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": 93.57,
                                            "postOrder": 7.857E+1
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
                        "reduceOnly": false
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
                        "reduceOnly": false
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": 68257.215192,
                                    "postOrder": 24308.314392
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -106.17985,
                                            "postOrder": -7.963585E+1
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
                        "reduceOnly": false,
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
                                    "current": 68257.215192,
                                    "postOrder": 43421.715192
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -106.17985,
                                            "postOrder": -91.17985
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