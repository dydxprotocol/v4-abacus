package exchange.dydx.abacus.state.manager

data class RpcInfo(
    val rpcUrl: String,
    val name: String,
)

typealias ChainRpcMap = Map<String, RpcInfo>
object RpcConfigs {
    var chainRpcMap: ChainRpcMap = emptyMap()
}
