package exchange.dydx.abacus.validator

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.output.input.ErrorAction
import exchange.dydx.abacus.output.input.ErrorFormat
import exchange.dydx.abacus.output.input.ErrorParam
import exchange.dydx.abacus.output.input.ErrorResources
import exchange.dydx.abacus.output.input.ErrorString
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.utils.JsonEncoder
import kollections.iListOf
import kollections.toIList

internal open class BaseInputValidator(
    internal val localizer: LocalizerProtocol?,
    internal val formatter: Formatter?,
    val parser: ParserProtocol,
) {
    private val jsonEncoder = JsonEncoder()

    fun required(
        errorCode: String,
        field: String,
        actionStringKey: String,
    ): ValidationError {
        return ValidationError(
            code = errorCode,
            type = ErrorType.required,
            fields = iListOf(field),
            action = null,
            link = null,
            linkText = null,
            resources = ErrorResources(
                title = null,
                text = null,
                action = ErrorString(
                    stringKey = actionStringKey,
                    params = null,
                    localized = null,
                ),
            ),
        )
    }

    fun error(
        type: ErrorType,
        errorCode: String,
        fields: List<String>?,
        actionStringKey: String?,
        titleStringKey: String,
        textStringKey: String,
        textParams: Map<String, Any>? = null,
        action: ErrorAction? = null,
        link: String? = null,
        linkText: String? = null,
    ): ValidationError {
        return ValidationError(
            code = errorCode,
            type = type,
            fields = fields?.toIList(),
            action = action,
            link = link,
            linkText = linkText,
            resources = ErrorResources(
                title = ErrorString(
                    stringKey = titleStringKey,
                    params = null,
                    localized = localize(titleStringKey),
                ),
                text = ErrorString(
                    stringKey = textStringKey,
                    params = params(parser, textParams)?.toIList(),
                    localized = localize(textStringKey, textParams),
                ),
                action = if (actionStringKey != null) {
                    ErrorString(
                        stringKey = actionStringKey,
                        params = null,
                        localized = null,
                    )
                } else {
                    null
                },
            ),
        )
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
        val format = ErrorFormat.invoke(parser.asString(params["format"]))
        val value = params["value"]
        val tickSize = parser.asString(params["tickSize"])
        return when (format) {
            ErrorFormat.StringVal -> {
                parser.asString(value)
            }

            ErrorFormat.UsdcPrice -> {
                parser.asDouble(value)?.let { amount ->
                    formatter?.price(amount, "0.01")
                } ?: run { null }
            }

            ErrorFormat.Price -> {
                parser.asDouble(value)?.let { amount ->
                    formatter?.price(amount, tickSize)
                } ?: run { null }
            }

            ErrorFormat.Percent -> {
                parser.asDouble(value)?.let { amount ->
                    formatter?.percent(amount, 2)
                } ?: run { null }
            }

            ErrorFormat.Size -> {
                parser.asDouble(value)?.toBigDecimal()?.toPlainString() ?: run { null }
            }

            else -> null
        }
    }

    private fun params(
        parser: ParserProtocol,
        map: Map<String, Any>?,
    ): List<ErrorParam>? {
        if (map != null) {
            val params = mutableListOf<ErrorParam>()
            for ((key, value) in map) {
                parser.asNativeMap(value)?.let {
                    val param = ErrorParam(
                        key = key,
                        value = parser.asString(it["value"]),
                        format = ErrorFormat.invoke(parser.asString(it["format"])),
                    )
                    params.add(param)
                }
            }
            return params
        }
        return null
    }
}
