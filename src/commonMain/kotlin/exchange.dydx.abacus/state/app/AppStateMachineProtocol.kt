package exchange.dydx.abacus.state.app

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.V3PrivateSignerProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.app.adaptors.AbUrl
import exchange.dydx.abacus.state.app.adaptors.HttpVerb
import exchange.dydx.abacus.state.app.adaptors.NetworkParam
import exchange.dydx.abacus.state.app.helper.ChainHelper
import exchange.dydx.abacus.state.app.signer.V3ApiKey
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.modal.ClosePositionInputField
import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.TransferInputField
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.ServerTime
import kollections.JsExport
import kollections.iMutableListOf
import kollections.toIMutableList
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class HistoricalPnlPeriod(val rawValue: String) {
    Period1d("1d"),
    Period7d("7d"),
    Period30d("30d"),
    Period90d("90d");

    companion object {
        operator fun invoke(rawValue: String) =
            HistoricalPnlPeriod.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class CandlesPeriod(val rawValue: String) {
    Period1m("1m"),
    Period5m("5m"),
    Period15m("15m"),
    Period30m("30m"),
    Period1h("1h"),
    Period4h("4h"),
    Period1d("1d");

    companion object {
        operator fun invoke(rawValue: String) =
            CandlesPeriod.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
enum class SocketRequestType(val rawValue: String) {
    SocketConnect("Connect"),
    SocketText("Text"),
    SocketClose("Close");

    companion object {
        operator fun invoke(rawValue: String) =
            SocketRequestType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class SigningRequest(
    val field: String,
    val data: String
) {

}

@JsExport
@Serializable
data class SocketRequest(
    val type: SocketRequestType,
    val url: AbUrl,
    val text: String?,
    val private: Boolean,
    val signingRequest: SigningRequest? = null
) {
}

@JsExport
@Serializable
data class RestRequest(
    val url: AbUrl,
    val verb: HttpVerb,
    val private: Boolean,
    val headers: IList<NetworkParam>?,
    val body: String?,
    val signingRequest: SigningRequest? = null
) {
    internal var timeStamp: Instant = ServerTime.now()

    companion object {
        internal fun buildRestRequest(
            scheme: String?,
            host: String?,
            path: String?,
            params: String? = null,
            verb: HttpVerb = HttpVerb.get,
            private: Boolean = false,
            headers: IMap<String, String>? = null,
            body: IMap<String, Any>? = null
        ): RestRequest? =
            buildRestRequestWithPort(scheme, host, null, path, params, verb, private, headers, body)

        internal fun buildRestRequestWithPort(
            scheme: String?,
            host: String?,
            port: Int? = null,
            path: String?,
            params: String? = null,
            verb: HttpVerb = HttpVerb.get,
            private: Boolean = false,
            headers: IMap<String, String>? = null,
            body: IMap<String, Any>? = null
        ): RestRequest? =
            if (host == null) {
                null
            } else {
                val scheme = scheme ?: "https"
                val bodyText = if (body != null) {
                    val encoder = exchange.dydx.abacus.utils.JsonEncoder()
                    encoder.encode(body)
                } else null
                val hostAndPort = if (port != null) "$host:$port" else host
                RestRequest(
                    AbUrl.fromString(if (params != null) "$scheme://$hostAndPort${path ?: ""}?$params" else "$scheme://$hostAndPort${path ?: ""}")
                        .validate(),
                    verb,
                    private,
                    NetworkParam.convert(headers),
                    bodyText
                )
            }
    }
}

/*
 * Rather than split these parameters out into individual array classes in AbArray, we can also
 * simply override equals and hashcode.
 */
@JsExport
@Serializable
data class NetworkRequests(
    val socketRequests: IList<SocketRequest>?,
    val restRequests: IList<RestRequest>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkRequests

        if (socketRequests != null) {
            if (other.socketRequests == null) return false
            if (socketRequests != other.socketRequests) return false
        } else if (other.socketRequests != null) return false
        if (restRequests != null) {
            if (other.restRequests == null) return false
            if (restRequests != other.restRequests) return false
        } else if (other.restRequests != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = socketRequests?.hashCode() ?: 0
        result = 31 * result + (restRequests?.hashCode() ?: 0)
        return result
    }

    fun add(
        socketRequests: IList<SocketRequest>?,
        restRequests: IList<RestRequest>?
    ): NetworkRequests {
        return if ((socketRequests?.size ?: 0) == 0 && (restRequests?.size ?: 0) == 0) this else {
            val modifiedSocketRequests = this.socketRequests?.toIMutableList() ?: iMutableListOf()
            val modifiedRestRequests = this.restRequests?.toIMutableList() ?: iMutableListOf()
            if (socketRequests != null) {
                modifiedSocketRequests.addAll(socketRequests)
            }
            if (restRequests != null) {
                modifiedRestRequests.addAll(restRequests)
            }
            NetworkRequests(
                modifiedSocketRequests,
                modifiedRestRequests
            )
        }
    }
}

@JsExport
@Serializable
enum class NetworkStatus(val rawValue: String) {
    UNKNOWN("UNKNOWN"),
    UNREACHABLE("UNREACHABLE"),
    HALTED("HALTED"),
    NORMAL("NORMAL");

    companion object {
        operator fun invoke(rawValue: String) =
            NetworkStatus.values().firstOrNull { it.rawValue == rawValue }
    }
}

internal class NetworkState() {
    var status: NetworkStatus = NetworkStatus.UNKNOWN
        private set
    var block: Int? = null
        private set
    private var blockTime: Instant? = null

    private var sameBlockCount: Int = 0
    private var failCount: Int = 0

    internal var time: Instant? = null

    /*
    requestTime and requestId are only here to keep old V4ApiAdatpor to compile. Remove after we retire V4ApiAdaptor
     */
    internal var requestTime: Instant? = null
    internal var requestId: Long? = null

    internal fun updateHeight(height: Int?, heightTime: Instant?) {
        time = ServerTime.now()
        if (height != null) {
            failCount = 0
            if (block != height) {
                block = height
                blockTime = heightTime
                sameBlockCount = 0
            } else {
                sameBlockCount += 1
            }
        } else {
            failCount += 1
        }
        updateStatus()
    }

    private fun updateStatus() {
        val time = time
        status = if (time != null) {
            if (failCount >= 3)
                NetworkStatus.UNREACHABLE
            else if (sameBlockCount >= 6)
                NetworkStatus.HALTED
            else if (block != null)
                NetworkStatus.NORMAL
            else
                NetworkStatus.UNKNOWN
        } else NetworkStatus.UNKNOWN

    }
}

@JsExport
@Serializable
enum class ApiStatus(val rawValue: String) {
    UNKNOWN("UNKNOWN"),
    VALIDATOR_DOWN("VALIDATOR_DOWN"),
    VALIDATOR_HALTED("VALIDATOR_HALTED"),
    INDEXER_DOWN("INDEXER_DOWN"),
    INDEXER_HALTED("INDEXER_HALTED"),
    INDEXER_TRAILING("INDEXER_TRAILING"),
    NORMAL("NORMAL");

    companion object {
        operator fun invoke(rawValue: String) =
            ApiStatus.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class ApiState(
    val status: ApiStatus?,
    val height: Int?,
    val haltedBlock: Int?,
    val trailingBlocks: Int?
) {
    fun abnormalState(): Boolean {
        return status == ApiStatus.INDEXER_DOWN || status == ApiStatus.INDEXER_HALTED || status == ApiStatus.VALIDATOR_DOWN || status == ApiStatus.VALIDATOR_HALTED
    }
}


@JsExport
@Serializable
data class AppStateResponse(
    val state: PerpetualState?,
    val changes: StateChanges?,
    val errors: IList<ParsingError>?,
    val networkRequests: NetworkRequests?,
    val apiState: ApiState?,
    val lastOrder: SubaccountOrder?
) {
}

@JsExport
@Serializable
enum class OrderbookGrouping(val rawValue: Int) {
    none(1),
    x10(10),
    x100(100),
    x1000(1000);

    companion object {
        operator fun invoke(rawValue: Int) =
            OrderbookGrouping.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
interface AppStateMachineProtocol {
    fun localizer(): LocalizerProtocol?
    val availableEnvironments: IList<SelectionOption>

    fun setEnvironment(environment: String?): AppStateResponse
    fun setChainId(chainId: String): AppStateResponse
    fun associatedEthereumChainId(): String?

    fun setReadyToConnect(readyToConnect: Boolean): AppStateResponse

    fun accountAddress(): String?
    fun subaccountNumber(): Int

    fun market(): String?
    fun setMarket(market: String?): AppStateResponse

    fun orderbookGrouping(): OrderbookGrouping
    fun setOrderbookGrouping(orderbookGrouping: OrderbookGrouping): AppStateResponse

    fun historicalPnlPeriod(): HistoricalPnlPeriod
    fun setHistoricalPnlPeriod(historicalPnlPeriod: HistoricalPnlPeriod): AppStateResponse
    fun updateHistoricalPnl(): AppStateResponse

    fun candlesResolution(): String
    fun setCandlesResolution(candlesResolution: String): AppStateResponse

    fun setSocketConnected(url: AbUrl, socketConnected: Boolean): AppStateResponse
    fun processSocketResponse(url: AbUrl, text: String): AppStateResponse
    fun processHttpResponse(url: AbUrl, text: String): AppStateResponse

    fun trade(data: String?, type: TradeInputField?): AppStateResponse
    fun closePosition(data: String?, type: ClosePositionInputField): AppStateResponse
    fun transfer(data: String?, type: TransferInputField?): AppStateResponse
    fun commit(): AppStateResponse

    fun faucet(amount: Int): AppStateResponse

    fun ping(): AppStateResponse

    fun currentEnvironment(): Environment?
}

@JsExport
interface V3AppStateMachineProtocol : AppStateMachineProtocol {
    fun signer(): V3PrivateSignerProtocol?
    fun setSigner(signer: V3PrivateSignerProtocol?)

    fun ethereumAddress(): String?
    fun setWalletEthereumAddress(
        ethereumAddress: String?,
        apiKey: V3ApiKey? = null
    ): AppStateResponse
}

@JsExport
@Serializable
data class V4PlaceOrderPayload(
    val clobPairId: Int,
    val side: String,
    val quantums: Int,
    val subticks: Int,
    val goodUntilTime: Double?,
    val clientId: Int,
    val timeInForce: String,
    val orderFlags: String,
    val reduceOnly: Boolean,
) {
}

@JsExport
@Serializable
data class V4SubaccountPlaceOrderPayload(
    val chainId: String,
    val address: String,
    val subaccountNumber: Int,
    val clobPairId: Int,
    val side: String,
    val quantums: Double,
    val subticks: Double,
    val goodUntilBlock: Int?,
    val goodUntilTime: Double?,
    val clientId: Int,
    val timeInForce: String,
    val orderFlags: String,
    val reduceOnly: Boolean,
    val clientMetadata: Int,
    val conditionType: String,
    val conditionalOrderTriggerSubticks: Double,
) {
}


@JsExport
@Serializable
data class V4SubaccountCancelOrderPayload(
    val chainId: String,
    val address: String,
    val subaccountNumber: Int,
    val clobPairId: Int,
    val clientId: Int,
    val orderFlags: String,
    val goodUntilBlock: Int?,
    val goodUntilTime: Double?,
) {
}

@JsExport
@Serializable
data class V4SubaccountPlaceOrderPayload2(
    val chainId: String,
    val address: String,
    val subaccountNumber: Int,
    val clobPairId: Int,
    val side: Int,
    val quantums: Double,
    val subticks: Double,
    val goodTilBlock: Int?,
    val goodTilBlockTime: Double?,
    val clientId: Int,
    val timeInForce: Int,
    val orderFlags: Int,
    val reduceOnly: Boolean,
    val clientMetadata: Int,
    val conditionType: Int,
    val conditionalOrderTriggerSubticks: Double,
) {
}


@JsExport
@Serializable
data class V4SubaccountCancelOrderPayload2(
    val chainId: String,
    val address: String,
    val subaccountNumber: Int,
    val clobPairId: Int,
    val clientId: Int,
    val orderFlags: Int,
    val goodTilBlock: Int?,
    val goodTilBlockTime: Double?
) {
}

@JsExport
interface V4AppStateMachineProtocol : AppStateMachineProtocol {
    val chainHelper: ChainHelper?
    fun cosmoAddress(): String?
    fun setWalletCosmoAddress(
        cosmoAddress: String?
    ): AppStateResponse

    fun setSubaccountNumber(subaccountNumber: Int): AppStateResponse
    fun placeOrderPayload(): V4SubaccountPlaceOrderPayload?
    fun closePositionPayload(): V4SubaccountPlaceOrderPayload?
    fun cancelOrderPayload(orderId: String): V4SubaccountCancelOrderPayload?

    fun placeOrderPayload2(): V4SubaccountPlaceOrderPayload2?
    fun closePositionPayload2(): V4SubaccountPlaceOrderPayload2?
    fun cancelOrderPayload2(orderId: String): V4SubaccountCancelOrderPayload2?
    fun transaction(signedTransaction: String): AppStateResponse
    fun transferStatus(hash: String): AppStateResponse

    fun parseOnChainEquityTiers(payload: String): AppStateResponse
    fun parseOnChainFeeTiers(payload: String): AppStateResponse
    fun parseOnChainUserFeeTier(payload: String): AppStateResponse
    fun parseOnChainUserStats(payload: String): AppStateResponse
}
