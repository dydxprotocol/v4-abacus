package indexer.models.chain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class OnChainFeeDiscountsResponse(
    val params: List<OnChainFeeDiscountsParams>? = null
)

@Serializable
data class OnChainFeeDiscountsParams(
    val clobPairId: Int,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    /**
     * Percentage of normal fee to charge during the period (in parts per
     * million) 0 = completely free (100% discount) 500000 = charge 50% of normal
     * fee (50% discount) 1000000 = charge 100% of normal fee (no discount)
     */
    val chargePpm: Double? = null,
)
