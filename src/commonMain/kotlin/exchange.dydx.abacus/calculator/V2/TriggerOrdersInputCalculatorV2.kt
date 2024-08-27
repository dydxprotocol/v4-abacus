package exchange.dydx.abacus.calculator.v2

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.SlippageConstants.MAJOR_MARKETS
import exchange.dydx.abacus.calculator.SlippageConstants.STOP_MARKET_ORDER_SLIPPAGE_BUFFER
import exchange.dydx.abacus.calculator.SlippageConstants.STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
import exchange.dydx.abacus.calculator.SlippageConstants.TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER
import exchange.dydx.abacus.calculator.SlippageConstants.TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TriggerOrderInputSummary
import exchange.dydx.abacus.output.input.TriggerPrice
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalTriggerOrderState
import exchange.dydx.abacus.state.internalstate.InternalTriggerOrdersInputState
import exchange.dydx.abacus.utils.Numeric
import indexer.codegen.IndexerPositionSide
import kotlin.math.max

internal class TriggerOrdersInputCalculatorV2() {
    fun calculate(
        triggerOrders: InternalTriggerOrdersInputState,
        account: InternalAccountState,
        subaccountNumber: Int,
    ): InternalTriggerOrdersInputState {
        val subaccount = account.groupedSubaccounts[subaccountNumber]
            ?: account.subaccounts[subaccountNumber]
        val marketId = triggerOrders.marketId
        val inputSize = triggerOrders.size
        val stopLossOrder = triggerOrders.stopLossOrder
        val takeProfitOrder = triggerOrders.takeProfitOrder
        val position = subaccount?.openPositions?.get(marketId)

        if (position != null) {
            if (stopLossOrder != null) {
                triggerOrders.stopLossOrder =
                    calculateTriggerOrderTrade(stopLossOrder, triggerOrders.marketId, position, inputSize)
            }
            if (takeProfitOrder != null) {
                triggerOrders.takeProfitOrder =
                    calculateTriggerOrderTrade(takeProfitOrder, triggerOrders.marketId, position, inputSize)
            }
        }

        return triggerOrders
    }

    private fun calculateTriggerOrderTrade(
        triggerOrder: InternalTriggerOrderState,
        marketId: String?,
        position: InternalPerpetualPosition,
        inputSize: Double?,
    ): InternalTriggerOrderState {
        val orderSize = triggerOrder.size
        val absSize = (inputSize ?: orderSize)?.abs()
        triggerOrder.price =
            triggerOrder.price?.let { calculateTriggerPrices(it, position, absSize) }

        return finalizeOrderFromPriceInputs(triggerOrder, marketId, position, absSize)
    }

    private fun calculateTriggerPrices(
        triggerPrices: TriggerPrice,
        position: InternalPerpetualPosition,
        size: Double?,
    ): TriggerPrice {
        var modified = triggerPrices

        val inputType = triggerPrices.input
        val currentPosition = position.calculated[CalculationPeriod.current]
        val entryPrice = position.entryPrice
        val positionSide = position.side
        val positionSize = currentPosition?.size?.abs() ?: return triggerPrices
        val notionalTotal = currentPosition.notionalTotal ?: return triggerPrices
        val leverage = currentPosition.leverage ?: return triggerPrices

        if (size == null || size == Numeric.double.ZERO || notionalTotal == Numeric.double.ZERO || leverage == Numeric.double.ZERO) {
            // A valid position size should never have 0 size, notional value or leverage.
            return triggerPrices;
        }

        val scaledLeverage = max(leverage.abs(), 1.0)
        val scaledNotionalTotal = size.div(positionSize).times(notionalTotal);

        if (entryPrice != null) {
            val triggerPrice = triggerPrices.triggerPrice
            val usdcDiff = triggerPrices.usdcDiff
            val percentDiff = triggerPrices.percentDiff?.let { it / 100.0 }

            when (inputType) {
                "stopLossOrder.price.triggerPrice" -> {
                    if (triggerPrice != null) {
                        val usdcDiffValue = when (positionSide) {
                            IndexerPositionSide.LONG -> size.times(entryPrice.minus(triggerPrice))
                            IndexerPositionSide.SHORT -> size.times(triggerPrice.minus(entryPrice))
                            else -> null
                        }
                        modified = modified.copy(usdcDiff = usdcDiffValue)
                        val percentDiffValue = when (positionSide) {
                            IndexerPositionSide.LONG -> size.times(
                                scaledLeverage.times(
                                    entryPrice.minus(
                                        triggerPrice,
                                    ),
                                ),
                            ).div(scaledNotionalTotal).times(100)

                            IndexerPositionSide.SHORT -> size.times(
                                scaledLeverage.times(
                                    triggerPrice.minus(entryPrice),
                                ),
                            ).div(scaledNotionalTotal).times(100)

                            else -> null
                        }
                        modified = modified.copy(percentDiff = percentDiffValue)
                    } else {
                        modified = modified.copy(usdcDiff = null)
                        modified = modified.copy(percentDiff = null)
                    }
                }

                "takeProfitOrder.price.triggerPrice" -> {
                    if (triggerPrice != null) {
                        val usdcDiffValue = when (positionSide) {
                            IndexerPositionSide.LONG -> size.times(triggerPrice.minus(entryPrice))
                            IndexerPositionSide.SHORT -> size.times(entryPrice.minus(triggerPrice))
                            else -> null
                        }
                        modified = modified.copy(usdcDiff = usdcDiffValue)
                        val percentDiffValue = when (positionSide) {
                            IndexerPositionSide.LONG -> size.times(
                                scaledLeverage.times(
                                    triggerPrice.minus(
                                        entryPrice,
                                    ),
                                ),
                            ).div(scaledNotionalTotal).times(100)

                            IndexerPositionSide.SHORT -> size.times(
                                scaledLeverage.times(
                                    entryPrice.minus(
                                        triggerPrice,
                                    ),
                                ),
                            ).div(scaledNotionalTotal).times(100)

                            else -> null
                        }
                        modified = modified.copy(percentDiff = percentDiffValue)
                    } else {
                        modified = modified.copy(usdcDiff = null)
                        modified = modified.copy(percentDiff = null)
                    }
                }

                "stopLossOrder.price.usdcDiff" -> {
                    if (usdcDiff != null) {
                        val triggerPriceValue = when (positionSide) {
                            IndexerPositionSide.LONG -> entryPrice.minus(usdcDiff.div(size))
                            IndexerPositionSide.SHORT -> entryPrice.plus(usdcDiff.div(size))
                            else -> null
                        }
                        modified = modified.copy(triggerPrice = triggerPriceValue)
                        val percentDiffValue =
                            usdcDiff.div(scaledNotionalTotal).times(scaledLeverage).times(100)
                        modified = modified.copy(percentDiff = percentDiffValue)
                    } else {
                        modified = modified.copy(triggerPrice = null)
                        modified = modified.copy(percentDiff = null)
                    }
                }

                "takeProfitOrder.price.usdcDiff" -> {
                    if (usdcDiff != null) {
                        val triggerPriceValue = when (positionSide) {
                            IndexerPositionSide.LONG -> entryPrice.plus(usdcDiff.div(size))
                            IndexerPositionSide.SHORT -> entryPrice.minus(usdcDiff.div(size))
                            else -> null
                        }
                        modified = modified.copy(triggerPrice = triggerPriceValue)
                        val percentDiffValue =
                            usdcDiff.div(scaledNotionalTotal).times(scaledLeverage).times(100)
                        modified = modified.copy(percentDiff = percentDiffValue)
                    } else {
                        modified = modified.copy(triggerPrice = null)
                        modified = modified.copy(percentDiff = null)
                    }
                }

                "stopLossOrder.price.percentDiff" -> {
                    if (percentDiff != null) {
                        val triggerPriceValue = when (positionSide) {
                            IndexerPositionSide.LONG -> entryPrice.minus(
                                percentDiff.times(
                                    scaledNotionalTotal,
                                ).div(scaledLeverage.times(size)),
                            )

                            IndexerPositionSide.SHORT -> entryPrice.plus(
                                percentDiff.times(
                                    scaledNotionalTotal,
                                ).div(scaledLeverage.times(size)),
                            )

                            else -> null
                        }
                        modified = modified.copy(triggerPrice = triggerPriceValue)
                        val usdcDiffValue =
                            percentDiff.times(scaledNotionalTotal).div(scaledLeverage)
                        modified = modified.copy(usdcDiff = usdcDiffValue)
                    } else {
                        modified = modified.copy(triggerPrice = null)
                        modified = modified.copy(usdcDiff = null)
                    }
                }

                "takeProfitOrder.price.percentDiff" -> {
                    if (percentDiff != null) {
                        val triggerPriceValue = when (positionSide) {
                            IndexerPositionSide.LONG -> entryPrice.plus(
                                percentDiff.times(
                                    scaledNotionalTotal,
                                ).div(scaledLeverage.times(size)),
                            )

                            IndexerPositionSide.SHORT -> entryPrice.minus(
                                percentDiff.times(
                                    scaledNotionalTotal,
                                ).div(scaledLeverage.times(size)),
                            )

                            else -> null
                        }
                        modified = modified.copy(triggerPrice = triggerPriceValue)
                        val usdcDiffValue =
                            percentDiff.times(scaledNotionalTotal).div(scaledLeverage)
                        modified = modified.copy(usdcDiff = usdcDiffValue)
                    } else {
                        modified = modified.copy(triggerPrice = null)
                        modified = modified.copy(usdcDiff = null)
                    }
                }

                else -> {}
            }
        }

        return modified
    }

    private fun finalizeOrderFromPriceInputs(
        triggerOrder: InternalTriggerOrderState,
        marketId: String?,
        position: InternalPerpetualPosition,
        size: Double?,
    ): InternalTriggerOrderState {
        triggerOrder.side = getOrderSide(position)
        triggerOrder.type = getOrderType(triggerOrder)

        val price: Double? = getPrice(triggerOrder, marketId)
        triggerOrder.summary = TriggerOrderInputSummary(size = size, price = price)

        return triggerOrder
    }

    private fun getOrderSide(
        position: InternalPerpetualPosition,
    ): OrderSide? {
        val positionSide = position.side

        return when (positionSide) {
            IndexerPositionSide.SHORT -> OrderSide.Buy
            IndexerPositionSide.LONG -> OrderSide.Sell
            else -> null
        }
    }

    private fun getOrderType(
        triggerOrder: InternalTriggerOrderState
    ): OrderType? {
        val limitPrice = triggerOrder.price?.limitPrice
        val type = triggerOrder.type

        if (limitPrice != null) {
            return when (type) {
                OrderType.TakeProfitMarket, OrderType.TakeProfitLimit -> OrderType.TakeProfitLimit
                OrderType.StopMarket, OrderType.StopLimit -> OrderType.StopLimit
                else -> null
            }
        } else {
            return when (type) {
                OrderType.TakeProfitMarket, OrderType.TakeProfitLimit -> OrderType.TakeProfitMarket
                OrderType.StopMarket, OrderType.StopLimit -> OrderType.StopMarket
                else -> null
            }
        }
    }

    private fun getPrice(
        triggerOrder: InternalTriggerOrderState,
        marketId: String?
    ): Double? {
        when (triggerOrder.type) {
            OrderType.TakeProfitMarket, OrderType.StopMarket -> {
                val triggerPrice = triggerOrder.price?.triggerPrice
                val majorMarket = MAJOR_MARKETS.contains(marketId)
                val slippagePercentage = if (majorMarket) {
                    if (triggerOrder.type == OrderType.StopMarket) {
                        STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                    } else {
                        TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                    }
                } else {
                    if (triggerOrder.type == OrderType.StopMarket) {
                        STOP_MARKET_ORDER_SLIPPAGE_BUFFER
                    } else {
                        TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER
                    }
                }
                val calculatedLimitPrice = if (triggerPrice != null) {
                    if (triggerOrder.side == OrderSide.Buy) {
                        triggerPrice * (Numeric.double.ONE + slippagePercentage)
                    } else {
                        triggerPrice * (Numeric.double.ONE - slippagePercentage)
                    }
                } else {
                    null
                }
                return calculatedLimitPrice
            }
            OrderType.TakeProfitLimit, OrderType.StopLimit -> {
                return triggerOrder.price?.limitPrice
            }
            else -> {
                return null
            }
        }
    }
}
