package exchange.dydx.abacus.calculator

import abs
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.AppVersion
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kotlin.math.sqrt


internal enum class CalculationPeriod(val rawValue: String) {
    current("current"), post("postOrder"), settled("postAllOrders");

    companion object {
        operator fun invoke(rawValue: String) =
            CalculationPeriod.values().firstOrNull { it.rawValue == rawValue }
    }
}


@Suppress("UNCHECKED_CAST")
internal class SubaccountCalculator(val parser: ParserProtocol) {
    internal fun calculate(
        subaccount: Map<String, Any>?,
        configs: Map<String, Any>?,
        markets: Map<String, Any>?,
        price: Map<String, Any>?,
        periods: Set<CalculationPeriod>,
        version: AppVersion,
    ): Map<String, Any>? {
        if (subaccount != null) {
            val modified = subaccount.mutable()
            val positions = calculatePositionsValues(
                parser.asNativeMap(subaccount["openPositions"]),
                markets,
                subaccount,
                price,
                periods,
                version
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
        version: AppVersion,
    ): MutableMap<String, MutableMap<String, Any>>? {
        return if (positions != null) {
            val modified = mutableMapOf<String, MutableMap<String, Any>>()
            for ((key, position) in positions) {
                parser.asNativeMap(position)?.let { position ->
                    parser.asNativeMap(markets?.get(key))?.let { market ->
                        modified[key] = calculatePositionValues(
                            position,
                            market,
                            subaccount,
                            parser.asDouble(price?.get(key)),
                            periods,
                            version
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
        version: AppVersion,
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
                                    period
                                )
                            }
                        }
                    }
                } else {
                    set(null, modified, "realizedPnlPercent", period)
                }

                val marketIndexPrice = parser.asDouble(market?.get("indexPrice"))
                    ?: parser.asDouble(market?.get("oraclePrice"))
                val indexPrice =
                    if (period == CalculationPeriod.current) marketIndexPrice else (price
                        ?: marketIndexPrice)
                if (indexPrice != null) {
                    when (status) {
                        "CLOSED", "LIQUIDATED" -> {
                            set(null, modified, "unrealizedPnl", period)
                            set(null, modified, "unrealizedPnlPercent", period)
                        }

                        else -> {
                            if (entryPrice != null) {
                                val entryValue = size * entryPrice
                                val currentValue = size * indexPrice
                                val unrealizedPnl = currentValue - entryValue
                                val unrealizedPnlPercent =
                                    if (entryValue != Numeric.double.ZERO) unrealizedPnl / entryValue.abs() else null
                                set(unrealizedPnl, modified, "unrealizedPnl", period)
                                set(unrealizedPnlPercent, modified, "unrealizedPnlPercent", period)
                            }
                        }
                    }
                } else {
                    set(null, modified, "unrealizedPnl", period)
                    set(null, modified, "unrealizedPnlPercent", period)
                }


                val marketOraclePrice = parser.asDouble(oraclePrice(market))
                val oraclePrice =
                    if (period == CalculationPeriod.current) marketOraclePrice else (price
                        ?: marketOraclePrice)
                if (oraclePrice != null) {
                    when (status) {
                        "CLOSED", "LIQUIDATED" -> {
                            set(null, modified, "valueTotal", period)
                            set(null, modified, "notionalTotal", period)
                            set(null, modified, "adjustedImf", period)
                            set(null, modified, "adjustedMmf", period)
                            set(null, modified, "initialRiskTotal", period)
                            set(null, modified, "maxLeverage", period)
                        }

                        else -> {
                            val valueTotal = size * oraclePrice
                            set(valueTotal, modified, "valueTotal", period)
                            val notional = valueTotal.abs()
                            set(notional, modified, "notionalTotal", period)
                            val adjustedImf = calculatedAdjustedImf(
                                parser.asNativeMap(market?.get("configs")),
                                subaccount,
                                size,
                                notional,
                                version
                            )
                            val adjustedMmf = calculatedAdjustedMmf(
                                parser.asNativeMap(market?.get("configs")),
                                notional,
                                version
                            )
                            val maxLeverage =
                                if (adjustedImf != Numeric.double.ZERO) Numeric.double.ONE / adjustedImf else null
                            set(adjustedImf, modified, "adjustedImf", period)
                            set(adjustedMmf, modified, "adjustedMmf", period)
                            set(
                                adjustedImf * notional,
                                modified,
                                "initialRiskTotal",
                                period
                            )
                            set(maxLeverage, modified, "maxLeverage", period)
                        }
                    }
                } else {
                    set(null, modified, "valueTotal", period)
                    set(null, modified, "notionalTotal", period)
                    set(null, modified, "adjustedImf", period)
                    set(null, modified, "adjustedMmf", period)
                    set(null, modified, "initialRiskTotal", period)
                    set(null, modified, "maxLeverage", period)
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
            }
        }
        return modified
    }

    private fun calculatedAdjustedImf(
        configs: Map<String, Any>?,
        subaccount: Map<String, Any>,
        size: Double?,
        notional: Double?,
        version: AppVersion,
    ): Double {
        val initialMarginFraction =
            parser.asDouble(configs?.get("initialMarginFraction")) ?: Numeric.double.ZERO
        val notionalValue: Double = parser.asDouble(notional) ?: Numeric.double.ZERO
        return calculateV4MarginFraction(configs, initialMarginFraction, notionalValue)
    }

    private fun calculatedAdjustedMmf(
        configs: Map<String, Any>?,
        notional: Double?,
        version: AppVersion,
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
        val basePositionNotional =
            parser.asDouble(configs?.get("basePositionNotional")) ?: Numeric.double.ZERO
        return if (basePositionNotional == Numeric.double.ZERO) {
            initialMarginFraction
        } else if (notional < basePositionNotional) {
            initialMarginFraction
        } else {
            val ratio = notional / basePositionNotional
            val adjusted: Double = parser.asDouble(initialMarginFraction)!! * sqrt(ratio)
            val min: Double = arrayOf(Numeric.double.ONE, adjusted).min()
            parser.asDouble(min) ?: initialMarginFraction
        }
    }

    private fun calculateSubaccountEquity(
        subaccount: MutableMap<String, Any>,
        positions: Map<String, Map<String, Any>>?,
        periods: Set<CalculationPeriod>,
    ) {
        for (period in periods) {
            val quoteBalance = parser.asDecimal(value(subaccount, "quoteBalance", period))
            if (quoteBalance != null) {
                var notionalTotal = Numeric.decimal.ZERO
                var valueTotal = Numeric.decimal.ZERO
                var initialRiskTotal = Numeric.decimal.ZERO
                positions?.let {
                    for ((key, position) in positions) {
                        notionalTotal += parser.asDecimal(
                            value(
                                position,
                                "notionalTotal",
                                period
                            )
                        ) ?: Numeric.decimal.ZERO

                        valueTotal += parser.asDecimal(
                            value(
                                position,
                                "valueTotal",
                                period
                            )
                        ) ?: Numeric.decimal.ZERO

                        initialRiskTotal += parser.asDecimal(
                            value(
                                position,
                                "initialRiskTotal",
                                period
                            )
                        ) ?: Numeric.decimal.ZERO
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
                        equity,
                        parser.asDouble(value(position, "valueTotal", period))
                    )
                    set(leverage, position, "leverage", period)
                    val liquidationPrice = calculatePositionLiquidationPrice(
                        equity ?: Numeric.double.ZERO,
                        key,
                        positions,
                        markets,
                        period
                    )
                    set(liquidationPrice, position, "liquidationPrice", period)
                    val buyingPower = calculatePositionBuyingPower(
                        equity,
                        initialRiskTotal,
                        parser.asDouble(value(position, "adjustedImf", period))
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
        market: String,
        positions: Map<String, MutableMap<String, Any>>?,
        markets: Map<String, Any>?,
        period: CalculationPeriod,
    ): Double? {
        val otherPositionsRisk =
            calculationOtherPositionsRisk(positions, markets, except = market, period)

        var liquidationPrice: Double? = null
        positions?.get(market)?.let { position ->
            parser.asNativeMap(markets?.get(market))?.let { market ->
                parser.asNativeMap(market["configs"])?.let { configs ->
                    parser.asDouble(value(position, "adjustedMmf", period))
                        ?.let { maintenanceMarginFraction ->
                            parser.asDouble(oraclePrice(market))?.let { oraclePrice ->
                                parser.asDouble(value(position, "size", period))?.let { size ->
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
                                    liquidationPrice = if (denominator != Numeric.double.ZERO) {
                                        (otherPositionsRisk + size * oraclePrice - equity) / denominator
                                    } else null
                                    if (liquidationPrice != null && liquidationPrice!! < Numeric.double.ZERO) {
                                        liquidationPrice = null
                                    }
                                }
                            }
                        }
                }
            }
        }
        if (liquidationPrice != null && liquidationPrice!! < Numeric.double.ZERO) {
            liquidationPrice = null
        }
        return liquidationPrice
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
                        period
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
                imf
            )
        } else null
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
                    period
                )
            } else {
                set(
                    null,
                    subaccount,
                    "buyingPower",
                    period
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
        return buyingPowerFreeCollateral / (if (imf > Numeric.double.ZERO) imf else parser.asDouble(
            0.05
        )!!)
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