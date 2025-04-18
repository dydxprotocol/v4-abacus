package exchange.dydx.abacus.processor.wallet.user

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalUserState
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
}
