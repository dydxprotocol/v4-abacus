package exchange.dydx.abacus.tickets

import exchange.dydx.abacus.output.LaunchIncentiveSeason
import exchange.dydx.abacus.state.model.launchIncentivePoints
import exchange.dydx.abacus.state.model.launchIncentiveSeasons
import kollections.toIList
import kotlin.test.Test
import kotlin.test.assertEquals

class TRCL3551Tests : TRCL2998Tests() {

    @Test
    fun testLaunchIncentive() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        if (perp.staticTyping) {
            perp.updateResponse(perp.launchIncentiveSeasons(mock.launchIncentiveMock.seasons))
            assertEquals(
                perp.internalState.launchIncentive.seasons,
                listOf(
                    LaunchIncentiveSeason(
                        label = "1",
                        startTimeInMilliseconds = 1701177710000.0,
                    ),
                    LaunchIncentiveSeason(
                        label = "2",
                        startTimeInMilliseconds = 1704384000000.0,
                    ),
                ).toIList(),
            )
        } else {
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
                    perp.updateResponse(
                        perp.launchIncentivePoints(
                            "2",
                            mock.launchIncentiveMock.points,
                        ),
                    )
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
}
