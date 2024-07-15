package exchange.dydx.abacus.state.internalstate

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.SubaccountFill

internal data class InternalState(
    var assets: MutableMap<String, Asset> = mutableMapOf(),
    val transfer: InternalTransferInputState = InternalTransferInputState(),
    val wallet: InternalWalletState = InternalWalletState(),
)

internal data class InternalWalletState(
    var account: InternalAccountState = InternalAccountState(),
    var walletAddress: String? = null,
)

internal data class InternalAccountState(
    var subaccounts: MutableMap<Int, InternalSubaccountState> = mutableMapOf(),
)

internal data class InternalSubaccountState(
    var fills: List<SubaccountFill>? = null,
    var subaccountNumber: Int,
)
