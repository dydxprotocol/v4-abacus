package exchange.dydx.abacus.functional.vault
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.v2.SubaccountCalculatorV2
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.processor.wallet.account.AssetPositionProcessor
import exchange.dydx.abacus.processor.wallet.account.PerpetualPositionProcessor
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountCalculated
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.utils.IMap
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionResponseObject
import kotlinx.datetime.Clock

import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

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
    return parser.asTypedObject<IndexerVaultHistoricalPnlResponse>(apiResponse)
}

@JsExport
fun getSubvaultHistoricalPnlResponse(apiResponse: String): IndexerSubvaultHistoricalPnlResponse? {
    return parser.asTypedObject<IndexerSubvaultHistoricalPnlResponse>(apiResponse)
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
    val marketId: String? = null,
    val marginUsdc: Double? = null,
    val equityUsdc: Double? = null,
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
    } ?: history.last()

    val totalValue = latestEntry.equity ?: 0.0

    val latestTotalPnl = latestEntry.totalPnl ?: 0.0
    val thirtyDaysAgoTotalPnl = thirtyDaysAgoEntry?.totalPnl ?: 0.0

    val pnlDifference = latestTotalPnl - thirtyDaysAgoTotalPnl
    val thirtyDaysAgoEquity = thirtyDaysAgoEntry.equity ?: 0.0
    val thirtyDayReturnPercent = if (thirtyDaysAgoEquity != 0.0) {
        (pnlDifference / thirtyDaysAgoEquity)
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
fun calculateVaultPositions(positions: IndexerVaultPositionResponse?,
                            histories: IndexerSubvaultHistoricalPnlResponse?,
                            markets: IMap<String, PerpetualMarket>?): VaultPositions? {
    if (positions?.positions == null) {
        return null
    }

    val historiesMap = histories?.vaultsPnl?.associateBy { it.marketId }

    return VaultPositions(positions = positions.positions.mapNotNull { calculateVaultPosition(it, historiesMap?.get(it.market), markets?.get(it.market)) })
}

fun calculateVaultPosition(position: IndexerVaultPosition, history: IndexerVaultHistoricalPnl?, perpetualMarket: PerpetualMarket?): VaultPosition? {
    if (position.market == null) {
        return null
    }
    val perpetualPosition = PerpetualPositionProcessor(parser, null).process(null, position.perpetualPosition)
    val assetPosition = AssetPositionProcessor(parser).process(position.assetPosition)
    val calculator = SubaccountCalculatorV2(parser)

    val assetPositionsMap = assetPosition?.let { mapOf((it.assetId ?: "") to it) }
    val subaccount = calculator.calculate(
        subaccount = InternalSubaccountState(
            equity = parser.asDouble(position.equity) ?: 0.0,
            assetPositions = assetPositionsMap,
            positions = perpetualPosition?.let { mapOf((it.market ?: "") to it) },
            subaccountNumber = 0,
            calculated = mutableMapOf(CalculationPeriod.current to
                    InternalSubaccountCalculated(quoteBalance = calculator.calculateQuoteBalance(assetPositionsMap)))
        ),
        marketsSummary = InternalMarketSummaryState(markets = mutableMapOf(position.market to InternalMarketState(perpetualMarket = perpetualMarket))),
        periods = setOf(CalculationPeriod.current),
        price = null,
        configs = null,
    )
    val calculated = subaccount?.calculated?.get(CalculationPeriod.current)
    val perpCalculated = perpetualPosition?.calculated?.get(CalculationPeriod.current)
    return VaultPosition(
        marketId = position.market,
        marginUsdc = subaccount?.equity,
        currentLeverageMultiple = perpCalculated?.leverage,
        currentPosition = CurrentPosition(
            asset = perpCalculated?.size,
            usdc = perpCalculated?.notionalTotal,
        ),
        thirtyDayPnl = calculateThirtyDayPnl(history)
    )
}

fun calculateThirtyDayPnl(vaultHistoricalPnl: IndexerVaultHistoricalPnl?): ThirtyDayPnl? {
    val historicalPnl = vaultHistoricalPnl?.historicalPnl ?: return null

    if (historicalPnl.isEmpty()) {
        return null
    }

    val sortedPnl = historicalPnl.sortedByDescending { parser.asLong(it.createdAt) }
    val latestEntry = sortedPnl.first()
    val latestTime = parser.asLong(latestEntry.createdAt) ?: Clock.System.now().toEpochMilliseconds()
    val thirtyDaysAgoTime = latestTime - 30.days.inWholeMilliseconds

    val thirtyDaysAgoEntry = sortedPnl.find {
        (parser.asLong(it.createdAt) ?: Long.MAX_VALUE) <= thirtyDaysAgoTime
    } ?: sortedPnl.last()

    val latestTotalPnl = parser.asDouble(latestEntry.totalPnl) ?: 0.0
    val thirtyDaysAgoTotalPnl = parser.asDouble(thirtyDaysAgoEntry?.totalPnl) ?: 0.0
    val absolutePnl = latestTotalPnl - thirtyDaysAgoTotalPnl

    val thirtyDaysAgoEquity = parser.asDouble(thirtyDaysAgoEntry.equity) ?: 0.0
    val percentPnl = if (thirtyDaysAgoEquity != 0.0) {
        (absolutePnl / thirtyDaysAgoEquity) * 100
    } else {
        0.0
    }

    val sparklinePoints = sortedPnl
        .takeWhile { (parser.asLong(it.createdAt) ?: Long.MAX_VALUE) >= thirtyDaysAgoTime }
        .groupBy { entry ->
            val timestamp = parser.asLong(entry.createdAt) ?: 0L
            timestamp.milliseconds.inWholeDays
        }
        .mapValues { (_, entries) ->
            parser.asDouble(entries.first().totalPnl) ?: 0.0
        }
        .toList()
        .sortedBy { (day, _) -> day }
        .map { (_, value) -> value }

    return ThirtyDayPnl(
        percent = percentPnl,
        absolute = absolutePnl,
        sparklinePoints = sparklinePoints
    )
}



