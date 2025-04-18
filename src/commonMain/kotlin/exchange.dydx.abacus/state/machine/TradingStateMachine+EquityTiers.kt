package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.models.chain.OnChainEquityTiersResponse
import kollections.iListOf

internal fun TradingStateMachine.onChainEquityTiers(payload: String): StateChanges {
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
}
