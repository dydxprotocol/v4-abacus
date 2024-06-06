package exchange.dydx.abacus.validation

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

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
                                "fee": 0.050005,
                                "feeRate": 0.0005,
                                "total": -100.060005,
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

        test({
            perp.trade("1100", TradeInputField.bracketsTakeProfitPrice, 0)
        }, null)

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
                            "fee": 0.049995,
                            "feeRate": 0.0005,
                            "total": 99.940005,
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
