package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.abacus.state.machine.triggerOrders
import exchange.dydx.abacus.tests.extensions.rest
import kollections.iListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class TriggerOrdersInputValidationTests : V4BaseTests() {
    override fun loadSubaccounts(): StateResponse {
        return perp.rest(
            AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
            mock.accountsChannel.v4_accounts_received_for_calculation,
            0,
            null,
        )
    }

    override fun reset() {
        super.reset()
        test({
            perp.triggerOrders("ETH-USD", TriggerOrdersInputField.marketId, 0)
        }, null)
    }

    @Test
    fun testTriggerOrderInputs() {
        setup()
        reset()

        test(
            {
                perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)
            },
            null,
        )

        perp.triggerOrders("0.00000001", TriggerOrdersInputField.size, 0)

        val input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        val triggerOrders = input.triggerOrders
        assertEquals(0.00000001, triggerOrders.size)

        val error = input.errors?.get(0)
        assertEquals(ErrorType.error, error?.type)
        assertEquals("ORDER_SIZE_BELOW_MIN_SIZE", error?.code)
        assertEquals(iListOf("size"), error?.fields)
    }

    @Test
    fun testTriggerOrderInputStopMarketType() {
        setup()
        reset()

        test({
            perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        perp.triggerOrders("900", TriggerOrdersInputField.stopLossPrice, 0)

        var input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        var triggerOrders = input.triggerOrders
        assertEquals(OrderType.StopMarket, triggerOrders.stopLossOrder?.type)

        var error = input.errors?.get(0)
        assertEquals(ErrorType.error, error?.type)
        assertEquals("SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE", error?.code)
        assertEquals(iListOf("stopLossOrder.price.triggerPrice"), error?.fields)
        assertEquals(
            "ERRORS.TRIGGERS_FORM_TITLE.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE",
            error?.resources?.title?.stringKey,
        )
        assertEquals(
            "ERRORS.TRIGGERS_FORM.SELL_TRIGGER_TOO_CLOSE_TO_LIQUIDATION_PRICE_NO_LIMIT",
            error?.resources?.text?.stringKey,
        )
        assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)

        perp.triggerOrders("2000", TriggerOrdersInputField.stopLossPrice, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrders = input.triggerOrders
        assertEquals(OrderType.StopMarket, triggerOrders.stopLossOrder?.type)

        error = input.errors?.get(0)
        assertEquals(ErrorType.error, error?.type)
        assertEquals("TRIGGER_MUST_BELOW_INDEX_PRICE", error?.code)
        assertEquals(iListOf("stopLossOrder.price.triggerPrice"), error?.fields)
        assertEquals(
            "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE",
            error?.resources?.title?.stringKey,
        )
        assertEquals(
            "ERRORS.TRIGGERS_FORM.STOP_LOSS_TRIGGER_MUST_BELOW_INDEX_PRICE",
            error?.resources?.text?.stringKey,
        )
        assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)

        test(
            {
                perp.triggerOrders("1", TriggerOrdersInputField.size, 0)
            },
            null,
        )

        perp.triggerOrders("4000", TriggerOrdersInputField.stopLossUsdcDiff, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrders = input.triggerOrders
        assertEquals(4000.0, triggerOrders.stopLossOrder?.price?.usdcDiff)
        assertEquals(-3000.0, triggerOrders.stopLossOrder?.price?.triggerPrice)

        error = input.errors?.get(0)
        assertEquals(ErrorType.error, error?.type)
        assertEquals("PRICE_MUST_POSITIVE", error?.code)
        assertEquals(iListOf("stopLossOrder.price.usdcDiff"), error?.fields)
        assertEquals(
            "ERRORS.TRIGGERS_FORM_TITLE.PRICE_MUST_POSITIVE",
            error?.resources?.title?.stringKey,
        )
        assertEquals("ERRORS.TRIGGERS_FORM.PRICE_MUST_POSITIVE", error?.resources?.text?.stringKey)
        assertEquals("APP.TRADE.MODIFY_PRICE", error?.resources?.action?.stringKey)
    }

    @Test
    fun testTriggerOrderInputStopLimitType() {
        setup()
        reset()

        test({
            perp.triggerOrders("STOP_LIMIT", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        perp.triggerOrders("2000", TriggerOrdersInputField.stopLossLimitPrice, 0)

        var input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        var triggerOrders = input.triggerOrders
        assertEquals(OrderType.StopLimit, triggerOrders.stopLossOrder?.type)
        assertEquals(2000.0, triggerOrders.stopLossOrder?.price?.limitPrice)
        assertEquals(OrderSide.Sell, triggerOrders.stopLossOrder?.side)

        var error = input.errors?.get(0)
        assertEquals(ErrorType.required, error?.type)
        assertEquals("REQUIRED_TRIGGER_PRICE", error?.code)

        test({
            perp.triggerOrders("800", TriggerOrdersInputField.stopLossLimitPrice, 0)
        }, null)

        perp.triggerOrders("900", TriggerOrdersInputField.stopLossPrice, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrders = input.triggerOrders
        assertEquals(OrderType.StopLimit, triggerOrders.stopLossOrder?.type)
        assertEquals(900.0, triggerOrders.stopLossOrder?.price?.triggerPrice)
        assertEquals(800.0, triggerOrders.stopLossOrder?.price?.limitPrice)
        assertEquals(OrderSide.Sell, triggerOrders.stopLossOrder?.side)

        error = input.errors?.firstOrNull()
        assertEquals(null, error)

        test(
            {
                perp.triggerOrders("1000", TriggerOrdersInputField.stopLossPrice, 0)
            },
            null,
        )

        perp.triggerOrders("2000", TriggerOrdersInputField.stopLossLimitPrice, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrders = input.triggerOrders
        assertEquals(OrderType.StopLimit, triggerOrders.stopLossOrder?.type)
        assertEquals(2000.0, triggerOrders.stopLossOrder?.price?.limitPrice)
        assertEquals(1000.0, triggerOrders.stopLossOrder?.price?.triggerPrice)
        assertEquals(OrderSide.Sell, triggerOrders.stopLossOrder?.side)

        error = input.errors?.get(0)
        assertEquals(ErrorType.error, error?.type)
        assertEquals("LIMIT_MUST_BELOW_TRIGGER_PRICE", error?.code)
        assertEquals(iListOf("stopLossOrder.price.limitPrice"), error?.fields)
        assertEquals(
            "ERRORS.TRIGGERS_FORM_TITLE.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE",
            error?.resources?.title?.stringKey,
        )
        assertEquals(
            "ERRORS.TRIGGERS_FORM.STOP_LOSS_LIMIT_MUST_BELOW_TRIGGER_PRICE",
            error?.resources?.text?.stringKey,
        )
        assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)
    }

    @Test
    fun testTriggerOrderInputTakeProfitMarketType() {
        setup()
        reset()

        test({
            perp.triggerOrders("TAKE_PROFIT_MARKET", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        perp.triggerOrders("1000", TriggerOrdersInputField.takeProfitPrice, 0)

        val input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        val triggerOrders = input.triggerOrders
        assertEquals(OrderType.TakeProfitMarket, triggerOrders.takeProfitOrder?.type)

        val error = input.errors?.get(0)
        assertEquals(ErrorType.error, error?.type)
        assertEquals("TRIGGER_MUST_ABOVE_INDEX_PRICE", error?.code)
        assertEquals(iListOf("takeProfitOrder.price.triggerPrice"), error?.fields)
        assertEquals(
            "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE",
            error?.resources?.title?.stringKey,
        )
        assertEquals(
            "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_TRIGGER_MUST_ABOVE_INDEX_PRICE",
            error?.resources?.text?.stringKey,
        )
        assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)
    }

    @Test
    fun testTriggerOrderInputTakeProfitLimitType() {
        setup()
        reset()

        test({
            perp.triggerOrders("TAKE_PROFIT", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        perp.triggerOrders("3000", TriggerOrdersInputField.takeProfitLimitPrice, 0)

        var input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        var triggerOrders = input.triggerOrders
        assertEquals(OrderType.TakeProfitLimit, triggerOrders.takeProfitOrder?.type)
        assertEquals(3000.0, triggerOrders.takeProfitOrder?.price?.limitPrice)
        assertEquals(OrderSide.Sell, triggerOrders.takeProfitOrder?.side)

        var error = input.errors?.get(0)
        assertEquals(ErrorType.required, error?.type)
        assertEquals("REQUIRED_TRIGGER_PRICE", error?.code)

        test(
            {
                perp.triggerOrders("2000", TriggerOrdersInputField.takeProfitPrice, 0)
            },
            null,
        )

        perp.triggerOrders("3000", TriggerOrdersInputField.takeProfitLimitPrice, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrders = input.triggerOrders
        assertEquals(OrderType.TakeProfitLimit, triggerOrders.takeProfitOrder?.type)
        assertEquals(3000.0, triggerOrders.takeProfitOrder?.price?.limitPrice)
        assertEquals(2000.0, triggerOrders.takeProfitOrder?.price?.triggerPrice)
        assertEquals(OrderSide.Sell, triggerOrders.takeProfitOrder?.side)

        error = input.errors?.get(0)
        assertEquals(ErrorType.error, error?.type)
        assertEquals("LIMIT_MUST_BELOW_TRIGGER_PRICE", error?.code)
        assertEquals(iListOf("takeProfitOrder.price.limitPrice"), error?.fields)
        assertEquals(
            "ERRORS.TRIGGERS_FORM_TITLE.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE",
            error?.resources?.title?.stringKey,
        )
        assertEquals(
            "ERRORS.TRIGGERS_FORM.TAKE_PROFIT_LIMIT_MUST_BELOW_TRIGGER_PRICE",
            error?.resources?.text?.stringKey,
        )
        assertEquals("APP.TRADE.MODIFY_TRIGGER_PRICE", error?.resources?.action?.stringKey)
    }
}
