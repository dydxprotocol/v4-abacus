package exchange.dydx.abacus.responses

import exchange.dydx.abacus.output.FeeDiscountResources
import exchange.dydx.abacus.output.RegulatoryRestriction
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IMap
import kotlinx.serialization.Serializable
import kollections.JsExport

@JsExport
@Serializable
enum class ParsingErrorType(val rawValue: String) {
    ParsingError("Parsing Error"),
    UnhandledEndpoint("Unhandled Endpoint"),
    UnknownChannel("Unknown Channel"),
    MissingChannel("Missing Channel"),
    MissingContent("Missing Content"),
    InvalidInput("Invalid Input"),
    MissingRequiredData("Missing Data"),
    InvalidUrl("Invalid Url"),
    Unhandled("Unhandled"),
    BackendError("Backend Error"),
    HttpError403("Http Error 403"),
    UserRestricted("User Restricted"),
    ;

    companion object {
        operator fun invoke(rawValue: String?) =
            ParsingErrorType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class ParsingError(
    val type: ParsingErrorType,
    val message: String,
    val stringKey: String? = null,
    val stackTrace: String? = null
)

class ParsingException(
    val type: ParsingErrorType,
    message: String,
    val throwable: Throwable? = null
) : Exception(message, throwable) {
    fun toParsingError(): ParsingError {
        val stackTrace = throwable?.stackTraceToString()
        return ParsingError(type = type, message = message ?: "null", stackTrace = stackTrace)
    }
}
