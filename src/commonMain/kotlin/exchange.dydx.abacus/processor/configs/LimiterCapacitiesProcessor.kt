package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

@Suppress("UNCHECKED_CAST")
internal class LimiterCapacitiesProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = LimiterCapacityProcessor(parser = parser)

    internal fun received(
        payload: List<Any>
    ): List<Any> {
        val modified = mutableListOf<Map<String, Any>>()
        for (item in payload) {
            parser.asNativeMap(item)?.let { it ->
                itemProcessor.received(null, it)?.let { received ->
                    modified.add(received)
                }
            }
        }
        return modified
    }
}
