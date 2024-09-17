package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.tests.extensions.loadMarketsChanged
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

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
            loadMarketsConfigurations()
            time = perp.log("Markets Configurations", time)
            testMarketsSubscribed()
            time = perp.log("Markets Subscribed", time)
        } else {
            testMarketsSubscribed()
            time = perp.log("Markets Subscribed", time)
            loadMarketsConfigurations()
            time = perp.log("Markets Configurations", time)
        }

        testMarketsChanged()
        perp.log("Markets Changed", time)
    }

    private fun testMarketsSubscribed() {
        if (perp.staticTyping) {
            loadMarkets()

            val markets = perp.internalState.marketsSummary.markets
            val ethMarket = markets["ETH-USD"]!!
            assertEquals(1753.2932, ethMarket.perpetualMarket?.oraclePrice)
            assertEquals(14.47502, ethMarket.perpetualMarket?.priceChange24H)
            assertEquals("ETH", ethMarket.perpetualMarket?.assetId)
            assertEquals("ETH-USD", ethMarket.perpetualMarket?.market)
            assertEquals(0.001, ethMarket.perpetualMarket?.configs?.stepSize)
            assertEquals(0.03, ethMarket.perpetualMarket?.configs?.maintenanceMarginFraction)
            assertEquals(0.05, ethMarket.perpetualMarket?.configs?.initialMarginFraction)
            assertEquals(0.1, ethMarket.perpetualMarket?.configs?.tickSize)
            assertEquals(true, ethMarket.perpetualMarket?.status?.canTrade)
            assertEquals(true, ethMarket.perpetualMarket?.status?.canReduce)
        } else {
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
                """.trimIndent(),
            )
        }
    }

    private fun testMarketsChanged() {
        if (perp.staticTyping) {
            perp.loadMarketsChanged(mock)

            val markets = perp.internalState.marketsSummary.markets
            val ethMarket = markets["ETH-USD"]!!
            assertEquals(1753.2932, ethMarket.perpetualMarket?.oraclePrice)
            assertEquals(14.47502, ethMarket.perpetualMarket?.priceChange24H)
        } else {
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
                """.trimIndent(),
            )
        }
    }
}
