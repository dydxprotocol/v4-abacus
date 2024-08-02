package indexer.models.chain

import kotlinx.serialization.Serializable

@Serializable
data class OnChainWithdrawalAndTransferGatingStatusResponse(
    val negativeTncSubaccountSeenAtBlock: Double? = null,
    val chainOutageSeenAtBlock: Double? = null,
    val withdrawalsAndTransfersUnblockedAtBlock: Double? = null,
)
