package exchange.dydx.abacus.utils

import exchange.dydx.abacus.utils.beth32.Bech32
import kotlin.String

fun String.isAddressValid(): Boolean {
    try {
        val (humanReadablePart, data) = Bech32.decode(this)
        return humanReadablePart == "dydx"
    } catch (e: Exception) {
        return false
    }
}