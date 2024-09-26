package indexer.models.chain

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class OnChainShareUnlock(
    val shares: OnChainNumShares?,
    val unlockBlockHeight: Double?,
)

@JsExport
@Serializable
data class OnChainNumShares(
    val numShares: Double?,
)

@JsExport
@Serializable
data class OnChainAccountVaultResponse(
    val address: String? = null,
    val shares: OnChainNumShares? = null,
    val shareUnlocks: Array<OnChainShareUnlock>? = null,
    val equity: Double? = null,
    val withdrawableEquity: Double? = null,
)
