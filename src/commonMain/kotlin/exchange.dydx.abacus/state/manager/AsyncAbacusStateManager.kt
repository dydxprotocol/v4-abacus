package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.V3PrivateSignerProtocol
import exchange.dydx.abacus.protocols.readCachedTextFile
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.app.AppVersion
import exchange.dydx.abacus.state.app.EnvironmentURIs
import exchange.dydx.abacus.state.app.HistoricalPnlPeriod
import exchange.dydx.abacus.state.app.IndexerURIs
import exchange.dydx.abacus.state.app.OrderbookGrouping
import exchange.dydx.abacus.state.app.V4Environment
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.app.helper.DynamicLocalizer
import exchange.dydx.abacus.state.app.signer.V3ApiKey
import exchange.dydx.abacus.state.manager.configs.V3StateManagerConfigs
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.modal.ClosePositionInputField
import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.TransferInputField
import exchange.dydx.abacus.state.modal.feeTiers
import exchange.dydx.abacus.state.modal.receivedFeeTiers
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.DummyFormatter
import exchange.dydx.abacus.utils.DummyLocalizer
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ProtocolNativeImpFactory
import exchange.dydx.abacus.utils.Threading
import exchange.dydx.abacus.utils.UIImplementations
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.js.JsName

@JsExport
class AsyncAbacusStateManager(
    val environmentsUrl: String,
    val environmentsFile: String,
    val ioImplementations: IOImplementations,
    val uiImplementations: UIImplementations,
    val stateNotification: StateNotificationProtocol? = null,
    val dataNotification: DataNotificationProtocol? = null
) {
    private var v3signer: V3PrivateSignerProtocol? = null
    private var apiKey: V3ApiKey? = null

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
            SelectionOption(environment.environment, environment.stringKey, null)
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
        val environments = iMutableListOf<V4Environment>()
        if (items != null) {
            val environmentsData = parser.asList(items["environments"]) ?: return false
            for (item in environmentsData) {
                val environment = parseEnvironment(parser.asMap(item), parser)
                if (environment != null) {
                    environments.add(environment)
                }
            }
            if (environments.isEmpty()) {
                return false
            }
            this.environments = environments
            val defaultEnvironment = parser.asString(items["defaultEnvironment"])
            if (defaultEnvironment != null && this.environmentId == null) {
                this.environmentId = defaultEnvironment
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
        val environment = parser.asString(item["environment"]) ?: return null
        val ethereumChainId = parser.asString(item["ethereumChainId"]) ?: return null
        val dydxChainId = parser.asString(item["dydxChainId"])
        val stringKey = parser.asString(item["stringKey"]) ?: return null
        val string = parser.asString(item["string"]) ?: return null
        val isMainNet = parser.asBool(item["isMainNet"]) ?: false
        val versionValue = parser.asString(item["version"]) ?: return null
        val version = AppVersion.invoke(versionValue) ?: return null
        val maxSubaccountNumber = parser.asInt(item["maxSubaccountNumber"]) ?: 0
        val URIs: EnvironmentURIs =
            parseEnviromentURIs(parser.asMap(item["endpoints"]), parser) ?: return null

        return V4Environment(
            environment,
            ethereumChainId,
            dydxChainId,
            stringKey,
            string,
            isMainNet,
            version,
            maxSubaccountNumber,
            URIs
        )
    }

    private fun parseEnviromentURIs(
        item: IMap<String, Any>?,
        parser: ParserProtocol,
    ): EnvironmentURIs? {
        if (item == null) {
            return null
        }
        val indexers =
            parser.asList(item["indexers"])?.map { parseIndexerURIs(parser.asMap(it), parser) }
                ?.filterNotNull()?.toIList()
        val configs = parser.asString(item["configs"])
        val validators =
            parser.asList(item["validators"])?.map { parser.asString(it) }?.filterNotNull()
                ?.toIList()
        val faucet = parser.asString(item["faucet"])
        val squid = parser.asString(item["0xsquid"])
        val statusPageUrl = parser.asString(item["statusPageUrl"])
        val marketImageUrl = parser.asString(item["marketImageUrl"])
        val tosUrl = parser.asString(item["tosUrl"])
        val privacyPolicyUrl = parser.asString(item["privacyPolicyUrl"])
        val mintscanUrl = parser.asString(item["mintscanUrl"])

        return EnvironmentURIs(
            indexers,
            configs,
            validators,
            faucet,
            squid,
            statusPageUrl,
            marketImageUrl,
            tosUrl,
            privacyPolicyUrl,
            mintscanUrl
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
            it.environment == environment
        }
    }

    private fun reconnect() {
        val environment = environment
        adaptor = when (environment?.version) {
            AppVersion.v4 -> {
                V4StateManagerAdaptor(
                    ioImplementations,
                    uiImplementations,
                    environment,
                    V4StateManagerConfigs(environment),
                    stateNotification,
                    dataNotification,
                )
            }

            AppVersion.v3 -> {
                V3StateManagerAdaptor(
                    ioImplementations,
                    uiImplementations,
                    environment,
                    V3StateManagerConfigs(environment),
                    stateNotification,
                    dataNotification,
                    v3signer,
                    apiKey,
                )
            }

            else -> {
                null
            }
        }
    }

    fun setv3(signer: V3PrivateSignerProtocol?, apiKey: V3ApiKey?) {
        v3signer = signer
        this.apiKey = apiKey
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

    fun commitPlaceOrder(callback: TransactionCallback) {
        try {
            adaptor?.commitPlaceOrder(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error)
        }
    }

    fun commitClosePosition(callback: TransactionCallback) {
        try {
            adaptor?.commitClosePosition(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error)
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
            callback(false, error)
        }
    }

    fun faucet(amount: Double, callback: TransactionCallback) {
        try {
            adaptor?.faucet(amount, callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error)
        }
    }

    fun cancelOrder(orderId: String, callback: TransactionCallback) {
        try {
            adaptor?.cancelOrder(orderId, callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error)
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
}
