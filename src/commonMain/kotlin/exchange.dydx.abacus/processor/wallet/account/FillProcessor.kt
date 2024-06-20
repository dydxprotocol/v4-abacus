package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.utils.OrderTypeProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.safeSet

internal class FillProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val fillKeyMap = mapOf(
        "string" to mapOf(
            "id" to "id",
            "side" to "side",
            "liquidity" to "liquidity",
            "type" to "type",
            "market" to "marketId",
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

    internal fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        subaccountNumber: Int,
    ): Map<String, Any> {
        val fill = transform(existing, payload, fillKeyMap)
        if (fill["marketId"] == null) {
            fill.safeSet("marketId", parser.asString(payload["ticker"]))
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
