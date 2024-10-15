package exchange.dydx.abacus.functional.vault
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.v2.SubaccountCalculatorV2
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.processor.wallet.account.AssetPositionProcessor
import exchange.dydx.abacus.processor.wallet.account.PerpetualPositionProcessor
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.internalstate.InternalAssetPositionState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalSubaccountCalculated
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalVaultState
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerMegavaultHistoricalPnlResponse
import indexer.codegen.IndexerMegavaultPositionResponse
import indexer.codegen.IndexerVaultHistoricalPnl
import indexer.codegen.IndexerVaultPosition
import indexer.codegen.IndexerVaultsHistoricalPnlResponse
import kollections.toIList
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

@JsExport
@Serializable
data class VaultDetails(
    val totalValue: Double? = null,
    val thirtyDayReturnPercent: Double? = null,
    val history: IList<VaultHistoryEntry>? = null
)

@JsExport
@Serializable
data class VaultPositions(
    val positions: IList<VaultPosition>? = null,
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
    val sparklinePoints: IList<Double>? = null
)

@JsExport
object VaultCalculator {
    private val parser = Parser()
    private val perpetualPositionProcessor = PerpetualPositionProcessor(parser, null)
    private val assetPositionProcessor = AssetPositionProcessor(parser)
    private val subaccountCalculator = SubaccountCalculatorV2(parser)

    fun getVaultHistoricalPnlResponse(apiResponse: String): IndexerMegavaultHistoricalPnlResponse? {
        return parser.asTypedObject<IndexerMegavaultHistoricalPnlResponse>(apiResponse)
    }

    fun getSubvaultHistoricalPnlResponse(apiResponse: String): IndexerVaultsHistoricalPnlResponse? {
        return parser.asTypedObject<IndexerVaultsHistoricalPnlResponse>(apiResponse)
    }

    fun getVaultPositionsResponse(apiResponse: String): IndexerMegavaultPositionResponse? {
        return parser.asTypedObject<IndexerMegavaultPositionResponse>(apiResponse)
    }

    fun calculateVaultSummary(historical: IndexerMegavaultHistoricalPnlResponse?): VaultDetails? {
        if (historical?.megavaultPnl.isNullOrEmpty()) {
            return null
        }

        val vaultOfVaultsPnl =
            historical!!.megavaultPnl!!.sortedByDescending { parser.asDatetime(it.createdAt)?.toEpochMilliseconds() ?: 0 }

        val history = vaultOfVaultsPnl.mapNotNull { entry ->
            parser.asDatetime(entry.createdAt)?.toEpochMilliseconds()?.toDouble()?.let { createdAt ->
                VaultHistoryEntry(
                    date = createdAt,
                    equity = parser.asDouble(entry.equity) ?: 0.0,
                    totalPnl = parser.asDouble(entry.totalPnl) ?: 0.0,
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
        val thirtyDaysAgoTotalPnl = thirtyDaysAgoEntry.totalPnl ?: 0.0

        val pnlDifference = latestTotalPnl - thirtyDaysAgoTotalPnl
        val timeDifferenceMs = if (latestEntry.date != null && thirtyDaysAgoEntry.date != null) {
            latestEntry.date - thirtyDaysAgoEntry.date
        } else {
            0.0
        }
        val thirtyDaysAgoEquity = thirtyDaysAgoEntry.equity ?: 0.0
        val thirtyDayReturnPercent = if (thirtyDaysAgoEquity != 0.0) {
            (pnlDifference / thirtyDaysAgoEquity)
        } else {
            0.0
        }

        return VaultDetails(
            totalValue = totalValue,
            thirtyDayReturnPercent = if (timeDifferenceMs > 0) thirtyDayReturnPercent * 365.days.inWholeMilliseconds / timeDifferenceMs else 0.0,
            history = history.toIList(),
        )
    }

    private fun maybeAddUsdcRow(positions: List<VaultPosition>, vaultTvl: Double?): List<VaultPosition> {
        if (vaultTvl != null) {
            val usdcTotal = vaultTvl - positions.sumOf { it.marginUsdc ?: 0.0 }

            // add a usdc row
            return positions + VaultPosition(
                marketId = "USDC-USD",
                marginUsdc = usdcTotal,
                equityUsdc = usdcTotal,
                currentLeverageMultiple = 1.0,
                currentPosition = CurrentPosition(
                    asset = usdcTotal,
                    usdc = usdcTotal,
                ),
                thirtyDayPnl = ThirtyDayPnl(
                    percent = 0.0,
                    absolute = 0.0,
                    sparklinePoints = null,
                ),
            )
        }
        return positions
    }

    fun calculateVaultPositions(
        positions: IndexerMegavaultPositionResponse?,
        histories: IndexerVaultsHistoricalPnlResponse?,
        markets: IMap<String, PerpetualMarket>?,
        vaultTvl: Double?,
    ): VaultPositions? {
        if (positions?.positions == null) {
            return null
        }

        val historiesMap = histories?.vaultsPnl?.associateBy { it.ticker }

        var processedPositions = positions.positions.mapNotNull {
            calculateVaultPosition(
                it,
                historiesMap?.get(it.ticker),
                markets?.get(it.ticker),
            )
        }

        processedPositions = maybeAddUsdcRow(processedPositions, vaultTvl)

        return VaultPositions(
            positions = processedPositions.toIList(),
        )
    }

    internal fun calculateVaultPositionsInternal(
        vault: InternalVaultState?,
        markets: IMap<String, PerpetualMarket>?
    ): VaultPositions? {
        if (vault?.positions == null) {
            return null
        }

        var positions: List<VaultPosition> = vault.positions.mapNotNull { position ->
            val ticker = position.ticker ?: return@mapNotNull null
            val history = vault.pnls.get(ticker)
            val market = markets?.get(ticker)
            calculateVaultPositionInternal(
                ticker = ticker,
                equity = position.equity,
                perpetualPosition = position.openPosition,
                assetPosition = position.assetPosition,
                thirtyDayPnl = history,
                perpetualMarket = market,
            )
        }

        positions = maybeAddUsdcRow(positions, vault.details?.totalValue)

        return VaultPositions(positions = positions.toIList())
    }

    fun calculateVaultPosition(
        position: IndexerVaultPosition,
        history: IndexerVaultHistoricalPnl?,
        perpetualMarket: PerpetualMarket?
    ): VaultPosition? {
        if (position.ticker != null) {
            val perpetualPosition =
                perpetualPositionProcessor.process(null, position.perpetualPosition)
            val assetPosition = assetPositionProcessor.process(position.assetPosition)
            val thirtyDayPnl = calculateThirtyDayPnl(history)
            return calculateVaultPositionInternal(
                ticker = position.ticker,
                equity = parser.asDouble(position.equity),
                perpetualPosition = perpetualPosition,
                assetPosition = assetPosition,
                thirtyDayPnl = thirtyDayPnl,
                perpetualMarket = perpetualMarket,
            )
        } else {
            return null
        }
    }

    internal fun calculateVaultPositionInternal(
        ticker: String,
        equity: Double?,
        perpetualPosition: InternalPerpetualPosition?,
        assetPosition: InternalAssetPositionState?,
        thirtyDayPnl: ThirtyDayPnl?,
        perpetualMarket: PerpetualMarket?,
    ): VaultPosition {
        val assetPositionsMap = assetPosition?.let { mapOf((it.symbol ?: "") to it) }
        val subaccount = subaccountCalculator.calculate(
            subaccount = InternalSubaccountState(
                equity = equity ?: 0.0,
                assetPositions = assetPositionsMap,
                openPositions = perpetualPosition?.let { mapOf((it.market ?: "") to it) },
                subaccountNumber = 0,
                calculated = mutableMapOf(
                    CalculationPeriod.current to
                        InternalSubaccountCalculated(
                            quoteBalance = subaccountCalculator.calculateQuoteBalance(
                                assetPositionsMap,
                            ),
                        ),
                ),
            ),
            marketsSummary = InternalMarketSummaryState(
                markets = mutableMapOf(
                    ticker to InternalMarketState(
                        perpetualMarket = perpetualMarket,
                    ),
                ),
            ),
            periods = setOf(CalculationPeriod.current),
            price = null,
            configs = null,
        )
        val calculated = subaccount?.calculated?.get(CalculationPeriod.current)
        val perpCalculated = perpetualPosition?.calculated?.get(CalculationPeriod.current)
        return VaultPosition(
            marketId = ticker,
            marginUsdc = calculated?.equity,
            currentLeverageMultiple = perpCalculated?.leverage,
            currentPosition = CurrentPosition(
                asset = perpCalculated?.size,
                usdc = perpCalculated?.notionalTotal,
            ),
            thirtyDayPnl = thirtyDayPnl,
        )
    }

    fun calculateThirtyDayPnl(vaultHistoricalPnl: IndexerVaultHistoricalPnl?): ThirtyDayPnl? {
        val historicalPnl = vaultHistoricalPnl?.historicalPnl ?: return null

        if (historicalPnl.isEmpty()) {
            return null
        }

        val sortedPnl = historicalPnl.sortedByDescending { parser.asDatetime(it.createdAt)?.toEpochMilliseconds() ?: 0 }
        val latestEntry = sortedPnl.first()
        val latestTime = parser.asDatetime(latestEntry.createdAt)?.toEpochMilliseconds() ?: Clock.System.now().toEpochMilliseconds()
        val thirtyDaysAgoTime = latestTime - 30.days.inWholeMilliseconds

        val thirtyDaysAgoEntry = sortedPnl.find {
            (parser.asDatetime(it.createdAt)?.toEpochMilliseconds() ?: Long.MAX_VALUE) <= thirtyDaysAgoTime
        } ?: sortedPnl.last()

        val latestTotalPnl = parser.asDouble(latestEntry.totalPnl) ?: 0.0
        val thirtyDaysAgoTotalPnl = parser.asDouble(thirtyDaysAgoEntry.totalPnl) ?: 0.0
        val absolutePnl = latestTotalPnl - thirtyDaysAgoTotalPnl

        val thirtyDaysAgoEquity = parser.asDouble(thirtyDaysAgoEntry.equity) ?: 0.0
        val percentPnl = if (thirtyDaysAgoEquity != 0.0) {
            (absolutePnl / thirtyDaysAgoEquity)
        } else {
            0.0
        }

        val sparklinePoints = sortedPnl
            .takeWhile { (parser.asDatetime(it.createdAt)?.toEpochMilliseconds() ?: Long.MAX_VALUE) >= thirtyDaysAgoTime }
            .groupBy { entry ->
                val timestamp = parser.asDatetime(entry.createdAt)?.toEpochMilliseconds() ?: 0L
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
            sparklinePoints = sparklinePoints.toIList(),
        )
    }
}
