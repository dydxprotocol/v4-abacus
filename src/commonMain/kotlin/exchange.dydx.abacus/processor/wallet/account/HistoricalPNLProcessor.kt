package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

@Suppress("UNCHECKED_CAST")
internal class HistoricalPNLProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val historicalPNLKeyMap = iMapOf(
        "double" to iMapOf(
            "equity" to "equity",
            "totalPnl" to "totalPnl",
            "netTransfers" to "netTransfers"
        ),
        "datetime" to iMapOf(
            "createdAt" to "createdAt"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        return transform(existing, payload, historicalPNLKeyMap)
    }
}