package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.output.MarketTrade
import exchange.dydx.abacus.output.MarketTradeResources
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.utils.DummyLocalizer
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerOrderSide
import indexer.codegen.IndexerTradeResponseObject
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TradesProcessorV2Tests {

    private val processor = TradesProcessorV2(
        tradeProcessor = TradeProcessorV2(Parser(), DummyLocalizer()),
        limit = 5,
    )

    @Test fun testSubscribed_happyPath() {
        val payload = TradesResponse(
            listOf(
                indexerTrade("1"),
                indexerTrade("2"),
                indexerTrade("3"),
            ),
        )

        val trades = processor.processSubscribed(payload)

        assertContentEquals(
            listOf(
                expectedMarketTrade("1"),
                expectedMarketTrade("2"),
                expectedMarketTrade("3"),
            ),
            trades,
        )
    }

    @Test fun testSubscribed_invalidTrades() {
        val payload = TradesResponse(
            listOf(
                indexerTrade("1"),
                IndexerTradeResponseObject(
                    id = "2",
                    side = null,
                    size = null,
                    price = null,
                    type = null,
                    createdAt = null,
                    createdAtHeight = null,
                ),
            ),
        )

        val trades = processor.processSubscribed(payload)

        assertContentEquals(
            listOf(
                expectedMarketTrade("1"),
            ),
            trades,
        )
    }

    @Test fun testChannelData_happyPath() {
        val subscribedPayload = TradesResponse(
            listOf(
                indexerTrade("1"),
                indexerTrade("2"),
            ),
        )
        val initialTrades = processor.processSubscribed(subscribedPayload)
        val channelPayload = TradesResponse(
            listOf(
                indexerTrade("3"),
                indexerTrade("4"),
            ),
        )

        val finalTrades = processor.processChannelData(initialTrades, channelPayload)

        assertContentEquals(
            listOf(
                expectedMarketTrade("3"),
                expectedMarketTrade("4"),
                expectedMarketTrade("1"),
                expectedMarketTrade("2"),
            ),
            finalTrades,
        )
    }

    @Test fun testChannelData_overTradesLimit() {
        val subscribedPayload = TradesResponse(
            listOf(
                indexerTrade("1"),
                indexerTrade("2"),
            ),
        )
        val initialTrades = processor.processSubscribed(subscribedPayload)
        val channelPayload = TradesResponse(
            listOf(
                indexerTrade("3"),
                indexerTrade("4"),
                indexerTrade("5"),
                indexerTrade("6"),
            ),
        )

        val finalTrades = processor.processChannelData(initialTrades, channelPayload)

        assertContentEquals(
            listOf(
                expectedMarketTrade("3"),
                expectedMarketTrade("4"),
                expectedMarketTrade("5"),
                expectedMarketTrade("6"),
                expectedMarketTrade("1"),
            ),
            finalTrades,
        )
    }

    @Test fun testChannelData_duplicateIds() {
        val subscribedPayload = TradesResponse(
            listOf(
                indexerTrade("1"),
                indexerTrade("2"),
            ),
        )
        val initialTrades = processor.processSubscribed(subscribedPayload)
        val channelPayload = TradesResponse(
            listOf(
                indexerTrade("1").copy(size = "500.0"),
                indexerTrade("2").copy(size = "500.0"),
            ),
        )

        val finalTrades = processor.processChannelData(initialTrades, channelPayload)

        assertContentEquals(
            listOf(
                expectedMarketTrade("1").copy(size = 500.0),
                expectedMarketTrade("2").copy(size = 500.0),
            ),
            finalTrades,
        )
    }

    private fun indexerTrade(id: String) =
        IndexerTradeResponseObject(
            id = id,
            side = IndexerOrderSide.BUY,
            size = "10",
            price = "100.0",
            type = null,
            createdAt = "2023-04-04T00:29:19.353Z",
            createdAtHeight = null,
        )

    // Expected Market Trade after processing an IndexerTradeResponseObject created by indexerTrade()
    private fun expectedMarketTrade(id: String) =
        MarketTrade(
            id = id,
            side = OrderSide.Buy,
            size = 10.0,
            price = 100.0,
            type = null,
            createdAtMilliseconds = 1.680568159353E12,
            resources = MarketTradeResources(
                sideString = "APP.GENERAL.BUY",
                sideStringKey = "APP.GENERAL.BUY",
            ),
        )
}
