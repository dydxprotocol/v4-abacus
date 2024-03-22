package exchange.dydx.abacus.state.v2.manager

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.ApiData
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.HistoricalPnlPeriod
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.NetworkState
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.PerpTradingStateMachine
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.state.v2.supervisor.AccountsSupervisor
import exchange.dydx.abacus.state.v2.supervisor.AppConfigsV2
import exchange.dydx.abacus.state.v2.supervisor.ConnectionDelegate
import exchange.dydx.abacus.state.v2.supervisor.ConnectionsSupervisor
import exchange.dydx.abacus.state.v2.supervisor.MarketsSupervisor
import exchange.dydx.abacus.state.v2.supervisor.NetworkHelper
import exchange.dydx.abacus.state.v2.supervisor.OnboardingSupervisor
import exchange.dydx.abacus.state.v2.supervisor.SystemSupervisor
import exchange.dydx.abacus.state.v2.supervisor.accountAddress
import exchange.dydx.abacus.state.v2.supervisor.addressRestriction
import exchange.dydx.abacus.state.v2.supervisor.cancelOrder
import exchange.dydx.abacus.state.v2.supervisor.cancelOrderPayload
import exchange.dydx.abacus.state.v2.supervisor.closePosition
import exchange.dydx.abacus.state.v2.supervisor.closePositionPayload
import exchange.dydx.abacus.state.v2.supervisor.commitClosePosition
import exchange.dydx.abacus.state.v2.supervisor.commitPlaceOrder
import exchange.dydx.abacus.state.v2.supervisor.connectedSubaccountNumber
import exchange.dydx.abacus.state.v2.supervisor.depositPayload
import exchange.dydx.abacus.state.v2.supervisor.faucet
import exchange.dydx.abacus.state.v2.supervisor.marketId
import exchange.dydx.abacus.state.v2.supervisor.notifications
import exchange.dydx.abacus.state.v2.supervisor.orderCanceled
import exchange.dydx.abacus.state.v2.supervisor.placeOrderPayload
import exchange.dydx.abacus.state.v2.supervisor.refresh
import exchange.dydx.abacus.state.v2.supervisor.screen
import exchange.dydx.abacus.state.v2.supervisor.sourceAddress
import exchange.dydx.abacus.state.v2.supervisor.stopWatchingLastOrder
import exchange.dydx.abacus.state.v2.supervisor.subaccountNumber
import exchange.dydx.abacus.state.v2.supervisor.subaccountTransferPayload
import exchange.dydx.abacus.state.v2.supervisor.trade
import exchange.dydx.abacus.state.v2.supervisor.triggerOrders
import exchange.dydx.abacus.state.v2.supervisor.withdrawPayload
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.UIImplementations
import kollections.JsExport
import kollections.iListOf

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
) : ConnectionDelegate {
    var stateMachine: TradingStateMachine = PerpTradingStateMachine(
        environment,
        uiImplementations.localizer,
        Formatter(uiImplementations.formatter),
        127,
    )

    internal val jsonEncoder = JsonEncoder()
    internal val parser = Parser()
    internal var analyticsUtils: AnalyticsUtils = AnalyticsUtils()

    internal val networkHelper = NetworkHelper(
        deploymentUri,
        environment,
        uiImplementations,
        ioImplementations,
        configs,
        stateNotification,
        dataNotification,
        parser,
    ) { indexerRestriction ->
        updateRestriction(indexerRestriction)
    }

    private val connections = ConnectionsSupervisor(
        stateMachine,
        networkHelper,
        analyticsUtils,
        this,
    )

    private val system = SystemSupervisor(
        stateMachine,
        networkHelper,
        analyticsUtils,
        appConfigs.systemConfigs,
    )

    private val onboarding = OnboardingSupervisor(
        stateMachine,
        networkHelper,
        analyticsUtils,
        appConfigs.onboardingConfigs,
    )

    private val accounts = AccountsSupervisor(
        stateMachine,
        networkHelper,
        analyticsUtils,
        appConfigs.accountConfigs,
    )

    private val markets = MarketsSupervisor(
        stateMachine,
        networkHelper,
        analyticsUtils,
        appConfigs.marketConfigs,
    )

    internal open var restriction: UsageRestriction = UsageRestriction.noRestriction
        set(value) {
            if (field != value) {
                field = value
                didSetRestriction(value)
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

    internal var market: String?
        get() {
            return markets.marketId
        }
        set(value) {
            if (value != market) {
                markets.marketId = value

                if (value != null) {
                    networkHelper.ioImplementations.threading?.async(ThreadingType.abacus) {
                        stateMachine.tradeInMarket(value, subaccountNumber)
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

    internal var accountAddress: String?
        get() {
            return accounts.accountAddress
        }
        set(value) {
            accounts.accountAddress = value
        }

    internal var sourceAddress: String?
        get() {
            return accounts.sourceAddress
        }
        set(value) {
            accounts.sourceAddress = value
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

    private fun didSetReadyToConnect(readyToConnect: Boolean) {
        connections.readyToConnect = readyToConnect
        system.readyToConnect = readyToConnect
        onboarding.readyToConnect = readyToConnect
        markets.readyToConnect = readyToConnect
        accounts.readyToConnect = readyToConnect
    }

    private fun didSetIndexerConnected(indexerConnected: Boolean) {
        system.indexerConnected = indexerConnected
        onboarding.indexerConnected = indexerConnected
        markets.indexerConnected = indexerConnected
        accounts.indexerConnected = indexerConnected
    }

    private fun didSetSocketConnected(socketConnected: Boolean) {
        connections.socketConnected = socketConnected
        system.socketConnected = socketConnected
        onboarding.socketConnected = socketConnected
        markets.socketConnected = socketConnected
        accounts.socketConnected = socketConnected
    }

    private fun didSetValidatorConnected(validatorConnected: Boolean) {
        system.validatorConnected = validatorConnected
        onboarding.validatorConnected = validatorConnected
        markets.validatorConnected = validatorConnected
        accounts.validatorConnected = validatorConnected
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

                    val info = SocketInfo(type, channel, id)
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

                        configs.subaccountChannel() -> {
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

    internal fun trade(data: String?, type: TradeInputField?) {
        accounts.trade(data, type)
    }

    internal fun closePosition(data: String?, type: ClosePositionInputField) {
        accounts.closePosition(data, type)
    }

    internal fun triggerOrders(data: String?, type: TriggerOrdersInputField?) {
        accounts.triggerOrders(data, type)
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

    internal fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        return accounts.commitClosePosition(currentHeight, callback)
    }

    internal fun stopWatchingLastOrder() {
        accounts.stopWatchingLastOrder()
    }

    internal fun cancelOrder(orderId: String, callback: TransactionCallback) {
        accounts.cancelOrder(orderId, callback)
    }

    internal fun orderCanceled(orderId: String) {
        accounts.orderCanceled(orderId)
    }

    internal fun faucet(amount: Double, callback: TransactionCallback) {
        accounts.faucet(amount, callback)
    }

    internal fun transfer(data: String?, type: TransferInputField?) {
        val address = accountAddress
        val source = sourceAddress
        if (address != null && source != null) {
            onboarding.transfer(data, type, address, source, subaccountNumber)
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

    private fun updateRestriction(indexerRestriction: UsageRestriction?) {
        restriction = indexerRestriction ?: accounts.addressRestriction ?: UsageRestriction.noRestriction
    }

    private fun didSetRestriction(restriction: UsageRestriction?) {
        val state = stateMachine.state
        stateMachine.state = PerpetualState(
            state?.assets,
            state?.marketsSummary,
            state?.orderbooks,
            state?.candles,
            state?.trades,
            state?.historicalFundings,
            state?.wallet,
            state?.account,
            state?.historicalPnl,
            state?.fills,
            state?.transfers,
            state?.fundingPayments,
            state?.configs,
            state?.input,
            state?.availableSubaccountNumbers ?: iListOf(),
            state?.transferStatuses,
            restriction,
            state?.launchIncentive,
        )
        ioImplementations.threading?.async(ThreadingType.main) {
            stateNotification?.stateChanged(
                stateMachine.state,
                StateChanges(
                    iListOf(Changes.restriction),
                ),
            )
        }
    }
}
