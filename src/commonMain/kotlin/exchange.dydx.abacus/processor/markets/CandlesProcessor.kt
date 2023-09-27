package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class CandlesProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = CandleProcessor(parser = parser)

    override fun received(
        existing: List<Any>?,
        payload: List<Any>
    ): List<Any>? {
        return merge(
            parser,
            existing,
            parser.asNativeList(payload)?.reversed()?.toList(),
            "startedAt",
            true
        )
    }

    internal fun subscribed(
        existing: Map<String, Any>?,
        resolution: String,
        content: Map<String, Any>,
    ): Map<String, Any>? {
        val payload =
            parser.asNativeList(content["candles"])
        return if (payload != null) {
            receivedChanges(existing, resolution, payload)
        } else existing
    }

    internal fun channel_data(
        existing: Map<String, Any>?,
        resolution: String,
        content: Map<String, Any>,
    ): Map<String, Any>? {
        // content is a single candle update
        return receivedChange(existing, resolution, content)
    }

    private fun receivedChanges(
        existing: Map<String, Any>?,
        resolution: String,
        payload: List<Any>?,
    ): Map<String, Any>? {
        if (payload != null) {
            val modified = existing?.mutable() ?: mutableMapOf()
            val existingResolution = parser.asNativeList(existing?.get(resolution))
            val candles = mutableListOf<Any>()
            for (value in payload.reversed()) {
                parser.asNativeMap(value)?.let {
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
        existing: Map<String, Any>?,
        resolution: String,
        payload: Map<String, Any>,
    ): Map<String, Any>? {
        if (payload != null) {
            val modified = existing?.mutable() ?: mutableMapOf()
            val existingResolution = parser.asNativeList(existing?.get(resolution))
            val candles = existingResolution?.mutable() ?: mutableListOf()
            val lastExisting = parser.asNativeMap(candles.lastOrNull())
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