package exchange.dydx.abacus.tickets

import exchange.dydx.abacus.output.LaunchIncentiveSeason
import exchange.dydx.abacus.state.machine.launchIncentiveSeasons
import kollections.toIList
import kotlin.test.Test
import kotlin.test.assertEquals

class TRCL3551Tests : TRCL2998Tests() {

    @Test
    fun testLaunchIncentive() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

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
    }
}
