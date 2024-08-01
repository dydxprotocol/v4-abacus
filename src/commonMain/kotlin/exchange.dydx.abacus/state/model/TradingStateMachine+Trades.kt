package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.processor.markets.TradesResponse
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.utils.toJson
import kollections.iListOf

internal fun TradingStateMachine.receivedTrades(
    market: String?,
    payload: Map<String, Any>
): StateChanges? {
    return if (market != null) {
        this.marketsSummary = marketsProcessor.receivedTradesDeprecated(marketsSummary, market, payload)
        if (staticTyping) {
            val marketState = internalState.markets[market]
            val response = parser.asTypedObject<TradesResponse>(payload.toJson())
            val trades = response?.let { tradesProcessorV2.processSubscribed(it) } ?: emptyList()

            if (marketState != null) {
                marketState.trades = trades
            } else {
                internalState.markets[market] = InternalMarketState(trades)
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
            processTradeUpdates(market, payload)
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
        if (staticTyping) {
            payload.mapNotNull { parser.asNativeMap(it) }.forEach { tradeUpdatePayload ->
                processTradeUpdates(market, tradeUpdatePayload)
            }
        }
        StateChanges(iListOf(Changes.trades), iListOf(market))
    } else {
        null
    }
}

private fun TradingStateMachine.processTradeUpdates(
    market: String,
    payload: Map<String, Any>,
) {
    val marketState = internalState.markets[market]
    val response = parser.asTypedObject<TradesResponse>(payload.toJson())
    val trades = response?.let {
        tradesProcessorV2.processChannelData(marketState?.trades, it)
    } ?: emptyList()

    if (marketState != null) {
        marketState.trades = trades
    } else {
        internalState.markets[market] = InternalMarketState(trades)
    }
}
