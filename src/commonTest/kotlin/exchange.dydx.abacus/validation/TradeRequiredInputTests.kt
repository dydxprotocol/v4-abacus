package exchange.dydx.abacus.validation

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.payload.v4.V4BaseTests
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.abacus.state.machine.trade
import exchange.dydx.abacus.tests.extensions.log
import exchange.dydx.abacus.utils.ServerTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TradeRequiredInputTests : V4BaseTests() {
    @Test
    fun testDataFeed() {
        setup()

        print("--------First round----------\n")

        testTradeInputOnce()
    }

    override fun setup() {
        super.setup()

        perp.internalState.wallet.walletAddress = "0x1234567890"
    }

    private fun testTradeInputOnce() {
        var time = ServerTime.now()
        reset()
        testTradeInputMarketType()
        time = perp.log("Market Order", time)

        reset()
        testTradeInputStopMarketType()
        time = perp.log("Stop Market Order", time)

        reset()
        testTradeInputTakeProfitMarketType()
        time = perp.log("Take Profit Market Order", time)

        reset()
        testTradeInputLimitType()
        time = perp.log("Limit Order", time)

        reset()
        testTradeInputStopLimitType()
        time = perp.log("Stop Limit Order", time)

        reset()
        testTradeInputTakeProfitLimitType()
        time = perp.log("Take Profit Limit Order", time)

        reset()
        testTradeInputTrailingStopType()
        time = perp.log("Trailing Stop Order", time)
    }

    override fun reset() {
        super.reset()
        perp.trade(null, null, 0)
    }

    private fun testTradeInputMarketType() {
        perp.trade("MARKET", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Market)
        var error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_SIZE")
        assertEquals(error?.fields?.firstOrNull(), "size.size")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")

        perp.trade("1.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Market)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "MARKET_ORDER_NOT_ENOUGH_LIQUIDITY")
    }

    private fun testTradeInputStopMarketType() {
        perp.trade("STOP_MARKET", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopMarket)
        var error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_SIZE")
        assertEquals(error?.fields?.firstOrNull(), "size.size")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
        val error2 = perp.internalState.input.errors?.get(1)
        assertEquals(error2?.type, ErrorType.required)
        assertEquals(error2?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error2?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopMarket)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1223", TradeInputField.triggerPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopMarket)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "TRIGGER_MUST_ABOVE_INDEX_PRICE")

        perp.trade("3000", TradeInputField.triggerPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopMarket)
        val errors = perp.internalState.input.errors
        assertTrue { errors.isNullOrEmpty() }
    }

    private fun testTradeInputTakeProfitMarketType() {
        perp.trade("TAKE_PROFIT_MARKET", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitMarket)
        var errors = perp.internalState.input.errors
        assertEquals(errors?.size, 2)
        var error = errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_SIZE")
        assertEquals(error?.fields?.firstOrNull(), "size.size")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
        val error2 = errors?.get(1)
        assertEquals(error2?.type, ErrorType.required)
        assertEquals(error2?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error2?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitMarket)
        errors = perp.internalState.input.errors
        assertEquals(errors?.size, 1)
        error = errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1923", TradeInputField.triggerPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitMarket)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "TRIGGER_MUST_BELOW_INDEX_PRICE")
    }

    private fun testTradeInputLimitType() {
        perp.trade("LIMIT", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        val errors = perp.internalState.input.errors
        assertEquals(errors?.size, 2)
        var error = errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_SIZE")
        assertEquals(error?.fields?.firstOrNull(), "size.size")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
        val error2 = errors?.get(1)
        assertEquals(error2?.type, ErrorType.required)
        assertEquals(error2?.code, "REQUIRED_LIMIT_PRICE")
        assertEquals(error2?.fields?.firstOrNull(), "price.limitPrice")
        assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")

        perp.trade("1.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_LIMIT_PRICE")
        assertEquals(error?.fields?.firstOrNull(), "price.limitPrice")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")

        perp.trade("1223", TradeInputField.limitPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.Limit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error, null)
    }

    private fun testTradeInputStopLimitType() {
        perp.trade("STOP_LIMIT", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopLimit)
        var errors = perp.internalState.input.errors
        assertEquals(errors?.size, 3)
        var error = errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_SIZE")
        assertEquals(error?.fields?.firstOrNull(), "size.size")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
        var error2 = errors?.get(1)
        assertEquals(error2?.type, ErrorType.required)
        assertEquals(error2?.code, "REQUIRED_LIMIT_PRICE")
        assertEquals(error2?.fields?.firstOrNull(), "price.limitPrice")
        assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
        var error3 = errors?.get(2)
        assertEquals(error3?.type, ErrorType.required)
        assertEquals(error3?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error3?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error3?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopLimit)
        errors = perp.internalState.input.errors
        assertEquals(errors?.size, 2)
        error = errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_LIMIT_PRICE")
        assertEquals(error?.fields?.firstOrNull(), "price.limitPrice")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
        error2 = errors?.get(1)
        assertEquals(error2?.type, ErrorType.required)
        assertEquals(error2?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error2?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1123", TradeInputField.limitPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopLimit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1800", TradeInputField.triggerPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopLimit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error, null)

        perp.trade("IOC", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopLimit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "LIMIT_MUST_ABOVE_TRIGGER_PRICE")

        perp.trade("1323", TradeInputField.triggerPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopLimit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "TRIGGER_MUST_ABOVE_INDEX_PRICE")

        perp.trade("1100", TradeInputField.triggerPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.StopLimit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "TRIGGER_MUST_ABOVE_INDEX_PRICE")
    }

    private fun testTradeInputTakeProfitLimitType() {
        perp.trade("TAKE_PROFIT", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitLimit)
        var errors = perp.internalState.input.errors
        assertEquals(errors?.size, 3)
        var error = errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_SIZE")
        assertEquals(error?.fields?.firstOrNull(), "size.size")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
        var error2 = errors?.get(1)
        assertEquals(error2?.type, ErrorType.required)
        assertEquals(error2?.code, "REQUIRED_LIMIT_PRICE")
        assertEquals(error2?.fields?.firstOrNull(), "price.limitPrice")
        assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
        var error3 = errors?.get(2)
        assertEquals(error3?.type, ErrorType.required)
        assertEquals(error3?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error3?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error3?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitLimit)
        errors = perp.internalState.input.errors
        assertEquals(errors?.size, 2)
        error = errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_LIMIT_PRICE")
        assertEquals(error?.fields?.firstOrNull(), "price.limitPrice")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_LIMIT_PRICE")
        error2 = errors?.get(1)
        assertEquals(error2?.type, ErrorType.required)
        assertEquals(error2?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error2?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1123", TradeInputField.limitPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitLimit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_TRIGGER_PRICE")
        assertEquals(error?.fields?.firstOrNull(), "price.triggerPrice")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRIGGER_PRICE")

        perp.trade("1923", TradeInputField.triggerPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitLimit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "TRIGGER_MUST_BELOW_INDEX_PRICE")

        perp.trade("1290", TradeInputField.triggerPrice, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitLimit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error, null)

        perp.trade("IOC", TradeInputField.execution, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TakeProfitLimit)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.error)
        assertEquals(error?.code, "LIMIT_MUST_ABOVE_TRIGGER_PRICE")
    }

    private fun testTradeInputTrailingStopType() {
        perp.trade("TRAILING_STOP", TradeInputField.type, 0)

        var trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TrailingStop)
        val errors = perp.internalState.input.errors
        assertEquals(errors?.size, 2)
        var error = errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_SIZE")
        assertEquals(error?.fields?.firstOrNull(), "size.size")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_AMOUNT")
        var error2 = errors?.get(1)
        assertEquals(error2?.type, ErrorType.required)
        assertEquals(error2?.code, "REQUIRED_TRAILING_PERCENT")
        assertEquals(error2?.fields?.firstOrNull(), "price.trailingPercent")
        assertEquals(error2?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRAILING_PERCENT")

        perp.trade("1.0", TradeInputField.size, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TrailingStop)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error?.type, ErrorType.required)
        assertEquals(error?.code, "REQUIRED_TRAILING_PERCENT")
        assertEquals(error?.fields?.firstOrNull(), "price.trailingPercent")
        assertEquals(error?.resources?.action?.stringKey, "APP.TRADE.ENTER_TRAILING_PERCENT")

        perp.trade("0.05", TradeInputField.trailingPercent, 0)

        trade = perp.internalState.input.trade
        assertEquals(trade.type, OrderType.TrailingStop)
        error = perp.internalState.input.errors?.firstOrNull()
        assertEquals(error, null)
    }
}
