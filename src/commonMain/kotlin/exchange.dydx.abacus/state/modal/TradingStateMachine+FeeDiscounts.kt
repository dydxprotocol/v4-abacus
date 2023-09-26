package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray

internal fun TradingStateMachine.feeDiscounts(payload: String): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonArray.toList()
    return receivedFeeDiscounts(json)
}

internal fun TradingStateMachine.receivedFeeDiscounts(payload: List<Any>): StateChanges {
    configs = configsProcessor.receivedFeeDiscounts(configs, payload)
    return StateChanges(iListOf(Changes.configs))
}
