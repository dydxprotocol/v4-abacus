package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.V3PrivateSignerProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.app.AppVersion
import exchange.dydx.abacus.state.app.EnvironmentURIs
import exchange.dydx.abacus.state.app.HistoricalPnlPeriod
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
import kollections.iMutableListOf
import kollections.toIList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlin.js.JsName

@JsExport
class AsyncAbacusStateManager(
    val ioImplementations: IOImplementations,
    val uiImplementations: UIImplementations,
    val stateNotification: StateNotificationProtocol? = null,
    val dataNotification: DataNotificationProtocol? = null,
    val v3signer: V3PrivateSignerProtocol? = null,
    val apiKey: V3ApiKey? = null,
) {
    private val _environmentsConfigs: String =
        """
            [
               {
                  "comment":"V3 Mainnet",
                  "environment":"1",
                  "ethereumChainId":"1",
                  "string":"v3 MainNet",
                  "stringKey":"CHAIN.V3_MAINNET",
                  "isMainNet":true,
                  "version":"v3",
                  "maxSubaccountNumber":0,
                  "endpoints":{
                     "api":"https://api.dydx.exchange",
                     "socket":"wss://api.dydx.exchange",
                     "configs":"https://dydx-shared-resources.vercel.app"
                  }
               },
               {
                  "comment":"V3 Staging",
                  "environment":"5",
                  "ethereumChainId":"5",
                  "string":"v3 Staging",
                  "stringKey":"CHAIN.V3_GOERLI",
                  "isMainNet":false,
                  "version":"v3",
                  "maxSubaccountNumber":0,
                  "endpoints":{
                     "api":"https://api.stage.dydx.exchange",
                     "socket":"wss://api.stage.dydx.exchange",
                     "configs":"https://dydx-shared-resources.vercel.app"
                  }
               },
               {
                  "comment":"V4 Dev",
                  "environment":"dydxprotocol-dev",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev",
                  "stringKey":"CHAIN.V4_DEVNET",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "api":"http://indexer.v4dev.dydx.exchange",
                     "socket":"wss://indexer.v4dev.dydx.exchange",
                     "faucet":"http://faucet.v4dev.dydx.exchange",
                     "validators":[
                        "http://validator.v4dev.dydx.exchange"
                     ],
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-shared-resources.vercel.app"
                  }
               },
               {
                  "comment":"V4 Dev 2",
                  "environment":"dydxprotocol-dev-2",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev 2",
                  "stringKey":"CHAIN.V4_DEVNET_2",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "api":"http://dev2-indexer-apne1-lb-public-2076363889.ap-northeast-1.elb.amazonaws.com",
                     "socket":"ws://dev2-indexer-apne1-lb-public-2076363889.ap-northeast-1.elb.amazonaws.com",
                     "validators":[
                        "http://35.75.227.118"
                     ],
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-shared-resources.vercel.app"
                  }
               },
               {
                  "comment":"V4 Dev 3",
                  "environment":"dydxprotocol-dev-3",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev 3",
                  "stringKey":"CHAIN.V4_DEVNET_3",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-shared-resources.vercel.app"
                  }
               },
               {
                  "comment":"V4 Dev 4",
                  "environment":"dydxprotocol-dev-4",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev 4",
                  "stringKey":"CHAIN.V4_DEVNET_4",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "api":"http://indexer.v4dev4.dydx.exchange",
                     "socket":"ws://indexer.v4dev4.dydx.exchange",
                     "validators":[
                        "http://validator.v4dev4.dydx.exchange"
                     ],
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-shared-resources.vercel.app"
                  }
               },
               {
                  "comment":"V4 Dev 5",
                  "environment":"dydxprotocol-dev-5",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Dev 5",
                  "stringKey":"CHAIN.V4_DEVNET_5",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "api":"http://dev5-indexer-apne1-lb-public-1721328151.ap-northeast-1.elb.amazonaws.com",
                     "socket":"ws://dev5-indexer-apne1-lb-public-1721328151.ap-northeast-1.elb.amazonaws.com",
                     "validators":[
                        "http://18.223.78.50"
                     ],
                     "0xsquid":"https://testnet.api.0xsquid.com",
                     "configs":"https://dydx-shared-resources.vercel.app"
                  }
               },
               {
                  "comment":"V4 Staging",
                  "environment":"dydxprotocol-staging",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydxprotocol-testnet",
                  "string":"v4 Staging",
                  "stringKey":"CHAIN.V4_STAGING",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "api":"https://indexer.v4staging.dydx.exchange",
                     "socket":"wss://indexer.v4staging.dydx.exchange",
                     "faucet":"https://faucet.v4staging.dydx.exchange",
                     "validators":[
                        "https://validator.v4staging.dydx.exchange"
                     ],
                     "0xsquid":"https://squid-api-git-feat-cosmos-maintestnet-0xsquid.vercel.app",
                     "configs":"https://dydx-shared-resources.vercel.app"
                  }
               },
               {
                  "comment":"V4 Public Testnet #2",
                  "environment":"dydxprotocol-testnet-2",
                  "ethereumChainId":"5",
                  "dydxChainId":"dydx-testnet-2",
                  "string":"v4 Public Testnet #2",
                  "stringKey":"CHAIN.V4_TESTNET2",
                  "isMainNet":false,
                  "version":"v4",
                  "maxSubaccountNumber":127,
                  "endpoints":{
                     "api":"https://indexer.v4testnet2.dydx.exchange",
                     "socket":"wss://indexer.v4testnet2.dydx.exchange",
                     "validators":[
                        "https://validator.v4testnet2.dydx.exchange"
                     ],
                     "0xsquid":"https://squid-api-git-feat-cosmos-maintestnet-0xsquid.vercel.app",
                     "configs":"https://dydx-shared-resources.vercel.app",
                     "faucet":"https://faucet.v4testnet2.dydx.exchange"
                  }
               }
            ]
        """.trimIndent()
    private val environments: IList<V4Environment> =
        parseEnvironments(Json.parseToJsonElement(_environmentsConfigs).jsonArray.toIList())


    val availableEnvironments: IList<SelectionOption> = environments.map { environment ->
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
                field?.didSetReadyToConnect(false)
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

    var candlesResolution: String = "1d"
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.candlesResolution = field
            }
        }

    @JsName("fromFactory")
    constructor(_nativeImplementations: ProtocolNativeImpFactory) : this(
        createIOImplementions(_nativeImplementations),
        createUIImplemention(_nativeImplementations),
        _nativeImplementations.stateNotification,
        _nativeImplementations.dataNotification,
        _nativeImplementations.v3Signer,
        _nativeImplementations.apiKey
    ) {
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
    }


    private fun parseEnvironments(items: IList<Any>?): IList<V4Environment> {
        val result = iMutableListOf<V4Environment>()
        if (items != null) {
            val parser = Parser()
            for (item in items) {
                val environment = parseEnvironment(parser.asMap(item), parser)
                if (environment != null) {
                    result.add(environment)
                }
            }
        }
        return result
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
        val api = parser.asString(item["api"]) ?: return null
        val socket = parser.asString(item["socket"]) ?: return null
        val configs = parser.asString(item["configs"])
        val validators = parser.asList(item["validators"])?.map { parser.asString(it) }?.filterNotNull()?.toIList()
        val faucet = parser.asString(item["faucet"])
        val squid = parser.asString(item["0xsquid"])

        return EnvironmentURIs(api, socket, configs, validators, faucet, squid)
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
        adaptor?.readyToConnect = readyToConnect
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
}
