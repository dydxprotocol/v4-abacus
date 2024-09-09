package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.receivedBatchedMarketsChanges
import exchange.dydx.abacus.state.model.receivedMarkets
import exchange.dydx.abacus.state.model.receivedMarketsChanges
import exchange.dydx.abacus.state.model.sparklines
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import indexer.codegen.IndexerSparklineTimePeriod

internal class MarketsSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    private val configs: MarketsConfigs,
) : NetworkSupervisor(stateMachine, helper, analyticsUtils) {
    internal val markets = mutableMapOf<String, MarketSupervisor>()

    private var sparklinesTimer: LocalTimerProtocol? = null
    private val sparklinesPollingDuration = 60.0 * 60.0 // 1 hour

    internal var candlesResolution: String = "1DAY"
        internal set(value) {
            if (field != value) {
                val oldValue = field
                field = value
                didSetCandlesResolution(oldValue)
            }
        }

    internal var orderbookGrouping: OrderbookGrouping = OrderbookGrouping.none
        internal set(value) {
            if (field != value) {
                field = value
                didSetOrderbookGrouping()
            }
        }

    internal fun subscribeToMarket(market: String) {
        val marketSupervisor = markets[market]
        marketSupervisor?.retain() ?: run {
            val newMarketSupervisor =
                MarketSupervisor(stateMachine, helper, analyticsUtils, configs, market)
            newMarketSupervisor.candlesResolution = candlesResolution
            newMarketSupervisor.orderbookGrouping = orderbookGrouping
            newMarketSupervisor.readyToConnect = readyToConnect
            newMarketSupervisor.indexerConnected = indexerConnected
            newMarketSupervisor.socketConnected = socketConnected
            newMarketSupervisor.validatorConnected = validatorConnected
            markets[market] = newMarketSupervisor
        }
    }

    internal fun unsubscribeFromMarket(market: String) {
        val marketSupervisor = markets[market] ?: return
        marketSupervisor.release()
        if (marketSupervisor.retainCount == 0) {
            markets.remove(market)
        }
    }

    internal fun didSetCandlesResolution(oldValue: String) {
        for (market in markets.values) {
            market.candlesResolution = candlesResolution
        }
    }

    fun didSetOrderbookGrouping() {
        for (market in markets.values) {
            market.orderbookGrouping = orderbookGrouping
        }
    }

    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        for (market in markets.values) {
            market.readyToConnect = readyToConnect
        }
    }

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)
        for (market in markets.values) {
            market.indexerConnected = indexerConnected
        }
    }

    override fun didSetSocketConnected(socketConnected: Boolean) {
        super.didSetSocketConnected(socketConnected)
        if (configs.subscribeToMarkets) {
            marketsChannelSubscription(socketConnected)
        }
        for (market in markets.values) {
            market.socketConnected = socketConnected
        }
    }

    override fun didSetValidatorConnected(validatorConnected: Boolean) {
        super.didSetValidatorConnected(validatorConnected)
        for (market in markets.values) {
            market.validatorConnected = validatorConnected
        }
    }

    @Throws(Exception::class)
    private fun marketsChannelSubscription(subscribe: Boolean = true) {
        val channel =
            helper.configs.marketsChannel() ?: throw Exception("markets channel is null")
        helper.socket(
            helper.socketAction(subscribe),
            channel,
            if (subscribe && shouldBatchMarketsChannelData()) {
                iMapOf("batched" to "true")
            } else {
                null
            },
        )
    }

    private fun shouldBatchMarketsChannelData(): Boolean {
        return true
    }

    private fun retrieveSparklines() {
        if (sparklinesTimer == null) {
            val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
            sparklinesTimer = timer.schedule(0.0, sparklinesPollingDuration) {
                if (indexerConnected) {
                    getSparklines()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun getSparklines() {
        val url = helper.configs.publicApiUrl("sparklines")
        if (url != null) {
            // Get 1 day sparkline for market display
            val period = IndexerSparklineTimePeriod.ONEDAY
            helper.get(url, iMapOf("timePeriod" to period.value), null) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    parseSparklinesResponse(response, period)
                }
            }

            if (configs.retrieveSevenDaySparkline) {
                // Get 7 day sparkline to determine if market is new
                val period = IndexerSparklineTimePeriod.SEVENDAYS
                helper.get(url, iMapOf("timePeriod" to period.value), null) { _, response, httpCode, _ ->
                    if (helper.success(httpCode) && response != null) {
                        parseSparklinesResponse(response, period)
                    }
                }
            }
        }
    }

    private fun parseSparklinesResponse(response: String, period: IndexerSparklineTimePeriod) {
        val oldState = stateMachine.state
        update(stateMachine.sparklines(response, period), oldState)
    }

    internal fun receiveMarketsChannelSocketData(
        info: SocketInfo,
        payload: IMap<String, Any>,
        subaccountNumber: Int?,
    ) {
        val oldState = stateMachine.state
        var changes: StateChanges? = null
        try {
            when (info.type) {
                "subscribed" -> {
                    val content = helper.parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = stateMachine.receivedMarkets(content, subaccountNumber ?: 0)

                    if (configs.retrieveSparklines) {
                        // Only refresh sparklines after we get the markets data
                        retrieveSparklines()
                    }
                }

                "unsubscribed" -> {}

                "channel_data" -> {
                    val content = helper.parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = stateMachine.receivedMarketsChanges(content, subaccountNumber ?: 0)
                }

                "channel_batch_data" -> {
                    val content =
                        helper.parser.asList(payload["contents"]) as? IList<IMap<String, Any>>
                            ?: throw ParsingException(
                                ParsingErrorType.MissingContent,
                                payload.toString(),
                            )
                    changes =
                        stateMachine.receivedBatchedMarketsChanges(content, subaccountNumber ?: 0)
                }

                else -> {
                    throw ParsingException(
                        ParsingErrorType.Unhandled,
                        "Type [ ${info.type} ] is not handled",
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

    internal fun receiveMarketCandlesChannelSocketData(
        info: SocketInfo,
        payload: IMap<String, Any>
    ) {
        val (market, resolution) = splitCandlesChannel(info.id)
        val marketSupervisor = markets[market] ?: return
        marketSupervisor.receiveMarketCandlesChannelSocketData(info, resolution, payload)
    }

    private fun splitCandlesChannel(id: String?): Pair<String, String> {
        if (id == null) {
            throw ParsingException(
                ParsingErrorType.UnknownChannel,
                "No candles channel id provided",
            )
        }
        val marketAndResolution = id.split("/")
        if (marketAndResolution.size != 2) {
            throw ParsingException(
                ParsingErrorType.UnknownChannel,
                "$id is not a valid candles channel id",
            )
        }
        val market = marketAndResolution[0]
        val resolution = marketAndResolution[1]
        return Pair(market, resolution)
    }

    internal fun receiveMarketOrderbooksChannelSocketData(
        info: SocketInfo,
        payload: IMap<String, Any>,
        subaccountNumber: Int?,
    ) {
        val marketId = info.id
        val marketSupervisor = markets[marketId] ?: return
        marketSupervisor.receiveMarketOrderbooksChannelSocketData(info, payload, subaccountNumber)
    }

    internal fun receiveMarketTradesChannelSocketData(
        info: SocketInfo,
        payload: IMap<String, Any>,
    ) {
        val marketId = info.id
        val marketSupervisor = markets[marketId] ?: return
        marketSupervisor.receiveMarketTradesChannelSocketData(info, payload)
    }
}

// Extension properties to help with current singular market

private val MarketsSupervisor.market: MarketSupervisor?
    get() {
        return if (markets.count() == 1) markets.values.firstOrNull() else null
    }

internal var MarketsSupervisor.marketId: String?
    get() {
        return market?.marketId
    }
    set(value) {
        markets.keys.filter { it != value }.forEach {
            markets[it]?.forceRelease()
            markets.remove(it)
        }
        if (markets.contains(value).not() && value != null) {
            subscribeToMarket(value)
        }
    }
