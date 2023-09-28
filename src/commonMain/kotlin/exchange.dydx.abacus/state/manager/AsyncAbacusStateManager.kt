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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@JsExport
data class IndexerURIs(
    val api: String,
    val socket: String
)

@JsExport
data class EnvironmentEndpoints(
    val indexers: IList<IndexerURIs>?,
    val validators: IList<String>?,
    val faucet: String?,
    val squid: String?,
)

@JsExport
data class EnvironmentLinks(
    val tos: String?,
    val privacy: String?,
    val mintscan: String?,
    val documentation: String?,
    val community: String?,
    val feedback: String?,
)

@JsExport
data class TokenInfo(
    val name: String,
    val denom: String,
    val imageUrl: String?,
)

@JsExport
open class Environment(
    val id: String,
    val name: String?,
    val ethereumChainId: String,
    val dydxChainId: String?,
    val isMainNet: Boolean,
) {
}

@JsExport
class V4Environment(
    id: String,
    name: String?,
    ethereumChainId: String,
    dydxChainId: String?,
    isMainNet: Boolean,
    val tokens: IMap<String, TokenInfo>,
    val endpoints: EnvironmentEndpoints,
    val links: EnvironmentLinks?,
) : Environment(
    id,
    name,
    ethereumChainId,
    dydxChainId,
    isMainNet,
)

@JsExport
class AppConfigs(val subscribeToCandles: Boolean) {
    companion object {
        val forApp = AppConfigs(true)
        val forWeb = AppConfigs(false)
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
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.market = field
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
        val environmentsUrl = "$deploymentUri$environmentsFile"
        ioImplementations.rest?.get(environmentsUrl, null, callback = { response, httpCode ->
            if (success(httpCode) && response != null) {
                val parser = Parser()
                val json = parser.asMap(Json.parseToJsonElement(response).jsonObject)
                if (!parseEnvironments(json, parser)) {
                    loadEnvironmentsFromLocalFile()
                }
            } else {
                loadEnvironmentsFromLocalFile()
            }
        })
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


    private fun parseEnvironments(items: IMap<String, Any>?, parser: ParserProtocol): Boolean {
        val deployments = parser.asMap(items?.get("deployments")) ?: return false
        val target = parser.asMap(deployments[deployment]) ?: return false
        val targetEnvironments = parser.asList(target["environments"]) ?: return false
        val targetDefault = parser.asString(target["default"])

        if (items != null) {
            val environmentsData = parser.asList(items["environments"]) ?: return false
            val parsedEnvironments = mutableMapOf<String, V4Environment>()
            for (item in environmentsData) {
                val environment = parseEnvironment(parser.asMap(item), parser)
                if (environment != null) {
                    parsedEnvironments[environment.id] = environment
                }
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

    private fun parseEnvironment(item: IMap<String, Any>?, parser: ParserProtocol): V4Environment? {
        if (item == null) {
            return null
        }
        val id = parser.asString(item["id"]) ?: return null
        val name = parser.asString(item["name"]) ?: id
        val ethereumChainId = parser.asString(item["ethereumChainId"]) ?: return null
        val dydxChainId = parser.asString(item["dydxChainId"])
        val isMainNet = parser.asBool(item["isMainNet"]) ?: false
        val tokens = parseTokens(parser.asMap(item["tokens"]), parser)
        val endpoints =
            parseEnviromentEndpoints(parser.asMap(item["endpoints"]), parser) ?: return null
        val links = parseEnviromentLinks(parser.asMap(item["links"]), parser) // Links are optional

        return V4Environment(
            id,
            name,
            ethereumChainId,
            dydxChainId,
            isMainNet,
            tokens,
            endpoints,
            links,
        )
    }


    private fun parseTokens(
        item: IMap<String, Any>?,
        parser: ParserProtocol,
    ): IMap<String, TokenInfo> {
        val tokens = iMutableMapOf<String, TokenInfo>()
        if (item != null) {
            for (key in item.keys) {
                val token = parser.asMap(item[key])
                if (token != null) {
                    val name = parser.asString(token["name"]) ?: continue
                    val denom = parser.asString(token["denom"]) ?: continue
                    val imageUrl = parser.asString(token["image"])?.let {
                        "$deploymentUri$it"
                    }
                    tokens[key] = TokenInfo(name, denom, imageUrl)
                }
            }
        }

        return tokens
    }

    private fun parseEnviromentEndpoints(
        item: IMap<String, Any>?,
        parser: ParserProtocol,
    ): EnvironmentEndpoints? {
        if (item == null) {
            return null
        }
        val indexers =
            parser.asList(item["indexers"])?.map { parseIndexerURIs(parser.asMap(it), parser) }
                ?.filterNotNull()?.toIList()
        val validators =
            parser.asList(item["validators"])?.map { parser.asString(it) }?.filterNotNull()
                ?.toIList()
        val faucet = parser.asString(item["faucet"])
        val squid = parser.asString(item["0xsquid"])

        return EnvironmentEndpoints(
            indexers,
            validators,
            faucet,
            squid,
        )
    }

    private fun parseEnviromentLinks(
        item: IMap<String, Any>?,
        parser: ParserProtocol,
    ): EnvironmentLinks? {
        if (item == null) {
            return null
        }
        val tos = parser.asString(item["tos"])
        val privacy = parser.asString(item["privacy"])
        val mintscan = parser.asString(item["mintscan"])
        val documentation = parser.asString(item["documentation"])
        val community = parser.asString(item["community"])
        val feedback = parser.asString(item["feedback"])

        return EnvironmentLinks(
            tos,
            privacy,
            mintscan,
            documentation,
            community,
            feedback,
        )
    }

    private fun parseIndexerURIs(
        item: IMap<String, Any>?,
        parser: ParserProtocol,
    ): IndexerURIs? {
        if (item == null) {
            return null
        }
        val api = parser.asString(item["api"]) ?: return null
        val socket = parser.asString(item["socket"]) ?: return null
        return IndexerURIs(api, socket)
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
