package exchange.dydx.abacus.app.manager.v2

import exchange.dydx.abacus.AsyncAbacusStateManagerV2
import exchange.dydx.abacus.app.manager.NetworkTests
import exchange.dydx.abacus.app.manager.TestChain
import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.app.manager.TestState
import exchange.dydx.abacus.app.manager.TestWebSocket
import exchange.dydx.abacus.output.ComplianceStatus
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.state.supervisor.AppConfigsV2
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class V4RestrictionsTests : NetworkTests() {
    val mock = AbacusMockData()
    private val testCosmoAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
    private var stateManager: AsyncAbacusStateManagerV2 = resetStateManager()
    private var ioImplementations = stateManager.ioImplementations
    private var testRest = stateManager.ioImplementations.rest as? TestRest
    private var testWebSocket = stateManager.ioImplementations.webSocket as? TestWebSocket
    private var testChain = stateManager.ioImplementations.chain as? TestChain
    private var testState = stateManager.stateNotification as? TestState
    private var v4Adapter = stateManager.adaptor

    @BeforeTest
    fun reset() {
        stateManager = resetStateManager()
        ioImplementations = stateManager.ioImplementations
        testRest = stateManager.ioImplementations.rest as? TestRest
        testWebSocket = stateManager.ioImplementations.webSocket as? TestWebSocket
        testChain = stateManager.ioImplementations.chain as? TestChain
        testState = stateManager.stateNotification as? TestState
        v4Adapter = stateManager.adaptor
    }

    fun resetStateManager(): AsyncAbacusStateManagerV2 {
        val ioImplementations = BaseTests.testIOImplementations()
        val localizer = BaseTests.testLocalizer(ioImplementations)
        val uiImplementations = BaseTests.testUIImplementations(localizer)
        stateManager = AsyncAbacusStateManagerV2(
            "https://api.examples.com",
            "DEV",
            AppConfigsV2.forApp,
            ioImplementations,
            uiImplementations,
            TestState(),
            null,
        )
        stateManager.environmentId = "dydxprotocol-staging"
        return stateManager
    }

    private fun setStateMachineReadyToConnect(stateManager: AsyncAbacusStateManagerV2) {
        stateManager.readyToConnect = true
    }

    private fun setStateMachineConnected(stateManager: AsyncAbacusStateManagerV2) {
        setStateMachineReadyToConnect(stateManager)
        (ioImplementations.webSocket as? TestWebSocket)?.simulateConnected(true)
        (ioImplementations.webSocket as? TestWebSocket)?.simulateReceived(mock.connectionMock.connectedMessage)
    }

    private fun setStateMachineConnectedWithMarkets(stateManager: AsyncAbacusStateManagerV2) {
        setStateMachineConnected(stateManager)
        (ioImplementations.webSocket as? TestWebSocket)?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        stateManager.market = "ETH-USD"
    }

    private fun setStateMachineConnectedWithMarketsAndSubaccounts(stateManager: AsyncAbacusStateManagerV2) {
        setStateMachineConnectedWithMarkets(stateManager)
        stateManager.setAddresses(null, testCosmoAddress, false)
    }

    @Test
    fun when403() {
        reset()

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/height",
            "403",
        )

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)

        assertEquals(
            Restriction.GEO_RESTRICTED,
            stateManager.adaptor?.stateMachine?.state?.restriction?.restriction,
            "Expected geo restriction",
        )
    }

    @Test
    fun testGeoEndpointHandling() {
        reset()

        testChain!!.signCompliancePayload = """
        {
            "signedMessage": "1",
            "publicKey": "1",
            "timestamp": "2024-05-14T20:40:00.415Z"
        }
        """.trimIndent()

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/compliance/geoblock",
            """
                {
                    "status": "CLOSE_ONLY",
                    "reason": null,
                    "updatedAt": "2024-05-14T20:40:00.415Z"
                }
            """.trimIndent(),
        )
        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/compliance/screen/$testCosmoAddress",
            """
                {
                    "status": "CLOSE_ONLY",
                    "reason": null,
                    "updatedAt": "2024-05-14T20:40:00.415Z"
                }
            """.trimIndent(),
        )

        setStateMachineConnected(stateManager)
        stateManager.setAddresses(null, testCosmoAddress, true)

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/compliance/screen/$testCosmoAddress",
            """
                {
                    "status": "CLOSE_ONLY",
                    "reason": null,
                    "updatedAt": "2024-05-14T20:40:00.415Z"
                }
            """.trimIndent(),
        )

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/compliance/geoblock",
            """
                {
                    "status": "CLOSE_ONLY",
                    "reason": null,
                    "updatedAt": "2024-05-14T20:40:00.415Z"
                }
            """.trimIndent(),
        )

        assertEquals(
            ComplianceStatus.CLOSE_ONLY,
            stateManager.adaptor?.stateMachine?.state?.compliance?.status,
            "Expected CLOSE_ONLY restriction",
        )

        assertEquals(
            "2024-05-14T20:40:00.415Z",
            stateManager.adaptor?.stateMachine?.state?.compliance?.updatedAt,
            "Expected different updatedAt",
        )

        // expires at is 7 days in advance
        assertEquals(
            "2024-05-21T20:40:00.415Z",
            stateManager.adaptor?.stateMachine?.state?.compliance?.expiresAt,
            "Expected different expires at",
        )
    }

//    @Test
//    fun whenUserRestricted() {
//        reset()
//        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
//
//        testRest?.setResponse(
//            "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//            """
//                {
//                    "restricted": true
//                }
//            """.trimIndent()
//        )
//
//        setStateMachineReadyToConnect(stateManager)
//        setStateMachineConnected(stateManager)
//
//        stateManager.setAddresses(null, testAddress)
//        assertEquals(
//            Restriction.USER_RESTRICTED,
//            stateManager.adaptor?.stateMachine?.state?.restriction?.restriction,
//            "Expected user restriction"
//        )
//
//
//        compareExpectedRequests(
//            """
//                [
//                    "https://api.examples.com/configs/documentation.json",
//                    "https://indexer.v4staging.dydx.exchange/v4/time",
//                    "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
//                    "https://indexer.v4staging.dydx.exchange/v4/height",
//                    "https://api.examples.com/configs/markets.json",
//                    "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
//                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
//                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
//                    "https://api.examples.com/configs/exchanges.json",
//                    "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                    "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?n=2",
//                    "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                    "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?period=WEEKLY"
//                ]
//            """.trimIndent(),
//            testRest?.requests
//        )
//
//        testRest?.setResponse(
//            "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//            """
//                {
//                    "restricted": true
//                }
//            """.trimIndent()
//        )
//        stateManager.screen("cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm") { restriction ->
//
//            assertEquals(
//                Restriction.USER_RESTRICTED,
//                stateManager.adaptor?.stateMachine?.state?.restriction?.restriction,
//                "Expected user restriction"
//            )
//        }
//    }
}
