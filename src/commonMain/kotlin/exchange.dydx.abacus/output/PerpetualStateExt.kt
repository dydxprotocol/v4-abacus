package exchange.dydx.abacus.output

import exchange.dydx.abacus.output.input.OrderSide

fun OrderSide.isOppositeOf(that: PositionSide): Boolean =
    (this == OrderSide.Buy && that == PositionSide.SHORT) || (this == OrderSide.Sell && that == PositionSide.LONG)
