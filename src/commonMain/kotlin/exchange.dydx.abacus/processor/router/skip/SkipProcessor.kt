package exchange.dydx.abacus.processor.router.Squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.router.*
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.safeSet

internal class SkipProcessor(parser: ParserProtocol) : BaseProcessor(parser), IRouterProcessor {
    override var chains: List<Any>? = null
//    possibly want to use a different variable so we aren't stuck with this bad type
//    actual type of the tokens payload is Map<str, Map<str, List<Map<str, Any>>>>
    override var tokens: List<Any>? = null
    override var exchangeDestinationChainId: String? = null
    val sharedRouterProcessor = SharedRouterProcessor(parser)

    override fun receivedV2SdkInfo(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        throw Error("Not Implemented!")
    }
    override fun receivedChains(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        throw Error("Not Implemented!")
    }
    override fun receivedTokens(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        throw Error("Not Implemented!")
    }

    override fun receivedRoute(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        requestId: String?,
    ): Map<String, Any>? {
        throw Error("Not Implemented!")
    }

    override fun receivedRouteV2(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        requestId: String?
    ): Map<String, Any>? {
        throw Error("Not Implemented!")
    }

    override fun usdcAmount(data: Map<String, Any>): Double? {
        throw Error("Not Implemented!")
    }

    override fun receivedStatus(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        transactionId: String?,
    ): Map<String, Any>? {
        throw Error("Not Implemented!")
    }

    override fun updateTokensDefaults(modified: MutableMap<String, Any>, selectedChainId: String?) {
        val tokenOptions = tokenOptions(selectedChainId)
        modified.safeSet("transfer.depositOptions.assets", tokenOptions)
        modified.safeSet("transfer.withdrawalOptions.assets", tokenOptions)
        modified.safeSet("transfer.token", defaultTokenAddress(selectedChainId))
        modified.safeSet("transfer.resources.tokenResources", tokenResources(selectedChainId))
    }

    override fun defaultChainId(): String? {
        throw Error("Not Implemented!")
    }

    override fun selectedTokenSymbol(tokenAddress: String?): String? {
        throw Error("Not Implemented!")
    }

    override fun selectedTokenDecimals(tokenAddress: String?): String? {
        throw Error("Not Implemented!")
    }

    override fun filteredTokens(chainId: String?): List<Any>? {
        throw Error("Not Implemented!")
    }

    override fun defaultTokenAddress(chainId: String?): String? {
        throw Error("Not Implemented!")
    }

    override fun chainResources(chainId: String?): Map<String, Any>? {
        throw Error("Not Implemented!")
    }

    override fun tokenResources(chainId: String?): Map<String, Any>? {
        throw Error("Not Implemented!")
    }

    override fun chainOptions(): List<Any> {
        throw Error("Not Implemented!")
    }

    override fun tokenOptions(chainId: String?): List<Any> {
        throw Error("Not Implemented!")
    }
}
