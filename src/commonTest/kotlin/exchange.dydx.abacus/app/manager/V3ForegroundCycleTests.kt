package exchange.dydx.abacus.app.manager

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.state.manager.AsyncAbacusStateManager
import exchange.dydx.abacus.state.manager.V4StateManagerAdaptor
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class V3ForegroundCycleTests {
    val mock = AbacusMockData()
    private val testEthereumAddress = "0x"
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
            "https://dydx-v4-shared-resources.vercel.app/config/staging/dev_endpoints.json",
            "/config/staging/dev_endpoints.json",
            ioImplementations,
            uiImplementations,
            TestState(),
            null
        )
        stateManager.environmentId = "5"
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
        stateManager.accountAddress = testEthereumAddress
    }

    @Test
    fun setReadyToConnectShouldQueueInitialRequests() {
        reset()

        setStateMachineReadyToConnect(stateManager)

        assertEquals(
            "wss://api.stage.dydx.exchange/v3/ws",
            testWebSocket?.connectUrl,
            "WebSocket should be connected to correct url"
        )
        assertEquals(
            4,
            testRest?.requests?.size,
            "Should have queued 4 requests"
        )
        assertEquals(
            "https://api.stage.dydx.exchange/v3/time",
            testRest?.requests?.get(0),
            "Request to time endpoint should be present"
        )
        assertEquals(
            "https://dydx-v4-shared-resources.vercel.app/config/markets.json",
            testRest?.requests?.get(1),
            "Request to time endpoint should be present"
        )
        assertEquals(
            "https://dydx-v4-shared-resources.vercel.app/config/fee_tiers.json",
            testRest?.requests?.get(2),
            "Request to height endpoint should be present"
        )
        assertEquals(
            "https://dydx-v4-shared-resources.vercel.app/config/fee_discounts.json",
            testRest?.requests?.get(3),
            "Request to height endpoint should be present"
        )
    }

    @Test
    fun whenConnectedToWSMarketSubscribeIsQueued() {
        reset()

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)

        assertEquals(
            1,
            testWebSocket?.messages?.size,
            "Should have queued 1 websocket requests"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v3_markets"}
            """.trimIndent(),
            testWebSocket?.messages?.get(0),
            "1st message should be v4_markets subscribe"
        )
    }

    @Test
    fun whenMarketsSocketIsSubscribedSummaryShouldBeValid() {
        reset()

        assertEquals(null, stateManager.adaptor?.stateMachine?.state?.marketsSummary)

        setStateMachineReadyToConnect(stateManager)
        assertNotNull(stateManager.adaptor?.stateMachine?.state?.marketsSummary)
        assertNotNull(stateManager.adaptor?.stateMachine?.state?.assets)
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)

        assertNotNull(stateManager.adaptor?.stateMachine?.state?.marketsSummary)
    }

    @Test
    fun setMarketShouldStartOrderbookAndTradeSocketRequests() {
        reset()

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        stateManager.market = "ETH-USD"

        assertEquals(
            3,
            testWebSocket?.messages?.size,
            "Should have queued 3 websocket requests"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v3_trades","id":"ETH-USD"}
            """.trimIndent(),
            testWebSocket?.messages?.get(1),
            "2nd message should be v4_trades subscribe"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v3_orderbook","id":"ETH-USD","batched":"true"}
            """.trimIndent(),
            testWebSocket?.messages?.get(2),
            "3nd message should be v4_orderbook subscribe"
        )

        assertEquals(
            5,
            testRest?.requests?.size,
            "Should have queued 5 REST requests"
        )

        assertEquals(
            "https://api.stage.dydx.exchange/v3/historical-funding/ETH-USD",
            testRest?.requests?.get(4),
            "Request to historical funding endpoint should be present"
        )


        stateManager.market = "BTC-USD"

        assertEquals(
            7,
            testWebSocket?.messages?.size,
            "Should have queued 7 websocket requests"
        )
        assertEquals(
            """
                {"type":"unsubscribe","channel":"v3_trades","id":"ETH-USD"}
            """.trimIndent(),
            testWebSocket?.messages?.get(3),
            "5th message should be v4_trades unsubscribe"
        )
        assertEquals(
            """
                {"type":"unsubscribe","channel":"v3_orderbook","id":"ETH-USD"}
            """.trimIndent(),
            testWebSocket?.messages?.get(4),
            "6th message should be v4_orderbook unsubscribe"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v3_trades","id":"BTC-USD"}
            """.trimIndent(),
            testWebSocket?.messages?.get(5),
            "7th message should be v4_trades subscribe"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v3_orderbook","id":"BTC-USD","batched":"true"}
            """.trimIndent(),
            testWebSocket?.messages?.get(6),
            "8th message should be v4_orderbook subscribe"
        )

        assertEquals(
            6,
            testRest?.requests?.size,
            "Should have queued 7 REST requests"
        )

        assertEquals(
            "https://api.stage.dydx.exchange/v3/historical-funding/BTC-USD",
            testRest?.requests?.get(5),
            "Request to historical funding endpoint should be present"
        )
    }

    @Test
    fun historicalFundingShouldCreateSubsequentPaginatedRequests() {
        reset()
        testRest?.setResponse(
            "https://api.stage.dydx.exchange/v3/historical-funding/ETH-USD",
            mock.historicalFundingsMock.call
        )

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.marketsChannel.subscribed)
        stateManager.market = "ETH-USD"

        assertNotNull(stateManager.adaptor?.stateMachine?.state?.historicalFundings?.get("ETH-USD"))

        /* Only getting historical funding rate once for now */

        assertEquals(
            7,
            testRest?.requests?.size,
            "Should have queued 7 REST requests"
        )

        assertEquals(
            "https://api.stage.dydx.exchange/v3/historical-funding/ETH-USD",
            testRest?.requests?.get(5),
            "Request to historical funding endpoint should be present"
        )
    }

    @Test
    fun tradesChannelSubscribeShouldNotQueueAnyOtherRequests() {
        reset()

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        stateManager.market = "ETH-USD"

        val previousRestSize = testRest?.requests?.size
        val previousSocketSize = testWebSocket?.messages?.size
        testWebSocket?.simulateReceived(mock.tradesChannel.subscribed)

        assertEquals(previousRestSize, testRest?.requests?.size)
        assertEquals(previousSocketSize, testWebSocket?.messages?.size)
    }

    @Test
    fun orderbookChannelSubscribeShouldNotQueueAnyOtherRequests() {
        reset()

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.orderbookChannel.subscribed)
        stateManager.market = "ETH-USD"

        val previousRestSize = testRest?.requests?.size
        val previousSocketSize = testWebSocket?.messages?.size
        testWebSocket?.simulateReceived(mock.tradesChannel.subscribed)

        assertEquals(previousRestSize, testRest?.requests?.size)
        assertEquals(previousSocketSize, testWebSocket?.messages?.size)
    }

    @Test
    fun connectWalletWithNoAccountShouldStartAccountAndPNLRequests() {
        reset()
        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        val testAddress = "0xsecondaryFakeAddress"
        stateManager.accountAddress = testAddress

        assertEquals(5, testRest?.requests?.size)
        assertEquals(
            "https://api.stage.dydx.exchange/v3/historical-pnl",
            testRest?.requests?.get(4)
        )
    }

    @Test
    fun connectWalletWithExistingAccountShouldStartAccountAndPNLRequests() {
        reset()

        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)


        stateManager.accountAddress = testAddress

        assertEquals(5, testRest?.requests?.size)
        assertEquals(
            "https://api.stage.dydx.exchange/v3/historical-pnl",
            testRest?.requests?.get(4)
        )

        assertEquals(2, testWebSocket?.messages?.size)
        assertEquals(
            """
                {"type":"subscribe","channel":"v3_accounts","accountNumber":"0"}
            """.trimIndent(),
            testWebSocket?.messages?.get(1)
        )
    }

    @Test
    fun historicalPNLShouldCreateSubsequentPaginatedRequests() {
        reset()

        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/$testAddress",
            mock.accountsChannel.v4accountsReceived
        )
        testRest?.setResponse(
            "https://api.stage.dydx.exchange/v3/historical-pnl",
            mock.historicalPNL.firstCall
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        stateManager.accountAddress = testAddress

        assertEquals(6, testRest?.requests?.size)
        assertEquals(
            "https://api.stage.dydx.exchange/v3/historical-pnl",
            testRest?.requests?.get(4)
        )
        assertEquals(
            "https://api.stage.dydx.exchange/v3/historical-pnl?createdAtOrAfter=2022-08-08T21:07:24.581Z",
            testRest?.requests?.get(5)
        )
    }

    @Test
    fun connectWalletWithExistingAccountShouldUnsubscribeFromPreviousAccountAndStartNewAccountRequets() {
        reset()

        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
        val secondAddress = "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f"

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/$testAddress",
            mock.accountsChannel.v4accountsReceived
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        stateManager.accountAddress = testAddress

        testWebSocket?.simulateReceived(mock.accountsChannel.v4_subscribed)
        stateManager.accountAddress = secondAddress

        assertEquals(6, testRest?.requests?.size)
        assertEquals(
            "https://api.stage.dydx.exchange/v3/historical-pnl",
            testRest?.requests?.get(5)
        )

        assertEquals(4, testWebSocket?.messages?.size)
        assertEquals(
            """
                {"type":"unsubscribe","channel":"v3_accounts","accountNumber":"0"}
            """.trimIndent(),
            testWebSocket?.messages?.get(2)
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v3_accounts","accountNumber":"0"}
            """.trimIndent(),
            testWebSocket?.messages?.get(3)
        )
    }

    @Test
    fun settingWalletAddressToNullShoulUnsubscribeFromSubaccountsChannel() {
        reset()

        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/$testAddress",
            mock.accountsChannel.v4accountsReceived
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        stateManager.accountAddress = testAddress

        testWebSocket?.simulateReceived(mock.accountsChannel.v4_subscribed)
        stateManager.accountAddress = null

        assertEquals(5, testRest?.requests?.size)
        assertEquals(3, testWebSocket?.messages?.size)
        assertEquals(
            """
                {"type":"unsubscribe","channel":"v3_accounts","accountNumber":"0"}
            """.trimIndent(),
            testWebSocket?.messages?.get(2)
        )
    }

    @Test
    fun setReadyToConnectToFalseShouldResetNetworkConnectionChangesAndErrors() {
        reset()
        setStateMachineReadyToConnect(stateManager)

        val restCount = testRest?.requests?.size
        val socketCount = testWebSocket?.messages?.size
        stateManager.readyToConnect = false

        assertEquals(restCount, testRest?.requests?.size)
        assertEquals(socketCount, testWebSocket?.messages?.size)
    }
}