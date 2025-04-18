package exchange.dydx.abacus.payload

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.abacus.state.machine.trade
import exchange.dydx.abacus.state.machine.tradeInMarket
import kotlin.test.Test
import kotlin.test.assertEquals

class TradeInputOptionsTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        testTradeInputOnce()
        testIsolatedInputOnce()
    }

    private fun testIsolatedInputOnce() {
        perp.trade("ISOLATED", TradeInputField.marginMode, 0)
        perp.trade("MARKET", TradeInputField.type, 0)
        var options = perp.internalState.input.trade.options
        assertEquals(options.needsSize, true)
        assertEquals(options.needsLeverage, false)
        assertEquals(options.needsBalancePercent, true)
        assertEquals(options.needsTriggerPrice, false)
        assertEquals(options.needsLimitPrice, false)
        assertEquals(options.needsTrailingPercent, false)
        assertEquals(options.needsReduceOnly, true)
        assertEquals(options.needsPostOnly, false)
        assertEquals(options.needsBrackets, true)
        assertEquals(options.needsGoodUntil, false)

        perp.trade("LIMIT", TradeInputField.type, 0)
        options = perp.internalState.input.trade.options
        assertEquals(options.needsSize, true)
        assertEquals(options.needsLeverage, false)
        assertEquals(options.needsBalancePercent, false)
        assertEquals(options.needsTriggerPrice, false)
        assertEquals(options.needsLimitPrice, true)
        assertEquals(options.needsTrailingPercent, false)
        assertEquals(options.needsReduceOnly, false)
        assertEquals(options.needsPostOnly, true)
        assertEquals(options.needsBrackets, false)
        assertEquals(options.needsGoodUntil, true)
    }

    private fun testTradeInputOnce() {
        perp.tradeInMarket("ETH-USD", 0)
        assertEquals(perp.internalState.input.currentType, InputType.TRADE)
        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        assertEquals(trade.side, OrderSide.Buy)
        assertEquals(trade.marketId, "ETH-USD")
        assertEquals(trade.timeInForce, "GTT")
        assertEquals(trade.options.needsPostOnly, true)
        assertEquals(trade.options.marginModeOptions?.size, 2)
        assertEquals(
            trade.options.marginModeOptions?.get(0),
            SelectionOption(
                type = "CROSS",
                stringKey = "APP.TRADE.CROSS_MARGIN",
                string = null,
                iconUrl = null,
            ),
        )
        assertEquals(
            trade.options.marginModeOptions?.get(1),
            SelectionOption(
                type = "ISOLATED",
                stringKey = "APP.TRADE.ISOLATED_MARGIN",
                string = null,
                iconUrl = null,
            ),
        )

        perp.trade(null, null, 0)
        assertEquals(perp.internalState.input.currentType, InputType.TRADE)
        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        assertEquals(trade.side, OrderSide.Buy)

        perp.trade("BUY", TradeInputField.side, 0)
        assertEquals(perp.internalState.input.currentType, InputType.TRADE)
        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        assertEquals(trade.side, OrderSide.Buy)

        perp.trade("MARKET", TradeInputField.type, 0)
        trade = perp.internalState.input.trade
        var options = trade.options
        assertEquals(options.needsSize, true)
        assertEquals(options.needsLeverage, true)
        assertEquals(options.needsBalancePercent, true)
        assertEquals(options.needsTriggerPrice, false)
        assertEquals(options.needsLimitPrice, false)
        assertEquals(options.needsTrailingPercent, false)
        assertEquals(options.needsReduceOnly, true)
        assertEquals(options.needsPostOnly, false)
        assertEquals(options.needsBrackets, true)
        assertEquals(options.needsGoodUntil, false)

        perp.trade("LIMIT", TradeInputField.type, 0)
        trade = perp.internalState.input.trade
        options = trade.options
        assertEquals(options.needsSize, true)
        assertEquals(options.needsLeverage, false)
        assertEquals(options.needsBalancePercent, false)
        assertEquals(options.needsTriggerPrice, false)
        assertEquals(options.needsLimitPrice, true)
        assertEquals(options.needsTrailingPercent, false)
        assertEquals(options.needsReduceOnly, false)
        assertEquals(options.needsPostOnly, true)
        assertEquals(options.needsBrackets, false)
        assertEquals(options.needsGoodUntil, true)

        perp.trade("GTT", TradeInputField.timeInForceType, 0)
        trade = perp.internalState.input.trade
        options = trade.options
        assertEquals(options.needsSize, true)
        assertEquals(options.needsLeverage, false)
        assertEquals(options.needsBalancePercent, false)
        assertEquals(options.needsTriggerPrice, false)
        assertEquals(options.needsLimitPrice, true)
        assertEquals(options.needsTrailingPercent, false)
        assertEquals(options.needsReduceOnly, false)
        assertEquals(options.needsPostOnly, true)
        assertEquals(options.needsBrackets, false)
        assertEquals(options.needsGoodUntil, true)

        perp.trade("GTT", TradeInputField.timeInForceType, 0)
        trade = perp.internalState.input.trade
        options = trade.options
        assertEquals(options.needsSize, true)
        assertEquals(options.needsLeverage, false)
        assertEquals(options.needsBalancePercent, false)
        assertEquals(options.needsTriggerPrice, false)
        assertEquals(options.needsLimitPrice, true)
        assertEquals(options.needsTrailingPercent, false)
        assertEquals(options.needsReduceOnly, false)
        assertEquals(options.needsPostOnly, true)
        assertEquals(options.needsBrackets, false)
        assertEquals(options.needsGoodUntil, true)
        assertEquals(options.timeInForceOptions?.size, 2)
        assertEquals(
            options.timeInForceOptions?.get(0),
            SelectionOption(
                type = "GTT",
                stringKey = "APP.TRADE.GOOD_TIL_TIME",
                string = null,
                iconUrl = null,
            ),
        )
        assertEquals(
            options.timeInForceOptions?.get(1),
            SelectionOption(
                type = "IOC",
                stringKey = "APP.TRADE.IMMEDIATE_OR_CANCEL",
                string = null,
                iconUrl = null,
            ),
        )
    }
}
