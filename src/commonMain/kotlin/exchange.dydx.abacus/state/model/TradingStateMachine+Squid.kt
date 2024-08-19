package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.Logger
import kollections.iEmptyList
import kollections.iListOf
import kotlinx.serialization.SerializationException
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

internal fun TradingStateMachine.evmSwapVenues(payload: String) {
    val json = parser.decodeJsonObject(payload)
    if (json != null) {
        routerProcessor.receivedEvmSwapVenues(input, json)
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

// DO-LATER: https://linear.app/dydx/issue/OTE-350/%5Babacus%5D-cleanup
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

internal fun TradingStateMachine.routerStatus(
    payload: String,
    transactionId: String?
): StateChanges {
    val json = try {
        Json.parseToJsonElement(payload).jsonObject.toMap()
    } catch (exception: SerializationException) {
        Logger.e { "Failed to deserialize squidStatus: $payload \nException: $exception" }
        return StateChanges(iEmptyList())
    }
    transferStatuses = routerProcessor.receivedStatus(transferStatuses, json, transactionId)
    return StateChanges(iListOf(Changes.transferStatuses))
}

internal fun TradingStateMachine.routerTrack(
    payload: String,
): StateChanges? {
    val json = try {
        Json.parseToJsonElement(payload).jsonObject.toMap()
    } catch (exception: SerializationException) {
        Logger.e { "Failed to deserialize skipTrack: $payload \nException: $exception" }
        return StateChanges(iEmptyList())
    }
    trackStatuses = routerProcessor.receivedTrack(trackStatuses, json)
    return StateChanges(iListOf(Changes.trackStatuses))
}
