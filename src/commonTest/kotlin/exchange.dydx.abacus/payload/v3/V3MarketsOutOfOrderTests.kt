package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.tests.extensions.*
import exchange.dydx.abacus.utils.ServerTime
import kotlinx.datetime.Clock
import kotlin.test.*

/*
Test if we receive REST payload from markets configurations first, then the socket
 */

class V3MarketsOutOfOrderTests : V3BaseTests() {
    @Test
    fun testOutOfOrder() {
        testMarketsOnce()
    }

    @Test
    fun control() {
        testMarketsOnce(false)
    }

    private fun test(map: MutableMap<String, String>) {
        map["test"] = "1"
    }

    private fun testMarketsOnce(outOfOrder: Boolean = true) {
        var time = ServerTime.now()

        if (outOfOrder) {
            testMarketsConfigurations()
            time = perp.log("Markets Configurations", time)
            testMarketsSubscribed()
            time = perp.log("Markets Subscribed", time)
        } else {
            testMarketsSubscribed()
            time = perp.log("Markets Subscribed", time)
            testMarketsConfigurations()
            time = perp.log("Markets Configurations", time)
        }

        testMarketsChanged()
        perp.log("Markets Changed", time)
    }

    private fun testMarketsSubscribed() {
        test(
            {
                loadMarkets()
            },
            """
                {
                    "markets": {
                        "markets": {
                            "ETH-USD": {
                                "assetId": "ETH",
                                "market": "ETH-USD",
                                "indexPrice": 1754.0223,
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
                                "indexPrice": 1754.0223,
                                "priceChange24H": 14.47502
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun testMarketsConfigurations() {
        test (
            {
                loadMarketsConfigurations()
            },
            """
                {
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
                                "imageUrl": "https://s3.amazonaws.com/dydx.exchange/currencies/eth.png",
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
}