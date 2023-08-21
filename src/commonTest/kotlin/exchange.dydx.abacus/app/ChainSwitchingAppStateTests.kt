package exchange.dydx.abacus.app

import exchange.dydx.abacus.state.app.AppStateMachine
import exchange.dydx.abacus.state.app.V3AppStateMachineProtocol
import exchange.dydx.abacus.tests.mock.V3MockSigner
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ChainSwitchingAppStateTests {
    val mock = AbacusMockData()
    val appStateMachine = kotlin.run {
        val appStateMachine = AppStateMachine()
        appStateMachine.setChainId("5")
        (appStateMachine as? V3AppStateMachineProtocol)?.setSigner(V3MockSigner())
        return@run appStateMachine
    }

    @Test
    fun testAppState() {
        assertNotNull(appStateMachine, "App State Machine shouldn't be null")

        var response = appStateMachine.setChainId("5")
        assertNull(response.networkRequests)

        response = appStateMachine.setChainId("1")
        assertNull(response.networkRequests)

        response = appStateMachine.setReadyToConnect(true)
        assertNotNull(response.networkRequests)

        response = appStateMachine.setChainId("5")
        assertNotNull(response.networkRequests)

        response = appStateMachine.setChainId("5")
        assertNull(response.networkRequests)

        response = appStateMachine.setEnvironment("1")
        assertNotNull(response.networkRequests)

        response = appStateMachine.setEnvironment("1")
        assertNull(response.networkRequests)

        response = appStateMachine.setEnvironment("dydxprotocol-dev")
        assertNotNull(response.networkRequests)
        var socket = response.networkRequests?.socketRequests?.firstOrNull()
        assertNotNull(socket)
        assertEquals("ws", socket.url.scheme)
        var rest = response.networkRequests?.restRequests?.lastOrNull()
        assertNotNull(rest)
        assertEquals("http", rest.url.scheme)

        response = appStateMachine.setEnvironment("dydxprotocol-staging")
        assertNotNull(response.networkRequests)
        socket = response.networkRequests?.socketRequests?.firstOrNull()
        assertNotNull(socket)
        assertEquals("wss", socket.url.scheme)
        rest = response.networkRequests?.restRequests?.lastOrNull()
        assertNotNull(rest)
        assertEquals("https", rest.url.scheme)

        response = appStateMachine.setChainId("dydxprotocol-staging")
        assertNull(response.networkRequests)
    }
}