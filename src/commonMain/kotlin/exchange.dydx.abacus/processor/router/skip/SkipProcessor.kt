package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputSize
import exchange.dydx.abacus.output.input.TransferInputTokenResource
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.router.ChainType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalTransferInputState
import exchange.dydx.abacus.state.InternalTransferVenueState
import exchange.dydx.abacus.state.manager.CctpConfig.cctpChainIds
import exchange.dydx.abacus.state.safeCreate
import exchange.dydx.abacus.utils.ETHEREUM_CHAIN_ID
import exchange.dydx.abacus.utils.NATIVE_TOKEN_DEFAULT_ADDRESS
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class SkipProcessor(
    parser: ParserProtocol,
    private val internalState: InternalTransferInputState,
) : BaseProcessor(parser) {
    var chains: List<Any>? = null

    var skipTokens: Map<String, Map<String, List<Map<String, Any>>>>? = null
    var exchangeDestinationChainId: String? = null
    var selectedChainType: ChainType? = ChainType.EVM
        set(value) {
            if (field != value) {
                field = value
                internalState.chains = chainOptions()
            }
        }

    fun receivedChains(
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
        internalState.chain = selectedChainId
        selectedChainId?.let {
            internalState.chainResources = chainResources(chainId = selectedChainId)
        }
        return modified
    }

    fun receivedEvmSwapVenues(
        payload: Map<String, Any>
    ) {
        val venues = parser.asNativeList(payload.get("venues"))
        val swapVenues = venues?.map {
            val swapVenue = parser.asMap(it)
            InternalTransferVenueState(
                name = parser.asString(swapVenue?.get("name")),
                chain_id = parser.asString(swapVenue?.get("chain_id")),
            )
        }
        if (swapVenues != null) {
            this.internalState.swapVenues = swapVenues
        }
    }

    fun receivedTokens(
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

    fun receivedRoute(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        requestId: String?,
        goFast: Boolean,
    ): Map<String, Any>? {
        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }
        val tokenAddress = parser.asString(parser.value(payload, "route.dest_asset_denom"))
        val selectedChainId = parser.asString(parser.value(payload, "route.dest_asset_chain_id"))
        val decimals = parser.asDouble(
            selectedTokenDecimals(
                tokenAddress = tokenAddress,
                selectedChainId = selectedChainId,
            ),
        )
        val route =
            SkipRouteProcessor(parser).received(null, payload, decimals = decimals).toMutableMap()
        if (requestId != null) {
            route.safeSet("requestPayload.requestId", requestId)
        }
        modified.safeSet("transfer.route", route)
        if (goFast) {
            val operations = parser.asList(parser.value(payload, "route.operations"))
            var goFastFound = false
            for (operation in operations ?: emptyList()) {
                val transaction = parser.asNativeMap(operation)
                if (transaction?.keys?.contains("go_fast_transfer") == true) {
                    goFastFound = true
                    break
                }
            }
            if (goFastFound) {
                internalState.goFastRoute = route
            } else {
                internalState.goFastRoute = null
            }
        } else {
            internalState.route = route
        }

        if (internalState.type == TransferType.deposit) {
            val value = usdcAmount()
            internalState.size = TransferInputSize.safeCreate(internalState.size)
                .copy(usdcSize = parser.asString(value))
        }

        return modified
    }

    fun usdcAmount(): Double? {
        val route = internalState.route
        val toAmountUSD = parser.asString(parser.value(route, "toAmountUSD"))
        val toAmount = parser.asString(parser.value(route, "toAmount"))
        return parser.asDouble(toAmountUSD) ?: parser.asDouble(toAmount)
    }

    fun receivedStatus(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        transactionId: String?,
    ): Map<String, Any>? {
        val processor = SkipStatusProcessor(parser, transactionId)
        return processor.received(existing, payload)
    }

    fun receivedTrack(
        hash: String,
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any>? {
        val processor = SkipTrackProcessor(hash, parser)
        return processor.received(existing, payload)
    }

    fun updateTokensDefaults(modified: MutableMap<String, Any>, selectedChainId: String?) {
        val tokenOptions = tokenOptions(selectedChainId)
        internalState.tokens = tokenOptions
        internalState.token = defaultTokenAddress(selectedChainId)
        internalState.tokenResources = tokenResources(selectedChainId)
    }

//    Update this when we move chains to state
//    https://linear.app/dydx/issue/OTE-470/migrate-chains-and-tokens-to-abacus-state
    fun getChainById(chainId: String): Map<String, Any>? {
        return parser.asNativeMap(this.chains?.find { parser.asString(parser.asNativeMap(it)?.get("chain_id")) == chainId })
    }

    fun defaultChainId(): String? {
        val firstChain = this.chains?.firstOrNull { parser.asString(parser.asNativeMap(it)?.get("chain_type")) == selectedChainType?.rawValue }
        val selectedChain = when (selectedChainType) {
            ChainType.EVM -> getChainById(chainId = ETHEREUM_CHAIN_ID) ?: parser.asNativeMap(firstChain)
            ChainType.COSMOS -> parser.asNativeMap(firstChain)
            ChainType.SVM -> getChainById(chainId = "solana") ?: parser.asNativeMap(firstChain)
            null -> return null
        }
        return parser.asString(selectedChain?.get("chain_id"))
    }

    fun getTokenByDenomAndChainId(tokenDenom: String?, chainId: String?): Map<String, Any>? {
        val tokensList = filteredTokens(chainId)
        tokensList?.find {
            parser.asString(parser.asNativeMap(it)?.get("denom")) == (tokenDenom ?: NATIVE_TOKEN_DEFAULT_ADDRESS)
        }?.let {
            return parser.asNativeMap(it)
        }
        return null
    }

    fun selectedTokenSymbol(tokenAddress: String?, selectedChainId: String?): String? {
        val token = getTokenByDenomAndChainId(tokenAddress, selectedChainId) ?: return null
        return parser.asString(token.get("symbol"))
    }

    fun selectedTokenDecimals(tokenAddress: String?, selectedChainId: String?): String? {
        val tokensList = filteredTokens(selectedChainId)
        tokensList?.find {
            (parser.asString(parser.asNativeMap(it)?.get("denom")) == tokenAddress || parser.asString(parser.asNativeMap(it)?.get("skipDenom")) == tokenAddress)
        }?.let {
            return parser.asString(parser.asNativeMap(it)?.get("decimals"))
        }
        return null
    }

    fun filteredTokens(chainId: String?): List<Any>? {
        val chainIdToUse = chainId ?: defaultChainId()
        val assetsMapForChainId = parser.asNativeMap(this.skipTokens?.get(chainIdToUse))
        val assetsForChainId = parser.asNativeList(assetsMapForChainId?.get("assets"))
//      coinbase exchange chainId is noble-1. we only allow usdc withdrawals from it
        if (chainId === exchangeDestinationChainId) {
            return assetsForChainId?.filter {
                parser.asString(parser.asNativeMap(it)?.get("denom")) == "uusdc"
            }
        }

        val tokensWithSkipDenom = mutableListOf<Map<String, Any>>()
/*        we have to replace skip's {chain-name}-native naming bc it doesn't play well with
        any of our SDKs.
        however, their {chain-name}-native denom naming is required for their API
        so we need to store both values
 */
        assetsForChainId?.forEach {
            val token = parser.asNativeMap(it)?.toMutableMap()
            if (token != null) {
                val denom = parser.asString(token["denom"])
                if (denom?.endsWith("native") == true) {
                    token["skipDenom"] = denom
                    token["denom"] = NATIVE_TOKEN_DEFAULT_ADDRESS
                }
                tokensWithSkipDenom.add(token.toMap())
            }
        }

        return tokensWithSkipDenom
    }

    fun defaultTokenAddress(chainId: String?): String? {
        if (chainId == null) {
            return null
        }
        val filteredTokensForChainId = filteredTokens(chainId) ?: return null

        // Find if any CctpChainTokenInfo item belongs to the provided chainId
        val cctpChain = cctpChainIds?.find { it.chainId == chainId }
//        If one does, then check for a token in the filteredTokens whose denom matches the found cctpChainTokenInfo, if one exists
        val filteredCctpToken = filteredTokensForChainId.find {
            parser.asString(parser.asNativeMap(it)?.get("denom"))?.lowercase() == cctpChain?.tokenAddress?.lowercase()
        }
        if (filteredCctpToken != null) {
            return parser.asString(parser.asNativeMap(filteredCctpToken)?.get("denom"))
        }
//        If no cctp item available, check for a native token item
        val nativeChain = filteredTokensForChainId.find {
            parser.asString(parser.asNativeMap(it)?.get("denom")) == NATIVE_TOKEN_DEFAULT_ADDRESS
        }
        if (nativeChain != null) {
            return parser.asString(parser.asNativeMap(nativeChain)?.get("denom"))
        }
//        Otherwise, just grab the first available token if any exist
        val firstTokenOrNull = parser.asNativeMap(filteredTokensForChainId.firstOrNull())
        if (firstTokenOrNull != null) {
            return parser.asString(parser.asNativeMap(firstTokenOrNull)?.get("denom"))
        }
        return null
    }

    fun chainResources(chainId: String?): Map<String, TransferInputChainResource>? {
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

    fun tokenResources(chainId: String?): Map<String, TransferInputTokenResource>? {
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

    fun chainOptions(): List<SelectionOption> {
        val chainProcessor = SkipChainProcessor(parser)
        val options = mutableListOf<SelectionOption>()

        this.chains?.let {
            for (chain in it) {
                parser.asNativeMap(chain)?.let { chain ->
                    if (parser.asString(chain.get("chain_type")) == selectedChainType?.rawValue) {
                        options.add(chainProcessor.received(chain))
                    }
                }
            }
        }

        options.sortBy { parser.asString(it.stringKey) }
        return options
    }

    fun tokenOptions(chainId: String?): List<SelectionOption> {
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
