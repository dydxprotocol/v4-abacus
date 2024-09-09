package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.codegen.IndexerTransferType.DEPOSIT
import indexer.codegen.IndexerTransferType.TRANSFERIN
import indexer.codegen.IndexerTransferType.TRANSFEROUT
import indexer.codegen.IndexerTransferType.WITHDRAWAL
import kollections.toIList
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class AccountVaultResponse(
    val address: String? = null,
    val shares: Double? = null,
    @Suppress("ConstructorParameterNaming")
    val locked_shares: Double? = null,
    val equity: Double? = null,
    @Suppress("ConstructorParameterNaming")
    val withdrawable_amount: Double? = null,
)

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
)

@JsExport
@Serializable
data class VaultTransfer(
    val timestampMs: Double?,
    val amountUsdc: Double?,
    val type: VaultTransferType?,
    val id: String?,
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

    fun getAccountVaultResponse(apiResponse: String): AccountVaultResponse? {
        return parser.asTypedObject<AccountVaultResponse>(apiResponse)
    }

    fun getTransfersBetweenResponse(apiResponse: String): IndexerTransferBetweenResponse? {
        return parser.asTypedObject<IndexerTransferBetweenResponse>(apiResponse)
    }

    fun calculateUserVaultInfo(
        vaultInfo: AccountVaultResponse,
        vaultTransfers: IndexerTransferBetweenResponse
    ): VaultAccount {
        val presentValue = vaultInfo.equity
        val netTransfers = parser.asDouble(vaultTransfers.totalNetTransfers)
        val withdrawable = vaultInfo.withdrawable_amount
        val allTimeReturn =
            if (presentValue != null && netTransfers != null) (presentValue - netTransfers) else null

        return VaultAccount(
            balanceUsdc = presentValue,
            balanceShares = vaultInfo.shares,
            lockedShares = vaultInfo.locked_shares,
            withdrawableUsdc = withdrawable,
            allTimeReturnUsdc = allTimeReturn,
            totalVaultTransfersCount = vaultTransfers.totalResults,
            vaultTransfers = vaultTransfers.transfersSubset?.map { el ->
                VaultTransfer(
                    timestampMs = parser.asDouble(el.createdAt),
                    amountUsdc = parser.asDouble(el.size),
                    type = when (el.type) {
                        TRANSFEROUT -> VaultTransferType.DEPOSIT
                        TRANSFERIN -> VaultTransferType.WITHDRAWAL
                        DEPOSIT, WITHDRAWAL, null -> null
                    },
                    id = el.id,
                )
            }?.toIList(),
        )
    }
}
