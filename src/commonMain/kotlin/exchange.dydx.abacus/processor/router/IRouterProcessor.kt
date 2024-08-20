package exchange.dydx.abacus.processor.router

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputTokenResource

enum class ChainType(val rawValue: String) {
    EVM("evm"),
    COSMOS("cosmos"),
    SVM("svm"),
}

interface IRouterProcessor {
    var tokens: List<Any>?
    var chains: List<Any>?
    var exchangeDestinationChainId: String?
    var selectedChainType: ChainType?

    fun receivedEvmSwapVenues(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    )

    fun receivedChains(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>?

    fun receivedTokens(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>?

    fun receivedV2SdkInfo(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>?

    fun receivedRoute(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        requestId: String?,
    ): Map<String, Any>?

    fun receivedRouteV2(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        requestId: String?
    ): Map<String, Any>?

    fun usdcAmount(data: Map<String, Any>): Double?
    fun receivedStatus(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        transactionId: String?,
    ): Map<String, Any>?

    fun getChainById(
        chainId: String
    ): Map<String, Any>?

    fun receivedTrack(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any>?

    fun getTokenByDenomAndChainId(tokenDenom: String?, chainId: String?): Map<String, Any>?
    fun updateTokensDefaults(modified: MutableMap<String, Any>, selectedChainId: String?)
    fun defaultChainId(): String?
    fun selectedTokenSymbol(tokenAddress: String?, selectedChainId: String?): String?
    fun selectedTokenDecimals(tokenAddress: String?, selectedChainId: String?): String?
    fun filteredTokens(chainId: String?): List<Any>?
    fun defaultTokenAddress(chainId: String?): String?
    fun chainResources(chainId: String?): Map<String, TransferInputChainResource>?
    fun tokenResources(chainId: String?): Map<String, TransferInputTokenResource>?
    fun chainOptions(): List<Any>
    fun tokenOptions(chainId: String?): List<SelectionOption>
}
