package exchange.dydx.abacus.utils

import kollections.toIMap
import kollections.toIMutableMap

typealias IMap<K, V> = kollections.Map<K, V>
typealias IMutableMap<K, V> = kollections.MutableMap<K, V>

internal fun <K, V> iMapOf(vararg pairs: Pair<K, V>): IMap<K, V> {
    val map = mapOf(pairs = pairs)
    return map.toIMap()
}

internal fun <K, V> mutableMapOf(vararg pairs: Pair<K, V>): IMutableMap<K, V> {
    val map = mapOf(pairs = pairs)
    return map.toIMutableMap()
}

internal fun <K, V> kotlin.collections.Map<K, V>.mutable(): MutableMap<K, V> {
    return this as? MutableMap<K, V> ?: this.toMutableMap()
}

internal fun <K, V> IMap<K, V>.mutable(): IMutableMap<K, V> {
    return this as? IMutableMap<K, V> ?: this.toIMutableMap()
}

internal fun <T> kotlin.collections.MutableMap<String, T>.typedSafeSet(key: String, value: T?) {
    if (value != null) {
        set(key, value)
    } else {
        remove(key)
    }
}

internal fun <T> IMutableMap<String, T>.typedSafeSet(key: String, value: T?) {
    if (value != null) {
        set(key, value)
    } else {
        remove(key)
    }
}

internal fun kotlin.collections.MutableMap<String, Any>.safeSet(key: String, value: Any?) {
    this.safeSet(key.split("."), value)
}

internal fun IMutableMap<String, Any>.safeSet(key: String, value: Any?) {
    this.safeSet(key.split("."), value)
}

internal fun kotlin.collections.Map<String, Any>.modify(key: String, value: Any?): Map<String, Any> {
    val modified = this.mutable()
    modified.safeSet(key, value)
    return modified
}

internal fun IMap<String, Any>.modify(key: String, value: Any?): IMap<String, Any> {
    val modified = this.mutable()
    modified.safeSet(key, value)
    return modified
}

fun <T> IMap<String, T>.values(): Collection<T> = this.toMap().values

private fun kotlin.collections.MutableMap<String, Any>.safeSet(route: List<String>, value: Any?) {
    when (route.size) {
        0 -> {}
        1 -> {
            if (value != null) {
                set(route.first(), value)
            } else {
                remove(route.first())
            }
        }

        else -> {
            val key = route.first()
            val rest = route.toMutableList()
            rest.removeFirst()
            val existingValue = get(key)
            val existing = (existingValue as? Map<String, Any>)
            if (value != null) {
                val modified = existing?.toMutableMap() ?: mutableMapOf()
                modified.safeSet(rest, value)
                set(key, modified)
            } else {
                if (existing != null) {
                    val modified = existing.toMutableMap()
                    modified.safeSet(rest, value)
                    set(key, modified)
                }
            }
        }
    }
}

private fun IMutableMap<String, Any>.safeSet(route: List<String>, value: Any?) {
    when (route.size) {
        0 -> {}
        1 -> {
            if (value != null) {
                set(route.first(), value)
            } else {
                remove(route.first())
            }
        }

        else -> {
            val key = route.first()
            val rest = route.toMutableList()
            rest.removeFirst()
            val existingValue = get(key)
            val existing = (
                (existingValue as? IMap<*, *>)
                    ?: (existingValue as? Map<*, *>)?.toIMap()
                ) as? IMap<String, Any>
            if (value != null) {
                val modified = existing?.toIMutableMap() ?: mutableMapOf()
                modified.safeSet(rest, value)
                set(key, modified)
            } else {
                if (existing != null) {
                    val modified = existing.toIMutableMap()
                    modified.safeSet(rest, value)
                    set(key, modified)
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<out K?, V?>.filterNotNull(): Map<K, V> =
    (this.toMap().filter { it.key != null && it.value != null } as Map<K, V>)

@Suppress("UNCHECKED_CAST")
fun <K, V> IMap<out K?, V?>.filterNotNull(): IMap<K, V> =
    (this.toMap().filter { it.key != null && it.value != null } as Map<K, V>).toIMap()

fun IMap<String, String>.toUrlParams(): String =
    entries.joinToString("&") {
        it.key + "=" + it.value
    }

fun Map<String, Any>.toCamelCaseKeys(): Map<String, Any> {
    return this.mapKeys { it.key.toCamelCase() }.mapValues { (_, value) ->
        when (value) {
            is Map<*, *> -> (value as Map<String, Any>).toCamelCaseKeys()
            is List<*> -> value.map {
                if (it is Map<*, *>) (it as Map<String, Any>).toCamelCaseKeys() else it
            }
            else -> value
        }
    }
}
