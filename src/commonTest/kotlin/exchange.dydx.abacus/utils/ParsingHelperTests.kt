package exchange.dydx.abacus.utils

import exchange.dydx.abacus.tests.payloads.AbacusMockData
import kotlin.test.Test
import kotlin.test.assertTrue

class ParsingHelperTests {
    private val mock = AbacusMockData()

    @Test
    fun testMerge() {
        val parser = Parser()
        val merged = ParsingHelper.merge(
            parser.decodeJsonObject(mock.localizationMock.appMock),
            parser.decodeJsonObject(mock.localizationMock.appMock2)
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
}