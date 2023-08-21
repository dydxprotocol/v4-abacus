package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.typedSafeSet

internal class OrdersProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private var itemProcessor = OrderProcessor(parser = parser)

    internal fun received(
        existing: IMap<String, Any>?,
        payload: IList<Any>?,
        height: Int?
    ): IMap<String, Any>? {
        return if (payload != null) {
            val orders = existing?.mutable() ?: iMutableMapOf<String, Any>()
            for (data in payload) {
                parser.asMap(data)?.let { data ->
                    val orderId = parser.asString(data["id"] ?: data["clientId"])
                    if (orderId != null) {
                        val existing = parser.asMap(orders[orderId])
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

    override fun received(
        existing: IMap<String, Any>,
        height: Int?
    ): Pair<IMap<String, Any>, Boolean> {
        var updated = false
        val modified = existing.mutable()
        for ((key, item) in existing) {
            val order = parser.asMap(item)
            if (order != null) {
                val (modifiedOrder, orderUpdated) = itemProcessor.received(order, height)
                if (orderUpdated) {
                    modified[key] = modifiedOrder
                    updated = orderUpdated
                }
            }
        }

        return Pair(modified, updated)
    }


    internal fun canceled(
        existing: IMap<String, Any>,
        orderId: String,
    ): Pair<IMap<String, Any>, Boolean> {
        val order = parser.asMap(existing.get(orderId))
        return if (order != null) {
            val modified = existing.mutable()
            itemProcessor.canceled(order)
            modified.typedSafeSet(orderId, order)
            Pair(modified, true)
        } else Pair(existing, false)
    }
}
