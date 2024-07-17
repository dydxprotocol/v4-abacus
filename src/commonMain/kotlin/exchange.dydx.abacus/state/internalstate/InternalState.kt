package exchange.dydx.abacus.state.internalstate

import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.MarketTrade
import exchange.dydx.abacus.output.SubaccountFill
import exchange.dydx.abacus.output.SubaccountOrder

internal data class InternalState(
    var assets: MutableMap<String, Asset> = mutableMapOf(),
    val transfer: InternalTransferInputState = InternalTransferInputState(),
    val wallet: InternalWalletState = InternalWalletState(),
    val markets: MutableMap<String, InternalMarketState> = mutableMapOf(),
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

internal data class InternalMarketState(
    var trades: List<MarketTrade>
)
