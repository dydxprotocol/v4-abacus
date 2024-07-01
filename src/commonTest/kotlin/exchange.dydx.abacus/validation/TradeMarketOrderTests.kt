package exchange.dydx.abacus.validation

import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class TradeMarketOrderTests : ValidationsTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTradeInputOnce()
    }

    private fun testTradeInputOnce() {
        reset()

        var time = ServerTime.now()
        testMarketOrderbookSlippageAndLiquidity()
        time = perp.log("Market Order Validation", time)
    }

    private fun testMarketOrderbookSlippageAndLiquidity() {
        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        test(
            {
                perp.trade("BUY", TradeInputField.side, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD"
                        },
                        "errors": [
                            {
                                "type": "REQUIRED",
                                "code": "REQUIRED_SIZE",
                                "fields": [
                                    "size.size"
                                ]
                            }
                        ]
                    }
                }
            """.trimIndent(),
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
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "timeInForce": "GTT",
                            "summary": {
                                "price": 1024.26,
                                "size": 1.0,
                                "usdcSize": 1024.26,
                                "total": -1024.26,
                                "slippage": 0.05989,
                                "indexSlippage": 0.06,
                                "filled": true
                            },
                            "size": {
                                "size": 1.0,
                                "usdcSize": 1024.26
                            },
                            "marketOrder": {
                                "orderbook": [
                                    {
                                        "size": 0.1,
                                        "price": 1000.1
                                    },
                                    {
                                        "size": 0.5,
                                        "price": 1000.5
                                    },
                                    {
                                        "size": 0.4,
                                        "price": 1060.0
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
                perp.trade("10.0", TradeInputField.size, 0)
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
                                "price": 1382.026,
                                "size": 10.0,
                                "usdcSize": 13820.26,
                                "total": -13820.26,
                                "slippage": 0.79982,
                                "indexSlippage": 0.8,
                                "filled": true
                            },
                            "size": {
                                "size": 10.0,
                                "usdcSize": 13820.26
                            },
                            "marketOrder": {
                                "orderbook": [
                                    {
                                        "size": 0.1,
                                        "price": 1000.1
                                    },
                                    {
                                        "size": 0.5,
                                        "price": 1000.5
                                    },
                                    {
                                        "size": 5.0,
                                        "price": 1060.0
                                    },
                                    {
                                        "size": 4.4,
                                        "price": 1800.0
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
