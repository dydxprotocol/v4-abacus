package exchange.dydx.abacus.app.manager

import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.state.manager.AppConfigs
import exchange.dydx.abacus.state.manager.AsyncAbacusStateManager
import exchange.dydx.abacus.state.manager.V4StateManagerAdaptor
import exchange.dydx.abacus.state.manager.setAddresses
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class V4RestrictionsTests {
    val mock = AbacusMockData()
    private val testCosmoAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
    private var stateManager: AsyncAbacusStateManager = resetStateManager()
    private var ioImplementations = stateManager.ioImplementations
    private var testRest = stateManager.ioImplementations.rest as? TestRest
    private var testWebSocket = stateManager.ioImplementations.webSocket as? TestWebSocket
    private var testChain = stateManager.ioImplementations.chain as? TestChain
    private var testState = stateManager.stateNotification as? TestState
    private var v4Adapter = stateManager.adaptor as? V4StateManagerAdaptor

    @BeforeTest
    fun reset() {
        stateManager = resetStateManager()
        ioImplementations = stateManager.ioImplementations
        testRest = stateManager.ioImplementations.rest as? TestRest
        testWebSocket = stateManager.ioImplementations.webSocket as? TestWebSocket
        testChain = stateManager.ioImplementations.chain as? TestChain
        testState = stateManager.stateNotification as? TestState
        v4Adapter = stateManager.adaptor as? V4StateManagerAdaptor
    }

    fun resetStateManager(): AsyncAbacusStateManager {
        val ioImplementations = BaseTests.testIOImplementations()
        val localizer = BaseTests.testLocalizer(ioImplementations)
        val uiImplementations = BaseTests.testUIImplementations(localizer)
        stateManager = AsyncAbacusStateManager(
            "https://api.examples.com",
            "DEV",
            AppConfigs.forApp,
            ioImplementations,
            uiImplementations,
            TestState(),
            null
        )
        stateManager.environmentId = "dydxprotocol-staging"
        return stateManager
    }

    private fun setStateMachineReadyToConnect(stateManager: AsyncAbacusStateManager) {
        stateManager.readyToConnect = true
    }

    private fun setStateMachineConnected(stateManager: AsyncAbacusStateManager) {
        setStateMachineReadyToConnect(stateManager)
        (ioImplementations.webSocket as? TestWebSocket)?.simulateConnected(true)
        (ioImplementations.webSocket as? TestWebSocket)?.simulateReceived(mock.connectionMock.connectedMessage)
    }

    private fun setStateMachineConnectedWithMarkets(stateManager: AsyncAbacusStateManager) {
        setStateMachineConnected(stateManager)
        (ioImplementations.webSocket as? TestWebSocket)?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        stateManager.market = "ETH-USD"
    }

    private fun setStateMachineConnectedWithMarketsAndSubaccounts(stateManager: AsyncAbacusStateManager) {
        setStateMachineConnectedWithMarkets(stateManager)
        stateManager.setAddresses(null, testCosmoAddress)
    }

    @Test
    fun when403() {
        reset()

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/height",
            "403"
        )

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)

        assertEquals(
            Restriction.GEO_RESTRICTED,
            stateManager.adaptor?.stateMachine?.state?.restriction?.restriction,
            "Expected geo restriction"
        )
    }

    @Test
    fun whenUserRestricted() {
        reset()
        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            """
                {
                    "restricted": true
                }
            """.trimIndent()
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        stateManager.setAddresses(null, testAddress)
        assertEquals(
            Restriction.USER_RESTRICTED,
            stateManager.adaptor?.stateMachine?.state?.restriction?.restriction,
            "Expected user restriction"
        )
        assertEquals(12, testRest?.requests?.size)
        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            testRest?.requests?.get(8)
        )
        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            testRest?.requests?.get(10)
        )


        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
            """
                {
                    "restricted": true
                }
            """.trimIndent()
        )
        stateManager.screen("cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm") { restriction ->

            assertEquals(
                Restriction.USER_RESTRICTED,
                stateManager.adaptor?.stateMachine?.state?.restriction?.restriction,
                "Expected user restriction"
            )
        }
    }

}