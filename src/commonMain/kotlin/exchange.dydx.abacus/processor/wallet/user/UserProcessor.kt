package exchange.dydx.abacus.processor.wallet.user

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalUserState
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
import indexer.models.chain.OnChainUserFeeTier
import indexer.models.chain.OnChainUserStatsResponse

internal interface UserProcessorProtocol {
    fun processOnChainUserFeeTier(
        existing: InternalUserState?,
        payload: OnChainUserFeeTier?,
    ): InternalUserState

    fun processOnChainUserStats(
        existing: InternalUserState?,
        payload: OnChainUserStatsResponse?,
    ): InternalUserState
}

internal class UserProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), UserProcessorProtocol {
    private val onchainUserFeeTierKeyMap = mapOf(
        "double" to mapOf(
            "makerFeePpm" to "makerFeePpm",
            "takerFeePpm" to "takerFeePpm",
        ),
        "string" to mapOf(
            "name" to "feeTierId",
        ),
    )

    private val onchainUserFeeStatsKeyMap = mapOf(
        "double" to mapOf(
            "makerNotional" to "makerNotional",
            "takerNotional" to "takerNotional",
        ),
    )

    override fun processOnChainUserFeeTier(
        existing: InternalUserState?,
        payload: OnChainUserFeeTier?,
    ): InternalUserState {
        val internalState = existing ?: InternalUserState()
        internalState.feeTierId = payload?.name
        val makerFeePpm = payload?.makerFeePpm
        if (makerFeePpm != null) {
            internalState.makerFeeRate = makerFeePpm / QUANTUM_MULTIPLIER
        }
        val takerFeePpm = payload?.takerFeePpm
        if (takerFeePpm != null) {
            internalState.takerFeeRate = takerFeePpm / QUANTUM_MULTIPLIER
        }
        return internalState
    }

    fun receivedOnChainUserFeeTierDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val received = transform(existing, payload, onchainUserFeeTierKeyMap)
        val makerFeePpm = parser.asDecimal(received["makerFeePpm"])
        if (makerFeePpm != null) {
            received["makerFeeRate"] = makerFeePpm / QUANTUM_MULTIPLIER
        }
        val takerFeePpm = parser.asDecimal(received["takerFeePpm"])
        if (takerFeePpm != null) {
            received["takerFeeRate"] = takerFeePpm / QUANTUM_MULTIPLIER
        }

        return received
    }

    override fun processOnChainUserStats(
        existing: InternalUserState?,
        payload: OnChainUserStatsResponse?,
    ): InternalUserState {
        val internalState = existing ?: InternalUserState()
        val makerNotional = parser.asDouble(payload?.makerNotional)
        if (makerNotional != null) {
            internalState.makerVolume30D = makerNotional / QUANTUM_MULTIPLIER
        }
        val takerNotional = parser.asDouble(payload?.takerNotional)
        if (takerNotional != null) {
            internalState.takerVolume30D = takerNotional / QUANTUM_MULTIPLIER
        }
        return internalState
    }

    fun receivedOnChainUserStatsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val received = transform(existing, payload, onchainUserFeeStatsKeyMap)
        val makerNotional = parser.asDecimal(received["makerNotional"])
        if (makerNotional != null) {
            received["makerVolume30D"] = makerNotional / QUANTUM_MULTIPLIER
        }
        val takerNotional = parser.asDecimal(received["takerNotional"])
        if (takerNotional != null) {
            received["takerVolume30D"] = takerNotional / QUANTUM_MULTIPLIER
        }
        return received
    }
}
