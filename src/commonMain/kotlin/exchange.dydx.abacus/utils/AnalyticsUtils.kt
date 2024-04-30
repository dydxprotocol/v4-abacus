package exchange.dydx.abacus.utils

import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.manager.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload
import exchange.dydx.abacus.state.manager.HumanReadableTriggerOrdersPayload
import kollections.JsExport
import kollections.toIMap
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class TriggerOrderAction(val rawValue: String) {
    REPLACE("REPLACE"),
    CANCEL("CANCEL"),
    CREATE("CREATE"),
    ;

    companion object {
        operator fun invoke(rawValue: String?) =
            TriggerOrderAction.values().firstOrNull { it.rawValue == rawValue }
    }
}

class AnalyticsUtils {
    /**
     * Format Trigger Orders Payload and add additional details for `TriggerOrders` Analytic Events
     * @param payload HumanReadableTriggerOrdersPayload
     */

    /**
     * Format Place Order Payload for `TriggerOrders` Analytic Event
     * @param payload HumanReadableTriggerOrdersPayload
     */
    fun triggerOrdersAnalyticsPayload(
        payload: HumanReadableTriggerOrdersPayload,
    ): IMap<String, Any>? {
        val placeOrderPayloads = payload.placeOrderPayloads
        val cancelOrderPayloads = payload.cancelOrderPayloads

        val stopLossOrderTypes = listOf(OrderType.stopMarket, OrderType.stopLimit)
        val takeProfitOrderTypes = listOf(OrderType.takeProfitMarket, OrderType.takeProfitLimit)

        var stopLossOrderAction: TriggerOrderAction? = null
        var takeProfitOrderAction: TriggerOrderAction? = null

        placeOrderPayloads.forEach { placePayload ->
            val orderType = OrderType.invoke(placePayload.type)
            if (stopLossOrderTypes.contains(orderType)) {
                stopLossOrderAction = TriggerOrderAction.CREATE
            } else if (takeProfitOrderTypes.contains(orderType)) {
                takeProfitOrderAction = TriggerOrderAction.CREATE
            }
        }

        cancelOrderPayloads.forEach { cancelPayload ->
            val orderType = OrderType.invoke(cancelPayload.type)
            if (stopLossOrderTypes.contains(orderType)) {
                stopLossOrderAction = if (stopLossOrderAction == null) {
                    TriggerOrderAction.CANCEL
                } else {
                    TriggerOrderAction.REPLACE
                }
            } else if (takeProfitOrderTypes.contains(orderType)) {
                takeProfitOrderAction = if (takeProfitOrderAction == null) {
                    TriggerOrderAction.CANCEL
                } else {
                    TriggerOrderAction.REPLACE
                }
            }
        }

        return iMapOf(
            "marketId" to payload.marketId,
            "positionSize" to payload.positionSize,
            "stopLossOrderAction" to stopLossOrderAction?.rawValue,
            "takeProfitOrderAction" to takeProfitOrderAction?.rawValue,
        ) as IMap<String, Any>?
    }

    /**
     * Format Place Order Payload and add additional details for `TradePlaceOrder` Analytic Events
     * @param payload HumanReadablePlaceOrderPayload
     * @param midMarketPrice Double?
     * @param isClosePosition Boolean?
     */
    fun placeOrderAnalyticsPayload(
        payload: HumanReadablePlaceOrderPayload,
        midMarketPrice: Double?,
        isClosePosition: Boolean? = false,
    ): IMap<String, Any>? {
        return ParsingHelper.merge(
            formatPlaceOrderPayload(payload, isClosePosition),
            iMapOf(
                "inferredTimeInForce" to calculateOrderTimeInForce(payload),
                "midMarketPrice" to midMarketPrice,
            ) as IMap<String, Any>?,
        )?.toIMap()
    }

    /**
     * Format Place Order Payload for `TradePlaceOrder` Analytic Event
     * @param payload HumanReadablePlaceOrderPayload
     * @param isClosePosition Boolean
     */
    private fun formatPlaceOrderPayload(
        payload: HumanReadablePlaceOrderPayload,
        isClosePosition: Boolean? = false,
    ): IMap<String, Any>? {
        return iMapOf(
            "clientId" to payload.clientId,
            "currentHeight" to payload.currentHeight,
            "execution" to payload.execution,
            "goodTilTimeInSeconds" to payload.goodTilTimeInSeconds,
            "goodTilBlock" to payload.goodTilBlock,
            "isClosePosition" to isClosePosition,
            "marketId" to payload.marketId,
            "postOnly" to payload.postOnly,
            "price" to payload.price,
            "reduceOnly" to payload.reduceOnly,
            "side" to payload.side,
            "size" to payload.size,
            "subaccountNumber" to payload.subaccountNumber,
            "timeInForce" to payload.timeInForce,
            "triggerPrice" to payload.triggerPrice,
            "type" to payload.type,
        ) as IMap<String, Any>?
    }

    /**
     * Infer time in force from order params for analytics, mirroring v4-clients
     * @param payload HumanReadablePlaceOrderPayload
     */
    private fun calculateOrderTimeInForce(
        payload: HumanReadablePlaceOrderPayload
    ): String? {
        return when (payload.type) {
            "MARKET" -> payload.timeInForce ?: "FOK"
            "LIMIT" -> {
                when (payload.timeInForce) {
                    "GTT" -> if (payload.postOnly == true) "POST_ONLY" else "GTT"
                    else -> payload.timeInForce
                }
            }

            "STOP_LIMIT", "TAKE_PROFIT" -> {
                when (payload.execution) {
                    "DEFAULT" -> "GTT"
                    else -> payload.execution
                }
            }

            "STOP_MARKET", "TAKE_PROFIT_MARKET" -> payload.execution
            else -> payload.timeInForce ?: payload.execution
        }
    }

    /**
     * Format Cancel Order Payload and add order details for `TradeCancelOrder` Analytic Events
     * @param payload HumanReadableCancelOrderPayload
     * @param existingOrder SubaccountOrder?
     */
    fun cancelOrderAnalyticsPayload(
        payload: HumanReadableCancelOrderPayload,
        existingOrder: SubaccountOrder?,
    ): IMap<String, Any>? {
        return ParsingHelper.merge(
            formatCancelOrderPayload(payload),
            if (existingOrder != null) formatOrder(existingOrder) else mapOf(),
        )?.toIMap()
    }

    private fun formatCancelOrderPayload(
        payload: HumanReadableCancelOrderPayload,
    ): IMap<String, Any>? {
        return iMapOf(
            "subaccountNumber" to payload.subaccountNumber,
            "clientId" to payload.clientId,
            "orderId" to payload.orderId,
            "orderFlags" to payload.orderFlags,
            "clobPairId" to payload.clobPairId,
            "goodTilBlock" to payload.goodTilBlock,
            "goodTilBlockTime" to payload.goodTilBlockTime,
        ) as IMap<String, Any>?
    }

    /**
     * Format SubaccountOrder for analytic events
     * @param order SubaccountOrder
     */
    fun formatOrder(order: SubaccountOrder): IMap<String, Any>? {
        return iMapOf(
            "orderId" to order.id,
            "clientId" to order.clientId,
            "type" to order.type.rawValue,
            "side" to order.side.rawValue,
            "status" to order.status.rawValue,
            "timeInForce" to order.timeInForce?.rawValue,
            "marketId" to order.marketId,
            "clobPairId" to order.clobPairId,
            "orderFlags" to order.orderFlags,
            "price" to order.price,
            "triggerPrice" to order.triggerPrice,
            "trailingPercent" to order.trailingPercent,
            "size" to order.size,
            "remainingSize" to order.remainingSize,
            "totalFilled" to order.totalFilled,
            "goodTilBlock" to order.goodTilBlock,
            "goodTilBlockTime" to order.goodTilBlockTime,
            "createdAtHeight" to order.createdAtHeight,
            "createdAtMilliseconds" to order.createdAtMilliseconds,
            "unfillableAtMilliseconds" to order.unfillableAtMilliseconds,
            "expiresAtMilliseconds" to order.expiresAtMilliseconds,
            "updatedAtMilliseconds" to order.updatedAtMilliseconds,
            "postOnly" to order.postOnly,
            "reduceOnly" to order.reduceOnly,
            "cancelReason" to order.cancelReason,
        ) as IMap<String, Any>?
    }
}
