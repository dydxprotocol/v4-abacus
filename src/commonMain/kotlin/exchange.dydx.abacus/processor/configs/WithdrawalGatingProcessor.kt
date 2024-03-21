package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER

@Suppress("UNCHECKED_CAST")
internal class WithdrawalGatingProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val withdrawalGatingKeyMap = mapOf(
        "int" to mapOf(
            "withdrawalsAndTransfersUnblockedAtBlock" to "withdrawalsAndTransfersUnblockedAtBlock",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return transform(existing, payload, withdrawalGatingKeyMap)
    }
}
