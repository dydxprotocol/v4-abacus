package exchange.dydx.abacus.tests.mock.processor.launchIncentive

import exchange.dydx.abacus.output.LaunchIncentiveSeason
import exchange.dydx.abacus.processor.launchIncentive.LaunchIncentiveSeasonsProcessorProtocol
import indexer.models.configs.ConfigsLaunchIncentiveResponse

internal class LaunchIncentiveSeasonsProcessorMock : LaunchIncentiveSeasonsProcessorProtocol {
    var processCallCount = 0
    var processAction: ((existing: List<LaunchIncentiveSeason>?, payload: ConfigsLaunchIncentiveResponse?) -> List<LaunchIncentiveSeason>?)? = null

    override fun process(
        existing: List<LaunchIncentiveSeason>?,
        payload: ConfigsLaunchIncentiveResponse?
    ): List<LaunchIncentiveSeason>? {
        processCallCount++
        return processAction?.invoke(existing, payload)
    }
}
