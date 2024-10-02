package exchange.dydx.abacus.state.v2.manager

import exchange.dydx.abacus.di.AbacusScope
import exchange.dydx.abacus.di.Deployment
import exchange.dydx.abacus.di.DeploymentUri
import exchange.dydx.abacus.output.ComplianceAction
import exchange.dydx.abacus.output.Documentation
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.FileLocation
import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.protocols.readCachedTextFile
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.app.helper.DynamicLocalizer
import exchange.dydx.abacus.state.manager.ApiData
import exchange.dydx.abacus.state.manager.AppSettings
import exchange.dydx.abacus.state.manager.ConfigFile
import exchange.dydx.abacus.state.manager.GasToken
import exchange.dydx.abacus.state.manager.HistoricalPnlPeriod
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.HumanReadableCancelAllOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableCloseAllPositionsPayload
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.state.manager.SingletonAsyncAbacusStateManagerProtocol
import exchange.dydx.abacus.state.manager.TransferChainInfo
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.WalletConnectionType
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.DummyFormatter
import exchange.dydx.abacus.utils.DummyLocalizer
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ProtocolNativeImpFactory
import exchange.dydx.abacus.utils.Threading
import exchange.dydx.abacus.utils.UIImplementations
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import me.tatarka.inject.annotations.Inject

@JsExport
@AbacusScope
@Inject
class AsyncAbacusStateManagerV2(
    val deploymentUri: DeploymentUri,
    val deployment: Deployment, // MAINNET, TESTNET, DEV
    val appConfigs: AppConfigsV2,
    val ioImplementations: IOImplementations,
    val uiImplementations: UIImplementations,
    val stateNotification: StateNotificationProtocol? = null,
    val dataNotification: DataNotificationProtocol? = null,
    private val presentationProtocol: PresentationProtocol? = null,
) : SingletonAsyncAbacusStateManagerProtocol {
    init {
        Logger.clientLogger = ioImplementations.logging
        if (appConfigs.enableLogger) {
            Logger.isDebugEnabled = true
        }
    }

    override val state: PerpetualState?
        get() = adaptor?.stateMachine?.state

    private var _appSettings: AppSettings? = null

    override val appSettings: AppSettings?
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

    override val availableEnvironments: IList<SelectionOption>
        get() = environments.map { environment ->
            SelectionOption(environment.id, environment.name, null, null)
        }

    override var environmentId: String? = null
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

    override val environment: V4Environment?
        get() = _environment

    override var documentation: Documentation? = null
        private set

    internal var adaptor: StateManagerAdaptorV2? = null
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
                value?.walletConnectionType = walletConnectionType
                field = value
            }
        }

    override var readyToConnect: Boolean = false
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.readyToConnect = field
            }
        }

    override var market: String? = null
        set(value) {
            if (isMarketValid(value) && field != value) {
                field = value
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    adaptor?.market = field
                }
            }
        }

    override var orderbookGrouping: OrderbookGrouping = OrderbookGrouping.none
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.orderbookGrouping = field
            }
        }

    override var candlesResolution: String = "1DAY"
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.candlesResolution = field
            }
        }

    override var accountAddress: String? = null
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.accountAddress = field
            }
        }

    override var walletConnectionType: WalletConnectionType? = WalletConnectionType.Ethereum
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.walletConnectionType = field
            }
        }

    override var sourceAddress: String? = null
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.sourceAddress = field
            }
        }

    override var subaccountNumber: Int = 0
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.subaccountNumber = field
            }
        }

    override var historicalPnlPeriod: HistoricalPnlPeriod = HistoricalPnlPeriod.Period7d
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.historicalPnlPeriod = field
            }
        }

    override var historicalTradingRewardPeriod: HistoricalTradingRewardsPeriod =
        HistoricalTradingRewardsPeriod.DAILY
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.historicalTradingRewardPeriod = field
            }
        }

    override var gasToken: GasToken? = null
        set(value) {
            field = value
            ioImplementations.threading?.async(ThreadingType.abacus) {
                adaptor?.gasToken = field
            }
        }

    private var pushNotificationToken: String? = null
    private var pushNotificationLanguageCode: String? = null

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
                logging = _nativeImplementations.logging,
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
        ioImplementations.threading?.async(ThreadingType.network) {
            val path = configFile.path
            if (appConfigs.loadRemote) {
                loadFromRemoteConfigFile(configFile)
                val configFileUrl = "$deploymentUri$path"
                ioImplementations.rest?.get(
                    configFileUrl,
                    null,
                    callback = { response, httpCode, headers ->
                        ioImplementations.threading?.async(ThreadingType.abacus) {
                            if (success(httpCode) && response != null) {
                                if (parse(response, configFile)) {
                                    writeToLocalFile(response, path)
                                }
                            }
                        }
                    },
                )
            } else {
                loadFromBundledLocalConfigFile(configFile)
            }
        }
    }

    private fun loadFromRemoteConfigFile(configFile: ConfigFile) {
        ioImplementations.fileSystem?.readCachedTextFile(
            configFile.path,
        )?.let {
            ioImplementations.threading?.async(ThreadingType.abacus) {
                parse(it, configFile)
            }
        }
    }

    private fun loadFromBundledLocalConfigFile(configFile: ConfigFile) {
        ioImplementations.fileSystem?.readTextFile(
            FileLocation.AppBundle,
            configFile.path,
        )?.let {
            ioImplementations.threading?.async(ThreadingType.abacus) {
                parse(it, configFile)
            }
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
        ioImplementations.threading?.async(ThreadingType.network) {
            ioImplementations.fileSystem?.writeTextFile(
                file,
                response,
            )
        }
    }

    private fun parseDocumentation(response: String) {
        val parser = Parser()
        this.documentation = parser.asTypedObject<Documentation>(response)
    }

    private fun parseEnvironments(response: String): Boolean {
        val parser = Parser()
        val items = parser.decodeJsonObject(response)
        val deployments = parser.asMap(items?.get("deployments")) ?: return false
        val target = parser.asMap(deployments[deployment]) ?: return false
        val targetEnvironments = parser.asList(target["environments"]) ?: return false
        val targetDefault = parser.asString(target["default"])

        val tokensData = parser.asNativeMap(items?.get("tokens"))
        val linksData = parser.asNativeMap(items?.get("links"))
        val walletsData = parser.asNativeMap(items?.get("wallets"))
        val governanceData = parser.asNativeMap(items?.get("governance"))

        if (items != null) {
            val environmentsData = parser.asMap(items["environments"]) ?: return false
            val parsedEnvironments = mutableMapOf<String, V4Environment>()
            for ((key, value) in environmentsData) {
                val data = parser.asMap(value) ?: continue
                val dydxChainId = parser.asString(data["dydxChainId"]) ?: continue
                val environment = V4Environment.parse(
                    id = key,
                    data = data,
                    parser = parser,
                    deploymentUri = deploymentUri,
                    localizer = uiImplementations.localizer,
                    tokensData = parser.asNativeMap(tokensData?.get(dydxChainId)),
                    linksData = parser.asNativeMap(linksData?.get(dydxChainId)),
                    walletsData = parser.asNativeMap(walletsData?.get(dydxChainId)),
                    governanceData = parser.asNativeMap(governanceData?.get(dydxChainId)),
                ) ?: continue
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
            adaptor = StateManagerAdaptorV2(
                deploymentUri = deploymentUri,
                environment = environment,
                ioImplementations = ioImplementations,
                uiImplementations = uiImplementations,
                configs = V4StateManagerConfigs(deploymentUri, environment),
                appConfigs = appConfigs,
                stateNotification = stateNotification,
                dataNotification = dataNotification,
                presentationProtocol = presentationProtocol,
            )
            adaptor?.gasToken = gasToken
            pushNotificationToken?.let { token ->
                adaptor?.registerPushNotification(token, pushNotificationLanguageCode)
            }
        }
    }

    override fun trade(data: String?, type: TradeInputField?) {
        adaptor?.trade(data, type)
    }

    override fun closePosition(data: String?, type: ClosePositionInputField) {
        adaptor?.closePosition(data, type)
    }

    override fun transfer(data: String?, type: TransferInputField?) {
        adaptor?.transfer(data, type)
    }

    override fun triggerOrders(data: String?, type: TriggerOrdersInputField?) {
        adaptor?.triggerOrders(data, type)
    }

    override fun adjustIsolatedMargin(data: String?, type: AdjustIsolatedMarginInputField?) {
        adaptor?.adjustIsolatedMargin(data, type)
    }

    override fun isMarketValid(marketId: String?): Boolean {
        return if (marketId == null) {
            true
        } else {
            val market = adaptor?.stateMachine?.state?.market(marketId)
            (market?.status?.canTrade == true || market?.status?.canReduce == true)
        }
    }

    override fun transferStatus(
        hash: String,
        fromChainId: String?,
        toChainId: String?,
        isCctp: Boolean,
        requestId: String?
    ) {
        adaptor?.transferStatus(hash, fromChainId, toChainId, isCctp, requestId)
    }

    override fun refresh(data: ApiData) {
        adaptor?.refresh(data)
    }

    override fun placeOrderPayload(): HumanReadablePlaceOrderPayload? {
        return adaptor?.placeOrderPayload()
    }

    override fun closePositionPayload(): HumanReadablePlaceOrderPayload? {
        return adaptor?.closePositionPayload()
    }

    override fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload? {
        return adaptor?.cancelOrderPayload(orderId)
    }

    override fun cancelAllOrdersPayload(marketId: String?): HumanReadableCancelAllOrdersPayload? {
        return adaptor?.cancelAllOrdersPayload(marketId)
    }

    override fun closeAllPositionsPayload(): HumanReadableCloseAllPositionsPayload? {
        return adaptor?.closeAllPositionsPayload()
    }

    override fun triggerOrdersPayload(): HumanReadableTriggerOrdersPayload? {
        return adaptor?.triggerOrdersPayload()
    }

    override fun adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload? {
        return adaptor?.adjustIsolatedMarginPayload()
    }

    override fun depositPayload(): HumanReadableDepositPayload? {
        return adaptor?.depositPayload()
    }

    override fun withdrawPayload(): HumanReadableWithdrawPayload? {
        return adaptor?.withdrawPayload()
    }

    override fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload? {
        return adaptor?.subaccountTransferPayload()
    }

    override fun commitPlaceOrder(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        return try {
            adaptor?.commitPlaceOrder(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
            null
        }
    }

    override fun commitTriggerOrders(callback: TransactionCallback): HumanReadableTriggerOrdersPayload? {
        return try {
            adaptor?.commitTriggerOrders(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
            null
        }
    }

    override fun commitAdjustIsolatedMargin(callback: TransactionCallback): HumanReadableSubaccountTransferPayload? {
        return try {
            adaptor?.commitAdjustIsolatedMargin(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
            null
        }
    }

    override fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        return try {
            adaptor?.commitClosePosition(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
            null
        }
    }

    override fun stopWatchingLastOrder() {
        adaptor?.stopWatchingLastOrder()
    }

    override fun commitTransfer(callback: TransactionCallback) {
        try {
            adaptor?.commitTransfer(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
        }
    }

    override fun commitCCTPWithdraw(callback: TransactionCallback) {
        try {
            adaptor?.commitCCTPWithdraw(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
        }
    }

    override fun faucet(amount: Double, callback: TransactionCallback) {
        try {
            adaptor?.faucet(amount, callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
        }
    }

    override fun cancelOrder(orderId: String, callback: TransactionCallback) {
        try {
            adaptor?.cancelOrder(orderId, callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
        }
    }

    override fun cancelAllOrders(marketId: String?, callback: TransactionCallback) {
        try {
            adaptor?.cancelAllOrders(marketId, callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
        }
    }

    override fun closeAllPositions(callback: TransactionCallback): HumanReadableCloseAllPositionsPayload? {
        return try {
            adaptor?.closeAllPositions(callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
            null
        }
    }

    override fun triggerCompliance(action: ComplianceAction, callback: TransactionCallback) {
        try {
            adaptor?.triggerCompliance(action, callback)
        } catch (e: Exception) {
            val error = V4TransactionErrors.error(null, e.toString())
            callback(false, error, null)
        }
    }

    // Bridge functions.
    // If client is not using cancelOrder function, it should call orderCanceled function with
    // payload from v4-client to process state
    override fun orderCanceled(orderId: String) {
        adaptor?.orderCanceled(orderId)
    }

    override fun screen(address: String, callback: (restriction: Restriction) -> Unit) {
        adaptor?.screen(address, callback)
    }

    override fun getChainById(chainId: String): TransferChainInfo? {
        val parser = Parser()
        val chainMap = adaptor?.stateMachine?.routerProcessor?.getChainById(chainId = chainId)
        val chainName = parser.asString(chainMap?.get("chain_name"))
        val logoUri = parser.asString(chainMap?.get("logo_uri"))
        val chainType = parser.asString(chainMap?.get("chain_type"))
        val isTestnet = parser.asBool(chainMap?.get("is_testnet"))
        if (chainName is String && logoUri is String && chainType is String && isTestnet is Boolean) {
            return TransferChainInfo(
                chainName = chainName,
                chainId = chainType,
                logoUri = logoUri,
                chainType = chainType,
                isTestnet = isTestnet,
            )
        }
        return null
    }

    override fun registerPushNotification(token: String, languageCode: String?) {
        pushNotificationToken = token
        pushNotificationLanguageCode = languageCode
        adaptor?.registerPushNotification(token, languageCode)
    }

    override fun refreshVaultAccount() {
        adaptor?.refreshVaultAccount()
    }
}
