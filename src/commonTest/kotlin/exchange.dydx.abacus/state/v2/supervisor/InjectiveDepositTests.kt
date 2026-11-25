package exchange.dydx.abacus.state.supervisor

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.app.manager.TestChain
import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.output.account.InjectiveBalances
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.InternalInjectiveBalancesState
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.machine.setInjectiveBalances
import exchange.dydx.abacus.state.manager.IndexerURIs
import exchange.dydx.abacus.state.manager.InjectiveDepositConfig
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.InjectiveTxVectors
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.toHex
import io.ktor.util.decodeBase64Bytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InjectiveDepositTests {
    private data class CommitResult(val successful: Boolean, val error: ParsingError?, val data: Any?)

    private class Fixture {
        val io = BaseTests.testIOImplementations()
        val rest = io.rest as TestRest
        val chain = io.chain as TestChain
        val configs: V4StateManagerConfigs
        val machine: TradingStateMachine
        val supervisor: AccountSupervisor

        init {
            val environment = AbacusMockData().v4Environment
            val localizer = BaseTests.testLocalizer(io)
            configs = V4StateManagerConfigs("https://example.com", environment)
            configs.indexerConfig = IndexerURIs(api = INDEXER, socket = "wss://indexer.test")
            val helper = NetworkHelper(
                deploymentUri = "https://example.com",
                environment = environment,
                uiImplementations = BaseTests.testUIImplementations(localizer),
                ioImplementations = io,
                configs = configs,
                stateNotification = null,
                dataNotification = null,
                parser = Parser(),
                indexerRestrictionChanged = {},
            )
            machine = TradingStateMachine(
                environment = environment,
                localizer = localizer,
                formatter = null,
                maxSubaccountNumber = 0,
                useParentSubaccount = false,
                trackingProtocol = null,
            )
            supervisor = AccountSupervisor(
                stateMachine = machine,
                helper = helper,
                analyticsUtils = AnalyticsUtils(),
                configs = AccountConfigs.forApp,
                screening = false,
                accountAddress = ACCOUNT,
            )
            supervisor.injectiveAddress = InjectiveTxVectors.ADDRESS
            chain.injectivePublicKeyResponse = """{"publicKey":"${InjectiveTxVectors.PUBLIC_KEY}"}"""
            chain.signInjectiveResponse = """{"signature":"${InjectiveTxVectors.PLAIN_SIGNATURE}"}"""
        }

        val broadcastUrl get() = configs.injectiveBroadcastUrl()

        fun setBalances(usdc: String, inj: String) {
            val address = InjectiveTxVectors.ADDRESS
            rest.setResponse(
                configs.injectiveBalanceUrl(address, configs.injectiveUsdcDenom),
                """{"balance":{"denom":"${configs.injectiveUsdcDenom}","amount":"$usdc"}}""",
            )
            rest.setResponse(
                configs.injectiveBalanceUrl(address, "inj"),
                """{"balance":{"denom":"inj","amount":"$inj"}}""",
            )
        }

        fun setAccount() {
            rest.setResponse(
                configs.injectiveAccountUrl(InjectiveTxVectors.ADDRESS),
                """
                {"account":{"@type":"/injective.types.v1beta1.EthAccount","base_account":{
                  "address":"${InjectiveTxVectors.ADDRESS}","account_number":"${InjectiveTxVectors.ACCOUNT_NUMBER}",
                  "sequence":"${InjectiveTxVectors.SEQUENCE}"},"code_hash":"xdJGAYb3IzySfn2y3McDwOUAtlPKgic7e/rYBF2FpHA="}}
                """.trimIndent(),
            )
        }

        fun setBroadcastResponse(code: Int = 0, rawLog: String = "") {
            rest.setPostResponse(
                broadcastUrl,
                """{"tx_response":{"height":"0","txhash":"ABC123","code":$code,"raw_log":"$rawLog"}}""",
                200,
            )
        }

        val sponsorshipBodies: List<String?>
            get() = rest.postedBodies.filter { (url, _) -> url == SPONSORSHIP_URL }.map { (_, body) -> body }

        val broadcastBodies: List<String?>
            get() = rest.postedBodies.filter { (url, _) -> url == broadcastUrl }.map { (_, body) -> body }

        fun signedDocHex(): String {
            val payload = parse(chain.injectiveSignPayloads.single())
            return assertNotNull(payload["signDoc"]).decodeBase64Bytes().toHex()
        }

        fun commit(): CommitResult {
            var result: CommitResult? = null
            supervisor.commitInjectiveDeposit { successful, error, data ->
                result = CommitResult(successful, error, data)
            }
            return assertNotNull(result)
        }
    }

    @Test
    fun testInjectiveBalancesOutput() {
        val machine = Fixture().machine
        val belowThreshold = InternalInjectiveBalancesState(usdc = BigDecimal.fromInt(999_999), inj = BigDecimal.parseString("450000000000000"))

        assertTrue(machine.setInjectiveBalances(belowThreshold).changes.contains(Changes.accountBalances))
        val belowMinimum = assertNotNull(
            InjectiveBalances.create(null, machine.internalState.wallet.account.injectiveBalances),
        )
        assertEquals(0.999999, belowMinimum.usdc)
        assertEquals(0.00045, belowMinimum.inj)
        assertFalse(belowMinimum.hasPendingDeposit)
        assertTrue(machine.setInjectiveBalances(belowThreshold).changes.isEmpty())

        machine.setInjectiveBalances(InternalInjectiveBalancesState(usdc = BigDecimal.fromInt(1_000_000), inj = BigDecimal.ZERO))
        val atMinimum = assertNotNull(
            InjectiveBalances.create(null, machine.internalState.wallet.account.injectiveBalances),
        )
        assertEquals(1.0, atMinimum.usdc)
        assertTrue(atMinimum.hasPendingDeposit)
        assertNull(InjectiveBalances.create(null, null))
    }

    @Test
    fun testCommitWithEnoughInjSignsAndBroadcasts() {
        val fixture = Fixture()
        fixture.setBalances(usdc = "2500000", inj = "1000000000000000")
        fixture.setAccount()
        fixture.setBroadcastResponse()

        val result = fixture.commit()

        assertTrue(result.successful, result.error?.message)
        assertEquals("ABC123", result.data)
        assertTrue(fixture.sponsorshipBodies.isEmpty())
        assertEquals(BigDecimal.fromInt(2_500_000), fixture.machine.internalState.wallet.account.injectiveBalances?.usdc)
        assertTrue(fixture.signedDocHex().endsWith(InjectiveTxVectors.PLAIN_AUTH_INFO + InjectiveTxVectors.SIGN_DOC_SUFFIX))

        val broadcast = parse(assertNotNull(fixture.broadcastBodies.single()))
        assertEquals("BROADCAST_MODE_SYNC", broadcast["mode"])
        val txRaw = assertNotNull(broadcast["tx_bytes"]).decodeBase64Bytes().toHex()
        val signature = InjectiveTxVectors.PLAIN_SIGNATURE.decodeBase64Bytes().toHex()
        assertTrue(txRaw.endsWith("127e" + InjectiveTxVectors.PLAIN_AUTH_INFO + "1a40" + signature))
    }

    @Test
    fun testCommitWithLowInjUsesSponsorship() {
        val fixture = Fixture()
        fixture.setBalances(usdc = "2500000", inj = "10")
        fixture.setAccount()
        fixture.setBroadcastResponse()
        fixture.rest.setResponse(
            SPONSORSHIP_URL,
            """
            {
              "chainId": "injective-888",
              "granter": "inj1granter",
              "grantee": "${InjectiveTxVectors.ADDRESS}",
              "feeDenom": "inj",
              "spendLimit": "900000000000000",
              "expiresAt": "2026-11-22T00:00:00Z",
              "allowedMessages": ["/ibc.applications.transfer.v1.MsgTransfer"],
              "status": "CONFIRMED",
              "gasLimit": "500000",
              "gasPrice": "1200000000"
            }
            """.trimIndent(),
        )

        val result = fixture.commit()

        assertTrue(result.successful, result.error?.message)
        assertEquals("ABC123", result.data)
        val sponsorshipBody = parse(assertNotNull(fixture.sponsorshipBodies.single()))
        assertEquals(ACCOUNT, sponsorshipBody["address"])
        assertEquals(InjectiveTxVectors.ADDRESS, sponsorshipBody["injectiveAddress"])
        assertTrue(fixture.signedDocHex().endsWith(InjectiveTxVectors.GRANTED_AUTH_INFO + InjectiveTxVectors.SIGN_DOC_SUFFIX))
    }

    @Test
    fun testSponsorshipErrorMapsCodeAndSendsNothing() {
        val fixture = Fixture()
        fixture.setBalances(usdc = "2500000", inj = "0")
        fixture.rest.setPostResponse(
            SPONSORSHIP_URL,
            """{"errors":[{"msg":"Try again later","code":"GAS_SPONSORSHIP_COOLDOWN"}]}""",
            429,
        )

        val result = fixture.commit()

        assertFalse(result.successful)
        val error = assertNotNull(result.error)
        assertEquals("ERRORS.GAS_SPONSORSHIP_COOLDOWN", error.stringKey)
        assertTrue(error.message.startsWith("Not enough INJ to pay for gas. Send at least 0.00045 INJ"))
        assertTrue(fixture.chain.injectiveSignPayloads.isEmpty())
        assertTrue(fixture.broadcastBodies.isEmpty())
    }

    @Test
    fun testSponsorshipDisabledSendsNothing() {
        val previous = InjectiveDepositConfig.gas_sponsorship_enabled
        InjectiveDepositConfig.gas_sponsorship_enabled = false
        try {
            val fixture = Fixture()
            fixture.setBalances(usdc = "2500000", inj = "0")

            val result = fixture.commit()

            assertFalse(result.successful)
            assertNull(result.error?.stringKey)
            assertTrue(fixture.sponsorshipBodies.isEmpty())
            assertTrue(fixture.chain.injectiveSignPayloads.isEmpty())
        } finally {
            InjectiveDepositConfig.gas_sponsorship_enabled = previous
        }
    }

    @Test
    fun testZeroUsdcSendsNothing() {
        val fixture = Fixture()
        fixture.setBalances(usdc = "0", inj = "1000000000000000")

        val result = fixture.commit()

        assertFalse(result.successful)
        assertEquals("No USDC found on Injective", result.error?.message)
        assertTrue(fixture.sponsorshipBodies.isEmpty())
        assertTrue(fixture.chain.injectiveSignPayloads.isEmpty())
    }

    @Test
    fun testSigningErrorIsReturned() {
        val fixture = Fixture()
        fixture.setBalances(usdc = "2500000", inj = "1000000000000000")
        fixture.setAccount()
        fixture.chain.signInjectiveResponse = """{"error":{"message":"Injective key unavailable"}}"""

        val result = fixture.commit()

        assertFalse(result.successful)
        assertEquals("Injective key unavailable", result.error?.message)
        assertTrue(fixture.broadcastBodies.isEmpty())
    }

    @Test
    fun testBroadcastRejectionIsReturned() {
        val fixture = Fixture()
        fixture.setBalances(usdc = "2500000", inj = "1000000000000000")
        fixture.setAccount()
        fixture.setBroadcastResponse(code = 5, rawLog = "insufficient funds")

        val result = fixture.commit()

        assertFalse(result.successful)
        assertEquals("insufficient funds", result.error?.message)
    }

    @Test
    fun testMissingAccountSendsNothing() {
        val fixture = Fixture()
        fixture.setBalances(usdc = "2500000", inj = "1000000000000000")

        val result = fixture.commit()

        assertFalse(result.successful)
        assertEquals("Injective account not found", result.error?.message)
        assertTrue(fixture.chain.injectiveSignPayloads.isEmpty())
    }

    companion object {
        private const val INDEXER = "https://indexer.test"
        private const val SPONSORSHIP_URL = "$INDEXER/v4/gasSponsorship"
        private const val ACCOUNT = "dydx14zzueazeh0hj67cghhf9jypslcf9sh2n5k6art"

        private fun parse(json: String): Map<String, String?> {
            val parser = Parser()
            return assertNotNull(parser.decodeJsonObject(json)).mapValues { (_, value) -> parser.asString(value) }
        }
    }
}
