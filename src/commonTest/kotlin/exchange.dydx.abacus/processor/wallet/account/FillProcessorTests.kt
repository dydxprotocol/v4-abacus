package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.FillLiquidity
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountFillResources
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.tests.mock.LocalizerProtocolMock
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerFillResponseObject
import indexer.codegen.IndexerFillType
import indexer.codegen.IndexerLiquidity
import indexer.codegen.IndexerMarketType
import indexer.codegen.IndexerOrderSide
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class FillProcessorTests {

    companion object {
        private val createdAt = Instant.parse("2021-01-01T00:00:00Z")

        val payloadMock = IndexerFillResponseObject(
            id = "1",
            side = IndexerOrderSide.BUY,
            liquidity = IndexerLiquidity.MAKER,
            type = IndexerFillType.LIMIT,
            market = "WETH-DAI",
            marketType = IndexerMarketType.PERPETUAL,
            price = "1.0",
            size = "2.0",
            fee = "3.0",
            createdAt = createdAt.toString(),
            createdAtHeight = "111",
            orderId = "2222",
            clientMetadata = "0",
            subaccountNumber = 0,
        )

        val fillMock = SubaccountFill(
            id = "1",
            side = OrderSide.Buy,
            liquidity = FillLiquidity.maker,
            type = OrderType.Limit,
            price = 1.0,
            size = 2.0,
            fee = 3.0,
            orderId = "2222",
            subaccountNumber = 0,
            marketId = "WETH-DAI",
            displayId = "WETH-DAI",
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

    private val fillProcessor = FillProcessor(
        parser = Parser(),
        localizer = LocalizerProtocolMock(),
    )

    @Test
    fun testProcess() {
        val fill = fillProcessor.process(
            payload = payloadMock,
            subaccountNumber = 0,
        )

        assertEquals(
            expected = fillMock,
            actual = fill,
        )
    }
}
