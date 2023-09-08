package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.onChainRewardsParams(payload: String): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    configs = configsProcessor.receivedRewardsParams(configs, json)
    return StateChanges(iListOf(Changes.configs))
}
