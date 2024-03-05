package exchange.dydx.abacus.state.v2.supervisor

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.manager.pendingCctpWithdraw
import exchange.dydx.abacus.state.manager.processingCctpWithdraw
import exchange.dydx.abacus.state.manager.utils.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.account
import exchange.dydx.abacus.state.model.launchIncentivePoints
import exchange.dydx.abacus.state.model.onChainAccountBalances
import exchange.dydx.abacus.state.model.onChainDelegations
import exchange.dydx.abacus.state.model.receivedHistoricalTradingRewards
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.toNobleAddress
import kollections.toIMap
import kotlin.time.Duration.Companion.days

internal open class AccountSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    internal val configs: AccountConfigs,
    internal val accountAddress: String,
) : NetworkSupervisor(stateMachine, helper) {
    val subaccountSupervisors = mutableMapOf<Int, SubaccountSupervisor>()

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

    internal fun subscribeToSubaccount(subaccountNumber: Int) {
        val subaccountSupervisor = subaccountSupervisors[subaccountNumber]
        if (subaccountSupervisor == null) {
            val subaccountConfigs = SubaccountSupervisor(
                stateMachine,
                helper,
                configs.subaccountConfigs,
                accountAddress,
                subaccountNumber
            )
            subaccountSupervisors[subaccountNumber] = subaccountConfigs
        } else {
            subaccountSupervisor.retainerCount++
        }
    }

    internal fun unsubscribeFromSubaccount(subaccountNumber: Int) {
        subaccountSupervisors[subaccountNumber]?.let {
            it.retainerCount--
            if (it.retainerCount == 0) {
                subaccountSupervisors.remove(subaccountNumber)
            }
        }
    }

    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        for ((_, subaccountSupervisor) in subaccountSupervisors) {
            subaccountSupervisor.readyToConnect = readyToConnect
        }

        if (readyToConnect) {
            if (configs.retrieveLaunchIncentivePoints) {
                retrieveLaunchIncentivePoints()
            }
        }
    }

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
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
        for ((_, subaccountSupervisor) in subaccountSupervisors) {
            subaccountSupervisor.socketConnected = socketConnected
        }
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

    private fun historicalTradingRewardAggregationsParams(period: String): IMap<String, String>? {
        return iMapOf("period" to period)
    }

    private fun retrieveHistoricalTradingRewards(period: HistoricalTradingRewardsPeriod, previousUrl: String? = null) {
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
}

internal class AutoAccountSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    configs: AccountConfigs,
    accountAddress: String,
    private val subaccountNumber: Int,
) : AccountSupervisor(stateMachine, helper, configs, accountAddress) {
    internal var connectedSubaccountNumber: Int? = null
        set(value) {
            if (field != value) {
                val oldValue = value
                field = value
                didSetConnectedSubaccountNumber(oldValue)
            }
        }

    override fun retrievedSubaccounts(response: String) {
        super.retrievedSubaccounts(response)
        updateConnectedSubaccountNumber()
    }

    private fun didSetConnectedSubaccountNumber(oldValue: Int?) {
        if (oldValue != null) {
            unsubscribeFromSubaccount(oldValue)
        }
        if (connectedSubaccountNumber != null) {
            subscribeToSubaccount(connectedSubaccountNumber!!)
        }
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

    private fun canConnectTo(subaccountNumber: Int): Boolean {
        return stateMachine.state?.subaccount(subaccountNumber) != null
    }
}
