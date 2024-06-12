package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.mergeWithIds
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.safeSet

internal class FillsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = FillProcessor(parser = parser)

    fun received(existing: List<Any>?, payload: List<Any>, subaccountNumber: Int?): List<Any>? {
        val new = payload.mapNotNull { eachPayload ->
            parser.asNativeMap(eachPayload)?.let { eachPayloadData ->
                val modified = eachPayloadData.toMutableMap()
                val fillSubaccountNumber = parser.asInt(eachPayloadData["subaccountNumber"])

                if (fillSubaccountNumber == null && subaccountNumber != null) {
                    modified.safeSet("subaccountNumber", subaccountNumber)
                }

                parser.asInt(modified["subaccountNumber"])?.run {
                    modified.safeSet("marginMode", if (this >= NUM_PARENT_SUBACCOUNTS) MarginMode.isolated.rawValue else MarginMode.cross.rawValue)
                }

                itemProcessor.received(
                    null,
                    modified,
                )
            }
        }
        existing?.let {
            return mergeWithIds(new, existing) { data -> parser.asNativeMap(data)?.let { parser.asString(it["id"]) } }
        }
        return new
    }
}
