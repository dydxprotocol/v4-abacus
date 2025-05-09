package exchange.dydx.abacus.payload.v4

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.helper.AbUrl
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.abacus.state.machine.triggerOrders
import exchange.dydx.abacus.tests.extensions.rest
import exchange.dydx.abacus.utils.Rounder
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TriggerOrderInputTests : V4BaseTests() {
    override fun loadSubaccounts(): StateResponse {
        return perp.rest(
            AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
            mock.accountsChannel.v4_accounts_received_for_calculation,
            0,
            null,
        )
    }

    @Test
    fun testInputs() {
        setup()

        // Tests on ETH-USD market with
        // - notional total: 1753.2932
        // - size: 0.5
        // - leverage: 2.054737105604498

        val leverage = 2.054737105604498;

        testDefaults()
        testSetPositionSize()
        testStopLossInput(leverage)
        testTakeProfitInput(leverage)
    }

    @Test
    fun testLessThanOneLeverageInputs() {
        setup()

        // Tests on ETH-USD market with
        // - notional total: 1753.2932
        // - size: 0.5
        // - leverage < 1 (defaults to 1 in calculations)

        test(
            {
                perp.rest(
                    AbUrl.fromString("$testRestUrl/v4/addresses/cosmo"),
                    mock.accountsChannel.v4_accounts_received_for_calculation_2,
                    0,
                    null,
                )
            },
            null,
        )

        val leverage = 1.0;

        testDefaults()
        testSetPositionSize()
        testStopLossInput(leverage)
        testTakeProfitInput(leverage)
    }

    private fun roundValue(value: Double): Double {
        return Rounder.round(value, 0.000001)
    }

    private fun testDefaults() {
        perp.triggerOrders("ETH-USD", TriggerOrdersInputField.marketId, 0)

        var input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)

        var triggerOrders = input.triggerOrders
        assertEquals("ETH-USD", triggerOrders.marketId)

        perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)

        triggerOrders = input.triggerOrders
        assertEquals(OrderType.StopMarket, triggerOrders.stopLossOrder?.type)
        assertEquals(OrderSide.Sell, triggerOrders.stopLossOrder?.side)
    }

    private fun testSetPositionSize() {
        perp.triggerOrders("2.0", TriggerOrdersInputField.stopLossOrderSize, 0)

        var input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        var triggerOrder = input.triggerOrders
        assertEquals(2.0, triggerOrder.stopLossOrder?.size)
        assertEquals(2.0, triggerOrder.stopLossOrder?.summary?.size)

        perp.triggerOrders("0.5", TriggerOrdersInputField.size, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrder = input.triggerOrders
        assertEquals(0.5, triggerOrder.size)
        assertEquals(2.0, triggerOrder.stopLossOrder?.size)
        assertEquals(0.5, triggerOrder.stopLossOrder?.summary?.size)

        perp.triggerOrders("1.0", TriggerOrdersInputField.takeProfitOrderSize, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrder = input.triggerOrders
        assertEquals(0.5, triggerOrder.size)
        assertEquals(2.0, triggerOrder.stopLossOrder?.size)
        assertEquals(0.5, triggerOrder.stopLossOrder?.summary?.size)
        assertEquals(1.0, triggerOrder.takeProfitOrder?.size)
        assertEquals(0.5, triggerOrder.takeProfitOrder?.summary?.size)
    }

    private fun testStopLossInput(leverageMultiplier: Double) {
        test({
            perp.triggerOrders("STOP_MARKET", TriggerOrdersInputField.stopLossOrderType, 0)
        }, null)

        perp.triggerOrders("1000.0", TriggerOrdersInputField.stopLossPrice, 0)

        var input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        var triggerOrder = input.triggerOrders
        assertEquals(OrderSide.Sell, triggerOrder.stopLossOrder?.side)
        assertEquals(OrderType.StopMarket, triggerOrder.stopLossOrder?.type)

        var stopLoss = triggerOrder.stopLossOrder
        assertEquals(1000.0, stopLoss?.price?.triggerPrice)
        assertEquals(0.0, stopLoss?.price?.usdcDiff)
        assertEquals(0.0, stopLoss?.price?.percentDiff)
        assertEquals("stopLossOrder.price.triggerPrice", stopLoss?.price?.input)
        assertEquals(950.0, stopLoss?.summary?.price)

        test({
            perp.triggerOrders("300.0", TriggerOrdersInputField.stopLossLimitPrice, 0)
        }, null)

        test({
            perp.triggerOrders("1234", TriggerOrdersInputField.stopLossOrderId, 0)
        }, null)

        perp.triggerOrders("400.0", TriggerOrdersInputField.stopLossPrice, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrder = input.triggerOrders
        stopLoss = triggerOrder.stopLossOrder
        assertEquals(OrderType.StopLimit, stopLoss?.type)
        assertEquals(OrderSide.Sell, stopLoss?.side)
        assertEquals(400.0, stopLoss?.price?.triggerPrice)
        assertEquals(300.0, stopLoss?.price?.limitPrice)
        assertEquals(300.0, stopLoss?.price?.usdcDiff)
        assertTrue {
            abs(34.221316 * leverageMultiplier - stopLoss?.price?.percentDiff!!) < 0.0001
        }
        assertEquals("stopLossOrder.price.triggerPrice", stopLoss?.price?.input)
        assertEquals(300.0, stopLoss?.summary?.price)

        perp.triggerOrders("400", TriggerOrdersInputField.stopLossUsdcDiff, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrder = input.triggerOrders
        stopLoss = triggerOrder.stopLossOrder
        assertEquals(OrderType.StopLimit, stopLoss?.type)
        assertEquals(OrderSide.Sell, stopLoss?.side)
        assertEquals(400.0, stopLoss?.price?.usdcDiff)
        assertEquals(300.0, stopLoss?.price?.limitPrice)
        assertEquals(200.0, stopLoss?.price?.triggerPrice)
        assertEquals(
            roundValue(45.62842084826428 * leverageMultiplier),
            roundValue(stopLoss?.price?.percentDiff!!),
        )
        assertEquals("stopLossOrder.price.usdcDiff", stopLoss?.price?.input)
        assertEquals(300.0, stopLoss?.summary?.price)

        perp.triggerOrders("25.00", TriggerOrdersInputField.stopLossPercentDiff, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrder = input.triggerOrders
        stopLoss = triggerOrder.stopLossOrder
        assertEquals(OrderType.StopLimit, stopLoss?.type)
        assertEquals(OrderSide.Sell, stopLoss?.side)
        assertEquals(
            roundValue(219.16165 / leverageMultiplier),
            roundValue(stopLoss?.price?.usdcDiff!!),
        )
        assertEquals(300.0, stopLoss?.price?.limitPrice)
        assertEquals(
            roundValue(1000.0 - 438.3233 / leverageMultiplier),
            roundValue(stopLoss?.price?.triggerPrice!!),
        )
        assertEquals(25.0, stopLoss?.price?.percentDiff)
        assertEquals("stopLossOrder.price.percentDiff", stopLoss?.price?.input)
        assertEquals(300.0, stopLoss?.summary?.price)
    }

    private fun testTakeProfitInput(leverageMultiplier: Double) {
        test({
            perp.triggerOrders("TAKE_PROFIT_MARKET", TriggerOrdersInputField.takeProfitOrderType, 0)
        }, null)

        perp.triggerOrders("1000.0", TriggerOrdersInputField.takeProfitPrice, 0)

        var input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        var triggerOrder = input.triggerOrders
        var takeProfit = triggerOrder.takeProfitOrder
        assertEquals(OrderType.TakeProfitMarket, takeProfit?.type)
        assertEquals(OrderSide.Sell, takeProfit?.side)
        assertEquals(1000.0, takeProfit?.price?.triggerPrice)
        assertEquals(0.0, takeProfit?.price?.usdcDiff)
        assertEquals(0.0, takeProfit?.price?.percentDiff)
        assertEquals("takeProfitOrder.price.triggerPrice", takeProfit?.price?.input)
        assertEquals(950.0, takeProfit?.summary?.price)

        test({
            perp.triggerOrders("1600.0", TriggerOrdersInputField.takeProfitLimitPrice, 0)
        }, null)

        test({
            perp.triggerOrders("4321", TriggerOrdersInputField.takeProfitOrderId, 0)
        }, null)

        test({
            perp.triggerOrders("4321", TriggerOrdersInputField.takeProfitOrderId, 0)
        }, null)

        perp.triggerOrders("1800.0", TriggerOrdersInputField.takeProfitPrice, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrder = input.triggerOrders
        takeProfit = triggerOrder.takeProfitOrder
        assertEquals(OrderType.TakeProfitLimit, takeProfit?.type)
        assertEquals(OrderSide.Sell, takeProfit?.side)
        assertEquals(1800.0, takeProfit?.price?.triggerPrice)
        assertEquals(1600.0, takeProfit?.price?.limitPrice)
        assertEquals(400.0, takeProfit?.price?.usdcDiff)
        assertTrue {
            abs(45.62842 * leverageMultiplier - takeProfit?.price?.percentDiff!!) < 0.0001
        }
        assertEquals("takeProfitOrder.price.triggerPrice", takeProfit?.price?.input)
        assertEquals(1600.0, takeProfit?.summary?.price)

        perp.triggerOrders("300.0", TriggerOrdersInputField.takeProfitUsdcDiff, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrder = input.triggerOrders
        takeProfit = triggerOrder.takeProfitOrder
        assertEquals(OrderType.TakeProfitLimit, takeProfit?.type)
        assertEquals(OrderSide.Sell, takeProfit?.side)
        assertEquals(300.0, takeProfit?.price?.usdcDiff)
        assertEquals(1600.0, takeProfit?.price?.limitPrice)
        assertEquals(1600.0, takeProfit?.price?.triggerPrice)
        assertEquals(
            roundValue(34.22131563619821 * leverageMultiplier),
            roundValue(takeProfit?.price?.percentDiff!!),
        )
        assertEquals("takeProfitOrder.price.usdcDiff", takeProfit?.price?.input)
        assertEquals(1600.0, takeProfit?.summary?.price)

        perp.triggerOrders("25.0", TriggerOrdersInputField.takeProfitPercentDiff, 0)

        input = perp.internalState.input
        assertEquals(InputType.TRIGGER_ORDERS, input.currentType)
        triggerOrder = input.triggerOrders
        takeProfit = triggerOrder.takeProfitOrder
        assertEquals(OrderType.TakeProfitLimit, takeProfit?.type)
        assertEquals(OrderSide.Sell, takeProfit?.side)
        assertEquals(
            roundValue(219.16165 / leverageMultiplier),
            roundValue(takeProfit?.price?.usdcDiff!!),
        )
        assertEquals(1600.0, takeProfit?.price?.limitPrice)
        assertEquals(
            roundValue(1000.0 + 438.3233 / leverageMultiplier),
            roundValue(takeProfit?.price?.triggerPrice!!),
        )
        assertEquals(25.0, takeProfit?.price?.percentDiff)
        assertEquals("takeProfitOrder.price.percentDiff", takeProfit?.price?.input)
        assertEquals(1600.0, takeProfit?.summary?.price)
    }
}
