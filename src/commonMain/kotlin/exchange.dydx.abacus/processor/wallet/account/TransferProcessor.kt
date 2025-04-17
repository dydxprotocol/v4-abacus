package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.output.account.SubaccountTransferResources
import exchange.dydx.abacus.output.account.TransferRecordType
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.BaseProcessorProtocol
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import indexer.codegen.IndexerTransferResponseObject

internal interface TransferProcessorProtocol : BaseProcessorProtocol {
    fun process(payload: IndexerTransferResponseObject?): SubaccountTransfer?
}

internal class TransferProcessor(
    parser: ParserProtocol,
    private val localizer: LocalizerProtocol?,
) : BaseProcessor(parser), TransferProcessorProtocol {
    private val typeMap = mapOf(
        "DEPOSIT" to "APP.GENERAL.DEPOSIT",
        "WITHDRAWAL" to "APP.GENERAL.WITHDRAW",
        "FAST_WITHDRAWAL" to "APP.GENERAL.FAST_WITHDRAW",
        "TRANSFER_OUT" to "APP.GENERAL.TRANSFER_OUT",
        "TRANSFER_IN" to "APP.GENERAL.TRANSFER_IN",
    )

    private val typeIconMap = mapOf(
        "DEPOSIT" to "Incoming",
        "WITHDRAWAL" to "Outgoing",
        "FAST_WITHDRAWAL" to "Outgoing",
        "TRANSFER_OUT" to "Outgoing",
        "TRANSFER_IN" to "Incoming",
    )

    private val statusMap = mapOf(
        "PENDING" to "pending",
        "CONFIRMED" to "confirmed",
    )

    override fun process(
        payload: IndexerTransferResponseObject?,
    ): SubaccountTransfer? {
        val id = payload?.id ?: payload?.transactionHash
        val type = TransferRecordType.invoke(payload?.type?.value)
        val updatedAtMilliseconds = parser.asDatetime(payload?.createdAt)?.toEpochMilliseconds()?.toDouble()
        val toAddress = parser.asString(payload?.recipient?.address)
        val modifiedType = when (type) {
            // For DEPOSIT, we always show as "Deposit"
            TransferRecordType.WITHDRAW -> {
                if (toAddress == accountAddress || toAddress == null) {
                    type
                } else {
                    TransferRecordType.TRANSFER_OUT
                }
            }
            else -> type
        }
        if (payload != null && id != null && modifiedType != null && updatedAtMilliseconds != null) {
            return SubaccountTransfer(
                id = id,
                type = modifiedType,
                asset = payload.symbol,
                amount = parser.asDouble(payload.size),
                updatedAtBlock = parser.asInt(payload.createdAtHeight),
                updatedAtMilliseconds = updatedAtMilliseconds,
                fromAddress = payload.sender?.address,
                toAddress = toAddress,
                transactionHash = payload.transactionHash,
                resources = createResource(
                    type = modifiedType,
                    transactionHash = payload.transactionHash,
                ),
            )
        } else {
            return null
        }
    }

    private fun createResource(
        type: TransferRecordType,
        transactionHash: String?
    ): SubaccountTransferResources {
        val mintscan = transactionHash?.let {
            environment?.links?.mintscan?.replace("{tx_hash}", it)
        }
        val typeStringKey = typeMap[type.rawValue]
        return SubaccountTransferResources(
            typeStringKey = typeStringKey,
            typeString = typeStringKey?.let { localizer?.localize(it) },
            statusStringKey = null,
            statusString = null,
            blockExplorerUrl = mintscan,
            iconLocal = typeIconMap[type.rawValue],
            indicator = statusMap["CONFIRMED"],
        )
    }
}
