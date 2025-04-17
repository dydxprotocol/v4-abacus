package exchange.dydx.abacus.payload.v4

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.EquityTier
import exchange.dydx.abacus.output.account.FillLiquidity
import exchange.dydx.abacus.output.account.TransferRecordType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderTimeInForce
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.internalstate.InternalAccountBalanceState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.notification.NotificationsProvider
import exchange.dydx.abacus.state.model.historicalTradingRewards
import exchange.dydx.abacus.state.model.onChainAccountBalances
import exchange.dydx.abacus.state.model.onChainDelegations
import exchange.dydx.abacus.state.model.updateHeight
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountSubscribed
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountWithOrdersAndFillsChanged
import exchange.dydx.abacus.tests.extensions.loadv4SubaccountsWithPositions
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.tests.extensions.parseOnChainEquityTiers
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.tests.extensions.socket
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ServerTime
import indexer.codegen.IndexerPerpetualPositionStatus
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

        testSubaccountFillsReceived()
        time = perp.log("Fills Received", time)

        testSubaccountSubscribed()
        time = perp.log("Accounts Subscribed", time)

        testSubaccountTransfersReceived()
        time = perp.log("Transfers Received", time)

        testSubaccountFillsChannelData()

        testSubaccountChanged()
        time = perp.log("Accounts Changed", time)

        testBatchedSubaccountChanged()

        testPartiallyFilledAndCanceledOrders()

        testEquityTiers()

        testFeeTiers()

        testUserFeeTier()

        testUserStats()

        testAccountHistoricalTradingRewards()
    }

    private fun testSubaccountsReceived() {
        if (perp.staticTyping) {
            perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")

            val account = perp.internalState.wallet.account
            assertEquals(2800.8, account.tradingRewards.total)

            val subaccount = account.subaccounts[0]
            val calculated = subaccount?.calculated?.get(CalculationPeriod.current)!!
            assertEquals(108116.7318528828, calculated.equity)
            assertEquals(106640.3767269893, calculated.freeCollateral)
            assertEquals(99872.368956, calculated.quoteBalance)
        } else {
            test(
                {
                    perp.loadv4SubaccountsWithPositions(mock, "$testRestUrl/v4/addresses/cosmo")
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
                """.trimIndent(),
            )
        }
    }

    private fun testSubaccountSubscribed() {
        if (perp.staticTyping) {
            perp.loadv4SubaccountSubscribed(mock, testWsUrl)

            val account = perp.internalState.wallet.account
            assertEquals(2800.8, account.tradingRewards.total)

            val subaccount = account.subaccounts[0]
            val calculated = subaccount?.calculated?.get(CalculationPeriod.current)!!
            assertEquals(122034.20090508368, calculated.equity)
            assertEquals(100728.9107212275, calculated.freeCollateral)
            assertEquals(68257.215192, calculated.quoteBalance)

            val orders = subaccount.orders
            assertEquals(2, orders?.size)

            val openPositions = subaccount.openPositions
            assertEquals(2, openPositions?.size)
        } else {
            test(
                {
                    perp.loadv4SubaccountSubscribed(mock, testWsUrl)
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
                                    }
                                ]
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 122034.20090508368
                                    },
                                    "freeCollateral": {
                                        "current": 100728.9107212275
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
                                                "current": 69361.31
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
                                                "current": 7.424091096228274E-4
                                            },
                                            "unrealizedPnlPercent": {
                                                "current": 0.4066
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
                                                "current": 1.9661815533313192
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
                                                "current": -51730.73627724242
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
                                                "current": -7.640711804814775E-4
                                            },
                                            "unrealizedPnlPercent": {
                                                "current": -0.3848
                                            },
                                            "valueTotal": {
                                                "current": -186164.40898202002
                                            },
                                            "notionalTotal": {
                                                "current": 186164.40898202002
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 9308.220449101002
                                            },
                                            "leverage": {
                                                "current": -1.5255101242217812
                                            },
                                            "liquidationPrice": {
                                            },
                                            "buyingPower": {
                                                "current": 2014578.2144245498
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

    private fun testSubaccountFillsReceived() {
        if (perp.staticTyping) {
            perp.rest(
                url = AbUrl.fromString("$testRestUrl/v4/fills?subaccountNumber=0"),
                payload = mock.fillsChannel.v4_rest,
                subaccountNumber = 0,
                height = null,
            )

            val fills = perp.internalState.wallet.account.subaccounts[0]?.fills
            assertEquals(100, fills?.size)
        } else {
            test(
                load = {
                    perp.rest(
                        AbUrl.fromString("$testRestUrl/v4/fills?subaccountNumber=0"),
                        mock.fillsChannel.v4_rest,
                        0,
                        null,
                    )
                },
                expected = """
                            {
                                "wallet": {
                                    "account": {
                                        "subaccounts": {
                                            "0": {
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
                moreVerification = {
                    val fills =
                        parser.asList(
                            parser.value(
                                perp.data,
                                "wallet.account.subaccounts.0.fills",
                            ),
                        )
                    assertEquals(
                        100,
                        fills?.size,
                    )
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.fillsChannel.v4_subscribed, 0, null)

            val fills = perp.internalState.wallet.account.subaccounts[0]?.fills
            assertEquals(100, fills?.size)
            val fill = fills?.first()!!
            assertEquals("dad7abeb-4c04-58d3-8dda-fd0bc0528deb", fill.id)
            assertEquals(OrderSide.Buy, fill.side)
            assertEquals(FillLiquidity.taker, fill.liquidity)
            assertEquals(OrderType.Limit, fill.type)
            assertEquals("BTC-USD", fill.marketId)
            assertEquals("4f2a6f7d-a897-5c4e-986f-d48f5760102a", fill.orderId)
            assertEquals(18275.31, fill.price)
            assertEquals(4.41E-6, fill.size)
            assertEquals(0.0, fill.fee)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.fillsChannel.v4_subscribed, 0, null)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 122034.20090508368
                                    },
                                    "freeCollateral": {
                                        "current": 100728.9107212275
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
                },
            )
        }
    }

    private fun testSubaccountTransfersReceived() {
        if (perp.staticTyping) {
            perp.rest(
                url = AbUrl.fromString("$testRestUrl/v4/transfers?subaccountNumber=0"),
                payload = mock.transfersMock.transfer_data,
                subaccountNumber = 0,
                height = null,
            )

            val transfers = perp.internalState.wallet.account.subaccounts[0]?.transfers
            val transfer = transfers?.first()!!
            assertEquals("89586775-0646-582e-9b36-4f131715644d", transfer.id)
            assertEquals(TransferRecordType.TRANSFER_OUT, transfer.type)
            assertEquals("USDC", transfer.asset)
            assertEquals(
                parser.asDatetime("2023-08-21T21:37:53.373Z")?.toEpochMilliseconds()?.toDouble(),
                transfer.updatedAtMilliseconds,
            )
            assertEquals(404014, transfer.updatedAtBlock)
            assertEquals(419.98472, transfer.amount)
            assertEquals("dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg", transfer.fromAddress)
            assertEquals("dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8", transfer.toAddress)
            assertEquals("MOCKHASH1", transfer.transactionHash)
        } else {
            test(
                {
                    perp.rest(
                        AbUrl.fromString("$testRestUrl/v4/transfers?subaccountNumber=0"),
                        mock.transfersMock.transfer_data,
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
                                    "transfers": [
                                        {
                                            "id": "89586775-0646-582e-9b36-4f131715644d",
                                            "type": "WITHDRAWAL",
                                            "asset": "USDC",
                                            "createdAt": "2023-08-21T21:37:53.373Z",
                                            "updatedAtBlock": 404014,
                                            "amount": 419.98472,
                                            "status": "CONFIRMED",
                                            "fromAddress": "dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                                            "toAddress": "dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8",
                                            "resources": {
                                                "typeStringKey": "APP.GENERAL.TRANSFER_OUT",
                                                "iconLocal": "Outgoing",
                                                "indicator": "confirmed"
                                            },
                                            "transactionHash": "MOCKHASH1"
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.transfersMock.channel_data, 0, null)

            val transfers = perp.internalState.wallet.account.subaccounts[0]?.transfers
            val transfer = transfers?.first()!!
            assertEquals("A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B", transfer.id)
            assertEquals("A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B", transfer.transactionHash)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.transfersMock.channel_data, 0, null)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "transfers": [
                                        {
                                            "id": "A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B",
                                            "transactionHash": "A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B"
                                        },
                                        {},
                                        {},
                                        {},
                                        {},
                                        {
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

    private fun testSubaccountFillsChannelData() {
        if (perp.staticTyping) {
            perp.socket(testWsUrl, mock.fillsChannel.v4_channel_data, 0, null)

            val account = perp.internalState.wallet.account
            val blockReward = account.tradingRewards.blockRewards[0]
            assertEquals("0.02", blockReward.tradingReward)
            assertEquals("2422", blockReward.createdAtHeight)
            val blockReward2 = account.tradingRewards.blockRewards[1]
            assertEquals("0.01", blockReward2.tradingReward)
            assertEquals("2501", blockReward2.createdAtHeight)

            val subaccount = account.subaccounts[0]
            val calculated = subaccount?.calculated?.get(CalculationPeriod.current)!!
            assertEquals(122034.20090508368, calculated.equity)
            assertEquals(100728.9107212275, calculated.freeCollateral)
            assertEquals(68257.215192, calculated.quoteBalance)

            val fills = subaccount.fills
            assertEquals(101, fills?.size)
            val fill = fills?.firstOrNull { it.id == "0cf41e16-036e-534d-bbaf-cf318b44b840" }
            assertEquals("0cf41e16-036e-534d-bbaf-cf318b44b840", fill?.id)
            assertEquals(OrderSide.Sell, fill?.side)
            assertEquals(FillLiquidity.taker, fill?.liquidity)
            assertEquals(OrderType.Limit, fill?.type)
            assertEquals("f5d440b9-6e93-535a-a5d6-fbb74852c6d8", fill?.orderId)
            assertEquals(1570.19, fill?.price)
            assertEquals(0.003, fill?.size)

            val orders = subaccount.orders
            assertEquals(3, orders?.size)
            val order = orders?.firstOrNull { it.id == "b812bea8-29d3-5841-9549-caa072f6f8a8" }
            assertEquals("b812bea8-29d3-5841-9549-caa072f6f8a8", order?.id)
            assertEquals(OrderSide.Sell, order?.side)
            assertEquals(OrderType.Limit, order?.type)
            assertEquals(OrderTimeInForce.GTT, order?.timeInForce)
            assertEquals(1255.927, order?.price)
            assertEquals(1.653451, order?.size)
            assertEquals(false, order?.postOnly)
            assertEquals(false, order?.reduceOnly)
            assertEquals(0.970818, order?.remainingSize)
            assertEquals(0.682633, order?.totalFilled)
        } else {
            test(
                {
                    perp.socket(testWsUrl, mock.fillsChannel.v4_channel_data, 0, null)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "blockRewards": [
                                    {
                                        "tradingReward": "0.02",
                                        "createdAtHeight": "2422"
                                    },
                                    {
                                        "tradingReward": "0.01",
                                        "createdAtHeight": "2501"
                                    }
                                ]
                            },
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": 122034.20090508368
                                    },
                                    "freeCollateral": {
                                        "current": 100728.9107212275
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
                        parser.asList(
                            parser.value(
                                perp.data,
                                "wallet.account.subaccounts.0.fills",
                            ),
                        )
                    assertEquals(
                        101,
                        fills?.size,
                    )
                },
            )
        }
    }

    private fun testSubaccountChanged() {
        if (perp.staticTyping) {
            perp.loadv4SubaccountWithOrdersAndFillsChanged(mock, testWsUrl)

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!

            val calculated = subaccount.calculated[CalculationPeriod.current]!!
            assertEquals(-161020.048352526628, calculated.equity)
            assertEquals(-172483.91152975295, calculated.freeCollateral)
            assertEquals(68257.215192, calculated.quoteBalance)

            val order = subaccount.orders?.firstOrNull { it.id == "b812bea8-29d3-5841-9549-caa072f6f8a8" }
            assertEquals("b812bea8-29d3-5841-9549-caa072f6f8a8", order?.id)
            assertEquals(OrderSide.Sell, order?.side)
            assertEquals(OrderType.Limit, order?.type)
            assertEquals(OrderTimeInForce.GTT, order?.timeInForce)
            assertEquals(1255.927, order?.price)
            assertEquals(1.653451, order?.size)
            assertEquals(false, order?.postOnly)
            assertEquals(false, order?.reduceOnly)

            val transfers = subaccount.transfers
            assertTrue {
                transfers?.any { it.id == "A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B" } == true
            }
            val transfer = transfers?.first { it.id == "89586775-0646-582e-9b36-4f131715644d" }!!
            assertEquals("89586775-0646-582e-9b36-4f131715644d", transfer.id)
            assertEquals(TransferRecordType.TRANSFER_OUT, transfer.type)
            assertEquals("USDC", transfer.asset)
            assertEquals(419.98472, transfer.amount)
            assertEquals("dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg", transfer.fromAddress)
            assertEquals("dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8", transfer.toAddress)
            assertEquals("MOCKHASH1", transfer.transactionHash)

            val fills = subaccount.fills
            assertEquals(102, fills?.size)

            val ethPosition = subaccount.openPositions?.get("ETH-USD")!!
            assertEquals("ETH-USD", ethPosition.market)
            assertEquals(IndexerPerpetualPositionStatus.OPEN, ethPosition.status)
            assertEquals(106.180627, ethPosition.maxSize)
            assertEquals(0.0, ethPosition.netFunding)
            assertEquals(-102.716895, ethPosition.realizedPnl)
            assertEquals(-51730.736277242424, ethPosition.calculated[CalculationPeriod.current]?.unrealizedPnl)
            assertEquals(parser.asDatetime("2022-12-11T17:29:39.792Z"), ethPosition.createdAt)
            assertEquals(1266.094016, ethPosition.entryPrice)
            assertEquals(-106.17985, ethPosition.size)
        } else {
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
                                        "current": -161020.04835252662
                                    },
                                    "freeCollateral": {
                                        "current": -172483.91152975295
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
                                    "transfers": [
                                        {
                                            "id": "A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B",
                                            "transactionHash": "A9758D092415E36F4E0D80D323BC4EE472644548392489309333CA55E963431B"
                                        },
                                        {
                                            "id": "89586775-0646-582e-9b36-4f131715644d",
                                            "type": "WITHDRAWAL",
                                            "asset": "USDC",
                                            "createdAt": "2023-08-21T21:37:53.373Z",
                                            "updatedAtBlock": 404014,
                                            "amount": 419.98472,
                                            "status": "CONFIRMED",
                                            "fromAddress": "dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                                            "toAddress": "dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8",
                                            "resources": {
                                                "typeStringKey": "APP.GENERAL.TRANSFER_OUT",
                                                "iconLocal": "Outgoing",
                                                "indicator": "confirmed"
                                            },
                                            "transactionHash": "MOCKHASH1"
                                        }
                                    ],
                                    "openPositions": {
                                        "BTC-USD": {
                                            "id": "BTC-USD",
                                            "status": "OPEN",
                                            "maxSize": 1.792239322,
                                            "exitPrice": 17106.497989,
                                            "netFunding": 0.0,
                                            "unrealizedPnl": {
                                                "current": 69361.30568685036
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
                                                "current": 0.7995
                                            },
                                            "valueTotal": {
                                                "current": -43112.854562506596
                                            },
                                            "notionalTotal": {
                                                "current": 43112.854562506596
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
                                                "current": -51730.74
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
                                                "current": -7.640711804814775E-4
                                            },
                                            "unrealizedPnlPercent": {
                                                "current": -0.587
                                            },
                                            "valueTotal": {
                                                "current": -186164.40898202002
                                            },
                                            "notionalTotal": {
                                                "current": 186164.40898202002
                                            },
                                            "adjustedImf": {
                                                "current": 5.0E-2
                                            },
                                            "initialRiskTotal": {
                                                "current": 9308.220449101002
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
                        parser.asList(
                            parser.value(
                                perp.data,
                                "wallet.account.subaccounts.0.fills",
                            ),
                        )
                    assertEquals(
                        102,
                        fills?.size,
                    )
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(
                url = testWsUrl,
                jsonString = mock.accountsChannel.v4_best_effort_cancelled,
                subaccountNumber = 0,
                height = BlockAndTime(16940, Clock.System.now()),
            )

            val order = perp.internalState.wallet.account.subaccounts[0]?.orders?.firstOrNull {
                it.id == "80133551-6d61-573b-9788-c1488e11027a"
            }
            assertEquals(OrderStatus.Pending, order?.status)
        } else {
            test(
                {
                    perp.socket(
                        testWsUrl,
                        mock.accountsChannel.v4_best_effort_cancelled,
                        0,
                        BlockAndTime(16940, Clock.System.now()),
                    )
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
                                            "status": "PENDING"
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
                url = testWsUrl,
                jsonString = mock.accountsChannel.v4_best_effort_cancelled,
                subaccountNumber = 0,
                height = BlockAndTime(16960, Clock.System.now()),
            )

            val order = perp.internalState.wallet.account.subaccounts[0]?.orders?.firstOrNull {
                it.id == "80133551-6d61-573b-9788-c1488e11027a"
            }
            assertEquals(OrderStatus.Canceled, order?.status)
        } else {
            test(
                {
                    perp.socket(
                        testWsUrl,
                        mock.accountsChannel.v4_best_effort_cancelled,
                        0,
                        BlockAndTime(16960, Clock.System.now()),
                    )
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

        if (perp.staticTyping) {
            perp.updateHeight(BlockAndTime(16960, Clock.System.now()))

            val order = perp.internalState.wallet.account.subaccounts[0]?.orders?.firstOrNull {
                it.id == "80133551-6d61-573b-9788-c1488e11027a"
            }
            assertEquals(OrderStatus.Canceled, order?.status)
        } else {
            test(
                {
                    perp.updateHeight(BlockAndTime(16960, Clock.System.now()))
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
    }

    private fun testBatchedSubaccountChanged() {
        if (perp.staticTyping) {
            perp.socket(
                url = testWsUrl,
                jsonString = mock.accountsChannel.v4_batched,
                subaccountNumber = 0,
                height = BlockAndTime(16960, Clock.System.now()),
            )

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            val calculated = subaccount.calculated[CalculationPeriod.current]!!
            assertEquals(-41124.184464506594, calculated.equity)

            val order = subaccount.orders?.firstOrNull { it.id == "1118c548-1715-5a72-9c41-f4388518c6e2" }
            assertEquals(OrderStatus.PartiallyFilled, order?.status)

            val fills = subaccount.fills
            assertEquals(112, fills?.size)
        } else {
            test(
                {
                    perp.socket(
                        testWsUrl,
                        mock.accountsChannel.v4_batched,
                        0,
                        BlockAndTime(16960, Clock.System.now()),
                    )
                },
                """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "equity": {
                                        "current": -41124.184464506594
                                    },
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
                        parser.asList(
                            parser.value(
                                perp.data,
                                "wallet.account.subaccounts.0.fills",
                            ),
                        )
                    assertEquals(
                        112,
                        fills?.size,
                    )
                },
            )
        }

        if (perp.staticTyping) {
            perp.socket(
                url = testWsUrl,
                jsonString = mock.accountsChannel.v4_position_closed,
                subaccountNumber = 0,
                height = BlockAndTime(16961, Clock.System.now()),
            )

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            val calculated = subaccount.calculated[CalculationPeriod.current]!!
            assertEquals(-41281.9808525066, calculated.equity)
            val btcPosition = subaccount.openPositions?.get("BTC-USD")!!
            val positionCalculated = btcPosition.calculated[CalculationPeriod.current]!!
            assertEquals(-1.792239322, btcPosition.size)

            val ioImplementations = testIOImplementations()
            val localizer = testLocalizer(ioImplementations)
            val uiImplementations = testUIImplementations(localizer)
            val notificationsProvider =
                NotificationsProvider(
                    stateMachine = perp,
                    uiImplementations = uiImplementations,
                    environment = mock.v4Environment,
                    parser = Parser(),
                    jsonEncoder = JsonEncoder(),
                )
            val notifications = notificationsProvider.buildNotifications(0)
            assertEquals(
                6,
                notifications.size,
            )
            val order = notifications["order:1118c548-1715-5a72-9c41-f4388518c6e2"]
            assertNotNull(order)
            assertEquals(
                "NOTIFICATIONS.ORDER_PARTIAL_FILL.TITLE",
                order.title,
            )
            val position = notifications["position:ETH-USD"]
            assertNotNull(position)
            assertEquals(
                "NOTIFICATIONS.POSITION_CLOSED.TITLE",
                position.title,
            )
        } else {
            test(
                load = {
                    perp.socket(
                        testWsUrl,
                        mock.accountsChannel.v4_position_closed,
                        0,
                        BlockAndTime(16961, Clock.System.now()),
                    )
                },
                expected = """
                            {
                                "wallet": {
                                    "account": {
                                        "subaccounts": {
                                            "0": {
                                                "equity": {
                                                    "current": -41281.9808525066
                                                },
                                                "openPositions": {
                                                    "BTC-USD": {
                                                        "size": {
                                                            "current": -1.792239322
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                """.trimIndent(),
                moreVerification = {
                    val ioImplementations = testIOImplementations()
                    val localizer = testLocalizer(ioImplementations)
                    val uiImplementations = testUIImplementations(localizer)
                    val notificationsProvider =
                        NotificationsProvider(
                            perp,
                            uiImplementations,
                            environment = mock.v4Environment,
                            Parser(),
                            JsonEncoder(),
                        )
                    val notifications = notificationsProvider.buildNotifications(0)
                    assertEquals(
                        6,
                        notifications.size,
                    )
                    val order = notifications["order:1118c548-1715-5a72-9c41-f4388518c6e2"]
                    assertNotNull(order)
                    assertEquals(
                        "NOTIFICATIONS.ORDER_PARTIAL_FILL.TITLE",
                        order.title,
                    )
                    val position = notifications["position:ETH-USD"]
                    assertNotNull(position)
                    assertEquals(
                        "NOTIFICATIONS.POSITION_CLOSED.TITLE",
                        position.title,
                    )
                },
            )
        }
    }

    private fun testPartiallyFilledAndCanceledOrders() {
        if (perp.staticTyping) {
            perp.socket(
                url = testWsUrl,
                jsonString = mock.accountsChannel.v4_parent_subaccounts_partially_filled_and_canceled_orders,
                subaccountNumber = 0,
                height = BlockAndTime(14689438, Clock.System.now()),
            )

            val subaccount = perp.internalState.wallet.account.subaccounts[0]!!
            val orders = subaccount.orders
            val order1 = orders?.firstOrNull { it.id == "3a8c6f8f-d8dd-54b5-a3a1-d318f586a80c" }
            assertEquals(OrderStatus.PartiallyCanceled, order1?.status)
            val order2 = orders?.firstOrNull { it.id == "a4586c75-c3f5-5bf5-877a-b3f2c8ff32a7" }
            assertEquals(OrderStatus.PartiallyFilled, order2?.status)

            val ioImplementations = testIOImplementations()
            val localizer = testLocalizer(ioImplementations)
            val uiImplementations = testUIImplementations(localizer)
            val notificationsProvider =
                NotificationsProvider(
                    stateMachine = perp,
                    uiImplementations = uiImplementations,
                    environment = mock.v4Environment,
                    parser = Parser(),
                    jsonEncoder = JsonEncoder(),
                )
            val notifications = notificationsProvider.buildNotifications(0)
            assertEquals(
                8,
                notifications.size,
            )
            val order = notifications["order:3a8c6f8f-d8dd-54b5-a3a1-d318f586a80c"]
            assertNotNull(order)
            assertEquals(
                "NOTIFICATIONS.ORDER_PARTIAL_FILL.TITLE",
                order.title,
            )
        } else {
            test(
                {
                    perp.socket(
                        testWsUrl,
                        mock.accountsChannel.v4_parent_subaccounts_partially_filled_and_canceled_orders,
                        0,
                        BlockAndTime(14689438, Clock.System.now()),
                    )
                },
                """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "orders": {
                                        "a4586c75-c3f5-5bf5-877a-b3f2c8ff32a7": {
                                            "status": "PARTIALLY_FILLED"
                                        },
                                        "3a8c6f8f-d8dd-54b5-a3a1-d318f586a80c": {
                                            "status": "PARTIALLY_CANCELED"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                    val ioImplementations = testIOImplementations()
                    val localizer = testLocalizer(ioImplementations)
                    val uiImplementations = testUIImplementations(localizer)
                    val notificationsProvider =
                        NotificationsProvider(
                            perp,
                            uiImplementations,
                            environment = mock.v4Environment,
                            Parser(),
                            JsonEncoder(),
                        )
                    val notifications = notificationsProvider.buildNotifications(0)
                    assertEquals(
                        8,
                        notifications.size,
                    )
                    val order = notifications["order:3a8c6f8f-d8dd-54b5-a3a1-d318f586a80c"]
                    assertNotNull(order)
                    assertEquals(
                        "NOTIFICATIONS.ORDER_PARTIAL_FILL.TITLE",
                        order.title,
                    )
                },
            )
        }
    }

    private fun testEquityTiers() {
        if (perp.staticTyping) {
            perp.parseOnChainEquityTiers(mock.v4OnChainMock.equity_tiers)
            assertEquals(
                perp.internalState.configs.equityTiers?.shortTermOrderEquityTiers?.size,
                6,
            )
            assertEquals(
                perp.internalState.configs.equityTiers?.statefulOrderEquityTiers?.size,
                6,
            )
            assertEquals(
                perp.internalState.configs.equityTiers?.shortTermOrderEquityTiers?.get(0),
                EquityTier(
                    requiredTotalNetCollateralUSD = 0.0,
                    nextLevelRequiredTotalNetCollateralUSD = 20.0,
                    maxOrders = 0,
                ),
            )
            assertEquals(
                perp.internalState.configs.equityTiers?.shortTermOrderEquityTiers?.get(1),
                EquityTier(
                    requiredTotalNetCollateralUSD = 20.0,
                    nextLevelRequiredTotalNetCollateralUSD = 100.0,
                    maxOrders = 1,
                ),
            )
            assertEquals(
                perp.internalState.configs.equityTiers?.statefulOrderEquityTiers?.get(0),
                EquityTier(
                    requiredTotalNetCollateralUSD = 0.0,
                    nextLevelRequiredTotalNetCollateralUSD = 20.0,
                    maxOrders = 0,
                ),
            )
            assertEquals(
                perp.internalState.configs.equityTiers?.statefulOrderEquityTiers?.get(1),
                EquityTier(
                    requiredTotalNetCollateralUSD = 20.0,
                    nextLevelRequiredTotalNetCollateralUSD = 100.0,
                    maxOrders = 1,
                ),
            )
        } else {
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
                },
            )
        }
    }

    private fun testFeeTiers() {
        if (perp.staticTyping) {
            perp.parseOnChainFeeTiers(mock.v4OnChainMock.fee_tiers)
            assertEquals(perp.internalState.configs.feeTiers?.size, 9)
            assertEquals(perp.internalState.configs.feeTiers?.get(0)?.tier, "1")
        } else {
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
                                "volume": 0,
                                "totalShare": 0.0,
                                "makerShare": 0.0
                            }
                        ]
                    }
                }
                """.trimIndent(),
                {
                },
            )
        }
    }

    private fun testUserFeeTier() {
        if (perp.staticTyping) {
            perp.parseOnChainUserFeeTier(mock.v4OnChainMock.user_fee_tier)
            assertEquals(perp.internalState.wallet.user?.feeTierId, "1")
            assertEquals(perp.internalState.wallet.user?.makerFeeRate, 0.0)
            assertEquals(perp.internalState.wallet.user?.takerFeeRate, 0.0)
        } else {
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
                },
            )
        }
    }

    private fun testUserStats() {
        if (perp.staticTyping) {
            perp.parseOnChainUserStats(mock.v4OnChainMock.user_stats)
            assertEquals(perp.internalState.wallet.user?.makerVolume30D, 1.0)
            assertEquals(perp.internalState.wallet.user?.takerVolume30D, 1.0)
        } else {
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
                },
            )
        }
    }

    @Test
    fun testAccountBalances() {
        if (perp.staticTyping) {
            val changes = perp.onChainAccountBalances(mock.v4OnChainMock.account_balances)
            perp.updateStateChanges(changes)
            assertEquals(perp.internalState.wallet.account.balances?.size, 2)
            assertEquals(
                perp.internalState.wallet.account.balances?.get("ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5"),
                InternalAccountBalanceState(
                    "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5",
                    110.0.toBigDecimal(),
                ),
            )
            assertEquals(
                perp.internalState.wallet.account.balances?.get("dv4tnt"),
                InternalAccountBalanceState(
                    "dv4tnt",
                    1220.0.toBigDecimal(),
                ),
            )
        } else {
            test(
                {
                    val changes = perp.onChainAccountBalances(mock.v4OnChainMock.account_balances)
                    perp.updateStateChanges(changes)
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
                },
            )
        }
    }

    @Test
    fun testAccountStakingBalances() {
        if (perp.staticTyping) {
            val changes = perp.onChainDelegations(mock.v4OnChainMock.account_delegations)
            assertEquals(perp.internalState.wallet.account.stakingBalances?.size, 1)
            assertEquals(
                perp.internalState.wallet.account.stakingBalances?.get("dv4tnt"),
                InternalAccountBalanceState(
                    "dv4tnt",
                    2001000.0.toBigDecimal(),
                ),
            )
            assertTrue { changes.changes.contains(Changes.accountBalances) }
        } else {
            test(
                {
                    val changes = perp.onChainDelegations(mock.v4OnChainMock.account_delegations)
                    perp.updateStateChanges(changes)
                    return@test StateResponse(perp.state, changes)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "stakingBalances": {
                                "dv4tnt": {
                                     "denom": "dv4tnt",
                                     "amount": "2001000"
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                },
            )
        }
    }

    @Test
    fun testAccountHistoricalTradingRewards() {
        reset()
        setup()

        if (perp.staticTyping) {
            var changes = perp.historicalTradingRewards(
                payload = mock.historicalTradingRewards.weeklyCall,
                period = HistoricalTradingRewardsPeriod.WEEKLY,
            )
            perp.updateStateChanges(changes)
            assertEquals(perp.internalState.wallet.account.tradingRewards.historical.size, 1)
            assertEquals(
                perp.internalState.wallet.account.tradingRewards.historical[HistoricalTradingRewardsPeriod.WEEKLY]?.size,
                2,
            )

            changes = perp.historicalTradingRewards(
                payload = mock.historicalTradingRewards.dailyCall,
                period = HistoricalTradingRewardsPeriod.DAILY,
            )
            perp.updateStateChanges(changes)
            assertEquals(perp.internalState.wallet.account.tradingRewards.historical.size, 2)
            assertEquals(
                perp.internalState.wallet.account.tradingRewards.historical[HistoricalTradingRewardsPeriod.DAILY]?.size,
                2,
            )

            changes = perp.historicalTradingRewards(
                payload = mock.historicalTradingRewards.monthlyCall,
                period = HistoricalTradingRewardsPeriod.MONTHLY,
            )
            perp.updateStateChanges(changes)
            assertEquals(perp.internalState.wallet.account.tradingRewards.historical.size, 3)
            assertEquals(
                perp.internalState.wallet.account.tradingRewards.historical[HistoricalTradingRewardsPeriod.MONTHLY]?.size,
                2,
            )

            changes = perp.historicalTradingRewards(
                payload = mock.historicalTradingRewards.monthlySecondCall,
                period = HistoricalTradingRewardsPeriod.MONTHLY,
            )
            perp.updateStateChanges(changes)
            assertEquals(perp.internalState.wallet.account.tradingRewards.historical.size, 3)
            assertEquals(
                perp.internalState.wallet.account.tradingRewards.historical[HistoricalTradingRewardsPeriod.MONTHLY]?.size,
                3,
            )
        } else {
            test(
                {
                    val changes = perp.historicalTradingRewards(
                        mock.historicalTradingRewards.weeklyCall,
                        HistoricalTradingRewardsPeriod.WEEKLY,
                    )
                    perp.updateStateChanges(changes)
                    return@test StateResponse(perp.state, changes)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "historical": {
                                     "WEEKLY": [
                                        {
                                            "amount": 1.0,
                                            "startedAt": "2023-12-03T00:00:00.000Z",
                                            "period": "WEEKLY"
                                         },
                                         {
                                            "amount": 124.03,
                                            "startedAt": "2023-11-26T00:00:00.000Z",
                                            "endedAt": "2023-12-03T00:00:00.000Z",
                                            "period": "WEEKLY"
                                         }
                                     ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                },
            )

            test(
                {
                    val changes = perp.historicalTradingRewards(
                        mock.historicalTradingRewards.dailyCall,
                        HistoricalTradingRewardsPeriod.DAILY,
                    )
                    perp.updateStateChanges(changes)
                    return@test StateResponse(perp.state, changes)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "historical": {
                                    "WEEKLY": [
                                        {   
                                            "period": "WEEKLY"
                                         },
                                         {
                                            "period": "WEEKLY"
                                         }
                                    ],
                                     "DAILY": [
                                        {
                                            "amount": 0.184633506816819606,
                                            "startedAt": "2024-01-29T00:00:00.000Z",
                                            "endedAt": "2024-01-30T00:00:00.000Z",
                                            "period": "DAILY"
                                          },
                                          {
                                            "amount": 0.631910345674096029,
                                            "startedAt": "2024-01-27T00:00:00.000Z",
                                            "endedAt": "2024-01-28T00:00:00.000Z",
                                            "period": "DAILY"
                                          }
                                     ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                },
            )

            test(
                {
                    val changes = perp.historicalTradingRewards(
                        mock.historicalTradingRewards.monthlyCall,
                        HistoricalTradingRewardsPeriod.MONTHLY,
                    )
                    perp.updateStateChanges(changes)
                    return@test StateResponse(perp.state, changes)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "historical": {
                                     "WEEKLY": [
                                        {   
                                            "period": "WEEKLY"
                                         },
                                         {
                                            "period": "WEEKLY"
                                         }
                                     ],
                                     "DAILY": [
                                        {   
                                            "period": "DAILY"
                                         },
                                         {
                                            "period": "DAILY"
                                         }
                                     ],
                                     "MONTHLY": [
                                        {   
                                            "amount": 1.00,
                                            "startedAt": "2023-12-01T00:00:00.000Z",
                                            "period": "MONTHLY"
                                        },
                                        {
                                            "amount": 124.03,
                                            "startedAt": "2023-11-01T00:00:00.000Z",
                                            "endedAt": "2023-12-01T00:00:00.000Z",
                                            "period": "MONTHLY"
                                        }
                                      ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                },
            )

            test(
                {
                    val changes = perp.historicalTradingRewards(
                        mock.historicalTradingRewards.monthlySecondCall,
                        HistoricalTradingRewardsPeriod.MONTHLY,
                    )
                    perp.updateStateChanges(changes)
                    return@test StateResponse(perp.state, changes)
                },
                """
                {
                    "wallet": {
                        "account": {
                            "tradingRewards": {
                                "historical": {
                                     "WEEKLY": [
                                        {
                                            "period": "WEEKLY"
                                         },
                                         {
                                            "period": "WEEKLY"
                                         }
                                     ],
                                     "MONTHLY": [
                                        {
                                            "amount": 1.00,
                                            "startedAt": "2023-12-01T00:00:00.000Z",
                                            "period": "MONTHLY"
                                        },
                                        {
                                            "amount": 124.03,
                                            "startedAt": "2023-11-01T00:00:00.000Z",
                                            "endedAt": "2023-12-01T00:00:00.000Z",
                                            "period": "MONTHLY"
                                        },
                                        {
                                            "amount": 100.0,
                                            "startedAt": "2023-09-01T00:00:00.000Z",
                                            "endedAt": "2023-10-01T00:00:00.000Z",
                                            "period": "MONTHLY"
                                         }
                                      ]
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
                {
                },
            )
        }
    }
}
