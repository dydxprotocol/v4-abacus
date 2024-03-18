package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class TriggerOrdersInputCalculator(val parser: ParserProtocol) {
    internal fun calculate(
        state: Map<String, Any>
    ): Map<String, Any> {
        return state
    }
}
