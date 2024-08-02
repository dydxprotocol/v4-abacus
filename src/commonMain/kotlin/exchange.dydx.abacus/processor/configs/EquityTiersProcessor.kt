package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.output.EquityTier
import exchange.dydx.abacus.output.EquityTiers
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
import indexer.models.chain.OnChainEquityTier
import indexer.models.chain.OnChainEquityTiersResponse
import kollections.toIList
import kotlin.math.roundToInt

internal interface EquityTiersProcessorProtocol {
    fun process(
        payload: OnChainEquityTiersResponse?
    ): EquityTiers?
}

internal class EquityTiersProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), EquityTiersProcessorProtocol {
    private val itemProcessor = EquityTierProcessor(parser = parser)

    override fun process(
        payload: OnChainEquityTiersResponse?
    ): EquityTiers? {
        if (payload?.equityTierLimitConfig == null) return null

        val shortTerms = payload.equityTierLimitConfig.shortTermOrderEquityTiers
        val shortTermEquityTiers = mutableListOf<EquityTier>()
        if (shortTerms != null) {
            for (i in shortTerms.indices) {
                val current = shortTerms[i]
                val next = shortTerms.getOrNull(i + 1)
                createEquityTier(current, next)?.let { shortTermEquityTiers.add(it) }
            }
        }

        val statefuls = payload.equityTierLimitConfig.statefulOrderEquityTiers
        val statefulOrderEquityTiers = mutableListOf<EquityTier>()
        if (statefuls != null) {
            for (i in statefuls.indices) {
                val current = statefuls[i]
                val next = statefuls.getOrNull(i + 1)
                createEquityTier(current, next)?.let { statefulOrderEquityTiers.add(it) }
            }
        }

        return EquityTiers(
            shortTermOrderEquityTiers = shortTermEquityTiers.toIList(),
            statefulOrderEquityTiers = statefulOrderEquityTiers.toIList(),
        )
    }

    private fun createEquityTier(
        current: OnChainEquityTier,
        next: OnChainEquityTier?,
    ): EquityTier? {
        val requiredTotalNetCollateralUSD = parser.asDouble(
            parser.asDecimal(current.usdTncRequired)
                ?.div(QUANTUM_MULTIPLIER),
        )
        val nextLevelRequiredTotalNetCollateralUSD = parser.asDouble(parser.asDecimal(next?.usdTncRequired)?.div(QUANTUM_MULTIPLIER))
        val maxOrders = current.limit?.roundToInt()
        return if (requiredTotalNetCollateralUSD != null && maxOrders != null) {
            EquityTier(
                requiredTotalNetCollateralUSD = requiredTotalNetCollateralUSD,
                nextLevelRequiredTotalNetCollateralUSD = nextLevelRequiredTotalNetCollateralUSD,
                maxOrders = maxOrders,
            )
        } else {
            null
        }
    }

    internal fun receivedDeprecated(
        payload: Map<String, Map<String, List<Any>>>?
    ): Map<String, Any>? {
        if (payload == null) return null
        val equityTiers = parser.asNativeMap(payload["equityTiers"])
        val modified = mutableMapOf<String, MutableList<Any>>(
            "shortTermOrderEquityTiers" to mutableListOf(),
            "statefulOrderEquityTiers" to mutableListOf(),
        )

        parser.asNativeList(equityTiers?.get("shortTermOrderEquityTiers"))?.let { shortTermOrderEquityTiers ->
            for (item in shortTermOrderEquityTiers) {
                parser.asNativeMap(item)?.let { it ->
                    itemProcessor.received(null, it)?.let { received ->
                        modified["shortTermOrderEquityTiers"]?.add(received)
                    }
                }
            }
        }

        parser.asNativeList(equityTiers?.get("statefulOrderEquityTiers"))?.let { statefulOrderEquityTiers ->
            for (item in statefulOrderEquityTiers) {
                parser.asNativeMap(item)?.let { it ->
                    itemProcessor.received(null, it)?.let { received ->
                        modified["statefulOrderEquityTiers"]?.add(received)
                    }
                }
            }
        }

        return modified
    }
}
