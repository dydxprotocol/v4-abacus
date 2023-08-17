package exchange.dydx.abacus.state.app.adaptors

import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.app.*
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.modal.*
import exchange.dydx.abacus.utils.*
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIMap
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.times

@OptIn(ExperimentalTime::class)
open class ApiAdaptor(
    private val environment: String,
    private val isMainNet: Boolean,
    private val version: AppVersion,
    private val maxSubaccountNumber: Int,
) : ApiAdaptorProtocol,
    ApiConfigurationsProtocol {
    override val localizer: LocalizerProtocol? = null // UIImplementations.localizer ?: throw Exception("Localizer not implemented")

    internal var parser = Parser()
    internal var jsonEncoder = JsonEncoder()

    internal var stateMachine: TradingStateMachine =
        PerpTradingStateMachine(null, null, version, maxSubaccountNumber)

    private var readyToConnect: Boolean = false
    private var accountAddress: String? = null
    private var market: String? = null
    private var historicalPnlPeriod: HistoricalPnlPeriod = HistoricalPnlPeriod.Period1d

    internal var subaccountNumber: Int = 0
    internal var connectedSubaccountNumber: Int? = null
    internal var lastOrder: SubaccountOrder? = null

    /*
    Client should get the list of candleOptions from market for displaying.
    Then set candlesResolution from one of the option's resolution
     */
    private var candlesResolution: String = "1HOUR"
    internal var socketConnected: Boolean = false
    internal var accountSocketId: String? = null

    private val maxGap = 2.hours
    private val maxDuration = 100.hours

    internal open fun respond(
        state: PerpetualState?,
        changes: StateChanges?,
        errors: IList<ParsingError>?,
        networkRequests: NetworkRequests?,
    ): AppStateResponse {
        return AppStateResponse(state, changes, errors, networkRequests, apiState(), lastOrder)
    }

    internal open fun apiState(): ApiState? {
        return null
    }

    internal open fun updateLastOrder(state: PerpetualState?) {
    }

    internal fun allStates(): IList<Changes> {
        return iListOf(
            Changes.configs,
            Changes.wallet,
            Changes.input,
            Changes.assets,
            Changes.markets,
            Changes.orderbook,
            Changes.candles,
            Changes.trades,
            Changes.subaccount,
            Changes.historicalPnl,
            Changes.fills,
            Changes.transfers,
            Changes.historicalFundings
        )
    }


    override fun setReadyToConnect(
        readyToConnect: Boolean,
    ): AppStateResponse {
        if (this.readyToConnect != readyToConnect) {
            this.readyToConnect = readyToConnect
            return if (this.readyToConnect) {
                respond(
                    stateMachine.state,
                    null,
                    null,
                    activatedRequests()
                )
            } else {
                websocketUrl()?.let {
                    setSocketConnected(it, false)
                }
                respond(stateMachine.state, null, null, null)
            }
        }
        return respond(stateMachine.state, null, null, null)
    }

    override fun accountAddress(): String? {
        return accountAddress
    }

    override fun subaccountNumber(): Int {
        return subaccountNumber
    }

    override fun orderbookGrouping(): OrderbookGrouping {
        return OrderbookGrouping.invoke(stateMachine.groupingMultiplier) ?: OrderbookGrouping.none
    }

    override fun setOrderbookGrouping(
        orderbookGrouping: OrderbookGrouping,
    ): AppStateResponse {
        val stateResponse = stateMachine.setOrderbookGrouping(market, orderbookGrouping.rawValue)
        return respond(
            stateResponse.state ?: stateMachine.state,
            stateResponse.changes,
            stateResponse.errors,
            null
        )
    }

    fun setAccountAddress(
        accountAddress: String?,
    ): AppStateResponse {
        /*
        unsubscribe from existing accounts channel
        subscribe to new accounts channel
         */
        val url = websocketUrl()
        return if (this.accountAddress != accountAddress && url != null) {
            val oldValue = this.accountAddress
            val oldConnectedSubaccountNumber = connectedSubaccountNumber
            this.accountAddress = accountAddress
            this.subaccountNumber = 0
            this.stateMachine.resetWallet(accountAddress)
            this.connectedSubaccountNumber =
                connectingSubaccountNumber(subaccountNumber)
            respond(
                stateMachine.state,
                StateChanges(iListOf(Changes.wallet), null),
                null,
                accountRequest(
                    url,
                    accountAddress,
                    connectedSubaccountNumber,
                    oldValue,
                    oldConnectedSubaccountNumber
                )
            )
        } else respond(stateMachine.state, null, null, null)
    }

    internal open fun connectingSubaccountNumber(
        subaccountNumber: Int,
    ): Int? {
        return 0
    }

    internal open fun accountRequest(
        url: AbUrl,
        address: String?,
        subaccountNumber: Int?,
        oldAddress: String?,
        oldSubaccountNumber: Int?,
    ): NetworkRequests? {
        val socketRequests = iMutableListOf<SocketRequest>()
        if (socketConnected) {
            if (oldAddress != null && oldSubaccountNumber != null) {
                val unsubscribeMessage = socketAccounts(false, oldAddress, oldSubaccountNumber)
                if (unsubscribeMessage != null) {
                    socketRequests.add(
                        SocketRequest(
                            SocketRequestType.SocketText,
                            url,
                            unsubscribeMessage,
                            false
                        )
                    )
                }
            }
            if (address != null && subaccountNumber != null) {
                val subscribeMessage = socketAccounts(true, address, subaccountNumber)
                if (subscribeMessage != null) {
                    socketRequests.add(
                        SocketRequest(
                            SocketRequestType.SocketText,
                            url,
                            subscribeMessage,
                            accountIsPrivate()
                        )
                    )
                }
            }
        }
        val historicalPnl = accountHistoricalPnl()
        return networkRequests(
            if (socketRequests.size > 0) socketRequests else null,
            if (historicalPnl != null)
                iListOf(historicalPnl)
            else null
        )
    }

    internal open fun networkRequests(
        socketRequests: IList<SocketRequest>?,
        restRequests: IList<RestRequest>?,
    ): NetworkRequests? {
        return if (socketRequests != null || restRequests != null)
            NetworkRequests(socketRequests, restRequests)
        else null
    }

    override fun market(): String? {
        return market
    }

    override fun setMarket(market: String?): AppStateResponse {
        /*
        unsubscribe from existing trades, orderbook, and candles channel
        subscribe to new accounts trades, orderbook, and candles channel
         */
        if (this.market != market) {
            val oldValue = this.market
            this.market = market
            val stateResponse =
                if (market != null) stateMachine.tradeInMarket(market, subaccountNumber) else {
                    DebugLogger.error("Unable to set tradeInMarket")
                    null
                }
            return respond(
                stateResponse?.state ?: stateMachine.state,
                stateResponse?.changes,
                stateResponse?.errors,
                networkRequests(
                    marketSocketRequests(stateMachine, oldValue),
                    marketRestRequests(stateMachine, oldValue)
                )
            )
        }
        return respond(stateMachine.state, null, null, null)
    }

    override fun historicalPnlPeriod(): HistoricalPnlPeriod {
        return historicalPnlPeriod
    }

    override fun setHistoricalPnlPeriod(
        historicalPnlPeriod: HistoricalPnlPeriod,
    ): AppStateResponse {
        return if (this.historicalPnlPeriod != historicalPnlPeriod && connectedSubaccountNumber != null) {
            this.historicalPnlPeriod = historicalPnlPeriod
            val stateResponse = stateMachine.setHistoricalPnlDays(
                when (historicalPnlPeriod) {
                    HistoricalPnlPeriod.Period1d -> 1
                    HistoricalPnlPeriod.Period7d -> 7
                    HistoricalPnlPeriod.Period30d -> 30
                    HistoricalPnlPeriod.Period90d -> 90
                }, connectedSubaccountNumber!!
            )
            return respond(
                stateResponse.state,
                stateResponse.changes,
                stateResponse.errors,
                null
            )
        } else respond(stateMachine.state, null, null, null)
    }

    override fun updateHistoricalPnl(): AppStateResponse {
        val historicalPnl = accountHistoricalPnl()
        val networkRequests =
            if (historicalPnl != null) NetworkRequests(null, iListOf(historicalPnl)) else null
        return respond(stateMachine.state, null, null, networkRequests)
    }

    override fun candlesResolution(): String {
        return candlesResolution
    }

    override fun setCandlesResolution(
        candlesResolution: String,
    ): AppStateResponse {
        if (this.candlesResolution != candlesResolution) {
            this.candlesResolution = candlesResolution
        }
        val candlesRequest = marketCandles()
        return respond(
            stateMachine.state, null, null,
            if (candlesRequest != null) networkRequests(null, iListOf(candlesRequest)) else null
        )
    }

    override fun setSocketConnected(
        url: AbUrl,
        socketConnected: Boolean,
    ): AppStateResponse {
        return if (this.socketConnected != socketConnected) {
            this.socketConnected = socketConnected
            respond(
                stateMachine.state,
                null,
                null,
                socketConnectedRequests()
            )
        } else {
            respond(stateMachine.state, null, null, null)
        }
    }

    override fun processSocketResponse(
        url: AbUrl,
        text: String,
        height: Int?,
    ): AppStateResponse? {
        return if (url.host == websocketHost()) {
            val stateResponse = stateMachine.socket(url, text, subaccountNumber, height)
            val socketInfo = stateResponse.info
            val restRequests = iMutableListOf<RestRequest>()
            when (socketInfo?.type) {
                "connected" -> {
                    return setSocketConnected(url, true)
                }

                "subscribed" -> {
                    when (socketInfo.channel) {
                        "v3_markets", "v4_markets" -> {
                            val market = this.market
                            val sparklines = sparklines()
                            if (sparklines != null) {
                                restRequests.add(sparklines)
                            }
                        }

                        "v3_accounts", "v4_subaccounts" -> {
                            accountSocketId = socketInfo.id
                            if (accountSocketId != null) {
                                val fillsRequest = subaccountFills()
                                if (fillsRequest != null) {
                                    restRequests.add(fillsRequest)
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
            updateLastOrder(stateResponse.state)
            respond(
                stateResponse.state,
                stateResponse.changes,
                stateResponse.errors,
                if (restRequests.size > 0) networkRequests(
                    null,
                    restRequests
                ) else null
            )
        } else null
    }

    override fun processHttpResponse(
        url: AbUrl,
        text: String,
        height: Int?,
    ): AppStateResponse? {
        return if (url.host == apiHost() || url.host == configsHost()) {
            if (url.urlString == serverTime()?.url?.urlString) {
                /*
            Server time is used for private API signings
            This is done at the AppStateMachine level, not StateMachine
             */
                val json = Json.parseToJsonElement(text).jsonObject.toIMap()
                val now = ServerTime.now()
                ServerTime.delta = parser.asDatetime(json["iso"])?.minus(now)

                respond(stateMachine.state, null, null, null)
            } else {
                val stateResponse = stateMachine.rest(url, text, subaccountNumber, height)
                updateLastOrder(stateResponse.state)
                respond(
                    stateResponse.state,
                    stateResponse.changes,
                    stateResponse.errors,
                    nextNetworkRequests(url, stateResponse.changes)
                )
            }
        } else null
    }

    internal open fun nextNetworkRequests(
        url: AbUrl,
        changes: StateChanges?,
    ): NetworkRequests? {
        val requests = iMutableListOf<RestRequest>()
        if (changes?.changes?.contains(Changes.historicalPnl) == true) {
            val request = accountHistoricalPnl()
            if (request != null) {
                requests.add(request)
            }
        }
        if (changes?.changes?.contains(Changes.historicalFundings) == true) {
            val request = marketHistoricalFunding()
            if (request != null) {
                requests.add(request)
            }
        }
        if (changes?.changes?.contains(Changes.candles) == true) {
            val request = marketCandles()
            if (request != null) {
                requests.add(request)
            }
        }
        return if (requests.size > 0) networkRequests(null, requests) else null
    }


    override fun trade(
        data: String?,
        type: TradeInputField?,
    ): AppStateResponse {
        val stateResponse = stateMachine.trade(data, type, subaccountNumber())
        return respond(
            stateResponse.state,
            stateResponse.changes,
            stateResponse.errors,
            null
        )
    }

    override fun closePosition(
        data: String?,
        type: ClosePositionInputField,
    ): AppStateResponse {
        val stateResponse = stateMachine.closePosition(data, type, subaccountNumber())
        return respond(
            stateResponse.state,
            stateResponse.changes,
            stateResponse.errors,
            null
        )
    }

    override fun transfer(
        data: String?,
        type: TransferInputField?,
    ): AppStateResponse {
        val stateResponse = stateMachine.transfer(data, type, subaccountNumber)
        return respond(
            stateResponse.state,
            stateResponse.changes,
            stateResponse.errors,
            null
        )
    }

    fun noChange(): AppStateResponse {
        return AppStateResponse(stateMachine.state, null, null, null, null, null)
    }

    override fun commit(): AppStateResponse {
        TODO("Not yet implemented")
    }

    override fun faucet(amount: Int): AppStateResponse {
        val faucetRequest = if (isMainNet) null else this.faucetRequest(amount)
        return respond(
            stateMachine.state,
            null,
            null,
            if (faucetRequest != null) {
                NetworkRequests(null, iListOf(faucetRequest))
            } else null
        )
    }

    override fun ping(): AppStateResponse {
        return respond(
            stateMachine.state,
            null,
            null,
            null
        )
    }

    internal open fun faucetRequest(amount: Int): RestRequest? {
        return null
    }

    private fun activatedRequests(): NetworkRequests? {
        return if (readyToConnect) {
            val connection = socketConnection()
            val socketConnections: IList<SocketRequest>? =
                if (connection != null) iListOf(connection) else null
            networkRequests(
                socketConnections,
                activateRestRequests()
            )
        } else null
    }

    internal open fun activateRestRequests(): IList<RestRequest>? {
        return if (readyToConnect) {
            val restRequests = iMutableListOf<RestRequest>()
            val marketConfigs = marketsConfigs()
            if (marketConfigs != null) {
                restRequests.add(marketConfigs)
            }
            val apiConfigs = apiConfigs()
            if (apiConfigs != null) {
                restRequests.add(apiConfigs)
            }
            val networkConfigs = networkConfigs()
            if (networkConfigs != null) {
                restRequests.add(networkConfigs)
            }
            val feeTiers = feeTiers()
            if (feeTiers != null) {
                restRequests.add(feeTiers)
            }
            val feeDiscounts = feeDiscounts()
            if (feeDiscounts != null) {
                restRequests.add(feeDiscounts)
            }
            val serverTime = serverTime()
            if (serverTime != null) {
                restRequests.add(serverTime)
            }
            val marketRequests = marketRestRequests(stateMachine, null)
            if (marketRequests != null) {
                restRequests.addAll(marketRequests)
            }
            val historicalPnl = accountHistoricalPnl()
            if (historicalPnl != null) {
                restRequests.add(historicalPnl)
            }

            val transferAssets = transferAssetsRequests()
            if (transferAssets != null) {
                restRequests.addAll(transferAssets)
            }

            return restRequests
        } else null
    }

    private fun socketConnectedRequests(): NetworkRequests? {
        return if (readyToConnect) {
            networkRequests(
                subscriptionsAtConnect(),
                null
            )
        } else null
    }

    internal open fun subscriptionsAtConnect(): IList<SocketRequest>? {
        val requests = iMutableListOf<SocketRequest>()
        websocketUrl()?.let { url ->
            val marketSocketText = socketMarkets(true)
            if (marketSocketText != null) {
                requests.add(
                    SocketRequest(
                        SocketRequestType.SocketText,
                        url,
                        marketSocketText, false
                    )
                )
            }

            val market = this.market
            if (market != null) {
                val tradeSocketText = socketTrades(true, market)
                if (tradeSocketText != null) {
                    requests.add(
                        SocketRequest(
                            SocketRequestType.SocketText,
                            url,
                            tradeSocketText,
                            false
                        )
                    )
                }
                val orderbookSocketText = socketOrderbook(true, market)
                if (orderbookSocketText != null) {
                    requests.add(
                        SocketRequest(
                            SocketRequestType.SocketText,
                            url,
                            orderbookSocketText,
                            false
                        )
                    )
                }
            }
        }
        websocketUrl()?.let { url ->
            val accountAddress = this.accountAddress()
            if (accountAddress != null) {
                requests.add(
                    SocketRequest(
                        SocketRequestType.SocketText,
                        url,
                        socketAccounts(true, accountAddress, subaccountNumber),
                        true
                    )
                )
            }
        }
        return if (requests.size != 0) requests else null
    }

    private fun marketSocketRequests(
        stateMachine: TradingStateMachine?,
        oldValue: String?,
    ): IList<SocketRequest>? {
        val url = websocketUrl()
        if (socketConnected && market != oldValue && url != null) {
            val requests = iMutableListOf<SocketRequest>()
            if (oldValue != null) {
                requests.add(
                    SocketRequest(
                        SocketRequestType.SocketText,
                        url,
                        socketTrades(false, oldValue),
                        false
                    )
                )
                requests.add(
                    SocketRequest(
                        SocketRequestType.SocketText,
                        url,
                        socketOrderbook(false, oldValue),
                        false
                    )
                )
            }
            val market = this.market
            if (market != null) {
                requests.add(
                    SocketRequest(
                        SocketRequestType.SocketText,
                        url,
                        socketTrades(true, market),
                        false
                    )
                )
                requests.add(
                    SocketRequest(
                        SocketRequestType.SocketText,
                        url,
                        socketOrderbook(true, market),
                        false
                    )
                )
            }
            return if (requests.size != 0) requests else null
        } else {
            return null
        }
    }

    internal open fun marketRestRequests(
        stateMachine: TradingStateMachine?,
        oldValue: String?,
    ): IList<RestRequest>? {
        return if (readyToConnect) {
            val restRequests = iMutableListOf<RestRequest>()
            val marketHistoricalFunding = marketHistoricalFunding()
            if (marketHistoricalFunding != null) {
                restRequests.add(marketHistoricalFunding)
            }
            val market = this.market
            val marketCandles = marketCandles()
            if (marketCandles != null) {
                restRequests.add(marketCandles)
            }
            if (restRequests.size > 0) restRequests else null
        } else null
    }

    internal fun marketHistoricalFunding(): RestRequest? {
        val market = market
        val host = apiHost()
        return if (host != null && market != null) {
            val historicalFunding = parser.asList(
                parser.value(
                    stateMachine.data,
                    "markets.markets.$market.historicalFunding"
                )
            )

            val apiPath = publicApiPath("historical-funding")
            val path = "$apiPath/$market"

            return timedRequest(
                host,
                path,
                historicalFunding,
                "effectiveAt",
                1.hours,
                30.days,
                "effectiveBeforeOrAt"
            )
        } else null
    }

    internal fun timedRequest(
        host: String,
        path: String,
        items: IList<Any>?,
        timeField: String,
        sampleDuration: Duration,
        maxDuration: Duration,
        beforeParam: String,
        afterParam: String? = null,
        private: Boolean = false,
        additionalParams: IList<String>? = null,
    ): RestRequest? {
        return if (items != null) {
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
                RestRequest.buildRestRequest(
                    apiScheme(),
                    apiHost(),
                    path,
                    timedParams(
                        beforeOrAt,
                        beforeParam,
                        lastItemTime + 1.seconds,
                        afterParam,
                        additionalParams
                    ),
                    HttpVerb.get,
                    private
                )
            } else if (firstItemTime != null) {
                /*
                Get previous fundings
                 */
                if (now - firstItemTime > maxDuration) {
                    null
                } else {
                    val beforeOrAt = firstItemTime - 1.seconds
                    val after = beforeOrAt - 99 * sampleDuration
                    RestRequest.buildRestRequest(
                        apiScheme(),
                        apiHost(), path,
                        timedParams(beforeOrAt, beforeParam, after, afterParam, additionalParams),
                        HttpVerb.get,
                        private
                    )
                }
            } else {
                /*
                No need to get more
                 */
                null
            }
        } else {
            /*
            Get latest
             */
            val params = additionalParams?.joinToString("&")
            RestRequest.buildRestRequest(
                apiScheme(), host, path, params, HttpVerb.get, private
            )
        }
    }

    private fun timedParams(
        before: Instant?,
        beforeParam: String,
        after: Instant?,
        afterParam: String?,
        additionalParams: IList<String>? = null,
    ): String? {
        val params = iMutableListOf<String>()
        val beforeString = before?.toString()
        if (beforeString != null) {
            params.add("$beforeParam=$beforeString")
        }

        val afterString = after?.toString()
        if (afterString != null) {
            params.add("$afterParam=$afterString")
        }

        if (additionalParams != null) {
            params.addAll(additionalParams)
        }
        return if (params.size > 0) {
            params.joinToString("&")
        } else null
    }

    internal fun marketCandles(): RestRequest? {
        val market = market()
        val host = apiHost()
        return if (market != null && host != null) {
            val candleResolution = candlesResolution() ?: return null
            val resolutionDuration = candleOptionDuration(stateMachine, market, candleResolution)
            if (resolutionDuration == null) null else {
                val maxDuration = resolutionDuration * 365
                val marketCandles = parser.asList(
                    parser.value(
                        stateMachine?.data,
                        "markets.markets.$market.candles.$candleResolution"
                    )
                )

                val apiPath = publicApiPath("candles")
                val path = "$apiPath/$market"

                return timedRequest(
                    host,
                    path,
                    marketCandles,
                    "startedAt",
                    resolutionDuration,
                    maxDuration,
                    "toISO",
                    "fromISO",
                    false,
                    iListOf("resolution=$candleResolution")
                )
            }
        } else null
    }

    private fun candleOptionDuration(
        stateMachine: TradingStateMachine?,
        market: String,
        option: String,
    ): Duration? {
        val options =
            stateMachine?.state?.marketsSummary?.markets?.get(market)?.configs?.candleOptions
        val option = options?.firstOrNull {
            it.value == option
        }
        return option?.seconds?.seconds
    }

    override fun websocketScheme(): String {
        return "wss"
    }

    override fun websocketHost(): String? {
        return null
    }

    override fun websocketPath(): String? {
        return null
    }

    internal fun websocketUrl(): AbUrl? {
        websocketHost()?.let { host ->
            websocketPath()?.let { path ->
                val scheme = websocketScheme()
                return AbUrl.fromString("$scheme://$host$path").validate()
            }
        }
        return null
    }

    override fun apiHost(): String? {
        return null
    }

    override fun privateApiPath(type: String): String? {
        return null
    }

    override fun publicApiPath(type: String): String? {
        return null
    }

    override fun configsHost(): String? {
        return null
    }

    override fun configsApiPath(type: String): String? {
        return null
    }

    private fun socketConnection(): SocketRequest? {
        return if (socketConnected) null else {
            val url = websocketUrl()
            if (url != null) SocketRequest(
                SocketRequestType.SocketConnect,
                url,
                null,
                false
            ) else null
        }
    }

    private fun networkConfigs(): RestRequest? {
        return RestRequest.buildRestRequest(
            "https",
            configsHost(),
            configsApiPath("network_configs")
        )
    }

    private fun marketsConfigs(): RestRequest? {
        return RestRequest.buildRestRequest("https", configsHost(), configsApiPath("markets"))
    }

    private fun feeTiers(): RestRequest? {
        return RestRequest.buildRestRequest("https", configsHost(), configsApiPath("fee_tiers"))
    }

    private fun feeDiscounts(): RestRequest? {
        return RestRequest.buildRestRequest("https", configsHost(), configsApiPath("fee_discounts"))
    }

    override fun apiScheme(): String {
        return "https"
    }

    override fun configsScheme(): String {
        return "https"
    }

    private fun apiConfigs(): RestRequest? {
        return RestRequest.buildRestRequest(configsScheme(), apiHost(), publicApiPath("config"))
    }

    internal open fun socketAccounts(
        subscribe: Boolean,
        address: String,
        subaccountNumber: Int,
    ): String? {
        return null
    }

    internal open fun sparklines(): RestRequest? {
        val apiPath = publicApiPath("sparklines")
        return RestRequest.buildRestRequest(
            apiScheme(),
            apiHost(),
            if (apiPath != null) "$apiPath" else null,
            "timePeriod=ONE_DAY"
        )
    }

    internal open fun subaccountFills(): RestRequest? {
        return null
    }

    internal open fun transferAssetsRequests(): IList<RestRequest>? {
        return null
    }

    private fun serverTime(): RestRequest? {
        return RestRequest.buildRestRequest(
            apiScheme(), apiHost(), publicApiPath("time")
        )
    }

    internal open fun accountHistoricalPnl(): RestRequest? {
        return if (accountIsConnected()) {
            val host = apiHost()
            val path =
                if (accountIsPrivate()) privateApiPath("historical-pnl") else publicApiPath("historical-pnl")
            return if (host != null && path != null) {
                val historicalPnl = parser.asList(
                    parser.value(
                        stateMachine.data,
                        "wallet.account.subaccounts.$subaccountNumber.historicalPnl"
                    )
                )?.mutable()
                val lastHistoricalPnl = parser.asMap(historicalPnl?.lastOrNull())
                if (lastHistoricalPnl != null && parser.asBool(lastHistoricalPnl["calculated"]) == true) {
                    historicalPnl?.removeLast()
                }
                timedRequest(
                    host,
                    path,
                    historicalPnl,
                    "createdAt",
                    1.days,
                    180.days,
                    "createdBeforeOrAt",
                    "createdAtOrAfter",
                    accountIsPrivate()
                )
            } else null
        } else null
    }

    internal open fun accountIsPrivate(): Boolean {
        return false
    }

    internal fun accountIsConnected(): Boolean {
        return accountAddress != null && connectedSubaccountNumber != null
    }

    internal fun socketType(subscribe: Boolean): String {
        return if (subscribe) "subscribe" else "unsubscribe"
    }

    internal open fun socketMarkets(subscribe: Boolean): String? {
        return null
    }

    internal open fun socketAccounts(subscribe: Boolean): String? {
        return null
    }

    internal open fun socketTrades(subscribe: Boolean, market: String): String? {
        return null
    }

    internal open fun socketOrderbook(subscribe: Boolean, market: String): String? {
        return null
    }

    internal fun socket(
        type: String, channel: String, params: IMap<String, Any>? = null,
    ): String {
        val request = iMutableMapOf<String, Any>("type" to type, "channel" to channel)
        params?.let {
            for ((key, value) in it) {
                request[key] = value
            }
        }
        return jsonEncoder.encode(request)
    }

}