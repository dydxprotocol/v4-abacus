package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.output.MarketHistoricalFunding
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketState
import indexer.codegen.IndexerHistoricalFundingResponseObject
import kotlinx.datetime.Instant

@Suppress("UNCHECKED_CAST")
internal class HistoricalFundingsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    fun process(
        existing: InternalMarketState,
        payload: List<IndexerHistoricalFundingResponseObject>?,
    ): InternalMarketState {
        val history = payload?.mapNotNull {
            val rate = parser.asDouble(it.rate)
            val price = parser.asDouble(it.price)
            val effectiveAt = parser.asDatetime(it.effectiveAt)?.toEpochMilliseconds()?.toDouble()
            if (rate != null && price != null && effectiveAt != null) {
                MarketHistoricalFunding(
                    rate = rate,
                    price = price,
                    effectiveAtMilliseconds = effectiveAt,
                )
            } else {
                null
            }
        }?.reversed()

        existing.historicalFundings = merge(
            parser = parser,
            existing = existing.historicalFundings,
            incoming = history,
            timeField = { item ->
                item?.effectiveAtMilliseconds?.toLong()?.let {
                    Instant.fromEpochMilliseconds(it)
                }
            },
            ascending = true,
        )

        return existing
    }
}
