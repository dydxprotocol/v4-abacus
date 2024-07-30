package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.models.chain.OnChainWithdrawalAndTransferGatingStatusResponse
import indexer.models.chain.OnChainWithdrawalCapacityResponse
import kollections.iListOf

fun TradingStateMachine.onChainWithdrawalGating(
    payload: String
): StateChanges {
    if (staticTyping) {
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
    } else {
        val json = parser.decodeJsonObject(payload)
        return json?.let {
            configs = configsProcessor.receivedWithdrawalGatingDeprecated(configs, it)
            return StateChanges(iListOf(Changes.configs, Changes.input))
        } ?: StateChanges.noChange
    }
}

fun TradingStateMachine.onChainWithdrawalCapacity(payload: String): StateChanges {
    if (staticTyping) {
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
    } else {
        val json = parser.decodeJsonObject(payload)
        return json?.let {
            configs = configsProcessor.receivedWithdrawalCapacityDeprecated(configs, it)
            return StateChanges(iListOf(Changes.configs, Changes.input))
        } ?: StateChanges.noChange
    }
}
