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
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }
}
