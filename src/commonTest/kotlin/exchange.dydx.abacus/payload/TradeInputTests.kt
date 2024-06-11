package exchange.dydx.abacus.payload

import exchange.dydx.abacus.payload.v3.V3BaseTests
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.loadSimpleAccounts
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class TradeInputTests : V3BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        loadOrderbook()

        testTradeInputOnce()
    }

    @Test
    fun testLeverage() {
        loadMarkets()
        loadMarketsConfigurations()
        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)
        test({
            perp.loadSimpleAccounts(mock)
        }, null)

        loadUser()

        loadOrderbook()
        testSimpleTradeInputOnce()
    }

    @Test
    fun testLimitOrder() {
        loadMarkets()
        loadMarketsConfigurations()
        test({
            perp.loadSimpleAccounts(mock)
        }, null)

        loadUser()

        loadOrderbook()
        testLimitTradeInputOnce()
    }

    @Test
    fun testNullSize() {
        loadMarkets()
        loadMarketsConfigurations()
        test({
            perp.loadSimpleAccounts(mock)
        }, null)
        loadUser()

        loadOrderbook()

        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

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

        test({
            perp.trade("LIMIT", TradeInputField.type, 0)
        }, null)

        test({
            perp.trade("1.0", TradeInputField.size, 0)
        }, null)

        test(
            {
                perp.trade(null, TradeInputField.usdcSize, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 1.0
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testTradeInputOnce() {
        var time = ServerTime.now()
        testMarketTradeInput()
        time = perp.log("Market Order", time)

        testLimitTradeInput()
        time = perp.log("Limit Order", time)

        testStopLimitTradeInput()
        perp.log("Stop Limit Order", time)

        testTakeProfitLimitTradeInput()
        perp.log("Take Profit Limit Order", time)

        testTrailingStopTradeInput()
        perp.log("Trailing Stop Order", time)

        testStopMarketTradeInput()
        perp.log("Stop Market Order", time)

        testTakeProfitMarketTradeInput()
        perp.log("Take Profit Market Order", time)
    }

    private fun testMarketTradeInput() {
        /*
        Initial setup
         */
        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

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

        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        /*
        size = 0.0
         */

        test(
            {
                perp.trade("0.", TradeInputField.size, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 0.0,
                                "input": "size.size"
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        /*
        size = 1.0
         */

        test(
            {
                perp.trade("1.", TradeInputField.size, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 1.0,
                                "usdcSize": 1655.7,
                                "leverage": 0.8017,
                                "input": "size.size"
                            },
                            "marketOrder": {
                                "size": 1.0,
                                "usdcSize": 1655.7,
                                "worstPrice": 1655.7,
                                "price": 1655.7,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 1.0,
                                        "price": 1655.7
                                    }
                                ]
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206821.31
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187976.44
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64353.8
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.21
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.12
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.87
                                    },
                                    "leverage": {
                                        "postOrder": 1.312
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0911
                                    },
                                    "buyingPower": {
                                        "postOrder": 3759528.74
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 94.57
                                            },
                                            "maxLeverage": {
                                                "current": 20,
                                                "postOrder": 20
                                            },
                                            "buyingPower": {
                                                "current": 3759346.73,
                                                "postOrder": 3759528.74
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

        test({
            loadOrderbook()
        }, null)
        /*
        size = 1.0
         */

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
                            "size": {
                                "size": 1.0,
                                "usdcSize": 1655.7,
                                "input": "size.size"
                            },
                            "marketOrder": {
                                "size": 1.0,
                                "usdcSize": 1655.7,
                                "worstPrice": 1655.7,
                                "price": 1655.7,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 1.0,
                                        "price": 1655.7
                                    }
                                ]
                            },
                            "summary": {
                                "price": 1655.7,
                                "payloadPrice": 1738.4850000000001
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206821.31
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187976.44
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64353.8
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.21
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.12
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.87
                                    },
                                    "leverage": {
                                        "postOrder": 1.312
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0911
                                    },
                                    "buyingPower": {
                                        "postOrder": 3759528.74
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 94.57
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

        /*
        size = 35.0
         */
        // First orderbook entry is 31.231
        test(
            {
                perp.trade("35.0", TradeInputField.size, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 35.0,
                                "usdcSize": 57949.8769,
                                "leverage": 1.07,
                                "input": "size.size"
                            },
                            "marketOrder": {
                                "size": 35.0,
                                "usdcSize": 57949.8769,
                                "worstPrice": 1655.8,
                                "price": 1655.71,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 31.231,
                                        "price": 1655.7
                                    }
                                ]
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 210110.96
                                    },
                                    "freeCollateral": {
                                        "postOrder": 188285.5
                                    },
                                    "quoteBalance": {
                                        "postOrder": -120676.13
                                    },
                                    "notionalTotal": {
                                        "postOrder": 330965.18
                                    },
                                    "valueTotal": {
                                        "postOrder": 330787.09
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 21825.47
                                    },
                                    "leverage": {
                                        "postOrder": 1.5752
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1039
                                    },
                                    "buyingPower": {
                                        "postOrder": 3765709.67
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 128.57
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

        /*
        usdcSize = 3500.0
         */
        test(
            {
                perp.trade("3500.0", TradeInputField.usdcSize, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 2.114,
                                "usdcSize": 3500.0,
                                "leverage": 0.8107,
                                "input": "size.usdcSize"
                            },
                            "marketOrder": {
                                "size": 2.114,
                                "usdcSize": 3500.15,
                                "worstPrice": 1655.7,
                                "price": 1655.7,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 2.114,
                                        "price": 1655.7
                                    }
                                ]
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206929.1
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187986.58
                                    },
                                    "quoteBalance": {
                                        "postOrder": -66199.18
                                    },
                                    "notionalTotal": {
                                        "postOrder": 273306.38
                                    },
                                    "valueTotal": {
                                        "postOrder": 273128.29
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18942.53
                                    },
                                    "leverage": {
                                        "postOrder": 1.32
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0915
                                    },
                                    "buyingPower": {
                                        "postOrder": 3759731.5
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 95.684
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

        /*
        usdcSize = 60000.0
         */

        test(
            {
                perp.trade("60000.0", TradeInputField.usdcSize, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 36.238,
                                "usdcSize": 60000.0,
                                "leverage": 1.1,
                                "input": "size.usdcSize"
                            },
                            "marketOrder": {
                                "size": 36.238,
                                "usdcSize": 59999.7573,
                                "worstPrice": 1655.8,
                                "price": 1655.71,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 31.231,
                                        "price": 1655.7
                                    }
                                ]
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 210230.63
                                    },
                                    "freeCollateral": {
                                        "postOrder": 188296.63
                                    },
                                    "quoteBalance": {
                                        "postOrder": -122727.04
                                    },
                                    "notionalTotal": {
                                        "postOrder": 333135.76
                                    },
                                    "valueTotal": {
                                        "postOrder": 332957.66
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 21934.00
                                    },
                                    "leverage": {
                                        "postOrder": 1.58
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1043
                                    },
                                    "buyingPower": {
                                        "postOrder": 3765932.52
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 129.808
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
                perp.trade("0.25", TradeInputField.leverage, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "SELL",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 65.029,
                                "usdcSize": 107507.0973,
                                "input": "size.leverage",
                                "leverage": 0.25
                            },
                            "marketOrder": {
                                "size": 65.029,
                                "usdcSize": 107507.0973,
                                "worstPrice": 1.6529E+3,
                                "price": 1653.2178,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 1.3363E+1,
                                        "price": 1.6543E+3
                                    },
                                    {
                                        "size": 1.955E+1,
                                        "price": 1.653E+3
                                    },
                                    {
                                        "size": 32.116,
                                        "price": 1.6529E+3
                                    }
                                ]
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206670.79
                                    },
                                    "freeCollateral": {
                                        "postOrder": 193614.33
                                    },
                                    "quoteBalance": {
                                        "postOrder": 51263.87
                                    },
                                    "notionalTotal": {
                                        "postOrder": 155585.01
                                    },
                                    "valueTotal": {
                                        "postOrder": 155406.92
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 13056.46
                                    },
                                    "leverage": {
                                        "postOrder": 0.75
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0632
                                    },
                                    "buyingPower": {
                                        "postOrder": 3872286.56
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 28.541
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
                perp.trade("1.25", TradeInputField.leverage, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 57.798,
                                "usdcSize": 95699.0911,
                                "leverage": 1.25,
                                "input": "size.leverage"
                            },
                            "marketOrder": {
                                "size": 57.798,
                                "usdcSize": 95699.0911,
                                "worstPrice": 1.656E+3,
                                "price": 1655.750910066092,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 3.1231E+1,
                                        "price": 1655.7
                                    }
                                ]
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 212314.4
                                    },
                                    "freeCollateral": {
                                        "postOrder": 188490.39
                                    },
                                    "quoteBalance": {
                                        "postOrder": -158444.22
                                    },
                                    "notionalTotal": {
                                        "postOrder": 370936.76
                                    },
                                    "valueTotal": {
                                        "postOrder": 370758.67
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 23824.05
                                    },
                                    "leverage": {
                                        "postOrder": 1.75
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1122
                                    },
                                    "buyingPower": {
                                        "postOrder": 3769807.9
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 151.368
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
                perp.trade("-1.25", TradeInputField.leverage, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "SELL",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 224.656,
                                "usdcSize": 371214.4258,
                                "leverage": -1.25,
                                "input": "size.leverage"
                            },
                            "marketOrder": {
                                "size": 224.656,
                                "usdcSize": 371214.4258,
                                "worstPrice": 1.6511E+3,
                                "price": 1652.37,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 13.363,
                                        "price": 1654.3
                                    }
                                ]
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206538.94
                                    },
                                    "freeCollateral": {
                                        "postOrder": 184492.9
                                    },
                                    "quoteBalance": {
                                        "postOrder": 331004.95
                                    },
                                    "notionalTotal": {
                                        "postOrder": 335376.47
                                    },
                                    "valueTotal": {
                                        "postOrder": -124466.01
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 22046.04
                                    },
                                    "leverage": {
                                        "postOrder": 1.62
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1067
                                    },
                                    "buyingPower": {
                                        "postOrder": 3689858.04
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": -131.086
                                            },
                                            "valueTotal": {
                                                "postOrder": -229832.19
                                            },
                                            "notionalTotal": {
                                                "postOrder": 229832.19
                                            },
                                            "leverage": {
                                                "postOrder": -1.11
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
                perp.trade("250", TradeInputField.size, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "SELL",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 250.0,
                                "usdcSize": 413058.0213,
                                "leverage": -1.3281,
                                "input": "size.size"
                            },
                            "marketOrder": {
                                "size": 2.5E+2,
                                "usdcSize": 413058.02,
                                "worstPrice": 1.651E+3,
                                "price": 1652.2320852,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 13.363,
                                        "price": 1654.3
                                    }
                                ]
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206518.02
                                    },
                                    "freeCollateral": {
                                        "postOrder": 182250.21
                                    },
                                    "quoteBalance": {
                                        "postOrder": 375419.49
                                    },
                                    "notionalTotal": {
                                        "postOrder": 379811.93
                                    },
                                    "valueTotal": {
                                        "postOrder": -168901.47
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 24267.81
                                    },
                                    "leverage": {
                                        "postOrder": 1.8391
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1175
                                    },
                                    "buyingPower": {
                                        "postOrder": 3645004.14
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": -156.43
                                            },
                                            "valueTotal": {
                                                "postOrder": -274267.66
                                            },
                                            "notionalTotal": {
                                                "postOrder": 274267.66
                                            },
                                            "leverage": {
                                                "postOrder": -1.33
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
                perp.trade("2", TradeInputField.leverage, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 159.796,
                                "usdcSize": 264649.0682,
                                "input": "size.leverage"
                            },
                            "summary": {
                                "size": 159.796,
                                "usdcSize": 264649.07,
                                "price": 1656.1683,
                                "total": -264781.39,
                                "slippage": 0.00078,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": false,
                                "needsLimitPrice": false,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": false,
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "needsBrackets": true
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 222112.4
                                    },
                                    "freeCollateral": {
                                        "postOrder": 189346.72
                                    },
                                    "quoteBalance": {
                                        "postOrder": -327478.67
                                    },
                                    "notionalTotal": {
                                        "postOrder": 549769.2
                                    },
                                    "valueTotal": {
                                        "postOrder": 549591.07
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 32765.67
                                    },
                                    "leverage": {
                                        "postOrder": 2.48
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1475
                                    },
                                    "buyingPower": {
                                        "postOrder": 3786934.0
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 253.366
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
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "SELL",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 159.796,
                                "usdcSize": 264098.155,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 159.796,
                                "usdcSize": 264098.155,
                                "price": 1652.72,
                                "total": 263966.11,
                                "slippage": 0.00126,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": false,
                                "needsLimitPrice": false,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": false,
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "needsBrackets": true
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206592.5
                                    },
                                    "freeCollateral": {
                                        "postOrder": 190232.4
                                    },
                                    "quoteBalance": {
                                        "postOrder": 217339.91
                                    },
                                    "notionalTotal": {
                                        "postOrder": 221657.87
                                    },
                                    "valueTotal": {
                                        "postOrder": -10747.41
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 16360.11
                                    },
                                    "leverage": {
                                        "postOrder": 1.07
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0792
                                    },
                                    "buyingPower": {
                                        "postOrder": 3804647.8
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": -66.226
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
                perp.trade("LIMIT", TradeInputField.type, 0)
            },
            null,
        )

        test(
            {
                perp.trade("1234.3", TradeInputField.limitPrice, 0)
                perp.trade("19023.4", TradeInputField.usdcSize, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "SELL",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 15.412,
                                "input": "size.usdcSize"
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testLimitTradeInput() {
        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("LIMIT", TradeInputField.type, 0)
        }, null)

        /*
        size = 1.0
         */
        test({
            perp.trade("1.0", TradeInputField.size, 0)
        }, null)

        test(
            {
                perp.trade("1833.9", TradeInputField.limitPrice, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "price": {
                                "limitPrice": 1833.9
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 1.0,
                                "usdcSize": 1833.9,
                                "price": 1833.9,
                                "total": -1834.26678,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": false,
                                "needsLimitPrice": true,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": true,
                                "needsReduceOnly": false,
                                "needsPostOnly": true,
                                "needsBrackets": false
                            },
                            "timeInForce": "GTT",
                            "goodTil": {
                                "unit": "D",
                                "duration": 28
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206724.18
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187879.3
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64450.94
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.21
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.12
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.87
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0912
                                    },
                                    "buyingPower": {
                                        "postOrder": 3757586.1
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 94.57
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
                perp.trade("10", TradeInputField.goodTilDuration, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "price": {
                                "limitPrice": 1833.9
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 1.0,
                                "usdcSize": 1833.9,
                                "price": 1833.9,
                                "total": -1834.26678,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": false,
                                "needsLimitPrice": true,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": true,
                                "needsReduceOnly": false,
                                "needsPostOnly": true,
                                "needsBrackets": false
                            },
                            "timeInForce": "GTT",
                            "goodTil": {
                                "unit": "D",
                                "duration": 10
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206724.18
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187879.3
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64450.94
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.21
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.12
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.87
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0912
                                    },
                                    "buyingPower": {
                                        "postOrder": 3757586.1
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 94.57
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
                perp.trade("M", TradeInputField.goodTilUnit, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "price": {
                                "limitPrice": 1833.9
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 1.0,
                                "usdcSize": 1833.9,
                                "price": 1833.9,
                                "total": -1834.26678,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": false,
                                "needsLimitPrice": true,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": true,
                                "needsReduceOnly": false,
                                "needsPostOnly": true,
                                "needsBrackets": false
                            },
                            "timeInForce": "GTT",
                            "goodTil": {
                                "unit": "M",
                                "duration": 10
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206724.18
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187879.3
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64450.94
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.21
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.12
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.87
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0912
                                    },
                                    "buyingPower": {
                                        "postOrder": 3757586.1
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 94.57
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

    private fun testStopLimitTradeInput() {
        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("STOP_LIMIT", TradeInputField.type, 0)
        }, null)

        /*
        size = 1.0
         */
        test({
            perp.trade("1.0", TradeInputField.size, 0)
        }, null)

        test({
            perp.trade("1833.9", TradeInputField.limitPrice, 0)
        }, null)

        test(
            {
                perp.trade("1800.0", TradeInputField.triggerPrice, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "price": {
                                "limitPrice": 1833.9,
                                "triggerPrice": 1800.0
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 1.0,
                                "usdcSize": 1833.9,
                                "price": 1833.9,
                                "total": -1834.81695,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": true,
                                "needsLimitPrice": true,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": true,
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "needsBrackets": false
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206723.63
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187878.75
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64451.5
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.21
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.12
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.87
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09
                                    },
                                    "buyingPower": {
                                        "postOrder": 3757575.1
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 94.57
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

    private fun testTakeProfitLimitTradeInput() {
        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("TAKE_PROFIT", TradeInputField.type, 0)
        }, null)

        /*
        size = 1.0
         */
        test({
            perp.trade("1.0", TradeInputField.size, 0)
        }, null)

        test({
            perp.trade("1833.9", TradeInputField.limitPrice, 0)
        }, null)

        test(
            {
                perp.trade("1800.0", TradeInputField.triggerPrice, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "price": {
                                "limitPrice": 1833.9,
                                "triggerPrice": 1800.0
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 1.0,
                                "usdcSize": 1833.9,
                                "price": 1833.9,
                                "total": -1834.81695,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": true,
                                "needsLimitPrice": true,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": true,
                                "needsReduceOnly": false,
                                "needsPostOnly": false,
                                "needsBrackets": false
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206723.63
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187878.75
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64451.5
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.21
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.12
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.87
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0912
                                    },
                                    "buyingPower": {
                                        "postOrder": 3757575.1
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 94.57
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

    private fun testTrailingStopTradeInput() {
        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("TRAILING_STOP", TradeInputField.type, 0)
        }, null)

        /*
        size = 1.0
         */
        test({
            perp.trade("1.0", TradeInputField.size, 0)
        }, null)
    }

    private fun testStopMarketTradeInput() {
        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("STOP_MARKET", TradeInputField.type, 0)
        }, null)

        /*
        size = 1.0
         */
        test({
            perp.trade("1.0", TradeInputField.size, 0)
        }, null)

        test(
            {
                perp.trade("2000.0", TradeInputField.triggerPrice, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "STOP_MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "price": {
                                "triggerPrice": 2000.0
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 1.0,
                                "usdcSize": 2000.0,
                                "price": 2000.0,
                                "payloadPrice": 2200.0,
                                "total": -2001.0,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": true,
                                "needsLimitPrice": false,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": true,
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "needsBrackets": false
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206723.55
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187878.67
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64451.57
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.21
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.12
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.87
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09
                                    },
                                    "buyingPower": {
                                        "postOrder": 3757573.4
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 94.57
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

    private fun testTakeProfitMarketTradeInput() {
        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)
        }, null)

        /*
        size = 1.0
         */
        test({
            perp.trade("1.0", TradeInputField.size, 0)
        }, null)

        test(
            {
                perp.trade("1500.0", TradeInputField.triggerPrice, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TAKE_PROFIT_MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "price": {
                                "triggerPrice": 1500.0
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 1.0,
                                "usdcSize": 1500.0,
                                "price": 1500.0,
                                "total": -1500.75,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": true,
                                "needsLimitPrice": false,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": true,
                                "needsReduceOnly": true,
                                "needsPostOnly": false,
                                "needsBrackets": false
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "postOrder": 206977.1
                                    },
                                    "freeCollateral": {
                                        "postOrder": 188132.21
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64198.03
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.21
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.12
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.87
                                    },
                                    "leverage": {
                                        "postOrder": 1.31
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.091
                                    },
                                    "buyingPower": {
                                        "postOrder": 3762644.3
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 94.57
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

    private fun testLimitTradeInputOnce() {
        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

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

        test({
            perp.trade("SELL", TradeInputField.side, 0)
        }, null)
    }

    private fun testSimpleTradeInputOnce() {
        var time = ServerTime.now()
        testSimpleMarketTradeInput()
        time = perp.log("Market Order", time)
    }

    private fun testSimpleMarketTradeInput() {
        /*
        Initial setup
         */

        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

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
                        "marketId": "ETH-USD",
                        "options": {
                            "needsSize": true,
                            "needsLeverage": false
                        }
                    }
                }
            }
            """.trimIndent(),
        )

        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        /*
        size = 0.1
         */
        test(
            {
                perp.trade(".1", TradeInputField.size, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "MARKET",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 0.1,
                                "usdcSize": 165.57,
                                "input": "size.size"
                            },
                            "marketOrder": {
                                "size": 0.1,
                                "usdcSize": 165.57,
                                "worstPrice": 1655.7,
                                "price": 1655.7,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 0.1,
                                        "price": 1655.7,
                                        "depth":31.231
                                    }
                                ]
                            },
                            "options": {
                                "needsSize": true,
                                "needsLeverage": true
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 10000.0,
                                        "postOrder": 10009.68
                                    },
                                    "freeCollateral": {
                                        "current": 10000.0,
                                        "postOrder": 10000.91
                                    },
                                    "quoteBalance": {
                                        "current": 10000.0,
                                        "postOrder": 9834.35
                                    },
                                    "notionalTotal": {
                                        "current": 0.0,
                                        "postOrder": 175.33
                                    },
                                    "valueTotal": {
                                        "current": 0.0,
                                        "postOrder": 175.33
                                    },
                                    "initialRiskTotal": {
                                        "current": 0.0,
                                        "postOrder": 8.77
                                    },
                                    "leverage": {
                                        "current": 0.0,
                                        "postOrder": 0.0175
                                    },
                                    "marginUsage": {
                                        "current": 0.0,
                                        "postOrder": 0.000876
                                    },
                                    "buyingPower": {
                                        "current": 200000.0,
                                        "postOrder": 200018.0
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 0.1
                                            },
                                            "valueTotal": {
                                                "postOrder": 175.33
                                            },
                                            "notionalTotal": {
                                                "postOrder": 175.33
                                            },
                                            "initialRiskTotal": {
                                                "postOrder": 8.77
                                            },
                                            "leverage": {
                                                "postOrder": 0.0175
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
                perp.trade("LIMIT", TradeInputField.type, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 0.1,
                                "input": "size.size"
                            },
                            "options": {
                                "needsSize": true,
                                "needsLeverage": false
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 10000.0,
                                        "postOrder": null
                                    },
                                    "freeCollateral": {
                                        "current": 10000.0,
                                        "postOrder": null
                                    },
                                    "quoteBalance": {
                                        "current": 10000.0,
                                        "postOrder": null
                                    },
                                    "notionalTotal": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "valueTotal": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "initialRiskTotal": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "leverage": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "marginUsage": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "buyingPower": {
                                        "current": 200000.0,
                                        "postOrder": null
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
                perp.trade("IOC", TradeInputField.timeInForceType, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "size": {
                                "size": 0.1,
                                "input": "size.size"
                            },
                            "options": {
                                "needsSize": true,
                                "needsLeverage": false
                            }
                        }
                    },
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 10000.0,
                                        "postOrder": null
                                    },
                                    "freeCollateral": {
                                        "current": 10000.0,
                                        "postOrder": null
                                    },
                                    "quoteBalance": {
                                        "current": 10000.0,
                                        "postOrder": null
                                    },
                                    "notionalTotal": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "valueTotal": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "initialRiskTotal": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "leverage": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "marginUsage": {
                                        "current": 0.0,
                                        "postOrder": null
                                    },
                                    "buyingPower": {
                                        "current": 200000.0,
                                        "postOrder": null
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
                perp.trade("ISOLATED", TradeInputField.marginMode, 0)
            },
            """
            
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "LIMIT",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "marginMode": "ISOLATED"
                        }
                    }
                }
            """.trimIndent(),
        )
    }
}
