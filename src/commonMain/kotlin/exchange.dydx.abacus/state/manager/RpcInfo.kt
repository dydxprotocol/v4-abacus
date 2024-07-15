package exchange.dydx.abacus.state.manager

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RpcInfo(
    @SerialName("rpc") val rpcUrl: String,
    val name: String,
)

typealias ChainRpcMap = Map<String, RpcInfo>
object RpcConfigs {
    var chainRpcMap: ChainRpcMap = emptyMap()
}
