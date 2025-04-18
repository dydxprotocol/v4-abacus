package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.models.chain.OnChainFeeTiersResponse
import kollections.iListOf

internal fun TradingStateMachine.onChainFeeTiers(payload: String): StateChanges {
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
}
