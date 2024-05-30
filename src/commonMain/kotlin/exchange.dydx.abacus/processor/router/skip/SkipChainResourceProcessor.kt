package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class SkipChainResourceProcessor(private val parser: ParserProtocol) {

    fun received(
        payload: Map<String, Any>
    ): TransferInputChainResource {
        return TransferInputChainResource(
            chainName=parser.asString(payload["chain_name"]),
            rpc=parser.asString(payload["rpc"]),
            networkName=parser.asString(payload["networkName"]),
            chainId=parser.asInt(payload["chain_id"]),
            iconUrl=parser.asString(payload["logo_uri"]),
        )
    }
}
