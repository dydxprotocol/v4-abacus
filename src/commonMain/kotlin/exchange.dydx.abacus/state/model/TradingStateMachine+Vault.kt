package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.codegen.IndexerVaultsHistoricalPnlResponse

internal fun TradingStateMachine.onVaultPnl(
    payload: String
): StateChanges {
    val pnlResponse = parser.asTypedObject<IndexerVaultsHistoricalPnlResponse>(payload)
    if (pnlResponse != null) {
        vaultProcessor.processVaultsHistoricalPnl(internalState.vault, pnlResponse)
        return StateChanges.noChange
    }
    return StateChanges.noChange
}
