package exchange.dydx.abacus.functional.vault
import exchange.dydx.abacus.protocols.asTypedObject
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionResponseObject

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class IndexerHistoricalPnl(
    val equity: String? = null,
    val totalPnl: String? = null,
    val netTransfers: String? = null,
    val createdAt: String? = null,
    val blockHeight: String? = null,
    val blockTime: String? = null
)

@JsExport
@Serializable
data class IndexerVaultHistoricalPnlResponse(
    val vaultOfVaultsPnl: List<IndexerHistoricalPnl>? = null
)

@JsExport
@Serializable
data class IndexerVaultHistoricalPnl(
    val marketId: String? = null,
    val historicalPnl: List<IndexerHistoricalPnl>? = null
)
@JsExport
@Serializable
data class IndexerSubvaultHistoricalPnlResponse(
    val vaultsPnl: List<IndexerVaultHistoricalPnl>? = null
)

@JsExport
fun getVaultHistoricalPnlResponse(apiResponse: String): IndexerVaultHistoricalPnlResponse? {
    return parser.asTypedObject<IndexerVaultHistoricalPnlResponse>(apiResponse);
}

@JsExport
fun getSubvaultHistoricalPnlResponse(apiResponse: String): IndexerSubvaultHistoricalPnlResponse? {
    return parser.asTypedObject<IndexerSubvaultHistoricalPnlResponse>(apiResponse);
}

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
    val positions: List<IndexerVaultPosition>? = null
)

@JsExport
fun getVaultPositionsResponse(apiResponse: String): IndexerVaultPositionResponse? {
    return parser.asTypedObject<IndexerVaultPositionResponse>(apiResponse)
}


@JsExport
@Serializable
data class VaultDetails(
    val totalValue: Double? = null,
    val thirtyDayReturnPercent: Double? = null,
    val history: List<VaultHistoryEntry>? = null
)

@JsExport
@Serializable
data class VaultPositions(
    val positions: List<VaultPosition>? = null,
)

@JsExport
@Serializable
data class VaultHistoryEntry(
    val date: Int? = null,
    val equity: Double? = null,
    val totalPnl: Double? = null
)

@JsExport
@Serializable
data class VaultPosition(
    val assetId: String? = null,
    val marketId: String? = null,
    val marginUsdc: Double? = null,
    val currentLeverageMultiple: Double? = null,
    val currentPosition: CurrentPosition? = null,
    val thirtyDayPnl: ThirtyDayPnl? = null
)


@JsExport
@Serializable
data class CurrentPosition(
    val asset: Double? = null,
    val usdc: Double? = null
)

@JsExport
@Serializable
data class ThirtyDayPnl(
    val percent: Double? = null,
    val absolute: Double? = null,
    val sparklinePoints: List<Double>? = null
)

@JsExport
fun calculateVaultSummary(historical: IndexerVaultHistoricalPnlResponse?): VaultDetails? {
    if (historical == null) {
        return null
    }

}


@JsExport
fun calculateVaultPositions(positions: IndexerVaultPositionResponse?, histories: IndexerSubvaultHistoricalPnlResponse?): VaultPositions? {
    if (positions?.positions == null) {
        return null
    }

    val historiesMap = histories?.vaultsPnl?.associateBy { it.marketId }

    return VaultPositions(positions = positions.positions.map { calculateVaultPosition(it, historiesMap?.get(it.market)) })
}

fun calculateVaultPosition(position: IndexerVaultPosition, history: IndexerVaultHistoricalPnl?): VaultPosition {
    return VaultPosition(
        assetId = "BTC",
        marketId = "BTC-USD",
        marginUsdc = 10000.0,
        currentLeverageMultiple = 2.5,
        currentPosition = CurrentPosition(
            asset = 0.5,
            usdc = 15000.0
        ),
        thirtyDayPnl = ThirtyDayPnl(
            percent = 5.2,
            absolute = 520.0,
            sparklinePoints = listOf(1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.2)
        )
    )
}



