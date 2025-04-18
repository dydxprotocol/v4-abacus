package exchange.dydx.abacus.state.helper

import exchange.dydx.abacus.protocols.FormatterProtocol
import kollections.JsExport
import kotlin.math.roundToLong

@JsExport
class Formatter(private val nativeFormatter: FormatterProtocol?) {
    fun percent(value: Double?, digits: Int): String? {
        nativeFormatter?.let {
            return it.percent(value, digits)
        } ?: run {
            return defaultPercent(value, digits)
        }
    }

    private fun defaultPercent(value: Double?, digits: Int): String? {
        // placeholder for now until we call native formatter with expect/actual
        return if (value != null) {
            val rounded = (value * 100).round(digits)
            val filled = "$rounded".fillZeros(digits)
            return "$filled%"
        } else {
            null
        }
    }

    fun price(value: Double?, tickSize: String?): String? {
        nativeFormatter?.let {
            return it.dollar(value, tickSize)
        } ?: run {
            return defaultPrice(value, tickSize)
        }
    }

    private fun defaultPrice(value: Double?, tickSize: String?): String? {
        // placeholder for now until we call native formatter with expect/actual
        val tickSizeValue = tickSize ?: "0.01"
        val digits = if (tickSizeValue.contains(".")) {
            tickSizeValue.split(".")[1].length
        } else {
            0
        }
        if (digits == 0) {
            var factor = 1.0
            repeat(tickSizeValue.length - 1) { factor *= 10 }
            return if (value != null) {
                val rounded = (value / factor).round(0) * factor
                val stripped = "$rounded".stripDecimal()
                return "\$$stripped"
            } else {
                null
            }
        } else {
            return if (value != null) {
                val rounded = value.round(digits)
                val filled = "$rounded".fillZeros(2)
                return "\$$filled"
            } else {
                null
            }
        }
    }
}

fun String.stripDecimal(): String {
    return if (this.contains(".")) {
        this.split(".")[0]
    } else {
        this
    }
}

fun String.fillZeros(digits: Int): String {
    val split = this.split(".")
    val decimal = if (split.size > 1) {
        split[1]
    } else {
        ""
    }
    val zeros = digits - decimal.length
    val period = if (decimal.isNotEmpty()) {
        ""
    } else {
        "."
    }
    return this + period + "0".repeat(zeros)
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (this * multiplier).roundToLong() / multiplier
}
