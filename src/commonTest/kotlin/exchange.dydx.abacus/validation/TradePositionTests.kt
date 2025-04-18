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
import kotlin.test.assertTrue

class TradePositionTests : ValidationsTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTradeInputOnce()
    }

    private fun testTradeInputOnce() {
        reset()

        var time = ServerTime.now()
        testPositions()
        time = perp.log("Position Validation", time)
    }

    private fun testPositions() {
        test({
            perp.tradeInMarket("ETH-USD", 0)
        }, null)

        test({
            perp.trade("SELL", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("LIMIT", TradeInputField.type, 0)
        }, null)

        test({
            perp.trade("IOC", TradeInputField.timeInForceType, 0)
        }, null)

        test({
            perp.trade("true", TradeInputField.reduceOnly, 0)
        }, null)

        test({
            perp.trade("1050.0", TradeInputField.limitPrice, 0)
        }, null)

        /*
        This test would throw an Flip Position error when reduceOnly is supported
         */
        perp.trade("110.0", TradeInputField.size, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        assertEquals(trade.side, OrderSide.Sell)
        assertEquals(trade.marketId, "ETH-USD")
        assertEquals(trade.timeInForce, "IOC")
        var errors = perp.internalState.input.errors
        var error = errors?.get(0)
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "ORDER_WOULD_FLIP_POSITION")
        assertEquals(
            error?.resources?.title?.stringKey,
            "ERRORS.TRADE_BOX_TITLE.ORDER_WOULD_FLIP_POSITION",
        )

        test({
            perp.trade("BUY", TradeInputField.side, 0)
        }, null)

        test({
            perp.trade("999.0", TradeInputField.limitPrice, 0)
        }, null)

        perp.trade("210.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        assertEquals(trade.side, OrderSide.Buy)
        assertEquals(trade.marketId, "ETH-USD")
        assertEquals(trade.timeInForce, "IOC")
        errors = perp.internalState.input.errors
        error = errors?.get(0)
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "ORDER_WOULD_FLIP_POSITION")
        assertEquals(
            error?.resources?.title?.stringKey,
            "ERRORS.TRADE_BOX_TITLE.ORDER_WOULD_FLIP_POSITION",
        )

        perp.trade("SELL", TradeInputField.side, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        assertEquals(trade.side, OrderSide.Sell)
        assertEquals(trade.marketId, "ETH-USD")
        assertEquals(trade.timeInForce, "IOC")
        errors = perp.internalState.input.errors
        error = errors?.get(0)
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "ORDER_WOULD_FLIP_POSITION")
        assertEquals(
            error?.resources?.title?.stringKey,
            "ERRORS.TRADE_BOX_TITLE.ORDER_WOULD_FLIP_POSITION",
        )

        perp.trade("10", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        assertEquals(trade.side, OrderSide.Sell)
        assertEquals(trade.marketId, "ETH-USD")
        assertEquals(trade.timeInForce, "IOC")
        errors = perp.internalState.input.errors
        assertTrue { errors.isNullOrEmpty() }
    }
}
