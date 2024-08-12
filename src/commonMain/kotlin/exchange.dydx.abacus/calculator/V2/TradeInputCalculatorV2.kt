package exchange.dydx.abacus.calculator.v2

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.OrderbookUsage
import exchange.dydx.abacus.output.input.TradeInputMarketOrder
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
import kollections.toIList

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
        val marketOrder = createMarketOrder(
            trade = trade,
            market = market,
        subaccount = subaccount,
        user = user,
        input = input,
        )
        val filled = marketOrder?.filled ?: false
        var tradeSize = TradeInputSize.safeCreate(trade.size)
        when (input) {
            "size.size", "size.percent" ->
                tradeSize = tradeSize.copy(usdcSize =  if (filled) marketOrder?.usdcSize else null)

            "size.usdcSize" ->
                tradeSize = tradeSize.copy(size = if (filled) marketOrder?.size else null)


            "size.leverage" -> {
                tradeSize = tradeSize.copy(
                    size = if (filled) marketOrder?.size else null,
                    usdcSize = if (filled) marketOrder?.usdcSize else null,
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

        trade.marketOrder = marketOrder
        trade.size = tradeSize

        return trade
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

    private fun createMarketOrder(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        input: String?,
    ): TradeInputMarketOrder? {
        val tradeSize = trade.size
        if (tradeSize != null) {
            return when (input) {
                "size.size", "size.percent" -> {
                    val orderbook = getOrderbook(market = market, isBuying = trade.isBuying)
                    createMarketOrderFromSize(
                        size =  tradeSize.size,
                        orderbook = orderbook,
                    )
                }

                "size.usdcSize" -> {
                    val stepSize = market?.perpetualMarket?.configs?.stepSize ?: 0.001
                    val orderbook = getOrderbook(market = market, isBuying = trade.isBuying)
                    createMarketOrderFromUsdcSize(
                        usdcSize = tradeSize.usdcSize,
                                orderbook = orderbook,
                        stepSize = stepSize,
                    )
                }

                "size.leverage" -> {
                    val leverage = tradeSize.leverage ?: return null
                    createMarketOrderFromLeverage(
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
            true -> market?.consolidatedOrderbook?.asks
            false -> market?.consolidatedOrderbook?.bids
            else -> null
        }
    }

    private fun createMarketOrderFromSize(
        size: Double?,
        orderbook: List<InternalOrderbookTick>?,
    ): TradeInputMarketOrder? {
        return if (size != null && size != Numeric.double.ZERO) {
            if (orderbook != null) {
                val desiredSize = size
                var sizeTotal = Numeric.double.ZERO
                var usdcSizeTotal = Numeric.double.ZERO
                var worstPrice: Double? = null
                var filled = false
                val marketOrderOrderBook = mutableListOf<InternalOrderbookTick>()
                orderbookLoop@ for (element in orderbook) {
                    val entryPrice = element.price
                    val entrySize = element.size

                    filled = (sizeTotal + entrySize >= size)

                    val matchedSize = if (filled) (desiredSize - sizeTotal) else entrySize
                    val matchedUsdcSize = matchedSize * entryPrice

                    sizeTotal += matchedSize
                    usdcSizeTotal += matchedUsdcSize

                    worstPrice = entryPrice
                    marketOrderOrderBook.add(matchingOrderbookEntry(element, matchedSize))
                    if (filled) {
                        break@orderbookLoop
                    }
                }
                createMarketOrderWith(
                    orderbook = marketOrderOrderBook,
                    size = sizeTotal,
                    usdcSize = usdcSizeTotal,
                    worstPrice = worstPrice,
                    filled = filled,
                )
            } else {
                createMarketOrderWith(
                    orderbook = listOf<InternalOrderbookTick>(),
                    size = size,
                    usdcSize = Numeric.double.ZERO,
                    worstPrice = null,
                    filled = false,
                )
            }
        } else {
            null
        }
    }

    private fun createMarketOrderFromUsdcSize(
        usdcSize: Double?,
        orderbook: List<InternalOrderbookTick>?,
        stepSize: Double,
    ): TradeInputMarketOrder? {
        return if (usdcSize != null && usdcSize != Numeric.double.ZERO) {
            if (orderbook != null) {
                val desiredUsdcSize =  usdcSize
                var sizeTotal = Numeric.double.ZERO
                var usdcSizeTotal = Numeric.double.ZERO
                var worstPrice: Double? = null
                var filled = false
                val marketOrderOrderBook = mutableListOf<InternalOrderbookTick>()

                orderbookLoop@ for (element in orderbook) {
                    val entryPrice = element.price
                    val entrySize = element.size

                    if (entryPrice > Numeric.double.ZERO) {
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
                        marketOrderOrderBook.add(matchingOrderbookEntry(element, matchedSize))
                        if (filled) {
                            break@orderbookLoop
                        }
                    }
                }
                createMarketOrderWith(
                    orderbook = marketOrderOrderBook,
                    size = sizeTotal,
                    usdcSize = usdcSizeTotal,
                    worstPrice = worstPrice,
                    filled = filled,
                )
            } else {
                createMarketOrderWith(
                    orderbook = listOf<InternalOrderbookTick>(),
                    size = Numeric.double.ZERO,
                    usdcSize = usdcSize,
                    worstPrice = null,
                    filled = false,
                )
            }
        } else {
            null
        }
    }

    private fun createMarketOrderFromLeverage(
        leverage: Double,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
    ): TradeInputMarketOrder? {
        val stepSize = market?.perpetualMarket?.configs?.stepSize ?: 0.001
        val equity = subaccount?.calculated?.get(CalculationPeriod.current)?.equity ?: return null
        val oraclePrice = market?.perpetualMarket?.oraclePrice ?: return null
        val feeRate = user?.takerFeeRate ?: Numeric.double.ZERO
        val positions = subaccount.openPositions
        val marketId = market.perpetualMarket?.id
        val positionSize = if (positions != null && marketId != null) {
            positions[marketId]?.calculated?.get(CalculationPeriod.current)?.size
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
                val orderbook = getOrderbook(market, calculatedIsBuying)
                if (orderbook != null) {
                    createMarketOrderFromLeverageWith(
                        equity = equity,
                        oraclePrice = oraclePrice,
                        positionSize = positionSize,
                        isBuying = calculatedIsBuying,
                        feeRate = feeRate,
                        leverage = leverage,
                        stepSize = stepSize,
                        orderbook = orderbook,
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

    private fun createMarketOrderFromLeverageWith(
        equity: Double,
        oraclePrice: Double,
        positionSize: Double?,
        isBuying: Boolean,
        feeRate: Double,
        leverage: Double,
        stepSize: Double,
        orderbook: List<InternalOrderbookTick>,
    ): TradeInputMarketOrder? {
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
        val marketOrderOrderBook = mutableListOf<InternalOrderbookTick>()

        /*
        Breaking naming rules a little bit to match the documentation above
         */
        @Suppress("LocalVariableName")
        val OR = oraclePrice

        @Suppress("LocalVariableName")
        val LV = leverage

        @Suppress("LocalVariableName")
        val OS: Double =
            if (isBuying) Numeric.double.POSITIVE else Numeric.double.NEGATIVE

        @Suppress("LocalVariableName")
        val FR = feeRate

        @Suppress("LocalVariableName")
        var AE = equity

        @Suppress("LocalVariableName")
        var SZ = positionSize ?: Numeric.double.ZERO

        orderbookLoop@ for (element in orderbook) {

            val entryPrice = element.price
            val entrySize = element.size
            if (entryPrice != Numeric.double.ZERO) {

                val MP = entryPrice

                @Suppress("LocalVariableName")
                val X = ((LV * AE) - (SZ * OR)) /
                        (OR + (OS * LV * MP * FR) - (LV * (OR - MP)))
                val desiredSize = X.abs()
                if (desiredSize < entrySize) {
                    val rounded = this.rounded(sizeTotal, desiredSize, stepSize)
                    sizeTotal = Rounder.quickRound(sizeTotal + rounded, stepSize)
                    usdcSizeTotal += rounded * MP
                    worstPrice = entryPrice
                    filled = true
                    marketOrderOrderBook.add(matchingOrderbookEntry(element, rounded))
                } else {
                    val rounded = this.rounded(sizeTotal, entrySize, stepSize)
                    sizeTotal = Rounder.quickRound(sizeTotal + rounded, stepSize)
                    usdcSizeTotal += rounded * MP
                    /*
                    new(AE) = AE + X * (OR - MP) - abs(X) * MP * FR
                     */
                    var signedSize = rounded
                    if (!isBuying) {
                        signedSize *= exchange.dydx.abacus.utils.Numeric.double.NEGATIVE
                    }
                    AE = AE + (signedSize * (OR - MP)) - (rounded * MP * FR)
                    SZ += signedSize
                    marketOrderOrderBook.add(matchingOrderbookEntry(element, rounded))
                }
            }

            if (filled) {
                break@orderbookLoop
            }
        }
        return createMarketOrderWith(
            orderbook = marketOrderOrderBook,
            size = sizeTotal,
            usdcSize = usdcSizeTotal,
            worstPrice = worstPrice,
            filled = filled,
        )
    }

    private fun createMarketOrderWith(
        orderbook: List<InternalOrderbookTick>,
        size: Double?,
        usdcSize: Double?,
        worstPrice: Double?,
        filled: Boolean,
    ): TradeInputMarketOrder? {
        return if (size != null && usdcSize != null && size != Numeric.double.ZERO) {
            TradeInputMarketOrder(
                orderbook = orderbook.map {
                    OrderbookUsage(
                        price = it.price,
                        size = it.size,
                    )
                }.toIList(),
                price = (usdcSize / size),
                size = size,
                usdcSize = usdcSize,
                worstPrice = worstPrice,
                filled = filled,
            )
        } else {
            null
        }
    }

    private fun matchingOrderbookEntry(
        entry: InternalOrderbookTick,
        size: Double,
    ): InternalOrderbookTick {
        return entry.copy(size = size)
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

}