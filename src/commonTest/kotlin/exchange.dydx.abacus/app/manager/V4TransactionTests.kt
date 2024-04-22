package exchange.dydx.abacus.app.manager

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.state.manager.AppConfigs
import exchange.dydx.abacus.state.manager.AsyncAbacusStateManager
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.V4StateManagerAdaptor
import exchange.dydx.abacus.state.manager.setAddresses
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
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

    fun triggerOrdersInput(marketId: String, stopLossTriggerPrice: String? = null, takeProfitTriggerPrice: String? = null, stopLossLimitPrice: String? = null, takeProfitLimitPrice: String? = null, stopLossOrderId: String? = null, takeProfitOrderId: String? = null, size: String? = "1") {
        stateManager.triggerOrders(marketId, TriggerOrdersInputField.marketId)
        stateManager.triggerOrders(size, TriggerOrdersInputField.size)

        // SL
        if (stopLossLimitPrice != null) {
            stateManager.triggerOrders("STOP_LIMIT", TriggerOrdersInputField.stopLossOrderType)
        } else {
            stateManager.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType)
        }
        stateManager.triggerOrders(stopLossLimitPrice, TriggerOrdersInputField.stopLossLimitPrice)
        stateManager.triggerOrders(stopLossOrderId, TriggerOrdersInputField.stopLossOrderId)
        stateManager.triggerOrders(stopLossTriggerPrice, TriggerOrdersInputField.stopLossPrice)

        // TP
        if (takeProfitLimitPrice != null) {
            stateManager.triggerOrders("TAKE_PROFIT", TriggerOrdersInputField.takeProfitOrderType)
        } else {
            stateManager.triggerOrders("TAKE_PROFIT_MARKET", TriggerOrdersInputField.takeProfitOrderType)
        }
        stateManager.triggerOrders(takeProfitLimitPrice, TriggerOrdersInputField.takeProfitLimitPrice)
        stateManager.triggerOrders(takeProfitOrderId, TriggerOrdersInputField.takeProfitOrderId)
        stateManager.triggerOrders(takeProfitTriggerPrice, TriggerOrdersInputField.takeProfitPrice)
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
    fun testTriggerOrdersTransactionQueue() {
        setStateMachineConnected(stateManager)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_channel_data_with_orders)

        val transactionQueue = v4Adapter?.transactionQueue
        var transactionCalledCount = 0
        val transactionCallback: TransactionCallback = { _, _, _ -> transactionCalledCount++ }

        val btcStopLossTriggerPrice = "30000"
        val btcStopLossLimitPrice = "31000"
        val btcTakeProfitTriggerPrice = "2000"
        val btcTakeProfitLimitPrice = "2100"

        val ethStopLimitOrderId = "31d7d484-8685-570c-aa62-c2589cb6c8d8"
        val ethStopLimitOrderSize = "0.01"
        val ethStopLimitTriggerPrice = "3300"
        val ethStopLimitLimitPrice = "2100"

        fun simulateNewBtcOrder(slTriggerPrice: String? = btcStopLossTriggerPrice, tpTriggerPrice: String? = btcTakeProfitTriggerPrice, slLimitPrice: String? = btcStopLossLimitPrice, tpLimitPrice: String? = btcTakeProfitLimitPrice): HumanReadableTriggerOrdersPayload? {
            triggerOrdersInput("BTC-USD", slTriggerPrice, tpTriggerPrice, slLimitPrice, tpLimitPrice)
            return v4Adapter?.commitTriggerOrders(transactionCallback)
        }

        fun simulateStopLimitOrderReplacement(triggerPrice: String? = ethStopLimitTriggerPrice, limitPrice: String? = ethStopLimitLimitPrice, size: String? = ethStopLimitOrderSize): HumanReadableTriggerOrdersPayload? {
            triggerOrdersInput(marketId = "ETH-USD", stopLossTriggerPrice = triggerPrice, stopLossLimitPrice = limitPrice, stopLossOrderId = ethStopLimitOrderId, size = size)
            return v4Adapter?.commitTriggerOrders(transactionCallback)
        }

        fun validateMarketOrderDefaults(payload: HumanReadablePlaceOrderPayload) {
            assertEquals(payload.execution, "IOC")
            assertEquals(payload.timeInForce, null)
            assertEquals(payload.reduceOnly, true)
            assertEquals(payload.postOnly, false)
        }

        fun validateLimitOrderDefaults(payload: HumanReadablePlaceOrderPayload) {
            assertEquals(payload.execution, "DEFAULT")
            assertEquals(payload.timeInForce, null)
            assertEquals(payload.goodTilTimeInSeconds, 2419200)
            assertEquals(payload.reduceOnly, true)
            assertEquals(payload.postOnly, false)
        }

        fun clearTransactions(numTimes: Int) {
            repeat(numTimes) {
                testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
            }
            assertEquals(0, transactionQueue?.size)
        }

        simulateNewBtcOrder(null, null, null, null)
        assertTransactionQueueEmpty("No orders enqueued")
        assertEquals(1, transactionCalledCount) // Signals to FE that actions have been processed

        simulateNewBtcOrder(tpTriggerPrice = null, slLimitPrice = null, tpLimitPrice = null)
        assertTransactionQueueStarted()

        // Creating New Orders
        val marketOrders = simulateNewBtcOrder(slLimitPrice = null, tpLimitPrice = null) // 2 new market orders created
        assertEquals(2, marketOrders?.placeOrderPayloads?.size)
        marketOrders?.placeOrderPayloads?.forEach { it -> validateMarketOrderDefaults(it) }

        assertEquals(2, transactionQueue?.size)
        assertEquals(1, transactionCalledCount)
        clearTransactions(2)

        val limitOrders = simulateNewBtcOrder() // 2 new limit orders created
        assertEquals(2, limitOrders?.placeOrderPayloads?.size)
        limitOrders?.placeOrderPayloads?.forEach { it -> validateLimitOrderDefaults(it) }

        assertEquals(2, transactionQueue?.size)
        assertEquals(3, transactionCalledCount)
        clearTransactions(2)

        // Updating Existing Order
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_subaccounts_update_1)

        simulateStopLimitOrderReplacement() // No action here since new order matches the existing order
        assertEquals(0, transactionQueue?.size)

        val replacedOrders = simulateStopLimitOrderReplacement(limitPrice = null) // Replaces order due to removing limit price (limit -> market)
        assertEquals(1, replacedOrders?.placeOrderPayloads?.size)
        assertEquals(1, replacedOrders?.cancelOrderPayloads?.size)

        assertEquals(2, transactionQueue?.size)
        clearTransactions(2)

        simulateStopLimitOrderReplacement(limitPrice = "2200") // Replaces order due to different limit price
        assertEquals(2, transactionQueue?.size)
        clearTransactions(2)

        simulateStopLimitOrderReplacement(triggerPrice = "3400") // Replaces order due to different trigger price
        assertEquals(2, transactionQueue?.size)
        clearTransactions(2)

        simulateStopLimitOrderReplacement(size = "0.2") // Replaces order due to different size
        assertEquals(2, transactionQueue?.size)
        clearTransactions(2)

        // Canceling Existing Orders
        val cancelledOrders = simulateStopLimitOrderReplacement(triggerPrice = null, limitPrice = null) // Cancels existing order due to null price inputs
        assertEquals(1, cancelledOrders?.cancelOrderPayloads?.size)
        assertEquals(1, transactionQueue?.size)
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
        triggerOrdersInput(marketId = "BTC-USD", stopLossTriggerPrice = "30000")
        v4Adapter?.commitTriggerOrders(transactionCallback)
        assertEquals(2, v4Adapter?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(1, transactionCalledCount)
        assertEquals(1, v4Adapter?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(2, transactionCalledCount)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(3, transactionCalledCount)
        assertTransactionQueueEmpty()
    }
}
