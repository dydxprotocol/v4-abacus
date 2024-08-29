package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.TrackingProtocol
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TradeValidationTracker(
    private val trackingProtocol: TrackingProtocol?
) {

    private var lastSeen: TradeValidationPayload? = null

    fun logValidationResult(payload: TradeValidationPayload) {
        if (payload == lastSeen) return

        trackingProtocol?.log(AnalyticsEvent.TradeValidation.name, TrackingJson.encodeToString(TradeValidationPayload.serializer(), payload))
        lastSeen = payload
    }
}

private val TrackingJson = Json {
    prettyPrint = true
    encodeDefaults = true
}

@Serializable
data class TradeValidationPayload(
    val errors: List<String> = emptyList(),
    val marketId: String,
    val size: Double? = null,
    val notionalSize: Double? = null,
) {
    // Fields declared in class body are still serialized, but are not used for data class equality.
    // Basically, don't want to emit more events if only slippage has changed (via orderbook)
    var indexSlippage: Double? = null
    var orderbookSlippage: Double? = null
}
