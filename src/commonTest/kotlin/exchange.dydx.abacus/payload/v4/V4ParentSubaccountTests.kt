package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
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

    internal fun loadSubaccounts2(): StateResponse {
        return test(
            {
                perp.socket(testWsUrl, mock.parentSubaccountsChannel.subscribed, 0, null)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 100000.0
                                    },
                                    "freeCollateral": {
                                        "current": 100000.0
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
                                                "current": -27928.13
                                            },
                                            "notionalTotal": {
                                                "current": 27928.13
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 1396.41
                                            },
                                            "leverage": {
                                                "current": -0.39
                                            },
                                            "subaccountNumber": 0,
                                            "marginMode": "CROSS"
                                        }
                                    }
                                },
                                "128": {
                                    "equity": {
                                        "current": 1132.02
                                    },
                                    "freeCollateral": {
                                        "current": 1005.612
                                    },
                                    "quoteBalance": {
                                        "current": 500.0
                                    },
                                    "marginUsage": {
                                        "current": 0.1117
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
                                                "current": 632.02
                                            },
                                            "valueTotal": {
                                                "current": 632.02
                                            },
                                            "initialRiskTotal": {
                                                "current": 126.403
                                            },
                                            "leverage": {
                                                "current": 0.56
                                            },
                                            "buyingPower": {
                                                "current": 5028.06
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
                                        "current": 73703.88
                                    },
                                    "freeCollateral": {
                                        "current": 70675.46
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
                                                "current": -27928.13
                                            },
                                            "notionalTotal": {
                                                "current": 27928.13
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 1396.41
                                            },
                                            "leverage": {
                                                "current": -0.39
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
                                                "current": 632.02
                                            },
                                            "valueTotal": {
                                                "current": 632.02
                                            },
                                            "initialRiskTotal": {
                                                "current": 126.403
                                            },
                                            "leverage": {
                                                "current": 0.56
                                            },
                                            "buyingPower": {
                                                "current": 5028.06
                                            },
                                            "childSubaccountNumber": 128,
                                            "equity": {
                                                "current": 1132.02
                                            },
                                            "quoteBalance": {
                                                "current": 500.0
                                            },
                                            "freeCollateral": {
                                                "current": 1005.612
                                            },
                                            "marginUsage": {
                                                "current": 0.1117
                                            },
                                            "subaccountNumber": 128,
                                            "marginMode": "ISOLATED"
                                        }
                                    },
                                    "pendingPositions": [
                                        {
                                            "assetId": "APE"
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

    internal override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    private fun testAccountsOnce() {
        var time = ServerTime.now()
        loadSubaccounts2()
        testTradeInput()
        testSubaccountChannelData()
        time = perp.log("Accounts Subscribed", time)
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
                            "marketId": "RUNE-USD",
                            "marginMode": "ISOLATED",
                            "targetLeverage": 1.0,
                            "options": {
                                "needsMarginMode": false
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
                perp.tradeInMarket("BTC-USD", 0)
            },
            """
                {
                    "input": {
                        "current": "trade",
                        "trade": {
                            "marketId": "BTC-USD",
                            "marginMode": "CROSS",
                            "options": {
                                "needsMarginMode": false
                            }
                        },
                        "receiptLines": [
                            "BUYING_POWER",
                            "MARGIN_USAGE",
                            "FEE",
                            "REWARD"
                        ]
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
                            "marketId": "APE-USD",
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

        // Test tradeInMarket when market has a marketType of ISOLATED
        test(
            {
                perp.tradeInMarket("ISO-USD", 0)
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

        test(
            {
                perp.tradeInMarket("ISO-USD", 0)
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
                perp.trade("2,0", TradeInputField.targetLeverage, 0)
            },
            """
                {
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("10", TradeInputField.size, 0)
            },
            """
                {
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("LIMIT", TradeInputField.type, 0)
            },
            """
                {
                }
            """.trimIndent(),
        )

        test(
            {
                perp.trade("20", TradeInputField.limitPrice, 0)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "quoteBalance": {
                                        "current": 100000.0,
                                        "postOrder": 99900.0
                                    }
                                },
                                "384": {
                                    "quoteBalance": {
                                        "postOrder": -100.0
                                    },
                                    "openPositions": {
                                        "ISO-USD": {
                                            "size": {
                                                "postOrder": 10.0
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
                                        "current": 10580.0
                                    },
                                    "freeCollateral": {
                                        "current": 10264.03
                                    },
                                    "quoteBalance": {
                                        "current": 9000.0
                                    },
                                    "marginUsage": {
                                        "current": 0.0299
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
                                                "current": 1580.0
                                            },
                                            "valueTotal": {
                                                "current": 1580.0
                                            },
                                            "initialRiskTotal": {
                                                "current": 316.01
                                            },
                                            "leverage": {
                                                "current": 0.149
                                            },
                                            "buyingPower": {
                                                "current": 51320.2
                                            }
                                        }
                                    }
                                }
                            },
                            "groupedSubaccounts": {
                                "1": {
                                    "equity": {
                                        "current": 10580.0
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
                                                "current": 1580.0
                                            },
                                            "valueTotal": {
                                                "current": 1580.0
                                            },
                                            "initialRiskTotal": {
                                                "current": 316.01
                                            },
                                            "leverage": {
                                                "current": 0.149
                                            },
                                            "buyingPower": {
                                                "current": 51320.2
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
                                        "current": 2001.4
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
                                                "current": 21.55
                                            },
                                            "notionalTotal": {
                                                "current": 21.55
                                            },
                                            "leverage": {
                                                "current": 0.07
                                            },
                                            "buyingPower": {
                                                "current": 1425.66
                                            },
                                            "childSubaccountNumber": 128,
                                            "quoteBalance": {
                                                "current": 267.89
                                            },
                                            "freeCollateral": {
                                                "current": 285.13
                                            },
                                            "marginUsage": {
                                                "current": 0.015
                                            },
                                            "equity": {
                                                "current": 289.4
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
                                        "current": 2113.16
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
                                                "current": 33.31
                                            },
                                            "notionalTotal": {
                                                "current": 33.31
                                            },
                                            "leverage": {
                                                "current": 0.08
                                            },
                                            "buyingPower": {
                                                "current": 1972.69
                                            },
                                            "childSubaccountNumber": 128,
                                            "quoteBalance": {
                                                "current": 367.89
                                            },
                                            "freeCollateral": {
                                                "current": 394.54
                                            },
                                            "marginUsage": {
                                                "current": 0.017
                                            },
                                            "equity": {
                                                "current": 401.2
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
