package exchange.dydx.abacus.state.manager

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class RpcInfo(
    val rpcUrl: String,
    val name: String,
)

typealias ChainRpcMap = Map<String, RpcInfo>
object RpcConfigs {
    var chainRpcMap: ChainRpcMap = emptyMap()
}
