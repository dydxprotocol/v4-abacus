package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.ParserProtocol
import kotlinx.serialization.json.JsonNull
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Suppress("UNCHECKED_CAST")
internal fun IMap<String, Any>.satisfies(
    map: IMap<String, Any>?,
    parser: ParserProtocol,
    path: String? = null
) {
    val key = path ?: "root"
    assertNotNull(map, "$key should not be null")
    for ((key, value) in map) {
        val nodePath = combine(path, key)
        val myValue = get(key)
        if (value is JsonNull) {
            assertNull(myValue, "$nodePath should be null")
        } else {
            assertNotNull(myValue, "$nodePath should not be null")
            satisfies(myValue, value, parser, nodePath)
        }
    }
}

private fun combine(path: String?, key: String): String {
    return if (path != null) "$path.$key" else key
}

private fun IMap<String, Any>.satisfies(
    item1: Any,
    item2: Any,
    parser: ParserProtocol,
    path: String
) {
    val map2 = parser.asMap(item2)
    if (map2 != null) {
        satisfiesMaps(parser.asMap(item1), map2, parser, path)
        return
    }
    val list2 = parser.asList(item2)
    if (list2 != null) {
        satisfiesLists(parser.asList(item1), list2, parser, path)
        return
    }
    if (item1 is JsonNull) {
        if (item2 is JsonNull) {
            return
        }
    }
    satsifiesValues(item1, item2, parser, path)
}

private fun IMap<String, Any>.satisfiesMaps(
    map1: IMap<String, Any>?,
    map2: IMap<String, Any>,
    parser: ParserProtocol,
    path: String
) {
    assertNotNull(map1, "$path should be a map")
    map1.satisfies(map2, parser, path)
}

private fun IMap<String, Any>.satisfiesLists(
    list1: IList<Any>?,
    list2: IList<Any>,
    parser: ParserProtocol,
    path: String
) {
    assertNotNull(list1, "$path should be a list")
    if (list1.size < list2.size) {
        assertFails { "$path should have a length of at least ${list2.size}" }
    }
    for (i in 0 until list2.size) {
        val item1 = list1[i]
        val item2 = list2[i]
        satisfies(item1, item2, parser, combine(path, i.toString()))
    }
}

private fun IMap<String, Any>.satsifiesValues(
    value1: Any,
    value2: Any?,
    parser: ParserProtocol,
    path: String
) {
    parser.asDouble(value1)?.let {
        assertEquals(parser.asDouble(value2), it, "$path should have a value of $it")
        return
    }
    parser.asInt(value1)?.let {
        assertEquals(parser.asInt(value2), it, "$path should have a value of $it")
        return
    }
    parser.asBool(value1)?.let {
        assertEquals(parser.asBool(value2), it, "$path should have a value of $it")
        return
    }
    parser.asDatetime(value1)?.let {
        assertEquals(parser.asDatetime(value2), it, "$path should have a value of $it")
        return
    }
    parser.asString(value1)?.let {
        assertEquals(parser.asString(value2), it, "$path should have a value of $it")
        return
    }
    assertFails { "$path data type unknown" }
}
