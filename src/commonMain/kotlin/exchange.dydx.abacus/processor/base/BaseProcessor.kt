package exchange.dydx.abacus.processor.base

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.mutable
import kotlinx.datetime.Instant

internal enum class ComparisonOrder {
    same,
    ascending,
    descending
}

internal interface BaseProcessorProtocol {
    var accountAddress: String?
    var environment: V4Environment?
}

internal open class BaseProcessor(val parser: ParserProtocol) : BaseProcessorProtocol {
    override var environment: V4Environment? = null
        set(value) {
            if (field != value) {
                field = value
                environmentChanged()
            }
        }

    override var accountAddress: String? = null
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

    internal fun <T : Any> merge(
        parser: ParserProtocol,
        existing: List<T>?,
        incoming: List<T>?,
        timeField: (T?) -> Instant?,
        ascending: Boolean = true
    ): List<T>? {
        return if (existing != null) {
            if (incoming != null) {
                val lastIncomingTime = timeField(incoming.lastOrNull())
                val firstIncomingTime = timeField(incoming.firstOrNull())
                val lastExisting = timeField(existing.lastOrNull())
                val firstExisting = timeField(existing.firstOrNull())
                if (lastIncomingTime != null && firstExisting != null && lastIncomingTime != firstExisting &&
                    ascending(
                        lastIncomingTime,
                        firstExisting,
                    ) == ascending
                ) {
                    incoming + existing
                } else if (firstIncomingTime != null && lastExisting != null && firstIncomingTime != lastExisting &&
                    ascending(
                        firstIncomingTime,
                        lastExisting,
                    ) != ascending
                ) {
                    existing + incoming
                } else {
                    return ParsingHelper.mergeTyped(
                        parser = parser,
                        existing = existing,
                        incoming = incoming,
                        comparison = { obj, data ->
                            val existingTime = timeField(obj)
                            val incomingTime = timeField(data)
                            ParsingHelper.compare(existingTime, incomingTime, ascending)
                        },
                        createObject = { _, _, itemData ->
                            itemData
                        },
                    )!!
                }
            } else {
                existing
            }
        } else {
            incoming
        }
    }

    private fun ascending(first: Instant, second: Instant): Boolean {
        return first < second
    }

    internal open fun accountAddressChanged() {
    }

    internal open fun environmentChanged() {
    }
}
