package exchange.dydx.abacus.processor.launchIncentive

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalLaunchIncentiveState
import exchange.dydx.abacus.utils.mutable
import indexer.models.configs.ConfigsLaunchIncentiveResponse

internal class LaunchIncentiveProcessor(
    parser: ParserProtocol,
    private val seasonsProcessor: LaunchIncentiveSeasonsProcessorProtocol = LaunchIncentiveSeasonsProcessor(parser = parser)
) : BaseProcessor(parser) {
    // private val pointsProcessor = LaunchIncentivePointsProcessor(parser = parser)

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

    internal fun receivedSeasons(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        /*
        Sample data:
        {
           "data":{
              "tradingSeasons":[
                 {
                    "startTimestamp":1701177710,
                    "label":"1",
                    "__typename":"TradingSeason"
                 },
                 {
                    "startTimestamp":1704384000,
                    "label":"2",
                    "__typename":"TradingSeason"
                 }
              ]
           }
        }
         */
        val data =
            parser.asNativeList(parser.value(payload, "data.tradingSeasons")) ?: return existing
        val seasonsProcessor = seasonsProcessor as? LaunchIncentiveSeasonsProcessor ?: return existing
        val seasons = seasonsProcessor.received(
            seasonsProcessor.received(
                parser.asNativeList(existing?.get("seasons")),
                data,
            ),
            data,
        ) ?: return existing

        val modified = existing?.mutable() ?: mutableMapOf()
        modified["seasons"] = seasons

        return modified
    }
}
