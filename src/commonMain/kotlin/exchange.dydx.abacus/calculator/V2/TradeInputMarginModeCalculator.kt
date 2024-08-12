package exchange.dydx.abacus.calculator.v2

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState

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

        // If there is an existing position or order, we have to use the same margin mode
        if (existingMarginMode != null) {
            tradeInput.marginMode = existingMarginMode
            if ( existingMarginMode == MarginMode.Isolated && tradeInput.targetLeverage == null) {
                val existingPosition = MarginCalculator.findExistingPosition(
                    account = account,
                    marketId = tradeInput.marketId,
                    subaccountNumber = subaccountNumber,
                )
                val existingPositionLeverage = existingPosition?.calculated?.get(CalculationPeriod.current)?.leverage
                tradeInput.targetLeverage = existingPositionLeverage ?: 1.0
            }
        } else {
            val marketMarginMode = MarginCalculator.findMarketMarginMode(
                market = markets?.get(tradeInput.marketId)?.perpetualMarket,
            )
            when (marketMarginMode) {
                MarginMode.Isolated -> {
                    tradeInput.marginMode = marketMarginMode
                    tradeInput.targetLeverage = tradeInput.targetLeverage ?: 1.0
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