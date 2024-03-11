package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf

internal fun TradingStateMachine.historicalPnl(payload: String, subaccountNumber: Int): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        receivedHistoricalPnls(json, subaccountNumber)
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.receivedHistoricalPnls(payload: Map<String, Any>, subaccountNumber: Int): StateChanges {
    val size = parser.asList(payload["historicalPnl"])?.size ?: 0
    return if (size > 0) {
        wallet = walletProcessor.receivedHistoricalPnls(wallet, payload, subaccountNumber)
        StateChanges(iListOf(Changes.historicalPnl), null, iListOf(subaccountNumber))
    } else {
        StateChanges(iListOf<Changes>())
    }
}
