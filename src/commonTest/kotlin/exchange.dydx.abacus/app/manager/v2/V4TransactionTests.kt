package exchange.dydx.abacus.app.manager.v2

import exchange.dydx.abacus.app.manager.NetworkTests
import exchange.dydx.abacus.app.manager.TestChain
import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.app.manager.TestState
import exchange.dydx.abacus.app.manager.TestWebSocket
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.setAddresses
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.v2.manager.AsyncAbacusStateManagerV2
import exchange.dydx.abacus.state.v2.manager.StateManagerAdaptorV2
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.state.v2.supervisor.SubaccountConfigs
import exchange.dydx.abacus.state.v2.supervisor.SubaccountSubscriptionType
import exchange.dydx.abacus.state.v2.supervisor.SubaccountSupervisor
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.values
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class V4TransactionTests : NetworkTests() {
    val mock = AbacusMockData()
    private val testCosmoAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
    private var stateManager: AsyncAbacusStateManagerV2 = resetStateManager()
    private var ioImplementations = stateManager.ioImplementations
    private var testRest = stateManager.ioImplementations.rest as? TestRest
    private var testWebSocket = stateManager.ioImplementations.webSocket as? TestWebSocket
    private var testChain = stateManager.ioImplementations.chain as? TestChain
    private var testState = stateManager.stateNotification as? TestState
    private var v4Adapter = stateManager.adaptor
    private var subaccountSupervisor: SubaccountSupervisor? = resetSubaccountSupervisor()

    internal fun resetSubaccountSupervisor(): SubaccountSupervisor? {
        return if (v4Adapter !== null) {
            SubaccountSupervisor(
                v4Adapter!!.stateMachine,
                v4Adapter!!.networkHelper,
                v4Adapter!!.analyticsUtils,
                SubaccountConfigs(true, true, true, SubaccountSubscriptionType.SUBACCOUNT),
                testCosmoAddress,
                0
            )
        } else null
    }

    @BeforeTest
    fun reset() {
        stateManager = resetStateManager()
        ioImplementations = stateManager.ioImplementations
        testRest = stateManager.ioImplementations.rest as? TestRest
        testWebSocket = stateManager.ioImplementations.webSocket as? TestWebSocket
        testChain = stateManager.ioImplementations.chain as? TestChain
        testState = stateManager.stateNotification as? TestState
        v4Adapter = stateManager.adaptor
        subaccountSupervisor = resetSubaccountSupervisor()
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

    private fun setStateMachineConnected(stateManager: AsyncAbacusStateManagerV2) {
        stateManager.readyToConnect = true
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.connectionMock.connectedMessage)
        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_subscribed)
        stateManager.market = "ETH-USD"
        stateManager.setAddresses(null, testCosmoAddress)
    }

    fun tradeInput(isShortTerm: Boolean, size: String = "0.01", limitPrice: String = "2000") {
        if (isShortTerm) {
            stateManager.trade("MARKET", TradeInputField.type)
        } else {
            stateManager.trade("LIMIT", TradeInputField.type)
            stateManager.trade("GTT", TradeInputField.timeInForceType)
            stateManager.trade(limitPrice, TradeInputField.limitPrice)
        }
        stateManager.trade(size, TradeInputField.size)
    }

    fun assertTransactionQueueStarted(message: String? = null) {
        assertEquals(0, subaccountSupervisor?.transactionQueue?.size)
        assertTrue(subaccountSupervisor?.transactionQueue?.isProcessing ?: false, message)
    }

    fun assertTransactionQueueEmpty(message: String? = null) {
        assertEquals(0, subaccountSupervisor?.transactionQueue?.size)
        assertFalse(subaccountSupervisor?.transactionQueue?.isProcessing ?: false, message)
    }

    @Test
    fun testPlaceOrderTransactionsQueue() {
        setStateMachineConnected(stateManager)
        val transactionQueue = subaccountSupervisor?.transactionQueue
        var transactionCalledCount = 0
        val transactionCallback: TransactionCallback = { _, _, _ -> transactionCalledCount++ }

        tradeInput(true)
        subaccountSupervisor?.commitPlaceOrder(0, transactionCallback)
        assertTransactionQueueEmpty("Short term order should not be enqueued")
        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(1, transactionCalledCount)

        // place multiple stateful orders
        tradeInput(false, "0.01")
        subaccountSupervisor?.commitPlaceOrder(0,transactionCallback)
        assertTransactionQueueStarted()
        tradeInput(false, "0.02")
        subaccountSupervisor?.commitPlaceOrder(0,transactionCallback)
        subaccountSupervisor?.commitPlaceOrder(0,transactionCallback)
        assertEquals(2, transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(2, transactionCalledCount)
        assertEquals(1, transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(3, transactionCalledCount)
        assertEquals(0, transactionQueue?.size)
        assertTrue(transactionQueue?.isProcessing ?: false)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(4, transactionCalledCount)
        assertTransactionQueueEmpty()
    }

    @Test
    fun testCancelOrders() {
        setStateMachineConnected(stateManager)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_channel_data_with_orders)

        var transactionCalledCount = 0
        val transactionCallback: TransactionCallback = { _, _, _ -> transactionCalledCount++ }

        val shortTermOrderId = "770933a5-0293-5aca-8a01-d9c4030d776d"
        val statefulOrderId1 = "31d7d484-8685-570c-aa62-c2589cb6c8d8"
        val statefulOrderId2 = "0ae98da9-4fdc-5f08-b880-2449464b6b45"
        val statefulOrderId3 = "734617f4-29ba-50fe-878d-391ad4e4fbd1"

        subaccountSupervisor?.cancelOrder(shortTermOrderId, transactionCallback)
        assertTransactionQueueEmpty("Short term order should not be enqueued")
        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)

        // cancel multiple stateful orders
        subaccountSupervisor?.cancelOrder(statefulOrderId1, transactionCallback)
        assertTransactionQueueStarted()
        subaccountSupervisor?.cancelOrder(statefulOrderId2, transactionCallback)
        subaccountSupervisor?.cancelOrder(statefulOrderId3, transactionCallback)
        assertEquals(2, subaccountSupervisor?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(1, subaccountSupervisor?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(4, transactionCalledCount)
        assertTransactionQueueEmpty()
    }

    @Test
    fun testMixedTransactions() {
        setStateMachineConnected(stateManager)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_channel_data_with_orders)

        var transactionCalledCount = 0
        val transactionCallback: TransactionCallback = { _, _, _ -> transactionCalledCount++ }

        val statefulOrderId1 = "31d7d484-8685-570c-aa62-c2589cb6c8d8"

        tradeInput(false)
        assertTransactionQueueEmpty()
        subaccountSupervisor?.commitPlaceOrder(0, transactionCallback)
        assertTransactionQueueStarted()
        subaccountSupervisor?.cancelOrder(statefulOrderId1, transactionCallback)
        assertEquals(1, subaccountSupervisor?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(1, transactionCalledCount)
        assertEquals(0, subaccountSupervisor?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(2, transactionCalledCount)
        assertTransactionQueueEmpty()
    }
}