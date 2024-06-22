package exchange.dydx.abacus.app.helper

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.protocols.ToastType
import exchange.dydx.abacus.state.app.helper.TriggerOrderToastGenerator
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.tests.mock.PresentationProtocolMock
import exchange.dydx.abacus.tests.mock.ThreadingProtocolMock
import exchange.dydx.abacus.utils.DummyFormatter
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_FLAGS
import kollections.iListOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TriggerOrderToastGeneratorTests {
    private val presentation = PresentationProtocolMock()
    private val parser = Parser()
    private val localizer = LocalizerProtocolMock()
    private val threading = ThreadingProtocolMock()
    private val formatter = DummyFormatter()

    @OptIn(ExperimentalSerializationApi::class)
    private val state: PerpetualState = run {
        val stateString =
            """
{
    "availableSubaccountNumbers": [],
    "input": {
        "triggerOrders": {
            "marketId": "ETH-USD"
        }
    },
    "marketsSummary": {
        "markets": {
            "ETH-USD": {
                "id": "id",
                "assetId": "assetId"
            }
        }
    },
    "account": {
        "subaccounts": {
            "0": {
                "subaccountNumber": 0,
                "orders": [
                    {
                        "id": "existingOrderId",
                        "type": "TakeProfitMarket",
                        "side": "Buy",
                        "status": "Untriggered",
                        "marketId": "ETH-USD",
                        "price": 1000,
                        "triggerPrice": 2000,
                        "size": 1,
                        "postOnly": false,
                        "reduceOnly": false,
                        "resources": {
                            "sideStringKey": "BUY"
                        }
                    }
                ],
                "openPositions": [
                    {
                        "id": "ETH-USD",
                        "assetId": "assetId",
                        "side": {
                            "current": "SHORT"
                        },
                        "size": {
                            "current": 94.57
                        },
                        "resources": {
                            "sideString": {},
                            "sideStringKey": {},
                            "indicator": {}
                        },
                        "entryPrice": {},
                        "realizedPnl": {},
                        "realizedPnlPercent": {},
                        "unrealizedPnl": {},
                        "unrealizedPnlPercent": {},
                        "notionalTotal": {},
                        "valueTotal": {},
                        "initialRiskTotal": {},
                        "adjustedImf": {},
                        "adjustedMmf": {},
                        "leverage": {},
                        "maxLeverage": {},
                        "buyingPower": {},
                        "liquidationPrice": {},
                        "freeCollateral": {},
                        "marginUsage": {},
                        "quoteBalance": {},
                        "equity": {},
                        "marginValue": {}
                    }
                ]
            }
        }
    }
}
            """.trimIndent()

        val json = Json {
            explicitNulls = false
        }
        json.decodeFromString<PerpetualState>(stateString)
    }

    @Test
    fun testOnTriggerOrderSubmitted() {
        val toastGenerator = createToastGenerator()

        toastGenerator.onTriggerOrderSubmitted(
            subaccountNumber = 0,
            payload = HumanReadableTriggerOrdersPayload(
                marketId = "ETH-USD",
                positionSize = 94.57,
                cancelOrderPayloads = iListOf(),
                placeOrderPayloads = iListOf(),
            ),
            state = state,
        )
        assertEquals(presentation.showToastCallCount, 0)

        toastGenerator.onTriggerOrderSubmitted(
            subaccountNumber = 0,
            payload = HumanReadableTriggerOrdersPayload(
                marketId = "ETH-USD",
                positionSize = 94.57,
                cancelOrderPayloads = iListOf(
                    HumanReadableCancelOrderPayload(
                        subaccountNumber = 0,
                        clientId = 0,
                        type = "TAKE_PROFIT",
                        orderId = "existingOrderId",
                        orderFlags = SHORT_TERM_ORDER_FLAGS,
                        clobPairId = 0,
                        goodTilBlock = null,
                        goodTilBlockTime = null,
                    ),
                ),
                placeOrderPayloads = iListOf(
                    HumanReadablePlaceOrderPayload(
                        subaccountNumber = 0,
                        marketId = "ETH-USD",
                        clientId = 0,
                        type = "STOP_LIMIT",
                        side = "buy",
                        price = 1000.0,
                        triggerPrice = 1500.0,
                        size = 1.0,
                        postOnly = false,
                        reduceOnly = false,
                        timeInForce = null,
                        execution = null,
                        goodTilTimeInSeconds = null,
                        goodTilBlock = null,
                        marketInfo = null,
                        currentHeight = null,
                    ),
                ),
            ),
            state = state,
        )
        // one cancel and one place order
        assertEquals(presentation.showToastCallCount, 2)

        val cancel = presentation.toasts[0]!!
        assertTrue { cancel.title.contains("NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVING.TITLE") }
        assertTrue { cancel.text!!.contains("NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVING.BODY") }
        assertTrue { cancel.text!!.contains("2000") }

        val placeOrder = presentation.toasts[1]!!
        assertTrue { placeOrder.title.contains("NOTIFICATIONS.STOP_LOSS_TRIGGER_CREATING.TITLE") }
        assertTrue { placeOrder.text!!.contains("NOTIFICATIONS.STOP_LOSS_TRIGGER_CREATING.BODY") }
        assertTrue { placeOrder.text!!.contains("1500") }
    }

    @Test
    fun testOnTriggerOrderResponse_RemoveAndPlaceOrder() {
        val toastGenerator = createToastGenerator()

        toastGenerator.onTriggerOrderSubmitted(
            subaccountNumber = 0,
            payload = HumanReadableTriggerOrdersPayload(
                marketId = "ETH-USD",
                positionSize = 94.57,
                cancelOrderPayloads = iListOf(),
                placeOrderPayloads = iListOf(),
            ),
            state = state,
        )

        toastGenerator.onTriggerOrderResponse(
            subaccountNumber = 0,
            successful = true,
            error = null,
            data = HumanReadableTriggerOrdersPayload(
                marketId = "ETH-USD",
                positionSize = 94.57,
                cancelOrderPayloads = iListOf(
                    HumanReadableCancelOrderPayload(
                        subaccountNumber = 0,
                        clientId = 0,
                        type = "TAKE_PROFIT",
                        orderId = "existingOrderId",
                        orderFlags = SHORT_TERM_ORDER_FLAGS,
                        clobPairId = 0,
                        goodTilBlock = null,
                        goodTilBlockTime = null,
                    ),
                ),
                placeOrderPayloads = iListOf(),
            ),
        )
        assertEquals(presentation.showToastCallCount, 1)

        val cancel = presentation.toasts[0]!!
        assertTrue { cancel.title.contains("NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVED.TITLE") }
        assertTrue { cancel.text!!.contains("NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVED.BODY") }
        assertTrue { cancel.text!!.contains("2000") }
        assertEquals(cancel.type, ToastType.Info)

        toastGenerator.onTriggerOrderResponse(
            subaccountNumber = 0,
            successful = true,
            error = null,
            data = HumanReadableTriggerOrdersPayload(
                marketId = "ETH-USD",
                positionSize = 94.57,
                cancelOrderPayloads = iListOf(),
                placeOrderPayloads = iListOf(
                    HumanReadablePlaceOrderPayload(
                        subaccountNumber = 0,
                        marketId = "ETH-USD",
                        clientId = 0,
                        type = "TAKE_PROFIT_MARKET",
                        side = "buy",
                        price = 1000.0,
                        triggerPrice = 3000.0,
                        size = 1.0,
                        postOnly = false,
                        reduceOnly = false,
                        timeInForce = null,
                        execution = null,
                        goodTilTimeInSeconds = null,
                        goodTilBlock = null,
                        marketInfo = null,
                        currentHeight = null,
                    ),
                ),
            ),
        )
        assertEquals(presentation.showToastCallCount, 2)

        val placeOrder = presentation.toasts[1]!!
        assertTrue { placeOrder.title.contains("NOTIFICATIONS.TAKE_PROFIT_TRIGGER_CREATED.TITLE") }
        assertTrue { placeOrder.text!!.contains("NOTIFICATIONS.TAKE_PROFIT_TRIGGER_CREATED.BODY") }
        assertTrue { placeOrder.text!!.contains("3000") }
        assertEquals(placeOrder.type, ToastType.Info)
    }

    @Test
    fun testOnTriggerOrderResponse_RemoveFailed() {
        val toastGenerator = createToastGenerator()

        toastGenerator.onTriggerOrderSubmitted(
            subaccountNumber = 0,
            payload = HumanReadableTriggerOrdersPayload(
                marketId = "ETH-USD",
                positionSize = 94.57,
                cancelOrderPayloads = iListOf(),
                placeOrderPayloads = iListOf(),
            ),
            state = state,
        )

        toastGenerator.onTriggerOrderResponse(
            subaccountNumber = 0,
            successful = false,
            error = null,
            data = HumanReadableTriggerOrdersPayload(
                marketId = "ETH-USD",
                positionSize = 94.57,
                cancelOrderPayloads = iListOf(
                    HumanReadableCancelOrderPayload(
                        subaccountNumber = 0,
                        type = "TAKE_PROFIT",
                        clientId = 0,
                        orderId = "existingOrderId",
                        orderFlags = SHORT_TERM_ORDER_FLAGS,
                        clobPairId = 0,
                        goodTilBlock = null,
                        goodTilBlockTime = null,
                    ),
                ),
                placeOrderPayloads = iListOf(),
            ),
        )
        assertEquals(presentation.showToastCallCount, 1)

        val cancelFailed = presentation.toasts[0]!!
        assertTrue { cancelFailed.title.contains("NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVING_ERROR.TITLE") }
        assertTrue { cancelFailed.text!!.contains("NOTIFICATIONS.TAKE_PROFIT_TRIGGER_REMOVING_ERROR.BODY") }
        assertTrue { cancelFailed.text!!.contains("2000") }
        assertEquals(cancelFailed.type, ToastType.Warning)
    }

    private fun createToastGenerator() =
        TriggerOrderToastGenerator(
            presentation,
            parser,
            formatter,
            localizer,
            threading,
        )
}
