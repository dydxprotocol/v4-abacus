package exchange.dydx.abacus.processor.squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

internal class SquidChainResourceProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = iMapOf(
        "string" to iMapOf(
            "chainName" to "chainName",
            "rpc" to "rpc",
            "networkName" to "networkName",
            "chainId" to "chainId",
            "chainIconURI" to "iconUrl"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        return transform(existing, payload, keyMap)
    }
}