package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.output.MarketOrderbookGrouping
import exchange.dydx.abacus.output.OrderbookLine
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalOrderbook
import exchange.dydx.abacus.state.internalstate.InternalOrderbookTick
import exchange.dydx.abacus.state.manager.OrderbookGrouping
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder
import kollections.toIList
import tickDecimals

internal interface OrderbookCalculatorProtocol {
    // Creates the orderbook entries for app display
    fun calculate(
        rawOrderbook: InternalOrderbook?,
        tickSize: Double,
        groupingMultiplier: Int,
    ): MarketOrderbook?

    // Creates the orderbook entries for trade calculations
    fun consolidate(
        rawOrderbook: InternalOrderbook?,
    ): InternalOrderbook?
}

internal class OrderbookCalculator(
    private val parser: ParserProtocol,
) : OrderbookCalculatorProtocol {
    private var groupingTickSize: Double? = null
    private var groupingLookup: MutableMap<Int, Double>? = null

    override fun calculate(
        rawOrderbook: InternalOrderbook?,
        tickSize: Double,
        groupingMultiplier: Int,
    ): MarketOrderbook? {
        if (rawOrderbook == null) {
            return null
        }

        buildGroupingLookup(tickSize)
        val groupingTickSize = getGroupingTickSizeDecimals(tickSize, groupingMultiplier)
        val asks = createGroup(rawOrderbook.asks, groupingTickSize)
        val bids = createGroup(rawOrderbook.bids, groupingTickSize)

        val firstAskPrice = asks?.firstOrNull()?.price
        val firstBidPrice = bids?.firstOrNull()?.price

        val multiplier = OrderbookGrouping.invoke(groupingMultiplier)
        if (firstAskPrice != null && firstBidPrice != null) {
            val midPrice = (firstAskPrice + firstBidPrice) / 2.0
            val spread = firstAskPrice - firstBidPrice
            val spreadPercent = spread / midPrice
            return MarketOrderbook(
                midPrice = midPrice,
                spreadPercent = spreadPercent,
                spread = spread,
                grouping = multiplier?.let {
                    MarketOrderbookGrouping(
                        tickSize = groupingTickSize,
                        multiplier = it,
                    )
                },
                asks = asks.toIList(),
                bids = bids.toIList(),
            )
        } else {
            return MarketOrderbook(
                midPrice = null,
                spreadPercent = null,
                spread = null,
                grouping = multiplier?.let {
                    MarketOrderbookGrouping(
                        tickSize = groupingTickSize,
                        multiplier = it,
                    )
                },
                asks = asks?.toIList(),
                bids = bids?.toIList(),
            )
        }
    }

    override fun consolidate(
        rawOrderbook: InternalOrderbook?,
    ): InternalOrderbook? {
        val asks = rawOrderbook?.asks?.toMutableList()
        val bids = rawOrderbook?.bids?.toMutableList()
        return if (asks != null && bids != null && asks.size > 0 && bids.size > 0) {
            var ask = asks.firstOrNull()
            var bid = bids.firstOrNull()
            while (ask != null && bid != null && crossed(ask, bid)) {
                val askSize = ask.size
                val bidSize = bid.size
                if (askSize >= bidSize) {
                    bids.removeFirst()
                    bid = bids.firstOrNull()
                } else {
                    asks.removeFirst()
                    ask = asks.firstOrNull()
                }
            }
            InternalOrderbook(
                asks = asks,
                bids = bids,
            )
        } else {
            rawOrderbook
        }
    }

    private fun crossed(ask: InternalOrderbookTick, bid: InternalOrderbookTick): Boolean {
        val askPrice = ask.price
        val bidPrice = bid.price
        return askPrice <= bidPrice
    }

    private fun createGroup(
        orderbookTicks: List<InternalOrderbookTick>?,
        grouping: Double,
    ): List<OrderbookLine>? {
        return if (!orderbookTicks.isNullOrEmpty()) {
            // orderbook always ordered in increasing depth which is either increasing (asks) or decreasing (bids) price
            // we want to round asks up and bids down so they don't have an overlapping group in the middle
            val firstPrice = orderbookTicks.first().price
            val lastPrice = orderbookTicks.last().price
            val shouldFloor = lastPrice <= firstPrice
            val result = mutableListOf<OrderbookLine>()

            // properties of the current group
            var curFloored = Rounder.round(firstPrice, grouping);
            var groupMin = if (curFloored != firstPrice) curFloored else (if (shouldFloor) curFloored else curFloored - grouping)
            var groupMax = groupMin + grouping
            var size = Numeric.double.ZERO
            var sizeCost = Numeric.double.ZERO
            var depth = Numeric.double.ZERO
            var depthCost = Numeric.double.ZERO

            for (item in orderbookTicks) {
                val linePrice = item.price
                val lineSize = item.size
                val lineSizeCost = lineSize * linePrice

                // if in this group
                // remember: if flooring then min inclusive max exclusive; if ceiling then min exclusive, max inclusive
                if ((linePrice > groupMin && linePrice < groupMax) || (linePrice == groupMin && shouldFloor) || (linePrice == groupMax && !shouldFloor)) {
                    size += lineSize
                    sizeCost += lineSizeCost
                    depth += lineSize
                    depthCost += lineSizeCost
                } else {
                    result.add(
                        OrderbookLine(
                            size = size,
                            price = if (shouldFloor) groupMin else groupMax,
                            depth = depth,
                            sizeCost = sizeCost,
                            depthCost = depthCost,
                        ),
                    )
                    curFloored = Rounder.round(linePrice, grouping);
                    groupMin = if (curFloored != linePrice) curFloored else (if (shouldFloor) curFloored else curFloored - grouping)
                    groupMax = groupMin + grouping

                    size = lineSize
                    sizeCost = lineSizeCost
                    depth += lineSize
                    depthCost += lineSizeCost
                }
            }
            result.add(
                OrderbookLine(
                    size = size,
                    price = if (shouldFloor) groupMin else groupMax,
                    depth = depth,
                    sizeCost = sizeCost,
                    depthCost = depthCost,
                ),
            )
            return result
        } else {
            null
        }
    }

    private fun buildGroupingLookup(tickSize: Double) {
        if (groupingTickSize != tickSize) {
            groupingTickSize = tickSize
            groupingLookup = mutableMapOf()
        }
    }

    private fun getGroupingTickSizeDecimals(tickSize: Double, groupingMultiplier: Int): Double {
        val cached = groupingLookup?.get(groupingMultiplier)
        return if (cached != null) {
            cached
        } else {
            val decimals = if (groupingMultiplier == 1) {
                parser.asDouble(tickSize.tickDecimals())!!
            } else {
                val tickDecimals = parser.asDouble(tickSize.tickDecimals())!!
                tickDecimals * groupingMultiplier
            }
            if (groupingLookup == null) {
                groupingLookup = mutableMapOf()
            }
            groupingLookup?.set(groupingMultiplier, decimals)
            decimals
        }
    }
}
