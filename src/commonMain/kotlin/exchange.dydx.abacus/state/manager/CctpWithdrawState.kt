package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.protocols.TransactionCallback

data class CctpWithdrawState(
//    JSON string of a single map representing a tx's single message
    val singleMessagePayload: String?,
    val callback: TransactionCallback?,
//    JSON string of a list of maps representing a tx's multiple messages
    val multiMessagePayload: String?,
)

internal var pendingCctpWithdraw: CctpWithdrawState? = null
internal var processingCctpWithdraw = false
