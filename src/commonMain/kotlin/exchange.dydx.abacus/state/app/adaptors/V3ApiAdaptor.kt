package exchange.dydx.abacus.state.app.adaptors

import exchange.dydx.abacus.protocols.V3PrivateSignerProtocol
import exchange.dydx.abacus.state.app.AppPlatform
import exchange.dydx.abacus.state.app.AppStateResponse
import exchange.dydx.abacus.state.app.AppVersion
import exchange.dydx.abacus.state.app.NetworkRequests
import exchange.dydx.abacus.state.app.RestRequest
import exchange.dydx.abacus.state.app.SigningRequest
import exchange.dydx.abacus.state.app.SocketRequest
import exchange.dydx.abacus.state.app.SocketRequestType
import exchange.dydx.abacus.state.app.signer.V3ApiKey
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.safeSet
import kollections.iMutableListOf
import kollections.toIMap
import kollections.toIMutableList
import kollections.toIMutableMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class V3ApiAdaptor(
    private val environment: String,
    private val isMainNet: Boolean,
    private val maxSubaccountNumber: Int,
    private val appPlatform: AppPlatform?
) : ApiAdaptor(environment, isMainNet, AppVersion.v3, maxSubaccountNumber), V3ApiAdaptorProtocol,
    ApiConfigurationsProtocol {
    override var signer: V3PrivateSignerProtocol? = null

    private var ethereumAddress: String? = null
    private var apiKey: V3ApiKey? = null

    companion object {
        private val configsText: String = """
            {
               "http":{
                  "api":{
                     "host":{
                        "1": "api.dydx.exchange",
                        "5": "api.stage.dydx.exchange"
                     },
                     "paths":{
                        "private":{
                           "fills":"/v3/fills",
                           "historical-pnl":"/v3/historical-pnl",
                           "users":"/v3/users"
                        },
                        "public":{
                           "candles":"/v3/candles",
                           "config":"/v3/config",
                           "historical-funding":"/v3/historical-funding",
                           "sparklines":"/v3/candles",
                           "time": "/v3/time",
                           "user-exists":"/v3/users/exists",
                           "username-exists":"/v3/usernames"
                        }
                     }
                  },
                  "configs":{
                     "host":"dydx-shared-resources.vercel.app",
                     "paths":{
                        "0x_assets":"/config/prod/0x_assets.json",
                        "countries":"/config/countries.json",
                        "epoch_start":"/config/epoch_start.json",
                        "fee_discounts":"/config/fee_discounts.json",
                        "fee_tiers":"/config/fee_tiers.json",
                        "markets":"/config/markets.json",
                        "network_configs":"/config/network_configs.json",
                        "version":"/config/version_ios.json",
                        "walletsV2":"/config/prod/walletsV2.json"
                     }
                  },
                  "referrer":{
                        "1": "api.dydx.exchange",
                        "5": "api.stage.dydx.exchange"
                  }
               },
               "wss":{
                  "dydx":{
                     "host":{
                        "1": "api.dydx.exchange",
                        "5": "api.stage.dydx.exchange"
                     },
                     "path":"/v3/ws",
                     "channels": {
                        "markets": "v3_markets",
                        "trades": "v3_trades",
                        "orderbook": "v3_orderbook",
                        "subaccount": "v3_accounts"
                     }
                  }
               }
            }
        """.trimIndent()
    }

    private val configs: IMap<String, Any> =
        Json.parseToJsonElement(V3ApiAdaptor.configsText).jsonObject.toIMap()

    override fun ethereumAddress(): String? {
        return ethereumAddress
    }
    override fun setWalletEthereumAddress(
        ethereumAddress: String?,
        apiKey: V3ApiKey?
    ): AppStateResponse {
        return if (this.ethereumAddress != ethereumAddress || this.apiKey != apiKey) {
            this.ethereumAddress = ethereumAddress
            this.apiKey = apiKey
            setAccountAddress(ethereumAddress)
        } else respond(
            stateMachine?.state,
            StateChanges(allStates(), null), null, null
        )
    }

    override fun websocketHost(): String? {
        return parser.asString(parser.value(configs, "wss.dydx.host.$environment"))
    }

    override fun websocketPath(): String? {
        return parser.asString(parser.value(configs, "wss.dydx.path"))
    }

    override fun apiHost(): String? {
        return parser.asString(parser.value(configs, "http.api.host.$environment"))
    }

    private fun referrer(): String? {
        return parser.asString(parser.value(configs, "http.referrer.$environment"))
    }

    override fun privateApiPath(type: String): String? {
        return parser.asString(parser.value(configs, "http.api.paths.private.$type"))
    }

    override fun publicApiPath(type: String): String? {
        return parser.asString(parser.value(configs, "http.api.paths.public.$type"))
    }

    override fun configsHost(): String? {
        return parser.asString(parser.value(configs, "http.configs.host"))
    }

    override fun configsApiPath(type: String): String? {
        return parser.asString(parser.value(configs, "http.configs.paths.$type"))
    }

    override fun networkRequests(
        socketRequests: IList<SocketRequest>?,
        restRequests: IList<RestRequest>?
    ): NetworkRequests? {
        return if (socketRequests != null || restRequests != null) {
            val transformedSocketRequests = socketRequests?.map {
                transformSocketRequest(it)
            }
            val transformedRestRequests = restRequests?.map {
                transformRestRequest(it)
            }
            NetworkRequests(
                transformedSocketRequests,
                transformedRestRequests
            )
        } else null
    }

    private fun transformSocketRequest(request: SocketRequest): SocketRequest {
        return if (request.private && request.type == SocketRequestType.SocketText) {
            val body = request.text ?: return request
            val payload = parser.asMap(Json.parseToJsonElement(body).jsonObject) ?: return request
            val channel = parser.asString(payload["channel"]) ?: return request
            val timeStamp = ServerTime.now().toString()
            val signingRequest = socketSigningRequest(channel, timeStamp)
            val signatureHeader = "signature"
            val secret = apiKey?.secret
            val signer = this.signer
            if (signer != null && secret != null) {
                val signature = signer.sign(signingRequest, secret)
                SocketRequest(
                    request.type,
                    request.url,
                    transformSocketText(payload, timeStamp, signatureHeader, signature),
                    false,
                    null
                )
            } else SocketRequest(
                request.type,
                request.url,
                transformSocketText(payload, timeStamp),
                request.private,
                SigningRequest(signatureHeader, signingRequest)
            )

        } else request
    }

    private fun transformSocketText(
        payload: IMap<String, Any>,
        timeStamp: String,
        signatureHeader: String? = null,
        signature: String? = null
    ): String {
        val modified = payload.toIMutableMap()
        modified.safeSet("apiKey", apiKey?.key)
        modified.safeSet("passphrase", apiKey?.passPhrase)
        modified.safeSet("timestamp", timeStamp)
        if (signature != null && signatureHeader != null) {
            modified.safeSet(signatureHeader, signature)
        }
        return jsonEncoder.encode(modified)
    }

    private fun socketSigningRequest(channel: String, timeStamp: String): String {
        return signingRequest("GET", transformSocketChannel(channel), null, null, timeStamp)
    }

    private fun transformSocketChannel(channel: String): String {
        return when (channel) {
            "v3_accounts" -> "/ws/accounts"
            else -> channel
        }
    }


    private fun transformRestRequest(request: RestRequest): RestRequest {
        return if (request.private) {
            val timeStamp = ServerTime.now().toString()
            val signingRequest =
                signingRequest(
                    request.verb.rawValue,
                    request.url.path,
                    request.url.params?.toString(),
                    request.body,
                    timeStamp
                )
            val signatureHeader = "dydx-signature"
            val secret = apiKey?.secret
            val signer = this.signer
            if (signer != null && secret != null) {
                val signature = signer.sign(signingRequest, secret)
                RestRequest(
                    request.url, request.verb, false,
                    privateHeaders(request.headers, timeStamp, signatureHeader, signature),
                    request.body,
                    null
                )
            } else RestRequest(
                request.url, request.verb, request.private,
                privateHeaders(request.headers, timeStamp),
                request.body,
                SigningRequest(signatureHeader, signingRequest)
            )

        } else request
    }

    private fun privateHeaders(
        headers: IList<NetworkParam>?,
        timeStamp: String,
        signatureHeader: String? = null,
        signature: String? = null
    ): IList<NetworkParam> {
        val params = headers?.toIMutableList() ?: iMutableListOf()
        val referrer = referrer()
        params.add(NetworkParam("Referer", referrer))
        params.add(NetworkParam("Origin", referrer))
        val ethereumAddress = this.ethereumAddress
        if (ethereumAddress != null) {
            params.add(NetworkParam("dydx-ethereum-address", ethereumAddress))
        }
        val apiKey = apiKey?.key
        if (apiKey != null) {
            params.add(NetworkParam("dydx-api-key", apiKey))
        }
        val passPhrase = this.apiKey?.passPhrase
        if (passPhrase != null) {
            params.add(NetworkParam("dydx-passphrase", passPhrase))
        }
        params.add(NetworkParam("dydx-timestamp", timeStamp))
        if (signature != null && signatureHeader != null) {
            params.add(NetworkParam(signatureHeader, signature))
        }
        return params
    }

    private fun signingRequest(
        verb: String,
        path: String,
        params: String?,
        body: String?,
        timeStamp: String
    ): String {
        val pathAndParams = if (params != null) "$path$params" else path
        return if (body != null) "$timeStamp$verb$pathAndParams$body" else "$timeStamp$verb$pathAndParams"
    }

    override fun nextNetworkRequests(
        url: AbUrl, changes: StateChanges?
    ): NetworkRequests? {
        val common = super.nextNetworkRequests(url, changes)
        val rest = common?.restRequests?.toIMutableList() ?: iMutableListOf()
        if (changes?.changes?.contains(Changes.candles) == true) {
            val request = marketCandles()
            if (request != null) {
                rest.add(request)
            }
        }
        return if (rest.size > 0) networkRequests(
            common?.socketRequests,
            rest
        ) else null
    }

    override fun accountIsPrivate(): Boolean {
        return true
    }

    override fun socketMarkets(subscribe: Boolean): String? {
        return socket(socketType(subscribe), "v3_markets")
    }

    override fun socketAccounts(
        subscribe: Boolean,
        address: String,
        subaccountNumber: Int
    ): String? {
        return if (subscribe) {
            socket(
                socketType(subscribe),
                "v3_accounts",
                iMapOf("accountNumber" to "$subaccountNumber")
            )
        } else {
            val accountSocketId = this.accountSocketId
            if (accountSocketId != null) {
                socket(
                    socketType(subscribe),
                    "v3_accounts",
                    iMapOf("accountNumber" to "$subaccountNumber")
                )
            } else null
        }
    }

    override fun socketTrades(subscribe: Boolean, market: String): String? {
        return socket(socketType(subscribe), "v3_trades", iMapOf("id" to market))
    }

    override fun socketOrderbook(subscribe: Boolean, market: String): String? {
        return socket(
            socketType(subscribe),
            "v3_orderbook",
            if (subscribe) iMapOf("id" to market, "batched" to "true") else iMapOf("id" to market)
        )
    }

    override fun subaccountFills(): RestRequest? {
        val apiPath = privateApiPath("fills")
        return RestRequest.buildRestRequest(
            apiScheme(),
            apiHost(),
            apiPath,
            null,
            HttpVerb.get,
            true
        )
    }
}