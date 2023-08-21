package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import kollections.toIList

@Suppress("UNCHECKED_CAST")
internal class CandlesProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = CandleProcessor(parser = parser)

    override fun received(
        existing: IList<Any>?,
        payload: IList<Any>
    ): IList<Any>? {
        return merge(
            parser,
            existing,
            parser.asList(payload)?.reversed()?.toIList(),
            "startedAt",
            true
        )
    }
}