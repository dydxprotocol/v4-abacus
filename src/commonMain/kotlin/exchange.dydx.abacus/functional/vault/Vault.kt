package exchange.dydx.abacus.functional.vault
import exchange.dydx.abacus.protocols.asTypedObject
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionResponseObject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Duration.Companion.days

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
    val date: Double? = null,
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
    if (historical?.vaultOfVaultsPnl.isNullOrEmpty()) {
        return null
    }

    val vaultOfVaultsPnl = historical!!.vaultOfVaultsPnl!!.sortedByDescending { parser.asDouble(it.createdAt) }

    val history = vaultOfVaultsPnl.mapNotNull { entry ->
        parser.asDouble(entry.createdAt)?.let { createdAt ->
            VaultHistoryEntry(
                date = createdAt,
                equity = parser.asDouble(entry.equity) ?: 0.0,
                totalPnl = parser.asDouble(entry.totalPnl) ?: 0.0
            )
        }
    }

    val latestEntry = history.first()
    val latestTime = latestEntry.date ?: Clock.System.now().toEpochMilliseconds().toDouble()
    val thirtyDaysAgoTime = latestTime - 30.days.inWholeMilliseconds

    val thirtyDaysAgoEntry = history.find {
        (it.date ?: Double.MAX_VALUE) <= thirtyDaysAgoTime
    }

    val totalValue = latestEntry.equity ?: 0.0

    val latestTotalPnl = latestEntry.totalPnl ?: 0.0
    val thirtyDaysAgoTotalPnl = thirtyDaysAgoEntry?.totalPnl ?: 0.0

    val thirtyDayReturnPercent = if (thirtyDaysAgoEntry != null) {
        val pnlDifference = latestTotalPnl - thirtyDaysAgoTotalPnl
        val thirtyDaysAgoEquity = thirtyDaysAgoEntry.equity ?: 0.0
        if (thirtyDaysAgoEquity != 0.0) {
            (pnlDifference / thirtyDaysAgoEquity)
        } else {
            0.0
        }
    } else {
        0.0
    }

    return VaultDetails(
        totalValue = totalValue,
        thirtyDayReturnPercent = thirtyDayReturnPercent,
        history = history
    )
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



