package exchange.dydx.abacus.state.internalstate

internal data class InternalState(
    val transfer: InternalTransferInputState = InternalTransferInputState(),
    val perpetualMarkets: InternalStatePerpetualMarkets = InternalStatePerpetualMarkets(),
)
