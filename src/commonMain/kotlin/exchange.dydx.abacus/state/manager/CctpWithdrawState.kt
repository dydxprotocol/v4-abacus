package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.protocols.TransactionCallback

data class CctpWithdrawState(
    val payload: String?,
    val callback: TransactionCallback?,
)

internal var pendingCctpWithdraw: CctpWithdrawState? = null
internal var processingCctpWithdraw = false
