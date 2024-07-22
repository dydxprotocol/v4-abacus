package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.LaunchIncentivePoint
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import indexer.models.configs.ConfigsLaunchIncentivePoints

internal interface LaunchIncentivePointsProcessorProtocol {
    fun process(
        season: String,
        existing: MutableMap<String, LaunchIncentivePoint>,
        payload: ConfigsLaunchIncentivePoints?,
    ): MutableMap<String, LaunchIncentivePoint>
}

internal class LaunchIncentivePointsProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), LaunchIncentivePointsProcessorProtocol {
    private val itemProcessor = LaunchIncentivePointProcessor(parser = parser)

    override fun process(
        season: String,
        existing: MutableMap<String, LaunchIncentivePoint>,
        payload: ConfigsLaunchIncentivePoints?,
    ): MutableMap<String, LaunchIncentivePoint> {
        payload?.let { data ->
            existing[season] = LaunchIncentivePoint(
                incentivePoints = data.incentivePoints ?: 0.0,
                marketMakingIncentivePoints = data.marketMakingIncentivePoints ?: 0.0,
            )
        }
        return existing
    }

    fun received(
        season: String,
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any>? {
        parser.asNativeMap(payload)?.let { data ->
            itemProcessor.received(
                parser.asNativeMap(existing?.get(season)),
                data,
            ).let { parsed ->
                val modified = existing?.mutable() ?: mutableMapOf()
                modified[season] = parsed
                return modified
            }
        }
        return existing
    }
}
