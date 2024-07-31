package exchange.dydx.abacus.processor.launchIncentive

import exchange.dydx.abacus.state.internalstate.InternalLaunchIncentiveState
import exchange.dydx.abacus.tests.mock.processor.launchIncentive.LaunchIncentiveSeasonsProcessorMock
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class LaunchIncentiveProcessorTests {
    private val seasonsProcessor = LaunchIncentiveSeasonsProcessorMock()
    private val processor = LaunchIncentiveProcessor(
        parser = Parser(),
        seasonsProcessor = seasonsProcessor,
    )

    @Test
    fun testProcessSeasons() {
        val existing = InternalLaunchIncentiveState()
        val payload = null
        val output = processor.processSeasons(existing, payload)
        assertEquals(seasonsProcessor.processCallCount, 1)
    }
}
