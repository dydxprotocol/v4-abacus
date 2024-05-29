package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class V4ParentSubaccountTests : V4BaseTests(true) {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testAccountsOnce()
    }

    internal override fun loadSubaccounts(): StateResponse {
        return test({
            perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")
        }, null)
    }

    private fun testAccountsOnce() {
        var time = ServerTime.now()
        testSubaccountSubscribed()
        testTradeInput()
        testSubaccountChannelData()
        time = perp.log("Accounts Subscribed", time)
    }

    private fun testSubaccountSubscribed() {
        test(
            {
                perp.socket(testWsUrl, mock.parentSubaccountsChannel.subscribed, 0, null)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "total": 2800.8
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 89358.63
                                    },
                                    "freeCollateral": {
                                        "current": 88826.56
                                    },
                                    "quoteBalance": {
                                        "current": 100000.0
                                    },
                                    "openPositions": {
                                        "BTC-USD": {
                                            "id": "BTC-USD",
                                            "status": "OPEN",
                                            "maxSize": 0.442388027,
                                            "netFunding": 0.0,
                                            "size": {
                                                "current": -0.442371112
                                            },
                                            "assetId": "BTC",
                                            "resources": {
                                            },
                                            "valueTotal": {
                                                "current": -10641.37
                                            },
                                            "notionalTotal": {
                                                "current": 10641.37
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 532.07
                                            },
                                            "leverage": {
                                                "current": -0.12
                                            },
                                            "subaccountNumber": 0,
                                            "marginMode": "CROSS"
                                        }
                                    }
                                },
                                "128": {
                                    "equity": {
                                        "current": 829.16
                                    },
                                    "freeCollateral": {
                                        "current": 796.244
                                    },
                                    "quoteBalance": {
                                        "current": 500.0
                                    },
                                    "marginUsage": {
                                        "current": 0.0397
                                    },
                                    "openPositions": {
                                        "RUNE-USD": {
                                            "id": "RUNE-USD",
                                            "status": "OPEN",
                                            "maxSize": 12.0,
                                            "netFunding": 0.271316,
                                            "realizedPnl": {
                                                "current": 0.271316
                                            },
                                            "size": {
                                                "current": 120.0
                                            },
                                            "assetId": "RUNE",
                                            "resources": {
                                            },
                                            "notionalTotal": {
                                                "current": 329.16
                                            },
                                            "valueTotal": {
                                                "current": 329.16
                                            },
                                            "initialRiskTotal": {
                                                "current": 32.916
                                            },
                                            "leverage": {
                                                "current": 0.397
                                            },
                                            "buyingPower": {
                                                "current": 7962.44
                                            },
                                            "subaccountNumber": 128,
                                            "marginMode": "ISOLATED"
                                        }
                                    },
                                    "orders": {
                                        "b812bea8-29d3-5841-9549-caa072f6f8a9": {
                                            "clientId": "194126269",
                                            "clobPairId": "134",
                                            "side": "SELL",
                                            "size": "1.653451",
                                            "totalFilled": "0.682633",
                                            "price": "1255.927",
                                            "type": "LIMIT",
                                            "status": "BEST_EFFORT_CANCELED",
                                            "timeInForce": "GTT",
                                            "postOnly": false,
                                            "reduceOnly": false,
                                            "goodTilBlock": "5837",
                                            "subaccountNumber": 128,
                                            "marginMode": "ISOLATED"
                                        }
                                    }
                                }
                            },
                            "groupedSubaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 90687.79
                                    },
                                    "freeCollateral": {
                                        "current": 88826.56
                                    },
                                    "quoteBalance": {
                                        "current": 100000.0
                                    },
                                    "openPositions": {
                                        "BTC-USD": {
                                            "id": "BTC-USD",
                                            "status": "OPEN",
                                            "maxSize": 0.442388027,
                                            "netFunding": 0.0,
                                            "size": {
                                                "current": -0.442371112
                                            },
                                            "assetId": "BTC",
                                            "resources": {
                                            },
                                            "valueTotal": {
                                                "current": -10641.37
                                            },
                                            "notionalTotal": {
                                                "current": 10641.37
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 532.07
                                            },
                                            "leverage": {
                                                "current": -0.12
                                            },
                                            "subaccountNumber": 0,
                                            "marginMode": "CROSS"
                                        },
                                        "RUNE-USD": {
                                            "id": "RUNE-USD",
                                            "status": "OPEN",
                                            "maxSize": 12.0,
                                            "netFunding": 0.271316,
                                            "realizedPnl": {
                                                "current": 0.271316
                                            },
                                            "size": {
                                                "current": 120.0
                                            },
                                            "assetId": "RUNE",
                                            "resources": {
                                            },
                                            "notionalTotal": {
                                                "current": 329.16
                                            },
                                            "valueTotal": {
                                                "current": 329.16
                                            },
                                            "initialRiskTotal": {
                                                "current": 32.916
                                            },
                                            "leverage": {
                                                "current": 0.397
                                            },
                                            "buyingPower": {
                                                "current": 7962.44
                                            },
                                            "childSubaccountNumber": 128,
                                            "equity": {
                                                "current": 829.16
                                            },
                                            "quoteBalance": {
                                                "current": 500.0
                                            },
                                            "freeCollateral": {
                                                "current": 796.244
                                            },
                                            "marginUsage": {
                                                "current": 0.0397
                                            },
                                            "subaccountNumber": 128,
                                            "marginMode": "ISOLATED"
                                        }
                                    },
                                    "pendingPositions": [
                                        {
                                            "assetId": "APE"
                                        },
                                        {
                                            "assetId": "RUNE",
                                            "orderCount": 1,
                                            "quoteBalance": {
                                                "current": 500.0
                                            },
                                            "freeCollateral": {
                                                "current": 500.0
                                            },
                                            "equity": {
                                                "current": 500.0
                                            }
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testTradeInput() {
        test(
            {
                perp.tradeInMarket("RUNE-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marginMode": "ISOLATED",
                            "targetLeverage": 1.0,
                            "options": {
                                "needsMarginMode": false
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.tradeInMarket("BTC-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marginMode": "CROSS",
                            "options": {
                                "needsMarginMode": false
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.tradeInMarket("APE-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marginMode": "ISOLATED",
                            "options": {
                                "needsMarginMode": false
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
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marginMode": "CROSS",
                            "options": {
                                "needsMarginMode": false
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.tradeInMarket("AVAX-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marginMode": "ISOLATED",
                            "options": {
                                "needsMarginMode": false
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        test(
            {
                perp.tradeInMarket("LINK-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marginMode": "CROSS",
                            "options": {
                                "needsMarginMode": true
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testSubaccountChannelData() {
        test(
            {
                perp.socket(testWsUrl, mock.parentSubaccountsChannel.channel_data, 0, null)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "129": {
                                    "equity": {
                                        "current": 9822.9
                                    },
                                    "freeCollateral": {
                                        "current": 9740.61
                                    },
                                    "quoteBalance": {
                                        "current": 9000.0
                                    },
                                    "marginUsage": {
                                        "current": 0.0084
                                    },
                                    "openPositions": {
                                        "RUNE-USD": {
                                            "id": "RUNE-USD",
                                            "status": "OPEN",
                                            "maxSize": 300.0,
                                            "size": {
                                                "current": 300.0
                                            },
                                            "assetId": "RUNE",
                                            "resources": {
                                            },
                                            "notionalTotal": {
                                                "current": 822.9
                                            },
                                            "valueTotal": {
                                                "current": 822.9
                                            },
                                            "initialRiskTotal": {
                                                "current": 82.29
                                            },
                                            "leverage": {
                                                "current": 0.084
                                            },
                                            "buyingPower": {
                                                "current": 97406.1
                                            }
                                        }
                                    }
                                }
                            },
                            "groupedSubaccounts": {
                                "1": {
                                    "equity": {
                                        "current": 9822.9
                                    },
                                    "openPositions": {
                                        "RUNE-USD": {
                                            "id": "RUNE-USD",
                                            "status": "OPEN",
                                            "maxSize": 300.0,
                                            "size": {
                                                "current": 300.0
                                            },
                                            "assetId": "RUNE",
                                            "resources": {
                                            },
                                            "notionalTotal": {
                                                "current": 822.9
                                            },
                                            "valueTotal": {
                                                "current": 822.9
                                            },
                                            "initialRiskTotal": {
                                                "current": 82.29
                                            },
                                            "leverage": {
                                                "current": 0.084
                                            },
                                            "buyingPower": {
                                                "current": 97406.1
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

    @Test
    fun testWithRealData() {
        loadMarketsConfigurations()
        loadMarkets()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        loadSubaccountsWithRealData()

        testParentSubaccountSubscribedWithPendingPositions()
        testParentSubaccountSubscribed()
        testParentSubaccountChannelData()
    }

    internal fun loadSubaccountsWithRealData(): StateResponse {
        return test({
            perp.rest(
                AbUrl.fromString("$testRestUrl/v4/addresses/dydxaddress"),
                mock.parentSubaccountsChannel.rest_response,
                0,
                null,
            )
        }, null)
    }

    private fun testParentSubaccountSubscribedWithPendingPositions() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.read_subscribed_with_pending,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 1979.85
                                    },
                                    "freeCollateral": {
                                        "current": 1711.96
                                    },
                                    "quoteBalance": {
                                        "current": 1711.96
                                    },
                                    "openPositions": {
                                    },
                                    "pendingPositions": [
                                        {
                                            "assetId": "LDO",
                                            "firstOrderId": "d1deed71-d743-5528-aff2-cf3daf8b6413",
                                            "quoteBalance": {
                                                "current": 267.89
                                            },
                                            "freeCollateral": {
                                                "current": 267.89
                                            },
                                            "equity": {
                                                "current": 267.89
                                            }
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testParentSubaccountSubscribed() {
        test(
            {
                perp.socket(testWsUrl, mock.parentSubaccountsChannel.real_subscribed, 0, null)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 1997.66
                                    },
                                    "freeCollateral": {
                                        "current": 1711.96
                                    },
                                    "quoteBalance": {
                                        "current": 1711.96
                                    },
                                    "openPositions": {
                                        "LDO-USD": {
                                            "id": "LDO-USD",
                                            "status": "OPEN",
                                            "size": {
                                                "current": 11.0
                                            },
                                            "assetId": "LDO",
                                            "valueTotal": {
                                                "current": 17.81
                                            },
                                            "notionalTotal": {
                                                "current": 17.81
                                            },
                                            "leverage": {
                                                "current": 0.06
                                            },
                                            "buyingPower": {
                                                "current": 1410.69
                                            },
                                            "childSubaccountNumber": 128,
                                            "quoteBalance": {
                                                "current": 267.89
                                            },
                                            "freeCollateral": {
                                                "current": 282.14
                                            },
                                            "marginUsage": {
                                                "current": 0.012
                                            },
                                            "equity": {
                                                "current": 285.70
                                            }
                                        }
                                    },
                                    "pendingPositions": null
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testParentSubaccountChannelData() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.real_channel_batch_data,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "groupedSubaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 2107.37
                                    },
                                    "freeCollateral": {
                                        "current": 1711.96
                                    },
                                    "quoteBalance": {
                                        "current": 1711.96
                                    },
                                    "openPositions": {
                                        "LDO-USD": {
                                            "id": "LDO-USD",
                                            "status": "OPEN",
                                            "size": {
                                                "current": 17.0
                                            },
                                            "assetId": "LDO",
                                            "valueTotal": {
                                                "current": 27.52
                                            },
                                            "notionalTotal": {
                                                "current": 27.52
                                            },
                                            "leverage": {
                                                "current": 0.07
                                            },
                                            "buyingPower": {
                                                "current": 1949.55
                                            },
                                            "childSubaccountNumber": 128,
                                            "quoteBalance": {
                                                "current": 367.89
                                            },
                                            "freeCollateral": {
                                                "current": 389.91
                                            },
                                            "marginUsage": {
                                                "current": 0.014
                                            },
                                            "equity": {
                                                "current": 395.41
                                            }
                                        }
                                    },
                                    "pendingPositions": null
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }
}
