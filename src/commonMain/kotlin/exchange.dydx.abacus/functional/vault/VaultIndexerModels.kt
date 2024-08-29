package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.utils.IList
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionResponseObject
import indexer.codegen.IndexerPnlTicksResponseObject
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class IndexerVaultHistoricalPnlResponse(
    val vaultOfVaultsPnl: IList<IndexerPnlTicksResponseObject>? = null
)

@JsExport
@Serializable
data class IndexerVaultHistoricalPnl(
    val marketId: String? = null,
    val historicalPnl: IList<IndexerPnlTicksResponseObject>? = null
)

@JsExport
@Serializable
data class IndexerSubvaultHistoricalPnlResponse(
    val vaultsPnl: IList<IndexerVaultHistoricalPnl>? = null
)

@JsExport
@Serializable
data class IndexerVaultPosition(
    val market: String? = null,
    val assetPosition: IndexerAssetPositionResponseObject? = null,
    val perpetualPosition: IndexerPerpetualPositionResponseObject? = null,
    val equity: String? = null
)

@JsExport
@Serializable
data class IndexerVaultPositionResponse(
    val positions: IList<IndexerVaultPosition>? = null
)
