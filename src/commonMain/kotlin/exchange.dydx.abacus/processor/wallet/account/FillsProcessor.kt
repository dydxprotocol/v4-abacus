package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.mergeWithIds
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.safeSet

internal class FillsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = FillProcessor(parser = parser)

    fun received(existing: List<Any>?, payload: List<Any>, subaccountNumber: Int): List<Any>? {
        val new = payload.mapNotNull { eachPayload ->
            parser.asNativeMap(eachPayload)?.let { eachPayloadData ->
                val modified = eachPayloadData.toMutableMap()

                itemProcessor.received(
                    null,
                    modified,
                    subaccountNumber
                )
            }
        }
        existing?.let {
            return mergeWithIds(new, existing) { data -> parser.asNativeMap(data)?.let { parser.asString(it["id"]) } }
        }
        return new
    }
}
