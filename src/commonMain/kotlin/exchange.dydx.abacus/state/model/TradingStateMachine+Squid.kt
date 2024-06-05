package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import kollections.iListOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.routerChains(payload: String): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = routerProcessor.receivedChains(input, json)
        StateChanges(iListOf(Changes.input))
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.routerTokens(payload: String): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = routerProcessor.receivedTokens(input, json)
        StateChanges(iListOf(Changes.input))
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.squidV2SdkInfo(payload: String): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = routerProcessor.receivedV2SdkInfo(input, json)
        StateChanges(iListOf(Changes.input))
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.squidRoute(
    payload: String,
    subaccountNumber: Int,
    requestId: String?,
): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = routerProcessor.receivedRoute(input, json, requestId)
        StateChanges(
            iListOf(Changes.input, Changes.subaccount),
            subaccountNumbers = iListOf(subaccountNumber),
        )
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.squidRouteV2(
    payload: String,
    subaccountNumber: Int,
    requestId: String?
): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = routerProcessor.receivedRouteV2(input, json, requestId)
        StateChanges(
            iListOf(Changes.input, Changes.subaccount),
            subaccountNumbers = iListOf(subaccountNumber),
        )
    } else {
        StateChanges.noChange
    }
}

internal fun TradingStateMachine.squidStatus(
    payload: String,
    transactionId: String?
): StateChanges? {
    val json = Json.parseToJsonElement(payload).jsonObject.toMap()
    transferStatuses = routerProcessor.receivedStatus(transferStatuses, json, transactionId)
    return StateChanges(iListOf(Changes.transferStatuses))
}
