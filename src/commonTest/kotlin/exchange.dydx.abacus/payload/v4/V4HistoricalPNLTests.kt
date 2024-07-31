package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.tests.extensions.loadHistoricalPnlsFirst
import exchange.dydx.abacus.tests.extensions.loadHistoricalPnlsSecond
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class V4HistoricalPNLTests : V4BaseTests() {
    @Test
    fun testHistoricalPNLs() {
        setup()

        print("--------First round----------\n")

        testHistoricalPNLsOnce()
    }

    private fun testHistoricalPNLsOnce() {
        ServerTime.overWrite = parser.asDatetime("2022-08-08T21:07:24.581Z")
        var time = ServerTime.now()
        testHistoricalPNLsFirstRound()
        time = perp.log("Historical PNL First Call", time)
    }

    private fun testHistoricalPNLsFirstRound() {
        if (perp.staticTyping) {
            perp.loadHistoricalPnlsFirst(mock)
            var pnls = perp.internalState.wallet.account.subaccounts[0]?.historicalPNLs
            assertEquals(
                pnls!![0],
                SubaccountHistoricalPNL(
                    equity = 184053.9362,
                    totalPnl = 125147.7662,
                    netTransfers = 0.0,
                    createdAtMilliseconds = parser.asDatetime("2022-08-04T18:07:23.427Z")?.toEpochMilliseconds()!!.toDouble(),
                ),
            )

            perp.loadHistoricalPnlsSecond(mock)
            pnls = perp.internalState.wallet.account.subaccounts[0]?.historicalPNLs
            assertEquals(
                pnls!![0],
                SubaccountHistoricalPNL(
                    equity = 201024.9767,
                    totalPnl = 142118.8067,
                    netTransfers = 0.0,
                    createdAtMilliseconds = parser.asDatetime("2022-07-31T13:07:23.243Z")?.toEpochMilliseconds()!!.toDouble(),
                ),
            )

            perp.setHistoricalPnlDays(30, 0)
            pnls = perp.internalState.wallet.account.subaccounts[0]?.historicalPNLs
            assertEquals(
                pnls!![0],
                SubaccountHistoricalPNL(
                    equity = 201024.9767,
                    totalPnl = 142118.8067,
                    netTransfers = 0.0,
                    createdAtMilliseconds = parser.asDatetime("2022-07-31T13:07:23.243Z")?.toEpochMilliseconds()!!.toDouble(),
                ),
            )
        } else {
            test(
                {
                    perp.loadHistoricalPnlsFirst(mock)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "historicalPnl": [
                                        {
                                            "equity": 184053.9362,
                                            "totalPnl": 125147.7662,
                                            "netTransfers": 0.0,
                                            "createdAt": "2022-08-04T18:07:23.427Z"
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
            )

            test(
                {
                    perp.loadHistoricalPnlsSecond(mock)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "historicalPnl": [
                                        {
                                            "equity": 201024.9767,
                                            "totalPnl": 142118.8067,
                                            "netTransfers": 0.0,
                                            "createdAt": "2022-07-31T13:07:23.243Z"
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
            )

            test(
                {
                    perp.setHistoricalPnlDays(30, 0)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "historicalPnl": [
                                        {
                                            "equity": 201024.9767,
                                            "totalPnl": 142118.8067,
                                            "netTransfers": 0.0,
                                            "createdAt": "2022-07-31T13:07:23.243Z"
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
