package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IList
import kollections.iListOf
import kollections.toIList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.feeTiers(payload: String): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonArray.toIList()
    return receivedFeeTiers(json)
}

internal fun TradingStateMachine.receivedFeeTiers(payload: IList<Any>): StateChanges {
    configs = configsProcessor.receivedFeeTiers(configs, payload)
    return StateChanges(iListOf(Changes.configs))
}

internal fun TradingStateMachine.onChainFeeTiers(payload: String): StateChanges {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    val tiers = parser.asList(parser.value(json, "params.tiers"))
    return if (tiers != null) {
        receivedOnChainFeeTiers(tiers)
    } else {
        StateChanges(iListOf())
    }
}

internal fun TradingStateMachine.receivedOnChainFeeTiers(payload: IList<Any>): StateChanges {
    configs = configsProcessor.receivedOnChainFeeTiers(configs, payload)
    return StateChanges(iListOf(Changes.configs))
}
