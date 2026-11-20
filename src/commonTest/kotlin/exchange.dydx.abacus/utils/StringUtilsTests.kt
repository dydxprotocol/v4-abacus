package exchange.dydx.abacus.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StringUtilsTests {
    @Test
    fun testAddressConversion() {
        val dydxAddress = "dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art"
        val nobleAddress = "noble14zzueazeh0hj67cghhf9jypslcf9sh2n4vp3mj"
        assertEquals(nobleAddress, dydxAddress.toNobleAddress())
        assertEquals(dydxAddress, nobleAddress?.toDydxAddress())
    }

    @Test
    fun testInjectiveAddress() {
        val injectiveAddress = "inj15qx9nl66pqxjh92dp367gm3z5rphzg669sh88w"
        val evmAddress = "0xa00c59ff5a080d2b954d0c75e46e22a0c371235a"
        val dydxAddress = "dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art"

        assertTrue(injectiveAddress.isInjectiveAddressValid())
        assertEquals(evmAddress, injectiveAddress.toInjectiveEvmAddress())
        assertEquals(evmAddress, AbacusStringUtils.toInjectiveEvmAddress(injectiveAddress))
        assertTrue(AbacusStringUtils.isInjectiveAddressValid(injectiveAddress))

        assertFalse(dydxAddress.isInjectiveAddressValid())
        assertNull(dydxAddress.toInjectiveEvmAddress())
        assertFalse("inj1invalid".isInjectiveAddressValid())
        assertNull("inj1invalid".toInjectiveEvmAddress())
    }
}
