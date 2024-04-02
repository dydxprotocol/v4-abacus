package exchange.dydx.abacus.calculator

import abs
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
internal class TriggerOrdersInputCalculator(val parser: ParserProtocol) {
    @Suppress("LocalVariableName", "PropertyName")
    private val STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET = 0.05

    @Suppress("LocalVariableName", "PropertyName")
    private val TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET = 0.1

    @Suppress("LocalVariableName", "PropertyName")
    private val STOP_MARKET_ORDER_SLIPPAGE_BUFFER = 0.1

    @Suppress("LocalVariableName", "PropertyName")
    private val TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER = 0.2

    internal fun calculate(
        state: Map<String, Any>,
        subaccountNumber: Int?,
    ): Map<String, Any> {
        val account = parser.asNativeMap(state["account"])
        val subaccount = if (subaccountNumber != null) {
            parser.asNativeMap(
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
        val orderSize = parser.asDouble(triggerOrders?.get("size"))
        val stopLossOrder = parser.asNativeMap(triggerOrders?.get("stopLossOrder"))
        val takeProfitOrder = parser.asNativeMap(triggerOrders?.get("takeProfitOrder"))
        val position = parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId"))

        return if (triggerOrders != null && position != null) {
            val modified = state.mutable()
            val modifiedStopLossOrder = if (stopLossOrder != null) {
                calculateTriggerOrderTrade(stopLossOrder, position, orderSize)
            } else {
                stopLossOrder
            }
            val modifiedTakeProfitOrder = if (takeProfitOrder != null) {
                calculateTriggerOrderTrade(takeProfitOrder, position, orderSize)
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
        orderSize: Double?,
    ): Map<String, Any> {
        val modified = triggerOrder.mutable()
        val triggerPrices = parser.asNativeMap(triggerOrder["price"])?.let { calculateTriggerPrices(it, position, orderSize) }
        modified.safeSet("price", triggerPrices)

        return finalizeOrderFromPriceInputs(modified, position)
    }

    private fun calculateTriggerPrices(
        triggerPrices: Map<String, Any>,
        position: Map<String, Any>,
        orderSize: Double?,
    ): MutableMap<String, Any> {
        val modified = triggerPrices.mutable()
        val entryPrice = parser.asDouble(parser.value(position, "entryPrice.current"))
        val inputType = parser.asString(parser.value(modified, "input"))
        val positionSide = parser.asString(parser.value(position, "resources.indicator.current"))
        val absOrderSize = orderSize?.abs() ?: return modified

        if (entryPrice != null) {
            val triggerPrice = parser.asDouble(parser.value(modified, "triggerPrice"))
            val usdcDiff = parser.asDouble(parser.value(modified, "usdcDiff"))
            // val percentDiff = parser.asDouble(parser.value(modified, "percentDiff")) TODO: CT-694

            when (inputType) {
                "stopLossOrder.price.triggerPrice" -> {
                    modified.safeSet(
                        "usdcDiff",
                        if (triggerPrice != null) {
                            when (positionSide) {
                                "long" -> absOrderSize.times(entryPrice.minus(triggerPrice))
                                "short" -> absOrderSize.times(triggerPrice.minus(entryPrice))
                                else -> null
                            }
                        } else {
                            null
                        },
                    )
                }
                "takeProfitOrder.price.triggerPrice" -> {
                    modified.safeSet(
                        "usdcDiff",
                        if (triggerPrice != null) {
                            when (positionSide) {
                                "long" -> absOrderSize.times(triggerPrice.minus(entryPrice))
                                "short" -> absOrderSize.times(entryPrice.minus(triggerPrice))
                                else -> null
                            }
                        } else {
                            null
                        },
                    )
                }
                "stopLossOrder.price.usdcDiff" -> {
                    modified.safeSet(
                        "triggerPrice",
                        if (usdcDiff != null) {
                            when (positionSide) {
                                "long" -> entryPrice.minus(usdcDiff.div(absOrderSize))
                                "short" -> entryPrice.plus(usdcDiff.div(absOrderSize))
                                else -> null
                            }
                        } else {
                            null
                        },
                    )
                }
                "takeProfitOrder.price.usdcDiff" -> {
                    modified.safeSet(
                        "triggerPrice",
                        if (usdcDiff != null) {
                            when (positionSide) {
                                "long" -> entryPrice.plus(usdcDiff.div(absOrderSize))
                                "short" -> entryPrice.minus(usdcDiff.div(absOrderSize))
                                else -> null
                            }
                        } else {
                            null
                        },
                    )
                }
                "stopLossOrder.price.percentDiff",
                "takeProfitOrder.price.percentDiff" -> {
                    // TODO: CT-694
                }
                else -> {}
            }
        }
        return modified
    }

    private fun finalizeOrderFromPriceInputs(triggerOrder: Map<String, Any>, position: Map<String, Any>): MutableMap<String, Any> {
        val modified = triggerOrder.mutable()

        val side = getOrderSide(position)
        modified.safeSet("side", side?.rawValue)

        val type = getOrderType(triggerOrder)
        modified.safeSet("type", type?.rawValue)

        when (type) {
            OrderType.takeProfitMarket, OrderType.stopMarket -> {
                val triggerPrice =
                    parser.asDouble(parser.value(triggerOrder, "price.triggerPrice"))
                val majorMarket = when (parser.asString(triggerOrder["marketId"])) {
                    "BTC-USD", "ETH-USD" -> true
                    else -> false
                }
                val slippagePercentage = if (majorMarket) {
                    if (type == OrderType.stopMarket) {
                        STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                    } else {
                        TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                    }
                } else {
                    if (type == OrderType.stopMarket) {
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
            OrderType.takeProfitLimit, OrderType.stopLimit -> {
                modified.safeSet("summary.price", parser.asDouble(parser.value(triggerOrder, "price.limitPrice")))
            }
            else -> {}
        }
        return modified
    }

    private fun getOrderSide(
        position: Map<String, Any>,
    ): OrderSide? {
        val positionSide = parser.asString(parser.value(position, "resources.indicator.current"))

        return when (positionSide) {
            "short" -> OrderSide.buy
            "long" -> OrderSide.sell
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
                OrderType.takeProfitMarket, OrderType.takeProfitLimit -> OrderType.takeProfitLimit
                OrderType.stopMarket, OrderType.stopLimit -> OrderType.stopLimit
                else -> null
            }
        } else {
            return when (type) {
                OrderType.takeProfitMarket, OrderType.takeProfitLimit -> OrderType.takeProfitMarket
                OrderType.stopMarket, OrderType.stopLimit -> OrderType.stopMarket
                else -> null
            }
        }
    }
}
