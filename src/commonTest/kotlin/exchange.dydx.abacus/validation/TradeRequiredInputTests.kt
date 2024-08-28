package exchange.dydx.abacus.validation

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TradeRequiredInputTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTradeInputOnce()
    }

    override fun setup() {
        super.setup()

        perp.internalState.wallet.walletAddress = "0x1234567890"
    }

    private fun testTradeInputOnce() {
        var time = ServerTime.now()
        reset()
        testTradeInputMarketType()
        time = perp.log("Market Order", time)

        reset()
        testTradeInputStopMarketType()
        time = perp.log("Stop Market Order", time)

        reset()
        testTradeInputTakeProfitMarketType()
        time = perp.log("Take Profit Market Order", time)

        reset()
        testTradeInputLimitType()
        time = perp.log("Limit Order", time)

        reset()
        testTradeInputStopLimitType()
        time = perp.log("Stop Limit Order", time)

        reset()
        testTradeInputTakeProfitLimitType()
        time = perp.log("Take Profit Limit Order", time)

        reset()
        testTradeInputTrailingStopType()
        time = perp.log("Trailing Stop Order", time)
    }

    override fun reset() {
        super.reset()
        perp.trade(null, null, 0)
    }

    private fun testTradeInputMarketType() {
        if (perp.staticTyping) {
            perp.trade("MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Market)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_SIZE")
            assertEquals(error?.fields?.firstOrNull(), "size.size")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
        } else {
            test(
                {
                    perp.trade("MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_SIZE",
                                "type": "REQUIRED",
                                "fields": [
                                    "size.size"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_AMOUNT"
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
            perp.trade("1.0", TradeInputField.size, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Market)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY")
        } else {
            test(
                {
                    perp.trade("1.0", TradeInputField.size, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET"
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY"
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testTradeInputStopMarketType() {
        if (perp.staticTyping) {
            perp.trade("STOP_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_SIZE")
            assertEquals(error?.fields?.firstOrNull(), "size.size")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
            val error2 = perp.internalState.input.errors?.get(1)
            assertEquals(error2?.type, ErrorType.required)
            assertEquals(error2?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error2?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("STOP_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_SIZE",
                                "type": "REQUIRED",
                                "fields": [
                                    "size.size"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_AMOUNT"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1.0", TradeInputField.size, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("1.0", TradeInputField.size, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1223", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "TRIGGER_MUST_ABOVE_INDEX_PRICE")
        } else {
            test(
                {
                    perp.trade("1223", TradeInputField.triggerPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET"
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "TRIGGER_MUST_ABOVE_INDEX_PRICE"
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("3000", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopMarket)
            val errors = perp.internalState.input.errors
            assertTrue { errors.isNullOrEmpty() }
        } else {
            test(
                {
                    perp.trade("3000", TradeInputField.triggerPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET"
                        },
                        "errors": null
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testTradeInputTakeProfitMarketType() {
        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 2)
            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_SIZE")
            assertEquals(error?.fields?.firstOrNull(), "size.size")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
            val error2 = errors?.get(1)
            assertEquals(error2?.type, ErrorType.required)
            assertEquals(error2?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error2?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_SIZE",
                                "type": "REQUIRED",
                                "fields": [
                                
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_AMOUNT"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1.0", TradeInputField.size, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 1)
            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("1.0", TradeInputField.size, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1923", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitMarket)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "TRIGGER_MUST_BELOW_INDEX_PRICE")
        } else {
            test(
                {
                    perp.trade("1923", TradeInputField.triggerPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET"
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "TRIGGER_MUST_BELOW_INDEX_PRICE"
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testTradeInputLimitType() {
        if (perp.staticTyping) {
            perp.trade("LIMIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Limit)
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 2)
            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_SIZE")
            assertEquals(error?.fields?.firstOrNull(), "size.size")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
            val error2 = errors?.get(1)
            assertEquals(error2?.type, ErrorType.required)
            assertEquals(error2?.code, "REQUIRED_LIMIT_PRICE")
            assertEquals(error2?.fields?.firstOrNull(), "price.limitPrice")
            assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
        } else {
            test(
                {
                    perp.trade("LIMIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_SIZE",
                                "type": "REQUIRED",
                                "fields": [
                                    "size.size"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_AMOUNT"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_LIMIT_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.limitPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_LIMIT_PRICE"
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
            perp.trade("1.0", TradeInputField.size, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Limit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_LIMIT_PRICE")
            assertEquals(error?.fields?.firstOrNull(), "price.limitPrice")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
        } else {
            test(
                {
                    perp.trade("1.0", TradeInputField.size, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_LIMIT_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.limitPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_LIMIT_PRICE"
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
            perp.trade("1223", TradeInputField.limitPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Limit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error, null)
        } else {
            test(
                {
                    perp.trade("1223", TradeInputField.limitPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT"
                        },
                        "errors": null
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testTradeInputStopLimitType() {
        if (perp.staticTyping) {
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 3)
            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_SIZE")
            assertEquals(error?.fields?.firstOrNull(), "size.size")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
            val error2 = errors?.get(1)
            assertEquals(error2?.type, ErrorType.required)
            assertEquals(error2?.code, "REQUIRED_LIMIT_PRICE")
            assertEquals(error2?.fields?.firstOrNull(), "price.limitPrice")
            assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
            val error3 = errors?.get(2)
            assertEquals(error3?.type, ErrorType.required)
            assertEquals(error3?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error3?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error3?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("STOP_LIMIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_SIZE",
                                "type": "REQUIRED",
                                "fields": [
                                    "size.size"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_AMOUNT"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_LIMIT_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.limitPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_LIMIT_PRICE"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1.0", TradeInputField.size, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 2)
            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_LIMIT_PRICE")
            assertEquals(error?.fields?.firstOrNull(), "price.limitPrice")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
            val error2 = errors?.get(1)
            assertEquals(error2?.type, ErrorType.required)
            assertEquals(error2?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error2?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("1.0", TradeInputField.size, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_LIMIT_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.limitPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_LIMIT_PRICE"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1123", TradeInputField.limitPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("1123", TradeInputField.limitPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1800", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error, null)
        } else {
            test(
                {
                    perp.trade("1800", TradeInputField.triggerPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT"
                        },
                        "errors": null
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("IOC", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "LIMIT_MUST_ABOVE_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("IOC", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT"
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("1323", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "TRIGGER_MUST_ABOVE_INDEX_PRICE")
        } else {
            test(
                {
                    perp.trade("1323", TradeInputField.triggerPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT"
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "TRIGGER_MUST_ABOVE_INDEX_PRICE"
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("1100", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.StopLimit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "TRIGGER_MUST_ABOVE_INDEX_PRICE")
        } else {
            test(
                {
                    perp.trade("1100", TradeInputField.triggerPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT"
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "TRIGGER_MUST_ABOVE_INDEX_PRICE"
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testTradeInputTakeProfitLimitType() {
        if (perp.staticTyping) {
            perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 3)
            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_SIZE")
            assertEquals(error?.fields?.firstOrNull(), "size.size")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
            val error2 = errors?.get(1)
            assertEquals(error2?.type, ErrorType.required)
            assertEquals(error2?.code, "REQUIRED_LIMIT_PRICE")
            assertEquals(error2?.fields?.firstOrNull(), "price.limitPrice")
            assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
            val error3 = errors?.get(2)
            assertEquals(error3?.type, ErrorType.required)
            assertEquals(error3?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error3?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error3?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_SIZE",
                                "type": "REQUIRED",
                                "fields": [
                                    "size.size"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_AMOUNT"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_LIMIT_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.limitPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_LIMIT_PRICE"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1.0", TradeInputField.size, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 2)
            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_LIMIT_PRICE")
            assertEquals(error?.fields?.firstOrNull(), "price.limitPrice")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
            val error2 = errors?.get(1)
            assertEquals(error2?.type, ErrorType.required)
            assertEquals(error2?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error2?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("1.0", TradeInputField.size, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_LIMIT_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.limitPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_LIMIT_PRICE"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1123", TradeInputField.limitPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_TRIGGER_PRICE")
            assertEquals(error?.fields?.firstOrNull(), "price.triggerPrice")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("1123", TradeInputField.limitPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_TRIGGER_PRICE",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.triggerPrice"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
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
            perp.trade("1923", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "TRIGGER_MUST_BELOW_INDEX_PRICE")
        } else {
            test(
                {
                    perp.trade("1923", TradeInputField.triggerPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT"
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "TRIGGER_MUST_BELOW_INDEX_PRICE"
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("1290", TradeInputField.triggerPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error, null)
        } else {
            test(
                {
                    perp.trade("1290", TradeInputField.triggerPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT"
                        },
                        "errors": null
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("IOC", TradeInputField.execution, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TakeProfitLimit)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "LIMIT_MUST_ABOVE_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("IOC", TradeInputField.execution, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT"
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                            }
                        ]
                    }
                }
                """.trimIndent(),
            )
        }
    }

    private fun testTradeInputTrailingStopType() {
        if (perp.staticTyping) {
            perp.trade("TRAILING_STOP", TradeInputField.type, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TrailingStop)
            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 2)
            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_SIZE")
            assertEquals(error?.fields?.firstOrNull(), "size.size")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
            val error2 = errors?.get(1)
            assertEquals(error2?.type, ErrorType.required)
            assertEquals(error2?.code, "REQUIRED_TRAILING_PERCENT")
            assertEquals(error2?.fields?.firstOrNull(), "price.trailingPercent")
            assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRAILING_PERCENT")
        } else {
            test(
                {
                    perp.trade("TRAILING_STOP", TradeInputField.type, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TRAILING_STOP"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_SIZE",
                                "type": "REQUIRED",
                                "fields": [
                                    "size.size"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_AMOUNT"
                                    }
                                }
                            },
                            {
                                "code": "REQUIRED_TRAILING_PERCENT",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.trailingPercent"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRAILING_PERCENT"
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
            perp.trade("1.0", TradeInputField.size, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TrailingStop)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_TRAILING_PERCENT")
            assertEquals(error?.fields?.firstOrNull(), "price.trailingPercent")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRAILING_PERCENT")
        } else {
            test(
                {
                    perp.trade("1.0", TradeInputField.size, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TRAILING_STOP"
                        },
                        "errors": [
                            {
                                "code": "REQUIRED_TRAILING_PERCENT",
                                "type": "REQUIRED",
                                "fields": [
                                    "price.trailingPercent"
                                ],
                                "resources": {
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRAILING_PERCENT"
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
            perp.trade("0.05", TradeInputField.trailingPercent, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.TrailingStop)
            val error = perp.internalState.input.errors?.firstOrNull()
            assertEquals(error, null)
        } else {
            test(
                {
                    perp.trade("0.05", TradeInputField.trailingPercent, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TRAILING_STOP"
                        },
                        "errors": null
                    }
                }
                """.trimIndent(),
            )
        }
    }
}
