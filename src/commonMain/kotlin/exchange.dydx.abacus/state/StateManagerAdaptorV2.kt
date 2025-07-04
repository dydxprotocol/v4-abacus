package exchange.dydx.abacus.state

import exchange.dydx.abacus.output.Compliance
import exchange.dydx.abacus.output.ComplianceStatus
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.PresentationProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.helper.TriggerOrderToastGenerator
import exchange.dydx.abacus.state.machine.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.machine.ClosePositionInputField
import exchange.dydx.abacus.state.machine.PerpTradingStateMachine
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.abacus.state.machine.WalletConnectionType
import exchange.dydx.abacus.state.machine.tradeInMarket
import exchange.dydx.abacus.state.manager.ApiData
import exchange.dydx.abacus.state.manager.BlockAndTime
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
import exchange.dydx.abacus.state.manager.NetworkState
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.supervisor.AccountsSupervisor
import exchange.dydx.abacus.state.supervisor.AppConfigsV2
import exchange.dydx.abacus.state.supervisor.ConnectionDelegate
import exchange.dydx.abacus.state.supervisor.ConnectionsSupervisor
import exchange.dydx.abacus.state.supervisor.MarketsSupervisor
import exchange.dydx.abacus.state.supervisor.NetworkHelper
import exchange.dydx.abacus.state.supervisor.OnboardingSupervisor
import exchange.dydx.abacus.state.supervisor.SystemSupervisor
import exchange.dydx.abacus.state.supervisor.VaultSupervisor
import exchange.dydx.abacus.state.supervisor.addressRestriction
import exchange.dydx.abacus.state.supervisor.adjustIsolatedMargin
import exchange.dydx.abacus.state.supervisor.adjustIsolatedMarginPayload
import exchange.dydx.abacus.state.supervisor.cancelAllOrders
import exchange.dydx.abacus.state.supervisor.cancelAllOrdersPayload
import exchange.dydx.abacus.state.supervisor.cancelOrder
import exchange.dydx.abacus.state.supervisor.cancelOrderPayload
import exchange.dydx.abacus.state.supervisor.closeAllPositions
import exchange.dydx.abacus.state.supervisor.closeAllPositionsPayload
import exchange.dydx.abacus.state.supervisor.closePosition
import exchange.dydx.abacus.state.supervisor.closePositionPayload
import exchange.dydx.abacus.state.supervisor.commitAdjustIsolatedMargin
import exchange.dydx.abacus.state.supervisor.commitClosePosition
import exchange.dydx.abacus.state.supervisor.commitPlaceOrder
import exchange.dydx.abacus.state.supervisor.commitTriggerOrders
import exchange.dydx.abacus.state.supervisor.connectedSubaccountNumber
import exchange.dydx.abacus.state.supervisor.depositPayload
import exchange.dydx.abacus.state.supervisor.faucet
import exchange.dydx.abacus.state.supervisor.marketId
import exchange.dydx.abacus.state.supervisor.notifications
import exchange.dydx.abacus.state.supervisor.orderCanceled
import exchange.dydx.abacus.state.supervisor.placeOrderPayload
import exchange.dydx.abacus.state.supervisor.refresh
import exchange.dydx.abacus.state.supervisor.screen
import exchange.dydx.abacus.state.supervisor.setAddresses
import exchange.dydx.abacus.state.supervisor.stopWatchingLastOrder
import exchange.dydx.abacus.state.supervisor.subaccountNumber
import exchange.dydx.abacus.state.supervisor.subaccountTransferPayload
import exchange.dydx.abacus.state.supervisor.trade
import exchange.dydx.abacus.state.supervisor.triggerOrders
import exchange.dydx.abacus.state.supervisor.triggerOrdersPayload
import exchange.dydx.abacus.state.supervisor.walletConnectionType
import exchange.dydx.abacus.state.supervisor.withdrawPayload
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.GEO_POLLING_DURATION_SECONDS
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.UIImplementations
import kollections.JsExport
import kollections.iListOf
import kollections.toIMap

@JsExport
internal class StateManagerAdaptorV2(
    val deploymentUri: String,
    val environment: V4Environment,
    val ioImplementations: IOImplementations,
    val uiImplementations: UIImplementations,
    val configs: V4StateManagerConfigs,
    val appConfigs: AppConfigsV2,
    var stateNotification: StateNotificationProtocol?,
    var dataNotification: DataNotificationProtocol?,
    private val presentationProtocol: PresentationProtocol?,
) : ConnectionDelegate {
    val stateMachine: TradingStateMachine = PerpTradingStateMachine(
        environment = environment,
        localizer = uiImplementations.localizer,
        formatter = Formatter(uiImplementations.formatter),
        maxSubaccountNumber = 127,
        useParentSubaccount = appConfigs.accountConfigs.subaccountConfigs.useParentSubaccount,
        skipGoFast = appConfigs.skipGoFast,
        trackingProtocol = ioImplementations.tracking,
    )

    internal val jsonEncoder = JsonEncoder()
    internal val parser = Parser()
    internal var analyticsUtils: AnalyticsUtils = AnalyticsUtils()

    internal val networkHelper = NetworkHelper(
        deploymentUri = deploymentUri,
        environment = environment,
        uiImplementations = uiImplementations,
        ioImplementations = ioImplementations,
        configs = configs,
        stateNotification = stateNotification,
        dataNotification = dataNotification,
        parser = parser,
    ) { indexerRestriction ->
        updateRestriction(indexerRestriction)
    }

    private val connections = ConnectionsSupervisor(
        stateMachine = stateMachine,
        helper = networkHelper,
        analyticsUtils = analyticsUtils,
        delegate = this,
    )

    private val accounts = AccountsSupervisor(
        stateMachine = stateMachine,
        helper = networkHelper,
        analyticsUtils = analyticsUtils,
        configs = appConfigs.accountConfigs,
        screening = appConfigs.screening,
    )

    private val system = SystemSupervisor(
        stateMachine = stateMachine,
        helper = networkHelper,
        analyticsUtils = analyticsUtils,
        configs = appConfigs.systemConfigs,
        incentiveSeasonReceived = {
            accounts.currentIncentiveSeason = it
        },
    )

    private val onboarding = OnboardingSupervisor(
        stateMachine = stateMachine,
        helper = networkHelper,
        analyticsUtils = analyticsUtils,
        configs = appConfigs.onboardingConfigs,
    )

    private val markets = MarketsSupervisor(
        stateMachine = stateMachine,
        helper = networkHelper,
        analyticsUtils = analyticsUtils,
        configs = appConfigs.marketConfigs,
    )

    private val vault = VaultSupervisor(
        stateMachine = stateMachine,
        helper = networkHelper,
        analyticsUtils = analyticsUtils,
        configs = appConfigs.vaultConfigs,
    )

    private val triggerOrderToastGenerator = TriggerOrderToastGenerator(
        presentation = presentationProtocol,
        parser = parser,
        formatter = uiImplementations.formatter,
        localizer = uiImplementations.localizer,
        threading = ioImplementations.threading,
    )

    internal var restriction: UsageRestriction = UsageRestriction.Companion.noRestriction
        set(value) {
            if (field != value) {
                field = value
                didSetRestriction(value)
            }
        }

    internal var geo: String? = null
        set(value) {
            if (field != value) {
                field = value
                didSetGeo(value)
            }
        }

    internal var readyToConnect: Boolean = false
        internal set(value) {
            if (field != value) {
                field = value
                didSetReadyToConnect(field)
            }
        }

    internal var indexerConnected: Boolean = false
        internal set(value) {
            if (field != value) {
                field = value
                didSetIndexerConnected(indexerConnected)
            }
        }

    internal var socketConnected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                didSetSocketConnected(socketConnected)
            }
        }

    internal var validatorConnected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                didSetValidatorConnected(validatorConnected)
            }
        }

    internal var gasToken: GasToken?
        get() {
            return connections.gasToken
        }
        set(value) {
            connections.gasToken = value
        }

    internal var market: String?
        get() {
            return markets.marketId
        }
        set(value) {
            if (value != market) {
                markets.marketId = value

                if (value != null) {
                    val stateResponse = stateMachine.tradeInMarket(value, subaccountNumber)
                    ioImplementations.threading?.async(ThreadingType.main) {
                        stateNotification?.stateChanged(
                            stateResponse.state,
                            stateResponse.changes,
                        )
                    }
                }
            }
        }

    internal var candlesResolution: String
        get() {
            return markets.candlesResolution
        }
        set(value) {
            markets.candlesResolution = value
        }

    internal var orderbookGrouping: OrderbookGrouping
        get() {
            return markets.orderbookGrouping
        }
        set(value) {
            markets.orderbookGrouping = value
        }

    internal val accountAddress: String?
        get() {
            return accounts.accountAddress
        }

    internal var walletConnectionType: WalletConnectionType?
        get() {
            return accounts.walletConnectionType
        }
        set(value) {
            accounts.walletConnectionType = value
            onboarding.walletConnectionType = value
        }

    internal val sourceAddress: String?
        get() {
            return accounts.sourceAddress
        }

    internal var historicalPnlPeriod: HistoricalPnlPeriod
        get() {
            return accounts.historicalPnlPeriod
        }
        set(value) {
            accounts.historicalPnlPeriod = value
        }

    internal var historicalTradingRewardPeriod: HistoricalTradingRewardsPeriod
        get() {
            return accounts.historicalTradingRewardPeriod
        }
        set(value) {
            accounts.historicalTradingRewardPeriod = value
        }

    internal var subaccountNumber: Int
        get() = accounts.subaccountNumber
        set(value) {
            accounts.subaccountNumber = value
        }

    private val currentHeight: Int?
        get() {
            return connections.calculateCurrentHeight()
        }

    internal val notifications: IMap<String, Notification>
        get() {
            return accounts.notifications
        }

    internal val indexerState: NetworkState
        get() {
            return connections.indexerState
        }

    internal val validatorState: NetworkState
        get() {
            return connections.validatorState
        }

    internal fun setAddresses(source: String?, account: String?, isNew: Boolean) {
        accounts.setAddresses(sourceAddress = source, accountAddress = account, isNew = isNew)
        vault.accountAddress = account
    }

    private fun didSetReadyToConnect(readyToConnect: Boolean) {
        connections.readyToConnect = readyToConnect
        system.readyToConnect = readyToConnect
        onboarding.readyToConnect = readyToConnect
        markets.readyToConnect = readyToConnect
        accounts.readyToConnect = readyToConnect
        vault.readyToConnect = readyToConnect
        if (readyToConnect) {
            pollGeo()
        }
    }

    private fun didSetIndexerConnected(indexerConnected: Boolean) {
        system.indexerConnected = indexerConnected
        onboarding.indexerConnected = indexerConnected
        markets.indexerConnected = indexerConnected
        accounts.indexerConnected = indexerConnected
        vault.indexerConnected = indexerConnected
    }

    private fun didSetSocketConnected(socketConnected: Boolean) {
        connections.socketConnected = socketConnected
        system.socketConnected = socketConnected
        onboarding.socketConnected = socketConnected
        markets.socketConnected = socketConnected
        accounts.socketConnected = socketConnected
        vault.socketConnected = socketConnected
    }

    private fun didSetValidatorConnected(validatorConnected: Boolean) {
        system.validatorConnected = validatorConnected
        onboarding.validatorConnected = validatorConnected
        markets.validatorConnected = validatorConnected
        accounts.validatorConnected = validatorConnected
        vault.validatorConnected = validatorConnected
    }

    internal fun dispose() {
        stateNotification = null
        dataNotification = null
        readyToConnect = false
    }

    override fun didConnectToIndexer(connectedToIndexer: Boolean) {
        indexerConnected = connectedToIndexer
    }

    override fun didConnectToValidator(connectedToValidator: Boolean) {
        validatorConnected = connectedToValidator
    }

    override fun didConnectToSocket(connectedToSocket: Boolean) {
        socketConnected = connectedToSocket
    }

    override fun processSocketResponse(message: String) {
        ioImplementations.threading?.async(ThreadingType.abacus) {
            try {
                val json = parser.decodeJsonObject(message)
                if (json != null) {
                    socket(json)
                }
            } catch (_: Exception) {
            }
        }
    }

    @Throws(Exception::class)
    private fun socket(
        payload: IMap<String, Any>,
    ) {
        val type = parser.asString(payload["type"]) ?: return

        try {
            when (type) {
                "connected" -> {
                    socketConnected = true
                }

                "error" -> {
                    throw ParsingException(ParsingErrorType.BackendError, payload.toString())
                }

                else -> {
                    val channel = parser.asString(payload["channel"]) ?: return
                    val id = parser.asString(payload["id"])

                    val info =
                        SocketInfo(type, channel, id, parser.asInt(payload["subaccountNumber"]))
                    when (channel) {
                        configs.marketsChannel() -> {
                            val subaccountNumber = accounts.connectedSubaccountNumber
                            markets.receiveMarketsChannelSocketData(info, payload, subaccountNumber)
                        }

                        configs.marketOrderbookChannel() -> {
                            val subaccountNumber = accounts.connectedSubaccountNumber
                            markets.receiveMarketOrderbooksChannelSocketData(
                                info,
                                payload,
                                subaccountNumber,
                            )
                        }

                        configs.marketTradesChannel() -> {
                            markets.receiveMarketTradesChannelSocketData(info, payload)
                        }

                        configs.marketCandlesChannel() -> {
                            markets.receiveMarketCandlesChannelSocketData(info, payload)
                        }

                        configs.subaccountChannel(false), configs.subaccountChannel(true) -> {
                            accounts.receiveSubaccountChannelSocketData(info, payload, height())
                        }

                        else -> {
                            throw ParsingException(
                                ParsingErrorType.UnknownChannel,
                                "$channel is not known",
                            )
                        }
                    }
                }
            }
        } catch (e: ParsingException) {
            val error = ParsingError(
                e.type,
                e.message ?: "Unknown error",
            )
            emitError(error)
        }
    }

    private fun emitError(error: ParsingError) {
        ioImplementations.threading?.async(ThreadingType.main) {
            stateNotification?.errorsEmitted(iListOf(error))
            dataNotification?.errorsEmitted(iListOf(error))
        }
    }

    private fun height(): BlockAndTime? {
        return null
    }

    private fun pollGeo() {
        ioImplementations.timer?.schedule(
            0.0,
            GEO_POLLING_DURATION_SECONDS,
        ) {
            fetchGeo()
            true
        }
    }
    private fun fetchGeo() {
        val url = environment.endpoints.geo
        if (url != null) {
            networkHelper.get(
                url = url,
                params = null,
                headers = null,
                callback = { _, response, httpCode, _ ->
                    geo = if (networkHelper.success(httpCode) && response != null) {
                        val payload = networkHelper.parser.decodeJsonObject(response)?.toIMap()
                        if (payload != null) {
                            val country = networkHelper.parser.asString(networkHelper.parser.value(payload, "geo.country"))
                            country
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                },
            )
        }
    }

    internal fun trade(data: String?, type: TradeInputField?) {
        accounts.trade(data, type)
    }

    internal fun closePosition(data: String?, type: ClosePositionInputField) {
        accounts.closePosition(data, type)
    }

    internal fun triggerOrders(data: String?, type: TriggerOrdersInputField?) {
        accounts.triggerOrders(data, type)
    }

    internal fun adjustIsolatedMargin(data: String?, type: AdjustIsolatedMarginInputField?) {
        accounts.adjustIsolatedMargin(data, type)
    }

    internal fun placeOrderPayload(): HumanReadablePlaceOrderPayload? {
        return accounts.placeOrderPayload(currentHeight)
    }

    internal fun closePositionPayload(): HumanReadablePlaceOrderPayload? {
        return accounts.closePositionPayload(currentHeight)
    }

    internal fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload? {
        return accounts.cancelOrderPayload(orderId)
    }

    internal fun cancelAllOrdersPayload(marketId: String?): HumanReadableCancelAllOrdersPayload? {
        return accounts.cancelAllOrdersPayload(marketId)
    }

    internal fun closeAllPositionsPayload(): HumanReadableCloseAllPositionsPayload? {
        return accounts.closeAllPositionsPayload(currentHeight)
    }

    internal fun triggerOrdersPayload(): HumanReadableTriggerOrdersPayload? {
        return accounts.triggerOrdersPayload(currentHeight)
    }

    internal fun depositPayload(): HumanReadableDepositPayload? {
        return accounts.depositPayload()
    }

    internal fun withdrawPayload(): HumanReadableWithdrawPayload? {
        return accounts.withdrawPayload()
    }

    internal fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload? {
        return accounts.subaccountTransferPayload()
    }

    internal fun commitPlaceOrder(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        return accounts.commitPlaceOrder(currentHeight, callback)
    }

    internal fun commitTriggerOrders(callback: TransactionCallback): HumanReadableTriggerOrdersPayload? {
        val payload = accounts.commitTriggerOrders(currentHeight) { successful, error, data ->
            if (appConfigs.triggerOrderToast) {
                triggerOrderToastGenerator.onTriggerOrderResponse(
                    subaccountNumber,
                    successful,
                    error,
                    data,
                )
            }
            callback(successful, error, data)
        }
        if (payload != null && appConfigs.triggerOrderToast) {
            triggerOrderToastGenerator.onTriggerOrderSubmitted(subaccountNumber, payload, stateMachine.state)
        }
        return payload
    }

    internal fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        return accounts.commitClosePosition(currentHeight, callback)
    }

    internal fun commitAdjustIsolatedMargin(callback: TransactionCallback): HumanReadableSubaccountTransferPayload? {
        return accounts.commitAdjustIsolatedMargin(callback)
    }

    internal fun adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload? {
        return accounts.adjustIsolatedMarginPayload()
    }

    internal fun stopWatchingLastOrder() {
        accounts.stopWatchingLastOrder()
    }

    internal fun cancelOrder(orderId: String, callback: TransactionCallback) {
        accounts.cancelOrder(orderId, callback)
    }

    internal fun cancelAllOrders(marketId: String?, callback: TransactionCallback) {
        accounts.cancelAllOrders(marketId, callback)
    }

    internal fun closeAllPositions(callback: TransactionCallback): HumanReadableCloseAllPositionsPayload? {
        return accounts.closeAllPositions(currentHeight, callback)
    }

    internal fun orderCanceled(orderId: String) {
        accounts.orderCanceled(orderId)
    }

    internal fun faucet(amount: Double, callback: TransactionCallback) {
        accounts.faucet(amount, callback)
    }

    internal fun transfer(data: String?, type: TransferInputField?) {
        onboarding.transfer(data, type, accountAddress, sourceAddress, subaccountNumber)

        data?.let {
            TransferType.Companion(rawValue = data)?.let {
                system.didSetTransferType(it)
            }
        }
    }

    internal fun commitTransfer(callback: TransactionCallback) {
        onboarding.commitTransfer(subaccountNumber, callback)
    }

    internal fun commitCCTPWithdraw(callback: TransactionCallback) {
        val address = accountAddress
        if (address != null) {
            onboarding.commitCCTPWithdraw(address, subaccountNumber, callback)
        }
    }

    internal fun transferStatus(
        hash: String,
        fromChainId: String?,
        toChainId: String?,
        isCctp: Boolean,
        requestId: String?,
    ) {
        onboarding.transferStatus(hash, fromChainId, toChainId, isCctp, requestId)
    }

    internal fun refresh(data: ApiData) {
        accounts.refresh(data)
    }

    internal fun screen(address: String, callback: (restriction: Restriction) -> Unit) {
        accounts.screen(address, callback)
    }

    internal fun registerPushNotification(token: String, languageCode: String?) {
        accounts.registerPushNotification(token, languageCode)
    }

    internal fun refreshVaultAccount() {
        vault.refreshVaultAccount()
    }

    private fun updateRestriction(indexerRestriction: UsageRestriction?) {
        restriction = indexerRestriction ?: accounts.addressRestriction
            ?: UsageRestriction.Companion.noRestriction
    }

    private fun didSetRestriction(restriction: UsageRestriction?) {
        val state = stateMachine.state ?: PerpetualState.Companion.newState()
        stateMachine.state = state.copy(restriction = restriction)
        ioImplementations.threading?.async(ThreadingType.main) {
            stateNotification?.stateChanged(
                state = stateMachine.state,
                changes = StateChanges(
                    iListOf(Changes.restriction),
                ),
            )
        }
    }

    private fun didSetGeo(geo: String?) {
        val state = stateMachine.state ?: PerpetualState.Companion.newState()
        stateMachine.state = state.copy(
            compliance = Compliance(
                geo = geo,
                status = state.compliance?.status ?: ComplianceStatus.COMPLIANT,
                updatedAt = state.compliance?.updatedAt,
                expiresAt = state.compliance?.expiresAt,
            ),
        )
        ioImplementations.threading?.async(ThreadingType.main) {
            stateNotification?.stateChanged(
                state = stateMachine.state,
                changes = StateChanges(
                    iListOf(Changes.compliance),
                ),
            )
        }
    }
}
