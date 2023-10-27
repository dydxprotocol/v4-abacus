package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.squidChains(payload: String): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    input = squidProcessor.receivedChains(input, json)
    return StateChanges(iListOf(Changes.input))
}

internal fun TradingStateMachine.squidTokens(payload: String): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    input = squidProcessor.receivedTokens(input, json)
    return StateChanges(iListOf(Changes.input))
}

internal fun TradingStateMachine.squidV2SdkInfo(payload: String): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    input = squidProcessor.receivedV2SdkInfo(input, json)
    return StateChanges(iListOf(Changes.input))
}

internal fun TradingStateMachine.squidRoute(payload: String, subaccountNumber: Int): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    input = squidProcessor.receivedRoute(input, json)
    return StateChanges(iListOf(Changes.input, Changes.subaccount), subaccountNumbers = iListOf(subaccountNumber))
}

internal fun TradingStateMachine.squidStatus(payload: String, transactionId: String?): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    transferStatuses = squidProcessor.receivedStatus(transferStatuses, json, transactionId)
    return StateChanges(iListOf(Changes.transferStatuses))
}
