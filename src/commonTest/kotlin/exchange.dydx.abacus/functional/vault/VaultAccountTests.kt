package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.functional.vault.VaultAccountCalculator.calculateUserVaultInfo
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.codegen.IndexerTransferResponseObject
import indexer.codegen.IndexerTransferType
import kollections.iListOf
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

// transformation is pretty simple, so we just test basic input+output
class VaultAccountTests {

    @Test
    fun calculateUserVaultInfo_basic() {
        val vaultInfo = AccountVaultResponse(
            address = "0x123",
            shares = NumShares(numShares = 100.0),
            shareUnlocks = arrayOf(ShareUnlock(unlockBlockHeight = 0.0, shares = NumShares(numShares = 50.0))),
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
                ),
                IndexerTransferResponseObject(
                    id = "2",
                    createdAt = Instant.fromEpochMilliseconds(1659552000000).toString(),
                    size = "2000.0",
                    type = IndexerTransferType.TRANSFER_IN,
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
                ),
                VaultTransfer(
                    timestampMs = 1659552000000.0,
                    amountUsdc = 2000.0,
                    type = VaultTransferType.WITHDRAWAL,
                    id = "2",
                ),
            ),
        )

        assertEquals(expectedVaultAccount, vaultAccount)
    }
}
