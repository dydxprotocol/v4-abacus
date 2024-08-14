package exchange.dydx.abacus.processor.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class OrderTypeProcessorTests {

    @Test
    fun testOrderTypeLiquidation() {
        val result = OrderTypeProcessor.orderType("LIQUIDATION", null)
        assertEquals("LIMIT", result)
    }

    fun testOrderTypeDeleveraged() {
        val result = OrderTypeProcessor.orderType("DELEVERAGED", null)
        assertEquals("LIQUIDATED", result)
    }

    fun testOrderTypeOffsetting() {
        val result = OrderTypeProcessor.orderType("OFFSETTING", null)
        assertEquals("DELEVERAGED", result)
    }

    @Test
    fun testOrderTypeLimitWithClientMetadata() {
        val result = OrderTypeProcessor.orderType("LIMIT", 1)
        assertEquals("MARKET", result)
    }

    @Test
    fun testOrderTypeStopLimitWithClientMetadata() {
        val result = OrderTypeProcessor.orderType("STOP_LIMIT", 1)
        assertEquals("STOP_MARKET", result)
    }

    @Test
    fun testOrderTypeTakeProfitWithClientMetadata() {
        val result = OrderTypeProcessor.orderType("TAKE_PROFIT", 1)
        assertEquals("TAKE_PROFIT_MARKET", result)
    }

    @Test
    fun testOrderTypeLimitWithoutClientMetadata() {
        val result = OrderTypeProcessor.orderType("LIMIT", null)
        assertEquals("LIMIT", result)
    }

    @Test
    fun testOrderTypeStopLimitWithoutClientMetadata() {
        val result = OrderTypeProcessor.orderType("STOP_LIMIT", null)
        assertEquals("STOP_LIMIT", result)
    }

    @Test
    fun testOrderTypeTakeProfitWithoutClientMetadata() {
        val result = OrderTypeProcessor.orderType("TAKE_PROFIT", null)
        assertEquals("TAKE_PROFIT", result)
    }
}
