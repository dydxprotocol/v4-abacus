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

fun String.toNobleAddress(): String? {
    try {
        val (humanReadablePart, data) = Bech32.decode(this)
        if (humanReadablePart != "dydx") {
            return null
        }
        return Bech32.encode("noble", data)
    } catch (e: Exception) {
        return null
    }
}

fun String.toDydxAddress(): String? {
    try {
        val (humanReadablePart, data) = Bech32.decode(this)
        if (humanReadablePart != "noble") {
            return null
        }
        return Bech32.encode("dydx", data)
    } catch (e: Exception) {
        return null
    }
}
