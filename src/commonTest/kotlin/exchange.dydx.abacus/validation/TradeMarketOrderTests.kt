package exchange.dydx.abacus.validation

import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.trade
import exchange.dydx.abacus.state.modal.tradeInMarket
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class TradeMarketOrderTests: ValidationsTests() {
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
        },null)

        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        },null)

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
                            "marketId": "ETH-USD",
                            "timeInForce": "GTT"
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
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "timeInForce": "GTT",
                            "summary": {
                                "price": 1004.26,
                                "size": 1.0,
                                "usdcSize": 1004.26,
                                "fee": 0.50213,
                                "feeRate": 0.0005,
                                "total": -1004.76213,
                                "slippage": 0.00989,
                                "indexSlippage": 0.01,
                                "filled": true
                            },
                            "size": {
                                "size": 1.0,
                                "usdcSize": 1004.26
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
                                        "price": 1010.0
                                    }
                                ]
                            }
                        },
                        "errors": [
                            {
                                "code": "MARKET_ORDER_WARNING_INDEX_PRICE_SLIPPAGE"
                            },
                            {
                                "type": "WARNING",
                                "code": "MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE",
                                "fields": [
                                    "size.size"
                                ]
                            }
                        ]
                    }
                }
            """.trimIndent()
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
                                "price": 1049.026,
                                "size": 10.0,
                                "usdcSize": 10490.26,
                                "fee": 5.24513,
                                "feeRate": 0.0005,
                                "total": -10495.50513,
                                "slippage": 0.09989,
                                "indexSlippage": 0.1,
                                "filled": true
                            },
                            "size": {
                                "size": 10.0,
                                "usdcSize": 10490.26
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
                                        "price": 1010.0
                                    },
                                    {
                                        "size": 4.4,
                                        "price": 1100.0
                                    }
                                ]
                            }
                        },
                        "errors": [
                            {
                                "type": "ERROR",
                                "code": "MARKET_ORDER_ERROR_INDEX_SLIPPAGE",
                                "fields": [
                                    "size.size"
                                ]
                            },
                            {
                                "type": "ERROR",
                                "code": "MARKET_ORDER_ERROR_ORDERBOOK_SLIPPAGE",
                                "fields": [
                                    "size.size"
                                ]
                            }
                        ]
                    }
                }
            """.trimIndent()
        )
    }
}