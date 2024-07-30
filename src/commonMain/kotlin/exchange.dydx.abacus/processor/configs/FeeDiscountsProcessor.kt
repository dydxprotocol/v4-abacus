package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

// Not used in the project
internal class FeeDiscountsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = FeeDiscountProcessor(parser = parser)

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
