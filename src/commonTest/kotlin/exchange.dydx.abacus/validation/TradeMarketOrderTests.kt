package exchange.dydx.abacus.validation

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

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

        if (perp.staticTyping) {
            perp.trade("BUY", TradeInputField.side, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.side, OrderSide.Buy)
            assertEquals(trade.type, OrderType.Market)
            assertEquals(trade.marketId, "ETH-USD")
            val errors = perp.internalState.input.errors
            val error = errors?.firstOrNull()
            assertEquals(error?.type, ErrorType.required)
            assertEquals(error?.code, "REQUIRED_SIZE")
            assertEquals(error?.fields?.firstOrNull(), "size.size")
        } else {
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
        }

        if (perp.staticTyping) {
            perp.trade("1.0", TradeInputField.size, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.timeInForce, "GTT")
            val summary = trade.summary!!
            assertEquals(summary.price, 1024.26)
            assertEquals(summary.size, 1.0)
            assertEquals(summary.usdcSize, 1024.26)
            assertEquals(summary.total, -1024.26)
            assertEquals(summary.slippage, 0.06)
            assertEquals(summary.indexSlippage, 0.06)
            assertEquals(summary.filled, true)

            val orderbook = trade.marketOrder!!.orderbook!!
            assertEquals(orderbook.size, 3)
            assertEquals(orderbook[0].size, 0.1)
            assertEquals(orderbook[0].price, 1000.1)
            assertEquals(orderbook[1].size, 0.5)
            assertEquals(orderbook[1].price, 1000.5)
            assertEquals(orderbook[2].size, 0.4)
            assertEquals(orderbook[2].price, 1060.0)

            assertEquals(trackingProtocol.lastEvent, "TradeValidation")
            assertEquals(
                trackingProtocol.lastData,
                """
                    {
                        "errors": [
                            "MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE"
                        ],
                        "marketId": "ETH-USD",
                        "size": 1.0,
                        "notionalSize": 1024.26,
                        "indexSlippage": 0.06,
                        "orderbookSlippage": 0.06
                    }
                """.trimIndent(),
            )
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
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "timeInForce": "GTT",
                            "summary": {
                                "price": 1024.26,
                                "size": 1.0,
                                "usdcSize": 1024.26,
                                "total": -1024.26,
                                "slippage": 0.06,
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

            assertEquals(trackingProtocol.lastEvent, "TradeValidation")
            assertEquals(
                trackingProtocol.lastData,
                """
                    {
                        "errors": [
                            "MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE"
                        ],
                        "marketId": "ETH-USD",
                        "size": 1.0,
                        "notionalSize": 1024.26,
                        "indexSlippage": 0.06,
                        "orderbookSlippage": 0.06
                    }
                """.trimIndent(),
            )
        }

        if (perp.staticTyping) {
            perp.trade("10.0", TradeInputField.size, 0)

            val trade = perp.internalState.input.trade
            assertEquals(trade.timeInForce, "GTT")
            val summary = trade.summary!!
            assertEquals(summary.price, 1382.0260000000003)
            assertEquals(summary.size, 10.0)
            assertEquals(summary.usdcSize, 13820.260000000002)
            assertEquals(summary.total, -13820.260000000002)
            assertEquals(summary.slippage, 0.8)
            assertEquals(summary.indexSlippage, 0.8)
            assertEquals(summary.filled, true)

            assertEquals(trade.size?.size, 10.0)
            assertEquals(trade.size?.usdcSize, 13820.260000000002)

            val orderbook = trade.marketOrder!!.orderbook!!
            assertEquals(orderbook.size, 4)
            assertEquals(orderbook[0].size, 0.1)
            assertEquals(orderbook[0].price, 1000.1)
            assertEquals(orderbook[1].size, 0.5)
            assertEquals(orderbook[1].price, 1000.5)
            assertEquals(orderbook[2].size, 5.0)
            assertEquals(orderbook[2].price, 1060.0)
            assertEquals(orderbook[3].size, 4.4)
            assertEquals(orderbook[3].price, 1800.0)
        } else {
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
                                "slippage": 0.8,
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
}
