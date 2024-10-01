package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.tests.extensions.rest
import kollections.iListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class V4VaultTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        print("--------First round----------\n")

        testVaultOnce()
    }

    private fun testVaultOnce() {
        testMegaVaultPnlReceived()
        testVaultMarketPnlsReceived()
        testVaultMarketPositionsReceived()

        // validate the output state
        val vault = perp.state?.vault

        val vaultDetails = vault?.details
        assertEquals(10000.0, vaultDetails?.totalValue)
        assertEquals(0.1 * 365, vaultDetails?.thirtyDayReturnPercent)
        assertEquals(2, vaultDetails?.history?.size)
        assertEquals(1000.0, vaultDetails?.history?.get(0)?.totalPnl)
        assertEquals(500.0, vaultDetails?.history?.get(1)?.totalPnl)

        val vaultPositions = vault?.positions?.positions
        val btcPosition = vaultPositions?.get(0)
        assertEquals("BTC-USD", btcPosition?.marketId)
        assertEquals(0.05, btcPosition?.thirtyDayPnl?.percent)
        assertEquals(500.0, btcPosition?.thirtyDayPnl?.absolute)
        assertEquals(iListOf(0.0, 500.0), btcPosition?.thirtyDayPnl?.sparklinePoints)
    }

    private fun testMegaVaultPnlReceived() {
        perp.rest(
            url = AbUrl.fromString("$testRestUrl/v4/vault/v1/megavault/historicalPnl"),
            payload = mock.vaultMocks.megaVaultPnlMocks,
            subaccountNumber = 0,
            height = null,
        )

        val vault = perp.internalState.vault
        assertEquals(10000.0, vault?.details?.totalValue)
        assertEquals(0.1 * 365, vault?.details?.thirtyDayReturnPercent)
        assertEquals(2, vault?.details?.history?.size)
        assertEquals(1000.0, vault?.details?.history?.get(0)?.totalPnl)
        assertEquals(500.0, vault?.details?.history?.get(1)?.totalPnl)
    }

    private fun testVaultMarketPnlsReceived() {
        perp.rest(
            url = AbUrl.fromString("$testRestUrl/v4/vault/v1/vaults/historicalPnl"),
            payload = mock.vaultMocks.vaultMarketPnlsMocks,
            subaccountNumber = 0,
            height = null,
        )

        val vault = perp.internalState.vault
        assertEquals(1, vault?.pnls?.size)
        val btcPnl = vault?.pnls?.get("BTC-USD")
        assertEquals(500.0, btcPnl?.absolute)
        assertEquals(0.05, btcPnl?.percent)
        assertEquals(iListOf(0.0, 500.0), btcPnl?.sparklinePoints)
    }

    private fun testVaultMarketPositionsReceived() {
        perp.rest(
            url = AbUrl.fromString("$testRestUrl/v4/vault/v1/megavault/positions"),
            payload = mock.vaultMocks.vaultMarketPositionsMocks,
            subaccountNumber = 0,
            height = null,
        )

        val vault = perp.internalState.vault
        assertEquals(1, vault?.positions?.size)
        val btcPosition = vault?.positions?.get(0)
        assertEquals("BTC-USD", btcPosition?.ticker)
        assertEquals(15000.0, btcPosition?.equity)
        assertEquals(1.0, btcPosition?.openPosition?.size)
        assertEquals(50000.0, btcPosition?.openPosition?.entryPrice)
        assertEquals(40000.0, btcPosition?.assetPosition?.size)
        assertEquals("USDC", btcPosition?.assetPosition?.symbol)
    }
}
