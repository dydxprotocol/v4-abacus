package exchange.dydx.abacus.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

class JsonEncoder {
    private val parser = Parser()
    internal fun encode(element: Any?): String {
        if (element == null) {
            return "null"
        }
        val list = parser.asList(element)
        if (list != null) {
            var first = true
            val text = StringBuilder("[")
            for (item in list) {
                if (first) {
                    first = false
                } else {
                    text.append(",")
                }
                text.append(encode(item))
            }
            text.append("]")
            return text.toString()
        }
        val map = parser.asMap(element)
        if (map != null) {
            var first = true
            val text = StringBuilder("{")
            for ((key, value) in map) {
                if (first) {
                    first = false
                } else {
                    text.append(",")
                }
                text.append("\"${key}\":")
                text.append(encode(value))
            }
            text.append("}")
            return text.toString()
        }
        val string = element as? String
        if (string != null) {
            return Json.encodeToString(string)
        }
        val bool = element as? Boolean
        if (bool != null) {
            return Json.encodeToString(bool)
        }
        val long = element as? Long
        if (long != null) {
            return Json.encodeToString(long)
        }
        val double = element as? Double
        if (double != null) {
            return Json.encodeToString(double)
        }
        val float = element as? Float
        if (float != null) {
            return Json.encodeToString(float)
        }
        val bigDecimal = element as? BigDecimal
        if (bigDecimal != null) {
            return Json.encodeToString(bigDecimal)
        }
        val int = element as? Int
        if (int != null) {
            return Json.encodeToString(int)
        }
        val datetime = element as? Instant
        if (datetime != null) {
            return Json.encodeToString(datetime.toEpochMilliseconds())
        }
        val jsonElement = element as? JsonElement
        if (jsonElement != null) {
            return Json.encodeToString(jsonElement)
        }
        return ""
    }

    private fun string(element: Any): String? {
        val string = element as? String
        string?.let { return it }
        val jsonPrimitive = element as? JsonPrimitive
        jsonPrimitive?.let {
            if (it.isString) {
                return it.content
            }
        }
        return null
    }

    private fun double(element: Any): Double? {
        val double = element as? Double
        double?.let { return it }
        val jsonPrimitive = element as? JsonPrimitive
        return jsonPrimitive?.doubleOrNull
    }

    private fun float(element: Any): Float? {
        val float = element as? Float
        float?.let { return it }
        val jsonPrimitive = element as? JsonPrimitive
        return jsonPrimitive?.floatOrNull
    }

    private fun long(element: Any): Long? {
        val long = element as? Long
        long?.let { return it }
        val jsonPrimitive = element as? JsonPrimitive
        return jsonPrimitive?.longOrNull
    }

    private fun int(element: Any): Int? {
        val int = element as? Int
        int?.let { return it }
        val jsonPrimitive = element as? JsonPrimitive
        return jsonPrimitive?.intOrNull
    }

    private fun boolean(element: Any): Boolean? {
        val bool = element as? Boolean
        bool?.let { return it }
        val jsonPrimitive = element as? JsonPrimitive
        return jsonPrimitive?.booleanOrNull
    }
}
