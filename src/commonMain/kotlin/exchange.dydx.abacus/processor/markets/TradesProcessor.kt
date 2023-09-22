package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.iMutableListOf
import kollections.toIList

@Suppress("UNCHECKED_CAST")
internal class TradesProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val LIMIT = 500
    private val tradeProcessor = TradeProcessor(parser = parser)
    internal fun subscribed(
        content: IMap<String, Any>,
    ): IList<Any>? {
        val payload =
            parser.asList(content["trades"])
        return if (payload != null) received(payload) else null
    }

    internal fun channel_data(
        existing: IList<Any>?,
        content: IMap<String, Any>,
    ): IList<Any>? {
        val payload =
            parser.asList(content["trades"]) as? IList<IMap<String, Any>>
        return if (payload != null) receivedChanges(existing, payload) else existing
    }

    private fun received(payload: IList<Any>): IList<Any> {
        return payload.mapNotNull { item ->
            parser.asMap(item)?.let {
                tradeProcessor.received(null, it)
            }
        }.toIList()
    }

    private fun receivedChanges(
        existing: IList<Any>?,
        payload: IList<Any>?,
    ): IList<Any>? {
        if (payload != null) {
            val trades = iMutableListOf<Any>()
            for (value in payload) {
                parser.asMap(value)?.let {
                    val trade = tradeProcessor.received(null, it)
                    trades.add(trade)
                }
            }
            val merged = merge(
                parser,
                existing,
                trades,
                "createdAt",
                false
            )
            return if (merged != null && merged.size > LIMIT) {
                merged.subList(0, LIMIT).toIList()
            } else merged
        } else {
            return existing
        }
    }
}