package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.loadOrderbook
import exchange.dydx.abacus.tests.extensions.loadv4Accounts
import kotlin.test.Test

open class V4TradeInputTests : V4BaseTests() {
    @Test
    fun testLimit() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testOnce()

        reset()

        print("--------Second round----------\n")

        testOnce()

        testAdjustedMarginFraction()

        testConditional()

        testReduceOnly()
        testExecution()
    }

    override fun setup() {
        super.setup()
        loadOrderbook()
    }

    override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.v4_subscribed_r1, 0, null)
        }, null)
    }

    override fun loadSubaccounts(): StateResponse {
        return test({
            perp.loadv4Accounts(mock, "$testRestUrl/v4/addresses/cosmo")
        }, null)
    }

    internal fun loadOrderbook(): StateResponse {
        return test({
            perp.loadOrderbook(mock)
        }, null)
    }

    private fun testOnce() {
        testMarketTradeInputOnce()
        testLimitTradeInputOnce()
        testUpdates()
    }

    private fun testLimitTradeInputOnce() {
        test(
            {
                perp.tradeInMarket("ETH-USD", 0)
            },
            """
            {
                "input": {
                    "trade": {
                        "marketId": "ETH-USD"
                    }
                }
            }
            """.trimIndent(),
        )

        test({
            perp.trade("LIMIT", TradeInputField.type, 0)
        }, null)

        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("0.2", TradeInputField.size, 0)
        }, null)

        test({
            perp.trade("1500", TradeInputField.limitPrice, 0)
        }, null)

        test(
            {
                perp.trade("1", TradeInputField.limitPrice, 0)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 100000.0
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "valueTotal": {
                                            "current": 0.0,
                                            "postOrder": 300.0
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

        test(
            {
                perp.trade("10000", TradeInputField.limitPrice, 0)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 100000.0
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "valueTotal": {
                                            "current": 0.0,
                                            "postOrder": 300
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

        test(
            {
                perp.trade("SELL", TradeInputField.side, 0)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 100000.0
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "valueTotal": {
                                            "current": 0.0,
                                            "postOrder": -300.0
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                "input": {
                    "current": "trade",
                    "trade": {
                        "marketId": "ETH-USD",
                        "size": {
                            "size": 0.2
                        },
                        "price": {
                            "limitPrice": 10000
                        }
                    },
                    "receiptLines": [
                        "LIQUIDATION_PRICE",
                        "POSITION_MARGIN",
                        "POSITION_LEVERAGE",
                        "FEE",
                        "REWARD"
                    ]
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("1", TradeInputField.limitPrice, 0)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 100000.0,
                                    "postOrder": 100000.0
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "valueTotal": {
                                            "current": 0.0,
                                            "postOrder": -300.0
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

//        test({
//            perp.trade(null, TradeInputField.limitPrice)
//        }, null)

        test({
            perp.trade("1500", TradeInputField.limitPrice, 0)
        }, null)

        test(
            {
                perp.tradeInMarket("BTC-USD", 0)
            },
            """
            {
                "input": {
                    "current": "trade",
                    "trade": {
                        "marketId": "BTC-USD",
                        "size": null,
                        "price": null
                    }
                }
            }
            """.trimIndent(),
        )

        test({
            perp.trade("10000", TradeInputField.limitPrice, 0)
        }, null)

        test({
            perp.trade("0.1", TradeInputField.size, 0)
        }, null)

        test(
            {
                perp.trade("190", TradeInputField.goodTilDuration, 0)
            },
            """
            {
                "input": {
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "INVALID_GOOD_TIL",
                            "fields": [
                                "goodTil"
                            ],
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRADE_BOX_TITLE.INVALID_GOOD_TIL"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRADE_BOX.INVALID_GOOD_TIL_MAX_90_DAYS"
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_GOOD_TIL"
                                }
                            }
                        }
                    ]
                }
            }
            """.trimIndent(),
        )
    }

    private fun testMarketTradeInputOnce() {
        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        test(
            {
                perp.tradeInMarket("LTC-USD", 0)
            },
            """
            {
            }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("1", TradeInputField.size, 0)
            },
            """
            {
                "input": {
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY",
                            "fields": [
                                "size.size"
                            ],
                            "resources": {
                                "title": {
                                    "stringKey": "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_NOT_ENOUGH_LIQUIDITY"
                                },
                                "text": {
                                    "stringKey": "ERRORS.TRADE_BOX.MARKET_ORDER_NOT_ENOUGH_LIQUIDITY"
                                },
                                "action": {
                                    "stringKey": "APP.TRADE.MODIFY_SIZE_FIELD"
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
                perp.tradeInMarket("ETH-USD", 0)
            },
            """
            {
            }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("SELL", TradeInputField.side, 0)
            },
            """
            {
            }
            """.trimIndent(),
        )

        test(
            {
                perp.trade(null, TradeInputField.usdcSize, 0)
            },
            """
            {
            }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("0.5", TradeInputField.usdcSize, 0)
            },
            """
            {
                "input": {
                    "trade": {
                        "size": {
                            "usdcSize": 0.5,
                            "size": 0.0
                        }
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
                perp.trade(null, TradeInputField.usdcSize, 0)
            },
            """
            {
                "input": {
                    "errors": [
                        {
                            "type": "REQUIRED",
                            "code": "REQUIRED_SIZE"
                        }
                    ]
                }
            }
            """.trimIndent(),
        )
    }

    private fun testUpdates() {
        test(
            {
                perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_1, 0, null)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4185.63
                                },
                                "quoteBalance": {
                                    "current": 7250.51
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -2.043254
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
            {
            },
        )

        test(
            {
                perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_2, 0, null)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
            {
            },
        )

        test(
            {
                perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_3, 0, null)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                },
                                "orders": {
                                    "2caebf6b-35d3-512c-a4d9-e438445d8dba": {
                                        "id": "2caebf6b-35d3-512c-a4d9-e438445d8dba",
                                        "side": "SELL",
                                        "type": "LIMIT",
                                        "status": "PARTIALLY_FILLED",
                                        "timeInForce": "GTT",
                                        "price": 1.0,
                                        "size": 1.0,
                                        "postOnly": false,
                                        "reduceOnly": false,
                                        "marketId": "ETH-USD",
                                        "resources": {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
            {
            },
        )

        test(
            {
                perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_4, 0, null)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                },
                                "orders": {
                                    "2caebf6b-35d3-512c-a4d9-e438445d8dba": {
                                        "id": "2caebf6b-35d3-512c-a4d9-e438445d8dba",
                                        "side": "SELL",
                                        "type": "LIMIT",
                                        "status": "FILLED",
                                        "timeInForce": "GTT",
                                        "price": 1.0,
                                        "size": 1.0,
                                        "postOnly": false,
                                        "reduceOnly": false,
                                        "marketId": "ETH-USD",
                                        "resources": {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
            {
            },
        )

        test(
            {
                perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_5, 0, null)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                },
                                "orders": {
                                    "2caebf6b-35d3-512c-a4d9-e438445d8dba": {
                                        "id": "2caebf6b-35d3-512c-a4d9-e438445d8dba",
                                        "side": "SELL",
                                        "type": "LIMIT",
                                        "status": "FILLED",
                                        "timeInForce": "GTT",
                                        "price": 1.0,
                                        "size": 1.0,
                                        "postOnly": false,
                                        "reduceOnly": false,
                                        "marketId": "ETH-USD",
                                        "resources": {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
            {
            },
        )

        test(
            {
                perp.socket(testWsUrl, mock.accountsChannel.v4_subaccounts_update_6, 0, null)
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 4272.44
                                },
                                "quoteBalance": {
                                    "current": 8772.44
                                },
                                "openPositions": {
                                    "ETH-USD": {
                                        "size": {
                                            "current": -3
                                        }
                                    }
                                },
                                "orders": {
                                    "2caebf6b-35d3-512c-a4d9-e438445d8dba": {
                                        "id": "2caebf6b-35d3-512c-a4d9-e438445d8dba",
                                        "side": "SELL",
                                        "type": "LIMIT",
                                        "status": "FILLED",
                                        "timeInForce": "GTT",
                                        "price": 1.0,
                                        "size": 1.0,
                                        "postOnly": false,
                                        "reduceOnly": false,
                                        "marketId": "ETH-USD",
                                        "resources": {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
            {
            },
        )
    }

    private fun testAdjustedMarginFraction() {
        test(
            {
                perp.socket(
                    mock.socketUrl,
                    mock.marketsChannel.v4_subscribed_for_adjusted_mf_calculation,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "openPositions": {
                                        "ETH-USD": {
                                            "adjustedImf": {
                                                "current": 0.05,
                                                "postOrder": 0.05
                                            },
                                            "adjustedMmf": {
                                                "current": 0.03,
                                                "postOrder": 0.03
                                            },
                                            "liquidationPrice": {
                                                "current": 2838.98,
                                                "postOrder": 2829.27
                                            }
                                        },
                                        "BTC-USD": {
                                            "adjustedImf": {
                                                "current": 0.05,
                                                "postOrder": 0.05
                                            },
                                            "adjustedMmf": {
                                                "current": 0.03,
                                                "postOrder": 0.03
                                            },
                                            "liquidationPrice": {
                                                "postOrder": 64878.0
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

    fun testConditional() {
        test({
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        }, null)

        test({
            perp.trade("12", TradeInputField.goodTilDuration, 0)
        }, null)

        test({
            perp.trade("D", TradeInputField.goodTilUnit, 0)
        }, null)

        test({
            perp.trade("900.0", TradeInputField.limitPrice, 0)
        }, null)

        test(
            {
                perp.trade("1000.0", TradeInputField.triggerPrice, 0)
            },
            """
            {
                "input": {
                    "trade": {
                        "price": {
                            "limitPrice": 900.0,
                            "triggerPrice": 1000.0
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.tradeInMarket("ETH-USD", 0)
            },
            """
            {
            }
            """.trimMargin(),
        )

        test(
            {
                perp.trade("0.1", TradeInputField.size, 0)
            },
            """
            {
            }
            """.trimMargin(),
        )

        test(
            {
                perp.trade("1000", TradeInputField.triggerPrice, 0)
            },
            """
            {
            }
            """.trimMargin(),
        )

        test(
            {
                perp.trade("STOP_MARKET", TradeInputField.type, 0)
            },
            """
            {
                "input": {
                    "trade": {
                        "summary": {
                            "payloadPrice": 900.0
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("2000.0", TradeInputField.triggerPrice, 0)
            },
            """
            {
                "input": {
                    "trade": {
                        "price": {
                            "triggerPrice": 2000.0
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
            },
            """
            {
                "input": {
                    "trade": {
                        "summary": {
                            "payloadPrice": 1800.0
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }

    fun testReduceOnly() {
        test(
            {
                perp.trade("MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("LIMIT", TradeInputField.type, 0)
        test(
            {
                perp.trade("GTT", TradeInputField.timeInForceType, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": true,
                                "postOnlyPromptStringKey": null
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
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": "GENERAL.TRADE.POST_ONLY_TIMEINFORCE_GTT"
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)

        test(
            {
                perp.trade("DEFAULT", TradeInputField.execution, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("IOC", TradeInputField.execution, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("POST_ONLY", TradeInputField.execution, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

        test(
            {
                perp.trade("DEFAULT", TradeInputField.execution, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("IOC", TradeInputField.execution, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("POST_ONLY", TradeInputField.execution, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "options": {
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        test(
            {
                perp.trade("IOC", TradeInputField.execution, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        test(
            {
                perp.trade("IOC", TradeInputField.execution, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "options": {
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "reduceOnlyPromptStringKey": null,
                                "postOnlyPromptStringKey": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecution() {
        testExecutionStopLimit()
        testExecutionStopMarket()
        testExecutionTakeProfit()
        testExecutionTakeProfitMarket()
    }

    private fun testExecutionStopLimit() {
        testExecutionStopLimitToStopMarket()
        testExecutionStopLimitToTakeProfit()
        testExecutionStopLimitToTakeProfitMarket()
    }

    private fun testExecutionStopLimitToStopMarket() {
        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        test(
            {
                perp.trade("STOP_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("STOP_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        test(
            {
                perp.trade("STOP_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionStopLimitToTakeProfit() {
        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        test(
            {
                perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "DEFAULT"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        test(
            {
                perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "POST_ONLY"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionStopLimitToTakeProfitMarket() {
        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        test(
            {
                perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
                // should change to IOC
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        test(
            {
                perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionStopMarket() {
        testExecutionStopMarketToStopLimit()
        testExecutionStopMarketToTakeProfit()
        testExecutionStopMarketToTakeProfitMarket()
    }

    private fun testExecutionStopMarketToStopLimit() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        test(
            {
                perp.trade("STOP_LIMIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionStopMarketToTakeProfit() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionStopMarketToTakeProfitMarket() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionTakeProfit() {
        testExecutionTakeProfitToStopMarket()
        testExecutionTakeProfitToStopLimit()
        testExecutionTakeProfitToTakeProfitMarket()
    }

    private fun testExecutionTakeProfitToStopMarket() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        test(
            {
                perp.trade("STOP_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("STOP_MARKET", TradeInputField.type, 0)
                // should change to IOC
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        test(
            {
                perp.trade("STOP_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionTakeProfitToStopLimit() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        test(
            {
                perp.trade("STOP_LIMIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "DEFAULT"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("STOP_LIMIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        test(
            {
                perp.trade("STOP_LIMIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "POST_ONLY"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionTakeProfitToTakeProfitMarket() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("DEFAULT", TradeInputField.execution, 0)

        test(
            {
                perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )

        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        perp.trade("POST_ONLY", TradeInputField.execution, 0)

        test(
            {
                perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionTakeProfitMarket() {
        testExecutionTakeProfitMarketToStopLimit()
        testExecutionTakeProfitMarketToTakeProfit()
        testExecutionTakeProfitMarketToStopMarket()
    }

    private fun testExecutionTakeProfitMarketToStopLimit() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)

        test(
            {
                perp.trade("STOP_LIMIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionTakeProfitMarketToTakeProfit() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testExecutionTakeProfitMarketToStopMarket() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        perp.trade("IOC", TradeInputField.execution, 0)
        test(
            {
                perp.trade("STOP_MARKET", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "execution": "IOC"
                        }
                    }
                }
            """.trimIndent(),
        )
    }
}
