package exchange.dydx.abacus.processor.launchIncentive

import exchange.dydx.abacus.output.LaunchIncentiveSeason
import exchange.dydx.abacus.utils.Parser
import indexer.models.configs.ConfigsLaunchIncentiveData
import indexer.models.configs.ConfigsLaunchIncentiveResponse
import indexer.models.configs.ConfigsLaunchIncentiveSeason
import kotlin.test.Test
import kotlin.test.assertEquals

class LaunchIncentiveSeasonsProcessorTests {
    companion object {
        val payloadMock = ConfigsLaunchIncentiveResponse(
            data = ConfigsLaunchIncentiveData(
                tradingSeasons = listOf(
                    ConfigsLaunchIncentiveSeason(
                        label = "Season 1",
                        startTimestamp = 1.0,
                    ),
                    ConfigsLaunchIncentiveSeason(
                        label = "Season 2",
                        startTimestamp = 2.0,
                    ),
                ),
            ),
        )

        val seasonsMock = listOf(
            LaunchIncentiveSeason(
                label = "Season 1",
                startTimeInMilliseconds = 1000.0,
            ),
            LaunchIncentiveSeason(
                label = "Season 2",
                startTimeInMilliseconds = 2000.0,
            ),
        )
    }

    private val processor = LaunchIncentiveSeasonsProcessor(parser = Parser())

    @Test
    fun testProcess() {
        val seasons = processor.process(null, payloadMock)
        assertEquals(seasons, seasonsMock)
    }
}
