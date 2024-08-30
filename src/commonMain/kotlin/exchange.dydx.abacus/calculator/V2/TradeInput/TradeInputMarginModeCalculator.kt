package exchange.dydx.abacus.calculator.v2.tradeinput

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.utils.Numeric

internal class TradeInputMarginModeCalculator {
    fun updateTradeInputMarginMode(
        tradeInput: InternalTradeInputState,
        markets: Map<String, InternalMarketState>?,
        account: InternalAccountState,
        subaccountNumber: Int,
    ): InternalTradeInputState {
        val existingMarginMode = MarginCalculator.findExistingMarginMode(
            account = account,
            marketId = tradeInput.marketId,
            subaccountNumber = subaccountNumber,
        )
        val market = markets?.get(tradeInput.marketId)
        val imf = market?.perpetualMarket?.configs?.initialMarginFraction ?: Numeric.double.ZERO
        val effectiveImf = market?.perpetualMarket?.configs?.effectiveInitialMarginFraction ?: Numeric.double.ZERO
        val maxMarketLeverage = if (effectiveImf > Numeric.double.ZERO) {
            Numeric.double.ONE / effectiveImf
        } else if (imf > Numeric.double.ZERO) {
            Numeric.double.ONE / imf
        } else {
            Numeric.double.ONE
        }

        // If there is an existing position or order, we have to use the same margin mode
        if (existingMarginMode != null) {
            tradeInput.marginMode = existingMarginMode
            if (existingMarginMode == MarginMode.Isolated && tradeInput.targetLeverage == null) {
                val existingPosition = MarginCalculator.findExistingPosition(
                    account = account,
                    marketId = tradeInput.marketId,
                    subaccountNumber = subaccountNumber,
                )
                val existingPositionLeverage = existingPosition?.calculated?.get(CalculationPeriod.current)?.leverage
                tradeInput.targetLeverage = existingPositionLeverage ?: maxMarketLeverage
            }
        } else {
            val marketMarginMode = MarginCalculator.findMarketMarginMode(
                market = market?.perpetualMarket,
            )
            when (marketMarginMode) {
                MarginMode.Isolated -> {
                    tradeInput.marginMode = marketMarginMode
                    tradeInput.targetLeverage = tradeInput.targetLeverage ?: maxMarketLeverage
                }

                MarginMode.Cross -> {
                    if (tradeInput.marginMode == null) {
                        tradeInput.marginMode = marketMarginMode
                    }
                }
            }
        }
        return tradeInput
    }
}
