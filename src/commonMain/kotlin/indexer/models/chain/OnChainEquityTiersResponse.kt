package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainEquityTiersResponse(
    val equityTierLimitConfig: OnChainEquityTiers? = null,
)

@Serializable
data class OnChainEquityTiers(
    val shortTermOrderEquityTiers: List<OnChainEquityTier>? = null,
    val statefulOrderEquityTiers: List<OnChainEquityTier>? = null,
)

@Serializable
data class OnChainEquityTier(
    val usdTncRequired: String? = null,
    val limit: Double? = null,
)
