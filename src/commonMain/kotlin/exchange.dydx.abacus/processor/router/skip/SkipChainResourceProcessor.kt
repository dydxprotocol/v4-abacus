package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.RpcConfigs

internal class SkipChainResourceProcessor(private val parser: ParserProtocol) {

    fun received(
        payload: Map<String, Any>
    ): TransferInputChainResource {
        return TransferInputChainResource(
            chainName = parser.asString(payload["chain_name"]),
            rpc = parser.asString(payload["chain_id"])?.let { RpcConfigs.chainRpcMap[it]?.rpcUrl },
            networkName = parser.asString(payload["networkName"]),
            chainId = parser.asInt(payload["chain_id"]),
            iconUrl = parser.asString(payload["logo_uri"]),
        )
    }
}
