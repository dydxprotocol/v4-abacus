package exchange.dydx.abacus.calculator.v2

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.state.internalstate.InternalOrderbookTick
import exchange.dydx.abacus.state.internalstate.InternalRewardsParamsState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.InternalUserState
import exchange.dydx.abacus.state.internalstate.InternalWalletState
import exchange.dydx.abacus.state.internalstate.safeCreate
import exchange.dydx.abacus.state.model.ClosePositionInputField
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal class TradeInputCalculatorV2(
    private val calculation: TradeCalculation,
    private val marginModeCalculator: TradeInputMarginModeCalculator = TradeInputMarginModeCalculator(),
) {
    fun calculate(
        trade: InternalTradeInputState,
        wallet: InternalWalletState,
        marketSummary: InternalMarketSummaryState,
        rewardsParams: InternalRewardsParamsState?,
        subaccountNumber: Int,
        input: String?,
    ): InternalTradeInputState {
        val account = wallet.account
        val subaccount =
            account.groupedSubaccounts[subaccountNumber] ?: account.subaccounts[subaccountNumber]
        val user = wallet.user
        val markets = marketSummary.markets

        marginModeCalculator.updateTradeInputMarginMode(
            tradeInput = trade,
            markets = markets,
            account = account,
            subaccountNumber = subaccountNumber,
        )

        if (input != null) {
            when (trade.type) {
                OrderType.Market,
                OrderType.StopMarket,
                OrderType.TakeProfitMarket ->
                    calculateMarketOrderTrade(
                        trade = trade,
                        market = markets[trade.marketId],
                        subaccount = subaccount,
                        user = user,
                        input = input,
                    )

                OrderType.Limit -> TODO()
                OrderType.StopLimit -> TODO()
                OrderType.TakeProfitLimit -> TODO()
                OrderType.TrailingStop -> TODO()
                OrderType.Liquidated -> TODO()
                OrderType.Liquidation -> TODO()
                OrderType.Offsetting -> TODO()
                OrderType.Deleveraged -> TODO()
                OrderType.FinalSettlement -> TODO()
                null -> TODO()
            }
        }

        return trade
    }

    private fun calculateMarketOrderTrade(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        input: String?,
    ): InternalTradeInputState {
        if (calculation == TradeCalculation.closePosition) {
            calculateClosePositionSize(trade, market, subaccount)
        }
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

    private fun calculateClosePositionSize(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?,
    ): InternalTradeInputState {
        val inputType = ClosePositionInputField.invoke(trade.size?.input)
        val marketId = trade.marketId ?: return trade
        val position = subaccount?.openPositions?.get(marketId) ?: return trade
        val positionSize = position.calculated[CalculationPeriod.current]?.size ?: return trade
        val positionSizeAbs = positionSize.abs()
        trade.side = if (positionSize > Numeric.double.ZERO) OrderSide.Sell else OrderSide.Buy
        when (inputType) {
            ClosePositionInputField.percent -> {
                val percent = trade.sizePercent ?: return trade
                val size =
                    if (percent > Numeric.double.ONE) positionSizeAbs else positionSizeAbs * percent
                val stepSize = market?.perpetualMarket?.configs?.stepSize ?: return trade
                trade.size =
                    TradeInputSize.safeCreate(trade.size).copy(size = Rounder.round(size, stepSize))
                return trade
            }

            ClosePositionInputField.size -> {
                trade.sizePercent = null
                val size = trade.size?.size ?: return trade
                if (size > positionSizeAbs) {
                    trade.size = TradeInputSize.safeCreate(trade.size).copy(size = positionSizeAbs)
                }
            }

            else -> {}
        }
        return trade
    }

    private fun calculateMarketOrder(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        input: String?,
    ): Map<String, Any>? {
        val tradeSize = trade.size
        if (tradeSize != null) {
            return when (input) {
                "size.size", "size.percent" -> {
                    val orderbook = getOrderbook(market = market, isBuying = trade.isBuying)
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

    private fun getOrderbook(
        market: InternalMarketState?,
        isBuying: Boolean?,
    ): List<InternalOrderbookTick>? {
        return when (isBuying) {
            true -> return market?.consolidatedOrderbook?.asks
            false -> return market?.consolidatedOrderbook?.bids
            else -> return null
        }
    }

    private fun calculateMarketOrderFromSize(
        size: Double?,
        orderbook: List<InternalOrderbookTick>?,
    ): Map<String, Any>? {
        return if (size != null && size != Numeric.double.ZERO) {
            if (orderbook != null) {
                val desiredSize = size
                var sizeTotal = Numeric.double.ZERO
                var usdcSizeTotal = Numeric.double.ZERO
                var worstPrice: Double? = null
                var filled = false
                val marketOrderOrderBook = mutableListOf<InternalOrderbookTick>()
                orderbookLoop@ for (element in orderbook) {
                    val entry = element
                    val entryPrice = entry.price
                    val entrySize = entry.size

                    filled = (sizeTotal + entrySize >= size)

                    val matchedSize = if (filled) (desiredSize - sizeTotal) else entrySize
                    val matchedUsdcSize = matchedSize * entryPrice

                    sizeTotal += matchedSize
                    usdcSizeTotal += matchedUsdcSize

                    worstPrice = entryPrice
                    marketOrderOrderBook.add(entry.copy(size = matchedSize))
                    if (filled) {
                        break@orderbookLoop
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
            val marketOrder = exchange.dydx.abacus.utils.mutableMapOf<String, Any>()
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
}