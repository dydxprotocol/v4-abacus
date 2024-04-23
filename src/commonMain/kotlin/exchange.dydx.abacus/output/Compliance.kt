package exchange.dydx.abacus.output

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
enum class ComplianceStatus(val rawValue: String?) {
    COMPLIANT("COMPLIANT"),
    FIRST_STRIKE("FIRST_STRIKE"),
    CLOSE_ONLY("CLOSE_ONLY"),
    BLOCKED("BLOCKED"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        operator fun invoke(rawValue: String?) =
            ComplianceStatus.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class Compliance(
    val geo: String?,
    val status: ComplianceStatus,
)
