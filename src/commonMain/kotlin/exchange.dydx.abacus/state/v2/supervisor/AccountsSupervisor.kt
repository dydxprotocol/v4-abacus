package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.output.ComplianceAction
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.UsageRestriction
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.ParsingException
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.ApiData
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.HistoricalPnlPeriod
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.model.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.TriggerOrdersInputField
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import kollections.iListOf

internal class AccountsSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    internal val configs: AccountConfigs,
) : NetworkSupervisor(stateMachine, helper, analyticsUtils) {
    internal val accounts = mutableMapOf<String, AccountSupervisor>()

    internal var historicalPnlPeriod: HistoricalPnlPeriod
        get() {
            return when (stateMachine.historicalPnlDays) {
                1 -> HistoricalPnlPeriod.Period1d
                7 -> HistoricalPnlPeriod.Period7d
                30 -> HistoricalPnlPeriod.Period30d
                90 -> HistoricalPnlPeriod.Period90d
                else -> HistoricalPnlPeriod.Period1d
            }
        }
        internal set(value) {
            if (historicalPnlPeriod != value) {
                helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                    when (value) {
                        HistoricalPnlPeriod.Period1d -> stateMachine.historicalPnlDays = 1
                        HistoricalPnlPeriod.Period7d -> stateMachine.historicalPnlDays = 7
                        HistoricalPnlPeriod.Period30d -> stateMachine.historicalPnlDays = 30
                        HistoricalPnlPeriod.Period90d -> stateMachine.historicalPnlDays = 90
                    }
                    didSetHistoricalPnlPeriod()

                    val changes = StateChanges(iListOf(Changes.historicalPnl))
                    val oldState = stateMachine.state
                    update(changes, oldState)
                }
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

    fun subscribeToAccount(address: String) {
        val accountSupervisor = accounts[address]
        accountSupervisor?.retain() ?: run {
            val newAccountSupervisor =
                AccountSupervisor(stateMachine, helper, analyticsUtils, configs, address)
            newAccountSupervisor.historicalTradingRewardPeriod = historicalTradingRewardPeriod
            newAccountSupervisor.readyToConnect = readyToConnect
            newAccountSupervisor.indexerConnected = indexerConnected
            newAccountSupervisor.socketConnected = socketConnected
            newAccountSupervisor.validatorConnected = validatorConnected
            accounts[address] = newAccountSupervisor
        }
    }

    fun unsubscribeFromAccount(address: String) {
        val accountSupervisor = accounts[address] ?: return
        accountSupervisor.release()
        if (accountSupervisor.retainCount == 0) {
            accounts.remove(address)
        }
    }

    private fun didSetHistoricalTradingRewardsPeriod(period: HistoricalTradingRewardsPeriod) {
        for (account in accounts.values) {
            account.historicalTradingRewardPeriod = period
        }
    }

    private fun didSetHistoricalPnlPeriod() {
        for (account in accounts.values) {
            account.retrieveHistoricalPnls()
        }
    }

    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        for (account in accounts.values) {
            account.readyToConnect = readyToConnect
        }
    }

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)
        for (account in accounts.values) {
            account.indexerConnected = indexerConnected
        }
    }

    override fun didSetSocketConnected(socketConnected: Boolean) {
        super.didSetSocketConnected(socketConnected)
        for (account in accounts.values) {
            account.socketConnected = socketConnected
        }
    }

    override fun didSetValidatorConnected(validatorConnected: Boolean) {
        super.didSetValidatorConnected(validatorConnected)
        for (account in accounts.values) {
            account.validatorConnected = validatorConnected
        }
    }

    internal fun receiveSubaccountChannelSocketData(
        info: SocketInfo,
        payload: IMap<String, Any>,
        height: BlockAndTime?,
    ) {
        val (address, subaccountNumber) = splitAddressAndSubaccountNumber(info.id)
        val accountSupervisor = accounts[address] ?: return
        accountSupervisor.receiveSubaccountChannelSocketData(
            info,
            subaccountNumber,
            payload,
            height,
        )
    }

    private fun splitAddressAndSubaccountNumber(id: String?): Pair<String, Int> {
        if (id == null) {
            throw ParsingException(
                ParsingErrorType.UnknownChannel,
                "No subaccount channel id provided",
            )
        }
        val addressAndSubaccountNumber = id.split("/")
        if (addressAndSubaccountNumber.size != 2) {
            throw ParsingException(
                ParsingErrorType.UnknownChannel,
                "$id is not a valid subaccount channel id",
            )
        }
        val address = addressAndSubaccountNumber[0]
        val subaccountNumber =
            helper.parser.asInt(addressAndSubaccountNumber[1]) ?: throw ParsingException(
                ParsingErrorType.UnknownChannel,
                "${addressAndSubaccountNumber[1]} is not a valid subaccount number",
            )

        return Pair(address, subaccountNumber)
    }
}

// Extension properties to help with current singular account

private val AccountsSupervisor.account: AccountSupervisor?
    get() {
        return if (accounts.count() == 1) accounts.values.firstOrNull() else null
    }

internal var AccountsSupervisor.accountAddress: String?
    get() {
        return account?.accountAddress
    }
    set(value) {
        if (value != accountAddress) {
            val stateResponse = stateMachine.resetWallet(value)
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                helper.stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
            accounts.keys.filter { it != value }.forEach {
                accounts[it]?.forceRelease()
                accounts.remove(it)
            }
            if (accounts.contains(value).not() && value != null) {
                subscribeToAccount(value)
            }
        }
    }

internal var AccountsSupervisor.sourceAddress: String?
    get() {
        return account?.sourceAddress
    }
    set(value) {
        account?.sourceAddress = value
    }

internal var AccountsSupervisor.subaccountNumber: Int
    get() {
        return account?.subaccountNumber ?: 0
    }
    set(value) {
        account?.subaccountNumber = value
    }

internal val AccountsSupervisor.connectedSubaccountNumber: Int?
    get() {
        return account?.connectedSubaccountNumber
    }

internal val AccountsSupervisor.addressRestriction: UsageRestriction?
    get() {
        return account?.addressRestriction
    }

internal val AccountsSupervisor.notifications: IMap<String, Notification>
    get() {
        return account?.notifications ?: iMapOf()
    }

internal fun AccountsSupervisor.trade(data: String?, type: TradeInputField?) {
    account?.trade(data, type)
}

internal fun AccountsSupervisor.closePosition(data: String?, type: ClosePositionInputField) {
    account?.closePosition(data, type)
}

internal fun AccountsSupervisor.triggerOrders(data: String?, type: TriggerOrdersInputField?) {
    account?.triggerOrders(data, type)
}

internal fun AccountsSupervisor.placeOrderPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload? {
    return account?.placeOrderPayload(currentHeight)
}

internal fun AccountsSupervisor.closePositionPayload(currentHeight: Int?): HumanReadablePlaceOrderPayload? {
    return account?.closePositionPayload(currentHeight)
}

internal fun AccountsSupervisor.triggerOrdersPayload(currentHeight: Int?): HumanReadableTriggerOrdersPayload? {
    return account?.triggerOrdersPayload(currentHeight)
}

internal fun AccountsSupervisor.cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload? {
    return account?.cancelOrderPayload(orderId)
}

internal fun AccountsSupervisor.depositPayload(): HumanReadableDepositPayload? {
    return account?.depositPayload()
}

internal fun AccountsSupervisor.withdrawPayload(): HumanReadableWithdrawPayload? {
    return account?.withdrawPayload()
}

internal fun AccountsSupervisor.subaccountTransferPayload(): HumanReadableSubaccountTransferPayload? {
    return account?.subaccountTransferPayload()
}

internal fun AccountsSupervisor.commitPlaceOrder(
    currentHeight: Int?,
    callback: TransactionCallback
): HumanReadablePlaceOrderPayload? {
    return account?.commitPlaceOrder(currentHeight, callback)
}

internal fun AccountsSupervisor.commitTriggerOrders(
    currentHeight: Int?,
    callback: TransactionCallback
): HumanReadableTriggerOrdersPayload? {
    return account?.commitTriggerOrders(currentHeight, callback)
}

internal fun AccountsSupervisor.adjustIsolatedMargin(
    data: String?,
    type: AdjustIsolatedMarginInputField?
) {
    account?.adjustIsolatedMargin(data, type)
}

internal fun AccountsSupervisor.commitAdjustIsolatedMargin(
    callback: TransactionCallback
): HumanReadableSubaccountTransferPayload? {
    return account?.commitAdjustIsolatedMargin(callback)
}

internal fun AccountsSupervisor.adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload? {
    return account?.adjustIsolatedMarginPayload()
}

internal fun AccountsSupervisor.commitClosePosition(
    currentHeight: Int?,
    callback: TransactionCallback
): HumanReadablePlaceOrderPayload? {
    return account?.commitClosePosition(currentHeight, callback)
}

internal fun AccountsSupervisor.stopWatchingLastOrder() {
    account?.stopWatchingLastOrder()
}

internal fun AccountsSupervisor.faucet(amount: Double, callback: TransactionCallback) {
    account?.faucet(amount, callback)
}

internal fun AccountsSupervisor.cancelOrder(orderId: String, callback: TransactionCallback) {
    account?.cancelOrder(orderId, callback)
}

internal fun AccountsSupervisor.orderCanceled(orderId: String) {
    account?.orderCanceled(orderId)
}

internal fun AccountsSupervisor.refresh(data: ApiData) {
    account?.refresh(data)
}

internal fun AccountsSupervisor.screen(
    address: String,
    callback: (restriction: Restriction) -> Unit
) {
    account?.screen(address, callback)
}

internal fun AccountsSupervisor.triggerCompliance(
    action: ComplianceAction,
    callback: TransactionCallback
) {
    account?.triggerCompliance(action, callback)
}
