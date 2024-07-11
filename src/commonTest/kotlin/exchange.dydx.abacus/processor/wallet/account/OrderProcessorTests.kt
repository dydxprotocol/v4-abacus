package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.SubaccountOrderResources
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerAPIOrderStatus
import indexer.codegen.IndexerOrderSide
import indexer.codegen.IndexerOrderType
import indexer.models.IndexerCompositeOrderObject
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderProcessorTests {
    private val orderProcessor = OrderProcessor(
        parser = Parser(),
        localizer = LocalizerProtocolMock(),
    )

    private val updatedAt = Instant.parse("2021-01-01T00:00:00Z")

    private val orderPayload = IndexerCompositeOrderObject(
        id = "1",
        subaccountId = "0",
        size = "3.0",
        price = "3.0",
        reduceOnly = true,
        side = IndexerOrderSide.BUY,
        status = IndexerAPIOrderStatus.OPEN,
        type = IndexerOrderType.LIMIT,
        ticker = "WETH-DAI",
        updatedAt = updatedAt.toString(),
        clientId = "1",
        subaccountNumber = 0.0,
    )

    private val expectedOrder = SubaccountOrder(
        subaccountNumber = 0,
        id = "1",
        clientId = 1,
        type = OrderType.Limit,
        side = OrderSide.Buy,
        status = OrderStatus.Open,
        timeInForce = null,
        marketId = "WETH-DAI",
        clobPairId = null,
        orderFlags = null,
        price = 3.0,
        triggerPrice = null,
        trailingPercent = null,
        size = 3.0,
        remainingSize = 3.0,
        totalFilled = 0.0,
        goodTilBlock = null,
        goodTilBlockTime = null,
        createdAtHeight = null,
        createdAtMilliseconds = null,
        unfillableAtMilliseconds = null,
        expiresAtMilliseconds = null,
        updatedAtMilliseconds = updatedAt.toEpochMilliseconds().toDouble(),
        postOnly = false,
        reduceOnly = true,
        cancelReason = null,
        resources = SubaccountOrderResources(
            sideString = "APP.GENERAL.BUY",
            statusString = "APP.TRADE.OPEN_STATUS",
            typeString = "APP.TRADE.LIMIT_ORDER_SHORT",
            timeInForceString = null,
            sideStringKey = "APP.GENERAL.BUY",
            statusStringKey = "APP.TRADE.OPEN_STATUS",
            typeStringKey = "APP.TRADE.LIMIT_ORDER_SHORT",
            timeInForceStringKey = null,
        ),
        marginMode = MarginMode.Cross,
    )

    @Test
    fun testProcess() {
        val order = orderProcessor.process(
            existing = null,
            payload = orderPayload,
            subaccountNumber = 0,
            height = null,
        )
        assertEquals(
            expected = expectedOrder,
            actual = order,
        )
    }

    @Test
    fun testProcess_partialOrder() {
        val order = orderProcessor.process(
            existing = null,
            payload = orderPayload.copy(totalFilled = "2.0"),
            subaccountNumber = 0,
            height = null,
        )
        assertEquals(
            expected = expectedOrder.copy(
                totalFilled = 2.0,
                remainingSize = 1.0,
                status = OrderStatus.PartiallyFilled,
                resources = expectedOrder.resources.copy(
                    statusString = "APP.TRADE.PARTIALLY_FILLED",
                    statusStringKey = "APP.TRADE.PARTIALLY_FILLED",
                ),
            ),
            actual = order,
        )
    }

    @Test
    fun testUpdateHeight_cancelOrder() {
        val existing = expectedOrder.copy(
            goodTilBlock = 1,
            status = OrderStatus.Pending,
        )
        val (updatedOrder, updated) = orderProcessor.updateHeight(
            existing = existing,
            height = BlockAndTime(
                block = 2,
                time = Instant.parse("2021-01-01T00:00:00Z"),
            ),
        )
        assertEquals(
            expected = expectedOrder.copy(
                goodTilBlock = 1,
                status = OrderStatus.Canceled,
                updatedAtMilliseconds = Instant.parse("2021-01-01T00:00:00Z").toEpochMilliseconds().toDouble(),
                resources = expectedOrder.resources.copy(
                    statusString = "APP.TRADE.CANCELED",
                    statusStringKey = "APP.TRADE.CANCELED",
                ),
            ),
            actual = updatedOrder,
        )

        assertTrue { updated }
    }

    @Test
    fun testUpdateHeight_skipped() {
        val existing = expectedOrder
        val (updatedOrder, updated) = orderProcessor.updateHeight(
            existing = existing,
            height = BlockAndTime(
                block = 1,
                time = Instant.parse("2021-01-01T00:00:00Z"),
            ),
        )
        assertEquals(
            expected = expectedOrder.copy(
                updatedAtMilliseconds = Instant.parse("2021-01-01T00:00:00Z").toEpochMilliseconds().toDouble(),
            ),
            actual = updatedOrder,
        )

        assertTrue { !updated }
    }
}
