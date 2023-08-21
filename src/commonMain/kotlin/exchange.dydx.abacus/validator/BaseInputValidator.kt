package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import kollections.iListOf
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIMap

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
    ): IMap<String, Any> {
        return iMapOf(
            "type" to "REQUIRED",
            "code" to errorCode,
            "fields" to iListOf(field),
            "resources" to iMapOf(
                "action" to iMapOf(
                    "stringKey" to actionStringKey
                )
            )
        )
    }

    internal fun error(
        type: String,
        errorCode: String,
        fields: IList<String>?,
        actionStringKey: String?,
        titleStringKey: String,
        textStringKey: String,
        textParams: IMap<String, Any>? = null,
        action: String? = null,
    ): IMap<String, Any> {
        return iMapOf(
            "type" to type,
            "code" to errorCode,
            "fields" to fields,
            "action" to action,
            "resources" to iMapOf(
                "title" to listOfNotNull(
                    localize(titleStringKey, null)?.let { "localized" to it } ?: run { null },
                    "stringKey" to titleStringKey
                ).toMap().toIMap(),
                "text" to listOfNotNull(
                    localize(textStringKey, textParams)?.let { "localized" to it } ?: run { null },
                    "stringKey" to textStringKey,
                    "params" to params(parser, textParams)
                ).toMap().toIMap(),
                "action" to listOfNotNull(
                    localize(actionStringKey, null)?.let { "localized" to it } ?: run { null },
                    "stringKey" to actionStringKey
                ).toMap().toIMap()
            )
        ).filterNotNull()
    }

    private fun localize(stringKey: String?, params: IMap<String, Any>? = null): String? {
        return if (stringKey == null) {
            null
        } else {
            val parameters = iMutableMapOf<String, String>()
            for ((key, value) in params ?: emptyMap()) {
                parser.asMap(value)?.let {
                    formatParam(it)?.let { formattedParam ->
                        parameters[key] = formattedParam
                    }
                }
            }
            localizer?.localize(stringKey, jsonEncoder.encode(parameters))
        }
    }

    private fun formatParam(params: IMap<String, Any>): String? {
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
        parser: ParserProtocol, map: IMap<String, Any>?,
    ): IList<IMap<String, Any>>? {
        if (map != null) {
            val params = iMutableListOf<IMap<String, Any>>()
            for ((key, value) in map) {
                parser.asMap(value)?.let {
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
