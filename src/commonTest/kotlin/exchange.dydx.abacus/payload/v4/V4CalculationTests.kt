package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import kotlin.test.Test
import kotlin.test.assertEquals

class V4CalculationTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/weC--\n")

        setup()
        testAccountsOnce()
        testNextFundingRate()
    }

    override fun loadMarkets(): StateResponse {
        if (perp.staticTyping) {
            return perp.socket(
                url = mock.socketUrl,
                jsonString = mock.marketsChannel.v4_subscribed_for_calculation,
                subaccountNumber = 0,
                height = null,
            )
        } else {
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
        if (perp.staticTyping) {
            perp.rest(
                url = AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
                payload = mock.accountsChannel.v4_accounts_received_for_calculation,
                subaccountNumber = 0,
                height = null,
            )

            val account = perp.internalState.wallet.account

            val tradingRewards = account.tradingRewards
            assertEquals(2800.8, tradingRewards.total)
            val blockRewards = tradingRewards.blockRewards
            assertEquals("0.02", blockRewards[0].tradingReward)
            assertEquals("2422", blockRewards[0].createdAtHeight)
            assertEquals("0.01", blockRewards[1].tradingReward)
            assertEquals("2500", blockRewards[1].createdAtHeight)

            val subaccount = account.subaccounts[0]
            val calculated = subaccount?.calculated?.get(CalculationPeriod.current)
            assertEquals(100.0, calculated?.equity)
            assertEquals(50.0, calculated?.freeCollateral)
            assertEquals(-900.0, calculated?.quoteBalance)
            assertEquals(1000.0, calculated?.notionalTotal)
            assertEquals(1000.0, calculated?.valueTotal)
            assertEquals(50.0, calculated?.initialRiskTotal)
            assertEquals(10.0, calculated?.leverage)
            assertEquals(0.5, calculated?.marginUsage)
            assertEquals(1000.0, calculated?.buyingPower)

            val ethPosition = subaccount?.openPositions?.get("ETH-USD")
            val positionCalculated = ethPosition?.calculated?.get(CalculationPeriod.current)
            assertEquals(1000.0, positionCalculated?.notionalTotal)
            assertEquals(1000.0, positionCalculated?.valueTotal)
            assertEquals(0.05, positionCalculated?.adjustedImf)
            assertEquals(50.0, positionCalculated?.initialRiskTotal)
            assertEquals(10.0, positionCalculated?.leverage)
            assertEquals(1000.0, positionCalculated?.buyingPower)
            assertEquals(927.8350515463918, positionCalculated?.liquidationPrice)
        } else {
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
        }

        if (perp.staticTyping) {
            perp.socket(
                url = mock.socketUrl,
                jsonString = mock.accountsChannel.v4_subscribed_for_calculation,
                subaccountNumber = 0,
                height = null,
            )

            val subaccount = perp.internalState.wallet.account.subaccounts[0]
            val calculated = subaccount?.calculated?.get(CalculationPeriod.current)!!
            assertEquals(100.0, calculated.equity)
            assertEquals(50.0, calculated.freeCollateral)
            assertEquals(1100.0, calculated.quoteBalance)
            assertEquals(1000.0, calculated.notionalTotal)
            assertEquals(-1000.0, calculated.valueTotal)
            assertEquals(50.0, calculated.initialRiskTotal)
            assertEquals(10.0, calculated.leverage)
            assertEquals(0.5, calculated.marginUsage)
            assertEquals(1000.0, calculated.buyingPower)

            val ethPosition = subaccount.openPositions?.get("ETH-USD")!!
            val positionCalculated = ethPosition.calculated[CalculationPeriod.current]!!
            assertEquals(1000.0, positionCalculated.notionalTotal)
            assertEquals(-1000.0, positionCalculated.valueTotal)
            assertEquals(0.05, positionCalculated.adjustedImf)
            assertEquals(50.0, positionCalculated.initialRiskTotal)
            assertEquals(-10.0, positionCalculated.leverage)
            assertEquals(1000.0, positionCalculated.buyingPower)
            assertEquals(1067.9611650485438, positionCalculated.liquidationPrice)
        } else {
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
    }

    private fun testNextFundingRate(): StateResponse {
        if (perp.staticTyping) {
            val response = perp.socket(
                url = mock.socketUrl,
                jsonString = mock.marketsChannel.v4_next_funding_rate_update,
                subaccountNumber = 0,
                height = null,
            )

            val markets = perp.internalState.marketsSummary.markets
            val btcMarket = markets["BTC-USD"]!!
            assertEquals(-0.0085756875, btcMarket.perpetualMarket?.perpetual?.nextFundingRate)
            val ethMarket = markets["ETH-USD"]!!
            assertEquals(-0.0084455625, ethMarket.perpetualMarket?.perpetual?.nextFundingRate)

            return response
        } else {
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
}
