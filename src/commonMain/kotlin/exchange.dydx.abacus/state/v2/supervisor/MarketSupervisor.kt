package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.candles
import exchange.dydx.abacus.state.model.historicalFundings
import exchange.dydx.abacus.state.model.receivedBatchOrderbookChanges
import exchange.dydx.abacus.state.model.receivedBatchedCandlesChanges
import exchange.dydx.abacus.state.model.receivedBatchedTradesChanges
import exchange.dydx.abacus.state.model.receivedCandles
import exchange.dydx.abacus.state.model.receivedCandlesChanges
import exchange.dydx.abacus.state.model.receivedOrderbook
import exchange.dydx.abacus.state.model.receivedOrderbookChanges
import exchange.dydx.abacus.state.model.receivedTrades
import exchange.dydx.abacus.state.model.receivedTradesChanges
import exchange.dydx.abacus.state.model.setOrderbookGrouping
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.iMapOf
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal open class MarketSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    private val configs: MarketsConfigs,
    internal val marketId: String
) : DynamicNetworkSupervisor(stateMachine, helper, analyticsUtils) {

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

    private var historicalFundingTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    internal fun didSetCandlesResolution(oldValue: String) {
        if (configs.retrieveCandles) {
            retrieveCandles()
        }

        if (socketConnected) {
            candlesChannelSubscription(oldValue, false)
            candlesChannelSubscription(candlesResolution, true)
        }
    }

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)
        if (indexerConnected) {
            if (configs.retrieveCandles) {
                retrieveCandles()
            }
            if (configs.retrieveHistoricalFundings) {
                retrieveHistoricalFundings()
            }
        } else {
            historicalFundingTimer = null
        }
    }

    override fun didSetSocketConnected(socketConnected: Boolean) {
        super.didSetSocketConnected(socketConnected)
        if (configs.subscribeToTrades) {
            tradesChannelSubscription(socketConnected)
        }
        if (configs.subscribeToOrderbook) {
            orderbookChannelSubscription(socketConnected)
        }
        if (configs.subscribeToCandles) {
            candlesChannelSubscription(candlesResolution, socketConnected)
        }
    }

    private fun retrieveCandles() {
        val url = helper.configs.publicApiUrl("candles") ?: return
        val candleResolution = candlesResolution
        val resolutionDuration =
            candleOptionDuration(stateMachine, marketId, candleResolution) ?: return
        val maxDuration = resolutionDuration * 365
        val marketCandles = helper.parser.asList(
            helper.parser.value(
                stateMachine.data,
                "markets.markets.$marketId.candles.$candleResolution",
            ),
        )

        return helper.retrieveTimed(
            url = "$url/$marketId",
            items = marketCandles,
            timeField = { item ->
                helper.parser.asDatetime(helper.parser.asMap(item)?.get("startedAt"))
            },
            sampleDuration = resolutionDuration,
            maxDuration = maxDuration,
            beforeParam = "toISO",
            afterParam = "fromISO",
            additionalParams = mapOf(
                "resolution" to candleResolution,
            ),
            previousUrl = null,
        ) { _, response, httpCode, _ ->
            val oldState = stateMachine.state
            if (helper.success(httpCode) && response != null) {
                val changes = stateMachine.candles(response)
                update(changes, oldState)
                if (changes.changes.contains(Changes.candles)) {
                    retrieveCandles()
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

    private fun retrieveHistoricalFundings() {
        historicalFundingTimer = null
        val oldState = stateMachine.state
        val url = helper.configs.publicApiUrl("historical-funding") ?: return
        helper.get("$url/$marketId", null, null, callback = { _, response, httpCode, _ ->
            if (helper.success(httpCode) && response != null) {
                update(stateMachine.historicalFundings(response), oldState)
            }
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                val nextHour = calculateNextFundingAt()
                val delay = nextHour - ServerTime.now()
                this.historicalFundingTimer = helper.ioImplementations.timer?.schedule(
                    // Give 30 seconds past the hour to make sure the funding is available
                    (delay + 30.seconds).inWholeSeconds.toDouble(),
                    null,
                ) {
                    retrieveHistoricalFundings()
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

    fun didSetOrderbookGrouping() {
        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse =
                stateMachine.setOrderbookGrouping(marketId, orderbookGrouping.rawValue)
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                helper.stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    @Throws(Exception::class)
    private fun orderbookChannelSubscription(subscribe: Boolean = true) {
        val channel =
            helper.configs.marketOrderbookChannel() ?: throw Exception("orderbook channel is null")
        helper.socket(
            helper.socketAction(subscribe),
            channel,
            if (subscribe && shouldBatchMarketOrderbookChannelData()) {
                iMapOf("id" to marketId, "batched" to "true")
            } else {
                iMapOf("id" to marketId)
            },
        )
    }

    open fun shouldBatchMarketOrderbookChannelData(): Boolean {
        return true
    }

    @Throws(Exception::class)
    private fun tradesChannelSubscription(subscribe: Boolean = true) {
        val channel =
            helper.configs.marketTradesChannel() ?: throw Exception("trades channel is null")
        helper.socket(
            helper.socketAction(subscribe),
            channel,
            if (subscribe && shouldBatchMarketTradesChannelData()) {
                iMapOf("id" to marketId, "batched" to "true")
            } else {
                iMapOf("id" to marketId)
            },
        )
    }

    internal open fun shouldBatchMarketTradesChannelData(): Boolean {
        return true
    }

    @Throws(Exception::class)
    fun candlesChannelSubscription(resolution: String, subscribe: Boolean = true) {
        val channel = helper.configs.candlesChannel() ?: throw Exception("candlesChannel is null")
        helper.socket(
            helper.socketAction(subscribe),
            channel,
            if (subscribe) {
                iMapOf("id" to "$marketId/$resolution", "batched" to "true")
            } else {
                iMapOf("id" to "$marketId/$resolution")
            },
        )
    }

    internal fun receiveMarketCandlesChannelSocketData(
        info: SocketInfo,
        resolution: String,
        payload: IMap<String, Any>
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
                    changes = stateMachine.receivedCandles(marketId, resolution, content)
                }

                "unsubscribed" -> {}

                "channel_data" -> {
                    val content = helper.parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = stateMachine.receivedCandlesChanges(marketId, resolution, content)
                }

                "channel_batch_data" -> {
                    val content = helper.parser.asList(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes =
                        stateMachine.receivedBatchedCandlesChanges(marketId, resolution, content)
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

    internal fun receiveMarketOrderbooksChannelSocketData(
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
                    changes =
                        stateMachine.receivedOrderbook(marketId, content, subaccountNumber ?: 0)
                }

                "unsubscribed" -> {}

                "channel_data" -> {
                    val content = helper.parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = stateMachine.receivedOrderbookChanges(
                        marketId,
                        content,
                        subaccountNumber ?: 0,
                    )
                }

                "channel_batch_data" -> {
                    val content = helper.parser.asList(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = stateMachine.receivedBatchOrderbookChanges(
                        marketId,
                        content,
                        subaccountNumber ?: 0,
                    )
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

    internal fun receiveMarketTradesChannelSocketData(
        info: SocketInfo,
        payload: IMap<String, Any>,
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
                    changes = stateMachine.receivedTrades(marketId, content)
                }

                "unsubscribed" -> {}

                "channel_data" -> {
                    val content = helper.parser.asMap(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = stateMachine.receivedTradesChanges(marketId, content)
                }

                "channel_batch_data" -> {
                    val content = helper.parser.asList(payload["contents"])
                        ?: throw ParsingException(
                            ParsingErrorType.MissingContent,
                            payload.toString(),
                        )
                    changes = stateMachine.receivedBatchedTradesChanges(marketId, content)
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

    override fun dispose() {
        super.dispose()
        socketConnected = false
    }
}
