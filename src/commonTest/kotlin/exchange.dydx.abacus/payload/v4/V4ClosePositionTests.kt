package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.manager.StatsigConfig
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.closePosition
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class V4ClosePositionTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        var time = ServerTime.now()
        testClosePositionInput()
        time = perp.log("Close Position", time)

        testCloseShortPositionInput()
        time = perp.log("Close Position", time)

        testLimitClosePositionInput()
    }

    override fun setup() {
        perp.internalState.wallet.walletAddress = "0x1234567890"
        loadMarkets()
        loadMarketsConfigurations()
        // do not load account

        loadOrderbook()
        loadSubaccounts()
    }

    private fun testClosePositionInput() {
        /*
        Initial setup
         */
        if (perp.staticTyping) {
            perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)

            assertEquals(perp.internalState.input.currentType, InputType.CLOSE_POSITION)
            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.type, OrderType.Market)
            assertEquals(closePosition.side, OrderSide.Sell)
            assertEquals(closePosition.sizePercent, 1.0)
            assertEquals(closePosition.size?.size, 10.771)
            assertEquals(closePosition.size?.input, "size.percent")
            assertEquals(closePosition.reduceOnly, true)
        } else {
            test(
                {
                    perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)
                },
                """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "SELL",
                         "size": {
                            "percent": 1,
                            "input": "size.percent",
                            "size": 10.771
                        },
                        "reduceOnly": true
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.closePosition("0.25", ClosePositionInputField.percent, 0)

            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.sizePercent, 0.25)
            assertEquals(closePosition.size?.size, 2.692)
            assertEquals(closePosition.size?.input, "size.percent")
            assertEquals(closePosition.size?.usdcSize, 4453.3756)
            val summary = closePosition.summary!!
            assertEquals(summary.price, 1654.3)
            assertEquals(summary.size, 2.692)
            assertEquals(summary.usdcSize, 4453.3756)
            assertEquals(summary.total, 4453.3756)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            assertEquals(
                subaccount.calculated[CalculationPeriod.current]?.quoteBalance,
                99872.368956,
            )
            assertEquals(
                subaccount.calculated[CalculationPeriod.post]?.quoteBalance,
                104592.23425040001,
            )

            val position = subaccount.openPositions?.get("ETH-USD")!!
            assertEquals(position.calculated[CalculationPeriod.current]?.size, 10.771577)
            assertEquals(position.calculated[CalculationPeriod.post]?.size, 8.079577)
        } else {
            test(
                {
                    perp.closePosition("0.25", ClosePositionInputField.percent, 0)
                },
                """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "SELL",
                        "size": {
                            "percent": 0.25,
                            "input": "size.percent",
                            "size": 2.692,
                            "usdcSize": 4453.3756
                        },
                        "reduceOnly": true,
                        "summary": {
                            "price": 1654.3,
                            "size": 2.692,
                            "usdcSize": 4453.3756,
                             "total": 4453.3756
                        }
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": 99872.368956,
                                    "postOrder": 104592.23425040001
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": 10.771577,
                                            "postOrder": 8.079577
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.closePosition("9", ClosePositionInputField.size, 0)

            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.sizePercent, null)
            assertEquals(closePosition.size?.size, 9.0)
            assertEquals(closePosition.size?.input, "size.size")
            assertEquals(closePosition.size?.usdcSize, 14888.699999999999)
            val summary = closePosition.summary!!
            assertEquals(summary.price, 1654.3)
            assertEquals(summary.size, 9.0)
            assertEquals(summary.usdcSize, 14888.699999999999)
            assertEquals(summary.total, 14888.699999999999)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            assertEquals(
                subaccount.calculated[CalculationPeriod.current]?.quoteBalance,
                99872.368956,
            )
            assertEquals(
                subaccount.calculated[CalculationPeriod.post]?.quoteBalance,
                115652.007756,
            )

            val position = subaccount.openPositions?.get("ETH-USD")!!
            assertEquals(position.calculated[CalculationPeriod.current]?.size, 10.771577)
            assertEquals(position.calculated[CalculationPeriod.post]?.size, 1.7715770000000006)
        } else {
            test(
                {
                    perp.closePosition("9", ClosePositionInputField.size, 0)
                },
                """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "SELL",
                        "size": {
                            "percent": null,
                            "input": "size.size",
                            "size": "9",
                            "usdcSize": 14888.699999999999
                        },
                        "reduceOnly": true,
                        "summary": {
                            "price": 1654.3,
                            "size": 9.0,
                            "usdcSize": 14888.699999999999,
                            "total": 14888.699999999999
                        }
                    }
                },
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "quoteBalance": {
                                    "current": 99872.368956,
                                    "postOrder": 115652.007756
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": 10.771577,
                                            "postOrder": 1.7715770000000006
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }
    }

    private fun testCloseShortPositionInput() {
        if (perp.staticTyping) {
            perp.socket(mock.socketUrl, mock.accountsChannel.v4_subscribed, 0, null)
        } else {
            test(
                {
                    perp.socket(mock.socketUrl, mock.accountsChannel.v4_subscribed, 0, null)
                },
                """
                {
                }
                """.trimIndent(),
            )
        }

        /*
        Initial setup
         */

        if (perp.staticTyping) {
            perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)

            assertEquals(perp.internalState.input.currentType, InputType.CLOSE_POSITION)
            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.type, OrderType.Market)
            assertEquals(closePosition.side, OrderSide.Buy)
            assertEquals(closePosition.sizePercent, 1.0)
            assertEquals(closePosition.size?.size, 106.179)
            assertEquals(closePosition.size?.input, "size.percent")
            assertEquals(closePosition.reduceOnly, true)
        } else {
            test(
                {
                    perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)
                },
                """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "BUY",
                         "size": {
                            "percent": 1,
                            "input": "size.percent",
                            "size": 106.179
                        },
                        "reduceOnly": true
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.closePosition("0.25", ClosePositionInputField.percent, 0)

            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.sizePercent, 0.25)
            assertEquals(closePosition.size?.size, 2.6544E+1)
            assertEquals(closePosition.size?.input, "size.percent")

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            assertEquals(
                subaccount.calculated[CalculationPeriod.current]?.quoteBalance,
                68257.215192,
            )
            assertEquals(
                subaccount.calculated[CalculationPeriod.post]?.quoteBalance,
                24308.314392,
            )

            val position = subaccount.openPositions?.get("ETH-USD")!!
            assertEquals(position.calculated[CalculationPeriod.current]?.size, -106.17985)
            assertEquals(position.calculated[CalculationPeriod.post]?.size, -79.63585)
        } else {
            test(
                {
                    perp.closePosition("0.25", ClosePositionInputField.percent, 0)
                },
                """
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
                                    "postOrder": 24308.3
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
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.closePosition("15", ClosePositionInputField.size, 0)

            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.sizePercent, null)
            assertEquals(closePosition.size?.size, 15.0)
            assertEquals(closePosition.size?.input, "size.size")
            assertEquals(closePosition.size?.usdcSize, 24835.5)

            val summary = closePosition.summary!!
            assertEquals(summary.price, 1655.7)
            assertEquals(summary.size, 15.0)
            assertEquals(summary.usdcSize, 24835.5)
            assertEquals(summary.total, -24835.5)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            assertEquals(
                subaccount.calculated[CalculationPeriod.current]?.quoteBalance,
                68257.215192,
            )
            assertEquals(
                subaccount.calculated[CalculationPeriod.post]?.quoteBalance,
                43421.715192,
            )

            val position = subaccount.openPositions?.get("ETH-USD")!!
            assertEquals(position.calculated[CalculationPeriod.current]?.size, -106.17985)
            assertEquals(position.calculated[CalculationPeriod.post]?.size, -91.17985)
        } else {
            test(
                {
                    perp.closePosition("15", ClosePositionInputField.size, 0)
                },
                """
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
                """.trimIndent(),
            )
        }
    }

    private fun testLimitClosePositionInput() {
        StatsigConfig.ff_enable_limit_close = true

        if (perp.staticTyping) {
            perp.socket(mock.socketUrl, mock.accountsChannel.v4_subscribed, 0, null)
        } else {
            test(
                {
                    perp.socket(mock.socketUrl, mock.accountsChannel.v4_subscribed, 0, null)
                },
                """
                {
                }
                """.trimIndent(),
            )
        }

        /*
        Initial setup
         */

        if (perp.staticTyping) {
            perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)

            assertEquals(perp.internalState.input.currentType, InputType.CLOSE_POSITION)
            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.type, OrderType.Market)
            assertEquals(closePosition.side, OrderSide.Buy)
            assertEquals(closePosition.sizePercent, 1.0)
            assertEquals(closePosition.size?.size, 106.179)
            assertEquals(closePosition.size?.input, "size.percent")
            assertEquals(closePosition.reduceOnly, true)
        } else {
            test(
                {
                    perp.closePosition("ETH-USD", ClosePositionInputField.market, 0)
                },
                """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "BUY",
                        "size": {
                            "percent": 1,
                            "input": "size.percent",
                            "size": 106.179
                        },
                        "reduceOnly": true
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.closePosition("true", ClosePositionInputField.useLimit, 0)

            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.type, OrderType.Limit)
            assertEquals(closePosition.side, OrderSide.Buy)
            assertEquals(closePosition.sizePercent, 1.0)
            assertEquals(closePosition.size?.size, 106.179)
            assertEquals(closePosition.size?.input, "size.percent")
            assertEquals(closePosition.price?.limitPrice, 1655.0)
            assertEquals(closePosition.reduceOnly, true)
        } else {
            test(
                {
                    perp.closePosition("true", ClosePositionInputField.useLimit, 0)
                },
                """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "LIMIT",
                        "side": "BUY",
                        "size": {
                            "percent": 1,
                            "input": "size.percent",
                            "size": 106.179
                        },
                        "price": {
                            "limitPrice": 1655
                        },
                        "reduceOnly": true
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.closePosition("2500", ClosePositionInputField.limitPrice, 0)

            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.price?.limitPrice, 2500.0)
        } else {
            test(
                {
                    perp.closePosition("2500", ClosePositionInputField.limitPrice, 0)
                },
                """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "LIMIT",
                        "side": "BUY",
                        "size": {
                            "percent": 1,
                            "input": "size.percent",
                            "size": 106.179
                        },
                        "price": {
                            "limitPrice": 2500
                        },
                        "reduceOnly": true
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.closePosition("false", ClosePositionInputField.useLimit, 0)

            val closePosition = perp.internalState.input.closePosition
            assertEquals(closePosition.type, OrderType.Market)
            assertEquals(closePosition.side, OrderSide.Buy)
            assertEquals(closePosition.sizePercent, 1.0)
            assertEquals(closePosition.size?.size, 106.179)
            assertEquals(closePosition.size?.input, "size.percent")
            assertEquals(closePosition.price?.limitPrice, 2500.0)
            assertEquals(closePosition.reduceOnly, true)
        } else {
            test(
                {
                    perp.closePosition("false", ClosePositionInputField.useLimit, 0)
                },
                """
            {
                "input": {
                    "current": "closePosition",
                    "closePosition": {
                        "type": "MARKET",
                        "side": "BUY",
                        "size": {
                            "percent": 1,
                            "input": "size.percent",
                            "size": 106.179
                        },
                        "price": {
                            "limitPrice": 2500
                        },
                        "reduceOnly": true
                    }
                }
            }
                """.trimIndent(),
            )
        }
    }
}
