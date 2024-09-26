package exchange.dydx.abacus.calculator.v2.tradeinput

import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputPrice
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder

internal class TradeInputNonMarketOrderCalculator {
    fun calculate(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        input: String,
    ): InternalTradeInputState {
        var tradeSize = trade.size
        if (tradeSize != null) {
            val isBuying = trade.isBuying
            val tradePrices = trade.price
            val stepSize = market?.perpetualMarket?.configs?.stepSize ?: 0.001
            val price = getNonMarketOrderPrice(tradePrices, market, trade.type, isBuying)
            when (input) {
                "size.size", "size.percent" -> {
                    val size = tradeSize.size
                    val usdcSize =
                        if (price != null && size != null) (price * size) else null
                    tradeSize = tradeSize.copy(usdcSize = usdcSize)
                }

                "size.usdcSize" -> {
                    val usdcSize = tradeSize.usdcSize
                    val size =
                        if (price != null && usdcSize != null && usdcSize > Numeric.double.ZERO && price > Numeric.double.ZERO) {
                            Rounder.round(usdcSize / price, stepSize)
                        } else {
                            null
                        }
                    tradeSize = tradeSize.copy(size = size)
                }

                else -> {
                    val size = tradeSize.size
                    val usdcSize =
                        if (price != null && size != null) (price * size) else null
                    tradeSize = tradeSize.copy(usdcSize = usdcSize)
                }
            }
            trade.size = tradeSize
        }

        trade.marketOrder = null
        return trade
    }

    private fun getNonMarketOrderPrice(
        prices: TradeInputPrice?,
        market: InternalMarketState?,
        type: OrderType?,
        isBuying: Boolean,
    ): Double? {
        if (prices == null) {
            return null
        }
        when (type) {
            OrderType.Limit, OrderType.StopLimit, OrderType.TakeProfitLimit -> {
                return prices.limitPrice
            }

            OrderType.TrailingStop -> {
                val oraclePrice = market?.perpetualMarket?.oraclePrice ?: return null

                val trailingPercent = prices.trailingPercent ?: Numeric.double.ZERO
                if (trailingPercent != Numeric.double.ZERO) {
                    val percent =
                        if (isBuying) {
                            (Numeric.double.ONE - trailingPercent)
                        } else {
                            (Numeric.double.ONE + trailingPercent)
                        }
                    return oraclePrice * percent
                }
                return null
            }

            OrderType.StopMarket, OrderType.TakeProfitMarket -> {
                return prices.triggerPrice
            }

            else -> {
                return null
            }
        }
    }
}
