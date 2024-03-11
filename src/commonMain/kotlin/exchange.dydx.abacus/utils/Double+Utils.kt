import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.utils.Numeric
import kotlin.math.absoluteValue

internal fun Double.abs(): Double {
    return this.absoluteValue
}

internal fun Double.longValue(ignore: Boolean): Long {
    return this.toLong()
}

internal fun Double.numberOfDecimals(): Int {
    val bigDecimal = toBigDecimal(null, Numeric.decimal.mode)
    val string = bigDecimal.toStringExpanded()
    val elements = string.split(".")
    return if (elements.size > 1) {
        elements.last().length
    } else {
        0
    }
}

internal fun Double.tickDecimals(): BigDecimal {
    val numberOfDecimals = numberOfDecimals()
    return if (numberOfDecimals > 0) {
        var decimal = Numeric.decimal.ONE
        val pointOne = 0.1.toBigDecimal(null, Numeric.decimal.mode)
        for (i in 0 until numberOfDecimals) {
            decimal *= pointOne
        }
        decimal
    } else {
        val intValue = this.toInt()
        if (intValue == 0) {
            // tickSize should never be 0. In case of 0, we will treat it as 1
            Numeric.decimal.ONE
        } else {
            var num = intValue
            while (num % 10 == 0) {
                num /= 10
            }
            (intValue / num).toBigDecimal(null, Numeric.decimal.mode)
        }
    }
}
