package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.models.chain.OnChainWithdrawalAndTransferGatingStatusResponse
import indexer.models.chain.OnChainWithdrawalCapacityResponse
import kollections.iListOf

internal fun TradingStateMachine.onChainWithdrawalGating(
    payload: String
): StateChanges {
    val response = parser.asTypedObject<OnChainWithdrawalAndTransferGatingStatusResponse>(payload)
    if (response != null) {
        val oldState = internalState.configs.withdrawalGating
        configsProcessor.processWithdrawalGating(
            existing = internalState.configs,
            payload = response,
        )
        return if (internalState.configs.withdrawalGating != oldState) {
            StateChanges(iListOf(Changes.configs, Changes.input))
        } else {
            StateChanges.noChange
        }
    } else {
        return StateChanges.noChange
    }
}

internal fun TradingStateMachine.onChainWithdrawalCapacity(payload: String): StateChanges {
    val response = parser.asTypedObject<OnChainWithdrawalCapacityResponse>(payload)
    if (response != null) {
        val oldState = internalState.configs.withdrawalCapacity
        configsProcessor.processWithdrawalCapacity(
            existing = internalState.configs,
            payload = response,
        )
        return if (internalState.configs.withdrawalCapacity != oldState) {
            StateChanges(iListOf(Changes.configs, Changes.input))
        } else {
            StateChanges.noChange
        }
    } else {
        return StateChanges.noChange
    }
}
