package exchange.dydx.abacus.output

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
enum class ComplianceStatus {
    COMPLIANT,
    FIRST_STRIKE,
    FIRST_STRIKE_CLOSE_ONLY,
    CLOSE_ONLY,
    BLOCKED,
    UNKNOWN;
}

@JsExport
@Serializable
enum class ComplianceAction {
    CONNECT,
    VALID_SURVEY,
    INVALID_SURVEY;
}

@JsExport
@Serializable
data class Compliance(
    val geo: String?,
    val status: ComplianceStatus,
    val updatedAt: String?,
    val expiresAt: String?,
)
