package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.utils.toJson
import indexer.codegen.IndexerTradeResponse
import kollections.iListOf

internal fun TradingStateMachine.receivedTrades(
    market: String?,
    payload: Map<String, Any>
): StateChanges? {
    return if (market != null) {
        val response = parser.asTypedObject<IndexerTradeResponse>(payload.toJson())
        marketsProcessor.processTradesSubscribed(
            existing = internalState.marketsSummary,
            marketId = market,
            content = response,
        )

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
        val response = parser.asTypedObject<IndexerTradeResponse>(payload.toJson())
        marketsProcessor.processTradesUpdates(
            existing = internalState.marketsSummary,
            marketId = market,
            content = response,
        )
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
        payload.mapNotNull { parser.asNativeMap(it) }.forEach { tradeUpdatePayload ->
            val response = parser.asTypedObject<IndexerTradeResponse>(tradeUpdatePayload.toJson())
            marketsProcessor.processTradesUpdates(
                existing = internalState.marketsSummary,
                marketId = market,
                content = response,
            )
        }
        StateChanges(iListOf(Changes.trades), iListOf(market))
    } else {
        null
    }
}
