package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

@Suppress("UNCHECKED_CAST")
internal class TradesProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    @Suppress("PropertyName")
    private val LIMIT = 500

    private val tradeProcessor = TradeProcessor(parser = parser)
    internal fun subscribed(
        content: Map<String, Any>,
    ): List<Any>? {
        val payload =
            parser.asNativeList(content["trades"])
        return if (payload != null) received(payload) else null
    }

    @Suppress("FunctionName")
    internal fun channel_data(
        existing: List<Any>?,
        content: Map<String, Any>,
    ): List<Any>? {
        val payload =
            parser.asNativeList(content["trades"]) as? List<Map<String, Any>>
        return if (payload != null) receivedChanges(existing, payload) else existing
    }

    private fun received(payload: List<Any>): List<Any> {
        return payload.mapNotNull { item ->
            parser.asNativeMap(item)?.let {
                tradeProcessor.received(null, it)
            }
        }.toList()
    }

    private fun receivedChanges(
        existing: List<Any>?,
        payload: List<Any>?,
    ): List<Any>? {
        if (payload != null) {
            val merged = mutableListOf<Any>()
            for (value in payload) {
                parser.asNativeMap(value)?.let {
                    val trade = tradeProcessor.received(null, it)
                    merged.add(trade)
                }
            }
            if (existing?.isNotEmpty() == true) {
                merged.addAll(existing)
            }
            return if (merged.size > LIMIT) {
                merged.subList(0, LIMIT).toList()
            } else {
                merged
            }
        } else {
            return existing
        }
    }
}
