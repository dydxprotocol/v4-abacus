package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

@Suppress("UNCHECKED_CAST")
internal class EquityTiersProcessor(parser: ParserProtocol): BaseProcessor(parser) {
    private val itemProcessor = EquityTierProcessor(parser = parser)

    internal fun received(
        payload: Map<String, Map<String, List<Any>>>?
    ): Map<String, Any>? {
        if (payload == null) return null
        val equityTiers = parser.asNativeMap(payload["equityTiers"])
        val modified = mutableMapOf<String, MutableList<Any>>(
            "shortTermOrderEquityTiers" to mutableListOf(),
            "statefulOrderEquityTiers" to mutableListOf()
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