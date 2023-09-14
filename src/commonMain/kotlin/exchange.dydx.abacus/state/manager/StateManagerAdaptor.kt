package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.TransferRecordType
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.app.HistoricalPnlPeriod
import exchange.dydx.abacus.state.app.IndexerURIs
import exchange.dydx.abacus.state.app.OrderbookGrouping
import exchange.dydx.abacus.state.app.V4Environment
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.Changes.candles
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.configs.StateManagerConfigs
import exchange.dydx.abacus.state.modal.ClosePositionInputField
import exchange.dydx.abacus.state.modal.PerpTradingStateMachine
import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.TradingStateMachine
import exchange.dydx.abacus.state.modal.TransferInputField
import exchange.dydx.abacus.state.modal.candles
import exchange.dydx.abacus.state.modal.closePosition
import exchange.dydx.abacus.state.modal.findOrder
import exchange.dydx.abacus.state.modal.historicalFundings
import exchange.dydx.abacus.state.modal.historicalPnl
import exchange.dydx.abacus.state.modal.orderCanceled
import exchange.dydx.abacus.state.modal.receivedAccountsChanges
import exchange.dydx.abacus.state.modal.receivedBatchOrderbookChanges
import exchange.dydx.abacus.state.modal.receivedBatchedMarketsChanges
import exchange.dydx.abacus.state.modal.receivedBatchedTradesChanges
import exchange.dydx.abacus.state.modal.receivedFills
import exchange.dydx.abacus.state.modal.receivedMarkets
import exchange.dydx.abacus.state.modal.receivedMarketsChanges
import exchange.dydx.abacus.state.modal.receivedOrderbook
import exchange.dydx.abacus.state.modal.receivedSubaccountSubscribed
import exchange.dydx.abacus.state.modal.receivedTrades
import exchange.dydx.abacus.state.modal.receivedTradesChanges
import exchange.dydx.abacus.state.modal.receivedTransfers
import exchange.dydx.abacus.state.modal.setOrderbookGrouping
import exchange.dydx.abacus.state.modal.sparklines
import exchange.dydx.abacus.state.modal.subaccounts
import exchange.dydx.abacus.state.modal.trade
import exchange.dydx.abacus.state.modal.tradeInMarket
import exchange.dydx.abacus.state.modal.transfer
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.GoodTil
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Parser
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.values
import kollections.JsExport
import kollections.iMutableListOf
import kollections.iMutableSetOf
import kollections.iSetOf
import kollections.toIList
import kollections.toIMap
import kollections.toISet
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.times

@JsExport
internal data class Subaccount(
    val address: String,
    val subaccountNumber: Int,
)

@JsExport
@Serializable
data class HumanReadablePlaceOrderPayload(
    val subaccountNumber: Int,
    val marketId: String,
    val clientId: Int,
    val type: String,
    val side: String,
    val price: Double,
    val triggerPrice: Double?,
    val size: Double,
    val reduceOnly: Boolean,
    val postOnly: Boolean,
    val timeInForce: String,
    val execution: String,
    val goodTilTimeInSeconds: Int?,
)

@JsExport
@Serializable
data class HumanReadableCancelOrderPayload(
    val subaccountNumber: Int,
    val orderId: String,
    val clientId: Int,
    val orderFlags: Int,
    val clobPairId: Int,
    val goodTilBlock: Int?,
    val goodTilBlockTime: Int?,
)

@JsExport
@Serializable
data class HumanReadableSubaccountTransferPayload(
    val subaccountNumber: Int,
    val amount: Double,
    val destinationAddress: String,
    val destinationSubaccountNumber: Int,
)

@JsExport
@Serializable
data class HumanReadableFaucetPayload(
    val subaccountNumber: Int,
    val amount: Double,
)

@JsExport
@Serializable
data class HumanReadableDepositPayload(
    val subaccountNumber: Int,
    val amount: Double,
)

@JsExport
@Serializable
data class HumanReadableWithdrawPayload(
    val subaccountNumber: Int,
    val amount: Double,
)

@JsExport
@Serializable
data class HumanReadableTransferPayload(
    val subaccountNumber: Int,
    val amount: Double,
    val recipient: String,
)

data class FaucetRecord(
    val subaccountNumber: Int,
    val amount: Double,
    val timestampInMilliseconds: Double,
)

data class PlaceOrderRecord(
    val subaccountNumber: Int,
    val clientId: Int,
    val timestampInMilliseconds: Double,
)

data class CancelOrderRecord(
    val subaccountNumber: Int,
    val clientId: Int,
    val timestampInMilliseconds: Double,
)


@JsExport
open class StateManagerAdaptor(
    val ioImplementations: IOImplementations,
    val uiImplementations: UIImplementations,
    val environment: V4Environment,
    open val configs: StateManagerConfigs,
    var stateNotification: StateNotificationProtocol?,
    var dataNotification: DataNotificationProtocol?,
) {
    var stateMachine: TradingStateMachine = PerpTradingStateMachine(
        environment,
        uiImplementations.localizer,
        Formatter(uiImplementations.formatter),
        environment.version,
        environment.maxSubaccountNumber,
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
        NotificationsProvider(uiImplementations, parser, jsonEncoder)

    private var subaccountsTimer: LocalTimerProtocol? = null
    private val subaccountsPollingDelay = 5.0
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

    var sourceAddress: String? = null

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

    var historicalPnlPeriod: HistoricalPnlPeriod = HistoricalPnlPeriod.Period1d
        internal set(value) {
            if (field != value) {
                field = value
                didSetHistoricalPnlPeriod()
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
                if (value != null) {
                    stateNotification?.lastOrderChanged(lastOrder)
                    dataNotification?.lastOrderChanged(lastOrder)
                }
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
                retrieveMarketCandles()
            }
            if (accountAddress != null) {
                retrieveSubaccounts()
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
        ioImplementations.threading?.async(ThreadingType.abacus) {
            stateMachine.resetWallet(accountAddress)
            ioImplementations.threading?.async(ThreadingType.main) {
                connectedSubaccountNumber = null
                subaccountNumber = 0
                updateConnectedSubaccountNumber()
            }
        }
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
                        false
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
        type: String, channel: String, params: IMap<String, Any>? = null,
    ) {
        val request = iMutableMapOf<String, Any>("type" to type, "channel" to channel)
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
            socketAction(subscribe), channel,
            if (subscribe && shouldBatchMarketsChannelData()) {
                iMapOf("batched" to "true")
            } else null
        )
    }

    open fun shouldBatchMarketsChannelData(): Boolean {
        return false
    }

    @Throws(Exception::class)
    private fun marketTradesChannelSubscription(market: String, subscribe: Boolean = true) {
        val channel = configs.marketTradesChannel() ?: throw Exception("trades channel is null")
        socket(
            socketAction(subscribe), channel,
            if (subscribe && shouldBatchMarketTradesChannelData())
                iMapOf("id" to market, "batched" to "true")
            else
                iMapOf("id" to market)
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
            socketAction(subscribe), channel,
            if (subscribe && shouldBatchMarketOrderbookChannelData())
                iMapOf("id" to market, "batched" to "true")
            else
                iMapOf("id" to market)
        )
    }

    open fun shouldBatchMarketOrderbookChannelData(): Boolean {
        return true
    }

    @Throws(Exception::class)
    private fun subaccountChannelSubscription(
        accountAddress: String, subaccountNumber: Int, subscribe: Boolean = true,
    ) {
        val channel =
            configs.subaccountChannel() ?: throw Exception("subaccount channel is null")
        socket(
            socketAction(subscribe),
            channel,
            subaccountChannelParams(accountAddress, subaccountNumber)
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
                val json = Json.parseToJsonElement(message).jsonObject.toIMap()
                socket(json)
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
                            payload.toString()
                        )
                    changes = socketSubscribed(channel, id, subaccountNumber, content)
                }

                "channel_data" -> {
                    val channel = parser.asString(payload["channel"]) ?: return
                    val info = SocketInfo(type, channel, id)
                    val content = parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString()
                        )
                    changes = socketChannelData(channel, id, subaccountNumber, info, content)
                }

                "channel_batch_data" -> {
                    val channel = parser.asString(payload["channel"]) ?: return
                    val info = SocketInfo(type, channel, id)
                    val content = parser.asList(payload["contents"]) as? IList<IMap<String, Any>>
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString()
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
                        "Type [ $type ] is not handled"
                    )
                }
            }
            update(changes, oldState)
        } catch (_: ParsingException) {
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
                changes
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
                            marketId
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
                            subaccountId
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

            configs.subaccountChannel() -> {
                stateMachine.receivedSubaccountSubscribed(content, height())
            }

            configs.marketOrderbookChannel() -> {
                stateMachine.receivedOrderbook(id, content, subaccountNumber ?: 0)
            }

            configs.marketTradesChannel() -> {
                stateMachine.receivedTrades(id, content)
            }

            else -> {
                throw ParsingException(
                    ParsingErrorType.UnknownChannel,
                    "$channel is not known"
                )
            }
        }
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

            configs.subaccountChannel() -> {
                stateMachine.receivedAccountsChanges(content, info, height())
            }

            configs.marketOrderbookChannel() -> {
                throw ParsingException(
                    ParsingErrorType.UnhandledEndpoint,
                    "channel_data for ${channel} is not implemented"
                )
            }

            configs.marketTradesChannel() -> {
                stateMachine.receivedTradesChanges(id, content)
            }

            else -> {
                throw ParsingException(
                    ParsingErrorType.UnknownChannel,
                    "$channel is not known"
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
                    subaccountNumber ?: 0
                )
            }

            configs.marketTradesChannel() -> {
                stateMachine.receivedBatchedTradesChanges(id, content)
            }

            configs.marketOrderbookChannel() -> {
                stateMachine.receivedBatchOrderbookChanges(
                    market,
                    content,
                    subaccountNumber ?: 0
                )
            }

            else -> {
                throw ParsingException(
                    ParsingErrorType.UnknownChannel,
                    "$channel is not known"
                )
            }
        }
    }

    open fun faucetBody(amount: Double): String? {
        return null
    }

    open fun retrieveFeeTiers() {

    }

    internal fun get(
        url: String,
        params: IMap<String, String>?,
        headers: IMap<String, String>?,
        private: Boolean,
        callback: (String?, Int) -> Unit,
    ) {
        val fullUrl = if (params != null) {
            val queryString = params.joinToString("&") { "${it.key}=${it.value}" }
            "$url?$queryString"
        } else url

        val modifiedHeaders = if (private) {
            privateHeaders(url, "GET", null, params, null)
        } else headers

        ioImplementations.threading?.async(ThreadingType.network) {
            ioImplementations.rest?.get(fullUrl, modifiedHeaders) { response, httpCode ->
                val time = if (configs.isIndexer(url) && success(httpCode)) {
                    Clock.System.now()
                } else null

                ioImplementations.threading?.async(ThreadingType.abacus) {
                    if (time != null) {
                        this.lastIndexerCallTime = time
                    }
                    try {
                        callback(response, httpCode)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    trackApiCall()
                }
            }
        }
    }

    internal open fun trackApiCall() {
    }

    internal open fun privateHeaders(
        path: String,
        verb: String,
        params: IMap<String, String>?,
        headers: IMap<String, String>?,
        body: String?,
    ): IMap<String, String>? {
        return headers
    }

    fun post(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: (String?, Int) -> Unit,
    ) {
        ioImplementations.threading?.async(ThreadingType.main) {
            ioImplementations.rest?.post(url, headers, body) { response, httpCode ->
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    callback(response, httpCode)
                }
                callback(response, httpCode)
            }
        }
    }

    private fun retrieveServerTime() {
        val url = configs.publicApiUrl("time")
        if (url != null) {
            get(url, null, null, false, callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    val json = Json.parseToJsonElement(response).jsonObject.toIMap()
                    val time = parser.asDatetime(json["time"])
                    if (time != null) {
                        ServerTime.overWrite = time
                    }
                }
            })
        }
    }

    private fun retrieveMarketConfigs() {
        val oldState = stateMachine.state
        val url = configs.configsUrl("markets")
        if (url != null) {
            get(url, null, null, false, callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.configurations(response, subaccountNumber), oldState)
                }
            })
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
            get(url, sparklinesParams(), null, false, callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    parseSparklinesResponse(response)
                }
            })
        }
    }

    open fun parseSparklinesResponse(response: String) {
        val oldState = stateMachine.state
        update(stateMachine.sparklines(response), oldState)
    }

    internal open fun sparklinesParams(): IMap<String, String>? {
        return null
    }

    private fun retrieveMarketCandles() {
        val market = market ?: return
        val url = configs.publicApiUrl("candles") ?: return
        val candleResolution = candlesResolution
        val resolutionDuration =
            candleOptionDuration(stateMachine, market, candleResolution) ?: return
        val maxDuration = resolutionDuration * 365
        val marketCandles = parser.asList(
            parser.value(
                stateMachine.data,
                "markets.markets.$market.candles.$candleResolution"
            )
        )


        return retrieveTimed(
            "$url/$market",
            marketCandles,
            "startedAt",
            resolutionDuration,
            maxDuration,
            "toISO",
            "fromISO",
            false,
            iMapOf(
                "resolution" to candleResolution
            ),
        ) { response, httpCode ->
            val oldState = stateMachine.state
            if (success(httpCode) && response != null) {
                val changes = stateMachine.candles(response)
                update(changes, oldState)
                if (changes.changes.contains(candles)) {
                    retrieveMarketCandles()
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
        val oldState = stateMachine.state
        val url = configs.publicApiUrl("historical-funding") ?: return
        val market = market ?: return
        get("$url/$market", null, null, false, callback = { response, httpCode ->
            if (success(httpCode) && response != null) {
                update(stateMachine.historicalFundings(response), oldState)
            }
        })
    }

    open fun retrieveSubaccounts() {
        val oldState = stateMachine.state
        val url = subaccountsUrl()
        if (url != null) {
            get(url, null, null, false, callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.subaccounts(response), oldState)
                    updateConnectedSubaccountNumber()
                } else {
                    subaccountsTimer =
                        ioImplementations.timer?.schedule(subaccountsPollingDelay, null) {
                            retrieveSubaccounts()
                            false
                        }
                }
            })
        }
    }

    open fun subaccountsUrl(): String? {
        return null
    }

    private fun retrieveSubaccountHistoricalPnls() {
        val url = configs.privateApiUrl("historical-pnl") ?: return
        val params = subaccountParams()
        val historicalPnl = parser.asList(
            parser.value(
                stateMachine.data,
                "wallet.account.subaccounts.$subaccountNumber.historicalPnl"
            )
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
            accountIsPrivate(),
            params
        ) { response, httpCode ->
            val oldState = stateMachine.state
            if (success(httpCode) && !response.isNullOrEmpty()) {
                val changes = stateMachine.historicalPnl(
                    payload = response,
                    subaccountNumber = subaccountNumber
                )
                update(changes, oldState)
                if (changes.changes.contains(Changes.historicalPnl)) {
                    retrieveSubaccountHistoricalPnls()
                }
            }
        }
    }

    open fun accountIsPrivate(): Boolean {
        return true
    }

    private fun retrieveSubaccountFills() {
        val oldState = stateMachine.state
        val url = configs.privateApiUrl("fills")
        val params = subaccountParams()
        if (url != null && params != null) {
            get(url, params, null, accountIsPrivate(), callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    val fills = Json.parseToJsonElement(response).jsonObject.toIMap()
                    if (fills.size != 0) {
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
            get(url, params, null, accountIsPrivate(), callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    val tranfers = Json.parseToJsonElement(response).jsonObject.toIMap()
                    if (tranfers.size != 0) {
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
        items: IList<Any>?,
        timeField: String,
        sampleDuration: Duration,
        maxDuration: Duration,
        beforeParam: String,
        afterParam: String? = null,
        private: Boolean = false,
        additionalParams: IMap<String, String>? = null,
        callback: (response: String?, httpCode: Int) -> Unit,
    ) {
        if (items != null) {
            val lastItemTime =
                parser.asDatetime(
                    parser.asMap(items.lastOrNull())?.get(timeField)
                )
            val firstItemTime =
                parser.asDatetime(
                    parser.asMap(items.firstOrNull())?.get(timeField)
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
                    additionalParams
                )
                get(url, params, null, private, callback)
            } else if (firstItemTime != null) {
                /*
                Get previous
                 */
                if (now - firstItemTime <= maxDuration) {
                    val beforeOrAt = firstItemTime - 1.seconds
                    val after = beforeOrAt - 99 * sampleDuration
                    val params =
                        timedParams(beforeOrAt, beforeParam, after, afterParam, additionalParams)
                    get(url, params, null, private, callback)
                }
            }
        } else {
            /*
            Get latest
             */
            get(url, additionalParams, null, private, callback)
        }
    }

    private fun timedParams(
        before: Instant?,
        beforeParam: String,
        after: Instant?,
        afterParam: String?,
        additionalParams: IMap<String, String>? = null,
    ): IMap<String, String>? {
        val params = iMutableMapOf<String, String>()
        val beforeString = before?.toString()
        if (beforeString != null) {
            params[beforeParam] = beforeString
        }

        val afterString = after?.toString()
        if (afterString != null && afterParam != null) {
            params[afterParam] = afterString
        }

        return if (additionalParams != null) {
            ParsingHelper.merge(params, additionalParams) as? IMap<String, String>
        } else {
            params
        }
    }

    fun success(httpCode: Int): Boolean {
        return httpCode in 200..299
    }

    open fun height(): Int? {
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
        retrieveMarketCandles()
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
            retrieveMarketCandles()
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
            val stateResponse = stateMachine.closePosition(data, type, subaccountNumber)
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
    ) {

    }

    internal open fun commitPlaceOrder(callback: TransactionCallback) {
        callback(false, V4TransactionErrors.error(null, "Not implemented"))
    }

    internal open fun commitClosePosition(callback: TransactionCallback) {
        callback(false, V4TransactionErrors.error(null, "Not implemented"))
    }

    fun stopWatchingLastOrder() {
        lastOrderClientId = null
    }

    internal open fun commitTransfer(callback: TransactionCallback) {
        callback(false, V4TransactionErrors.error(null, "Not implemented"))
    }

    internal open fun faucet(amount: Double, callback: TransactionCallback) {
        callback(false, V4TransactionErrors.error(null, "Not implemented"))
    }

    internal open fun cancelOrder(orderId: String, callback: TransactionCallback) {
        callback(false, V4TransactionErrors.error(null, "Not implemented"))
    }

    internal open fun parseTransactionResponse(response: String?): ParsingError? {
        return null
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
        val triggerPrice = trade.price?.triggerPrice

        val size = summary.size ?: throw Exception("size is null")
        val reduceOnly = trade.reduceOnly
        val postOnly = trade.postOnly

        val timeInForce = trade.timeInForce ?: "IOC"
        val execution = trade.execution ?: "Default"
        val goodTilTimeInSeconds = ((if (timeInForce == "GTT") {
            val timeInterval =
                GoodTil.duration(trade.goodUntil) ?: throw Exception("goodUntil is null")
            timeInterval / 1.seconds
        } else null))?.toInt()
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
            goodTilTimeInSeconds
        )
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
        val timeInForce = "IOK"
        val execution = "Default"
        val reduceOnly = false  // TODO, change to true when protocol supports it
        val postOnly = false
        val goodTilTimeInSeconds = null
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
            goodTilTimeInSeconds
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
            amount
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
            amount
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
        val order = subaccount.orders?.firstOrNull() { it.id == orderId }
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
            goodTilBlockTime
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
            val transfersAfter = transfers.subList(firstIndexTransferAfter ?: 0, transfers.size)
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
                            tracking(AnalyticsEvent.TransferFaucetConfirmed.rawValue, trackingParams(interval))
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

    private fun parseOrdersToMatchPlaceOrdersAndCancelOrders() {
        if (placeOrderRecords.isNotEmpty() || cancelOrderRecords.isNotEmpty()) {
            val subaccount = stateMachine.state?.subaccount(subaccountNumber) ?: return
            val orders = subaccount.orders ?: return
            for (order in orders) {
                val placeOrderRecord = placeOrderRecords.firstOrNull {
                    it.clientId == order.clientId
                }
                if (placeOrderRecord != null) {
                    val interval = Clock.System.now().toEpochMilliseconds()
                        .toDouble() - placeOrderRecord.timestampInMilliseconds
                    tracking(AnalyticsEvent.TradePlaceOrderConfirmed.rawValue, trackingParams(interval))
                    placeOrderRecords.remove(placeOrderRecord)
                    break
                }
                val cancelOrderRecord = cancelOrderRecords.firstOrNull {
                    it.clientId == order.clientId
                }
                if (cancelOrderRecord != null) {
                    val interval = Clock.System.now().toEpochMilliseconds()
                        .toDouble() - cancelOrderRecord.timestampInMilliseconds
                    tracking(AnalyticsEvent.TradeCancelOrderConfirmed.rawValue, trackingParams(interval))
                    cancelOrderRecords.remove(cancelOrderRecord)
                    break
                }
            }
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
