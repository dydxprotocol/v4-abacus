package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerCandleResponse
import indexer.codegen.IndexerCandleResponseObject
import kotlinx.datetime.Instant

internal class CandlesProcessor(
    parser: ParserProtocol,
    private val itemProcessor: CandleProcessorProtocol = CandleProcessor(parser = parser),
) : BaseProcessor(parser) {

    fun processSubscribed(
        existing: InternalMarketState,
        resolution: String,
        payload: IndexerCandleResponse?
    ): InternalMarketState {
        if (payload != null && (payload.candles?.size ?: 0) > 0) {
            val candles = payload.candles?.reversed()?.mapNotNull {
                itemProcessor.process(it)
            }
            val merged = merge(
                parser = parser,
                existing = existing.candles?.get(resolution),
                incoming = candles,
                timeField = {
                    val startAt = it?.startedAtMilliseconds?.toLong()
                    if (startAt != null) Instant.fromEpochMilliseconds(startAt) else null
                },
                ascending = true,
            )
            if (merged != null) {
                val modified = existing.candles ?: mutableMapOf()
                modified[resolution] = merged
                existing.candles = modified
            }
        }
        return existing
    }

    fun processBatchUpdate(
        existing: InternalMarketState,
        resolution: String,
        payload: List<IndexerCandleResponseObject>?
    ): InternalMarketState {
        if (!payload.isNullOrEmpty()) {
            val candles = payload.reversed().mapNotNull {
                itemProcessor.process(it)
            }
            val merged = merge(
                parser = parser,
                existing = existing.candles?.get(resolution),
                incoming = candles,
                timeField = {
                    val startAt = it?.startedAtMilliseconds?.toLong()
                    if (startAt != null) Instant.fromEpochMilliseconds(startAt) else null
                },
                ascending = true,
            )
            if (merged != null) {
                val modified = existing.candles ?: mutableMapOf()
                modified[resolution] = merged
                existing.candles = modified
            }
        }
        return existing
    }

    fun processUpdate(
        existing: InternalMarketState,
        resolution: String,
        payload: IndexerCandleResponseObject?
    ): InternalMarketState {
        if (payload != null) {
            val candle = itemProcessor.process(payload)
            if (candle != null) {
                val modified = existing.candles ?: mutableMapOf()
                val existingResolution = modified[resolution]?.toMutableList() ?: mutableListOf()
                val lastExisting = existingResolution.lastOrNull()
                val lastStartAt = lastExisting?.startedAtMilliseconds
                val incomingStartAt = candle.startedAtMilliseconds
                if (lastStartAt == incomingStartAt) {
                    existingResolution.removeLast()
                }
                existingResolution.add(candle)
                modified[resolution] = existingResolution
                existing.candles = modified
            }
        }
        return existing
    }

    override fun received(
        existing: List<Any>?,
        payload: List<Any>
    ): List<Any>? {
        return mergeDeprecated(
            parser,
            existing,
            parser.asNativeList(payload)?.reversed()?.toList(),
            "startedAt",
            true,
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
        } else {
            existing
        }
    }

    @Suppress("FunctionName")
    internal fun channel_data(
        existing: Map<String, Any>?,
        resolution: String,
        content: Map<String, Any>,
    ): Map<String, Any>? {
        // content is a single candle update
        if (content != null) {
            val modified = existing?.mutable() ?: mutableMapOf()
            val existingResolution = parser.asNativeList(existing?.get(resolution))
            val candles = existingResolution?.mutable() ?: mutableListOf()
            val lastExisting = parser.asNativeMap(candles.lastOrNull())
            val lastStartAt = parser.asDatetime(lastExisting?.get("startedAt"))
            val itemProcessor = itemProcessor as CandleProcessor
            val incoming = itemProcessor.received(null, content)
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
                    val candleProcessor = itemProcessor as CandleProcessor
                    val candle = candleProcessor.received(null, it)
                    candles.add(candle)
                }
            }
            val mergedResolution = mergeDeprecated(
                parser,
                existingResolution,
                candles,
                "startedAt",
                true,
            )
            modified.safeSet(resolution, mergedResolution)
            return modified
        } else {
            return existing
        }
    }
}
