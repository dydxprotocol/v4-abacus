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

        testStopLossInput()
        testTakeProfitInput()
        testSetPositionSize()
    }

    private fun testDefaultPositionSize() {
        test(
            {
                perp.triggerOrders("STOP_LIMIT", TriggerOrdersInputField.stopLossOrderType, 0)
            },
            """
        {
            "input": {
                "current": "triggerOrders",
                "triggerOrders": {
                    "marketId": "ETH-USD",
                    "stopLossOrder": {
                        "type": "STOP_LIMIT",
                        "size": "1.0"
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
            perp.triggerOrders("STOP_LIMIT", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        test({
            perp.triggerOrders("300.0", TriggerOrdersInputField.stopLossLimitPrice, 0)
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
                            "type": "STOP_LIMIT",
                            "price": {
                                "limitPrice": "300.0",
                                "triggerPrice": "400.0",
                                "percentDiff": "0.6",
                                "usdcDiff": "600",
                                "input": "stopLossOrder.price.triggerPrice"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("0.4", TriggerOrdersInputField.stopLossPercentDiff, 0)
            },
            """
        {
            "input": {
                "current": "triggerOrders",
                "triggerOrders": {
                    "stopLossOrder": {
                        "type": "STOP_LIMIT",
                        "price": {
                            "limitPrice": "300.0",
                            "triggerPrice": "600.0",
                            "percentDiff": "0.4",
                            "usdcDiff": "400",
                            "input": "stopLossOrder.price.percentDiff"
                    }
                    }
                }
            }
        }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("200", TriggerOrdersInputField.stopLossUsdcDiff, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "type": "STOP_LIMIT",
                            "price": {
                                "limitPrice": "300.0",
                                "triggerPrice": "800.0",
                                "percentDiff": "0.2",
                                "usdcDiff": "200",
                                "input": "stopLossOrder.price.usdcDiff"
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
            perp.triggerOrders("TAKE_PROFIT_LIMIT", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        test({
            perp.triggerOrders("2000.0", TriggerOrdersInputField.takeProfitLimitPrice, 0)
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
                        "type": "TAKE_PROFIT_LIMIT",
                        "price": {
                            "limitPrice": "2000.0",
                            "triggerPrice": "1800.0",
                            "percentDiff": "0.8",
                            "usdcDiff": "800",
                            "input": "takeProfitOrder.price.triggerPrice"
                    }
                    }
                }
            }
        }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("0.4", TriggerOrdersInputField.takeProfitPercentDiff, 0)
            },
            """
        {
            "input": {
                "current": "triggerOrders",
                "triggerOrders": {
                    "takeProfitOrder": {
                        "type": "TAKE_PROFIT_LIMIT",
                        "price": {
                            "limitPrice": "2000.0",
                            "triggerPrice": "1400.0",
                            "percentDiff": "0.4",
                            "usdcDiff": "400",
                            "input": "takeProfitOrder.price.percentDiff"
                    }
                    }
                }
            }
        }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("200.0", TriggerOrdersInputField.takeProfitUsdcDiff, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "takeProfitOrder": {
                            "type": "TAKE_PROFIT_LIMIT",
                            "price": {
                                "limitPrice": "2000.0",
                                "triggerPrice": "1200.0",
                                "percentDiff": "0.2",
                                "usdcDiff": "200.0",
                                "input": "takeProfitOrder.price.usdcDiff"
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }
}
