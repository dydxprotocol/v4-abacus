package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable

internal class SkipStatusProcessor(
    parser: ParserProtocol,
    private val transactionId: String?,
) : BaseProcessor(parser) {

    enum class TransferTypes(val value: String) {
        IBCTransfer("ibc_transfer"),
        AxelarTransfer("axelar_transfer"),
        CCTPTransfer("cctp_transfer")
    }

    enum class TransferDirection(val value: String) {
        From("from"),
        To("to"),
    }

    data class ChainData(
        val chainId: String? = null,
        val transactionUrl: String? = null,
        val txHash: String? = null,
        val status: String? = null
    ) {
        fun toRouteStatusMap(): Map<String, String?> {
            return mapOf(
                "chainId" to chainId,
                "txHash" to txHash,
                "status" to status,
            )
        }
        fun toTransferChainStatusMap(): Map<String, String?> {
            return mapOf(
                "transactionId" to txHash,
                "transactionUrl" to transactionUrl,
            )
        }
    }

    private fun getStrFromMap(data: Map<String, Any?>, path: String): String? {
        return parser.asString(parser.value(data, path))
    }

    private fun getSquidStatusFromState(state: String?): String? {
        if (state == null) return null
        if (state.contains("SUCCESS") || state.contains("RECEIVED")) return "success"
        return "ongoing"
    }

    /**
     *  CCTP transfer state:
     *  CCTP_TRANSFER_UNKNOWN - Unknown error
     *  CCTP_TRANSFER_SENT - The burn transaction on the source chain has executed
     *  CCTP_TRANSFER_PENDING_CONFIRMATION - CCTP transfer is pending confirmation by the cctp attestation api
     *  CCTP_TRANSFER_CONFIRMED - CCTP transfer has been confirmed by the cctp attestation api
     *  CCTP_TRANSFER_RECEIVED - CCTP transfer has been received at the destination chain
     *
     *  Axelar transfer state:
     *  AXELAR_TRANSFER_UNKNOWN - Unknown error
     *  AXELAR_TRANSFER_PENDING_CONFIRMATION - Axelar transfer is pending confirmation
     *  AXELAR_TRANSFER_PENDING_RECEIPT - Axelar transfer is pending receipt at destination
     *  AXELAR_TRANSFER_SUCCESS - Axelar transfer succeeded and assets have been received
     *  AXELAR_TRANSFER_FAILURE - Axelar transfer failed
     *
     *  IBC Transfer state:
     *  TRANSFER_UNKNOWN - Transfer state is not known.
     *  TRANSFER_PENDING - The send packet for the transfer has been committed and the transfer is pending.
     *  TRANSFER_RECEIVED - The transfer packet has been received by the destination chain. It can still fail and revert if it is part of a multi-hop PFM transfer.
     *  TRANSFER_SUCCESS - The transfer has been successfully completed and will not revert.
     *  TRANSFER_FAILURE - The transfer has failed.
     *
     */
    private fun getStatusFromTransferState(state: String?, transferDirection: TransferDirection): String? {
        if (state == null) return null
//        If state is not unknown, it means the FROM tx succeeded
        if (!state.contains("UNKNOWN") && transferDirection == TransferDirection.From) return "success"
//        Both TO and FROM tx are successful when transfer has succeeded
        return getSquidStatusFromState(state)
    }

    private fun getChainDataFromIbcTransfer(ibcTransfer: Map<String, Any>, transferDirection: TransferDirection): ChainData? {
        val txType = if (transferDirection == TransferDirection.From) "send_tx" else "receive_tx"
        return ChainData(
            chainId = getStrFromMap(ibcTransfer, "packet_txs.$txType.chain_id"),
            transactionUrl = getStrFromMap(ibcTransfer, "packet_txs.$txType.explorer_link"),
            txHash = getStrFromMap(ibcTransfer, "packet_txs.$txType.tx_hash"),
            status = getStatusFromTransferState(state = getStrFromMap(ibcTransfer, "state"), transferDirection = transferDirection),
        )
    }

    private fun getChainDataFromCCTPTransfer(cctpTransfer: Map<String, Any>, transferDirection: TransferDirection): ChainData? {
        val txType = if (transferDirection == TransferDirection.From) "send_tx" else "receive_tx"
        return ChainData(
            chainId = getStrFromMap(cctpTransfer, "txs.$txType.chain_id"),
            transactionUrl = getStrFromMap(cctpTransfer, "txs.$txType.explorer_link"),
            txHash = getStrFromMap(cctpTransfer, "txs.$txType.tx_hash"),
            status = getStatusFromTransferState(state = getStrFromMap(cctpTransfer, "state"), transferDirection = transferDirection),
        )
    }

    private fun getChainDataFromAxelarTransfer(axelarTransfer: Map<String, Any>, transferDirection: TransferDirection): ChainData? {
        val txType = if (transferDirection === TransferDirection.To) "execute_tx" else "send_tx"
        if (parser.value(axelarTransfer, "txs.contract_call_with_token_txs") != null) {
            return ChainData(
                chainId = getStrFromMap(axelarTransfer, "txs.contract_call_with_token_txs.$txType.chain_id"),
                transactionUrl = getStrFromMap(axelarTransfer, "txs.contract_call_with_token_txs.$txType.explorer_link"),
                txHash = getStrFromMap(axelarTransfer, "txs.contract_call_with_token_txs.$txType.tx_hash"),
                status = getStatusFromTransferState(state = getStrFromMap(axelarTransfer, "state"), transferDirection = transferDirection),
            )
        }
        return ChainData(
            chainId = getStrFromMap(axelarTransfer, "${transferDirection.value}_chain_id"),
            transactionUrl = getStrFromMap(axelarTransfer, "txs.send_token_txs.$txType.explorer_link"),
            txHash = getStrFromMap(axelarTransfer, "txs.send_token_txs.$txType.tx_hash"),
            status = getStatusFromTransferState(state = getStrFromMap(axelarTransfer, "state"), transferDirection = transferDirection),
        )
    }

    private fun getRelevantTransfer(payload: Map<String, Any>, transferDirection: TransferDirection): Map<String, Any>? {
        val transferSequence = parser.asList(parser.value(payload, "transfers.0.transfer_sequence")) ?: return null
        if (transferDirection === TransferDirection.To) return parser.asMap(transferSequence.lastOrNull())
        return parser.asMap(transferSequence.firstOrNull())
    }

    private fun getChainDataFromTransferSequence(payload: Map<String, Any>, transferDirection: TransferDirection): ChainData? {
        val transferWrapper = getRelevantTransfer(payload, transferDirection) ?: return null
        if (transferWrapper.containsKey(TransferTypes.IBCTransfer.value)) {
            val ibcTransfer = parser.asMap(transferWrapper.get(TransferTypes.IBCTransfer.value)) ?: return null
            return getChainDataFromIbcTransfer(ibcTransfer = ibcTransfer, transferDirection = transferDirection)
        }
        if (transferWrapper.containsKey(TransferTypes.CCTPTransfer.value)) {
            val cctpTransfer = parser.asMap(transferWrapper.get(TransferTypes.CCTPTransfer.value)) ?: return null
            return getChainDataFromCCTPTransfer(cctpTransfer = cctpTransfer, transferDirection = transferDirection)
        }
        if (transferWrapper.containsKey(TransferTypes.AxelarTransfer.value)) {
            val axelarTransfer = parser.asMap(transferWrapper.get(TransferTypes.AxelarTransfer.value)) ?: return null
            return getChainDataFromAxelarTransfer(axelarTransfer = axelarTransfer, transferDirection = transferDirection)
        }
        return null
    }

    private fun getAxelarTxUrl(payload: Map<String, Any>): String? {
        val transferSequence = parser.asList(parser.value(payload, "transfers.0.transfer_sequence")) ?: return null
        val axelarTransfer = parser.asMap(
            transferSequence.firstOrNull { transfer ->
                parser.asMap(transfer)?.containsKey(TransferTypes.AxelarTransfer.value) ?: false
            },
        ) ?: return null
        return getStrFromMap(axelarTransfer, "axelar_transfer.axelar_scan_link")
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
    ): Map<String, Any> {
        val modified = existing?.mutable() ?: mutableMapOf()
        val fromChainData = getChainDataFromTransferSequence(payload = payload, transferDirection = TransferDirection.From)
        val toChainData = getChainDataFromTransferSequence(payload = payload, transferDirection = TransferDirection.To)
        val skipState = mutableMapOf(
            "squidTransactionStatus" to getSquidStatusFromState(getStrFromMap(payload, "transfers.0.state")),
            "axelarTransactionUrl" to getAxelarTxUrl(payload),
            "fromChainStatus" to fromChainData?.toTransferChainStatusMap(),
            "toChainStatus" to toChainData?.toTransferChainStatusMap(),
            "routeStatuses" to listOfNotNull(
                fromChainData?.toRouteStatusMap(),
                toChainData?.toRouteStatusMap(),
            ),
        )
        val errorMessage = payload.get("message")
        if (errorMessage != null) skipState.set("error", errorMessage)
        val hash = transactionId ?: getStrFromMap(skipState, "fromChainStatus.transactionId")
        if (hash != null) {
            modified[hash] = skipState
        }
        return modified
    }
}
