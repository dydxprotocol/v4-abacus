package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.safeSet

internal class FillProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val fillKeyMap = iMapOf(
        "string" to iMapOf(
            "id" to "id",
            "side" to "side",
            "liquidity" to "liquidity",
            "type" to "type",
            "market" to "marketId",
            "orderId" to "orderId"
        ),
        "datetime" to iMapOf(
            "createdAt" to "createdAt"
        ),
        "double" to iMapOf(
            "price" to "price",
            "size" to "size",
            "fee" to "fee"
        )
    )

    private val sideMap = iMapOf(
        "BUY" to "APP.GENERAL.BUY",
        "SELL" to "APP.GENERAL.SELL"
    )

    private val liquidityMap = iMapOf(
        "MAKER" to "APP.TRADE.MAKER",
        "TAKER" to "APP.TRADE.TAKER"
    )

    private val typeMap = iMapOf(
        "MARKET" to "APP.TRADE.MARKET_ORDER_SHORT",
        "LIMIT" to "APP.TRADE.LIMIT_ORDER_SHORT",
        "STOP_LIMIT" to "APP.TRADE.STOP_LIMIT",
        "TRAILING_STOP" to "APP.TRADE.TRAILING_STOP",
        "TAKE_PROFIT" to "APP.TRADE.TAKE_PROFIT_LIMIT_SHORT",
        "STOP_MARKET" to "APP.TRADE.STOP_MARKET",
        "TAKE_PROFIT_MARKET" to "APP.TRADE.TAKE_PROFIT_MARKET_SHORT",
        "LIQUIDATED" to "APP.TRADE.LIQUIDATED",
        "LIQUIDATION" to "APP.TRADE.LIQUIDATION"
    )

    private val sideIconMap = iMapOf(
        "BUY" to "Buy",
        "SELL" to "Sell"
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val fill = transform(existing, payload, fillKeyMap)
        if (fill["marketId"] == null) {
            fill.safeSet("marketId", parser.asString(payload["ticker"]))
        }

        val resources = iMutableMapOf<String, Any>()
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
