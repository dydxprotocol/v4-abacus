package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.toJson
import indexer.codegen.IndexerTradeResponse
import kollections.iListOf

internal fun TradingStateMachine.receivedTrades(
    market: String?,
    payload: Map<String, Any>
): StateChanges? {
    return if (market != null) {
        if (staticTyping) {
            val response = parser.asTypedObject<IndexerTradeResponse>(payload.toJson())
            marketsProcessor.processTradesSubscribed(
                existing = internalState.marketsSummary,
                marketId = market,
                content = response,
            )
        } else {
            this.marketsSummary = marketsProcessor.receivedTradesDeprecated(marketsSummary, market, payload)
        }
        StateChanges(iListOf(Changes.trades), iListOf(market))
    } else {
        null
    }
}

internal fun TradingStateMachine.receivedTradesChanges(
    market: String?,
    payload: Map<String, Any>
): StateChanges? {
    return if (market != null) {
        if (staticTyping) {
            val response = parser.asTypedObject<IndexerTradeResponse>(payload.toJson())
            marketsProcessor.processTradesUpdates(
                existing = internalState.marketsSummary,
                marketId = market,
                content = response,
            )
        } else {
            this.marketsSummary = marketsProcessor.receivedTradesChangesDeprecated(marketsSummary, market, payload)
        }
        StateChanges(iListOf(Changes.trades), iListOf(market))
    } else {
        null
    }
}

internal fun TradingStateMachine.receivedBatchedTradesChanges(
    market: String?,
    payload: List<Any>
): StateChanges? {
    return if (market != null) {
        if (staticTyping) {
            payload.mapNotNull { parser.asNativeMap(it) }.forEach { tradeUpdatePayload ->
                val response = parser.asTypedObject<IndexerTradeResponse>(tradeUpdatePayload.toJson())
                marketsProcessor.processTradesUpdates(
                    existing = internalState.marketsSummary,
                    marketId = market,
                    content = response,
                )
            }
        } else {
            this.marketsSummary = marketsProcessor.receivedBatchedTradesChanges(marketsSummary, market, payload)
        }
        StateChanges(iListOf(Changes.trades), iListOf(market))
    } else {
        null
    }
}
