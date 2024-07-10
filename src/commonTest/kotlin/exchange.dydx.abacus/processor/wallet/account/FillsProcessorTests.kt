package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.FillLiquidity
import exchange.dydx.abacus.output.SubaccountFill
import exchange.dydx.abacus.output.SubaccountFillResources
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.tests.mock.processor.wallet.account.FillProcessorMock
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerFillResponseObject
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class FillsProcessorTests {
    private val fillProcessor = FillProcessorMock()
    private val fillsProcessor = FillsProcessor(
        parser = Parser(),
        localizer = LocalizerProtocolMock(),
        fillProcessor = fillProcessor,
    )

    @Test
    fun testProcess_emptyPayload() {
        val output = fillsProcessor.process(
            existing = null,
            payload = emptyList(),
            subaccountNumber = 0,
        )
        assertEquals(0, output.size)
    }

    @Test
    fun testProcess_nonEmptyPayload() {
        fillProcessor.processAction = { input, _ ->
            createSubaccountFill(input.id!!)
        }

        val output = fillsProcessor.process(
            existing = null,
            payload = listOf(
                IndexerFillResponseObject(
                    id = "1",
                ),
            ),
            subaccountNumber = 0,
        )
        assertEquals(1, output.size)
        assertEquals(output[0].id, "1")
    }

    @Test
    fun testProcess_withMerge() {
        fillProcessor.processAction = { input, _ ->
            createSubaccountFill(input.id!!)
        }

        val output = fillsProcessor.process(
            existing = listOf(
                createSubaccountFill("1"),
            ),
            payload = listOf(
                IndexerFillResponseObject(
                    id = "1",
                ),
                IndexerFillResponseObject(
                    id = "2",
                ),
            ),
            subaccountNumber = 0,
        )
        assertEquals(2, output.size)
        assertEquals(output[0].id, "1")
        assertEquals(output[1].id, "2")
    }

    private fun createSubaccountFill(id: String): SubaccountFill {
        val createdAt = Instant.parse("2021-01-01T00:00:00Z")
        return SubaccountFill(
            id = id,
            side = OrderSide.Buy,
            liquidity = FillLiquidity.maker,
            type = OrderType.Limit,
            price = 1.0,
            size = 2.0,
            fee = 3.0,
            orderId = "2222",
            subaccountNumber = 0,
            marketId = "WETH-DAI",
            marginMode = MarginMode.Cross,
            createdAtMilliseconds = createdAt.toEpochMilliseconds().toDouble(),
            resources = SubaccountFillResources(
                sideString = "APP.GENERAL.BUY",
                liquidityString = "APP.TRADE.MAKER",
                typeString = "APP.TRADE.LIMIT_ORDER_SHORT",
                sideStringKey = "APP.GENERAL.BUY",
                liquidityStringKey = "APP.TRADE.MAKER",
                typeStringKey = "APP.TRADE.LIMIT_ORDER_SHORT",
                iconLocal = "Buy",
            ),
        )
    }
}
