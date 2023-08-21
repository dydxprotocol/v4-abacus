package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

internal class HistoricalFundingProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = iMapOf(
        "double" to iMapOf(
            "rate" to "rate",
            "price" to "price"
        ),
        "datetime" to iMapOf(
            "effectiveAt" to "effectiveAt"
        )
    )

    override fun received(existing: IMap<String, Any>?, payload: IMap<String, Any>): IMap<String, Any> {
        return transform(existing, payload, keyMap)
    }
}