package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.Logger
import kollections.iEmptyList
import kollections.iListOf
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray

internal fun TradingStateMachine.feeDiscounts(payload: String): StateChanges {
    val json = try {
        Json.parseToJsonElement(payload).jsonArray.toList()
    } catch (exception: SerializationException) {
        Logger.e {
            "Failed to deserialize feeDiscounts: $payload \n" +
                "Exception: $exception"
        }
        return StateChanges(iEmptyList())
    } catch (exception: IllegalArgumentException) { // .jsonArray exception
        Logger.e {
            "Failed to deserialize feeDiscounts: $payload \n" +
                "Exception: $exception"
        }
        return StateChanges(iEmptyList())
    }
    configs = configsProcessor.receivedFeeDiscounts(configs, json)
    return StateChanges(iListOf(Changes.configs))
}
