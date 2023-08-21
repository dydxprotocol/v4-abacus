package exchange.dydx.abacus.payload.v3

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.tests.extensions.*
import exchange.dydx.abacus.utils.ServerTime
import kotlinx.datetime.Clock
import kotlin.test.*


class V3FeeTests : V3BaseTests() {
    @Test
    fun testFeeTiers() {
        print("--------First round----------\n")

        testFeeTiersOnce()
        perp.state = null

        print("--------Second round----------\n")
        testFeeTiersOnce()
    }

    private fun testFeeTiersOnce() {
        var time = ServerTime.now()
        testFeeTiersOneRound()
        time = perp.log("Fee Tiers Call", time)
    }

    private fun testFeeTiersOneRound() {
        test(
            {
                perp.loadFeeTiers(mock)
            },
            """
                {
                "configs": {
                    "feeTiers": [
                        {
                            "tier": "Free",
                            "symbol": "<",
                            "volume": 100000,
                            "maker": 0.0,
                            "taker": "0.0",
                            "id": "Free",
                            "resources": {
                            }
                        }
                    ]
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testFeeDiscounts() {
        print("--------First round----------\n")

        testFeeDiscountsOnce()
        perp.state = null

        print("--------Second round----------\n")
        testFeeDiscountsOnce()
    }

    private fun testFeeDiscountsOnce() {
        var time = ServerTime.now()
        testFeeDiscountsOneRound()
        time = perp.log("Fee Tiers Call", time)
    }

    private fun testFeeDiscountsOneRound() {
        test(
            {
                perp.loadFeeDiscounts(mock)
            },
            """
                {
                    "configs": {
                        "feeDiscounts": [
                            {
                                "tier": "I",
                                "symbol": "≥",
                                "balance": 100,
                                "discount": 0.03,
                                "id": "I",
                                "resources": {
                                }
                            }
                        ]
                    }
                }
            """.trimIndent()
        )
    }
}