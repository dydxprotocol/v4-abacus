package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.tests.extensions.*
import exchange.dydx.abacus.utils.ServerTime
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
                                "oraclePrice": 1753.2932,
                                "priceChange24H": 14.47502,
                                "status": {
                                    "canTrade": true,
                                    "canReduce": true
                                },
                                "configs": {
                                    "stepSize": 0.001,
                                    "maintenanceMarginFraction": 0.03,
                                    "initialMarginFraction": 0.05,
                                    "tickSize": 0.1
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
                                "oraclePrice": 1753.2932,
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
                            "id": "ETH",
                            "name": "Ethereum",
                            "tags": [
                                "Layer 1"
                            ],
                            "resources": {
                                "websiteLink": "https://ethereum.org/",
                                "whitepaperLink": "https://ethereum.org/whitepaper/",
                                "coinMarketCapsLink": "https://coinmarketcap.com/currencies/ethereum/",
                                "imageUrl": "https://api.examples.com/currencies/eth.png",
                                "primaryDescriptionKey": "__ASSETS.ETH.PRIMARY",
                                "secondaryDescriptionKey": "__ASSETS.ETH.SECONDARY"
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