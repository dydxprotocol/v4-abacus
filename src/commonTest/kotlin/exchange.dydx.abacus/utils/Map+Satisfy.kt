package exchange.dydx.abacus.utils

import exchange.dydx.abacus.protocols.ParserProtocol
import kotlinx.serialization.json.JsonNull
import tickDecimals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Suppress("UNCHECKED_CAST")
internal fun Map<String, Any>.satisfies(
    expected: Map<String, Any>,
    parser: ParserProtocol,
    path: String? = null
) {
    val key = path ?: "root"
    for ((key, value) in expected) {
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

private fun Map<String, Any>.satisfies(
    actual: Any,
    expected: Any,
    parser: ParserProtocol,
    path: String
) {
    val expectedMap = parser.asMap(expected)
    if (expectedMap != null) {
        satisfiesMaps(parser.asMap(actual), expectedMap, parser, path)
        return
    }
    val expectedList = parser.asList(expected)
    if (expectedList != null) {
        satisfiesLists(parser.asList(actual), expectedList, parser, path)
        return
    }
    if (actual is JsonNull) {
        if (expected is JsonNull) {
            return
        }
    }
    satsifiesValues(actual, expected, parser, path)
}

private fun Map<String, Any>.satisfiesMaps(
    map1: Map<String, Any>?,
    map2: Map<String, Any>,
    parser: ParserProtocol,
    path: String
) {
    assertNotNull(map1, "$path should be a map")
    map1.satisfies(map2, parser, path)
}

private fun Map<String, Any>.satisfiesLists(
    actual: List<Any>?,
    expected: List<Any>,
    parser: ParserProtocol,
    path: String
) {
    assertNotNull(actual, "$path should be a list")
    if (actual.size < expected.size) {
        assertFails { "$path should have a length of at least ${expected.size}" }
    }
    for (i in 0 until expected.size) {
        val item1 = actual[i]
        val item2 = expected[i]
        satisfies(item1, item2, parser, combine(path, i.toString()))
    }
}

private fun Map<String, Any>.satsifiesValues(
    actual: Any,
    expected: Any,
    parser: ParserProtocol,
    path: String
) {
    parser.asDouble(expected)?.let {
        val value = parser.asDouble(actual)
        if (value != null) {
            // No need to check, tickDecimals is always a BigDecimal and asDouble will always return a Double
            val stepSize = parser.asDouble(it.tickDecimals())!!
            val roundedValue = Rounder.round(value, stepSize, Rounder.RoundingMode.NEAREST)
            if (it != roundedValue) {
                val x = 0
                val roundedValue = Rounder.round(value, stepSize, Rounder.RoundingMode.NEAREST)
            }
            assertEquals(it, roundedValue, "$path should have a value of $it")
            return
        } else {
            assertFails { "$path should have a value of $it" }
        }
    }
    parser.asInt(expected)?.let {
        assertEquals(it, parser.asInt(actual), "$path should have a value of $it")
        return
    }
    parser.asBool(expected)?.let {
        assertEquals(it, parser.asBool(actual), "$path should have a value of $it")
        return
    }
    parser.asDatetime(expected)?.let {
        assertEquals(it, parser.asDatetime(actual), "$path should have a value of $it")
        return
    }
    parser.asString(expected)?.let {
        assertEquals(it, parser.asString(actual), "$path should have a value of $it")
        return
    }
    assertFails { "$path data type unknown" }
}
