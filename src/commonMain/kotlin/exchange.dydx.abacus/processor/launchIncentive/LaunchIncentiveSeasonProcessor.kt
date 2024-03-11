package exchange.dydx.abacus.processor.launchIncentive

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class LaunchIncentiveSeasonProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val seasonKeyMap = mapOf(
        "double" to mapOf(
            "startTimestamp" to "startTimestamp",
        ),
        "string" to mapOf(
            "label" to "label",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, seasonKeyMap)
    }
}
