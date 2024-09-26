package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import indexer.models.chain.OnChainFeeTiersResponse
import kollections.iListOf

internal fun TradingStateMachine.onChainFeeTiers(payload: String): StateChanges {
    if (staticTyping) {
        val response = parser.asTypedObject<OnChainFeeTiersResponse>(payload)
        val oldState = internalState.configs.feeTiers
        configsProcessor.processOnChainFeeTiers(
            existing = internalState.configs,
            payload = response,
        )
        return if (internalState.configs.feeTiers != oldState) {
            StateChanges(iListOf(Changes.configs))
        } else {
            StateChanges(iListOf())
        }
    } else {
        val json = parser.decodeJsonObject(payload)
        val tiers = parser.asList(parser.value(json, "params.tiers"))
        return if (tiers != null) {
            configs = configsProcessor.receivedOnChainFeeTiersDeprecated(configs, tiers)
            return StateChanges(iListOf(Changes.configs))
        } else {
            StateChanges(iListOf())
        }
    }
}
