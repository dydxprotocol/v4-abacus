package exchange.dydx.abacus.app.manager

import exchange.dydx.abacus.payload.BaseTests
import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.state.manager.AppConfigs
import exchange.dydx.abacus.state.manager.AsyncAbacusStateManager
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.StateManagerAdaptor
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.V4StateManagerAdaptor
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.manager.setAddresses
import exchange.dydx.abacus.tests.payloads.AbacusMockData
import exchange.dydx.abacus.utils.Numeric
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V4TransactionsTests: V4BaseTests() {
    private val testCosmoAddress = "cosmos1fq8q55896ljfjj7v3x0qd0z3sr78wmes940uhm"
    private var stateManager: TestV4AsyncAbacusStateManager = resetStateManager()
    private var ioImplementations = stateManager.ioImplementations
    private var testRest = stateManager.ioImplementations.rest as? TestRest
    private var testChain = stateManager.ioImplementations.chain as? TestChain
    private var testState = stateManager.stateNotification as? TestState

    @BeforeTest
    override fun reset() {
        stateManager = resetStateManager()
        ioImplementations = stateManager.ioImplementations
        testRest = stateManager.ioImplementations.rest as? TestRest
        testChain = stateManager.ioImplementations.chain as? TestChain
        testState = stateManager.stateNotification as? TestState
        setup()
    }

    fun resetStateManager(): TestV4AsyncAbacusStateManager {
        val ioImplementations = testIOImplementations()
        val localizer = testLocalizer(ioImplementations)
        val uiImplementations = testUIImplementations(localizer)
        stateManager = TestV4AsyncAbacusStateManager(
            "https://api.examples.com",
            mock.v4Environment,
            ioImplementations,
            uiImplementations,
            V4StateManagerConfigs("https://api.examples.com", mock.v4Environment),
            AppConfigs(false),
            null,
            null
        )
        return stateManager
    }

    fun cancelOrderPayload(orderId: String, isShortTerm: Boolean): HumanReadableCancelOrderPayload {
        return HumanReadableCancelOrderPayload(
            0,
            orderId,
            256189119,
            if (isShortTerm) 0 else 64,
            1,
            null,
            null
        )
    }

    fun placeOrderPayload(isShortTerm: Boolean): HumanReadablePlaceOrderPayload {
        return HumanReadablePlaceOrderPayload(
            0,
            "ETH-USD",
            1715327590,
            if (isShortTerm) "MARKET" else "LIMIT",
            "BUY",
            parser.asDouble(3600) ?: Numeric.double.ZERO,
            null,
            0.1,
            false,
            null,
            "GTT",
            null,
            280000,
        )
    }

    @Test
    fun testCancelOrders() {
        var transactionCalledCount = 0
        val shortTermOrderId1 = "short-term-order-1"
        val shortTermOrderId2 = "short-term-order-2"
        val statefulOrderId1 = "stateful-order-1"
        val statefulOrderId2 = "stateful-order-2"

        val shortTermCancelOrderPayload1 = cancelOrderPayload(shortTermOrderId1, true)
        val shortTermCancelOrderPayload2 = cancelOrderPayload(shortTermOrderId2, true)
        val statefulCancelOrderPayload1 = cancelOrderPayload(statefulOrderId1, false)
        val statefulCancelOrderPayload2 = cancelOrderPayload(statefulOrderId2, false)

        val mockCallback: TransactionCallback = { successful, error, data ->
            transactionCalledCount++
        }

        stateManager.cancelOrderTestPayload = shortTermCancelOrderPayload1
        stateManager.cancelOrder(shortTermOrderId1, mockCallback)
        assertEquals(transactionCalledCount, 1)
        assertEquals(stateManager.orderCanceledViaQueueCount, 0)

        stateManager.cancelOrderTestPayload = statefulCancelOrderPayload1
        stateManager.cancelOrder(statefulOrderId1, mockCallback)
        assertEquals(transactionCalledCount, 2)
        assertEquals(stateManager.orderCanceledViaQueueCount, 1)

        stateManager.cancelOrderTestPayload = statefulCancelOrderPayload2
        stateManager.cancelOrder(statefulOrderId2, mockCallback)
        stateManager.cancelOrderTestPayload = shortTermCancelOrderPayload2
        stateManager.cancelOrder(shortTermOrderId2, mockCallback)
        assertEquals(transactionCalledCount, 4)
        assertEquals(stateManager.orderCanceledViaQueueCount, 2)
    }

    @Test
    fun testStatefulPlaceOrder() {
        var transactionCalledCount = 0
        val shortTermPlaceOrderPayload1 = placeOrderPayload(true)
        val shortTermPlaceOrderPayload2 = placeOrderPayload(true)
        val statefulPlaceOrderPayload1 = placeOrderPayload(false)
        val statefulPlaceOrderPayload2 = placeOrderPayload(false)
        val mockCallback: TransactionCallback = { successful, error, data ->
            transactionCalledCount++
        }

        stateManager.placeOrderTestPayload = shortTermPlaceOrderPayload1
        stateManager.commitPlaceOrder(mockCallback)
        assertEquals(transactionCalledCount, 1)
        assertEquals(stateManager.orderPlacedViaQueueCount, 0)

        stateManager.placeOrderTestPayload = statefulPlaceOrderPayload1
        stateManager.commitPlaceOrder(mockCallback)
        assertEquals(transactionCalledCount, 2)
        assertEquals(stateManager.orderPlacedViaQueueCount, 1)

        stateManager.placeOrderTestPayload = statefulPlaceOrderPayload2
        stateManager.commitPlaceOrder(mockCallback)
        stateManager.placeOrderTestPayload = shortTermPlaceOrderPayload2
        stateManager.commitPlaceOrder(mockCallback)
        assertEquals(transactionCalledCount, 4)
        assertEquals(stateManager.orderPlacedViaQueueCount, 2)
    }
}
