package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.triggerOrders
import exchange.dydx.abacus.tests.extensions.rest
import kollections.iListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class TriggerOrdersInputValidationTests : V4BaseTests() {
    override fun loadSubaccounts(): StateResponse {
        return perp.rest(
            AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
            mock.accountsChannel.v4_accounts_received_for_calculation,
            0,
            null,
        )
    }

    override fun reset() {
        super.reset()
        test({
            perp.triggerOrders("ETH-USD", TriggerOrdersInputField.marketId, 0)
        }, null)
    }

    @Test
    fun testTriggerOrderInputs() {
        setup()
        reset()

        test(
            {
                perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)
            },
            null,
        )

        if (perp.staticTyping) {
            perp.triggerOrders("0.00000001", TriggerOrdersInputField.size, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(0.00000001, triggerOrders.size)

            val error = input.errors?.get(0)
            assertEquals(ErrorType.error, error?.type)
            assertEquals("ORDER_SIZE_BELOW_MIN_SIZE", error?.code)
            assertEquals(iListOf("size"), error?.fields)
        } else {
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
                            "code": "ORDER_SIZE_BELOW_MIN_SIZE",
                            "fields": ["size"]
                        }
                    ]        
                }
            }
                """.trimIndent(),
            )
        }
    }

    @Test
    fun testTriggerOrderInputStopMarketType() {
        setup()
        reset()

        test({
            perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        if (perp.staticTyping) {
            perp.triggerOrders("900", TriggerOrdersInputField.stopLossPrice, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(OrderType.StopMarket, triggerOrders.stopLossOrder?.type)

            val error = input.errors?.get(0)
            assertEquals(ErrorType.error, error?.type)
            assertEquals("SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE", error?.code)
            assertEquals(iListOf("stopLossOrder.price.triggerPrice"), error?.fields)
            assertEquals("ERRORS.TRIGGERS_FORM_TITLE.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE", error?.resources?.title?.stringKey)
            assertEquals("ERRORS.TRIGGERS_FORM.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE_NO_LIMIT", error?.resources?.text?.stringKey)
            assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)
        } else {
            test(
                {
                    perp.triggerOrders("900", TriggerOrdersInputField.stopLossPrice, 0)
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
                            "code": "SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
                            "fields": ["stopLossOrder.price.triggerPrice"],
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM_TITLE.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRIGGERS_FORM.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE_NO_LIMIT"
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

        if (perp.staticTyping) {
            perp.triggerOrders("2000", TriggerOrdersInputField.stopLossPrice, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(OrderType.StopMarket, triggerOrders.stopLossOrder?.type)

            val error = input.errors?.get(0)
            assertEquals(ErrorType.error, error?.type)
            assertEquals("TRIGGER_MUST_BELOW_INDEX_PRICE", error?.code)
            assertEquals(iListOf("stopLossOrder.price.triggerPrice"), error?.fields)
            assertEquals("ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE", error?.resources?.title?.stringKey)
            assertEquals("ERRORS.TRIGGERS_FORM.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE", error?.resources?.text?.stringKey)
            assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)
        } else {
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
                            "fields": ["stopLossOrder.price.triggerPrice"],
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
        }

        test(
            {
                perp.triggerOrders("1", TriggerOrdersInputField.size, 0)
            },
            null,
        )

        if (perp.staticTyping) {
            perp.triggerOrders("4000", TriggerOrdersInputField.stopLossUsdcDiff, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(4000.0, triggerOrders.stopLossOrder?.price?.usdcDiff)
            assertEquals(-3000.0, triggerOrders.stopLossOrder?.price?.triggerPrice)

            val error = input.errors?.get(0)
            assertEquals(ErrorType.error, error?.type)
            assertEquals("PRICE_MUST_POSITIVE", error?.code)
            assertEquals(iListOf("stopLossOrder.price.usdcDiff"), error?.fields)
            assertEquals("ERRORS.TRIGGERS_FORM_TITLE.PRICE_MUST_POSITIVE", error?.resources?.title?.stringKey)
            assertEquals("ERRORS.TRIGGERS_FORM.PRICE_MUST_POSITIVE", error?.resources?.text?.stringKey)
            assertEquals("APP.TRADE.MODIFY_PRICE", error?.resources?.action?.stringKey)
        } else {
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
                            "fields": ["stopLossOrder.price.usdcDiff"],
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
    }

    @Test
    fun testTriggerOrderInputStopLimitType() {
        setup()
        reset()

        test({
            perp.triggerOrders("STOP_LIMIT", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        if (perp.staticTyping) {
            perp.triggerOrders("2000", TriggerOrdersInputField.stopLossLimitPrice, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(OrderType.StopLimit, triggerOrders.stopLossOrder?.type)
            assertEquals(2000.0, triggerOrders.stopLossOrder?.price?.limitPrice)
            assertEquals(OrderSide.Sell, triggerOrders.stopLossOrder?.side)

            val error = input.errors?.get(0)
            assertEquals(ErrorType.required, error?.type)
            assertEquals("REQUIRED_TRIGGER_PRICE", error?.code)
        } else {
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
        }

        test({
            perp.triggerOrders("800", TriggerOrdersInputField.stopLossLimitPrice, 0)
        }, null)

        if (perp.staticTyping) {
            perp.triggerOrders("900", TriggerOrdersInputField.stopLossPrice, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(OrderType.StopLimit, triggerOrders.stopLossOrder?.type)
            assertEquals(900.0, triggerOrders.stopLossOrder?.price?.triggerPrice)
            assertEquals(800.0, triggerOrders.stopLossOrder?.price?.limitPrice)
            assertEquals(OrderSide.Sell, triggerOrders.stopLossOrder?.side)

            val error = input.errors?.firstOrNull()
            assertEquals(null, error)
        } else {
            test(
                {
                    perp.triggerOrders("900", TriggerOrdersInputField.stopLossPrice, 0)
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
                                "triggerPrice": "900",
                                "limitPrice": "800"
                            }
                        }
                    },
                    "errors": null        
                }
            }
                """.trimIndent(),
            )
        }

        test(
            {
                perp.triggerOrders("1000", TriggerOrdersInputField.stopLossPrice, 0)
            },
            null,
        )

        if (perp.staticTyping) {
            perp.triggerOrders("2000", TriggerOrdersInputField.stopLossLimitPrice, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(OrderType.StopLimit, triggerOrders.stopLossOrder?.type)
            assertEquals(2000.0, triggerOrders.stopLossOrder?.price?.limitPrice)
            assertEquals(1000.0, triggerOrders.stopLossOrder?.price?.triggerPrice)
            assertEquals(OrderSide.Sell, triggerOrders.stopLossOrder?.side)

            val error = input.errors?.get(0)
            assertEquals(ErrorType.error, error?.type)
            assertEquals("LIMIT_MUST_BELOW_TRIGGER_PRICE", error?.code)
            assertEquals(iListOf("stopLossOrder.price.limitPrice"), error?.fields)
            assertEquals("ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE", error?.resources?.title?.stringKey)
            assertEquals("ERRORS.TRIGGERS_FORM.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE", error?.resources?.text?.stringKey)
            assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)
        } else {
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
                            "fields": ["stopLossOrder.price.limitPrice"],
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
    }

    @Test
    fun testTriggerOrderInputTakeProfitMarketType() {
        setup()
        reset()

        test({
            perp.triggerOrders("TAKE_PROFIT_MARKET", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        if (perp.staticTyping) {
            perp.triggerOrders("1000", TriggerOrdersInputField.takeProfitPrice, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(OrderType.TakeProfitMarket, triggerOrders.takeProfitOrder?.type)

            val error = input.errors?.get(0)
            assertEquals(ErrorType.error, error?.type)
            assertEquals("TRIGGER_MUST_ABOVE_INDEX_PRICE", error?.code)
            assertEquals(iListOf("takeProfitOrder.price.triggerPrice"), error?.fields)
            assertEquals("ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE", error?.resources?.title?.stringKey)
            assertEquals("ERRORS.TRIGGERS_FORM.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE", error?.resources?.text?.stringKey)
            assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)
        } else {
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
                            "fields": ["takeProfitOrder.price.triggerPrice"],
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
    }

    @Test
    fun testTriggerOrderInputTakeProfitLimitType() {
        setup()
        reset()

        test({
            perp.triggerOrders("TAKE_PROFIT", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        if (perp.staticTyping) {
            perp.triggerOrders("3000", TriggerOrdersInputField.takeProfitLimitPrice, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(OrderType.TakeProfitLimit, triggerOrders.takeProfitOrder?.type)
            assertEquals(3000.0, triggerOrders.takeProfitOrder?.price?.limitPrice)
            assertEquals(OrderSide.Sell, triggerOrders.takeProfitOrder?.side)

            val error = input.errors?.get(0)
            assertEquals(ErrorType.required, error?.type)
            assertEquals("REQUIRED_TRIGGER_PRICE", error?.code)
        } else {
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
        }

        test(
            {
                perp.triggerOrders("2000", TriggerOrdersInputField.takeProfitPrice, 0)
            },
            null,
        )

        if (perp.staticTyping) {
            perp.triggerOrders("3000", TriggerOrdersInputField.takeProfitLimitPrice, 0)

            val input = perp.internalState.input
            assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
            val triggerOrders = input.triggerOrders
            assertEquals(OrderType.TakeProfitLimit, triggerOrders.takeProfitOrder?.type)
            assertEquals(3000.0, triggerOrders.takeProfitOrder?.price?.limitPrice)
            assertEquals(2000.0, triggerOrders.takeProfitOrder?.price?.triggerPrice)
            assertEquals(OrderSide.Sell, triggerOrders.takeProfitOrder?.side)

            val error = input.errors?.get(0)
            assertEquals(ErrorType.error, error?.type)
            assertEquals("LIMIT_MUST_BELOW_TRIGGER_PRICE", error?.code)
            assertEquals(iListOf("takeProfitOrder.price.limitPrice"), error?.fields)
            assertEquals("ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE", error?.resources?.title?.stringKey)
            assertEquals("ERRORS.TRIGGERS_FORM.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE", error?.resources?.text?.stringKey)
            assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)
        } else {
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
                            "fields": ["takeProfitOrder.price.limitPrice"],
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
}
