package exchange.dydx.abacus

import exchange.dydx.abacus.output.Documentation
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.state.machine.AdjustIsolatedMarginInputField
import exchange.dydx.abacus.state.machine.ClosePositionInputField
import exchange.dydx.abacus.state.machine.TradeInputField
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.abacus.state.machine.TriggerOrdersInputField
import exchange.dydx.abacus.state.machine.WalletConnectionType
import exchange.dydx.abacus.state.manager.ApiData
import exchange.dydx.abacus.state.manager.AppSettings
import exchange.dydx.abacus.state.manager.GasToken
import exchange.dydx.abacus.state.manager.HistoricalPnlPeriod
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.HumanReadableCancelAllOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableCloseAllPositionsPayload
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.state.manager.TransferChainInfo
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.IList
import kotlin.js.JsExport

@JsExport
interface AsyncAbacusStateManagerProtocol {

    val state: PerpetualState?

    // Connection environments
    val availableEnvironments: IList<SelectionOption>
    var environmentId: String?
    val environment: V4Environment?
    val documentation: Documentation?

    // app should set it to true when foregrounded and with network connection
    var readyToConnect: Boolean

    // account/subaccount data options
    var historicalPnlPeriod: HistoricalPnlPeriod

    // market data options
    var orderbookGrouping: OrderbookGrouping
    var historicalTradingRewardPeriod: HistoricalTradingRewardsPeriod
    var candlesResolution: String

    val appSettings: AppSettings?

    // client requested gas token
    var gasToken: GasToken?

    // input fields
    fun trade(data: String?, type: TradeInputField?)
    fun closePosition(data: String?, type: ClosePositionInputField)
    fun transfer(data: String?, type: TransferInputField?)
    fun triggerOrders(data: String?, type: TriggerOrdersInputField?)
    fun adjustIsolatedMargin(data: String?, type: AdjustIsolatedMarginInputField?)

    // helper functions
    fun isMarketValid(marketId: String?): Boolean
    fun transferStatus(
        hash: String,
        fromChainId: String?,
        toChainId: String?,
        isCctp: Boolean,
        requestId: String?
    )

    fun start()

    // Refresh some part of the state
    fun refresh(data: ApiData)

    // If FE wants to send onchain transactions without using commit... functions,
    // these functions provide payload
    fun placeOrderPayload(): HumanReadablePlaceOrderPayload?
    fun closePositionPayload(): HumanReadablePlaceOrderPayload?
    fun closeAllPositionsPayload(): HumanReadableCloseAllPositionsPayload?
    fun triggerOrdersPayload(): HumanReadableTriggerOrdersPayload?
    fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload?
    fun cancelAllOrdersPayload(marketId: String?): HumanReadableCancelAllOrdersPayload?
    fun depositPayload(): HumanReadableDepositPayload?
    fun withdrawPayload(): HumanReadableWithdrawPayload?
    fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload?
    fun adjustIsolatedMarginPayload(): HumanReadableSubaccountTransferPayload?

    // Commit changes with input objects
    fun commitPlaceOrder(callback: TransactionCallback): HumanReadablePlaceOrderPayload?
    fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload?
    fun commitTriggerOrders(callback: TransactionCallback): HumanReadableTriggerOrdersPayload?
    fun commitAdjustIsolatedMargin(callback: TransactionCallback): HumanReadableSubaccountTransferPayload?
    fun stopWatchingLastOrder()
    fun commitTransfer(callback: TransactionCallback)
    fun commitCCTPWithdraw(callback: TransactionCallback)

    // Commit changes with params
    fun faucet(amount: Double, callback: TransactionCallback)
    fun cancelOrder(orderId: String, callback: TransactionCallback)
    fun cancelAllOrders(marketId: String?, callback: TransactionCallback)
    fun closeAllPositions(callback: TransactionCallback): HumanReadableCloseAllPositionsPayload?

    // Bridge functions.
    // If client is not using cancelOrder function, it should call orderCanceled function with
    // payload from v4-client to process state
    fun orderCanceled(orderId: String)

    // Screen for restrictions
    fun screen(address: String, callback: (restriction: Restriction) -> Unit)

    // Get chain data from id. Necessary to know chain name based on chain id
    fun getChainById(chainId: String): TransferChainInfo?

    fun registerPushNotification(token: String, languageCode: String?)
    fun refreshVaultAccount()

    fun setAddresses(source: String?, account: String?, isNew: Boolean)
}

@JsExport
interface AsyncAbacusStateManagerSingletonProtocol {
    val accountAddress: String?
    val sourceAddress: String?
    var subaccountNumber: Int
    var market: String?
    var walletConnectionType: WalletConnectionType?
}

@JsExport
interface SingletonAsyncAbacusStateManagerProtocol :
    AsyncAbacusStateManagerProtocol,
    AsyncAbacusStateManagerSingletonProtocol
