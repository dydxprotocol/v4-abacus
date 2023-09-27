package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.tests.extensions.loadAccountsChanged
import exchange.dydx.abacus.tests.extensions.loadFillsReceived
import exchange.dydx.abacus.tests.extensions.loadMarketsChanged
import exchange.dydx.abacus.tests.extensions.loadOrderbook
import exchange.dydx.abacus.tests.extensions.loadOrderbookChanged
import exchange.dydx.abacus.tests.extensions.loadTrades
import exchange.dydx.abacus.tests.extensions.loadTradesChanged
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test


class V3PerpTests : V3BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result

        print("--------First round----------\n")

        testMarketsOnce()
        testAccountsOnce()
        testTradesOnce()
        testOrderbookOnce()
        testHistoricalFundingsOnce()

    }

    private fun testMarketsOnce() {
        var time = ServerTime.now()
        testMarketsSubscribed()
        time = perp.log("Markets Subscribed", time)

        testMarketsChanged()
        time = perp.log("Markets Changed", time)

        testMarketsConfigurations()
        perp.log("Markets Configurations", time)
    }

    private fun testMarketsSubscribed() {
        test(
            {
                loadMarkets()
            },
            """
                {
                    "markets": {
                        "volume24HUSDC": 1.4184778841793003E9,
                        "openInterestUSDC": 4.26177643402556E8,
                        "trades24H": 246191,
                        "markets": {
                            "ETH-USD": {
                                "assetId": "ETH",
                                "market": "ETH-USD",
                                "oraclePrice": 1753.2932,
                                "priceChange24H": 14.47502,
                                "status": {
                                    "canTrade": true,
                                    "canReduce": true
                                },
                                "configs": {
                                    "baselinePositionSize": 500.0,
                                    "maxPositionSize": 10000.0,
                                    "incrementalInitialMarginFraction": 0.01,
                                    "stepSize": 0.001,
                                    "maintenanceMarginFraction": 0.03,
                                    "initialMarginFraction": 0.05,
                                    "minOrderSize": 0.01,
                                    "incrementalPositionSize": 100.0,
                                    "tickSize": 0.1
                                },
                                "perpetual": {
                                    "volume24H": 774356829.1303,
                                    "trades24H": 86995.0,
                                    "openInterest": 81484.747,
                                    "nextFundingRate": -0.0000178049
                                }
                            },
                            "SOL-USD": {
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testMarketsChanged() {
        test(
            {
                perp.loadMarketsChanged(mock)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "assetId": "ETH",
                                "market": "ETH-USD",
                                "oraclePrice": 1753.2932,
                                "priceChange24H": 14.47502,
                                "status": {
                                    "canTrade": true,
                                    "canReduce": true
                                },
                                "configs": {
                                    "baselinePositionSize": 500.0,
                                    "maxPositionSize": 10000.0,
                                    "incrementalInitialMarginFraction": 0.01,
                                    "stepSize": 0.001,
                                    "maintenanceMarginFraction": 0.03,
                                    "initialMarginFraction": 0.05,
                                    "minOrderSize": 0.01,
                                    "incrementalPositionSize": 100.0,
                                    "tickSize": 0.1
                                },
                                "perpetual": {
                                    "volume24H": 7.751469063948E8,
                                    "trades24H": 87076,
                                    "openInterest": 81495.704,
                                    "nextFundingRate": -0.0000178049
                                }
                            },
                            "SOL-USD": {
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testMarketsConfigurations() {
        test(
            {
                loadMarketsConfigurations()
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "marketCaps": 2.077248341931288E11
                            }
                        }
                    },
                    "assets": {
                        "ETH": {
                            "symbol": "ETH",
                            "name": "Ethereum",
                            "circulatingSupply": 1.1847695194E8,
                            "tags": [
                                "Layer 1"
                            ],
                            "resources": {
                                "websiteLink": "https://ethereum.org/",
                                "whitepaperLink": "https://ethereum.org/whitepaper/",
                                "coinMarketCapsLink": "https://coinmarketcap.com/currencies/ethereum/",
                                "imageUrl": "https://v4.testnet.dydx.exchange/currencies/eth.svg",
                                "primaryDescriptionKey": "ETHEREUM_PRIMARY_DESCRIPTION",
                                "secondaryDescriptionKey": "ETHEREUM_SECONDARY_DESCRIPTION"
                            }
                        },
                        "SOL": {
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testAccountsOnce() {
        var time = ServerTime.now()
        testAccountsSubscribed()
        time = perp.log("Accounts Subscribed", time)

        testFillsReceived()
        time = perp.log("Fills Received", time)

        testAccountsChanged()
        perp.log("Accounts Changed", time)
    }

    private fun testAccountsSubscribed() {
        test(
            {
                loadAccounts()
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "quoteBalance": {
                                        "current": -62697.279528
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "id": "ETH-USD",
                                            "assetId": "ETH",
                                            "entryPrice": {
                                                "current": 1091.812076
                                            },
                                            "exitPrice": 1091.236219,
                                            "netFunding": -4101.337527,
                                            "size": {
                                                "current": 93.57
                                            },
                                            "createdAt": "2022-06-30T01:01:10.234Z",
                                            "resources": {
                                                "sideStringKey": {
                                                    "current": "APP.GENERAL.LONG_POSITION_SHORT"
                                                },
                                                "indicator": {
                                                    "current": "long"
                                                }
                                            }
                                        }
                                    },
                                    "orders": {
                                        "3c5193d7a49805ffcf231af1ed446188f04aaa6756bf9df7b5913568b2763d7": {
                                            "id": "3c5193d7a49805ffcf231af1ed446188f04aaa6756bf9df7b5913568b2763d7",
                                            "marketId": "ETH-USD",
                                            "price": 1500.0,
                                            "size": 0.1,
                                            "remainingSize": 0.1,
                                            "createdAt": "2022-08-01T22:25:31.111Z",
                                            "expiresAt": "2022-08-29T22:45:30.776Z",
                                            "postOnly": false,
                                            "reduceOnly": false
                                        }
                                    },
                                    "transfers": [
                                        {
                                            "id": "89586775-0646-582e-9b36-4f131715644d",
                                            "type": "WITHDRAWAL",
                                            "asset": "USDC",
                                            "updatedAtBlock": 404014,
                                            "amount": 419.98472,
                                            "fromAddress": "dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                                            "toAddress": "dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8",
                                            "resources": {
                                                "typeStringKey": "APP.GENERAL.TRANSFER_OUT",
                                                "iconLocal": "Outgoing",
                                                "indicator": "confirmed"
                                            },
                                            "transactionHash": "MOCKHASH"
                                        }
                                    ],
                                    "fundingPayments": [
                                        {
                                            "marketId": "SUSHI-USD",
                                            "payment": -1.42E-4,
                                            "rate": 7.7675E-6,
                                            "positionSize": 12.0,
                                            "price": 1.524267,
                                            "effectiveAt": "2022-07-29T23:00:00Z"
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }


    private fun testFillsReceived() {
        test(
            {
                perp.loadFillsReceived(mock)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "fills": [
                                        {
                                            "id": "c4f12741-dbe6-597c-8efc-0fd5801f99b3",
                                            "side": "BUY",
                                            "liquidity": "TAKER",
                                            "type": "MARKET",
                                            "marketId": "ETH-USD",
                                            "orderId": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                                            "createdAt": "2022-08-01T19:53:29.686Z",
                                            "price": 1621.6,
                                            "size": 0.583,
                                            "fee": 0.472696,
                                            "resources": {
                                            }
                                        }
                                    ]   
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }


    private fun testAccountsChanged() {
        test(
            {
                perp.loadAccountsChanged(mock)
            },
            """
                {
                    "wallet": {
                        "account": {
                            "subaccounts": {
                                "0": {
                                    "quoteBalance": {
                                        "current": -222333.2879
                                    },
                                    "openPositions": {
                                        "ETH-USD": {
                                            "id": "ETH-USD",
                                            "assetId": "ETH",
                                            "entryPrice": {
                                                "current": 1314.480485
                                            },
                                            "exitPrice": 1265.594735,
                                            "netFunding": -4155.221089,
                                            "size": {
                                                "current": 192.096
                                            },
                                            "createdAt": "2022-06-30T01:01:10.234Z",
                                            "resources": {
                                                "sideStringKey": {
                                                    "current": "APP.GENERAL.LONG_POSITION_SHORT"
                                                },
                                                "indicator": {
                                                    "current": "long"
                                                }
                                            }
                                        }
                                    },
                                    "orders": {
                                        "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397": {
                                            "id": "025f806ef88ff3cdefaa7eea314040f77625cd71cbd837d951036b23c51f397",
                                            "marketId": "ETH-USD",
                                            "price": 1702.7,
                                            "size": 45.249,
                                            "remainingSize": 0.0,
                                            "createdAt": "2022-08-01T19:53:29.653Z",
                                            "expiresAt": "2022-08-01T20:13:29.361Z",
                                            "postOnly": false,
                                            "reduceOnly": false,
                                            "resources": {
                                                "sideStringKey": "APP.GENERAL.BUY",
                                                "typeStringKey": "APP.TRADE.MARKET_ORDER_SHORT",
                                                "statusStringKey": "APP.TRADE.ORDER_FILLED",
                                                "timeInForceStringKey": "APP.TRADE.FILL_OR_KILL"
                                            }
                                        }
                                    },
                                    "transfers": [
                                        {
                                            "id": "89586775-0646-582e-9b36-4f131715644d",
                                            "type": "WITHDRAWAL",
                                            "asset": "USDC",
                                            "updatedAtBlock": 404014,
                                            "amount": 419.98472,
                                            "fromAddress": "dydx1sxdvx2kzgdykutxfv06ka9gt0klu8wctfwskhg",
                                            "toAddress": "dydx1vvjr376v4hfpy5r6m3dmu4u3mu6yl6sjds3gz8",
                                            "resources": {
                                                "typeStringKey": "APP.GENERAL.TRANSFER_OUT",
                                                "iconLocal": "Outgoing",
                                                "indicator": "confirmed"
                                            },
                                            "transactionHash": "MOCKHASH"
                                        }
                                    ],
                                    "fundingPayments": [
                                        {
                                            "marketId": "SUSHI-USD",
                                            "payment": -1.42E-4,
                                            "rate": 7.7675E-6,
                                            "positionSize": 12.0,
                                            "price": 1.524267,
                                            "effectiveAt": "2022-07-29T23:00:00Z"
                                        }
                                    ]   
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testOrderbookOnce() {
        var time = ServerTime.now()
        testOrderbookSubscribed()
        time = perp.log("Orderbook Subscribed", time)

        testOrderbookBatchChanged()
        perp.log("Orderbook Changed", time)
    }

    private fun testOrderbookSubscribed() {
        test(
            {
                perp.loadOrderbook(mock)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                    "asks": [
                                        {
                                            "price": 1655.7,
                                            "size": 31.231,
                                            "depth": 31.231
                                        },
                                        {
                                            "price": 1655.8,
                                            "size": 25.138,
                                            "depth": 56.369
                                        }
                                    ],
                                    "bids": [
                                        {
                                            "price": 1654.3,
                                            "size": 13.363,
                                            "depth": 13.363
                                        },
                                        {
                                            "price": 1653.0,
                                            "size": 19.55,
                                            "depth": 32.913
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }


    private fun testOrderbookBatchChangedv4() {
        test(
            {
                perp.socket(mock.socketUrl, mock.orderbookChannel.v4_channel_batch_data, 0, null)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                    "asks": [
                                        {
                                            "price": 1655.5,
                                            "size": 17.433
                                        }
                                    ],
                                    "bids": [
                                        {
                                            "price": 1654.3,
                                            "size": 13.363
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testOrderbookBatchChanged() {
        test(
            {
                perp.loadOrderbookChanged(mock)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "orderbook": {
                                    "asks": [
                                        {
                                            "price": 1655.5,
                                            "size": 17.433,
                                            "offset": 161942818
                                        }
                                    ],
                                    "bids": [
                                        {
                                            "price": 1654.3,
                                            "size": 13.363,
                                            "offset": 0
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testTradesOnce() {
        var time = ServerTime.now()
        testTradesSubscribed()
        time = perp.log("Trades Subscribed", time)

        testTradesBatchChanged()
        perp.log("Trades Changed", time)
    }

    private fun testTradesSubscribed() {
        test(
            {
                perp.loadTrades(mock)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "trades": [
                                    {
                                        "side": "BUY",
                                        "price": 1656.2,
                                        "size": 0.01,
                                        "liquidation": true,
                                        "createdAt": "2022-08-01T16:58:12.989Z"
                                    }
                                ]
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testTradesBatchChanged() {
        test(
            {
                perp.loadTradesChanged(mock)
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "trades": [
                                    {
                                        "side": "SELL",
                                        "price": 1645.7,
                                        "size": 24.243,
                                        "liquidation": false,
                                        "createdAt": "2022-08-01T17:05:28.592Z"
                                    }
                                ]
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }


    private fun testHistoricalFundingsOnce() {
        var time = ServerTime.now()
        testHistoricalFundingsReceived()
        time = perp.log("Historical Fundings Subscribed", time)
    }

    private fun testHistoricalFundingsReceived() {
        val url = AbUrl(
            host = "api.stage.dydx.exchange",
            port = null,
            path = "/v3/historical-funding/ETH-USD",
            scheme = "https://",
        )

        test(
            {
                perp.rest(
                    url, mock.historicalFundingsMock.call, 0, null
                )
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "historicalFunding": [
                                    {
                                        "rate": 1.25E-5,
                                        "price": 1325.1622645184,
                                        "effectiveAt": "2022-10-08T19:00:00.000Z"
                                    }
                                ]
                            }
                        }
                    }
                }
            """.trimIndent()
        )

        test(
            {
                perp.rest(
                    url, mock.historicalFundingsMock.call, 0, null
                )
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "historicalFunding": [
                                    {
                                        "rate": 1.25E-5,
                                        "price": 1325.1622645184,
                                        "effectiveAt": "2022-10-08T19:00:00.000Z"
                                    }
                                ]
                            }
                        }
                    }
                }
            """.trimIndent()
        )

        test(
            {
                perp.rest(
                    url, mock.historicalFundingsMock.call2, 0, null
                )
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "historicalFunding": [
                                    {
                                        "rate": 0.0000116939,
                                        "price": 1329.3999999296,
                                        "effectiveAt": "2022-10-08T17:00:00.000Z"
                                    }
                                ]
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

}