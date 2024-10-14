package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.functional.vault.VaultCalculator.calculateVaultPosition
import exchange.dydx.abacus.functional.vault.VaultCalculator.calculateVaultSummary
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerMegavaultHistoricalPnlResponse
import indexer.codegen.IndexerPerpetualPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionStatus
import indexer.codegen.IndexerPnlTicksResponseObject
import indexer.codegen.IndexerPositionSide
import indexer.codegen.IndexerVaultHistoricalPnl
import indexer.codegen.IndexerVaultPosition
import kollections.iListOf
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.days

class VaultTests {

    @Test
    fun calculateVaultSummary_basic() {
        val historicalPnl = IndexerMegavaultHistoricalPnlResponse(
            megavaultPnl = arrayOf(
                IndexerPnlTicksResponseObject(
                    equity = "10000.0",
                    totalPnl = "1000.0",
                    netTransfers = "0.0",
                    createdAt = Instant.fromEpochMilliseconds(1659465600000).toString(),
                ),
                IndexerPnlTicksResponseObject(
                    equity = "5000.0",
                    totalPnl = "500",
                    netTransfers = "0.0",
                    createdAt = Instant.fromEpochMilliseconds(1659379200000).toString(),
                ),
            ),
        )

        val vaultDetails = calculateVaultSummary(historicalPnl)

        val expectedVaultDetails = VaultDetails(
            totalValue = 10000.0,
            thirtyDayReturnPercent = 0.1 * 365,
            history = iListOf(
                VaultHistoryEntry(
                    date = 1659465600000.0,
                    equity = 10000.0,
                    totalPnl = 1000.0,
                ),
                VaultHistoryEntry(
                    date = 1659379200000.0,
                    equity = 5000.0,
                    totalPnl = 500.0,
                ),
            ),
        )

        assertEquals(expectedVaultDetails, vaultDetails)
    }

    @Test
    fun shouldReturnNullForNullOrEmptyHistoricalPnl() {
        val nullHistoricalPnl = IndexerMegavaultHistoricalPnlResponse(megavaultPnl = null)
        val emptyHistoricalPnl = IndexerMegavaultHistoricalPnlResponse(megavaultPnl = arrayOf())

        val nullVaultDetails = calculateVaultSummary(nullHistoricalPnl)
        val emptyVaultDetails = calculateVaultSummary(emptyHistoricalPnl)

        assertEquals(null, nullVaultDetails)
        assertEquals(null, emptyVaultDetails)
    }

    @Test
    fun shouldCalculate30DayReturnCorrectly() {
        val latestTimestamp = 1659465600000L
        val thirtyOneDaysAgoTimestamp = latestTimestamp - 31.days.inWholeMilliseconds
        val thirtyDaysAgoTimestamp = latestTimestamp - 30.days.inWholeMilliseconds
        val twentyNineDaysAgoTimestamp = latestTimestamp - 29.days.inWholeMilliseconds

        val historicalPnl = IndexerMegavaultHistoricalPnlResponse(
            megavaultPnl = arrayOf(
                IndexerPnlTicksResponseObject(
                    equity = "10000.0",
                    totalPnl = "1000.0",
                    netTransfers = "0.0",
                    createdAt = Instant.fromEpochMilliseconds(latestTimestamp).toString(),
                ),
                IndexerPnlTicksResponseObject(
                    equity = "9700.0",
                    totalPnl = "700.0",
                    netTransfers = "0.0",
                    createdAt = Instant.fromEpochMilliseconds(twentyNineDaysAgoTimestamp).toString(),
                ),
                IndexerPnlTicksResponseObject(
                    equity = "9500.0",
                    totalPnl = "500.0",
                    netTransfers = "0.0",
                    createdAt = Instant.fromEpochMilliseconds(thirtyDaysAgoTimestamp).toString(),
                ),
                IndexerPnlTicksResponseObject(
                    equity = "9300.0",
                    totalPnl = "300.0",
                    netTransfers = "0.0",
                    createdAt = Instant.fromEpochMilliseconds(thirtyOneDaysAgoTimestamp).toString(),
                ),
                IndexerPnlTicksResponseObject(
                    equity = "9000.0",
                    totalPnl = "0.0",
                    netTransfers = "0.0",
                    createdAt = Instant.fromEpochMilliseconds((thirtyDaysAgoTimestamp - 7.days.inWholeMilliseconds)).toString(),
                ),
            ),
        )

        val vaultDetails = calculateVaultSummary(historicalPnl)

        assertNotNull(vaultDetails)
        assertEquals(0.6403508771929824, vaultDetails.thirtyDayReturnPercent)
    }

    @Test
    fun shouldCalculateVaultPositionCorrectly() {
        val position = IndexerVaultPosition(
            ticker = "BTC-USD",
            assetPosition = IndexerAssetPositionResponseObject(
                symbol = "USDC",
                side = IndexerPositionSide.SHORT,
                size = "40000.0",
                assetId = "0",
                subaccountNumber = NUM_PARENT_SUBACCOUNTS,
            ),
            perpetualPosition = IndexerPerpetualPositionResponseObject(
                market = "BTC-USD",
                status = IndexerPerpetualPositionStatus.OPEN,
                side = IndexerPositionSide.LONG,
                size = "1.0",
                maxSize = null,
                entryPrice = "50000.0",
                realizedPnl = null,
                createdAt = "2023-08-01T00:00:00Z",
                createdAtHeight = "1000",
                sumOpen = null,
                sumClose = null,
                netFunding = null,
                unrealizedPnl = "5000.0",
                closedAt = null,
                exitPrice = null,
                subaccountNumber = NUM_PARENT_SUBACCOUNTS,
            ),
            equity = "15000.0",
        )

        val history = IndexerVaultHistoricalPnl(
            ticker = "BTC-USD",
            historicalPnl = arrayOf(
                IndexerPnlTicksResponseObject(
                    id = "1",
                    equity = "10500.0",
                    totalPnl = "500.0",
                    netTransfers = "0.0",
                    createdAt = Instant.fromEpochMilliseconds(1659465600000).toString(),
                ),
                IndexerPnlTicksResponseObject(
                    id = "2",
                    equity = "10000.0",
                    totalPnl = "0.0",
                    netTransfers = "0.0",
                    createdAt = Instant.fromEpochMilliseconds(1659379200000).toString(),
                ),
            ),
        )

        val market = PerpetualMarket(
            id = "BTC-USD",
            assetId = "0",
            market = "BTC-USD",
            displayId = null,
            oraclePrice = 55000.0,
            marketCaps = null,
            priceChange24H = null,
            priceChange24HPercent = null,
            status = null,
            configs = null,
            perpetual = null,
        )

        val vaultPosition = calculateVaultPosition(position, history, market)

        val expectedVaultPosition = VaultPosition(
            marketId = "BTC-USD",
            marginUsdc = 15000.0,
            currentLeverageMultiple = 55.0 / 15.0,
            currentPosition = CurrentPosition(
                asset = 1.0,
                usdc = 55000.0,
            ),
            thirtyDayPnl = ThirtyDayPnl(
                percent = 0.05,
                absolute = 500.0,
                sparklinePoints = iListOf(0.0, 500.0),
            ),
        )

        assertEquals(expectedVaultPosition, vaultPosition)
    }
}
