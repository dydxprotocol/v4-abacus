package exchange.dydx.abacus.calculator.v2.tradeinput

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.calculator.SlippageConstants.MAJOR_MARKETS
import exchange.dydx.abacus.calculator.SlippageConstants.MARKET_ORDER_MAX_SLIPPAGE
import exchange.dydx.abacus.calculator.SlippageConstants.SLIPPAGE_STEP_SIZE
import exchange.dydx.abacus.calculator.SlippageConstants.STOP_MARKET_ORDER_SLIPPAGE_BUFFER
import exchange.dydx.abacus.calculator.SlippageConstants.STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
import exchange.dydx.abacus.calculator.SlippageConstants.TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER
import exchange.dydx.abacus.calculator.SlippageConstants.TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalRewardsParamsState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputSummary
import exchange.dydx.abacus.state.internalstate.InternalUserState
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
import exchange.dydx.abacus.utils.Rounder
import kotlin.math.abs
import kotlin.math.pow

internal class TradeInputSummaryCalculator {
    fun calculate(
        trade: InternalTradeInputState,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        market: InternalMarketState?,
        rewardsParams: InternalRewardsParamsState?,
        feeTiers: List<FeeTier>?,
    ): InternalTradeInputState {
        trade.summary = when (trade.type) {
            OrderType.Market -> {
                calculateForMarketOrder(trade, subaccount, user, market, rewardsParams, feeTiers)
            }

            OrderType.StopMarket, OrderType.TakeProfitMarket -> {
                calculateForStopTakeProfitMarketOrder(
                    trade,
                    subaccount,
                    user,
                    market,
                    rewardsParams,
                    feeTiers,
                )
            }

            OrderType.Limit, OrderType.StopLimit, OrderType.TakeProfitLimit -> {
                calculateForLimitOrder(trade, subaccount, user, market, rewardsParams, feeTiers)
            }

            else -> null
        }

        return trade
    }

    private fun calculateForMarketOrder(
        trade: InternalTradeInputState,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        market: InternalMarketState?,
        rewardsParams: InternalRewardsParamsState?,
        feeTiers: List<FeeTier>?,
    ): InternalTradeInputSummary? {
        val marketOrder = trade.marketOrder ?: return null
        val multiplier = getMultiplier(trade)

        val feeRate = user?.takerFeeRate
        val midMarketPrice = marketOrderbookMidPrice(market)
        val worstPrice = marketOrder.worstPrice
        val slippageFromMidPrice = marketOrderSlippageFromMidPrice(worstPrice, midMarketPrice)
        val price = marketOrder.price
        val side = trade.side
        val payloadPrice = if (price != null) {
            when (side) {
                OrderSide.Buy -> price * (Numeric.double.ONE + MARKET_ORDER_MAX_SLIPPAGE)

                else -> price * (Numeric.double.ONE - MARKET_ORDER_MAX_SLIPPAGE)
            }
        } else {
            null
        }

        val size = marketOrder.size
        val usdcSize =
            if (price != null && size != null) (price * size) else null
        val fee =
            if (usdcSize != null && feeRate != null) (usdcSize * feeRate) else null
        val total =
            if (usdcSize != null) {
                usdcSize * multiplier + (fee ?: Numeric.double.ZERO) * Numeric.double.NEGATIVE
            } else {
                null
            }

        val oraclePrice = market?.perpetualMarket?.oraclePrice
        val priceDiff = slippage(
            price = worstPrice,
            oraclePrice = oraclePrice,
            side = side,
        )
        val indexSlippage =
            if (priceDiff != null && oraclePrice != null && oraclePrice > Numeric.double.ZERO) {
                Rounder.quickRound(
                    number = priceDiff / oraclePrice,
                    stepSize = SLIPPAGE_STEP_SIZE,
                )
            } else {
                null
            }
        /*
            indexSlippage can be negative. For example, it is OK to buy below index price
         */
        val reward = calculateTakerReward(
            usdcSize = usdcSize,
            fee = fee,
            rewardsParams = rewardsParams,
            feeTiers = feeTiers,
        )

        return InternalTradeInputSummary(
            price = price,
            payloadPrice = payloadPrice,
            size = size,
            usdcSize = usdcSize,
            slippage = slippageFromMidPrice,
            fee = fee,
            total = if (total == Numeric.double.ZERO) Numeric.double.ZERO else total,
            reward = reward,
            filled = marketOrder.filled,
            positionMargin = calculatePositionMargin(trade, subaccount, market),
            positionLeverage = getPositionLeverage(subaccount, market),
            feeRate = feeRate,
            indexSlippage = indexSlippage,
        )
    }

    private fun calculateForStopTakeProfitMarketOrder(
        trade: InternalTradeInputState,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        market: InternalMarketState?,
        rewardsParams: InternalRewardsParamsState?,
        feeTiers: List<FeeTier>?,
    ): InternalTradeInputSummary? {
        val marketOrder = trade.marketOrder ?: return null
        val multiplier = getMultiplier(trade)

        val feeRate = user?.takerFeeRate
        val midMarketPrice = marketOrderbookMidPrice(market)
        val worstPrice = marketOrder.worstPrice
        val slippageFromMidPrice = marketOrderSlippageFromMidPrice(worstPrice, midMarketPrice)

        val triggerPrice = trade.price?.triggerPrice
        val marketOrderPrice = marketOrder.price
        val slippagePercentage =
            if (midMarketPrice != null && marketOrderPrice != null && midMarketPrice > Numeric.double.ZERO) {
                abs((marketOrderPrice - midMarketPrice) / midMarketPrice)
            } else {
                null
            }

        val marketId = trade.marketId
        val adjustedslippagePercentage = if (slippagePercentage != null) {
            val majorMarket = MAJOR_MARKETS.contains(marketId)
            if (majorMarket) {
                if (trade.type == OrderType.StopMarket) {
                    slippagePercentage + STOP_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                } else {
                    slippagePercentage + TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER_MAJOR_MARKET
                }
            } else {
                if (trade.type == OrderType.StopMarket) {
                    slippagePercentage + STOP_MARKET_ORDER_SLIPPAGE_BUFFER
                } else {
                    slippagePercentage + TAKE_PROFIT_MARKET_ORDER_SLIPPAGE_BUFFER
                }
            }
        } else {
            null
        }

        val price = if (triggerPrice != null && slippageFromMidPrice != null) {
            if (trade.side == OrderSide.Buy) {
                triggerPrice * (Numeric.double.ONE + slippageFromMidPrice)
            } else {
                triggerPrice * (Numeric.double.ONE - slippageFromMidPrice)
            }
        } else {
            null
        }

        val payloadPrice =
            if (triggerPrice != null && adjustedslippagePercentage != null) {
                if (trade.side == OrderSide.Buy) {
                    triggerPrice * (Numeric.double.ONE + adjustedslippagePercentage)
                } else {
                    triggerPrice * (Numeric.double.ONE - adjustedslippagePercentage)
                }
            } else {
                null
            }

        val size = marketOrder.size
        val usdcSize =
            if (price != null && size != null) (price * size) else null
        val fee =
            if (usdcSize != null && feeRate != null) (usdcSize * feeRate) else null
        val total =
            if (usdcSize != null) {
                usdcSize * multiplier + (fee ?: Numeric.double.ZERO) * Numeric.double.NEGATIVE
            } else {
                null
            }

        val reward = calculateTakerReward(
            usdcSize = usdcSize,
            fee = fee,
            rewardsParams = rewardsParams,
            feeTiers = feeTiers,
        )

        return InternalTradeInputSummary(
            price = price,
            payloadPrice = payloadPrice,
            size = size,
            usdcSize = usdcSize,
            slippage = slippageFromMidPrice,
            fee = fee,
            total = if (total == Numeric.double.ZERO) Numeric.double.ZERO else total,
            reward = reward,
            filled = marketOrder.filled,
            positionMargin = calculatePositionMargin(trade, subaccount, market),
            positionLeverage = getPositionLeverage(subaccount, market),
            feeRate = feeRate,
            indexSlippage = null,
        )
    }

    private fun calculateForLimitOrder(
        trade: InternalTradeInputState,
        subaccount: InternalSubaccountState?,
        user: InternalUserState?,
        market: InternalMarketState?,
        rewardsParams: InternalRewardsParamsState?,
        feeTiers: List<FeeTier>?,
    ): InternalTradeInputSummary {
        val multiplier = getMultiplier(trade)

        val timeInForce = trade.timeInForce
        val execution = trade.execution
        val isMaker =
            (trade.type == OrderType.Limit && timeInForce == "GTT") || execution == "POST_ONLY"

        val feeRate = if (isMaker) user?.makerFeeRate else user?.takerFeeRate

        val price = trade.price?.limitPrice
        val size = trade.size?.size
        val usdcSize =
            if (price != null && size != null) (price * size) else null
        val fee =
            if (usdcSize != null && feeRate != null) (usdcSize * feeRate) else null
        val total =
            if (usdcSize != null) {
                usdcSize * multiplier + (fee ?: Numeric.double.ZERO) * Numeric.double.NEGATIVE
            } else {
                null
            }

        val reward =
            if (isMaker) {
                calculateMakerReward(
                    fee = fee,
                    rewardsParams = rewardsParams,
                )
            } else {
                calculateTakerReward(
                    usdcSize = usdcSize,
                    fee = fee,
                    rewardsParams = rewardsParams,
                    feeTiers = feeTiers,
                )
            }

        return InternalTradeInputSummary(
            price = price,
            payloadPrice = price,
            size = size,
            usdcSize = usdcSize,
            slippage = null,
            fee = fee,
            total = if (total == Numeric.double.ZERO) Numeric.double.ZERO else total,
            reward = reward,
            filled = true,
            positionMargin = calculatePositionMargin(trade, subaccount, market),
            positionLeverage = getPositionLeverage(subaccount, market),
            feeRate = feeRate,
            indexSlippage = null,
        )
    }

    private fun marketOrderbookMidPrice(market: InternalMarketState?): Double? {
        val orderbook = market?.consolidatedOrderbook
        val firstAskPrice = orderbook?.asks?.firstOrNull()?.price
        val firstBidPrice = orderbook?.bids?.firstOrNull()?.price
        return if (firstAskPrice != null && firstBidPrice != null) {
            (firstAskPrice + firstBidPrice) / 2.0
        } else {
            null
        }
    }

    private fun marketOrderSlippageFromMidPrice(
        worstPrice: Double?,
        midMarketPrice: Double?
    ): Double? {
        return if (worstPrice != null && midMarketPrice != null && midMarketPrice > Numeric.double.ZERO) {
            Rounder.round(
                number = (worstPrice - midMarketPrice).abs() / midMarketPrice,
                stepSize = SLIPPAGE_STEP_SIZE,
            )
        } else {
            null
        }
    }

    private fun slippage(price: Double?, oraclePrice: Double?, side: OrderSide?): Double? {
        return if (price != null && oraclePrice != null) {
            if (side == OrderSide.Buy) price - oraclePrice else oraclePrice - price
        } else {
            null
        }
    }

    private fun getMultiplier(trade: InternalTradeInputState): Double {
        return if (trade.side == OrderSide.Sell) Numeric.double.POSITIVE else Numeric.double.NEGATIVE
    }

    private fun calculateTakerReward(
        usdcSize: Double?,
        fee: Double?,
        rewardsParams: InternalRewardsParamsState?,
        feeTiers: List<FeeTier>?,
    ): Double? {
        val feeMultiplierPpm = rewardsParams?.feeMultiplierPpm
        val tokenPrice = rewardsParams?.tokenPrice
        val tokenPriceExponent = rewardsParams?.tokenExpoonent
        val notional = usdcSize
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
                tokenPrice * 10.0.pow(tokenPriceExponent)
                )
        }
        return null
    }

    private fun calculateMakerReward(
        fee: Double?,
        rewardsParams: InternalRewardsParamsState?
    ): Double? {
        val feeMultiplierPpm = rewardsParams?.feeMultiplierPpm
        val tokenPrice = rewardsParams?.tokenPrice
        val tokenPriceExponent = rewardsParams?.tokenExpoonent

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

    private fun findMaxMakerRebate(feeTiers: List<FeeTier>?): Double {
        if (feeTiers.isNullOrEmpty()) return 0.0

        val smallestNegative = feeTiers.map { it.maker ?: 0.0 }
            .filter { it < 0.0 }
            .minOrNull()

        return abs(smallestNegative ?: 0.0)
    }

    /**
     * Calculate the current and postOrder position margin to be displayed in the TradeInput Summary.
     */
    private fun calculatePositionMargin(
        trade: InternalTradeInputState,
        subaccount: InternalSubaccountState?,
        market: InternalMarketState?,
    ): Double? {
        if (subaccount == null || market == null) {
            return null
        }

        val marginMode = trade.marginMode
        val marketId = market.perpetualMarket?.id ?: return null
        val position = subaccount.openPositions?.get(marketId)

        if (position != null) {
            when (marginMode) {
                MarginMode.Isolated -> {
                    val currentEquity = subaccount.calculated[CalculationPeriod.current]?.equity
                    val postOrderEquity = subaccount.calculated[CalculationPeriod.post]?.equity
                    if (currentEquity != null) {
                        if (postOrderEquity != null) {
                            return postOrderEquity
                        }
                        return currentEquity
                    }
                }

                MarginMode.Cross -> {
                    val currentNotionalTotal =
                        position.calculated[CalculationPeriod.current]?.notionalTotal
                    val postOrderNotionalTotal =
                        position.calculated[CalculationPeriod.post]?.notionalTotal
                    val mmf = market.perpetualMarket?.configs?.maintenanceMarginFraction
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
        subaccount: InternalSubaccountState?,
        market: InternalMarketState?,
    ): Double? {
        if (subaccount == null || market == null) return null

        if (!subaccount.isParentSubaccount) {
            val currentLeverage = subaccount.calculated[CalculationPeriod.current]?.leverage
            val postOrderLeverage = subaccount.calculated[CalculationPeriod.post]?.leverage
            return postOrderLeverage ?: currentLeverage
        }

        val marketId = market.perpetualMarket?.id
        val position = subaccount.openPositions?.get(marketId) ?: return null

        val currentLeverage = position.calculated[CalculationPeriod.current]?.leverage
        val postOrderLeverage = position.calculated[CalculationPeriod.post]?.leverage
        return postOrderLeverage ?: currentLeverage
    }
}
