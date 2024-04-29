package exchange.dydx.abacus.protocols

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.output.MarketHistoricalFunding
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.output.MarketTrade
import exchange.dydx.abacus.output.Notification
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.PerpetualMarketSummary
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.Subaccount
import exchange.dydx.abacus.output.SubaccountFill
import exchange.dydx.abacus.output.SubaccountFundingPayment
import exchange.dydx.abacus.output.SubaccountHistoricalPNL
import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.SubaccountTransfer
import exchange.dydx.abacus.output.TransferStatus
import exchange.dydx.abacus.output.Wallet
import exchange.dydx.abacus.output.input.Input
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.ApiState
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
interface V3PrivateSignerProtocol {
    fun sign(text: String, secret: String): String?
}

@JsExport
interface FormatterProtocol {
    fun percent(value: Double?, digits: Int): String?
    fun dollar(value: Double?, tickSize: String?): String?
}

@JsExport
enum class FileLocation {
    AppBundle,
    AppDocs
}

@JsExport
interface FileSystemProtocol {
    fun readTextFile(location: FileLocation, path: String): String?
    fun writeTextFile(
        path: String,
        text: String,
    ): Boolean
}

@JsExport
interface SynchronizedFileSystemProtocol {
    fun readTextFile(location: FileLocation, path: String): String?
    fun writeTextFile(
        location: FileLocation,
        path: String,
        text: String,
    ): Boolean

    fun deleteFile(location: FileLocation, path: String): Boolean
    fun itemExists(
        location: FileLocation,
        path: String,
    ): Boolean

    fun isDirectory(location: FileLocation, path: String): Boolean

    fun files(location: FileLocation, path: String, extension: String?): String?
    fun directories(location: FileLocation, path: String): String?
}

@JsExport
interface RestProtocol {

    fun get(
        url: String,
        headers: IMap<String, String>?,
        callback: RestCallback,
    )

    fun post(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: RestCallback,
    )

    fun put(
        url: String,
        headers: IMap<String, String>?,
        body: String?,
        callback: RestCallback,
    )

    fun delete(
        url: String,
        headers: IMap<String, String>?,
        callback: RestCallback,
    )
}

typealias RestCallback = (response: String?, httpCode: Int, headersAsJsonString: String?) -> Unit

@JsExport
interface WebSocketProtocol {
    fun connect(
        url: String,
        connected: ((result: Boolean) -> Unit),
        received: ((message: String) -> Unit),
    )

    fun disconnect()
    fun send(message: String)
}

@JsExport
@Serializable
enum class HistoricalPnlPeriod(val rawValue: String) {
    Period1d("1d"),
    Period7d("7d"),
    Period30d("30d"),
    Period90d("90d");

    companion object {
        operator fun invoke(rawValue: String) =
            HistoricalPnlPeriod.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
enum class QueryType(val rawValue: String) {
    Height("getHeight"),
    EquityTiers("getEquityTiers"),
    FeeTiers("getFeeTiers"),
    UserFeeTier("getUserFeeTier"),
    UserStats("getUserStats"),
    OptimalNode("getOptimalNode"),
    OptimalIndexer("getOptimalIndexer"),
    GetAccountBalances("getAccountBalances"),
    GetMarketPrice("getMarketPrice"),
    GetDelegations("getDelegatorDelegations"),
    RewardsParams("getRewardsParams"),
    GetNobleBalance("getNobleBalance"),
    GetWithdrawalAndTransferGatingStatus("getWithdrawalAndTransferGatingStatus"),
    GetWithdrawalCapacityByDenom("getWithdrawalCapacityByDenom");

    companion object {
        operator fun invoke(rawValue: String) =
            QueryType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
enum class TransactionType(val rawValue: String) {
    PlaceOrder("placeOrder"),
    CancelOrder("cancelOrder"),
    Deposit("deposit"),
    Withdraw("withdraw"),
    SubaccountTransfer("subaccountTransfer"),
    Faucet("faucet"),
    simulateWithdraw("simulateWithdraw"),
    simulateTransferNativeToken("simulateTransferNativeToken"),
    SendNobleIBC("sendNobleIBC"),
    WithdrawToNobleIBC("withdrawToNobleIBC"),
    CctpWithdraw("cctpWithdraw"),
    SignCompliancePayload("signCompliancePayload");

    companion object {
        operator fun invoke(rawValue: String) =
            TransactionType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
interface DYDXChainTransactionsProtocol {
    fun connectNetwork(
        paramsInJson: String,
        /*
        indexerUrl: String,
        indexerSocketUrl: String,
        validatorUrl: String,
        chainId: String,
        faucetUrl: String? = null,
        USDC_DENOM: String? = null,
        USDC_DENOM_EXPONENT: Int? = null,
        USDC_GAS_DENOM: String? = null,
        CHAINTOKEN_DENOM: String? = null,
        CHAINTOKEN_DENOM_EXPONENT: String? = null,
         */
        callback: ((response: String?) -> Unit),
    )

    fun get(type: QueryType, paramsInJson: String?, callback: ((response: String?) -> Unit))

    fun transaction(
        type: TransactionType,
        paramsInJson: String?,
        callback: ((response: String?) -> Unit),
    )
}

@JsExport
enum class AnalyticsEvent(val rawValue: String) {
    // App
    NetworkStatus("NetworkStatus"),

    // Trade Events
    TradePlaceOrderClick("TradePlaceOrderClick"),
    TradeCancelOrderClick("TradeCancelOrderClick"),
    TradeTriggerOrderClick("TradeTriggerOrderClick"),
    TradePlaceOrder("TradePlaceOrder"), // analytics here
    TradeCancelOrder("TradeCancelOrder"), // analytics here
    TradeTriggerPlaceOrder("TradeTriggerPlaceOrder"),
    TradeTriggerCancelOrder("TradeTriggerCancelOrder"),
    TradePlaceOrderSubmissionConfirmed("TradePlaceOrderSubmissionConfirmed"),
    TradeCancelOrderSubmissionConfirmed("TradeCancelOrderSubmissionConfirmed"),
    TradeTriggerPlaceOrderSubmissionConfirmed("TradeTriggerPlaceOrderSubmissionConfirmed"),
    TradeTriggerCancelOrderSubmissionConfirmed("TradeTriggerCancelOrderSubmissionConfirmed"),
    TradePlaceOrderSubmissionFailed("TradePlaceOrderSubmissionFailed"),
    TradeCancelOrderSubmissionFailed("TradeCancelOrderSubmissionFailed"),
    TradeTriggerPlaceOrderSubmissionFailed("TradeTriggerPlaceOrderSubmissionFailed"),
    TradeTriggerCancelOrderSubmissionFailed("TradeTriggerCancelOrderSubmissionFailed"),
    TradeCancelOrderConfirmed("TradeCancelOrderConfirmed"),
    TradePlaceOrderConfirmed("TradePlaceOrderConfirmed"),

    // Transfers
    TransferFaucetConfirmed("TransferFaucetConfirmed");

    companion object {
        operator fun invoke(rawValue: String) =
            AnalyticsEvent.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
interface TrackingProtocol {
    fun log(event: String, data: String?)
}

@JsExport
interface StateNotificationProtocol {
    fun environmentsChanged()
    fun stateChanged(state: PerpetualState?, changes: StateChanges?)
    fun apiStateChanged(apiState: ApiState?)
    fun errorsEmitted(errors: IList<ParsingError>)
    fun lastOrderChanged(order: SubaccountOrder?)

    fun notificationsChanged(notifications: IList<Notification>)
}

@JsExport
interface DataNotificationProtocol {
    fun environmentsChanged()
    fun marketsSummaryChanged(marketsSummary: PerpetualMarketSummary?)
    fun assetChanged(asset: Asset?, assetId: String)
    fun marketChanged(market: PerpetualMarket?, marketId: String)
    fun marketOrderbookChanged(orderbook: MarketOrderbook?, marketId: String)
    fun marketTradesChanged(trades: IList<MarketTrade>?, marketId: String)
    fun marketCandlesChanged(candles: IList<MarketCandle>?, marketId: String, resolution: String)
    fun marketHistoricalFundingChanged(funding: IList<MarketHistoricalFunding>?, marketId: String)
    fun marketSparklinesChanged(sparklines: IList<Double>?, marketId: String)
    fun walletChanged(wallet: Wallet?)
    fun subaccountChanged(subaccount: Subaccount?, subaccountNumber: Int)
    fun subaccountHistoricalPnlChanged(pnl: IList<SubaccountHistoricalPNL>?, subaccountNumber: Int)
    fun subaccountFillsChanged(fills: IList<SubaccountFill>?, subaccountNumber: Int)
    fun subaccountTransfersChanged(transfers: IList<SubaccountTransfer>?, subaccountNumber: Int)
    fun subaccountFundingPaymentsChanged(
        payments: IList<SubaccountFundingPayment>?,
        subaccountNumber: Int,
    )

    fun transferStatusChanged(statuses: TransferStatus?, hash: String)
    fun inputChanged(input: Input?)
    fun feeTiersChanged(feeTiers: IList<FeeTier>?)

    fun apiStateChanged(apiState: ApiState?)

    fun errorsEmitted(errors: IList<ParsingError>)
    fun lastOrderChanged(order: SubaccountOrder?)

    fun notificationsChanged(notifications: IList<Notification>)
}

@JsExport
enum class ThreadingType {
    main,
    abacus,
    network
}

@JsExport
interface ThreadingProtocol {
    fun async(type: ThreadingType, block: (() -> Unit))
}

@JsExport
interface LocalTimerProtocol {
    fun cancel()
}

@JsExport
interface TimerProtocol {
    fun schedule(delay: Double, repeat: Double?, block: (() -> Boolean)): LocalTimerProtocol
}

@JsExport
fun TimerProtocol.run(after: Double, block: (() -> Unit)): LocalTimerProtocol {
    return this.schedule(after, null) {
        block()
        false
    }
}

@JsExport
fun FileSystemProtocol.readCachedTextFile(
    path: String,
): String? {
    var data = this.readTextFile(FileLocation.AppDocs, path)
    return if (data == null) {
        data = this.readTextFile(FileLocation.AppBundle, path)
        if (data != null) {
            this.writeTextFile(path, data)
        }
        data
    } else {
        data
    }
}

typealias TransactionCallback = (successful: Boolean, error: ParsingError?, data: Any?) -> Unit
