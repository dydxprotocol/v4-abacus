package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputTokenResource
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.router.IRouterProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.state.manager.CctpConfig.cctpChainIds
import exchange.dydx.abacus.utils.ALLOWED_CHAIN_TYPES
import exchange.dydx.abacus.utils.ETHEREUM_CHAIN_ID
import exchange.dydx.abacus.utils.NATIVE_TOKEN_DEFAULT_ADDRESS
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("NotImplementedDeclaration", "ForbiddenComment")
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
//        We diff based on map values in order to determine whether to return new state
//        Until we diff on `internalState` changes we need to update old map state as well
        modified.safeSet("transfer.depositOptions.chains", chainOptions)
        modified.safeSet("transfer.withdrawalOptions.chains", chainOptions)
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
        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }
        val tokenAddress = parser.asString(parser.value(payload, "route.dest_asset_denom"))
        val selectedChainId = parser.asString(parser.value(payload, "route.dest_asset_chain_id"))
        val decimals = parser.asDouble(selectedTokenDecimals(tokenAddress = tokenAddress, selectedChainId = selectedChainId))
        val processor = SkipRouteProcessor(parser)

        modified.safeSet(
            "transfer.route",
            processor.received(null, payload, decimals = decimals) as MutableMap<String, Any>,
        )
        if (requestId != null) {
            modified.safeSet("transfer.route.requestPayload.requestId", requestId)
        }
        if (parser.asNativeMap(existing?.get("transfer"))?.get("type") == "DEPOSIT") {
            val value = usdcAmount(modified)
            modified.safeSet("transfer.size.usdcSize", value)
        }
        return modified
    }

    override fun receivedRouteV2(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        requestId: String?
    ): Map<String, Any>? {
        return receivedRoute(existing, payload, requestId)
    }

    override fun usdcAmount(data: Map<String, Any>): Double? {
        var toAmountUSD = parser.asString(parser.value(data, "transfer.route.toAmountUSD"))
        toAmountUSD = toAmountUSD?.replace(",", "")
        var toAmount = parser.asString(parser.value(data, "transfer.route.toAmount"))
        toAmount = toAmount?.replace(",", "")
        return parser.asDouble(toAmountUSD) ?: parser.asDouble(toAmount)
    }

    override fun receivedStatus(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        transactionId: String?,
    ): Map<String, Any>? {
        val processor = SkipStatusProcessor(parser, transactionId)
        return processor.received(existing, payload)
    }

    override fun receivedTrack(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any>? {
        val processor = SkipTrackProcessor(parser)
        return processor.received(existing, payload)
    }

    override fun updateTokensDefaults(modified: MutableMap<String, Any>, selectedChainId: String?) {
        val tokenOptions = tokenOptions(selectedChainId)
        internalState.tokens = tokenOptions
        modified.safeSet("transfer.token", defaultTokenAddress(selectedChainId))
        modified.safeSet("transfer.depositOptions.tokens", tokenOptions)
        modified.safeSet("transfer.withdrawalOptions.tokens", tokenOptions)
        internalState.tokenResources = tokenResources(selectedChainId)
    }

//    Update this when we move chains to state
//    https://linear.app/dydx/issue/OTE-470/migrate-chains-and-tokens-to-abacus-state
    override fun getChainById(chainId: String): Map<String, Any>? {
        return parser.asNativeMap(this.chains?.find { parser.asString(parser.asNativeMap(it)?.get("chain_id")) == chainId })
    }

    override fun defaultChainId(): String? {
        val selectedChain = getChainById(chainId = ETHEREUM_CHAIN_ID) ?: parser.asNativeMap(this.chains?.firstOrNull())

        return parser.asString(selectedChain?.get("chain_id"))
    }

    override fun getTokenByDenomAndChainId(tokenDenom: String?, chainId: String?): Map<String, Any>? {
        val tokensList = filteredTokens(chainId)
        tokensList?.find {
            parser.asString(parser.asNativeMap(it)?.get("denom")) == (tokenDenom ?: NATIVE_TOKEN_DEFAULT_ADDRESS)
        }?.let {
            return parser.asNativeMap(it)
        }
        return null
    }

    override fun selectedTokenSymbol(tokenAddress: String?, selectedChainId: String?): String? {
        val token = getTokenByDenomAndChainId(tokenAddress, selectedChainId) ?: return null
        return parser.asString(token.get("symbol"))
    }

    override fun selectedTokenDecimals(tokenAddress: String?, selectedChainId: String?): String? {
        val tokensList = filteredTokens(selectedChainId)
        tokensList?.find {
            (parser.asString(parser.asNativeMap(it)?.get("denom")) == tokenAddress || parser.asString(parser.asNativeMap(it)?.get("skipDenom")) == tokenAddress)
        }?.let {
            return parser.asString(parser.asNativeMap(it)?.get("decimals"))
        }
        return null
    }

    override fun filteredTokens(chainId: String?): List<Any>? {
        val chainIdToUse = chainId ?: defaultChainId()
        val assetsMapForChainId = parser.asNativeMap(this.skipTokens?.get(chainIdToUse))
        val assetsForChainId = parser.asNativeList(assetsMapForChainId?.get("assets"))
//      coinbase exchange chainId is noble-1. we only allow usdc withdrawals from it
        if (chainId === exchangeDestinationChainId) {
            return assetsForChainId?.filter {
                parser.asString(parser.asNativeMap(it)?.get("denom")) == "uusdc"
            }
        }

        val filteredTokens = mutableListOf<Map<String, Any>>()
//        we have to replace skip's {chain-name}-native naming bc it doesn't play well with
//        any of our SDKs.
//        however, their {chain-name}-native denom naming is required for their API
//        so we need to store both values
        assetsForChainId?.forEach {
            val token = parser.asNativeMap(it)?.toMutableMap()
            if (token != null) {
                val denom = parser.asString(token["denom"])
                if (denom?.endsWith("native") == true) {
                    token["skipDenom"] = denom
                    token["denom"] = NATIVE_TOKEN_DEFAULT_ADDRESS
                }
                filteredTokens.add(token.toMap())
            }
        }
        return filteredTokens
    }

    override fun defaultTokenAddress(chainId: String?): String? {
        if (chainId == null) {
            return null
        }
        val filteredTokensForChainId = filteredTokens(chainId) ?: return null

        // Find if any CctpChainTokenInfo item belongs to the provided chainId
        val cctpChain = cctpChainIds?.find { it.chainId == chainId }
//        If one does, then check for a token in the filteredTokens whose denom matches the found cctpChainTokenInfo, if one exists
        val filteredCctpToken = filteredTokensForChainId.find {
            parser.asString(parser.asNativeMap(it)?.get("denom")) == cctpChain?.tokenAddress
        }
        if (filteredCctpToken != null) {
            return parser.asString(parser.asNativeMap(filteredCctpToken)?.get("denom"))
        }
//        If no cctp item available, check for a native token item
        val nativeChain = filteredTokensForChainId?.find {
            parser.asString(parser.asNativeMap(it)?.get("denom")) == NATIVE_TOKEN_DEFAULT_ADDRESS
        }
        if (nativeChain != null) {
            return parser.asString(parser.asNativeMap(nativeChain)?.get("denom"))
        }
//        Otherwise, just grab the first available token if any exist
        val firstTokenOrNull = parser.asNativeMap(filteredTokensForChainId?.firstOrNull())
        if (firstTokenOrNull != null) {
            return parser.asString(parser.asNativeMap(firstTokenOrNull)?.get("denom"))
        }
        return null
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
                    if (parser.asString(chain.get("chain_type")) in ALLOWED_CHAIN_TYPES) {
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

        val sortedOptions = options.sortedBy { if (it.type === NATIVE_TOKEN_DEFAULT_ADDRESS) 0 else 1 }
        return sortedOptions
    }
}
