package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.mergeWithIds
import exchange.dydx.abacus.protocols.ParserProtocol

@Deprecated("static-typing")
@Suppress("UNCHECKED_CAST")
internal class TradesProcessor(
    parser: ParserProtocol,
) : BaseProcessor(parser) {
    @Suppress("PropertyName")
    private val LIMIT = 500

    private val tradeProcessor = TradeProcessor(parser = parser)

    @Deprecated("static-typing")
    internal fun subscribedDeprecated(
        content: Map<String, Any>,
    ): List<Any>? {
        val payload =
            parser.asNativeList(content["trades"])
        return if (payload != null) received(payload) else null
    }

    @Deprecated("static-typing")
    @Suppress("FunctionName")
    internal fun channel_dataDeprecated(
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
            val new = payload.mapNotNull { eachPayload ->
                parser.asNativeMap(eachPayload)?.let { eachPayloadData -> tradeProcessor.received(null, eachPayloadData) }
            }
            val merged = existing?.let {
                mergeWithIds(new, existing) { data -> parser.asNativeMap(data)?.let { parser.asString(it["id"]) } }
            } ?: new

            return if (merged.size > LIMIT) {
                merged.subList(0, LIMIT)
            } else {
                merged
            }
        } else {
            return existing
        }
    }
}
