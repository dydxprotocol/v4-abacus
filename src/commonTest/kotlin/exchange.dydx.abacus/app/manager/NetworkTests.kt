package exchange.dydx.abacus.app.manager

import exchange.dydx.abacus.utils.Parser
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

open class NetworkTests {
    val parser = Parser()

    internal fun compareExpectedRequests(expected: String, result: List<String>?) {
        assertNotNull(result, "No requests")
        val urlList = parser.decodeJsonArray(expected)
        assertNotNull(urlList, "Request list does not have the right format")
        assertEquals(urlList.size, result.size, "Request list do not match")
        for (i in 0 until urlList.size) {
            val expectedUrl = parser.asString(urlList[i])
            val resultUrl = result[i]
            assertEquals(expectedUrl, resultUrl, "Result list $i do not match")
        }
    }
}
