package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.squidChains(payload: String): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = squidProcessor.receivedChains(input, json)
        StateChanges(iListOf(Changes.input))
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.squidTokens(payload: String): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = squidProcessor.receivedTokens(input, json)
        StateChanges(iListOf(Changes.input))
    } else StateChanges.noChange
}

internal fun TradingStateMachine.squidV2SdkInfo(payload: String): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = squidProcessor.receivedV2SdkInfo(input, json)
        StateChanges(iListOf(Changes.input))
    } else StateChanges.noChange
}

internal fun TradingStateMachine.squidRoute(payload: String, subaccountNumber: Int): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = squidProcessor.receivedRoute(input, json)
        StateChanges(
            iListOf(Changes.input, Changes.subaccount),
            subaccountNumbers = iListOf(subaccountNumber)
        )
    } else StateChanges.noChange
}

internal fun TradingStateMachine.squidRouteV2(payload: String, subaccountNumber: Int): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = squidProcessor.receivedRouteV2(input, json)
        StateChanges(
            iListOf(Changes.input, Changes.subaccount),
            subaccountNumbers = iListOf(subaccountNumber)
        )
    } else StateChanges.noChange
}


internal fun TradingStateMachine.squidStatus(payload: String, transactionId: String?): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    transferStatuses = squidProcessor.receivedStatus(transferStatuses, json, transactionId)
    return StateChanges(iListOf(Changes.transferStatuses))
}
