package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.iMutableListOf
import kollections.toIList

@Suppress("UNCHECKED_CAST")
internal class HistoricalFundingsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = HistoricalFundingProcessor(parser = parser)
    internal fun received(
        existing: IList<Any>?,
        content: IMap<String, Any>,
    ): IList<Any>? {
        val payload = parser.asList(content["historicalFunding"]) as? IList<IMap<String, Any>>
        return if (payload != null) receivedList(existing, payload) else null
    }

    private fun receivedList(
        existing: IList<Any>?,
        payload: IList<Any>?,
    ): IList<Any>? {
        if (payload != null) {
            val history = iMutableListOf<Any>()
            for (value in payload) {
                parser.asMap(value)?.let {
                    val item = itemProcessor.received(null, it)
                    history.add(item)
                }
            }
            return merge(
                parser,
                existing,
                history.reversed().toIList(),
                "effectiveAt",
                true
            )
        } else {
            return existing
        }
    }
}