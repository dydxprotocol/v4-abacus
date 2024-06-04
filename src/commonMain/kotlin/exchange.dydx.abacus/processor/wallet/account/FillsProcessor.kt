package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.mergeWithIds
import exchange.dydx.abacus.protocols.ParserProtocol

internal class FillsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = FillProcessor(parser = parser)

    override fun received(existing: List<Any>?, payload: List<Any>): List<Any>? {
        val new = payload.mapNotNull { eachPayload ->
            parser.asNativeMap(eachPayload)?.let { eachPayloadData -> itemProcessor.received(null, eachPayloadData) }
        }
        existing?.let {
            return mergeWithIds(new, existing) { data -> parser.asNativeMap(data)?.let { parser.asString(it["id"]) } }
        }
        return new
    }
}
