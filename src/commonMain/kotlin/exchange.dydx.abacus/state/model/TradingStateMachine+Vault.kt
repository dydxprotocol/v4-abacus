package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalVaultState
import indexer.codegen.IndexerMegavaultHistoricalPnlResponse
import indexer.codegen.IndexerMegavaultPositionResponse
import indexer.codegen.IndexerVaultsHistoricalPnlResponse
import kollections.iListOf

internal fun TradingStateMachine.onMegaVaultPnl(
    payload: String
): StateChanges {
    val pnlResponse = parser.asTypedObject<IndexerMegavaultHistoricalPnlResponse>(payload)
    val newState = vaultProcessor.processMegaVaultsHistoricalPnl(internalState.vault, pnlResponse)
    return updateVaultState(internalState, newState)
}

internal fun TradingStateMachine.onVaultMarketPnls(
    payload: String
): StateChanges {
    val pnlResponse = parser.asTypedObject<IndexerVaultsHistoricalPnlResponse>(payload)
    val newState = vaultProcessor.processVaultMarketHistoricalPnls(internalState.vault, pnlResponse)
    return updateVaultState(internalState, newState)
}

internal fun TradingStateMachine.onVaultMarketPositions(
    payload: String
): StateChanges {
    val positionResponse = parser.asTypedObject<IndexerMegavaultPositionResponse>(payload)
    val newState = vaultProcessor.processVaultMarketPositions(internalState.vault, positionResponse)
    return updateVaultState(internalState, newState)
}

private fun updateVaultState(
    state: InternalState,
    newVaultState: InternalVaultState?
): StateChanges {
    if (newVaultState != state.vault) {
        state.vault = newVaultState
        return StateChanges(changes = iListOf(Changes.vault))
    }
    return StateChanges.noChange
}
