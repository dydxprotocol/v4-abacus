package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test

class V4ParentSubaccountTests : V4BaseTests() {
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
        testSubaccountChannelData()
        testSubaccountChannelDataWithFill()
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
                                            }
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
                                            }
                                        }
                                    },
                                    "orders": {
                                        "b812bea8-29d3-5841-9549-caa072f6f8a9": {
                                            "clientId": "2194126269",
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
                                            "goodTilBlock": "5837"
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
                                            }
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
                                            }
                                        }
                                    },
                                    "pendingPositions": [
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
                                    },
                                    "fills":[
                                        {
                                           "id":"180c2462-eb3b-5985-a702-32c503462a37",
                                           "side":"BUY",
                                           "size":"0.01",
                                           "type":"LIMIT",
                                           "price":"1878",
                                           "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                                           "createdAt":"2023-07-07T17:20:10.369Z",
                                           "liquidity":"TAKER",
                                           "marketId":"ETH-USD",
                                           "resources": {
                                           }
                                        }
                                     ],
                                     "transfers": [
                                         {
                                             "id": "89586775-0646-582e-9b36-4f131715644d",
                                             "type": "WITHDRAWAL",
                                             "asset": "USDC",
                                             "transactionHash": "MOCKHASH1",
                                             "createdAt": "2023-08-21T21:37:53.373Z",
                                             "amount": 419.98472,
                                             "updatedAtBlock": 404014,
                                             "fromAddress": "dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                                             "toAddress": "dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8",
                                             "status": "CONFIRMED"
                                         }
                                     ]
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
                                    },
                                    "fills":[
                                        {
                                           "id":"180c2462-eb3b-5985-a702-32c503462a37",
                                           "side":"BUY",
                                           "size":"0.01",
                                           "type":"LIMIT",
                                           "price":"1878",
                                           "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                                           "createdAt":"2023-07-07T17:20:10.369Z",
                                           "liquidity":"TAKER",
                                           "marketId":"ETH-USD",
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
        )
    }

    private fun testSubaccountChannelDataWithFill() {
        test(
            {
                perp.socket(testWsUrl, mock.parentSubaccountsChannel.channel_data_with_fill_only, 0, null)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "1": {
                                    "fills":[
                                        {
                                           "id":"180c2462-eb3b-5985-a702-32c503462a37",
                                           "side":"BUY",
                                           "size":"0.01",
                                           "type":"LIMIT",
                                           "price":"1878",
                                           "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                                           "createdAt":"2023-07-07T17:11:10.369Z",
                                           "liquidity":"TAKER",
                                           "marketId":"ETH-USD",
                                           "resources": {
                                           }
                                        }
                                     ]
                                },
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
                                    },
                                    "fills":[
                                        {
                                           "id":"180c2462-eb3b-5985-a702-32c503462a37",
                                           "side":"BUY",
                                           "size":"0.01",
                                           "type":"LIMIT",
                                           "price":"1878",
                                           "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                                           "createdAt":"2023-07-07T17:20:10.369Z",
                                           "liquidity":"TAKER",
                                           "marketId":"ETH-USD",
                                           "resources": {
                                           }
                                        }
                                     ]
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
                                    },
                                    "fills":[
                                        {
                                           "id":"180c2462-eb3b-5985-a702-32c503462a37",
                                           "subaccountNumber": 1,
                                           "side":"BUY",
                                           "size":"0.01",
                                           "type":"LIMIT",
                                           "price":"1878",
                                           "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                                           "createdAt":"2023-07-07T17:11:10.369Z",
                                           "liquidity":"TAKER",
                                           "marketId":"ETH-USD",
                                           "resources": {
                                           }
                                        },
                                        {
                                           "id":"180c2462-eb3b-5985-a702-32c503462a37",
                                           "subaccountNumber": 129,
                                           "side":"BUY",
                                           "size":"0.01",
                                           "type":"LIMIT",
                                           "price":"1878",
                                           "orderId":"1118c548-1715-5a72-9c41-f4388518c6e2",
                                           "createdAt":"2023-07-07T17:20:10.369Z",
                                           "liquidity":"TAKER",
                                           "marketId":"ETH-USD",
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
        )
    }
}
