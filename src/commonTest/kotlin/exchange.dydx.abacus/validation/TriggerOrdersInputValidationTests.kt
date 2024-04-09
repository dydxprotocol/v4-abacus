package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.triggerOrders
import kotlin.test.Test

class TriggerOrdersInputValidationTests : V4BaseTests() {
    override fun loadSubaccounts(): StateResponse {
        return perp.rest(
            AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
            mock.accountsChannel.v4_accounts_received_for_calculation,
            0,
            null,
        )
    }

    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTriggerOrdersInputOnce()
    }

    override fun reset() {
        super.reset()
        test({
            perp.triggerOrders("ETH-USD", TriggerOrdersInputField.marketId, 0)
        }, null)
    }

    private fun testTriggerOrdersInputOnce() {
        reset()
        testTriggerOrderInputs()

        reset()
        testTriggerOrderInputStopMarketType()

        reset()
        testTriggerOrderInputStopLimitType()

        reset()
        testTriggerOrderInputTakeProfitMarketType()

        reset()
        testTriggerOrderInputTakeProfitLimitType()
    }

    private fun testTriggerOrderInputs() {
        test(
            {
                perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)
            },
            null,
        )

        test(
            {
                perp.triggerOrders("0.00000001", TriggerOrdersInputField.size, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "size": "0.00000001"
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "ORDER_SIZE_BELOW_MIN_SIZE"
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("2.12333321", TriggerOrdersInputField.size, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "size": "2.12333321"
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "AMOUNT_INPUT_STEP_SIZE"
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("2.12333321", TriggerOrdersInputField.stopLossOrderSize, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "size": "2.12333321"
                        }
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "AMOUNT_INPUT_STEP_SIZE"
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )
    }

    private fun testTriggerOrderInputStopMarketType() {
        test({
            perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        test(
            {
                perp.triggerOrders("2000", TriggerOrdersInputField.stopLossPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "type": "STOP_MARKET"
                        }
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "TRIGGER_MUST_BELOW_INDEX_PRICE",
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE"
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_TRIGGER_PRICE"
                                }
                            }
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("1", TriggerOrdersInputField.size, 0)
            },
            null,
        )

        test(
            {
                perp.triggerOrders("4000", TriggerOrdersInputField.stopLossUsdcDiff, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "size": "1",
                        "stopLossOrder": {
                            "type": "STOP_MARKET",
                            "price": {
                                "triggerPrice": -3000,
                                "usdcDiff": 4000
                            }
                        }
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "PRICE_MUST_POSITIVE",
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM_TITLE.PRICE_MUST_POSITIVE"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM.PRICE_MUST_POSITIVE"
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_PRICE"
                                }
                            }
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )
    }

    private fun testTriggerOrderInputStopLimitType() {
        test({
            perp.triggerOrders("STOP_LIMIT", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        test(
            {
                perp.triggerOrders("2000", TriggerOrdersInputField.stopLossLimitPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "type": "STOP_LIMIT",
                            "side": "SELL",
                            "price": {
                                "limitPrice": "2000"
                            }
                        }
                    },
                    "errors": [
                        {
                            "type": "REQUIRED",
                            "code": "REQUIRED_TRIGGER_PRICE"
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("1000", TriggerOrdersInputField.stopLossPrice, 0)
            },
            null,
        )

        test(
            {
                perp.triggerOrders("2000", TriggerOrdersInputField.stopLossLimitPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "stopLossOrder": {
                            "type": "STOP_LIMIT",
                            "side": "SELL"
                        }
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "LIMIT_MUST_BELOW_TRIGGER_PRICE",
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_TRIGGER_PRICE"
                                }
                            }
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )
    }

    private fun testTriggerOrderInputTakeProfitMarketType() {
        test({
            perp.triggerOrders("TAKE_PROFIT_MARKET", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        test(
            {
                perp.triggerOrders("1000", TriggerOrdersInputField.takeProfitPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "takeProfitOrder": {
                            "type": "TAKE_PROFIT_MARKET"
                        }
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "TRIGGER_MUST_ABOVE_INDEX_PRICE",
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE"
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_TRIGGER_PRICE"
                                }
                            }
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )
    }

    private fun testTriggerOrderInputTakeProfitLimitType() {
        test({
            perp.triggerOrders("TAKE_PROFIT", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        test(
            {
                perp.triggerOrders("3000", TriggerOrdersInputField.takeProfitLimitPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "takeProfitOrder": {
                            "type": "TAKE_PROFIT",
                            "side": "SELL",
                            "price": {
                                "limitPrice": 3000
                            }
                        }
                    },
                    "errors": [
                        {
                            "type": "REQUIRED",
                            "code": "REQUIRED_TRIGGER_PRICE"
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.triggerOrders("2000", TriggerOrdersInputField.takeProfitPrice, 0)
            },
            null,
        )

        test(
            {
                perp.triggerOrders("3000", TriggerOrdersInputField.takeProfitLimitPrice, 0)
            },
            """
            {
                "input": {
                    "current": "triggerOrders",
                    "triggerOrders": {
                        "takeProfitOrder": {
                            "type": "TAKE_PROFIT",
                            "side": "SELL",
                            "price": {
                                "triggerPrice": 2000,
                                "limitPrice": 3000
                            }
                        }
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "LIMIT_MUST_BELOW_TRIGGER_PRICE",
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE"
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_TRIGGER_PRICE"
                                }
                            }
                        }
                    ]        
                }
            }
            """.trimIndent(),
        )
    }
}
