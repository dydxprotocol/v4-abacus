package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.output.MarketTrade
import exchange.dydx.abacus.output.MarketTradeResources
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.processor.base.mergeWithIds
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IndexerResponseParsingException
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.parseException
import indexer.codegen.IndexerTradeResponseObject
import kotlinx.serialization.Serializable

@Serializable
data class TradesResponse(
    val trades: List<IndexerTradeResponseObject>
)

internal class TradesProcessorV2(
    private val tradeProcessor: TradeProcessorV2,
    private val limit: Int = TRADES_LIMIT,
) {
    fun processSubscribed(
        payload: TradesResponse
    ): List<MarketTrade> {
        return payload.trades.mapNotNull {
            tradeProcessor.process(it)
        }
    }

    fun processChannelData(
        existing: List<MarketTrade>?,
        payload: TradesResponse,
    ): List<MarketTrade> {
        val new = payload.trades.mapNotNull {
            tradeProcessor.process(it)
        }
        val merged = existing?.let {
            mergeWithIds(new, existing) { trade -> trade.id }
        } ?: new

        return if (merged.size > limit) {
            merged.subList(0, limit)
        } else {
            merged
        }
    }
}

internal class TradeProcessorV2(
    private val parser: ParserProtocol,
    private val localizer: LocalizerProtocol?,
) {
    fun process(payload: IndexerTradeResponseObject): MarketTrade? {
        return try {
            MarketTrade(
                id = payload.id,
                side = payload.side?.name?.let { OrderSide(it) } ?: parseException(payload),
                size = payload.size?.toDouble() ?: parseException(payload),
                price = payload.price?.toDouble() ?: parseException(payload),
                type = payload.type?.name?.let { OrderType(it) },
                createdAtMilliseconds = payload.createdAt?.let { parser.asDatetime(it)?.toEpochMilliseconds()?.toDouble() } ?: parseException(payload),
                resources = run {
                    val key = payload.side?.name?.let { sideStringKeys[it] } ?: parseException(payload)
                    val string = localizer?.localize(key)

                    MarketTradeResources(
                        sideString = string,
                        sideStringKey = key,
                    )
                },
            )
        } catch (e: IndexerResponseParsingException) {
            Logger.e { "${e.message}" }
            null
        }
    }
}

private val sideStringKeys = mapOf(
    "BUY" to "APP.GENERAL.BUY",
    "SELL" to "APP.GENERAL.SELL",
)

private const val TRADES_LIMIT = 500
