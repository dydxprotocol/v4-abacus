package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.codegen.IndexerHistoricalPnlResponse
import kollections.iListOf

internal fun TradingStateMachine.historicalPnl(payload: String, subaccountNumber: Int): StateChanges {
    if (staticTyping) {
        val response = parser.asTypedObject<IndexerHistoricalPnlResponse>(payload)
        if (response != null && response.historicalPnl.isNullOrEmpty().not()) {
            walletProcessor.processHistoricalPnls(
                existing = internalState.wallet,
                payload = response.historicalPnl?.toList(),
                subaccountNumber = subaccountNumber,
            )
            return StateChanges(iListOf(Changes.historicalPnl), null, iListOf(subaccountNumber))
        } else {
            return StateChanges(iListOf<Changes>())
        }
    } else {
        val json = parser.decodeJsonObject(payload)
        return if (json != null) {
            receivedHistoricalPnlsDeprecated(json, subaccountNumber)
        } else {
            StateChanges.noChange
        }
    }
}

private fun TradingStateMachine.receivedHistoricalPnlsDeprecated(payload: Map<String, Any>, subaccountNumber: Int): StateChanges {
    val size = parser.asList(payload["historicalPnl"])?.size ?: 0
    return if (size > 0) {
        wallet = walletProcessor.receivedHistoricalPnlsDeprecated(wallet, payload, subaccountNumber)
        StateChanges(iListOf(Changes.historicalPnl), null, iListOf(subaccountNumber))
    } else {
        StateChanges(iListOf<Changes>())
    }
}
