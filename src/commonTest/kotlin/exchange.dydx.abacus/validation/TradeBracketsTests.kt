package exchange.dydx.abacus.validation

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TradeBracketsTests : ValidationsTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTradeInputOnce()
    }

    fun loadValidationsAccounts2(): StateResponse {
        return perp.socket(mock.socketUrl, mock.validationsMock.accountsSubscribed2, 0, null)
    }

    private fun testTradeInputOnce() {
        reset()

        var time = ServerTime.now()
        testMarketOrderBrackets()
        time = perp.log("Market Order Validation", time)
    }

    private fun testMarketOrderBrackets() {
        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("0.1", TradeInputField.size, 0)
        }, null)

        test({
            perp.trade("900", TradeInputField.bracketsTakeProfitPrice, 0)
        }, null)

        if (perp.staticTyping) {
            perp.trade("1100", TradeInputField.bracketsStopLossPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Market)
            assertEquals(trade.side, OrderSide.Buy)
            assertEquals(trade.marketId, "ETH-USD")
            assertEquals(trade.timeInForce, "GTT")
            val summary = trade.summary!!
            assertEquals(summary.price, 1000.1)
            assertEquals(summary.size, 0.1)
            assertEquals(summary.usdcSize, 100.01)
            assertEquals(summary.total, -100.01)
            assertEquals(summary.slippage, 0.0001)
            assertEquals(summary.indexSlippage, 0.0001)
            assertEquals(summary.filled, true)
            val size = trade.size!!
            assertEquals(size.size, 0.1)
            assertEquals(size.usdcSize, 100.01)

            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 2)

            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE")
            assertEquals(error?.fields?.size, 2)
            assertEquals(error?.fields?.get(0), "brackets.takeProfit.triggerPrice")
            assertEquals(error?.fields?.get(1), "brackets.takeProfit.percent")
            assertEquals(error?.resources?.title?.stringKey, "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE")
            assertEquals(error?.resources?.text?.stringKey, "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE")
            assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

            val error2 = errors?.get(1)
            assertEquals(error2?.type, ErrorType.error)
            assertEquals(error2?.code, "BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE")
            assertEquals(error2?.fields?.size, 2)
            assertEquals(error2?.fields?.get(0), "brackets.stopLoss.triggerPrice")
            assertEquals(error2?.fields?.get(1), "brackets.stopLoss.percent")
            assertEquals(error2?.resources?.title?.stringKey, "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE")
            assertEquals(error2?.resources?.text?.stringKey, "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE")
            assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")
        } else {
            test(
                {
                    perp.trade("1100", TradeInputField.bracketsStopLossPrice, 0)
                },
                """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "timeInForce": "GTT",
                            "summary": {
                                "price": 1000.1,
                                "size": 0.1,
                                "usdcSize": 100.01,
                                "total": -100.01,
                                "slippage": 0.0,
                                "indexSlippage": 0.0001,
                                "filled": true
                            },
                            "size": {
                                "size": 0.1,
                                "usdcSize": 100.01
                            },
                            "marketOrder": {
                                "orderbook": [
                                    {
                                        "size": 0.1,
                                        "price": 1000.1
                                    }
                                ]
                            }
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE",
                                "fields": [
                                    "brackets.takeProfit.triggerPrice",
                                    "brackets.takeProfit.percent"
                                ],
                                "resources": {
                                    "title": {
                                        "stringKey": "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE"
                                    },
                                    "text": {
                                        "stringKey": "ERRORS.TRADE_BOX.BRACKET_ORDER_TAKE_PROFIT_ABOVE_EXPECTED_PRICE"
                                    },
                                    "action": {
                                        "stringKey": "APP.TRADE.ENTER_TRIGGER_PRICE"
                                    }
                                }
                            },
                            {
                                "type": "ERROR",
                                "code": "BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE",
                                "fields": [
                                    "brackets.stopLoss.triggerPrice",
                                    "brackets.stopLoss.percent"
                                ],
                                "resources": {
                                    "title": {
                                        "stringKey": "ERRORS.TRADE_BOX_TITLE.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE"
                                    },
                                    "text": {
                                        "stringKey": "ERRORS.TRADE_BOX.BRACKET_ORDER_STOP_LOSS_BELOW_EXPECTED_PRICE"
                                    },
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

        test({
            perp.trade("1100", TradeInputField.bracketsTakeProfitPrice, 0)
        }, null)

        if (perp.staticTyping) {
            perp.trade("900", TradeInputField.bracketsStopLossPrice, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Market)
            assertEquals(trade.side, OrderSide.Buy)
            assertEquals(trade.marketId, "ETH-USD")
            assertEquals(trade.timeInForce, "GTT")
        } else {
            test(
                {
                    perp.trade("900", TradeInputField.bracketsStopLossPrice, 0)
                },
                """
                {
                "input": {
                    "current": "trade",
                    "trade": {
                        "type": "MARKET",
                        "side": "BUY",
                        "marketId": "ETH-USD",
                        "timeInForce": "GTT"
                    },
                    "errors": null
                }
            }
                """.trimIndent(),
            )
        }

        test({
            loadValidationsAccounts2()
        }, null)

        test({
            perp.trade("SELL", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("0.1", TradeInputField.size, 0)
        }, null)

        test({
            perp.trade("900", TradeInputField.bracketsTakeProfitPrice, 0)
        }, null)

        test({
            perp.trade("1100", TradeInputField.bracketsStopLossPrice, 0)
        }, null)

        test({
            perp.trade("true", TradeInputField.bracketsTakeProfitReduceOnly, 0)
        }, null)

        if (perp.staticTyping) {
            perp.trade("true", TradeInputField.bracketsStopLossReduceOnly, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.type, OrderType.Market)
            assertEquals(trade.side, OrderSide.Sell)
            assertEquals(trade.marketId, "ETH-USD")
            assertEquals(trade.timeInForce, "GTT")
            val summary = trade.summary!!
            assertEquals(summary.price, 999.9000000000001)
            assertEquals(summary.size, 0.1)
            assertEquals(summary.usdcSize, 99.99000000000001)
            assertEquals(summary.total, 99.99000000000001)
            assertEquals(summary.slippage, 0.0001)
            assertEquals(summary.indexSlippage, 0.0001)
            assertEquals(summary.filled, true)
            val size = trade.size!!
            assertEquals(size.size, 0.1)
            assertEquals(size.usdcSize, 99.99000000000001)

            val errors = perp.internalState.input.errors
            assertEquals(errors?.size, 2)

            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.error)
            assertEquals(error?.code, "WOULD_NOT_REDUCE_UNCHECK")
            assertEquals(error?.fields?.size, 2)
            assertEquals(error?.fields?.get(0), "brackets.takeProfit.triggerPrice")
            assertEquals(error?.fields?.get(1), "brackets.takeProfit.reduceOnly")

            val error2 = errors?.get(1)
            assertEquals(error2?.type, ErrorType.error)
            assertEquals(error2?.code, "WOULD_NOT_REDUCE_UNCHECK")
            assertEquals(error2?.fields?.size, 2)
            assertEquals(error2?.fields?.get(0), "brackets.stopLoss.triggerPrice")
            assertEquals(error2?.fields?.get(1), "brackets.stopLoss.reduceOnly")
        } else {
            test(
                {
                    perp.trade("true", TradeInputField.bracketsStopLossReduceOnly, 0)
                },
                """
                {
                "input": {
                    "current": "trade",
                    "trade": {
                        "type": "MARKET",
                        "side": "SELL",
                        "marketId": "ETH-USD",
                        "timeInForce": "GTT",
                        "summary": {
                            "price": 999.9,
                            "size": 0.1,
                            "usdcSize": 99.99,
                            "total": 99.99,
                            "slippage": 0.0,
                            "indexSlippage": 1.0E-4,
                            "filled": true
                        },
                        "size": {
                            "size": 0.1,
                            "usdcSize": 99.99
                        },
                        "marketOrder": {
                            "orderbook": [
                                {
                                    "size": 0.1,
                                    "price": 999.9
                                }
                            ]
                        }
                    },
                    "errors": [
                        {
                            "type": "ERROR",
                            "code": "WOULD_NOT_REDUCE_UNCHECK",
                            "fields": [
                                "brackets.takeProfit.triggerPrice",
                                "brackets.takeProfit.reduceOnly"
                            ]
                        },
                        {
                            "type": "ERROR",
                            "code": "WOULD_NOT_REDUCE_UNCHECK",
                            "fields": [
                                "brackets.stopLoss.triggerPrice",
                                "brackets.stopLoss.reduceOnly"
                            ]
                        }
                    ]
                }
            }
                """.trimIndent(),
            )
        }
    }
}
