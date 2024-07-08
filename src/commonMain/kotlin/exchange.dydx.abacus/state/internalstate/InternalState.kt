package exchange.dydx.abacus.state.internalstate

import kollections.JsExport
import kotlinx.serialization.Serializable

@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class InternalState(
    val transfer: InternalTransferInputState = InternalTransferInputState(),
    val perpetualMarkets: InternalStatePerpetualMarkets = InternalStatePerpetualMarkets(),
)
