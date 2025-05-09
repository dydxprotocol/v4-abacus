package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedList
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.codegen.IndexerOrderbookResponseObject
import indexer.models.IndexerWsOrderbookUpdateResponse
import kollections.iListOf

internal fun TradingStateMachine.receivedOrderbook(
    marketId: String?,
    payload: Map<String, Any>,
    subaccountNumber: Int
): StateChanges? {
    if (marketId == null) {
        return null
    }
    val orderbookPayload = parser.asTypedObject<IndexerOrderbookResponseObject>(payload)
    val market = internalState.marketsSummary.markets[marketId]
    marketsProcessor.processOrderbook(
        existing = internalState.marketsSummary,
        marketId = marketId,
        tickSize = market?.perpetualMarket?.configs?.tickSize,
        content = orderbookPayload,
    )

    return StateChanges(
        iListOf(Changes.orderbook, Changes.input),
        iListOf(marketId),
        iListOf(subaccountNumber),
    )
}

internal fun TradingStateMachine.receivedOrderbookChanges(
    market: String?,
    payload: Map<String, Any>,
    subaccountNumber: Int
): StateChanges? {
    // To do: marketsProcessor needs a receivedOrderbookChanges function
    return if (market != null) {
        StateChanges(iListOf(Changes.orderbook, Changes.input), iListOf(market), iListOf(subaccountNumber))
    } else {
        null
    }
}

internal fun TradingStateMachine.receivedBatchOrderbookChanges(
    marketId: String?,
    payload: List<Any>,
    subaccountNumber: Int
): StateChanges? {
    if (marketId == null) {
        return null
    }
    val orderbookUpdatePayload = parser.asTypedList<IndexerWsOrderbookUpdateResponse>(payload)
    val market = internalState.marketsSummary.markets[marketId]
    marketsProcessor.processBatchOrderbookChanges(
        existing = internalState.marketsSummary,
        tickSize = market?.perpetualMarket?.configs?.tickSize,
        marketId = marketId,
        content = orderbookUpdatePayload,
    )

    return StateChanges(
        iListOf(Changes.orderbook, Changes.input),
        iListOf(marketId),
        iListOf(subaccountNumber),
    )
}

internal fun TradingStateMachine.setOrderbookGrouping(
    marketId: String,
    groupingMultiplier: Int,
): StateResponse {
    return if (this.groupingMultiplier != groupingMultiplier) {
        val market = internalState.marketsSummary.markets[marketId]
        marketsProcessor.groupOrderbook(
            existing = internalState.marketsSummary,
            tickSize = market?.perpetualMarket?.configs?.tickSize,
            marketId = marketId,
            groupingMultiplier = groupingMultiplier,
        )

        val changes =
            StateChanges(
                iListOf(Changes.orderbook),
                iListOf(marketId),
                null,
            )

        changes.let {
            updateStateChanges(it)
        }
        return StateResponse(state, changes, null)
    } else {
        StateResponse(state, null, null)
    }
}
