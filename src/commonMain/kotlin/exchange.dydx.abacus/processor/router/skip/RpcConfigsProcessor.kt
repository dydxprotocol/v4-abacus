import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.ChainRpcMap
import exchange.dydx.abacus.state.manager.RpcInfo
import exchange.dydx.abacus.utils.filterNotNull

internal class RpcConfigsProcessor(
    private val parser: ParserProtocol,
    private val alchemyApiKey: String?,
) {
    fun received(
        payload: String
    ): ChainRpcMap {
        return parser.decodeJsonObject(payload)?.mapValues { entry ->
            parser.asMap(entry.value)?.let { rpcInfoMap ->
                // use alchemy RPC if available
                parser.asString(rpcInfoMap["name"])?.let { name ->
                    val fallbackRpcUrl = parser.asString(rpcInfoMap["fallbackRpcUrl"])
                    val alchemyRpcUrl = parser.asString(rpcInfoMap["alchemyRpcUrl"])

                    if (alchemyApiKey != null && alchemyRpcUrl != null) {
                        return@let RpcInfo(
                            rpcUrl = "$alchemyRpcUrl/$alchemyApiKey",
                            name = name,
                        )
                    } else if (fallbackRpcUrl != null) {
                        return@let RpcInfo(
                            rpcUrl = fallbackRpcUrl,
                            name = name,
                        )
                    } else {
                        return@let null
                    }
                }
            }
        }?.filterNotNull() ?: emptyMap()
    }
}
