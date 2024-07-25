package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainStakingRewardsResponse(
    val rewards: List<OnChainStakingReward>? = null,
    val total: List<OnChainStakingRewardAmount>? = null,
)

@Serializable
data class OnChainStakingReward(
    val validatorAddress: String? = null,
    val reward: List<OnChainStakingRewardAmount>? = null,
)

@Serializable
data class OnChainStakingRewardAmount(
    val denom: String? = null,
    val amount: String? = null,
)
