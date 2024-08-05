package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.codegen.IndexerOrderbookResponseObject
import kollections.iListOf

internal fun TradingStateMachine.receivedOrderbook(
    market: String?,
    payload: Map<String, Any>,
    subaccountNumber: Int
): StateChanges? {
    return if (market != null) {
        if (staticTyping) {
            val orderbookPayload = parser.asTypedObject<IndexerOrderbookResponseObject>(payload)
            print("orderbookPayload: $orderbookPayload")
        } else {
            this.marketsSummary =
                marketsProcessor.receivedOrderbookDeprecated(marketsSummary, market, payload)
        }
        StateChanges(iListOf(Changes.orderbook, Changes.input), iListOf(market), iListOf(subaccountNumber))
    } else {
        null
    }
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
    market: String?,
    payload: List<Any>,
    subaccountNumber: Int
): StateChanges? {
    return if (market != null) {
        this.marketsSummary = marketsProcessor.receivedBatchOrderbookChanges(marketsSummary, market, payload)
        StateChanges(iListOf(Changes.orderbook, Changes.input), iListOf(market), iListOf(subaccountNumber))
    } else {
        null
    }
}

internal fun TradingStateMachine.setOrderbookGrouping(market: String?, groupingMultiplier: Int): StateResponse {
    return if (this.groupingMultiplier != groupingMultiplier) {
        this.groupingMultiplier = groupingMultiplier
        this.marketsSummary = marketsProcessor.groupOrderbook(marketsSummary, market)

        val changes =
            StateChanges(iListOf(Changes.orderbook), if (market != null) iListOf(market) else null, null)

        changes.let {
            update(it)
        }
        return StateResponse(state, changes, null)
    } else {
        StateResponse(state, null, null)
    }
}
