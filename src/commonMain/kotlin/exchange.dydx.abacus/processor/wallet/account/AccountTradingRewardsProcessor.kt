package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.codegen.IndexerHistoricalTradingRewardAggregation

internal class AccountTradingRewardsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    fun processHistoricalTradingRewards(
        existing: List<IndexerHistoricalTradingRewardAggregation>?,
        payload: List<IndexerHistoricalTradingRewardAggregation>?,
    ): List<IndexerHistoricalTradingRewardAggregation>? {
        return merge(
            parser = parser,
            existing = existing,
            incoming = payload,
            timeField = { item ->
                parser.asDatetime(item?.startedAt)
            },
            ascending = false,
        )
    }
}
