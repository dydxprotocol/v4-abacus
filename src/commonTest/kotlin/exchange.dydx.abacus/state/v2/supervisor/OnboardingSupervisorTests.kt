package exchange.dydx.abacus.state.supervisor

import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.state.InternalTransferVenueState
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.abacus.state.machine.routerTokens
import exchange.dydx.abacus.state.machine.transfer
import exchange.dydx.abacus.state.manager.CctpChainTokenInfo
import exchange.dydx.abacus.state.manager.CctpConfig
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnboardingSupervisorTests {
    @Test
    fun testGoFastDepositRouteTargetsInjective() {
        val bodies = depositBodies(skipGoFast = true, cctp = false, injectiveAddress = INJECTIVE_ADDRESS)
        assertEquals(2, bodies.size)
        bodies.forEach { assertInjectiveDeposit(it, includeOsmosisAndNeutron = true) }
    }

    @Test
    fun testNonCctpDepositRouteTargetsInjective() {
        val bodies = depositBodies(skipGoFast = false, cctp = false, injectiveAddress = INJECTIVE_ADDRESS)
        assertEquals(1, bodies.size)
        assertInjectiveDeposit(bodies.first(), includeOsmosisAndNeutron = true)
    }

    @Test
    fun testCctpDepositRouteTargetsInjective() {
        val bodies = depositBodies(skipGoFast = false, cctp = true, injectiveAddress = INJECTIVE_ADDRESS)
        assertEquals(1, bodies.size)
        assertInjectiveDeposit(bodies.first(), includeOsmosisAndNeutron = false)
    }

    @Test
    fun testDepositRouteWaitsForInjectiveAddress() {
        assertTrue(depositBodies(skipGoFast = true, cctp = false, injectiveAddress = null).isEmpty())
        assertTrue(depositBodies(skipGoFast = true, cctp = false, injectiveAddress = "dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art").isEmpty())
    }

    private fun depositBodies(skipGoFast: Boolean, cctp: Boolean, injectiveAddress: String?): List<String> {
        val previousCctp = CctpConfig.cctpChainIds
        CctpConfig.cctpChainIds = if (cctp) listOf(CctpChainTokenInfo(FROM_CHAIN, TOKEN)) else null
        try {
            val io = BaseTests.testIOImplementations()
            val rest = io.rest as TestRest
            val environment = AbacusMockData().v4Environment
            val localizer = BaseTests.testLocalizer(io)
            val helper = NetworkHelper(
                deploymentUri = "https://example.com",
                environment = environment,
                uiImplementations = BaseTests.testUIImplementations(localizer),
                ioImplementations = io,
                configs = V4StateManagerConfigs("https://example.com", environment),
                stateNotification = null,
                dataNotification = null,
                parser = Parser(),
                indexerRestrictionChanged = {},
            )
            val machine = TradingStateMachine(
                environment = environment,
                localizer = localizer,
                formatter = null,
                maxSubaccountNumber = 0,
                useParentSubaccount = false,
                skipGoFast = skipGoFast,
                trackingProtocol = null,
            )
            machine.routerTokens(SKIP_TOKENS)
            machine.transfer("DEPOSIT", TransferInputField.type, environment = environment)
            machine.transfer(FROM_CHAIN, TransferInputField.chain, environment = environment)
            machine.transfer(TOKEN, TransferInputField.token, environment = environment)
            machine.transfer("6", TransferInputField.decimals, environment = environment)
            machine.internalState.input.transfer.swapVenues = listOf(
                InternalTransferVenueState(chain_id = "osmosis-1", name = "osmosis-pool"),
            )
            rest.postedBodies.clear()
            OnboardingSupervisor(
                stateMachine = machine,
                helper = helper,
                analyticsUtils = AnalyticsUtils(),
                configs = OnboardingConfigs(retrieveRoutes = false),
            ).transfer(
                data = "10",
                type = TransferInputField.size,
                accountAddress = ACCOUNT,
                sourceAddress = SOURCE,
                subaccountNumber = 0,
                injectiveAddress = injectiveAddress,
            )
            return rest.postedBodies
                .filter { (url, _) -> url.contains("msgs_direct") }
                .mapNotNull { (_, body) -> body }
        } finally {
            CctpConfig.cctpChainIds = previousCctp
        }
    }

    private fun assertInjectiveDeposit(body: String, includeOsmosisAndNeutron: Boolean) {
        val parser = Parser()
        val json = parser.decodeJsonObject(body)
        assertNotNull(json)
        assertEquals("1776", parser.asString(json["dest_asset_chain_id"]))
        assertEquals(
            "0xa00C59fF5a080D2b954d0c75e46E22a0c371235a",
            parser.asString(json["dest_asset_denom"]),
        )
        assertEquals("10000000", parser.asString(json["amount_in"]))
        assertEquals("true", parser.asString(json["allow_multi_tx"]))
        assertFalse(json.containsKey("swap_venues"))
        assertFalse(body.contains("noble"))
        assertFalse(body.contains("osmosis-pool"))

        val addresses = parser.asNativeMap(json["chain_ids_to_addresses"])
        assertEquals(INJECTIVE_ADDRESS, parser.asString(addresses?.get("injective-888")))
        assertEquals(INJECTIVE_EVM_ADDRESS, parser.asString(addresses?.get("1776")))
        assertEquals(ACCOUNT, parser.asString(addresses?.get("test")))
        assertEquals(SOURCE, parser.asString(addresses?.get(FROM_CHAIN)))
        assertNull(addresses?.get("grand-1"))
        if (includeOsmosisAndNeutron) {
            assertNotNull(parser.asString(addresses?.get("osmo-test-5")))
            assertNotNull(parser.asString(addresses?.get("pion-1")))
        } else {
            assertFalse(addresses?.containsKey("osmo-test-5") == true)
            assertFalse(addresses?.containsKey("pion-1") == true)
        }
    }

    companion object {
        private const val INJECTIVE_ADDRESS = "inj15qx9nl66pqxjh92dp367gm3z5rphzg669sh88w"
        private const val INJECTIVE_EVM_ADDRESS = "0xa00c59ff5a080d2b954d0c75e46e22a0c371235a"
        private const val ACCOUNT = "dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art"
        private const val SOURCE = "0x1111111111111111111111111111111111111111"
        private const val FROM_CHAIN = "1"
        private const val TOKEN = "0xusdc"
        private val SKIP_TOKENS = """
            {
              "chain_to_assets_map": {
                "1": {
                  "assets": [
                    {"denom": "0xusdc", "decimals": 6, "symbol": "USDC"}
                  ]
                }
              }
            }
        """.trimIndent()
    }
}
