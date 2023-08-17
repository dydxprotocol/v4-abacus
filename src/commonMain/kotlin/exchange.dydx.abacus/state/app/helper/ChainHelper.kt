package exchange.dydx.abacus.state.app.helper

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.RestRequest
import exchange.dydx.abacus.state.app.adaptors.HttpVerb
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import kollections.JsExport
import kollections.toIMap
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.random.Random
import kotlin.time.Duration

@JsExport
data class AccountInfo(val accountNumber: Int, val sequence: Int) {
}

internal class TransactionRequest(
    internal val request: RestRequest,
    internal val requestId: Long
) {
}

@JsExport
class ChainHelper(private val parser: ParserProtocol, var environment: String) {
    companion object {
        private val configsText: String = """
            {
               "http":{
                  "chainApi":{
                     "scheme":{
                        "dydxprotocol-testnet":"https",
                        "dydxprotocol-staging":"https",
                        "dydxprotocol-dev":"http",
                        "dydxprotocol-dev2":"http",
                        "dydxprotocol-dev3":"http",
                        "dydxprotocol-dev4":"http",
                        "dydxprotocol-dev5":"http",
                        "dydxprotocol-ibc-testnet":"http",
                        "dydxprotocol-testnet-1":"https"
                     },
                     "host":{
                        "dydxprotocol-testnet":"validator.v4testnet.dydx.exchange",
                        "dydxprotocol-staging":"validator.v4staging.dydx.exchange",
                        "dydxprotocol-dev":"validator.v4dev.dydx.exchange",
                        "dydxprotocol-dev2":"35.75.227.118",
                        "dydxprotocol-dev3":"????",
                        "dydxprotocol-dev4":"validator.v4dev4.dydx.exchange",
                        "dydxprotocol-dev5":"18.223.78.50",
                        "dydxprotocol-ibc-testnet":"3.15.49.202",
                        "dydxprotocol-testnet-1":"validator.v4testnet1.dydx.exchange"
                     },
                     "chainId": {
                        "dydxprotocol-testnet":"validator.v4testnet.dydx.exchange",
                        "dydxprotocol-staging":"validator.v4staging.dydx.exchange",
                        "dydxprotocol-dev":"validator.v4dev.dydx.exchange",
                        "dydxprotocol-dev2":"35.75.227.118",
                        "dydxprotocol-dev3":"????",
                        "dydxprotocol-dev4":"validator.v4dev4.dydx.exchange",
                        "dydxprotocol-dev5":"18.223.78.50",
                        "dydxprotocol-ibc-testnet":"3.15.49.202",
                        "dydxprotocol-testnet-1":"validator.v4testnet1.dydx.exchange"
                     }
                  }
               }
            }
        """.trimIndent()
    }

    private val configs: IMap<String, Any> =
        Json.parseToJsonElement(ChainHelper.configsText).jsonObject.toIMap()

    internal fun randomId(): Long {
        return Random.nextLong(100000000000, 999999999999)
    }

    private fun scheme(): String {
        return parser.asString(parser.value(configs, "http.chainApi.scheme.$environment"))
            ?: "https"
    }

    internal fun host(): String? {
        return parser.asString(parser.value(configs, "http.chainApi.host.$environment"))
    }

    private fun path(): String {
        return "/"
    }

    internal fun validatorUrl(): String? {
        val host = host() ?: return null
        val path = path()
        val scheme = scheme()
        return "$scheme://$host$path"
    }

    internal fun heightRequest(
        lastRequestTime: Instant?,
        now: Instant,
        requestId: Long,
        pollingDuration: Duration
    ): RestRequest? {
        return if (lastRequestTime == null || now - lastRequestTime > pollingDuration) {
            val body = iMapOf(
                "jsonrpc" to "2.0",
                "id" to requestId,
                "method" to "block",
                "params" to iMapOf<String, Any>(
                )
            )
            return RestRequest.buildRestRequestWithPort(
                scheme(),
                host(),
                null,
                path(),
                null,
                HttpVerb.post,
                false,
                null,
                body
            )
        } else null
    }

    fun accountRequest(
        data: String
    ): RestRequest? {
        val body = iMapOf(
            "jsonrpc" to "2.0",
            "id" to randomId(),
            "method" to "abci_query",
            "params" to iMapOf(
                "path" to "/cosmos.auth.v1beta1.Query/Account",
                "data" to data,
                "prove" to false
            )
        )
        val scheme = scheme()
        return RestRequest.buildRestRequestWithPort(
            scheme,
            host(),
            null,
            path(),
            null,
            HttpVerb.post,
            false,
            null,
            body
        )
    }

    fun getAccountResponseValue(text: String): String? {
        val response = parser.decodeJsonObject(text)
        return parser.asString(parser.value(response, "result.response.value"))
    }

    internal fun transactionRequest(
        signedTransaction: String
    ): TransactionRequest? {
        val requestId = randomId()
        val body = iMapOf(
            "jsonrpc" to "2.0",
            "id" to requestId,
            "method" to "broadcast_tx_sync",
            "params" to iMapOf(
                "tx" to signedTransaction
            )
        )
        val scheme = scheme()
        val request = RestRequest.buildRestRequestWithPort(
            scheme,
            host(),
            null,
            path(),
            null,
            HttpVerb.post,
            false,
            null,
            body
        )
        return if (request != null) TransactionRequest(request, requestId) else null
    }
}