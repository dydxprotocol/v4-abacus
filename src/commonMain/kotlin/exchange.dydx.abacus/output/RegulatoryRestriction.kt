package exchange.dydx.abacus.output

import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
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
data class UsageRestriction(
    val restriction: Restriction,
    val displayError: ParsingError?
) {
    companion object {
        internal val userRestriction: UsageRestriction = UsageRestriction(
            Restriction.USER_RESTRICTED,
            ParsingError(
                ParsingErrorType.UserRestricted,
                "User restricted",
                "ERROR.USER_RESTRICTED",
                null,
            ),
        )

        internal val userRestrictionUnknown: UsageRestriction = UsageRestriction(
            Restriction.USER_RESTRICTION_UNKNOWN,
            null,
        )

        internal val noRestriction: UsageRestriction = UsageRestriction(
            Restriction.NO_RESTRICTION,
            null,
        )

        internal val http403Restriction: UsageRestriction = UsageRestriction(
            Restriction.GEO_RESTRICTED,
            ParsingError(
                ParsingErrorType.HttpError403,
                "Indexer restricted",
                "ERROR.HTTPCODE_403",
                null,
            ),
        )
    }
}
