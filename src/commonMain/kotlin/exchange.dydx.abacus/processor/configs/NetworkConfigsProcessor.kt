package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

@Suppress("UNCHECKED_CAST")
internal class NetworkConfigsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = iMapOf(
        "string" to iMapOf(
            "api" to "api",
            "node" to "node"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        return transform(existing, payload, keyMap)
    }
}