package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER

internal class LimiterCapacityProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "limiter.period.seconds" to "seconds",
            "capacity" to "capacity",
            "limiter.baselineMinimum" to "baselineMinimum",
        ),
        "double" to mapOf(
            "limiter.period.nanos" to "nanos",
            "limiter.baselineTvlPpm" to "baselineTvlPpm",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return transform(existing, payload, keyMap)
    }
}