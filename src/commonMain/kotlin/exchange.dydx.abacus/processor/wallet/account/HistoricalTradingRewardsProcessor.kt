package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class HistoricalTradingRewardsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = HistoricalTradingRewardProcessor(parser = parser)

    override fun received(
        existing: List<Any>?,
        payload: List<Any>,
    ): List<Any>? {
        val history = mutableListOf<Any>()
        for (item in payload) {
            parser.asNativeMap(item)?.let {
                history.add(itemProcessor.received(null, it))
            }
        }
        return mergeDeprecated(
            parser = parser,
            existing = existing,
            incoming = history,
            timeField = "startedAt",
            ascending = false,
        )
    }

    fun receivedBlockTradingRewardDeprecated(
        existing: List<Any>?,
        payload: Any,
    ): List<Any>? {
        val modified = existing?.toMutableList() ?: mutableListOf()
        parser.asNativeMap(payload)?.let {
            modified.add(itemProcessor.receivedBlockTradingRewardDeprecated(null, it))
        }
        return modified
    }
}
