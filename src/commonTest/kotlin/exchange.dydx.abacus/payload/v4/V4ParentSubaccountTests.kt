package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import kotlin.test.BeforeTest
import kotlin.test.Test

class V4ParentSubaccountTests : V4BaseTests(true) {
    internal override fun loadMarkets(): StateResponse {
        return test({
            perp.socket(testWsUrl, mock.marketsChannel.subscribed_2, 0, null)
        }, null)
    }

    @BeforeTest
    private fun prepareTest() {
        reset()
        loadMarketsConfigurations()
        loadMarkets()
        perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
        loadSubaccountsWithRealData()
    }

    private fun loadSubaccountsWithRealData(): StateResponse {
        return test({
            perp.rest(
                AbUrl.fromString("$testRestUrl/v4/addresses/dydxaddress"),
                mock.parentSubaccountsChannel.rest_response,
                0,
                null,
            )
        }, null)
    }

    @Test
    fun testParentSubaccountSubscribed() {
        reset()
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
                                            },
                                            "marginValue": {
                                                "current": 285.689378002
                                            }
                                        }
                                    },
                                    "pendingPositions": []
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    @Test
    fun testParentSubaccountChannelData() {
        testParentSubaccountSubscribed()
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
                                    "pendingPositions": []
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    @Test
    fun testParentSubaccountSubscribedWithPendingPositions() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.real_subscribed_with_pending,
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
                                        "current": 2021.4
                                    },
                                    "freeCollateral": {
                                        "current": 1712.0
                                    },
                                    "quoteBalance": {
                                        "current": 1712.0
                                    },
                                    "openPositions": {
                                    },
                                    "pendingPositions": [
                                        {
                                            "assetId": "ARB",
                                            "firstOrderId": "d1deed71-d743-5528-aff2-cf3daf8b6413",
                                            "quoteBalance": {
                                                "current": 20
                                            },
                                            "freeCollateral": {
                                                "current": 20
                                            },
                                            "equity": {
                                                "current": 20
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

    @Test
    fun testParentSubaccountSubscribedWithMultiplePendingPositions() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.real_subscribe_with_multiple_pending,
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
                                        "current": 1108.7
                                    },
                                    "freeCollateral": {
                                        "current": 1009.0
                                    },
                                    "quoteBalance": {
                                        "current": 1005.0
                                    },
                                    "openPositions": {
                                    },
                                    "pendingPositions": [
                                        {
                                            "assetId": "ETH",
                                            "firstOrderId": "5f7ad499-1d48-5ab1-acfd-d4664e07e7e3",
                                            "quoteBalance": {
                                                "current": 20
                                            },
                                            "freeCollateral": {
                                                "current": 20
                                            },
                                            "equity": {
                                                "current": 20
                                            }
                                        },
                                        {
                                            "assetId": "XLM",
                                            "firstOrderId": "89d1fe83-5b0d-5c3e-aaf5-42d1b2537837",
                                            "orderCount": 3,
                                            "quoteBalance": {
                                                "current": 60
                                            },
                                            "freeCollateral": {
                                                "current": 60
                                            },
                                            "equity": {
                                                "current": 60
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

    @Test
    fun testParentSubaccountSubscribedWithUnpopulatedChild() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.parentSubaccountsChannel.real_subscribed_with_unpopulated_child,
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
                                        "current": 1979.9
                                    },
                                    "freeCollateral": {
                                        "current": 1712.0
                                    },
                                    "quoteBalance": {
                                        "current": 1712.0
                                    },
                                    "openPositions": {
                                    },
                                    "pendingPositions": [
                                        {
                                            "assetId": "ARB",
                                            "firstOrderId": "d1deed71-d743-5528-aff2-cf3daf8b6413",
                                            "quoteBalance": {
                                                "current": 270
                                            },
                                            "freeCollateral": {
                                                "current": 270
                                            },
                                            "equity": {
                                                "current": 270
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
