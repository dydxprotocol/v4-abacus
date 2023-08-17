package exchange.dydx.abacus.state.app.adaptors

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.V3PrivateSignerProtocol
import exchange.dydx.abacus.state.app.AppStateResponse
import exchange.dydx.abacus.state.app.HistoricalPnlPeriod
import exchange.dydx.abacus.state.app.OrderbookGrouping
import exchange.dydx.abacus.state.app.V4SubaccountCancelOrderPayload
import exchange.dydx.abacus.state.app.V4SubaccountCancelOrderPayload2
import exchange.dydx.abacus.state.app.V4SubaccountPlaceOrderPayload
import exchange.dydx.abacus.state.app.V4SubaccountPlaceOrderPayload2
import exchange.dydx.abacus.state.app.helper.ChainHelper
import exchange.dydx.abacus.state.app.signer.V3ApiKey
import exchange.dydx.abacus.state.modal.ClosePositionInputField
import exchange.dydx.abacus.state.modal.TradeInputField
import exchange.dydx.abacus.state.modal.TransferInputField
import kollections.JsExport

interface ApiConfigurationsProtocol {
    fun websocketScheme(): String
    fun websocketHost(): String?
    fun websocketPath(): String?

    fun apiScheme(): String
    fun apiHost(): String?
    fun privateApiPath(type: String): String?
    fun publicApiPath(type: String): String?

    fun configsScheme(): String
    fun configsHost(): String?
    fun configsApiPath(type: String): String?
}

interface ApiAdaptorProtocol {
    val localizer: LocalizerProtocol?

    fun setReadyToConnect(readyToConnect: Boolean): AppStateResponse

    fun accountAddress(): String?
    fun subaccountNumber(): Int

    fun orderbookGrouping(): OrderbookGrouping
    fun setOrderbookGrouping(orderbookGrouping: OrderbookGrouping): AppStateResponse

    fun market(): String?
    fun setMarket(market: String?): AppStateResponse

    fun historicalPnlPeriod(): HistoricalPnlPeriod
    fun setHistoricalPnlPeriod(historicalPnlPeriod: HistoricalPnlPeriod): AppStateResponse
    fun updateHistoricalPnl(): AppStateResponse

    fun candlesResolution(): String
    fun setCandlesResolution(candlesResolution: String): AppStateResponse

    fun setSocketConnected(
        url: AbUrl,
        socketConnected: Boolean
    ): AppStateResponse

    fun processSocketResponse(
        url: AbUrl,
        text: String,
        height: Int?
    ): AppStateResponse?

    fun processHttpResponse(
        url: AbUrl,
        text: String,
        height: Int?
    ): AppStateResponse?

    fun trade(
        data: String?,
        type: TradeInputField?
    ): AppStateResponse

    fun closePosition(
        data: String?,
        type: ClosePositionInputField
    ): AppStateResponse

    fun transfer(
        data: String?,
        type: TransferInputField?
    ): AppStateResponse

    fun commit(): AppStateResponse

    fun faucet(amount: Int): AppStateResponse

    fun ping(): AppStateResponse
}

@JsExport
interface V3ApiAdaptorProtocol : ApiAdaptorProtocol {
    var signer: V3PrivateSignerProtocol?
    fun ethereumAddress(): String?
    fun setWalletEthereumAddress(
        ethereumAddress: String?,
        apiKey: V3ApiKey? = null
    ): AppStateResponse
}

@JsExport
interface V4ApiAdaptorProtocol : ApiAdaptorProtocol {
    val chainHelper: ChainHelper

    fun cosmoAddress(): String?
    fun setWalletCosmoAddress(cosmoAddress: String?): AppStateResponse

    fun setSubaccountNumber(subaccountNumber: Int
    ): AppStateResponse

    fun transaction(signedTransaction: String): AppStateResponse

    fun placeOrderPayload(): V4SubaccountPlaceOrderPayload?
    fun closePositionPayload(): V4SubaccountPlaceOrderPayload?
    fun cancelOrderPayload(orderId: String): V4SubaccountCancelOrderPayload?

    fun placeOrderPayload2(): V4SubaccountPlaceOrderPayload2?
    fun closePositionPayload2(): V4SubaccountPlaceOrderPayload2?
    fun cancelOrderPayload2(orderId: String): V4SubaccountCancelOrderPayload2?

    fun transferStatus(hash: String): AppStateResponse

    fun parseOnChainFeeTiers(payload: String): AppStateResponse
    fun parseOnChainUserFeeTier(payload: String): AppStateResponse
    fun parseOnChainUserStats(payload: String): AppStateResponse
}