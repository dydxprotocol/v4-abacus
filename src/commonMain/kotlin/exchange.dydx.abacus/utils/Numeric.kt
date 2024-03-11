package exchange.dydx.abacus.utils

import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal

internal class Doubles {
    @Suppress("PropertyName")
    val ZERO = 0.0

    @Suppress("PropertyName")
    val ONE = 1.0

    @Suppress("PropertyName")
    val TEN = 10.0

    @Suppress("PropertyName")
    val POSITIVE = 1.0

    @Suppress("PropertyName")
    val NEGATIVE = -1.0
}

internal class Decimals {
    val mode = DecimalMode(24, RoundingMode.TOWARDS_ZERO, 24)
    val highDefinitionMode = DecimalMode(24, RoundingMode.TOWARDS_ZERO, 24)

    @Suppress("PropertyName")
    val ZERO = (0.0).toBigDecimal(null, mode)

    @Suppress("PropertyName")
    val ONE = (1.0).toBigDecimal(null, mode)

    @Suppress("PropertyName")
    val TEN = (10.0).toBigDecimal(null, mode)

    @Suppress("PropertyName")
    val POSITIVE = (1.0).toBigDecimal(null, mode)

    @Suppress("PropertyName")
    val NEGATIVE = (-1.0).toBigDecimal(null, mode)
}

internal class Numeric {
    companion object {
        val double = Doubles()
        val decimal = Decimals()
    }
}
