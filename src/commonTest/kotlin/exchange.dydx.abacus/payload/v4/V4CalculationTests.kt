package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import kotlin.test.Test

class V4CalculationTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/weC--\n")

        setup()
        testAccountsOnce()
        testNextFundingRate()
    }

    override fun loadMarkets(): StateResponse {
        return test(
            {
                perp.socket(
                    mock.socketUrl,
                    mock.marketsChannel.v4_subscribed_for_calculation,
                    0,
                    null,
                )
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "indexPrice": null
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    override fun loadSubaccounts(): StateResponse {
        return perp.rest(
            AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
            mock.accountsChannel.v4_accounts_received_for_calculation,
            0,
            null,
        )
    }

    fun testAccountsOnce() {
        test(
            {
                perp.rest(
                    AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
                    mock.accountsChannel.v4_accounts_received_for_calculation,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "total": 2800.8,
                                "blockRewards": [
                                    {
                                        "tradingReward": "0.02",
                                        "createdAtHeight": "2422"
                                    },
                                    {
                                        "tradingReward": "0.01",
                                        "createdAtHeight": "2500"
                                    }
                                ]
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 100.0
                                    },
                                    "freeCollateral": {
                                        "current": 50.0
                                    },
                                    "quoteBalance": {
                                        "current": -900.0
                                    },
                                    "notionalTotal": {
                                        "current": 1000.0
                                    },
                                    "valueTotal": {
                                        "current": 1000.0
                                    },
                                    "initialRiskTotal": {
                                        "current": 50.0
                                    },
                                    "leverage": {
                                        "current": 10.0
                                    },
                                    "marginUsage": {
                                        "current": 0.5
                                    },
                                    "buyingPower": {
                                        "current": 1000.0
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "notionalTotal": {
                                                "current": 1000.0
                                            },
                                            "valueTotal": {
                                                "current": 1000.0
                                            },
                                            "adjustedImf": {
                                                "current": 0.05
                                            },
                                            "initialRiskTotal": {
                                                "current": 50.0
                                            },
                                            "leverage": {
                                                "current": 10.0
                                            },
                                            "buyingPower": {
                                                "current": 1000.0
                                            },
                                            "liquidationPrice": {
                                                "current": 927.8350515463918
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
                perp.socket(
                    mock.socketUrl,
                    mock.accountsChannel.v4_subscribed_for_calculation,
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
                                        "current": 100.0
                                    },
                                    "freeCollateral": {
                                        "current": 50.0
                                    },
                                    "quoteBalance": {
                                        "current": 1100.0
                                    },
                                    "notionalTotal": {
                                        "current": 1000.0
                                    },
                                    "valueTotal": {
                                        "current": -1000.0
                                    },
                                    "initialRiskTotal": {
                                        "current": 50.0
                                    },
                                    "leverage": {
                                        "current": 10.0
                                    },
                                    "marginUsage": {
                                        "current": 0.5
                                    },
                                    "buyingPower": {
                                        "current": 1000.0
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "notionalTotal": {
                                                "current": 1000.0
                                            },
                                            "valueTotal": {
                                                "current": -1000.0
                                            },
                                            "adjustedImf": {
                                                "current": 0.05
                                            },
                                            "initialRiskTotal": {
                                                "current": 50.0
                                            },
                                            "leverage": {
                                                "current": -10.0
                                            },
                                            "buyingPower": {
                                                "current": 1000.0
                                            },
                                            "liquidationPrice": {
                                                "current": 1067.9611650485438
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

    private fun testNextFundingRate(): StateResponse {
        return test(
            {
                perp.socket(
                    mock.socketUrl,
                    mock.marketsChannel.v4_next_funding_rate_update,
                    0,
                    null,
                )
            },
            """
                {
                    "markets": {
                        "markets": {
                            "BTC-USD":{
                                "perpetual": {
                                    "nextFundingRate": "-0.0085756875"
                                }
                            },
                            "ETH-USD":{
                                "perpetual": {
                                    "nextFundingRate": "-0.0084455625"
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }
}
