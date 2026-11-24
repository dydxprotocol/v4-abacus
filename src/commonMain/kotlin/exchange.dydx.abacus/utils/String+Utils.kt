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

fun String.toOsmosisAddress(): String? {
    try {
        val (humanReadablePart, data) = Bech32.decode(this)
        if (humanReadablePart != "dydx") {
            return null
        }
        return Bech32.encode("osmo", data)
    } catch (e: Exception) {
        return null
    }
}
fun String.toNeutronAddress(): String? {
    try {
        val (humanReadablePart, data) = Bech32.decode(this)
        if (humanReadablePart != "dydx") {
            return null
        }
        return Bech32.encode("neutron", data)
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

private val evmAddressHexRegex = Regex("[0-9a-fA-F]{40}")

fun String.isInjectiveAddressValid(): Boolean {
    return injectiveAddressBytes()?.size == 20
}

fun String.toInjectiveEvmAddress(): String? {
    val bytes = injectiveAddressBytes() ?: return null
    if (bytes.size != 20) {
        return null
    }
    return "0x" + bytes.toHex()
}

fun String.evmToInjectiveAddress(): String? {
    val hex = removePrefix("0x").removePrefix("0X")
    if (!evmAddressHexRegex.matches(hex)) {
        return null
    }
    val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    val data = convertBits(bytes, fromBits = 8, toBits = 5, pad = true) ?: return null
    return Bech32.encode("inj", data)
}

private fun String.injectiveAddressBytes(): ByteArray? {
    try {
        val (humanReadablePart, data) = Bech32.decode(this)
        if (humanReadablePart != "inj") {
            return null
        }
        return convertBits(data, fromBits = 5, toBits = 8, pad = false)
    } catch (e: Exception) {
        return null
    }
}

private fun convertBits(data: ByteArray, fromBits: Int, toBits: Int, pad: Boolean): ByteArray? {
    var acc = 0
    var bits = 0
    val maxv = (1 shl toBits) - 1
    val maxAcc = (1 shl (fromBits + toBits - 1)) - 1
    val converted = ArrayList<Byte>(data.size * fromBits / toBits + 1)
    for (value in data) {
        val v = value.toInt() and 0xff
        if ((v shr fromBits) != 0) {
            return null
        }
        acc = ((acc shl fromBits) or v) and maxAcc
        bits += fromBits
        while (bits >= toBits) {
            bits -= toBits
            converted.add(((acc shr bits) and maxv).toByte())
        }
    }
    if (pad) {
        if (bits > 0) {
            converted.add(((acc shl (toBits - bits)) and maxv).toByte())
        }
    } else if (bits >= fromBits || ((acc shl (toBits - bits)) and maxv) != 0) {
        return null
    }
    return converted.toByteArray()
}

private fun ByteArray.toHex(): String {
    return joinToString(separator = "") { byte ->
        (byte.toInt() and 0xff).toString(16).padStart(2, '0')
    }
}

fun String.toCamelCase(): String {
    return this.split("_", "-")
        .mapIndexed { index, s -> if (index == 0) s.lowercase() else s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
        .joinToString("")
}

// Need to wrap this in a class so iOS can access it
object AbacusStringUtils {
    fun isInjectiveAddressValid(address: String): Boolean {
        return address.isInjectiveAddressValid()
    }

    fun toInjectiveEvmAddress(injectiveAddress: String): String? {
        return injectiveAddress.toInjectiveEvmAddress()
    }

    fun evmToInjectiveAddress(evmAddress: String): String? {
        return evmAddress.evmToInjectiveAddress()
    }
}
