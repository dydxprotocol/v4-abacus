package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketState
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
            val candles = payload.mapNotNull {
                itemProcessor.process(it)
            }.sortedBy {
                it.startedAtMilliseconds
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
}
