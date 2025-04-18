package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketSummaryState
import exchange.dydx.abacus.utils.Numeric

internal class MarketCalculator(val parser: ParserProtocol) {
    fun calculate(
        marketsSummary: InternalMarketSummaryState,
    ): InternalMarketSummaryState {
        val markets = marketsSummary.markets

        var volume24HUSDC = Numeric.double.ZERO
        var openInterestUSDC = Numeric.double.ZERO
        var trades24H = Numeric.double.ZERO

        for ((_, market) in markets) {
            val perpetual = market.perpetualMarket?.perpetual
            if (perpetual != null) {
                volume24HUSDC += perpetual.volume24H ?: Numeric.double.ZERO
                openInterestUSDC += perpetual.openInterestUSDC
                trades24H += perpetual.trades24H ?: Numeric.double.ZERO
            }
        }

        marketsSummary.volume24HUSDC = volume24HUSDC
        marketsSummary.openInterestUSDC = openInterestUSDC
        marketsSummary.trades24H = trades24H

        return marketsSummary
    }
}
