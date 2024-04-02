package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.triggerOrders
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

        testDefaults()
        testSetPositionSize()
        testStopLossInput()
        testTakeProfitInput()
    }

    private fun testDefaults() {
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
                perp.triggerOrders("0.5", TriggerOrdersInputField.size, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "size": "0.5"
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testStopLossInput() {
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
                                "input": "stopLossOrder.price.triggerPrice"
                            },
                            "summary": {
                                "price": "900.0"
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
    }

    private fun testTakeProfitInput() {
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
    }
}
