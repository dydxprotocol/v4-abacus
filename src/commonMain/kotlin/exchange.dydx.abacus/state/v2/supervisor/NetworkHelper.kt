package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.Transaction
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.protocols.run
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.UIImplementations
import kollections.iListOf
import kollections.iSetOf
import kollections.toIMap
import kollections.toISet
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.times

typealias RestCallbackWithUrl = (url: String, response: String?, httpCode: Int, headers: Map<String, Any>?) -> Unit

class NetworkHelper(
    internal val deploymentUri: String,
    internal val environment: V4Environment,
    internal val uiImplementations: UIImplementations,
    internal val ioImplementations: IOImplementations,
    internal val configs: V4StateManagerConfigs,
    internal var stateNotification: StateNotificationProtocol?,
    internal var dataNotification: DataNotificationProtocol?,
    internal val parser: ParserProtocol,
    internal val indexerRestrictionChanged: (UsageRestriction?) -> Unit
) {
    internal var lastValidatorCallTime: Instant? = null
    internal var lastIndexerCallTime: Instant? = null
    internal val jsonEncoder = JsonEncoder()

    private var indexerRestriction: UsageRestriction? = null
        set(value) {
            if (field !== value) {
                field = value
                didSetIndexerRestriction(field)
            }
        }

    private var restRetryTimers: MutableMap<String, LocalTimerProtocol> =
        exchange.dydx.abacus.utils.mutableMapOf()

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
        callback: RestCallbackWithUrl,
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

    fun get(
        url: String,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        callback: RestCallbackWithUrl,
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

    private fun getWithFullUrl(
        fullUrl: String,
        headers: Map<String, String>?,
        callback: RestCallbackWithUrl,
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
                        when (httpCode) {
                            403 -> {
                                indexerRestriction = restrictionReason(response)
                            }

                            429 -> {
                                // retry after 5 seconds
                                val timer = ioImplementations.timer ?: CoroutineTimer.instance
                                val localTimer = timer.run(5.0) {
                                    restRetryTimers[fullUrl]?.cancel()
                                    restRetryTimers.remove(fullUrl)

                                    getWithFullUrl(fullUrl, headers, callback)
                                }
                                restRetryTimers[fullUrl] = localTimer
                            }

                            else -> {
                                val headers = parser.decodeJsonObject(headersAsJsonString)
                                callback(fullUrl, response, httpCode, headers)
                            }
                        }
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

    private fun didSetIndexerRestriction(indexerRestriction: UsageRestriction?) {
        notifyRestriction()
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

    private fun notifyRestriction() {
        indexerRestrictionChanged(indexerRestriction)
    }

    fun post(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: RestCallbackWithUrl,
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

    fun success(httpCode: Int): Boolean {
        return httpCode in 200..299
    }

    internal fun emitError(error: ParsingError) {
        ioImplementations.threading?.async(ThreadingType.main) {
            stateNotification?.errorsEmitted(iListOf(error))
            dataNotification?.errorsEmitted(iListOf(error))
        }
    }

    internal open fun trackApiCall() {
    }

    @Throws(Exception::class)
    internal fun getOnChain(
        type: QueryType,
        paramsInJson: String?,
        callback: (response: String) -> Unit,
    ) {
        val query = ioImplementations.chain
        if (query === null) {
            throw Exception("chain query is null")
        }
        query.get(type, paramsInJson) { response ->
            // Parse the response
            if (response != null) {
                val time = if (!response.contains("error")) {
                    Clock.System.now()
                } else {
                    null
                }
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    if (time != null) {
                        lastValidatorCallTime = time
                    }
                    callback(response)
                    trackApiCall()
                }
            }
        }
    }

    internal fun updateStateChanges(
        stateMachine: TradingStateMachine,
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
                val state = stateMachine.state
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
        }
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

    fun socketAction(subscribe: Boolean): String {
        return if (subscribe) "subscribe" else "unsubscribe"
    }

    @Throws(Exception::class)
    fun transaction(
        transactions: IList<Transaction>,
        callback: (response: String) -> Unit,
    ) {
        val transactionsImplementation = ioImplementations.chain
        if (transactionsImplementation === null) {
            throw Exception("chain is not DYDXChainTransactionsProtocol")
        }
        transactionsImplementation.transaction(transactions) { response ->
            if (response != null) {
                val time = if (!response.contains("error")) {
                    Clock.System.now()
                } else {
                    null
                }
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    if (time != null) {
                        lastValidatorCallTime = time
                    }
                    callback(response)
//                    trackValidatorCall()
                }
            }
        }
    }

    internal fun parseTransactionResponse(response: String?): ParsingError? {
        return if (response == null) {
            V4TransactionErrors.error(null, "Unknown error")
        } else {
            val result = parser.decodeJsonObject(response)
            if (result != null) {
                val error = parser.asMap(result["error"])
                if (error != null) {
                    val message = parser.asString(error["message"])
                    val code = parser.asInt(error["code"])
                    return V4TransactionErrors.error(code, message)
                } else {
                    null
                }
            } else {
                return V4TransactionErrors.error(null, "unknown error")
            }
        }
    }

    internal fun send(error: ParsingError?, callback: TransactionCallback, data: Any? = null) {
        ioImplementations.threading?.async(ThreadingType.main) {
            if (error != null) {
                callback(false, error, data)
            } else {
                callback(true, null, data)
            }
        }
    }

    private fun tracking(eventName: String, params: IMap<String, Any>?) {
        val paramsAsString = jsonEncoder.encode(params)
        ioImplementations.threading?.async(ThreadingType.main) {
            ioImplementations.tracking?.log(eventName, paramsAsString)
        }
    }
}
