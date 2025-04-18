package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.InternalVaultState
import exchange.dydx.abacus.state.StateChanges
import indexer.codegen.IndexerMegavaultHistoricalPnlResponse
import indexer.codegen.IndexerMegavaultPositionResponse
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.codegen.IndexerVaultsHistoricalPnlResponse
import indexer.models.chain.OnChainAccountVaultResponse
import kollections.iListOf

internal fun TradingStateMachine.onMegaVaultPnl(
    payloads: Array<String>
): StateChanges {
    val responses = payloads.mapNotNull { parser.asTypedObject<IndexerMegavaultHistoricalPnlResponse>(it) }
    val newState = vaultProcessor.processMegaVaultsHistoricalPnl(internalState.vault, responses)
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

internal fun TradingStateMachine.onVaultTransferHistory(
    payload: String
): StateChanges {
    val transferBetweenResponse = parser.asTypedObject<IndexerTransferBetweenResponse>(payload)
    val newState = vaultProcessor.processTransferBetween(internalState.vault, transferBetweenResponse)
    return updateVaultState(internalState, newState)
}

internal fun TradingStateMachine.onAccountOwnerShares(
    payload: String
): StateChanges {
    val accountVaultResponse = parser.asTypedObject<OnChainAccountVaultResponse>(payload)
    return onAccountOwnerShares(accountVaultResponse)
}

internal fun TradingStateMachine.onAccountOwnerShares(
    response: OnChainAccountVaultResponse?
): StateChanges {
    val newState = vaultProcessor.processAccountOwnerShares(internalState.vault, response)
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
