package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.Compliance
import exchange.dydx.abacus.output.ComplianceAction
import exchange.dydx.abacus.output.ComplianceStatus
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.TransferRecordType
import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputGoodUntil
import exchange.dydx.abacus.output.input.TriggerOrder
import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.Changes.candles
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.configs.StateManagerConfigs
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.manager.utils.Address
import exchange.dydx.abacus.state.manager.utils.DydxAddress
import exchange.dydx.abacus.state.manager.utils.EvmAddress
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.PerpTradingStateMachine
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.state.model.account
import exchange.dydx.abacus.state.model.adjustIsolatedMargin
import exchange.dydx.abacus.state.model.candles
import exchange.dydx.abacus.state.model.closePosition
import exchange.dydx.abacus.state.model.findOrder
import exchange.dydx.abacus.state.model.historicalFundings
import exchange.dydx.abacus.state.model.historicalPnl
import exchange.dydx.abacus.state.model.orderCanceled
import exchange.dydx.abacus.state.model.receivedBatchOrderbookChanges
import exchange.dydx.abacus.state.model.receivedBatchedCandlesChanges
import exchange.dydx.abacus.state.model.receivedBatchedMarketsChanges
import exchange.dydx.abacus.state.model.receivedBatchedTradesChanges
import exchange.dydx.abacus.state.model.receivedCandles
import exchange.dydx.abacus.state.model.receivedCandlesChanges
import exchange.dydx.abacus.state.model.receivedFills
import exchange.dydx.abacus.state.model.receivedHistoricalTradingRewards
import exchange.dydx.abacus.state.model.receivedMarkets
import exchange.dydx.abacus.state.model.receivedMarketsChanges
import exchange.dydx.abacus.state.model.receivedOrderbook
import exchange.dydx.abacus.state.model.receivedSubaccountSubscribed
import exchange.dydx.abacus.state.model.receivedSubaccountsChanges
import exchange.dydx.abacus.state.model.receivedTrades
import exchange.dydx.abacus.state.model.receivedTradesChanges
import exchange.dydx.abacus.state.model.receivedTransfers
import exchange.dydx.abacus.state.model.setOrderbookGrouping
import exchange.dydx.abacus.state.model.sparklines
import exchange.dydx.abacus.state.model.trade
import exchange.dydx.abacus.state.model.tradeInMarket
import exchange.dydx.abacus.state.model.transfer
import exchange.dydx.abacus.state.model.triggerOrders
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.GoodTil
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_DURATION
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import exchange.dydx.abacus.utils.values
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.iSetOf
import kollections.toIList
import kollections.toIMap
import kollections.toISet
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.collections.mutableMapOf
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.times
import kotlin.time.toDuration

@JsExport
open class StateManagerAdaptor(
    val deploymentUri: String,
    val environment: V4Environment,
    val ioImplementations: IOImplementations,
    val uiImplementations: UIImplementations,
    open val configs: StateManagerConfigs,
    val appConfigs: AppConfigs,
    var stateNotification: StateNotificationProtocol?,
    var dataNotification: DataNotificationProtocol?,
) {
    @Suppress("LocalVariableName", "PropertyName")
    private val TRIGGER_ORDER_DEFAULT_DURATION_DAYS = 28.0

    var stateMachine: TradingStateMachine = PerpTradingStateMachine(
        environment,
        uiImplementations.localizer,
        Formatter(uiImplementations.formatter),
        127,
        false,
    )

    internal var indexerConfig: IndexerURIs?
        get() = configs.indexerConfig
        set(value) {
            if (configs.indexerConfig != value) {
                configs.indexerConfig = value
                didSetIndexerConfig()
            }
        }
    private var indexerTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }
    internal val serverPollingDuration = 10.0

    internal val jsonEncoder = JsonEncoder()
    internal val parser = Parser()
    private val notificationsProvider =
        NotificationsProvider(uiImplementations, environment, parser, jsonEncoder)

    private var subaccountsTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }
    private val subaccountsPollingDelay = 15.0
    private var sparklinesTimer: LocalTimerProtocol? = null
    private val sparklinesPollingDuration = 60.0

    internal var lastIndexerCallTime: Instant? = null

    var readyToConnect: Boolean = false
        internal set(value) {
            if (field != value) {
                field = value
                didSetReadyToConnect(field)
            }
        }

    internal var socketConnected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                didSetSocketConnected(socketConnected)
            }
        }

    var accountAddress: String? = null
        internal set(value) {
            if (field != value) {
                val oldValue = field
                field = value
                didSetAccountAddress(accountAddress, oldValue)
            }
        }

    private var accountAddressTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private var accountAddressRestriction: Restriction? = null
        set(value) {
            if (field != value) {
                field = value
                didSetAccountAddressRestriction(value)
            }
        }

    var sourceAddress: String? = null
        internal set(value) {
            if (field != value) {
                val oldValue = field
                field = value
                didSetSourceAddress(sourceAddress, oldValue)
            }
        }

    private val addressRetryDuration = 10.0
    private val addressContinuousMonitoringDuration = 60.0 * 60.0

    private var sourceAddressTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private var historicalFundingTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private var sourceAddressRestriction: Restriction? = null
        set(value) {
            if (field != value) {
                field = value
                didSetSourceAddressRestriction(value)
            }
        }

    internal var addressRestriction: UsageRestriction? = null
        set(value) {
            if (field != value) {
                field = value
                didSetAddressRestriction(value)
            }
        }

    internal open var restriction: UsageRestriction = UsageRestriction.noRestriction
        set(value) {
            if (field != value) {
                field = value
                didSetRestriction(value)
            }
        }

    private var compliance: Compliance = Compliance(null, ComplianceStatus.COMPLIANT)
        set(value) {
            if (field != value) {
                field = value
                didSetCompliance(value)
            }
        }

    var subaccountNumber: Int = 0
        internal set(value) {
            if (field != value) {
                field = value
                didSetSubaccountNumber(value)
            }
        }

    var connectedSubaccountNumber: Int? = null
        internal set(value) {
            if (field != value) {
                field = value
                val accountAddress = this.accountAddress
                val connectedSubaccountNumber = this.connectedSubaccountNumber
                if (subaccount?.address != accountAddress || subaccount?.subaccountNumber != connectedSubaccountNumber) {
                    subaccount = if (accountAddress != null && connectedSubaccountNumber != null) {
                        Subaccount(accountAddress, connectedSubaccountNumber)
                    } else {
                        null
                    }
                }
            }
        }

    internal var subaccount: Subaccount? = null
        internal set(value) {
            if (field != value) {
                val oldValue = field
                field = value
                didSetSubaccount(subaccount, oldValue)
            }
        }

    var orderbookGrouping: OrderbookGrouping = OrderbookGrouping.none
        internal set(value) {
            if (field != value) {
                field = value
                didSetOrderbookGrouping()
            }
        }

    var market: String? = null
        internal set(value) {
            if (field != value) {
                val oldValue = field
                field = value
                didSetMarket(market, oldValue)
            }
        }

    var historicalTradingRewardPeriod: HistoricalTradingRewardsPeriod =
        HistoricalTradingRewardsPeriod.WEEKLY
        internal set(value) {
            if (field != value) {
                field = value
                didSetHistoricalTradingRewardsPeriod(value.name)
            }
        }

    var historicalPnlPeriod: HistoricalPnlPeriod
        get() {
            return when (stateMachine.historicalPnlDays) {
                1 -> HistoricalPnlPeriod.Period1d
                7 -> HistoricalPnlPeriod.Period7d
                30 -> HistoricalPnlPeriod.Period30d
                90 -> HistoricalPnlPeriod.Period90d
                else -> HistoricalPnlPeriod.Period1d
            }
        }
        internal set(value) {
            if (historicalPnlPeriod != value) {
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    when (value) {
                        HistoricalPnlPeriod.Period1d -> stateMachine.historicalPnlDays = 1
                        HistoricalPnlPeriod.Period7d -> stateMachine.historicalPnlDays = 7
                        HistoricalPnlPeriod.Period30d -> stateMachine.historicalPnlDays = 30
                        HistoricalPnlPeriod.Period90d -> stateMachine.historicalPnlDays = 90
                    }
                    didSetHistoricalPnlPeriod()

                    val changes = StateChanges(iListOf(Changes.historicalPnl))
                    val oldState = stateMachine.state
                    update(changes, oldState)
                }
            }
        }

    var candlesResolution: String = "1DAY"
        internal set(value) {
            if (field != value) {
                val oldValue = field
                field = value
                didSetCandlesResolution(oldValue)
            }
        }

    internal var faucetRecords: IMutableList<FaucetRecord> = iMutableListOf()
        internal set(value) {
            if (field !== value) {
                field = value
                didSetFaucetRecords()
            }
        }

    internal var placeOrderRecords: IMutableList<PlaceOrderRecord> = iMutableListOf()
        internal set(value) {
            if (field !== value) {
                field = value
                didSetPlaceOrderRecords()
            }
        }

    internal var cancelOrderRecords: IMutableList<CancelOrderRecord> = iMutableListOf()
        internal set(value) {
            if (field !== value) {
                field = value
                didSetCancelOrderRecords()
            }
        }

    internal var lastOrderClientId: Int? = null
        set(value) {
            if (field != value) {
                field = value
                lastOrder = if (value != null) {
                    stateMachine.findOrder(value, subaccountNumber)
                } else {
                    null
                }
            }
        }

    private var lastOrder: SubaccountOrder? = null
        set(value) {
            if (field !== value) {
                field = value
                stateNotification?.lastOrderChanged(lastOrder)
                dataNotification?.lastOrderChanged(lastOrder)
            }
        }

    internal var notifications: IMap<String, Notification> = iMapOf()
        set(value) {
            if (field !== value) {
                field = value
                sendNotifications()
            }
        }

    open fun didSetReadyToConnect(readyToConnect: Boolean) {
        if (readyToConnect) {
            bestEffortConnectIndexer()
            fetchGeo()
        } else {
            indexerConfig = null
        }
    }

    internal open fun bestEffortConnectIndexer() {
        findOptimalIndexer { config ->
            this.indexerConfig = config
        }
    }

    internal open fun findOptimalIndexer(callback: (config: IndexerURIs?) -> Unit) {
        val first = configs.indexerConfigs?.firstOrNull()
        ioImplementations.threading?.async(ThreadingType.abacus) {
            callback(first)
        }
    }

    internal open fun didSetIndexerConfig() {
        if (indexerConfig != null) {
            connectSocket()
            retrieveServerTime()
            retrieveMarketConfigs()
            retrieveFeeTiers()
            if (market != null) {
                retrieveMarketHistoricalFundings()
                maybeRetrieveMarketCandles()
            }
            if (sourceAddress != null) {
                screenSourceAddress()
                sourceAddress?.let { complianceScreen(EvmAddress(it)) }
            }
            if (accountAddress != null) {
                screenAccountAddress()
                accountAddress?.let {
                    complianceScreen(DydxAddress(it))
                }
                retrieveAccount()
                retrieveAccountHistoricalTradingRewards()
            }
            if (subaccount != null) {
                retrieveSubaccountFills()
                retrieveSubaccountTransfers()
                retrieveSubaccountHistoricalPnls()
            }
        } else {
            disconnectSocket()
            sparklinesTimer = null
            reconnectIndexer()
        }
    }

    private fun reconnectIndexer() {
        if (readyToConnect) {
            // Create a timer, to try to connect the chain again
            // Do not repeat. This timer is recreated in bestEffortConnectChain if needed
            val timer = ioImplementations.timer ?: CoroutineTimer.instance
            indexerTimer = timer.schedule(serverPollingDuration, null) {
                if (readyToConnect) {
                    bestEffortConnectIndexer()
                }
                false
            }
        }
    }

    internal open fun didSetAccountAddress(accountAddress: String?, oldValue: String?) {
        val stateResponse = stateMachine.resetWallet(accountAddress)
        ioImplementations.threading?.async(ThreadingType.main) {
            stateNotification?.stateChanged(
                stateResponse.state,
                stateResponse.changes,
            )
            connectedSubaccountNumber = null
            subaccountNumber = 0
            updateConnectedSubaccountNumber()
        }
        accountAddressTimer = null
        accountAddressRestriction = null

        subaccountsTimer = null
        screenAccountAddress()
        accountAddress?.let {
            complianceScreen(DydxAddress(it))
        }
        retrieveAccountHistoricalTradingRewards()
    }

    private fun didSetAccountAddressRestriction(accountAddressRestriction: Restriction?) {
        updateAddressRestriction()
    }

    private fun didSetSourceAddress(sourceAddress: String?, oldValue: String?) {
        sourceAddressTimer = null
        sourceAddressRestriction = null
        screenSourceAddress()
        sourceAddress?.let { complianceScreen(EvmAddress(it)) }
    }

    internal open fun didSetSubaccountNumber(subaccountNumber: Int) {
        updateConnectedSubaccountNumber()
    }

    private fun updateConnectedSubaccountNumber() {
        if (connectedSubaccountNumber != subaccountNumber) {
            connectedSubaccountNumber = if (canConnectTo(subaccountNumber)) {
                subaccountNumber
            } else {
                null
            }
        }
    }

    internal open fun canConnectTo(subaccountNumber: Int): Boolean {
        return stateMachine.state?.subaccount(subaccountNumber) != null
    }

    internal open fun didSetSubaccount(subaccount: Subaccount?, oldValue: Subaccount?) {
        if (subaccount !== oldValue) {
            if (readyToConnect) {
                if (oldValue != null) {
                    // unsubscribe existing subaccount channel
                    subaccountChannelSubscription(
                        oldValue.address,
                        oldValue.subaccountNumber,
                        false,
                    )
                }

                if (subaccount != null) {
                    if (socketConnected) {
                        subaccountChannelSubscription(subaccount.address, subaccountNumber, true)
                    }
                    retrieveSubaccountFills()
                    retrieveSubaccountTransfers()
                    retrieveSubaccountHistoricalPnls()
                }
            }
        }
    }

    private fun connectSocket() {
        val webscoketUrl = configs.websocketUrl()
        if (webscoketUrl != null) {
            ioImplementations.webSocket?.connect(webscoketUrl, connected = { connected ->
                if (!connected) {
                    // Do not set socketConnected to true here, wait for the "connected" message
                    socketConnected = false
                }
            }, received = { message ->
                processSocketResponse(message)
            })
        }
    }

    private fun disconnectSocket() {
        ioImplementations.webSocket?.disconnect()
    }

    open fun didSetSocketConnected(socketConnected: Boolean) {
        if (socketConnected) {
            marketsChannelSubscription(true)
            val market = market
            if (market != null) {
                marketTradesChannelSubscription(market, true)
                marketOrderbookChannelSubscription(market, true)
            }
            val accountAddress = accountAddress
            val connectedSubaccountNumber = connectedSubaccountNumber
            if (accountAddress != null && connectedSubaccountNumber != null) {
                subaccountChannelSubscription(accountAddress, connectedSubaccountNumber, true)
            }
        } else {
            if (readyToConnect) {
                connectSocket()
            }
        }
    }

    fun socketAction(subscribe: Boolean): String {
        return if (subscribe) "subscribe" else "unsubscribe"
    }

    internal fun socket(
        type: String,
        channel: String,
        params: IMap<String, Any>? = null,
    ) {
        val request = mutableMapOf<String, Any>("type" to type, "channel" to channel)
        if (params != null) {
            for ((key, value) in params) {
                request[key] = value
            }
        }
        val message = jsonEncoder.encode(request)
        ioImplementations.webSocket?.send(message)
    }

    @Throws(Exception::class)
    private fun marketsChannelSubscription(subscribe: Boolean = true) {
        val channel = configs.marketsChannel() ?: throw Exception("market is null")
        socket(
            socketAction(subscribe),
            channel,
            if (subscribe && shouldBatchMarketsChannelData()) {
                iMapOf("batched" to "true")
            } else {
                null
            },
        )
    }

    open fun shouldBatchMarketsChannelData(): Boolean {
        return false
    }

    @Throws(Exception::class)
    private fun marketTradesChannelSubscription(market: String, subscribe: Boolean = true) {
        val channel = configs.marketTradesChannel() ?: throw Exception("trades channel is null")
        socket(
            socketAction(subscribe),
            channel,
            if (subscribe && shouldBatchMarketTradesChannelData()) {
                iMapOf("id" to market, "batched" to "true")
            } else {
                iMapOf("id" to market)
            },
        )
    }

    open fun shouldBatchMarketTradesChannelData(): Boolean {
        return false
    }

    @Throws(Exception::class)
    private fun marketOrderbookChannelSubscription(market: String, subscribe: Boolean = true) {
        val channel =
            configs.marketOrderbookChannel() ?: throw Exception("orderbook channel is null")
        socket(
            socketAction(subscribe),
            channel,
            if (subscribe && shouldBatchMarketOrderbookChannelData()) {
                iMapOf("id" to market, "batched" to "true")
            } else {
                iMapOf("id" to market)
            },
        )
    }

    open fun shouldBatchMarketOrderbookChannelData(): Boolean {
        return true
    }

    @Throws(Exception::class)
    private fun subaccountChannelSubscription(
        accountAddress: String,
        subaccountNumber: Int,
        subscribe: Boolean = true,
    ) {
        val channel =
            configs.subaccountChannel(false) ?: throw Exception("subaccount channel is null")
        socket(
            socketAction(subscribe),
            channel,
            subaccountChannelParams(accountAddress, subaccountNumber),
        )
    }

    open fun subaccountChannelParams(
        accountAddress: String,
        subaccountNumber: Int,
    ): IMap<String, Any> {
        TODO("Not yet implemented")
    }

    private fun processSocketResponse(message: String) {
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
        val oldState = stateMachine.state

        val type = parser.asString(payload["type"])
        val id = parser.asString(payload["id"])

        var changes: StateChanges? = null
        try {
            when (type) {
                "subscribed" -> {
                    val channel = parser.asString(payload["channel"]) ?: return
                    val content = parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = socketSubscribed(channel, id, subaccountNumber, content)
                }

                "unsubscribed" -> {}

                "channel_data" -> {
                    val channel = parser.asString(payload["channel"]) ?: return
                    val info = SocketInfo(type, channel, id, parser.asInt(payload["subaccountNumber"]))
                    val content = parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = socketChannelData(channel, id, subaccountNumber, info, content)
                }

                "channel_batch_data" -> {
                    val channel = parser.asString(payload["channel"]) ?: return
                    val info = SocketInfo(type, channel, id, parser.asInt(payload["subaccountNumber"]))
                    val content = parser.asList(payload["contents"]) as? IList<IMap<String, Any>>
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = socketChannelBatchData(channel, id, subaccountNumber, content)
                }

                "connected" -> {
                    socketConnected = true
                }

                "error" -> {
                    throw ParsingException(ParsingErrorType.BackendError, payload.toString())
                }

                else -> {
                    throw ParsingException(
                        ParsingErrorType.Unhandled,
                        "Type [ $type ] is not handled",
                    )
                }
            }
            update(changes, oldState)
        } catch (e: ParsingException) {
            val error = ParsingError(
                e.type,
                e.message ?: "Unknown error",
            )
            emitError(error)
        }
    }

    internal fun emitError(error: ParsingError) {
        ioImplementations.threading?.async(ThreadingType.main) {
            stateNotification?.errorsEmitted(iListOf(error))
            dataNotification?.errorsEmitted(iListOf(error))
        }
    }

    internal fun update(changes: StateChanges?, oldState: PerpetualState?) {
        if (changes != null) {
            var realChanges = changes
            changes.let {
                realChanges = stateMachine.update(it)
            }
            if (realChanges != null) {
                ioImplementations.threading?.async(ThreadingType.main) {
                    updateStateChanges(stateMachine.state, realChanges, oldState)
                }
                updateTracking(changes = realChanges!!)
                updateNotifications()
            }
        }
    }

    private fun updateStateChanges(
        state: PerpetualState?,
        changes: StateChanges?,
        oldState: PerpetualState?,
    ) {
        if (changes != null) {
            val stateNotification = this.stateNotification
            val dataNotification = this.dataNotification
            stateNotification?.stateChanged(
                stateMachine.state,
                changes,
            )
            if (dataNotification != null) {
                if (state?.marketsSummary !== oldState?.marketsSummary) {
                    dataNotification.marketsSummaryChanged(state?.marketsSummary)
                }
                val marketIds = state?.marketIds()?.toISet() ?: iSetOf()
                val oldMarketIds = oldState?.marketIds()?.toISet() ?: iSetOf()
                val merged = marketIds.union(oldMarketIds)
                for (marketId in merged) {
                    val market = state?.market(marketId)
                    val oldMarket = oldState?.market(marketId)
                    if (market !== oldMarket) {
                        dataNotification.marketChanged(market, marketId)
                        val sparklines = market?.perpetual?.line
                        val oldSparklines = oldMarket?.perpetual?.line
                        if (sparklines !== oldSparklines) {
                            dataNotification.marketSparklinesChanged(sparklines, marketId)
                        }
                    }

                    val trades = state?.marketTrades(marketId)
                    val oldTrades = oldState?.marketTrades(marketId)
                    if (trades !== oldTrades) {
                        dataNotification.marketTradesChanged(trades, marketId)
                    }

                    val orderbook = state?.marketOrderbook(marketId)
                    val oldOrderbook = oldState?.marketOrderbook(marketId)
                    if (orderbook !== oldOrderbook) {
                        dataNotification.marketOrderbookChanged(orderbook, marketId)
                    }

                    val marketHistoricalFunding = state?.historicalFunding(marketId)
                    val oldMarketHistoricalFunding = oldState?.historicalFunding(marketId)
                    if (marketHistoricalFunding !== oldMarketHistoricalFunding) {
                        dataNotification.marketHistoricalFundingChanged(
                            marketHistoricalFunding,
                            marketId,
                        )
                    }

                    val marketCandles = state?.marketCandles(marketId)
                    val oldMarketCandles = oldState?.marketCandles(marketId)
                    if (marketCandles !== oldMarketCandles) {
                        val candleResolutions = marketCandles?.candles?.keys?.toISet() ?: iSetOf()
                        val oldCandleResolutions =
                            oldMarketCandles?.candles?.keys?.toISet() ?: iSetOf()
                        val mergedCandleResolutions = candleResolutions.union(oldCandleResolutions)
                        for (resolution in mergedCandleResolutions) {
                            val candles = marketCandles?.candles?.get(resolution)
                            val oldCandles = oldMarketCandles?.candles?.get(resolution)
                            if (candles !== oldCandles) {
                                dataNotification.marketCandlesChanged(candles, marketId, resolution)
                            }
                        }
                    }
                }

                if (state?.wallet !== oldState?.wallet) {
                    dataNotification.walletChanged(state?.wallet)
                }
                val subaccountIds = state?.availableSubaccountNumbers?.toISet() ?: iSetOf()
                val oldSubaccountIds = oldState?.availableSubaccountNumbers?.toISet() ?: iSetOf()
                val mergedSubaccountIds = subaccountIds.union(oldSubaccountIds)
                for (subaccountId in mergedSubaccountIds) {
                    val subaccount = state?.subaccount(subaccountId)
                    val oldSubaccount = oldState?.subaccount(subaccountId)
                    if (subaccount !== oldSubaccount) {
                        dataNotification.subaccountChanged(subaccount, subaccountId)
                    }

                    val fills = state?.subaccountFills(subaccountId)
                    val oldFills = oldState?.subaccountFills(subaccountId)
                    if (fills !== oldFills) {
                        dataNotification.subaccountFillsChanged(fills, subaccountId)
                    }

                    val historicalPNL = state?.subaccountHistoricalPnl(subaccountId)
                    val oldHistoricalPNL = oldState?.subaccountHistoricalPnl(subaccountId)
                    if (historicalPNL !== oldHistoricalPNL) {
                        dataNotification.subaccountHistoricalPnlChanged(historicalPNL, subaccountId)
                    }

                    val transfers = state?.subaccountTransfers(subaccountId)
                    val oldTransfers = oldState?.subaccountTransfers(subaccountId)
                    if (transfers !== oldTransfers) {
                        dataNotification.subaccountTransfersChanged(transfers, subaccountId)
                    }

                    val fundingPayments = state?.subaccountFundingPayments(subaccountId)
                    val oldFundingPayments = oldState?.subaccountFundingPayments(subaccountId)
                    if (fundingPayments !== oldFundingPayments) {
                        dataNotification.subaccountFundingPaymentsChanged(
                            fundingPayments,
                            subaccountId,
                        )
                    }
                }

                val transferHashes = state?.transferStatuses?.keys?.toISet() ?: iSetOf()
                val oldTransferHashes = oldState?.transferStatuses?.keys?.toISet() ?: iSetOf()
                val mergedTransferHashes = transferHashes.union(oldTransferHashes)
                for (transferHash in mergedTransferHashes) {
                    val transferStatus = state?.transferStatuses?.get(transferHash)
                    val oldTransferStatus = oldState?.transferStatuses?.get(transferHash)
                    if (transferStatus !== oldTransferStatus) {
                        dataNotification.transferStatusChanged(transferStatus, transferHash)
                    }
                }

                val input = state?.input
                val oldInput = oldState?.input
                if (input !== oldInput) {
                    dataNotification.inputChanged(input)
                }

                val feeTiers = state?.configs?.feeTiers
                val oldFeeTiers = oldState?.configs?.feeTiers
                if (feeTiers !== oldFeeTiers) {
                    dataNotification.feeTiersChanged(feeTiers)
                }
            }
            val lastOrderClientId = this.lastOrderClientId
            if (lastOrderClientId != null) {
                lastOrder = stateMachine.findOrder(lastOrderClientId, subaccountNumber)
            }
        }
    }

    @Throws(Exception::class)
    private fun socketSubscribed(
        channel: String,
        id: String?,
        subaccountNumber: Int?,
        content: IMap<String, Any>,
    ): StateChanges? {
        return when (channel) {
            configs.marketsChannel() -> {
                val changes = stateMachine.receivedMarkets(content, subaccountNumber ?: 0)
                refreshSparklines()
                changes
            }

            configs.subaccountChannel(false), configs.subaccountChannel(true) -> {
                stateMachine.receivedSubaccountSubscribed(content, height())
            }

            configs.marketOrderbookChannel() -> {
                stateMachine.receivedOrderbook(id, content, subaccountNumber ?: 0)
            }

            configs.marketTradesChannel() -> {
                stateMachine.receivedTrades(id, content)
            }

            configs.marketCandlesChannel() -> {
                val (market, resolution) = splitCandlesChannel(id)
                stateMachine.receivedCandles(market, resolution, content)
            }

            else -> {
                throw ParsingException(
                    ParsingErrorType.UnknownChannel,
                    "$channel is not known",
                )
            }
        }
    }

    private fun splitCandlesChannel(channel: String?): Pair<String, String> {
        if (channel == null) {
            throw ParsingException(
                ParsingErrorType.UnknownChannel,
                "$channel is not known",
            )
        }
        val marketAndResolution = channel.split("/")
        if (marketAndResolution.size != 2) {
            throw ParsingException(
                ParsingErrorType.UnknownChannel,
                "$channel is not known",
            )
        }
        val market = marketAndResolution[0]
        val resolution = marketAndResolution[1]
        return Pair(market, resolution)
    }

    open fun socketConnectedSubaccountNumber(id: String?): Int {
        return 0
    }

    @Throws(Exception::class)
    private fun socketChannelData(
        channel: String,
        id: String?,
        subaccountNumber: Int?,
        info: SocketInfo,
        content: IMap<String, Any>,
    ): StateChanges? {
        return when (channel) {
            configs.marketsChannel() -> {
                stateMachine.receivedMarketsChanges(content, subaccountNumber ?: 0)
            }

            configs.subaccountChannel(false), configs.subaccountChannel(true) -> {
                stateMachine.receivedSubaccountsChanges(content, info, height())
            }

            configs.marketOrderbookChannel() -> {
                throw ParsingException(
                    ParsingErrorType.UnhandledEndpoint,
                    "channel_data for $channel is not implemented",
                )
            }

            configs.marketTradesChannel() -> {
                stateMachine.receivedTradesChanges(id, content)
            }

            configs.marketCandlesChannel() -> {
                val (market, resolution) = splitCandlesChannel(id)
                stateMachine.receivedCandlesChanges(market, resolution, content)
            }

            else -> {
                throw ParsingException(
                    ParsingErrorType.UnknownChannel,
                    "$channel is not known",
                )
            }
        }
    }

    @Throws(Exception::class)
    private fun socketChannelBatchData(
        channel: String,
        id: String?,
        subaccountNumber: Int?,
        content: IList<IMap<String, Any>>,
    ): StateChanges? {
        return when (channel) {
            configs.marketsChannel() -> {
                stateMachine.receivedBatchedMarketsChanges(
                    content,
                    subaccountNumber ?: 0,
                )
            }

            configs.marketTradesChannel() -> {
                stateMachine.receivedBatchedTradesChanges(id, content)
            }

            configs.marketOrderbookChannel() -> {
                stateMachine.receivedBatchOrderbookChanges(
                    id,
                    content,
                    subaccountNumber ?: 0,
                )
            }

            configs.marketCandlesChannel() -> {
                val (market, resolution) = splitCandlesChannel(id)
                stateMachine.receivedBatchedCandlesChanges(market, resolution, content)
            }

            else -> {
                throw ParsingException(
                    ParsingErrorType.UnknownChannel,
                    "$channel is not known",
                )
            }
        }
    }

    open fun faucetBody(amount: Double): String? {
        return null
    }

    open fun retrieveFeeTiers() {
    }

    fun get(
        url: String,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        callback: (url: String, response: String?, code: Int, headers: Map<String, Any>?) -> Unit,
    ) {
        val fullUrl = fullUrl(url, params)

        getWithFullUrl(fullUrl, headers, callback)
    }

    private fun fullUrl(
        url: String,
        params: Map<String, String>?,
    ): String {
        return if (params != null) {
            val queryString = params.toIMap().joinToString("&") { "${it.key}=${it.value}" }
            "$url?$queryString"
        } else {
            url
        }
    }

    open fun getWithFullUrl(
        fullUrl: String,
        headers: Map<String, String>?,
        callback: (url: String, response: String?, code: Int, headers: Map<String, Any>?) -> Unit,
    ) {
        ioImplementations.threading?.async(ThreadingType.network) {
            ioImplementations.rest?.get(fullUrl, headers?.toIMap()) { response, httpCode, headersAsJsonString ->
                val time = if (configs.isIndexer(fullUrl) && success(httpCode)) {
                    Clock.System.now()
                } else {
                    null
                }

                ioImplementations.threading?.async(ThreadingType.abacus) {
                    if (time != null) {
                        this.lastIndexerCallTime = time
                    }
                    try {
                        val headers = parser.decodeJsonObject(headersAsJsonString)
                        callback(fullUrl, response, httpCode, headers)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val error = ParsingError(
                            ParsingErrorType.Unhandled,
                            e.message ?: "Unknown error",
                        )
                        emitError(error)
                    }
                    trackApiCall()
                }
            }
        }
    }

    @Throws(Exception::class)
    internal open fun transaction(
        type: TransactionType,
        paramsInJson: String?,
        callback: (response: String) -> Unit,
    ) {}

    internal open fun trackApiCall() {
    }

    internal open fun privateHeaders(
        path: String,
        verb: String,
        params: Map<String, String>?,
        headers: Map<String, String>?,
        body: String?,
    ): IMap<String, String>? {
        return headers?.toIMap()
    }

    fun post(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: (String, String?, Int, Map<String, Any>?) -> Unit,
    ) {
        ioImplementations.threading?.async(ThreadingType.main) {
            ioImplementations.rest?.post(url, headers, body) { response, httpCode, headersAsJsonString ->
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    val headers = parser.decodeJsonObject(headersAsJsonString)
                    callback(url, response, httpCode, headers)
                }
            }
        }
    }

    private fun retrieveServerTime() {
        val url = configs.publicApiUrl("time")
        if (url != null) {
            get(url, null, null) { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    val json = parser.decodeJsonObject(response)
                    val time = parser.asDatetime(json?.get("time"))
                    if (time != null) {
                        ServerTime.overWrite = time
                    }
                }
            }
        }
    }

    private fun retrieveMarketConfigs() {
        val oldState = stateMachine.state
        val url = configs.configsUrl("markets")
        if (url != null) {
            get(url, null, null) { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    update(
                        stateMachine.configurations(response, subaccountNumber, deploymentUri),
                        oldState,
                    )
                }
            }
        }
    }

    private fun refreshSparklines() {
        if (sparklinesTimer == null) {
            val timer = ioImplementations.timer ?: CoroutineTimer.instance
            sparklinesTimer = timer.schedule(0.0, sparklinesPollingDuration) {
                if (readyToConnect) {
                    retrieveSparklines()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun retrieveSparklines() {
        val url = configs.publicApiUrl("sparklines")
        if (url != null) {
            get(url, sparklinesParams(), null) { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    parseSparklinesResponse(response)
                }
            }
        }
    }

    open fun parseSparklinesResponse(response: String) {
        val oldState = stateMachine.state
        update(stateMachine.sparklines(response), oldState)
    }

    internal open fun sparklinesParams(): IMap<String, String>? {
        return null
    }

    private fun maybeRetrieveMarketCandles() {
        if (!appConfigs.subscribeToCandles) return
        val market = market ?: return
        val url = configs.publicApiUrl("candles") ?: return
        val candleResolution = candlesResolution
        val resolutionDuration =
            candleOptionDuration(stateMachine, market, candleResolution) ?: return
        val maxDuration = resolutionDuration * 365
        val marketCandles = parser.asList(
            parser.value(
                stateMachine.data,
                "markets.markets.$market.candles.$candleResolution",
            ),
        )

        return retrieveTimed(
            "$url/$market",
            marketCandles,
            "startedAt",
            resolutionDuration,
            maxDuration,
            "toISO",
            "fromISO",
            mapOf(
                "resolution" to candleResolution,
            ),
            null,
        ) { url, response, httpCode, _ ->
            val oldState = stateMachine.state
            if (success(httpCode) && response != null) {
                val changes = stateMachine.candles(response)
                update(changes, oldState)
                if (changes.changes.contains(candles)) {
                    maybeRetrieveMarketCandles()
                }
            }
        }
    }

    private fun candleOptionDuration(
        stateMachine: TradingStateMachine?,
        market: String,
        candleResolution: String,
    ): Duration? {
        val options =
            stateMachine?.state?.marketsSummary?.markets?.get(market)?.configs?.candleOptions
        val option = options?.firstOrNull {
            it.value == candleResolution
        }
        return option?.seconds?.seconds
    }

    private fun retrieveMarketHistoricalFundings() {
        historicalFundingTimer = null
        val oldState = stateMachine.state
        val url = configs.publicApiUrl("historical-funding") ?: return
        val market = market ?: return
        get("$url/$market", null, null, callback = { _, response, httpCode, _ ->
            if (success(httpCode) && response != null) {
                update(stateMachine.historicalFundings(response), oldState)
            }
            ioImplementations.threading?.async(ThreadingType.main) {
                val nextHour = calculateNextFundingAt()
                val delay = nextHour - ServerTime.now()
                this.historicalFundingTimer = ioImplementations.timer?.schedule(
                    // Give 30 seconds past the hour to make sure the funding is available
                    (delay + 30.seconds).inWholeSeconds.toDouble(),
                    null,
                ) {
                    retrieveMarketHistoricalFundings()
                    false
                }
            }
        })
    }

    private fun calculateNextFundingAt(): Instant {
        return nextHour()
        // Can use nextMinute() for testing
        // return nextMinute()
    }

    private fun nextHour(): Instant {
        val now: Instant = ServerTime.now()
        val time = now.toLocalDateTime(TimeZone.UTC)
        val minute = time.minute
        val second = time.second
        val nanosecond = time.nanosecond
        val duration =
            nanosecond.toDuration(DurationUnit.NANOSECONDS) +
                second.toDuration(DurationUnit.SECONDS) +
                minute.toDuration(DurationUnit.MINUTES)

        return now.minus(duration).plus(1.hours)
    }

    private fun nextMinute(): Instant {
        val now: Instant = ServerTime.now()
        val time = now.toLocalDateTime(TimeZone.UTC)
        val second = time.second
        val nanosecond = time.nanosecond
        val duration =
            nanosecond.toDuration(DurationUnit.NANOSECONDS) +
                second.toDuration(DurationUnit.SECONDS)

        return now.minus(duration).plus(1.minutes)
    }

    open fun retrieveAccount() {
        val oldState = stateMachine.state
        val url = accountUrl()
        if (url != null) {
            get(url, null, null, callback = { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.account(response), oldState)
                    updateConnectedSubaccountNumber()
                } else {
                    subaccountsTimer =
                        ioImplementations.timer?.schedule(subaccountsPollingDelay, null) {
                            retrieveAccount()
                            false
                        }
                }
            })
        }
    }

    open fun retrieveAccountHistoricalTradingRewards(
        period: String = "WEEKLY",
        previousUrl: String? = null
    ) {
        val oldState = stateMachine.state
        var url = historicalTradingRewardAggregationsUrl() ?: return
        val params = historicalTradingRewardAggregationsParams(period)
        val historicalTradingRewardsInPeriod = parser.asNativeList(
            parser.value(
                stateMachine.data,
                "wallet.account.tradingRewards.historical.$period",
            ),
        )?.mutable()

        retrieveTimed(
            url,
            historicalTradingRewardsInPeriod,
            "startedAt",
            0.days,
            180.days,
            "endedAt",
            null,
            params,
            previousUrl,
        ) { url, response, httpCode, _ ->
            if (success(httpCode) && !response.isNullOrEmpty()) {
                val historicalTradingRewards = parser.decodeJsonObject(response)?.toIMap()
                if (historicalTradingRewards != null) {
                    val changes = stateMachine.receivedHistoricalTradingRewards(
                        historicalTradingRewards,
                        period,
                    )
                    update(changes, oldState)
                    if (changes.changes.contains(Changes.tradingRewards)) {
                        retrieveAccountHistoricalTradingRewards(period, url)
                    }
                }
            }
        }
    }

    open fun accountUrl(): String? {
        return null
    }

    open fun screenUrl(): String? {
        return null
    }

    open fun complianceScreenUrl(address: String): String? {
        return null
    }

    open fun complianceGeoblockUrl(): String? {
        return null
    }

    open fun geoUrl(): String? {
        return null
    }

    open fun historicalTradingRewardAggregationsUrl(): String? {
        return null
    }

    internal open fun historicalTradingRewardAggregationsParams(period: String): IMap<String, String>? {
        return null
    }

    private fun retrieveSubaccountHistoricalPnls(previousUrl: String? = null) {
        val url = configs.privateApiUrl("historical-pnl") ?: return
        val params = subaccountParams()
        val historicalPnl = parser.asNativeList(
            parser.value(
                stateMachine.data,
                "wallet.account.subaccounts.$subaccountNumber.historicalPnl",
            ),
        )?.mutable()

        if (historicalPnl != null) {
            val last = parser.asMap(historicalPnl.lastOrNull())
            if (parser.asBool(last?.get("calculated")) == true) {
                historicalPnl.removeLast()
            }
        }

        retrieveTimed(
            url,
            historicalPnl,
            "createdAt",
            1.days,
            180.days,
            "createdBeforeOrAt",
            "createdAtOrAfter",
            params,
            previousUrl,
        ) { url, response, httpCode, _ ->
            val oldState = stateMachine.state
            if (success(httpCode) && !response.isNullOrEmpty()) {
                val changes = stateMachine.historicalPnl(
                    payload = response,
                    subaccountNumber = subaccountNumber,
                )
                update(changes, oldState)
                if (changes.changes.contains(Changes.historicalPnl)) {
                    retrieveSubaccountHistoricalPnls(url)
                }
            }
        }
    }

    private fun retrieveSubaccountFills() {
        val oldState = stateMachine.state
        val url = configs.privateApiUrl("fills")
        val params = subaccountParams()
        if (url != null && params != null) {
            get(url, params, null, callback = { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    val fills = parser.decodeJsonObject(response)?.toIMap()
                    if (fills != null && fills.size != 0) {
                        update(stateMachine.receivedFills(fills, subaccountNumber), oldState)
                    }
                }
            })
        }
    }

    private fun retrieveSubaccountTransfers() {
        val oldState = stateMachine.state
        val url = configs.privateApiUrl("transfers")
        val params = subaccountParams()
        if (url != null && params != null) {
            get(url, params, null, callback = { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    val tranfers = parser.decodeJsonObject(response)
                    if (tranfers != null && tranfers.size != 0) {
                        update(stateMachine.receivedTransfers(tranfers, subaccountNumber), oldState)
                    }
                }
            })
        }
    }

    internal open fun subaccountParams(): IMap<String, String>? {
        return null
    }

    internal fun retrieveTimed(
        url: String,
        items: List<Any>?,
        timeField: String,
        sampleDuration: Duration,
        maxDuration: Duration,
        beforeParam: String,
        afterParam: String? = null,
        additionalParams: Map<String, String>? = null,
        previousUrl: String?,
        callback: (url: String, response: String?, httpCode: Int, headers: Map<String, Any>?) -> Unit,
    ) {
        if (items != null) {
            val lastItemTime =
                parser.asDatetime(
                    parser.asMap(items.lastOrNull())?.get(timeField),
                )
            val firstItemTime =
                parser.asDatetime(
                    parser.asMap(items.firstOrNull())?.get(timeField),
                )
            val now = ServerTime.now()
            if (lastItemTime != null && (now.minus(lastItemTime)) > sampleDuration * 2.0) {
                /*
                Get latest
                 */
                val forwardTime = lastItemTime + 99 * sampleDuration
                val beforeOrAt = if (forwardTime > now) forwardTime else null
                val params = timedParams(
                    beforeOrAt,
                    beforeParam,
                    lastItemTime + 1.seconds,
                    afterParam,
                    additionalParams,
                )
                val fullUrl = fullUrl(url, params)
                if (fullUrl != previousUrl) {
                    getWithFullUrl(fullUrl, null, callback)
                }
            } else if (firstItemTime != null) {
                /*
                Get previous
                 */
                if (now - firstItemTime <= maxDuration) {
                    val beforeOrAt = firstItemTime - 1.seconds
                    val after = beforeOrAt - 99 * sampleDuration
                    val params =
                        timedParams(beforeOrAt, beforeParam, after, afterParam, additionalParams)

                    val fullUrl = fullUrl(url, params)
                    if (fullUrl != previousUrl) {
                        getWithFullUrl(fullUrl, null, callback)
                    }
                }
            }
        } else {
            /*
            Get latest
             */
            val fullUrl = fullUrl(url, additionalParams)
            if (fullUrl != previousUrl) {
                getWithFullUrl(fullUrl, null, callback)
            }
        }
    }

    private fun timedParams(
        before: Instant?,
        beforeParam: String,
        after: Instant?,
        afterParam: String?,
        additionalParams: Map<String, String>? = null,
    ): Map<String, String>? {
        val params = mutableMapOf<String, String>()
        val beforeString = before?.toString()
        if (beforeString != null) {
            params[beforeParam] = beforeString
        }

        val afterString = after?.toString()
        if (afterString != null && afterParam != null) {
            params[afterParam] = afterString
        }

        return if (additionalParams != null) {
            ParsingHelper.merge(params, additionalParams) as? Map<String, String>
        } else {
            params
        }
    }

    fun success(httpCode: Int): Boolean {
        return httpCode in 200..299
    }

    open fun height(): BlockAndTime? {
        return null
    }

    fun didSetOrderbookGrouping() {
        ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse =
                stateMachine.setOrderbookGrouping(market, orderbookGrouping.rawValue)
            ioImplementations.threading?.async(ThreadingType.main) {
                stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    internal open fun didSetCandlesResolution(oldValue: String) {
        maybeRetrieveMarketCandles()
    }

    fun didSetHistoricalTradingRewardsPeriod(period: String) {
        retrieveAccountHistoricalTradingRewards(period)
    }

    fun didSetHistoricalPnlPeriod() {
        if (subaccount != null) {
            retrieveSubaccountHistoricalPnls()
        }
    }

    open fun didSetMarket(market: String?, oldValue: String?) {
        if (market != oldValue) {
            if (oldValue != null) {
                marketTradesChannelSubscription(oldValue, false)
                marketOrderbookChannelSubscription(oldValue, false)
            }
            if (market != null) {
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    stateMachine.tradeInMarket(market, subaccountNumber)
                }
                if (socketConnected) {
                    marketTradesChannelSubscription(market, true)
                    marketOrderbookChannelSubscription(market, true)
                }
            }
            retrieveMarketHistoricalFundings()
            maybeRetrieveMarketCandles()
        }
    }

    internal fun refresh(data: ApiData) {
        when (data) {
            ApiData.HISTORICAL_PNLS -> {
                retrieveSubaccountHistoricalPnls()
            }

            ApiData.HISTORICAL_TRADING_REWARDS -> {
                retrieveAccountHistoricalTradingRewards()
            }
        }
    }

    fun trade(
        data: String?,
        type: TradeInputField?,
    ) {
        ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse = stateMachine.trade(data, type, subaccountNumber)
            ioImplementations.threading?.async(ThreadingType.main) {
                stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    fun closePosition(
        data: String?,
        type: ClosePositionInputField,
    ) {
        ioImplementations.threading?.async(ThreadingType.abacus) {
            val currentMarket =
                parser.asString(parser.value(stateMachine.input, "closePosition.marketId"))
            var stateResponse = stateMachine.closePosition(data, type, subaccountNumber)
            if (type == ClosePositionInputField.market && currentMarket != data) {
                val nextResponse = stateMachine.closePosition(
                    "1",
                    ClosePositionInputField.percent,
                    subaccountNumber,
                )
                stateResponse = nextResponse.merge(stateResponse)
            }
            ioImplementations.threading?.async(ThreadingType.main) {
                stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    internal fun transfer(
        data: String?,
        type: TransferInputField?,
    ) {
        ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse = stateMachine.transfer(data, type, subaccountNumber)
            didUpdateStateForTransfer(data, type)
            ioImplementations.threading?.async(ThreadingType.main) {
                stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    internal open fun didUpdateStateForTransfer(
        data: String?,
        type: TransferInputField?,
    ) {
    }

    internal open fun transferStatus(
        hash: String,
        fromChainId: String? = null,
        toChainId: String? = null,
        isCctp: Boolean,
        requestId: String? = null,
    ) {
    }

    fun triggerOrders(
        data: String?,
        type: TriggerOrdersInputField?,
    ) {
        ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse = stateMachine.triggerOrders(data, type, subaccountNumber)
            ioImplementations.threading?.async(ThreadingType.main) {
                stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    fun adjustIsolatedMargin(
        data: String?,
        type: AdjustIsolatedMarginInputField?,
    ) {
        ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse = stateMachine.adjustIsolatedMargin(data, type, subaccountNumber)
            ioImplementations.threading?.async(ThreadingType.main) {
                stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    internal open fun commitPlaceOrder(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        callback(false, V4TransactionErrors.error(null, "Not implemented"), null)
        return null
    }

    internal open fun commitTriggerOrders(callback: TransactionCallback): HumanReadableTriggerOrdersPayload? {
        callback(false, V4TransactionErrors.error(null, "Not implemented"), null)
        return null
    }

    internal open fun commitAdjustIsolatedMargin(callback: TransactionCallback): HumanReadableSubaccountTransferPayload? {
        callback(false, V4TransactionErrors.error(null, "Not implemented"), null)
        return null
    }

    internal open fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        callback(false, V4TransactionErrors.error(null, "Not implemented"), null)
        return null
    }

    fun stopWatchingLastOrder() {
        lastOrderClientId = null
    }

    internal open fun commitTransfer(callback: TransactionCallback) {
        callback(false, V4TransactionErrors.error(null, "Not implemented"), null)
    }

    internal open fun commitCCTPWithdraw(callback: TransactionCallback) {
        callback(false, V4TransactionErrors.error(null, "Not implemented"), null)
    }

    internal open fun faucet(amount: Double, callback: TransactionCallback) {
        callback(false, V4TransactionErrors.error(null, "Not implemented"), null)
    }

    internal open fun cancelOrder(orderId: String, callback: TransactionCallback) {
        callback(false, V4TransactionErrors.error(null, "Not implemented"), null)
    }

    internal open fun parseTransactionResponse(response: String?): ParsingError? {
        return null
    }

    private fun triggerOrderPayload(triggerOrder: TriggerOrder, subaccountNumber: Int, marketId: String): HumanReadablePlaceOrderPayload {
        val clientId = Random.nextInt(0, Int.MAX_VALUE)
        val type = triggerOrder.type?.rawValue ?: throw Exception("type is null")
        val side = triggerOrder.side?.rawValue ?: throw Exception("side is null")
        val size = triggerOrder.summary?.size ?: throw Exception("size is null")

        val price = triggerOrder.summary?.price ?: throw Exception("summary.price is null")
        val triggerPrice = triggerOrder.price?.triggerPrice ?: throw Exception("triggerPrice is null")

        val reduceOnly = true
        val postOnly = false

        // TP/SL orders always have a null timeInForce. IOC/FOK/PostOnly/GTD is distinguished by the execution field.
        val timeInForce = null;

        /**
         * TP/SL market orders default to IOC execution.
         * TP/SL limit orders default to GTD (default) execution.
         */
        val execution = when (triggerOrder.type) {
            OrderType.stopMarket, OrderType.takeProfitMarket -> "IOC"
            OrderType.stopLimit, OrderType.takeProfitLimit -> "DEFAULT"
            else -> throw Exception("invalid triggerOrderType")
        }

        val duration = GoodTil.duration(TradeInputGoodUntil(TRIGGER_ORDER_DEFAULT_DURATION_DAYS, "D")) ?: throw Exception("invalid duration")
        val goodTilTimeInSeconds = (duration / 1.seconds).toInt()
        val goodTilBlock = null

        val marketInfo = marketInfo(marketId)
        val currentHeight = calculateCurrentHeight()

        return HumanReadablePlaceOrderPayload(
            subaccountNumber,
            marketId,
            clientId,
            type,
            side,
            price,
            triggerPrice,
            size,
            reduceOnly,
            postOnly,
            timeInForce,
            execution,
            goodTilTimeInSeconds,
            goodTilBlock,
            marketInfo,
            currentHeight,
        )
    }

    private fun isTriggerOrderEqualToExistingOrder(triggerOrder: TriggerOrder, existingOrder: SubaccountOrder): Boolean {
        val limitPriceCheck = when (triggerOrder.type) {
            OrderType.stopLimit, OrderType.takeProfitLimit -> triggerOrder.price?.limitPrice == existingOrder.price
            else -> true
        }
        val size = triggerOrder.summary?.size

        return size == existingOrder.size &&
            triggerOrder.type == existingOrder.type &&
            triggerOrder.side == existingOrder.side &&
            triggerOrder.price?.triggerPrice == existingOrder.triggerPrice &&
            limitPriceCheck
    }

    @Throws(Exception::class)
    fun triggerOrdersPayload(): HumanReadableTriggerOrdersPayload {
        val placeOrderPayloads = mutableListOf<HumanReadablePlaceOrderPayload>()
        val cancelOrderPayloads = mutableListOf<HumanReadableCancelOrderPayload>()
        val triggerOrders = stateMachine.state?.input?.triggerOrders

        val subaccountNumber = connectedSubaccountNumber ?: throw Exception("subaccountNumber is null")
        val subaccount = stateMachine.state?.subaccount(subaccountNumber) ?: throw Exception("subaccount is null")

        val marketId = triggerOrders?.marketId ?: throw Exception("marketId is null")

        fun updateTriggerOrder(triggerOrder: TriggerOrder) {
            // Cases
            // 1. Existing order -> update
            // 2. Existing order -> nothing should be done
            // 3. Existing order -> should delete
            // 4. No existing order -> create a new one
            // 5. No existing order -> nothing should be done

            if (triggerOrder.orderId != null) {
                val existingOrder = subaccount.orders?.firstOrNull { it.id == triggerOrder.orderId }
                    ?: throw Exception("order is null")
                if (triggerOrder.price?.triggerPrice != null) {
                    if (!isTriggerOrderEqualToExistingOrder(triggerOrder, existingOrder)) {
                        // (1) Existing order -> update
                        cancelOrderPayloads.add(cancelOrderPayload(triggerOrder.orderId))
                        placeOrderPayloads.add(triggerOrderPayload(triggerOrder, subaccountNumber, marketId))
                    } // (2) Existing order -> nothing changed
                } else {
                    // (3) Existing order -> should delete
                    cancelOrderPayloads.add(cancelOrderPayload(triggerOrder.orderId))
                }
            } else {
                if (triggerOrder.price?.triggerPrice != null) {
                    // (4) No existing order -> create a new one
                    placeOrderPayloads.add(triggerOrderPayload(triggerOrder, subaccountNumber, marketId))
                } // (5)
            }
        }

        if (triggerOrders.stopLossOrder != null) {
            updateTriggerOrder(triggerOrders.stopLossOrder)
        }

        if (triggerOrders.takeProfitOrder != null) {
            updateTriggerOrder(triggerOrders.takeProfitOrder)
        }

        return HumanReadableTriggerOrdersPayload(
            placeOrderPayloads,
            cancelOrderPayloads,
        )
    }

    internal fun isShortTermOrder(type: String, timeInForce: String?): Boolean {
        return when (type) {
            "MARKET" -> true
            "LIMIT" -> {
                when (timeInForce) {
                    "GTT" -> false
                    else -> true
                }
            }

            else -> false
        }
    }

    @Throws(Exception::class)
    fun placeOrderPayload(): HumanReadablePlaceOrderPayload {
        val subaccountNumber =
            connectedSubaccountNumber ?: throw Exception("subaccountNumber is null")
        val trade = stateMachine.state?.input?.trade
        val marketId = trade?.marketId ?: throw Exception("marketId is null")
        val summary = trade.summary ?: throw Exception("summary is null")
        val clientId = Random.nextInt(0, Int.MAX_VALUE)
        val type = trade.type?.rawValue ?: throw Exception("type is null")
        val side = trade.side?.rawValue ?: throw Exception("side is null")
        val price = summary.payloadPrice ?: throw Exception("price is null")
        val triggerPrice =
            if (trade.options?.needsTriggerPrice == true) trade.price?.triggerPrice else null

        val size = summary.size ?: throw Exception("size is null")
        val reduceOnly = if (trade.options?.needsReduceOnly == true) trade.reduceOnly else null
        val postOnly = if (trade.options?.needsPostOnly == true) trade.postOnly else null

        val timeInForce = if (trade.options?.timeInForceOptions != null) {
            when (trade.type) {
                OrderType.market -> "FOK"
                else -> trade.timeInForce ?: "FOK"
            }
        } else {
            null
        }

        val execution = if (trade.options?.executionOptions != null) {
            trade.execution ?: "DEFAULT"
        } else {
            null
        }

        val goodTilTimeInSeconds = (
            (
                if (trade.options?.goodTilUnitOptions != null) {
                    val timeInterval =
                        GoodTil.duration(trade.goodTil) ?: throw Exception("goodTil is null")
                    timeInterval / 1.seconds
                } else {
                    null
                }
                )
            )?.toInt()

        val marketInfo = marketInfo(marketId)
        val currentHeight = calculateCurrentHeight()

        val goodTilBlock =
            if (isShortTermOrder(trade.type.rawValue, trade.timeInForce)) {
                currentHeight?.plus(
                    SHORT_TERM_ORDER_DURATION,
                )
            } else {
                null
            }

        return HumanReadablePlaceOrderPayload(
            subaccountNumber,
            marketId,
            clientId,
            type,
            side,
            price,
            triggerPrice,
            size,
            reduceOnly,
            postOnly,
            timeInForce,
            execution,
            goodTilTimeInSeconds,
            goodTilBlock,
            marketInfo,
            currentHeight,
        )
    }

    internal open fun marketInfo(market: String): PlaceOrderMarketInfo? {
        return null
    }

    internal open fun calculateCurrentHeight(): Int? {
        return null
    }

    fun placeOrderPayloadJson(): String {
        return Json.encodeToString(placeOrderPayload())
    }

    @Throws(Exception::class)
    fun closePositionPayload(): HumanReadablePlaceOrderPayload {
        val subaccountNumber =
            connectedSubaccountNumber ?: throw Exception("subaccountNumber is null")
        val closePosition = stateMachine.state?.input?.closePosition
        val marketId = closePosition?.marketId ?: throw Exception("marketId is null")
        val summary = closePosition.summary ?: throw Exception("summary is null")
        val clientId = Random.nextInt(0, Int.MAX_VALUE)
        val side = closePosition.side?.rawValue ?: throw Exception("side is null")
        val price = summary.payloadPrice ?: throw Exception("price is null")
        val size = summary.size ?: throw Exception("size is null")
        val timeInForce = "IOC"
        val execution = "DEFAULT"
        val reduceOnly = environment.featureFlags.reduceOnlySupported
        val postOnly = false
        val goodTilTimeInSeconds = null
        val currentHeight = calculateCurrentHeight()
        val goodTilBlock = currentHeight?.plus(SHORT_TERM_ORDER_DURATION)

        return HumanReadablePlaceOrderPayload(
            subaccountNumber,
            marketId,
            clientId,
            "MARKET",
            side,
            price,
            null,
            size,
            reduceOnly,
            postOnly,
            timeInForce,
            execution,
            goodTilTimeInSeconds,
            goodTilBlock,
        )
    }

    fun closePositionPayloadJson(): String {
        return Json.encodeToString(closePositionPayload())
    }

    @Throws(Exception::class)
    fun depositPayload(): HumanReadableDepositPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val subaccountNumber =
            connectedSubaccountNumber ?: throw Exception("subaccountNumber is null")
        val amount = transfer.size?.size ?: throw Exception("size is null")
        return HumanReadableDepositPayload(
            subaccountNumber,
            amount,
        )
    }

    fun depositPayloadJson(): String {
        return Json.encodeToString(depositPayload())
    }

    @Throws(Exception::class)
    fun withdrawPayload(): HumanReadableWithdrawPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val subaccountNumber =
            connectedSubaccountNumber ?: throw Exception("subaccountNumber is null")
        val amount = transfer.size?.usdcSize ?: throw Exception("usdcSize is null")
        return HumanReadableWithdrawPayload(
            subaccountNumber,
            amount,
        )
    }

    fun transferNativeTokenPayloadJson(): String {
        return Json.encodeToString(transferNativeTokenPayload())
    }

    @Throws(Exception::class)
    fun transferNativeTokenPayload(): HumanReadableTransferPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val subaccountNumber =
            connectedSubaccountNumber ?: throw Exception("subaccountNumber is null")
        val amount = transfer.size?.size ?: throw Exception("size is null")
        val recipient = transfer.address ?: throw Exception("address is null")
        return HumanReadableTransferPayload(
            subaccountNumber,
            amount,
            recipient,
        )
    }

    fun withdrawPayloadJson(): String {
        return Json.encodeToString(withdrawPayload())
    }

    @Throws(Exception::class)
    fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload {
        val subaccountNumber =
            connectedSubaccountNumber ?: throw Exception("subaccountNumber is null")
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val size = transfer.size?.size ?: throw Exception("size is null")
        val destinationAddress = transfer.address ?: throw Exception("destination address is null")

        return HumanReadableSubaccountTransferPayload(
            subaccountNumber,
            size,
            destinationAddress,
            0,
        )
    }

    fun faucetPayload(subaccountNumber: Int, amount: Double): HumanReadableFaucetPayload {
        return HumanReadableFaucetPayload(subaccountNumber, amount)
    }

    fun subaccountTransferPayloadJson(): String {
        return Json.encodeToString(subaccountTransferPayload())
    }

    @Throws(Exception::class)
    fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload {
        val subaccount = stateMachine.state?.subaccount(subaccountNumber)
            ?: throw Exception("subaccount is null")
        val order = subaccount.orders?.firstOrNull { it.id == orderId }
            ?: throw Exception("order is null")
        val clientId = order.clientId ?: throw Exception("clientId is null")
        val orderFlags = order.orderFlags ?: throw Exception("orderFlags is null")
        val clobPairId = order.clobPairId ?: throw Exception("clobPairId is null")
        val goodTilBlock = order.goodTilBlock
        val goodTilBlockTime = order.goodTilBlockTime

        return HumanReadableCancelOrderPayload(
            subaccountNumber,
            orderId,
            clientId,
            orderFlags,
            clobPairId,
            goodTilBlock,
            goodTilBlockTime,
        )
    }

    internal fun orderCanceled(orderId: String) {
        val connectedSubaccount = connectedSubaccountNumber
        if (connectedSubaccount != null) {
            ioImplementations.threading?.async(ThreadingType.abacus) {
                val changes = stateMachine.orderCanceled(orderId, connectedSubaccount)
                if (changes.changes.size != 0) {
                    ioImplementations.threading?.async(ThreadingType.main) {
                        stateNotification?.stateChanged(
                            stateMachine.state,
                            changes,
                        )
                    }
                }
            }
        }
    }

    @Throws(Exception::class)
    fun adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload {
        val subaccount = stateMachine.state?.subaccount(subaccountNumber)
            ?: error("subaccount is null")
        val parentSubaccountNumber = subaccount.subaccountNumber
        val wallet = stateMachine.state?.wallet ?: error("wallet is null")
        val walletAddress = wallet.walletAddress ?: error("walletAddress is null")
        val isolatedMarginAdjustment = stateMachine.state?.input?.adjustIsolatedMargin
            ?: error("isolatedMarginAdjustment is null")
        val amount = isolatedMarginAdjustment.amount ?: error("amount is null")
        val childSubaccountNumber = isolatedMarginAdjustment.childSubaccountNumber
            ?: error("childSubaccountNumber is null")

        return HumanReadableSubaccountTransferPayload(
            parentSubaccountNumber,
            amount,
            walletAddress,
            childSubaccountNumber
        )
    }

    private fun updateTracking(changes: StateChanges) {
        if (changes.changes.contains(Changes.transfers)) {
            parseTransfersToMatchFaucetRecords()
        }
        if (changes.changes.contains(Changes.subaccount)) {
            parseOrdersToMatchPlaceOrdersAndCancelOrders()
        }
    }

    private fun updateNotifications() {
        val notifications = notificationsProvider.buildNotifications(stateMachine, subaccountNumber)
        consolidateNotifications(notifications)
    }

    private fun consolidateNotifications(notifications: IMap<String, Notification>) {
        val consolidated = iMutableMapOf<String, Notification>()
        var modified = false
        for ((key, notification) in notifications) {
            val existing = this.notifications[key]
            if (existing != null) {
                if (existing.updateTimeInMilliseconds != notification.updateTimeInMilliseconds ||
                    existing.text != notification.text
                ) {
                    consolidated[key] = notification
                    modified = true
                } else {
                    consolidated[key] = existing
                }
            } else {
                consolidated[key] = notification
                modified = true
            }
        }
        if (modified) {
            this.notifications = consolidated
        }
    }

    private fun sendNotifications() {
        val notifications = notifications.values().sortedWith { notification1, notification2 ->
            val comparison = notification1.priority.compareTo(notification2.priority)
            if (comparison == 0) {
                notification1.updateTimeInMilliseconds.compareTo(notification2.updateTimeInMilliseconds) * -1
            } else {
                comparison * -1
            }
        }.toIList()

        ioImplementations.threading?.async(ThreadingType.main) {
            stateNotification?.notificationsChanged(notifications)
            dataNotification?.notificationsChanged(notifications)
        }
    }

    internal open fun parseFaucetResponse(
        response: String,
        subaccountNumber: Int,
        amount: Double,
        submitTimeInMilliseconds: Double
    ): ParsingError? {
        return null
    }

    private fun didSetFaucetRecords() {
        parseTransfersToMatchFaucetRecords()
    }

    private fun parseTransfersToMatchFaucetRecords() {
        val subaccountFaucetRecords =
            faucetRecords.filter { it.subaccountNumber == subaccountNumber }
        if (subaccountFaucetRecords.isNotEmpty()) {
            val transfers = stateMachine.state?.subaccountTransfers(subaccountNumber) ?: return
            val earliest = subaccountFaucetRecords.first().timestampInMilliseconds
            val firstIndexTransferAfter = transfers.indexOfFirst {
                it.updatedAtMilliseconds < earliest
            }
            if (firstIndexTransferAfter != -1) {
                val transfersAfter = transfers.subList(firstIndexTransferAfter, transfers.size)
                // Now, try to match transfers with faucet records
                for (transfer in transfersAfter) {
                    when (transfer.type) {
                        TransferRecordType.TRANSFER_IN -> {
                            val faucet = subaccountFaucetRecords.firstOrNull {
                                it.amount == transfer.amount && it.timestampInMilliseconds < transfer.updatedAtMilliseconds
                            }
                            if (faucet != null) {
                                val interval = Clock.System.now().toEpochMilliseconds()
                                    .toDouble() - faucet.timestampInMilliseconds
                                tracking(
                                    AnalyticsEvent.TransferFaucetConfirmed.rawValue,
                                    trackingParams(interval),
                                )
                                faucetRecords.remove(faucet)
                                break
                            }
                        }

                        else -> {}
                    }
                    val transferType = transfer.type
                }
            }
        }
    }

    internal open fun trackingParams(interval: Double): IMap<String, Any> {
        return iMapOf(
            "roundtripMs" to interval,
        )
    }

    internal fun tracking(eventName: String, params: IMap<String, Any>?) {
        val paramsAsString = jsonEncoder.encode(params)
        ioImplementations.threading?.async(ThreadingType.main) {
            ioImplementations.tracking?.log(eventName, paramsAsString)
        }
    }

    private fun didSetPlaceOrderRecords() {
        parseOrdersToMatchPlaceOrdersAndCancelOrders()
    }

    private fun didSetCancelOrderRecords() {
        parseOrdersToMatchPlaceOrdersAndCancelOrders()
    }

    internal var analyticsUtils: AnalyticsUtils = AnalyticsUtils()

    private fun parseOrdersToMatchPlaceOrdersAndCancelOrders() {
        if (placeOrderRecords.isNotEmpty() || cancelOrderRecords.isNotEmpty()) {
            val subaccount = stateMachine.state?.subaccount(subaccountNumber) ?: return
            val orders = subaccount.orders ?: return
            for (order in orders) {
                val orderAnalyticsPayload = analyticsUtils.formatOrder(order)
                val placeOrderRecord = placeOrderRecords.firstOrNull {
                    it.clientId == order.clientId
                }
                if (placeOrderRecord != null) {
                    val interval = Clock.System.now().toEpochMilliseconds()
                        .toDouble() - placeOrderRecord.timestampInMilliseconds
                    tracking(
                        AnalyticsEvent.TradePlaceOrderConfirmed.rawValue,
                        ParsingHelper.merge(
                            trackingParams(interval),
                            orderAnalyticsPayload,
                        )?.toIMap(),
                    )
                    placeOrderRecords.remove(placeOrderRecord)
                    break
                }
                val cancelOrderRecord = cancelOrderRecords.firstOrNull {
                    it.clientId == order.clientId
                }
                if (cancelOrderRecord != null) {
                    val interval = Clock.System.now().toEpochMilliseconds()
                        .toDouble() - cancelOrderRecord.timestampInMilliseconds
                    tracking(
                        AnalyticsEvent.TradeCancelOrderConfirmed.rawValue,
                        ParsingHelper.merge(
                            trackingParams(interval),
                            orderAnalyticsPayload,
                        )?.toIMap(),
                    )
                    cancelOrderRecords.remove(cancelOrderRecord)
                    break
                }
            }
        }
    }

    open fun screenSourceAddress() {
        val address = sourceAddress
        if (address != null) {
            screen(address) { restriction ->
                when (restriction) {
                    Restriction.USER_RESTRICTED,
                    Restriction.NO_RESTRICTION,
                    Restriction.USER_RESTRICTION_UNKNOWN -> {
                        sourceAddressRestriction = restriction
                    }

                    else -> {
                        throw Exception("Unexpected restriction value")
                    }
                }
                rerunAddressScreeningDelay(sourceAddressRestriction)?.let {
                    val timer = ioImplementations.timer ?: CoroutineTimer.instance
                    sourceAddressTimer = timer.schedule(it, it) {
                        screenSourceAddress()
                        true
                    }
                }
            }
        } else {
            sourceAddressRestriction = Restriction.NO_RESTRICTION
        }
    }

    private fun rerunAddressScreeningDelay(restriction: Restriction?): Double? {
        return when (restriction) {
            Restriction.NO_RESTRICTION -> addressContinuousMonitoringDuration
            Restriction.USER_RESTRICTION_UNKNOWN -> addressRetryDuration
            else -> null
        }
    }

    private fun fetchGeo() {
        val url = geoUrl()
        if (url != null) {
            get(
                url,
                null,
                null,
                callback = { _, response, httpCode, _ ->
                    compliance = if (success(httpCode) && response != null) {
                        val payload = parser.decodeJsonObject(response)?.toIMap()
                        if (payload != null) {
                            val country = parser.asString(parser.value(payload, "geo.country"))
                            Compliance(country, compliance.status)
                        } else {
                            Compliance(null, compliance.status)
                        }
                    } else {
                        Compliance(null, compliance.status)
                    }
                },
            )
        }
    }

    private fun handleComplianceResponse(response: String?, httpCode: Int): ComplianceStatus {
        compliance = if (success(httpCode) && response != null) {
            val res = parser.decodeJsonObject(response)?.toIMap()
            if (res != null) {
                val status = parser.asString(res["status"])
                val complianceStatus =
                    if (status != null) {
                        ComplianceStatus.valueOf(status)
                    } else {
                        ComplianceStatus.UNKNOWN
                    }
                Compliance(compliance?.geo, complianceStatus)
            } else {
                Compliance(compliance?.geo, ComplianceStatus.UNKNOWN)
            }
        } else {
            Compliance(compliance?.geo, ComplianceStatus.UNKNOWN)
        }
        return compliance.status
    }

    private fun updateCompliance(address: DydxAddress, status: ComplianceStatus) {
        val message = "Compliance verification message"
        val action = if ((stateMachine.state?.account?.subaccounts?.size ?: 0) > 0) {
            ComplianceAction.CONNECT
        } else {
            ComplianceAction.ONBOARD
        }
        val payload = jsonEncoder.encode(
            mapOf(
                "message" to message,
                "action" to action.toString(),
                "status" to status.toString(),
            ),
        )
        transaction(
            TransactionType.SignCompliancePayload,
            payload,
        ) { additionalPayload ->
            val error = parseTransactionResponse(additionalPayload)
            val result = parser.decodeJsonObject(additionalPayload)

            if (error == null && result != null) {
                val url = complianceGeoblockUrl()
                val signedMessage = parser.asString(result["signedMessage"])
                val publicKey = parser.asString(result["publicKey"])
                val timestamp = parser.asString(result["timestamp"])

                val isUrlAndKeysPresent =
                    url != null && signedMessage != null && publicKey != null && timestamp != null
                val isStatusValid = status != ComplianceStatus.UNKNOWN

                if (isUrlAndKeysPresent && isStatusValid) {
                    val body: IMap<String, String> = iMapOf(
                        "address" to address.rawAddress,
                        "message" to message,
                        "currentStatus" to status.toString(),
                        "action" to action.toString(),
                        "signedMessage" to signedMessage!!,
                        "pubkey" to publicKey!!,
                        "timestamp" to timestamp!!,
                    )
                    val header = iMapOf(
                        "Content-Type" to "application/json",
                    )
                    post(
                        url!!,
                        header,
                        body.toJsonPrettyPrint(),
                        callback = { _, response, httpCode, _ ->
                            handleComplianceResponse(response, httpCode)
                        },
                    )
                } else {
                    compliance = Compliance(compliance?.geo, ComplianceStatus.UNKNOWN)
                }
            } else {
                compliance = Compliance(compliance?.geo, ComplianceStatus.UNKNOWN)
            }
        }
    }

    private fun complianceScreen(address: Address) {
        val url = complianceScreenUrl(address.rawAddress)
        if (url != null) {
            get(
                url,
                null,
                null,
                callback = { _, response, httpCode, _ ->
                    val complianceStatus = handleComplianceResponse(response, httpCode)
                    if (address is DydxAddress) {
                        updateCompliance(address, complianceStatus)
                    }
                },
            )
        }
    }

    open fun screenAccountAddress() {
        val address = accountAddress
        if (address != null) {
            screen(address) { restriction ->
                when (restriction) {
                    Restriction.USER_RESTRICTED,
                    Restriction.NO_RESTRICTION,
                    Restriction.USER_RESTRICTION_UNKNOWN -> {
                        accountAddressRestriction = restriction
                    }

                    else -> {
                        throw Exception("Unexpected restriction value")
                    }
                }
                rerunAddressScreeningDelay(accountAddressRestriction)?.let {
                    val timer = ioImplementations.timer ?: CoroutineTimer.instance
                    accountAddressTimer = timer.schedule(it, it) {
                        screenAccountAddress()
                        true
                    }
                }
            }
        } else {
            accountAddressRestriction = Restriction.NO_RESTRICTION
        }
    }

    open fun screen(address: String, callback: ((Restriction) -> Unit)) {
        val url = screenUrl()
        if (url != null) {
            get(
                url,
                mapOf("address" to address),
                null,
                callback = { _, response, httpCode, _ ->
                    if (success(httpCode) && response != null) {
                        val payload = parser.decodeJsonObject(response)?.toIMap()
                        if (payload != null) {
                            val restricted = parser.asBool(payload["restricted"]) ?: false
                            callback(if (restricted) Restriction.USER_RESTRICTED else Restriction.NO_RESTRICTION)
                        } else {
                            callback(Restriction.USER_RESTRICTION_UNKNOWN)
                        }
                    } else {
                        if (httpCode == 403) {
                            // It could be 403 due to GEOBLOCKED
                            val usageRestriction = restrictionReason(response)
                            callback(usageRestriction.restriction)
                        } else {
                            callback(Restriction.USER_RESTRICTION_UNKNOWN)
                        }
                    }
                },
            )
        }
    }

    internal fun restrictionReason(response: String?): UsageRestriction {
        return if (response != null) {
            val json = parser.decodeJsonObject(response)
            val errors = parser.asList(parser.value(json, "errors"))
            val geoRestriciton = errors?.firstOrNull { error ->
                val code = parser.asString(parser.value(error, "code"))
                code?.contains("GEOBLOCKED") == true
            }

            if (geoRestriciton !== null) {
                UsageRestriction.http403Restriction
            } else {
                UsageRestriction.userRestriction
            }
        } else {
            UsageRestriction.http403Restriction
        }
    }

    private fun didSetSourceAddressRestriction(sourceAddressRestriction: Restriction?) {
        updateAddressRestriction()
    }

    private fun updateAddressRestriction() {
        val restrictions: Set<Restriction?> =
            iSetOf(accountAddressRestriction, sourceAddressRestriction)
        addressRestriction = if (restrictions.contains(Restriction.USER_RESTRICTED)) {
            UsageRestriction.userRestriction
        } else if (restrictions.contains(Restriction.USER_RESTRICTION_UNKNOWN)) {
            UsageRestriction.userRestrictionUnknown
        } else {
            if (sourceAddressRestriction == null && accountAddressRestriction == null) {
                null
            } else {
                UsageRestriction.noRestriction
            }
        }
    }

    private fun didSetAddressRestriction(addressRestriction: UsageRestriction?) {
        updateRestriction()
    }

    internal open fun updateRestriction() {
        restriction = addressRestriction ?: UsageRestriction.noRestriction
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
            state?.compliance,
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

    private fun didSetCompliance(compliance: Compliance?) {
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
            state?.restriction,
            state?.launchIncentive,
            compliance,
        )
        ioImplementations.threading?.async(ThreadingType.main) {
            stateNotification?.stateChanged(
                stateMachine.state,
                StateChanges(
                    iListOf(Changes.compliance),
                ),
            )
        }
    }

    internal open fun dispose() {
        stateNotification = null
        dataNotification = null
        readyToConnect = false
        disconnectSocket()
        sparklinesTimer?.cancel()
        sparklinesTimer = null
        subaccountsTimer?.cancel()
        subaccountsTimer = null
    }
}
