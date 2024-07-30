package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainFeeTiersResponse(
    val params: OnChainFeeTierParams? = null
)

@Serializable
data class OnChainFeeTierParams(
    val tiers: List<OnChainFeeTier>? = null
)

@Serializable
data class OnChainFeeTier(
    val name: String? = null,
    val absoluteVolumeRequirement: String? = null,
    val totalVolumeShareRequirementPpm: Double? = null,
    val makerVolumeShareRequirementPpm: Double? = null,
    val makerFeePpm: Double? = null,
    val takerFeePpm: Double? = null,
)
