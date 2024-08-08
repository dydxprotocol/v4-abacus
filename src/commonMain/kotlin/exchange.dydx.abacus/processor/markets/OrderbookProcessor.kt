package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.calculator.OrderbookCalculator
import exchange.dydx.abacus.calculator.OrderbookCalculatorProtocol
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.ComparisonOrder
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalOrderbook
import exchange.dydx.abacus.state.internalstate.InternalOrderbookTick
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerOrderbookResponseObject
import indexer.codegen.IndexerOrderbookResponsePriceLevel
import indexer.models.IndexerWsOrderbookUpdateItem
import indexer.models.IndexerWsOrderbookUpdateResponse
import indexer.models.getPrice
import indexer.models.getSize
import tickDecimals

@Suppress("UNCHECKED_CAST")
internal class OrderbookProcessor(
    parser: ParserProtocol,
    private val calculator: OrderbookCalculatorProtocol = OrderbookCalculator(parser),
) : BaseProcessor(parser) {
    private var entryProcessor = OrderbookEntryProcessor(parser = parser)
    internal var groupingMultiplier: Int = 1

    private var groupingTickSize: Double? = null
    private var groupingLookup: MutableMap<Int, Double>? = null

    private var lastOffset: Long = 0

    private val defaultTickSize = 0.1

    fun processSubscribed(
        existing: InternalMarketState,
        tickSize: Double?,
        content: IndexerOrderbookResponseObject?,
    ): InternalMarketState {
        existing.rawOrderbook = InternalOrderbook(
            asks = content?.asks?.mapNotNull {
                createTick(it)
            },
            bids = content?.bids?.mapNotNull {
                createTick(it)
            },
        )
        existing.consolidatedOrderbook = calculator.consolidate(
            rawOrderbook = existing.rawOrderbook,
        )
        existing.groupedOrderbook = calculator.calculate(
            rawOrderbook = existing.rawOrderbook,
            tickSize = tickSize ?: defaultTickSize,
            groupingMultiplier = groupingMultiplier,
        )
        return existing
    }

    fun processChannelBatchData(
        existing: InternalMarketState,
        tickSize: Double?,
        content: List<IndexerWsOrderbookUpdateResponse>?,
    ): InternalMarketState {
        content?.forEach {
            processChannelData(existing, tickSize, it)
        }
        return existing
    }

    fun processGrouping(
        existing: InternalMarketState,
        tickSize: Double?,
        groupingMultiplier: Int,
    ): InternalMarketState {
        this.groupingMultiplier = groupingMultiplier
        existing.groupedOrderbook = calculator.calculate(
            rawOrderbook = existing.rawOrderbook,
            tickSize = tickSize ?: defaultTickSize,
            groupingMultiplier = groupingMultiplier,
        )
        return existing
    }

    private fun processChannelData(
        existing: InternalMarketState,
        tickSize: Double?,
        content: IndexerWsOrderbookUpdateResponse?,
    ): InternalMarketState {
        existing.rawOrderbook = InternalOrderbook(
            asks = processChangesBinary(
                existing = existing.rawOrderbook?.asks,
                changes = content?.asks,
                ascending = true,
            ),
            bids = processChangesBinary(
                existing = existing.rawOrderbook?.bids,
                changes = content?.bids,
                ascending = false,
            ),
        )
        existing.consolidatedOrderbook = calculator.consolidate(
            rawOrderbook = existing.rawOrderbook,
        )
        existing.groupedOrderbook = calculator.calculate(
            rawOrderbook = existing.rawOrderbook,
            tickSize = tickSize ?: defaultTickSize,
            groupingMultiplier = groupingMultiplier,
        )
        return existing
    }

    private fun createTick(payload: IndexerOrderbookResponsePriceLevel): InternalOrderbookTick? {
        val price = parser.asDouble(payload.price)
        val size = parser.asDouble(payload.size)
        return createTick(price, size)
    }

    private fun createTick(price: Double?, size: Double?): InternalOrderbookTick? {
        return if (price != null && size != null) {
            InternalOrderbookTick(
                price = price,
                size = size,
            )
        } else {
            null
        }
    }

    internal fun subscribedDeprecated(
        content: Map<String, Any>
    ): Map<String, Any> {
        return received(null, content)
    }

    @Suppress("FunctionName")
    internal fun channel_batch_data(
        existing: Map<String, Any>?,
        content: List<Any>
    ): Map<String, Any> {
        val orderbook = receivedBatchedChanges(existing, content)
        return if (orderbook != null) calculate(orderbook) else mutableMapOf()
    }

    /*
    asks: list of low to high
    bids: list of high to low
     */
    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val orderbook = existing?.mutable() ?: mutableMapOf<String, Any>()
        val asks = parser.asNativeList(payload["asks"])?.map {
            parser.asNativeMap(it)?.let {
                entryProcessor.received(null, it)
            }
        }
        orderbook.safeSet("asks", asks)
        val bids = parser.asNativeList(payload["bids"])?.map {
            parser.asNativeMap(it)?.let {
                entryProcessor.received(null, it)
            }
        }
        orderbook.safeSet("bids", bids)
        lastOffset = 0
        return orderbook
    }

    private fun receivedBatchedChanges(
        existing: Map<String, Any>?,
        payload: List<Any>
    ): Map<String, Any>? {
        var orderbook = existing
        for (change in payload) {
            orderbook = receivedChanges(orderbook, parser.asNativeMap(change))
        }

        return if (orderbook != null) calculate(orderbook) else null
    }

    private fun receivedChanges(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?
    ): Map<String, Any>? {
        if (payload != null) {
            val orderbook = existing?.mutable() ?: mutableMapOf()
            // offset in v4 is always null. We just increment our own offset
            val offset = parser.asLong(payload["offset"]) ?: (lastOffset + 1)
            orderbook["asks"] = receivedChangesDeprecated(
                orderbook["asks"] as? List<Map<String, Any>>,
                parser.asNativeList(payload["asks"] ?: payload["ask"]),
                offset,
                true,
            )

            orderbook["bids"] = receivedChangesDeprecated(
                orderbook["bids"] as? List<Map<String, Any>>,
                parser.asNativeList(payload["bids"] ?: payload["bid"]),
                offset,
                false,
            )
            lastOffset = offset
            return orderbook
        }
        return existing
    }

    private fun receivedChangesDeprecated(
        existing: List<Map<String, Any>>?,
        changes: List<Any>?,
        offset: Long?,
        ascending: Boolean
    ): List<Map<String, Any>> {
        if (changes == null || changes.size == 0) {
            return existing ?: mutableListOf()
        }

        val bindaryResult = receivedChangesBinaryDeprecated(existing, changes, offset, ascending)
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

    private fun processChangesBinary(
        existing: List<InternalOrderbookTick>?,
        changes: List<IndexerWsOrderbookUpdateItem>?,
        ascending: Boolean
    ): List<InternalOrderbookTick> {
        val comparator = compareBy<InternalOrderbookTick> {
            val price = it.price
            if (ascending) price else (price * Numeric.double.NEGATIVE)
        }

        val orderbook = existing?.mutable() ?: mutableListOf()
        for (change in changes ?: emptyList()) {
            processChangeBinary(orderbook, change, comparator)
        }
        return orderbook
    }

    private fun processChangeBinary(
        orderbook: MutableList<InternalOrderbookTick>,
        change: IndexerWsOrderbookUpdateItem,
        comparator: Comparator<InternalOrderbookTick>,
    ) {
        val price = change.getPrice(parser)
        val size = change.getSize(parser)
        if (price != null && size != null) {
            val item = InternalOrderbookTick(
                price = price,
                size = size,
            )
            val index = orderbook.binarySearch(item, comparator)
            if (index >= 0) {
                // found the item
                val existing = orderbook[index]
                orderbook.removeAt(index)
                if (size != Numeric.double.ZERO) {
                    orderbook.add(index, item)
                }
            } else {
                if (size != Numeric.double.ZERO) {
                    val insertionIndex = (index + 1) * -1
                    orderbook.add(insertionIndex, item)
                }
            }
        }
    }

    private fun receivedChangesBinaryDeprecated(
        existing: List<Map<String, Any>>?,
        changes: List<Any>,
        offset: Long?,
        ascending: Boolean
    ): List<Map<String, Any>> {
        val comparator = compareBy<Map<String, Any>> {
            val price = parser.asDouble(it["price"])
            if (price != null) {
                if (ascending) price else (price * Numeric.double.NEGATIVE)
            } else {
                null
            }
        }
        var orderbook = existing?.mutable() ?: mutableListOf()
        for (change in changes) {
            orderbook = receiveChangeBinaryDeprecated(orderbook, change, offset, comparator)
        }
        return orderbook
    }

    private fun receiveChangeBinaryDeprecated(
        existing: List<Map<String, Any>>,
        change: Any,
        offset: Long?,
        comparator: Comparator<Map<String, Any>>
    ): MutableList<Map<String, Any>> {
        val orderbook = existing.mutable()
        val price = entryPrice(change)
        val size = entrySize(change)
        if (price != null && size != null) {
            val item = mutableMapOf<String, Any>("price" to price, "size" to size)
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
        result1: List<Map<String, Any>>,
        result2: List<Map<String, Any>>
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
        existing: List<Map<String, Any>>?,
        changes: List<Any>,
        offset: Long?,
        ascending: Boolean
    ): List<Map<String, Any>> {
        val size1 = existing?.size ?: 0
        val size2 = changes.size ?: 0
        var cursor1 = 0
        var cursor2 = 0

        val result = mutableListOf<Map<String, Any>>()

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
                                    size,
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
            parser.asNativeMap(entry)?.get("price") ?: parser.asNativeList(entry)?.getOrNull(0),
        )
    }

    private fun entrySize(entry: Any): Double? {
        return parser.asDouble(
            parser.asNativeMap(entry)?.get("size") ?: parser.asNativeList(entry)?.getOrNull(1),
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

    private fun calculate(orderbook: Map<String, Any>): Map<String, Any> {
        var asksDepth = 0.0
        val asks = parser.asNativeList(orderbook.get("asks"))?.map { ask ->
            val map = parser.asNativeMap(ask)!!
            val size = parser.asDouble(map["size"]) ?: 0.0
            val price = parser.asDouble(map["price"]) ?: 0.0
            asksDepth += size
            val modified = map.mutable()
            modified["depth"] = asksDepth
            modified["sizeCost"] = size * price
            modified
        }
        var bidsDepth = 0.0
        val bids = parser.asNativeList(orderbook.get("bids"))?.map { bid ->
            val map = parser.asNativeMap(bid)!!
            val size = parser.asDouble(map["size"]) ?: 0.0
            val price = parser.asDouble(map["price"]) ?: 0.0

            bidsDepth += size
            val modified = map.mutable()
            modified["depth"] = bidsDepth
            modified["sizeCost"] = size * price
            modified
        }
        val firstAskPrice = parser.asDouble(
            parser.asNativeMap(
                asks?.firstOrNull { item ->
                    val size = parser.asDouble(parser.asNativeMap(item)?.get("size"))
                    (size != 0.0)
                },
            )?.get("price"),
        )
        val firstBidPrice = parser.asDouble(
            parser.asNativeMap(
                bids?.firstOrNull { item ->
                    val size = parser.asDouble(parser.asNativeMap(item)?.get("size"))
                    (size != 0.0)
                },
            )?.get("price"),
        )
        val modified = orderbook.mutable()
        if (firstAskPrice != null && firstBidPrice != null) {
            val midPrice = (firstAskPrice + firstBidPrice) / 2.0
            val spread = firstAskPrice.minus(firstBidPrice)
            val spreadPercent = spread / midPrice
            modified.safeSet("midPrice", midPrice)
            modified.safeSet("spreadPercent", spreadPercent)
            modified.safeSet("spread", spread)
            modified.safeSet("asks", asks)
            modified.safeSet("bids", bids)
        } else {
            modified.safeSet("midPrice", null)
            modified.safeSet("spreadPercent", null)
            modified.safeSet("spread", null)
            modified.safeSet("asks", asks)
            modified.safeSet("bids", bids)
        }
        return modified
    }

    fun consolidate(orderbook: Map<String, Any>?, stepSize: Double): Map<String, Any>? {
        /*
        val stepSizeDecimals = stepSize.numberOfDecimals()
        val asks = parser.asNativeList(orderbook?.get("asks"))?.mutable()
        val bids = parser.asNativeList(orderbook?.get("bids"))?.mutable()
        var ask = parser.asNativeMap(asks?.firstOrNull())
        var bid = parser.asNativeMap(bids?.firstOrNull())
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
                    bid = parser.asNativeMap(bids.firstOrNull())
                    bids.removeFirstOrNull()
                    bidPrice = parser.asDouble(bid?.get("price"))
                    bidSize = parser.asDouble(bid?.get("size")) ?: Numeric.double.ZERO
                } else {
                    bidSize -= askSize
                    bidSize = Rounder.quickRound(bidSize, stepSize, stepSizeDecimals)
                    ask = parser.asNativeMap(asks.firstOrNull())
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
            calculate(mapOf("asks" to asks, "bids" to bids))
        } else orderbook
         */

        val asks = parser.asNativeList(orderbook?.get("asks"))?.mutable()
        val bids = parser.asNativeList(orderbook?.get("bids"))?.mutable()
        return if (asks != null && bids != null && asks.size > 0 && bids.size > 0) {
            var ask = parser.asNativeMap(asks.firstOrNull())
            var bid = parser.asNativeMap(bids.firstOrNull())
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
                        bid = parser.asNativeMap(bids.firstOrNull())
                    } else {
                        asks.removeFirst()
                        ask = parser.asNativeMap(asks.firstOrNull())
                    }
                } else {
                    // Offsets are not equal. Give precedence to the larger offset.
                    if (askOffset > bidOffset) {
                        bids.removeFirst()
                        bid = parser.asNativeMap(bids.firstOrNull())
                    } else {
                        asks.removeFirst()
                        ask = parser.asNativeMap(asks.firstOrNull())
                    }
                }
            }
            calculate(mapOf("asks" to asks, "bids" to bids))
        } else {
            orderbook
        }
    }

    private fun crossed(ask: Map<String, Any>, bid: Map<String, Any>): Boolean {
        val askPrice = parser.asDouble(ask["price"])
        val bidPrice = parser.asDouble(bid["price"])
        return if (askPrice != null && bidPrice != null) {
            askPrice <= bidPrice
        } else {
            false
        }
    }

    private fun buildGroupingLookup(tickSize: Double) {
        if (groupingTickSize != tickSize) {
            groupingTickSize = tickSize
            groupingLookup = mutableMapOf()
        }
    }

    fun group(orderbook: Map<String, Any>?, tickSize: Double): Map<String, Any>? {
        buildGroupingLookup(tickSize)
        return if (orderbook != null) {
            val groupingTickSize = grouping(tickSize, groupingMultiplier)
            val modified = if (groupingMultiplier != 1) {
                val modified = mutableMapOf<String, Any>()
                modified.safeSet(
                    "asks",
                    group(parser.asNativeList(orderbook["asks"]), groupingTickSize),
                )
                modified.safeSet(
                    "bids",
                    group(parser.asNativeList(orderbook["bids"]), groupingTickSize),
                )
                modified.safeSet("midPrice", orderbook["midPrice"])
                modified.safeSet("spreadPercent", orderbook["spreadPercent"])
                modified.safeSet("spread", orderbook["spread"])
                modified
            } else {
                orderbook.toMutableMap()
            }
            modified.safeSet(
                "grouping",
                mapOf("tickSize" to groupingTickSize, "multiplier" to groupingMultiplier),
            )
            modified
        } else {
            orderbook
        }
    }

    private fun group(orderbook: List<Any>?, grouping: Double): List<Any>? {
        return if (!orderbook.isNullOrEmpty()) {
            // orderbook always ordered in increasing depth which is either increasing (asks) or decreasing (bids) price
            // we want to round asks up and bids down so they don't have an overlapping group in the middle
            val firstPrice = parser.asDouble(parser.value(orderbook.firstOrNull(), "price"))!!
            val lastPrice = parser.asDouble(parser.value(orderbook.lastOrNull(), "price"))!!
            val shouldFloor = lastPrice <= firstPrice
            val result = mutableListOf<Map<String, Any>>()

            // properties of the current group
            var curFloored = Rounder.round(firstPrice, grouping);
            var groupMin = if (curFloored != firstPrice) curFloored else (if (shouldFloor) curFloored else curFloored - grouping)
            var groupMax = groupMin + grouping
            var size = Numeric.double.ZERO
            var sizeCost = Numeric.double.ZERO
            var depth = Numeric.double.ZERO

            for (item in orderbook) {
                val line = parser.asNativeMap(item)
                val linePrice = parser.asDouble(line?.get("price"))
                if (linePrice != null) {
                    val lineSize = parser.asDouble(line?.get("size")) ?: Numeric.double.ZERO
                    val lineSizeCost = lineSize * linePrice

                    // if in this group
                    // remember: if flooring then min inclusive max exclusive; if ceiling then min exclusive, max inclusive
                    if ((linePrice > groupMin && linePrice < groupMax) || (linePrice == groupMin && shouldFloor) || (linePrice == groupMax && !shouldFloor)) {
                        size += lineSize
                        sizeCost += lineSizeCost
                        depth += lineSize
                    } else {
                        result.add(
                            mapOf(
                                "price" to (if (shouldFloor) groupMin else groupMax),
                                "size" to size,
                                "sizeCost" to sizeCost,
                                "depth" to depth,
                            ),
                        )
                        curFloored = Rounder.round(linePrice, grouping);
                        groupMin = if (curFloored != linePrice) curFloored else (if (shouldFloor) curFloored else curFloored - grouping)
                        groupMax = groupMin + grouping

                        size = lineSize
                        sizeCost = lineSizeCost
                        depth += lineSize
                    }
                }
            }
            result.add(mapOf("price" to (if (shouldFloor) groupMin else groupMax), "size" to size, "sizeCost" to sizeCost, "depth" to depth))

            return result
        } else {
            null
        }
    }

    private fun grouping(tickSize: Double, grouping: Int): Double {
        val cached = groupingLookup?.get(grouping)
        return if (cached != null) {
            cached
        } else {
            val decimals = if (grouping == 1) {
                parser.asDouble(tickSize.tickDecimals())!!
            } else {
                val tickDecimals = parser.asDouble(tickSize.tickDecimals())!!
                tickDecimals * grouping
            }
            if (groupingLookup == null) {
                groupingLookup = mutableMapOf()
            }
            groupingLookup?.set(grouping, decimals)
            decimals
        }
    }
}
