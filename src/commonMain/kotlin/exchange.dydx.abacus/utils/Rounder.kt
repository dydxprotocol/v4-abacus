package exchange.dydx.abacus.utils

import abs
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kollections.JsExport
import kotlinx.serialization.Serializable
import numberOfDecimals
import kotlin.math.roundToInt

@JsExport
@Serializable
class Rounder {
    enum class RoundingMode {
        TOWARDS_ZERO,
        NEAREST,
    }
    companion object {
        private val positive = 0.5.toBigDecimal(null, Numeric.decimal.mode)
        private val negative = -0.5.toBigDecimal(null, Numeric.decimal.mode)

        private val cache = mutableMapOf<Double, Int>()

        fun numberOfDecimals(stepSize: Double): Int {
            val cached = cache[stepSize]
            return if (cached != null) {
                cached
            } else {
                val stepSizeDecimals = stepSize.numberOfDecimals()
                cache[stepSize] = stepSizeDecimals
                return stepSizeDecimals
            }
        }

        fun quickRound(number: Double, stepSize: Double): Double {
            val stepSizeDecimals = numberOfDecimals(stepSize)
            val negative = number < 0.0
            val multiplier = (number.abs() / stepSize).roundToInt()

            val absValue = if (stepSizeDecimals > 0) {
                val multiplierString = multiplier.toString()
                val length = multiplierString.length
                val stringBuilder = StringBuilder()
                if (multiplierString.length <= stepSizeDecimals) {
                    stringBuilder.append("0.")
                    for (i in 0 until stepSizeDecimals - multiplierString.length) {
                        stringBuilder.append("0")
                    }
                    stringBuilder.append(multiplierString)
                } else {
                    stringBuilder.append(multiplierString.substring(0, length - stepSizeDecimals))
                    stringBuilder.append(".")
                    stringBuilder.append(
                        multiplierString.substring(
                            length - stepSizeDecimals,
                            length,
                        ),
                    )
                }
                stringBuilder.toString().toDouble()
            } else {
                (multiplier * stepSize.toInt()).toDouble()
            }
            return if (absValue == 0.0) {
                0.0
            } else {
                absValue * (if (negative) -1 else 1)
            }
        }

        fun quickRound2(number: Double, stepSize: Double, roundingMode: RoundingMode = RoundingMode.TOWARDS_ZERO): Double {
            if (stepSize > 0) {
                val factor = stepSize
                when (roundingMode) {
                    RoundingMode.TOWARDS_ZERO -> {
                        val sign = if (number < 0) -1 else 1
                        return kotlin.math.floor(number.abs() / factor) * factor * sign
                    }
                    RoundingMode.NEAREST -> return kotlin.math.round(number / factor) * factor
                }
            } else {
                return number
            }
        }

        fun round(number: Double, stepSize: Double, roundingMode: RoundingMode = RoundingMode.TOWARDS_ZERO): Double {
            val roundedDecimal = roundDecimal(
                number.toBigDecimal(null, Numeric.decimal.mode),
                stepSize.toBigDecimal(null, Numeric.decimal.mode),
                roundingMode,
            )
            return roundedDecimal.doubleValue(false)
        }

        fun roundDecimal(number: BigDecimal, stepSize: BigDecimal, roundingMode: RoundingMode = RoundingMode.TOWARDS_ZERO): BigDecimal {
            /*
            It should be (number / stepSize).toLong() * stepSize
            However, calculation with Double will cause problems
            We are using TextNumber to work around this
             */
            /*
            return if (stepSize > 0.0) {
                val textNumber = TextNumber(number)
                val textStepSize = TextNumber(stepSize)
                val long = textNumber.divide(textStepSize)?.toLong()
                if (long != null) {
                    val divided = TextNumber(long)
                    divided.multiply(textStepSize).toDouble()
                } else {
                    number
                }
            } else {
                number
            }
             */
            return if (stepSize > Numeric.double.ZERO) {
                val modifier = when (roundingMode) {
                    RoundingMode.TOWARDS_ZERO -> Numeric.decimal.ZERO
                    RoundingMode.NEAREST ->
                        if (number >= Numeric.decimal.ZERO) positive else negative
                }
                val long =
                    (number / stepSize + modifier).longValue(false)
                return stepSize * long.toBigDecimal(null, Numeric.decimal.mode)
            } else {
                number
            }
        }
    }
}
