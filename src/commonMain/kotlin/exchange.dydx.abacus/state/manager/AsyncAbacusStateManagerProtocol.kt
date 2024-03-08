package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.Documentation
import exchange.dydx.abacus.output.Restriction
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.manager.utils.ApiData
import exchange.dydx.abacus.state.manager.utils.HistoricalPnlPeriod
import exchange.dydx.abacus.state.manager.utils.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.state.manager.utils.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.utils.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.utils.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.utils.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.utils.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.utils.OrderbookGrouping
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.state.model.TransferInputField

interface AsyncAbacusStateManagerProtocol {
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

    // input fields
    fun trade(data: String?, type: TradeInputField?)
    fun closePosition(data: String?, type: ClosePositionInputField)
    fun transfer(data: String?, type: TransferInputField?)

    // helper functions
    fun isMarketValid(marketId: String?): Boolean
    fun transferStatus(hash: String, fromChainId: String?, toChainId: String?, isCctp: Boolean)

    // Refresh some part of the state
    fun refresh(data: ApiData)

    // If FE wants to send onchain transactions without using commit... functions,
    // these functions provide payload
    fun placeOrderPayload(): HumanReadablePlaceOrderPayload?
    fun closePositionPayload(): HumanReadablePlaceOrderPayload?
    fun cancelOrderPayload(orderId: String): HumanReadableCancelOrderPayload?
    fun depositPayload(): HumanReadableDepositPayload?
    fun withdrawPayload(): HumanReadableWithdrawPayload?
    fun subaccountTransferPayload(): HumanReadableSubaccountTransferPayload?

    // Commit changes with input objects
    fun commitPlaceOrder(callback: TransactionCallback): HumanReadablePlaceOrderPayload?
    fun commitClosePosition(callback: TransactionCallback): HumanReadablePlaceOrderPayload?
    fun stopWatchingLastOrder()
    fun commitTransfer(callback: TransactionCallback)
    fun commitCCTPWithdraw(callback: TransactionCallback)

    // Commit changes with params
    fun faucet(amount: Double, callback: TransactionCallback)
    fun cancelOrder(orderId: String, callback: TransactionCallback)

    // Bridge functions.
    // If client is not using cancelOrder function, it should call orderCanceled function with
    // payload from v4-client to process state
    fun orderCanceled(orderId: String)

    // Screen for restrictions
    fun screen(address: String, callback: (restriction: Restriction) -> Unit)
}


interface AsyncAbacusStateManagerSingletonProtocol {
    var accountAddress: String?
    var sourceAddress: String?
    var subaccountNumber: Int
    var market: String?
}

internal fun AsyncAbacusStateManagerSingletonProtocol.setAddresses(source: String?, account: String?) {
    accountAddress = account
    sourceAddress = source
}
