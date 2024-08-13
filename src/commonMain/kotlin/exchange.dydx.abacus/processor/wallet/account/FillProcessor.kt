package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.FillLiquidity
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountFillResources
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.processor.utils.OrderTypeProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerFillResponseObject

internal interface FillProcessorProtocol {
    fun process(
        payload: IndexerFillResponseObject,
        subaccountNumber: Int,
    ): SubaccountFill?
}

internal class FillProcessor(
    parser: ParserProtocol,
    private val localizer: LocalizerProtocol?,
) : BaseProcessor(parser), FillProcessorProtocol {
    private val fillKeyMap = mapOf(
        "string" to mapOf(
            "id" to "id",
            "side" to "side",
            "liquidity" to "liquidity",
            "type" to "type",
            "market" to "marketId",
            "displayId" to "displayId",
            "orderId" to "orderId",
        ),
        "datetime" to mapOf(
            "createdAt" to "createdAt",
        ),
        "double" to mapOf(
            "price" to "price",
            "size" to "size",
            "fee" to "fee",
        ),
        "int" to mapOf(
            "clientMetadata" to "clientMetadata",
            "subaccountNumber" to "subaccountNumber",
        ),
    )

    private val sideMap = mapOf(
        "BUY" to "APP.GENERAL.BUY",
        "SELL" to "APP.GENERAL.SELL",
    )

    private val liquidityMap = mapOf(
        "MAKER" to "APP.TRADE.MAKER",
        "TAKER" to "APP.TRADE.TAKER",
    )

    private val typeMap = mapOf(
        "MARKET" to "APP.TRADE.MARKET_ORDER_SHORT",
        "LIMIT" to "APP.TRADE.LIMIT_ORDER_SHORT",
        "STOP_LIMIT" to "APP.TRADE.STOP_LIMIT",
        "TRAILING_STOP" to "APP.TRADE.TRAILING_STOP",
        "TAKE_PROFIT" to "APP.TRADE.TAKE_PROFIT_LIMIT_SHORT",
        "STOP_MARKET" to "APP.TRADE.STOP_MARKET",
        "TAKE_PROFIT_MARKET" to "APP.TRADE.TAKE_PROFIT_MARKET_SHORT",
        "LIQUIDATED" to "APP.TRADE.LIQUIDATED",
        "LIQUIDATION" to "APP.TRADE.LIQUIDATION",
        "DELEVERAGED" to "APP.TRADE.DELEVERAGED",
        "OFFSETTING" to "APP.TRADE.OFFSETTING",
        "FINAL_SETTLEMENT" to "APP.TRADE.FINAL_SETTLEMENT",
    )

    private val sideIconMap = mapOf(
        "BUY" to "Buy",
        "SELL" to "Sell",
    )

    override fun process(
        payload: IndexerFillResponseObject,
        subaccountNumber: Int,
    ): SubaccountFill? {
        fun doProcess(): SubaccountFill? {
            val fillSubaccountNumber = parser.asInt(payload.subaccountNumber) ?: subaccountNumber

            val id = payload.id ?: return null
            val marketId = payload.market ?: return null
            val displayId = MarketId.getDisplayId(marketId)
            val side = payload.side?.name?.let { OrderSide.invoke(rawValue = it) } ?: return null
            val liquidity =
                payload.liquidity?.name?.let { FillLiquidity.invoke(rawValue = it) } ?: return null

            val typeString = OrderTypeProcessor.orderType(
                type = payload.type?.name,
                clientMetadata = parser.asInt(payload.clientMetadata),
            )
            val type = typeString?.let { OrderType.invoke(rawValue = it) } ?: return null

            return SubaccountFill(
                id = id,
                marketId = marketId,
                displayId = displayId,
                orderId = payload.orderId,
                subaccountNumber = fillSubaccountNumber,
                marginMode = if (fillSubaccountNumber >= NUM_PARENT_SUBACCOUNTS) MarginMode.Isolated else MarginMode.Cross,
                side = side,
                type = type,
                liquidity = liquidity,
                price = parser.asDouble(payload.price) ?: return null,
                size = parser.asDouble(payload.size) ?: return null,
                fee = parser.asDouble(payload.fee),
                createdAtMilliseconds = parser.asDatetime(payload.createdAt)?.toEpochMilliseconds()
                    ?.toDouble() ?: return null,
                resources = SubaccountFillResources(
                    sideString = sideMap[payload.side.name]?.let { localizer?.localize(it) },
                    liquidityString = liquidityMap[payload.liquidity.name]?.let { localizer?.localize(it) },
                    typeString = typeMap[typeString]?.let { localizer?.localize(it) },
                    sideStringKey = sideMap[payload.side.name],
                    liquidityStringKey = liquidityMap[payload.liquidity.name],
                    typeStringKey = typeMap[typeString],
                    iconLocal = sideIconMap[payload.side.name],
                ),
            )
        }

        return doProcess().also { fill ->
            if (fill == null) {
                Logger.e { "Failed to parse fill: $payload" }
            }
        }
    }

    internal fun receivedDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        subaccountNumber: Int,
    ): Map<String, Any> {
        val fill = transform(existing, payload, fillKeyMap)

        if (fill["marketId"] == null) {
            fill.safeSet("marketId", parser.asString(payload["ticker"]))
            parser.asString(payload["ticker"])?.let { marketId ->
                fill.safeSet("displayId", MarketId.getDisplayId(marketId))
            }
        } else {
            parser.asString(fill["marketId"])?.let { marketId ->
                fill.safeSet("displayId", MarketId.getDisplayId(marketId))
            }
        }

        val fillSubaccountNumber = parser.asInt(payload["subaccountNumber"])

        if (fillSubaccountNumber == null) {
            fill.safeSet("subaccountNumber", subaccountNumber)
        }

        parser.asInt(fill["subaccountNumber"])?.run {
            fill.safeSet("marginMode", if (this >= NUM_PARENT_SUBACCOUNTS) MarginMode.Isolated.rawValue else MarginMode.Cross.rawValue)
        }

        fill.safeSet(
            "type",
            OrderTypeProcessor.orderType(
                parser.asString(fill["type"]),
                parser.asInt(fill["clientMetadata"]),
            ),
        )

        val resources = mutableMapOf<String, Any>()
        fill["side"]?.let {
            sideMap[it]?.let {
                resources["sideStringKey"] = it
            }
            sideIconMap[it]?.let {
                resources["iconLocal"] = it
            }
        }
        fill["liquidity"]?.let {
            liquidityMap[it]?.let {
                resources["liquidityStringKey"] = it
            }
        }
        fill["type"]?.let {
            typeMap[it]?.let {
                resources["typeStringKey"] = it
            }
        }
        fill["resources"] = resources
        return fill
    }
}
