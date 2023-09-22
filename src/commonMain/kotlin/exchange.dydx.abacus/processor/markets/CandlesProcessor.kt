package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iListOf
import kollections.iMutableListOf
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

    internal fun subscribed(
        existing: IMap<String, Any>?,
        resolution: String,
        content: IMap<String, Any>,
    ): IMap<String, Any>? {
        val payload =
            parser.asList(content["candles"])
        return if (payload != null) {
            receivedChanges(existing, resolution, payload)
        } else existing
    }

    internal fun channel_data(
        existing: IMap<String, Any>?,
        resolution: String,
        content: IMap<String, Any>,
    ): IMap<String, Any>? {
        // content is a single candle update
        return receivedChange(existing, resolution, content)
    }

    private fun receivedChanges(
        existing: IMap<String, Any>?,
        resolution: String,
        payload: IList<Any>?,
    ): IMap<String, Any>? {
        if (payload != null) {
            val modified = existing?.mutable() ?: iMutableMapOf()
            val existingResolution = parser.asList(existing?.get(resolution))
            val candles = iMutableListOf<Any>()
            for (value in payload.reversed()) {
                parser.asMap(value)?.let {
                    val candle = itemProcessor.received(null, it)
                    candles.add(candle)
                }
            }
            val mergedResolution = merge(
                parser,
                existingResolution,
                candles,
                "startedAt",
                true
            )
            modified.safeSet(resolution, mergedResolution)
            return modified
        } else {
            return existing
        }
    }

    private fun receivedChange(
        existing: IMap<String, Any>?,
        resolution: String,
        payload: IMap<String, Any>,
    ): IMap<String, Any>? {
        if (payload != null) {
            val modified = existing?.mutable() ?: iMutableMapOf()
            val existingResolution = parser.asList(existing?.get(resolution))
            val candles = existingResolution?.mutable() ?: iMutableListOf()
            val lastExisting = parser.asMap(candles.lastOrNull())
            val lastStartAt = parser.asDatetime(lastExisting?.get("startedAt"))
            val incoming = itemProcessor.received(null, payload)
            val incomingStartAt = parser.asDatetime(incoming["startedAt"])
            if (lastStartAt == incomingStartAt) {
                candles.removeLast()
            }
            candles.add(incoming)
            modified.safeSet(resolution, candles)
            return modified
        } else {
            return existing
        }
    }
}