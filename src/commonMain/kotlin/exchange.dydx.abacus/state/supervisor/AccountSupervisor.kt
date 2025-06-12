package exchange.dydx.abacus.state.supervisor

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.processor.router.skip.SkipRoutePayloadProcessor
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.Changes
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
import exchange.dydx.abacus.state.machine.onChainUserStats
import exchange.dydx.abacus.state.manager.ApiData
import exchange.dydx.abacus.state.manager.AutoSweepConfig
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
import exchange.dydx.abacus.state.manager.pendingCctpWithdraw
import exchange.dydx.abacus.state.manager.processingCctpWithdraw
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.MIN_USDC_AMOUNT_FOR_AUTO_SWEEP
import exchange.dydx.abacus.utils.SLIPPAGE_PERCENT
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import exchange.dydx.abacus.utils.toNobleAddress
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

    private val nobleBalancePollingDuration = 10.0
    private var nobleBalancesTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
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
            if (value == WalletConnectionType.Cosmos) {
                nobleBalancesTimer?.cancel()
                nobleBalancesTimer = null
            }

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
            }
            if (configs.retrieveBalances) {
                retrieveBalances()
            }
            if (configs.transferNobleBalances) {
                retrieveNobleBalance()
            }
        } else {
            userStatsTimer = null
            accountBalancesTimer = null
            nobleBalancesTimer = null
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

    private fun retrieveNobleBalance() {
        if (walletConnectionType == WalletConnectionType.Cosmos) {
            nobleBalancesTimer = null
            return
        }
        if (AutoSweepConfig.disable_autosweep) {
            return
        }
        val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
        nobleBalancesTimer =
            timer.schedule(0.0, nobleBalancePollingDuration) {
                if (validatorConnected) {
                    getNobleBalance()
                    true
                } else {
                    false
                }
            }
    }

    private fun getNobleBalance() {
        helper.getOnChain(QueryType.GetNobleBalance, "") { response ->
            val balance = helper.parser.decodeJsonObject(response)
            if (balance != null) {
                val amount = helper.parser.asDecimal(balance["amount"])
//                minimum usdc required for successful tx (gas fee)
                if (amount != null && amount > MIN_USDC_AMOUNT_FOR_AUTO_SWEEP) {
                    if (processingCctpWithdraw) {
                        return@getOnChain
                    }
//                    if pending withdrawal, perform CCTP Withdrawal
                    pendingCctpWithdraw?.let { walletState ->
                        processingCctpWithdraw = true
                        val callback = walletState.callback
//                        if walletState has a singleMessagePayload, it's a single message tx
//                        otherwise walletState represents a multi message tx (like smart relay) and we should use the relevant transaction
                        val transactionType = if (walletState.multiMessagePayload == null) TransactionType.CctpWithdraw else TransactionType.CctpMultiMsgWithdraw
                        val payload = if (walletState.multiMessagePayload == null) walletState.singleMessagePayload else walletState.multiMessagePayload
                        helper.transaction(
                            transactionType,
                            payload,
                        ) { hash ->
                            val error = helper.parseTransactionResponse(hash)
                            if (error != null) {
                                Logger.e { "TransactionType.CctpWithdraw error: $error" }
                                callback?.let { it -> helper.send(error, it, hash) }
                            } else {
                                callback?.let { it -> helper.send(null, it, hash) }
                            }
                            pendingCctpWithdraw = null
                            processingCctpWithdraw = false
                        }
                    }
//                        else, transfer noble balance back to dydx
                        ?: run { sweepNobleBalanceToDydx(amount) }
                } else if (balance["error"] != null) {
                    Logger.e { "Error checking noble balance: $response" }
                }
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

    private fun sweepNobleBalanceToDydx(amount: BigDecimal) {
        sweepNobleBalanceToDydxSkip(amount = amount)
    }

    private fun sweepNobleBalanceToDydxSkip(amount: BigDecimal) {
        val url = helper.configs.skipV2MsgsDirect()
        val fromChain = helper.configs.nobleChainId()
        val fromToken = helper.configs.nobleDenom
        val nobleAddress = accountAddress.toNobleAddress() ?: return
        val chainId = helper.environment.dydxChainId ?: return
        val dydxTokenDemon = helper.environment.tokens["usdc"]?.denom ?: return
        val body: Map<String, Any> = mapOf(
            "amount_in" to amount.toPlainString(),
//              from noble denom and chain
            "source_asset_denom" to fromToken,
            "source_asset_chain_id" to fromChain,
//              to dydx denom and chain
            "dest_asset_denom" to dydxTokenDemon,
            "dest_asset_chain_id" to chainId,
            "chain_ids_to_addresses" to mapOf(
                fromChain to nobleAddress,
                chainId to accountAddress,
            ),
            "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
        )
        val header =
            iMapOf(
                "Content-Type" to "application/json",
            )
        helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, _ ->
            if (response != null) {
                val json = helper.parser.decodeJsonObject(response)
                if (json != null) {
                    val skipRoutePayloadProcessor = SkipRoutePayloadProcessor(parser = helper.parser)
                    val processedPayload = skipRoutePayloadProcessor.received(existing = mapOf(), payload = json)
                    val ibcPayload =
                        helper.parser.asString(
                            processedPayload.get("data"),
                        )
                    if (ibcPayload != null) {
                        helper.transaction(TransactionType.SendNobleIBC, ibcPayload) {
                            val error = helper.parseTransactionResponse(it)
                            if (error != null) {
                                Logger.e { "sweepNobleBalanceToDydxSkip error: $error" }
                            }
                        }
                    }
                }
            } else {
                Logger.e { "sweepNobleBalanceToDydxSkip error, code: $code" }
            }
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
