package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.readCachedTextFile
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.app.helper.DynamicLocalizer
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.modal.ClosePositionInputField
import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.TransferInputField
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.DummyFormatter
import exchange.dydx.abacus.utils.DummyLocalizer
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ProtocolNativeImpFactory
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.Threading
import exchange.dydx.abacus.utils.UIImplementations
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIList
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@JsExport
data class IndexerURIs(
    val api: String,
    val socket: String
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
        ): IndexerURIs? {
            val api = parser.asString(data["api"]) ?: return null
            val socket = parser.asString(data["socket"]) ?: return null
            return IndexerURIs(api, socket)
        }
    }
}

@JsExport
data class EnvironmentEndpoints(
    val indexers: IList<IndexerURIs>?,
    val validators: IList<String>?,
    val faucet: String?,
    val squid: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
        ): EnvironmentEndpoints? {
            val indexers = parser.asList(data["indexers"])?.mapNotNull {
                IndexerURIs.parse(parser.asMap(it) ?: return null, parser)
            }?.toIList() ?: return null
            val validators = parser.asList(data["validators"])?.mapNotNull {
                parser.asString(it)
            }?.toIList() ?: return null
            val faucet = parser.asString(data["faucet"])
            val squid = parser.asString(data["0xsquid"])
            return EnvironmentEndpoints(indexers, validators, faucet, squid)
        }
    }
}

@JsExport
data class EnvironmentLinks(
    val tos: String?,
    val privacy: String?,
    val mintscan: String?,
    val documentation: String?,
    val community: String?,
    val feedback: String?,
    val blogs: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
        ): EnvironmentLinks? {
            val tos = parser.asString(data["tos"])
            val privacy = parser.asString(data["privacy"])
            val mintscan = parser.asString(data["mintscan"])
            val documentation = parser.asString(data["documentation"])
            val community = parser.asString(data["community"])
            val feedback = parser.asString(data["feedback"])
            val blogs = parser.asString(data["blogs"])
            return EnvironmentLinks(
                tos,
                privacy,
                mintscan,
                documentation,
                community,
                feedback,
                blogs
            )
        }
    }
}

@JsExport
data class TokenInfo(
    val name: String,
    val denom: String,
    val gasDenom: String?,
    val imageUrl: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>,
            parser: ParserProtocol,
        ): TokenInfo? {
            val name = parser.asString(data["name"]) ?: return null
            val denom = parser.asString(data["denom"]) ?: return null
            val gasDenom = parser.asString(data["gasDenom"])
            val imageUrl = parser.asString(data["imageUrl"])
            return TokenInfo(name, denom, gasDenom, imageUrl)
        }
    }
}

@JsExport
data class WalletConnectClient(
    val name: String,
    val description: String,
    val iconUrl: String?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): WalletConnectClient? {
            val name = parser.asString(data?.get("name")) ?: return null
            val description = parser.asString(data?.get("description")) ?: return null
            val iconUrl = parser.asString(data?.get("iconUrl"))
            return WalletConnectClient(name, description, "$deploymentUri$iconUrl")
        }
    }
}

@JsExport
data class WalletConnectV1(
    val bridgeUrl: String,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
        ): WalletConnectV1? {
            val bridgeUrl = parser.asString(data?.get("bridgeUrl")) ?: return null
            return WalletConnectV1(bridgeUrl)
        }
    }
}

@JsExport
data class WalletConnectV2(
    val projectId: String,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
        ): WalletConnectV2? {
            val projectId = parser.asString(data?.get("projectId")) ?: return null
            return WalletConnectV2(projectId)
        }
    }
}

@JsExport
data class WalletConnect(
    val client: WalletConnectClient,
    val v1: WalletConnectV1?,
    val v2: WalletConnectV2?,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): WalletConnect? {
            val client =
                WalletConnectClient.parse(parser.asMap(data?.get("client")), parser, deploymentUri)
                    ?: return null
            val v1 = WalletConnectV1.parse(parser.asMap(data?.get("v1")), parser)
            val v2 = WalletConnectV2.parse(parser.asMap(data?.get("v2")), parser)
            return if (v1 != null || v2 != null)
                WalletConnect(client, v1, v2)
            else null
        }
    }
}

@JsExport
data class WalletSegue(
    val callbackUrl: String,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): WalletSegue? {
            val callbackUrl = parser.asString(data?.get("callbackUrl")) ?: return null
            return WalletSegue("$deploymentUri$callbackUrl")
        }
    }
}

@JsExport
data class WalletConnection(
    val walletConnect: WalletConnect?,
    val walletSegue: WalletSegue?,
    val images: String,
) {
    companion object {
        fun parse(
            data: Map<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): WalletConnection? {
            val walletConnect =
                WalletConnect.parse(
                    parser.asMap(data?.get("walletConnect"))
                        ?: parser.asMap(data?.get("walletconnect")),
                    parser,
                    deploymentUri
                )
            val walletSegue =
                WalletSegue.parse(parser.asMap(data?.get("walletSegue")), parser, deploymentUri)
            val images = parser.asString(data?.get("images")) ?: return null
            return if (walletConnect != null || walletSegue != null)
                WalletConnection(walletConnect, walletSegue, "$deploymentUri$images")
            else null
        }
    }
}


@JsExport
open class Environment(
    val id: String,
    val name: String?,
    val ethereumChainId: String,
    val dydxChainId: String?,
    val isMainNet: Boolean,
    val endpoints: EnvironmentEndpoints,
    val links: EnvironmentLinks?,
    val walletConnection: WalletConnection?,
)

@JsExport
class V4Environment(
    id: String,
    name: String?,
    ethereumChainId: String,
    dydxChainId: String?,
    isMainNet: Boolean,
    endpoints: EnvironmentEndpoints,
    links: EnvironmentLinks?,
    walletConnection: WalletConnection?,
    val tokens: IMap<String, TokenInfo>,
) : Environment(
    id,
    name,
    ethereumChainId,
    dydxChainId,
    isMainNet,
    endpoints,
    links,
    walletConnection,
) {
    companion object {
        fun parse(
            id: String,
            data: Map<String, Any>,
            parser: ParserProtocol,
            deploymentUri: String,
        ): V4Environment? {
            val name = parser.asString(data["name"])
            val ethereumChainId = parser.asString(data["ethereumChainId"]) ?: return null
            val dydxChainId = parser.asString(data["dydxChainId"])
            val isMainNet = parser.asBool(data["isMainNet"]) ?: return null
            val endpoints =
                EnvironmentEndpoints.parse(parser.asMap(data["endpoints"]) ?: return null, parser)
                    ?: return null
            val links = EnvironmentLinks.parse(parser.asMap(data["links"]) ?: return null, parser)
            val walletConnection = WalletConnection.parse(
                parser.asMap(data["walletConnection"])
                    ?: parser.asMap(data["wallets"]),
                parser,
                deploymentUri
            )
            val tokens = parseTokens(parser.asMap(data["tokens"]), parser, deploymentUri)

            return V4Environment(
                id,
                name,
                ethereumChainId,
                dydxChainId,
                isMainNet,
                endpoints,
                links,
                walletConnection,
                tokens
            )
        }

        private fun parseTokens(
            item: IMap<String, Any>?,
            parser: ParserProtocol,
            deploymentUri: String,
        ): IMap<String, TokenInfo> {
            val tokens = iMutableMapOf<String, TokenInfo>()
            if (item != null) {
                for (key in item.keys) {
                    val token = parser.asMap(item[key])
                    if (token != null) {
                        val name = parser.asString(token["name"]) ?: continue
                        val denom = parser.asString(token["denom"]) ?: continue
                        val gasDenom = parser.asString(token["gasDenom"])
                        val imageUrl = parser.asString(token["image"])?.let {
                            "$deploymentUri$it"
                        }
                        tokens[key] = TokenInfo(name, denom, gasDenom, imageUrl)
                    }
                }
            }

            return tokens
        }
    }
}

@JsExport
class AppConfigs(val subscribeToCandles: Boolean, val loadRemote: Boolean = true) {
    companion object {
        val forApp = AppConfigs(true, true)
        val forAppDebug = AppConfigs(true, false)
        val forWeb = AppConfigs(false, true)
    }
}

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

internal class BlockAndTime(val block: Int, val time: Instant)

internal class NetworkState() {
    var status: NetworkStatus = NetworkStatus.UNKNOWN
        private set
    var blockAndTime: BlockAndTime? = null
        private set
    private var sameBlockCount: Int = 0
    private var failCount: Int = 0

    internal var time: Instant? = null

    /*
    requestTime and requestId are only here to keep old V4ApiAdatpor to compile. Remove after we retire V4ApiAdaptor
     */
    internal var requestTime: Instant? = null

    internal fun updateHeight(height: Int?, heightTime: Instant?) {
        time = ServerTime.now()
        if (height != null && heightTime != null) {
            failCount = 0
            if (blockAndTime?.block != height) {
                blockAndTime = BlockAndTime(height, heightTime)
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
            else if (blockAndTime != null)
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
class AsyncAbacusStateManager(
    val deploymentUri: String,
    val deployment: String, // MAINNET, TESTNET, DEV
    val appConfigs: AppConfigs,
    val ioImplementations: IOImplementations,
    val uiImplementations: UIImplementations,
    val stateNotification: StateNotificationProtocol? = null,
    val dataNotification: DataNotificationProtocol? = null
) {
    private val environmentsFile = "/configs/env.json"

    private var environments: IList<V4Environment> = iListOf()
        set(value) {
            field = value
            stateNotification?.environmentsChanged()
            dataNotification?.environmentsChanged()
            ioImplementations.threading?.async(ThreadingType.abacus) {
                _environment = findEnvironment(environmentId)
            }
        }

    val availableEnvironments: IList<SelectionOption>
        get() = environments.map { environment ->
            SelectionOption(environment.id, environment.name, null, null)
        }

    var environmentId: String? = null
        set(value) {
            if (field != value) {
                field = value
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    _environment = findEnvironment(environmentId)
                }
            }
        }

    private var _environment: V4Environment? = null
        set(value) {
            if (field !== value) {
                field = value
                reconnect()
            }
        }

    val environment: V4Environment?
        get() = _environment


    var adaptor: StateManagerAdaptor? = null
        private set(value) {
            if (field !== value) {
                field?.dispose()

                value?.market = market
                value?.accountAddress = accountAddress
                value?.sourceAddress = sourceAddress
                value?.subaccountNumber = subaccountNumber
                value?.orderbookGrouping = orderbookGrouping
                value?.historicalPnlPeriod = historicalPnlPeriod
                value?.candlesResolution = candlesResolution
                value?.readyToConnect = readyToConnect
                field = value
            }
        }

    var readyToConnect: Boolean = false
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.readyToConnect = field
            }
        }

    var market: String? = null
        set(value) {
            if (isMarketValid(value) && field != value) {
                field = value
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    adaptor?.market = field
                }
            }
        }

    var accountAddress: String? = null
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.accountAddress = field
            }
        }

    var sourceAddress: String? = null
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.sourceAddress = field
            }
        }

    var subaccountNumber: Int = 0
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.subaccountNumber = field
            }
        }

    var orderbookGrouping: OrderbookGrouping = OrderbookGrouping.none
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.orderbookGrouping = field
            }
        }

    var historicalPnlPeriod: HistoricalPnlPeriod = HistoricalPnlPeriod.Period7d
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.historicalPnlPeriod = field
            }
        }

    var candlesResolution: String = "1DAY"
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.candlesResolution = field
            }
        }

    companion object {
        private fun createIOImplementions(_nativeImplementations: ProtocolNativeImpFactory): IOImplementations {
            return IOImplementations(
                rest = _nativeImplementations.rest,
                webSocket = _nativeImplementations.webSocket,
                chain = _nativeImplementations.chain,
                tracking = _nativeImplementations.tracking,
                threading = _nativeImplementations.threading ?: Threading(),
                timer = _nativeImplementations.timer ?: CoroutineTimer(),
                fileSystem = _nativeImplementations.fileSystem,
            )
        }

        private fun createUIImplemention(_nativeImplementations: ProtocolNativeImpFactory): UIImplementations {
            return UIImplementations(
                localizer = _nativeImplementations.localizer ?: DummyLocalizer(),
                formatter = _nativeImplementations.formatter ?: DummyFormatter(),
            )
        }
    }

    init {
        if (ioImplementations.rest === null) {
            throw Error("IOImplementations.rest is not set")
        }
        if (ioImplementations.webSocket === null) {
            throw Error("IOImplementations.webSocket is not set")
        }
        if (ioImplementations.chain === null) {
            throw Error("IOImplementations.chain is not set")
        }
        if (ioImplementations.threading === null) {
            throw Error("IOImplementations.threading is not set")
        }
        if (ioImplementations.timer === null) {
            throw Error("IOImplementations.timer is not set")
        }
        if (uiImplementations.localizer === null) {
            throw Error("UIImplementations.localizer is not set")
        }
//        if (UIImplementations.formatter === null) {
//            throw Error("UIImplementations.formatter is not set")
//        }
        if (uiImplementations.localizer is DynamicLocalizer) {
            if (ioImplementations.fileSystem === null) {
                throw Error("IOImplementations.fileSystem is not set, used by Abacus localizer")
            }
        }
        if (stateNotification === null && dataNotification === null) {
            throw Error("Either stateNotification or dataNotification need to be set")
        }
        loadEnvironments()
    }

    private fun loadEnvironments() {
        loadEnvironmentsFromLocalFile()
        if (appConfigs.loadRemote) {
            val environmentsUrl = "$deploymentUri$environmentsFile"
            ioImplementations.rest?.get(environmentsUrl, null, callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    val parser = Parser()
                    val json = parser.asMap(Json.parseToJsonElement(response).jsonObject)
                    if (parseEnvironments(json, parser)) {
                        writeEnvironmentsToLocalFile(response)
                    }
                }
            })
        }
    }

    private fun loadEnvironmentsFromLocalFile() {
        ioImplementations.fileSystem?.readCachedTextFile(
            environmentsFile
        )?.let { response ->
            val parser = Parser()
            val json = parser.asMap(Json.parseToJsonElement(response).jsonObject)
            parseEnvironments(json, parser)
        }
    }

    private fun success(httpCode: Int): Boolean {
        return httpCode in 200..299
    }

    private fun writeEnvironmentsToLocalFile(response: String) {
        ioImplementations.fileSystem?.writeTextFile(
            environmentsFile,
            response
        )
    }


    private fun parseEnvironments(items: IMap<String, Any>?, parser: ParserProtocol): Boolean {
        val deployments = parser.asMap(items?.get("deployments")) ?: return false
        val target = parser.asMap(deployments[deployment]) ?: return false
        val targetEnvironments = parser.asList(target["environments"]) ?: return false
        val targetDefault = parser.asString(target["default"])

        if (items != null) {
            val environmentsData = parser.asMap(items["environments"]) ?: return false
            val parsedEnvironments = mutableMapOf<String, V4Environment>()
            for ((key, value) in environmentsData) {
                val data = parser.asMap(value) ?: continue
                val environment = V4Environment.parse(key, data, parser, deploymentUri) ?: continue
                parsedEnvironments[environment.id] = environment
            }
            if (parsedEnvironments.isEmpty()) {
                return false
            }
            val environments = iMutableListOf<V4Environment>()
            for (environmentId in targetEnvironments) {
                val environment = parsedEnvironments[parser.asString(environmentId)!!]
                if (environment != null) {
                    environments.add(environment)
                }
            }

            this.environments = environments
            if (targetDefault != null && this.environmentId == null) {
                this.environmentId = targetDefault
            }
            return true
        } else {
            return false
        }
    }


    private fun findEnvironment(environment: String?): V4Environment? {
        return environments.firstOrNull { it ->
            it.id == environment
        }
    }

    private fun reconnect() {
        val environment = environment
        if (environment != null) {
            adaptor = V4StateManagerAdaptor(
                deploymentUri,
                environment,
                ioImplementations,
                uiImplementations,
                V4StateManagerConfigs(deploymentUri, environment),
                appConfigs,
                stateNotification,
                dataNotification,
            )
        }
    }

    fun setAddresses(source: String?, account: String?) {
        sourceAddress = source
        accountAddress = account
    }

    fun trade(data: String?, type: TradeInputField?) {
        adaptor?.trade(data, type)
    }

    fun closePosition(data: String?, type: ClosePositionInputField) {
        adaptor?.closePosition(data, type)
    }

    fun transfer(data: String?, type: TransferInputField?) {
        adaptor?.transfer(data, type)
    }

    fun isMarketValid(marketId: String?): Boolean {
        return if (marketId == null) {
            true
        } else {
            val market = adaptor?.stateMachine?.state?.market(marketId)
            (market?.status?.canTrade == true || market?.status?.canReduce == true)
        }
    }

    fun transferStatus(hash: String, fromChainId: String?, toChainId: String?) {
        adaptor?.transferStatus(hash, fromChainId, toChainId)
    }

    fun placeOrderPayload(): HumanReadablePlaceOrderPayload? {
        return adaptor?.placeOrderPayload()
    }

    fun closePositionPayload(): HumanReadablePlaceOrderPayload? {
        return adaptor?.closePositionPayload()
    }

    fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload? {
        return adaptor?.cancelOrderPayload(orderId)
    }

    fun depositPayload(): HumanReadableDepositPayload? {
        return adaptor?.depositPayload()
    }

    fun withdrawPayload(): HumanReadableWithdrawPayload? {
        return adaptor?.withdrawPayload()
    }

    fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload? {
        return adaptor?.subaccountTransferPayload()
    }

    fun commitPlaceOrder(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        try {
            return adaptor?.commitPlaceOrder(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
            return null
        }
    }

    fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        try {
            return adaptor?.commitClosePosition(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
            return null
        }
    }

    fun stopWatchingLastOrder() {
        adaptor?.stopWatchingLastOrder()
    }

    fun commitTransfer(callback: TransactionCallback) {
        try {
            adaptor?.commitTransfer(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
        }
    }

    fun faucet(amount: Double, callback: TransactionCallback) {
        try {
            adaptor?.faucet(amount, callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
        }
    }

    fun cancelOrder(orderId: String, callback: TransactionCallback) {
        try {
            adaptor?.cancelOrder(orderId, callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
        }
    }

    internal fun parseTransactionResponse(response: String?): ParsingError? {
        return adaptor?.parseTransactionResponse(response)
    }

    // Bridge functions.
    // If client is not using cancelOrder function, it should call orderCanceled function with
    // payload from v4-client to process state
    fun orderCanceled(orderId: String) {
        adaptor?.orderCanceled(orderId)
    }

    fun parseFaucetResponse(
        response: String,
        amount: Double,
        submitTimeInMilliseconds: Double
    ): ParsingError? {
        return adaptor?.parseFaucetResponse(
            response,
            subaccountNumber,
            amount,
            submitTimeInMilliseconds
        )
    }

    fun screen(address: String, callback: (restriction: Restriction) -> Unit) {
        adaptor?.screen(address, callback)
    }
}
