package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.codegen.IndexerMegavaultHistoricalPnlResponse
import indexer.codegen.IndexerMegavaultPositionResponse
import indexer.codegen.IndexerVaultsHistoricalPnlResponse
import kollections.iListOf

internal fun TradingStateMachine.onMegaVaultPnl(
    payload: String
): StateChanges {
    val pnlResponse = parser.asTypedObject<IndexerMegavaultHistoricalPnlResponse>(payload)
    if (pnlResponse != null) {
        internalState.vault = vaultProcessor.processMegaVaultsHistoricalPnl(internalState.vault, pnlResponse)
        return StateChanges(changes = iListOf(Changes.vault))
    }
    return StateChanges.noChange
}

internal fun TradingStateMachine.onVaultMarketPnls(
    payload: String
): StateChanges {
    val pnlResponse = parser.asTypedObject<IndexerVaultsHistoricalPnlResponse>(payload)
    if (pnlResponse != null) {
        internalState.vault = vaultProcessor.processVaultMarketHistoricalPnls(internalState.vault, pnlResponse)
        return StateChanges(changes = iListOf(Changes.vault))
    }
    return StateChanges.noChange
}

internal fun TradingStateMachine.onVaultMarketPositions(
    payload: String
): StateChanges {
    val pnlResponse = parser.asTypedObject<IndexerMegavaultPositionResponse>(payload)
    if (pnlResponse != null) {
        internalState.vault = vaultProcessor.processVaultMarketPositions(internalState.vault, pnlResponse)
        return StateChanges(changes = iListOf(Changes.vault))
    }
    return StateChanges.noChange
}
