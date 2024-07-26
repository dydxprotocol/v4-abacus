package exchange.dydx.abacus.calculator.v2

import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAssetPositionState

internal class SubaccountCalculatorV2(
    val parser: ParserProtocol
) {
    fun calculateQuoteBalance(
        assetPositions: Map<String, InternalAssetPositionState>? = null,
    ): Double? {
        val usdc = assetPositions?.get("USDC")
        return if (usdc != null) {
            val size = usdc.size
            if (size != null) {
                val side = usdc.side
                if (side == PositionSide.LONG) size else size * -1.0
            } else {
                null
            }
        } else {
            null
        }
    }
}
