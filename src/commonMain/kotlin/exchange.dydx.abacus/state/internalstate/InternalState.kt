package exchange.dydx.abacus.state.internalstate

import exchange.dydx.abacus.output.SubaccountFill
import exchange.dydx.abacus.output.SubaccountOrder

internal data class InternalState(
    val transfer: InternalTransferInputState = InternalTransferInputState(),
    var wallet: InternalWalletState = InternalWalletState(),
)

internal data class InternalWalletState(
    var account: InternalAccountState = InternalAccountState(),
    var walletAddress: String? = null,
)

internal data class InternalAccountState(
    var subaccounts: MutableMap<Int, InternalSubaccountState> = mutableMapOf(),
    var groupedSubaccounts: MutableMap<Int, InternalSubaccountState> = mutableMapOf(),
)

internal data class InternalSubaccountState(
    var fills: List<SubaccountFill>? = null,
    var orders: List<SubaccountOrder>? = null,
    var subaccountNumber: Int,
)
