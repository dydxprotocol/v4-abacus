package exchange.dydx.abacus.validation

import exchange.dydx.abacus.payload.v3.V3BaseTests
import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.trade
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class TradeRequiredInputTests : V3BaseTests() {
    @Test
    fun testDataFeed() {
        setup()


        print("--------First round----------\n")

        testTradeInputOnce()
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
            """.trimIndent()
        )

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
                        "errors": null
                    }
                }
            """.trimIndent()
        )
    }

    private fun testTradeInputStopMarketType() {
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
            """.trimIndent()
        )

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
            """.trimIndent()
        )

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
            """.trimIndent()
        )
    }

    private fun testTradeInputTakeProfitMarketType() {
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
            """.trimIndent()
        )

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
            """.trimIndent()
        )

        test(
            {
                perp.trade("1223", TradeInputField.triggerPrice, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET"
                        },
                        "errors": null
                    }
                }
            """.trimIndent()
        )
    }


    private fun testTradeInputLimitType() {
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
            """.trimIndent()
        )


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
            """.trimIndent()
        )

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
            """.trimIndent()
        )
    }

    private fun testTradeInputStopLimitType() {
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
            """.trimIndent()
        )

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
            """.trimIndent()
        )

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
            """.trimIndent()
        )

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
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "LIMIT_MUST_ABOVE_TRIGGER_PRICE"
                            }
                        ]
                    }
                }
            """.trimIndent()
        )

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
            """.trimIndent()
        )

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
            """.trimIndent()
        )
    }


    private fun testTradeInputTakeProfitLimitType() {
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
            """.trimIndent()
        )

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
            """.trimIndent()
        )

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
            """.trimIndent()
        )

        test(
            {
                perp.trade("1323", TradeInputField.triggerPrice, 0)
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
            """.trimIndent()
        )
    }


    private fun testTradeInputTrailingStopType() {
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
            """.trimIndent()
        )

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
            """.trimIndent()
        )

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
            """.trimIndent()
        )
    }

}