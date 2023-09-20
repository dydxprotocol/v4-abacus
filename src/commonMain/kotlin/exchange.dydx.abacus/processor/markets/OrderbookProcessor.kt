package exchange.dydx.abacus.processor.markets

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.ComparisonOrder
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMutableListOf
import numberOfDecimals
import tickDecimals

@Suppress("UNCHECKED_CAST")
internal class OrderbookProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private var entryProcessor = OrderbookEntryProcessor(parser = parser)
    internal var groupingMultiplier: Int = 1

    private var groupingTickSize: Double? = null
    private var groupingLookup: MutableMap<Int, BigDecimal>? = null

    private var lastOffset: Long = 0

    internal fun subscribed(
        content: IMap<String, Any>
    ): IMap<String, Any> {
        return received(null, content)
    }

    internal fun channel_batch_data(
        existing: IMap<String, Any>?,
        content: IList<Any>
    ): IMap<String, Any>? {
        val orderbook = receivedBatchedChanges(existing, content)
        return if (orderbook != null) calculate(orderbook) else null
    }

    /*
    asks: list of low to high
    bids: list of high to low
     */
    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val orderbook = existing?.mutable() ?: iMutableMapOf<String, Any>()
        val asks = parser.asList(payload["asks"])?.map {
            parser.asMap(it)?.let {
                entryProcessor.received(null, it)
            }
        }
        orderbook.safeSet("asks", asks)
        val bids = parser.asList(payload["bids"])?.map {
            parser.asMap(it)?.let {
                entryProcessor.received(null, it)
            }
        }
        orderbook.safeSet("bids", bids)
        lastOffset = 0
        return orderbook
    }

    private fun receivedBatchedChanges(
        existing: IMap<String, Any>?,
        payload: IList<Any>
    ): IMap<String, Any>? {
        var orderbook = existing
        for (change in payload) {
            orderbook = receivedChanges(orderbook, parser.asMap(change))
        }

        return if (orderbook != null) calculate(orderbook) else null
    }

    private fun receivedChanges(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>?
    ): IMap<String, Any>? {
        if (payload != null) {
            val orderbook = existing?.mutable() ?: iMutableMapOf()
            val offset = parser.asLong(payload["offset"]) ?: (lastOffset + 1)
            orderbook["asks"] = receivedChanges(
                orderbook["asks"] as? IList<IMap<String, Any>>,
                parser.asList(payload["asks"] ?: payload["ask"]),
                offset,
                true
            )

            orderbook["bids"] = receivedChanges(
                orderbook["bids"] as? IList<IMap<String, Any>>,
                parser.asList(payload["bids"] ?: payload["bid"]),
                offset,
                false
            )
            lastOffset = offset
            return orderbook
        }
        return existing
    }

    private fun receivedChanges(
        existing: IList<IMap<String, Any>>?,
        changes: IList<Any>?,
        offset: Long?,
        ascending: Boolean
    ): IList<IMap<String, Any>> {
        if (changes == null || changes.size == 0) {
            return existing ?: iMutableListOf()
        }

        val bindaryResult = receivedChangesBinary(existing, changes, offset, ascending)
        /*
        k = Number of channel_data contained in channel_batched_data
        g = Number of changed items in channel_data
        N = Number of items in the orderbook
        History: It made sense to use linear algorithm for channel_data, with O(N) regardless
        how big g is, because calculating depth can be done in the same loop.
        With channel_batched_data, it became
            O(k*N) with linear search
            O(k*g*log(N)) with binary search
        For v3, with N around 50, and g around 3, the difference is not that much.
        For v4, with N around 300+, g almost always 1, it is worth the effort to use binary search

        Keep the old code here, commented out, for reference and for testing
        */
        /*
        val linearResult = receivedChangesLinear(existing, changes, offset, ascending)
        compareResults(linearResult, bindaryResult)
         */
        return bindaryResult
    }

    private fun receivedChangesBinary(
        existing: IList<IMap<String, Any>>?,
        changes: IList<Any>,
        offset: Long?,
        ascending: Boolean
    ): IList<IMap<String, Any>> {
        val comparator = compareBy<IMap<String, Any>> {
            val price = parser.asDouble(it["price"])
            if (price != null) {
                if (ascending) price else (price * Numeric.double.NEGATIVE)
            } else null
        }
        var orderbook = existing?.mutable() ?: iMutableListOf()
        for (change in changes) {
            orderbook = receiveChangeBinary(orderbook, change, offset, comparator)
        }
        return orderbook
    }


    private fun receiveChangeBinary(
        existing: IList<IMap<String, Any>>,
        change: Any,
        offset: Long?,
        comparator: Comparator<IMap<String, Any>>
    ): IMutableList<IMap<String, Any>> {
        val orderbook = existing.mutable()
        val price = entryPrice(change)
        val size = entrySize(change)
        if (price != null && size != null) {
            val item = iMutableMapOf<String, Any>("price" to price, "size" to size)
            item.safeSet("offset", offset)
            val index = existing.binarySearch(item, comparator)
            if (index >= 0) {
                // found the item
                val existing = orderbook[index]
                if (offset != null) {
                    // v3, keep size of 0
                    val existingOffset = parser.asLong(existing["offset"]) ?: 0
                    if (offset > existingOffset) {
                        orderbook.removeAt(index)
                        orderbook.add(index, item)
                    }
                } else {
                    if (size != Numeric.double.ZERO) {
                        orderbook.removeAt(index)
                        orderbook.add(index, item)
                    } else {
                        orderbook.removeAt(index)
                    }
                }
            } else {
                if (size != Numeric.double.ZERO) {
                    val insertionIndex = (index + 1) * -1
                    orderbook.add(insertionIndex, item)
                }
            }
        }
        return orderbook
    }

    @Throws(Exception::class)
    private fun compareResults(
        result1: IList<IMap<String, Any>>,
        result2: IList<IMap<String, Any>>
    ) {
        if (result1.size == result2.size) {
            for (i in 0 until result2.size) {
                val item1 = result1[i]
                val item2 = result2[i]
                if (parser.asDouble(item1["size"]) != parser.asDouble(item2["size"]) ||
                    parser.asDouble(item1["price"]) != parser.asDouble(item2["price"]) ||
                    parser.asDouble(item1["offset"]) != parser.asDouble(item2["offset"])
                ) {
                    throw Exception()
                }
            }
        } else {
            throw Exception()
        }
    }

    private fun receivedChangesLinear(
        existing: IList<IMap<String, Any>>?,
        changes: IList<Any>,
        offset: Long?,
        ascending: Boolean
    ): IList<IMap<String, Any>> {
        val size1 = existing?.size ?: 0
        val size2 = changes.size ?: 0
        var cursor1 = 0
        var cursor2 = 0

        val result = iMutableListOf<IMap<String, Any>>()

        if (existing != null) {
            while (cursor1 < size1 && cursor2 < size2) {
                val existingEntry = existing[cursor1]
                val existingPrice = parser.asDouble(existingEntry["price"])
                if (existingPrice != null) {
                    val changeEntry = changes[cursor2]
                    val price = entryPrice(changeEntry)
                    val size = entrySize(changeEntry)
                    if (price != null && size != null) {
                        when (compare(existingPrice, price, ascending)) {
                            ComparisonOrder.same -> {
                                val entry = entryProcessor.receivedDelta(
                                    existingEntry,
                                    offset,
                                    price,
                                    size
                                )
                                if (entry != null) {
                                    result.add(entry)
                                }
                                cursor1 += 1
                                cursor2 += 1
                            }

                            ComparisonOrder.ascending -> {
                                result.add(existingEntry)
                                cursor1 += 1
                            }

                            ComparisonOrder.descending -> {
                                val entry = entryProcessor.receivedDelta(null, offset, price, size)
                                if (entry != null) {
                                    result.add(entry)
                                }
                                cursor2 += 1
                            }
                        }
                    } else {
                        cursor2 += 1
                    }
                } else {
                    cursor1 += 1
                }
            }
        }

        if (cursor1 >= size1) {
            // list1 finished
            for (i in cursor2 until size2) {
                val changeEntry = changes?.get(i)
                if (changeEntry != null) {
                    val price = entryPrice(changeEntry)
                    val size = entrySize(changeEntry)
                    if (price != null && size != null) {
                        val entry = entryProcessor.receivedDelta(null, offset, price, size)
                        if (entry != null) {
                            result.add(entry)
                        }
                    }
                }
            }
        }
        if (cursor2 >= size2) {
            for (i in cursor1 until size1) {
                val entry = existing?.get(i)
                if (entry != null) {
                    result.add(entry)
                }
            }
        }

        return result
    }

    private fun entryPrice(entry: Any): Double? {
        return parser.asDouble(
            parser.asMap(entry)?.get("price") ?: parser.asList(entry)?.getOrNull(0)
        )
    }

    private fun entrySize(entry: Any): Double? {
        return parser.asDouble(
            parser.asMap(entry)?.get("size") ?: parser.asList(entry)?.getOrNull(1)
        )
    }

    private fun compare(price1: Double, price2: Double, ascending: Boolean): ComparisonOrder {
        return if (price1 == price2) {
            ComparisonOrder.same
        } else {
            if (ascending) {
                if (price2 > price1) ComparisonOrder.ascending else ComparisonOrder.descending
            } else {
                if (price2 > price1) ComparisonOrder.descending else ComparisonOrder.ascending
            }
        }
    }

    private fun calculate(orderbook: IMap<String, Any>): IMap<String, Any> {
        var asksDepth = 0.0
        val asks = parser.asList(orderbook.get("asks"))?.map { ask ->
            val map = parser.asMap(ask)!!
            val size = parser.asDouble(map["size"]) ?: 0.0
            asksDepth += size
            val modified = map.mutable()
            modified["depth"] = asksDepth
            modified
        }
        var bidsDepth = 0.0
        val bids = parser.asList(orderbook.get("bids"))?.map { bid ->
            val map = parser.asMap(bid)!!
            val size = parser.asDouble(map["size"]) ?: 0.0
            bidsDepth += size
            val modified = map.mutable()
            modified["depth"] = bidsDepth
            modified
        }
        val firstAsk = parser.asDouble(parser.asMap(asks?.firstOrNull { item ->
            val size = parser.asDouble(parser.asMap(item)?.get("size"))
            (size != 0.0)
        })?.get("price"))
        val firstBid = parser.asDouble(parser.asMap(bids?.firstOrNull { item ->
            val size = parser.asDouble(parser.asMap(item)?.get("size"))
            (size != 0.0)
        })?.get("price"))
        val modified = orderbook.mutable()
        if (firstAsk != null && firstBid != null) {
            val firstAskPrice = firstAsk
            val midPrice = (firstAskPrice + firstBid) / 2.0
            val spread = firstAskPrice.minus(midPrice)
            val spreadPercent = spread / midPrice
            modified.safeSet("midPrice", midPrice)
            modified.safeSet("spreadPercent", spreadPercent)
            modified.safeSet("asks", asks)
            modified.safeSet("bids", bids)
        } else {
            modified.safeSet("midPrice", null)
            modified.safeSet("spreadPercent", null)
            modified.safeSet("asks", asks)
            modified.safeSet("bids", bids)
        }
        return modified
    }

    fun consolidate(orderbook: IMap<String, Any>?, stepSize: Double): IMap<String, Any>? {
        /*
        val stepSizeDecimals = stepSize.numberOfDecimals()
        val asks = parser.asList(orderbook?.get("asks"))?.mutable()
        val bids = parser.asList(orderbook?.get("bids"))?.mutable()
        var ask = parser.asMap(asks?.firstOrNull())
        var bid = parser.asMap(bids?.firstOrNull())
        return if (asks != null && bids != null && ask != null && bid != null) {
            asks.removeFirstOrNull()
            bids.removeFirstOrNull()
            var askPrice = parser.asDouble(ask["price"])
            var askSize = parser.asDouble(ask["size"]) ?: Numeric.double.ZERO
            var bidPrice = parser.asDouble(bid["price"])
            var bidSize = parser.asDouble(bid["size"]) ?: Numeric.double.ZERO
            while (askPrice != null && bidPrice != null && askPrice <= bidPrice) {
                if (askSize > bidSize) {
                    askSize -= bidSize
                    askSize = Rounder.quickRound(askSize, stepSize, stepSizeDecimals)
                    bid = parser.asMap(bids.firstOrNull())
                    bids.removeFirstOrNull()
                    bidPrice = parser.asDouble(bid?.get("price"))
                    bidSize = parser.asDouble(bid?.get("size")) ?: Numeric.double.ZERO
                } else {
                    bidSize -= askSize
                    bidSize = Rounder.quickRound(bidSize, stepSize, stepSizeDecimals)
                    ask = parser.asMap(asks.firstOrNull())
                    asks.removeFirstOrNull()
                    askPrice = parser.asDouble(ask?.get("price"))
                    askSize = parser.asDouble(ask?.get("size")) ?: Numeric.double.ZERO
                }
            }
            if (ask != null && askSize != null && askSize != Numeric.double.ZERO) {
                val ask = ask.mutable()
                ask["size"] = askSize
                asks.add(0, ask)
            }
            if (bid != null && bidSize != null && bidSize != Numeric.double.ZERO) {
                val bid = bid.mutable()
                bid["size"] = bidSize
                bids.add(0, bid)
            }
            calculate(iMapOf("asks" to asks, "bids" to bids))
        } else orderbook
        */

        val asks = parser.asList(orderbook?.get("asks"))?.mutable()
        val bids = parser.asList(orderbook?.get("bids"))?.mutable()
        return if (asks != null && bids != null && asks.size > 0 && bids.size > 0) {
            var ask = parser.asMap(asks.firstOrNull())
            var bid = parser.asMap(bids.firstOrNull())
            while (ask != null && bid != null && crossed(ask, bid)) {
                val askOffset = parser.asLong(ask["offset"]) ?: 0L
                val bidOffset = parser.asLong(bid["offset"]) ?: 0L
                // Drop the order on the side with the lower offset.
                // The offset of the other side is higher and so supercedes.
                if (askOffset == bidOffset) {
                    // If offsets are the same, give precedence to the larger size. In this case,
                    // one of the sizes *should* be zero, but we simply check for the larger size.
                    val askSize = parser.asDouble(ask["size"]) ?: Numeric.double.ZERO
                    val bidSize = parser.asDouble(bid["size"]) ?: Numeric.double.ZERO
                    if (askSize >= bidSize) {
                        bids.removeFirst()
                        bid = parser.asMap(bids.firstOrNull())
                    } else {
                        asks.removeFirst()
                        ask = parser.asMap(asks.firstOrNull())
                    }
                } else {
                    // Offsets are not equal. Give precedence to the larger offset.
                    if (askOffset > bidOffset) {
                        bids.removeFirst()
                        bid = parser.asMap(bids.firstOrNull())
                    } else {
                        asks.removeFirst()
                        ask = parser.asMap(asks.firstOrNull())
                    }
                }
            }
            calculate(iMapOf("asks" to asks, "bids" to bids))
        } else orderbook
    }

    private fun crossed(ask: Map<String, Any>, bid: Map<String, Any>): Boolean {
        val askPrice = parser.asDouble(ask["price"])
        val bidPrice = parser.asDouble(bid["price"])
        return if (askPrice != null && bidPrice != null) {
            askPrice <= bidPrice
        } else false
    }

    private fun buildGroupingLookup(tickSize: Double) {
        if (groupingTickSize != tickSize) {
            groupingTickSize = tickSize
            groupingLookup = iMutableMapOf()
        }
    }

    fun group(orderbook: IMap<String, Any>?, tickSize: Double): IMap<String, Any>? {
        buildGroupingLookup(tickSize)
        return if (orderbook != null) {
            if (groupingMultiplier != 1) {
                val groupingTickSize = grouping(tickSize, groupingMultiplier)
                val modified = orderbook.mutable()
                modified.safeSet("asks", group(parser.asList(modified["asks"]), groupingTickSize))
                modified.safeSet("bids", group(parser.asList(modified["bids"]), groupingTickSize))
                modified.safeSet(
                    "grouping",
                    iMapOf("tickSize" to groupingTickSize, "multiplier" to groupingMultiplier)
                )
                modified
            } else {
                val groupingTickSize = grouping(tickSize, groupingMultiplier)
                val modified = orderbook.mutable()
                modified.safeSet(
                    "grouping",
                    iMapOf("tickSize" to groupingTickSize, "multiplier" to groupingMultiplier)
                )
                modified
            }
        } else orderbook
    }

    fun group(orderbook: IList<Any>?, grouping: BigDecimal): IList<Any>? {
        return if (orderbook != null && orderbook.size > 0) {
            val firstPrice = parser.asDecimal(parser.value(orderbook.firstOrNull(), "price"))!!
            var floor = Rounder.roundDecimal(firstPrice, grouping).doubleValue(false)
            var ceiling = (parser.asDecimal(floor)!! + grouping).doubleValue(false)
            var size = Numeric.double.ZERO
            var depth = Numeric.double.ZERO
            val result = iMutableListOf<IMap<String, Any>>()

            for (item in orderbook) {
                val line = parser.asMap(item)
                val linePrice = parser.asDouble(line?.get("price"))
                if (linePrice != null) {
                    val lineSize = parser.asDouble(line?.get("size")) ?: Numeric.double.ZERO
                    val lineDepth = parser.asDouble(line?.get("depth")) ?: Numeric.double.ZERO
                    if (linePrice >= floor) {
                        if (linePrice < ceiling) {
                            size += lineSize
                            depth += lineDepth
                        } else {
                            result.add(iMapOf("price" to floor, "size" to size, "depth" to depth))
                            floor = ceiling
                            ceiling = (parser.asDecimal(floor)!! + grouping).doubleValue(false)
                        }
                    } else {
                        result.add(iMapOf("price" to floor, "size" to size, "depth" to depth))
                        ceiling = floor
                        floor = (parser.asDecimal(ceiling)!! - grouping).doubleValue(false)
                    }
                }
            }
            val item = iMapOf("price" to floor, "size" to size, "depth" to depth)
            result.add(item)

            return result
        } else null
    }

    private fun grouping(tickSize: Double, grouping: Int): BigDecimal {
        val cached = groupingLookup?.get(grouping)
        return if (cached != null) {
            cached
        } else {
            val decimals = if (grouping == 1)
                tickSize.tickDecimals()
            else {
                val tickDecimals = tickSize.tickDecimals()
                tickDecimals * grouping.toBigDecimal(null, Numeric.decimal.mode)
            }
            if (groupingLookup == null) {
                groupingLookup = iMutableMapOf()
            }
            groupingLookup?.set(grouping, decimals)
            decimals
        }
    }
}