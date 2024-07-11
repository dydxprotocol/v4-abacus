package exchange.dydx.abacus.state.manager

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RpcInfo(
    @SerialName("rpc") val rpcUrl: String,
    val name: String,
)


object RpcConfigs {
    var chainIdToRpcMap: Map<String, RpcInfo> = mapOf()
}