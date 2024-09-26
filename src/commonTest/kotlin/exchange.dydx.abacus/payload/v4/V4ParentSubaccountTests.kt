package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import indexer.codegen.IndexerPerpetualPositionStatus
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class V4ParentSubaccountTests : V4BaseTests(true) {
    override fun loadMarkets(): StateResponse {
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

        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.parentSubaccountsChannel.real_subscribed, 0, null)

            val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
            val calculated = subaccount.calculated[CalculationPeriod.current]
            assertEquals(2001.402434402, calculated?.equity)
            assertEquals(1711.959192, calculated?.freeCollateral)
            assertEquals(1711.959192, calculated?.quoteBalance)

            val position = subaccount.openPositions?.get("LDO-USD")!!
            assertEquals(IndexerPerpetualPositionStatus.OPEN, position.status)
            assertEquals(11.0, position.size)
            assertEquals("LDO-USD", position.market)
            assertEquals(128, position.childSubaccountNumber)

            val positionCalculated = position.calculated[CalculationPeriod.current]!!
            assertEquals(21.552185402, positionCalculated.valueTotal)
            assertEquals(21.552185402, positionCalculated.notionalTotal)
            assertEquals(0.07446083461180532, positionCalculated.leverage)
            assertEquals(1425.664026608, positionCalculated.buyingPower)
            assertEquals(285.689378002, positionCalculated.marginValue)
        } else {
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
    }

    @Test
    fun testParentSubaccountChannelData() {
        testParentSubaccountSubscribed()

        if (perp.staticTyping) {
            perp.socket(
                url = testWsUrl,
                jsonString = mock.parentSubaccountsChannel.real_channel_batch_data,
                subaccountNumber = 0,
                height = null,
            )

            val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
            val calculated = subaccount.calculated[CalculationPeriod.current]
            assertEquals(2113.158171894, calculated?.equity)
            assertEquals(1711.959192, calculated?.freeCollateral)
            assertEquals(1711.959192, calculated?.quoteBalance)

            val position = subaccount.openPositions?.get("LDO-USD")!!
            assertEquals(IndexerPerpetualPositionStatus.OPEN, position.status)
            assertEquals(17.0, position.size)
            assertEquals("LDO-USD", position.market)
            assertEquals(128, position.childSubaccountNumber)

            val positionCalculated = position.calculated[CalculationPeriod.current]!!
            assertEquals(33.307922894, positionCalculated.valueTotal)
            assertEquals(33.307922894, positionCalculated.notionalTotal)
            assertEquals(0.0830209560921621, positionCalculated.leverage)
            assertEquals(1972.686976576, positionCalculated.buyingPower)
        } else {
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
    }

    @Test
    fun testParentSubaccountSubscribedWithPendingPositions() {
        if (perp.staticTyping) {
            perp.socket(
                url = testWsUrl,
                jsonString = mock.parentSubaccountsChannel.real_subscribed_with_pending,
                subaccountNumber = 0,
                height = null,
            )

            val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
            val calculated = subaccount.calculated[CalculationPeriod.current]
            assertEquals(2021.402434402, calculated?.equity)
            assertEquals(1711.959192, calculated?.freeCollateral)
            assertEquals(1711.959192, calculated?.quoteBalance)

            val pendingPosition = subaccount.pendingPositions?.get(0)!!
            assertEquals("ARB", pendingPosition.assetId)
            assertEquals("d1deed71-d743-5528-aff2-cf3daf8b6413", pendingPosition.firstOrderId)
            val positionCalculated = pendingPosition.calculated[CalculationPeriod.current]!!
            assertEquals(20.0, positionCalculated.quoteBalance)
            assertEquals(20.0, positionCalculated.freeCollateral)
            assertEquals(20.0, positionCalculated.equity)
        } else {
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
    }

    @Test
    fun testParentSubaccountSubscribedWithMultiplePendingPositions() {
        if (perp.staticTyping) {
            perp.socket(
                url = testWsUrl,
                jsonString = mock.parentSubaccountsChannel.real_subscribe_with_multiple_pending,
                subaccountNumber = 0,
                height = null,
            )

            val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
            val calculated = subaccount.calculated[CalculationPeriod.current]
            assertEquals(1108.672653491, calculated?.equity)
            assertEquals(1009.1322948555, calculated?.freeCollateral)
            assertEquals(1004.771214, calculated?.quoteBalance)

            val pendingPosition = subaccount.pendingPositions?.get(0)!!
            assertEquals("ETH", pendingPosition.assetId)
            assertEquals("5f7ad499-1d48-5ab1-acfd-d4664e07e7e3", pendingPosition.firstOrderId)
            val positionCalculated = pendingPosition.calculated[CalculationPeriod.current]!!
            assertEquals(20.0, positionCalculated.quoteBalance)
            assertEquals(20.0, positionCalculated.freeCollateral)
            assertEquals(20.0, positionCalculated.equity)

            val pendingPosition1 = subaccount.pendingPositions?.get(1)!!
            assertEquals("XLM", pendingPosition1.assetId)
            assertEquals("89d1fe83-5b0d-5c3e-aaf5-42d1b2537837", pendingPosition1.firstOrderId)
            val positionCalculated1 = pendingPosition1.calculated[CalculationPeriod.current]!!
            assertEquals(60.0, positionCalculated1.quoteBalance)
            assertEquals(60.0, positionCalculated1.freeCollateral)
            assertEquals(60.0, positionCalculated1.equity)
        } else {
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
    }

    @Test
    fun testParentSubaccountSubscribedWithUnpopulatedChild() {
        if (perp.staticTyping) {
            perp.socket(
                url = testWsUrl,
                jsonString = mock.parentSubaccountsChannel.real_subscribed_with_unpopulated_child,
                subaccountNumber = 0,
                height = null,
            )

            val subaccount = perp.internalState.wallet.account.groupedSubaccounts[0]!!
            val calculated = subaccount.calculated[CalculationPeriod.current]
            assertEquals(1979.850249, calculated?.equity)
            assertEquals(1711.959192, calculated?.freeCollateral)
            assertEquals(1711.959192, calculated?.quoteBalance)

            val pendingPosition = subaccount.pendingPositions?.get(0)!!
            assertEquals("ARB", pendingPosition.assetId)
            assertEquals("d1deed71-d743-5528-aff2-cf3daf8b6413", pendingPosition.firstOrderId)
            val positionCalculated = pendingPosition.calculated[CalculationPeriod.current]!!
            assertEquals(267.891057, positionCalculated.quoteBalance)
            assertEquals(267.891057, positionCalculated.freeCollateral)
            assertEquals(267.891057, positionCalculated.equity)
        } else {
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
                                                "current": 268
                                            },
                                            "freeCollateral": {
                                                "current": 268
                                            },
                                            "equity": {
                                                "current": 268
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
}
