package exchange.dydx.abacus.utils

import exchange.dydx.abacus.state.manager.HumanReadablePlaceOrderPayload

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
}