package exchange.dydx.abacus.app

import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.state.app.ApiStatus
import exchange.dydx.abacus.state.app.AppStateMachine
import exchange.dydx.abacus.state.app.AppStateResponse
import exchange.dydx.abacus.state.app.NetworkStatus
import exchange.dydx.abacus.state.app.V4AppStateMachineProtocol
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.app.adaptors.V4ApiAdaptor
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class V4AppStateMachineTests {
    private val testWsUrl =
        AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")
    private val testRestUrl = "https://indexer.v4staging.dydx.exchange"
    private val chainUrl = "https://validator.v4staging.dydx.exchange"
    val mock = AbacusMockData()

    private val appStateMachine = kotlin.run {
        val appStateMachine = AppStateMachine()
        appStateMachine.setChainId("dydxprotocol-testnet")
        return@run appStateMachine
    }

    private val testCosmoAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"

    private fun setStateMachineConnectedWithMarketsAndSubaccounts(): AppStateResponse {
        appStateMachine.setChainId("dydxprotocol-testnet")
        appStateMachine.setReadyToConnect(true)
        appStateMachine.processSocketResponse(
            testWsUrl, mock.connectionMock.connectedMessage
        )
        appStateMachine.processSocketResponse(
            testWsUrl, mock.marketsChannel.v4_subscribed_r1
        )
        appStateMachine.setMarket("BTC-USD")
        val address = testCosmoAddress
        val addressState =
            (appStateMachine as V4AppStateMachineProtocol).setWalletCosmoAddress(address)
        val subaccountsRequest = addressState.networkRequests!!.restRequests?.firstOrNull()
        (appStateMachine as V4AppStateMachineProtocol).processHttpResponse(
            subaccountsRequest!!.url,
            mock.accountsChannel.v4accountsReceived
        )
        return appStateMachine.processSocketResponse(
            testWsUrl, mock.accountsChannel.subscribed
        )
    }

    @Test
    fun testExistingSubaccountsAndFaucetState() {
        val envs = appStateMachine.availableEnvironments
        appStateMachine.setEnvironment("dydxprotocol-staging")
        var state = appStateMachine.setReadyToConnect(true)
        var requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests)  // connect
        assertNotNull(requests.restRequests)

        state = appStateMachine.processSocketResponse(
            testWsUrl, mock.connectionMock.connectedMessage
        )
        requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests)
        assertEquals(requests.socketRequests?.size, 1)   // v4_markets
        assertNull(requests.restRequests)

        state = appStateMachine.processSocketResponse(
            testWsUrl, mock.marketsChannel.subscribed
        )
        state = appStateMachine.processHttpResponse(
            AbUrl(
                host = "dydx-v4-shared-resources.vercel.app",
                path = "/v4/markets.json",
                scheme = "https://"
            ),
            mock.marketsConfigurations.configurations
        )
        val newCosmoAddress = "0xfakeAddress"
        state =
            (appStateMachine as V4AppStateMachineProtocol).setWalletCosmoAddress(newCosmoAddress)
        requests = state.networkRequests
        assertNotNull(requests)
        assertNull(requests.socketRequests)
        assertNotNull(requests.restRequests)

        val subaccountsRequest = requests.restRequests?.firstOrNull()
        assertNotNull(subaccountsRequest)
        val subaccountsUrl = subaccountsRequest.url // /v4/addresses/

        assertNotNull(subaccountsUrl)
        state = (appStateMachine as V4AppStateMachineProtocol).processHttpResponse(
            subaccountsUrl,
            mock.accountsChannel.v4accountsReceived
        )
        requests = state.networkRequests

        assertNotNull(requests?.socketRequests) // v4_subaccounts
        assertNotNull(requests?.restRequests)   // historical_pnl

        state = appStateMachine.faucet(100)
        requests = state.networkRequests
        val post = requests?.restRequests?.firstOrNull()
        assertNotNull(post)
        val faucetUrl = post.url
        assertNotNull(faucetUrl)
        state = (appStateMachine as V4AppStateMachineProtocol).processHttpResponse(
            faucetUrl,
            mock.accountsChannel.v4_faucet_succeeded
        )
        requests = state.networkRequests
        assertNotNull(requests) // v4_subaccounts


        appStateMachine.setReadyToConnect(false)
        state = appStateMachine.setReadyToConnect(true)
        requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests) // connect
        assertNotNull(requests.restRequests)

        appStateMachine.setReadyToConnect(true)

        val newMarket = "ETH-USD"
        val market = appStateMachine.market()
        state = appStateMachine.setMarket(newMarket)
        requests = state.networkRequests

        val restRequests = requests?.restRequests
        assertNotNull(restRequests)
        assertEquals(2, restRequests.size)

        val historicalFunding = restRequests.first()
        assertEquals("/v4/historicalFunding/ETH-USD", historicalFunding.url.path)
        val candles = restRequests[1]
        assertEquals("/v4/candles/perpetualMarkets/ETH-USD", candles.url.path)


        /* Candles Received */
        state = appStateMachine.processHttpResponse(
            AbUrl.fromString("https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/ETH-USD"),
            mock.candles.v4FirstCall
        )
        requests = state.networkRequests
        assertNotNull(requests?.restRequests)


        /* Historical Funding Received */
        state = appStateMachine.processHttpResponse(
            AbUrl.fromString("https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD"),
            mock.historicalFundingsMock.call_v4
        )
        requests = state.networkRequests
        assertNotNull(requests)

    }


    @Test
    fun testNoSubaccountsAndFaucetState() {
        val appStateMachine = kotlin.run {
            val appStateMachine = AppStateMachine()
            appStateMachine.setChainId("dydxprotocol-testnet")
            return@run appStateMachine
        }

        assertNotNull(appStateMachine, "App State Machine shouldn't be null")
        appStateMachine.setChainId("dydxprotocol-testnet")
        var state = appStateMachine.setReadyToConnect(true)
        var requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests)  // connect
        assertNotNull(requests.restRequests)

        state = appStateMachine.processSocketResponse(
            testWsUrl, mock.connectionMock.connectedMessage
        )
        requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests)
        assertEquals(requests.socketRequests?.size, 1)   // v4_markets
        assertNull(requests.restRequests)

        val newCosmoAddress = "0xfakeAddress"
        state =
            (appStateMachine as V4AppStateMachineProtocol).setWalletCosmoAddress(newCosmoAddress)
        requests = state.networkRequests
        assertNotNull(requests)
        assertNull(requests.socketRequests)
        assertNotNull(requests.restRequests)

        val subaccountsRequest = requests.restRequests?.firstOrNull()
        assertNotNull(subaccountsRequest)
        val subaccountsUrl = subaccountsRequest.url // /v4/addresses/

        assertNotNull(subaccountsUrl)
        state = (appStateMachine as V4AppStateMachineProtocol).processHttpResponse(
            subaccountsUrl,
            mock.accountsChannel.v4_subaccounts_failed
        )
        requests = state.networkRequests

        assertNull(requests?.socketRequests) // v4_subaccounts
        assertNull(requests?.restRequests)   // historical_pnl

        state = appStateMachine.faucet(100)
        requests = state.networkRequests
        val post = requests?.restRequests?.firstOrNull()
        assertNotNull(post)
        val faucetUrl = post.url
        assertNotNull(faucetUrl)
        state = (appStateMachine as V4AppStateMachineProtocol).processHttpResponse(
            faucetUrl,
            mock.accountsChannel.v4_faucet_succeeded
        )
        requests = state.networkRequests
        assertNotNull(requests) // v4_subaccounts


        appStateMachine.setReadyToConnect(false)
        state = appStateMachine.setReadyToConnect(true)
        requests = state.networkRequests
        assertNotNull(requests)
        assertNotNull(requests.socketRequests) // connect
        assertNotNull(requests.restRequests)

        appStateMachine.processSocketResponse(
            testWsUrl, mock.connectionMock.connectedMessage
        )

        state = (appStateMachine as V4AppStateMachineProtocol).processHttpResponse(
            subaccountsUrl,
            mock.accountsChannel.v4accountsReceived
        )
        requests = state.networkRequests

        assertNotNull(requests?.socketRequests) // v4_subaccounts
        assertNotNull(requests?.restRequests)   // historical_pnl
    }

    @Test
    fun testHeight() {
        /* no height yet */
        var state = appStateMachine.processSocketResponse(testWsUrl, mock.accountsChannel.v4_subaccounts_update_7)
        state = appStateMachine.processSocketResponse(testWsUrl, mock.accountsChannel.v4_best_effort_cancelled)
        var order = state.state?.subaccount(0)?.orders?.firstOrNull()
        assertEquals(OrderStatus.canceling, order?.status)

        var url = AbUrl.fromString("$testRestUrl/v4/height")
        state = appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)
        assertEquals(ApiStatus.NORMAL, state.apiState?.status)
        assertNull(state.apiState?.haltedBlock)
        assertNull(state.apiState?.trailingBlocks)
        var v4Adapter = (appStateMachine.adaptor as? V4ApiAdaptor)
        assertNotNull(v4Adapter)
        assertEquals(NetworkStatus.NORMAL, v4Adapter.indexerState.status)
        assertEquals(16750, v4Adapter.indexerState.block)

        (appStateMachine.adaptor as? V4ApiAdaptor)?.validatorState?.requestId = 284772725242
        url = AbUrl.fromString("$chainUrl/")
        state = appStateMachine.processHttpResponse(url, mock.heightMock.validatorHeight)
        order = state.state?.subaccount(0)?.orders?.firstOrNull()
        assertEquals(OrderStatus.canceling, order?.status)

        assertEquals(ApiStatus.NORMAL, state.apiState?.status)
        v4Adapter = (appStateMachine.adaptor as? V4ApiAdaptor)
        assertNotNull(v4Adapter)
        assertEquals(NetworkStatus.NORMAL, v4Adapter.indexerState.status)
        assertEquals(16753, v4Adapter.validatorState.block)

        url = AbUrl.fromString("$testRestUrl/v4/height")
        appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)
        state = appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight)

        assertEquals(ApiStatus.INDEXER_HALTED, state.apiState?.status)
        assertEquals(16750, state.apiState?.haltedBlock)
        v4Adapter = (appStateMachine.adaptor as? V4ApiAdaptor)
        assertEquals(NetworkStatus.HALTED, v4Adapter?.indexerState?.status)

        url = AbUrl.fromString("$chainUrl/")
        appStateMachine.processHttpResponse(url, mock.heightMock.validatorHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.validatorHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.validatorHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.validatorHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.validatorHeight)
        appStateMachine.processHttpResponse(url, mock.heightMock.validatorHeight)
        state = appStateMachine.processHttpResponse(url, mock.heightMock.validatorHeight)

        assertEquals(ApiStatus.VALIDATOR_HALTED, state.apiState?.status)
        assertEquals(16753, state.apiState?.haltedBlock)
        v4Adapter = (appStateMachine.adaptor as? V4ApiAdaptor)
        assertEquals(NetworkStatus.HALTED, v4Adapter?.validatorState?.status)

        url = AbUrl.fromString("$testRestUrl/v4/height")
        state = appStateMachine.processHttpResponse(url, mock.heightMock.indexerHeight2)
        assertEquals(ApiStatus.VALIDATOR_HALTED, state.apiState?.status)

        url = AbUrl.fromString("$chainUrl/")
        state = appStateMachine.processHttpResponse(url, mock.heightMock.validatorHeight2)
        assertEquals(ApiStatus.INDEXER_TRAILING, state.apiState?.status)
        assertEquals(153, state.apiState?.trailingBlocks)

        order = state.state?.subaccount(0)?.orders?.firstOrNull()
        assertEquals(OrderStatus.cancelled, order?.status)
        assertEquals("APP.TRADE.CANCELED", order?.resources?.statusStringKey)
        assertEquals(0.4, order?.totalFilled)
        assertEquals(0.6, order?.remainingSize)

        // Out-of-order status update
        state = appStateMachine.processSocketResponse(testWsUrl, mock.accountsChannel.v4_subaccounts_update_7)
        order = state.state?.subaccount(0)?.orders?.firstOrNull()
        assertEquals(OrderStatus.cancelled, order?.status)
        assertEquals("APP.TRADE.CANCELED", order?.resources?.statusStringKey)
    }

    @Test
    fun faucetCreatesRequestWithCorrectAddressSubaccountAndAmount() {
        setStateMachineConnectedWithMarketsAndSubaccounts()
        val subaccountNumber = 20
        val faucetAmount = 100

        var state = appStateMachine.ping()
        assertNull(state.networkRequests?.restRequests)

        (appStateMachine as V4AppStateMachineProtocol).setSubaccountNumber(subaccountNumber)

        state = appStateMachine.faucet(faucetAmount)
        val requests = state.networkRequests
        val post = requests!!.restRequests!!.firstOrNull()
        assertEquals(
            """{"address":"$testCosmoAddress","subaccountNumber":$subaccountNumber,"amount":$faucetAmount}""",
            post!!.body
        )
    }
}