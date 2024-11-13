package indexer.models

import indexer.codegen.IndexerFillType
import indexer.codegen.IndexerIsoString
import indexer.codegen.IndexerLiquidity
import indexer.codegen.IndexerMarketType
import indexer.codegen.IndexerOrderSide
import kotlinx.serialization.Serializable

@Serializable
data class IndexerCompositeFillObject(

    // Copied from IndexerFillResponseObject
    val id: kotlin.String? = null,
    val side: IndexerOrderSide? = null,
    val liquidity: IndexerLiquidity? = null,
    val type: IndexerFillType? = null,
    val market: kotlin.String? = null,
    val marketType: IndexerMarketType? = null,
    val price: kotlin.String? = null,
    val size: kotlin.String? = null,
    val fee: kotlin.String? = null,
    val affiliateRevShare: kotlin.String? = null,
    val createdAt: IndexerIsoString? = null,
    val createdAtHeight: kotlin.String? = null,
    val orderId: kotlin.String? = null,
    val clientMetadata: kotlin.String? = null,
    val subaccountNumber: kotlin.Int? = null,

    // Additional fields for WS
    val ticker: kotlin.String? = null,
)
