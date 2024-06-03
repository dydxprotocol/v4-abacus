package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputTokenResource
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.router.IRouterProcessor
import exchange.dydx.abacus.processor.router.SharedRouterProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.state.manager.CctpConfig.cctpChainIds
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("NotImplementedDeclaration")
internal class SkipProcessor(
    parser: ParserProtocol,
    private val internalState: InternalTransferInputState
) : BaseProcessor(parser), IRouterProcessor {
    override var chains: List<Any>? = null

//    possibly want to use a different variable so we aren't stuck with this bad type
//    actual type of the tokens payload is Map<str, Map<str, List<Map<str, Any>>>>
    override var tokens: List<Any>? = null

    var skipTokens: Map<String, Map<String, List<Map<String, Any>>>>? = null
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

        internalState.chains = chainOptions
        val selectedChainId = defaultChainId()
        modified.safeSet("transfer.chain", selectedChainId)
        selectedChainId?.let {
            internalState.chainResources = chainResources(chainId = selectedChainId)
        }
        return modified
    }

    override fun receivedTokens(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        if (this.chains != null && this.skipTokens != null) {
            return existing
        }

        val chainToAssetsMap = payload["chain_to_assets_map"] as Map<String, Map<String, List<Map<String, Any>>>>?

        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }
        if (chainToAssetsMap == null) {
            return existing
        }
        val selectedChainId = defaultChainId()
        this.skipTokens = chainToAssetsMap
        updateTokensDefaults(modified, selectedChainId)

        return modified
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
        internalState.tokens = tokenOptions
        modified.safeSet("transfer.token", defaultTokenAddress(selectedChainId))
        internalState.tokenResources = tokenResources(selectedChainId)
    }

    override fun defaultChainId(): String? {
        val selectedChain = parser.asNativeMap(this.chains?.find { parser.asString(parser.asNativeMap(it)?.get("chain_id")) == "1" })

        return parser.asString(selectedChain?.get("chain_id"))
    }

    override fun selectedTokenSymbol(tokenAddress: String?, selectedChainId: String?): String? {
        val tokensList = filteredTokens(selectedChainId)
        tokensList?.find {
            parser.asString(parser.asNativeMap(it)?.get("denom")) == tokenAddress
        }?.let {
            return parser.asString(parser.asNativeMap(it)?.get("symbol"))
        }
        return null
    }

    override fun selectedTokenDecimals(tokenAddress: String?, selectedChainId: String?): String? {
        val tokensList = filteredTokens(selectedChainId)
        tokensList?.find {
            parser.asString(parser.asNativeMap(it)?.get("denom")) == tokenAddress
        }?.let {
            return parser.asString(parser.asNativeMap(it)?.get("decimals"))
        }
        return null
    }

    override fun filteredTokens(chainId: String?): List<Any>? {
        val chainIdToUse = chainId ?: defaultChainId()
        val assetsMapForChainId = parser.asNativeMap(this.skipTokens?.get(chainIdToUse))
        return parser.asNativeList(assetsMapForChainId?.get("assets"))
    }

    override fun defaultTokenAddress(chainId: String?): String? {
        return chainId?.let { cid ->
            // Retrieve the list of filtered tokens for the given chainId
            val filteredTokens = this.filteredTokens(cid)?.mapNotNull {
                parser.asString(parser.asNativeMap(it)?.get("denom"))
            }.orEmpty()
            // Find a matching CctpChainTokenInfo and check if its tokenAddress is in the filtered tokens
            cctpChainIds?.firstOrNull { it.chainId == cid && filteredTokens.contains(it.tokenAddress) }?.tokenAddress
                ?: run {
                    // Fallback to the first token's address from the filtered list if no CctpChainTokenInfo match is found
                    filteredTokens.firstOrNull()
                }
        }
    }

    override fun chainResources(chainId: String?): Map<String, TransferInputChainResource>? {
        val chainResources = mutableMapOf<String, TransferInputChainResource>()
        chainId?.let {
            this.chains?.find {
                parser.asString(parser.asNativeMap(it)?.get("chain_id")) == chainId
            }?.let {
                val processor = SkipChainResourceProcessor(parser)
                parser.asNativeMap(it)?.let { payload ->
                    chainResources[chainId] = processor.received(payload)
                }
            }
        }
        return chainResources
    }

    override fun tokenResources(chainId: String?): Map<String, TransferInputTokenResource>? {
        val tokenResources = mutableMapOf<String, TransferInputTokenResource>()
        filteredTokens(chainId)?.forEach {
            parser.asString(parser.asNativeMap(it)?.get("denom"))?.let { key ->
                val processor = SkipTokenResourceProcessor(parser)
                parser.asNativeMap(it)?.let { payload ->
                    tokenResources[key] = processor.received(payload)
                }
            }
        }
        return tokenResources
    }

    override fun chainOptions(): List<SelectionOption> {
        val chainProcessor = SkipChainProcessor(parser)
        val options = mutableListOf<SelectionOption>()

        this.chains?.let {
            for (chain in it) {
                parser.asNativeMap(chain)?.let { chain ->
                    if (parser.asString(chain.get("chainType")) != "cosmos") {
                        options.add(chainProcessor.received(chain))
                    }
                }
            }
        }

        options.sortBy { parser.asString(it.stringKey) }
        return options
    }

    override fun tokenOptions(chainId: String?): List<SelectionOption> {
        val processor = SkipTokenProcessor(parser)
        val options = mutableListOf<SelectionOption>()
        val tokensForSelectedChain = filteredTokens(chainId)
        tokensForSelectedChain?.let {
            for (asset in it) {
                parser.asNativeMap(asset)?.let { _asset ->
                    options.add(processor.received(_asset))
                }
            }
        }
        options.sortBy { parser.asString(it.stringKey) }
        return options
    }
}
