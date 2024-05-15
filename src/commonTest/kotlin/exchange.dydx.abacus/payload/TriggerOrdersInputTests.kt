package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.triggerOrders
import exchange.dydx.abacus.utils.Rounder
import kotlin.test.Test

class TriggerOrderInputTests : V4BaseTests() {
    override fun loadSubaccounts(): StateResponse {
        return perp.rest(
            AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
            mock.accountsChannel.v4_accounts_received_for_calculation,
            0,
            null,
        )
    }

    @Test
    fun testInputs() {
        setup()

        // Tests on ETH-USD market with
        // - notional total: 1753.2932
        // - size: 0.5
        // - leverage: 2.054737105604498

        val leverage = 2.054737105604498;

        testDefaults()
        testSetPositionSize()
        testStopLossInput(leverage)
        testTakeProfitInput(leverage)
    }

    @Test
    fun testLessThanOneLeverageInputs() {
        setup()

        // Tests on ETH-USD market with
        // - notional total: 1753.2932
        // - size: 0.5
        // - leverage < 1 (defaults to 1 in calculations)

        test(
            {
                perp.rest(
                    AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
                    mock.accountsChannel.v4_accounts_received_for_calculation_2,
                    0,
                    null,
                )
            },
            null,
        )

        val leverage = 1.0;

        testDefaults()
        testSetPositionSize()
        testStopLossInput(leverage)
        testTakeProfitInput(leverage)
    }

    private fun roundValue(value: Double): Double {
        return Rounder.round(value, 0.000001)
    }

    private fun testDefaults() {
        test(
            {
                perp.triggerOrders("ETH-USD", TriggerOrdersInputField.marketId, 0)
            },
            """
        {
            "input": {
                "current": "triggerOrders",
                "triggerOrders": {
                    "marketId": "ETH-USD"
                }
            }
        }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "marketId": "ETH-USD",
                        "stopLossOrder": {
                            "type": "STOP_MARKET",
                            "side": "SELL"
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testSetPositionSize() {
        test(
            {
                perp.triggerOrders("2.0", TriggerOrdersInputField.stopLossOrderSize, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "size": "2.0",
                            "summary": {
                                "size": "2.0"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("0.5", TriggerOrdersInputField.size, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "size": "0.5",
                        "stopLossOrder": {
                            "size": "2.0",
                            "summary": {
                                "size": "0.5"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("1.0", TriggerOrdersInputField.takeProfitOrderSize, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "size": "0.5",
                        "stopLossOrder": {
                            "size": "2.0",
                            "summary": {
                                "size": "0.5"
                            }
                        },
                        "takeProfitOrder": {
                            "size": "1.0",
                            "summary": {
                                "size": "0.5"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testStopLossInput(leverageMultiplier: Double) {
        test({
            perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        test(
            {
                perp.triggerOrders("1000.0", TriggerOrdersInputField.stopLossPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "type": "STOP_MARKET",
                            "side": "SELL",
                            "price": {
                                "triggerPrice": "1000.0",
                                "usdcDiff": "0",
                                "percentDiff": "0",
                                "input": "stopLossOrder.price.triggerPrice"
                            },
                            "summary": {
                                "price": "800.0"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test({
            perp.triggerOrders("300.0", TriggerOrdersInputField.stopLossLimitPrice, 0)
        }, null)

        test({
            perp.triggerOrders("1234", TriggerOrdersInputField.stopLossOrderId, 0)
        }, null)

        test(
            {
                perp.triggerOrders("400.0", TriggerOrdersInputField.stopLossPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "orderId": "1234",
                            "type": "STOP_LIMIT",
                            "side": "SELL",
                            "price": {
                                "limitPrice": "300.0",
                                "triggerPrice": "400.0",
                                "usdcDiff": "300",
                                "percentDiff": "${roundValue(34.221316 * leverageMultiplier)}",
                                "input": "stopLossOrder.price.triggerPrice"
                            },
                            "summary": {
                                "price": "300.0"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("400", TriggerOrdersInputField.stopLossUsdcDiff, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "orderId": "1234",
                            "type": "STOP_LIMIT",
                            "side": "SELL",
                            "price": {
                                "limitPrice": "300.0",
                                "triggerPrice": "200.0",
                                "usdcDiff": "400",
                                "percentDiff": "${roundValue(45.62842084826428 * leverageMultiplier)}",
                                "input": "stopLossOrder.price.usdcDiff"
                            },
                            "summary": {
                                "price": "300.0"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("25.00", TriggerOrdersInputField.stopLossPercentDiff, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "orderId": "1234",
                            "type": "STOP_LIMIT",
                            "side": "SELL",
                            "price": {
                                "limitPrice": "300.0",
                                "triggerPrice": "${roundValue(1000.0 - 438.3233 / leverageMultiplier)}",
                                "usdcDiff": "${roundValue(219.16165 / leverageMultiplier)}",
                                "percentDiff": "25.0",
                                "input": "stopLossOrder.price.percentDiff"
                            },
                            "summary": {
                                "price": "300.0"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testTakeProfitInput(leverageMultiplier: Double) {
        test({
            perp.triggerOrders("TAKE_PROFIT_MARKET", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        test(
            {
                perp.triggerOrders("1000.0", TriggerOrdersInputField.takeProfitPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "takeProfitOrder": {
                            "type": "TAKE_PROFIT_MARKET",
                            "side": "SELL",
                            "price": {
                                "triggerPrice": "1000.0",
                                "usdcDiff": "0",
                                "percentDiff": "0",
                                "input": "takeProfitOrder.price.triggerPrice"
                            },
                            "summary": {
                                "price": "800.0"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test({
            perp.triggerOrders("1600.0", TriggerOrdersInputField.takeProfitLimitPrice, 0)
        }, null)

        test({
            perp.triggerOrders("4321", TriggerOrdersInputField.takeProfitOrderId, 0)
        }, null)

        test({
            perp.triggerOrders("4321", TriggerOrdersInputField.takeProfitOrderId, 0)
        }, null)

        test(
            {
                perp.triggerOrders("1800.0", TriggerOrdersInputField.takeProfitPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "takeProfitOrder": {
                            "orderId": "4321",
                            "type": "TAKE_PROFIT",
                            "side": "SELL",
                            "price": {
                                "limitPrice": "1600.0",
                                "triggerPrice": "1800.0",
                                "usdcDiff": "400",
                                "percentDiff": "${roundValue(45.62842084826428 * leverageMultiplier)}",
                                "input": "takeProfitOrder.price.triggerPrice"
                            },
                            "summary": {
                                "price": "1600.0"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("300.0", TriggerOrdersInputField.takeProfitUsdcDiff, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "takeProfitOrder": {
                            "orderId": "4321",
                            "type": "TAKE_PROFIT",
                            "side": "SELL",
                            "price": {
                                "limitPrice": "1600.0",
                                "triggerPrice": "1600.0",
                                "usdcDiff": "300.0",
                                "percentDiff": "${34.22131563619821 * leverageMultiplier}",
                                "input": "takeProfitOrder.price.usdcDiff"
                            },
                            "summary": {
                                "price": "1600.0"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("25.0", TriggerOrdersInputField.takeProfitPercentDiff, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "takeProfitOrder": {
                            "orderId": "4321",
                            "type": "TAKE_PROFIT",
                            "side": "SELL",
                            "price": {
                                "limitPrice": "1600.0",
                                "triggerPrice": "${roundValue(1000.0 + 438.3233 / leverageMultiplier)}",
                                "usdcDiff": "${roundValue(219.16165 / leverageMultiplier)}",
                                "percentDiff": "25.0",
                                "input": "takeProfitOrder.price.percentDiff"
                            },
                            "summary": {
                                "price": "1600.0"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }
}
