package indexer.models.chain

import kotlinx.serialization.Serializable

//
// getUserFeeTier Response
//
@Serializable
data class OnChainUserFeeTierResponse(
    val index: Int? = null,
    val tier: OnChainUserFeeTier? = null
)

@Serializable
data class OnChainUserFeeTier(
    val name: String? = null,
    val absoluteVolumeRequirement: String? = null,
    val totalVolumeShareRequirementPpm: Double? = null,
    val makerVolumeShareRequirementPpm: Double? = null,
    val makerFeePpm: Double? = null,
    val takerFeePpm: Double? = null,
)
