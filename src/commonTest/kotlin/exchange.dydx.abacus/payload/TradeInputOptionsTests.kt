package exchange.dydx.abacus.payload

import exchange.dydx.abacus.payload.v3.V3BaseTests
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import kotlin.test.Test

class TradeInputOptionsTests : V3BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        loadOrderbook()

        testTradeInputOnce()
    }

    private fun testTradeInputOnce() {
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

        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

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
