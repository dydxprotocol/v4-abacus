package exchange.dydx.abacus.calculator

import abs
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class TriggerOrdersInputCalculator(val parser: ParserProtocol) {
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
        val stopLossOrder = parser.asNativeMap(triggerOrders?.get("stopLossOrder"))
        val takeProfitOrder = parser.asNativeMap(triggerOrders?.get("takeProfitOrder"))
        val position = parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId"))

        return if (triggerOrders != null && position != null) {
            val modified = state.mutable()
            val modifiedStopLossOrder = if (stopLossOrder != null) {
                calculateTriggerOrderTrade(stopLossOrder, position)
            } else {
                stopLossOrder
            }
            val modifiedTakeProfitOrder = if (takeProfitOrder != null) {
                calculateTriggerOrderTrade(takeProfitOrder, position)
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
    ): Map<String, Any> {
        val modified = calculatePrice(triggerOrder, position)
        val side = getOrderSide(position)
        modified.safeSet("side", side?.rawValue)
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

    private fun calculatePrice(
        triggerOrder: Map<String, Any>,
        position: Map<String, Any>,
    ): MutableMap<String, Any> {
        val modified = triggerOrder.mutable()
        val entryPrice = parser.asDouble(parser.value(position, "entryPrice.current"))
        val inputType = parser.asString(parser.value(modified, "price.input"))

        if (entryPrice != null) {
            val triggerPrice = parser.asDouble(parser.value(modified, "price.triggerPrice"))
            val usdcDiff = parser.asDouble(parser.value(modified, "price.usdcDiff"))
            val percentDiff = parser.asDouble(parser.value(modified, "price.percentDiff"))

            when (inputType) {
                "stopLossOrder.price.triggerPrice" -> {
                    modified.safeSet(
                        "price.usdcDiff",
                        if (triggerPrice != null) entryPrice.minus(triggerPrice).abs() else null,
                    )
                    modified.safeSet(
                        "price.percentDiff",
                        if (triggerPrice != null) Numeric.double.ONE.minus(triggerPrice.div(entryPrice)) else null,
                    )
                }
                "takeProfitOrder.price.triggerPrice" -> {
                    modified.safeSet(
                        "price.usdcDiff",
                        if (triggerPrice != null) entryPrice.minus(triggerPrice).abs() else null,
                    )
                    modified.safeSet(
                        "price.percentDiff",
                        if (triggerPrice != null) triggerPrice.div(entryPrice).minus(Numeric.double.ONE) else null,
                    )
                }
                "stopLossOrder.price.usdcDiff" -> {
                    modified.safeSet(
                        "price.triggerPrice",
                        if (usdcDiff != null) entryPrice.minus(usdcDiff) else null,
                    )
                    modified.safeSet(
                        "price.percentDiff",
                        if (usdcDiff != null) usdcDiff.div(entryPrice) else null,
                    )
                }
                "takeProfitOrder.price.usdcDiff" -> {
                    modified.safeSet(
                        "price.triggerPrice",
                        if (usdcDiff != null) entryPrice.plus(usdcDiff) else null,
                    )
                    modified.safeSet(
                        "price.percentDiff",
                        if (usdcDiff != null) usdcDiff.div(entryPrice) else null,
                    )
                }
                "stopLossOrder.price.percentDiff" -> {
                    modified.safeSet(
                        "price.triggerPrice",
                        if (percentDiff != null) entryPrice * Numeric.double.ONE.minus(percentDiff) else null,
                    )
                    modified.safeSet(
                        "price.usdcDiff",
                        if (percentDiff != null) entryPrice * percentDiff else null,
                    )
                }
                "takeProfitOrder.price.percentDiff" -> {
                    modified.safeSet(
                        "price.triggerPrice",
                        if (percentDiff != null) entryPrice * Numeric.double.ONE.plus(percentDiff) else null,
                    )
                    modified.safeSet(
                        "price.usdcDiff",
                        if (percentDiff != null) entryPrice * percentDiff else null,
                    )
                }
                else -> {}
            }
        }
        return modified
    }
}
