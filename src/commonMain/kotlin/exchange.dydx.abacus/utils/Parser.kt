package exchange.dydx.abacus.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.protocols.ParserProtocol
import kollections.iListOf
import kollections.toIList
import kollections.toIMap
import kollections.toIMutableList
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

inline fun <reified T> cast(any: Any?): T = any as T

@Suppress("UNCHECKED_CAST")
class Parser : ParserProtocol {
    override fun asString(data: Any?): String? {
        val jsonLiteral = data as? JsonPrimitive
        if (jsonLiteral != null) {
            val jsonNull = data as? JsonNull
            if (jsonNull != null) {
                return null
            }
            return jsonLiteral.content
        }
        val string = data as? String
        if (string != null) {
            return string.trim()
        }

        val datetime = data as? Instant
        if (datetime != null) {
            return datetime.toString()
        }

        val decimal = data as? BigDecimal
        if (decimal != null) {
            return decimal.toStringExpanded()
        }

        if (data != null) {
            return "$data"
        }

        return null
    }

    override fun asStrings(data: Any?): IList<String>? {
        val list = this.asList(data)
        if (list != null) {
            return list.mapNotNull {
                asString(it)
            }.toIList()
        }

        val string = asString(data)
        if (string != null) {
            return iListOf(string)
        }
        return null
    }

    override fun asDouble(data: Any?): Double? {
        val decimal = data as? BigDecimal
        if (decimal != null) {
            return decimal.doubleValue(false)
        }
        val jsonLiteral = data as? JsonPrimitive
        if (jsonLiteral != null) {
            return jsonLiteral.doubleOrNull
        }
        val double = data as? Double
        if (double != null) {
            return double
        }

        val float = data as? Float
        if (float != null) {
            return float.toDouble()
        }

        val long = data as? Long
        if (long != null) {
            return long.toDouble()
        }

        val int = data as? Int
        if (int != null) {
            return int.toDouble()
        }

        val string = data as? String
        if (string != null) {
            val withDecimalPoint = string.replace(",", ".")
            return try {
                withDecimalPoint.toDouble()
            } catch (e: Exception) {
                try {
                    withDecimalPoint.toBigDecimal(null, Numeric.decimal.mode).doubleValue(false)
                } catch (e: Exception) {
                    Logger.e { "Failed to parse double: $string" }
                    Logger.e { "Exception: $e" }
                    null
                }
            }
        }

        return null
    }

    override fun asDecimal(data: Any?): BigDecimal? {
        val decimal = data as? BigDecimal
        if (decimal != null) {
            return decimal
        }

        val string = data as? String
        if (string != null) {
            return try {
                string.toBigDecimal(null, null)
            } catch (e: Exception) {
                Logger.e { "Failed to parse double: $string" }
                Logger.e { "Exception: $e" }
                null
            }
        }

        val jsonLiteral = data as? JsonPrimitive
        if (jsonLiteral != null) {
            return if (jsonLiteral.isString) {
                try {
                    jsonLiteral.content.toBigDecimal(null, null)
                } catch (e: Exception) {
                    Logger.e { "Failed to parse jsonLiteral: $jsonLiteral.content" }
                    Logger.e { "Exception: $e" }
                    null
                }
            } else {
                jsonLiteral.doubleOrNull?.toBigDecimal(null, null)
            }
        }
        val double = data as? Double
        if (double != null) {
            return double.toBigDecimal(null, null)
        }

        val float = data as? Float
        if (float != null) {
            return float.toBigDecimal(null, null)
        }

        val long = data as? Long
        if (long != null) {
            return long.toBigDecimal(null, null)
        }

        val int = data as? Int
        if (int != null) {
            return int.toBigDecimal(null, null)
        }

        return null
    }

    override fun asInt(data: Any?): Int? {
        val jsonLiteral = data as? JsonPrimitive
        if (jsonLiteral != null) {
            return jsonLiteral.intOrNull
        }

        val long = data as? Long
        if (long != null) {
            return long.toInt()
        }

        val int = data as? Int
        if (int != null) {
            return int
        }

        val float = data as? Float
        if (float != null) {
            return float.toInt()
        }

        val double = data as? Double
        if (double != null) {
            return double.toInt()
        }

        val decimal = data as? BigDecimal
        if (decimal != null) {
            return decimal.doubleValue(false).toInt()
        }

        val string = data as? String
        if (string != null) {
            return try {
                string.toInt()
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    override fun asLong(data: Any?): Long? {
        val jsonLiteral = data as? JsonPrimitive
        if (jsonLiteral != null) {
            return jsonLiteral.longOrNull
        }

        val long = data as? Long
        if (long != null) {
            return long
        }

        val int = data as? Int
        if (int != null) {
            return int.toLong()
        }

        val float = data as? Float
        if (float != null) {
            return float.toLong()
        }

        val double = data as? Double
        if (double != null) {
            return double.toLong()
        }

        val string = data as? String
        if (string != null) {
            return try {
                string.toLong()
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    override fun asBool(data: Any?): Boolean? {
        val jsonLiteral = data as? JsonPrimitive
        if (jsonLiteral != null) {
            return jsonLiteral.booleanOrNull
        }

        val boolean = data as? Boolean
        if (boolean != null) {
            return boolean
        }
        val int = data as? Int
        if (int != null) {
            return int != 0
        }
        val string = (data as? String)?.lowercase()
        if (string != null) {
            if (string == "y" || string == "1" || string == "true" || string == "yes" || string == "on") {
                return true
            } else if (string == "n" || string == "0" || string == "false" || string == "no" || string == "off") {
                return false
            }
        }

        return null
    }

    override fun asDatetime(data: Any?): Instant? {
        val string = asString(data)
        if (string != null) {
            return try {
                Instant.parse(string)
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    override fun asMap(data: Any?): IMap<String, Any>? {
        if (data == null) return null
        val imap = data as? IMap<String, Any>
        if (imap != null) return imap
        val map = data as? Map<String, Any>
        if (map != null) return map.toIMap()
        val jsonObject = data as? JsonObject
        if (jsonObject != null) return jsonObject.toMap().toIMap()
        return null
    }

    override fun asNativeMap(data: Any?): Map<String, Any>? {
        if (data == null) return null
        val map = data as? Map<String, Any>
        if (map != null) return map
        val imap = data as? IMap<String, Any>
        if (imap != null) return imap.toMap()
        val jsonObject = data as? JsonObject
        if (jsonObject != null) return jsonObject.toMap()
        return null
    }

    override fun asList(data: Any?): IList<Any>? {
        if (data == null) return null
        val iList = data as? IList<Any>
        if (iList != null) return iList
        val list = data as? List<Any>
        if (list != null) return list.toIList()
        val jsonArray = data as? JsonArray
        if (jsonArray != null) return jsonArray.toIList()
        return null
    }

    override fun asNativeList(data: Any?): List<Any>? {
        if (data == null) return null
        val list = data as? List<Any>
        if (list != null) return list
        val jsonArray = data as? JsonArray
        if (jsonArray != null) return jsonArray.toList()
        return null
    }

    override fun value(data: Any?, path: String): Any? {
        val elements = path.split(".").toIMutableList()
        return value(data, elements.mutable())
    }

    private fun value(data: Any?, path: IMutableList<String>): Any? {
        data?.let {
            val firstKey = path.firstOrNull()
            if (firstKey != null) {
                val map = asMap(data)
                if (map != null) {
                    val item = asMap(data)?.get(firstKey)
                    path.removeFirst()
                    return value(item, path)
                } else {
                    val list = asList(data)
                    if (list != null) {
                        val index = asInt(firstKey)
                        if (index != null && index < list.size) {
                            val item = list[index]
                            path.removeFirst()
                            return value(item, path)
                        } else {
                            return null
                        }
                    } else {
                        return null
                    }
                }
            } else {
                return data
            }
        }
        return null
    }

    override fun decodeJsonObject(text: String?): IMap<String, Any>? {
        if (text == null) return null
        val map = try {
            Json.parseToJsonElement(text).jsonObject.toMap().toIMap()
        } catch (e: SerializationException) {
            Logger.e { "Unable to decode json object: $text" }
            null
        }
        return map
    }

    override fun decodeJsonArray(text: String?): IList<Any>? {
        if (text == null) return null
        val list = try {
            Json.parseToJsonElement(text).jsonArray.toIList()
        } catch (e: SerializationException) {
            Logger.e { "Unable to decode json object: $text" }
            null
        } catch (e: IllegalArgumentException) {
            Logger.e { "Unable to decode json object: $text" }
            null
        }
        return list
    }
}
