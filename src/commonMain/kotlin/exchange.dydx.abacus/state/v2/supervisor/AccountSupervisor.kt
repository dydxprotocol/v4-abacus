package exchange.dydx.abacus.state.v2.supervisor

import BlockAndTime
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.pendingCctpWithdraw
import exchange.dydx.abacus.state.manager.processingCctpWithdraw
import exchange.dydx.abacus.state.manager.utils.ApiData
import exchange.dydx.abacus.state.manager.utils.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.utils.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.utils.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.utils.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.utils.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.utils.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.account
import exchange.dydx.abacus.state.model.launchIncentivePoints
import exchange.dydx.abacus.state.model.onChainAccountBalances
import exchange.dydx.abacus.state.model.onChainDelegations
import exchange.dydx.abacus.state.model.receivedHistoricalTradingRewards
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.toNobleAddress
import kollections.iListOf
import kollections.iSetOf
import kollections.toIMap
import kotlin.time.Duration.Companion.days

internal open class AccountSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    internal val configs: AccountConfigs,
    internal val accountAddress: String,
) : DynamicNetworkSupervisor(stateMachine, helper, analyticsUtils) {
    val subaccounts = mutableMapOf<Int, SubaccountSupervisor>()
    var onboarding: OnboardingSupervisor? = null

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
        HistoricalTradingRewardsPeriod.WEEKLY
        internal set(value) {
            if (field != value) {
                field = value
                didSetHistoricalTradingRewardsPeriod(value)
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

    private val addressRetryDuration = 10.0
    private val addressContinuousMonitoringDuration = 60.0 * 60.0

    private var sourceAddressTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    internal var subaccountNumber: Int = 0  // Desired subaccountNumber
        set(value) {
            if (field != value) {
                field = value
                didSetSubaccountNumber()
            }
        }

    private fun didSetSubaccountNumber() {
        updateConnectedSubaccountNumber()
    }

    internal fun subscribeToSubaccount(subaccountNumber: Int) {
        val subaccountSupervisor = subaccounts[subaccountNumber]
        subaccountSupervisor?.retain() ?: run {
            val newSubaccountSupervisor = SubaccountSupervisor(
                stateMachine,
                helper,
                analyticsUtils,
                configs.subaccountConfigs,
                accountAddress,
                subaccountNumber
            )
            newSubaccountSupervisor.readyToConnect = readyToConnect
            newSubaccountSupervisor.indexerConnected = indexerConnected
            newSubaccountSupervisor.socketConnected = socketConnected
            newSubaccountSupervisor.validatorConnected = validatorConnected
            subaccounts[subaccountNumber] = newSubaccountSupervisor
        }
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
        onboarding?.readyToConnect = readyToConnect

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
        onboarding?.indexerConnected = indexerConnected

        if (indexerConnected) {
            if (configs.retrieveSubaccounts) {
                retrieveSubaccounts()
            }
            if (configs.retrieveHistoricalTradingRewards) {
                retrieveHistoricalTradingRewards(historicalTradingRewardPeriod)
            }
        }
    }

    override fun didSetValidatorConnected(validatorConnected: Boolean) {
        super.didSetValidatorConnected(validatorConnected)
        for ((_, subaccountSupervisor) in subaccounts) {
            subaccountSupervisor.validatorConnected = validatorConnected
        }
        onboarding?.validatorConnected = validatorConnected

        if (validatorConnected) {
            if (configs.retrieveBalances) {
                retrieveBalances()
            }
            if (configs.transferNobleBalances) {
                retrieveNobleBalance()
            }
        }
    }

    override fun didSetSocketConnected(socketConnected: Boolean) {
        super.didSetSocketConnected(socketConnected)
        for ((_, subaccountSupervisor) in subaccounts) {
            subaccountSupervisor.socketConnected = socketConnected
        }
        onboarding?.socketConnected = socketConnected
    }

    private fun retrieveSubaccounts() {
        val oldState = stateMachine.state
        val url = accountUrl()
        if (url != null) {
            helper.get(url, null, null, callback = { _, response, httpCode ->
                if (helper.success(httpCode) && response != null) {
                    retrievedSubaccounts(response)
                } else {
                    subaccountsTimer =
                        helper.ioImplementations.timer?.schedule(subaccountsPollingDelay, null) {
                            retrieveSubaccounts()
                            false
                        }
                }
            })
        }
    }

    open fun retrievedSubaccounts(response: String) {
        val oldState = stateMachine.state
        update(stateMachine.account(response), oldState)

        // Automatically connect to the first subaccount if no subaccount is connected
        updateConnectedSubaccountNumber()
    }

    private fun accountUrl(): String? {
        val url = helper.configs.privateApiUrl("account")
        return if (url != null) {
            "$url/$accountAddress"
        } else null
    }

    private fun retrieveBalances() {
        val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
        accountBalancesTimer = timer.schedule(0.0, accountBalancePollingDuration) {
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
    }

    private fun retrieveNobleBalance() {
        val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
        nobleBalancesTimer = timer.schedule(0.0, nobleBalancePollingDuration) {
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
                if (amount != null && amount > 5000) {
                    if (processingCctpWithdraw) {
                        return@getOnChain
                    }
                    pendingCctpWithdraw?.let { walletState ->
                        processingCctpWithdraw = true
                        val callback = walletState.callback
                        helper.transaction(
                            TransactionType.CctpWithdraw,
                            walletState.payload
                        ) { hash ->
                            val error = helper.parseTransactionResponse(hash)
                            if (error != null) {
                                DebugLogger.error("TransactionType.CctpWithdraw error: $error")
                                callback?.let { it -> helper.send(error, it, hash) }
                            } else {
                                callback?.let { it -> helper.send(null, it, hash) }
                            }
                            pendingCctpWithdraw = null
                            processingCctpWithdraw = false
                        }
                    } ?: run {
                        transferNobleBalance(amount)
                    }
                } else if (balance["error"] != null) {
                    DebugLogger.error("Error checking noble balance: $response")
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
        } else null
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
        val historicalTradingRewardsInPeriod = helper.parser.asNativeList(
            helper.parser.value(
                stateMachine.data,
                "wallet.account.tradingRewards.historical.$period"
            )
        )?.mutable()

        helper.retrieveTimed(
            url,
            historicalTradingRewardsInPeriod,
            "startedAt",
            0.days,
            180.days,
            "endedAt",
            null,
            params,
            previousUrl
        ) { url, response, httpCode ->
            if (helper.success(httpCode) && !response.isNullOrEmpty()) {
                val historicalTradingRewards = helper.parser.decodeJsonObject(response)?.toIMap()
                if (historicalTradingRewards != null) {
                    val changes = stateMachine.receivedHistoricalTradingRewards(
                        historicalTradingRewards,
                        period.rawValue
                    )
                    update(changes, oldState)
                    if (changes.changes.contains(Changes.tradingRewards)) {
                        retrieveHistoricalTradingRewards(period, url)
                    }
                }
            }
        }
    }

    private fun retrieveLaunchIncentivePoints() {
        val season = stateMachine.state?.launchIncentive?.currentSeason ?: return
        val url = helper.configs.launchIncentiveUrl("points")
        if (url != null) {
            helper.get(
                "${url}/${accountAddress}", iMapOf(
                    "n" to season,
                ), null
            ) { _, response, httpCode ->
                if (helper.success(httpCode) && response != null) {
                    val oldState = stateMachine.state
                    update(stateMachine.launchIncentivePoints(season, response), oldState)
                }
            }
        }
    }

    private fun updateConnectedSubaccountNumber() {
        if (subaccountNumber != connectedSubaccountNumber) {
            connectedSubaccountNumber = if (canConnectTo(subaccountNumber)) {
                subaccountNumber
            } else {
                null
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

    private fun transferNobleBalance(amount: BigDecimal) {
        val url = helper.configs.squidRoute()
        val fromChain = helper.configs.nobleChainId()
        val fromToken = helper.configs.nobleDenom()
        val nobleAddress = accountAddress.toNobleAddress()
        val chainId = helper.environment.dydxChainId
        val squidIntegratorId = helper.environment.squidIntegratorId
        val dydxTokenDemon = helper.environment.tokens["usdc"]?.denom
        if (url != null &&
            fromChain != null &&
            fromToken != null &&
            nobleAddress != null &&
            chainId != null &&
            dydxTokenDemon != null &&
            squidIntegratorId != null
        ) {
            val params: Map<String, String> = mapOf(
                "fromChain" to fromChain,
                "fromToken" to fromToken,
                "fromAddress" to nobleAddress,
                "fromAmount" to amount.toPlainString(),
                "toChain" to chainId,
                "toToken" to dydxTokenDemon,
                "toAddress" to accountAddress.toString(),
                "slippage" to "1",
                "enableForecall" to "false",
            )
            val header = iMapOf(
                "x-integrator-id" to squidIntegratorId,
            )
            helper.get(url, params, header) { _, response, code ->
                if (response != null) {
                    val json = helper.parser.decodeJsonObject(response)
                    val ibcPayload =
                        helper.parser.asString(
                            helper.parser.value(
                                json,
                                "route.transactionRequest.data"
                            )
                        )
                    if (ibcPayload != null) {
                        helper.transaction(TransactionType.SendNobleIBC, ibcPayload) {
                            val error = helper.parseTransactionResponse(it)
                            if (error != null) {
                                DebugLogger.error("transferNobleBalance error: $error")
                            }
                        }
                    }
                } else {
                    DebugLogger.error("transferNobleBalance error, code: $code")
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
                    val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
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


    private fun didSetAccountAddressRestriction(accountAddressRestriction: Restriction?) {
        updateAddressRestriction()
    }


    open fun screenAccountAddress() {
        val address = accountAddress
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
                val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
                accountAddressTimer = timer.schedule(it, it) {
                    screenAccountAddress()
                    true
                }
            }
        }
    }

    private fun rerunAddressScreeningDelay(restriction: Restriction?): Double? {
        return when (restriction) {
            Restriction.NO_RESTRICTION -> addressContinuousMonitoringDuration
            Restriction.USER_RESTRICTION_UNKNOWN -> addressRetryDuration
            else -> null
        }
    }

    open fun screen(address: String, callback: ((Restriction) -> Unit)) {
        val url = screenUrl()
        if (url != null) {
            helper.get(
                url,
                mapOf("address" to address),
                null,
                callback = { _, response, httpCode ->
                    if (helper.success(httpCode) && response != null) {
                        val payload = helper.parser.decodeJsonObject(response)?.toIMap()
                        if (payload != null) {
                            val restricted = helper.parser.asBool(payload["restricted"]) ?: false
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
                })
        }
    }

    private fun screenUrl(): String? {
        return helper.configs.publicApiUrl("screen")
    }

    internal fun restrictionReason(response: String?): UsageRestriction {
        return if (response != null) {
            val json = helper.parser.decodeJsonObject(response)
            val errors = helper.parser.asList(helper.parser.value(json, "errors"))
            val geoRestriciton = errors?.firstOrNull { error ->
                val code = helper.parser.asString(helper.parser.value(error, "code"))
                code?.contains("GEOBLOCKED") == true
            }

            if (geoRestriciton !== null)
                UsageRestriction.http403Restriction
            else
                UsageRestriction.userRestriction
        } else UsageRestriction.http403Restriction
    }

    private fun didSetSourceAddress(sourceAddress: String?, oldValue: String?) {
        sourceAddressTimer = null
        sourceAddressRestriction = null
        screenSourceAddress()
        onboarding = sourceAddress?.let {
            val newOnboarding = OnboardingSupervisor(
                stateMachine,
                helper,
                analyticsUtils,
                configs.onboardingConfigs,
                accountAddress,
                it,
            )
            newOnboarding.readyToConnect = readyToConnect
            newOnboarding.indexerConnected = indexerConnected
            newOnboarding.socketConnected = socketConnected
            newOnboarding.validatorConnected = validatorConnected
            newOnboarding
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
        )
        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.stateNotification?.stateChanged(
                stateMachine.state,
                StateChanges(
                    iListOf(Changes.restriction),
                ),
            )
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
}

// Extension properties to help with current singular subaccount

private val AccountSupervisor.subaccount: SubaccountSupervisor?
    get() {
        return if (subaccounts.count() == 1) subaccounts.values.firstOrNull() else null
    }

internal var AccountSupervisor.connectedSubaccountNumber: Int?
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

internal fun AccountSupervisor.transfer(data: String?, type: TransferInputField?) {
    onboarding?.transfer(data, type, connectedSubaccountNumber)
}


internal fun AccountSupervisor.placeOrderPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload? {
    return subaccount?.placeOrderPayload(currentHeight)
}

internal fun AccountSupervisor.closePositionPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload? {
    return subaccount?.closePositionPayload(currentHeight)
}

internal fun AccountSupervisor.cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload? {
    return subaccount?.cancelOrderPayload(orderId)
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

internal fun AccountSupervisor.commitClosePosition(
    currentHeight: Int?,
    callback: TransactionCallback
): HumanReadablePlaceOrderPayload? {
    return subaccount?.commitClosePosition(currentHeight, callback)
}

internal fun AccountSupervisor.stopWatchingLastOrder() {
    subaccount?.stopWatchingLastOrder()
}

internal fun AccountSupervisor.commitTransfer(callback: TransactionCallback) {
    onboarding?.commitTransfer(connectedSubaccountNumber, callback)
}

internal fun AccountSupervisor.commitCCTPWithdraw(callback: TransactionCallback) {
    onboarding?.commitCCTPWithdraw(connectedSubaccountNumber, callback)
}

internal fun AccountSupervisor.faucet(amount: Double, callback: TransactionCallback) {
    subaccount?.faucet(amount, callback)
}

internal fun AccountSupervisor.cancelOrder(orderId: String, callback: TransactionCallback) {
    subaccount?.cancelOrder(orderId, callback)
}

internal fun AccountSupervisor.orderCanceled(orderId: String) {
    subaccount?.orderCanceled(orderId)
}

internal fun AccountSupervisor.transferStatus(
    hash: String,
    fromChainId: String?,
    toChainId: String?,
    isCctp: Boolean
) {
    onboarding?.transferStatus(hash, fromChainId, toChainId, isCctp)
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
