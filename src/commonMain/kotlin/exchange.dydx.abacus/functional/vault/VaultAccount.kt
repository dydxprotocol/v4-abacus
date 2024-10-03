package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.codegen.IndexerTransferType.DEPOSIT
import indexer.codegen.IndexerTransferType.TRANSFER_IN
import indexer.codegen.IndexerTransferType.TRANSFER_OUT
import indexer.codegen.IndexerTransferType.WITHDRAWAL
import indexer.models.chain.OnChainAccountVaultResponse
import kollections.toIList
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class VaultAccount(
    val balanceUsdc: Double?,
    val balanceShares: Double?,
    val lockedShares: Double?,
    val withdrawableUsdc: Double?,
    val allTimeReturnUsdc: Double?,
    val vaultTransfers: IList<VaultTransfer>?,
    val totalVaultTransfersCount: Int?,
    val vaultShareUnlocks: IList<VaultShareUnlock>?,
) {
    val shareValue: Double?
        get() =
            if (balanceShares != null && balanceUsdc != null && balanceShares > 0) {
                balanceUsdc / balanceShares
            } else {
                null
            }
}

@JsExport
@Serializable
data class VaultTransfer(
    val timestampMs: Double?,
    val amountUsdc: Double?,
    val type: VaultTransferType?,
    val id: String?,
    val transactionHash: String?,
)

@JsExport
@Serializable
data class VaultShareUnlock(
    val unlockBlockHeight: Double?,
    val amountUsdc: Double?,
)

@JsExport
@Serializable
enum class VaultTransferType {
    WITHDRAWAL,
    DEPOSIT
}

@JsExport
object VaultAccountCalculator {
    private val parser = Parser()

    fun getAccountVaultResponse(apiResponse: String): OnChainAccountVaultResponse? {
        return parser.asTypedObject<OnChainAccountVaultResponse>(apiResponse)
    }

    fun getTransfersBetweenResponse(apiResponse: String): IndexerTransferBetweenResponse? {
        return parser.asTypedObject<IndexerTransferBetweenResponse>(apiResponse)
    }

    fun calculateUserVaultInfo(
        vaultInfo: OnChainAccountVaultResponse,
        vaultTransfers: IndexerTransferBetweenResponse,
    ): VaultAccount {
        val presentValue = vaultInfo.equity?.let { it / 1_000_000 }
        val netTransfers = parser.asDouble(vaultTransfers.totalNetTransfers)
        val withdrawable = vaultInfo.withdrawableEquity?.let { it / 1_000_000 }
        val allTimeReturn =
            if (presentValue != null && netTransfers != null) (presentValue - netTransfers) else null

        val impliedShareValue: Double = if (
            vaultInfo.shares?.numShares != null &&
            vaultInfo.shares.numShares > 0 &&
            presentValue != null
        ) {
            presentValue / vaultInfo.shares.numShares
        } else {
            0.0
        }

        return VaultAccount(
            balanceUsdc = presentValue,
            balanceShares = vaultInfo.shares?.numShares,
            lockedShares = vaultInfo.shareUnlocks?.sumOf { el -> el.shares?.numShares ?: 0.0 },
            withdrawableUsdc = withdrawable,
            allTimeReturnUsdc = allTimeReturn,
            totalVaultTransfersCount = vaultTransfers.totalResults,
            vaultTransfers = vaultTransfers.transfersSubset?.map { el ->
                VaultTransfer(
                    timestampMs = parser.asDatetime(el.createdAt)?.toEpochMilliseconds()?.toDouble(),
                    amountUsdc = parser.asDouble(el.size),
                    type = when (el.type) {
                        TRANSFER_OUT -> VaultTransferType.DEPOSIT
                        TRANSFER_IN -> VaultTransferType.WITHDRAWAL
                        DEPOSIT, WITHDRAWAL, null -> null
                    },
                    id = el.id,
                    transactionHash = el.transactionHash,
                )
            }?.toIList(),
            vaultShareUnlocks = vaultInfo.shareUnlocks?.map { el ->
                VaultShareUnlock(
                    unlockBlockHeight = el.unlockBlockHeight,
                    amountUsdc = el.shares?.numShares?.let { it * impliedShareValue },
                )
            }?.sortedBy { it.unlockBlockHeight }?.toIList(),
        )
    }
}
