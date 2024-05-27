package exchange.dydx.abacus.processor.router.Squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class SquidChainProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "chainName" to "stringKey",
            "networkIdentifier" to "stringKey",
            "chainId" to "type",
            "chainIconURI" to "iconUrl",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, keyMap)
    }
}
