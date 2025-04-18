package exchange.dydx.abacus.app

import exchange.dydx.abacus.state.helper.Formatter
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatterTests {
    val formatter = Formatter(nativeFormatter = null)

    @Test
    fun testPercent() {
        assertEquals(formatter.percent(1.0, 2), "100.00%")
        assertEquals(formatter.percent(1.00001, 2), "100.00%")
        assertEquals(formatter.percent(1.00005, 2), "100.01%")
    }

    @Test
    fun testPrice() {
        assertEquals(formatter.price(100.0, "0.01"), "$100.00")
        assertEquals(formatter.price(100.122, "0.01"), "$100.12")
        assertEquals(formatter.price(100.125, "0.01"), "$100.13")
        assertEquals(formatter.price(109.125, "1"), "$109")
        assertEquals(formatter.price(109.125, "10"), "$110")
    }
}
