package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.modify
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.typedSafeSet

internal class OrdersProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private var itemProcessor = OrderProcessor(parser = parser)

    internal fun received(
        existing: Map<String, Any>?,
        payload: List<Any>?,
        height: BlockAndTime?,
        subaccountNumber: Int?,
    ): Map<String, Any>? {
        return if (payload != null) {
            val orders = existing?.mutable() ?: mutableMapOf<String, Any>()
            for (data in payload) {
                parser.asNativeMap(data)?.let { data ->
                    val orderId = parser.asString(data["id"] ?: data["clientId"])
                    val modified = data.toMutableMap()

                    subaccountNumber?.run {
                        modified.safeSet("subaccountNumber", this)
                    }

                    if (orderId != null) {
                        val existing = parser.asNativeMap(orders[orderId])
                        val order = itemProcessor.received(existing, data, height)
                        orders.typedSafeSet(orderId, order)
                    }
                }
            }

            orders
        } else {
            existing
        }
    }

    internal fun updateHeight(
        existing: Map<String, Any>,
        height: BlockAndTime?
    ): Pair<Map<String, Any>, Boolean> {
        var updated = false
        val modified = existing.mutable()
        for ((key, item) in existing) {
            val order = parser.asNativeMap(item)
            if (order != null) {
                val (modifiedOrder, orderUpdated) = itemProcessor.updateHeight(order, height)
                if (orderUpdated) {
                    modified[key] = modifiedOrder
                    updated = orderUpdated
                }
            }
        }

        return Pair(modified, updated)
    }

    internal fun canceled(
        existing: Map<String, Any>,
        orderId: String,
    ): Pair<Map<String, Any>, Boolean> {
        val order = parser.asNativeMap(existing.get(orderId))
        return if (order != null) {
            val modified = existing.mutable()
            itemProcessor.canceled(order)
            modified.typedSafeSet(orderId, order)
            Pair(modified, true)
        } else {
            Pair(existing, false)
        }
    }
}
