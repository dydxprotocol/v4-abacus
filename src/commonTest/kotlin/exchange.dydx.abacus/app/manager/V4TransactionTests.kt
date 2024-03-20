package exchange.dydx.abacus.app.manager

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.state.manager.AppConfigs
import exchange.dydx.abacus.state.manager.AsyncAbacusStateManager
import exchange.dydx.abacus.state.manager.V4StateManagerAdaptor
import exchange.dydx.abacus.state.manager.setAddresses
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class V4TransactionTests : NetworkTests() {
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
            null,
        )
        stateManager.environmentId = "dydxprotocol-staging"
        return stateManager
    }

    private fun setStateMachineConnected(stateManager: AsyncAbacusStateManager) {
        stateManager.readyToConnect = true
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.connectionMock.connectedMessage)
        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_subscribed)
        stateManager.market = "ETH-USD"
        stateManager.setAddresses(null, testCosmoAddress)
        v4Adapter?.connectedSubaccountNumber = 0
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
        assertEquals(0, v4Adapter?.transactionQueue?.size)
        assertTrue(v4Adapter?.transactionQueue?.isProcessing ?: false, message)
    }

    fun assertTransactionQueueEmpty(message: String? = null) {
        assertEquals(0, v4Adapter?.transactionQueue?.size)
        assertFalse(v4Adapter?.transactionQueue?.isProcessing ?: false, message)
    }

    @Test
    fun testPlaceOrderTransactionsQueue() {
        setStateMachineConnected(stateManager)
        val transactionQueue = v4Adapter?.transactionQueue
        var transactionCalledCount = 0
        val transactionCallback: TransactionCallback = { _, _, _ -> transactionCalledCount++ }

        tradeInput(true)
        v4Adapter?.commitPlaceOrder(transactionCallback)
        assertTransactionQueueEmpty("Short term order should not be enqueued")
        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(1, transactionCalledCount)

        // place multiple stateful orders
        tradeInput(false, "0.01")
        v4Adapter?.commitPlaceOrder(transactionCallback)
        assertTransactionQueueStarted()
        tradeInput(false, "0.02")
        v4Adapter?.commitPlaceOrder(transactionCallback)
        v4Adapter?.commitPlaceOrder(transactionCallback)
        assertEquals(2, v4Adapter?.transactionQueue?.size)

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

        v4Adapter?.cancelOrder(shortTermOrderId, transactionCallback)
        assertTransactionQueueEmpty("Short term order should not be enqueued")
        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)

        // cancel multiple stateful orders
        v4Adapter?.cancelOrder(statefulOrderId1, transactionCallback)
        assertTransactionQueueStarted()
        v4Adapter?.cancelOrder(statefulOrderId2, transactionCallback)
        v4Adapter?.cancelOrder(statefulOrderId3, transactionCallback)
        assertEquals(2, v4Adapter?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(1, v4Adapter?.transactionQueue?.size)

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
        v4Adapter?.commitPlaceOrder(transactionCallback)
        assertTransactionQueueStarted()
        v4Adapter?.cancelOrder(statefulOrderId1, transactionCallback)
        assertEquals(1, v4Adapter?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(1, transactionCalledCount)
        assertEquals(0, v4Adapter?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(2, transactionCalledCount)
        assertTransactionQueueEmpty()
    }
}