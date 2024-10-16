package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.functional.vault.VaultAccountCalculator.calculateUserVaultInfo
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.codegen.IndexerTransferResponseObject
import indexer.codegen.IndexerTransferType
import indexer.models.chain.OnChainAccountVaultResponse
import indexer.models.chain.OnChainNumShares
import indexer.models.chain.OnChainShareUnlock
import kollections.iListOf
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

// transformation is pretty simple, so we just test basic input+output
class VaultAccountTests {

    @Test
    fun calculateUserVaultInfo_basic() {
        val vaultInfo = OnChainAccountVaultResponse(
            address = "0x123",
            shares = OnChainNumShares(numShares = 100.0),
            shareUnlocks = arrayOf(OnChainShareUnlock(unlockBlockHeight = 0.0, shares = OnChainNumShares(numShares = 50.0))),
            equity = 10000.0 * 1_000_000,
            withdrawableEquity = 5000.0 * 1_000_000,
        )

        val vaultTransfers = IndexerTransferBetweenResponse(
            totalResults = 2,
            totalNetTransfers = "4000.0",
            transfersSubset = arrayOf(
                IndexerTransferResponseObject(
                    id = "1",
                    createdAt = Instant.fromEpochMilliseconds(1659465600000).toString(),
                    size = "6000.0",
                    type = IndexerTransferType.TRANSFER_OUT,
                    transactionHash = "tx1",
                ),
                IndexerTransferResponseObject(
                    id = "2",
                    createdAt = Instant.fromEpochMilliseconds(1659552000000).toString(),
                    size = "2000.0",
                    type = IndexerTransferType.TRANSFER_IN,
                    transactionHash = "tx2",
                ),
            ),
        )

        val vaultAccount = calculateUserVaultInfo(vaultInfo, vaultTransfers)

        val expectedVaultAccount = VaultAccount(
            balanceUsdc = 10000.0,
            withdrawableUsdc = 5000.0,
            allTimeReturnUsdc = 6000.0,
            totalVaultTransfersCount = 2,
            balanceShares = 100.0,
            lockedShares = 50.0,
            vaultTransfers = iListOf(
                VaultTransfer(
                    timestampMs = 1659465600000.0,
                    amountUsdc = 6000.0,
                    type = VaultTransferType.DEPOSIT,
                    id = "1",
                    transactionHash = "tx1",
                ),
                VaultTransfer(
                    timestampMs = 1659552000000.0,
                    amountUsdc = 2000.0,
                    type = VaultTransferType.WITHDRAWAL,
                    id = "2",
                    transactionHash = "tx2",
                ),
            ),
            vaultShareUnlocks = iListOf(VaultShareUnlock(unlockBlockHeight = 0.0, amountUsdc = 5000.0)),
        )

        assertEquals(expectedVaultAccount, vaultAccount)
    }

    @Test
    fun calculateUserVaultInfo_empty() {
        val vaultTransfers = IndexerTransferBetweenResponse(
            totalResults = 2,
            totalNetTransfers = "-500.0",
            transfersSubset = arrayOf(
                IndexerTransferResponseObject(
                    id = "1",
                    createdAt = Instant.fromEpochMilliseconds(1659465600000).toString(),
                    size = "6000.0",
                    type = IndexerTransferType.TRANSFER_OUT,
                    transactionHash = "tx1",
                ),
                IndexerTransferResponseObject(
                    id = "2",
                    createdAt = Instant.fromEpochMilliseconds(1659552000000).toString(),
                    size = "6500.0",
                    type = IndexerTransferType.TRANSFER_IN,
                    transactionHash = "tx2",
                ),
            ),
        )

        val vaultAccount = calculateUserVaultInfo(null, vaultTransfers)

        val expectedVaultAccount = VaultAccount(
            balanceUsdc = 0.0,
            withdrawableUsdc = 0.0,
            allTimeReturnUsdc = 500.0,
            totalVaultTransfersCount = 2,
            balanceShares = 0.0,
            lockedShares = 0.0,
            vaultTransfers = iListOf(
                VaultTransfer(
                    timestampMs = 1659465600000.0,
                    amountUsdc = 6000.0,
                    type = VaultTransferType.DEPOSIT,
                    id = "1",
                    transactionHash = "tx1",
                ),
                VaultTransfer(
                    timestampMs = 1659552000000.0,
                    amountUsdc = 6500.0,
                    type = VaultTransferType.WITHDRAWAL,
                    id = "2",
                    transactionHash = "tx2",
                ),
            ),
            vaultShareUnlocks = null,
        )

        assertEquals(expectedVaultAccount, vaultAccount)
    }
}
