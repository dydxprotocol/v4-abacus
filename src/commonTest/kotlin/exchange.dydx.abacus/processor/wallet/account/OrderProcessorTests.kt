package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.account.SubaccountOrderResources
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
    companion object {
        private val updatedAt = Instant.parse("2021-01-01T00:00:00Z")

        val payloadMock = IndexerCompositeOrderObject(
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

        val orderMock = SubaccountOrder(
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
    }
    private val orderProcessor = OrderProcessor(
        parser = Parser(),
        localizer = LocalizerProtocolMock(),
    )

    @Test
    fun testProcess() {
        val order = orderProcessor.process(
            existing = null,
            payload = payloadMock,
            subaccountNumber = 0,
            height = null,
        )
        assertEquals(
            expected = orderMock,
            actual = order,
        )
    }

    @Test
    fun testProcess_partialOrder() {
        val order = orderProcessor.process(
            existing = null,
            payload = payloadMock.copy(totalFilled = "2.0"),
            subaccountNumber = 0,
            height = null,
        )
        assertEquals(
            expected = orderMock.copy(
                totalFilled = 2.0,
                remainingSize = 1.0,
                status = OrderStatus.PartiallyFilled,
                resources = orderMock.resources.copy(
                    statusString = "APP.TRADE.PARTIALLY_FILLED",
                    statusStringKey = "APP.TRADE.PARTIALLY_FILLED",
                ),
            ),
            actual = order,
        )
    }

    @Test
    fun testUpdateHeight_cancelOrder() {
        val existing = orderMock.copy(
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
            expected = orderMock.copy(
                goodTilBlock = 1,
                status = OrderStatus.Canceled,
                updatedAtMilliseconds = Instant.parse("2021-01-01T00:00:00Z").toEpochMilliseconds().toDouble(),
                resources = orderMock.resources.copy(
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
        val existing = orderMock
        val (updatedOrder, updated) = orderProcessor.updateHeight(
            existing = existing,
            height = BlockAndTime(
                block = 1,
                time = Instant.parse("2021-01-01T00:00:00Z"),
            ),
        )
        assertEquals(
            expected = orderMock.copy(
                updatedAtMilliseconds = Instant.parse("2021-01-01T00:00:00Z").toEpochMilliseconds().toDouble(),
            ),
            actual = updatedOrder,
        )

        assertTrue { !updated }
    }

    @Test
    fun testCanceled() {
        val existing = orderMock.copy(
            status = OrderStatus.Open,
        )
        val updatedOrder = orderProcessor.canceled(
            existing = existing,
        )
        assertEquals(
            expected = orderMock.copy(
                status = OrderStatus.Canceling,
                updatedAtMilliseconds = updatedAt.toEpochMilliseconds().toDouble(),
                cancelReason = "USER_CANCELED",
                resources = orderMock.resources.copy(
                    statusString = "APP.TRADE.CANCELING",
                    statusStringKey = "APP.TRADE.CANCELING",
                ),
            ),
            actual = updatedOrder,
        )
    }
}
