package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class LaunchIncentivePointProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val pointsKeyMap = mapOf(
        "int" to mapOf(
            "incentivePoints" to "incentivePoints",
            "marketMakingIncentivePoints" to "marketMakingIncentivePoints",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, pointsKeyMap)
    }
}