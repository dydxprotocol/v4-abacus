package exchange.dydx.abacus.app

import exchange.dydx.abacus.state.app.AppStateMachine
import exchange.dydx.abacus.state.app.V3AppStateMachineProtocol
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.app.signer.V3ApiKey
import exchange.dydx.abacus.tests.mock.V3MockSigner
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class V3AppStateTests {
    val mock = AbacusMockData()
    val appStateMachine = kotlin.run {
        val appStateMachine = AppStateMachine()
        appStateMachine.setChainId("5")
        (appStateMachine as? V3AppStateMachineProtocol)?.setSigner(V3MockSigner())
        return@run appStateMachine
    }

    @Test
    fun testAppState() {
        testForegroundCycle()
        testBackgroundAndForegroundCycle()
        testSwitchingNetworkCycle()
    }

    fun testForegroundCycle() {
        assertNotNull(appStateMachine, "App State Machine shouldn't be null")

        /*
        Initial connection
         */
        var state = appStateMachine.setChainId("5")
        assertNull(state.networkRequests)
        state = appStateMachine.setReadyToConnect(true)
        var requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests)
        assertNotNull(requests.restRequests)

        state = appStateMachine.setCandlesResolution("15MINS")

        /*
        Connected
         */
        val market = appStateMachine.market()
        val ethereumAddress = appStateMachine.accountAddress()
        state = appStateMachine.processSocketResponse(
            AbUrl.fromString("wss://api.stage.dydx.exchange/v3/ws"),
            mock.connectionMock.connectedMessage
        )
        requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests)
        assertNull(requests.restRequests)
        assertNull(state.errors)

        var socketRequestsCount = 1 // markets
        if (market != null) {
            socketRequestsCount += 2
        }
        if (ethereumAddress != null) {
            socketRequestsCount += 1
        }
        assertEquals(socketRequestsCount, requests.socketRequests!!.size)

        appStateMachine.processHttpResponse(
            AbUrl.fromString("https://dydx-v4-shared-resources.vercel.app/v4/markets.json"),
            mock.marketsConfigurations.configurations
        )
        /* Markets Connected */
        state = appStateMachine.processSocketResponse(
            AbUrl.fromString("wss://api.stage.dydx.exchange/v3/ws"),
            mock.marketsChannel.subscribed
        )
        requests = state.networkRequests
        assertNotNull(requests)
        
        assertNotNull(state.state)
        assertNotNull(state.state?.marketsSummary)
        assertNotNull(state.state?.marketsSummary?.markets)

        /* Select Market */
        val newMarket = "ETH-USD"
        state = appStateMachine.setMarket(newMarket)
        requests = state.networkRequests
        if (newMarket != market) {
            assertNotNull(requests)
            assertNotNull(requests.socketRequests)

            assertEquals(if (market != null) 4 else 2, requests.socketRequests!!.size)
        } else {
            assertNull(requests)
        }

        val restRequests = requests?.restRequests
        assertNotNull(restRequests)
        assertEquals(2, restRequests.size)

        val historicalFunding = restRequests.first()
        assertEquals("/v3/historical-funding/ETH-USD", historicalFunding.url.path)
        val candles = restRequests[1]
        assertEquals("/v3/candles/ETH-USD", candles.url.path)


        /* Trades Connected */
        state = appStateMachine.processSocketResponse(
            AbUrl.fromString("wss://api.stage.dydx.exchange/v3/ws"),
            mock.tradesChannel.subscribed
        )
        requests = state.networkRequests
        assertNull(requests)

        /* Orderbook Connected */
        state = appStateMachine.processSocketResponse(
            AbUrl.fromString("wss://api.stage.dydx.exchange/v3/ws"),
            mock.orderbookChannel.subscribed
        )
        requests = state.networkRequests
        assertNull(requests)

        /*
        Connect Wallet
        Todo: Should get the info needed for the PNL graph
        */
        val newEthereumAddress = "0x"
        state = (appStateMachine as V3AppStateMachineProtocol).setWalletEthereumAddress(newEthereumAddress,
            V3ApiKey("key", "secret", "passPhrase"))
        requests = state.networkRequests
        if (newEthereumAddress != ethereumAddress) {
            assertNotNull(requests)
            assertNotNull(requests.socketRequests)
            assertEquals(if (ethereumAddress != null) 2 else 1, requests.socketRequests!!.size)

            val socketRequest = requests.socketRequests?.firstOrNull()
            assertNotNull(socketRequest)
            assertEquals(socketRequest.private, false)
            assertNull(socketRequest.signingRequest)
            assertTrue(socketRequest.text?.contains("Dummy") == true)
        } else {
            assertNull(requests)
        }

        /* Orderbook Connected */
        state = appStateMachine.processSocketResponse(
            AbUrl.fromString("wss://api.stage.dydx.exchange/v3/ws"),
            mock.accountsChannel.subscribed
        )
        requests = state.networkRequests
        assertNotNull(requests)


        /* PNL Received */
        state = appStateMachine.processHttpResponse(
            AbUrl.fromString("https://api.stage.dydx.exchange/v3/historical-pnl"),
            mock.historicalPNL.firstCall
        )
        requests = state.networkRequests
        assertNotNull(requests?.restRequests)



        /* Candles Received */
        state = appStateMachine.processHttpResponse(
            AbUrl.fromString("https://api.stage.dydx.exchange/v3/candles/ETH-USD"),
            mock.candles.firstCall
        )
        requests = state.networkRequests
        assertNotNull(requests?.restRequests)


        /* Historical Funding Received */
        state = appStateMachine.processHttpResponse(
            AbUrl.fromString("https://api.stage.dydx.exchange/v3/historical-funding/ETH-USD"),
            mock.historicalFundingsMock.call
        )
        requests = state.networkRequests
        assertNotNull(requests)

        state = appStateMachine.setMarket(null)
        requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests)
        assertNull(requests.restRequests)

        assertEquals(2, requests.socketRequests!!.size)


        state = appStateMachine.setWalletEthereumAddress(null, null)
        requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests)
        assertEquals(1, requests.socketRequests!!.size)
//        assertNotNull(requests.restRequests)
    }

    fun testBackgroundAndForegroundCycle() {
        /* Backgrounded */
        val state = appStateMachine.setReadyToConnect(false)
        assertNull(state.networkRequests)

        testForegroundCycle()
    }

    fun testSwitchingNetworkCycle() {
        val state = appStateMachine.setChainId("1")
        assertNotNull(state.networkRequests)
    }
}