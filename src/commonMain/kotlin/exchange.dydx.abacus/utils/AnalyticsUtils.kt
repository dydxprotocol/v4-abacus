package exchange.dydx.abacus.utils

import exchange.dydx.abacus.output.SubaccountOrder
import exchange.dydx.abacus.state.manager.utils.HumanReadableCancelOrderPayload
import exchange.dydx.abacus.state.manager.utils.HumanReadablePlaceOrderPayload

class AnalyticsUtils {
    /**
     * Format Place Order Payload for `TradePlaceOrder` Analytic Event
     * @param payload HumanReadablePlaceOrderPayload
     * @param isClosePosition Boolean
     */
    fun formatPlaceOrderPayload(payload: HumanReadablePlaceOrderPayload, isClosePosition: Boolean? = false): IMap<String, Any>? {
        return iMapOf(
            "clientId" to payload.clientId,
            "currentHeight" to payload.currentHeight,
            "execution" to payload.execution,
            "goodTilTimeInSeconds" to payload.goodTilTimeInSeconds,
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
     * Format Cancel Order Payload for `TradeCancelOrder` Analytic Event
     * @param payload HumanReadableCancelOrderPayload
     */
    fun formatCancelOrderPayload(payload: HumanReadableCancelOrderPayload): IMap<String, Any>? {
        return iMapOf(
            "subaccountNumber" to payload.subaccountNumber,
            "clientId" to payload.clientId,
            "orderId" to payload.orderId,
            "orderFlags" to payload.orderFlags,
            "clobPairId" to payload.clobPairId,
            "goodTilBlock" to payload.goodTilBlock,
            "goodTilBlockTime" to payload.goodTilBlockTime,
        )  as IMap<String, Any>?
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