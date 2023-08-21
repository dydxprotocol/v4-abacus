package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.iMutableListOf

@Suppress("UNCHECKED_CAST")
internal class FeeDiscountsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = FeeDiscountProcessor(parser = parser)

    internal fun received(
        payload: IList<Any>
    ): IList<Any> {
        val modified = iMutableListOf<IMap<String, Any>>()
        for (item in payload) {
            parser.asMap(item)?.let { it ->
                itemProcessor.received(null, it)?.let { received ->
                    modified.add(received)
                }
            }
        }
        return modified
    }
}