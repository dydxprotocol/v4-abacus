package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.protocols.run
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.launchIncentivePoints
import exchange.dydx.abacus.state.model.launchIncentiveSeasons
import exchange.dydx.abacus.state.model.onChainAccountBalances
import exchange.dydx.abacus.state.model.onChainDelegations
import exchange.dydx.abacus.state.model.onChainEquityTiers
import exchange.dydx.abacus.state.model.onChainFeeTiers
import exchange.dydx.abacus.state.model.onChainRewardTokenPrice
import exchange.dydx.abacus.state.model.onChainRewardsParams
import exchange.dydx.abacus.state.model.onChainUserFeeTier
import exchange.dydx.abacus.state.model.onChainUserStats
import exchange.dydx.abacus.state.model.squidChains
import exchange.dydx.abacus.state.model.squidTokens
import exchange.dydx.abacus.state.model.squidV2SdkInfo
import exchange.dydx.abacus.state.model.updateHeight
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.isAddressValid
import exchange.dydx.abacus.utils.mutableMapOf
import exchange.dydx.abacus.utils.safeSet
import kollections.toIMap
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds

class V4StateManagerAdaptor(
    deploymentUri: String,
    environment: V4Environment,
    ioImplementations: IOImplementations,
    uiImplementations: UIImplementations,
    override var configs: V4StateManagerConfigs,
    appConfigs: AppConfigs,
    stateNotification: StateNotificationProtocol?,
    dataNotification: DataNotificationProtocol?,
) : StateManagerAdaptor(
    deploymentUri,
    environment,
    ioImplementations,
    uiImplementations,
    configs,
    appConfigs,
    stateNotification,
    dataNotification,
) {
    private var validatorUrl: String? = null
        set(value) {
            field = value
            didSetValidatorUrl(value)
        }

    private var validatorConnected: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                didSetValidatorConnected(value)
            }
        }

    private val heightPollingDuration = 10.0
    private var heightTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private var chainTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private val userStatsPollingDuration = 60.0
    private var userStatsTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private val accountBalancePollingDuration = 10.0
    private var accountBalancesTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private val nobleBalancePollingDuration = 10.0
    private var nobleBalancesTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private var firstBlockAndTime: BlockAndTime? = null

    private var restRetryTimers: MutableMap<String, LocalTimerProtocol> = mutableMapOf()

    internal var indexerState = NetworkState()
    internal var validatorState = NetworkState()
    private var apiState: ApiState? = null
        set(value) {
            val oldValue = field
            if (field !== value) {
                field = value
                didSetApiState(field, oldValue)
            }
        }

    private var indexerRestriction: UsageRestriction? = null
        set(value) {
            if (field !== value) {
                field = value
                didSetIndexerRestriction(field)
            }
        }

    private var lastValidatorCallTime: Instant? = null

    override fun didSetSocketConnected(socketConnected: Boolean) {
        super.didSetSocketConnected(socketConnected)
        if (socketConnected) {
            val market = market
            if (market != null) {
                marketCandlesSubscription(market, resolution = candlesResolution, true)
            }
        }
    }

    override fun didSetMarket(market: String?, oldValue: String?) {
        super.didSetMarket(market, oldValue)
        if (market != oldValue) {
            if (oldValue != null) {
                marketCandlesSubscription(oldValue, resolution = candlesResolution, false)
            }
            if (market != null) {
                if (socketConnected) {
                    marketCandlesSubscription(market, resolution = candlesResolution, true)
                }
            }
        }
    }

    override fun didSetCandlesResolution(oldValue: String) {
        super.didSetCandlesResolution(oldValue)
        val market = market
        if (market != null && socketConnected) {
            marketCandlesSubscription(market, oldValue, false)
            marketCandlesSubscription(market, candlesResolution, true)
        }
    }

    @Throws(Exception::class)
    fun marketCandlesSubscription(market: String, resolution: String, subscribe: Boolean = true) {
        if (appConfigs.subscribeToCandles) {
            val channel = configs.candlesChannel() ?: throw Exception("candlesChannel is null")
            socket(
                socketAction(subscribe),
                channel,
                iMapOf("id" to "$market/$resolution", "batched" to "true"),
            )
        }
    }

    override fun subaccountChannelParams(
        accountAddress: String,
        subaccountNumber: Int,
    ): IMap<String, Any> {
        return iMapOf("id" to "$accountAddress/$subaccountNumber")
    }

    override fun faucetBody(amount: Double): String? {
        return if (accountAddress != null) {
            val params = iMapOf(
                "address" to accountAddress,
                "subaccountNumber" to subaccountNumber,
                "amount" to amount,
            )
            jsonEncoder.encode(params)
        } else {
            null
        }
    }

    override fun socketConnectedSubaccountNumber(id: String?): Int {
        return if (id != null) {
            val parts = id.split("/")
            if (parts.size == 2) {
                parts[1].toIntOrNull() ?: 0
            } else {
                0
            }
        } else {
            0
        }
    }

    override fun accountUrl(): String? {
        val url = configs.privateApiUrl("account")
        return if (accountAddress != null && url != null) {
            "$url/$accountAddress"
        } else {
            null
        }
    }

    override fun screenUrl(): String? {
        return configs.publicApiUrl("screen")
    }

    override fun geoUrl(): String {
        return "https://api.dydx.exchange/v4/geo"
    }

    override fun complianceScreenUrl(address: String): String? {
        val url = configs.publicApiUrl("complianceScreen") ?: return null
        return "$url/$address"
    }

    override fun complianceGeoblockUrl(): String? {
        return configs.publicApiUrl("complianceGeoblock")
    }

    override fun historicalTradingRewardAggregationsUrl(): String? {
        val url = configs.privateApiUrl("historicalTradingRewardAggregations")
        return if (accountAddress != null && url != null) {
            "$url/$accountAddress"
        } else {
            null
        }
    }

    override fun historicalTradingRewardAggregationsParams(period: String): IMap<String, String>? {
        return iMapOf("period" to "$period")
    }

    override fun subaccountParams(): IMap<String, String>? {
        val accountAddress = accountAddress
        val subaccountNumber = subaccountNumber
        return if (accountAddress != null) {
            iMapOf(
                "address" to accountAddress,
                "subaccountNumber" to "$subaccountNumber",
            )
        };
        else {
            null
        }
    }

    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        if (readyToConnect) {
            when (appConfigs.squidVersion) {
                AppConfigs.SquidVersion.V1 -> {
                    retrieveTransferChains()
                    retrieveTransferTokens()
                }

                AppConfigs.SquidVersion.V2,
                AppConfigs.SquidVersion.V2DepositOnly,
                AppConfigs.SquidVersion.V2WithdrawalOnly -> {
                    retrieveTransferAssets()
                    retrieveCctpChainIds()
                }
            }

            retrieveDepositExchanges()
            bestEffortConnectChain()
            retrieveLaunchIncentiveSeasons()
        } else {
            validatorConnected = false
            heightTimer = null
        }
    }

    private fun bestEffortConnectChain() {
        findOptimalNode { url ->
            this.validatorUrl = url
        }
    }

    private fun didSetValidatorUrl(validatorUrl: String?) {
        validatorConnected = false
        if (validatorUrl != null) {
            connectChain(validatorUrl) { successful ->
                validatorConnected = successful
            }
        } else {
            reconnectChain()
        }
    }

    private fun didSetValidatorConnected(validatorConnected: Boolean) {
        if (validatorConnected) {
            getEquityTiers()
            getFeeTiers()
            getRewardsParams()
            if (subaccount != null) {
                getUserFeeTier()
                pollUserStats()
            }
            if (accountAddress != null) {
                pollAccountBalances()
                pollNobleBalance()
            }

            val timer = ioImplementations.timer ?: CoroutineTimer.instance
            heightTimer = timer.schedule(0.0, heightPollingDuration) {
                if (readyToConnect) {
                    retrieveHeights()
                    true
                } else {
                    false
                }
            }
        } else {
            reconnectChain()
        }
    }

    private fun retrieveHeights() {
        // serialize height retrieval. Get indexer height first, then validator height
        // If indexer height is not available, then validator height is not available
        // indexer height no longer triggers api state change
        retrieveIndexerHeight {
            retrieveValidatorHeight()
        }
    }

    private fun reconnectChain() {
        if (readyToConnect) {
            // Create a timer, to try to connect the chain again
            // Do not repeat. This timer is recreated in bestEffortConnectChain if needed
            val timer = ioImplementations.timer ?: CoroutineTimer.instance
            chainTimer = timer.schedule(serverPollingDuration, null) {
                if (readyToConnect) {
                    bestEffortConnectChain()
                }
                false
            }
        }
    }

    private fun getEquityTiers() {
        getOnChain(QueryType.EquityTiers, null) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainEquityTiers(response), oldState)
        }
    }

    private fun getFeeTiers() {
        getOnChain(QueryType.FeeTiers, null) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainFeeTiers(response), oldState)
        }
    }

    private fun getRewardsParams() {
        getOnChain(QueryType.RewardsParams, null) { rewardsParams ->
            val oldState = stateMachine.state
            update(stateMachine.onChainRewardsParams(rewardsParams), oldState)

            val json = parser.decodeJsonObject(rewardsParams)
            val marketId = parser.asString(parser.value(json, "params.marketId"))
            val params = iMapOf("marketId" to marketId)
            val paramsInJson = jsonEncoder.encode(params)

            getOnChain(QueryType.GetMarketPrice, paramsInJson) { marketPrice ->
                update(stateMachine.onChainRewardTokenPrice(marketPrice), oldState)
            }
        }
    }

    @Throws(Exception::class)
    private fun getOnChain(
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

    override fun didSetAccountAddress(accountAddress: String?, oldValue: String?) {
        super.didSetAccountAddress(accountAddress, oldValue)

        if (accountAddress != null) {
            if (sourceAddress != null) {
                screenSourceAddress()
            }
            if (readyToConnect) {
                retrieveAccount()
                retrieveLaunchIncentivePoints()
                if (validatorConnected) {
                    pollAccountBalances()
                    pollNobleBalance()
                }
            }
        } else {
            accountBalancesTimer = null
        }
    }

    private fun pollAccountBalances() {
        val timer = ioImplementations.timer ?: CoroutineTimer.instance
        accountBalancesTimer = timer.schedule(0.0, accountBalancePollingDuration) {
            if (validatorConnected && accountAddress != null) {
                getAccountBalances()
                true
            } else {
                false
            }
        }
    }

    private fun getAccountBalances() {
        getOnChain(QueryType.GetAccountBalances, "") { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainAccountBalances(response), oldState)
        }

        val params = iMapOf("address" to accountAddress)
        val paramsInJson = jsonEncoder.encode(params)
        getOnChain(QueryType.GetDelegations, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainDelegations(response), oldState)
        }
    }

    private fun pollNobleBalance() {
        val timer = ioImplementations.timer ?: CoroutineTimer.instance
        nobleBalancesTimer = timer.schedule(0.0, nobleBalancePollingDuration) {
            if (validatorConnected && accountAddress != null) {
                checkNobleBalance()
                true
            } else {
                false
            }
        }
    }

    private fun checkNobleBalance() {
        getOnChain(QueryType.GetNobleBalance, "") { response ->
            val balance = parser.decodeJsonObject(response)
            if (balance != null) {
                val amount = parser.asDecimal(balance["amount"])
                if (amount != null && amount > 5000) {
                    if (processingCctpWithdraw) {
                        return@getOnChain
                    }
                    pendingCctpWithdraw?.let { walletState ->
                        processingCctpWithdraw = true
                        val callback = walletState.callback
                        transaction(TransactionType.CctpWithdraw, walletState.payload) { hash ->
                            val error = parseTransactionResponse(hash)
                            if (error != null) {
                                Logger.e { "TransactionType.CctpWithdraw error: $error" }
                                callback?.let { it -> send(error, it, hash) }
                            } else {
                                callback?.let { it -> send(null, it, hash) }
                            }
                            pendingCctpWithdraw = null
                            processingCctpWithdraw = false
                        }
                    } ?: run {
                        transferNobleBalance(amount)
                    }
                } else if (balance["error"] != null) {
                    Logger.e { "Error checking noble balance: $response" }
                }
            }
        }
    }

    override fun didSetSubaccount(subaccount: Subaccount?, oldValue: Subaccount?) {
        super.didSetSubaccount(subaccount, oldValue)
        if (validatorConnected && subaccount != null) {
            getUserFeeTier()
            pollUserStats()
        } else {
            userStatsTimer = null
        }
    }

    private fun getUserFeeTier() {
        val params = iMapOf("address" to accountAddress)
        val paramsInJson = jsonEncoder.encode(params)
        getOnChain(QueryType.UserFeeTier, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainUserFeeTier(response), oldState)
        }
    }

    private fun pollUserStats() {
        val timer = ioImplementations.timer ?: CoroutineTimer.instance
        userStatsTimer = timer.schedule(0.0, userStatsPollingDuration) {
            if (validatorConnected && subaccount != null) {
                getUserStats()
                true
            } else {
                false
            }
        }
    }

    private fun getUserStats() {
        val params = iMapOf("address" to accountAddress)
        val paramsInJson = jsonEncoder.encode(params)
        getOnChain(QueryType.UserStats, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainUserStats(response), oldState)
        }
    }

    private fun findOptimalNode(callback: (node: String?) -> Unit) {
        val endpointUrls = configs.validatorUrls()
        if (endpointUrls != null && endpointUrls.size > 1) {
            val param = iMapOf(
                "endpointUrls" to endpointUrls,
                "chainId" to environment.dydxChainId,
            )
            val json = jsonEncoder.encode(param)
            ioImplementations.threading?.async(ThreadingType.main) {
                ioImplementations.chain?.get(QueryType.OptimalNode, json) { result ->
                    if (result != null) {
                        /*
                    response = {
                        "url": "https://...",
                         */
                        val map = parser.decodeJsonObject(result)
                        val node = parser.asString(map?.get("url"))
                        ioImplementations.threading?.async(ThreadingType.abacus) {
                            callback(node)
                        }
                    } else {
                        // Not handled by client yet
                        ioImplementations.threading?.async(ThreadingType.abacus) {
                            callback(endpointUrls.firstOrNull())
                        }
                    }
                }
            }
        } else {
            val first = parser.asString(endpointUrls?.firstOrNull())
            ioImplementations.threading?.async(ThreadingType.abacus) {
                callback(first)
            }
        }
    }

    private fun connectChain(validatorUrl: String, callback: (successful: Boolean) -> Unit) {
        val indexerUrl = environment.endpoints.indexers?.firstOrNull()?.api ?: return
        val websocketUrl = configs.websocketUrl() ?: return
        val chainId = environment.dydxChainId ?: return
        val faucetUrl = configs.faucetUrl()
        val usdcToken = environment.tokens["usdc"] ?: return
        val chainToken = environment.tokens["chain"] ?: return
        val usdcDenom = usdcToken.denom
        val usdcDecimals = usdcToken.decimals
        val usdcGasDenom = usdcToken.gasDenom
        val chainTokenDenom = chainToken.denom
        val chainTokenDecimals = chainToken.decimals
        val nobleValidator = environment.endpoints.nobleValidator

        val params = mutableMapOf<String, Any>()
        params["indexerUrl"] = indexerUrl
        params["websocketUrl"] = websocketUrl
        params["validatorUrl"] = validatorUrl
        params["chainId"] = chainId
        params.safeSet("faucetUrl", faucetUrl)
        params.safeSet("nobleValidatorUrl", nobleValidator)

        params.safeSet("USDC_DENOM", usdcDenom)
        params.safeSet("USDC_DECIMALS", usdcDecimals)
        params.safeSet("USDC_GAS_DENOM", usdcGasDenom)
        params.safeSet("CHAINTOKEN_DENOM", chainTokenDenom)
        params.safeSet("CHAINTOKEN_DECIMALS", chainTokenDecimals)
        params.safeSet("txnMemo", "dYdX Frontend (${SystemUtils.platform.rawValue})")
        val jsonString = JsonEncoder().encode(params) ?: return

        ioImplementations.threading?.async(ThreadingType.main) {
            ioImplementations.chain?.connectNetwork(
                jsonString,
            ) { response ->
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    if (response != null) {
                        val json = parser.decodeJsonObject(response)
                        ioImplementations.threading?.async(ThreadingType.main) {
                            if (json != null) {
                                callback(json["error"] == null)
                            } else {
                                callback(false)
                            }
                        }
                    } else {
                        ioImplementations.threading?.async(ThreadingType.main) {
                            callback(false)
                        }
                    }
                }
            }
        }
    }

    override fun findOptimalIndexer(callback: (config: IndexerURIs?) -> Unit) {
        val endpointUrls = configs.indexerConfigs
        if (endpointUrls != null && endpointUrls.size > 1) {
            val param = iMapOf(
                "endpointUrls" to endpointUrls.map { it.api },
            )
            val json = jsonEncoder.encode(param)
            ioImplementations.threading?.async(ThreadingType.main) {
                ioImplementations.chain?.get(QueryType.OptimalIndexer, json) { result ->
                    if (result != null) {
                        /*
                    response = {
                        "url": "https://...",
                         */
                        val map = parser.decodeJsonObject(result)
                        val url = parser.asString(map?.get("url"))
                        val config = endpointUrls.firstOrNull { it.api == url }
                        ioImplementations.threading?.async(ThreadingType.abacus) {
                            callback(config)
                        }
                    } else {
                        // Not handled by client yet
                        ioImplementations.threading?.async(ThreadingType.abacus) {
                            callback(endpointUrls.firstOrNull())
                        }
                    }
                }
            }
        } else {
            val first = endpointUrls?.firstOrNull()
            ioImplementations.threading?.async(ThreadingType.abacus) {
                callback(first)
            }
        }
    }

    private fun retrieveIndexerHeight(callback: (() -> Unit)? = null) {
        val url = configs.publicApiUrl("height")
        if (url != null) {
            indexerState.previousRequestTime = indexerState.requestTime
            indexerState.requestTime = Clock.System.now()
            get(url, null, null) { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    val json = parser.decodeJsonObject(response)
                    if (json != null) {
                        val height = parser.asInt(json["height"])
                        val time = parser.asDatetime(json["time"])
                        indexerState.updateHeight(height, time)
                    } else {
                        indexerState.updateHeight(null, null)
                    }
                } else {
                    indexerState.updateHeight(null, null)
                }
                callback?.invoke()
                /*
                Indexer height no longer triggers api state change
                 */
            }
        }
    }

    private fun retrieveValidatorHeight() {
        validatorState.requestTime = Clock.System.now()
        getOnChain(QueryType.Height, null) { response ->
            parseHeight(response)
        }
    }

    private fun retrieveTransferChains() {
        val oldState = stateMachine.state
        val url = configs.squidChains()
        val squidIntegratorId = environment.squidIntegratorId
        if (url != null && squidIntegratorId != null) {
            val header = iMapOf("x-integrator-id" to squidIntegratorId)
            get(url, null, header) { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.squidChains(response), oldState)
                }
            }
        }
    }

    private fun retrieveTransferAssets() {
        val oldState = stateMachine.state
        val url = configs.squidV2Assets()
        val squidIntegratorId = environment.squidIntegratorId
        if (url != null && squidIntegratorId != null) {
            val header = iMapOf("x-integrator-id" to squidIntegratorId)
            get(url, null, header) { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.squidV2SdkInfo(response), oldState)
                }
            }
        }
    }

    private fun retrieveTransferTokens() {
        val oldState = stateMachine.state
        val url = configs.squidToken()
        val squidIntegratorId = environment.squidIntegratorId
        if (url != null && squidIntegratorId != null) {
            val header = iMapOf("x-integrator-id" to squidIntegratorId)
            get(url, null, header) { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.squidTokens(response), oldState)
                }
            }
        }
    }

    private fun parseHeight(response: String) {
        val json = parser.decodeJsonObject(response)
        if (json != null && json["error"] != null) {
            validatorState.updateHeight(null, null)
            firstBlockAndTime = null
        } else {
            val header = parser.asMap(parser.value(json, "header"))
            val height = parser.asInt(header?.get("height"))
            val time = parser.asDatetime(header?.get("time"))
            validatorState.updateHeight(height, time)
            // Always use validator blockAndHeight as source of truth
            if (firstBlockAndTime == null) {
                firstBlockAndTime = validatorState.blockAndTime
            }
            if (height != null && time != null) {
                val stateResponse = stateMachine.updateHeight(BlockAndTime(height, time))
                ioImplementations.threading?.async(ThreadingType.main) {
                    stateNotification?.stateChanged(
                        stateResponse.state,
                        stateResponse.changes,
                    )
                }
            }
        }
        updateApiState()
    }

    private fun updateApiState() {
        ioImplementations.threading?.async(ThreadingType.main) {
            apiState = apiState(apiState, indexerState, validatorState)
        }
    }

    private fun delayedInRequestTime(networkState: NetworkState): Boolean {
        val now = Clock.System.now()
        val time = networkState.requestTime
        return if (time != null) {
            val gap = now - time
            if (gap > 4.seconds) {
                // If request (time) was sent more than 4 seconds ago
                // The app was probably in background
                true
            } else {
                val previousTime = networkState.previousRequestTime
                if (previousTime != null) {
                    val gap = time - previousTime
                    // If request (time) was sent more than 15 seconds after
                    // previous request (previousTime)
                    // The app was probably in background
                    gap > (heightPollingDuration * 1.5).seconds
                } else {
                    false
                }
            }
        } else {
            false
        }
    }

    private fun delayedStatus(
        indexerState: NetworkState,
        validatorState: NetworkState,
    ): Boolean {
        return delayedInRequestTime(indexerState) || delayedInRequestTime(validatorState)
    }

    private fun apiState(
        apiState: ApiState?,
        indexerState: NetworkState,
        validatorState: NetworkState,
    ): ApiState {
        var status = apiState?.status ?: ApiStatus.UNKNOWN
        var haltedBlock = apiState?.haltedBlock
        var blockDiff: Int? = null

        val delayedStatus = delayedStatus(indexerState, validatorState)
        when (validatorState.status) {
            NetworkStatus.NORMAL -> {
                when (indexerState.status) {
                    NetworkStatus.NORMAL, NetworkStatus.UNKNOWN -> {
                        status = ApiStatus.NORMAL
                        haltedBlock = null
                    }

                    NetworkStatus.UNREACHABLE -> {
                        status = ApiStatus.INDEXER_DOWN
                        haltedBlock = null
                    }

                    NetworkStatus.HALTED -> {
                        status = ApiStatus.INDEXER_HALTED
                        haltedBlock = indexerState.blockAndTime?.block
                    }
                }
            }

            NetworkStatus.UNKNOWN -> {
                when (indexerState.status) {
                    NetworkStatus.NORMAL -> {
                        status = ApiStatus.NORMAL
                        haltedBlock = null
                    }

                    NetworkStatus.UNKNOWN -> {
                        status = ApiStatus.UNKNOWN
                        haltedBlock = null
                    }

                    NetworkStatus.UNREACHABLE -> {
                        status = ApiStatus.INDEXER_DOWN
                        haltedBlock = null
                    }

                    NetworkStatus.HALTED -> {
                        status = ApiStatus.INDEXER_HALTED
                        haltedBlock = indexerState.blockAndTime?.block
                    }
                }
            }

            NetworkStatus.UNREACHABLE -> {
                status = ApiStatus.VALIDATOR_DOWN
                haltedBlock = null
            }

            NetworkStatus.HALTED -> {
                status = ApiStatus.VALIDATOR_HALTED
                haltedBlock = validatorState.blockAndTime?.block
            }
        }
        if (status == ApiStatus.NORMAL) {
            if (!delayedStatus) {
                val indexerBlock = indexerState.blockAndTime?.block
                val validatorBlock = validatorState.blockAndTime?.block
                if (indexerBlock != null && validatorBlock != null) {
                    val diff = validatorBlock - indexerBlock
                    if (diff > MAX_NUM_BLOCK_DELAY) {
                        status = ApiStatus.INDEXER_TRAILING
                        blockDiff = diff
                        haltedBlock = null
                    }
                }
            }
        }
        val validatorBlockAndTime = validatorState.blockAndTime
        val indexerBlockAndTime = indexerState.blockAndTime
        val block = if (validatorBlockAndTime != null) {
            if (indexerBlockAndTime != null) {
                max(validatorBlockAndTime.block, indexerBlockAndTime.block)
            } else {
                validatorBlockAndTime.block
            }
        } else {
            indexerBlockAndTime?.block
        }
        if (apiState?.status != status ||
            apiState.height != block ||
            apiState.haltedBlock != haltedBlock ||
            apiState.trailingBlocks != blockDiff
        ) {
            return ApiState(status, block, haltedBlock, blockDiff)
        }
        return apiState
    }

    override fun sparklinesParams(): IMap<String, String> {
        return iMapOf("timePeriod" to "ONE_DAY")
    }

    override fun shouldBatchMarketsChannelData(): Boolean {
        return true
    }

    override fun shouldBatchMarketTradesChannelData(): Boolean {
        return true
    }

    @Throws(Exception::class)
    fun transaction(
        type: TransactionType,
        paramsInJson: String?,
        callback: (response: String) -> Unit,
    ) {
        val transactionsImplementation = ioImplementations.chain
        if (transactionsImplementation === null) {
            throw Exception("chain is not DYDXChainTransactionsProtocol")
        }
        transactionsImplementation.transaction(type, paramsInJson) { response ->
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
                    trackValidatorCall()
                }
            }
        }
    }

    val transactionQueue = TransactionQueue(this::transaction)

    private fun submitTransaction(
        transactionType: TransactionType,
        transactionPayloadString: String,
        onSubmitTransaction: (() -> Unit?)?,
        transactionCallback: (String?) -> Unit,
        useTransactionQueue: Boolean,
    ) {
        if (useTransactionQueue) {
            transactionQueue.enqueue(
                TransactionParams(
                    transactionType,
                    transactionPayloadString,
                    transactionCallback,
                    onSubmitTransaction,
                ),
            )
        } else {
            onSubmitTransaction?.invoke()
            transaction(transactionType, transactionPayloadString, transactionCallback)
        }
    }

    private fun submitPlaceOrder(
        callback: TransactionCallback,
        payload: HumanReadablePlaceOrderPayload,
        analyticsPayload: IMap<String, Any>?,
        isTriggerOrder: Boolean = false,
    ): HumanReadablePlaceOrderPayload {
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)
        val uiClickTimeMs = trackOrderClick(analyticsPayload)

        stopWatchingLastOrder()

        submitTransaction(
            TransactionType.PlaceOrder,
            string,
            onSubmitTransaction = {
                val submitTimeMs = trackOrderSubmit(uiClickTimeMs, analyticsPayload)
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.placeOrderRecords.add(
                        PlaceOrderRecord(
                            subaccountNumber,
                            clientId,
                            submitTimeMs,
                        ),
                    )
                }
            },
            transactionCallback = { response: String? ->
                val error = parseTransactionResponse(response)
                trackOrderSubmitted(error, analyticsPayload)
                if (error == null) {
                    lastOrderClientId = clientId
                } else {
                    val placeOrderRecord = this.placeOrderRecords.firstOrNull {
                        it.clientId == clientId
                    }
                    this.placeOrderRecords.remove(placeOrderRecord)
                }

                send(
                    error,
                    callback,
                    if (isTriggerOrder) {
                        HumanReadableTriggerOrdersPayload(
                            listOf(payload),
                            emptyList(),
                        )
                    } else {
                        payload
                    },
                )
            },
            useTransactionQueue = !isShortTermOrder(payload.type, payload.timeInForce),
        )

        return payload
    }

    private fun submitCancelOrder(
        orderId: String,
        callback: TransactionCallback,
        payload: HumanReadableCancelOrderPayload,
        analyticsPayload: IMap<String, Any>?,
        isTriggerOrder: Boolean = false,
    ) {
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)

        val uiClickTimeMs = trackOrderClick(analyticsPayload, isCancel = true)
        val isShortTermOrder = payload.orderFlags == 0

        stopWatchingLastOrder()

        submitTransaction(
            TransactionType.CancelOrder,
            string,
            onSubmitTransaction = {
                val submitTimeMs = trackOrderSubmit(uiClickTimeMs, analyticsPayload, true)
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.cancelOrderRecords.add(
                        CancelOrderRecord(
                            subaccountNumber,
                            clientId,
                            submitTimeMs,
                        ),
                    )
                }
            },
            transactionCallback = { response: String? ->
                val error = parseTransactionResponse(response)
                trackOrderSubmitted(error, analyticsPayload, true)
                if (error == null) {
                    this.orderCanceled(orderId)
                } else {
                    val cancelOrderRecord = this.cancelOrderRecords.firstOrNull {
                        it.clientId == clientId
                    }
                    this.cancelOrderRecords.remove(cancelOrderRecord)
                }

                send(
                    error,
                    callback,
                    if (isTriggerOrder) {
                        HumanReadableTriggerOrdersPayload(
                            emptyList(),
                            listOf(payload),
                        )
                    } else {
                        payload
                    },
                )
            },
            useTransactionQueue = !isShortTermOrder,
        )
    }

    private fun trackOrderClick(
        analyticsPayload: IMap<String, Any>?,
        isCancel: Boolean = false
    ): Double {
        val uiClickTimeMs = Clock.System.now().toEpochMilliseconds().toDouble()
        tracking(
            if (isCancel) AnalyticsEvent.TradeCancelOrderClick.rawValue else AnalyticsEvent.TradePlaceOrderClick.rawValue,
            analyticsPayload,
        )
        return uiClickTimeMs
    }

    private fun trackOrderSubmit(
        uiClickTimeMs: Double,
        analyticsPayload: IMap<String, Any>?,
        isCancel: Boolean = false
    ): Double {
        val submitTimeMs = Clock.System.now().toEpochMilliseconds().toDouble()
        val uiDelayTimeMs = submitTimeMs - uiClickTimeMs

        tracking(
            if (isCancel) AnalyticsEvent.TradeCancelOrder.rawValue else AnalyticsEvent.TradePlaceOrder.rawValue,
            ParsingHelper.merge(uiTrackingParams(uiDelayTimeMs), analyticsPayload)
                ?.toIMap(),
        )

        return submitTimeMs
    }

    private fun trackOrderSubmitted(
        error: ParsingError?,
        analyticsPayload: IMap<String, Any>?,
        isCancel: Boolean = false,
    ) {
        if (error != null) {
            tracking(
                if (isCancel) AnalyticsEvent.TradeCancelOrderSubmissionFailed.rawValue else AnalyticsEvent.TradePlaceOrderSubmissionFailed.rawValue,
                ParsingHelper.merge(errorTrackingParams(error), analyticsPayload)?.toIMap(),
            )
        } else {
            tracking(
                if (isCancel) AnalyticsEvent.TradeCancelOrderSubmissionConfirmed.rawValue else AnalyticsEvent.TradePlaceOrderSubmissionConfirmed.rawValue,
                analyticsPayload,
            )
        }
    }

    override fun commitPlaceOrder(callback: TransactionCallback): HumanReadablePlaceOrderPayload {
        val payload = placeOrderPayload()
        val midMarketPrice = stateMachine.state?.marketOrderbook(payload.marketId)?.midPrice
        val analyticsPayload = analyticsUtils.placeOrderAnalyticsPayload(payload, midMarketPrice)
        return submitPlaceOrder(callback, payload, analyticsPayload)
    }

    override fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload {
        val payload = closePositionPayload()
        val midMarketPrice = stateMachine.state?.marketOrderbook(payload.marketId)?.midPrice
        val analyticsPayload = analyticsUtils.placeOrderAnalyticsPayload(payload, midMarketPrice, true)
        return submitPlaceOrder(callback, payload, analyticsPayload)
    }

    override fun cancelOrder(orderId: String, callback: TransactionCallback) {
        val payload = cancelOrderPayload(orderId)
        val subaccount = stateMachine.state?.subaccount(subaccountNumber)
        val existingOrder = subaccount?.orders?.firstOrNull { it.id == orderId }
        val analyticsPayload = analyticsUtils.cancelOrderAnalyticsPayload(
            payload,
            existingOrder
        )

        submitCancelOrder(orderId, callback, payload, analyticsPayload)
    }

    override fun commitTriggerOrders(callback: TransactionCallback): HumanReadableTriggerOrdersPayload {
        val payloads = triggerOrdersPayload()

        payloads.cancelOrderPayloads.forEach {payload ->
            val subaccount = stateMachine.state?.subaccount(subaccountNumber)
            val existingOrder = subaccount?.orders?.firstOrNull { it.id == payload.orderId }
            val analyticsPayload = analyticsUtils.cancelOrderAnalyticsPayload(
                payload,
                existingOrder,
                true
            )
            submitCancelOrder(payload.orderId, callback, payload, analyticsPayload, true)
        }

        payloads.placeOrderPayloads.forEach { payload ->
            val midMarketPrice = stateMachine.state?.marketOrderbook(payload.marketId)?.midPrice
            val analyticsPayload = analyticsUtils.placeOrderAnalyticsPayload(
                payload,
                midMarketPrice,
                isClosePosition = false,
                fromSlTpDialog = true,
            )
            submitPlaceOrder(callback, payload, analyticsPayload, true)
        }

        if (payloads.cancelOrderPayloads.isEmpty() && payloads.placeOrderPayloads.isEmpty()) {
            send(null, callback, payloads)
        }

        return payloads
    }

    override fun commitTransfer(callback: TransactionCallback) {
        val type = stateMachine.state?.input?.transfer?.type
        when (type) {
            TransferType.deposit -> {
                commitDeposit(callback)
            }

            TransferType.withdrawal -> {
                commitWithdrawal(callback)
            }

            TransferType.transferOut -> {
                commitTransferOut(callback)
            }

            else -> {}
        }
    }

    private fun commitDeposit(callback: TransactionCallback) {
        val payload = depositPayload()
        val string = Json.encodeToString(payload)

        transaction(TransactionType.Deposit, string) { response ->
            val error = parseTransactionResponse(response)
            send(error, callback, payload)
        }
    }

    private fun commitWithdrawal(callback: TransactionCallback) {
        val payload = withdrawPayload()
        val string = Json.encodeToString(payload)

        transaction(TransactionType.Withdraw, string) { response ->
            val error = parseTransactionResponse(response)
            send(error, callback, payload)
        }
    }

    private fun commitTransferOut(callback: TransactionCallback) {
        val payload = subaccountTransferPayload()
        val string = Json.encodeToString(payload)

        transaction(TransactionType.PlaceOrder, string) { response ->
            val error = parseTransactionResponse(response)
            send(error, callback, payload)
        }
    }

    override fun commitCCTPWithdraw(callback: TransactionCallback) {
        val state = stateMachine.state
        if (state?.input?.transfer?.type == TransferType.withdrawal) {
            val decimals = environment.tokens["usdc"]?.decimals ?: 6

            val usdcSize =
                parser.asDouble(state.input.transfer.size?.usdcSize) ?: Numeric.double.ZERO
            if (usdcSize > Numeric.double.ZERO) {
                simulateWithdrawal(decimals) { gasFee ->
                    if (gasFee != null) {
                        cctpToNoble(state, decimals, gasFee, callback)
                    } else {
                        cctpToNoble(state, decimals, Numeric.decimal.ZERO, callback)
                    }
                }
            } else {
                send(V4TransactionErrors.error(null, "Invalid usdcSize"), callback)
            }
        } else {
            send(V4TransactionErrors.error(null, "Invalid transfer type"), callback)
        }
    }

    override fun faucet(amount: Double, callback: TransactionCallback) {
        val payload = faucetPayload(subaccountNumber, amount)
        val string = Json.encodeToString(payload)
        val submitTimeInMilliseconds = Clock.System.now().toEpochMilliseconds().toDouble()

        transaction(TransactionType.Faucet, string) { response ->
            val error =
                parseFaucetResponse(response, subaccountNumber, amount, submitTimeInMilliseconds)
            send(error, callback, payload)
        }
    }

    override fun parseFaucetResponse(
        response: String,
        subaccountNumber: Int,
        amount: Double,
        submitTimeInMilliseconds: Double
    ): ParsingError? {
        val result = parser.decodeJsonObject(response)
        val status = parser.asInt(result?.get("status"))
        return if (status == 202) {
            this.ioImplementations.threading?.async(ThreadingType.abacus) {
                this.faucetRecords.add(
                    FaucetRecord(
                        subaccountNumber,
                        amount,
                        submitTimeInMilliseconds,
                    ),
                )
            }
            null
        } else if (status != null) {
            V4TransactionErrors.error(null, "API error: $status")
        } else {
            val resultError = parser.asMap(result?.get("error"))
            val message = parser.asString(resultError?.get("message"))
            V4TransactionErrors.error(null, message ?: "Unknown error")
        }
    }

    override fun parseTransactionResponse(response: String?): ParsingError? {
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

    override fun didUpdateStateForTransfer(data: String?, type: TransferInputField?) {
        super.didUpdateStateForTransfer(data, type)

        val state = stateMachine.state
        if (state?.input?.transfer?.type == TransferType.deposit) {
            if (type == TransferInputField.size) {
                retrieveDepositRoute(state)
            }
        } else if (state?.input?.transfer?.type == TransferType.withdrawal) {
            if (type == TransferInputField.usdcSize ||
                type == TransferInputField.address ||
                type == TransferInputField.chain ||
                type == TransferInputField.exchange ||
                type == TransferInputField.token
            ) {
                val decimals = environment.tokens["usdc"]?.decimals ?: 6

                val usdcSize =
                    parser.asDouble(state.input.transfer.size?.usdcSize) ?: Numeric.double.ZERO
                if (usdcSize > Numeric.double.ZERO) {
                    simulateWithdrawal(decimals) { gasFee ->
                        if (gasFee != null) {
                            retrieveWithdrawalRoute(state, decimals, gasFee)
                        } else {
                            retrieveWithdrawalRoute(state, decimals, Numeric.decimal.ZERO)
                        }
                    }
                }
            }
        } else if (state?.input?.transfer?.type == TransferType.transferOut &&
            state.input.transfer.address != null &&
            (state.input.transfer.errors?.count() ?: 0) == 0
        ) {
            if (type == TransferInputField.usdcSize ||
                type == TransferInputField.size ||
                type == TransferInputField.token ||
                type == TransferInputField.address
            ) {
                val token = state.input.transfer.token
                if (token == "usdc") {
                    val decimals = environment.tokens[token]?.decimals ?: 6

                    val usdcSize =
                        parser.asDouble(state.input.transfer.size?.usdcSize) ?: Numeric.double.ZERO
                    if (usdcSize > Numeric.double.ZERO) {
                        simulateWithdrawal(decimals) { gasFee ->
                            receiveTransferGas(gasFee)
                        }
                    } else {
                        receiveTransferGas(null)
                    }
                } else if (token == "chain") {
                    val decimals = environment.tokens[token]?.decimals ?: 18

                    val address = state.input.transfer.address
                    val tokenSize =
                        parser.asDouble(state.input.transfer.size?.size) ?: Numeric.double.ZERO
                    if (tokenSize > Numeric.double.ZERO && address.isAddressValid()
                    ) {
                        simulateTransferNativeToken(decimals) { gasFee ->
                            receiveTransferGas(gasFee)
                        }
                    } else {
                        receiveTransferGas(null)
                    }
                }
            }
        }
    }

    override fun transferStatus(
        hash: String,
        fromChainId: String?,
        toChainId: String?,
        isCctp: Boolean,
        requestId: String?,
    ) {
        super.transferStatus(hash, fromChainId, toChainId, isCctp, requestId)

        fetchTransferStatus(hash, fromChainId, toChainId, isCctp, requestId)
    }

    private fun uiTrackingParams(interval: Double): IMap<String, Any> {
        return iMapOf(
            "clickToSubmitOrderDelayMs" to interval,
        )
    }

    private fun errorTrackingParams(error: ParsingError): IMap<String, Any> {
        return iMapOf(
            "errorType" to error.type.rawValue,
            "errorMessage" to error.message,
        )
    }

    override fun trackingParams(interval: Double): IMap<String, Any> {
        val validatorUrl = this.validatorUrl
        return if (validatorUrl != null) {
            iMapOf(
                "roundtripMs" to interval,
                "validatorUrl" to validatorUrl,
            )
        } else {
            iMapOf(
                "roundtripMs" to interval,
            )
        }
    }

    private fun didSetApiState(apiState: ApiState?, oldValue: ApiState?) {
        stateNotification?.apiStateChanged(apiState)
        dataNotification?.apiStateChanged(apiState)
        trackApiStateIfNeeded(apiState, oldValue)
        when (apiState?.status) {
            ApiStatus.VALIDATOR_DOWN, ApiStatus.VALIDATOR_HALTED -> {
                validatorUrl = null
            }

            ApiStatus.INDEXER_DOWN, ApiStatus.INDEXER_HALTED -> {
                indexerConfig = null
            }

            else -> {}
        }
    }

    override fun trackApiCall() {
        trackApiStateIfNeeded(apiState, null)
    }

    private fun trackValidatorCall() {
        trackApiStateIfNeeded(apiState, null)
    }

    private fun trackApiStateIfNeeded(apiState: ApiState?, oldValue: ApiState?) {
        if (apiState?.abnormalState() == true || oldValue?.abnormalState() == true) {
            val indexerTime = lastIndexerCallTime?.toEpochMilliseconds()?.toDouble()
            val validatorTime = lastValidatorCallTime?.toEpochMilliseconds()?.toDouble()
            val interval = if (indexerTime != null) {
                (
                    Clock.System.now().toEpochMilliseconds()
                        .toDouble() - indexerTime
                    )
            } else {
                null
            }
            val params = mapOf(
                "lastSuccessfulIndexerRPC" to indexerTime,
                "lastSuccessfulFullNodeRPC" to validatorTime,
                "elapsedTime" to interval,
                "blockHeight" to indexerState.blockAndTime?.block,
                "nodeHeight" to validatorState.blockAndTime?.block,
            ).filterValues { it != null } as Map<String, Any>

            tracking(AnalyticsEvent.NetworkStatus.rawValue, params.toIMap())
        }
    }

    override fun getWithFullUrl(
        fullUrl: String,
        headers: Map<String, String>?,
        callback: (url: String, response: String?, code: Int, headers: Map<String, Any>?) -> Unit
    ) {
        super.getWithFullUrl(fullUrl, headers) { url, response, httpCode, headersAsJsonString ->
            when (httpCode) {
                403 -> {
                    indexerRestriction = restrictionReason(response)
                }

                429 -> {
                    // retry after 5 seconds
                    val timer = ioImplementations.timer ?: CoroutineTimer.instance
                    val localTimer = timer.run(5.0) {
                        restRetryTimers[url]?.cancel()
                        restRetryTimers.remove(url)

                        getWithFullUrl(fullUrl, headers, callback)
                    }
                    restRetryTimers[url] = localTimer
                }

                else -> callback(url, response, httpCode, headersAsJsonString)
            }
        }
    }

    private fun didSetIndexerRestriction(indexerRestriction: UsageRestriction?) {
        updateRestriction()
    }

    override fun updateRestriction() {
        restriction = indexerRestriction ?: addressRestriction ?: UsageRestriction.noRestriction
    }

    override fun marketInfo(market: String): PlaceOrderMarketInfo? {
        val market = stateMachine.state?.market(market) ?: return null
        val v4config = market.configs?.v4 ?: return null

        return PlaceOrderMarketInfo(
            v4config.clobPairId,
            v4config.atomicResolution,
            v4config.stepBaseQuantums,
            v4config.quantumConversionExponent,
            v4config.subticksPerTick,
        )
    }

    override fun calculateCurrentHeight(): Int? {
        val latestBlockAndTime =
            validatorState.blockAndTime ?: indexerState.blockAndTime ?: return null
        val currentTime = Clock.System.now()
        val lapsedTime = currentTime - latestBlockAndTime.time
        return if (lapsedTime.inWholeMilliseconds <= 0L) {
            // This should never happen unless the clock is wrong, then we don't want to estimate height
            null
        } else {
            val firstBlockAndTime = firstBlockAndTime
            if (firstBlockAndTime == null) {
                // This should never happen, but just in case, assume 1.5s per block
                null
            } else {
                val lapsedBlocks = latestBlockAndTime.block - firstBlockAndTime.block
                if (lapsedBlocks <= 0) {
                    // This should never happen
                    null
                } else {
                    val betweenLastAndFirstBlockTime =
                        latestBlockAndTime.time - firstBlockAndTime.time
                    val averageMillisecondsPerBlock =
                        betweenLastAndFirstBlockTime.inWholeMilliseconds / lapsedBlocks
                    if (averageMillisecondsPerBlock <= 0L) {
                        // This should never happen
                        latestBlockAndTime.block
                    } else {
                        latestBlockAndTime.block + (lapsedTime.inWholeMilliseconds / averageMillisecondsPerBlock).toInt()
                    }
                }
            }
        }
    }

    private fun retrieveLaunchIncentiveSeasons() {
        val url = configs.launchIncentiveUrl("graphql")
        if (url != null) {
            val requestBody =
                "{\"operationName\":\"TradingSeasons\",\"variables\":{},\"query\":\"query TradingSeasons {tradingSeasons {startTimestamp label __typename }}\"}"
            post(
                url,
                iMapOf(
                    "content-type" to "application/json",
                    "protocol" to "dydx-v4",
                ),
                requestBody,
            ) { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    val oldState = stateMachine.state
                    update(stateMachine.launchIncentiveSeasons(response), oldState)

                    retrieveLaunchIncentivePoints()
                }
            }
        }
    }

    private fun retrieveLaunchIncentivePoints() {
        val season = stateMachine.state?.launchIncentive?.currentSeason ?: return
        val url = configs.launchIncentiveUrl("points")
        val address = accountAddress
        if (url != null && address != null) {
            get(
                "$url/$address",
                iMapOf(
                    "n" to season,
                ),
                null,
            ) { _, response, httpCode, _ ->
                if (success(httpCode) && response != null) {
                    val oldState = stateMachine.state
                    update(stateMachine.launchIncentivePoints(season, response), oldState)
                }
            }
        }
    }

    override fun dispose() {
        super.dispose()
        chainTimer?.cancel()
        chainTimer = null
        heightTimer?.cancel()
        heightTimer = null
        userStatsTimer?.cancel()
        userStatsTimer = null
        accountBalancesTimer?.cancel()
        accountBalancesTimer = null
    }

    companion object {
        @Suppress("PropertyName")
        private const val MAX_NUM_BLOCK_DELAY = 15
    }
}
