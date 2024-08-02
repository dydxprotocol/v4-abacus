package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.models.chain.OnChainEquityTiersResponse
import kollections.iListOf

internal fun TradingStateMachine.onChainEquityTiers(payload: String): StateChanges {
    if (staticTyping) {
        val response = parser.asTypedObject<OnChainEquityTiersResponse>(payload)
        return if (response != null) {
            val oldConfigs = internalState.configs.equityTiers
            configsProcessor.processOnChainEquityTiers(
                existing = internalState.configs,
                payload = response,
            )
            return if (internalState.configs.equityTiers != oldConfigs) {
                StateChanges(iListOf(Changes.configs))
            } else {
                StateChanges.noChange
            }
        } else {
            StateChanges.noChange
        }
    } else {
        val json = parser.decodeJsonObject(payload)
        return if (json != null) {
            val equityTiers =
                parser.asMap(json["equityTierLimitConfig"]) ?: return StateChanges(iListOf())
            configs = configsProcessor.receivedOnChainEquityTiersDeprecated(configs, equityTiers)
            return StateChanges(iListOf(Changes.configs))
        } else {
            StateChanges.noChange
        }
    }
}
