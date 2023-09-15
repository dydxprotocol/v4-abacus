package exchange.dydx.abacus.app

import exchange.dydx.abacus.state.app.AppStateMachine
import exchange.dydx.abacus.state.app.AppStateResponse
import exchange.dydx.abacus.state.app.V4AppStateMachineProtocol
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.app.adaptors.paramString
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kollections.iListOf
import kollections.toIList
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class V4AppStateMachineForegroundCycleTests {
    val mock = AbacusMockData()
    private val testWsUrl = AbUrl.fromString("wss://indexer.v4staging.dydx.exchange/v4/ws")
    private val testRestUrl = "https://indexer.v4staging.dydx.exchange"
    private val testCosmoAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
    private var appStateMachine: AppStateMachine = kotlin.run {
        val appStateMachine = AppStateMachine()
        appStateMachine.setChainId("dydxprotocol-testnet")
        return@run appStateMachine
    }

    @BeforeTest
    fun resetAppStateMachine() {
        appStateMachine = kotlin.run {
            val appStateMachine = AppStateMachine()
            appStateMachine.setChainId("dydxprotocol-testnet")
            return@run appStateMachine
        }
    }

    private fun setStateMachineReadyToConnect(): AppStateResponse {
        val state = appStateMachine.setChainId("dydxprotocol-testnet")
        assertNull(state.networkRequests)
        return appStateMachine.setReadyToConnect(true)
    }

    private fun setStateMachineConnected(): AppStateResponse {
        appStateMachine.setChainId("dydxprotocol-testnet")
        appStateMachine.setReadyToConnect(true)
        return appStateMachine.processSocketResponse(
            testWsUrl, mock.connectionMock.connectedMessage
        )
    }

    private fun setStateMachineConnectedWithMarkets(): AppStateResponse {
        appStateMachine.setChainId("dydxprotocol-testnet")
        appStateMachine.setReadyToConnect(true)
        appStateMachine.processHttpResponse(
            AbUrl.fromString("https://dydx-v4-shared-resources.vercel.app/v4/markets.json"),
            mock.marketsConfigurations.configurations
        )
        appStateMachine.processSocketResponse(
            testWsUrl, mock.connectionMock.connectedMessage
        )
        appStateMachine.processSocketResponse(
            testWsUrl, mock.marketsChannel.v4_subscribed_r1
        )
        return appStateMachine.setMarket("ETH-USD")
    }

    private fun setStateMachineConnectedWithMarketsAndSubaccounts(): AppStateResponse {
        appStateMachine.setChainId("dydxprotocol-testnet")
        appStateMachine.setReadyToConnect(true)
        appStateMachine.processSocketResponse(
            testWsUrl, mock.connectionMock.connectedMessage
        )
        appStateMachine.processSocketResponse(
            testWsUrl, mock.marketsChannel.v4_subscribed_r1
        )
        appStateMachine.setMarket("ETH-USD")
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
    fun setReadyToConnectShouldQueueInitialRequests() {
        assertNotNull(appStateMachine, "AppStateMachine should not be null")

        val state = setStateMachineReadyToConnect()

        val restRequests = state.networkRequests?.restRequests!!
        assertEquals(8, restRequests.size)
        assertEquals(
            "/v4/markets.json",
            restRequests[0].url.path,
            "Request to markets endpoint should be present"
        )
        assertEquals(
            "/v4/config",
            restRequests[1].url.path,
            "Request to markets endpoint should be present"
        )
        assertEquals(
            "/config/network_configs.json",
            restRequests[2].url.path,
            "Request to markets endpoint should be present"
        )
        assertEquals(
            "/config/fee_tiers.json",
            restRequests[3].url.path,
            "Request to markets endpoint should be present"
        )
        assertEquals(
            "/config/fee_discounts.json",
            restRequests[4].url.path,
            "Request to markets endpoint should be present"
        )
        assertEquals(
            "/v4/time", restRequests[5].url.path, "Request to markets endpoint should be present"
        )
        assertEquals(
            "/v1/chains", restRequests[6].url.path, "Request to markets endpoint should be present"
        )
        assertEquals(
            "/v1/tokens", restRequests[7].url.path, "Request to markets endpoint should be present"
        )

        val socketRequests = state.networkRequests?.socketRequests!!
        assertEquals(
            "/v4/ws",
            socketRequests[0].url.path,
            "A new AppStateMachine should start with 1 socket request"
        )
    }

    @Test
    fun whenConnectedToWSMarketSubscribeIsQueued() {
        setStateMachineReadyToConnect()
        val state = appStateMachine.processSocketResponse(
            testWsUrl, mock.connectionMock.connectedMessage
        )
        val requests = state.networkRequests!!
        assertEquals(1, requests.socketRequests!!.size)
        assertEquals(
            "{\"type\":\"subscribe\",\"channel\":\"v4_markets\",\"batched\":\"true\"}",
            requests.socketRequests!![0].text
        )
    }

    @Test
    fun whenMarketsSocketIsSubscribedSummaryShouldBeValid() {
        setStateMachineReadyToConnect()

        appStateMachine.processHttpResponse(
            AbUrl.fromString("https://dydx-v4-shared-resources.vercel.app/v4/markets.json"),
            mock.marketsConfigurations.configurations
        )
        /* Markets Connected */
        val state = appStateMachine.processSocketResponse(
            testWsUrl, mock.marketsChannel.v4_subscribed_r1
        )
        val requests = state.networkRequests
        assertNotNull(requests)

        assertEquals(
            state.state!!.marketsSummary!!.markets!!.keys.toIList(),
            iListOf("BTC-USD", "ETH-USD")
        )
    }

    @Test
    fun setMarketShouldStartOrderbookAndTradeSocketRequests() {
        val newMarket = "ETH-USD"
        setStateMachineConnected()
        appStateMachine.processSocketResponse(
            testWsUrl, mock.marketsChannel.v4_subscribed_r1
        )

        val market = appStateMachine.market()
        //Without an existing market selected, should queue subscribe requests
        assertNull(market)
        var state = appStateMachine.setMarket(newMarket)

        assertEquals(
            """{"type":"subscribe","channel":"v4_trades","id":"ETH-USD","batched":"true"}""",
            state.networkRequests!!.socketRequests!![0].text
        )
        assertEquals(
            """{"type":"subscribe","channel":"v4_orderbook","id":"ETH-USD","batched":"true"}""",
            state.networkRequests!!.socketRequests!![1].text
        )

        //With an existing selected market should queue unsubscribe from previous market and
        //subscribe requests for new market
        val secondMarket = "BTC-USD"
        state = appStateMachine.setMarket(secondMarket)

        assertEquals(
            """{"type":"unsubscribe","channel":"v4_trades","id":"ETH-USD","batched":"true"}""",
            state.networkRequests!!.socketRequests!![0].text
        )
        assertEquals(
            """{"type":"unsubscribe","channel":"v4_orderbook","id":"ETH-USD"}""",
            state.networkRequests!!.socketRequests!![1].text
        )
        assertEquals(
            """{"type":"subscribe","channel":"v4_trades","id":"BTC-USD","batched":"true"}""",
            state.networkRequests!!.socketRequests!![2].text
        )
        assertEquals(
            """{"type":"subscribe","channel":"v4_orderbook","id":"BTC-USD","batched":"true"}""",
            state.networkRequests!!.socketRequests!![3].text
        )
    }

    @Test
    fun setMarketShouldStartHistoricalFundingAndCandlesRequests() {
        val newMarket = "ETH-USD"
        setStateMachineConnected()
        appStateMachine.processSocketResponse(
            testWsUrl, mock.marketsChannel.v4_subscribed_r1
        )
        val state = appStateMachine.setMarket(newMarket)
        assertEquals(
            "/v4/historicalFunding/ETH-USD",
            state.networkRequests!!.restRequests!![0].url.path
        )
    }

    @Test
    fun historicalFundingShouldCreateSubsequentPaginatedRequests() {
        setStateMachineConnectedWithMarketsAndSubaccounts()
        val state = appStateMachine.processHttpResponse(
            AbUrl.fromString("$testRestUrl/v4/historicalFunding/ETH-USD"),
            mock.historicalFundingsMock.call
        )
        val requests = state.networkRequests
        assertNotNull(requests)
    }

    @Test
    fun tradesChannelSubscribeShouldNotQueueAnyOtherRequests() {
        setStateMachineConnectedWithMarkets()
        val state = appStateMachine.processSocketResponse(
            testWsUrl, mock.tradesChannel.subscribed
        )
        val requests = state.networkRequests
        assertNull(requests)
    }

    @Test
    fun orderbookChannelSubscribeShouldNotQueueAnyOtherRequests() {
        setStateMachineConnectedWithMarkets()
        val state = appStateMachine.processSocketResponse(
            testWsUrl, mock.orderbookChannel.subscribed
        )
        val requests = state.networkRequests
        assertNull(requests)
    }

    @Test
    fun connectWalletWithNoAccountShouldStartAccountAndPNLRequests() {
        setStateMachineConnectedWithMarkets()

        val cosmoAddress = "0xsecondaryFakeAddress"
        val state =
            (appStateMachine as V4AppStateMachineProtocol).setWalletCosmoAddress(cosmoAddress)
        val requests = state.networkRequests!!

        assertEquals(1, requests.restRequests!!.size)
        assertEquals("/v4/addresses/$cosmoAddress", requests.restRequests!![0].url.path)
        assertNull(requests.socketRequests)

        val subaccountsRequest = requests.restRequests?.firstOrNull()
        val subaccountState = (appStateMachine as V4AppStateMachineProtocol).processHttpResponse(
            subaccountsRequest!!.url,
            mock.accountsChannel.v4accountsReceived
        )
        val subaccountRequests = subaccountState.networkRequests!!

        assertEquals(
            """{"type":"subscribe","channel":"v4_subaccounts","id":"0xsecondaryFakeAddress/0","batched":"true"}""",
            subaccountRequests.socketRequests!![0].text
        )
        assertEquals("/v4/historical-pnl", subaccountRequests.restRequests!![0].url.path)
    }

    @Test
    fun historicalPNLShouldCreateSubsequentPaginatedRequests() {
        setStateMachineConnectedWithMarketsAndSubaccounts()
        val state = appStateMachine.processHttpResponse(
            AbUrl.fromString("$testRestUrl/v4/historical-pnl"),
            mock.historicalPNL.firstCall
        )
        val requests = state.networkRequests
        assertEquals(
            "?createdAtOrAfter=2022-08-08T21:07:24.581Z&address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
            requests!!.restRequests!![0].url.params!!.paramString()
        )
    }

    @Test
    fun connectWalletWithExistingAccountShouldUnsubscribeFromPreviousAccountAndStartNewAccountRequets() {
        setStateMachineConnectedWithMarkets()

        val firstAddressState =
            (appStateMachine as V4AppStateMachineProtocol).setWalletCosmoAddress(testCosmoAddress)
        val subaccountsRequest = firstAddressState.networkRequests!!.restRequests?.firstOrNull()
        (appStateMachine as V4AppStateMachineProtocol).processHttpResponse(
            subaccountsRequest!!.url,
            mock.accountsChannel.v4accountsReceived
        )
        appStateMachine.processSocketResponse(
            testWsUrl, mock.accountsChannel.subscribed
        )

        val secondAddress = "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f"
        val secondAddressState =
            (appStateMachine as V4AppStateMachineProtocol).setWalletCosmoAddress(secondAddress)
        val secondAccountsSocketRequests = secondAddressState.networkRequests!!.socketRequests!!
        val secondAccountsRestRequests = secondAddressState.networkRequests!!.restRequests!!

        assertEquals(
            """{"type":"unsubscribe","channel":"v4_subaccounts","id":"$testCosmoAddress/0"}""",
            secondAccountsSocketRequests[0].text
        )
        assertEquals(
            "/v4/addresses/$secondAddress",
            secondAccountsRestRequests[0].url.path
        )
    }

    @Test
    fun settingWalletCosmoAddressToNullShoulUnsubscribeFromSubaccountsChannel() {
        setStateMachineConnectedWithMarketsAndSubaccounts()
        val state = appStateMachine.setWalletCosmoAddress(null)
        val requests = state.networkRequests!!
        assertEquals(1, requests.socketRequests!!.size)
        assertEquals(
            """{"type":"unsubscribe","channel":"v4_subaccounts","id":"$testCosmoAddress/0"}""",
            state.networkRequests!!.socketRequests!![0].text
        )
    }

    @Test
    fun setReadyToConnectToFalseShouldResetNetworkConnectionChangesAndErrors() {
        /* Backgrounded */
        val state = appStateMachine.setReadyToConnect(false)
        assertNull(state.networkRequests)
        assertNull(state.changes)
        assertNull(state.errors)
    }
}