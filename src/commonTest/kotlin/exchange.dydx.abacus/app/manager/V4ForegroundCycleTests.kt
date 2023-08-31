package exchange.dydx.abacus.app.manager

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.state.manager.AsyncAbacusStateManager
import exchange.dydx.abacus.state.manager.V4StateManagerAdaptor
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.values
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class V4ForegroundCycleTests {
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
            "https://dydx-v4-shared-resources.vercel.app/config/staging/dev_endpoints.json",
            "/config/staging/dev_endpoints.json",
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
        stateManager.accountAddress = testCosmoAddress
    }

    @Test
    fun setReadyToConnectShouldQueueInitialRequests() {
        reset()

        setStateMachineReadyToConnect(stateManager)

        assertEquals(
            "wss://indexer.v4staging.dydx.exchange/v4/ws",
            testWebSocket?.connectUrl,
            "WebSocket should be connected to correct url"
        )
        assertEquals(
            5,
            testRest?.requests?.size,
            "Should have queued 4 requests"
        )
        assertContains(
            testRest?.requests?.toTypedArray()!!,
            "https://indexer.v4staging.dydx.exchange/v4/time",
            "Request to time endpoint should be present"
        )
        assertContains(
            testRest?.requests?.toTypedArray()!!,
            "https://dydx-v4-shared-resources.vercel.app/v4/markets.json",
            "Request to time endpoint should be present"
        )
        assertContains(
            testRest?.requests?.toTypedArray()!!,
            "https://indexer.v4staging.dydx.exchange/v4/height",
            "Request to height endpoint should be present"
        )
        assertContains(
            testRest?.requests?.toTypedArray()!!,
            "https://squid-api-git-feat-cosmos-maintestnet-0xsquid.vercel.app/v1/tokens",
            "Request to squid tokens endpoint should be present"
        )
        assertContains(
            testRest?.requests?.toTypedArray()!!,
            "https://squid-api-git-feat-cosmos-maintestnet-0xsquid.vercel.app/v1/chains",
            "Request to squid chains endpoint should be present"
        )
        assertEquals(
            stateManager.adaptor?.stateMachine?.state?.assets?.get("BTC")?.resources?.imageUrl,
            "https://trader-fe.vercel.app/currenties/btc.svg",
            "Asset image url should be correct"
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
                {"type":"subscribe","channel":"v4_markets","batched":"true"}
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
            4,
            testWebSocket?.messages?.size,
            "Should have queued 4 websocket requests"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v4_trades","id":"ETH-USD","batched":"true"}
            """.trimIndent(),
            testWebSocket?.messages?.get(1),
            "2nd message should be v4_trades subscribe"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v4_orderbook","id":"ETH-USD","batched":"true"}
            """.trimIndent(),
            testWebSocket?.messages?.get(2),
            "3nd message should be v4_orderbook subscribe"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v4_candles","id":"ETH-USD/1DAY"}
            """.trimIndent(),
            testWebSocket?.messages?.get(3),
            "4th message should be v4_candles subscribe"
        )

        assertEquals(
            8,
            testRest?.requests?.size,
            "Should have queued 8 REST requests"
        )

        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD",
            testRest?.requests?.get(6),
            "Request to historical funding endpoint should be present"
        )

        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/ETH-USD?resolution=1DAY",
            testRest?.requests?.get(7),
            "Request to candles endpoint should be present"
        )


        stateManager.market = "BTC-USD"

        assertEquals(
            10,
            testWebSocket?.messages?.size,
            "Should have queued 10 websocket requests"
        )
        assertEquals(
            """
                {"type":"unsubscribe","channel":"v4_trades","id":"ETH-USD"}
            """.trimIndent(),
            testWebSocket?.messages?.get(4),
            "5th message should be v4_trades unsubscribe"
        )
        assertEquals(
            """
                {"type":"unsubscribe","channel":"v4_orderbook","id":"ETH-USD"}
            """.trimIndent(),
            testWebSocket?.messages?.get(5),
            "6th message should be v4_orderbook unsubscribe"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v4_trades","id":"BTC-USD","batched":"true"}
            """.trimIndent(),
            testWebSocket?.messages?.get(6),
            "7th message should be v4_trades subscribe"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v4_orderbook","id":"BTC-USD","batched":"true"}
            """.trimIndent(),
            testWebSocket?.messages?.get(7),
            "8th message should be v4_orderbook subscribe"
        )
        assertEquals(
            """
                {"type":"unsubscribe","channel":"v4_candles","id":"ETH-USD/1DAY"}
            """.trimIndent(),
            testWebSocket?.messages?.get(8),
            "9th message should be v4_candles unsubscribe"
        )
        assertEquals(
            """
                {"type":"subscribe","channel":"v4_candles","id":"BTC-USD/1DAY"}
            """.trimIndent(),
            testWebSocket?.messages?.get(9),
            "10th message should be v4_candles subscribe"
        )

        assertEquals(
            10,
            testRest?.requests?.size,
            "Should have queued 10 REST requests"
        )

        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/BTC-USD",
            testRest?.requests?.get(8),
            "Request to historical funding endpoint should be present"
        )

        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/BTC-USD?resolution=1DAY",
            testRest?.requests?.get(9),
            "Request to candles endpoint should be present"
        )
    }

    @Test
    fun historicalFundingShouldCreateSubsequentPaginatedRequests() {
        reset()
        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD",
            mock.historicalFundingsMock.call
        )

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        stateManager.market = "ETH-USD"

        assertNotNull(stateManager.adaptor?.stateMachine?.state?.historicalFundings?.get("ETH-USD"))

        /* Only getting historical funding rate once for now */

        assertEquals(
            8,
            testRest?.requests?.size,
            "Should have queued 6 REST requests"
        )

        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD",
            testRest?.requests?.get(6),
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

        assertEquals(6, testRest?.requests?.size)
        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/0xsecondaryFakeAddress",
            testRest?.requests?.get(5)
        )
    }

    @Test
    fun connectWalletWithExistingAccountShouldStartAccountAndPNLRequests() {
        reset()

        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/$testAddress",
            mock.accountsChannel.v4accountsReceived
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)


        stateManager.accountAddress = testAddress

        assertEquals(9, testRest?.requests?.size)
        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/fills?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
            testRest?.requests?.get(6)
        )
        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
            testRest?.requests?.get(8)
        )

        assertEquals(2, testWebSocket?.messages?.size)
        assertEquals(
            """
                {"type":"subscribe","channel":"v4_subaccounts","id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0"}
            """.trimIndent(),
            testWebSocket?.messages?.get(1)
        )

        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_channel_data_with_orders)

        assertEquals(1, stateManager.adaptor?.notifications?.size)
        val notification = stateManager.adaptor?.notifications?.values()?.first()
        assertEquals(
            "order:b812bea8-29d3-5841-9549-caa072f6f8a8",
            notification?.id
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
            "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
            mock.historicalPNL.firstCall
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        stateManager.accountAddress = testAddress

        assertEquals(10, testRest?.requests?.size)
        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
            testRest?.requests?.get(8)
        )
        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?createdAtOrAfter=2022-08-08T21:07:24.581Z&address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
            testRest?.requests?.get(9)
        )
    }


    @Test
    fun connectWalletWithExistingAccountShouldUnsubscribeFromPreviousAccountAndStartNewAccountRequests() {
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

        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_subscribed)
        stateManager.accountAddress = secondAddress

        assertEquals(10, testRest?.requests?.size)
        assertEquals(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
            testRest?.requests?.get(9)
        )
        assertEquals(3, testWebSocket?.messages?.size)
        assertEquals(
            """
                {"type":"unsubscribe","channel":"v4_subaccounts","id":"$testAddress/0"}
            """.trimIndent(),
            testWebSocket?.messages?.get(2)
        )
    }

    @Test
    fun settingWalletCosmoAddressToNullShoulUnsubscribeFromSubaccountsChannel() {
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

        assertEquals(9, testRest?.requests?.size)
        assertEquals(3, testWebSocket?.messages?.size)
        assertEquals(
            """
                {"type":"unsubscribe","channel":"v4_subaccounts","id":"$testAddress/0"}
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