package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

@Suppress("UNCHECKED_CAST")
internal class HistoricalTradingRewardProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val tradingRewardKeyMap = mapOf(
        "string" to mapOf(
            "period" to "period",
        ),
        "datetime" to mapOf(
            "startedAt" to "startedAt",
            "endedAt" to "endedAt",
        ),
        "double" to mapOf(
            "tradingReward" to "amount",
        ),
    )

    private val blockTradingRewardKeyMap = mapOf(
        "datetime" to mapOf(
            "createdAt" to "createdAt",
        ),
        "double" to mapOf(
            "tradingReward" to "tradingReward",
        ),
        "int" to mapOf(
            "height" to "createdAtHeight",
            "createdAtHeight" to "createdAtHeight",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, tradingRewardKeyMap)
    }

    fun receivedBlockTradingRewardDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        /*
        To be revised
        Spec uses height, and FE needs both height and createdAt timestamp
        To have constant naming, proposed to use createdAtHeight and createdAt
         */
        return transform(existing, payload, blockTradingRewardKeyMap)
    }
}
