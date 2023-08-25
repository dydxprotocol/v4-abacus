package exchange.dydx.abacus.state.app.adaptors

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.app.ApiState
import exchange.dydx.abacus.state.app.ApiStatus
import exchange.dydx.abacus.state.app.AppPlatform
import exchange.dydx.abacus.state.app.AppStateResponse
import exchange.dydx.abacus.state.app.AppVersion
import exchange.dydx.abacus.state.app.NetworkRequests
import exchange.dydx.abacus.state.app.NetworkState
import exchange.dydx.abacus.state.app.NetworkStatus
import exchange.dydx.abacus.state.app.RestRequest
import exchange.dydx.abacus.state.app.V4SubaccountCancelOrderPayload
import exchange.dydx.abacus.state.app.V4SubaccountCancelOrderPayload2
import exchange.dydx.abacus.state.app.V4SubaccountPlaceOrderPayload
import exchange.dydx.abacus.state.app.V4SubaccountPlaceOrderPayload2
import exchange.dydx.abacus.state.app.helper.ChainHelper
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.modal.PerpTradingStateMachine
import exchange.dydx.abacus.state.modal.TransferInputField
import exchange.dydx.abacus.state.modal.cancelOrder
import exchange.dydx.abacus.state.modal.cancelOrder2
import exchange.dydx.abacus.state.modal.closePositionPayload
import exchange.dydx.abacus.state.modal.closePositionPayload2
import exchange.dydx.abacus.state.modal.placeOrder
import exchange.dydx.abacus.state.modal.placeOrder2
import exchange.dydx.abacus.state.modal.squidChains
import exchange.dydx.abacus.state.modal.squidRoute
import exchange.dydx.abacus.state.modal.squidStatus
import exchange.dydx.abacus.state.modal.squidTokens
import exchange.dydx.abacus.state.modal.transfer
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.toUrlParams
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIMap
import kollections.toIMutableList
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

class V4ApiAdaptor(
    private val environment: String,
    private val chainId: String,
    private val isMainNet: Boolean,
    private val maxSubaccountNumber: Int,
    private val appPlatform: AppPlatform?
) : ApiAdaptor(environment, isMainNet, AppVersion.v4, maxSubaccountNumber), V4ApiAdaptorProtocol,
    ApiConfigurationsProtocol {
    override val chainHelper = ChainHelper(parser, environment)

    private var cosmoAddress: String? = null

    private var faucetSubaccountId: Int? = null
    private var subaccountsRequestTime: Instant? = null

    /*
    Network states of indexer and validator
     */
    private val pollingDuration = 10.seconds
    private val MAX_NUM_BLOCK_DELAY = 10

    internal var indexerState = NetworkState()
    internal var validatorState = NetworkState()
    private var apiState: ApiState? = null

    /*
    Special handling for order transaction
    We don't have an orderId from transaction, so we need to match order with clientId
     */
    internal var lastOrderClientId: Int? = null
    internal var lastTransactionRequestId: Long? = null
    internal var lastTransmittedOrderClientId: Int? = null

    companion object {
        private val configsText: String = """
            {
               "http":{
                  "api":{
                     "scheme":{
                        "dydxprotocol-testnet":"https",
                        "dydxprotocol-staging":"https",
                        "dydxprotocol-dev":"http",
                        "dydxprotocol-dev2":"http",
                        "dydxprotocol-dev3":"http",
                        "dydxprotocol-dev4":"http",
                        "dydxprotocol-dev5":"http",
                        "dydxprotocol-ibc-testnet":"http",
                        "dydxprotocol-testnet-1": "https"
                     },
                     "host":{
                        "dydxprotocol-testnet":"indexer.v4testnet.dydx.exchange",
                        "dydxprotocol-staging":"indexer.v4staging.dydx.exchange",
                        "dydxprotocol-dev":"indexer.v4dev.dydx.exchange",
                        "dydxprotocol-dev2":"dev2-indexer-apne1-lb-public-2076363889.ap-northeast-1.elb.amazonaws.com",
                        "dydxprotocol-dev3":"???",
                        "dydxprotocol-dev4":"indexer.v4dev4.dydx.exchange",
                        "dydxprotocol-dev5":"dev5-indexer-apne1-lb-public-1721328151.ap-northeast-1.elb.amazonaws.com",
                        "dydxprotocol-ibc-testnet":"testnet-indexer-apne1-lb-public-1153637034.ap-northeast-1.elb.amazonaws.com",
                        "dydxprotocol-testnet-1":"indexer.v4testnet1.dydx.exchange"
                     },
                     "paths":{
                        "public":{
                           "fills":"/v4/fills",
                           "candles":"/v4/candles/perpetualMarkets",
                           "config":"/v4/config",
                           "historical-funding":"/v4/historicalFunding",
                           "historical-pnl":"/v4/historical-pnl",
                           "sparklines":"/v4/sparklines",
                           "subaccounts":"/v4/addresses",
                           "time":"/v4/time",
                           "height":"/v4/height"
                        }
                     }
                  },
                  "faucetApi":{
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
                        "dydxprotocol-testnet":"faucet.v4testnet.dydx.exchange",
                        "dydxprotocol-staging":"faucet.v4staging.dydx.exchange",
                        "dydxprotocol-dev":"faucet.v4dev.dydx.exchange",
                        "dydxprotocol-dev2":"dev2-faucet-lb-public-2098845166.us-east-2.elb.amazonaws.com",
                        "dydxprotocol-dev3":"????",
                        "dydxprotocol-dev4":"faucet.v4dev4.dydx.exchange",
                        "dydxprotocol-dev5":"dev5-faucet-lb-public-893963411.us-east-2.elb.amazonaws.com",
                        "dydxprotocol-ibc-testnet":"testnet-faucet-lb-public-1082933201.us-east-2.elb.amazonaws.com",
                        "dydxprotocol-testnet-1":"faucet.v4testnet1.dydx.exchange"
                     },
                     "paths":{
                        "public":{
                           "faucet":"/faucet/tokens"
                        }
                     }
                  },
                  "0xSquid":{
                     "scheme":{
                        "dydxprotocol-testnet":"https",
                        "dydxprotocol-staging":"https",
                        "dydxprotocol-dev":"https",
                        "dydxprotocol-dev2":"https",
                        "dydxprotocol-dev3":"https",
                        "dydxprotocol-dev4":"https",
                        "dydxprotocol-dev5":"https",
                        "dydxprotocol-ibc-testnet":"https",
                        "dydxprotocol-testnet-1":"https"
                     },
                     "host":{
                        "dydxprotocol-testnet":"squid-api-git-feat-dydx-poc2-v2-0xsquid.vercel.app",
                        "dydxprotocol-staging":"squid-api-git-feat-dydx-poc2-v2-0xsquid.vercel.app",
                        "dydxprotocol-dev":"testnet.api.0xsquid.com",
                        "dydxprotocol-dev2":"testnet.api.0xsquid.com",
                        "dydxprotocol-dev3":"testnet.api.0xsquid.com",
                        "dydxprotocol-dev4":"testnet.api.0xsquid.com",
                        "dydxprotocol-dev5":"testnet.api.0xsquid.com",
                        "dydxprotocol-ibc-testnet":"squid-api-git-feat-dydx-poc2-v2-0xsquid.vercel.app",
                        "dydxprotocol-testnet-1":"squid-api-git-feat-dydx-poc2-v2-0xsquid.vercel.app",
                        "production": "api.0xsquid.com"
                     },
                     "paths":{
                        "public":{
                           "chains":"/v1/chains",
                           "tokens":"/v1/tokens",
                           "route":"/v1/route",
                           "status":"/v1/status"
                        }
                     }
                  },
                  "configs":{
                     "host":"dydx-v4-shared-resources.vercel.app",
                     "paths":{
                        "0x_assets":"/config/prod/0x_assets.json",
                        "countries":"/config/countries.json",
                        "epoch_start":"/config/epoch_start.json",
                        "fee_discounts":"/config/fee_discounts.json",
                        "fee_tiers":"/config/fee_tiers.json",
                        "markets":"/v4/staging/markets.json",
                        "network_configs":"/config/network_configs.json",
                        "version":"/config/version_ios.json",
                        "walletsV2":"/config/prod/walletsV2.json"
                     }
                  }
               },
               "wss":{
                  "dydx":{
                     "scheme":{
                        "dydxprotocol-testnet":"wss",
                        "dydxprotocol-staging":"wss",
                        "dydxprotocol-dev":"ws",
                        "dydxprotocol-dev2":"ws",
                        "dydxprotocol-dev3":"ws",
                        "dydxprotocol-dev4":"ws",
                        "dydxprotocol-dev5":"ws",
                        "dydxprotocol-ibc-testnet":"ws",
                        "dydxprotocol-testnet-1":"wss"
                     },
                     "host":{
                        "dydxprotocol-testnet":"indexer.v4testnet.dydx.exchange",
                        "dydxprotocol-staging":"indexer.v4staging.dydx.exchange",
                        "dydxprotocol-dev":"indexer.v4dev.dydx.exchange",
                        "dydxprotocol-dev2":"dev2-indexer-apne1-lb-public-2076363889.ap-northeast-1.elb.amazonaws.com",
                        "dydxprotocol-dev3":"???",
                        "dydxprotocol-dev4":"indexer.v4dev4.dydx.exchange",
                        "dydxprotocol-dev5":"dev5-indexer-apne1-lb-public-1721328151.ap-northeast-1.elb.amazonaws.com",
                        "dydxprotocol-ibc-testnet":"testnet-indexer-apne1-lb-public-1153637034.ap-northeast-1.elb.amazonaws.com",
                        "dydxprotocol-testnet-1":"indexer.v4testnet1.dydx.exchange"
                     },
                     "path":"/v4/ws",
                     "channels": {
                        "markets": "v4_markets",
                        "trades": "v4_trades",
                        "candles": "v4_candles",
                        "orderbook": "v4_orderbook",
                        "subaccount": "v4_subaccounts"
                     }
                  }
               }
            }
        """.trimIndent()
    }

    private val configs: IMap<String, Any> =
        Json.parseToJsonElement(V4ApiAdaptor.configsText).jsonObject.toIMap()

    override fun cosmoAddress(): String? {
        return this.cosmoAddress
    }

    override fun setWalletCosmoAddress(
        cosmoAddress: String?
    ): AppStateResponse {
        this.cosmoAddress = cosmoAddress
        return setAccountAddress(cosmoAddress)
    }

    override fun subaccountNumber(): Int {
        return subaccountNumber
    }

    override fun connectingSubaccountNumber(
        subaccountNumber: Int
    ): Int? {
        val subaccountNumbers =
            stateMachine.state?.account?.subaccounts?.keys?.mapNotNull { it -> parser.asInt(it) }
        return if (subaccountNumbers != null) {
            return if (subaccountNumbers.firstOrNull { it -> it == subaccountNumber } != null) subaccountNumber else null
        } else null
    }

    fun setCosmoAddress(
        cosmoAddress: String?
    ): AppStateResponse {
        /*
        unsubscribe from existing accounts channel
        subscribe to new accounts channel
         */
        val url = websocketUrl()
        return if (this.cosmoAddress != cosmoAddress && url != null) {
            val oldValue = this.cosmoAddress
            val oldSubaccountNumber = subaccountNumber
            this.cosmoAddress = cosmoAddress
            this.subaccountNumber = 0
            respond(
                stateMachine.state,
                StateChanges(iListOf(Changes.wallet), null),
                null,
                accountRequest(
                    url,
                    cosmoAddress,
                    subaccountNumber,
                    oldValue,
                    oldSubaccountNumber
                )
            )
        } else respond(stateMachine.state, null, null, null)
    }

    override fun setSubaccountNumber(
        subaccountNumber: Int
    ): AppStateResponse {
        val url = websocketUrl()
        return if (this.cosmoAddress != null && this.subaccountNumber != subaccountNumber && url != null) {
            val oldValue = subaccountNumber
            this.subaccountNumber = subaccountNumber
            return respond(
                stateMachine.state,
                StateChanges(iListOf<Changes>()),
                null,
                accountRequest(
                    url,
                    cosmoAddress,
                    subaccountNumber,
                    cosmoAddress,
                    oldValue
                )
            )
        } else respond(stateMachine.state, null, null, null)
    }

    override fun accountRequest(
        url: AbUrl,
        address: String?,
        subaccountNumber: Int?,
        oldAddress: String?,
        oldSubaccountNumber: Int?
    ): NetworkRequests? {
        subaccountsRequestTime = null   // force a subaccounts call
        return super.accountRequest(
            url,
            address,
            subaccountNumber,
            oldAddress,
            oldSubaccountNumber
        )
    }

    private fun subaccountsRequest(): RestRequest? {
        val accountAddress = accountAddress()
        return if (accountAddress != null) {
            val apiPath = publicApiPath("subaccounts")
            RestRequest.buildRestRequest(
                apiScheme(), apiHost(), "$apiPath/$accountAddress"
            )
        } else null
    }

    private fun indexerHeightRequest(): RestRequest? {
        val now = ServerTime.now()
        val lastRequestTime = indexerState.requestTime
        return if (lastRequestTime == null || now - lastRequestTime > pollingDuration) {
            indexerState.requestTime = now
            RestRequest.buildRestRequest(
                apiScheme(), apiHost(), publicApiPath("height")
            )
        } else null
    }

    private fun updateApiState() {
        apiState = apiState(apiState, indexerState, validatorState)
    }

    private fun apiState(
        apiState: ApiState?,
        indexerState: NetworkState,
        validatorState: NetworkState
    ): ApiState {
        var status = apiState?.status ?: ApiStatus.UNKNOWN
        var haltedBlock = apiState?.haltedBlock
        var blockDiff: Int? = null
        when (validatorState.status) {
            NetworkStatus.NORMAL -> {
                when (indexerState.status) {
                    NetworkStatus.NORMAL, NetworkStatus.UNKNOWN -> {
                        status = ApiStatus.NORMAL
                        haltedBlock = null
                    }

                    NetworkStatus.UNREACHABLE -> {
                        status = ApiStatus.INDEXER_DOWN
                        haltedBlock = null
                    }

                    NetworkStatus.HALTED -> {
                        status = ApiStatus.INDEXER_HALTED
                        haltedBlock = indexerState.block
                    }
                }
            }

            NetworkStatus.UNKNOWN -> {
                when (indexerState.status) {
                    NetworkStatus.NORMAL -> {
                        status = ApiStatus.NORMAL
                        haltedBlock = null
                    }

                    NetworkStatus.UNKNOWN -> {
                        status = ApiStatus.UNKNOWN
                        haltedBlock = null
                    }

                    NetworkStatus.UNREACHABLE -> {
                        status = ApiStatus.INDEXER_DOWN
                        haltedBlock = null
                    }

                    NetworkStatus.HALTED -> {
                        status = ApiStatus.INDEXER_HALTED
                        haltedBlock = indexerState.block
                    }
                }
            }

            NetworkStatus.UNREACHABLE -> {
                status = ApiStatus.VALIDATOR_DOWN
                haltedBlock = null
            }

            NetworkStatus.HALTED -> {
                status = ApiStatus.VALIDATOR_HALTED
                haltedBlock = validatorState.block
            }
        }
        if (status == ApiStatus.NORMAL) {
            val indexerBlock = indexerState.block
            val validatorBlock = validatorState.block
            if (indexerBlock != null && validatorBlock != null) {
                val diff = validatorBlock - indexerBlock
                if (diff > MAX_NUM_BLOCK_DELAY) {
                    status = ApiStatus.INDEXER_TRAILING
                    blockDiff = diff
                    haltedBlock = null
                }
            }
        }
        val block = if (validatorState.block != null) {
            if (indexerState.block != null) {
                max(validatorState.block!!, indexerState.block!!)
            } else validatorState.block
        } else indexerState.block
        if (apiState?.status != status ||
            apiState.height != block ||
            apiState.haltedBlock != haltedBlock ||
            apiState.trailingBlocks != blockDiff
        ) {
            return ApiState(status, block, haltedBlock, blockDiff)
        }
        return apiState
    }

    private fun validatorHeightRequest(): RestRequest? {
        val now = ServerTime.now()
        val requestId = chainHelper.randomId()
        val request =
            chainHelper.heightRequest(validatorState.requestTime, now, requestId, pollingDuration)
        return if (request != null) {
            validatorState.requestId = requestId
            validatorState.requestTime = now
            return request
        } else null
    }

    override fun processHttpResponse(
        url: AbUrl,
        text: String,
        height: Int?,
    ): AppStateResponse? {
        val response = super.processHttpResponse(url, text, height)
        return if (url.host == apiHost() && url.path.startsWith(publicApiPath("subaccounts")!!)) {
            val oldConnectedSubaccountNumber = connectedSubaccountNumber
            this.connectedSubaccountNumber =
                connectingSubaccountNumber(subaccountNumber)
            val url = websocketUrl()
            return if (url != null && oldConnectedSubaccountNumber != connectedSubaccountNumber) {
                val accountAddress = accountAddress()
                val accountRequest = accountRequest(
                    url,
                    accountAddress,
                    connectedSubaccountNumber,
                    accountAddress,
                    oldConnectedSubaccountNumber
                )
                val modifiedNetworkRequests = if (response?.networkRequests != null)
                    response.networkRequests.add(
                        accountRequest?.socketRequests,
                        accountRequest?.restRequests
                    )
                else accountRequest
                return respond(
                    response?.state,
                    response?.changes,
                    response?.errors,
                    modifiedNetworkRequests
                )
            } else response
        } else if (url.host == apiHost() && url.path == publicApiPath("height")) {
            val changes = processIndexerHeight(text)
            updateApiState()
            respond(stateMachine.state, changes, null, null)
        } else if (url.host == chainHelper.host()) {
            val response = processValidatorMessages(text)
            updateApiState()
            respond(response.state, response.changes, response.errors, null)
        } else if (url.host == faucetApiHost() && url.path == faucetApiPath("faucet")) {
            subaccountsRequestTime = null   // force a refresh of subaccounts
            respond(
                response?.state,
                response?.changes,
                response?.errors,
                response?.networkRequests
            )
        } else if (url.host == squidApiHost() && url.path == squidApiPath("chains")) {
            var change = stateMachine.squidChains(text)
            if (change != null) {
                stateMachine.update(change)
            }
            return respond(stateMachine.state, change, null, null)
        } else if (url.host == squidApiHost() && url.path == squidApiPath("tokens")) {
            var change = stateMachine.squidTokens(text)
            if (change != null) {
                stateMachine.update(change)
            }
            return respond(stateMachine.state, change, null, null)
        } else if (url.host == squidApiHost() && url.path == squidApiPath("route")) {
            var change = stateMachine.squidRoute(text)
            if (change != null) {
                stateMachine.update(change)
            }
            return respond(stateMachine.state, change, null, null)
        } else if (url.host == squidApiHost() && url.path == squidApiPath("status")) {
            var change = stateMachine.squidStatus(text)
            if (change != null) {
                stateMachine.update(change)
            }
            return respond(stateMachine.state, change, null, null)
        } else response
    }

    private fun response(text: String): IMap<String, Any>? {
        val json = try {
            Json.parseToJsonElement(text).jsonObject.toIMap()
        } catch (e: Exception) {
            null
        }
        return json
    }

    private fun processIndexerHeight(text: String): StateChanges? {
        val response = response(text)
        if (response != null) {
            val height = parser.asInt(response["height"])
            val time = parser.asDatetime(response["time"])
            indexerState.block = height
            indexerState.time = time
            return null
        } else {
            indexerState.block = null
            indexerState.time = null
            return null
        }
    }

    private fun processValidatorMessages(text: String): AppStateResponse {
        val response = response(text)
        return if (response != null) {
            val requestId = parser.asLong(response["id"])
            if (requestId == validatorState.requestId) {
                val height = parser.asInt(parser.value(response, "result.block.header.height"))
                val time = parser.asDatetime(parser.value(response, "result.block.header.time"))
                validatorState.block = height
                validatorState.time = time
                val subaccountNumber = subaccountNumber()
                val response = stateMachine.received(subaccountNumber, height)
                AppStateResponse(
                    response.state,
                    response.changes,
                    response.errors,
                    null,
                    null,
                    null
                )
            } else if (requestId == lastTransactionRequestId) {
                val result = parser.asMap(response["result"])
                val code = parser.asInt(result?.get("code"))
                if (code != null) {
                    if (code == 0) {
                        lastTransmittedOrderClientId = lastOrderClientId
                        val response = stateMachine.clearInput(subaccountNumber)
                        AppStateResponse(
                            response.state,
                            response.changes,
                            response.errors,
                            null,
                            null,
                            null
                        )
                    } else {
                        val message = parser.asString(result?.get("log")) ?: "error"
                        val error = V4TransactionErrors.error(code, message)
                        AppStateResponse(
                            stateMachine.state,
                            null,
                            if (error != null) iListOf(error) else null,
                            null,
                            null,
                            null
                        )
                    }
                } else
                    noChange()
            } else {
                noChange()
            }
        } else {
            validatorState.block = null
            validatorState.time = null
            noChange()
        }
    }

    override fun websocketScheme(): String {
        return parser.asString(parser.value(configs, "wss.dydx.scheme.$environment")) ?: "wss"
    }

    override fun websocketHost(): String? {
        return parser.asString(parser.value(configs, "wss.dydx.host.$environment"))
    }

    override fun websocketPath(): String? {
        return parser.asString(parser.value(configs, "wss.dydx.path"))
    }

    override fun apiScheme(): String {
        return parser.asString(parser.value(configs, "http.api.scheme.$environment")) ?: "https"
    }

    override fun apiHost(): String? {
        return parser.asString(parser.value(configs, "http.api.host.$environment"))
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

    private fun faucetApiScheme(): String {
        return parser.asString(parser.value(configs, "http.faucetApi.scheme.$environment"))
            ?: "https"
    }

    private fun faucetApiHost(): String? {
        return parser.asString(parser.value(configs, "http.faucetApi.host.$environment"))
    }

    private fun faucetApiPath(type: String): String? {
        return parser.asString(parser.value(configs, "http.faucetApi.paths.public.$type"))
    }

    private fun squidApiScheme(): String {
        return parser.asString(parser.value(configs, "http.0xSquid.scheme.$environment"))
            ?: "https"
    }

    private fun squidApiHost(): String? {
        return parser.asString(parser.value(configs, "http.0xSquid.host.$environment"))
    }

    private fun squidApiPath(type: String): String? {
        return parser.asString(parser.value(configs, "http.0xSquid.paths.public.$type"))
    }

    override fun socketMarkets(subscribe: Boolean): String? {
        return socket(socketType(subscribe), "v4_markets", iMapOf("batched" to "true"))
    }

    override fun socketAccounts(
        subscribe: Boolean,
        address: String,
        subaccountNumber: Int
    ): String? {
        return if (subscribe) {
            socket(
                socketType(subscribe),
                "v4_subaccounts",
                iMapOf("id" to "$address/$subaccountNumber", "batched" to "true"),

            )
        } else {
            val accountSocketId = this.accountSocketId
            if (accountSocketId != null) {
                socket(
                    socketType(subscribe),
                    "v4_subaccounts",
                    iMapOf("id" to "$address/$subaccountNumber")
                )
            } else null
        }
    }

    override fun socketTrades(subscribe: Boolean, market: String): String? {
        return socket(
            socketType(subscribe),
            "v4_trades",
            iMapOf("id" to market, "batched" to "true")
        )
    }

    override fun socketOrderbook(subscribe: Boolean, market: String): String? {
        return socket(
            socketType(subscribe),
            "v4_orderbook",
            if (subscribe) iMapOf("id" to market, "batched" to "true") else iMapOf("id" to market)
        )
    }

    override fun faucetRequest(amount: Int): RestRequest? {
        val accountAddress = accountAddress()
        val subaccountNumber = subaccountNumber()
        return if (accountAddress != null) {
            faucetSubaccountId = subaccountNumber
            val params = iMapOf(
                "address" to accountAddress,
                "subaccountNumber" to subaccountNumber,
                "amount" to amount
            )
            RestRequest.buildRestRequest(
                faucetApiScheme(),
                faucetApiHost(),
                faucetApiPath("faucet"),
                null,
                HttpVerb.post,
                false,
                null,
                params
            )
        } else null
    }


    override fun subaccountFills(): RestRequest? {
        val address = accountAddress()
        return if (address != null) {
            val subaccountNumber = subaccountNumber()
            val apiPath = publicApiPath("fills")
            RestRequest.buildRestRequest(
                apiScheme(),
                apiHost(),
                apiPath,
                "address=$address&subaccountNumber=$subaccountNumber",
                HttpVerb.get,
                true
            )
        } else null
    }

    override fun accountHistoricalPnl(): RestRequest? {
        val address = accountAddress()
        val subaccountNumber = subaccountNumber()
        return if (address != null && subaccountNumber != null) {
            val request = super.accountHistoricalPnl()
            if (request != null) {
                val url = request.url
                val params = url.params?.toIMutableList() ?: iMutableListOf()
                params.add(NetworkParam("address", address))
                params.add(NetworkParam("subaccountNumber", "$subaccountNumber"))
                val modifiedUrl = AbUrl(url.host, url.port, url.path, url.scheme, params)

                RestRequest(modifiedUrl, request.verb, false, null, null)
            } else null
        } else null
    }

    override fun transferAssetsRequests(): IList<RestRequest>? {
        val requests = iMutableListOf<RestRequest>()
        RestRequest.buildRestRequest(
            squidApiScheme(),
            squidApiHost(),
            squidApiPath("chains"),
            null,
            HttpVerb.get,
            false,
            null,
            null
        )?.let {
            requests.add(it)
        }
        RestRequest.buildRestRequest(
            squidApiScheme(),
            squidApiHost(),
            squidApiPath("tokens"),
            null,
            HttpVerb.get,
            false,
            null,
            null
        )?.let {
            requests.add(it)
        }
        return requests
    }

    override fun transaction(
        signedTransaction: String
    ): AppStateResponse {
        val transactionRequest = chainHelper.transactionRequest(signedTransaction)
        lastTransactionRequestId = transactionRequest?.requestId
        return respond(
            stateMachine.state,
            null,
            null,
            if (transactionRequest != null) {
                NetworkRequests(null, iListOf(transactionRequest.request))
            } else null
        )
    }

    override fun transfer(data: String?, type: TransferInputField?): AppStateResponse {
        val stateResponse = stateMachine.transfer(data, type, subaccountNumber())
        var networkRequests: NetworkRequests? = null
        if (stateResponse.state?.input?.transfer?.type == TransferType.deposit) {
            if  (type == TransferInputField.size) {
                val transferRequest = fetchDepositRouteIfNeeded(stateMachine.state)
                networkRequests = if (transferRequest != null) {
                    NetworkRequests(null, iListOf(transferRequest))
                } else null
            }
        } else if (stateResponse.state?.input?.transfer?.type == TransferType.withdrawal) {
            if  (type == TransferInputField.usdcSize ||
                type == TransferInputField.address ||
                type == TransferInputField.chain ||
                type == TransferInputField.token ) {
                val transferRequest = fetchWithdrawalRouteIfNeeded(stateMachine.state)
                networkRequests = if (transferRequest != null) {
                    NetworkRequests(null, iListOf(transferRequest))
                } else null
            }
        }

        return respond(
            stateResponse.state,
            stateResponse.changes,
            stateResponse.errors,
            networkRequests
        )
    }

    private fun height(): Int? {
        val indexerHeight = indexerState.block
        val validatorHeight = validatorState.block
        return if (indexerHeight != null) {
            if (validatorHeight != null) max(indexerHeight, validatorHeight) else indexerHeight
        } else validatorHeight
    }

    override fun placeOrderPayload(
    ): V4SubaccountPlaceOrderPayload? {
        val height = height()
        lastOrderClientId = null
        lastTransmittedOrderClientId = null
        return if (height != null) {
            val subaccountNumber = connectedSubaccountNumber ?: return null
            val address = cosmoAddress ?: return null
            val placeOrder = (stateMachine as? PerpTradingStateMachine)?.placeOrder(height)
            if (placeOrder != null) {
                lastOrderClientId = placeOrder.clientId
                return V4SubaccountPlaceOrderPayload(
                    chainId,
                    address,
                    subaccountNumber,
                    clobPairId = placeOrder.clobPairId,
                    side = placeOrder.side,
                    quantums = placeOrder.quantums,
                    subticks = placeOrder.subticks,
                    goodUntilBlock = placeOrder.goodUntilBlock,
                    goodUntilTime = placeOrder.goodUntilTime,
                    clientId = placeOrder.clientId,
                    timeInForce = placeOrder.timeInForce,
                    orderFlags = placeOrder.orderFlags,
                    reduceOnly = placeOrder.reduceOnly,
                    clientMetadata = placeOrder.clientMetadata,
                    conditionType = placeOrder.conditionType,
                    conditionalOrderTriggerSubticks = placeOrder.conditionalOrderTriggerSubticks
                )
            } else null
        } else null
    }

    override fun closePositionPayload(
    ): V4SubaccountPlaceOrderPayload? {
        val height = height()
        lastOrderClientId = null
        lastTransmittedOrderClientId = null
        return if (height != null) {
            val subaccountNumber = connectedSubaccountNumber ?: return null
            val address = cosmoAddress ?: return null
            val placeOrder =
                (stateMachine as? PerpTradingStateMachine)?.closePositionPayload(height)
            if (placeOrder != null) {
                lastOrderClientId = placeOrder.clientId
                return V4SubaccountPlaceOrderPayload(
                    chainId,
                    address,
                    subaccountNumber,
                    clobPairId = placeOrder.clobPairId,
                    side = placeOrder.side,
                    quantums = placeOrder.quantums,
                    subticks = placeOrder.subticks,
                    goodUntilBlock = placeOrder.goodUntilBlock,
                    goodUntilTime = placeOrder.goodUntilTime,
                    clientId = placeOrder.clientId,
                    timeInForce = placeOrder.timeInForce,
                    orderFlags = placeOrder.orderFlags,
                    reduceOnly = placeOrder.reduceOnly,
                    clientMetadata = placeOrder.clientMetadata,
                    conditionType = placeOrder.conditionType,
                    conditionalOrderTriggerSubticks = placeOrder.conditionalOrderTriggerSubticks
                )
            } else null
        } else null
    }

    override fun cancelOrderPayload(
        orderId: String
    ): V4SubaccountCancelOrderPayload? {
        val height = height()
        return if (height != null) {
            val subaccountNumber = connectedSubaccountNumber ?: return null
            val address = cosmoAddress ?: return null
            val cancelOrder = (stateMachine as? PerpTradingStateMachine)?.cancelOrder(
                orderId,
                subaccountNumber,
                height
            )
            if (cancelOrder != null) {
                return V4SubaccountCancelOrderPayload(
                    chainId,
                    address,
                    subaccountNumber,
                    clobPairId = cancelOrder.clobPairId,
                    clientId = cancelOrder.clientId,
                    orderFlags = cancelOrder.orderFlags,
                    goodUntilBlock = cancelOrder.goodUntilBlock,
                    goodUntilTime = cancelOrder.goodUntilTime,
                )
            } else null
        } else null
    }

    override fun placeOrderPayload2(
    ): V4SubaccountPlaceOrderPayload2? {
        val height = height()
        lastOrderClientId = null
        lastTransmittedOrderClientId = null
        return if (height != null) {
            val subaccountNumber = connectedSubaccountNumber ?: return null
            val address = cosmoAddress ?: return null
            val placeOrder = (stateMachine as? PerpTradingStateMachine)?.placeOrder2(height)
            if (placeOrder != null) {
                lastOrderClientId = placeOrder.clientId
                return V4SubaccountPlaceOrderPayload2(
                    chainId,
                    address,
                    subaccountNumber,
                    clobPairId = placeOrder.clobPairId,
                    side = placeOrder.side,
                    quantums = placeOrder.quantums,
                    subticks = placeOrder.subticks,
                    goodTilBlock = placeOrder.goodTilBlock,
                    goodTilBlockTime = placeOrder.goodTilBlockTime,
                    clientId = placeOrder.clientId,
                    timeInForce = placeOrder.timeInForce,
                    orderFlags = placeOrder.orderFlags,
                    reduceOnly = placeOrder.reduceOnly,
                    clientMetadata = placeOrder.clientMetadata,
                    conditionType = placeOrder.conditionType,
                    conditionalOrderTriggerSubticks = placeOrder.conditionalOrderTriggerSubticks
                )
            } else null
        } else {
            throw IllegalStateException("Height is null")
        }
    }

    override fun closePositionPayload2(
    ): V4SubaccountPlaceOrderPayload2? {
        val height = height()
        lastOrderClientId = null
        lastTransmittedOrderClientId = null
        return if (height != null) {
            val subaccountNumber = connectedSubaccountNumber ?: return null
            val address = cosmoAddress ?: return null
            val placeOrder =
                (stateMachine as? PerpTradingStateMachine)?.closePositionPayload2(height)
            if (placeOrder != null) {
                lastOrderClientId = placeOrder.clientId
                return V4SubaccountPlaceOrderPayload2(
                    chainId,
                    address,
                    subaccountNumber,
                    clobPairId = placeOrder.clobPairId,
                    side = placeOrder.side,
                    quantums = placeOrder.quantums,
                    subticks = placeOrder.subticks,
                    goodTilBlock = placeOrder.goodTilBlock,
                    goodTilBlockTime = placeOrder.goodTilBlockTime,
                    clientId = placeOrder.clientId,
                    timeInForce = placeOrder.timeInForce,
                    orderFlags = placeOrder.orderFlags,
                    reduceOnly = placeOrder.reduceOnly,
                    clientMetadata = placeOrder.clientMetadata,
                    conditionType = placeOrder.conditionType,
                    conditionalOrderTriggerSubticks = placeOrder.conditionalOrderTriggerSubticks
                )
            } else null
        } else {
            throw IllegalStateException("Height is null")
        }
    }

    override fun cancelOrderPayload2(
        orderId: String
    ): V4SubaccountCancelOrderPayload2? {
        val height = height()
        return if (height != null) {
            val subaccountNumber = connectedSubaccountNumber ?: return null
            val address = cosmoAddress ?: return null
            val cancelOrder = (stateMachine as? PerpTradingStateMachine)?.cancelOrder2(
                orderId,
                subaccountNumber,
                height
            )
            if (cancelOrder != null) {
                return V4SubaccountCancelOrderPayload2(
                    chainId,
                    address,
                    subaccountNumber,
                    clobPairId = cancelOrder.clobPairId,
                    clientId = cancelOrder.clientId,
                    orderFlags = cancelOrder.orderFlags,
                    goodTilBlock = cancelOrder.goodTilBlock,
                    goodTilBlockTime = cancelOrder.goodTilBlockTime
                )
            } else null
        } else {
            throw IllegalStateException("Height is null")
        }
    }

    private fun retrieveSubaccountsIfNeeded(state: PerpetualState?): RestRequest? {
        var subaccountsRequest: RestRequest? = null
        val now = ServerTime.now()
        if (faucetSubaccountId != null && state?.subaccount(faucetSubaccountId!!) == null) {
            if (subaccountsRequestTime == null ||
                now - subaccountsRequestTime!! > 5.seconds
            ) {
                subaccountsRequest = subaccountsRequest()
            }
        } else if (connectedSubaccountNumber == null) {
            if (subaccountsRequestTime == null ||
                now - subaccountsRequestTime!! > 10.seconds
            ) {
                subaccountsRequest = subaccountsRequest()
            }
        }
        if (subaccountsRequest != null) {
            subaccountsRequestTime = now
        }
        return subaccountsRequest
    }

    private fun fetchDepositRouteIfNeeded(state: PerpetualState?): RestRequest? {
        val fromChain = state?.input?.transfer?.chain
        val fromToken = state?.input?.transfer?.token
        val fromAmount = state?.input?.transfer?.size?.size?.let {
            val decimals = parser.asDouble(stateMachine.squidProcessor.selectedTokenDecimals(fromToken))
            if (decimals != null) {
                (it * 10.0.pow(decimals)).toBigDecimal().toBigInteger()
            } else null
        }
        val fromAmountString = parser.asString(fromAmount)
        if (fromChain != null &&
            fromToken != null &&
            fromAmount != null && fromAmount > 0 &&
            fromAmountString != null &&
            cosmoAddress != null
        ) {
             val params: IMap<String, String> = iMapOf<String, String>(
                "fromChain" to fromChain,
                "fromToken" to fromToken,
                "fromAmount" to fromAmountString,
                "toChain" to chainId,
                "toToken" to "ibc/39549F06486BACA7494C9ACDD53CDD30AA9E723AB657674DBD388F867B61CA7B",
                "toAddress" to cosmoAddress.toString(),
                "slippage" to "1",
                "enableForecall" to "true",
            )

            val routeRequest = RestRequest.buildRestRequest(
                squidApiScheme(),
                squidApiHost(),
                squidApiPath("route"),
                params.toUrlParams(),
                HttpVerb.get,
                false,
                null,
                null
            )

            return routeRequest
        }
        return null
    }

    private fun fetchWithdrawalRouteIfNeeded(state: PerpetualState?): RestRequest? {
        val toChain = state?.input?.transfer?.chain
        val toToken = state?.input?.transfer?.token
        val toAddress = state?.input?.transfer?.address
        val fromAmount = state?.input?.transfer?.size?.usdcSize?.let {
            val decimals = 2
            (it * 10.0.pow(decimals)).toBigDecimal().toBigInteger()
        }
        val fromAmountString = parser.asString(fromAmount)
        if (toChain != null &&
            toToken != null &&
            toAddress != null &&
            fromAmount != null && fromAmount > 0 &&
            fromAmountString != null &&
            cosmoAddress != null
        ) {
            val params: IMap<String, String> = iMapOf<String, String>(
                "fromChain" to "dydxprotocol-testnet",
                "fromToken" to "ibc/39549F06486BACA7494C9ACDD53CDD30AA9E723AB657674DBD388F867B61CA7B",
                "fromAmount" to fromAmountString,
                "toChain" to toChain,
                "toToken" to toToken,
                "toAddress" to toAddress,
                "slippage" to "1",
                "enableForecall" to "true",
                "cosmosSignerAddress" to cosmoAddress.toString(),
            )

            val routeRequest = RestRequest.buildRestRequest(
                squidApiScheme(),
                squidApiHost(),
                squidApiPath("route"),
                params.toUrlParams(),
                HttpVerb.get,
                false,
                null,
                null
            )

            return routeRequest
        }
        return null
    }

    override fun activateRestRequests(): IList<RestRequest>? {
        val list = super.activateRestRequests()?.toIMutableList() ?: iMutableListOf()
        val indexerHeight = indexerHeightRequest()
        if (indexerHeight != null) {
            list.add(indexerHeight)
        }
        val validatorHeight = validatorHeightRequest()
        if (validatorHeight != null) {
            list.add(validatorHeight)
        }
        return list
    }

    override fun respond(
        state: PerpetualState?,
        changes: StateChanges?,
        errors: IList<ParsingError>?,
        networkRequests: NetworkRequests?
    ): AppStateResponse {
        val response = super.respond(state, changes, errors, networkRequests)
        val requests = iListOf<RestRequest?>(
            retrieveSubaccountsIfNeeded(state),
            indexerHeightRequest(),
            validatorHeightRequest()
        ).filterNotNull()

        return if (requests.isNotEmpty()) {
            val modifiedRestRequests =
                networkRequests?.restRequests?.toIMutableList() ?: iMutableListOf()
            modifiedRestRequests.addAll(requests)
            AppStateResponse(
                state,
                changes,
                errors,
                NetworkRequests(
                    networkRequests?.socketRequests,
                    modifiedRestRequests
                ),
                apiState(),
                lastOrder
            )
        } else response
    }

    override fun apiState(): ApiState? {
        return apiState
    }

    override fun updateLastOrder(state: PerpetualState?) {
        lastOrder = if (lastTransmittedOrderClientId != null) {
            val subaccountNumber = subaccountNumber()
            val subaccount = state?.subaccount(subaccountNumber)
            val order = subaccount?.orders?.firstOrNull { order ->
                val sameOrderId = (order.clientId == lastTransmittedOrderClientId)
                sameOrderId
            }
            order
        } else null
    }

    override fun transferStatus(hash: String): AppStateResponse {
        val networkRequests = iMutableListOf<RestRequest>()
        transferStatusRequest(hash)?.let {
            networkRequests.add(it)
        }
        return respond(
            stateMachine.state,
            null,
            null,
            NetworkRequests(
                null,
                networkRequests
            )
        )
    }

    override fun parseOnChainEquityTiers(payload: String): AppStateResponse {
        val stateResponse = stateMachine.parseOnChainEquityTiers(payload)
        return respond(
            stateResponse.state,
            stateResponse.changes,
            stateResponse.errors,
            null,
        )
    }

    override fun parseOnChainFeeTiers(payload: String): AppStateResponse {
        val stateResponse = stateMachine.parseOnChainFeeTiers(payload)
        return respond(
            stateResponse.state,
            stateResponse.changes,
            stateResponse.errors,
            null
        )
    }

    override fun parseOnChainUserFeeTier(payload: String): AppStateResponse {
        val stateResponse = stateMachine.parseOnChainUserFeeTier(payload)
        return respond(
            stateResponse.state,
            stateResponse.changes,
            stateResponse.errors,
            null
        )
    }

    override fun parseOnChainUserStats(payload: String): AppStateResponse {
        val stateResponse = stateMachine.parseOnChainUserStats(payload)
        return respond(
            stateResponse.state,
            stateResponse.changes,
            stateResponse.errors,
            null
        )
    }

    private fun transferStatusRequest(hash: String): RestRequest? {
        val params: IMap<String, String> = iMapOf<String, String>(
            "transactionId" to hash
        )

        val transferStatusRequest = RestRequest.buildRestRequest(
            squidApiScheme(),
            squidApiHost(),
            squidApiPath("status"),
            params.toUrlParams(),
            HttpVerb.get,
            false,
            null,
            null
        )

        return transferStatusRequest
    }
}
