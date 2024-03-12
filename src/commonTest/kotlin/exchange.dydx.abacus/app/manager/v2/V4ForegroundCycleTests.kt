package exchange.dydx.abacus.app.manager.v2

import exchange.dydx.abacus.app.manager.NetworkTests
import exchange.dydx.abacus.app.manager.TestChain
import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.app.manager.TestState
import exchange.dydx.abacus.app.manager.TestWebSocket
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.state.manager.setAddresses
import exchange.dydx.abacus.state.v2.manager.AsyncAbacusStateManagerV2
import exchange.dydx.abacus.state.v2.manager.StateManagerAdaptorV2
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.values
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class V4ForegroundCycleTests : NetworkTests() {
    val mock = AbacusMockData()
    private val testCosmoAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
    private var stateManager: AsyncAbacusStateManagerV2 = resetStateManager()
    private var ioImplementations = stateManager.ioImplementations
    private var testRest = stateManager.ioImplementations.rest as? TestRest
    private var testWebSocket = stateManager.ioImplementations.webSocket as? TestWebSocket
    private var testChain = stateManager.ioImplementations.chain as? TestChain
    private var testState = stateManager.stateNotification as? TestState
    private var v4Adapter = stateManager.adaptor as? StateManagerAdaptorV2

    @BeforeTest
    fun reset() {
        stateManager = resetStateManager()
        ioImplementations = stateManager.ioImplementations
        testRest = stateManager.ioImplementations.rest as? TestRest
        testWebSocket = stateManager.ioImplementations.webSocket as? TestWebSocket
        testChain = stateManager.ioImplementations.chain as? TestChain
        testState = stateManager.stateNotification as? TestState
        v4Adapter = stateManager.adaptor as? StateManagerAdaptorV2
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
        stateManager.setAddresses(null, testCosmoAddress)
    }

    @Test
    fun setReadyToConnectShouldQueueInitialRequests() {
        reset()

        setStateMachineReadyToConnect(stateManager)

        assertEquals(
            "wss://indexer.v4staging.dydx.exchange/v4/ws",
            testWebSocket?.connectUrl,
            "WebSocket should be connected to correct url",
        )

        compareExpectedRequests(
//            """
//                [
//                   "https://api.examples.com/configs/documentation.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/time",
//                   "https://api.examples.com/configs/markets.json",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
//                   "https://api.examples.com/configs/exchanges.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/height",
//                   "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals"
//                ]
//            """.trimIndent(),
            """
                [
                   "https://api.examples.com/configs/documentation.json",
                   "https://indexer.v4staging.dydx.exchange/v4/time",
                   "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
                   "https://indexer.v4staging.dydx.exchange/v4/height",
                   "https://api.examples.com/configs/markets.json",
                   "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
                   "https://api.examples.com/configs/exchanges.json"
                ]
            """.trimIndent(),
            testRest?.requests,
        )

        compareExpectedRequests(
            """
                [
                   "getEquityTiers",
                   "getFeeTiers",
                   "getRewardsParams",
                   "getHeight"
                ]
            """.trimIndent(),
            testChain?.requests?.map { it.rawValue },
        )
    }

    @Test
    fun whenConnectedToWSMarketSubscribeIsQueued() {
        reset()

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)

        compareExpectedRequests(
            """
                [
                   {"type":"subscribe","channel":"v4_markets","batched":"true"}
                ]
            """.trimIndent(),
            testWebSocket?.messages,
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

        compareExpectedRequests(
            """
                [
                    {"type":"subscribe","channel":"v4_markets","batched":"true"},
                    {"type":"subscribe","channel":"v4_trades","id":"ETH-USD","batched":"true"},
                    {"type":"subscribe","channel":"v4_orderbook","id":"ETH-USD","batched":"true"},
                    {"type":"subscribe","channel":"v4_candles","id":"ETH-USD/1DAY","batched":"true"}
                ]
            """.trimIndent(),
            testWebSocket?.messages,
        )

        compareExpectedRequests(
//            """
//                [
//                   "https://api.examples.com/configs/documentation.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/time",
//                   "https://api.examples.com/configs/markets.json",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
//                   "https://api.examples.com/configs/exchanges.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/height",
//                   "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
//                   "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD",
//                   "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/ETH-USD?resolution=1DAY"
//                ]
//            """.trimIndent(),
            """
                [
                    "https://api.examples.com/configs/documentation.json",
                    "https://indexer.v4staging.dydx.exchange/v4/time",
                    "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/height",
                    "https://api.examples.com/configs/markets.json",
                    "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
                    "https://api.examples.com/configs/exchanges.json",
                    "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/ETH-USD?resolution=1DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD"
                ]
            """.trimIndent(),
            testRest?.requests,
        )

        compareExpectedRequests(
            """
                [
                   "getEquityTiers",
                   "getFeeTiers",
                   "getRewardsParams",
                   "getHeight"
                ]
            """.trimIndent(),
            testChain?.requests?.map { it.rawValue },
        )

        stateManager.market = "BTC-USD"

        compareExpectedRequests(
            """
                [
                    {"type":"subscribe","channel":"v4_markets","batched":"true"},
                    {"type":"subscribe","channel":"v4_trades","id":"ETH-USD","batched":"true"},
                    {"type":"subscribe","channel":"v4_orderbook","id":"ETH-USD","batched":"true"},
                    {"type":"subscribe","channel":"v4_candles","id":"ETH-USD/1DAY","batched":"true"},
                    {"type":"unsubscribe","channel":"v4_trades","id":"ETH-USD"},
                    {"type":"unsubscribe","channel":"v4_orderbook","id":"ETH-USD"},
                    {"type":"unsubscribe","channel":"v4_candles","id":"ETH-USD/1DAY"},
                    {"type":"subscribe","channel":"v4_trades","id":"BTC-USD","batched":"true"},
                    {"type":"subscribe","channel":"v4_orderbook","id":"BTC-USD","batched":"true"},
                    {"type":"subscribe","channel":"v4_candles","id":"BTC-USD/1DAY","batched":"true"}
                ]
            """.trimIndent(),
            testWebSocket?.messages,
        )

        compareExpectedRequests(
//            """
//                [
//                   "https://api.examples.com/configs/documentation.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/time",
//                   "https://api.examples.com/configs/markets.json",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
//                   "https://api.examples.com/configs/exchanges.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/height",
//                   "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
//                   "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD",
//                   "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/ETH-USD?resolution=1DAY",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/BTC-USD",
//                   "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/BTC-USD?resolution=1DAY"
//                ]
//            """.trimIndent(),
            """
                [
                    
                    "https://api.examples.com/configs/documentation.json",
                    "https://indexer.v4staging.dydx.exchange/v4/time",
                    "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/height",
                    "https://api.examples.com/configs/markets.json",
                    "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
                    "https://api.examples.com/configs/exchanges.json",
                    "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/ETH-USD?resolution=1DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD",
                    "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/BTC-USD?resolution=1DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/BTC-USD"
                ]
            """.trimIndent(),
            testRest?.requests,
        )
    }

    @Test
    fun historicalFundingShouldCreateSubsequentPaginatedRequests() {
        reset()
        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD",
            mock.historicalFundingsMock.call,
        )

        setStateMachineReadyToConnect(stateManager)
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        stateManager.market = "ETH-USD"

        assertNotNull(stateManager.adaptor?.stateMachine?.state?.historicalFundings?.get("ETH-USD"))

        /* Only getting historical funding rate once for now */

        compareExpectedRequests(
//            """
//                [
//                   "https://api.examples.com/configs/documentation.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/time",
//                   "https://api.examples.com/configs/markets.json",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
//                   "https://api.examples.com/configs/exchanges.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/height",
//                   "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
//                   "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD",
//                   "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/ETH-USD?resolution=1DAY"
//                ]
//            """.trimIndent(),
            """
                [
                    "https://api.examples.com/configs/documentation.json",
                    "https://indexer.v4staging.dydx.exchange/v4/time",
                    "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/height",
                    "https://api.examples.com/configs/markets.json",
                    "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
                    "https://api.examples.com/configs/exchanges.json",
                    "https://indexer.v4staging.dydx.exchange/v4/candles/perpetualMarkets/ETH-USD?resolution=1DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalFunding/ETH-USD"
                ]
            """.trimIndent(),
            testRest?.requests,
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
        stateManager.setAddresses(null, testAddress)

        compareExpectedRequests(
//            """
//                [
//                   "https://api.examples.com/configs/documentation.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/time",
//                   "https://api.examples.com/configs/markets.json",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
//                   "https://api.examples.com/configs/exchanges.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/height",
//                   "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
//                   "https://indexer.v4staging.dydx.exchange/v4/screen?address=0xsecondaryFakeAddress",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/0xsecondaryFakeAddress?period=WEEKLY",
//                   "https://indexer.v4staging.dydx.exchange/v4/addresses/0xsecondaryFakeAddress",
//                   "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/0xsecondaryFakeAddress?n=2"
//                ]
//            """.trimIndent(),
            """
                [
                    "https://api.examples.com/configs/documentation.json",
                    "https://indexer.v4staging.dydx.exchange/v4/time",
                    "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/height",
                    "https://api.examples.com/configs/markets.json",
                    "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
                    "https://api.examples.com/configs/exchanges.json",
                    "https://indexer.v4staging.dydx.exchange/v4/screen?address=0xsecondaryFakeAddress",
                    "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/0xsecondaryFakeAddress?n=2",
                    "https://indexer.v4staging.dydx.exchange/v4/addresses/0xsecondaryFakeAddress",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/0xsecondaryFakeAddress?period=WEEKLY"
                ]
            """.trimIndent(),
            testRest?.requests,
        )

        compareExpectedRequests(
            """
                [
                   "getEquityTiers",
                   "getFeeTiers",
                   "getRewardsParams",
                   "getHeight",
                   "getAccountBalances",
                   "getDelegatorDelegations",
                   "getNobleBalance"
                ]
            """.trimIndent(),
            testChain?.requests?.map { it.rawValue },
        )
    }

    @Test
    fun connectWalletWithExistingAccountShouldStartAccountAndPNLRequests() {
        reset()

        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/$testAddress",
            mock.accountsChannel.v4accountsReceived,
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        stateManager.setAddresses(null, testAddress)

        compareExpectedRequests(
//            """
//                [
//                   "https://api.examples.com/configs/documentation.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/time",
//                   "https://api.examples.com/configs/markets.json",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
//                   "https://api.examples.com/configs/exchanges.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/height",
//                   "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
//                   "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?period=WEEKLY",
//                   "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                   "https://indexer.v4staging.dydx.exchange/v4/fills?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://indexer.v4staging.dydx.exchange/v4/transfers?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?n=2"
//                ]
//            """.trimIndent(),
            """
                [
                    "https://api.examples.com/configs/documentation.json",
                    "https://indexer.v4staging.dydx.exchange/v4/time",
                    "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/height",
                    "https://api.examples.com/configs/markets.json",
                    "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
                    "https://api.examples.com/configs/exchanges.json",
                    "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?n=2",
                    "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "https://indexer.v4staging.dydx.exchange/v4/fills?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/transfers?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?period=WEEKLY"
                ]
            """.trimIndent(),
            testRest?.requests,
        )

        compareExpectedRequests(
            """
                [
                    {"type":"subscribe","channel":"v4_markets","batched":"true"},
                    {"type":"subscribe","channel":"v4_subaccounts","id":"cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm/0"}
                ]
            """.trimIndent(),
            testWebSocket?.messages,
        )

        compareExpectedRequests(
            """
                [
                   "getEquityTiers",
                   "getFeeTiers",
                   "getRewardsParams",
                   "getHeight",
                   "getUserFeeTier",
                   "getUserStats",
                   "getAccountBalances",
                   "getDelegatorDelegations",
                   "getNobleBalance"
                ]
            """.trimIndent(),
            testChain?.requests?.map { it.rawValue },
        )

        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_channel_data_with_orders)

        assertEquals(1, stateManager.adaptor?.notifications?.size)
        val notification = stateManager.adaptor?.notifications?.values()?.first()
        assertEquals(
            "order:b812bea8-29d3-5841-9549-caa072f6f8a8",
            notification?.id,
        )
    }

    @Test
    fun historicalPNLShouldCreateSubsequentPaginatedRequests() {
        reset()

        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/$testAddress",
            mock.accountsChannel.v4accountsReceived,
        )
        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
            mock.historicalPNL.firstCall,
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        stateManager.setAddresses(null, testAddress)

        compareExpectedRequests(
//            """
//                [
//                   "https://api.examples.com/configs/documentation.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/time",
//                   "https://api.examples.com/configs/markets.json",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
//                   "https://api.examples.com/configs/exchanges.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/height",
//                   "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
//                   "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?period=WEEKLY",
//                   "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                   "https://indexer.v4staging.dydx.exchange/v4/fills?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://indexer.v4staging.dydx.exchange/v4/transfers?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?createdAtOrAfter=2022-08-08T21:07:24.581Z&address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?n=2"
//                ]
//            """.trimIndent(),
            """
                [
                    "https://api.examples.com/configs/documentation.json",
                    "https://indexer.v4staging.dydx.exchange/v4/time",
                    "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/height",
                    "https://api.examples.com/configs/markets.json",
                    "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
                    "https://api.examples.com/configs/exchanges.json",
                    "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?n=2",
                    "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "https://indexer.v4staging.dydx.exchange/v4/fills?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/transfers?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?createdAtOrAfter=2022-08-08T21:07:24.581Z&address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?period=WEEKLY"
                ]
            """.trimIndent(),
            testRest?.requests,
        )
    }

    @Test
    fun connectWalletWithExistingAccountShouldUnsubscribeFromPreviousAccountAndStartNewAccountRequests() {
        reset()

        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
        val secondAddress = "cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f"

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/$testAddress",
            mock.accountsChannel.v4accountsReceived,
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        stateManager.setAddresses(null, testAddress)

        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_subscribed)
        stateManager.setAddresses(null, secondAddress)

        compareExpectedRequests(
//            """
//                [
//                   "https://api.examples.com/configs/exchanges.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/height",
//                   "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?period=WEEKLY",
//                   "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                   "https://indexer.v4staging.dydx.exchange/v4/fills?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://indexer.v4staging.dydx.exchange/v4/transfers?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?n=2",
//                   "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f?period=WEEKLY",
//                   "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
//                   "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f?n=2"
//                ]
//            """.trimIndent(),
            """
                [
                    "https://api.examples.com/configs/documentation.json",
                    "https://indexer.v4staging.dydx.exchange/v4/time",
                    "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/height",
                    "https://api.examples.com/configs/markets.json",
                    "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
                    "https://api.examples.com/configs/exchanges.json",
                    "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?n=2",
                    "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "https://indexer.v4staging.dydx.exchange/v4/fills?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/transfers?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?period=WEEKLY",
                    "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
                    "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f?n=2",
                    "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1d67qczf2dz0n30qau2wg893fhpdeekmfu44p4f?period=WEEKLY"
                ]
            """.trimIndent(),
            testRest?.requests,
        )

        compareExpectedRequests(
            """
                [
                    {"type":"subscribe","channel":"v4_markets","batched":"true"},
                    {"type":"subscribe","channel":"v4_subaccounts","id":"$testAddress/0"},
                    {"type":"unsubscribe","channel":"v4_subaccounts","id":"$testAddress/0"}
                ]
            """.trimIndent(),
            testWebSocket?.messages,
        )

        compareExpectedRequests(
            """
                [
                   "getEquityTiers",
                   "getFeeTiers",
                   "getRewardsParams",
                   "getHeight",
                   "getUserFeeTier",
                   "getUserStats",
                   "getAccountBalances",
                   "getDelegatorDelegations",
                   "getNobleBalance",
                   "getAccountBalances",
                   "getDelegatorDelegations",
                   "getNobleBalance"
                ]
            """.trimIndent(),
            testChain?.requests?.map { it.rawValue },
        )
    }

    @Test
    fun settingWalletCosmoAddressToNullShoulUnsubscribeFromSubaccountsChannel() {
        reset()

        val testAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"

        testRest?.setResponse(
            "https://indexer.v4staging.dydx.exchange/v4/addresses/$testAddress",
            mock.accountsChannel.v4accountsReceived,
        )

        setStateMachineReadyToConnect(stateManager)
        setStateMachineConnected(stateManager)

        stateManager.setAddresses(null, testAddress)

        testWebSocket?.simulateReceived(mock.accountsChannel.v4_subscribed)
        stateManager.setAddresses(null, null)

        compareExpectedRequests(
//            """
//                [
//                   "https://api.examples.com/configs/documentation.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/time",
//                   "https://api.examples.com/configs/markets.json",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
//                   "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
//                   "https://api.examples.com/configs/exchanges.json",
//                   "https://indexer.v4staging.dydx.exchange/v4/height",
//                   "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
//                   "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                   "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?period=WEEKLY",
//                   "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
//                   "https://indexer.v4staging.dydx.exchange/v4/fills?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://indexer.v4staging.dydx.exchange/v4/transfers?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
//                   "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?n=2"
//                ]
//            """.trimIndent(),
            """
                [
                    "https://api.examples.com/configs/documentation.json",
                    "https://indexer.v4staging.dydx.exchange/v4/time",
                    "https://indexer.v4staging.dydx.exchange/v4/sparklines?timePeriod=ONE_DAY",
                    "https://indexer.v4staging.dydx.exchange/v4/height",
                    "https://api.examples.com/configs/markets.json",
                    "https://dydx.exchange/v4-launch-incentive/query/ccar-perpetuals",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/chains",
                    "https://squid-api-git-main-cosmos-testnet-0xsquid.vercel.app/v1/tokens",
                    "https://api.examples.com/configs/exchanges.json",
                    "https://indexer.v4staging.dydx.exchange/v4/screen?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "https://dydx.exchange/v4-launch-incentive/query/api/dydx/points/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?n=2",
                    "https://indexer.v4staging.dydx.exchange/v4/addresses/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm",
                    "https://indexer.v4staging.dydx.exchange/v4/fills?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/transfers?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/historical-pnl?address=cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm&subaccountNumber=0",
                    "https://indexer.v4staging.dydx.exchange/v4/historicalTradingRewardAggregations/cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm?period=WEEKLY"
                ]
            """.trimIndent(),
            testRest?.requests,
        )

        compareExpectedRequests(
            """
                [
                    {"type":"subscribe","channel":"v4_markets","batched":"true"},
                    {"type":"subscribe","channel":"v4_subaccounts","id":"$testAddress/0"},
                    {"type":"unsubscribe","channel":"v4_subaccounts","id":"$testAddress/0"}
                ]
            """.trimIndent(),
            testWebSocket?.messages,
        )

        compareExpectedRequests(
            """
                [
                   "getEquityTiers",
                   "getFeeTiers",
                   "getRewardsParams",
                   "getHeight",
                   "getUserFeeTier",
                   "getUserStats",
                   "getAccountBalances",
                   "getDelegatorDelegations",
                   "getNobleBalance"
                ]
            """.trimIndent(),
            testChain?.requests?.map { it.rawValue },
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
