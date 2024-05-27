package exchange.dydx.abacus.processor.router

interface IRouterProcessor {
    var tokens: List<Any>?
    var chains: List<Any>?
    var exchangeDestinationChainId: String?
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

    fun updateTokensDefaults(modified: MutableMap<String, Any>, selectedChainId: String?)
    fun defaultChainId(): String?
    fun selectedTokenSymbol(tokenAddress: String?): String?
    fun selectedTokenDecimals(tokenAddress: String?, selectedChainId: String?): String?
    fun filteredTokens(chainId: String?): List<Any>?
    fun defaultTokenAddress(chainId: String?): String?
    fun chainResources(chainId: String?): Map<String, Any>?
    fun tokenResources(chainId: String?): Map<String, Any>?
    fun chainOptions(): List<Any>
    fun tokenOptions(chainId: String?): List<Any>
}