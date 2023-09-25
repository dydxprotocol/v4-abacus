package exchange.dydx.abacus.output

import exchange.dydx.abacus.responses.ParsingError
import kollections.JsExport
import kotlinx.serialization.Serializable

@kotlin.js.JsExport
@Serializable
enum class Restriction(val rawValue: Int) {
    NO_RESTRICTION(0),
    GEO_RESTRICTED(1),
    USER_RESTRICTED(2),
    USER_RESTRICTION_UNKNOWN(3),
    ;

    companion object {
        operator fun invoke(rawValue: Int?) =
            Restriction.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class RegulatoryRestriction(
    val restriction: RegulatoryRestriction,
    val displayError: ParsingError?
)