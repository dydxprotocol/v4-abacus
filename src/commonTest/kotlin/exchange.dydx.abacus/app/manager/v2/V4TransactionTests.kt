package exchange.dydx.abacus.app.manager.v2

import exchange.dydx.abacus.app.manager.NetworkTests
import exchange.dydx.abacus.app.manager.TestChain
import exchange.dydx.abacus.app.manager.TestRest
import exchange.dydx.abacus.app.manager.TestState
import exchange.dydx.abacus.app.manager.TestWebSocket
import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.setAddresses
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.v2.manager.AsyncAbacusStateManagerV2
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.state.v2.supervisor.SubaccountConfigs
import exchange.dydx.abacus.state.v2.supervisor.SubaccountSupervisor
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlinx.serialization.json.Json
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
                SubaccountConfigs(true, true, true, true, false),
                testCosmoAddress,
                0,
            )
        } else {
            null
        }
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
            AppConfigsV2.forAppWithIsolatedMargins,
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

    private fun tradeInput(isShortTerm: Boolean, size: String = "0.01", limitPrice: String = "2000") {
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

    private fun assertTransactionQueueStarted(message: String? = null) {
        assertEquals(0, subaccountSupervisor?.transactionQueue?.size)
        assertTrue(subaccountSupervisor?.transactionQueue?.isProcessing ?: false, message)
    }

    private fun assertTransactionQueueEmpty(message: String? = null) {
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
        subaccountSupervisor?.commitPlaceOrder(0, transactionCallback)
        assertTransactionQueueStarted()
        tradeInput(false, "0.02")
        subaccountSupervisor?.commitPlaceOrder(0, transactionCallback)
        subaccountSupervisor?.commitPlaceOrder(0, transactionCallback)
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

        subaccountSupervisor?.cancelOrder(shortTermOrderId, callback = transactionCallback)
        assertTransactionQueueEmpty("Short term order should not be enqueued")
        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)

        // cancel multiple stateful orders
        subaccountSupervisor?.cancelOrder(statefulOrderId1, callback = transactionCallback)
        assertTransactionQueueStarted()
        subaccountSupervisor?.cancelOrder(statefulOrderId2, callback = transactionCallback)
        subaccountSupervisor?.cancelOrder(statefulOrderId3, callback = transactionCallback)
        assertEquals(2, subaccountSupervisor?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(1, subaccountSupervisor?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(4, transactionCalledCount)
        assertTransactionQueueEmpty()
    }

    @Test
    fun testCancelTriggerOrdersWithClosedOrFlippedPositions() {
        setStateMachineConnected(stateManager)
        val canceldOrderPayloads = testChain!!.canceldOrderPayloads
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_parent_subaccounts_subscribed_with_trigger_orders_and_open_positions)
        assertEquals(0, canceldOrderPayloads.size)

        val triggerOrderWithClosedPositionIsolated = "f581f56c-9f1b-54e0-97d6-5f934dd0eb67"
        val triggerOrderWithFlippedPositionCross = "aeb40307-861a-52c1-9568-2a95468e8687"
        val orderIdsToBeCanceled = setOf(triggerOrderWithClosedPositionIsolated, triggerOrderWithFlippedPositionCross)

        testWebSocket?.simulateReceived(mock.accountsChannel.v4_parent_subaccounts_batched_closed_and_flipped_positions)
        // 3 total orders, while only 2 should be canceled
        repeat(3) {
            testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        }
        assertEquals(2, canceldOrderPayloads.size)
        val canceledOrderIds = canceldOrderPayloads.map {
            parser.asNativeMap(Json.parseToJsonElement(it))?.get("orderId")?.toString()?.trim('"')
        }.toSet()
        assertEquals(orderIdsToBeCanceled, canceledOrderIds)
    }

    @Test
    fun testTriggerOrdersTransactionQueue() {
        setStateMachineConnected(stateManager)
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_channel_data_with_orders)

        val transactionQueue = subaccountSupervisor?.transactionQueue
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

        fun simulateNewBtcOrder(slTriggerPrice: String? = btcStopLossTriggerPrice, tpTriggerPrice: String? = btcTakeProfitTriggerPrice, slLimitPrice: String? = btcStopLossLimitPrice, tpLimitPrice: String? = btcTakeProfitLimitPrice): HumanReadableTriggerOrdersPayload {
            triggerOrdersInput("BTC-USD", slTriggerPrice, tpTriggerPrice, slLimitPrice, tpLimitPrice)
            val payload = subaccountSupervisor?.commitTriggerOrders(0, transactionCallback)
            return requireNotNull(payload)
        }

        fun simulateStopLimitOrderReplacement(triggerPrice: String? = ethStopLimitTriggerPrice, limitPrice: String? = ethStopLimitLimitPrice, size: String? = ethStopLimitOrderSize): HumanReadableTriggerOrdersPayload {
            triggerOrdersInput(marketId = "ETH-USD", stopLossTriggerPrice = triggerPrice, stopLossLimitPrice = limitPrice, stopLossOrderId = ethStopLimitOrderId, size = size)
            val payload = subaccountSupervisor?.commitTriggerOrders(0, transactionCallback)
            return requireNotNull(payload)
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
            assertEquals(payload.goodTilTimeInSeconds, 7776000)
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
        assertEquals(2, marketOrders.placeOrderPayloads.size)
        marketOrders.placeOrderPayloads.forEach { it -> validateMarketOrderDefaults(it) }

        assertEquals(2, transactionQueue?.size)
        assertEquals(1, transactionCalledCount)
        clearTransactions(2)

        val limitOrders = simulateNewBtcOrder() // 2 new limit orders created
        assertEquals(2, limitOrders.placeOrderPayloads.size)
        limitOrders.placeOrderPayloads.forEach { it -> validateLimitOrderDefaults(it) }

        assertEquals(2, transactionQueue?.size)
        assertEquals(3, transactionCalledCount)
        clearTransactions(2)

        // Updating Existing Order
        testWebSocket?.simulateReceived(mock.accountsChannel.v4_subaccounts_update_1)
        simulateStopLimitOrderReplacement() // No action here since new order matches the existing order
        assertEquals(0, transactionQueue?.size)

        val replacedOrders = simulateStopLimitOrderReplacement(limitPrice = null) // Replaces order due to removing limit price (limit -> market)
        assertEquals(1, replacedOrders.placeOrderPayloads.size)
        assertEquals(1, replacedOrders.cancelOrderPayloads.size)

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
        assertEquals(1, cancelledOrders.cancelOrderPayloads.size)
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
        subaccountSupervisor?.commitPlaceOrder(0, transactionCallback)
        assertTransactionQueueStarted()
        subaccountSupervisor?.cancelOrder(statefulOrderId1, callback = transactionCallback)
        assertEquals(1, subaccountSupervisor?.transactionQueue?.size)
        triggerOrdersInput(marketId = "BTC-USD", stopLossTriggerPrice = "30000")
        subaccountSupervisor?.commitTriggerOrders(0, transactionCallback)
        assertEquals(2, subaccountSupervisor?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(1, transactionCalledCount)
        assertEquals(1, subaccountSupervisor?.transactionQueue?.size)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(2, transactionCalledCount)

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(3, transactionCalledCount)
        assertTransactionQueueEmpty()
    }

    private fun setStateMachineForIsolatedMarginTests(stateManager: AsyncAbacusStateManagerV2, withPositions: Boolean = false) {
        stateManager.readyToConnect = true
        testWebSocket?.simulateConnected(true)
        testWebSocket?.simulateReceived(mock.connectionMock.connectedMessage)
        testWebSocket?.simulateReceived(mock.marketsChannel.v4_subscribed_r1)
        stateManager.market = "ETH-USD"
        testWebSocket?.simulateReceived(mock.orderbookChannel.load_test_2_subscribed)
        stateManager.setAddresses(null, "dydx155va0m7wz5n8zcqscn9afswwt04n4usj46wvp5")

        if (withPositions) {
            testWebSocket?.simulateReceived(mock.v4ParentSubaccountsMock.subscribed_with_positions)
        } else {
            testWebSocket?.simulateReceived(mock.v4ParentSubaccountsMock.subscribed)
        }
    }

    private fun prepareIsolatedMarginClosePosition() {
        stateManager.closePosition("APE-USD", ClosePositionInputField.market)
        stateManager.closePosition("1", ClosePositionInputField.percent)
    }

    private fun prepareIsolatedMarginTrade(isShortTerm: Boolean) {
        stateManager.trade("2000", TradeInputField.limitPrice)
        stateManager.trade("0.01", TradeInputField.size)
        stateManager.trade("ISOLATED", TradeInputField.marginMode)
        stateManager.trade("2", TradeInputField.targetLeverage)

        if (isShortTerm) {
            stateManager.trade("MARKET", TradeInputField.type)
        } else {
            stateManager.trade("LIMIT", TradeInputField.type)
            stateManager.trade("GTT", TradeInputField.timeInForceType)
        }
    }

    @Test
    fun testIsolatedMarginClosePosition() {
        setStateMachineForIsolatedMarginTests(stateManager, withPositions = true)
        prepareIsolatedMarginClosePosition()
        val closePositionPayload = subaccountSupervisor?.closePositionPayload(0)
        assertNotNull(closePositionPayload, "Close position payload should not be null")
        assertEquals(128, closePositionPayload.subaccountNumber)
    }

    @Test
    fun testIsolatedMarginPlaceOrderTransactions() {
        setStateMachineForIsolatedMarginTests(stateManager)
        prepareIsolatedMarginTrade(false)

        val orderPayload = subaccountSupervisor?.placeOrderPayload(0)
        assertNotNull(orderPayload, "Order payload should not be null")
        assertEquals(256, orderPayload.subaccountNumber, "Should be 256 since 0 and 128 are unavailable")

        val transferPayload = subaccountSupervisor?.getTransferPayloadForIsolatedMarginTrade(orderPayload)
        assertNotNull(transferPayload, "Transfer payload should not be null")
        assertEquals(0, transferPayload.subaccountNumber, "The parent subaccount 0 should be the origin")
        assertEquals(256, transferPayload.destinationSubaccountNumber, "Should have 2 transactions")
    }

    @Test
    fun testIsolatedMarginPlaceShortTermOrderTransactions() {
        setStateMachineForIsolatedMarginTests(stateManager)
        prepareIsolatedMarginTrade(true)

        val orderPayload = subaccountSupervisor?.placeOrderPayload(0)
        assertNotNull(orderPayload, "Order payload should not be null")
        assertEquals(256, orderPayload.subaccountNumber, "Should be 256 since 0 and 128 are unavailable")

        val transferPayload = subaccountSupervisor?.getTransferPayloadForIsolatedMarginTrade(orderPayload)
        assertNotNull(transferPayload, "Transfer payload should not be null")
        assertEquals(0, transferPayload.subaccountNumber, "The parent subaccount 0 should be the origin")
        assertEquals(256, transferPayload.destinationSubaccountNumber, "Should have 2 transactions")
    }

    @Test
    fun testIsolatedMarginPlaceOrderWithReduceOnly() {
        setStateMachineForIsolatedMarginTests(stateManager)
        prepareIsolatedMarginTrade(true)
        stateManager.trade("true", TradeInputField.reduceOnly)
        var transactionData: Any? = null

        val transactionCallback: TransactionCallback = { _, _, data ->
            transactionData = data
        }

        val orderPayload = subaccountSupervisor?.commitPlaceOrder(0, transactionCallback)
        assertNotNull(orderPayload, "Order payload should not be null")

        testChain?.simulateTransactionResponse(testChain!!.dummySuccess)
        assertEquals(orderPayload, transactionData, "Transaction data should match order payload instead of transfer payload")
    }

    @Test
    fun testCancelOrderForChildSubaccount() {
        setStateMachineForIsolatedMarginTests(stateManager)
        val transactionCallback: TransactionCallback = { _, _, _ -> }

        val cancelPayload = subaccountSupervisor?.cancelOrder("24b68694-d6ae-5df4-baf5-55b0716296e9", callback = transactionCallback)
        assertNotNull(cancelPayload, "Cancel payload should not be null")
        assertEquals(128, cancelPayload.subaccountNumber)
    }
}
