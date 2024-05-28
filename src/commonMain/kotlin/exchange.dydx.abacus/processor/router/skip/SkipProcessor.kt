package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.router.IRouterProcessor
import exchange.dydx.abacus.processor.router.SharedRouterProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("NotImplementedDeclaration")
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
        throw NotImplementedError("receivedV2SdkInfo is not implemented in SkipProcessor!")
    }

    override fun receivedChains(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        if (this.chains != null) {
            return existing
        }
        this.chains = parser.asNativeList(payload["chains"])
        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }
        val chainOptions = chainOptions()

        modified.safeSet("transfer.depositOptions.chains", chainOptions)
        modified.safeSet("transfer.withdrawalOptions.chains", chainOptions)
        val selectedChainId = defaultChainId()
        modified.safeSet("transfer.chain", selectedChainId)
        selectedChainId?.let {
            modified.safeSet("transfer.resources.chainResources", chainResources(selectedChainId))
        }
        return modified
    }

    override fun receivedTokens(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        throw NotImplementedError("receivedTokens is not implemented in SkipProcessor!")
    }

    override fun receivedRoute(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        requestId: String?,
    ): Map<String, Any>? {
        throw NotImplementedError("receivedRoute is not implemented in SkipProcessor!")
    }

    override fun receivedRouteV2(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        requestId: String?
    ): Map<String, Any>? {
        throw NotImplementedError("receivedRouteV2 is not implemented in SkipProcessor!")
    }

    override fun usdcAmount(data: Map<String, Any>): Double? {
        throw NotImplementedError("usdcAmount is not implemented in SkipProcessor!")
    }

    override fun receivedStatus(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        transactionId: String?,
    ): Map<String, Any>? {
        throw NotImplementedError("receivedStatus is not implemented in SkipProcessor!")
    }

    override fun updateTokensDefaults(modified: MutableMap<String, Any>, selectedChainId: String?) {
        val tokenOptions = tokenOptions(selectedChainId)
        modified.safeSet("transfer.depositOptions.assets", tokenOptions)
        modified.safeSet("transfer.withdrawalOptions.assets", tokenOptions)
        modified.safeSet("transfer.token", defaultTokenAddress(selectedChainId))
        modified.safeSet("transfer.resources.tokenResources", tokenResources(selectedChainId))
    }

    override fun defaultChainId(): String? {
        val selectedChain = parser.asNativeMap(this.chains?.find{ parser.asString(parser.asNativeMap(it)?.get("chain_id")) === "1" })

        return parser.asString(selectedChain?.get("chain_id"))
    }

    override fun selectedTokenSymbol(tokenAddress: String?): String? {
        throw NotImplementedError("selectedTokenSymbol is not implemented in SkipProcessor!")
    }

    override fun selectedTokenDecimals(tokenAddress: String?): String? {
        throw NotImplementedError("selectedTokenDecimals is not implemented in SkipProcessor!")
    }

    override fun filteredTokens(chainId: String?): List<Any>? {
        throw NotImplementedError("filteredTokens is not implemented in SkipProcessor!")
    }

    override fun defaultTokenAddress(chainId: String?): String? {
        throw NotImplementedError("defaultTokenAddress is not implemented in SkipProcessor!")
    }

    override fun chainResources(chainId: String?): Map<String, Any>? {
        val chainResources = mutableMapOf<String, Any>()
        chainId?.let {
            this.chains?.find {
                parser.asString(parser.asNativeMap(it)?.get("chainId")) == chainId
            }?.let {
                val processor = SkipChainResourceProcessor(parser)
                parser.asNativeMap(it)?.let { payload ->
                    chainResources[chainId] = processor.received(null, payload)
                }
            }
        }
        return chainResources
    }

    override fun tokenResources(chainId: String?): Map<String, Any>? {
        throw NotImplementedError("tokenResources is not implemented in SkipProcessor!")
    }

    override fun chainOptions(): List<Any> {
        val chainProcessor = SkipChainProcessor(parser)
        val options = mutableListOf<Any>()

        this.chains?.let { it ->
            for (chain in it) {
                parser.asNativeMap(chain)?.let { chain ->
                    if (parser.asString(chain.get("chainType")) != "cosmos") {
                        options.add(chainProcessor.received(null, chain))
                    }
                }
            }
        }

        options.sortBy { parser.asString(parser.asNativeMap(it)?.get("stringKey")) }
        return options
    }

    override fun tokenOptions(chainId: String?): List<Any> {
        throw NotImplementedError("tokenOptions is not implemented in SkipProcessor!")
    }
}
