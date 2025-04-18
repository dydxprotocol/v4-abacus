package exchange.dydx.abacus.processor.launchIncentive

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalLaunchIncentiveState
import indexer.models.configs.ConfigsLaunchIncentiveResponse

internal class LaunchIncentiveProcessor(
    parser: ParserProtocol,
    private val seasonsProcessor: LaunchIncentiveSeasonsProcessorProtocol = LaunchIncentiveSeasonsProcessor(parser = parser)
) : BaseProcessor(parser) {
    internal fun processSeasons(
        existing: InternalLaunchIncentiveState,
        payload: ConfigsLaunchIncentiveResponse?
    ): InternalLaunchIncentiveState {
        existing.seasons = seasonsProcessor.process(
            existing = existing.seasons,
            payload = payload,
        )
        return existing
    }
}
