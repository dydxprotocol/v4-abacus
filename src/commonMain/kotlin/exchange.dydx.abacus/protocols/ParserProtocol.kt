package exchange.dydx.abacus.protocols
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.toJson
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

interface ParserProtocol {
    // parse a field to string
    fun asString(data: Any?): String?

    // parse a field to string
    fun asStrings(data: Any?): IList<String>?

    // parse  a field to double
    fun asDouble(data: Any?): Double?

    // parse  a field to BigDecimal
    fun asDecimal(data: Any?): BigDecimal?

    // parse  a field to int
    fun asInt(data: Any?): Int?

    // parse  a field to int
    fun asLong(data: Any?): Long?

    // parse  a field to boolean
    fun asBool(data: Any?): Boolean?

    // parse a field to a date int
    fun asDatetime(data: Any?): Instant?

    // parse a field to a map
    fun asMap(data: Any?): IMap<String, Any>?

    fun asNativeMap(data: Any?): Map<String, Any>?

    // parse to a list
    fun asList(data: Any?): IList<Any>?

    fun asNativeList(data: Any?): List<Any>?

    fun value(data: Any?, path: String): Any?

    fun decodeJsonObject(text: String?): IMap<String, Any>?
    fun decodeJsonArray(text: String?): IList<Any>?
}

private val jsonCoder = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

internal inline fun <reified T> ParserProtocol.asTypedList(list: Any?): List<T>? {
    val payload = asNativeList(list) ?: return null
    return payload.mapNotNull { item ->
        if (item is T) {
            item
        } else {
            val itemString: String? = asString(item)
            if (itemString != null) {
                if (itemString is T) {
                    return@mapNotNull itemString
                }
                try {
                    jsonCoder.decodeFromString<T>(itemString)
                } catch (e: SerializationException) {
                    val className = (T::class).simpleName
                    Logger.e { "Failed to parse item: $item as $className: ${e.message}" }
                    null
                } catch (e: IllegalArgumentException) {
                    val className = (T::class).simpleName
                    Logger.e { "Failed to parse item: $item as $className: ${e.message}" }
                    null
                }
            } else {
                null
            }
        }
    }
}

internal inline fun <reified T> ParserProtocol.asTypedObject(item: Any?): T? {
    if (item is T) {
        return item
    }
    val itemString = if (item is Map<*, *>) {
        item.toJson()
    } else {
        asString(item)
    }
    return if (itemString != null) {
        if (itemString is T) {
            itemString
        }
        try {
            jsonCoder.decodeFromString<T>(itemString)
        } catch (e: SerializationException) {
            val className = (T::class).simpleName
            Logger.e { "Failed to parse item: $item as $className: ${e.message}\"" }
            null
        } catch (e: IllegalArgumentException) {
            val className = (T::class).simpleName
            Logger.e { "Failed to parse item: $item as $className: ${e.message}\"" }
            null
        }
    } else {
        null
    }
}

internal inline fun <reified T> ParserProtocol.asTypedStringMap(payload: Map<String, Any>?): Map<String, T>? {
    if (payload == null) {
        return null
    }
    val result = mutableMapOf<String, T>()
    for ((key, value) in payload) {
        val typedValue = asTypedObject<T>(value)
        if (typedValue != null) {
            result[key] = typedValue
        }
    }
    return result
}

internal inline fun <reified T> ParserProtocol.asTypedStringMapOfList(payload: Map<String, List<T>>?): Map<String, List<T>>? {
    if (payload == null) {
        return null
    }
    val result = mutableMapOf<String, List<T>>()
    for ((key, value) in payload) {
        val typedValue = asTypedList<T>(value)
        if (typedValue != null) {
            result[key] = typedValue
        }
    }
    return result
}
