package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
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
        routerProcessor.receivedEvmSwapVenues(json)
    }
}

// DO-LATER: https://linear.app/dydx/issue/OTE-350/%5Babacus%5D-cleanup
internal fun TradingStateMachine.routerRoute(
    payload: String,
    subaccountNumber: Int,
    requestId: String?,
    goFast: Boolean,
): StateChanges? {
    val json = parser.decodeJsonObject(payload)
    return if (json != null) {
        input = routerProcessor.receivedRoute(input, json, requestId, goFast)
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
    hash: String,
    payload: String,
): StateChanges? {
    val json = try {
        Json.parseToJsonElement(payload).jsonObject.toMap()
    } catch (exception: SerializationException) {
        Logger.e { "Failed to deserialize skipTrack: $payload \nException: $exception" }
        return StateChanges(iEmptyList())
    }
    trackStatuses = routerProcessor.receivedTrack(hash, trackStatuses, json)
    return StateChanges(iListOf(Changes.trackStatuses))
}
