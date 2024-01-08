package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf

internal fun TradingStateMachine.receivedTrades(
    market: String?,
    payload: Map<String, Any>
): StateChanges? {
    return if (market != null) {
        this.marketsSummary = marketsProcessor.receivedTrades(marketsSummary, market, payload)
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
        this.marketsSummary = marketsProcessor.receivedTradesChanges(marketsSummary, market, payload)
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
        this.marketsSummary = marketsProcessor.receivedBatchedTradesChanges(marketsSummary, market, payload)
        StateChanges(iListOf(Changes.trades), iListOf(market))
    } else {
        null
    }
}
