package exchange.dydx.abacus.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StringUtilsTests {
    // Injective address to lowercase EVM address, verified against the BIP173 reference implementation
    private val injectiveToEvmAddresses = listOf(
        "inj15qx9nl66pqxjh92dp367gm3z5rphzg669sh88w" to "0xa00c59ff5a080d2b954d0c75e46e22a0c371235a",
        // Address conversion example from the Injective docs
        "inj14au322k9munkmx5wrchz9q30juf5wjgz2cfqku" to "0xaf79152ac5df276d9a8e1e2e22822f9713474902",
        // Keplr test wallet
        "inj100dha6y80q8883x65zr6ssuap2jejhkxmchhpq" to "0x7bdb7ee887780e73c4daa087a8439d0aa5995ec6",
        "inj1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqe2hm49" to "0x0000000000000000000000000000000000000000",
        "inj1llllllllllllllllllllllllllllllll4mk5j0" to "0xffffffffffffffffffffffffffffffffffffffff",
        "inj1qy352euf40x77qfrg4ncn27dauqjx3t8r0l89x" to "0x0123456789abcdef0123456789abcdef01234567",
    )

    @Test
    fun testInjectiveToEvmAddress() {
        injectiveToEvmAddresses.forEach { (injectiveAddress, evmAddress) ->
            assertTrue(injectiveAddress.isInjectiveAddressValid(), injectiveAddress)
            assertEquals(evmAddress, injectiveAddress.toInjectiveEvmAddress())
        }
    }

    @Test
    fun testEvmToInjectiveAddress() {
        injectiveToEvmAddresses.forEach { (injectiveAddress, evmAddress) ->
            assertEquals(injectiveAddress, evmAddress.evmToInjectiveAddress())
            assertEquals(injectiveAddress, evmAddress.removePrefix("0x").evmToInjectiveAddress())
            assertEquals(injectiveAddress, evmAddress.uppercase().evmToInjectiveAddress())
        }
        assertEquals(
            "inj14au322k9munkmx5wrchz9q30juf5wjgz2cfqku",
            "0xAF79152AC5dF276D9A8e1E2E22822f9713474902".evmToInjectiveAddress(),
        )
        assertEquals(
            "inj100dha6y80q8883x65zr6ssuap2jejhkxmchhpq",
            "0x7bdb7Ee887780E73c4dAa087a8439D0Aa5995eC6".evmToInjectiveAddress(),
        )
    }

    @Test
    fun testInjectiveAddressRoundTrip() {
        injectiveToEvmAddresses.forEach { (injectiveAddress, evmAddress) ->
            assertEquals(injectiveAddress, injectiveAddress.toInjectiveEvmAddress()?.evmToInjectiveAddress())
            assertEquals(evmAddress, evmAddress.evmToInjectiveAddress()?.toInjectiveEvmAddress())
        }
    }

    @Test
    fun testUppercaseInjectiveAddress() {
        val uppercaseAddress = "INJ15QX9NL66PQXJH92DP367GM3Z5RPHZG669SH88W"
        assertTrue(uppercaseAddress.isInjectiveAddressValid())
        assertEquals("0xa00c59ff5a080d2b954d0c75e46e22a0c371235a", uppercaseAddress.toInjectiveEvmAddress())

        val mixedCaseAddress = "inj15qx9nl66pqxjh92dp367gm3z5rphzg669sh88W"
        assertFalse(mixedCaseAddress.isInjectiveAddressValid())
        assertNull(mixedCaseAddress.toInjectiveEvmAddress())
    }

    @Test
    fun testInvalidInjectiveAddresses() {
        listOf(
            // Same 20 bytes as a valid Injective address, but other chains' prefixes
            "cosmos15qx9nl66pqxjh92dp367gm3z5rphzg660eqr4k",
            "dydx15qx9nl66pqxjh92dp367gm3z5rphzg66xqw84p",
            "dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art",
            // Last character changed, so the checksum fails
            "inj15qx9nl66pqxjh92dp367gm3z5rphzg669sh88q",
            // Valid checksum, but 32 bytes instead of 20
            "inj14w46h2at4w46h2at4w46h2at4w46h2at4w46h2at4w46h2at4w4swmhe4e",
            "inj15qx9nl66pqxjh92dp367gm3z5rphzg669sh88",
            "inj1invalid",
            "0xa00c59ff5a080d2b954d0c75e46e22a0c371235a",
            "",
        ).forEach { address ->
            assertFalse(address.isInjectiveAddressValid(), address)
            assertNull(address.toInjectiveEvmAddress(), address)
        }
    }

    @Test
    fun testInvalidEvmAddresses() {
        listOf(
            "0xa00c59ff5a080d2b954d0c75e46e22a0c371235",
            "0xa00c59ff5a080d2b954d0c75e46e22a0c371235a0",
            "0xg00c59ff5a080d2b954d0c75e46e22a0c371235a",
            "0x+f" + "00".repeat(19),
            "0x-1" + "00".repeat(19),
            "0x 0" + "00".repeat(19),
            "0x0xa00c59ff5a080d2b954d0c75e46e22a0c37123",
            "inj15qx9nl66pqxjh92dp367gm3z5rphzg669sh88w",
            "0x",
            "",
        ).forEach { address ->
            assertNull(address.evmToInjectiveAddress(), address)
        }
    }

    @Test
    fun testAbacusStringUtilsInjective() {
        val (injectiveAddress, evmAddress) = injectiveToEvmAddresses.first()
        assertTrue(AbacusStringUtils.isInjectiveAddressValid(injectiveAddress))
        assertFalse(AbacusStringUtils.isInjectiveAddressValid("inj1invalid"))
        assertEquals(evmAddress, AbacusStringUtils.toInjectiveEvmAddress(injectiveAddress))
        assertNull(AbacusStringUtils.toInjectiveEvmAddress("inj1invalid"))
        assertEquals(injectiveAddress, AbacusStringUtils.evmToInjectiveAddress(evmAddress))
        assertNull(AbacusStringUtils.evmToInjectiveAddress("0x1234"))
    }
}
