package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IList
import kollections.iListOf
import kollections.toIList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray

internal fun TradingStateMachine.feeDiscounts(payload: String): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonArray.toIList()
    return receivedFeeDiscounts(json)
}

internal fun TradingStateMachine.receivedFeeDiscounts(payload: IList<Any>): StateChanges {
    configs = configsProcessor.receivedFeeDiscounts(configs, payload)
    return StateChanges(iListOf(Changes.configs))
}
