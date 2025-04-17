package exchange.dydx.abacus.output.account

import kollections.JsExport
import kotlinx.serialization.Serializable

/*
debit and credit info are set depending on the type of transfer
*/
@JsExport
@Serializable
data class SubaccountTransfer(
    val id: String,
    val type: TransferRecordType,
    val asset: String?,
    val amount: Double?,
    val updatedAtBlock: Int?,
    val updatedAtMilliseconds: Double,
    val fromAddress: String?,
    val toAddress: String?,
    val transactionHash: String?,
    val resources: SubaccountTransferResources,
)

/*
typeStringKey, statusStringKey, iconLocal and indicator are set to optional, in case
BE returns new transfer type enum values or status enum values which Abacus doesn't recognize
*/
@JsExport
@Serializable
data class SubaccountTransferResources(
    val typeString: String?,
    val statusString: String?,
    val typeStringKey: String?,
    val blockExplorerUrl: String?,
    val statusStringKey: String?,
    val iconLocal: String?,
    val indicator: String?,
)

@JsExport
@Serializable
enum class TransferRecordType(val rawValue: String) {
    DEPOSIT("DEPOSIT"),
    WITHDRAW("WITHDRAWAL"),
    TRANSFER_IN("TRANSFER_IN"),
    TRANSFER_OUT("TRANSFER_OUT");

    companion object {
        operator fun invoke(rawValue: String?) =
            TransferRecordType.entries.firstOrNull { it.rawValue == rawValue }
    }
}
