package exchange.dydx.abacus.output.account

internal actual fun normalizeClientId(clientId: UInt): UInt {
    /**
     * https://stackoverflow.com/a/11385688
     *
     * UInt becomes a JS `number` which by default is a signed 64 bit float.
     *
     * The following expression forces it to be interpreted as a 32-bit unsigned integer instead.
     *
     * clientId is cast to a 32-bit integer for bitwise operation
     * >>> 0 has no effect (no bits are shifted)
     * The result is converted to a Number
     *
     * This works because of how floating point representation works.
     * A 32-bit uint will always be within the safe range of the mantissa (52 bits)
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number#number_encoding
     */
    return js("clientId >>> 0")
}