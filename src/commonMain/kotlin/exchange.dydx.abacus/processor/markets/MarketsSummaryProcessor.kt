package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class MarketsSummaryProcessor(parser: ParserProtocol, calculateSparklines: Boolean) :
    BaseProcessor(parser) {
    private val marketsProcessor = MarketsProcessor(parser, calculateSparklines)

    internal var groupingMultiplier: Int
        get() = marketsProcessor.groupingMultiplier
        set(value) {
            marketsProcessor.groupingMultiplier = value
        }

    internal fun subscribed(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>
    ): IMap<String, Any>? {
        val markets = marketsProcessor.subscribed(parser.asMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    internal fun channel_data(
        existing: IMap<String, Any>?,
        content: IMap<String, Any>
    ): IMap<String, Any>? {
        val markets = marketsProcessor.channel_data(parser.asMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    internal fun channel_batch_data(
        existing: IMap<String, Any>?,
        content: IList<Any>
    ): IMap<String, Any>? {
        val markets =
            marketsProcessor.channel_batch_data(parser.asMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    internal fun receivedConfigurations(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val markets =
            marketsProcessor.receivedConfigurations(parser.asMap(existing?.get("markets")), payload)
        return modify(existing, markets)!!
    }

    internal fun receivedOrderbook(
        existing: IMap<String, Any>?,
        market: String,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val markets = marketsProcessor.receivedOrderbook(
            parser.asMap(existing?.get("markets")),
            market,
            payload
        )
        return modify(existing, markets)
    }

    internal fun receivedBatchOrderbookChanges(
        existing: IMap<String, Any>?,
        market: String,
        payload: IList<Any>
    ): IMap<String, Any>? {
        val markets = marketsProcessor.receivedBatchOrderbookChanges(
            parser.asMap(existing?.get("markets")),
            market,
            payload
        )
        return modify(existing, markets)
    }

    internal fun receivedTrades(
        existing: IMap<String, Any>?,
        market: String,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val markets =
            marketsProcessor.receivedTrades(parser.asMap(existing?.get("markets")), market, payload)
        return modify(existing, markets)
    }

    internal fun receivedTradesChanges(
        existing: IMap<String, Any>?,
        market: String,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val markets = marketsProcessor.receivedTradesChanges(
            parser.asMap(existing?.get("markets")),
            market,
            payload
        )
        return modify(existing, markets)
    }

    internal fun receivedBatchedTradesChanges(
        existing: IMap<String, Any>?,
        market: String,
        payload: IList<Any>
    ): IMap<String, Any>? {
        val markets = marketsProcessor.receivedBatchedTradesChanges(
            parser.asMap(existing?.get("markets")),
            market,
            payload
        )
        return modify(existing, markets)
    }

    internal fun receivedCandles(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val markets =
            marketsProcessor.receivedCandles(parser.asMap(existing?.get("markets")), payload)
        return modify(existing, markets)
    }

    internal fun receivedSparklines(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val markets =
            marketsProcessor.receivedSparklines(parser.asMap(existing?.get("markets")), payload)
        return modify(existing, markets)
    }

    internal fun receivedHistoricalFundings(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val markets = marketsProcessor.receivedHistoricalFundings(
            parser.asMap(existing?.get("markets")),
            payload
        )
        return modify(existing, markets)
    }

    private fun modify(
        existing: IMap<String, Any>?, markets: IMap<String, Any>?, key: String = "markets"
    ): IMap<String, Any>? {

        return if (markets != null) {
            val modified = existing?.mutable() ?: iMutableMapOf()
            modified.safeSet(key, markets)
            modified
        } else {
            val modified = existing?.mutable()
            modified?.safeSet(key, null)
            modified
        }
    }

    internal fun groupOrderbook(existing: IMap<String, Any>?, market: String?): IMap<String, Any>? {
        val markets =
            marketsProcessor.groupOrderbook(parser.asMap(existing?.get("markets")), market)
        return modify(existing, markets)
    }
}