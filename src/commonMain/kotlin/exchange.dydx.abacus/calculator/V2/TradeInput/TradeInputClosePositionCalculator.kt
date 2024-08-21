package exchange.dydx.abacus.calculator.v2.tradeinput

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.safeCreate
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder

internal object TradeInputClosePositionConstants {
    const val LIMIT_CLOSE_ORDER_DEFAULT_DURATION_DAYS = 28.0
}
internal class TradeInputClosePositionCalculator {
    fun calculate(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?,
    ): InternalTradeInputState {
        val inputType = ClosePositionInputField.invoke(trade.size?.input)
        val marketId = trade.marketId ?: return trade
        val position = subaccount?.openPositions?.get(marketId) ?: return trade
        val positionSize = position.calculated[CalculationPeriod.current]?.size ?: return trade
        val positionSizeAbs = positionSize.abs()
        trade.side = if (positionSize > Numeric.double.ZERO) OrderSide.Sell else OrderSide.Buy
        when (inputType) {
            ClosePositionInputField.percent -> {
                val percent = trade.sizePercent ?: return trade
                val size =
                    if (percent > Numeric.double.ONE) positionSizeAbs else positionSizeAbs * percent
                val stepSize = market?.perpetualMarket?.configs?.stepSize ?: return trade
                trade.size =
                    TradeInputSize.safeCreate(trade.size).copy(size = Rounder.round(size, stepSize))
                return trade
            }

            ClosePositionInputField.size -> {
                trade.sizePercent = null
                val size = trade.size?.size ?: return trade
                if (size > positionSizeAbs) {
                    trade.size = TradeInputSize.safeCreate(trade.size).copy(size = positionSizeAbs)
                }
            }

            else -> {}
        }
        return trade
    }
}
