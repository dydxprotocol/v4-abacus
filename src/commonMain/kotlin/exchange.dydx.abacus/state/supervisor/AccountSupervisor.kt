package exchange.dydx.abacus.state.supervisor

import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.InternalInjectiveBalancesState
import exchange.dydx.abacus.state.machine.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.machine.ClosePositionInputField
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.abacus.state.machine.WalletConnectionType
import exchange.dydx.abacus.state.machine.account
import exchange.dydx.abacus.state.machine.historicalTradingRewards
import exchange.dydx.abacus.state.machine.launchIncentivePoints
import exchange.dydx.abacus.state.machine.onChainAccountBalances
import exchange.dydx.abacus.state.machine.onChainDelegations
import exchange.dydx.abacus.state.machine.onChainStakingRewards
import exchange.dydx.abacus.state.machine.onChainUnbonding
import exchange.dydx.abacus.state.machine.onChainUserFeeTier
import exchange.dydx.abacus.state.machine.onChainUserStakingTier
import exchange.dydx.abacus.state.machine.onChainUserStats
import exchange.dydx.abacus.state.machine.setInjectiveBalances
import exchange.dydx.abacus.state.manager.ApiData
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.HumanReadableCancelAllOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableCloseAllPositionsPayload
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.Boolean
import kotlin.collections.mutableMapOf
import kotlin.time.Duration.Companion.days

internal open class AccountSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    private val configs: AccountConfigs,
    private val screening: Boolean,
    internal val accountAddress: String,
    private val pushNotificationRegistrationHandler: PushNotificationRegistrationHandlerProtocol = PushNotificationRegistrationHandler(
        helper = helper,
        accountAddress = accountAddress,
    )
) : DynamicNetworkSupervisor(stateMachine, helper, analyticsUtils) {
    val screener = AccountScreener(
        stateMachine = stateMachine,
        helper = helper,
        analyticsUtils = analyticsUtils,
        screeningUpdate = screening,
        accountAddress = accountAddress,
        complianceUpdated = {
            if (subaccounts.isEmpty()) {
                retrieveSubaccounts()
            }
        },
    )

    val subaccounts = mutableMapOf<Int, SubaccountSupervisor>()

    var currentIncentiveSeason: String? = null
        set(value) {
            if (field != value) {
                field = value
                retrieveLaunchIncentivePoints()
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

    private val injectiveBalancesPollingSeconds = 10.0
    private var injectiveBalancesTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private val injectiveDeposit = InjectiveDepositSupervisor(helper)

    internal var injectiveAddress: String? = null
        set(value) {
            if (field != value) {
                field = value
                val oldState = stateMachine.state
                update(stateMachine.setInjectiveBalances(null), oldState)
                retrieveInjectiveBalances()
            }
        }

    private val subaccountsPollingDelay = 15.0
    private var subaccountsTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    var historicalTradingRewardPeriod: HistoricalTradingRewardsPeriod =
        HistoricalTradingRewardsPeriod.DAILY
        internal set(value) {
            if (field != value) {
                field = value
                didSetHistoricalTradingRewardsPeriod(value)
            }
        }

    var walletConnectionType: WalletConnectionType? = WalletConnectionType.Ethereum
        internal set(value) {
            field = value
            sendPushNotificationToken()
        }

    internal var subaccountNumber: Int? // Desired subaccountNumber
        get() {
            return subaccount?.subaccountNumber
        }
        set(value) {
            subaccounts.keys.filter { it != value }.forEach {
                subaccounts[it]?.forceRelease()
                subaccounts.remove(it)
            }
            if (subaccounts.contains(value).not() && value != null) {
                subscribeToSubaccount(value)
            }
        }

    internal val connectedSubaccountNumber: Int?
        get() {
            return if (subaccount?.realized == true) subaccount?.subaccountNumber else null
        }

    private val userStatsPollingDuration = 60.0
    private var userStatsTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private var pushNotificationToken: String? = null
    private var pushNotificationLanguageCode: String? = null

    internal fun subscribeToSubaccount(subaccountNumber: Int) {
        val isSubaccountRealized = stateMachine.state?.subaccount(subaccountNumber) != null
        val subaccountSupervisor = subaccounts[subaccountNumber]
        subaccountSupervisor?.retain()
            ?: run {
                val newSubaccountSupervisor =
                    SubaccountSupervisor(
                        stateMachine = stateMachine,
                        helper = helper,
                        analyticsUtils = analyticsUtils,
                        configs = configs.subaccountConfigs,
                        accountAddress = accountAddress,
                        subaccountNumber = subaccountNumber,
                    )
                newSubaccountSupervisor.readyToConnect = readyToConnect
                newSubaccountSupervisor.indexerConnected = indexerConnected
                newSubaccountSupervisor.socketConnected = socketConnected
                newSubaccountSupervisor.validatorConnected = validatorConnected
                subaccounts[subaccountNumber] = newSubaccountSupervisor
            }

        // if this is the first realized subaccount, retrieve user fee tier and user stats
        if (validatorConnected && isSubaccountRealized && !subaccounts.values.any { it.realized }) {
            if (configs.retrieveUserFeeTier) {
                retrieveUserFeeTier()
            }
            if (configs.retrieveUserStats) {
                retrieveUserStats()
            }
        }

        subaccounts[subaccountNumber]?.realized = isSubaccountRealized
    }

    internal fun unsubscribeFromSubaccount(subaccountNumber: Int) {
        val accountSupervisor = subaccounts[subaccountNumber] ?: return
        accountSupervisor.release()
        if (accountSupervisor.retainCount == 0) {
            subaccounts.remove(subaccountNumber)
        }
    }

    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        for ((_, subaccountSupervisor) in subaccounts) {
            subaccountSupervisor.readyToConnect = readyToConnect
        }

        if (readyToConnect) {
            if (configs.retrieveLaunchIncentivePoints) {
                retrieveLaunchIncentivePoints()
            }
            retrieveInjectiveBalances()
        } else {
            injectiveBalancesTimer = null
        }
    }

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)
        for ((_, subaccountSupervisor) in subaccounts) {
            subaccountSupervisor.indexerConnected = indexerConnected
        }

        if (indexerConnected) {
            if (configs.retrieveSubaccounts) {
                retrieveSubaccounts()
            }
            if (configs.retrieveHistoricalTradingRewards) {
                retrieveHistoricalTradingRewards(historicalTradingRewardPeriod)
            }

            sendPushNotificationToken()
        } else {
            subaccountsTimer = null
        }
    }

    override fun didSetValidatorConnected(validatorConnected: Boolean) {
        super.didSetValidatorConnected(validatorConnected)
        for ((_, subaccountSupervisor) in subaccounts) {
            subaccountSupervisor.validatorConnected = validatorConnected
        }

        if (validatorConnected) {
            if (subaccounts.values.any { it.realized }) {
                if (configs.retrieveUserFeeTier) {
                    retrieveUserFeeTier()
                }
                if (configs.retrieveUserStats) {
                    retrieveUserStats()
                }
                if (configs.retrieveUserStakingTier) {
                    retrieveUserStakingTier()
                }
            }
            if (configs.retrieveBalances) {
                retrieveBalances()
            }
        } else {
            userStatsTimer = null
            accountBalancesTimer = null
        }
    }

    override fun didSetSocketConnected(socketConnected: Boolean) {
        super.didSetSocketConnected(socketConnected)
        for ((_, subaccountSupervisor) in subaccounts) {
            subaccountSupervisor.socketConnected = socketConnected
        }
    }

    private fun retrieveSubaccounts() {
        val url = accountUrl()
        if (url != null) {
            helper.get(
                url = url,
                params = null,
                headers = null,
                callback = { _, response, httpCode, _ ->
                    val isValidResponse = helper.success(httpCode) && response != null
                    if (isValidResponse) {
                        response?.let { retrievedSubaccounts(it) }
                    }
                    if (!isValidResponse && httpCode != 403) {
                        subaccountNumber = 0
                        subaccountsTimer =
                            helper.ioImplementations.timer?.schedule(
                                subaccountsPollingDelay,
                                null,
                            ) {
                                retrieveSubaccounts()
                                false
                            }
                    }
                },
            )
        }
    }

    open fun retrievedSubaccounts(response: String) {
        val oldState = stateMachine.state
        update(stateMachine.account(response), oldState)

        if (subaccounts.isNotEmpty()) {
            for ((subaccountNumber, subaccount) in subaccounts) {
                subaccount.realized = canConnectTo(subaccountNumber)
            }
        } else {
            subaccountNumber = 0
        }
    }

    private fun accountUrl(): String? {
        val url = helper.configs.privateApiUrl("account")
        return if (url != null) {
            "$url/$accountAddress"
        } else {
            null
        }
    }

    private fun retrieveUserFeeTier() {
        val params = iMapOf("address" to accountAddress)
        val paramsInJson = helper.jsonEncoder.encode(params)
        helper.getOnChain(QueryType.UserFeeTier, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainUserFeeTier(response), oldState)
        }
    }

    private fun retrieveUserStats() {
        val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
        userStatsTimer =
            timer.schedule(0.0, userStatsPollingDuration) {
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
        val paramsInJson = helper.jsonEncoder.encode(params)
        helper.getOnChain(QueryType.UserStats, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainUserStats(response), oldState)
        }
    }

    private fun retrieveUserStakingTier() {
        val params = iMapOf("address" to accountAddress)
        val paramsInJson = helper.jsonEncoder.encode(params)
        helper.getOnChain(QueryType.UserStakingTier, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainUserStakingTier(response), oldState)
        }
    }

    private fun retrieveBalances() {
        val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
        accountBalancesTimer =
            timer.schedule(0.0, accountBalancePollingDuration) {
                if (validatorConnected) {
                    getAccountBalances()
                    true
                } else {
                    false
                }
            }
    }

    private fun getAccountBalances() {
        helper.getOnChain(QueryType.GetAccountBalances, "") { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainAccountBalances(response), oldState)
        }

        val params = iMapOf("address" to accountAddress)
        val paramsInJson = helper.jsonEncoder.encode(params)
        helper.getOnChain(QueryType.GetDelegations, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainDelegations(response), oldState)
        }

        helper.getOnChain(QueryType.GetCurrentUnstaking, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainUnbonding(response), oldState)
        }

        helper.getOnChain(QueryType.GetStakingRewards, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainStakingRewards(response), oldState)
        }
    }

    private fun retrieveInjectiveBalances() {
        if (injectiveAddress == null || !readyToConnect) {
            injectiveBalancesTimer = null
            return
        }
        val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
        injectiveBalancesTimer =
            timer.schedule(0.0, injectiveBalancesPollingSeconds) {
                if (readyToConnect) {
                    getInjectiveBalances()
                    true
                } else {
                    false
                }
            }
    }

    private fun getInjectiveBalances(completion: ((InternalInjectiveBalancesState?) -> Unit)? = null) {
        val address = injectiveAddress ?: return
        injectiveDeposit.fetchBalances(address) { balances ->
            if (balances != null && address == injectiveAddress) {
                val oldState = stateMachine.state
                update(stateMachine.setInjectiveBalances(balances), oldState)
            }
            completion?.invoke(balances)
        }
    }

    internal fun commitInjectiveDeposit(callback: TransactionCallback) {
        val injectiveAddress = injectiveAddress
        if (injectiveAddress == null) {
            helper.send(
                ParsingError(ParsingErrorType.MissingRequiredData, "Injective wallet not connected"),
                callback,
            )
            return
        }
        getInjectiveBalances { balances ->
            injectiveDeposit.commit(accountAddress, injectiveAddress, balances) { error, hash ->
                getInjectiveBalances()
                helper.send(error, callback, hash)
            }
        }
    }

    private fun didSetHistoricalTradingRewardsPeriod(period: HistoricalTradingRewardsPeriod) {
        if (indexerConnected) {
            retrieveHistoricalTradingRewards(period)
        }
    }

    private fun historicalTradingRewardAggregationsUrl(): String? {
        val url = helper.configs.privateApiUrl("historicalTradingRewardAggregations")
        return if (url != null) {
            "$url/$accountAddress"
        } else {
            null
        }
    }

    private fun historicalTradingRewardAggregationsParams(period: String): IMap<String, String> {
        return iMapOf("period" to period)
    }

    internal fun retrieveHistoricalTradingRewards(
        period: HistoricalTradingRewardsPeriod,
        previousUrl: String? = null
    ) {
        val oldState = stateMachine.state
        val url = historicalTradingRewardAggregationsUrl() ?: return
        val params = historicalTradingRewardAggregationsParams(period.rawValue)
        val historicalTradingRewardsInPeriod =
            helper.parser
                .asNativeList(
                    helper.parser.value(
                        stateMachine.data,
                        "wallet.account.tradingRewards.historical.$period",
                    ),
                )
                ?.mutable()

        val tradingRewardsStartDate =
            Instant.fromEpochMilliseconds(helper.environment.rewardsHistoryStartDateMs.toLong())
        val maxDuration = Clock.System.now() - tradingRewardsStartDate + 2.days

        helper.retrieveTimed(
            url = url,
            items = historicalTradingRewardsInPeriod,
            timeField = { item ->
                helper.parser.asDatetime(helper.parser.asMap(item)?.get("startedAt"))
            },
            sampleDuration = 1.days,
            maxDuration = maxDuration,
            beforeParam = "startingBeforeOrAt",
            afterParam = null,
            additionalParams = params,
            previousUrl = previousUrl,
        ) { url, response, httpCode, _ ->
            if (helper.success(httpCode) && !response.isNullOrEmpty()) {
                val changes = stateMachine.historicalTradingRewards(response, period)
                update(changes, oldState)
                if (changes.changes.contains(Changes.tradingRewards)) {
                    retrieveHistoricalTradingRewards(period, url)
                }
            }
        }
    }

    private fun retrieveLaunchIncentivePoints() {
        val season = stateMachine.state?.launchIncentive?.currentSeason ?: return
        val url = helper.configs.launchIncentiveUrl("points")
        if (url != null) {
            helper.get(
                url = "$url/$accountAddress",
                params = iMapOf(
                    "n" to season,
                ),
                headers = null,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    val oldState = stateMachine.state
                    update(stateMachine.launchIncentivePoints(season, response), oldState)
                }
            }
        }
    }

    private fun canConnectTo(subaccountNumber: Int): Boolean {
        return stateMachine.state?.subaccount(subaccountNumber) != null
    }

    internal fun retrieveHistoricalPnls() {
        for ((_, subaccount) in subaccounts) {
            subaccount.retrieveHistoricalPnls()
        }
    }

    internal fun receiveSubaccountChannelSocketData(
        info: SocketInfo,
        subaccountNumber: Int,
        payload: IMap<String, Any>,
        height: BlockAndTime?,
    ) {
        val subaccount = subaccounts[subaccountNumber] ?: return
        subaccount.receiveSubaccountChannelSocketData(info, payload, height)
    }

    internal fun registerPushNotification(token: String, languageCode: String?) {
        pushNotificationToken = token
        pushNotificationLanguageCode = languageCode
        sendPushNotificationToken()
    }

    private fun sendPushNotificationToken() {
        val pushNotificationToken = pushNotificationToken ?: return
        val pushNotificationLanguageCode = pushNotificationLanguageCode ?: "en"
        pushNotificationRegistrationHandler.sendPushNotificationToken(
            token = pushNotificationToken,
            languageCode = pushNotificationLanguageCode,
            isKepler = walletConnectionType == WalletConnectionType.Cosmos,
        )
    }
}

// Extension properties to help with current singular subaccount

private val AccountSupervisor.subaccount: SubaccountSupervisor?
    get() {
        return if (subaccounts.count() == 1) subaccounts.values.firstOrNull() else null
    }

internal val AccountSupervisor.notifications: IMap<String, Notification>
    get() {
        return subaccount?.notifications ?: iMapOf()
    }

internal fun AccountSupervisor.trade(data: String?, type: TradeInputField?) {
    subaccount?.trade(data, type)
}

internal fun AccountSupervisor.closePosition(data: String?, type: ClosePositionInputField) {
    subaccount?.closePosition(data, type)
}

internal fun AccountSupervisor.triggerOrders(data: String?, type: TriggerOrdersInputField?) {
    subaccount?.triggerOrders(data, type)
}

internal fun AccountSupervisor.adjustIsolatedMargin(
    data: String?,
    type: AdjustIsolatedMarginInputField?,
) {
    subaccount?.adjustIsolatedMargin(data, type)
}

internal fun AccountSupervisor.placeOrderPayload(
    currentHeight: Int?
): HumanReadablePlaceOrderPayload? {
    return subaccount?.placeOrderPayload(currentHeight)
}

internal fun AccountSupervisor.closePositionPayload(
    currentHeight: Int?
): HumanReadablePlaceOrderPayload? {
    return subaccount?.closePositionPayload(currentHeight)
}

internal fun AccountSupervisor.triggerOrdersPayload(
    currentHeight: Int?
): HumanReadableTriggerOrdersPayload? {
    return subaccount?.triggerOrdersPayload(currentHeight)
}

internal fun AccountSupervisor.adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload? {
    return subaccount?.adjustIsolatedMarginPayload()
}

internal fun AccountSupervisor.cancelOrderPayload(
    orderId: String
): HumanReadableCancelOrderPayload? {
    return subaccount?.cancelOrderPayload(orderId)
}

internal fun AccountSupervisor.cancelAllOrdersPayload(marketId: String?): HumanReadableCancelAllOrdersPayload? {
    return subaccount?.cancelAllOrdersPayload(marketId)
}

internal fun AccountSupervisor.closeAllPositionsPayload(currentHeight: Int?): HumanReadableCloseAllPositionsPayload? {
    return subaccount?.closeAllPositionsPayload(currentHeight)
}

internal fun AccountSupervisor.depositPayload(): HumanReadableDepositPayload? {
    return subaccount?.depositPayload()
}

internal fun AccountSupervisor.withdrawPayload(): HumanReadableWithdrawPayload? {
    return subaccount?.withdrawPayload()
}

internal fun AccountSupervisor.subaccountTransferPayload(): HumanReadableSubaccountTransferPayload? {
    return subaccount?.subaccountTransferPayload()
}

internal fun AccountSupervisor.commitPlaceOrder(
    currentHeight: Int?,
    callback: TransactionCallback
): HumanReadablePlaceOrderPayload? {
    return subaccount?.commitPlaceOrder(currentHeight, callback)
}

internal fun AccountSupervisor.commitTriggerOrders(
    currentHeight: Int?,
    callback: TransactionCallback
): HumanReadableTriggerOrdersPayload? {
    return subaccount?.commitTriggerOrders(currentHeight, callback)
}

internal fun AccountSupervisor.commitAdjustIsolatedMargin(
    callback: TransactionCallback
): HumanReadableSubaccountTransferPayload? {
    return subaccount?.commitAdjustIsolatedMargin(callback)
}

internal fun AccountSupervisor.commitClosePosition(
    currentHeight: Int?,
    callback: TransactionCallback
): HumanReadablePlaceOrderPayload? {
    return subaccount?.commitClosePosition(currentHeight, callback)
}

internal fun AccountSupervisor.stopWatchingLastOrder() {
    subaccount?.stopWatchingLastOrder()
}

internal fun AccountSupervisor.faucet(amount: Double, callback: TransactionCallback) {
    subaccount?.faucet(amount, callback)
}

internal fun AccountSupervisor.cancelOrder(orderId: String, callback: TransactionCallback) {
    subaccount?.cancelOrder(orderId = orderId, callback = callback)
}

internal fun AccountSupervisor.cancelAllOrders(marketId: String?, callback: TransactionCallback) {
    subaccount?.cancelAllOrders(marketId, callback)
}

internal fun AccountSupervisor.closeAllPositions(currentHeight: Int?, callback: TransactionCallback): HumanReadableCloseAllPositionsPayload? {
    return subaccount?.closeAllPositions(currentHeight, callback)
}

internal fun AccountSupervisor.orderCanceled(orderId: String) {
    subaccount?.orderCanceled(orderId)
}

internal fun AccountSupervisor.refresh(data: ApiData) {
    when (data) {
        ApiData.HISTORICAL_TRADING_REWARDS -> {
            retrieveHistoricalTradingRewards(historicalTradingRewardPeriod)
        }
        ApiData.HISTORICAL_PNLS -> {
            subaccount?.refresh(data)
        }
    }
}
