package indexer.models.chain

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class OnChainVaultDepositWithdrawSlippageResponse(
    val sharesToWithdraw: OnChainNumShares,
    val expectedQuoteQuantums: Double,
)
