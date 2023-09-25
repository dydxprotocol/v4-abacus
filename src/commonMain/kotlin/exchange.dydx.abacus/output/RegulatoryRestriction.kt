package exchange.dydx.abacus.output

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IMap
import kollections.JsExport
import kotlinx.serialization.Serializable

@kotlin.js.JsExport
@Serializable
enum class Restriction(val rawValue: String?) {
    NO_RESTRICTION("NO_RESTRICTION"),
    GEO_RESTRICTED("GEO_RESTRICTED"),
    USER_RESTRICTED("USER_RESTRICTED"),
    USER_RESTRICTION_UNKNOWN("USER_RESTRICTION_UNKNOWN"),
    ;

    companion object {
        operator fun invoke(rawValue: String?) =
            Restriction.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class RegulatoryRestriction(
    val restriction: Restriction,
    val displayError: ParsingError?
) {
    companion object {
        internal val userRestriction: RegulatoryRestriction = RegulatoryRestriction(
            Restriction.USER_RESTRICTED,
            ParsingError(
                ParsingErrorType.UserRestricted,
                "User restricted",
                "ERROR.USER_RESTRICTED",
                null
            )
        )

        internal val userRestrictionUnknown: RegulatoryRestriction = RegulatoryRestriction(
            Restriction.USER_RESTRICTION_UNKNOWN,
            null
        )

        internal val noRestriction: RegulatoryRestriction = RegulatoryRestriction(
            Restriction.NO_RESTRICTION,
            null
        )

        internal val http403Restriction: RegulatoryRestriction = RegulatoryRestriction(
            Restriction.GEO_RESTRICTED,
            ParsingError(
                ParsingErrorType.HttpError403,
                "Indexer restricted",
                "ERROR.HTTPCODE_403",
                null
            )
        )
    }
}