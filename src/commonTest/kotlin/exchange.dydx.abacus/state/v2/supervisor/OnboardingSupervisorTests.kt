package exchange.dydx.abacus.state.supervisor

import exchange.dydx.abacus.app.manager.TestChain
import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.state.InternalTransferVenueState
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.abacus.state.machine.routerTokens
import exchange.dydx.abacus.state.machine.transfer
import exchange.dydx.abacus.state.manager.CctpChainTokenInfo
import exchange.dydx.abacus.state.manager.CctpConfig
import exchange.dydx.abacus.state.manager.V4Environment
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
    private val parser = Parser()

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

    @Test
    fun testCctpWithdrawalRouteGoesThroughInjective() {
        val bodies = withdrawalBodies(cctp = true, injectiveAddress = INJECTIVE_ADDRESS)
        assertEquals(1, bodies.size)
        val json = assertWithdrawalFromDydx(bodies.first(), includeOsmosisAndNeutron = false)
        assertEquals("true", parser.asString(json["smart_relay"]))
        assertEquals<List<String?>?>(listOf("CCTP", "IBC"), parser.asList(json["bridges"])?.map { parser.asString(it) })
    }

    @Test
    fun testNonCctpWithdrawalRouteUsesInjectiveAddresses() {
        val bodies = withdrawalBodies(cctp = false, injectiveAddress = INJECTIVE_ADDRESS)
        assertEquals(1, bodies.size)
        val json = assertWithdrawalFromDydx(bodies.first(), includeOsmosisAndNeutron = true)
        assertFalse(json.containsKey("swap_venues"))
        assertFalse(bodies.first().contains("osmosis-pool"))
    }

    @Test
    fun testWithdrawalRouteWaitsForInjectiveAddress() {
        assertTrue(withdrawalBodies(cctp = true, injectiveAddress = null).isEmpty())
        assertTrue(withdrawalBodies(cctp = false, injectiveAddress = null).isEmpty())
    }

    @Test
    fun testWithdrawalToInjectiveKeepsDestinationAddress() {
        val bodies = withdrawalBodies(
            cctp = false,
            injectiveAddress = INJECTIVE_ADDRESS,
            toChain = "injective-888",
            toAddress = OTHER_INJECTIVE_ADDRESS,
        )
        assertEquals(1, bodies.size)
        val addresses = parser.asNativeMap(parser.decodeJsonObject(bodies.first())?.get("chain_ids_to_addresses"))
        assertEquals(OTHER_INJECTIVE_ADDRESS, parser.asString(addresses?.get("injective-888")))
    }

    private fun depositBodies(skipGoFast: Boolean, cctp: Boolean, injectiveAddress: String?): List<String> {
        return msgsDirectBodies(
            skipGoFast = skipGoFast,
            cctpToken = if (cctp) CctpChainTokenInfo(FROM_CHAIN, TOKEN) else null,
            injectiveAddress = injectiveAddress,
            field = TransferInputField.size,
        ) { environment ->
            transfer("DEPOSIT", TransferInputField.type, environment = environment)
            transfer(FROM_CHAIN, TransferInputField.chain, environment = environment)
            transfer(TOKEN, TransferInputField.token, environment = environment)
            transfer("6", TransferInputField.decimals, environment = environment)
        }
    }

    private fun withdrawalBodies(
        cctp: Boolean,
        injectiveAddress: String?,
        toChain: String = TO_CHAIN,
        toAddress: String = DESTINATION,
    ): List<String> {
        return msgsDirectBodies(
            skipGoFast = false,
            cctpToken = if (cctp) CctpChainTokenInfo(toChain, TOKEN) else null,
            injectiveAddress = injectiveAddress,
            field = TransferInputField.usdcSize,
        ) { environment ->
            transfer("WITHDRAWAL", TransferInputField.type, environment = environment)
            transfer(toChain, TransferInputField.chain, environment = environment)
            transfer(TOKEN, TransferInputField.token, environment = environment)
            transfer(toAddress, TransferInputField.address, environment = environment)
        }
    }

    private fun msgsDirectBodies(
        skipGoFast: Boolean,
        cctpToken: CctpChainTokenInfo?,
        injectiveAddress: String?,
        field: TransferInputField,
        setUpInput: TradingStateMachine.(V4Environment) -> Unit,
    ): List<String> {
        val previousCctp = CctpConfig.cctpChainIds
        CctpConfig.cctpChainIds = cctpToken?.let { listOf(it) }
        try {
            val io = BaseTests.testIOImplementations()
            val rest = io.rest as TestRest
            (io.chain as TestChain).simulateWithdrawResponse = SIMULATE_WITHDRAW_RESPONSE
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
            machine.setUpInput(environment)
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
                type = field,
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
        assertEquals(SOURCE, parser.asString(addresses?.get(FROM_CHAIN)))
        assertInjectiveHops(addresses, includeOsmosisAndNeutron)
    }

    private fun assertWithdrawalFromDydx(body: String, includeOsmosisAndNeutron: Boolean): Map<String, Any> {
        val json = parser.decodeJsonObject(body)
        assertNotNull(json)
        assertEquals("test", parser.asString(json["source_asset_chain_id"]))
        assertEquals(
            AbacusMockData().v4Environment.tokens["usdc"]?.denom,
            parser.asString(json["source_asset_denom"]),
        )
        assertEquals(TO_CHAIN, parser.asString(json["dest_asset_chain_id"]))
        assertEquals(TOKEN, parser.asString(json["dest_asset_denom"]))
        // 10 USDC minus the simulated 0.01 USDC gas fee
        assertEquals("9990000", parser.asString(json["amount_in"]))
        assertEquals("false", parser.asString(json["allow_multi_tx"]))
        assertFalse(body.contains("noble"))

        val addresses = parser.asNativeMap(json["chain_ids_to_addresses"])
        assertEquals(DESTINATION, parser.asString(addresses?.get(TO_CHAIN)))
        assertInjectiveHops(addresses, includeOsmosisAndNeutron)
        return json
    }

    private fun assertInjectiveHops(addresses: Map<String, Any>?, includeOsmosisAndNeutron: Boolean) {
        assertEquals(INJECTIVE_ADDRESS, parser.asString(addresses?.get("injective-888")))
        assertEquals(INJECTIVE_EVM_ADDRESS, parser.asString(addresses?.get("1776")))
        assertEquals(ACCOUNT, parser.asString(addresses?.get("test")))
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
        private const val OTHER_INJECTIVE_ADDRESS = "inj14au322k9munkmx5wrchz9q30juf5wjgz2cfqku"
        private const val ACCOUNT = "dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art"
        private const val SOURCE = "0x1111111111111111111111111111111111111111"
        private const val DESTINATION = "0x2222222222222222222222222222222222222222"
        private const val FROM_CHAIN = "1"
        private const val TO_CHAIN = "42161"
        private const val TOKEN = "0xusdc"
        private val SIMULATE_WITHDRAW_RESPONSE = """
            {"amount": [{"amount": "10000", "denom": "ibc/usdc"}]}
        """.trimIndent()
        private val SKIP_TOKENS = """
            {
              "chain_to_assets_map": {
                "1": {
                  "assets": [
                    {"denom": "0xusdc", "decimals": 6, "symbol": "USDC"}
                  ]
                },
                "42161": {
                  "assets": [
                    {"denom": "0xusdc", "decimals": 6, "symbol": "USDC"}
                  ]
                },
                "injective-888": {
                  "assets": [
                    {"denom": "0xusdc", "decimals": 6, "symbol": "USDC"}
                  ]
                }
              }
            }
        """.trimIndent()
    }
}
