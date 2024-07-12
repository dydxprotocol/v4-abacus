import exchange.dydx.abacus.state.manager.ChainRpcMap
import exchange.dydx.abacus.state.manager.RpcConfigs
import exchange.dydx.abacus.utils.Logger
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class RpcConfigsProcessor {
     fun received(
        payload: String
    ): ChainRpcMap {
        try {
            return payload.let { Json.decodeFromString<ChainRpcMap>(it) }
        } catch (e: IllegalArgumentException) {
            Logger.e { "retrieveChainRpcEndpoints IllegalArgumentException error: $e" }
        } catch (e: SerializationException) {
            Logger.e { "retrieveChainRpcEndpoints SerializationException error: $e" }
        }
        return RpcConfigs.chainRpcMap
    }
}