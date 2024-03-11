package exchange.dydx.abacus.processor.launchIncentive

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class LaunchIncentiveSeasonsProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val itemProcessor = LaunchIncentiveSeasonProcessor(parser = parser)

    override fun received(
        existing: List<Any>?,
        payload: List<Any>
    ): List<Any>? {
        val modified = mutableListOf<Map<String, Any>>()
        for (item in payload) {
            parser.asNativeMap(item)?.let { it ->
                itemProcessor.received(null, it)?.let { received ->
                    modified.add(received)
                }
            }
        }
        return modified
    }
}
