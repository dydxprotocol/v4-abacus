@file:Suppress("ktlint:standard:property-naming")

package exchange.dydx.abacus.calculator.v2.tradeinput

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderbookUsage
import exchange.dydx.abacus.output.input.TradeInputMarketOrder
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalOrderbook
import exchange.dydx.abacus.state.internalstate.InternalOrderbookTick
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.InternalUserState
import exchange.dydx.abacus.state.internalstate.safeCreate
import exchange.dydx.abacus.utils.MAX_FREE_COLLATERAL_BUFFER_PERCENT
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.Rounder
import kollections.toIList
import kotlin.math.max
import kotlin.math.min

internal class TradeInputMarketOrderCalculator() {
    fun calculate(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        input: String?,
    ): InternalTradeInputState {
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
                tradeSize = tradeSize.copy(
                    usdcSize = if (filled) marketOrder?.usdcSize else null,
                    balancePercent = if (filled) marketOrder?.balancePercent else null,
                )

            "size.usdcSize" ->
                tradeSize = tradeSize.copy(
                    size = if (filled) marketOrder?.size else null,
                    balancePercent = if (filled) marketOrder?.balancePercent else null,
                )

            "size.leverage" -> {
                tradeSize = tradeSize.copy(
                    size = if (filled) marketOrder?.size else null,
                    usdcSize = if (filled) marketOrder?.usdcSize else null,
                    balancePercent = if (filled) marketOrder?.balancePercent else null,
                )
                val orderbook = market?.consolidatedOrderbook
                if (marketOrder != null && orderbook != null) {
                    val side = calculateSide(marketOrder, orderbook)
                    if (side != null && side != trade.side) {
                        trade.side = side
                    }
                }
            }

            "size.balancePercent" -> {
                tradeSize = tradeSize.copy(
                    size = if (filled) marketOrder?.size else null,
                    usdcSize = if (filled) marketOrder?.usdcSize else null,
                )
            }
        }

        trade.marketOrder = marketOrder
        trade.size = tradeSize

        return trade
    }

    private fun calculateSide(
        marketOrder: TradeInputMarketOrder,
        orderbook: InternalOrderbook,
    ): OrderSide? {
        val firstMarketOrderbookPrice = marketOrder.orderbook?.firstOrNull()?.price ?: return null
        val firstAskPrice = orderbook.asks?.firstOrNull()?.price ?: return null
        val firstBidPrice = orderbook.bids?.firstOrNull()?.price ?: return null
        return if (firstMarketOrderbookPrice == firstAskPrice) {
            OrderSide.Buy
        } else if (firstMarketOrderbookPrice == firstBidPrice) {
            OrderSide.Sell
        } else {
            null
        }
    }

    private fun createMarketOrder(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        input: String?,
    ): TradeInputMarketOrder? {
        val tradeSide = trade.side
        val tradeSize = trade.size
        val freeCollateral = subaccount?.calculated?.get(CalculationPeriod.current)?.freeCollateral

        if (tradeSize != null && tradeSide != null && freeCollateral != null && freeCollateral > Numeric.double.ZERO) {
            val maxMarketLeverage = market?.perpetualMarket?.configs?.maxMarketLeverage ?: Numeric.double.ONE
            val targetLeverage = trade.targetLeverage
            val marginMode = trade.marginMode ?: MarginMode.Cross
            val tradeLeverage = if (marginMode == MarginMode.Isolated && targetLeverage != null && targetLeverage > Numeric.double.ZERO) {
                targetLeverage
            } else {
                maxMarketLeverage
            }

            val positions = subaccount.openPositions
            val marketId = market?.perpetualMarket?.id
            val positionNotionalSize = if (positions != null && marketId != null) {
                positions[marketId]?.calculated?.get(CalculationPeriod.current)?.notionalTotal ?: Numeric.double.ZERO
            } else {
                Numeric.double.ZERO
            }
            val positionSize = if (positions != null && marketId != null) {
                positions[marketId]?.calculated?.get(CalculationPeriod.current)?.size ?: Numeric.double.ZERO
            } else {
                Numeric.double.ZERO
            }
            val isTradeSameSide = ((tradeSide == OrderSide.Buy && positionSize >= Numeric.double.ZERO) || (tradeSide == OrderSide.Sell && positionSize <= Numeric.double.ZERO))

            return when (input) {
                "size.size", "size.percent" -> {
                    val orderbook = getOrderbook(market = market, isBuying = trade.isBuying)
                    createMarketOrderFromSize(
                        size = tradeSize.size,
                        existingPositionNotionalSize = positionNotionalSize,
                        isTradeSameSide = isTradeSameSide,
                        freeCollateral = freeCollateral,
                        tradeLeverage = tradeLeverage,
                        orderbook = orderbook,
                    )
                }

                "size.usdcSize" -> {
                    val stepSize = market?.perpetualMarket?.configs?.stepSize ?: 0.001
                    val orderbook = getOrderbook(market = market, isBuying = trade.isBuying)
                    createMarketOrderFromUsdcSize(
                        usdcSize = tradeSize.usdcSize,
                        existingPositionNotionalSize = positionNotionalSize,
                        isTradeSameSide = isTradeSameSide,
                        freeCollateral = freeCollateral,
                        tradeLeverage = tradeLeverage,
                        orderbook = orderbook,
                        stepSize = stepSize,
                    )
                }

                "size.leverage" -> {
                    val leverage = tradeSize.leverage ?: return null
                    createMarketOrderFromLeverage(
                        leverage = leverage,
                        existingPositionNotionalSize = positionNotionalSize,
                        existingPositionSize = positionSize,
                        isTradeSameSide = isTradeSameSide,
                        market = market,
                        freeCollateral = freeCollateral,
                        tradeLeverage = tradeLeverage,
                        subaccount = subaccount,
                        user = user,
                    )
                }

                "size.balancePercent" -> {
                    val stepSize = market?.perpetualMarket?.configs?.stepSize ?: 0.001
                    val orderbook = getOrderbook(market = market, isBuying = trade.isBuying)
                    val balancePercent = tradeSize.balancePercent ?: return null
                    val oraclePrice = market?.perpetualMarket?.oraclePrice
                    val isReduceOnly = trade.reduceOnly

                    createMarketOrderFromBalancePercent(
                        balancePercent = balancePercent,
                        existingPositionNotionalSize = positionNotionalSize,
                        existingPositionSize = positionSize,
                        isTradeSameSide = isTradeSameSide,
                        marginMode = marginMode,
                        freeCollateral = freeCollateral,
                        tradeLeverage = tradeLeverage,
                        orderbook = orderbook,
                        stepSize = stepSize,
                        oraclePrice = oraclePrice,
                        isReduceOnly = isReduceOnly,
                        tradeSide = tradeSide,
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

    private fun isolatedPnlImpactForBalance(
        marginMode: MarginMode,
        tradeSide: OrderSide,
        desiredBalance: Double,
        tradeLeverage: Double,
        entryPrice: Double,
        oraclePrice: Double?,
        isReduceOnly: Boolean
    ): Double {
        // Calculates the pnl impact for an isolated order trade, given:
        // - the difference between the oracle price and the ask/bid price
        // - a total balance to be used for the trade, note this balance should also be used for the pnl impact
        // TODO CT-1192: refactor to call into MarginCalculator.getShouldTransferInCollateralDeprecated and MarginCalculator.getTransferAmountFromTargetLeverage

        // Formula Derivation:
        // pnlImpact = diff * size
        // size = balance * tradeLeverage / entryPrice
        // pnlImpact = diff * (balance - pnlImpact) * tradeLeverage / entryPrice
        // pnlImpact = (diff * balance - diff * pnlImpact) * tradeLeverage / entryPrice
        // pnlImpact * (entryPrice + diff * tradeLeverage) = diff * balance * tradeLeverage
        // pnlImpact = (diff * balance * tradeLeverage) / (entryPrice + diff * tradeLeverage)

        return when (marginMode) {
            MarginMode.Cross -> Numeric.double.ZERO
            MarginMode.Isolated -> if (isReduceOnly) {
                Numeric.double.ZERO
            } else {
                val diff = when (tradeSide) {
                    OrderSide.Buy -> entryPrice - (oraclePrice ?: entryPrice)
                    OrderSide.Sell -> (oraclePrice ?: entryPrice) - entryPrice
                }
                val pnlImpact = if ((entryPrice + diff * tradeLeverage) > Numeric.double.ZERO) (diff * desiredBalance * tradeLeverage) / (entryPrice + diff * tradeLeverage) else Numeric.double.ZERO
                max(pnlImpact, Numeric.double.ZERO)
            }
        }
    }

    private fun createMarketOrderFromBalancePercent(
        balancePercent: Double,
        existingPositionNotionalSize: Double,
        existingPositionSize: Double,
        isTradeSameSide: Boolean,
        marginMode: MarginMode,
        freeCollateral: Double,
        tradeLeverage: Double,
        orderbook: List<InternalOrderbookTick>?,
        stepSize: Double,
        oraclePrice: Double?,
        isReduceOnly: Boolean,
        tradeSide: OrderSide,
    ): TradeInputMarketOrder? {
        if (marginMode == MarginMode.Isolated && !isTradeSameSide) {
            // For isolated margin orders where the user is trading on the opposite side of their currentPosition, the balancePercent represents a percentage of their current position rather than freeCollateral
            val desiredSize = existingPositionSize.abs() * balancePercent
            return createMarketOrderFromSize(size = desiredSize, existingPositionNotionalSize = existingPositionNotionalSize, isTradeSameSide = isTradeSameSide, freeCollateral = freeCollateral, tradeLeverage = tradeLeverage, orderbook = orderbook)
        }

        val cappedPercent = min(balancePercent, MAX_FREE_COLLATERAL_BUFFER_PERCENT)

        val existingBalance = existingPositionNotionalSize.abs() / tradeLeverage

        val desiredBalance = when (marginMode) {
            MarginMode.Cross -> {
                if (isTradeSameSide) cappedPercent * freeCollateral else cappedPercent * (existingBalance + freeCollateral) + existingBalance
            }
            MarginMode.Isolated -> {
                cappedPercent * freeCollateral
            }
        }

        return if (desiredBalance != Numeric.double.ZERO) {
            if (orderbook != null) {
                var sizeTotal = Numeric.double.ZERO
                var usdcSizeTotal = Numeric.double.ZERO
                var balanceTotal = Numeric.double.ZERO
                var worstPrice: Double? = null
                var filled = false
                val marketOrderOrderBook = mutableListOf<InternalOrderbookTick>()

                orderbookLoop@ for (element in orderbook) {
                    val entryPrice = element.price
                    val entrySize = element.size

                    if (entryPrice > Numeric.double.ZERO) {
                        val entryUsdcSize = entrySize * entryPrice
                        val entryBalanceSize = entryUsdcSize / tradeLeverage
                        val pnlImpact = isolatedPnlImpactForBalance(
                            marginMode = marginMode,
                            tradeSide = tradeSide,
                            desiredBalance = desiredBalance,
                            tradeLeverage = tradeLeverage,
                            entryPrice = entryPrice,
                            oraclePrice = oraclePrice,
                            isReduceOnly = isReduceOnly,
                        )
                        filled = (balanceTotal + entryBalanceSize + pnlImpact) >= desiredBalance

                        var matchedSize = entrySize
                        var matchedUsdcSize = entryUsdcSize
                        var matchedBalance = matchedUsdcSize / tradeLeverage

                        if (filled) {
                            matchedBalance = desiredBalance - balanceTotal - pnlImpact
                            matchedUsdcSize = matchedBalance * tradeLeverage
                            matchedSize = matchedUsdcSize / entryPrice
                            // Round the size to appropriate step size for market and recalculate
                            matchedSize =
                                Rounder.quickRound(
                                    matchedSize,
                                    stepSize,
                                )
                            matchedUsdcSize = matchedSize * entryPrice
                            matchedBalance = matchedUsdcSize / tradeLeverage + pnlImpact
                        }
                        sizeTotal += matchedSize
                        usdcSizeTotal += matchedUsdcSize
                        balanceTotal += matchedBalance

                        worstPrice = entryPrice
                        marketOrderOrderBook.add(matchingOrderbookEntry(element, matchedSize))
                        if (filled) {
                            break@orderbookLoop
                        }
                    }
                }
                val balancePercentTotal = balanceTotal / freeCollateral
                createMarketOrderWith(
                    orderbook = marketOrderOrderBook,
                    size = sizeTotal,
                    usdcSize = usdcSizeTotal,
                    balancePercent = balancePercentTotal,
                    worstPrice = worstPrice,
                    filled = filled,
                )
            } else {
                createMarketOrderWith(
                    orderbook = listOf<InternalOrderbookTick>(),
                    size = Numeric.double.ZERO,
                    usdcSize = Numeric.double.ZERO,
                    balancePercent = balancePercent,
                    worstPrice = null,
                    filled = false,
                )
            }
        } else {
            null
        }
    }

    private fun createMarketOrderFromSize(
        size: Double?,
        existingPositionNotionalSize: Double,
        isTradeSameSide: Boolean,
        freeCollateral: Double,
        tradeLeverage: Double,
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
                val balancePercentTotal = calculateBalancePercentFromUsdcSize(usdcSize = usdcSizeTotal, freeCollateral = freeCollateral, positionSize = existingPositionNotionalSize, tradeLeverage = tradeLeverage, isTradeSameSide = isTradeSameSide)
                createMarketOrderWith(
                    orderbook = marketOrderOrderBook,
                    size = sizeTotal,
                    usdcSize = usdcSizeTotal,
                    balancePercent = balancePercentTotal,
                    worstPrice = worstPrice,
                    filled = filled,
                )
            } else {
                createMarketOrderWith(
                    orderbook = listOf<InternalOrderbookTick>(),
                    size = size,
                    usdcSize = Numeric.double.ZERO,
                    balancePercent = Numeric.double.ZERO,
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
        existingPositionNotionalSize: Double,
        isTradeSameSide: Boolean,
        freeCollateral: Double,
        tradeLeverage: Double,
        orderbook: List<InternalOrderbookTick>?,
        stepSize: Double,
    ): TradeInputMarketOrder? {
        return if (usdcSize != null && usdcSize != Numeric.double.ZERO) {
            if (orderbook != null) {
                val desiredUsdcSize = usdcSize
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
                val balancePercentTotal = calculateBalancePercentFromUsdcSize(usdcSize = usdcSizeTotal, freeCollateral = freeCollateral, positionSize = existingPositionNotionalSize, tradeLeverage = tradeLeverage, isTradeSameSide = isTradeSameSide)
                createMarketOrderWith(
                    orderbook = marketOrderOrderBook,
                    size = sizeTotal,
                    usdcSize = usdcSizeTotal,
                    balancePercent = balancePercentTotal,
                    worstPrice = worstPrice,
                    filled = filled,
                )
            } else {
                createMarketOrderWith(
                    orderbook = listOf<InternalOrderbookTick>(),
                    size = Numeric.double.ZERO,
                    usdcSize = usdcSize,
                    balancePercent = Numeric.double.ZERO,
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
        existingPositionNotionalSize: Double,
        existingPositionSize: Double,
        isTradeSameSide: Boolean,
        market: InternalMarketState?,
        freeCollateral: Double,
        tradeLeverage: Double,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
    ): TradeInputMarketOrder? {
        val stepSize = market?.perpetualMarket?.configs?.stepSize ?: 0.001
        val equity = subaccount?.calculated?.get(CalculationPeriod.current)?.equity ?: return null
        val oraclePrice = market?.perpetualMarket?.oraclePrice ?: return null
        val feeRate = user?.takerFeeRate ?: Numeric.double.ZERO
        return if (equity > Numeric.double.ZERO) {
            val existingLeverage =
                (existingPositionSize * oraclePrice) / equity
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
                        positionSize = existingPositionSize,
                        positionSizeNotional = existingPositionNotionalSize,
                        isBuying = calculatedIsBuying,
                        feeRate = feeRate,
                        leverage = leverage,
                        stepSize = stepSize,
                        isTradeSameSide = isTradeSameSide,
                        freeCollateral = freeCollateral,
                        tradeLeverage = tradeLeverage,
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
        positionSize: Double,
        positionSizeNotional: Double,
        isBuying: Boolean,
        feeRate: Double,
        leverage: Double,
        stepSize: Double,
        isTradeSameSide: Boolean,
        freeCollateral: Double,
        tradeLeverage: Double,
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
        @Suppress("LocalVariableName", "PropertyName", "VariableNaming")
        val _OR = oraclePrice

        @Suppress("LocalVariableName", "PropertyName", "VariableNaming")
        val LV = leverage

        @Suppress("LocalVariableName", "PropertyName", "VariableNaming")
        val OS: Double =
            if (isBuying) Numeric.double.POSITIVE else Numeric.double.NEGATIVE

        @Suppress("LocalVariableName", "PropertyName", "VariableNaming")
        val FR = feeRate

        @Suppress("LocalVariableName", "PropertyName", "VariableNaming")
        var AE = equity

        @Suppress("LocalVariableName", "PropertyName", "VariableNaming")
        var SZ = positionSize

        orderbookLoop@ for (element in orderbook) {
            val entryPrice = element.price
            val entrySize = element.size
            if (entryPrice != Numeric.double.ZERO) {
                @Suppress("LocalVariableName", "PropertyName", "VariableNaming")
                val MP = entryPrice

                @Suppress("LocalVariableName", "PropertyName", "VariableNaming")
                val X = ((LV * AE) - (SZ * _OR)) /
                    (_OR + (OS * LV * MP * FR) - (LV * (_OR - MP)))
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
                    AE = AE + (signedSize * (_OR - MP)) - (rounded * MP * FR)
                    SZ += signedSize
                    marketOrderOrderBook.add(matchingOrderbookEntry(element, rounded))
                }
            }

            if (filled) {
                break@orderbookLoop
            }
        }
        val balancePercentTotal = calculateBalancePercentFromUsdcSize(usdcSize = usdcSizeTotal, freeCollateral = freeCollateral, positionSize = positionSizeNotional, tradeLeverage = tradeLeverage, isTradeSameSide = isTradeSameSide)
        return createMarketOrderWith(
            orderbook = marketOrderOrderBook,
            size = sizeTotal,
            usdcSize = usdcSizeTotal,
            balancePercent = balancePercentTotal,
            worstPrice = worstPrice,
            filled = filled,
        )
    }

    private fun calculateBalancePercentFromUsdcSize(
        usdcSize: Double,
        freeCollateral: Double,
        positionSize: Double,
        tradeLeverage: Double,
        isTradeSameSide: Boolean,
    ): Double {
        if (freeCollateral <= Numeric.double.ZERO || tradeLeverage <= Numeric.double.ZERO) {
            return Numeric.double.ZERO
        }
        return if (isTradeSameSide) {
            (usdcSize / tradeLeverage) / freeCollateral
        } else {
            val existingBalance = positionSize.abs() / tradeLeverage
            (usdcSize / tradeLeverage - existingBalance) / (freeCollateral + existingBalance)
        }
    }

    private fun createMarketOrderWith(
        orderbook: List<InternalOrderbookTick>,
        size: Double?,
        usdcSize: Double?,
        balancePercent: Double?,
        worstPrice: Double?,
        filled: Boolean,
    ): TradeInputMarketOrder? {
        return if (size != null && usdcSize != null) {
            TradeInputMarketOrder(
                orderbook = orderbook.map {
                    OrderbookUsage(
                        price = it.price,
                        size = it.size,
                    )
                }.toIList(),
                price = if (size != Numeric.double.ZERO) usdcSize / size else null,
                size = size,
                usdcSize = usdcSize,
                balancePercent = balancePercent,
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
