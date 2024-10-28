package exchange.dydx.abacus.calculator.v2

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.MarketConfigs
import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAssetPositionState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalPositionCalculated
import exchange.dydx.abacus.state.internalstate.InternalSubaccountCalculated
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.utils.Numeric
import indexer.codegen.IndexerPerpetualPositionStatus
import kotlin.math.max

internal class SubaccountCalculatorV2(
    val parser: ParserProtocol
) {
    fun calculate(
        subaccount: InternalSubaccountState?,
        configs: MarketConfigs?,
        marketsSummary: InternalMarketSummaryState,
        price: Map<String, Double>?,
        periods: Set<CalculationPeriod>,
    ): InternalSubaccountState? {
        if (subaccount == null) return null

        calculatePositionsValues(
            subaccount = subaccount,
            markets = marketsSummary.markets,
            price = price,
            periods = periods,
        )
        calculateSubaccountEquity(
            subaccount = subaccount,
            positions = subaccount.openPositions,
            periods = periods,
        )
        calculatePositionsLeverages(
            positions = subaccount.openPositions,
            markets = marketsSummary.markets,
            subaccount = subaccount,
            periods = periods,
        )
        calculateSubaccountBuyingPower(
            subaccount = subaccount,
            configs = configs,
            periods = periods,
        )
        return subaccount
    }

    fun calculateQuoteBalance(
        assetPositions: Map<String, InternalAssetPositionState>? = null,
    ): Double? {
        val usdc = assetPositions?.get("USDC")
        return if (usdc != null) {
            val size = usdc.size
            if (size != null) {
                val side = usdc.side
                if (side == PositionSide.LONG) size else size * -1.0
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun calculateSubaccountEquity(
        subaccount: InternalSubaccountState,
        positions: Map<String, InternalPerpetualPosition>?,
        periods: Set<CalculationPeriod>,
    ) {
        for (period in periods) {
            val calculated = subaccount.calculated[period] ?: InternalSubaccountCalculated()
            subaccount.calculated[period] = calculated

            var hasPositionCalculated = false
            for (position in positions?.values ?: emptyList()) {
                if (position.calculated[period] != null) {
                    hasPositionCalculated = true
                }
            }
            val positionsReady = positions.isNullOrEmpty() || hasPositionCalculated

            val quoteBalance = calculated.quoteBalance
            if (quoteBalance != null && positionsReady) {
                var notionalTotal = Numeric.double.ZERO
                var valueTotal = Numeric.double.ZERO
                var initialRiskTotal = Numeric.double.ZERO

                for (position in positions?.values ?: emptyList()) {
                    val positionCalculated = position.calculated[period]
                    notionalTotal += positionCalculated?.notionalTotal ?: Numeric.double.ZERO
                    valueTotal += positionCalculated?.valueTotal ?: Numeric.double.ZERO
                    initialRiskTotal += positionCalculated?.initialRiskTotal ?: Numeric.double.ZERO
                }

                calculated.notionalTotal = notionalTotal
                calculated.valueTotal = valueTotal
                calculated.initialRiskTotal = initialRiskTotal

                val equity = valueTotal + quoteBalance
                val freeCollateral = equity - initialRiskTotal

                calculated.equity = equity
                calculated.freeCollateral = freeCollateral

                if (equity > Numeric.double.ZERO) {
                    calculated.leverage = notionalTotal / equity
                    calculated.marginUsage = Numeric.double.ONE - freeCollateral / equity
                } else {
                    calculated.leverage = null
                    calculated.marginUsage = null
                }
            } else {
                calculated.notionalTotal = null
                calculated.valueTotal = null
                calculated.initialRiskTotal = null
                calculated.equity = null
                calculated.freeCollateral = null
                calculated.leverage = null
                calculated.marginUsage = null
            }
        }
    }

    private fun calculatePositionsLeverages(
        positions: Map<String, InternalPerpetualPosition>?,
        markets: Map<String, InternalMarketState>?,
        subaccount: InternalSubaccountState?,
        periods: Set<CalculationPeriod>,
    ) {
        if (positions.isNullOrEmpty()) {
            return
        }

        for (period in periods) {
            val subaccountCalculated = subaccount?.calculated?.get(period)
            val initialRiskTotal = subaccountCalculated?.initialRiskTotal
            val equity = subaccountCalculated?.equity
            for ((key, position) in positions) {
                val positionCalculated = position.calculated[period]

                positionCalculated?.leverage = calculatePositionLeverage(
                    equity = equity,
                    notionalValue = positionCalculated?.valueTotal,
                )

                positionCalculated?.liquidationPrice = calculatePositionLiquidationPrice(
                    equity = equity ?: Numeric.double.ZERO,
                    marketId = key,
                    positions = positions,
                    markets = markets,
                    period = period,
                )

                positionCalculated?.buyingPower = calculatePositionBuyingPower(
                    equity = equity,
                    initialRiskTotal = initialRiskTotal,
                    imf = positionCalculated?.adjustedImf,
                )
            }
        }
    }

    private fun calculateSubaccountBuyingPower(
        subaccount: InternalSubaccountState?,
        configs: MarketConfigs?,
        periods: Set<CalculationPeriod>,
    ) {
        for (period in periods) {
            val calculated = subaccount?.calculated?.get(period)
            val quoteBalance = calculated?.quoteBalance
            val equity = calculated?.equity
            val initialRiskTotal = calculated?.initialRiskTotal
            if (quoteBalance != null && equity != null && initialRiskTotal != null) {
                val imf = configs?.initialMarginFraction ?: 0.05

                calculated.buyingPower = calculateBuyingPower(
                    equity = equity,
                    initialRiskTotal = initialRiskTotal,
                    imf = imf,
                )
            } else {
                calculated?.buyingPower = null
            }
        }
    }

    private fun calculatePositionBuyingPower(
        equity: Double?,
        initialRiskTotal: Double?,
        imf: Double?,
    ): Double? {
        return if (equity != null && initialRiskTotal != null && imf != null) {
            calculateBuyingPower(
                equity = equity,
                initialRiskTotal = initialRiskTotal,
                imf = imf,
            )
        } else {
            null
        }
    }

    private fun calculateBuyingPower(
        equity: Double,
        initialRiskTotal: Double,
        imf: Double,
    ): Double {
        val buyingPowerFreeCollateral = equity - initialRiskTotal
        return buyingPowerFreeCollateral / (
            if (imf > Numeric.double.ZERO) {
                imf
            } else {
                0.05
            }
            )
    }

    private fun calculatePositionLeverage(
        equity: Double?,
        notionalValue: Double?,
    ): Double? {
        return if (equity != null && notionalValue != null && equity > Numeric.double.ZERO) {
            notionalValue / equity
        } else {
            null
        }
    }

    private fun calculatePositionLiquidationPrice(
        equity: Double,
        marketId: String,
        positions: Map<String, InternalPerpetualPosition>?,
        markets: Map<String, InternalMarketState>?,
        period: CalculationPeriod,
    ): Double? {
        val otherPositionsRisk =
            calculationOtherPositionsRisk(
                positions = positions,
                markets = markets,
                except = marketId,
                period = period,
            )

        val position = positions?.get(marketId) ?: return null
        val market = markets?.get(marketId) ?: return null
        val calculated = position.calculated[period]
        val maintenanceMarginFraction = calculated?.adjustedMmf ?: return null
        val oraclePrice = market.perpetualMarket?.oraclePrice ?: return null
        val size = calculated.size ?: return null

        /*
          const liquidationPrice =
            side === POSITION_SIDES.LONG
              ? otherPositionsRisk
                  .plus(sizeBN.times(oraclePrice))
                  .minus(accountEquity)
                  .div(sizeBN.minus(sizeBN.times(maintenanceMarginFraction)))
              : otherPositionsRisk
                  .plus(sizeBN.times(oraclePrice))
                  .minus(accountEquity)
                  .div(sizeBN.times(maintenanceMarginFraction).plus(sizeBN));
         */
        val denominator =
            if (size > Numeric.double.ZERO) (size - size * maintenanceMarginFraction) else (size + size * maintenanceMarginFraction)

        val liquidationPrice = if (denominator != Numeric.double.ZERO) {
            (otherPositionsRisk + size * oraclePrice - equity) / denominator
        } else {
            null
        }

        return liquidationPrice?.takeUnless { it < Numeric.double.ZERO }
    }

    private fun calculationOtherPositionsRisk(
        positions: Map<String, InternalPerpetualPosition>?,
        markets: Map<String, InternalMarketState>?,
        except: String,
        period: CalculationPeriod,
    ): Double {
        var risk = Numeric.double.ZERO
        for ((key, position) in positions ?: emptyMap()) {
            if (key != except) {
                risk += calculatePositionRisk(
                    position = position,
                    market = markets?.get(key),
                    period = period,
                )
            }
        }
        return risk
    }

    private fun calculatePositionRisk(
        position: InternalPerpetualPosition?,
        market: InternalMarketState?,
        period: CalculationPeriod,
    ): Double {
        val maintenanceMarginFraction = position?.calculated?.get(period)?.adjustedMmf
        val oraclePrice = market?.perpetualMarket?.oraclePrice
        val size = position?.calculated?.get(period)?.size

        return if (maintenanceMarginFraction != null && oraclePrice != null && size != null) {
            size.abs() * oraclePrice * maintenanceMarginFraction
        } else {
            Numeric.double.ZERO
        }
    }

    private fun calculatePositionsValues(
        subaccount: InternalSubaccountState,
        markets: Map<String, InternalMarketState>?,
        price: Map<String, Double>?,
        periods: Set<CalculationPeriod>,
    ): InternalSubaccountState {
        for ((key, position) in subaccount.openPositions ?: emptyMap()) {
            val market = markets?.get(key)
            if (market != null) {
                calculatePositionValues(
                    position = position,
                    market = market,
                    subaccount = subaccount,
                    price = parser.asDouble(price?.get(key)),
                    periods = periods,
                )
            }
        }
        return subaccount
    }

    private fun calculatePositionValues(
        position: InternalPerpetualPosition,
        market: InternalMarketState,
        subaccount: InternalSubaccountState,
        price: Double?,
        periods: Set<CalculationPeriod>,
    ): InternalPerpetualPosition {
        for (period in periods) {
            val calculated = position.calculated[period] ?: InternalPositionCalculated()
            position.calculated[period] = calculated

            if (period == CalculationPeriod.current) {
                calculated.size = position.size
            }
            val size = calculated.size
            val entryPrice = position.entryPrice
            val status = position.status

            if (size != null && status != null) {
                val realizedPnl = position.realizedPnl
                if (realizedPnl != null) {
                    when (status) {
                        IndexerPerpetualPositionStatus.CLOSED, IndexerPerpetualPositionStatus.LIQUIDATED -> {
                            calculated.realizedPnlPercent = null
                        }
                        else -> {
                            if (entryPrice != null) {
                                val positionEntryValue = (size * entryPrice).abs()
                                calculated.realizedPnlPercent = if (positionEntryValue > Numeric.double.ZERO) realizedPnl / positionEntryValue else null
                            }
                        }
                    }
                } else {
                    calculated.realizedPnlPercent = null
                }

                val marketOraclePrice = market.perpetualMarket?.oraclePrice
                val oraclePrice =
                    if (period == CalculationPeriod.current) {
                        marketOraclePrice
                    } else {
                        price ?: marketOraclePrice
                    }

                if (oraclePrice != null) {
                    when (status) {
                        IndexerPerpetualPositionStatus.CLOSED, IndexerPerpetualPositionStatus.LIQUIDATED -> {
                            resetCalculated(calculated)
                        }
                        else -> {
                            val configs = market.perpetualMarket?.configs
                            val valueTotal = size * oraclePrice
                            calculated.valueTotal = valueTotal
                            val notional = valueTotal.abs()
                            calculated.notionalTotal = notional
                            val adjustedImf = calculatedAdjustedImf(configs)
                            val adjustedMmf = calculatedAdjustedMmf(
                                configs = configs,
                                notional = notional,
                            )
                            val maxLeverage =
                                if (adjustedImf != Numeric.double.ZERO) Numeric.double.ONE / adjustedImf else null
                            calculated.adjustedImf = adjustedImf
                            calculated.adjustedMmf = adjustedMmf
                            calculated.initialRiskTotal = adjustedImf * notional
                            calculated.maxLeverage = maxLeverage

                            if (entryPrice != null) {
                                val leverage = position.calculated[period]?.leverage
                                val scaledLeverage = max(leverage?.abs() ?: 1.0, 1.0)
                                val entryValue = size * entryPrice
                                val currentValue = size * oraclePrice
                                val unrealizedPnl = currentValue - entryValue
                                val scaledUnrealizedPnlPercent =
                                    if (entryValue != Numeric.double.ZERO) unrealizedPnl / entryValue.abs() * scaledLeverage else null
                                calculated.unrealizedPnl = unrealizedPnl
                                calculated.unrealizedPnlPercent = scaledUnrealizedPnlPercent
                            }

                            val marginMode = position.marginMode
                            when (marginMode) {
                                MarginMode.Isolated -> {
                                    val equity = subaccount.equity
                                    calculated.marginValue = equity
                                }

                                MarginMode.Cross -> {
                                    val maintenanceMarginFraction =
                                        configs?.maintenanceMarginFraction ?: Numeric.double.ZERO
                                    calculated.marginValue = maintenanceMarginFraction * notional
                                }

                                else -> {
                                    calculated.marginValue = null
                                }
                            }
                        }
                    }
                } else {
                    resetCalculated(calculated)
                }
            } else {
                resetCalculated(calculated)
            }
        }
        return position
    }

    private fun calculatedAdjustedImf(
        configs: MarketConfigs?
    ): Double {
        return configs?.effectiveInitialMarginFraction ?: Numeric.double.ZERO
    }

    private fun calculatedAdjustedMmf(
        configs: MarketConfigs?,
        notional: Double?,
    ): Double {
        val maintenanceMarginFraction = configs?.maintenanceMarginFraction ?: Numeric.double.ZERO
        val notionalValue = notional ?: Numeric.double.ZERO
        return calculateV4MarginFraction(configs, maintenanceMarginFraction, notionalValue)
    }

    private fun calculateV4MarginFraction(
        configs: MarketConfigs?,
        initialMarginFraction: Double,
        notional: Double,
    ): Double {
        return initialMarginFraction
    }

    private fun resetCalculated(calculated: InternalPositionCalculated) {
        calculated.valueTotal = null
        calculated.notionalTotal = null
        calculated.adjustedImf = null
        calculated.adjustedMmf = null
        calculated.initialRiskTotal = null
        calculated.maxLeverage = null
        calculated.unrealizedPnl = null
        calculated.unrealizedPnlPercent = null
        calculated.marginValue = null
        calculated.realizedPnlPercent = null
        calculated.leverage = null
        calculated.size = null
        calculated.liquidationPrice = null
        calculated.buyingPower = null
    }
}
