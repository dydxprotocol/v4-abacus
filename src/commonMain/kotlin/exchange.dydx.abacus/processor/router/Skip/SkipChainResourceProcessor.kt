package exchange.dydx.abacus.processor.router.Skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class SkipChainResourceProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "chain_name" to "chainName",
            "rpc" to "rpc",
            "networkName" to "networkName",
            "chain_id" to "chainId",
            "logo_uri" to "iconUrl",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, keyMap)
    }
}
