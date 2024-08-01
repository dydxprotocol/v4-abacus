package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import kollections.iListOf

internal fun TradingStateMachine.receivedTrades(
    market: String?,
    payload: Map<String, Any>
): StateChanges? {
    return if (market != null) {
        this.marketsSummary = marketsProcessor.receivedTradesDeprecated(marketsSummary, market, payload)
        if (staticTyping) {
            val marketState = internalState.marketsSummary.markets[market]
            val trades = tradesProcessorV2.processSubscribed(payload)
            if (marketState != null) {
                marketState.trades = trades
            } else {
                internalState.marketsSummary.markets[market] = InternalMarketState(trades)
            }
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
        this.marketsSummary = marketsProcessor.receivedTradesChangesDeprecated(marketsSummary, market, payload)
        if (staticTyping) {
            val marketState = internalState.marketsSummary.markets[market]
            val trades = tradesProcessorV2.processChannelData(marketState?.trades, payload)
            if (marketState != null) {
                marketState.trades = trades
            } else {
                internalState.marketsSummary.markets[market] = InternalMarketState(trades)
            }
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
        this.marketsSummary = marketsProcessor.receivedBatchedTradesChanges(marketsSummary, market, payload)
        StateChanges(iListOf(Changes.trades), iListOf(market))
    } else {
        null
    }
}
