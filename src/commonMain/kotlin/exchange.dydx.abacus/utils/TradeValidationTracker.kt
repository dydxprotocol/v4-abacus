package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.TrackingProtocol
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class TradeValidationTracker(
    private val trackingProtocol: TrackingProtocol?
) {

    private var lastSeen: TradeValidationPayload? = null

    fun logValidationResult(payload: TradeValidationPayload) {
        // if (payload == lastSeen) return

		// try {
		// 	val serializedPayload = TrackingJson.encodeToString(TradeValidationPayload.serializer(), payload)
		// 	trackingProtocol?.log(AnalyticsEvent.TradeValidation.name, serializedPayload)
		// 	lastSeen = payload
		// } catch (e: SerializationException) {
		// 	// Handle serialization-specific errors
		// 	Logger.e { "Serialization error: ${e.message}" }
		// } catch (e: Exception) {
		// 	// Handle any other exceptions that may occur
        //     Logger.e  { "An error occurred while logging TradeValidation: ${e.message}" }
		// }
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
