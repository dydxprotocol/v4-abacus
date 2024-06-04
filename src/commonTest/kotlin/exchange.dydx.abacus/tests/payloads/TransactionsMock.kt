package exchange.dydx.abacus.tests.payloads

@Suppress("PropertyName")
internal class TransactionsMock {
    internal val place_order_transaction = """
        {
        	"jsonrpc": "2.0",
        	"id": 106799053660,
        	"result": {
        		"code": 0,
        		"data": "",
        		"log": "[]",
        		"codespace": "",
        		"hash": "B68C6FBE20D49E8EF15246F7CFB2151DF8E9234FF5C7FFF7575E38CB7094BF54"
        	}
        }
    """.trimIndent()
}
