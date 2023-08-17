package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.iListOf

internal fun TradingStateMachine.receivedTrades(
    market: String?,
    payload: IMap<String, Any>
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
    payload: IMap<String, Any>
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
    payload: IList<Any>
): StateChanges? {
    return if (market != null) {
        this.marketsSummary = marketsProcessor.receivedBatchedTradesChanges(marketsSummary, market, payload)
        StateChanges(iListOf(Changes.trades), iListOf(market))
    } else {
        null
    }
}
