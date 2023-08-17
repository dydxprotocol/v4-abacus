package exchange.dydx.abacus.tests.payloads

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

    internal val place_order_fok_failed_transaction = """
        {
        	"jsonrpc": "2.0",
        	"id": 106799053660,
        	"result": {
        		"code": 2000,
        		"data": "",
        		"log": "FillOrKill order could not be fully filled",
        		"codespace": "clob",
        		"hash": "E8E355275220AEF2C38E51B9B901FA448F42C8CBECAB2C25886E0D682D843BCA"
        	}
        }
    """.trimIndent()
}