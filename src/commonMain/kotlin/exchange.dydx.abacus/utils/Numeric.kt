package exchange.dydx.abacus.utils

import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

internal class Doubles {
    val ZERO = 0.0
    val ONE = 1.0
    val TEN = 10.0
    val POSITIVE = 1.0
    val NEGATIVE = -1.0
}

internal class Decimals {
    val mode = DecimalMode(24, RoundingMode.AWAY_FROM_ZERO, 24)
    val highDefinitionMode = DecimalMode(24, RoundingMode.TOWARDS_ZERO, 24)

    val ZERO = (0.0).toBigDecimal(null, mode)
    val ONE = (1.0).toBigDecimal(null, mode)
    val TEN = (10.0).toBigDecimal(null, mode)
    val POSITIVE = (1.0).toBigDecimal(null, mode)
    val NEGATIVE = (-1.0).toBigDecimal(null, mode)
}

internal class Numeric {
    companion object {
        val double = Doubles()
        val decimal = Decimals()
    }
}