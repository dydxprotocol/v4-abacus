package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedList
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.codegen.IndexerCandleResponse
import indexer.codegen.IndexerCandleResponseObject
import kollections.iListOf
import kollections.toIList
import kotlinx.serialization.json.JsonNull.content

// Called in test code only
internal fun TradingStateMachine.candles(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedCandles(json)
    } else {
        StateChanges(iListOf<Changes>())
    }
}

private fun TradingStateMachine.receivedCandles(payload: Map<String, Any>): StateChanges {
    val markets = parser.asMap(payload["candles"])
    val marketIds = if (markets != null) {
        markets.keys.toIList()
    } else {
        val marketId = parser.asString(parser.value(payload, "candles.0.market"))
            ?: parser.asString(parser.value(payload, "candles.0.ticker"))
        if (marketId != null) iListOf(marketId) else null
    }
    return if (marketIds != null) {
        val list = parser.asList(payload["candles"])
        val size = list?.size ?: 0
        if (size > 0) {
            if (staticTyping) {
                for (marketId in marketIds) {
                    val candlesPayload =
                        parser.asTypedList<IndexerCandleResponseObject>(list)
                    val resolution = candlesPayload?.firstOrNull()?.resolution
                    if (resolution != null) {
                        marketsProcessor.processBatchCandlesChanges(
                            existing = internalState.marketsSummary,
                            marketId = marketId,
                            resolution = resolution.value,
                            content = candlesPayload,
                        )
                    }
                }
            } else {
                marketsSummary = marketsProcessor.receivedCandles(marketsSummary, payload)
            }
            StateChanges(iListOf(Changes.candles), marketIds)
        } else {
            val size = parser.asMap(payload["candles"])?.size ?: 0
            if (size > 0) {
                if (staticTyping) {
                    for (marketId in marketIds) {
                        val candlesPayload =
                            parser.asTypedList<IndexerCandleResponseObject>(markets?.get(marketId))
                        val resolution = candlesPayload?.firstOrNull()?.resolution
                        if (resolution != null) {
                            marketsProcessor.processBatchCandlesChanges(
                                existing = internalState.marketsSummary,
                                marketId = marketId,
                                resolution = resolution.value,
                                content = candlesPayload,
                            )
                        }
                    }
                } else {
                    marketsSummary = marketsProcessor.receivedCandles(marketsSummary, payload)
                }
                StateChanges(iListOf(Changes.candles), marketIds)
            } else {
                StateChanges(iListOf())
            }
        }
    } else {
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.receivedCandles(
    marketId: String,
    resolution: String,
    payload: Map<String, Any>
): StateChanges {
    if (staticTyping) {
        val candlesPayload = parser.asTypedObject<IndexerCandleResponse>(payload)
        marketsProcessor.processCandles(
            existing = internalState.marketsSummary,
            marketId = marketId,
            resolution = resolution,
            content = candlesPayload,
        )
    } else {
        this.marketsSummary =
            marketsProcessor.receivedCandlesDeprecated(marketsSummary, marketId, resolution, payload)
    }
    return StateChanges(iListOf(Changes.candles), iListOf(marketId))
}

internal fun TradingStateMachine.receivedCandlesChanges(
    market: String,
    resolution: String,
    payload: Map<String, Any>
): StateChanges {
    if (staticTyping) {
        val candlesPayload = parser.asTypedObject<IndexerCandleResponseObject>(payload)
        marketsProcessor.processCandlesChanges(
            existing = internalState.marketsSummary,
            marketId = market,
            resolution = resolution,
            content = candlesPayload,
        )
    } else {
        this.marketsSummary =
            marketsProcessor.receivedCandlesChanges(marketsSummary, market, resolution, payload)
    }
    return StateChanges(iListOf(Changes.candles), iListOf(market))
}

internal fun TradingStateMachine.receivedBatchedCandlesChanges(
    market: String,
    resolution: String,
    payload: List<Any>
): StateChanges {
    if (staticTyping) {
        val candlesPayload = parser.asTypedList<IndexerCandleResponseObject>(payload)
        marketsProcessor.processBatchCandlesChanges(
            existing = internalState.marketsSummary,
            marketId = market,
            resolution = resolution,
            content = candlesPayload,
        )
    } else {
        this.marketsSummary =
            marketsProcessor.receivedBatchedCandlesChanges(marketsSummary, market, resolution, payload)
    }
    return StateChanges(iListOf(Changes.candles), iListOf(market))
}
