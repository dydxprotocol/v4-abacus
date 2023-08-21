package exchange.dydx.abacus.processor.squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMutableListOf

internal class SquidProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private var chains: IList<Any>? = null
    private var tokens: IList<Any>? = null

    internal fun receivedChains(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        if (this.chains != null) {
            return existing
        }
        this.chains = parser.asList(payload.get("chains"))

        var modified = iMutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }
        val chainOptions = chainOptions()
        modified.safeSet("transfer.depositOptions.chains", chainOptions)
        modified.safeSet("transfer.withdrawalOptions.chains", chainOptions)
        val selectedChainId = defaultChainId()
        modified.safeSet("transfer.chain",  selectedChainId)
        selectedChainId?.let {
            modified.safeSet("transfer.resources.chainResources", chainResources(selectedChainId))
        }

        updateTokensDefaults(modified, selectedChainId)

        return modified
    }

    internal fun receivedTokens(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        if (this.tokens != null) {
            return existing
        }
        this.tokens = parser.asList(payload.get("tokens")) as IList<IMap<String, Any>>?

        var modified = iMutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }

        val selectedChainId = defaultChainId()
        updateTokensDefaults(modified, selectedChainId)

        return modified
    }

    internal fun receivedRoute(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        var modified = iMutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }

        val processor = SquidRouteProcessor(parser)
        modified.safeSet("transfer.route", processor.received(null, payload) as IMutableMap<String, Any>)
        if (parser.asMap(existing?.get("transfer"))?.get("type") == "DEPOSIT") {
		    val value = parser.value(modified, "transfer.route.toAmountUSD")
            modified.safeSet("transfer.size.usdcSize", value)
        }

        return modified
    }

    internal fun receivedStatus(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        var modified = iMutableMapOf<String, Any>()
        existing?.let {
            modified = it.mutable()
        }

        val processor = SquidStatusProcessor(parser)
        return processor.received(existing, payload)
    }

    private fun updateTokensDefaults(modified: IMutableMap<String, Any>, selectedChainId: String?) {
        modified.safeSet("transfer.depositOptions.assets", tokenOptions(selectedChainId))
        modified.safeSet("transfer.withdrawalOptions.assets", tokenOptions(selectedChainId))
        modified.safeSet("transfer.token", defaultTokenAddress(selectedChainId))
        modified.safeSet("transfer.resources.tokenResources", tokenResources(selectedChainId))
    }

    internal fun defaultChainId(): String? {
        val selectedChain = parser.asMap(this.chains?.firstOrNull())
        return parser.asString(selectedChain?.get("chainId"))
    }
    
    internal fun selectedTokenSymbol(tokenAddress: String?): String? {
        this.tokens?.find {
            parser.asString(parser.asMap(it)?.get("address")) == tokenAddress
        }?.let {
            return parser.asString(parser.asMap(it)?.get("symbol"))
        }
        return null
    }

    internal fun selectedTokenDecimals(tokenAddress: String?): String? {
        this.tokens?.find {
            parser.asString(parser.asMap(it)?.get("address")) == tokenAddress
        }?.let {
            return parser.asString(parser.asMap(it)?.get("decimals"))
        }
        return null
    }

    private fun filteredTokens(chainId: String?): IList<Any>? {
        chainId?.let {
            val filteredTokens = iMutableListOf<IMap<String, Any>>()
            this.tokens?.let {
                for (token in it) {
                    parser.asMap(token)?.let { token ->
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
        val selectedToken = parser.asMap(this.filteredTokens(chainId)?.firstOrNull())
        return parser.asString(selectedToken?.get("address"))
    }

    internal fun chainResources(chainId: String?): IMap<String, Any>? {
        val chainResources = iMutableMapOf<String, Any>()
        chainId?.let {
            this.chains?.find {
                parser.asString(parser.asMap(it)?.get("chainId")) == chainId
            }?.let {
                val processor = SquidChainResourceProcessor(parser)
                parser.asMap(it)?.let { payload ->
                    chainResources[chainId] = processor.received(null, payload)
                }
            }
        }
        return chainResources
    }

    internal fun tokenResources(chainId: String?): IMap<String, Any>? {
        val tokenResources = iMutableMapOf<String, Any>()
        filteredTokens(chainId)?.forEach {
            parser.asString(parser.asMap(it)?.get("address"))?.let { key ->
                val processor = SquidTokenResourceProcessor(parser)
                parser.asMap(it)?.let { payload ->
                    tokenResources[key] = processor.received(null, payload)
                }
            }
        }
        return tokenResources
    }

    private fun chainOptions(): IList<Any> {
        val chainProcessor = SquidChainProcessor(parser)
        val options = iMutableListOf<Any>()

        this.chains?.let {
            for (chain in it) {
                parser.asMap(chain)?.let { chain ->
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

    internal fun tokenOptions(chainId: String?): IList<Any> {
        val processor = SquidTokenProcessor(parser)
        val options = iMutableListOf<Any>()

        val selectedChainId = chainId ?: defaultChainId()
        selectedChainId?.let { selectedChainId
            this.tokens?.let {
                for (token in it) {
                    parser.asMap(token)?.let { token ->
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
