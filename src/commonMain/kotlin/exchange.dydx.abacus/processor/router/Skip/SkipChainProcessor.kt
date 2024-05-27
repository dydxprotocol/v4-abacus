package exchange.dydx.abacus.processor.router.Skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class SkipChainProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "chain_name" to "stringKey",
            "networkIdentifier" to "stringKey",
            "chain_id" to "type",
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
