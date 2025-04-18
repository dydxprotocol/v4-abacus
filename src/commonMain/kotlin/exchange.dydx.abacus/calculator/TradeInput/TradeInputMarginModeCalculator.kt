package exchange.dydx.abacus.calculator.tradeinput

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.state.InternalAccountState
import exchange.dydx.abacus.state.InternalMarketState
import exchange.dydx.abacus.state.InternalTradeInputState
import exchange.dydx.abacus.utils.DEFAULT_TARGET_LEVERAGE
import exchange.dydx.abacus.utils.Numeric
import kotlin.math.min

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
        val maxMarketLeverage = market?.perpetualMarket?.configs?.maxMarketLeverage ?: Numeric.double.ONE

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
                tradeInput.targetLeverage = if (existingPositionLeverage != null && existingPositionLeverage > Numeric.double.ZERO) existingPositionLeverage else min(DEFAULT_TARGET_LEVERAGE, maxMarketLeverage)
            }
        } else {
            val marketMarginMode = MarginCalculator.findMarketMarginMode(
                market = market?.perpetualMarket,
            )
            when (marketMarginMode) {
                MarginMode.Isolated -> {
                    tradeInput.marginMode = marketMarginMode
                    tradeInput.targetLeverage = tradeInput.targetLeverage ?: min(DEFAULT_TARGET_LEVERAGE, maxMarketLeverage)
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
