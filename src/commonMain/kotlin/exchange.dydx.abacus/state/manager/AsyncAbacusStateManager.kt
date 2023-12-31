package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.Documentation
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.FileLocation
import exchange.dydx.abacus.protocols.LocalizerProtocol
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
import exchange.dydx.abacus.utils.DebugLogger
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
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

@JsExport
class AppConfigs(
    val subscribeToCandles: Boolean,
    val loadRemote: Boolean = true,
    val enableLogger: Boolean = false,
) {
    enum class SquidVersion {
        V1, V2, V2DepositOnly, V2WithdrawalOnly,
    }
    var squidVersion: SquidVersion = SquidVersion.V1

    companion object {
        val forApp = AppConfigs(subscribeToCandles = true, loadRemote = true)
        val forAppDebug = AppConfigs(subscribeToCandles = true, loadRemote = false, enableLogger = true)
        val forWeb = AppConfigs(subscribeToCandles = false, loadRemote = true)
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
enum class HistoricaTradingRewardsPeriod(val rawValue: String) {
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    BLOCK("BLOCK");

    companion object {
        operator fun invoke(rawValue: String) = 
        HistoricaTradingRewardsPeriod.values().firstOrNull { it.rawValue == rawValue }
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

public class BlockAndTime(val block: Int, val time: Instant)

internal class NetworkState() {
    var status: NetworkStatus = NetworkStatus.UNKNOWN
        private set
    var blockAndTime: BlockAndTime? = null
        private set
    private var sameBlockCount: Int = 0
    private var failCount: Int = 0

    internal var time: Instant? = null

    internal var previousRequestTime: Instant? = null
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
enum class ApiData {
    HISTORICAL_PNLS,
    HISTORICAL_TRADING_REWARDS,
}

@JsExport
enum class ConfigFile(val rawValue: String) {
    DOCUMENTATION("DOCUMENTATION") {
        override val path: String
            get() = "/configs/documentation.json"
    },
    ENV("ENV") {
        override val path: String
            get() = "/configs/env.json"
    };

    abstract val path: String
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
    init {
        if (appConfigs.enableLogger) {
            DebugLogger.enable()
        }
    }

    private val environmentsFile = ConfigFile.ENV
    private val documentationFile = ConfigFile.DOCUMENTATION

    private var _appSettings: AppSettings? = null

    val appSettings: AppSettings?
        get() = _appSettings

    private var environments: IList<V4Environment> = iListOf()
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                _environment = findEnvironment(environmentId)
                ioImplementations.threading?.async(ThreadingType.main) {
                    stateNotification?.environmentsChanged()
                    dataNotification?.environmentsChanged()
                }
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

    var documentation: Documentation? = null
        private set

    var adaptor: StateManagerAdaptor? = null
        private set(value) {
            if (field !== value) {
                field?.dispose()

                value?.market = market
                value?.accountAddress = accountAddress
                value?.sourceAddress = sourceAddress
                value?.subaccountNumber = subaccountNumber
                value?.orderbookGrouping = orderbookGrouping
                value?.historicalTradingRewardPeriod = historicalTradingRewardPeriod
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
    
    var historicalTradingRewardPeriod: HistoricaTradingRewardsPeriod = HistoricaTradingRewardsPeriod.WEEKLY
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.historicalTradingRewardPeriod = field
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
        ConfigFile.values().forEach {
            load(it)
        }
    }

    private fun load(configFile: ConfigFile) {
        val path = configFile.path
        if (appConfigs.loadRemote) {
            loadFromRemoteConfigFile(configFile)
            val configFileUrl = "$deploymentUri$path"
            ioImplementations.rest?.get(configFileUrl, null, callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    if (parse(response, configFile)) {
                        writeToLocalFile(response, path)
                    }
                }
            })
        } else {
            loadFromBundledLocalConfigFile(configFile)
        }
    }

    private fun loadFromRemoteConfigFile(configFile: ConfigFile) {
        ioImplementations.fileSystem?.readCachedTextFile(
            configFile.path
        )?.let {
            parse(it, configFile)
        }
    }

    private fun loadFromBundledLocalConfigFile(configFile: ConfigFile) {
        ioImplementations.fileSystem?.readTextFile(
            FileLocation.AppBundle,
            configFile.path,
        )?.let {
            parse(it, configFile)
        }
    }

    private fun parse(response: String, configFile: ConfigFile): Boolean {
        return when (configFile) {
            ConfigFile.DOCUMENTATION -> {
                parseDocumentation(response)
                return true
            }
            ConfigFile.ENV -> parseEnvironments(response)
        }
    }

    private fun success(httpCode: Int): Boolean {
        return httpCode in 200..299
    }

    private fun writeToLocalFile(response: String, file: String) {
        ioImplementations.fileSystem?.writeTextFile(
            file,
            response
        )
    }

    private fun parseDocumentation(response: String) {
        this.documentation = Json.decodeFromString<Documentation>(response)
    }

    private fun parseEnvironments(response: String): Boolean {
        val parser = Parser()
        val items = parser.decodeJsonObject(response)
        val deployments = parser.asMap(items?.get("deployments")) ?: return false
        val target = parser.asMap(deployments[deployment]) ?: return false
        val targetEnvironments = parser.asList(target["environments"]) ?: return false
        val targetDefault = parser.asString(target["default"])

        if (items != null) {
            val environmentsData = parser.asMap(items["environments"]) ?: return false
            val parsedEnvironments = mutableMapOf<String, V4Environment>()
            for ((key, value) in environmentsData) {
                val data = parser.asMap(value) ?: continue
                val environment = V4Environment.parse(key, data, parser, deploymentUri, uiImplementations.localizer) ?: continue
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

            val apps = parser.asMap(items["apps"])
            if (apps != null) {
                this._appSettings = AppSettings.parse(apps, parser)
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

    fun transferStatus(hash: String, fromChainId: String?, toChainId: String?, isCctp: Boolean) {
        adaptor?.transferStatus(hash, fromChainId, toChainId, isCctp)
    }

    fun refresh(data: ApiData) {
        adaptor?.refresh(data)
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
        return try {
            adaptor?.commitPlaceOrder(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
            null
        }
    }

    fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        return try {
            adaptor?.commitClosePosition(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
            null
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

    fun commitCCTPWithdraw(callback: TransactionCallback) {
        try {
            adaptor?.commitCCTPWithdraw(callback)
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
