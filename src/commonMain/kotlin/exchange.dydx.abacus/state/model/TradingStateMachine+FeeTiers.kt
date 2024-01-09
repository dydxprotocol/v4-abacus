package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf

internal fun TradingStateMachine.onChainFeeTiers(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    val tiers = parser.asList(parser.value(json, "params.tiers"))
    return if (tiers != null) {
        receivedOnChainFeeTiers(tiers)
    } else {
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.receivedOnChainFeeTiers(payload: List<Any>): StateChanges {
    configs = configsProcessor.receivedOnChainFeeTiers(configs, payload)
    return StateChanges(iListOf(Changes.configs))
}
