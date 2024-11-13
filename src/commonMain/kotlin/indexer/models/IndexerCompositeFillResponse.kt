package indexer.models

import kotlinx.serialization.Serializable

@Serializable
data class IndexerCompositeFillResponse(
    val pageSize: kotlin.Int? = null,
    val totalResults: kotlin.Int? = null,
    val offset: kotlin.Int? = null,
    val fills: kotlin.Array<IndexerCompositeFillObject>? = null
)
