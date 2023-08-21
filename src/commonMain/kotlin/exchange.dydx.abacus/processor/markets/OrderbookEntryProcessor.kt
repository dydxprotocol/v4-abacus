package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class OrderbookEntryProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val orderbookEntryKeyMap = iMapOf(
        "decimal" to iMapOf(
            "size" to "size",
            "price" to "price"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val received = transform(existing, payload, orderbookEntryKeyMap)
        received["offset"] = 0.toLong()
        return received
    }

    internal fun receivedDelta(
        entry: IMap<String, Any>?,
        offset: Long?,
        price: Double,
        size: Double
    ): IMap<String, Any>? {
        val oldOffset = parser.asLong(entry?.get("offset")) ?: 0
        if ((offset == null && size > 0.0) || (offset != null && offset > oldOffset)) {
            val received = entry?.mutable() ?: iMutableMapOf<String, Any>()
            received["price"] = price
            received["size"] = size
            received.safeSet("offset", offset)
            return received
        }
        /*
         offset is not null -> it is v3
         in V3, we need to use previous entry because offset may be out of order
         No need to worry about that for v4
         */
        return if (offset == null) null else entry
    }
}