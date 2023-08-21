package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.squidChains(payload: String): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    input = squidProcessor.receivedChains(input, json)
    return StateChanges(iListOf(Changes.input))
}

internal fun TradingStateMachine.squidTokens(payload: String): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    input = squidProcessor.receivedTokens(input, json)
    return StateChanges(iListOf(Changes.input))
}

internal fun TradingStateMachine.squidRoute(payload: String): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    input = squidProcessor.receivedRoute(input, json)
    return StateChanges(iListOf(Changes.input))
}

internal fun TradingStateMachine.squidStatus(payload: String): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toIMap()
    transferStatuses = squidProcessor.receivedStatus(transferStatuses, json)
    return StateChanges(iListOf(Changes.transferStatuses))
}