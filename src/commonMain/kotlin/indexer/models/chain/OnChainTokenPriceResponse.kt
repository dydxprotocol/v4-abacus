package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainTokenPriceResponse(
    val marketPrice: OnChainTokenPrice? = null,
)

@Serializable
data class OnChainTokenPrice(
    val price: String? = null,
    val id: Double? = null,
    val exponent: Double? = null,
)
