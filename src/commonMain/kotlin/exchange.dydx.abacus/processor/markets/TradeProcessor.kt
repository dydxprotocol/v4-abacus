package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf

internal class TradeProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val tradeKeyMap = iMapOf(
        "string" to iMapOf(
            "id" to "id",
            "side" to "side"
        ),
        "double" to iMapOf(
            "size" to "size",
            "price" to "price"
        ),
        "datetime" to iMapOf(
            "createdAt" to "createdAt"
        ),
        "bool" to iMapOf(
            "liquidation" to "liquidation"
        )
    )

    private val sideStringKeys = iMapOf(
        "BUY" to "APP.GENERAL.BUY",
        "SELL" to "APP.GENERAL.SELL"
    )

    override fun received(existing: IMap<String, Any>?, payload: IMap<String, Any>): IMap<String, Any> {
        val trade = transform(existing, payload, tradeKeyMap)
        val resources = iMutableMapOf<String, Any>()

        (parser.asString(payload["side"])).let {
            sideStringKeys[it]?.let {
                resources["sideStringKey"] = it
            }
        }
        trade["resources"] = resources
        return trade
    }
}
