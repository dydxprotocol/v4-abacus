package exchange.dydx.abacus.output

import exchange.dydx.abacus.output.input.OrderSide

fun OrderSide.isOppositeOf(that: PositionSide): Boolean =
    (this == OrderSide.buy && that == PositionSide.SHORT) || (this == OrderSide.sell && that == PositionSide.LONG)
