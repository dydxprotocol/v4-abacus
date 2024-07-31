package exchange.dydx.abacus.calculator

import abs
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal enum class CalculationPeriod(val rawValue: String) {
    current("current"),
    post("postOrder"),
    settled("postAllOrders");

    companion object {
        operator fun invoke(rawValue: String) =
            CalculationPeriod.values().firstOrNull { it.rawValue == rawValue }
    }
}

internal class SubaccountCalculator(val parser: ParserProtocol) {
    internal fun calculate(
        subaccount: Map<String, Any>?,
        configs: Map<String, Any>?,
        markets: Map<String, Any>?,
        price: Map<String, Any>?,
        periods: Set<CalculationPeriod>,
    ): Map<String, Any>? {
        if (subaccount != null) {
            val modified = subaccount.mutable()
            val positions = calculatePositionsValues(
                positions = parser.asNativeMap(subaccount["openPositions"]),
                markets = markets,
                subaccount = subaccount,
                price = price,
                periods = periods,
            )
            positions?.let {
                modified.safeSet("openPositions", it)
            }

            calculateSubaccountEquity(modified, positions, periods)
            calculatePositionsLeverages(positions, markets, modified, periods)
            calculateSubaccountBuyingPower(modified, configs, periods)

            return modified
        } else {
            return subaccount
        }
    }

    private fun calculatePositionsValues(
        positions: Map<String, Any>?,
        markets: Map<String, Any>?,
        subaccount: Map<String, Any>,
        price: Map<String, Any>?,
        periods: Set<CalculationPeriod>,
    ): MutableMap<String, MutableMap<String, Any>>? {
        return if (positions != null) {
            val modified = mutableMapOf<String, MutableMap<String, Any>>()
            for ((key, position) in positions) {
                parser.asNativeMap(position)?.let { position ->
                    parser.asNativeMap(markets?.get(key))?.let { market ->
                        modified[key] = calculatePositionValues(
                            position = position,
                            market = market,
                            subaccount = subaccount,
                            price = parser.asDouble(price?.get(key)),
                            periods = periods,
                        )
                    }
                }
            }
            modified
        } else {
            null
        }
    }

    private fun oraclePrice(market: Map<*, *>?): Double? {
        return parser.asDouble(market?.get("oraclePrice"))
    }

    private fun calculatePositionValues(
        position: Map<String, Any>,
        market: Map<String, Any>?,
        subaccount: Map<String, Any>,
        price: Double?,
        periods: Set<CalculationPeriod>,
    ): MutableMap<String, Any> {
        val modified = position.mutable()
        for (period in periods) {
            val size = parser.asDouble(value(position, "size", period))
            val entryPrice = parser.asDouble(value(position, "entryPrice", period))
            val status = parser.asString(value(position, "status", period))

            if (size != null && status != null) {
                val realizedPnl = parser.asDouble(value(position, "realizedPnl", period))
                if (realizedPnl != null) {
                    when (status) {
                        "CLOSED", "LIQUIDATED" -> {
                            set(null, modified, "realizedPnlPercent", period)
                        }

                        else -> {
                            if (entryPrice != null) {
                                val positionEntryValue = (size * entryPrice).abs()
                                set(
                                    if (positionEntryValue > Numeric.double.ZERO) realizedPnl / positionEntryValue else null,
                                    modified,
                                    "realizedPnlPercent",
                                    period,
                                )
                            }
                        }
                    }
                } else {
                    set(null, modified, "realizedPnlPercent", period)
                }

                val marketOraclePrice = parser.asDouble(oraclePrice(market))
                val oraclePrice =
                    if (period == CalculationPeriod.current) {
                        marketOraclePrice
                    } else {
                        price ?: marketOraclePrice
                    }

                if (oraclePrice != null) {
                    when (status) {
                        "CLOSED", "LIQUIDATED" -> {
                            set(null, modified, "valueTotal", period)
                            set(null, modified, "notionalTotal", period)
                            set(null, modified, "adjustedImf", period)
                            set(null, modified, "adjustedMmf", period)
                            set(null, modified, "initialRiskTotal", period)
                            set(null, modified, "maxLeverage", period)
                            set(null, modified, "unrealizedPnl", period)
                            set(null, modified, "unrealizedPnlPercent", period)
                            set(null, modified, "marginValue", period)
                        }

                        else -> {
                            val configs = parser.asNativeMap(market?.get("configs"))
                            val valueTotal = size * oraclePrice
                            set(valueTotal, modified, "valueTotal", period)
                            val notional = valueTotal.abs()
                            set(notional, modified, "notionalTotal", period)
                            val adjustedImf = calculatedAdjustedImf(configs)
                            val adjustedMmf = calculatedAdjustedMmf(
                                configs,
                                notional,
                            )
                            val maxLeverage =
                                if (adjustedImf != Numeric.double.ZERO) Numeric.double.ONE / adjustedImf else null
                            set(adjustedImf, modified, "adjustedImf", period)
                            set(adjustedMmf, modified, "adjustedMmf", period)
                            set(adjustedImf * notional, modified, "initialRiskTotal", period)
                            set(maxLeverage, modified, "maxLeverage", period)

                            if (entryPrice != null) {
                                val entryValue = size * entryPrice
                                val currentValue = size * oraclePrice
                                val unrealizedPnl = currentValue - entryValue
                                val unrealizedPnlPercent =
                                    if (entryValue != Numeric.double.ZERO) unrealizedPnl / entryValue.abs() else null
                                set(unrealizedPnl, modified, "unrealizedPnl", period)
                                set(unrealizedPnlPercent, modified, "unrealizedPnlPercent", period)
                            }

                            val marginMode = parser.asString(parser.value(position, "marginMode"))
                            when (marginMode) {
                                "ISOLATED" -> {
                                    val equity = parser.asDouble(value(subaccount, "equity", period))
                                    set(equity, modified, "marginValue", period)
                                }
                                "CROSS" -> {
                                    val maintenanceMarginFraction =
                                        parser.asDouble(configs?.get("maintenanceMarginFraction")) ?: Numeric.double.ZERO
                                    set(maintenanceMarginFraction * notional, modified, "marginValue", period)
                                }
                                else -> {
                                    set(null, modified, "marginValue", period)
                                }
                            }
                        }
                    }
                } else {
                    set(null, modified, "valueTotal", period)
                    set(null, modified, "notionalTotal", period)
                    set(null, modified, "adjustedImf", period)
                    set(null, modified, "adjustedMmf", period)
                    set(null, modified, "initialRiskTotal", period)
                    set(null, modified, "maxLeverage", period)
                    set(null, modified, "unrealizedPnl", period)
                    set(null, modified, "unrealizedPnlPercent", period)
                    set(null, modified, "marginValue", period)
                }
            } else {
                set(null, modified, "realizedPnlPercent", period)
                set(null, modified, "unrealizedPnl", period)
                set(null, modified, "unrealizedPnlPercent", period)
                set(null, modified, "valueTotal", period)
                set(null, modified, "notionalTotal", period)
                set(null, modified, "adjustedImf", period)
                set(null, modified, "adjustedMmf", period)
                set(null, modified, "initialRiskTotal", period)
                set(null, modified, "maxLeverage", period)
                set(null, modified, "marginValue", period)
            }
        }
        return modified
    }

    private fun calculatedAdjustedImf(
        configs: Map<String, Any>?,
    ): Double {
        return parser.asDouble(configs?.get("effectiveInitialMarginFraction")) ?: Numeric.double.ZERO
    }

    private fun calculatedAdjustedMmf(
        configs: Map<String, Any>?,
        notional: Double?,
    ): Double {
        val maintenanceMarginFraction =
            parser.asDouble(configs?.get("maintenanceMarginFraction")) ?: Numeric.double.ZERO
        val notionalValue: Double = parser.asDouble(notional) ?: Numeric.double.ZERO
        return calculateV4MarginFraction(configs, maintenanceMarginFraction, notionalValue)
    }

    private fun calculateV4MarginFraction(
        configs: Map<String, Any>?,
        initialMarginFraction: Double,
        notional: Double,
    ): Double {
        return initialMarginFraction
    }

    private fun calculateSubaccountEquity(
        subaccount: MutableMap<String, Any>,
        positions: Map<String, Map<String, Any>>?,
        periods: Set<CalculationPeriod>,
    ) {
        for (period in periods) {
            val quoteBalance = parser.asDouble(value(subaccount, "quoteBalance", period))
            if (quoteBalance != null) {
                var notionalTotal = Numeric.double.ZERO
                var valueTotal = Numeric.double.ZERO
                var initialRiskTotal = Numeric.double.ZERO
                positions?.let {
                    for ((key, position) in positions) {
                        notionalTotal += parser.asDouble(
                            value(
                                position,
                                "notionalTotal",
                                period,
                            ),
                        ) ?: Numeric.double.ZERO

                        valueTotal += parser.asDouble(
                            value(
                                position,
                                "valueTotal",
                                period,
                            ),
                        ) ?: Numeric.double.ZERO

                        initialRiskTotal += parser.asDouble(
                            value(
                                position,
                                "initialRiskTotal",
                                period,
                            ),
                        ) ?: Numeric.double.ZERO
                    }
                }

                val notionalTotalDouble = parser.asDouble(notionalTotal)!!
                set(notionalTotalDouble, subaccount, "notionalTotal", period)
                set(parser.asDouble(valueTotal)!!, subaccount, "valueTotal", period)
                set(parser.asDouble(initialRiskTotal)!!, subaccount, "initialRiskTotal", period)

                val equity = valueTotal + quoteBalance
                val freeCollateral = equity - initialRiskTotal

                val equityDouble = parser.asDouble(equity)!!
                val freeCollateralDouble = parser.asDouble(freeCollateral)!!
                set(equityDouble, subaccount, "equity", period)
                set(freeCollateralDouble, subaccount, "freeCollateral", period)

                if (equityDouble > Numeric.double.ZERO) {
                    val leverage = notionalTotalDouble / equityDouble
                    val marginUsage = Numeric.double.ONE - freeCollateralDouble / equityDouble

                    set(parser.asDouble(leverage), subaccount, "leverage", period)
                    set(parser.asDouble(marginUsage), subaccount, "marginUsage", period)
                } else {
                    set(null, subaccount, "leverage", period)
                    set(null, subaccount, "marginUsage", period)
                }
            } else {
                set(null, subaccount, "notionalTotal", period)
                set(null, subaccount, "valueTotal", period)
                set(null, subaccount, "initialRiskTotal", period)
                set(null, subaccount, "equity", period)
                set(null, subaccount, "freeCollateral", period)
                set(null, subaccount, "leverage", period)
                set(null, subaccount, "marginUsage", period)
            }
        }
    }

    private fun calculatePositionsLeverages(
        positions: MutableMap<String, MutableMap<String, Any>>?,
        markets: Map<String, Any>?,
        subaccount: MutableMap<String, Any>,
        periods: Set<CalculationPeriod>,
    ) {
        positions?.let {
            for (period in periods) {
                val initialRiskTotal =
                    parser.asDouble(value(subaccount, "initialRiskTotal", period))
                val equity = parser.asDouble(value(subaccount, "equity", period))
                for ((key, position) in positions) {
                    val leverage = calculatePositionLeverage(
                        equity = equity,
                        notionalValue = parser.asDouble(value(position, "valueTotal", period)),
                    )
                    set(leverage, position, "leverage", period)
                    val liquidationPrice = calculatePositionLiquidationPrice(
                        equity = equity ?: Numeric.double.ZERO,
                        marketId = key,
                        positions = positions,
                        markets = markets,
                        period = period,
                    )
                    set(liquidationPrice, position, "liquidationPrice", period)
                    val buyingPower = calculatePositionBuyingPower(
                        equity = equity,
                        initialRiskTotal = initialRiskTotal,
                        imf = parser.asDouble(value(position, "adjustedImf", period)),
                    )
                    set(buyingPower, position, "buyingPower", period)
                }
            }
        }
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
        positions: Map<String, MutableMap<String, Any>>?,
        markets: Map<String, Any>?,
        period: CalculationPeriod,
    ): Double? {
        val otherPositionsRisk =
            calculationOtherPositionsRisk(positions, markets, except = marketId, period)

        val position = positions?.get(marketId) ?: return null
        val market = parser.asNativeMap(markets?.get(marketId)) ?: return null
        val maintenanceMarginFraction = parser.asDouble(value(position, "adjustedMmf", period)) ?: return null
        val oraclePrice = parser.asDouble(oraclePrice(market)) ?: return null
        val size = parser.asDouble(value(position, "size", period)) ?: return null

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
        positions: Map<String, Map<String, Any>>?,
        markets: Map<String, Any>?,
        except: String,
        period: CalculationPeriod,
    ): Double {
        positions?.let {
            var risk = Numeric.double.ZERO
            for ((key, position) in positions) {
                if (key != except) {
                    risk += calculatePositionRisk(
                        position,
                        parser.asNativeMap(markets?.get(key)),
                        period,
                    )
                }
            }
            return risk
        }
        return Numeric.double.ZERO
    }

    private fun calculatePositionRisk(
        position: Map<String, Any>,
        market: Map<String, Any>?,
        period: CalculationPeriod,
    ): Double {
        market?.let {
            parser.asNativeMap(market["configs"])?.let { configs ->
                parser.asDouble(value(position, "adjustedMmf", period))
                    ?.let { maintenanceMarginFraction ->
                        parser.asDouble(oraclePrice(market))?.let { oraclePrice ->
                            parser.asDouble(value(position, "size", period))?.let { size ->
                                return size.abs() * oraclePrice * maintenanceMarginFraction
                            }
                        }
                    }
            }
        }
        return Numeric.double.ZERO
    }

    private fun calculatePositionBuyingPower(
        equity: Double?,
        initialRiskTotal: Double?,
        imf: Double?,
    ): Double? {
        return if (equity != null && initialRiskTotal != null && imf != null) {
            calculateBuyingPower(
                equity,
                initialRiskTotal,
                imf,
            )
        } else {
            null
        }
    }

    private fun calculateSubaccountBuyingPower(
        subaccount: MutableMap<String, Any>,
        configs: Map<String, Any>?,
        periods: Set<CalculationPeriod>,
    ) {
        for (period in periods) {
            val quoteBalance = parser.asDouble(value(subaccount, "quoteBalance", period))
            if (quoteBalance != null) {
                val equity =
                    parser.asDouble(value(subaccount, "equity", period)) ?: Numeric.double.ZERO
                val initialRiskTotal =
                    parser.asDouble(value(subaccount, "initialRiskTotal", period))
                        ?: Numeric.double.ZERO
                val imf =
                    parser.asDouble(configs?.get("initialMarginFraction"))
                        ?: parser.asDouble(0.05)!!
                set(
                    calculateBuyingPower(equity, initialRiskTotal, imf),
                    subaccount,
                    "buyingPower",
                    period,
                )
            } else {
                set(
                    null,
                    subaccount,
                    "buyingPower",
                    period,
                )
            }
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
                parser.asDouble(
                    0.05,
                )!!
            }
            )
    }

    private fun key(period: CalculationPeriod): String {
        return period.rawValue
    }

    private fun value(data: Map<*, *>, key: String, period: CalculationPeriod): Any? {
        val value = data[key]
        val map = parser.asNativeMap(value)
        return if (map != null) {
            map[key(period)]
        } else {
            value
        }
    }

    private fun set(
        value: Any?,
        data: MutableMap<String, Any>,
        key: String,
        period: CalculationPeriod,
    ) {
        val map: MutableMap<String, Any> =
            parser.asNativeMap(data[key])?.mutable() ?: mutableMapOf<String, Any>()
        map.safeSet(key(period), value)
        data[key] = map
    }
}
