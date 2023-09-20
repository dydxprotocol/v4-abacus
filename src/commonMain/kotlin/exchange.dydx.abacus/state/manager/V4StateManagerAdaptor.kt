package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.AnalyticsEvent
import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.app.ApiState
import exchange.dydx.abacus.state.app.ApiStatus
import exchange.dydx.abacus.state.app.IndexerURIs
import exchange.dydx.abacus.state.app.NetworkState
import exchange.dydx.abacus.state.app.NetworkStatus
import exchange.dydx.abacus.state.app.V4Environment
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.configs.V4StateManagerConfigs
import exchange.dydx.abacus.state.modal.TransferInputField
import exchange.dydx.abacus.state.modal.onChainAccountBalances
import exchange.dydx.abacus.state.modal.onChainEquityTiers
import exchange.dydx.abacus.state.modal.onChainFeeTiers
import exchange.dydx.abacus.state.modal.onChainRewardsParams
import exchange.dydx.abacus.state.modal.onChainUserFeeTier
import exchange.dydx.abacus.state.modal.onChainUserStats
import exchange.dydx.abacus.state.modal.squidChains
import exchange.dydx.abacus.state.modal.squidTokens
import exchange.dydx.abacus.state.modal.transfer
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.isAddressValid
import io.ktor.client.utils.EmptyContent.status
import kollections.iListOf
import kollections.iMapOf
import kollections.toIMap
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.math.max

class V4StateManagerAdaptor(
    ioImplementations: IOImplementations,
    uiImplementations: UIImplementations,
    environment: V4Environment,
    override var configs: V4StateManagerConfigs,
    stateNotification: StateNotificationProtocol?,
    dataNotification: DataNotificationProtocol?,
) : StateManagerAdaptor(
    ioImplementations, uiImplementations, environment, configs, stateNotification, dataNotification
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


    private val MAX_NUM_BLOCK_DELAY = 10

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
        val channel = configs.candlesChannel() ?: throw Exception("candlesChannel is null")
        socket(socketAction(subscribe), channel, iMapOf("id" to "$market/$resolution"))
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
                "amount" to amount
            )
            jsonEncoder.encode(params)
        } else null
    }

    override fun socketConnectedSubaccountNumber(id: String?): Int {
        return if (id != null) {
            val parts = id.split("/")
            if (parts.size == 2) {
                parts[1].toIntOrNull() ?: 0
            } else 0
        } else 0
    }

    override fun subaccountsUrl(): String? {
        val url = configs.privateApiUrl("subaccounts")
        return if (url != null) {
            "$url/$accountAddress"
        } else null
    }

    override fun subaccountParams(): IMap<String, String>? {
        val accountAddress = accountAddress
        val subaccountNumber = subaccountNumber
        return if (accountAddress != null) iMapOf(
            "address" to accountAddress,
            "subaccountNumber" to "$subaccountNumber",
        );
        else null
    }

    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        if (readyToConnect) {
            retrieveTransferChains()
            retrieveTransferTokens()

            bestEffortConnectChain()
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
                getTransfers()
            }
            if (accountAddress != null) {
                pollAccountBalances()
            }

            val timer = ioImplementations.timer ?: CoroutineTimer.instance
            heightTimer = timer.schedule(0.0, heightPollingDuration) {
                if (readyToConnect) {
                    retrieveIndexerHeight()
                    retrieveValidatorHeight()
                    true
                } else {
                    false
                }
            }
        } else {
            reconnectChain()
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
        getOnChain(QueryType.RewardsParams, null) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainRewardsParams(response), oldState)
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
                } else null
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
            retrieveSubaccounts()
            pollAccountBalances()
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
    }

    override fun didSetSubaccount(subaccount: Subaccount?, oldValue: Subaccount?) {
        super.didSetSubaccount(subaccount, oldValue)
        if (validatorConnected && subaccount != null) {
            getUserFeeTier()
            pollUserStats()
            getTransfers()
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

    private fun getTransfers() {
        getOnChain(QueryType.Transfers, null) { response ->
//            stateMachine.parseOnChainUserFeeTier(response)
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
                        val map = Json.parseToJsonElement(result).jsonObject.toIMap()
                        val node = parser.asString(map["url"])
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
        val indexerUrl = environment.URIs.indexers?.firstOrNull()?.api ?: return
        val websocketUrl = configs.websocketUrl() ?: return
        val chainId = environment.dydxChainId ?: return
        val faucetUrl = configs.faucetUrl()
        ioImplementations.threading?.async(ThreadingType.main) {
            ioImplementations.chain?.connectNetwork(
                indexerUrl,
                websocketUrl,
                validatorUrl,
                chainId,
                faucetUrl
            ) { response ->
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    if (response != null) {
                        val json = Json.parseToJsonElement(response).jsonObject.toIMap()
                        ioImplementations.threading?.async(ThreadingType.main) {
                            callback(json["error"] == null)
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
                        val map = Json.parseToJsonElement(result).jsonObject.toIMap()
                        val url = parser.asString(map["url"])
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

    private fun retrieveIndexerHeight() {
        val url = configs.publicApiUrl("height")
        if (url != null) {
            indexerState.requestTime = Clock.System.now()
            get(url, null, null, false) { response, httpCode ->
                if (success(httpCode) && response != null) {
                    val json = Json.parseToJsonElement(response).jsonObject.toIMap()
                    val height = parser.asInt(json["height"])
                    val time = parser.asDatetime(json["time"])
                    indexerState.updateHeight(height, time)
                } else {
                    indexerState.updateHeight(null, null)
                }
                updateApiState()
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
        if (url != null) {
            get(url, null, null, false) { response, httpCode ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.squidChains(response), oldState)
                }
            }
        }
    }

    private fun retrieveTransferTokens() {
        val oldState = stateMachine.state
        val url = configs.squidToken()
        if (url != null) {
            get(url, null, null, false) { response, httpCode ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.squidTokens(response), oldState)
                }
            }
        }
    }

    private fun parseHeight(response: String) {
        val json = Json.parseToJsonElement(response).jsonObject.toIMap()
        if (json["error"] != null) {
            validatorState.updateHeight(null, null)
        } else {
            val header = parser.asMap(parser.value(json, "header"))
            val height = parser.asInt(header?.get("height"))
            val time = parser.asDatetime(header?.get("time"))
            validatorState.updateHeight(height, time)
        }
        updateApiState()
    }

    private fun updateApiState() {
        ioImplementations.threading?.async(ThreadingType.main) {
            apiState = apiState(apiState, indexerState, validatorState)
        }
    }

    private fun apiState(
        apiState: ApiState?,
        indexerState: NetworkState,
        validatorState: NetworkState,
    ): ApiState {
        var status = apiState?.status ?: ApiStatus.UNKNOWN
        var haltedBlock = apiState?.haltedBlock
        var blockDiff: Int? = null
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
                        haltedBlock = indexerState.block
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
                        haltedBlock = indexerState.block
                    }
                }
            }

            NetworkStatus.UNREACHABLE -> {
                status = ApiStatus.VALIDATOR_DOWN
                haltedBlock = null
            }

            NetworkStatus.HALTED -> {
                status = ApiStatus.VALIDATOR_HALTED
                haltedBlock = validatorState.block
            }
        }
        if (status == ApiStatus.NORMAL) {
            val indexerBlock = indexerState.block
            val validatorBlock = validatorState.block
            if (indexerBlock != null && validatorBlock != null) {
                val diff = validatorBlock - indexerBlock
                if (diff > MAX_NUM_BLOCK_DELAY) {
                    status = ApiStatus.INDEXER_TRAILING
                    blockDiff = diff
                    haltedBlock = null
                }
            }
        }
        val block = if (validatorState.block != null) {
            if (indexerState.block != null) {
                max(validatorState.block!!, indexerState.block!!)
            } else validatorState.block
        } else indexerState.block
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
                } else null
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

    override fun commitPlaceOrder(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        val submitTimeInMilliseconds = Clock.System.now().toEpochMilliseconds().toDouble()
        val payload = placeOrderPayload()
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)

        lastOrderClientId = null
        transaction(TransactionType.PlaceOrder, string) { response ->
            val error = parseTransactionResponse(response)
            if (error == null) {
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.placeOrderRecords.add(
                        PlaceOrderRecord(
                            subaccountNumber,
                            payload.clientId,
                            submitTimeInMilliseconds,
                        )
                    )
                    lastOrderClientId = clientId
                }
            }
            send(error, callback, payload)
        }
        return payload
    }

    override fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload? {
        val submitTimeInMilliseconds = Clock.System.now().toEpochMilliseconds().toDouble()
        val payload = closePositionPayload()
        val clientId = payload.clientId
        val string = Json.encodeToString(payload)

        lastOrderClientId = null
        transaction(TransactionType.PlaceOrder, string) { response ->
            val error = parseTransactionResponse(response)
            if (error == null) {
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.placeOrderRecords.add(
                        PlaceOrderRecord(
                            subaccountNumber,
                            payload.clientId,
                            submitTimeInMilliseconds,
                        )
                    )
                    lastOrderClientId = clientId
                }
            }
            send(error, callback, payload)
        }
        return payload
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
        val result =
            Json.parseToJsonElement(response).jsonObject.toIMap()
        val status = parser.asInt(result["status"])
        return if (status == 202) {
            this.ioImplementations.threading?.async(ThreadingType.abacus) {
                this.faucetRecords.add(
                    FaucetRecord(
                        subaccountNumber,
                        amount,
                        submitTimeInMilliseconds,
                    )
                )
            }
            null
        } else if (status != null) {
            V4TransactionErrors.error(null, "API error: $status")
        } else {
            val resultError = parser.asMap(result.get("error"))
            val message = parser.asString(resultError?.get("message"))
            V4TransactionErrors.error(null, message ?: "Unknown error")
        }
    }

    override fun cancelOrder(orderId: String, callback: TransactionCallback) {
        val submitTimeInMilliseconds = Clock.System.now().toEpochMilliseconds().toDouble()
        val payload = cancelOrderPayload(orderId)
        val string = Json.encodeToString(payload)

        transaction(TransactionType.CancelOrder, string) { response ->
            val error = parseTransactionResponse(response)
            if (error == null) {
                ioImplementations.threading?.async(ThreadingType.abacus) {
                    this.cancelOrderRecords.add(
                        CancelOrderRecord(
                            subaccountNumber,
                            payload.clientId,
                            submitTimeInMilliseconds,
                        )
                    )
                }
            }
            send(error, callback, payload)
        }
    }

    override fun parseTransactionResponse(response: String?): ParsingError? {
        return if (response == null) {
            V4TransactionErrors.error(null, "Unknown error")
        } else {
            val result = Json.parseToJsonElement(response).jsonObject.toIMap()
            val error = parser.asMap(result["error"])
            if (error != null) {
                val message = parser.asString(error["message"])
                val code = parser.asInt(error["code"])
                return V4TransactionErrors.error(code, message)
            } else {
                null
            }
        }
    }

    private fun send(error: ParsingError?, callback: TransactionCallback, data: Any? = null) {
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
                type == TransferInputField.token
            ) {
                if ((state.input.transfer.size?.usdcSize ?: 0.0) > 0.0) {
                    simulateWithdrawal { gasFee ->
                        if (gasFee != null) {
                            retrieveWithdrawalRoute(gasFee)
                        } else {
                            retrieveWithdrawalRoute(0.0)
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
                    if ((state.input.transfer.size?.usdcSize ?: 0.0) > 0.0) {
                        simulateWithdrawal { gasFee ->
                            receiveTransferGas(gasFee)
                        }
                    } else {
                        receiveTransferGas(null)
                    }
                } else if (token == "dydx") {
                    val address = state.input.transfer.address
                    if ((state.input.transfer.size?.size
                            ?: 0.0) > 0.0 && address.isAddressValid()
                    ) {
                        simulateTransferNativeToken { gasFee ->
                            receiveTransferGas(gasFee)
                        }
                    } else {
                        receiveTransferGas(null)
                    }
                }
            }
        }
    }

    override fun transferStatus(hash: String, fromChainId: String?, toChainId: String?) {
        super.transferStatus(hash, fromChainId, toChainId)

        fetchTransferStatus(hash, fromChainId, toChainId)
    }

    override fun trackingParams(interval: Double): IMap<String, Any> {
        val validatorUrl = this.validatorUrl
        return if (validatorUrl != null) iMapOf(
            "roundtripMs" to interval,
            "validatorUrl" to validatorUrl,
        ) else iMapOf(
            "roundtripMs" to interval,
        )
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
            val interval = if (indexerTime != null) (Clock.System.now().toEpochMilliseconds()
                .toDouble() - indexerTime) else null
            val params = mapOf(
                "lastSuccessfulIndexerRPC" to indexerTime,
                "lastSuccessfulFullNodeRPC" to validatorTime,
                "elapsedTime" to interval,
                "blockHeight" to indexerState.block,
                "nodeHeight" to validatorState.block,
            ).filterValues { it != null } as Map<String, Any>

            tracking(AnalyticsEvent.NetworkStatus.rawValue, params.toIMap())
        }
    }

    override fun get(
        url: String,
        params: IMap<String, String>?,
        headers: IMap<String, String>?,
        private: Boolean,
        callback: (String?, Int) -> Unit
    ) {
        super.get(url, params, headers, private) { response, httpCode ->
            when (httpCode) {
                403 -> {
                    ioImplementations.threading?.async(ThreadingType.abacus) {
                        val error = ParsingError(
                            ParsingErrorType.HttpError403,
                            "API error: $httpCode",
                            "ERRORS.HTTP_ERROR_$httpCode"
                        )

                        emitError(error)
                        trackApiCall()
                    }
                }

                429 -> {
                    /* We may need better handling of 429 */
                    callback(response, httpCode)
                }

                else -> callback(response, httpCode)
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
}
