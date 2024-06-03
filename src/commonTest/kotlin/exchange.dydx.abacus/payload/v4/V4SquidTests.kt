package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.routerChains
import exchange.dydx.abacus.state.model.routerTokens
import exchange.dydx.abacus.state.model.squidRoute
import exchange.dydx.abacus.state.model.squidRouteV2
import exchange.dydx.abacus.state.model.squidStatus
import exchange.dydx.abacus.state.model.squidV2SdkInfo
import exchange.dydx.abacus.state.model.transfer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class V4SquidTests : V4BaseTests() {
    @Test
    fun testDepositChains() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        val stateChange = perp.routerChains(mock.squidChainsMock.payload)
        assertNotNull(stateChange)

        test({
            perp.transfer("DEPOSIT", TransferInputField.type, 0)
        }, null, {
            val chains = it.state?.input?.transfer?.depositOptions?.chains!!
            assertTrue(chains.size > 0)
            val chain = chains[0]
            assertTrue(chain.type.isNotEmpty())
            assertTrue(chain.iconUrl!!.isNotEmpty())
            assertTrue(chain.stringKey != null || chain.string != null)
            assertTrue(it.state?.input?.transfer?.resources?.chainResources != null)
        })

        test({
            perp.transfer(null, TransferInputField.type, 0)
        }, null, {
            assertTrue(it.state?.input?.transfer?.depositOptions == null)
        })
    }

    @Test
    fun testDepositTokens() {
        // Due to the JIT compiler nature for JVM (and Kotlin) and JS, Android/web would ran slow the first round. Second round give more accurate result
        setup()

        var stateChange = perp.routerChains(mock.squidChainsMock.payload)
        assertNotNull(stateChange)

        stateChange = perp.routerTokens(mock.squidTokensMock.payload)
        assertNotNull(stateChange)

        test({
            perp.transfer("DEPOSIT", TransferInputField.type, 0)
        }, null, {
            val assets = it.state?.input?.transfer?.depositOptions?.assets!!
            assertTrue(assets.size > 0)
            val token = assets[0]
            assertTrue(token.type.isNotEmpty())
            assertTrue(token.iconUrl!!.isNotEmpty())
            assertTrue(token.stringKey != null || token.string != null)
            assertTrue(it.state?.input?.transfer?.resources?.tokenResources != null)
            assertTrue(it.state?.input?.transfer?.token != null)
        })

        test({
            perp.transfer(null, TransferInputField.type, 0)
        }, null, {
            assertTrue(it.state?.input?.transfer?.depositOptions == null)
        })
    }

    @Test
    fun testSquidRouteV2() {
        setup()

        perp.transfer("DEPOSIT", TransferInputField.type, 0)

        var stateChange = perp.squidV2SdkInfo(mock.squidV2AssetsMock.payload)
        assertNotNull(stateChange)

        stateChange = perp.squidRouteV2(mock.squidV2RouteMock.payload, 0, null)
        assertNotNull(stateChange)

        test({
            perp.transfer("DEPOSIT", TransferInputField.type, 0)
        }, null, {
            val summary = it.state?.input?.transfer?.summary!!
            assertNotNull(summary)
            assertTrue { summary.slippage!!.toInt() == 0 }
            assertTrue { summary.exchangeRate!! > 0 }
            assertTrue { summary.estimatedRouteDuration!! > 0 }
            assertTrue { summary.gasFee!! > 0 }
            // assertTrue { summary.bridgeFee!! > 0 }
            assertNotNull(it.state?.input?.transfer?.requestPayload)
            assertNotNull(it.state?.input?.transfer?.size?.usdcSize)
        })

        test({
            perp.transfer("0", TransferInputField.size, 0)
        }, null, {
            assertNull(it.state?.input?.transfer?.requestPayload)
        })
    }

    @Test
    fun testSquidRoute() {
        setup()

        perp.transfer("DEPOSIT", TransferInputField.type, 0)

        var stateChange = perp.routerChains(mock.squidChainsMock.payload)
        assertNotNull(stateChange)

        stateChange = perp.routerTokens(mock.squidTokensMock.payload)
        assertNotNull(stateChange)

        stateChange = perp.squidRoute(mock.squidRouteMock.payload, 0, null)
        assertNotNull(stateChange)

        test({
            perp.transfer("DEPOSIT", TransferInputField.type, 0)
        }, null, {
            val summary = it.state?.input?.transfer?.summary!!
            assertNotNull(summary)
            assertTrue { summary.slippage!! > 0 }
            assertTrue { summary.exchangeRate!! > 0 }
            assertTrue { summary.estimatedRouteDuration!! > 0 }
            assertTrue { summary.gasFee!! > 0 }
            assertTrue { summary.bridgeFee!! > 0 }
            assertNotNull(it.state?.input?.transfer?.requestPayload)
            assertNotNull(it.state?.input?.transfer?.size?.usdcSize)
        })

        test({
            perp.transfer("0", TransferInputField.size, 0)
        }, null, {
            assertNull(it.state?.input?.transfer?.requestPayload)
        })
    }

    @Test
    fun testSquidRoute_error() {
        setup()

        perp.transfer("DEPOSIT", TransferInputField.type, 0)

        var stateChange = perp.routerChains(mock.squidChainsMock.payload)
        assertNotNull(stateChange)

        stateChange = perp.routerTokens(mock.squidTokensMock.payload)
        assertNotNull(stateChange)

        stateChange = perp.squidRoute(mock.squidRouteMock.errors_payload, 0, null)
        assertNotNull(stateChange)

        test({
            perp.transfer("DEPOSIT", TransferInputField.type, 0)
        }, null, {
            val errors = it.state?.input?.transfer?.errors!!
            assertNotNull(errors)

            assertEquals(
                it.state?.input?.transfer?.errorMessage,
                "toChain: dydxprotocol-testnet-1 unsupported chain id",
            )
        })
    }

    @Test
    fun testSquidStatus() {
        setup()

        val stateChange = perp.squidStatus(mock.squidStatusMock.payload, null)
        assertNotNull(stateChange)
        assertNotNull(perp.data?.get("transferStatuses"))

        perp.update(stateChange)

        test({
            perp.transfer("DEPOSIT", TransferInputField.type, 0)
        }, null, {
            val transferStatuses = it.state?.transferStatuses
            assertNotNull(transferStatuses)
            val status = transferStatuses.values.first()
            assertEquals(status.status, "destination_executed")
            assertEquals(status.gasStatus, "gas_paid_enough_gas")
            assertNotNull(status.axelarTransactionUrl)
            assertEquals(status.routeStatuses?.first()?.status, "success")
            assertEquals(status.routeStatuses?.last()?.status, "success")
            assertNotNull(status.squidTransactionStatus)
        })
    }

    @Test
    fun testSelectedTokenSymbol() {
        setup()

        val stateChange = perp.routerTokens(mock.squidTokensMock.payload)
        assertNotNull(stateChange)

        assertTrue(perp.squidProcessor.selectedTokenSymbol("0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE", "should_not_matter") == "ETH")
    }

    @Test
    fun testSelectedTokenDecimals() {
        setup()

        val stateChange = perp.routerTokens(mock.squidTokensMock.payload)
        assertNotNull(stateChange)

        assertTrue(perp.squidProcessor.selectedTokenDecimals("0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE", "should_not_matter") == "18")
    }

    @Test
    fun testDefaultTokenAddress() {
        setup()

        var stateChange = perp.routerChains(mock.squidChainsMock.payload)
        assertNotNull(stateChange)

        stateChange = perp.routerTokens(mock.squidTokensMock.payload)
        assertNotNull(stateChange)

        assertTrue(perp.squidProcessor.defaultTokenAddress("1") == "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE")
    }

    @Test
    fun testChainResources() {
        setup()

        val stateChange = perp.routerChains(mock.squidChainsMock.payload)
        assertNotNull(stateChange)

        val result = perp.squidProcessor.chainResources("1")
        val resource = result?.get("1")
        assertTrue(resource?.chainId == 1) // Ethereum
        assertTrue(resource?.chainName == "Ethereum") // Ethereum
    }

    @Test
    fun testTokenResources() {
        setup()

        var stateChange = perp.routerChains(mock.squidChainsMock.payload)
        assertNotNull(stateChange)

        stateChange = perp.routerTokens(mock.squidTokensMock.payload)
        assertNotNull(stateChange)

        val result = perp.squidProcessor.tokenResources("1")
        assertTrue((result?.keys?.size ?: 0) > 0)
    }

    @Test
    fun testTokenOptions() {
        setup()

        var stateChange = perp.routerChains(mock.squidChainsMock.payload)
        assertNotNull(stateChange)

        stateChange = perp.routerTokens(mock.squidTokensMock.payload)
        assertNotNull(stateChange)

        val result = perp.squidProcessor.tokenOptions("1")
        assertTrue(result.size > 0)
    }
}
