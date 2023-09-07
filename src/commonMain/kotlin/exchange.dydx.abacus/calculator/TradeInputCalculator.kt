package exchange.dydx.abacus.calculator

import abs
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.reduceOnlySupported
import exchange.dydx.abacus.utils.safeSet
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIMutableList
import kollections.toIMutableMap
import kotlinx.serialization.Serializable
import kotlin.math.abs

@JsExport
@Serializable
enum class TradeCalculation(val rawValue: String) {
    trade("MAKER"), closePosition("TAKER");

    companion object {
        operator fun invoke(rawValue: String) =
            TradeCalculation.values().firstOrNull { it.rawValue == rawValue }
    }
}

@Suppress("UNCHECKED_CAST")
internal class TradeInputCalculator(
    val parser: ParserProtocol,
    private val calculation: TradeCalculation,
) {
    private val accountTransformer = AccountTransformer()
    private val fokDisabled = false

    private val MARKET_ORDER_MAX_SLIPPAGE = 0.01                                // 0.05 for v3
    private val STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET = 0.01           // 0.05 for v3
    private val TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET = 0.02    // 0.05 for v3
    private val STOP_MARKET_ORDER_SLIPPAGE_BUFFER = 0.02                        // 0.1 for v3
    private val TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER = 0.04                 // 0.1 for v3

    internal fun calculate(
        state: IMap<String, Any>,
        subaccountNumber: Int?,
        input: String?,
    ): IMap<String, Any> {
        val account = parser.asMap(state["account"])
        val subaccount = if (subaccountNumber != null) parser.asMap(
            parser.value(
                account,
                "subaccounts.$subaccountNumber"
            )
        ) else null
        val user = parser.asMap(state["user"]) ?: iMapOf()
        val markets = parser.asMap(state["markets"])
        val trade = parser.asMap(state["trade"])
        val marketId = parser.asString(trade?.get("marketId"))
        val type = parser.asString(trade?.get("type"))
        val market = if (marketId != null) parser.asMap(markets?.get(marketId)) else null
        return if (trade != null && type != null) {
            val isBuying = isBuying(trade)
            val modified = state.mutable()
            val trade = if (input != null) {
                val modifiedTrade = when (type) {
                    "MARKET", "STOP_MARKET", "TAKE_PROFIT_MARKET" -> {
                        calculateMarketOrderTrade(
                            trade,
                            market,
                            subaccount,
                            user,
                            isBuying,
                            input
                        )
                    }

                    else -> {
                        calculateNonMarketTrade(
                            trade,
                            market,
                            type,
                            isBuying,
                            input
                        )
                    }
                }
                finalize(modifiedTrade, subaccount, user, market, type)
            } else {
                finalize(trade, subaccount, user, market, type)
            }
            modified["trade"] = trade
            modified.safeSet(
                "account",
                accountTransformer.applyTradeToAccount(
                    account,
                    subaccountNumber,
                    trade,
                    parser,
                    "postOrder"
                )
            )
            modified
        } else state
    }

    private fun calculateNonMarketTrade(
        trade: IMap<String, Any>,
        market: IMap<String, Any>?,
        type: String,
        isBuying: Boolean,
        input: String,
    ): IMap<String, Any> {
        val modifiedTrade = trade.mutable()
        val tradeSize = parser.asMap(trade["size"])
        val tradePrices = parser.asMap(trade["price"])
        val stepSize =
            parser.asDecimal(parser.value(market, "configs.stepSize") ?: 0.001)!!
        if (tradeSize != null) {
            val modifiedTradeSize = tradeSize.mutable()
            when (input) {
                "size.size" -> {
                    val price =
                        parser.asDecimal(nonMarketOrderPrice(tradePrices, market, type, isBuying))
                    val size = parser.asDecimal(tradeSize.get("size"))
                    val usdcSize =
                        if (price != null && size != null) (price * size).doubleValue(false) else null
                    modifiedTradeSize.safeSet("usdcSize", usdcSize)
                }

                "size.usdcSize" -> {
                    val price =
                        parser.asDecimal(nonMarketOrderPrice(tradePrices, market, type, isBuying))
                    val usdcSize = parser.asDecimal(tradeSize.get("usdcSize"))
                    val size =
                        if (price != null && usdcSize != null && usdcSize > Numeric.decimal.ZERO && price > Numeric.decimal.ZERO)
                            Rounder.roundDecimal(usdcSize / price, stepSize)
                        else null
                    modifiedTradeSize.safeSet("size", size)
                }

                else -> {
                    val price =
                        parser.asDecimal(nonMarketOrderPrice(tradeSize, market, type, isBuying))
                    val size = parser.asDecimal(tradeSize["size"])
                    val usdcSize =
                        if (price != null && size != null) (price * size).doubleValue(false) else null
                    modifiedTradeSize.safeSet("usdcSize", usdcSize)
                }
            }
            modifiedTrade["size"] = modifiedTradeSize
        }

        modifiedTrade.safeSet("marketOrder", null)
        return modifiedTrade
    }

    private fun nonMarketOrderPrice(
        prices: IMap<String, Any>?,
        market: IMap<String, Any>?,
        type: String,
        isBuying: Boolean,
    ): Double? {
        val prices = prices ?: return null
        when (type) {
            "LIMIT", "STOP_LIMIT", "TAKE_PROFIT" -> {
                return parser.asDouble(prices["limitPrice"])
            }

            "TRAIING_STOP" -> {
                market?.let {
                    parser.asDouble(market["oraclePrice"])?.let { oraclePrice ->
                        val trailingPercent =
                            parser.asDouble(prices["trailingPercent"]) ?: Numeric.double.ZERO
                        if (trailingPercent != Numeric.double.ZERO) {
                            val percent =
                                if (isBuying)
                                    (Numeric.double.ONE - trailingPercent)
                                else
                                    (Numeric.double.ONE + trailingPercent)
                            return oraclePrice * percent
                        }
                    }
                }
                return null
            }

            "STOP_MARKET", "TAKE_PROFIT_MARKET" -> {
                return parser.asDouble(prices["triggerPrice"])
            }

            else -> {
                return null
            }
        }
    }

    private fun orderbook(
        market: IMap<String, Any>?,
        buy: Boolean?,
    ): IList<IMap<String, Any>>? {
        parser.asMap(market?.get("orderbook_consolidated"))?.let { orderbook ->
            return when (buy) {
                true -> parser.asList(orderbook["asks"]) as? IList<IMap<String, Any>>
                false -> parser.asList(orderbook["bids"]) as? IList<IMap<String, Any>>
                else -> null
            }
        }
        return null
    }

    private fun isBuying(trade: IMap<String, Any>): Boolean {
        return when (parser.asString(trade["side"])) {
            "BUY" -> true
            "SELL" -> false
            else -> true
        }
    }

    private fun calculateMarketOrderTrade(
        trade: IMap<String, Any>,
        market: IMap<String, Any>?,
        subaccount: IMap<String, Any>?,
        user: IMap<String, Any>,
        isBuying: Boolean,
        input: String,
    ): IMap<String, Any> {
        val modified = calculateSize(trade, subaccount, market)
        val marketOrder = calculateMarketOrder(modified, market, subaccount, user, isBuying, input)
        val filled = parser.asBool(marketOrder?.get("filled")) ?: false
        val tradeSize = parser.asMap(modified["size"])?.mutable()
        when (input) {
            "size.size", "size.percent" -> tradeSize?.safeSet(
                "usdcSize",
                if (filled) parser.asDouble(marketOrder?.get("usdcSize")) else null
            )

            "size.usdcSize" -> tradeSize?.safeSet(
                "size",
                if (filled) parser.asDouble(marketOrder?.get("size")) else null
            )

            "size.leverage" -> {
                tradeSize?.safeSet(
                    "size",
                    if (filled) parser.asDouble(marketOrder?.get("size")) else null
                )
                tradeSize?.safeSet(
                    "usdcSize",
                    if (filled) parser.asDouble(marketOrder?.get("usdcSize")) else null
                )

                val orderbook = parser.asMap(market?.get("orderbook_consolidated"))
                if (marketOrder != null && orderbook != null) {
                    val side = side(marketOrder, orderbook)
                    if (side != null && side != parser.asString(modified["side"])) {
                        modified.safeSet("side", side)
                    }
                }
            }
        }
        modified.safeSet("marketOrder", marketOrder)
        modified.safeSet("size", tradeSize)

        return modified
    }

    private fun calculateSize(
        trade: IMap<String, Any>,
        subaccount: IMap<String, Any>?,
        market: IMap<String, Any>?,
    ): IMutableMap<String, Any> {
        val modified = trade.mutable()
        if (calculation == TradeCalculation.trade) {
            return modified
        } else {
            val inputType = parser.asString(parser.value(trade, "size.input"))
            val marketId = parser.asString(trade["marketId"]) ?: return modified
            val position = parser.asMap(parser.value(subaccount, "openPositions.$marketId"))
                ?: return modified
            val positionSize =
                parser.asDecimal(parser.value(position, "size.current")) ?: return modified
            val positionSizeAbs = positionSize.abs()
            modified.safeSet("side", if (positionSize > Numeric.double.ZERO) "SELL" else "BUY")
            when (inputType) {
                "size.percent" -> {
                    val percent =
                        parser.asDecimal(parser.value(trade, "size.percent"))?.abs()
                            ?: return modified
                    val size =
                        if (percent > Numeric.decimal.ONE) positionSizeAbs else positionSizeAbs * percent
                    val stepSize = parser.asDecimal(parser.value(market, "configs.stepSize"))
                        ?: return modified
                    modified.safeSet("size.size", Rounder.roundDecimal(size, stepSize))
                    return modified
                }

                "size.size" -> {
                    modified.safeSet("size.percent", null)
                    val size =
                        parser.asDecimal(parser.value(trade, "size.size")) ?: return modified
                    if (size > positionSizeAbs) {
                        modified.safeSet("size.size", positionSizeAbs)
                    }
                }

                else -> {}
            }
            return modified
        }
    }

    private fun calculateMarketOrder(
        trade: IMap<String, Any>,
        market: IMap<String, Any>?,
        subaccount: IMap<String, Any>?,
        user: IMap<String, Any>,
        isBuying: Boolean,
        input: String,
    ): IMap<String, Any>? {
        val tradeSize = parser.asMap(trade["size"])
        if (tradeSize != null) {
            val stepSize =
                parser.asDouble(parser.value(market, "configs.stepSize"))
                    ?: 0.001
            return when (input) {
                "size.size", "size.percent" -> {
                    val orderbook = orderbook(market, isBuying)
                    if (orderbook != null)
                        calculateMarketOrderFromSize(
                            parser.asDouble(tradeSize["size"]),
                            orderbook
                        )
                    else null
                }

                "size.usdcSize" -> {
                    val orderbook = orderbook(market, isBuying)
                    if (orderbook != null)
                        calculateMarketOrderFromUsdcSize(
                            parser.asDouble(tradeSize["usdcSize"]),
                            orderbook,
                            stepSize
                        )
                    else null
                }

                "size.leverage" -> {
                    val equity = parser.asDouble(parser.value(subaccount, "equity.current"))
                    val oraclePrice =
                        parser.asDouble(parser.value(market, "oraclePrice"))
                    val feeRate =
                        parser.asDouble(parser.value(user, "takerFeeRate"))
                            ?: Numeric.double.ZERO
                    val positions = parser.asMap(subaccount?.get("openPositions"))
                    val positionSize = if (positions != null && market != null) parser.asDouble(
                        parser.value(
                            positions,
                            "${market["id"]}.size.current"
                        )
                    ) else null
                    val leverage = parser.asDouble(parser.value(trade, "size.leverage"))
                    if (equity != null && equity > Numeric.double.ZERO && oraclePrice != null && leverage != null) {
                        val existingLeverage =
                            ((positionSize ?: Numeric.double.ZERO) * oraclePrice) / equity
                        val calculatedIsBuying =
                            if (leverage > existingLeverage) true else if (leverage < existingLeverage) false else null
                        if (calculatedIsBuying != null) {
                            val orderbook = orderbook(market, calculatedIsBuying)
                            if (orderbook != null)
                                calculateMarketOrderFromLeverage(
                                    equity,
                                    oraclePrice,
                                    positionSize,
                                    calculatedIsBuying,
                                    feeRate,
                                    leverage,
                                    stepSize,
                                    orderbook
                                ) else null
                        } else null
                    } else null
                }

                else -> null
            }
        }
        return null
    }

    private fun calculateMarketOrderFromSize(
        size: Double?,
        orderbook: IList<IMap<String, Any>>,
    ): IMap<String, Any>? {
        size?.let {
            val desiredSize = parser.asDecimal(size)!!
            var sizeTotal = Numeric.decimal.ZERO
            var usdcSizeTotal = Numeric.decimal.ZERO
            var worstPrice: BigDecimal? = null
            var filled = false
            val marketOrderOrderBook = iMutableListOf<IMap<String, Any>>()
            orderbookLoop@ for (i in 0 until orderbook.size) {
                val entry = orderbook[i]
                val entryPrice = parser.asDecimal(entry["price"])
                val entrySize = parser.asDecimal(entry["size"])

                if (entryPrice != null && entrySize != null) {
                    filled = (sizeTotal + entrySize >= size)

                    val matchedSize = if (filled) (desiredSize - sizeTotal) else entrySize
                    val matchedUsdcSize = matchedSize * entryPrice

                    sizeTotal = sizeTotal.plus(matchedSize)
                    usdcSizeTotal = usdcSizeTotal.plus(matchedUsdcSize)

                    worstPrice = entryPrice
                    marketOrderOrderBook.add(matchingOrderbookEntry(entry, matchedSize))
                    if (filled) {
                        break@orderbookLoop
                    }
                }
            }
            return marketOrder(
                marketOrderOrderBook,
                sizeTotal,
                usdcSizeTotal,
                worstPrice,
                filled
            )
        }
        return null
    }

    private fun marketOrder(
        orderbook: IList<IMap<String, Any>>,
        size: BigDecimal?,
        usdcSize: BigDecimal?,
        worstPrice: BigDecimal?,
        filled: Boolean,
    ): IMap<String, Any>? {
        return if (size != null && size > BigDecimal.ZERO && usdcSize != null) {
            val marketOrder = iMutableMapOf<String, Any>()
            marketOrder.safeSet("orderbook", orderbook)
            marketOrder.safeSet("price", (usdcSize / size).doubleValue(false))
            marketOrder.safeSet("size", size)
            marketOrder.safeSet("usdcSize", usdcSize)
            marketOrder.safeSet("worstPrice", worstPrice)
            marketOrder.safeSet("filled", filled)
            marketOrder
        } else {
            null
        }
    }

    private fun calculateMarketOrderFromUsdcSize(
        usdcSize: Double?,
        orderbook: IList<IMap<String, Any>>,
        stepSize: Double,
    ): IMap<String, Any>? {
        usdcSize?.let {
            val desiredUsdcSize = parser.asDecimal(usdcSize)!!
            var sizeTotal = Numeric.decimal.ZERO
            var usdcSizeTotal = Numeric.decimal.ZERO
            var worstPrice: BigDecimal? = null
            var filled = false
            val marketOrderOrderBook = iMutableListOf<IMap<String, Any>>()

            val stepSizeDecimal = parser.asDecimal(stepSize)!!
            orderbookLoop@ for (i in 0 until orderbook.size) {
                val entry = orderbook[i]
                val entryPrice = parser.asDecimal(entry["price"])
                val entrySize = parser.asDecimal(entry["size"])

                if (entryPrice != null && entryPrice > Numeric.decimal.ZERO && entrySize != null) {
                    val entryUsdcSize = entrySize * entryPrice
                    filled = (usdcSizeTotal + entryUsdcSize >= desiredUsdcSize)

                    var matchedSize = entrySize
                    var matchedUsdcSize = entryUsdcSize
                    if (filled) {
                        matchedUsdcSize = desiredUsdcSize - usdcSizeTotal
                        matchedSize = matchedUsdcSize / entryPrice
                        matchedSize =
                            Rounder.roundDecimal(
                                matchedSize,
                                stepSizeDecimal
                            )
                        matchedUsdcSize = matchedSize * entryPrice
                    }
                    matchedSize.let {
                        sizeTotal = sizeTotal + matchedSize
                        usdcSizeTotal = usdcSizeTotal + matchedUsdcSize

                        worstPrice = entryPrice
                        marketOrderOrderBook.add(
                            matchingOrderbookEntry(
                                entry,
                                matchedSize
                            )
                        )
                    }
                    if (filled) {
                        break@orderbookLoop
                    }
                }
            }
            return marketOrder(
                marketOrderOrderBook,
                sizeTotal,
                usdcSizeTotal,
                worstPrice,
                filled
            )
        }
        return null
    }

    private fun matchingOrderbookEntry(
        entry: IMap<String, Any>,
        size: BigDecimal,
    ): IMap<String, Any> {
        val matchingEntry = entry.toIMutableMap()
        matchingEntry.safeSet("size", size.doubleValue(false))
        return matchingEntry
    }

    private fun rounded(
        sizeTotal: BigDecimal,
        desiredSize: BigDecimal,
        stepSize: BigDecimal,
    ): BigDecimal {
        val desiredTotal = sizeTotal + desiredSize
        val rounded = Rounder.roundDecimal(desiredTotal, stepSize)
        return rounded - sizeTotal
    }

    private fun calculateMarketOrderFromLeverage(
        equity: Double,
        oraclePrice: Double,
        positionSize: Double?,
        isBuying: Boolean,
        feeRate: Double,
        leverage: Double,
        stepSize: Double,
        orderbook: IList<IMap<String, Any>>,
    ): IMap<String, Any>? {
        /*
             leverage = (size * oracle_price) / account_equity
             leverage and size are signed

             new_account_equity = old_account_equity + order_size * (oracle_price - market_price) - abs(order_size) * market_price * fee rate
             order_size is signed

             (old_size + order_size) * oracle_price = leverage * (old_account_equity + order_size * (oracle_price - market_price) - abs(order_size) * market_price * fee_rate)

             X = order_size
             SZ = old_size
             OR = oracle_price
             AE = account_equity
             MP = market price
             FR = fee rate
             LV = leverage
             PS = positionSign LONG ? 1 : -1
             OS = orderSign BUY ? 1 : -1

             (SZ + X) * OR = LV * (AE + X * (OR - MP) - OS * X * MP * FR)
             SZ * OR + OR * X = LV * AE + LV * X * (OR - MP) - OS * LV * MP * FR * X
             OR * X + OS * LV * MP * FR * X - LV * X * (OR - MP) = LV * AE - SZ * OR
             X = (LV * AE - SZ * OR) / (OR + OS * LV * MP * FR - LV * (OR - MP))
             X = (LV * AE - SZ * OR) / (OR + OS * LV * MP * FR - LV * (OR - MP))

            new(AE) = AE + X * (OR - MP) - abs(X) * MP * FR
        */
        var sizeTotal = Numeric.decimal.ZERO
        var usdcSizeTotal = Numeric.decimal.ZERO
        var worstPrice: BigDecimal? = null
        var filled = false
        val marketOrderOrderBook = iMutableListOf<IMap<String, Any>>()

        /*
        Breaking naming rules a little bit to match the documentation above
         */
        val OR = parser.asDecimal(oraclePrice)!!
        val LV = parser.asDecimal(leverage)!!
        val OS: BigDecimal =
            if (isBuying) Numeric.decimal.POSITIVE else Numeric.decimal.NEGATIVE
        val FR = parser.asDecimal(feeRate)!!

        var AE = parser.asDecimal(equity)!!
        var SZ = parser.asDecimal(positionSize) ?: Numeric.decimal.ZERO

        val stepSizeDecimal = parser.asDecimal(stepSize)!!
        orderbookLoop@ for (i in 0 until orderbook.size) {
            val entry = orderbook[i]

            val entryPrice = parser.asDecimal(entry["price"])
            val entrySize = parser.asDecimal(entry["size"])
            if (entryPrice != null && entryPrice != Numeric.decimal.ZERO && entrySize != null) {
                val MP = entryPrice
                val X = ((LV * AE) - (SZ * OR)) /
                        (OR + (OS * LV * MP * FR) - (LV * (OR - MP)))
                val desiredSize = X.abs()
                if (desiredSize < entrySize) {
                    val rounded = this.rounded(sizeTotal, desiredSize, stepSizeDecimal)
                    sizeTotal = sizeTotal + rounded
                    usdcSizeTotal = usdcSizeTotal + rounded * MP
                    worstPrice = entryPrice
                    filled = true
                    marketOrderOrderBook.add(matchingOrderbookEntry(entry, rounded))
                } else {
                    sizeTotal = sizeTotal + entrySize
                    usdcSizeTotal = usdcSizeTotal + entrySize * MP
                    /*
                    new(AE) = AE + X * (OR - MP) - abs(X) * MP * FR
                    */
                    var signedSize = entrySize
                    if (!isBuying) {
                        signedSize = signedSize * Numeric.decimal.NEGATIVE
                    }
                    AE = AE + (signedSize * (OR - MP)) - (entrySize * MP * FR)
                    SZ = SZ + signedSize
                    marketOrderOrderBook.add(
                        matchingOrderbookEntry(
                            entry,
                            entrySize
                        )
                    )
                }
            }

            if (filled) {
                break@orderbookLoop
            }
        }
        return marketOrder(
            marketOrderOrderBook,
            sizeTotal,
            usdcSizeTotal,
            worstPrice,
            filled
        )
    }

    private fun side(marketOrder: IMap<String, Any>, orderbook: IMap<String, Any>): String? {
        val firstMarketOrderbookPrice =
            parser.asDouble(parser.value(marketOrder, "orderbook.0.price")) ?: return null
        val firstAskPrice =
            parser.asDouble(parser.value(orderbook, "asks.0.price")) ?: return null
        val firstBidPrice =
            parser.asDouble(parser.value(orderbook, "bids.0.price")) ?: return null
        return if (firstMarketOrderbookPrice == firstAskPrice) "BUY"
        else if (firstMarketOrderbookPrice == firstBidPrice) "SELL"
        else null
    }

    private fun finalize(
        trade: IMap<String, Any>,
        subaccount: IMap<String, Any>?,
        user: IMap<String, Any>?,
        market: IMap<String, Any>?,
        type: String,
    ): IMap<String, Any> {
        val marketId = parser.asString(market?.get("id"))
        val position = if (marketId != null) parser.asMap(
            parser.value(
                subaccount,
                "openPositions.$marketId"
            )
        ) else null
        val modified = defaultOptions(trade, position, market)
        val fields = requiredFields(trade)
        modified.safeSet("fields", fields)
        modified.safeSet("options", calculatedOptionsFromFields(fields, position, market))
        modified.safeSet("summary", summaryForType(trade, subaccount, user, market, type))

        return modified
    }

    private fun requiredFields(trade: IMap<String, Any>): IList<Any>? {
        val type = parser.asString(trade["type"])
        val timeInForce = parser.asString(trade["timeInForce"])
        return when (type) {
            "MARKET" -> fieldList(
                iListOf(
                    sizeField(),
                    leverageField(),
                    bracketsField()
                ), reduceOnlyField()
            )

            "STOP_MARKET", "TAKE_PROFIT_MARKET" -> fieldList(
                iListOf(
                    sizeField(),
                    triggerPriceField(),
                    goodUntilField(),
                    executionField(false)
                ), reduceOnlyField()
            )

            "LIMIT" -> {
                when (timeInForce) {
                    "GTT" -> fieldList(
                        iListOf(
                            sizeField(),
                            limitPriceField(),
                            timeInForceField(),
                            goodUntilField(),
                            postOnlyField()
                        ), reduceOnlyField()
                    )

                    else -> fieldList(
                        iListOf(
                            sizeField(),
                            limitPriceField(),
                            timeInForceField(),
                        ), reduceOnlyField()
                    )
                }
            }

            "STOP_LIMIT", "TAKE_PROFIT" -> fieldList(
                iListOf(
                    sizeField(),
                    limitPriceField(),
                    triggerPriceField(),
                    goodUntilField(),
                    executionField(true),
                ), reduceOnlyField()
            )

            "TRAILING_STOP" -> fieldList(
                iListOf(
                    sizeField(),
                    trailingPercentField(),
                    goodUntilField(),
                    executionField(false),
                ), reduceOnlyField()
            )

            else -> null
        }
    }

    private fun fieldList(
        list: IList<IMap<String, Any>>,
        reduceOnly: IMap<String, Any>?,
    ): IList<IMap<String, Any>> {
        return if (reduceOnly != null) {
            val modified = list.toIMutableList()
            modified.add(reduceOnly)
            modified
        } else list
    }

    private fun sizeField(): IMap<String, Any> {
        return iMapOf(
            "field" to "size.size",
            "type" to "double"
        )
    }

    private fun leverageField(): IMap<String, Any> {
        return iMapOf(
            "field" to "size.leverage",
            "type" to "double"
        )
    }

    private fun limitPriceField(): IMap<String, Any> {
        return iMapOf(
            "field" to "price.limitPrice",
            "type" to "double"
        )
    }

    private fun triggerPriceField(): IMap<String, Any> {
        return iMapOf(
            "field" to "price.triggerPrice",
            "type" to "double"
        )
    }

    private fun trailingPercentField(): IMap<String, Any> {
        return iMapOf(
            "field" to "price.trailingPercent",
            "type" to "double"
        )
    }

    private fun reduceOnlyField(): IMap<String, Any>? {
        return if (reduceOnlySupported) {
            iMapOf(
                "field" to "reduceOnly",
                "type" to "bool",
                "default" to false
            )
        } else null
    }

    private fun reducedOnlyFieldWithTimeInForce(trade: IMap<String, Any>): IMap<String, Any>? {
        val timeInForce = parser.asString(
            parser.value(
                trade,
                "timeInForce"
            )
        )
        return when (timeInForce) {
            "FOK", "IOC" -> reduceOnlyField()
            else -> null
        }
    }

    private fun reducedOnlyFieldWithExecution(trade: IMap<String, Any>): IMap<String, Any>? {
        val execution = parser.asString(
            parser.value(
                trade,
                "execution"
            )
        )
        return when (execution) {
            "FOK", "IOC" -> reduceOnlyField()
            else -> null
        }
    }

    private fun postOnlyField(): IMap<String, Any> {
        return iMapOf(
            "field" to "postOnly",
            "type" to "bool",
            "default" to false
        )
    }

    private fun bracketsField(): IMap<String, Any> {
        return iMapOf(
            "field" to "brackets",
            "type" to iListOf(
                stopLossField(),
                takeProfitField(),
                goodUntilField(),
                executionField(false)
            )
        )
    }

    private fun stopLossField(): IMap<String, Any> {
        return iMapOf(
            "field" to "stopLoss",
            "type" to fieldList(
                iListOf(
                    priceField(),
                ),
                reduceOnlyField()
            )
        )
    }

    private fun takeProfitField(): IMap<String, Any> {
        return iMapOf(
            "field" to "takeProfit",
            "type" to fieldList(
                iListOf(
                    priceField(),
                ),
                reduceOnlyField()
            )
        )
    }

    private fun priceField(): IMap<String, Any> {
        return iMapOf(
            "field" to "price",
            "type" to "double"
        )
    }

    private fun timeInForceField(): IMap<String, Any> {
        return iMapOf(
            "field" to "timeInForce",
            "type" to "string",
            "options" to if (fokDisabled) iListOf(
                timeInForceOptionGTT,
                timeInForceOptionIOC
            ) else iListOf(
                timeInForceOptionGTT,
                timeInForceOptionFOK,
                timeInForceOptionIOC
            )
        )
    }

    private fun goodUntilField(): IMap<String, Any> {
        return iMapOf(
            "field" to "goodUntil",
            "type" to iListOf(
                goodUntilDurationField(),
                goodUntilUnitField()
            )
        )
    }

    private fun goodUntilDurationField(): IMap<String, Any> {
        return iMapOf(
            "field" to "duration",
            "type" to "int"
        )
    }

    private fun goodUntilUnitField(): IMap<String, Any> {
        return iMapOf(
            "field" to "unit",
            "type" to "string",
            "options" to iListOf(
                goodUntilUnitMinutes,
                goodUntilUnitHours,
                goodUntilUnitDays,
                goodUntilUnitWeeks
            )
        )
    }

    private fun executionField(conditionalLimit: Boolean): IMap<String, Any> {
        return iMapOf(
            "field" to "execution",
            "type" to "string",
            "options" to if (fokDisabled) {
                if (conditionalLimit) iListOf(
                    executionDefault,
                    executionPostOnly,
                    executionIOC
                ) else iListOf(
                    executionIOC
                )
            } else {
                if (conditionalLimit) iListOf(
                    executionDefault,
                    executionPostOnly,
                    executionFOK,
                    executionIOC
                ) else iListOf(
                    executionFOK,
                    executionIOC
                )
            }
        )
    }

    private fun calculatedOptionsFromFields(
        fields: IList<Any>?,
        position: IMap<String, Any>?,
        market: IMap<String, Any>?,
    ): IMap<String, Any>? {
        fields?.let { fields ->
            val options = iMutableMapOf<String, Any>(
                "needsSize" to false,
                "needsLeverage" to false,
                "needsTriggerPrice" to false,
                "needsLimitPrice" to false,
                "needsTrailingPercent" to false,
                "needsReduceOnly" to false,
                "needsPostOnly" to false,
                "needsBrackets" to false,
                "needsTimeInForce" to false,
                "needsGoodUntil" to false,
                "needsExecution" to false
            )
            for (item in fields) {
                parser.asMap(item)?.let { field ->
                    when (parser.asString(field["field"])) {
                        "size.size" -> options["needsSize"] = true
                        "size.leverage" -> options["needsLeverage"] = true
                        "price.triggerPrice" -> options["needsTriggerPrice"] = true
                        "price.limitPrice" -> options["needsLimitPrice"] = true
                        "price.trailingPercent" -> options["needsTrailingPercent"] = true
                        "timeInForce" -> {
                            options.safeSet(
                                "timeInForceOptions",
                                parser.asList(field["options"])
                            )
                            options.safeSet("needsTimeInForce", true)
                        }

                        "goodUntil" -> {
                            options.safeSet(
                                "goodUntilUnitOptions",
                                parser.asList(parser.value(field, "type.1.options"))
                            )
                            options.safeSet("needsGoodUntil", true)
                        }

                        "execution" -> {
                            options.safeSet(
                                "executionOptions",
                                parser.asList(field["options"])
                            )
                            options.safeSet("needsExecution", true)
                        }

                        "reduceOnly" -> options["needsReduceOnly"] = true
                        "postOnly" -> options["needsPostOnly"] = true
                        "brackets" -> options["needsBrackets"] = true

                    }
                }
            }
            if (parser.asBool(options["needsLeverage"]) == true) {
                options.safeSet("maxLeverage", maxLeverage(position, market))
            } else {
                options.safeSet("maxLeverage", null)
            }
            return options
        }
        return null
    }

    private fun maxLeverage(
        position: IMap<String, Any>?,
        market: IMap<String, Any>?,
    ): Double? {
        return null
    }

    private fun calculatedOptions(
        trade: IMap<String, Any>,
        position: IMap<String, Any>?,
        market: IMap<String, Any>?,
    ): IMap<String, Any>? {
        val fields = requiredFields(trade)
        return calculatedOptionsFromFields(fields, position, market)
    }

    private fun defaultOptions(
        trade: IMap<String, Any>,
        position: IMap<String, Any>?,
        market: IMap<String, Any>?,
    ): IMutableMap<String, Any> {
        val modified = trade.toIMutableMap()
        parser.asList(calculatedOptions(trade, position, market)?.get("timeInForceOptions"))
            ?.let { items ->
                if (!found(parser.asString(trade["timeInForce"]), items)) {
                    modified.safeSet("timeInForce", first(items))
                }
            }
        parser.asList(calculatedOptions(trade, position, market)?.get("goodUntilUnitOptions"))
            ?.let { items ->
                val key = "goodUntil.unit"
                if (!found(parser.asString(parser.value(trade, key)), items)) {
                    modified.safeSet("goodUntil.unit", "D")
                }
            }
        parser.asList(calculatedOptions(trade, position, market)?.get("executionOptions"))
            ?.let { items ->
                if (!found(parser.asString(trade["execution"]), items)) {
                    modified.safeSet("execution", first(items))
                }
            }
        if (parser.asBool(
                calculatedOptions(
                    trade,
                    position,
                    market
                )?.get("needsGoodUntil")
            ) == true
        ) {
            val key = "goodUntil.duration"
            if (parser.value(modified, key) == null) {
                modified.safeSet("goodUntil.duration", 28)
            }
        }

        return modified
    }

    private fun found(data: String?, options: IList<Any>): Boolean {
        if (data != null) {
            for (option in options) {
                parser.asMap(option)?.let {
                    if (parser.asString(it["type"]) == data) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun first(options: IList<Any>): String? {
        return parser.asString(parser.asMap(options.firstOrNull())?.get("type"))
    }

    private fun summaryForType(
        trade: IMap<String, Any>,
        subaccount: IMap<String, Any>?,
        user: IMap<String, Any>?,
        market: IMap<String, Any>?,
        type: String,
    ): IMap<String, Any> {
        val summary = iMutableMapOf<String, Any>()
        val multiplier =
            if (parser.asString(trade["side"]) == "SELL") Numeric.decimal.POSITIVE else Numeric.decimal.NEGATIVE
        when (type) {
            "MARKET" -> {
                parser.asMap(trade["marketOrder"])?.let { marketOrder ->
                    val feeRate = parser.asDouble(parser.value(user, "takerFeeRate"))
                    val bestPrice = marketOrderBestPrice(marketOrder)
                    val worstPrice = marketOrderWorstPrice(marketOrder)
                    val slippage =
                        if (worstPrice != null && bestPrice != null && bestPrice > Numeric.double.ZERO) Rounder.round(
                            (worstPrice - bestPrice).abs() / bestPrice,
                            0.00001
                        ) else null

                    val price = marketOrderPrice(marketOrder)
                    val payloadPrice = if (price != null) {
                        when (parser.asString(trade["side"])) {
                            "BUY" -> price * (Numeric.double.ONE + MARKET_ORDER_MAX_SLIPPAGE)
                            else -> price * (Numeric.double.ONE - MARKET_ORDER_MAX_SLIPPAGE)
                        }
                    } else null
                    val size = marketOrderSize(marketOrder)
                    val usdcSize =
                        if (price != null && size != null) (parser.asDecimal(price)!! * parser.asDecimal(
                            size
                        )!!) else null
                    val fee =
                        if (usdcSize != null && feeRate != null) (usdcSize * parser.asDecimal(
                            feeRate
                        )!!) else null
                    val total =
                        if (usdcSize != null) (usdcSize * multiplier + (fee
                            ?: Numeric.decimal.ZERO) * Numeric.decimal.NEGATIVE) else null

                    val indexPrice = parser.asDouble(market?.get("indexPrice"))
                        ?: parser.asDouble(market?.get("oraclePrice"))  // if no indexPrice(v4), use oraclePrice
                    val priceDiff =
                        slippage(worstPrice, indexPrice, parser.asString(trade["side"]))
                    val indexSlippage =
                        if (priceDiff != null && indexPrice != null && indexPrice > Numeric.double.ZERO) Rounder.round(
                            priceDiff / indexPrice, 0.00001
                        ) else null
                    /*
                    indexSlippage can be negative. For example, it is OK to buy below index price
                     */

                    summary.safeSet("price", price)
                    summary.safeSet("payloadPrice", payloadPrice)
                    summary.safeSet("size", size)
                    summary.safeSet("usdcSize", usdcSize?.doubleValue(false))
                    summary.safeSet("fee", fee?.doubleValue(false))
                    summary.safeSet("feeRate", feeRate)
                    summary.safeSet("total", total?.doubleValue(false))
                    summary.safeSet("slippage", slippage)
                    summary.safeSet("indexSlippage", indexSlippage)
                    summary.safeSet("filled", marketOrderFilled(marketOrder))
                }
            }

            "STOP_MARKET", "TAKE_PROFIT_MARKET" -> {
                parser.asMap(trade["marketOrder"])?.let { marketOrder ->
                    val feeRate = parser.asDouble(parser.value(user, "takerFeeRate"))
                    val bestPrice = marketOrderBestPrice(marketOrder)
                    val worstPrice = marketOrderWorstPrice(marketOrder)
                    val slippage =
                        if (worstPrice != null && bestPrice != null && bestPrice > Numeric.double.ZERO) Rounder.round(
                            (worstPrice - bestPrice).abs() / bestPrice,
                            0.00001
                        ) else null

                    val triggerPrice =
                        parser.asDouble(parser.value(trade, "price.triggerPrice"))
                    val marketOrderPrice = marketOrderPrice(marketOrder)
                    val priceslippage =
                        if (bestPrice != null && marketOrderPrice != null) (marketOrderPrice - bestPrice) else null
                    val slippagePercentage =
                        if (priceslippage != null && bestPrice != null)
                            abs(priceslippage / bestPrice)
                        else null
                    val adjustedslippagePercentage = if (slippagePercentage != null) {
                        val majorMarket = when (parser.asString(trade["marketId"])) {
                            "BTC-USD", "ETH-USD" -> true
                            else -> false
                        }
                        if (majorMarket) {
                            if (type == "STOP_MARKET") {
                                slippagePercentage + STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                            } else {
                                slippagePercentage + TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                            }
                        } else {
                            if (type == "STOP_MARKET") {
                                slippagePercentage + STOP_MARKET_ORDER_SLIPPAGE_BUFFER
                            } else {
                                slippagePercentage + TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER
                            }
                        }
                    } else null

                    val price = if (triggerPrice != null && slippagePercentage != null) {
                        if (parser.asString(trade["side"]) == "BUY") {
                            triggerPrice * (Numeric.double.ONE + slippagePercentage)
                        } else {
                            triggerPrice * (Numeric.double.ONE - slippagePercentage)
                        }
                    } else {
                        null
                    }

                    val payloadPrice =
                        if (triggerPrice != null && adjustedslippagePercentage != null) {
                            if (parser.asString(trade["side"]) == "BUY") {
                                triggerPrice * (Numeric.double.ONE + adjustedslippagePercentage)
                            } else {
                                triggerPrice * (Numeric.double.ONE - adjustedslippagePercentage)
                            }
                        } else {
                            null
                        }

                    val size = parser.asDouble(marketOrderSize(marketOrder))
                    val usdcSize =
                        if (price != null && size != null) parser.asDecimal(price * size) else null
                    val fee =
                        if (usdcSize != null && feeRate != null) (usdcSize * parser.asDecimal(
                            feeRate
                        )!!) else null
                    val total =
                        if (usdcSize != null) (usdcSize * multiplier + (fee
                            ?: Numeric.decimal.ZERO) * Numeric.decimal.NEGATIVE) else null

                    summary.safeSet("price", price)
                    summary.safeSet("payloadPrice", payloadPrice)
                    summary.safeSet("size", size)
                    summary.safeSet("usdcSize", usdcSize?.doubleValue(false))
                    summary.safeSet("fee", fee?.doubleValue(false))
                    summary.safeSet("feeRate", feeRate)
                    summary.safeSet("total", total?.doubleValue(false))
                    summary.safeSet("slippage", slippage)
                    summary.safeSet("filled", marketOrderFilled(marketOrder))
                }
            }

            "LIMIT", "STOP_LIMIT", "TAKE_PROFIT" -> {
                val feeRate = parser.asDouble(parser.value(user, "takerFeeRate"))
                val price = parser.asDouble(parser.value(trade, "price.limitPrice"))
                val size = parser.asDouble(parser.value(trade, "size.size"))
                val usdcSize =
                    if (price != null && size != null) parser.asDecimal(price * size) else null
                val fee =
                    if (usdcSize != null && feeRate != null) (usdcSize * parser.asDecimal(feeRate)!!) else null
                val total =
                    if (usdcSize != null) (usdcSize * multiplier + (fee
                        ?: Numeric.decimal.ZERO) * Numeric.decimal.NEGATIVE) else null

                summary.safeSet("price", price)
                summary.safeSet("payloadPrice", price)
                summary.safeSet("size", size)
                summary.safeSet("usdcSize", usdcSize?.doubleValue(false))
                summary.safeSet("fee", fee?.doubleValue(false))
                summary.safeSet("feeRate", feeRate)
                summary.safeSet("total", total?.doubleValue(false))
                summary.safeSet("filled", true)
            }

            "TRAILING_STOP" -> {
                val feeRate = parser.asDecimal(parser.value(user, "takerFeeRate"))
                val trailingPercent =
                    parser.asDecimal(parser.value(trade, "price.trailingPercent"))
                val side = parser.asString(trade["side"])
                val price: BigDecimal? = if (trailingPercent != null) {
                    parser.asDecimal(parser.value(market, "oraclePrice"))?.let {
                        if (side == "BUY") {
                            it * (Numeric.decimal.ONE + trailingPercent)
                        } else {
                            it * (Numeric.decimal.ONE - trailingPercent)
                        }
                    }
                } else null

                val size = parser.asDecimal(parser.value(trade, "size.size"))
                val usdcSize = if (price != null && size != null) (price * size) else null
                val fee =
                    if (usdcSize != null && feeRate != null) (usdcSize * feeRate) else null
                val total =
                    if (usdcSize != null) (usdcSize * multiplier + (fee
                        ?: Numeric.decimal.ZERO) * Numeric.decimal.NEGATIVE) else null

                summary.safeSet("price", price?.doubleValue(false))
                summary.safeSet("size", size?.doubleValue(false))
                summary.safeSet("usdcSize", usdcSize?.doubleValue(false))
                summary.safeSet("fee", fee?.doubleValue(false))
                summary.safeSet("feeRate", feeRate?.doubleValue(false))
                summary.safeSet("total", total?.doubleValue(false))
                summary.safeSet("filled", true)
            }

            else -> {}
        }
        return summary
    }

    private fun slippage(price: Double?, indexPrice: Double?, side: String?): Double? {
        return if (price != null && indexPrice != null) {
            if (side == "BUY") price - indexPrice else indexPrice - price
        } else null
    }

    private fun marketOrderBestPrice(marketOrder: IMap<String, Any>): Double? {
        parser.asList(marketOrder["orderbook"])?.let { orderbook ->
            if (orderbook.isNotEmpty()) {
                parser.asMap(orderbook.firstOrNull())?.let { firstLine ->
                    parser.asDouble(firstLine["price"])?.let { bestPrice ->
                        if (bestPrice != Numeric.double.ZERO) {
                            return bestPrice
                        }
                    }
                }
            }
        }
        return null
    }

    private fun marketOrderWorstPrice(marketOrder: IMap<String, Any>): Double? {
        return parser.asDouble(marketOrder["worstPrice"])
    }

    private fun marketOrderPrice(marketOrder: IMap<String, Any>): Double? {
        return parser.asDouble(marketOrder["price"])
    }

    private fun marketOrderSize(marketOrder: IMap<String, Any>): Double? {
        return parser.asDouble(marketOrder["size"])
    }

    private fun marketOrderUsdcSize(marketOrder: IMap<String, Any>): Double? {
        return parser.asDouble(marketOrder["usdcSize"])
    }

    private fun marketOrderFilled(marketOrder: IMap<String, Any>): Boolean? {
        return parser.asBool(marketOrder["filled"])
    }

    private fun market(
        trade: IMap<String, Any>,
        markets: IMap<String, Any>,
    ): IMap<String, Any>? {
        parser.asString(trade["marketId"])?.let {
            return parser.asMap(markets[it])
        }
        return null
    }

    private val timeInForceOptionGTT: IMap<String, Any>
        get() = iMapOf("type" to "GTT", "stringKey" to "APP.TRADE.GOOD_TIL_TIME")
    private val timeInForceOptionFOK: IMap<String, Any>
        get() = iMapOf("type" to "FOK", "stringKey" to "APP.TRADE.FILL_OR_KILL")
    private val timeInForceOptionIOC: IMap<String, Any>
        get() = iMapOf("type" to "IOC", "stringKey" to "APP.TRADE.IMMEDIATE_OR_CANCEL")

    private val goodUntilUnitMinutes: IMap<String, Any>
        get() = iMapOf(
            "type" to "M",
            "stringKey" to "APP.GENERAL.TIME_STRINGS.MINUTES_ABBREVIATED"
        )
    private val goodUntilUnitHours: IMap<String, Any>
        get() = iMapOf(
            "type" to "H",
            "stringKey" to "APP.GENERAL.TIME_STRINGS.HOURS_ABBREVIATED"
        )
    private val goodUntilUnitDays: IMap<String, Any>
        get() = iMapOf(
            "type" to "D",
            "stringKey" to "APP.GENERAL.TIME_STRINGS.DAYS_ABBREVIATED"
        )
    private val goodUntilUnitWeeks: IMap<String, Any>
        get() = iMapOf(
            "type" to "W",
            "stringKey" to "APP.GENERAL.TIME_STRINGS.WEEKS_ABBREVIATED"
        )

    private val executionDefault: IMap<String, Any>
        get() = iMapOf("type" to "DEFAULT", "stringKey" to "APP.GENERAL.DEFAULT")
    private val executionPostOnly: IMap<String, Any>
        get() = iMapOf("type" to "POST_ONLY", "stringKey" to "APP.TRADE.POST_ONLY")
    private val executionFOK: IMap<String, Any>
        get() = iMapOf("type" to "FOK", "stringKey" to "APP.TRADE.FILL_OR_KILL")
    private val executionIOC: IMap<String, Any>
        get() = iMapOf("type" to "IOC", "stringKey" to "APP.TRADE.IMMEDIATE_OR_CANCEL")
}
