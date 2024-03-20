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
//        testSubaccountSubscribed()
        time = perp.log("Accounts Subscribed", time)

    }

//    private fun testSubaccountSubscribed() {
//        test(
//            {
//                perp.socket(testWsUrl, mock.parentSubaccountsChannel.subscribed, 0, null)
//            },
//            """
//                {
//                    "wallet": {
//                        "account": {
//                            "tradingRewards": {
//                                "total": 2800.8,
//                                "blockRewards": [
//                                    {
//                                        "tradingReward": "0.02",
//                                        "createdAtHeight": "2422"
//                                    }
//                                ]
//                            },
//                            "subaccounts": {
//                                "0": {
//                                    "equity": {
//                                        "current": 122034.20090508368
//                                    },
//                                    "freeCollateral": {
//                                        "current": 100728.9107212275
//                                    },
//                                    "quoteBalance": {
//                                        "current": 68257.215192
//                                    },
//                                    "orders": {
//                                        "b812bea8-29d3-5841-9549-caa072f6f8a8": {
//                                            "id": "b812bea8-29d3-5841-9549-caa072f6f8a8",
//                                            "side": "SELL",
//                                            "type": "LIMIT",
//                                            "status": "BEST_EFFORT_OPENED",
//                                            "timeInForce": "GTT",
//                                            "price": 1255.927,
//                                            "size": 1.653451,
//                                            "postOnly": false,
//                                            "reduceOnly": false,
//                                            "remainingSize": 0.970818,
//                                            "totalFilled": 0.682633,
//                                            "resources": {
//                                                "statusStringKey": "APP.TRADE.PENDING"
//                                            }
//                                        },
//                                        "b812bea8-29d3-5841-9549-caa072f6f8a9": {
//                                            "status": "BEST_EFFORT_CANCELED",
//                                            "resources": {
//                                                "statusStringKey": "APP.TRADE.CANCELING"
//                                            }
//                                        }
//                                    },
//                                    "openPositions": {
//                                        "BTC-USD": {
//                                            "id": "BTC-USD",
//                                            "status": "OPEN",
//                                            "maxSize": 9.974575029,
//                                            "exitPrice": 17106.497989,
//                                            "netFunding": 0.0,
//                                            "realizedPnl": {
//                                                "current": 126.640212
//                                            },
//                                            "unrealizedPnl": {
//                                                "current": 69361.31
//                                            },
//                                            "createdAt": "2022-12-11T17:27:36.351Z",
//                                            "entryPrice": {
//                                                "current": 17101.489388
//                                            },
//                                            "size": {
//                                                "current": 9.974575029
//                                            },
//                                            "assetId": "BTC",
//                                            "resources": {
//                                            },
//                                            "realizedPnlPercent": {
//                                                "current": 7.424091096228274E-4
//                                            },
//                                            "unrealizedPnlPercent": {
//                                                "current": 0.4066
//                                            },
//                                            "valueTotal": {
//                                                "current": 2.399413946951037E+5
//                                            },
//                                            "notionalTotal": {
//                                                "current": 2.399413946951037E+5
//                                            },
//                                            "adjustedImf": {
//                                                "current": 5.0E-2
//                                            },
//                                            "initialRiskTotal": {
//                                                "current": 1.1997069734755185E+4
//                                            },
//                                            "leverage": {
//                                                "current": 1.9661815533313192
//                                            }
//                                        },
//                                        "ETH-USD": {
//                                            "id": "ETH-USD",
//                                            "status": "OPEN",
//                                            "maxSize": 106.180627,
//                                            "netFunding": 0.0,
//                                            "realizedPnl": {
//                                                "current": -102.716895
//                                            },
//                                            "unrealizedPnl": {
//                                                "current": -51730.73627724242
//                                            },
//                                            "createdAt": "2022-12-11T17:29:39.792Z",
//                                            "entryPrice": {
//                                                "current": 1266.094016
//                                            },
//                                            "size": {
//                                                "current": -106.17985
//                                            },
//                                            "assetId": "ETH",
//                                            "resources": {
//                                            },
//                                            "realizedPnlPercent": {
//                                                "current": -7.640711804814775E-4
//                                            },
//                                            "unrealizedPnlPercent": {
//                                                "current": -0.3848
//                                            },
//                                            "valueTotal": {
//                                                "current": -186164.40898202002
//                                            },
//                                            "notionalTotal": {
//                                                "current": 186164.40898202002
//                                            },
//                                            "adjustedImf": {
//                                                "current": 5.0E-2
//                                            },
//                                            "initialRiskTotal": {
//                                                "current": 9308.220449101002
//                                            },
//                                            "leverage": {
//                                                "current": -1.5255101242217812
//                                            },
//                                            "liquidationPrice": {
//                                            },
//                                            "buyingPower": {
//                                                "current": 2014578.2144245498
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            """.trimIndent(),
//        )
//    }
}
