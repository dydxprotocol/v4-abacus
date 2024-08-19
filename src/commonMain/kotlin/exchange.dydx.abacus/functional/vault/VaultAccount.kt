package exchange.dydx.abacus.functional.vault

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.codegen.IndexerTransferType.DEPOSIT
import indexer.codegen.IndexerTransferType.TRANSFERIN
import indexer.codegen.IndexerTransferType.TRANSFEROUT
import indexer.codegen.IndexerTransferType.WITHDRAWAL
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

val parser = Parser();


@JsExport
@Serializable
data class AccountVaultResponse(
    val address: String? = null,
    val shares: Double? = null,
    val locked_shares: Double? = null,
    val equity: Double? = null,
    val withdrawable_amount: Double? = null,
)


@JsExport
fun getAccountVaultResponse(apiResponse: String): AccountVaultResponse? {
    return parser.asTypedObject<AccountVaultResponse>(apiResponse);
}


@JsExport
fun getTransfersBetweenResponse(apiResponse: String): IndexerTransferBetweenResponse? {
    return parser.asTypedObject<IndexerTransferBetweenResponse>(apiResponse);
}

@JsExport
@Serializable
data class VaultAccount(
    val balanceUsdc: Double?,
    val allTimeReturnUsdc: Double?,
    val vaultTransfers: List<VaultTransfer>?,
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
    DEPOSIT;
}

@JsExport
fun calculateUserVaultInfo(vaultInfo: AccountVaultResponse, vaultTransfers: IndexerTransferBetweenResponse): VaultAccount {
    val presentValue = vaultInfo.equity
    val netTransfers = parser.asDouble(vaultTransfers.totalNetTransfers)
    val allTimeReturn = if (presentValue != null && netTransfers != null) (presentValue - netTransfers) else null

    return VaultAccount(
        balanceUsdc = presentValue,
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
                id = el.id)
            },
    )
}
