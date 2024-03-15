package exchange.dydx.abacus.tickets

import exchange.dydx.abacus.state.model.launchIncentivePoints
import exchange.dydx.abacus.state.model.launchIncentiveSeasons
import kotlin.test.Test

internal class TRCL3551Tests : TRCL2998Tests() {

    @Test
    fun testLaunchIncentive() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        test(
            {
                perp.updateResponse(perp.launchIncentiveSeasons(mock.launchIncentiveMock.seasons))
            },
            """
            {
                "launchIncentive": {
                    "seasons": [
                        {
                            "label": "1",
                            "startTimestamp": 1701177710
                        },
                        {
                            "label": "2",
                            "startTimestamp": 1704384000
                        }
                    ]
                }
            }
            """.trimIndent(),
        )

        test(
            {
                perp.updateResponse(perp.launchIncentivePoints("2", mock.launchIncentiveMock.points))
            },
            """
            {
                "wallet": {
                    "account": {
                        "launchIncentivePoints": {
                            "2": {
                                "incentivePoints": 0.01,
                                "marketMakingIncentivePoints": 0
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
    }
}
