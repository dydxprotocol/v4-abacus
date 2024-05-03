package exchange.dydx.abacus.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTests {
    @Test
    fun testAsDecimal() {
        val parser = Parser()

        var x = parser.asDecimal(1000000000000000000L)
        assertEquals(BigDecimal.fromLong(1000000000000000000), x)

        x = parser.asDecimal(100)
        assertEquals(BigDecimal.fromLong(100), x)

        x = parser.asDecimal("1000000000000000000")
        assertEquals(BigDecimal.fromLong(1000000000000000000), x)

        x = parser.asDecimal(0.00000000000000000000002034002340)
        assertEquals(BigDecimal.parseString("0.0000000000000000000000203400234"), x)

        x = parser.asDecimal("0.00000000000000000000002034002340")
        assertEquals(BigDecimal.parseString("0.0000000000000000000000203400234"), x)

        x = parser.asDecimal("invalid")
        assertEquals(null, x)

        x = parser.asDecimal(JsonPrimitive("1000000000000000000"))
        assertEquals(BigDecimal.fromLong(1000000000000000000), x)

        x = parser.asDecimal(JsonPrimitive("invalid"))
        assertEquals(null, x)
    }
}
