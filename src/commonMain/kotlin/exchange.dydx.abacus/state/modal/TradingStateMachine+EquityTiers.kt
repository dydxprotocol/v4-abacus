package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.onChainEquityTiers(payload: String): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    val equityTiers = parser.asMap(json["equityTierLimitConfig"]) ?: return StateChanges(iListOf())
    return receivedOnChainEquityTiers(equityTiers)

}

internal fun TradingStateMachine.receivedOnChainEquityTiers(payload: Map<String, Any>): StateChanges {
    configs = configsProcessor.receivedOnChainEquityTiers(configs, payload)
    return StateChanges(iListOf(Changes.configs))
}
