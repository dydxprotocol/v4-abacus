package exchange.dydx.abacus.tests.payloads

import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerMegavaultHistoricalPnlResponse
import indexer.codegen.IndexerMegavaultPositionResponse
import indexer.codegen.IndexerPerpetualPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionStatus
import indexer.codegen.IndexerPnlTicksResponseObject
import indexer.codegen.IndexerPositionSide
import indexer.codegen.IndexerVaultHistoricalPnl
import indexer.codegen.IndexerVaultPosition
import indexer.codegen.IndexerVaultsHistoricalPnlResponse
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class VaultMocks {

    private val megaVaultPnl = IndexerMegavaultHistoricalPnlResponse(
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
    internal val megaVaultPnlMocks = Json.encodeToString(megaVaultPnl)

    private val btcHistory = IndexerVaultHistoricalPnl(
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
    private val marketPnls = IndexerVaultsHistoricalPnlResponse(
        vaultsPnl = arrayOf(btcHistory),
    )
    internal val vaultMarketPnlsMocks = Json.encodeToString(marketPnls)

    private val btcPosition = IndexerVaultPosition(
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
    private val megaVaultPosition = IndexerMegavaultPositionResponse(
        positions = arrayOf(btcPosition),
    )
    internal val vaultMarketPositionsMocks = Json.encodeToString(megaVaultPosition)
}
