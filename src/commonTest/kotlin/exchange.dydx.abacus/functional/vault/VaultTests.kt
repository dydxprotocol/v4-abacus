package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionStatus
import indexer.codegen.IndexerPositionSide
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.codegen.IndexerTransferResponseObject
import indexer.codegen.IndexerTransferType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.days

class VaultTests {

    @Test
    fun calculateVaultSummary_basic() {
        val historicalPnl = IndexerVaultHistoricalPnlResponse(
            vaultOfVaultsPnl = listOf(
                IndexerHistoricalPnl(
                    equity = "10000.0",
                    totalPnl = "1000.0",
                    netTransfers = "0.0",
                    createdAt = "1659465600000"
                ),
                IndexerHistoricalPnl(
                    equity = "5000.0",
                    totalPnl = "500",
                    netTransfers = "0.0",
                    createdAt = "1659379200000"
                )
            )
        )

        val vaultDetails = calculateVaultSummary(historicalPnl)

        val expectedVaultDetails = VaultDetails(
            totalValue = 10000.0,
            thirtyDayReturnPercent = 0.1,
            history = listOf(
                VaultHistoryEntry(
                    date = 1659465600000.0,
                    equity = 10000.0,
                    totalPnl = 1000.0
                ),
                VaultHistoryEntry(
                    date = 1659379200000.0,
                    equity = 5000.0,
                    totalPnl = 500.0
                )
            )
        )

        assertEquals(expectedVaultDetails, vaultDetails)
    }

    @Test
    fun shouldReturnNullForNullOrEmptyHistoricalPnl() {
        val nullHistoricalPnl = IndexerVaultHistoricalPnlResponse(vaultOfVaultsPnl = null)
        val emptyHistoricalPnl = IndexerVaultHistoricalPnlResponse(vaultOfVaultsPnl = emptyList())

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

        val historicalPnl = IndexerVaultHistoricalPnlResponse(
            vaultOfVaultsPnl = listOf(
                IndexerHistoricalPnl(
                    equity = "10000.0",
                    totalPnl = "1000.0",
                    netTransfers = "0.0",
                    createdAt = latestTimestamp.toString()
                ),
                IndexerHistoricalPnl(
                    equity = "9700.0",
                    totalPnl = "700.0",
                    netTransfers = "0.0",
                    createdAt = twentyNineDaysAgoTimestamp.toString()
                ),
                IndexerHistoricalPnl(
                    equity = "9500.0",
                    totalPnl = "500.0",
                    netTransfers = "0.0",
                    createdAt = thirtyDaysAgoTimestamp.toString()
                ),
                IndexerHistoricalPnl(
                    equity = "9300.0",
                    totalPnl = "300.0",
                    netTransfers = "0.0",
                    createdAt = thirtyOneDaysAgoTimestamp.toString()
                ),
                IndexerHistoricalPnl(
                    equity = "9000.0",
                    totalPnl = "0.0",
                    netTransfers = "0.0",
                    createdAt = (thirtyDaysAgoTimestamp - 7.days.inWholeMilliseconds).toString()
                )
            )
        )

        val vaultDetails = calculateVaultSummary(historicalPnl)

        assertNotNull(vaultDetails)
        assertEquals(0.05263157894736842, vaultDetails.thirtyDayReturnPercent)
    }

    @Test
    fun shouldCalculateVaultPositionCorrectly() {
        val position = IndexerVaultPosition(
            market = "BTC-USD",
            assetPosition = IndexerAssetPositionResponseObject(
                symbol = "USDC",
                side = IndexerPositionSide.SHORT,
                size = "40000.0",
                assetId = "USDC",
                subaccountNumber = NUM_PARENT_SUBACCOUNTS
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
                subaccountNumber = NUM_PARENT_SUBACCOUNTS
            ),
            equity = "15000.0"
        )

        val history = IndexerVaultHistoricalPnl(
            marketId = "BTC-USD",
            historicalPnl = listOf(
                IndexerHistoricalPnl(
                    equity = "10500.0",
                    totalPnl = "500.0",
                    netTransfers = "0.0",
                    createdAt = "1659465600000"
                ),
                IndexerHistoricalPnl(
                    equity = "10000.0",
                    totalPnl = "0.0",
                    netTransfers = "0.0",
                    createdAt = "1659379200000"
                ),
            )
        )

        val market =  PerpetualMarket(
                id = "BTC-USD",
                assetId = "BTC",
                market = "BTC-USD",
                displayId = null,
                oraclePrice = 55000.0,
                marketCaps = null,
                priceChange24H = null,
                priceChange24HPercent = null,
                status = null,
                configs = null,
                perpetual = null
            )

        val vaultPosition = calculateVaultPosition(position, history, market)

        val expectedVaultPosition = VaultPosition(
            marketId = "BTC-USD",
            marginUsdc = 15000.0,
            currentLeverageMultiple = 55.0 / 15.0,
            currentPosition = CurrentPosition(
                asset = 1.0,
                usdc = 55000.0
            ),
            thirtyDayPnl = ThirtyDayPnl(
                percent = 0.05,
                absolute = 500.0,
                sparklinePoints = listOf(0.0, 500.0)
            )
        )

        assertEquals(expectedVaultPosition, vaultPosition)
    }

}
