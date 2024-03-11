package exchange.dydx.abacus.payload.utils

import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTests {
    @Test
    fun testParser() {
        val parser = Parser()
        assertEquals(null, parser.asDouble("this is not a number"))
        assertEquals(0.00000000000000000000001, parser.asDouble("0.00000000000000000000001"))
        assertEquals(0.100000000000001, parser.asDouble("0.100000000000001"))
        assertEquals(0.1, parser.asDouble("0.1000000000000000000000000000000000001"))
        assertEquals(10000000000000000.0, parser.asDouble("10000000000000000"))
    }
}
