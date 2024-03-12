package exchange.dydx.abacus.utils

import kotlin.math.absoluteValue

internal class StringHelper {
    companion object {
        internal fun zeros(digits: Int): String {
            var string = ""
            for (i in 0 until digits) {
                string += "0"
            }
            return string
        }

        internal fun decimals(afterDecimals: String, decimals: Int? = null): String {
            return if (decimals != null) {
                if (afterDecimals.length > decimals) {
                    "." + afterDecimals.substring(0, decimals)
                } else {
                    "." + afterDecimals + StringHelper.zeros(decimals - afterDecimals.length)
                }
            } else {
                if (afterDecimals.isNotEmpty()) ".$afterDecimals" else ""
            }
        }

        internal fun shiftLeft(number: ULong, decimals: UInt): String {
            val string = number.toString()
            val length = string.length
            return if (decimals == 0u) {
                string
            } else {
                if (decimals >= length.toUInt()) {
                    "0" + StringHelper.decimals("", decimals.toInt() - length) + string
                } else {
                    string.substring(0, string.length - decimals.toInt()) + StringHelper.decimals(string.substring(string.length - decimals.toInt()))
                }
            }
        }

        internal fun format(number: Double, decimals: Int? = null): String {
            val string = number.toString()
            return formatNumericString(string, decimals)
        }

        internal fun format(number: Float, decimals: Int? = null): String {
            val string = number.toString()
            return formatNumericString(string, decimals)
        }

        private fun formatNumericString(string: String, decimals: Int? = null): String {
            val scientific = string.split("E")
            if (scientific.size == 1) {
                val decimalComponents = string.split(".")
                val beforeDecimal = decimalComponents.firstOrNull()
                val afterDecimal = if (decimalComponents.size != 1) decimalComponents.last() else ""
                return beforeDecimal + StringHelper.decimals(
                    afterDecimal,
                    decimals,
                )
            } else {
                val beforeE = scientific.first()
                val afterE = scientific.last()
                val decimalComponents = beforeE.split(".")
                val beforeDecimal = decimalComponents.firstOrNull() ?: ""
                val afterDecimal = if (decimalComponents.size != 1) decimalComponents.last() else ""
                val shift = afterE.toInt()
                if (shift > 0) {
                    /*
                    1233.2343E2
                     */
                    return if (shift > afterDecimal.length) {
                        beforeDecimal + afterDecimal + StringHelper.zeros(shift - afterDecimal.length) + StringHelper.decimals(
                            "",
                            decimals,
                        )
                    } else {
                        beforeDecimal + afterDecimal.substring(0, shift) + StringHelper.decimals(
                            afterDecimal.substring(
                                shift,
                            ),
                            decimals,
                        )
                    }
                } else {
                    val backShift = shift.absoluteValue

                    return if (backShift > beforeDecimal.length) {
                        "0" + StringHelper.decimals(
                            StringHelper.zeros(backShift - beforeDecimal.length) + beforeDecimal + afterDecimal,
                            decimals,
                        )
                    } else {
                        beforeDecimal.substring(
                            0,
                            beforeDecimal.length - backShift,
                        ) + StringHelper.decimals(
                            beforeDecimal.substring(beforeDecimal.length - backShift) + afterDecimal,
                            decimals,
                        )
                    }
                }
            }
        }
    }
}

internal fun Double.format(decimals: Int? = null): String {
    return StringHelper.format(this, decimals)
}

internal fun Float.format(decimals: Int? = null): String {
    return StringHelper.format(this, decimals)
}
