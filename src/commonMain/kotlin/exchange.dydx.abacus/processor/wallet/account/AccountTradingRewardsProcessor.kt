package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class AccountTradingRewardsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val historicalTradingRewardsProcessor =
        HistoricalTradingRewardsProcessor(parser = parser)

    fun receivedTotalTradingRewards(
        existing: Map<String, Any>?,
        payload: Any?,
    ): Map<String, Any> {
        val modified = existing?.mutable() ?: mutableMapOf<String, Any>()
        val totalTradingRewards = parser.asDouble(payload)
        if (totalTradingRewards != null) {
            modified.safeSet("total", totalTradingRewards)
        }
        return modified
    }

    fun recievedHistoricalTradingRewards(
        existing: List<Any>?,
        payload: List<Any>?,
    ): List<Any>? {
        return if (payload != null) {
            historicalTradingRewardsProcessor.received(
                existing,
                payload,
            )
        } else {
            null
        }
    }

    fun recievedBlockTradingReward(
        existing: List<Any>?,
        payload: Any?,
    ): List<Any>? {
        return if (payload != null) {
            historicalTradingRewardsProcessor.receivedBlockTradingReward(
                existing,
                payload,
            )
        } else {
            null
        }
    }
}
