package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

@Suppress("UNCHECKED_CAST")
internal class HistoricalFundingsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = HistoricalFundingProcessor(parser = parser)
    internal fun received(
        existing: List<Any>?,
        content: Map<String, Any>,
    ): List<Any>? {
        val payload = parser.asNativeList(content["historicalFunding"]) as? List<Map<String, Any>>
        return if (payload != null) receivedList(existing, payload) else null
    }

    private fun receivedList(
        existing: List<Any>?,
        payload: List<Any>?,
    ): List<Any>? {
        if (payload != null) {
            val history = mutableListOf<Any>()
            for (value in payload) {
                parser.asNativeMap(value)?.let {
                    val item = itemProcessor.received(null, it)
                    history.add(item)
                }
            }
            return mergeDeprecated(
                parser,
                existing,
                history.reversed().toList(),
                "effectiveAt",
                true,
            )
        } else {
            return existing
        }
    }
}
