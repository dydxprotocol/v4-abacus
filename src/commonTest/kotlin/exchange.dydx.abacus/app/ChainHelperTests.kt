package exchange.dydx.abacus.app

import exchange.dydx.abacus.state.app.AppStateMachine
import exchange.dydx.abacus.state.app.V4AppStateMachineProtocol
import exchange.dydx.abacus.utils.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChainHelperTests {
    val parser = Parser()

    val appStateMachine = kotlin.run {
        val appStateMachine = AppStateMachine()
        appStateMachine.setChainId("dydxprotocol-testnet")
        return@run appStateMachine
    }

    @Test
    fun testAppState() {
        testAccountRequest()
        testAccountReponse()
    }

    fun testAccountRequest() {
        val data = "0a2d636f736d6f73313430746d78726536377a656c637a787a326665376a30686e7a6d326a777266726c3861377671"
        val chainHelper = (appStateMachine as? V4AppStateMachineProtocol)?.chainHelper
        assertNotNull(chainHelper)
        val request = chainHelper.accountRequest(data)
        val body = request?.body
        assertNotNull(body)

        val payload = parser.decodeJsonObject(body)
        val data2 = parser.asString(parser.value(payload, "params.data"))
        assertEquals(data, data2)
    }

    fun testAccountReponse() {
        val expectedValue = "ClUKIC9jb3Ntb3MuYXV0aC52MWJldGExLkJhc2VBY2NvdW50EjEKLWNvc21vczE0MHRteHJlNjd6ZWxjenh6MmZlN2owaG56bTJqd3Jmcmw4YTd2cRhP"
        val response = """
            {
            	"jsonrpc": "2.0",
            	"id": 358845818561,
            	"result": {
            		"response": {
            			"code": 0,
            			"log": "",
            			"info": "",
            			"index": "0",
            			"key": null,
            			"value": $expectedValue,
            			"proofOps": null,
            			"height": "509",
            			"codespace": ""
            		}
            	}
            }
        """.trimIndent()

        val chainHelper = (appStateMachine as? V4AppStateMachineProtocol)?.chainHelper
        assertNotNull(chainHelper)
        val value = chainHelper.getAccountResponseValue(response)
        assertEquals(expectedValue, value)
    }
}