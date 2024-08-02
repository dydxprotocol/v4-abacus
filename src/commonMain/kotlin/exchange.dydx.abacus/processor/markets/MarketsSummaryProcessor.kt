package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class MarketsSummaryProcessor(
    parser: ParserProtocol,
    calculateSparklines: Boolean = false
) : BaseProcessor(parser) {
    private val marketsProcessor = MarketsProcessor(parser, calculateSparklines)

    internal var groupingMultiplier: Int
        get() = marketsProcessor.groupingMultiplier
        set(value) {
            marketsProcessor.groupingMultiplier = value
        }

    internal fun subscribed(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.processSubscribed(parser.asNativeMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    @Suppress("FunctionName")
    internal fun channel_data(
        existing: Map<String, Any>?,
        content: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.processChannelData(parser.asNativeMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    @Suppress("FunctionName")
    internal fun channel_batch_data(
        existing: Map<String, Any>?,
        content: List<Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.processChannelBatchData(parser.asNativeMap(existing?.get("markets")), content)
        return modify(existing, markets)
    }

    internal fun receivedConfigurations(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val markets =
            marketsProcessor.receivedConfigurations(parser.asNativeMap(existing?.get("markets")), payload)
        return modify(existing, markets)!!
    }

    internal fun receivedOrderbook(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedOrderbook(
            parser.asNativeMap(existing?.get("markets")),
            market,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedBatchOrderbookChanges(
        existing: Map<String, Any>?,
        market: String,
        payload: List<Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedBatchOrderbookChanges(
            parser.asNativeMap(existing?.get("markets")),
            market,
            payload,
        )
        return modify(existing, markets)
    }

    @Deprecated("static-typing")
    internal fun receivedTradesDeprecated(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedTradesDeprecated(parser.asNativeMap(existing?.get("markets")), market, payload)
        return modify(existing, markets)
    }

    @Deprecated("static-typing")
    internal fun receivedTradesChangesDeprecated(
        existing: Map<String, Any>?,
        market: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedTradesChangesDeprecated(
            parser.asNativeMap(existing?.get("markets")),
            market,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedBatchedTradesChanges(
        existing: Map<String, Any>?,
        market: String,
        payload: List<Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedBatchedTradesChanges(
            parser.asNativeMap(existing?.get("markets")),
            market,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedCandles(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedCandles(parser.asNativeMap(existing?.get("markets")), payload)
        return modify(existing, markets)
    }

    internal fun receivedSparklines(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedSparklines(parser.asNativeMap(existing?.get("markets")), payload)
        return modify(existing, markets)
    }

    internal fun receivedCandles(
        existing: Map<String, Any>?,
        market: String,
        resolution: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets =
            marketsProcessor.receivedCandles(
                parser.asNativeMap(existing?.get("markets")),
                market,
                resolution,
                payload,
            )
        return modify(existing, markets)
    }

    internal fun receivedCandlesChanges(
        existing: Map<String, Any>?,
        market: String,
        resolution: String,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedCandlesChanges(
            parser.asNativeMap(existing?.get("markets")),
            market,
            resolution,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedBatchedCandlesChanges(
        existing: Map<String, Any>?,
        market: String,
        resolution: String,
        payload: List<Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedBatchedCandlesChanges(
            parser.asNativeMap(existing?.get("markets")),
            market,
            resolution,
            payload,
        )
        return modify(existing, markets)
    }

    internal fun receivedHistoricalFundings(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val markets = marketsProcessor.receivedHistoricalFundings(
            parser.asNativeMap(existing?.get("markets")),
            payload,
        )
        return modify(existing, markets)
    }

    private fun modify(
        existing: Map<String, Any>?,
        markets: Map<String, Any>?,
        key: String = "markets"
    ): Map<String, Any>? {
        return if (markets != null) {
            val modified = existing?.mutable() ?: mutableMapOf()
            modified.safeSet(key, markets)
            modified
        } else {
            val modified = existing?.mutable()
            modified?.safeSet(key, null)
            modified
        }
    }

    internal fun groupOrderbook(existing: Map<String, Any>?, market: String?): Map<String, Any>? {
        val markets =
            marketsProcessor.groupOrderbook(parser.asNativeMap(existing?.get("markets")), market)
        return modify(existing, markets)
    }
}
