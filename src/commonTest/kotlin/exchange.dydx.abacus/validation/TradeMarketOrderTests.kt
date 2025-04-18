package exchange.dydx.abacus.validation

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.abacus.state.machine.trade
import exchange.dydx.abacus.state.machine.tradeInMarket
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TradeMarketOrderTests : ValidationsTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTradeInputOnce()
    }

    private fun testTradeInputOnce() {
        reset()

        var time = ServerTime.now()
        testMarketOrderbookSlippageAndLiquidity()
        time = perp.log("Market Order Validation", time)
    }

    private fun testMarketOrderbookSlippageAndLiquidity() {
        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

        test({
            perp.trade("MARKET", TradeInputField.type, 0)
        }, null)

        perp.trade("BUY", TradeInputField.side, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.side, OrderSide.Buy)
        assertEquals(trade.type, OrderType.Market)
        assertEquals(trade.marketId, "ETH-USD")
        val errors = perp.internalState.input.errors
        val error = errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_SIZE")
        assertEquals(error?.fields?.firstOrNull(), "size.size")

        perp.trade("1.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.timeInForce, "GTT")
        var summary = trade.summary!!
        assertEquals(summary.price, 1024.26)
        assertEquals(summary.size, 1.0)
        assertEquals(summary.usdcSize, 1024.26)
        assertEquals(summary.total, -1024.26)
        assertEquals(summary.slippage, 0.06)
        assertEquals(summary.indexSlippage, 0.06)
        assertEquals(summary.filled, true)

        var orderbook = trade.marketOrder!!.orderbook!!
        assertEquals(orderbook.size, 3)
        assertEquals(orderbook[0].size, 0.1)
        assertEquals(orderbook[0].price, 1000.1)
        assertEquals(orderbook[1].size, 0.5)
        assertEquals(orderbook[1].price, 1000.5)
        assertEquals(orderbook[2].size, 0.4)
        assertEquals(orderbook[2].price, 1060.0)

        assertEquals(trackingProtocol.lastEvent, "TradeValidation")
        assertEquals(
            trackingProtocol.lastData,
            """
                    {
                        "errors": [
                            "MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE"
                        ],
                        "marketId": "ETH-USD",
                        "size": 1.0,
                        "notionalSize": 1024.26,
                        "indexSlippage": 0.06,
                        "orderbookSlippage": 0.06
                    }
            """.trimIndent(),
        )

        assertEquals(trackingProtocol.lastEvent, "TradeValidation")
        assertEquals(
            trackingProtocol.lastData,
            """
                    {
                        "errors": [
                            "MARKET_ORDER_WARNING_ORDERBOOK_SLIPPAGE"
                        ],
                        "marketId": "ETH-USD",
                        "size": 1.0,
                        "notionalSize": 1024.26,
                        "indexSlippage": 0.06,
                        "orderbookSlippage": 0.06
                    }
            """.trimIndent(),
        )

        perp.trade("10.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.timeInForce, "GTT")
        summary = trade.summary!!
        assertEquals(summary.price, 1382.0260000000003)
        assertEquals(summary.size, 10.0)
        assertEquals(summary.usdcSize, 13820.260000000002)
        assertEquals(summary.total, -13820.260000000002)
        assertEquals(summary.slippage, 0.8)
        assertEquals(summary.indexSlippage, 0.8)
        assertEquals(summary.filled, true)

        assertEquals(trade.size?.size, 10.0)
        assertEquals(trade.size?.usdcSize, 13820.260000000002)

        orderbook = trade.marketOrder!!.orderbook!!
        assertEquals(orderbook.size, 4)
        assertEquals(orderbook[0].size, 0.1)
        assertEquals(orderbook[0].price, 1000.1)
        assertEquals(orderbook[1].size, 0.5)
        assertEquals(orderbook[1].price, 1000.5)
        assertEquals(orderbook[2].size, 5.0)
        assertEquals(orderbook[2].price, 1060.0)
        assertEquals(orderbook[3].size, 4.4)
        assertEquals(orderbook[3].price, 1800.0)
    }
}
