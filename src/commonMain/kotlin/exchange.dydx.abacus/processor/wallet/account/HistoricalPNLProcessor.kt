package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.codegen.IndexerPnlTicksResponseObject

internal interface HistoricalPNLProcessorProtocol {
    fun process(
        existing: SubaccountHistoricalPNL?,
        payload: IndexerPnlTicksResponseObject,
    ): SubaccountHistoricalPNL?
}

internal class HistoricalPNLProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), HistoricalPNLProcessorProtocol {
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

    override fun process(
        existing: SubaccountHistoricalPNL?,
        payload: IndexerPnlTicksResponseObject,
    ): SubaccountHistoricalPNL? {
        val equity = parser.asDouble(payload.equity) ?: return null
        val totalPnl = parser.asDouble(payload.totalPnl) ?: return null
        val netTransfers = parser.asDouble(payload.netTransfers) ?: return null
        val createdAt = parser.asDatetime(payload.createdAt) ?: return null

        val pnl = SubaccountHistoricalPNL(
            equity = equity,
            totalPnl = totalPnl,
            netTransfers = netTransfers,
            createdAtMilliseconds = createdAt.toEpochMilliseconds().toDouble(),
        )

        return if (pnl != existing) pnl else existing
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, historicalPNLKeyMap)
    }
}
