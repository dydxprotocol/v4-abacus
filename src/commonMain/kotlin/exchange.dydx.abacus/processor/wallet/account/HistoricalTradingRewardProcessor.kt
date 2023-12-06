package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable

import exchange.dydx.abacus.utils.DebugLogger

@Suppress("UNCHECKED_CAST")
internal class HistoricalTradingRewardProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val tradingRewardKeyMap = mapOf(
        "string" to mapOf(
            "period" to "period",
        ),
        "datetime" to mapOf(
            "startedAt" to "startedAt",
            "endedAt" to "endedAt"
        ),
        "double" to mapOf(
            "tradingRewards" to "amount",
        ),
        "int" to mapOf(
            "startedAtHeight" to "startedAtHeight",
            "endedAtHeight" to "endedAtHeight",
            "height" to "height"
        )
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, tradingRewardKeyMap)
    }
}
