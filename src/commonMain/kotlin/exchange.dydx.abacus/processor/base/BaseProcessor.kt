package exchange.dydx.abacus.processor.base

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIMutableMap
import kotlinx.datetime.Instant

internal enum class ComparisonOrder {
    same,
    ascending,
    descending
}

internal open class BaseProcessor(val parser: ParserProtocol) {
    internal var environment: V4Environment? = null
        set(value) {
            if (field != value) {
                field = value
                environmentChanged()
            }
        }

    internal var accountAddress: String? = null
        set(value) {
            if (field != value) {
                field = value
                accountAddressChanged()
            }
        }

    internal fun transform(
        existing: IMap<String, Any>?,
        input: IMap<*, *>?,
        keymap: IMap<String, IMap<String, String>>
    ): IMutableMap<String, Any> {
        val output = existing?.mutable() ?: iMutableMapOf<String, Any>()
        if (input != null) {
            for ((type, map) in keymap) {
                for ((key, target) in map) {
                    val value: Any? = value(input, key, type)
                    if (value != null) {
                        output[target] = value
                    }
                }
            }
        }
        return output
    }

    internal fun transform(
        existing: IMap<String, Any>?,
        input: IMap<*, *>,
        state: String,
        keymap: IMap<String, IMap<String, String>>
    ): IMutableMap<String, Any> {
        val output = existing?.mutable() ?: iMutableMapOf<String, Any>()
        for ((type, map) in keymap) {
            for ((key, target) in map) {
                val value: Any? = value(input, key, type)
                val existingValue = parser.asMap(existing?.get(value))
                val outputMap = existingValue?.mutable() ?: iMutableMapOf<String, Any>()
                outputMap.safeSet(state, value)
                output[target] = outputMap
            }
        }
        return output
    }

    private fun value(input: IMap<*, *>, key: String, type: String): Any? {
        val result = parser.value(input, key)
        when (type) {
            "string" -> {
                return parser.asString(result)
            }

            "bool" -> {
                return parser.asBool(result)
            }

            "decimal" -> {
                return parser.asDecimal(result)
            }

            "double" -> {
                return parser.asDouble(result)
            }

            "int" -> {
                return parser.asLong(result)
            }

            "datetime" -> {
                return parser.asDatetime(result)
            }

            "strings" -> {
                return parser.asStrings(result)
            }

            else -> {
                return null
            }
        }
    }

    internal fun receivedObject(
        existing: IMap<String, Any>?,
        key: String,
        payload: Any?,
        process: (Any?, payload: Any) -> Any?
    ): IMap<String, Any>? {
        if (payload != null) {
            val modified = existing?.toIMutableMap() ?: iMutableMapOf()
            val map = parser.asMap(payload)
            val transformed = if (map != null) {
                process(parser.asMap(modified[key]), map)
            } else {
                val list = parser.asList(payload)
                if (list != null) {
                    process(modified[key], list)
                } else null
            }
            modified.safeSet(key, transformed)
            return modified
        }
        return existing
    }

    internal open fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>,
        height: Int?,
    ): IMap<String, Any>? {
        return received(existing, payload)
    }

    internal open fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>,
    ): IMap<String, Any>? {
        return existing
    }

    internal open fun received(
        existing: IList<Any>?,
        payload: IList<Any>,
        height: Int?,
    ): IList<Any>? {
        return existing
    }

    internal open fun received(
        existing: IList<Any>?,
        payload: IList<Any>
    ): IList<Any>? {
        return received(existing, payload)
    }

    internal open fun received(
        existing: IList<Any>,
        height: Int?,
    ): Pair<IList<Any>, Boolean> {
        return Pair(existing, false)
    }

    internal open fun received(
        existing: IMap<String, Any>,
        height: Int?,
    ): Pair<IMap<String, Any>, Boolean> {
        return Pair(existing, false)
    }


    internal open fun merge(
        parser: ParserProtocol,
        existing: IList<Any>?,
        incoming: IList<Any>?,
        timeField: String,
        ascending: Boolean = true
    ): IList<Any>? {
        return if (existing != null) {
            if (incoming != null) {
                val lastIncomingTime =
                    parser.asDatetime(parser.asMap(incoming.lastOrNull())?.get(timeField))
                val firstIncomingTime =
                    parser.asDatetime(parser.asMap(incoming.firstOrNull())?.get(timeField))
                val lastExisting =
                    parser.asDatetime(parser.asMap(existing.lastOrNull())?.get(timeField))
                val firstExisting =
                    parser.asDatetime(parser.asMap(existing.firstOrNull())?.get(timeField))
                if (lastIncomingTime != null && firstExisting != null && lastIncomingTime != firstExisting && ascending(
                        lastIncomingTime,
                        firstExisting
                    ) == ascending
                ) {
                    val result = iMutableListOf<Any>()
                    result.addAll(incoming)
                    result.addAll(existing)
                    result
                } else if (firstIncomingTime != null && lastExisting != null && firstIncomingTime != lastExisting && ascending(
                        firstIncomingTime,
                        lastExisting
                    ) != ascending
                ) {
                    val result = iMutableListOf<Any>()
                    result.addAll(existing)
                    result.addAll(incoming)
                    result
                } else {
                    return ParsingHelper.merge(parser, existing, incoming,
                        { obj, data ->
                            val existingTime = parser.asDatetime(parser.asMap(obj)?.get(timeField))
                            val incomingTime = parser.asDatetime(parser.asMap(data)?.get(timeField))
                            ParsingHelper.compare(existingTime, incomingTime, ascending)
                        },
                        { _, _, itemData ->
                            itemData
                        })!!
                }

            } else existing
        } else incoming
    }

    private fun ascending(first: Instant, second: Instant): Boolean {
        return first < second
    }

    internal open fun accountAddressChanged() {
    }

    internal open fun environmentChanged() {
    }
}
