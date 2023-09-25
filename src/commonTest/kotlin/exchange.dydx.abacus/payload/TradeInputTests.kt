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
                                "leverage": 0.8017014152220583,
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
                                        "postOrder": 206821.311246
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187976.4370598
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64353.807378
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.210824
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.118624
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.874186200002
                                    },
                                    "leverage": {
                                        "postOrder": 1.312017650353467
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09111669427424385
                                    },
                                    "buyingPower": {
                                        "postOrder": 3759528.7411959996
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
                                                "current": 3759346.7273959992,
                                                "postOrder": 3759528.7411959996
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
                                        "postOrder": 206821.311246
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187976.4370598
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64353.807378
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.210824
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.118624
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.874186200002
                                    },
                                    "leverage": {
                                        "postOrder": 1.312017650353467
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09111669427424385
                                    },
                                    "buyingPower": {
                                        "postOrder": 3759528.7411959996
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
                                "leverage": 1.0728660273301338,
                                "input": "size.size"
                            },
                            "marketOrder": {
                                "size": 35.0,
                                "usdcSize": 57949.8769,
                                "worstPrice": 1655.8,
                                "price": 1655.7107685714286,
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
                                        "postOrder": 210110.95605755
                                    },
                                    "freeCollateral": {
                                        "postOrder": 188285.48343135
                                    },
                                    "quoteBalance": {
                                        "postOrder": -120676.13136645
                                    },
                                    "notionalTotal": {
                                        "postOrder": 330965.179624
                                    },
                                    "valueTotal": {
                                        "postOrder": 330787.087424
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 21825.4726262
                                    },
                                    "leverage": {
                                        "postOrder": 1.5751923927914908
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.10387593791264238
                                    },
                                    "buyingPower": {
                                        "postOrder": 3765709.668627
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
                                "leverage": 0.8107145169233759,
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
                                        "postOrder": 206929.01108055003
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187986.56612777003
                                    },
                                    "quoteBalance": {
                                        "postOrder": -66197.52287505
                                    },
                                    "notionalTotal": {
                                        "postOrder": 273304.6261556
                                    },
                                    "valueTotal": {
                                        "postOrder": 273126.53395560005
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18942.444952780002
                                    },
                                    "leverage": {
                                        "postOrder": 1.3207651490163086
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09154078905546204
                                    },
                                    "buyingPower": {
                                        "postOrder": 3759731.3225554004
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
                                "leverage": 1.082580051235497,
                                "input": "size.usdcSize"
                            },
                            "marketOrder": {
                                "size": 36.238,
                                "usdcSize": 59999.7573,
                                "worstPrice": 1655.8,
                                "price": 1655.7138169876926,
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
                                        "postOrder": 210230.62769895
                                    },
                                    "freeCollateral": {
                                        "postOrder": 188296.62622367
                                    },
                                    "quoteBalance": {
                                        "postOrder": -122727.03670665
                                    },
                                    "notionalTotal": {
                                        "postOrder": 333135.7566056
                                    },
                                    "valueTotal": {
                                        "postOrder": 332957.6644056
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 21934.00147528
                                    },
                                    "leverage": {
                                        "postOrder": 1.5846204725348108
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.10433304469170623
                                    },
                                    "buyingPower": {
                                        "postOrder": 3765932.5244734
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
                                        "postOrder": 200163.0873642
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187106.53534847999
                                    },
                                    "quoteBalance": {
                                        "postOrder": 44754.4121498
                                    },
                                    "notionalTotal": {
                                        "postOrder": 155586.7674144
                                    },
                                    "valueTotal": {
                                        "postOrder": 155408.6752144
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 13056.55201572
                                    },
                                    "leverage": {
                                        "postOrder": 0.7772999980326409
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.06522956948582337
                                    },
                                    "buyingPower": {
                                        "postOrder": 3742130.7069696
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
                                        "postOrder": 212314.44562405004
                                    },
                                    "freeCollateral": {
                                        "postOrder": 188490.39407917002
                                    },
                                    "quoteBalance": {
                                        "postOrder": -158444.22017355
                                    },
                                    "notionalTotal": {
                                        "postOrder": 370936.7579976
                                    },
                                    "valueTotal": {
                                        "postOrder": 370758.66579760006
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 23824.051544880003
                                    },
                                    "leverage": {
                                        "postOrder": 1.7471103151145262
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.11221116620140781
                                    },
                                    "buyingPower": {
                                        "postOrder": 3769807.8815834005
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
                                "price": 1652.3681854398967,
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
                                        "postOrder": 183865.63036265
                                    },
                                    "freeCollateral": {
                                        "postOrder": 161819.68111655
                                    },
                                    "quoteBalance": {
                                        "postOrder": 308329.88878465
                                    },
                                    "notionalTotal": {
                                        "postOrder": 335374.712022
                                    },
                                    "valueTotal": {
                                        "postOrder": -124464.258422
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 22045.9492461
                                    },
                                    "leverage": {
                                        "postOrder": 1.8240206794522658
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1199025027277657
                                    },
                                    "buyingPower": {
                                        "postOrder": 3236393.622331
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": -131.085
                                            },
                                            "valueTotal": {
                                                "postOrder": -229830.439122
                                            },
                                            "notionalTotal": {
                                                "postOrder": 229830.439122
                                            },
                                            "leverage": {
                                                "postOrder": -1.2499913043492177
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
                                "leverage": -1.5131779967678753,
                                "input": "size.size"
                            },
                            "marketOrder": {
                                "size": 2.5E+2,
                                "usdcSize": 4.130580213E+5,
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
                                        "postOrder": 181252.73818534997
                                    },
                                    "freeCollateral": {
                                        "postOrder": 156984.92813154997
                                    },
                                    "quoteBalance": {
                                        "postOrder": 350154.21276135
                                    },
                                    "notionalTotal": {
                                        "postOrder": 379811.928176
                                    },
                                    "valueTotal": {
                                        "postOrder": -168901.47457600004
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 24267.810053800004
                                    },
                                    "leverage": {
                                        "postOrder": 2.095482429554264
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.1338893431170326
                                    },
                                    "buyingPower": {
                                        "postOrder": 3139698.5626309994
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": -156.43
                                            },
                                            "valueTotal": {
                                                "postOrder": -274267.65527600003
                                            },
                                            "notionalTotal": {
                                                "postOrder": 274267.65527600003
                                            },
                                            "leverage": {
                                                "postOrder": -1.5131779967678753
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
                                "usdcSize": 264647.41120000003,
                                "price": 1656.1682856159455,
                                "total": -264779.7349056,
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
                                        "postOrder": 222112.29788440003
                                    },
                                    "freeCollateral": {
                                        "postOrder": 189346.71401350005
                                    },
                                    "quoteBalance": {
                                        "postOrder": -327477.0144336
                                    },
                                    "notionalTotal": {
                                        "postOrder": 549767.4045180001
                                    },
                                    "valueTotal": {
                                        "postOrder": 549589.312318
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 32765.5838709
                                    },
                                    "leverage": {
                                        "postOrder": 2.4751776905398124
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.14751809865094934
                                    },
                                    "buyingPower": {
                                        "postOrder": 3786934.2802700005
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
                                "price": 1652.7206908851967,
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
                                        "postOrder": 190521.5135506
                                    },
                                    "freeCollateral": {
                                        "postOrder": 174161.4941521
                                    },
                                    "quoteBalance": {
                                        "postOrder": 201267.1750206
                                    },
                                    "notionalTotal": {
                                        "postOrder": 221656.11507
                                    },
                                    "valueTotal": {
                                        "postOrder": -10745.66146999999
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 16360.0193985
                                    },
                                    "leverage": {
                                        "postOrder": 1.1634177733483682
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.08586966948567198
                                    },
                                    "buyingPower": {
                                        "postOrder": 3483229.883042
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
                            "goodUntil": {
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
                                        "postOrder": 206643.572316
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.6981298
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64531.546308
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.210824
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.118624
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.874186200002
                                    },
                                    "leverage": {
                                        "postOrder": 1.3131461471690289
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09119506585659665
                                    },
                                    "buyingPower": {
                                        "postOrder": 3755973.9625959997
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

                perp.trade("10", TradeInputField.goodUntilDuration, 0)
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
                            "goodUntil": {
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
                                        "postOrder": 206643.572316
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.6981298
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64531.546308
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.210824
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.118624
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.874186200002
                                    },
                                    "leverage": {
                                        "postOrder": 1.3131461471690289
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09119506585659665
                                    },
                                    "buyingPower": {
                                        "postOrder": 3755973.9625959997
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
                perp.trade("M", TradeInputField.goodUntilUnit, 0)
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
                            "goodUntil": {
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
                                        "postOrder": 206643.572316
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.6981298
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64531.546308
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.210824
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.118624
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.874186200002
                                    },
                                    "leverage": {
                                        "postOrder": 1.3131461471690289
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09119506585659665
                                    },
                                    "buyingPower": {
                                        "postOrder": 3755973.9625959997
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
                                        "postOrder": 206643.022146
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.1479598
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64532.096478
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.210824
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.118624
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.874186200002
                                    },
                                    "leverage": {
                                        "postOrder": 1.3131496433123213
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0911953086559365
                                    },
                                    "buyingPower": {
                                        "postOrder": 3755962.959196
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
                                        "postOrder": 206643.022146
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187798.1479598
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64532.096478
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.210824
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.118624
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.874186200002
                                    },
                                    "leverage": {
                                        "postOrder": 1.3131496433123213
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.0911953086559365
                                    },
                                    "buyingPower": {
                                        "postOrder": 3755962.959196
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

        test(
            {
                perp.trade("0.05", TradeInputField.trailingPercent, 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "type": "TRAILING_STOP",
                            "side": "BUY",
                            "marketId": "ETH-USD",
                            "price": {
                                "trailingPercent": 0.05
                            },
                            "size": {
                                "size": 1.0,
                                "input": "size.size"
                            },
                            "summary": {
                                "size": 1.0,
                                "usdcSize": 1840.95786,
                                "price": 1840.95786,
                                "total": -1841.87833893,
                                "filled": true
                            },
                            "options": {
                                "needsSize": true,
                                "needsTriggerPrice": false,
                                "needsLimitPrice": false,
                                "needsTrailingPercent": true,
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
                                        "postOrder": 206635.96075707
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187791.08657087
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64539.15786693
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.210824
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.118624
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.874186200002
                                    },
                                    "leverage": {
                                        "postOrder": 1.313194517691015
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09119842508127052
                                    },
                                    "buyingPower": {
                                        "postOrder": 3755821.7314173994
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
                                        "postOrder": 206476.839096
                                    },
                                    "freeCollateral": {
                                        "postOrder": 187631.9649098
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64698.279528
                                    },
                                    "notionalTotal": {
                                        "postOrder": 271353.210824
                                    },
                                    "valueTotal": {
                                        "postOrder": 271175.118624
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 18844.874186200002
                                    },
                                    "leverage": {
                                        "postOrder": 1.3142065328588073
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09126870727344971
                                    },
                                    "buyingPower": {
                                        "postOrder": 3752639.298196
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
                                        "postOrder": 183023.151172
                                    },
                                    "freeCollateral": {
                                        "postOrder": 165375.973882
                                    },
                                    "quoteBalance": {
                                        "postOrder": -64198.029528
                                    },
                                    "notionalTotal": {
                                        "postOrder": 247399.2729
                                    },
                                    "valueTotal": {
                                        "postOrder": 247221.1807
                                    },
                                    "initialRiskTotal": {
                                        "postOrder": 17647.17729
                                    },
                                    "leverage": {
                                        "postOrder": 1.3517375879267925
                                    },
                                    "marginUsage": {
                                        "postOrder": 0.09642046471714227
                                    },
                                    "buyingPower": {
                                        "postOrder": 3307519.47764
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
                                        "postOrder": 10009.676535
                                    },
                                    "freeCollateral": {
                                        "current": 10000.0,
                                        "postOrder": 10000.910069
                                    },
                                    "quoteBalance": {
                                        "current": 10000.0,
                                        "postOrder": 9834.347215
                                    },
                                    "notionalTotal": {
                                        "current": 0.0,
                                        "postOrder": 175.32932000000002
                                    },
                                    "valueTotal": {
                                        "current": 0.0,
                                        "postOrder": 175.32932000000002
                                    },
                                    "initialRiskTotal": {
                                        "current": 0.0,
                                        "postOrder": 8.766466000000001
                                    },
                                    "leverage": {
                                        "current": 0.0,
                                        "postOrder": 0.01751598259813298
                                    },
                                    "marginUsage": {
                                        "current": 0.0,
                                        "postOrder": 8.757991299067625E-4
                                    },
                                    "buyingPower": {
                                        "current": 200000.0,
                                        "postOrder": 200018.20138
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "postOrder": 0.1
                                            },
                                            "valueTotal": {
                                                "postOrder": 175.32932000000002
                                            },
                                            "notionalTotal": {
                                                "postOrder": 175.32932000000002
                                            },
                                            "initialRiskTotal": {
                                                "postOrder": 8.766466000000001
                                            },
                                            "leverage": {
                                                "postOrder": 0.01751598259813298
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