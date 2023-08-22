package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.modal.onChainAccountBalances
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountSubscribed
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountWithOrdersAndFillsChanged
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class V4AccountTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testAccountsOnce()
    }

    private fun testAccountsOnce() {
        var time = ServerTime.now()
        testSubaccountsReceived()
        time = perp.log("Accounts Received", time)

        testSubaccountSubscribed()
        time = perp.log("Accounts Subscribed", time)

        testSubaccountFillsReceived()
        time = perp.log("Fills Received", time)

        testSubaccountFillsChannelData()

        testSubaccountChanged()
        time = perp.log("Accounts Changed", time)

        testBatchedSubaccountChanged()

        testEquityTiers()

        testFeeTiers()

        testUserFeeTier()

        testUserStats()
    }

    private fun testSubaccountsReceived() {
        test(
            {
                perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")
            },
            """
            {
                "wallet": {
                    "account": {
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 108116.7318528828
                                },
                                "freeCollateral": {
                                    "current": 106640.3767269893
                                },
                                "quoteBalance": {
                                    "current": 99872.368956
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()
        )
    }


    private fun testSubaccountSubscribed() {
        test(
            {
                perp.loadv4SubaccountSubscribed(mock, testWsUrl)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 122034.2009050837
                                    },
                                    "freeCollateral": {
                                        "current": 100728.91072122751
                                    },
                                    "quoteBalance": {
                                        "current": 68257.215192
                                    },
                                    "orders": {
                                        "b812bea8-29d3-5841-9549-caa072f6f8a8": {
                                            "id": "b812bea8-29d3-5841-9549-caa072f6f8a8",
                                            "side": "SELL",
                                            "type": "LIMIT",
                                            "status": "BEST_EFFORT_OPENED",
                                            "timeInForce": "GTT",
                                            "price": 1255.927,
                                            "size": 1.653451,
                                            "postOnly": false,
                                            "reduceOnly": false,
                                            "remainingSize": 0.970818,
                                            "totalFilled": 0.682633,
                                            "resources": {
                                                "statusStringKey": "APP.TRADE.PENDING"
                                            }
                                        },
                                        "b812bea8-29d3-5841-9549-caa072f6f8a9": {
                                            "status": "BEST_EFFORT_CANCELED",
                                            "resources": {
                                                "statusStringKey": "APP.TRADE.CANCELING"
                                            }
                                        }
                                    },
                                    "openPositions": {
                                        "BTC-USD": {
                                            "id": "BTC-USD",
                                            "status": "OPEN",
                                            "maxSize": 9.974575029,
                                            "exitPrice": 17106.497989,
                                            "netFunding": 0.0,
                                            "realizedPnl": {
                                                "current": 126.640212
                                            },
                                            "unrealizedPnl": {
                                                "current": 69435.46665219103
                                            },
                                            "createdAt": "2022-12-11T17:27:36.351Z",
                                            "entryPrice": {
                                                "current": 17101.489388
                                            },
                                            "size": {
                                                "current": 9.974575029
                                            },
                                            "assetId": "BTC",
                                            "resources": {
                                            },
                                            "realizedPnlPercent": {
                                                "current": 7.4240911E-4
                                            },
                                            "unrealizedPnlPercent": {
                                                "current": 4.07054932706E-1
                                            },
                                            "valueTotal": {
                                                "current": 2.399413946951037E+5
                                            },
                                            "notionalTotal": {
                                                "current": 2.399413946951037E+5
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 1.1997069734755185E+4
                                            },
                                            "leverage": {
                                                "current": 1.966181553332
                                            }
                                        },
                                        "ETH-USD": {
                                            "id": "ETH-USD",
                                            "status": "OPEN",
                                            "maxSize": 106.180627,
                                            "netFunding": 0.0,
                                            "realizedPnl": {
                                                "current": -102.716895
                                            },
                                            "unrealizedPnl": {
                                                "current": -51808.1520058774
                                            },
                                            "createdAt": "2022-12-11T17:29:39.792Z",
                                            "entryPrice": {
                                                "current": 1266.094016
                                            },
                                            "size": {
                                                "current": -106.17985
                                            },
                                            "assetId": "ETH",
                                            "resources": {
                                            },
                                            "realizedPnlPercent": {
                                                "current": -7.64071181E-4
                                            },
                                            "unrealizedPnlPercent": {
                                                "current": -3.85380767806E-1
                                            },
                                            "valueTotal": {
                                                "current": -1.8616440898202E+5
                                            },
                                            "notionalTotal": {
                                                "current": 1.8616440898202E+5
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 9.308220449101E+3
                                            },
                                            "leverage": {
                                                "current": -1.525510124222
                                            },
                                            "liquidationPrice": {
                                            },
                                            "buyingPower": {
                                                "current": 2014578.2144245503
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

    private fun testSubaccountFillsReceived() {
        test(
            {
                perp.rest(
                    AbUrl.fromString("$testRestUrl/v4/fills?subaccountNumber=0"),
                    mock.fillsChannel.v4_rest,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 122034.2009050837
                                    },
                                    "freeCollateral": {
                                        "current": 100728.91072122751
                                    },
                                    "quoteBalance": {
                                        "current": 68257.215192
                                    },
                                    "fills": [
                                        {
                                            "id": "dad7abeb-4c04-58d3-8dda-fd0bc0528deb",
                                            "side": "BUY",
                                            "liquidity": "TAKER",
                                            "type": "LIMIT",
                                            "marketId": "BTC-USD",
                                            "orderId": "4f2a6f7d-a897-5c4e-986f-d48f5760102a",
                                            "createdAt": "2022-12-14T18:32:21.298Z",
                                            "price": 18275.31,
                                            "size" : 4.41E-6,
                                            "fee": 0.0,
                                            "resources": {
                                            }
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
            {
                val fills =
                    parser.asList(parser.value(perp.data, "wallet.account.subaccounts.0.fills"))
                assertEquals(
                    100,
                    fills?.size
                )
            }
        )
    }

    private fun testSubaccountFillsChannelData() {
        test(
            {
                perp.socket(testWsUrl, mock.fillsChannel.v4_channel_data, 0, null)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 122034.2009050837
                                    },
                                    "freeCollateral": {
                                        "current": 100728.91072122751
                                    },
                                    "quoteBalance": {
                                        "current": 68257.215192
                                    },
                                    "orders": {
                                        "b812bea8-29d3-5841-9549-caa072f6f8a8": {
                                            "id": "b812bea8-29d3-5841-9549-caa072f6f8a8",
                                            "side": "SELL",
                                            "type": "LIMIT",
                                            "status": "BEST_EFFORT_OPENED",
                                            "timeInForce": "GTT",
                                            "price": 1255.927,
                                            "size": 1.653451,
                                            "postOnly": false,
                                            "reduceOnly": false,
                                            "resources": {
                                            }
                                        },
                                        "f5d440b9-6e93-535a-a5d6-fbb74852c6d8": {
                                            "id": "f5d440b9-6e93-535a-a5d6-fbb74852c6d8",
                                            "side": "SELL",
                                            "type": "LIMIT",
                                            "status": "FILLED",
                                            "timeInForce": "GTT",
                                            "price": 1500.0,
                                            "size": 0.003,
                                            "postOnly": false,
                                            "reduceOnly": false,
                                            "resources":  {
                                            }
                                        }
                                    },
                                    "fills": [
                                        {
                                            "id": "0cf41e16-036e-534d-bbaf-cf318b44b840",
                                            "side": "SELL",
                                            "liquidity": "TAKER",
                                            "type": "LIMIT",
                                            "orderId": "f5d440b9-6e93-535a-a5d6-fbb74852c6d8",
                                            "createdAt": "2023-01-18T02:39:27.607Z",
                                            "price": 1570.19,
                                            "size": 0.003,
                                            "resources": {
                                            }
                                        },
                                        {
                                            "id": "dad7abeb-4c04-58d3-8dda-fd0bc0528deb",
                                            "side": "BUY",
                                            "liquidity": "TAKER",
                                            "type": "LIMIT",
                                            "marketId": "BTC-USD",
                                            "orderId": "4f2a6f7d-a897-5c4e-986f-d48f5760102a",
                                            "createdAt": "2022-12-14T18:32:21.298Z",
                                            "price": 18275.31,
                                            "size" : 4.41E-6,
                                            "fee": 0.0,
                                            "resources": {
                                            }
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
            {
                val fills =
                    parser.asList(parser.value(perp.data, "wallet.account.subaccounts.0.fills"))
                assertEquals(
                    101,
                    fills?.size
                )
            }
        )
    }

    private fun testSubaccountChanged() {
        test(
            {
                perp.loadv4SubaccountWithOrdersAndFillsChanged(mock, testWsUrl)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": -161020.0483525266
                                    },
                                    "freeCollateral": {
                                        "current": -172483.91152975292
                                    },
                                    "quoteBalance": {
                                        "current": 68257.215192
                                    },
                                    "orders": {
                                        "b812bea8-29d3-5841-9549-caa072f6f8a8": {
                                            "id": "b812bea8-29d3-5841-9549-caa072f6f8a8",
                                            "side": "SELL",
                                            "type": "LIMIT",
                                            "status": "FILLED",
                                            "timeInForce": "GTT",
                                            "price": 1255.927,
                                            "size": 1.653451,
                                            "postOnly": false,
                                            "reduceOnly": false,
                                            "resources": {
                                            }
                                        }
                                    },
                                    "openPositions": {
                                        "BTC-USD": {
                                            "id": "BTC-USD",
                                            "status": "OPEN",
                                            "maxSize": 1.792239322,
                                            "exitPrice": 17106.497989,
                                            "netFunding": 0.0,
                                            "unrealizedPnl": {
                                                "current": 69435.46665219103
                                            },
                                            "createdAt": "2022-12-11T17:27:36.351Z",
                                            "entryPrice": {
                                            },
                                            "size": {
                                                "current": -1.792239322
                                            },
                                            "assetId": "BTC",
                                            "resources": {
                                            },
                                            "unrealizedPnlPercent": {
                                                "current": 4.07054932706E-1
                                            },
                                            "valueTotal": {
                                                "current": -43112.8545625066
                                            },
                                            "notionalTotal": {
                                                "current": 43112.8545625066
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 2155.64272812533
                                            },
                                            "leverage": {
                                            }
                                        },
                                        "ETH-USD": {
                                            "id": "ETH-USD",
                                            "status": "OPEN",
                                            "maxSize": 106.180627,
                                            "netFunding": 0.0,
                                            "realizedPnl": {
                                                "current": -102.716895
                                            },
                                            "unrealizedPnl": {
                                                "current": -51808.1520058774
                                            },
                                            "createdAt": "2022-12-11T17:29:39.792Z",
                                            "entryPrice": {
                                                "current": 1266.094016
                                            },
                                            "size": {
                                                "current": -106.17985
                                            },
                                            "assetId": "ETH",
                                            "resources": {
                                            },
                                            "realizedPnlPercent": {
                                                "current": -7.64071181E-4
                                            },
                                            "unrealizedPnlPercent": {
                                                "current": -3.85380767806E-1
                                            },
                                            "valueTotal": {
                                                "current": -1.8616440898202E+5
                                            },
                                            "notionalTotal": {
                                                "current": 1.8616440898202E+5
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 9.308220449101E+3
                                            },
                                            "leverage": {
                                            },
                                            "liquidationPrice": {
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
            {
                val fills =
                    parser.asList(parser.value(perp.data, "wallet.account.subaccounts.0.fills"))
                assertEquals(
                    102,
                    fills?.size
                )
            }
        )

        test(
            {
                perp.socket(testWsUrl, mock.accountsChannel.v4_best_effort_cancelled, 0, 16940)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "orders": {
                                        "80133551-6d61-573b-9788-c1488e11027a": {
                                            "id": "80133551-6d61-573b-9788-c1488e11027a",
                                            "status": "BEST_EFFORT_CANCELED"
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
                perp.socket(testWsUrl, mock.accountsChannel.v4_best_effort_cancelled, 0, 16960)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "orders": {
                                        "80133551-6d61-573b-9788-c1488e11027a": {
                                            "id": "80133551-6d61-573b-9788-c1488e11027a",
                                            "status": "CANCELED"
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


    private fun testBatchedSubaccountChanged() {
        test(
            {
                perp.socket(testWsUrl, mock.accountsChannel.v4_batched, 0, 16960)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "openPositions": {
                                        "ETH-USD": {
                                            "size": {
                                                "current": 0.09
                                            }
                                        }
                                    },
                                    "orders": {
                                        "1118c548-1715-5a72-9c41-f4388518c6e2": {
                                            "status": "PARTIALLY_FILLED"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
            {
                val fills =
                    parser.asList(parser.value(perp.data, "wallet.account.subaccounts.0.fills"))
                assertEquals(
                    112,
                    fills?.size
                )
            }
        )
    }

    private fun testEquityTiers() {
        test(
            {
                perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
            },
            """
                {
                    "configs": {
                        "equityTiers": {
                            "shortTermOrderEquityTiers": [
                                {
                                    "requiredTotalNetCollateralUSD": "0",
                                    "maxOrders": 0
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "20",
                                    "maxOrders": 1
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "100",
                                    "maxOrders": 5
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "1000",
                                    "maxOrders": 10
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "10000",
                                    "maxOrders": 100
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "100000",
                                    "maxOrders": 200
                                }
                            ],
                            "statefulOrderEquityTiers": [
                                {
                                    "requiredTotalNetCollateralUSD": "0",
                                    "maxOrders": 0
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "20",
                                    "maxOrders": 1
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "100",
                                    "maxOrders": 5
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "1000",
                                    "maxOrders": 10
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "10000",
                                    "maxOrders": 100
                                },
                                {
                                    "requiredTotalNetCollateralUSD": "100000",
                                    "maxOrders": 200
                                }
                            ]
                        }
                    }
                }
            """.trimIndent(),
            {
            }
        )
    }

    private fun testFeeTiers() {
        val maxLong: Long = 9223372036854775807
        test(
            {
                perp.parseOnChainFeeTiers(mock.v4OnChainMock.fee_tiers)
            },
            """
                {
                    "configs": {
                        "feeTiers": [
                            {
                                "tier": "1",
                                "symbol": "≥",
                                "volume": 0.0,
                                "totalShare": 0.0,
                                "makerShare": 0.0
                            }
                        ]
                    }
                }
            """.trimIndent(),
            {
            }
        )
    }

    private fun testUserFeeTier() {
        test(
            {
                perp.parseOnChainUserFeeTier(mock.v4OnChainMock.user_fee_tier)
            },
            """
                {
                    "wallet": {
                        "user": {
                            "feeTierId": "1",
                            "makerFeeRate": 0.0,
                            "takerFeeRate": 0.0
                        }
                    }
                }
            """.trimIndent(),
            {
            }
        )
    }

    private fun testUserStats() {
        test(
            {
                perp.parseOnChainUserStats(mock.v4OnChainMock.user_stats)
            },
            """
                {
                    "wallet": {
                        "user": {
                            "makerVolume30D": 1.0,
                            "takerVolume30D": 1.0
                        }
                    }
                }
            """.trimIndent(),
            {
            }
        )
    }

    @Test
    fun testAccountBalances() {
        test(
            {
                val changes = perp.onChainAccountBalances(mock.v4OnChainMock.account_balances)
                perp.update(changes)
                return@test StateResponse(perp.state, changes)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "balances": {
                                "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5": {
                                     "denom": "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                                      "amount": "110"
                                },
                                "dv4tnt": {
                                     "denom": "dv4tnt",
                                     "amount": "1220"
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
            {
            }
        )
    }
}