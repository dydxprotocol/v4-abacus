package exchange.dydx.abacus.processor.base

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
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
        existing: Map<String, Any>?,
        input: Map<*, *>?,
        keymap: Map<String, Map<String, String>>
    ): MutableMap<String, Any> {
        val output = existing?.mutable() ?: mutableMapOf<String, Any>()
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
        existing: Map<String, Any>?,
        input: Map<*, *>,
        state: String,
        keymap: Map<String, Map<String, String>>
    ): MutableMap<String, Any> {
        val output = existing?.mutable() ?: mutableMapOf<String, Any>()
        for ((type, map) in keymap) {
            for ((key, target) in map) {
                val value: Any? = value(input, key, type)
                val existingValue = parser.asNativeMap(existing?.get(value))
                val outputMap = existingValue?.mutable() ?: mutableMapOf<String, Any>()
                outputMap.safeSet(state, value)
                output[target] = outputMap
            }
        }
        return output
    }

    private fun value(input: Map<*, *>, key: String, type: String): Any? {
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
        existing: Map<String, Any>?,
        key: String,
        payload: Any?,
        process: (Any?, payload: Any) -> Any?
    ): Map<String, Any>? {
        if (payload != null) {
            val modified = existing?.toMutableMap() ?: mutableMapOf()
            val map = parser.asNativeMap(payload)
            val transformed = if (map != null) {
                process(parser.asNativeMap(modified[key]), map)
            } else {
                val list = parser.asNativeList(payload)
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
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        return received(existing, payload)
    }

    internal open fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any>? {
        return existing
    }

    internal open fun received(
        existing: List<Any>?,
        payload: List<Any>,
        height: BlockAndTime?,
    ): List<Any>? {
        return existing
    }

    internal open fun received(
        existing: List<Any>?,
        payload: List<Any>
    ): List<Any>? {
        return received(existing, payload)
    }

    internal open fun received(
        existing: List<Any>,
        height: BlockAndTime?,
    ): Pair<List<Any>, Boolean> {
        return Pair(existing, false)
    }

    internal open fun received(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        return Pair(existing, false)
    }


    internal open fun merge(
        parser: ParserProtocol,
        existing: List<Any>?,
        incoming: List<Any>?,
        timeField: String,
        ascending: Boolean = true
    ): List<Any>? {
        return if (existing != null) {
            if (incoming != null) {
                val lastIncomingTime =
                    parser.asDatetime(parser.asNativeMap(incoming.lastOrNull())?.get(timeField))
                val firstIncomingTime =
                    parser.asDatetime(parser.asNativeMap(incoming.firstOrNull())?.get(timeField))
                val lastExisting =
                    parser.asDatetime(parser.asNativeMap(existing.lastOrNull())?.get(timeField))
                val firstExisting =
                    parser.asDatetime(parser.asNativeMap(existing.firstOrNull())?.get(timeField))
                if (lastIncomingTime != null && firstExisting != null && lastIncomingTime != firstExisting && ascending(
                        lastIncomingTime,
                        firstExisting
                    ) == ascending
                ) {
                    val result = mutableListOf<Any>()
                    result.addAll(incoming)
                    result.addAll(existing)
                    result
                } else if (firstIncomingTime != null && lastExisting != null && firstIncomingTime != lastExisting && ascending(
                        firstIncomingTime,
                        lastExisting
                    ) != ascending
                ) {
                    val result = mutableListOf<Any>()
                    result.addAll(existing)
                    result.addAll(incoming)
                    result
                } else {
                    return ParsingHelper.merge(parser, existing, incoming,
                        { obj, data ->
                            val existingTime =
                                parser.asDatetime(parser.asNativeMap(obj)?.get(timeField))
                            val incomingTime =
                                parser.asDatetime(parser.asNativeMap(data)?.get(timeField))
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
