package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class ErrorFormat(val rawValue: String) {
    StringVal("string"),
    UsdcPrice("usdcPrice"),
    Price("price"),
    Percent("percent"),
    Size("size");

    companion object {
        operator fun invoke(rawValue: String?) =
            ErrorFormat.entries.firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class ErrorParam(
    val key: String,
    val value: String?,
    val format: ErrorFormat?
)

@JsExport
@Serializable
data class ErrorString(
    val stringKey: String,
    val params: IList<ErrorParam>?,
    val localized: String?
)

@JsExport
@Serializable
data class ErrorResources(
    val title: ErrorString?,
    val text: ErrorString?,
    val action: ErrorString?
)

@JsExport
@Serializable
enum class ErrorType(val rawValue: String) {
    error("ERROR"),
    warning("WARNING"),
    required("REQUIRED");

    companion object {
        operator fun invoke(rawValue: String) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class ErrorAction(val rawValue: String) {
    CONNECT_WALLET("/onboard"),
    DEPOSIT("/deposit");

    companion object {
        operator fun invoke(rawValue: String) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class ValidationError(
    val code: String,
    val type: ErrorType,
    val fields: IList<String>?,
    val action: ErrorAction?,
    val link: String?,
    val linkText: String?,
    val resources: ErrorResources
)
