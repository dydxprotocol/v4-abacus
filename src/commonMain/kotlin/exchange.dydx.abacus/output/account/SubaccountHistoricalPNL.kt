package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ParsingHelper
import kollections.JsExport
import kollections.toIList
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountHistoricalPNL(
    val equity: Double,
    val totalPnl: Double,
    val netTransfers: Double,
    val createdAtMilliseconds: Double,
) {
    companion object {
        private fun create(
            existing: SubaccountHistoricalPNL?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): SubaccountHistoricalPNL? {
            Logger.d { "creating Account Historical PNL\n" }
            data?.let {
                val equity = parser.asDouble(data["equity"])
                val totalPnl = parser.asDouble(data["totalPnl"])
                val netTransfers = parser.asDouble(data["netTransfers"])
                val createdAtMilliseconds =
                    parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                if (equity != null && totalPnl != null && netTransfers != null && createdAtMilliseconds != null) {
                    return if (existing?.equity != equity ||
                        existing.totalPnl != totalPnl ||
                        existing.netTransfers != netTransfers ||
                        existing.createdAtMilliseconds != createdAtMilliseconds
                    ) {
                        SubaccountHistoricalPNL(
                            equity,
                            totalPnl,
                            netTransfers,
                            createdAtMilliseconds,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Account Historical PNL not valid" }
            return null
        }

        fun create(
            existing: IList<SubaccountHistoricalPNL>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
            startTime: Instant,
        ): IList<SubaccountHistoricalPNL>? {
            return ParsingHelper.merge(
                parser = parser,
                existing = existing,
                data = data,
                comparison = { obj, itemData ->
                    val time2 = parser.asDatetime(itemData["createdAt"])
                    if (time2 != null && time2 >= startTime) {
                        val time1 = (obj as SubaccountHistoricalPNL).createdAtMilliseconds
                        val time2MS = time2.toEpochMilliseconds().toDouble()
                        ParsingHelper.compare(time1, time2MS ?: 0.0, true)
                    } else {
                        null
                    }
                },
                createObject = { _, obj, itemData ->
                    obj ?: SubaccountHistoricalPNL.create(
                        null,
                        parser,
                        parser.asMap(itemData),
                    )
                },
                syncItems = true,
                includesObjectBlock = { item ->
                    val ms = (item as SubaccountHistoricalPNL).createdAtMilliseconds.toDouble()
                    val createdAt = Instant.fromEpochMilliseconds(ms.toLong())
                    createdAt >= startTime
                },
                includesDataBlock = { itemData ->
                    val createdAt = parser.asDatetime(itemData["createdAt"])
                    createdAt != null && createdAt >= startTime
                },
            )?.toIList()
        }
    }
}
