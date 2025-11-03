package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import indexer.models.chain.OnChainFeeDiscountsResponse
import kollections.iListOf

internal fun TradingStateMachine.onChainFeeDiscounts(payload: String): StateChanges {
    val response = parser.asTypedObject<OnChainFeeDiscountsResponse>(payload)
    val oldState = internalState.configs.allMarketFeeDiscounts
    configsProcessor.processOnChainFeeDiscounts(
        existing = internalState.configs,
        payload = response,
    )
    return if (internalState.configs.allMarketFeeDiscounts != oldState) {
        StateChanges(iListOf(Changes.configs))
    } else {
        StateChanges(iListOf())
    }
}
