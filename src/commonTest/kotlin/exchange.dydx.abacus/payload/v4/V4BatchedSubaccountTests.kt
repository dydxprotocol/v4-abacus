package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.NotificationsProvider
import exchange.dydx.abacus.state.model.historicalTradingRewards
import exchange.dydx.abacus.state.model.onChainAccountBalances
import exchange.dydx.abacus.state.model.onChainDelegations
import exchange.dydx.abacus.state.model.updateHeight
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountSubscribed
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountWithOrdersAndFillsChanged
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ServerTime
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class V4BatchedSubaccountTests : V4BaseTests() {
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
        testSubaccountSubscribed()
        testSubaccountChanged1()
        testSubaccountChanged2()
        testSubaccountChanged3()
        testSubaccountChanged4()
    }

    private fun testSubaccountsReceived() {
        test(
            {
                perp.rest(
                    AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
                    mock.batchedSubaccountsChannel.rest_response,
                    0,
                    null,
                )
            },
            """
            {
                "wallet": {
                    "account": {
                        "tradingRewards": {
                            "total": 36059.41
                        },
                        "subaccounts": {
                            "0": {
                                "equity": {
                                    "current": 1296914.796
                                },
                                "freeCollateral": {
                                    "current": 1270982.602
                                },
                                "quoteBalance": {
                                    "current": 1625586.094
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }

    private fun testSubaccountSubscribed() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.batchedSubaccountsChannel.subscribed,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "total": 36059.41
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 1296914.796
                                    },
                                    "freeCollateral": {
                                        "current": 1270982.602
                                    },
                                    "quoteBalance": {
                                        "current": 1625586.094
                                    },
                                    "orders": {
                                    },
                                    "openPositions": {
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun testSubaccountChanged1() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.batchedSubaccountsChannel.channel_batch_data_1,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "total": 36059.41
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 1296914.796
                                    },
                                    "freeCollateral": {
                                        "current": 1270982.602
                                    },
                                    "quoteBalance": {
                                        "current": 1625586.094
                                    },
                                    "orders": {
                                    },
                                    "openPositions": {
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }
    private fun testSubaccountChanged2() {
        test(
            {

                perp.socket(
                    testWsUrl,
                    mock.batchedSubaccountsChannel.channel_batch_data_2,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "total": 36059.41
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 1296914.796
                                    },
                                    "freeCollateral": {
                                        "current": 1270982.602
                                    },
                                    "quoteBalance": {
                                        "current": 1625586.094
                                    },
                                    "orders": {
                                    },
                                    "openPositions": {
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }
    private fun testSubaccountChanged3() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.batchedSubaccountsChannel.channel_batch_data_3,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "total": 36059.41
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 1296914.796
                                    },
                                    "freeCollateral": {
                                        "current": 1270982.602
                                    },
                                    "quoteBalance": {
                                        "current": 1625586.094
                                    },
                                    "orders": {
                                    },
                                    "openPositions": {
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent(),
        )
    }
    private fun testSubaccountChanged4() {
        test(
            {
                perp.socket(
                    testWsUrl,
                    mock.batchedSubaccountsChannel.channel_batch_data_4,
                    0,
                    null,
                )
            },
            """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "total": 36059.41
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 1296914.796
                                    },
                                    "freeCollateral": {
                                        "current": 1270982.602
                                    },
                                    "quoteBalance": {
                                        "current": 1625586.094
                                    },
                                    "orders": {
                                    },
                                    "openPositions": {
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
