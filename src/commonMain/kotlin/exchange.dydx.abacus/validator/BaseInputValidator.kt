package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.mutable

internal open class BaseInputValidator(
    internal val localizer: LocalizerProtocol?,
    internal val formatter: Formatter?,
    val parser: ParserProtocol,
) {
    private val jsonEncoder = JsonEncoder()
    internal fun required(
        errorCode: String,
        field: String,
        actionStringKey: String,
    ): Map<String, Any> {
        return mapOf(
            "type" to "REQUIRED",
            "code" to errorCode,
            "fields" to listOf(field),
            "resources" to mapOf(
                "action" to mapOf(
                    "stringKey" to actionStringKey
                )
            )
        )
    }

    internal fun error(
        type: String,
        errorCode: String,
        fields: List<String>?,
        actionStringKey: String?,
        titleStringKey: String,
        textStringKey: String,
        textParams: Map<String, Any>? = null,
        action: String? = null,
    ): Map<String, Any> {
        return mapOf(
            "type" to type,
            "code" to errorCode,
            "fields" to fields,
            "action" to action,
            "resources" to mapOf(
                "title" to listOfNotNull(
                    localize(titleStringKey, null)?.let { "localized" to it } ?: run { null },
                    "stringKey" to titleStringKey
                ).toMap(),
                "text" to listOfNotNull(
                    localize(textStringKey, textParams)?.let { "localized" to it } ?: run { null },
                    "stringKey" to textStringKey,
                    "params" to params(parser, textParams)
                ).toMap(),
                "action" to listOfNotNull(
                    localize(actionStringKey, null)?.let { "localized" to it } ?: run { null },
                    "stringKey" to actionStringKey
                ).toMap(),
            )
        ).filterNotNull()
    }

    private fun localize(stringKey: String?, params: Map<String, Any>? = null): String? {
        return if (stringKey == null) {
            null
        } else {
            val parameters = mutableMapOf<String, String>()
            for ((key, value) in params ?: emptyMap()) {
                parser.asNativeMap(value)?.let {
                    formatParam(it)?.let { formattedParam ->
                        parameters[key] = formattedParam
                    }
                }
            }
            localizer?.localize(stringKey, jsonEncoder.encode(parameters))
        }
    }

    private fun formatParam(params: Map<String, Any>): String? {
        val format = parser.asString(params["format"])
        val value = params["value"]
        val tickSize = parser.asString(params["tickSize"])
        return when (format) {
            "string" -> {
                parser.asString(value)
            }

            "price" -> {
                parser.asDouble(value)?.let { amount ->
                    formatter?.price(amount, tickSize)
                } ?: run { null }
            }

            "percent" -> {
                parser.asDouble(value)?.let { amount ->
                    formatter?.percent(amount, 2)
                } ?: run { null }
            }

            "size" -> {
                parser.asDouble(value)?.let { amount ->
                    "$amount"
                } ?: run { null }
            }

            else -> null
        }
    }

    private fun params(
        parser: ParserProtocol, map: Map<String, Any>?,
    ): List<Map<String, Any>>? {
        if (map != null) {
            val params = mutableListOf<Map<String, Any>>()
            for ((key, value) in map) {
                parser.asNativeMap(value)?.let {
                    val param = it.mutable()
                    param["key"] = key
                    params.add(param)
                }
            }
            return params
        }
        return null
    }
}
