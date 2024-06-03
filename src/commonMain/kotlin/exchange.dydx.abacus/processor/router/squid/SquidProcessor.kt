package exchange.dydx.abacus.processor.router.squid

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputTokenResource
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.router.IRouterProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.state.manager.CctpConfig.cctpChainIds
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class SquidProcessor(
    parser: ParserProtocol,
    private val internalState: InternalTransferInputState,
) : BaseProcessor(parser), IRouterProcessor {
    override var chains: List<Any>? = null
    override var tokens: List<Any>? = null
    override var exchangeDestinationChainId: String? = null

    override fun receivedChains(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        if (this.chains != null) {
            return existing
        }
        this.chains = parser.asNativeList(payload.get("chains"))

        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }
        val chainOptions = chainOptions()

        internalState.chains = chainOptions

        val selectedChainId = defaultChainId()
        modified.safeSet("transfer.chain", selectedChainId)
        selectedChainId?.let {
            internalState.chainResources = chainResources(selectedChainId)
        }

        updateTokensDefaults(modified, selectedChainId)

        return modified
    }

    override fun receivedTokens(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        if (this.tokens != null) {
            return existing
        }
        this.tokens = parser.asNativeList(payload.get("tokens")) as List<Map<String, Any>>?

        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }

        val selectedChainId = defaultChainId()
        updateTokensDefaults(modified, selectedChainId)

        return modified
    }

    override fun receivedV2SdkInfo(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        if (this.chains != null && this.tokens != null) {
            return existing
        }
        this.chains = parser.asNativeList(payload["chains"])
        this.tokens = parser.asNativeList(payload["tokens"])

        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }
        val chainOptions = chainOptions()

        internalState.chains = chainOptions

        val selectedChainId = defaultChainId()
        modified.safeSet("transfer.chain", selectedChainId)
        selectedChainId?.let {
            internalState.chainResources = chainResources(selectedChainId)
        }

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

        val processor = SquidRouteProcessor(parser)
        modified.safeSet(
            "transfer.route",
            processor.received(null, payload) as MutableMap<String, Any>,
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
        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }

        val processor = SquidRouteV2Processor(parser)
        modified.safeSet(
            "transfer.route",
            processor.received(null, payload) as MutableMap<String, Any>,
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
        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }

        val processor = SquidStatusProcessor(parser, transactionId)
        return processor.received(existing, payload)
    }

    override fun updateTokensDefaults(modified: MutableMap<String, Any>, selectedChainId: String?) {
        val tokenOptions = tokenOptions(selectedChainId)
        internalState.tokens = tokenOptions
        modified.safeSet("transfer.depositOptions.assets", tokenOptions)
        modified.safeSet("transfer.withdrawalOptions.assets", tokenOptions)
        modified.safeSet("transfer.token", defaultTokenAddress(selectedChainId))
        internalState.tokenResources = tokenResources(selectedChainId)
    }

    override fun defaultChainId(): String? {
        val selectedChain = parser.asNativeMap(this.chains?.firstOrNull())
        return parser.asString(selectedChain?.get("chainId"))
    }

    override fun selectedTokenSymbol(tokenAddress: String?, selectedChainId: String?): String? {
        this.tokens?.find {
            parser.asString(parser.asNativeMap(it)?.get("address")) == tokenAddress
        }?.let {
            return parser.asString(parser.asNativeMap(it)?.get("symbol"))
        }
        return null
    }

    override fun selectedTokenDecimals(tokenAddress: String?, selectedChainId: String?): String? {
        this.tokens?.find {
            parser.asString(parser.asNativeMap(it)?.get("address")) == tokenAddress
        }?.let {
            return parser.asString(parser.asNativeMap(it)?.get("decimals"))
        }
        return null
    }

    override fun filteredTokens(chainId: String?): List<Any>? {
        chainId?.let {
            val filteredTokens = mutableListOf<Map<String, Any>>()
            this.tokens?.let {
                for (token in it) {
                    parser.asNativeMap(token)?.let { token ->
                        if (parser.asString(token.get("chainId")) == chainId) {
                            filteredTokens.add(token)
                        }
                    }
                }
            }
            return filteredTokens
        }
        return tokens
    }

    override fun defaultTokenAddress(chainId: String?): String? {
        return chainId?.let { cid ->
            // Retrieve the list of filtered tokens for the given chainId
            val filteredTokens = this.filteredTokens(cid)?.mapNotNull {
                parser.asString(parser.asNativeMap(it)?.get("address"))
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
                parser.asString(parser.asNativeMap(it)?.get("chainId")) == chainId
            }?.let {
                val processor = SquidChainResourceProcessor(parser)
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
            parser.asString(parser.asNativeMap(it)?.get("address"))?.let { key ->
                val processor = SquidTokenResourceProcessor(parser)
                parser.asNativeMap(it)?.let { payload ->
                    tokenResources[key] = processor.received(payload)
                }
            }
        }
        return tokenResources
    }

    override fun chainOptions(): List<SelectionOption> {
        val chainProcessor = SquidChainProcessor(parser)
        val options = mutableListOf<SelectionOption>()

        this.chains?.let {
            for (chain in it) {
                parser.asNativeMap(chain)?.let { chain ->
                    if (parser.asString(chain["chainType"]) != "cosmos") {
                        options.add(chainProcessor.received(chain))
                    }
                }
            }
        }

        options.sortBy { parser.asString(parser.asNativeMap(it)?.get("stringKey")) }
        return options
    }

    override fun tokenOptions(chainId: String?): List<SelectionOption> {
        val processor = SquidTokenProcessor(parser)
        val options = mutableListOf<SelectionOption>()

        val selectedChainId = chainId ?: defaultChainId()
        selectedChainId?.let {
            selectedChainId
            this.tokens?.let {
                for (token in it) {
                    parser.asNativeMap(token)?.let { token ->
                        if (parser.asString(token["chainId"]) == selectedChainId) {
                            options.add(processor.received(token))
                        }
                    }
                }
            }
        }

        options.sortBy { it.stringKey }
        return options
    }
}
