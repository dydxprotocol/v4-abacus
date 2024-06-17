package exchange.dydx.abacus.calculator

import abs
import exchange.dydx.abacus.calculator.SlippageConstants.STOP_MARKET_ORDER_SLIPPAGE_BUFFER
import exchange.dydx.abacus.calculator.SlippageConstants.STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
import exchange.dydx.abacus.calculator.SlippageConstants.TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER
import exchange.dydx.abacus.calculator.SlippageConstants.TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kotlin.math.max

internal object TriggerOrdersConstants {
    const val TRIGGER_ORDER_DEFAULT_DURATION_DAYS = 90.0
}

@Suppress("UNCHECKED_CAST")
internal class TriggerOrdersInputCalculator(val parser: ParserProtocol) {
    internal fun calculate(
        state: Map<String, Any>,
        subaccountNumber: Int?,
    ): Map<String, Any> {
        val account = parser.asNativeMap(state["account"])
        val subaccount = if (subaccountNumber != null) {
            parser.asMap(parser.value(account, "groupedSubaccounts.$subaccountNumber"))
                ?: parser.asNativeMap(
                    parser.value(
                        account,
                        "subaccounts.$subaccountNumber",
                    ),
                )
        } else {
            null
        }
        val triggerOrders = parser.asNativeMap(state["triggerOrders"])
        val marketId = parser.asString(triggerOrders?.get("marketId"))
        val inputSize = parser.asDouble(triggerOrders?.get("size"))
        val stopLossOrder = parser.asNativeMap(triggerOrders?.get("stopLossOrder"))
        val takeProfitOrder = parser.asNativeMap(triggerOrders?.get("takeProfitOrder"))
        val position = parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId"))

        return if (triggerOrders != null && position != null) {
            val modified = state.mutable()
            val modifiedStopLossOrder = if (stopLossOrder != null) {
                calculateTriggerOrderTrade(stopLossOrder, position, inputSize)
            } else {
                stopLossOrder
            }
            val modifiedTakeProfitOrder = if (takeProfitOrder != null) {
                calculateTriggerOrderTrade(takeProfitOrder, position, inputSize)
            } else {
                takeProfitOrder
            }
            val modifiedTriggerOrders = triggerOrders.mutable()
            modifiedTriggerOrders.safeSet("stopLossOrder", modifiedStopLossOrder)
            modifiedTriggerOrders.safeSet("takeProfitOrder", modifiedTakeProfitOrder)

            modified["triggerOrders"] = modifiedTriggerOrders
            modified
        } else {
            state
        }
    }

    private fun calculateTriggerOrderTrade(
        triggerOrder: Map<String, Any>,
        position: Map<String, Any>,
        inputSize: Double?,
    ): Map<String, Any> {
        val modified = triggerOrder.mutable()
        val orderSize = parser.asDouble(triggerOrder["size"])
        val absSize = (inputSize ?: orderSize)?.abs()
        val triggerPrices = parser.asNativeMap(triggerOrder["price"])?.let { calculateTriggerPrices(it, position, absSize) }
        modified.safeSet("price", triggerPrices)

        return finalizeOrderFromPriceInputs(modified, position, absSize)
    }

    private fun calculateTriggerPrices(
        triggerPrices: Map<String, Any>,
        position: Map<String, Any>,
        size: Double?,
    ): MutableMap<String, Any> {
        val modified = triggerPrices.mutable()
        val entryPrice = parser.asDouble(parser.value(position, "entryPrice.current"))
        val inputType = parser.asString(parser.value(modified, "input"))
        val positionSide = parser.asString(parser.value(position, "resources.indicator.current"))
        val positionSize = parser.asDouble(parser.value(position, "size.current"))?.abs() ?: return modified
        val notionalTotal = parser.asDouble(parser.value(position, "notionalTotal.current")) ?: return modified
        val leverage = parser.asDouble(parser.value(position, "leverage.current")) ?: return modified

        if (size == null || size == Numeric.double.ZERO || notionalTotal == Numeric.double.ZERO || leverage == Numeric.double.ZERO) {
            // A valid position size should never have 0 size, notional value or leverage.
            return modified;
        }

        val scaledLeverage = max(leverage.abs(), 1.0)
        val scaledNotionalTotal = size.div(positionSize).times(notionalTotal);

        if (entryPrice != null) {
            val triggerPrice = parser.asDouble(parser.value(modified, "triggerPrice"))
            val usdcDiff = parser.asDouble(parser.value(modified, "usdcDiff"))
            val percentDiff = parser.asDouble(parser.value(modified, "percentDiff"))?.let { it / 100.0 }

            when (inputType) {
                "stopLossOrder.price.triggerPrice" -> {
                    if (triggerPrice != null) {
                        modified.safeSet(
                            "usdcDiff",
                            when (positionSide) {
                                "long" -> size.times(entryPrice.minus(triggerPrice))
                                "short" -> size.times(triggerPrice.minus(entryPrice))
                                else -> null
                            },
                        )
                        modified.safeSet(
                            "percentDiff",
                            when (positionSide) {
                                "long" -> size.times(scaledLeverage.times(entryPrice.minus(triggerPrice))).div(scaledNotionalTotal).times(100)
                                "short" -> size.times(scaledLeverage.times(triggerPrice.minus(entryPrice))).div(scaledNotionalTotal).times(100)
                                else -> null
                            },
                        )
                    } else {
                        modified.safeSet(
                            "usdcDiff",
                            null,
                        )
                        modified.safeSet(
                            "percentDiff",
                            null,
                        )
                    }
                }
                "takeProfitOrder.price.triggerPrice" -> {
                    if (triggerPrice != null) {
                        modified.safeSet(
                            "usdcDiff",
                            when (positionSide) {
                                "long" -> size.times(triggerPrice.minus(entryPrice))
                                "short" -> size.times(entryPrice.minus(triggerPrice))
                                else -> null
                            },
                        )
                        modified.safeSet(
                            "percentDiff",
                            when (positionSide) {
                                "long" -> size.times(scaledLeverage.times(triggerPrice.minus(entryPrice))).div(scaledNotionalTotal).times(100)
                                "short" -> size.times(scaledLeverage.times(entryPrice.minus(triggerPrice))).div(scaledNotionalTotal).times(100)
                                else -> null
                            },
                        )
                    } else {
                        modified.safeSet(
                            "usdcDiff",
                            null,
                        )
                        modified.safeSet(
                            "percentDiff",
                            null,
                        )
                    }
                }
                "stopLossOrder.price.usdcDiff" -> {
                    if (usdcDiff != null) {
                        modified.safeSet(
                            "triggerPrice",
                            when (positionSide) {
                                "long" -> entryPrice.minus(usdcDiff.div(size))
                                "short" -> entryPrice.plus(usdcDiff.div(size))
                                else -> null
                            },
                        )
                        modified.safeSet(
                            "percentDiff",
                            usdcDiff.div(scaledNotionalTotal).times(scaledLeverage).times(100),
                        )
                    } else {
                        modified.safeSet(
                            "triggerPrice",
                            null,
                        )
                        modified.safeSet(
                            "percentDiff",
                            null,
                        )
                    }
                }
                "takeProfitOrder.price.usdcDiff" -> {
                    if (usdcDiff != null) {
                        modified.safeSet(
                            "triggerPrice",
                            when (positionSide) {
                                "long" -> entryPrice.plus(usdcDiff.div(size))
                                "short" -> entryPrice.minus(usdcDiff.div(size))
                                else -> null
                            },
                        )
                        modified.safeSet(
                            "percentDiff",
                            usdcDiff.div(scaledNotionalTotal).times(scaledLeverage).times(100),
                        )
                    } else {
                        modified.safeSet(
                            "triggerPrice",
                            null,
                        )
                        modified.safeSet(
                            "percentDiff",
                            null,
                        )
                    }
                }
                "stopLossOrder.price.percentDiff" -> {
                    if (percentDiff != null) {
                        modified.safeSet(
                            "triggerPrice",
                            when (positionSide) {
                                "long" -> entryPrice.minus(percentDiff.times(scaledNotionalTotal).div(scaledLeverage.times(size)))
                                "short" -> entryPrice.plus(percentDiff.times(scaledNotionalTotal).div(scaledLeverage.times(size)))
                                else -> null
                            },
                        )
                        modified.safeSet(
                            "usdcDiff",
                            percentDiff.times(scaledNotionalTotal).div(scaledLeverage),
                        )
                    } else {
                        modified.safeSet(
                            "triggerPrice",
                            null,
                        )
                        modified.safeSet(
                            "usdcDiff",
                            null,
                        )
                    }
                }
                "takeProfitOrder.price.percentDiff" -> {
                    if (percentDiff != null) {
                        modified.safeSet(
                            "triggerPrice",
                            when (positionSide) {
                                "long" -> entryPrice.plus(percentDiff.times(scaledNotionalTotal).div(scaledLeverage.times(size)))
                                "short" -> entryPrice.minus(percentDiff.times(scaledNotionalTotal).div(scaledLeverage.times(size)))
                                else -> null
                            },
                        )
                        modified.safeSet(
                            "usdcDiff",
                            percentDiff.times(scaledNotionalTotal).div(scaledLeverage),
                        )
                    } else {
                        modified.safeSet(
                            "triggerPrice",
                            null,
                        )
                        modified.safeSet(
                            "usdcDiff",
                            null,
                        )
                    }
                }
                else -> {}
            }
        }
        return modified
    }

    private fun finalizeOrderFromPriceInputs(triggerOrder: Map<String, Any>, position: Map<String, Any>, size: Double?): MutableMap<String, Any> {
        val modified = triggerOrder.mutable()

        val side = getOrderSide(position)
        modified.safeSet("side", side?.rawValue)

        val type = getOrderType(triggerOrder)
        modified.safeSet("type", type?.rawValue)

        when (type) {
            OrderType.TakeProfitMarket, OrderType.StopMarket -> {
                val triggerPrice =
                    parser.asDouble(parser.value(triggerOrder, "price.triggerPrice"))
                val majorMarket = when (parser.asString(triggerOrder["marketId"])) {
                    "BTC-USD", "ETH-USD" -> true
                    else -> false
                }
                val slippagePercentage = if (majorMarket) {
                    if (type == OrderType.StopMarket) {
                        STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                    } else {
                        TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                    }
                } else {
                    if (type == OrderType.StopMarket) {
                        STOP_MARKET_ORDER_SLIPPAGE_BUFFER
                    } else {
                        TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER
                    }
                }
                val calculatedLimitPrice = if (triggerPrice != null) {
                    if (parser.asString(triggerOrder["side"]) == "BUY") {
                        triggerPrice * (Numeric.double.ONE + slippagePercentage)
                    } else {
                        triggerPrice * (Numeric.double.ONE - slippagePercentage)
                    }
                } else {
                    null
                }
                modified.safeSet("summary.price", calculatedLimitPrice)
            }
            OrderType.TakeProfitLimit, OrderType.StopLimit -> {
                modified.safeSet("summary.price", parser.asDouble(parser.value(triggerOrder, "price.limitPrice")))
            }
            else -> {}
        }

        modified.safeSet("summary.size", size)
        return modified
    }

    private fun getOrderSide(
        position: Map<String, Any>,
    ): OrderSide? {
        val positionSide = parser.asString(parser.value(position, "resources.indicator.current"))

        return when (positionSide) {
            "short" -> OrderSide.Buy
            "long" -> OrderSide.Sell
            else -> null
        }
    }

    private fun getOrderType(triggerOrder: Map<String, Any>): OrderType? {
        val limitPrice = parser.asDouble(parser.value(triggerOrder, "price.limitPrice"))
        val type = parser.asString(triggerOrder["type"])?.let {
            OrderType.invoke(it)
        }
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
}
