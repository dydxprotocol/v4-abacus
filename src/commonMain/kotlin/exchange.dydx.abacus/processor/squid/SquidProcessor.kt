package exchange.dydx.abacus.processor.squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class SquidProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private var chains: List<Any>? = null
    private var tokens: List<Any>? = null

    internal fun receivedChains(
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
        modified.safeSet("transfer.depositOptions.chains", chainOptions)
        modified.safeSet("transfer.withdrawalOptions.chains", chainOptions)
        val selectedChainId = defaultChainId()
        modified.safeSet("transfer.chain", selectedChainId)
        selectedChainId?.let {
            modified.safeSet("transfer.resources.chainResources", chainResources(selectedChainId))
        }

        updateTokensDefaults(modified, selectedChainId)

        return modified
    }

    internal fun receivedTokens(
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

    internal fun receivedV2SdkInfo(
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
        modified.safeSet("transfer.depositOptions.chains", chainOptions)
        modified.safeSet("transfer.withdrawalOptions.chains", chainOptions)
        val selectedChainId = defaultChainId()
        modified.safeSet("transfer.chain", selectedChainId)
        selectedChainId?.let {
            modified.safeSet("transfer.resources.chainResources", chainResources(selectedChainId))
        }

        updateTokensDefaults(modified, selectedChainId)

        return modified
    }

    internal fun receivedRoute(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }

        val processor = SquidRouteProcessor(parser)
        modified.safeSet(
            "transfer.route",
            processor.received(null, payload) as MutableMap<String, Any>
        )
        if (parser.asNativeMap(existing?.get("transfer"))?.get("type") == "DEPOSIT") {
            val value = usdcAmount(modified)
            modified.safeSet("transfer.size.usdcSize", value)
        }

        return modified
    }

    internal fun receivedRouteV2(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        var modified = mutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }

        val processor = SquidRouteV2Processor(parser)
        modified.safeSet(
            "transfer.route",
            processor.received(null, payload) as MutableMap<String, Any>
        )
        if (parser.asNativeMap(existing?.get("transfer"))?.get("type") == "DEPOSIT") {
            val value = usdcAmount(modified)
            modified.safeSet("transfer.size.usdcSize", value)
        }

        return modified
    }

    private fun usdcAmount(data: Map<String, Any>): Double? {
        var toAmountUSD = parser.asString(parser.value(data, "transfer.route.toAmountUSD"))
        toAmountUSD = toAmountUSD?.replace(",", "")
        var toAmount = parser.asString(parser.value(data, "transfer.route.toAmount"))
        toAmount = toAmount?.replace(",", "")
        return parser.asDouble(toAmountUSD) ?: parser.asDouble(toAmount)?.div(QUANTUM_MULTIPLIER)
    }

    internal fun receivedStatus(
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

    private fun updateTokensDefaults(modified: MutableMap<String, Any>, selectedChainId: String?) {
        modified.safeSet("transfer.depositOptions.assets", tokenOptions(selectedChainId))
        modified.safeSet("transfer.withdrawalOptions.assets", tokenOptions(selectedChainId))
        modified.safeSet("transfer.token", defaultTokenAddress(selectedChainId))
        modified.safeSet("transfer.resources.tokenResources", tokenResources(selectedChainId))
    }

    internal fun defaultChainId(): String? {
        val selectedChain = parser.asNativeMap(this.chains?.firstOrNull())
        return parser.asString(selectedChain?.get("chainId"))
    }

    internal fun selectedTokenSymbol(tokenAddress: String?): String? {
        this.tokens?.find {
            parser.asString(parser.asNativeMap(it)?.get("address")) == tokenAddress
        }?.let {
            return parser.asString(parser.asNativeMap(it)?.get("symbol"))
        }
        return null
    }

    internal fun selectedTokenDecimals(tokenAddress: String?): String? {
        this.tokens?.find {
            parser.asString(parser.asNativeMap(it)?.get("address")) == tokenAddress
        }?.let {
            return parser.asString(parser.asNativeMap(it)?.get("decimals"))
        }
        return null
    }

    private fun filteredTokens(chainId: String?): List<Any>? {
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

    internal fun defaultTokenAddress(chainId: String?): String? {
        val selectedToken = parser.asNativeMap(this.filteredTokens(chainId)?.firstOrNull())
        return parser.asString(selectedToken?.get("address"))
    }

    internal fun chainResources(chainId: String?): Map<String, Any>? {
        val chainResources = mutableMapOf<String, Any>()
        chainId?.let {
            this.chains?.find {
                parser.asString(parser.asNativeMap(it)?.get("chainId")) == chainId
            }?.let {
                val processor = SquidChainResourceProcessor(parser)
                parser.asNativeMap(it)?.let { payload ->
                    chainResources[chainId] = processor.received(null, payload)
                }
            }
        }
        return chainResources
    }

    internal fun tokenResources(chainId: String?): Map<String, Any>? {
        val tokenResources = mutableMapOf<String, Any>()
        filteredTokens(chainId)?.forEach {
            parser.asString(parser.asNativeMap(it)?.get("address"))?.let { key ->
                val processor = SquidTokenResourceProcessor(parser)
                parser.asNativeMap(it)?.let { payload ->
                    tokenResources[key] = processor.received(null, payload)
                }
            }
        }
        return tokenResources
    }

    private fun chainOptions(): List<Any> {
        val chainProcessor = SquidChainProcessor(parser)
        val options = mutableListOf<Any>()

        this.chains?.let {
            for (chain in it) {
                parser.asNativeMap(chain)?.let { chain ->
                    if (parser.asString(chain.get("chainType")) != "cosmos") {
                        chainProcessor.received(null, chain)?.let {
                            options.add(it)
                        }
                    }
                }
            }
        }

        return options
    }

    internal fun tokenOptions(chainId: String?): List<Any> {
        val processor = SquidTokenProcessor(parser)
        val options = mutableListOf<Any>()

        val selectedChainId = chainId ?: defaultChainId()
        selectedChainId?.let {
            selectedChainId
            this.tokens?.let {
                for (token in it) {
                    parser.asNativeMap(token)?.let { token ->
                        if (parser.asString(token.get("chainId")) == selectedChainId) {
                            processor.received(null, token)?.let {
                                options.add(it)
                            }
                        }
                    }
                }
            }
        }

        return options
    }
}
