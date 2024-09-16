package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kollections.iMutableListOf
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
            ErrorFormat.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class ErrorParam(
    val key: String,
    val value: String?,
    val format: ErrorFormat?
) {
    companion object {
        internal fun create(
            existing: ErrorParam?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): ErrorParam? {
            Logger.d { "creating Error Param\n" }

            data?.let {
                parser.asString(data["key"])?.let { key ->
                    val value = data["value"]
                    val format = parser.asString(data["format"])?.let {
                        ErrorFormat.invoke(it)
                    }
                    return if (existing?.key != key ||
                        existing.value != value ||
                        existing.format != format
                    ) {
                        ErrorParam(
                            key,
                            value.toString(),
                            format,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Error Param not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class ErrorString(
    val stringKey: String,
    val params: IList<ErrorParam>?,
    val localized: String?
) {
    companion object {
        internal fun create(
            existing: ErrorString?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): ErrorString? {
            Logger.d { "creating Error String\n" }

            data?.let {
                parser.asString(data["stringKey"])?.let { stringKey ->
                    var params: IMutableList<ErrorParam>? = null

                    parser.asList(data["params"])?.let { data ->
                        params = iMutableListOf()
                        for (i in data.indices) {
                            val item = data[i]
                            ErrorParam.create(
                                existing?.params?.getOrNull(i),
                                parser,
                                parser.asMap(item),
                            )?.let { param ->
                                params?.add(param)
                            }
                        }
                    }
                    val localized = parser.asString(data["localized"])
                    return if (existing?.stringKey != stringKey ||
                        existing.params != params ||
                        existing.localized != localized
                    ) {
                        ErrorString(stringKey, params, localized)
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Error String not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class ErrorResources(
    val title: ErrorString?,
    val text: ErrorString?,
    val action: ErrorString?
) {
    companion object {
        internal fun create(
            existing: ErrorResources?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): ErrorResources? {
            Logger.d { "creating Error Resources\n" }

            data?.let {
                val title = ErrorString.create(existing?.title, parser, parser.asMap(data["title"]))
                val text = ErrorString.create(existing?.text, parser, parser.asMap(data["text"]))
                val action =
                    ErrorString.create(existing?.action, parser, parser.asMap(data["action"]))
                if (title != null || text != null || action != null) {
                    return if (existing?.title !== title ||
                        existing?.text !== text ||
                        existing?.action !== action
                    ) {
                        ErrorResources(title, text, action)
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Error Resources not valid" }
            return null
        }
    }
}

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
) {
    companion object {
        internal fun create(
            existing: IList<ValidationError>?,
            parser: ParserProtocol,
            data: List<Any>?
        ): IList<ValidationError>? {
            Logger.d { "creating Validation Errors\n" }
            return if (data != null) {
                val errors = iMutableListOf<ValidationError>()
                for (i in data.indices) {
                    parser.asMap(data[i])?.let {
                        create(existing?.getOrNull(i), parser, it)?.let {
                            errors.add(it)
                        }
                    }
                }
                errors
            } else {
                null
            }
        }

        internal fun create(
            existing: ValidationError?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): ValidationError? {
            Logger.d { "creating Validation Error\n" }

            data?.let {
                parser.asString(data["code"])?.let { code ->
                    parser.asString(data["type"])?.let {
                        ErrorType.invoke(it)?.let { type ->
                            ErrorResources.create(
                                existing?.resources,
                                parser,
                                parser.asMap(data["resources"]),
                            )
                                ?.let { resources ->
                                    var fields: IMutableList<String>? = null
                                    parser.asList(data["fields"])?.let { data ->
                                        fields = iMutableListOf()
                                        for (item in data) {
                                            parser.asString(item)?.let {
                                                fields?.add(it)
                                            }
                                        }
                                    }
                                    val actionString = parser.asString(data["action"])
                                    val action =
                                        if (actionString != null) ErrorAction.invoke(actionString) else null
                                    val link = parser.asString(data["link"])
                                    val linkText = parser.asString(data["linkText"])
                                    return if (existing?.code != code ||
                                        existing.type !== type ||
                                        existing.fields != fields ||
                                        existing.resources !== resources
                                    ) {
                                        ValidationError(
                                            code,
                                            type,
                                            fields,
                                            action,
                                            link,
                                            linkText,
                                            resources,
                                        )
                                    } else {
                                        existing
                                    }
                                }
                        }
                    }
                }
            }
            Logger.d { "Validation Error not valid" }
            return null
        }
    }
}
