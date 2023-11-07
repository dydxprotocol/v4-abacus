package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf

internal fun TradingStateMachine.onChainEquityTiers(payload: String): StateChanges {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        val equityTiers =
            parser.asMap(json["equityTierLimitConfig"]) ?: return StateChanges(iListOf())
        receivedOnChainEquityTiers(equityTiers)
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.receivedOnChainEquityTiers(payload: Map<String, Any>): StateChanges {
    configs = configsProcessor.receivedOnChainEquityTiers(configs, payload)
    return StateChanges(iListOf(Changes.configs))
}
