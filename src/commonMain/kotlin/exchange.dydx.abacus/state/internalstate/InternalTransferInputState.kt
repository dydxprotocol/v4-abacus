package exchange.dydx.abacus.state.internalstate

import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputTokenResource

internal data class InternalTransferInputState(
    var chains: List<SelectionOption>? = null,
    var tokens: List<SelectionOption>? = null,
    var chainResources: Map<String, TransferInputChainResource>? = null,
    var tokenResources: Map<String, TransferInputTokenResource>? = null,
)
