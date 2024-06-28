@file:Suppress("ktlint:standard:property-naming")

package exchange.dydx.abacus.calculator

import abs
import exchange.dydx.abacus.calculator.SlippageConstants.MARKET_ORDER_MAX_SLIPPAGE
import exchange.dydx.abacus.calculator.SlippageConstants.STOP_MARKET_ORDER_SLIPPAGE_BUFFER
import exchange.dydx.abacus.calculator.SlippageConstants.STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
import exchange.dydx.abacus.calculator.SlippageConstants.TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER
import exchange.dydx.abacus.calculator.SlippageConstants.TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.EnvironmentFeatureFlags
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.mutableMapOf
import exchange.dydx.abacus.utils.safeSet
import kollections.JsExport
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.pow

@JsExport
@Serializable
enum class TradeCalculation(val rawValue: String) {
    trade("TRADE"),
    closePosition("CLOSE_POSITION");

    companion object {
        operator fun invoke(rawValue: String) =
            TradeCalculation.values().firstOrNull { it.rawValue == rawValue }
    }
}

internal object SlippageConstants {
    const val MARKET_ORDER_MAX_SLIPPAGE = 0.05
    const val STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET = 0.1
    const val TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET = 0.1
    const val STOP_MARKET_ORDER_SLIPPAGE_BUFFER = 0.2
    const val TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER = 0.2
}

@Suppress("UNCHECKED_CAST")
internal class TradeInputCalculator(
    val parser: ParserProtocol,
    private val calculation: TradeCalculation,
    val featureFlags: EnvironmentFeatureFlags,
) {
    private val accountTransformer = AccountTransformer()

    internal fun calculate(
        state: Map<String, Any>,
        subaccountNumber: Int,
        input: String?,
    ): Map<String, Any> {
        val account = parser.asNativeMap(state["account"])
        val subaccount = if (subaccountNumber != null) {
            parser.asMap(parser.value(account, "groupedSubaccounts.$subaccountNumber"))
                ?: parser.asNativeMap(
                    parser.value(
                        account,
                        "subaccounts.$subaccountNumber",
                    ),
                )
        } else {
            null
        }
        val user = parser.asNativeMap(state["user"]) ?: mapOf()
        val markets = parser.asNativeMap(state["markets"])
        val rewardsParams = parser.asNativeMap(state["rewardsParams"])
        val trade = updateTradeInputFromMarket(
            markets,
            account,
            parser.asNativeMap(state["trade"]),
            subaccountNumber ?: 0,
        )
        val marketId = parser.asString(trade?.get("marketId"))
        val type = parser.asString(trade?.get("type"))
        val market = if (marketId != null) parser.asNativeMap(markets?.get(marketId)) else null
        val feeTiers = parser.asNativeList(parser.value(state, "configs.feeTiers"))

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
                            input,
                        )
                    }

                    else -> {
                        calculateNonMarketTrade(
                            trade,
                            market,
                            type,
                            isBuying,
                            input,
                        )
                    }
                }
                finalize(
                    modifiedTrade,
                    account,
                    subaccount,
                    user,
                    market,
                    rewardsParams,
                    feeTiers,
                    type,
                )
            } else {
                finalize(trade, account, subaccount, user, market, rewardsParams, feeTiers, type)
            }
            modified["trade"] = trade
            modified.safeSet(
                "account",
                accountTransformer.applyTradeToAccount(
                    account,
                    subaccountNumber,
                    trade,
                    market,
                    parser,
                    "postOrder",
                ),
            )
            modified
        } else {
            state
        }
    }

    private fun updateTradeInputFromMarket(
        markets: Map<String, Any>?,
        account: Map<String, Any>?,
        tradeInput: Map<String, Any>?,
        subaccountNumber: Int,
    ): MutableMap<String, Any>? {
        val modified = tradeInput?.mutable() ?: return null
        val marketId = parser.asString(tradeInput["marketId"])
        val existingMarginMode =
            MarginCalculator.findExistingMarginMode(
                parser,
                account,
                marketId,
                subaccountNumber,
            )
        // If there is an existing position or order, we have to use the same margin mode
        if (existingMarginMode != null) {
            modified["marginMode"] = existingMarginMode
            if (
                existingMarginMode == "ISOLATED" &&
                parser.asDouble(tradeInput["targetLeverage"]) == null
            ) {
                modified["targetLeverage"] = 1.0
            }
        } else {
            val marketMarginMode = MarginCalculator.findMarketMarginMode(
                parser,
                parser.asMap(markets?.get(marketId)),
            )
            when (marketMarginMode) {
                "ISOLATED" -> {
                    modified["marginMode"] = marketMarginMode
                    if (parser.asDouble(tradeInput["targetLeverage"]) == null) {
                        modified["targetLeverage"] = 1.0
                    }
                }

                "CROSS" -> {
                    if (modified["marginMode"] == null) {
                        modified["marginMode"] = marketMarginMode
                    }
                }
            }
        }
        return modified
    }

    private fun calculateNonMarketTrade(
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        type: String,
        isBuying: Boolean,
        input: String,
    ): Map<String, Any> {
        val modifiedTrade = trade.mutable()
        val tradeSize = parser.asNativeMap(trade["size"])
        val tradePrices = parser.asNativeMap(trade["price"])
        val stepSize =
            parser.asDouble(parser.value(market, "configs.stepSize") ?: 0.001)!!
        if (tradeSize != null) {
            val modifiedTradeSize = tradeSize.mutable()
            when (input) {
                "size.size" -> {
                    val price = nonMarketOrderPrice(tradePrices, market, type, isBuying)
                    val size = parser.asDouble(tradeSize.get("size"))
                    val usdcSize =
                        if (price != null && size != null) (price * size) else null
                    modifiedTradeSize.safeSet("usdcSize", usdcSize)
                }

                "size.usdcSize" -> {
                    val price = nonMarketOrderPrice(tradePrices, market, type, isBuying)
                    val usdcSize = parser.asDouble(tradeSize.get("usdcSize"))
                    val size =
                        if (price != null && usdcSize != null && usdcSize > Numeric.double.ZERO && price > Numeric.double.ZERO) {
                            Rounder.round(usdcSize / price, stepSize)
                        } else {
                            null
                        }
                    modifiedTradeSize.safeSet("size", size)
                }

                else -> {
                    val price = nonMarketOrderPrice(tradeSize, market, type, isBuying)
                    val size = parser.asDouble(tradeSize["size"])
                    val usdcSize =
                        if (price != null && size != null) (price * size) else null
                    modifiedTradeSize.safeSet("usdcSize", usdcSize)
                }
            }
            modifiedTrade["size"] = modifiedTradeSize
        }

        modifiedTrade.safeSet("marketOrder", null)
        return modifiedTrade
    }

    private fun nonMarketOrderPrice(
        prices: Map<String, Any>?,
        market: Map<String, Any>?,
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
                                if (isBuying) {
                                    (Numeric.double.ONE - trailingPercent)
                                } else {
                                    (Numeric.double.ONE + trailingPercent)
                                }
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
        market: Map<String, Any>?,
        buy: Boolean?,
    ): List<Map<String, Any>>? {
        parser.asNativeMap(market?.get("orderbook_consolidated"))?.let { orderbook ->
            return when (buy) {
                true -> parser.asNativeList(orderbook["asks"]) as? List<Map<String, Any>>
                false -> parser.asNativeList(orderbook["bids"]) as? List<Map<String, Any>>
                else -> null
            }
        }
        return null
    }

    private fun isBuying(trade: Map<String, Any>): Boolean {
        return when (parser.asString(trade["side"])) {
            "BUY" -> true
            "SELL" -> false
            else -> true
        }
    }

    private fun calculateMarketOrderTrade(
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        user: Map<String, Any>,
        isBuying: Boolean,
        input: String,
    ): Map<String, Any> {
        val modified = calculateSize(trade, subaccount, market)
        val marketOrder = calculateMarketOrder(modified, market, subaccount, user, isBuying, input)
        val filled = parser.asBool(marketOrder?.get("filled")) ?: false
        val tradeSize = parser.asNativeMap(modified["size"])?.mutable()
        when (input) {
            "size.size", "size.percent" -> tradeSize?.safeSet(
                "usdcSize",
                if (filled) parser.asDouble(marketOrder?.get("usdcSize")) else null,
            )

            "size.usdcSize" -> tradeSize?.safeSet(
                "size",
                if (filled) parser.asDouble(marketOrder?.get("size")) else null,
            )

            "size.leverage" -> {
                tradeSize?.safeSet(
                    "size",
                    if (filled) parser.asDouble(marketOrder?.get("size")) else null,
                )
                tradeSize?.safeSet(
                    "usdcSize",
                    if (filled) parser.asDouble(marketOrder?.get("usdcSize")) else null,
                )

                val orderbook = parser.asNativeMap(market?.get("orderbook_consolidated"))
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
        trade: Map<String, Any>,
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
    ): MutableMap<String, Any> {
        val modified = trade.mutable()
        if (calculation == TradeCalculation.trade) {
            return modified
        } else {
            val inputType = parser.asString(parser.value(trade, "size.input"))
            val marketId = parser.asString(trade["marketId"]) ?: return modified
            val position = parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId"))
                ?: return modified
            val positionSize =
                parser.asDouble(parser.value(position, "size.current")) ?: return modified
            val positionSizeAbs = positionSize.abs()
            modified.safeSet("side", if (positionSize > Numeric.double.ZERO) "SELL" else "BUY")
            when (inputType) {
                "size.percent" -> {
                    val percent =
                        parser.asDouble(parser.value(trade, "size.percent"))?.abs()
                            ?: return modified
                    val size =
                        if (percent > Numeric.double.ONE) positionSizeAbs else positionSizeAbs * percent
                    val stepSize = parser.asDouble(parser.value(market, "configs.stepSize"))
                        ?: return modified
                    modified.safeSet("size.size", Rounder.round(size, stepSize))
                    return modified
                }

                "size.size" -> {
                    modified.safeSet("size.percent", null)
                    val size =
                        parser.asDouble(parser.value(trade, "size.size")) ?: return modified
                    if (size > positionSizeAbs) {
                        modified.safeSet("size.size", positionSizeAbs)
                    }
                }

                else -> {}
            }
            return modified
        }
    }

    /**
     * Calculate the current and postOrder position margin to be displayed in the TradeInput Summary.
     */
    private fun calculatePositionMargin(
        trade: Map<String, Any>,
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?
    ): Double? {
        if (subaccount == null || market == null) {
            return null
        }

        val marginMode = parser.asString(parser.value(trade, "marginMode"))
        val marketId = parser.asString(market["id"]) ?: return null
        val position = parser.asNativeMap(
            parser.value(
                subaccount,
                "openPositions.$marketId",
            ),
        )

        if (position != null) {
            when (marginMode) {
                "ISOLATED" -> {
                    val currentEquity = parser.asDouble(parser.value(position, "equity.current"))
                    val postOrderEquity = parser.asDouble(parser.value(position, "equity.postOrder"))
                    if (currentEquity != null) {
                        if (postOrderEquity != null) {
                            return postOrderEquity
                        }
                        return currentEquity
                    }
                }

                "CROSS" -> {
                    val currentNotionalTotal =
                        parser.asDouble(parser.value(position, "notionalTotal.current"))
                    val postOrderNotionalTotal =
                        parser.asDouble(parser.value(position, "notionalTotal.postOrder"))
                    val mmf =
                        parser.asDouble(parser.value(market, "configs.maintenanceMarginFraction"))
                    if (currentNotionalTotal != null && mmf != null) {
                        if (postOrderNotionalTotal != null) {
                            return postOrderNotionalTotal.times(mmf)
                        }
                        return currentNotionalTotal.times(mmf)
                    }
                }

                else -> return null
            }
        }
        return null
    }

    /**
     * Return Subaccount leverage to display as Position leverage in the TradeInput Summary.
     * Use Subaccount leverage since a subaccount can only have 1 isolated position
     */
    private fun getPositionLeverage(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?
    ): Double? {
        if (subaccount == null || market == null) return null
        val subaccountNumber = parser.asInt(subaccount["subaccountNumber"]) ?: return null

        if (subaccountNumber >= NUM_PARENT_SUBACCOUNTS) {
            val currentLeverage = parser.asDouble(parser.value(subaccount, "leverage.current"))
            val postOrderLeverage = parser.asDouble(parser.value(subaccount, "leverage.postOrder"))
            return postOrderLeverage ?: currentLeverage
        }

        val marketId = parser.asString(market["id"])
        val position = parser.asNativeMap(
            parser.value(
                subaccount,
                "openPositions.$marketId",
            ),
        )
        val currentLeverage = parser.asDouble(parser.value(position, "leverage.current"))
        val postOrderLeverage = parser.asDouble(parser.value(position, "leverage.postOrder"))
        return postOrderLeverage ?: currentLeverage
    }

    private fun calculateMarketOrder(
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        user: Map<String, Any>,
        isBuying: Boolean,
        input: String,
    ): Map<String, Any>? {
        val tradeSize = parser.asNativeMap(trade["size"])
        if (tradeSize != null) {
            return when (input) {
                "size.size", "size.percent" -> {
                    val orderbook = orderbook(market, isBuying)
                    calculateMarketOrderFromSize(
                        parser.asDouble(tradeSize["size"]),
                        orderbook,
                    )
                }

                "size.usdcSize" -> {
                    val stepSize =
                        parser.asDouble(parser.value(market, "configs.stepSize"))
                            ?: 0.001
                    val orderbook = orderbook(market, isBuying)
                    calculateMarketOrderFromUsdcSize(
                        parser.asDouble(tradeSize["usdcSize"]),
                        orderbook,
                        stepSize,
                    )
                }

                "size.leverage" -> {
                    val leverage =
                        parser.asDouble(parser.value(trade, "size.leverage")) ?: return null
                    calculateMarketOrderFromLeverage(
                        leverage,
                        market,
                        subaccount,
                        user,
                    )
                }

                else -> null
            }
        }
        return null
    }

    private fun calculateMarketOrderFromLeverage(
        leverage: Double,
        market: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        user: Map<String, Any>,
    ): Map<String, Any>? {
        val stepSize =
            parser.asDouble(parser.value(market, "configs.stepSize"))
                ?: 0.001
        val equity = parser.asDouble(parser.value(subaccount, "equity.current")) ?: return null
        val oraclePrice =
            parser.asDouble(parser.value(market, "oraclePrice")) ?: return null
        val feeRate =
            parser.asDouble(parser.value(user, "takerFeeRate"))
                ?: Numeric.double.ZERO
        val positions = parser.asNativeMap(subaccount?.get("openPositions"))
        val positionSize = if (positions != null && market != null) {
            parser.asDouble(
                parser.value(
                    positions,
                    "${market["id"]}.size.current",
                ),
            )
        } else {
            null
        }
        return if (equity > Numeric.double.ZERO) {
            val existingLeverage =
                ((positionSize ?: Numeric.double.ZERO) * oraclePrice) / equity
            val calculatedIsBuying =
                if (leverage > existingLeverage) {
                    true
                } else if (leverage < existingLeverage) {
                    false
                } else {
                    null
                }
            if (calculatedIsBuying != null) {
                val orderbook = orderbook(market, calculatedIsBuying)
                if (orderbook != null) {
                    calculateMarketOrderFromLeverage(
                        equity,
                        oraclePrice,
                        positionSize,
                        calculatedIsBuying,
                        feeRate,
                        leverage,
                        stepSize,
                        orderbook,
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun calculateMarketOrderFromSize(
        size: Double?,
        orderbook: List<Map<String, Any>>?,
    ): Map<String, Any>? {
        return if (size != null && size != Numeric.double.ZERO) {
            if (orderbook != null) {
                val desiredSize = parser.asDouble(size)!!
                var sizeTotal = Numeric.double.ZERO
                var usdcSizeTotal = Numeric.double.ZERO
                var worstPrice: Double? = null
                var filled = false
                val marketOrderOrderBook = mutableListOf<Map<String, Any>>()
                orderbookLoop@ for (i in 0 until orderbook.size) {
                    val entry = orderbook[i]
                    val entryPrice = parser.asDouble(entry["price"])
                    val entrySize = parser.asDouble(entry["size"])

                    if (entryPrice != null && entrySize != null) {
                        filled = (sizeTotal + entrySize >= size)

                        val matchedSize = if (filled) (desiredSize - sizeTotal) else entrySize
                        val matchedUsdcSize = matchedSize * entryPrice

                        sizeTotal = sizeTotal + matchedSize
                        usdcSizeTotal = usdcSizeTotal + matchedUsdcSize

                        worstPrice = entryPrice
                        marketOrderOrderBook.add(matchingOrderbookEntry(entry, matchedSize))
                        if (filled) {
                            break@orderbookLoop
                        }
                    }
                }
                marketOrder(
                    marketOrderOrderBook,
                    sizeTotal,
                    usdcSizeTotal,
                    worstPrice,
                    filled,
                )
            } else {
                marketOrder(
                    mutableListOf<Map<String, Any>>(),
                    parser.asDouble(size)!!,
                    Numeric.double.ZERO,
                    null,
                    false,
                )
            }
        } else {
            null
        }
    }

    private fun marketOrder(
        orderbook: List<Map<String, Any>>,
        size: Double?,
        usdcSize: Double?,
        worstPrice: Double?,
        filled: Boolean,
    ): Map<String, Any>? {
        return if (size != null && usdcSize != null) {
            val marketOrder = mutableMapOf<String, Any>()
            marketOrder.safeSet("orderbook", orderbook)
            if (size != Numeric.double.ZERO) {
                marketOrder.safeSet("price", (usdcSize / size))
            }
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
        orderbook: List<Map<String, Any>>?,
        stepSize: Double,
    ): Map<String, Any>? {
        return if (usdcSize != null && usdcSize != Numeric.double.ZERO) {
            if (orderbook != null) {
                val desiredUsdcSize = parser.asDouble(usdcSize)!!
                var sizeTotal = Numeric.double.ZERO
                var usdcSizeTotal = Numeric.double.ZERO
                var worstPrice: Double? = null
                var filled = false
                val marketOrderOrderBook = mutableListOf<Map<String, Any>>()

                orderbookLoop@ for (i in 0 until orderbook.size) {
                    val entry = orderbook[i]
                    val entryPrice = parser.asDouble(entry["price"])
                    val entrySize = parser.asDouble(entry["size"])

                    if (entryPrice != null && entryPrice > Numeric.double.ZERO && entrySize != null) {
                        val entryUsdcSize = entrySize * entryPrice
                        filled = (usdcSizeTotal + entryUsdcSize >= desiredUsdcSize)

                        var matchedSize = entrySize
                        var matchedUsdcSize = entryUsdcSize
                        if (filled) {
                            matchedUsdcSize = desiredUsdcSize - usdcSizeTotal
                            matchedSize = matchedUsdcSize / entryPrice
                            matchedSize =
                                Rounder.quickRound(
                                    matchedSize,
                                    stepSize,
                                )
                            matchedUsdcSize = matchedSize * entryPrice
                        }
                        sizeTotal += matchedSize
                        usdcSizeTotal += matchedUsdcSize

                        worstPrice = entryPrice
                        marketOrderOrderBook.add(
                            matchingOrderbookEntry(
                                entry,
                                matchedSize,
                            ),
                        )
                        if (filled) {
                            break@orderbookLoop
                        }
                    }
                }
                marketOrder(
                    marketOrderOrderBook,
                    sizeTotal,
                    usdcSizeTotal,
                    worstPrice,
                    filled,
                )
            } else {
                marketOrder(
                    mutableListOf<Map<String, Any>>(),
                    Numeric.double.ZERO,
                    usdcSize,
                    null,
                    false,
                )
            }
        } else {
            null
        }
    }

    private fun matchingOrderbookEntry(
        entry: Map<String, Any>,
        size: Double,
    ): Map<String, Any> {
        val matchingEntry = entry.toMutableMap()
        matchingEntry.safeSet("size", size)
        return matchingEntry
    }

    private fun rounded(
        sizeTotal: Double,
        desiredSize: Double,
        stepSize: Double,
    ): Double {
        val desiredTotal = sizeTotal + desiredSize
        val rounded = Rounder.quickRound(desiredTotal, stepSize)
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
        orderbook: List<Map<String, Any>>,
    ): Map<String, Any>? {
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
        var sizeTotal = Numeric.double.ZERO
        var usdcSizeTotal = Numeric.double.ZERO
        var worstPrice: Double? = null
        var filled = false
        val marketOrderOrderBook = mutableListOf<Map<String, Any>>()

        /*
        Breaking naming rules a little bit to match the documentation above
         */
        @Suppress("LocalVariableName", "PropertyName")
        val OR = parser.asDouble(oraclePrice)!!

        @Suppress("LocalVariableName", "PropertyName")
        val LV = parser.asDouble(leverage)!!

        @Suppress("LocalVariableName", "PropertyName")
        val OS: Double =
            if (isBuying) Numeric.double.POSITIVE else Numeric.double.NEGATIVE

        @Suppress("LocalVariableName", "PropertyName")
        val FR = parser.asDouble(feeRate)!!

        @Suppress("LocalVariableName", "PropertyName")
        var AE = parser.asDouble(equity)!!

        @Suppress("LocalVariableName", "PropertyName")
        var SZ = parser.asDouble(positionSize) ?: Numeric.double.ZERO

        orderbookLoop@ for (i in 0 until orderbook.size) {
            val entry = orderbook[i]

            val entryPrice = parser.asDouble(entry["price"])
            val entrySize = parser.asDouble(entry["size"])
            if (entryPrice != null && entryPrice != Numeric.double.ZERO && entrySize != null) {
                @Suppress("LocalVariableName", "PropertyName")
                val MP = entryPrice

                @Suppress("LocalVariableName", "PropertyName")
                val X = ((LV * AE) - (SZ * OR)) /
                    (OR + (OS * LV * MP * FR) - (LV * (OR - MP)))
                val desiredSize = X.abs()
                if (desiredSize < entrySize) {
                    val rounded = this.rounded(sizeTotal, desiredSize, stepSize)
                    sizeTotal = Rounder.quickRound(sizeTotal + rounded, stepSize)
                    usdcSizeTotal += rounded * MP
                    worstPrice = entryPrice
                    filled = true
                    marketOrderOrderBook.add(matchingOrderbookEntry(entry, rounded))
                } else {
                    val rounded = this.rounded(sizeTotal, entrySize, stepSize)
                    sizeTotal = Rounder.quickRound(sizeTotal + rounded, stepSize)
                    usdcSizeTotal += rounded * MP
                    /*
                    new(AE) = AE + X * (OR - MP) - abs(X) * MP * FR
                     */
                    var signedSize = rounded
                    if (!isBuying) {
                        signedSize *= Numeric.double.NEGATIVE
                    }
                    AE = AE + (signedSize * (OR - MP)) - (rounded * MP * FR)
                    SZ += signedSize
                    marketOrderOrderBook.add(
                        matchingOrderbookEntry(
                            entry,
                            rounded,
                        ),
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
            filled,
        )
    }

    private fun side(marketOrder: Map<String, Any>, orderbook: Map<String, Any>): String? {
        val firstMarketOrderbookPrice =
            parser.asDouble(parser.value(marketOrder, "orderbook.0.price")) ?: return null
        val firstAskPrice =
            parser.asDouble(parser.value(orderbook, "asks.0.price")) ?: return null
        val firstBidPrice =
            parser.asDouble(parser.value(orderbook, "bids.0.price")) ?: return null
        return if (firstMarketOrderbookPrice == firstAskPrice) {
            "BUY"
        } else if (firstMarketOrderbookPrice == firstBidPrice) {
            "SELL"
        } else {
            null
        }
    }

    private fun finalize(
        trade: Map<String, Any>,
        account: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        user: Map<String, Any>?,
        market: Map<String, Any>?,
        rewardsParams: Map<String, Any>?,
        feeTiers: List<Any>?,
        type: String,
    ): Map<String, Any> {
        val marketId = parser.asString(market?.get("id"))
        val position = if (marketId != null) {
            parser.asNativeMap(
                parser.value(
                    subaccount,
                    "openPositions.$marketId",
                ),
            )
        } else {
            null
        }
        var modified = trade.mutable()
        val fields = requiredFields(account, subaccount, trade, market)
        modified.safeSet("fields", fields)
        modified.safeSet("options", calculatedOptionsFromFields(fields, trade, position, market))
        modified = defaultOptions(account, subaccount, modified, position, market)
        modified.safeSet(
            "summary",
            summaryForType(trade, subaccount, user, market, rewardsParams, feeTiers, type),
        )

        return modified
    }

    private fun requiredFields(
        account: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        trade: Map<String, Any>,
        market: Map<String, Any>?,
    ): List<Any>? {
        val type = parser.asString(trade["type"])
        return when (type) {
            "MARKET" -> {
                val marginMode = parser.asString(trade["marginMode"])
                return when (MarginMode.invoke(marginMode)) {
                    MarginMode.Isolated -> listOf(
                        sizeField(),
                        bracketsField(),
                        marginModeField(market, account, subaccount),
                        reduceOnlyField(),
                    ).filterNotNull()
                    else -> listOf(
                        sizeField(),
                        leverageField(),
                        bracketsField(),
                        marginModeField(market, account, subaccount),
                        reduceOnlyField(),
                    ).filterNotNull()
                }
            }

            "LIMIT" -> {
                val timeInForce = parser.asString(trade["timeInForce"])
                when (timeInForce) {
                    "GTT" ->
                        listOf(
                            sizeField(),
                            limitPriceField(),
                            timeInForceField(),
                            goodTilField(),
                            postOnlyField(),
                            marginModeField(market, account, subaccount),
                        ).filterNotNull()

                    else ->
                        listOf(
                            sizeField(),
                            limitPriceField(),
                            timeInForceField(),
                            marginModeField(market, account, subaccount),
                            reduceOnlyField(),
                        ).filterNotNull()
                }
            }

            "STOP_LIMIT", "TAKE_PROFIT" -> {
                val execution = parser.asString(trade["execution"])
                listOf(
                    sizeField(),
                    limitPriceField(),
                    triggerPriceField(),
                    goodTilField(),
                    executionField(true),
                    marginModeField(market, account, subaccount),
                    when (execution) {
                        "IOC" -> reduceOnlyField()
                        else -> null
                    },
                ).filterNotNull()
            }

            "STOP_MARKET", "TAKE_PROFIT_MARKET" ->
                listOf(
                    sizeField(),
                    triggerPriceField(),
                    goodTilField(),
                    executionField(false),
                    marginModeField(market, account, subaccount),
                    reduceOnlyField(),
                ).filterNotNull()

            "TRAILING_STOP" ->
                listOf(
                    sizeField(),
                    trailingPercentField(),
                    goodTilField(),
                    executionField(false),
                    marginModeField(market, account, subaccount),
                ).filterNotNull()

            else -> null
        }
    }

    private fun sizeField(): Map<String, Any> {
        return mapOf(
            "field" to "size.size",
            "type" to "double",
        )
    }

    private fun leverageField(): Map<String, Any> {
        return mapOf(
            "field" to "size.leverage",
            "type" to "double",
        )
    }

    private fun limitPriceField(): Map<String, Any> {
        return mapOf(
            "field" to "price.limitPrice",
            "type" to "double",
        )
    }

    private fun triggerPriceField(): Map<String, Any> {
        return mapOf(
            "field" to "price.triggerPrice",
            "type" to "double",
        )
    }

    private fun trailingPercentField(): Map<String, Any> {
        return mapOf(
            "field" to "price.trailingPercent",
            "type" to "double",
        )
    }

    private fun reduceOnlyField(): Map<String, Any>? {
        return mapOf(
            "field" to "reduceOnly",
            "type" to "bool",
            "default" to false,
        )
    }

    private fun postOnlyField(): Map<String, Any> {
        return mapOf(
            "field" to "postOnly",
            "type" to "bool",
            "default" to false,
        )
    }

    private fun bracketsField(): Map<String, Any> {
        return mapOf(
            "field" to "brackets",
            "type" to listOf(
                stopLossField(),
                takeProfitField(),
                goodTilField(),
                executionField(false),
            ),
        )
    }

    private fun stopLossField(): Map<String, Any> {
        return mapOf(
            "field" to "stopLoss",
            "type" to
                listOf(
                    priceField(),
                    reduceOnlyField(),
                ).filterNotNull(),
        )
    }

    private fun takeProfitField(): Map<String, Any> {
        return mapOf(
            "field" to "takeProfit",
            "type" to
                listOf(
                    priceField(),
                    reduceOnlyField(),
                ).filterNotNull(),
        )
    }

    private fun priceField(): Map<String, Any> {
        return mapOf(
            "field" to "price",
            "type" to "double",
        )
    }

    private fun timeInForceField(): Map<String, Any> {
        return mapOf(
            "field" to "timeInForce",
            "type" to "string",
            "options" to listOf(
                timeInForceOptionGTT,
                timeInForceOptionIOC,
            ),
        )
    }

    private fun goodTilField(): Map<String, Any> {
        return mapOf(
            "field" to "goodTil",
            "type" to listOf(
                goodTilDurationField(),
                goodTilUnitField(),
            ),
        )
    }

    private fun goodTilDurationField(): Map<String, Any> {
        return mapOf(
            "field" to "duration",
            "type" to "int",
        )
    }

    private fun goodTilUnitField(): Map<String, Any> {
        return mapOf(
            "field" to "unit",
            "type" to "string",
            "options" to listOf(
                goodTilUnitMinutes,
                goodTilUnitHours,
                goodTilUnitDays,
                goodTilUnitWeeks,
            ),
        )
    }

    private fun executionField(includesDefaultAndPostOnly: Boolean): Map<String, Any> {
        return mapOf(
            "field" to "execution",
            "type" to "string",
            "options" to
                if (includesDefaultAndPostOnly) {
                    listOf(
                        executionDefault,
                        executionIOC,
                        executionPostOnly,
                    )
                } else {
                    listOf(
                        executionIOC,
                    )
                },
        )
    }

    private fun marginModeField(
        market: Map<String, Any>?,
        account: Map<String, Any>?,
        subaccount: Map<String, Any>?
    ): Map<String, Any>? {
        val selectableMarginMode = MarginCalculator.selectableMarginModes(
            parser = parser,
            account = account,
            market = market,
            subaccountNumber = parser.asInt(subaccount?.get("subaccountNumber")) ?: 0,
        )
        return if (selectableMarginMode) {
            mapOf(
                "field" to "marginMode",
                "type" to "string",
                "options" to listOf(
                    marginModeCross,
                    marginModeIsolated,
                ),
            )
        } else {
            null
        }
    }

    private fun calculatedOptionsFromFields(
        fields: List<Any>?,
        trade: Map<String, Any>,
        position: Map<String, Any>?,
        market: Map<String, Any>?,
    ): Map<String, Any>? {
        fields?.let { fields ->
            val options = mutableMapOf<String, Any>(
                "needsSize" to false,
                "needsLeverage" to false,
                "needsTargetLeverage" to false,
                "needsTriggerPrice" to false,
                "needsLimitPrice" to false,
                "needsTrailingPercent" to false,
                "needsReduceOnly" to false,
                "needsPostOnly" to false,
                "needsBrackets" to false,
                "needsTimeInForce" to false,
                "needsGoodUntil" to false,
                "needsExecution" to false,
                "needsMarginMode" to false,
            )
            for (item in fields) {
                parser.asNativeMap(item)?.let { field ->
                    when (parser.asString(field["field"])) {
                        "size.size" -> options["needsSize"] = true
                        "size.leverage" -> options["needsLeverage"] = true
                        "price.triggerPrice" -> options["needsTriggerPrice"] = true
                        "price.limitPrice" -> options["needsLimitPrice"] = true
                        "price.trailingPercent" -> options["needsTrailingPercent"] = true
                        "timeInForce" -> {
                            options.safeSet(
                                "timeInForceOptions",
                                parser.asNativeList(field["options"]),
                            )
                            options.safeSet("needsTimeInForce", true)
                        }

                        "goodTil" -> {
                            options.safeSet(
                                "goodTilUnitOptions",
                                parser.asNativeList(parser.value(field, "type.1.options")),
                            )
                            options.safeSet("needsGoodUntil", true)
                        }

                        "execution" -> {
                            options.safeSet(
                                "executionOptions",
                                parser.asNativeList(field["options"]),
                            )
                            options.safeSet("needsExecution", true)
                        }

                        "marginMode" -> {
                            options.safeSet(
                                "marginModeOptions",
                                parser.asNativeList(field["options"]),
                            )
                            options["needsMarginMode"] = true
                        }

                        "reduceOnly" -> {
                            options["needsReduceOnly"] = true
                        }

                        "postOnly" -> options["needsPostOnly"] = true
                        "brackets" -> options["needsBrackets"] = true
                    }
                }
            }
            if (parser.asBool(options["needsLeverage"]) == true) {
                options.safeSet("maxLeverage", maxLeverageFromPosition(position, market))
            } else {
                options.safeSet("maxLeverage", null)
            }
            if (parser.asBool(options["needsReduceOnly"]) == true) {
                options.safeSet("reduceOnlyPromptStringKey", null)
            } else {
                options.safeSet("reduceOnlyPromptStringKey", reduceOnlyPromptFromTrade(trade))
            }
            if (parser.asBool(options["needsPostOnly"]) == true) {
                options.safeSet("postOnlyPromptStringKey", null)
            } else {
                options.safeSet("postOnlyPromptStringKey", postOnlyPromptFromTrade(trade))
            }
            if (parser.asString(parser.value(trade, "marginMode")) == "ISOLATED") {
                options.safeSet("needsTargetLeverage", true)
            } else {
                options.safeSet("needsTargetLeverage", false)
            }
            return options
        }
        return null
    }

    private fun maxLeverageFromPosition(
        position: Map<String, Any>?,
        market: Map<String, Any>?,
    ): Double? {
        if (position != null) {
            return parser.asDouble(parser.value(position, "maxLeverage.current"))
        } else {
            val initialMarginFraction =
                parser.asDouble(parser.value(market, "configs.effectiveInitialMarginFraction"))
                    ?: return null
            return 1.0 / initialMarginFraction
        }
    }

    private fun reduceOnlyPromptFromTrade(
        trade: Map<String, Any>,
    ): String? {
        return when (parser.asString(trade["type"])) {
            "LIMIT" -> "GENERAL.TRADE.REDUCE_ONLY_TIMEINFORCE_IOC"
            "STOP_LIMIT", "TAKE_PROFIT" -> "GENERAL.TRADE.REDUCE_ONLY_TIMEINFORCE_IOC"

            else -> return null
        }
    }

    private fun postOnlyPromptFromTrade(
        trade: Map<String, Any>,
    ): String? {
        return when (parser.asString(trade["type"])) {
            "LIMIT" -> "GENERAL.TRADE.POST_ONLY_TIMEINFORCE_GTT"

            else -> return null
        }
    }

    private fun calculatedOptions(
        account: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        trade: Map<String, Any>,
        position: Map<String, Any>?,
        market: Map<String, Any>?,
    ): Map<String, Any>? {
        val fields = requiredFields(account, subaccount, trade, market)
        return calculatedOptionsFromFields(fields, trade, position, market)
    }

    private fun defaultOptions(
        account: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        trade: Map<String, Any>,
        position: Map<String, Any>?,
        market: Map<String, Any>?,
    ): MutableMap<String, Any> {
        val modified = trade.toMutableMap()
        parser.asNativeList(
            calculatedOptions(
                account,
                subaccount,
                trade,
                position,
                market,
            )?.get("timeInForceOptions"),
        )
            ?.let { items ->
                if (!found(parser.asString(trade["timeInForce"]), items)) {
                    modified.safeSet("timeInForce", first(items))
                }
            }
        parser.asNativeList(
            calculatedOptions(
                account,
                subaccount,
                trade,
                position,
                market,
            )?.get("goodTilUnitOptions"),
        )
            ?.let { items ->
                val key = "goodTil.unit"
                if (!found(parser.asString(parser.value(trade, key)), items)) {
                    modified.safeSet("goodTil.unit", "D")
                }
            }
        parser.asNativeList(
            calculatedOptions(
                account,
                subaccount,
                trade,
                position,
                market,
            )?.get("executionOptions"),
        )
            ?.let { items ->
                if (!found(parser.asString(trade["execution"]), items)) {
                    modified.safeSet("execution", first(items))
                }
            }
        parser.asNativeList(
            calculatedOptions(
                account,
                subaccount,
                trade,
                position,
                market,
            )?.get("marginModeOptions"),
        )
            ?.let { items ->
                if (!found(parser.asString(trade["marginMode"]), items)) {
                    modified.safeSet("marginMode", first(items))
                }
            }
        if (parser.asBool(
                calculatedOptions(
                    account,
                    subaccount,
                    trade,
                    position,
                    market,
                )?.get("needsGoodUntil"),
            ) == true
        ) {
            val key = "goodTil.duration"
            if (parser.value(modified, key) == null) {
                modified.safeSet("goodTil.duration", 28)
            }
        }

        return modified
    }

    private fun found(data: String?, options: List<Any>): Boolean {
        if (data != null) {
            for (option in options) {
                parser.asNativeMap(option)?.let {
                    if (parser.asString(it["type"]) == data) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun first(options: List<Any>): String? {
        return parser.asString(parser.asNativeMap(options.firstOrNull())?.get("type"))
    }

    private fun findMaxMakerRebate(feeTiers: List<Any>?): Double {
        if (feeTiers.isNullOrEmpty()) return 0.0

        val smallestNegative = feeTiers.map { parser.asDouble(parser.value(it, "maker")) ?: 0.0 }
            .filter { it < 0.0 }
            .minOrNull()

        return abs(smallestNegative ?: 0.0)
    }

    private fun calculateTakerReward(
        usdcSize: Double?,
        fee: Double?,
        rewardsParams: Map<String, Any>?,
        feeTiers: List<Any>?
    ): Double? {
        val feeMultiplierPpm = parser.asDouble(parser.value(rewardsParams, "feeMultiplierPpm"))
        val tokenPrice = parser.asDouble(parser.value(rewardsParams, "tokenPrice.price"))
        val tokenPriceExponent = parser.asDouble(parser.value(rewardsParams, "tokenPrice.exponent"))
        val notional = parser.asDouble(usdcSize)
        val maxMakerRebate = findMaxMakerRebate(feeTiers)

        if (fee != null &&
            feeMultiplierPpm != null &&
            tokenPrice != null &&
            tokenPriceExponent != null &&
            fee > 0.0 &&
            notional != null &&
            tokenPrice > 0.0
        ) {
            val feeMultiplier = feeMultiplierPpm / QUANTUM_MULTIPLIER
            return feeMultiplier * (fee - maxMakerRebate * notional) / (
                tokenPrice * 10.0.pow(
                    tokenPriceExponent,
                )
                )
        }
        return null
    }

    private fun calculateMakerReward(fee: Double?, rewardsParams: Map<String, Any>?): Double? {
        val feeMultiplierPpm = parser.asDouble(parser.value(rewardsParams, "feeMultiplierPpm"))
        val tokenPrice = parser.asDouble(parser.value(rewardsParams, "tokenPrice.price"))
        val tokenPriceExponent = parser.asDouble(parser.value(rewardsParams, "tokenPrice.exponent"))

        if (fee != null &&
            feeMultiplierPpm != null &&
            tokenPrice != null &&
            tokenPriceExponent != null &&
            fee > 0.0 &&
            tokenPrice > 0.0
        ) {
            val feeMultiplier = feeMultiplierPpm / QUANTUM_MULTIPLIER
            return fee * feeMultiplier / (tokenPrice * 10.0.pow(tokenPriceExponent))
        }
        return null
    }

    private fun summaryForType(
        trade: Map<String, Any>,
        subaccount: Map<String, Any>?,
        user: Map<String, Any>?,
        market: Map<String, Any>?,
        rewardsParams: Map<String, Any>?,
        feeTiers: List<Any>?,
        type: String,
    ): Map<String, Any> {
        val summary = mutableMapOf<String, Any>()
        val multiplier =
            if (parser.asString(trade["side"]) == "SELL") Numeric.double.POSITIVE else Numeric.double.NEGATIVE
        when (type) {
            "MARKET" -> {
                parser.asNativeMap(trade["marketOrder"])?.let { marketOrder ->
                    val feeRate = parser.asDouble(parser.value(user, "takerFeeRate"))
                    val bestPrice = marketOrderBestPrice(marketOrder)
                    val worstPrice = marketOrderWorstPrice(marketOrder)
                    val slippage =
                        if (worstPrice != null && bestPrice != null && bestPrice > Numeric.double.ZERO) {
                            Rounder.round(
                                (worstPrice - bestPrice).abs() / bestPrice,
                                0.00001,
                            )
                        } else {
                            null
                        }

                    val price = marketOrderPrice(marketOrder)
                    val side = parser.asString(trade["side"])
                    val payloadPrice = if (price != null) {
                        when (side) {
                            "BUY" -> price * (Numeric.double.ONE + MARKET_ORDER_MAX_SLIPPAGE)

                            else -> price * (Numeric.double.ONE - MARKET_ORDER_MAX_SLIPPAGE)
                        }
                    } else {
                        null
                    }

                    val size = marketOrderSize(marketOrder)
                    val usdcSize =
                        if (price != null && size != null) (price * size) else null
                    val fee =
                        if (usdcSize != null && feeRate != null) (usdcSize * feeRate) else null
                    val total =
                        if (usdcSize != null) {
                            (
                                usdcSize * multiplier + (
                                    fee
                                        ?: Numeric.double.ZERO
                                    ) * Numeric.double.NEGATIVE
                                )
                        } else {
                            null
                        }

                    val oraclePrice =
                        parser.asDouble(market?.get("oraclePrice")) // if no indexPrice(v4), use oraclePrice
                    val priceDiff =
                        slippage(worstPrice, oraclePrice, parser.asString(trade["side"]))
                    val slippageStepSize = 0.00001
                    val indexSlippage =
                        if (priceDiff != null && oraclePrice != null && oraclePrice > Numeric.double.ZERO) {
                            Rounder.quickRound(
                                priceDiff / oraclePrice,
                                slippageStepSize,
                            )
                        } else {
                            null
                        }
                    /*
                    indexSlippage can be negative. For example, it is OK to buy below index price
                     */
                    val reward = calculateTakerReward(
                        usdcSize,
                        parser.asDouble(fee),
                        rewardsParams,
                        feeTiers,
                    )

                    summary.safeSet("price", price)
                    summary.safeSet("payloadPrice", payloadPrice)
                    summary.safeSet("size", size)
                    summary.safeSet("usdcSize", usdcSize)
                    summary.safeSet("fee", fee)
                    summary.safeSet("feeRate", feeRate)
                    summary.safeSet(
                        "total",
                        if (total == Numeric.double.ZERO) Numeric.double.ZERO else total,
                    )
                    summary.safeSet("slippage", slippage)
                    summary.safeSet("indexSlippage", indexSlippage)
                    summary.safeSet("filled", marketOrderFilled(marketOrder))
                    summary.safeSet("reward", reward)
                    summary.safeSet(
                        "positionMargin",
                        calculatePositionMargin(trade, subaccount, market),
                    )
                    summary.safeSet("positionLeverage", getPositionLeverage(subaccount, market))
                }
            }

            "STOP_MARKET", "TAKE_PROFIT_MARKET" -> {
                parser.asNativeMap(trade["marketOrder"])?.let { marketOrder ->
                    val feeRate = parser.asDouble(parser.value(user, "takerFeeRate"))
                    val bestPrice = marketOrderBestPrice(marketOrder)
                    val worstPrice = marketOrderWorstPrice(marketOrder)
                    val slippageStepSize = 0.00001
                    val slippage =
                        if (worstPrice != null && bestPrice != null && bestPrice > Numeric.double.ZERO) {
                            Rounder.quickRound(
                                (worstPrice - bestPrice).abs() / bestPrice,
                                slippageStepSize,
                            )
                        } else {
                            null
                        }

                    val triggerPrice =
                        parser.asDouble(parser.value(trade, "price.triggerPrice"))
                    val marketOrderPrice = marketOrderPrice(marketOrder)
                    val priceslippage =
                        if (bestPrice != null && marketOrderPrice != null) (marketOrderPrice - bestPrice) else null
                    val slippagePercentage =
                        if (priceslippage != null && bestPrice != null) {
                            abs(priceslippage / bestPrice)
                        } else {
                            null
                        }
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
                    } else {
                        null
                    }

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
                        if (price != null && size != null) (price * size) else null
                    val fee =
                        if (usdcSize != null && feeRate != null) (usdcSize * feeRate) else null
                    val total =
                        if (usdcSize != null) {
                            (
                                usdcSize * multiplier + (
                                    fee
                                        ?: Numeric.double.ZERO
                                    ) * Numeric.double.NEGATIVE
                                )
                        } else {
                            null
                        }

                    val reward = calculateTakerReward(
                        usdcSize,
                        parser.asDouble(fee),
                        rewardsParams,
                        feeTiers,
                    )

                    summary.safeSet("price", price)
                    summary.safeSet("payloadPrice", payloadPrice)
                    summary.safeSet("size", size)
                    summary.safeSet("usdcSize", usdcSize)
                    summary.safeSet("fee", fee)
                    summary.safeSet("feeRate", feeRate)
                    summary.safeSet(
                        "total",
                        if (total == Numeric.double.ZERO) Numeric.double.ZERO else total,
                    )
                    summary.safeSet("slippage", slippage)
                    summary.safeSet("filled", marketOrderFilled(marketOrder))
                    summary.safeSet("reward", reward)
                    summary.safeSet(
                        "positionMargin",
                        calculatePositionMargin(trade, subaccount, market),
                    )
                    summary.safeSet("positionLeverage", getPositionLeverage(subaccount, market))
                }
            }

            "LIMIT", "STOP_LIMIT", "TAKE_PROFIT" -> {
                val timeInForce = parser.asString(trade["timeInForce"])
                val execution = parser.asString(trade["execution"])
                val isMaker = (type == "LIMIT" && timeInForce == "GTT") || execution == "POST_ONLY"

                val feeRate = parser.asDouble(
                    parser.value(
                        user,
                        if (isMaker) "makerFeeRate" else "takerFeeRate",
                    ),
                )
                val price = parser.asDouble(parser.value(trade, "price.limitPrice"))
                val size = parser.asDouble(parser.value(trade, "size.size"))
                val usdcSize =
                    if (price != null && size != null) (price * size) else null
                val fee =
                    if (usdcSize != null && feeRate != null) (usdcSize * feeRate) else null
                val total =
                    if (usdcSize != null) {
                        (
                            usdcSize * multiplier + (
                                fee
                                    ?: Numeric.double.ZERO
                                ) * Numeric.double.NEGATIVE
                            )
                    } else {
                        null
                    }

                val reward =
                    if (isMaker) {
                        calculateMakerReward(parser.asDouble(fee), rewardsParams)
                    } else {
                        calculateTakerReward(
                            usdcSize,
                            parser.asDouble(fee),
                            rewardsParams,
                            feeTiers,
                        )
                    }

                summary.safeSet("price", price)
                summary.safeSet("payloadPrice", price)
                summary.safeSet("size", size)
                summary.safeSet("usdcSize", usdcSize)
                summary.safeSet("fee", fee)
                summary.safeSet("feeRate", feeRate)
                summary.safeSet(
                    "total",
                    if (total == Numeric.double.ZERO) Numeric.double.ZERO else total,
                )
                summary.safeSet("filled", true)
                summary.safeSet("reward", reward)
                summary.safeSet(
                    "positionMargin",
                    calculatePositionMargin(trade, subaccount, market),
                )
                summary.safeSet("positionLeverage", getPositionLeverage(subaccount, market))
            }

            "TRAILING_STOP" -> {
            }

            else -> {}
        }

        return summary
    }

    private fun maxLeverage(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?
    ): Double? {
        if (subaccount == null || market == null) {
            return null
        }
        val marketId = parser.asString(market["id"]) ?: return null
        val position = parser.asNativeMap(
            parser.value(
                subaccount,
                "openPositions.$marketId",
            ),
        )
        return maxLeverageFromPosition(position, market)
    }

    private fun slippage(price: Double?, oraclePrice: Double?, side: String?): Double? {
        return if (price != null && oraclePrice != null) {
            if (side == "BUY") price - oraclePrice else oraclePrice - price
        } else {
            null
        }
    }

    private fun marketOrderBestPrice(marketOrder: Map<String, Any>): Double? {
        parser.asNativeList(marketOrder["orderbook"])?.let { orderbook ->
            if (orderbook.isNotEmpty()) {
                parser.asNativeMap(orderbook.firstOrNull())?.let { firstLine ->
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

    private fun marketOrderWorstPrice(marketOrder: Map<String, Any>): Double? {
        return parser.asDouble(marketOrder["worstPrice"])
    }

    private fun marketOrderPrice(marketOrder: Map<String, Any>): Double? {
        return parser.asDouble(marketOrder["price"])
    }

    private fun marketOrderSize(marketOrder: Map<String, Any>): Double? {
        return parser.asDouble(marketOrder["size"])
    }

    private fun marketOrderUsdcSize(marketOrder: Map<String, Any>): Double? {
        return parser.asDouble(marketOrder["usdcSize"])
    }

    private fun marketOrderFilled(marketOrder: Map<String, Any>): Boolean? {
        return parser.asBool(marketOrder["filled"])
    }

    private fun market(
        trade: Map<String, Any>,
        markets: Map<String, Any>,
    ): Map<String, Any>? {
        parser.asString(trade["marketId"])?.let {
            return parser.asNativeMap(markets[it])
        }
        return null
    }

    private val timeInForceOptionGTT: Map<String, Any>
        get() = mapOf("type" to "GTT", "stringKey" to "APP.TRADE.GOOD_TIL_TIME")
    private val timeInForceOptionIOC: Map<String, Any>
        get() = mapOf("type" to "IOC", "stringKey" to "APP.TRADE.IMMEDIATE_OR_CANCEL")

    private val goodTilUnitMinutes: Map<String, Any>
        get() = mapOf(
            "type" to "M",
            "stringKey" to "APP.GENERAL.TIME_STRINGS.MINUTES_ABBREVIATED",
        )
    private val goodTilUnitHours: Map<String, Any>
        get() = mapOf(
            "type" to "H",
            "stringKey" to "APP.GENERAL.TIME_STRINGS.HOURS_ABBREVIATED",
        )
    private val goodTilUnitDays: Map<String, Any>
        get() = mapOf(
            "type" to "D",
            "stringKey" to "APP.GENERAL.TIME_STRINGS.DAYS_ABBREVIATED",
        )
    private val goodTilUnitWeeks: Map<String, Any>
        get() = mapOf(
            "type" to "W",
            "stringKey" to "APP.GENERAL.TIME_STRINGS.WEEKS_ABBREVIATED",
        )

    private val executionDefault: Map<String, Any>
        get() = mapOf("type" to "DEFAULT", "stringKey" to "APP.TRADE.GOOD_TIL_DATE")
    private val executionPostOnly: Map<String, Any>
        get() = mapOf("type" to "POST_ONLY", "stringKey" to "APP.TRADE.POST_ONLY")
    private val executionIOC: Map<String, Any>
        get() = mapOf("type" to "IOC", "stringKey" to "APP.TRADE.IMMEDIATE_OR_CANCEL")
    private val marginModeCross: Map<String, Any>
        get() = mapOf("type" to "CROSS", "stringKey" to "APP.TRADE.CROSS_MARGIN")
    private val marginModeIsolated: Map<String, Any>
        get() = mapOf("type" to "ISOLATED", "stringKey" to "APP.TRADE.ISOLATED_MARGIN")
}
