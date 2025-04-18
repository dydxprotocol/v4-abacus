package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.codegen.IndexerHistoricalPnlResponse
import kollections.iListOf

internal fun TradingStateMachine.historicalPnl(payload: String, subaccountNumber: Int): StateChanges {
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
}
