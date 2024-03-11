package exchange.dydx.abacus.utils

import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParsingHelperTests {
    private val mock = AbacusMockData()

    @Test
    fun testMerge() {
        val parser = Parser()
        val merged = ParsingHelper.merge(
            parser.decodeJsonObject(mock.localizationMock.appMock),
            parser.decodeJsonObject(mock.localizationMock.appMock2),
        )

        val expected = """
        {
           "APP": {
               "GENERAL": {
                 "TIME_STRINGS": {
                      "ALL_TIME": "all time",
                      "ALL_TIME2": "all time2, {PARAM1}"
                 },
                 "ABOUT": "About",
                 "ABOUT2": "About2"
               }
           }
        }
        """.trimIndent()
        val decoded = parser.decodeJsonObject(expected)
        assertTrue(merged == decoded)
    }

    @Test
    fun testParsing() {
        val parser = Parser()

        var x = parser.asDecimal(1000000000000000000L)
        var y = parser.asString(x)
        assertEquals("1000000000000000000", y)

        x = parser.asDecimal("1000000000000000000")
        y = parser.asString(x)
        assertEquals("1000000000000000000", y)

        x = parser.asDecimal(0.00000000000000000000002034002340)
        y = parser.asString(x)
        assertEquals("0.0000000000000000000000203400234", y)

        x = parser.asDecimal("0.00000000000000000000002034002340")
        y = parser.asString(x)
        assertEquals("0.0000000000000000000000203400234", y)
    }
}
