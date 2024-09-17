package exchange.dydx.abacus.payload

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import kotlin.test.Test
import kotlin.test.assertEquals

class TradeInputOptionsTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        testTradeInputOnce()
        testIsolatedInputOnce()
    }

    private fun testIsolatedInputOnce() {
        if (perp.staticTyping) {
            perp.trade("ISOLATED", TradeInputField.marginMode, 0)
            perp.trade("MARKET", TradeInputField.type, 0)
            val options = perp.internalState.input.trade.options
            assertEquals(options.needsSize, true)
            assertEquals(options.needsLeverage, false)
            assertEquals(options.needsBalancePercent, true)
            assertEquals(options.needsTriggerPrice, false)
            assertEquals(options.needsLimitPrice, false)
            assertEquals(options.needsTrailingPercent, false)
            assertEquals(options.needsReduceOnly, true)
            assertEquals(options.needsPostOnly, false)
            assertEquals(options.needsBrackets, true)
            assertEquals(options.needsGoodUntil, false)
        } else {
            perp.trade("ISOLATED", TradeInputField.marginMode, 0)
            test(
                {
                    perp.trade("MARKET", TradeInputField.type, 0)
                },
                """
                {
                "input": {
                    "trade": {
                        "options": {
                            "needsSize": true,
                            "needsLeverage": false,
                            "needsBalancePercent": true,
                            "needsTriggerPrice": false,
                            "needsLimitPrice": false,
                            "needsTrailingPercent": false,
                            "needsReduceOnly": true,
                            "needsPostOnly": false,
                            "needsBrackets": true,
                            "needsTimeInForce": false,
                            "needsGoodUntil": false,
                            "needsExecution": false
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("LIMIT", TradeInputField.type, 0)
            val options = perp.internalState.input.trade.options
            assertEquals(options.needsSize, true)
            assertEquals(options.needsLeverage, false)
            assertEquals(options.needsBalancePercent, false)
            assertEquals(options.needsTriggerPrice, false)
            assertEquals(options.needsLimitPrice, true)
            assertEquals(options.needsTrailingPercent, false)
            assertEquals(options.needsReduceOnly, false)
            assertEquals(options.needsPostOnly, true)
            assertEquals(options.needsBrackets, false)
            assertEquals(options.needsGoodUntil, true)
        } else {
            test(
                {
                    perp.trade("LIMIT", TradeInputField.type, 0)
                },
                """
                {
                "input": {
                    "trade": {
                        "options": {
                            "needsSize": true,
                            "needsLeverage": false,
                            "needsBalancePercent": false,
                            "needsTriggerPrice": false,
                            "needsLimitPrice": true,
                            "needsTrailingPercent": false,
                            "needsReduceOnly": false,
                            "needsPostOnly": false,
                            "needsBrackets": true,
                            "needsTimeInForce": true,
                            "needsGoodUntil": false,
                            "needsExecution": true
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }
    }

    private fun testTradeInputOnce() {
        if (perp.staticTyping) {
            perp.tradeInMarket("ETH-USD", 0)
            assertEquals(perp.internalState.input.currentType, InputType.TRADE)
            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Limit)
            assertEquals(trade.side, OrderSide.Buy)
            assertEquals(trade.marketId, "ETH-USD")
            assertEquals(trade.timeInForce, "GTT")
            assertEquals(trade.options.needsPostOnly, true)
            assertEquals(trade.options.marginModeOptions?.size, 2)
            assertEquals(
                trade.options.marginModeOptions?.get(0),
                SelectionOption(
                    type = "CROSS",
                    stringKey = "APP.TRADE.CROSS_MARGIN",
                    string = null,
                    iconUrl = null,
                ),
            )
            assertEquals(
                trade.options.marginModeOptions?.get(1),
                SelectionOption(
                    type = "ISOLATED",
                    stringKey = "APP.TRADE.ISOLATED_MARGIN",
                    string = null,
                    iconUrl = null,
                ),
            )
        } else {
            test(
                {
                    perp.tradeInMarket("ETH-USD", 0)
                },
                """
            {
                "input": {
                    "current": "trade",
                    "trade": {
                        "type": "LIMIT",
                        "side": "BUY",
                        "marketId": "ETH-USD",
                        "timeInForce": "GTT",
                        "options": {
                            "needsPostOnly": true,
                            "marginModeOptions": [
                                {
                                    "type": "CROSS",
                                    "stringKey": "APP.TRADE.CROSS_MARGIN"
                                },
                                {
                                    "type": "ISOLATED",
                                    "stringKey": "APP.TRADE.ISOLATED_MARGIN"
                                }
                            ]
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade(null, null, 0)
            assertEquals(perp.internalState.input.currentType, InputType.TRADE)
            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Limit)
            assertEquals(trade.side, OrderSide.Buy)
        } else {
            test(
                {
                    perp.trade(null, null, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD"
                        }
                    }
                }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("BUY", TradeInputField.side, 0)
            assertEquals(perp.internalState.input.currentType, InputType.TRADE)
            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Limit)
            assertEquals(trade.side, OrderSide.Buy)
        } else {
            test({
                perp.trade("BUY", TradeInputField.side, 0)
            }, null)
        }

        if (perp.staticTyping) {
            perp.trade("MARKET", TradeInputField.type, 0)
            val trade = perp.internalState.input.trade
            val options = trade.options
            assertEquals(options.needsSize, true)
            assertEquals(options.needsLeverage, true)
            assertEquals(options.needsBalancePercent, true)
            assertEquals(options.needsTriggerPrice, false)
            assertEquals(options.needsLimitPrice, false)
            assertEquals(options.needsTrailingPercent, false)
            assertEquals(options.needsReduceOnly, true)
            assertEquals(options.needsPostOnly, false)
            assertEquals(options.needsBrackets, true)
            assertEquals(options.needsGoodUntil, false)
        } else {
            test(
                {
                    perp.trade("MARKET", TradeInputField.type, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "options": {
                            "needsSize": true,
                            "needsLeverage": true,
                            "needsBalancePercent": true,
                            "needsTriggerPrice": false,
                            "needsLimitPrice": false,
                            "needsTrailingPercent": false,
                            "needsReduceOnly": true,
                            "needsPostOnly": false,
                            "needsBrackets": true,
                            "needsTimeInForce": false,
                            "needsGoodUntil": false,
                            "needsExecution": false
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("LIMIT", TradeInputField.type, 0)
            val trade = perp.internalState.input.trade
            val options = trade.options
            assertEquals(options.needsSize, true)
            assertEquals(options.needsLeverage, false)
            assertEquals(options.needsBalancePercent, false)
            assertEquals(options.needsTriggerPrice, false)
            assertEquals(options.needsLimitPrice, true)
            assertEquals(options.needsTrailingPercent, false)
            assertEquals(options.needsReduceOnly, false)
            assertEquals(options.needsPostOnly, true)
            assertEquals(options.needsBrackets, false)
            assertEquals(options.needsGoodUntil, true)
        } else {
            test(
                {
                    perp.trade("LIMIT", TradeInputField.type, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "options": {
                            "needsSize": true,
                            "needsLeverage": false,
                            "needsBalancePercent": false,
                            "needsTriggerPrice": false,
                            "needsLimitPrice": true,
                            "needsTrailingPercent": false,
                            "needsReduceOnly": false,
                            "needsPostOnly": true,
                            "needsBrackets": false,
                            "needsTimeInForce": true,
                            "needsGoodUntil": true,
                            "needsExecution": false
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("GTT", TradeInputField.timeInForceType, 0)
            val trade = perp.internalState.input.trade
            val options = trade.options
            assertEquals(options.needsSize, true)
            assertEquals(options.needsLeverage, false)
            assertEquals(options.needsBalancePercent, false)
            assertEquals(options.needsTriggerPrice, false)
            assertEquals(options.needsLimitPrice, true)
            assertEquals(options.needsTrailingPercent, false)
            assertEquals(options.needsReduceOnly, false)
            assertEquals(options.needsPostOnly, true)
            assertEquals(options.needsBrackets, false)
            assertEquals(options.needsGoodUntil, true)
        } else {
            test(
                {
                    perp.trade("GTT", TradeInputField.timeInForceType, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "options": {
                            "needsSize": true,
                            "needsLeverage": false,
                            "needsBalancePercent": false,
                            "needsTriggerPrice": false,
                            "needsLimitPrice": true,
                            "needsTrailingPercent": false,
                            "needsReduceOnly": false,
                            "needsPostOnly": true,
                            "needsBrackets": false,
                            "needsTimeInForce": true,
                            "needsGoodUntil": true,
                            "needsExecution": false
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("GTT", TradeInputField.timeInForceType, 0)
            val trade = perp.internalState.input.trade
            val options = trade.options
            assertEquals(options.needsSize, true)
            assertEquals(options.needsLeverage, false)
            assertEquals(options.needsBalancePercent, false)
            assertEquals(options.needsTriggerPrice, false)
            assertEquals(options.needsLimitPrice, true)
            assertEquals(options.needsTrailingPercent, false)
            assertEquals(options.needsReduceOnly, false)
            assertEquals(options.needsPostOnly, true)
            assertEquals(options.needsBrackets, false)
            assertEquals(options.needsGoodUntil, true)
            assertEquals(options.timeInForceOptions?.size, 2)
            assertEquals(
                options.timeInForceOptions?.get(0),
                SelectionOption(
                    type = "GTT",
                    stringKey = "APP.TRADE.GOOD_TIL_TIME",
                    string = null,
                    iconUrl = null,
                ),
            )
            assertEquals(
                options.timeInForceOptions?.get(1),
                SelectionOption(
                    type = "IOC",
                    stringKey = "APP.TRADE.IMMEDIATE_OR_CANCEL",
                    string = null,
                    iconUrl = null,
                ),
            )
        } else {
            test(
                {
                    perp.trade("IOC", TradeInputField.timeInForceType, 0)
                },
                """
            {
                "input": {
                    "trade": {
                        "options": {
                            "needsSize": true,
                            "needsLeverage": false,
                            "needsBalancePercent": false,
                            "needsTriggerPrice": false,
                            "needsLimitPrice": true,
                            "needsTrailingPercent": false,
                            "needsReduceOnly": true,
                            "needsPostOnly": false,
                            "needsBrackets": false,
                            "needsTimeInForce": true,
                            "needsGoodUntil": false,
                            "needsExecution": false,
                            "timeInForceOptions": [
                                {
                                    "type": "GTT",
                                    "stringKey": "APP.TRADE.GOOD_TIL_TIME"
                                },
                                {
                                    "type": "IOC",
                                    "stringKey": "APP.TRADE.IMMEDIATE_OR_CANCEL"
                                }
                            ]
                        }
                    }
                }
            }
                """.trimIndent(),
            )
        }
    }
}
