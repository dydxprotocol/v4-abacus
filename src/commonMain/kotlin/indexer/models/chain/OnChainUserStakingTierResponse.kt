package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainUserStakingTierResponse(
    val feeTierName: String? = null,
    val stakedBaseTokens: String? = null,
    val discountPpm: Double? = null,
)
