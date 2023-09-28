package exchange.dydx.abacus.payload

import exchange.dydx.abacus.payload.v3.V3BaseTests
import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.trade
import exchange.dydx.abacus.state.modal.tradeInMarket
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
            """.trimIndent()
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
            """.trimIndent()
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
            """.trimIndent()
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
            """.trimIndent()
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
                                        "postOrder": -64353.81
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
                                        "postOrder": 1.3120
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
            """.trimIndent()
        )

        test ({
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
                                        "postOrder": -64353.81
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
                                        "postOrder": 1.3120
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
            """.trimIndent()
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
                                        "postOrder": 188285.48
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
            """.trimIndent()
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
                                "size": 2.113,
                                "usdcSize": 3500.0,
                                "leverage": 0.8107,
                                "input": "size.usdcSize"
                            },
                            "marketOrder": {
                                "size": 2.113,
                                "usdcSize": 3498.4941,
                                "worstPrice": 1655.7,
                                "price": 1655.7,
                                "filled": true,
                                "orderbook": [
                                    {
                                        "size": 2.113,
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
                                        "postOrder": 206929.01
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187986.57
                                    },
                                    "quoteBalance": {
                                        "postOrder": -66197.52
                                    },
                                    "notionalTotal": {
                                        "postOrder": 273304.63
                                    },
                                    "valueTotal": {
                                        "postOrder": 273126.53
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18942.44
                                    },
                                    "leverage": {
                                        "postOrder": 1.32
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0915
                                    },
                                    "buyingPower": {
                                        "postOrder": 3759731.32
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 95.683
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
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
                                "leverage": 1.08,
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
            """.trimIndent()
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
                                "size": 65.028,
                                "usdcSize": 107505.4444,
                                "input": "size.leverage",
                                "leverage": 0.25
                            },
                            "marketOrder": {
                                "size": 65.028,
                                "usdcSize": 107505.4444,
                                "worstPrice": 1.6529E+3,
                                "price": 1653.217758504029,
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
                                        "size": 32.115,
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
                                        "postOrder": 200163.09
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187106.54
                                    },
                                    "quoteBalance": {
                                        "postOrder": 44754.41
                                    },
                                    "notionalTotal": {
                                        "postOrder": 155586.77
                                    },
                                    "valueTotal": {
                                        "postOrder": 155408.68
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 13056.55
                                    },
                                    "leverage": {
                                        "postOrder": 0.78
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0652
                                    },
                                    "buyingPower": {
                                        "postOrder": 3742130.71
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 28.542
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
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
                                "price": 1655.7509100660923,
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
                                        "postOrder": 212314.45
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
                                        "postOrder": 3769807.88
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
            """.trimIndent()
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
                                "size": 224.655,
                                "usdcSize": 371212.7747,
                                "leverage": -1.25,
                                "input": "size.leverage"
                            },
                            "marketOrder": {
                                "size": 224.655,
                                "usdcSize": 371212.7747,
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
                                        "postOrder": 183865.63
                                    },
                                    "freeCollateral": {
                                        "postOrder": 161819.68
                                    },
                                    "quoteBalance": {
                                        "postOrder": 308329.89
                                    },
                                    "notionalTotal": {
                                        "postOrder": 335374.71
                                    },
                                    "valueTotal": {
                                        "postOrder": -124464.26
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 22045.95
                                    },
                                    "leverage": {
                                        "postOrder": 1.82
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1199
                                    },
                                    "buyingPower": {
                                        "postOrder": 3236393.62
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": -131.085
                                            },
                                            "valueTotal": {
                                                "postOrder": -229830.44
                                            },
                                            "notionalTotal": {
                                                "postOrder": 229830.44
                                            },
                                            "leverage": {
                                                "postOrder": -1.25
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
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
                                "leverage": -1.5132,
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
                                        "postOrder": 181252.74
                                    },
                                    "freeCollateral": {
                                        "postOrder": 156984.93
                                    },
                                    "quoteBalance": {
                                        "postOrder": 350154.21
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
                                        "postOrder": 2.0955
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1339
                                    },
                                    "buyingPower": {
                                        "postOrder": 3139698.56
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
                                                "postOrder": -1.51
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
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
                                "size": 159.795,
                                "usdcSize": 264647.4112,
                                "input": "size.leverage"
                            },
                            "summary": {
                                "size": 159.795,
                                "usdcSize": 264647.41,
                                "price": 1656.1682856159455,
                                "total": -264779.73,
                                "slippage": 0.00078,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": false,
                                "needsLimitPrice": false,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": false,
                                "needsReduceOnly": false,
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
                                        "postOrder": 222112.30
                                    },
                                    "freeCollateral": {
                                        "postOrder": 189346.71
                                    },
                                    "quoteBalance": {
                                        "postOrder": -327477.01
                                    },
                                    "notionalTotal": {
                                        "postOrder": 549767.40
                                    },
                                    "valueTotal": {
                                        "postOrder": 549589.31
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 32765.58
                                    },
                                    "leverage": {
                                        "postOrder": 2.48
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1475
                                    },
                                    "buyingPower": {
                                        "postOrder": 3786934.28
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 253.365
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
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
                                "size": 159.795,
                                "usdcSize": 264096.5028,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 159.795,
                                "usdcSize": 264096.5028,
                                "price": 1652.72,
                                "total": 263964.4545486,
                                "slippage": 0.00126,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": false,
                                "needsLimitPrice": false,
                                "needsTrailingPercent": false,
                                "needsGoodUntil": false,
                                "needsReduceOnly": false,
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
                                        "postOrder": 190521.51
                                    },
                                    "freeCollateral": {
                                        "postOrder": 174161.49
                                    },
                                    "quoteBalance": {
                                        "postOrder": 201267.18
                                    },
                                    "notionalTotal": {
                                        "postOrder": 221656.12
                                    },
                                    "valueTotal": {
                                        "postOrder": -10745.66
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 16360.02
                                    },
                                    "leverage": {
                                        "postOrder": 1.16
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0859
                                    },
                                    "buyingPower": {
                                        "postOrder": 3483229.88
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": -66.225
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )

        test(
            {
                perp.trade("LIMIT", TradeInputField.type, 0)
            }, null)


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
            """.trimIndent()
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
                                        "postOrder": 206643.57
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.70
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64531.55
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
                                        "postOrder": 3755973.96
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
            """.trimIndent()
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
                                        "postOrder": 206643.57
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.70
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64531.55
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
                                        "postOrder": 3755973.96
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
            """.trimIndent()
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
                                        "postOrder": 206643.57
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.70
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64531.55
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
                                        "postOrder": 3755973.96
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
            """.trimIndent()
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
                                        "postOrder": 206643.02
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.15
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64532.10
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
                                        "postOrder": 3755962.96
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
            """.trimIndent()
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
                                        "postOrder": 206643.02
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.15
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64532.10
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
                                        "postOrder": 3755962.96
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
            """.trimIndent()
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
                                "payloadPrice": 2100.0,
                                "total": -2001.0,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": true,
                                "needsLimitPrice": false,
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
                                        "postOrder": 206476.84
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187631.96
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64698.28
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
                                        "postOrder": 3752639.30
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
            """.trimIndent()
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
                                        "postOrder": 183023.15
                                    },
                                    "freeCollateral": {
                                        "postOrder": 165375.97
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64198.03
                                    },
                                    "notionalTotal": {
                                        "postOrder": 247399.27
                                    },
                                    "valueTotal": {
                                        "postOrder": 247221.18
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 17647.18
                                    },
                                    "leverage": {
                                        "postOrder": 1.35
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0964
                                    },
                                    "buyingPower": {
                                        "postOrder": 3307519.48
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
            """.trimIndent()
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
            """.trimIndent()
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
                                        "postOrder": 200018.20
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
            """.trimIndent()
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
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": null
                                            },
                                            "valueTotal": {
                                                "postOrder": null
                                            },
                                            "notionalTotal": {
                                                "postOrder": null
                                            },
                                            "initialRiskTotal": {
                                                "postOrder": null
                                            },
                                            "leverage": {
                                                "postOrder": null
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
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
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": null
                                            },
                                            "valueTotal": {
                                                "postOrder": null
                                            },
                                            "notionalTotal": {
                                                "postOrder": null
                                            },
                                            "initialRiskTotal": {
                                                "postOrder": null
                                            },
                                            "leverage": {
                                                "postOrder": null
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }
}