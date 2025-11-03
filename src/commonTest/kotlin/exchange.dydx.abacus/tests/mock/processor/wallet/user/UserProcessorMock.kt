package exchange.dydx.abacus.tests.mock.processor.wallet.user

import exchange.dydx.abacus.processor.wallet.user.UserProcessorProtocol
import exchange.dydx.abacus.state.InternalUserState
import indexer.models.chain.OnChainUserFeeTier
import indexer.models.chain.OnChainUserStakingTierResponse
import indexer.models.chain.OnChainUserStatsResponse

internal class UserProcessorMock : UserProcessorProtocol {
    var processOnChainUserFeeTierCallCount = 0
    var processOnChainUserFeeTierAction: ((existing: InternalUserState?, payload: OnChainUserFeeTier?) -> InternalUserState)? = null
    var processOnChainUserStatsCallCount = 0
    var processOnChainUserStatsAction: ((existing: InternalUserState?, payload: OnChainUserStatsResponse?) -> InternalUserState)? = null
    var processOnChainUserStakingTierCallCount = 0
    var processOnChainUserStakingTierAction: ((existing: InternalUserState?, payload: OnChainUserStakingTierResponse?) -> InternalUserState)? = null

    override fun processOnChainUserFeeTier(
        existing: InternalUserState?,
        payload: OnChainUserFeeTier?
    ): InternalUserState {
        processOnChainUserFeeTierCallCount++
        return processOnChainUserFeeTierAction?.invoke(existing, payload) ?: InternalUserState()
    }

    override fun processOnChainUserStats(
        existing: InternalUserState?,
        payload: OnChainUserStatsResponse?
    ): InternalUserState {
        processOnChainUserStatsCallCount++
        return processOnChainUserStatsAction?.invoke(existing, payload) ?: InternalUserState()
    }

    override fun processOnChainUserStakingTier(
        existing: InternalUserState?,
        payload: OnChainUserStakingTierResponse?
    ): InternalUserState {
        processOnChainUserStakingTierCallCount++
        return processOnChainUserStakingTierAction?.invoke(existing, payload) ?: InternalUserState()
    }
}
