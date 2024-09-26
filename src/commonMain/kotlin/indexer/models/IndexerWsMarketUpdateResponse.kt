package indexer.models

import indexer.codegen.IndexerIsoString
import kotlinx.serialization.Serializable

@Serializable
data class IndexerWsMarketUpdateResponse(
    val trading: Map<String, IndexerCompositeMarketObject>? = null,
    val oraclePrices: Map<String, IndexerWsMarketOraclePriceObject>? = null,
)

@Serializable
data class IndexerWsMarketOraclePriceObject(
    val oraclePrice: kotlin.String? = null,
    val effectiveAt: IndexerIsoString? = null,
    val effectiveAtHeight: kotlin.String? = null,
    val marketId: kotlin.Int? = null,
)
