package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

@Suppress("UNCHECKED_CAST")
internal class HistoricalPNLProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val historicalPNLKeyMap = mapOf(
        "double" to mapOf(
            "equity" to "equity",
            "totalPnl" to "totalPnl",
            "netTransfers" to "netTransfers",
        ),
        "datetime" to mapOf(
            "createdAt" to "createdAt",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, historicalPNLKeyMap)
    }
}
