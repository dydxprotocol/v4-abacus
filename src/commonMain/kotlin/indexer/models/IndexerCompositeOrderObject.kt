package indexer.models

import indexer.codegen.IndexerAPIOrderStatus
import indexer.codegen.IndexerAPITimeInForce
import indexer.codegen.IndexerIsoString
import indexer.codegen.IndexerOrderSide
import indexer.codegen.IndexerOrderType
import kotlinx.serialization.Serializable

@Serializable
data class IndexerCompositeOrderObject(

    // Copied from IndexerOrderResponseObject

    val id: kotlin.String? = null,
    val subaccountId: kotlin.String? = null,
    val clientId: kotlin.String? = null,
    val clobPairId: kotlin.String? = null,
    val side: IndexerOrderSide? = null,
    val size: kotlin.String? = null,
    val totalFilled: kotlin.String? = null,
    val price: kotlin.String? = null,
    val type: IndexerOrderType? = null,
    val reduceOnly: kotlin.Boolean? = null,
    val orderFlags: kotlin.String? = null,
    val goodTilBlock: kotlin.String? = null,
    val goodTilBlockTime: kotlin.String? = null,
    val createdAtHeight: kotlin.String? = null,
    val clientMetadata: kotlin.String? = null,
    val triggerPrice: kotlin.String? = null,
    val timeInForce: IndexerAPITimeInForce? = null,
    val status: IndexerAPIOrderStatus? = null,
    val postOnly: kotlin.Boolean? = null,
    val ticker: kotlin.String? = null,
    val updatedAt: IndexerIsoString? = null,
    val updatedAtHeight: kotlin.String? = null,
    val subaccountNumber: kotlin.Double? = null,

    // Additional fields for WS

    val removalReason: kotlin.String? = null,
    val totalOptimisticFilled: kotlin.String? = null,
)
