package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.LaunchIncentivePoint
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import indexer.models.configs.ConfigsLaunchIncentivePoints

internal class LaunchIncentivePointsProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser) {
    private val itemProcessor = LaunchIncentivePointProcessor(parser = parser)
    
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
