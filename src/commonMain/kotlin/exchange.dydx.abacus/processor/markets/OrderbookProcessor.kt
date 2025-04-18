package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.calculator.OrderbookCalculator
import exchange.dydx.abacus.calculator.OrderbookCalculatorProtocol
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketState
import exchange.dydx.abacus.state.InternalOrderbook
import exchange.dydx.abacus.state.InternalOrderbookTick
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import indexer.codegen.IndexerOrderbookResponseObject
import indexer.codegen.IndexerOrderbookResponsePriceLevel
import indexer.models.IndexerWsOrderbookUpdateItem
import indexer.models.IndexerWsOrderbookUpdateResponse
import indexer.models.getPrice
import indexer.models.getSize

@Suppress("UNCHECKED_CAST")
internal class OrderbookProcessor(
    parser: ParserProtocol,
    private val calculator: OrderbookCalculatorProtocol = OrderbookCalculator(parser),
) : BaseProcessor(parser) {
    internal var groupingMultiplier: Int = 1

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
            rawOrderbook = existing.consolidatedOrderbook,
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
            rawOrderbook = existing.consolidatedOrderbook,
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
            rawOrderbook = existing.consolidatedOrderbook,
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
}
