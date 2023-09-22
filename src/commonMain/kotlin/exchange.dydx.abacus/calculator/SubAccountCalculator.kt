package exchange.dydx.abacus.calculator

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.AppVersion
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMutableMapOf
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
        subaccount: IMap<String, Any>?,
        configs: IMap<String, Any>?,
        markets: IMap<String, Any>?,
        price: IMap<String, Any>?,
        periods: kollections.Set<CalculationPeriod>,
        version: AppVersion,
    ): IMap<String, Any>? {
        if (subaccount != null) {
            val modified = subaccount.mutable()
            val positions = calculatePositionsValues(
                parser.asMap(subaccount["openPositions"]),
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
        positions: IMap<String, Any>?,
        markets: IMap<String, Any>?,
        subaccount: IMap<String, Any>,
        price: IMap<String, Any>?,
        periods: kollections.Set<CalculationPeriod>,
        version: AppVersion,
    ): IMutableMap<String, IMutableMap<String, Any>>? {
        return if (positions != null) {
            val modified = iMutableMapOf<String, IMutableMap<String, Any>>()
            for ((key, position) in positions) {
                parser.asMap(position)?.let { position ->
                    parser.asMap(markets?.get(key))?.let { market ->
                        modified[key] = calculatePositionValues(
                            position,
                            market,
                            subaccount,
                            parser.asDecimal(price?.get(key)),
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

    private fun oraclePrice(market: IMap<*, *>?): Double? {
        return parser.asDouble(market?.get("oraclePrice"))
    }

    private fun calculatePositionValues(
        position: IMap<String, Any>,
        market: IMap<String, Any>?,
        subaccount: IMap<String, Any>,
        price: BigDecimal?,
        periods: kollections.Set<CalculationPeriod>,
        version: AppVersion,
    ): IMutableMap<String, Any> {
        val modified = position.mutable()
        for (period in periods) {
            val size = parser.asDecimal(value(position, "size", period))
            val entryPrice = parser.asDecimal(value(position, "entryPrice", period))
            val status = parser.asString(value(position, "status", period))

            if (size != null && status != null) {
                val realizedPnl = parser.asDecimal(value(position, "realizedPnl", period))
                if (realizedPnl != null) {
                    when (status) {
                        "CLOSED", "LIQUIDATED" -> {
                            set(null, modified, "realizedPnlPercent", period)
                        }

                        else -> {
                            if (entryPrice != null) {
                                val positionEntryValue = (size * entryPrice).abs()
                                set(
                                    if (positionEntryValue > Numeric.decimal.ZERO) realizedPnl / positionEntryValue else null,
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

                val marketIndexPrice = parser.asDecimal(market?.get("indexPrice"))
                    ?: parser.asDecimal(market?.get("oraclePrice"))
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
                                    if (entryValue != Numeric.decimal.ZERO) unrealizedPnl / entryValue.abs() else null
                                set(unrealizedPnl, modified, "unrealizedPnl", period)
                                set(unrealizedPnlPercent, modified, "unrealizedPnlPercent", period)
                            }
                        }
                    }
                } else {
                    set(null, modified, "unrealizedPnl", period)
                    set(null, modified, "unrealizedPnlPercent", period)
                }


                val marketOraclePrice = parser.asDecimal(oraclePrice(market))
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
                                parser.asMap(market?.get("configs")),
                                subaccount,
                                size,
                                notional,
                                version
                            )
                            val adjustedMmf = calculatedAdjustedMmf(
                                parser.asMap(market?.get("configs")),
                                notional,
                                version
                            )
                            val maxLeverage =
                                if (adjustedImf != Numeric.decimal.ZERO) Numeric.decimal.ONE / adjustedImf else null
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
        configs: IMap<String, Any>?,
        subaccount: IMap<String, Any>,
        size: BigDecimal?,
        notional: BigDecimal?,
        version: AppVersion,
    ): BigDecimal {
        val initialMarginFraction =
            parser.asDecimal(configs?.get("initialMarginFraction")) ?: Numeric.decimal.ZERO
        val notionalValue: Double = parser.asDouble(notional) ?: Numeric.double.ZERO
        return calculateV4MarginFraction(configs, initialMarginFraction, notionalValue)
    }

    private fun calculatedAdjustedMmf(
        configs: IMap<String, Any>?,
        notional: BigDecimal?,
        version: AppVersion,
    ): BigDecimal {
        val maintenanceMarginFraction =
            parser.asDecimal(configs?.get("maintenanceMarginFraction")) ?: Numeric.decimal.ZERO
        val notionalValue: Double = parser.asDouble(notional) ?: Numeric.double.ZERO
        return calculateV4MarginFraction(configs, maintenanceMarginFraction, notionalValue)
    }

    private fun calculateV4MarginFraction(
        configs: IMap<String, Any>?,
        initialMarginFraction: BigDecimal,
        notional: Double,
    ): BigDecimal {
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
            parser.asDecimal(min) ?: initialMarginFraction
        }
    }

    private fun calculateSubaccountEquity(
        subaccount: IMutableMap<String, Any>,
        positions: IMap<String, IMap<String, Any>>?,
        periods: kollections.Set<CalculationPeriod>,
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
        positions: IMutableMap<String, IMutableMap<String, Any>>?,
        markets: IMap<String, Any>?,
        subaccount: IMutableMap<String, Any>,
        periods: kollections.Set<CalculationPeriod>,
    ) {
        positions?.let {
            for (period in periods) {
                val initialRiskTotal =
                    parser.asDecimal(value(subaccount, "initialRiskTotal", period))
                val equity = parser.asDecimal(value(subaccount, "equity", period))
                for ((key, position) in positions) {
                    val leverage = calculatePositionLeverage(
                        equity,
                        parser.asDecimal(value(position, "valueTotal", period))
                    )
                    set(leverage?.doubleValue(false), position, "leverage", period)
                    val liquidationPrice = calculatePositionLiquidationPrice(
                        equity ?: Numeric.decimal.ZERO,
                        key,
                        positions,
                        markets,
                        period
                    )
                    set(liquidationPrice?.doubleValue(false), position, "liquidationPrice", period)
                    val buyingPower = calculatePositionBuyingPower(
                        equity,
                        initialRiskTotal,
                        parser.asDecimal(value(position, "adjustedImf", period))
                    )
                    set(buyingPower?.doubleValue(false), position, "buyingPower", period)
                }
            }
        }
    }

    private fun calculatePositionLeverage(
        equity: BigDecimal?,
        notionalValue: BigDecimal?,
    ): BigDecimal? {
        return if (equity != null && notionalValue != null && equity > Numeric.decimal.ZERO) {
            notionalValue / equity
        } else {
            null
        }
    }


    private fun calculatePositionLiquidationPrice(
        equity: BigDecimal,
        market: String,
        positions: IMap<String, IMutableMap<String, Any>>?,
        markets: IMap<String, Any>?,
        period: CalculationPeriod,
    ): BigDecimal? {
        val otherPositionsRisk =
            calculationOtherPositionsRisk(positions, markets, except = market, period)

        var liquidationPrice: BigDecimal? = null
        positions?.get(market)?.let { position ->
            parser.asMap(markets?.get(market))?.let { market ->
                parser.asMap(market["configs"])?.let { configs ->
                    parser.asDecimal(value(position, "adjustedMmf", period))
                        ?.let { maintenanceMarginFraction ->
                            parser.asDecimal(oraclePrice(market))?.let { oraclePrice ->
                                parser.asDecimal(value(position, "size", period))?.let { size ->
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
                                        if (size > Numeric.decimal.ZERO) (size - size * maintenanceMarginFraction) else (size + size * maintenanceMarginFraction)
                                    liquidationPrice = if (denominator != Numeric.decimal.ZERO) {
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
        if (liquidationPrice != null && liquidationPrice!! < Numeric.decimal.ZERO) {
            liquidationPrice = null
        }
        return liquidationPrice
    }

    private fun calculationOtherPositionsRisk(
        positions: IMap<String, IMutableMap<String, Any>>?,
        markets: IMap<String, Any>?,
        except: String,
        period: CalculationPeriod,
    ): BigDecimal {
        positions?.let {
            var risk = Numeric.decimal.ZERO
            for ((key, position) in positions) {
                if (key != except) {
                    risk += calculatePositionRisk(
                        position,
                        parser.asMap(markets?.get(key)),
                        period
                    )
                }
            }
            return risk
        }
        return Numeric.decimal.ZERO
    }

    private fun calculatePositionRisk(
        position: IMap<String, Any>,
        market: IMap<String, Any>?,
        period: CalculationPeriod,
    ): BigDecimal {
        market?.let {
            parser.asMap(market["configs"])?.let { configs ->
                parser.asDecimal(value(position, "adjustedMmf", period))
                    ?.let { maintenanceMarginFraction ->
                        parser.asDecimal(oraclePrice(market))?.let { oraclePrice ->
                            parser.asDecimal(value(position, "size", period))?.let { size ->
                                return size.abs() * oraclePrice * maintenanceMarginFraction
                            }
                        }
                    }
            }
        }
        return Numeric.decimal.ZERO
    }


    private fun calculatePositionBuyingPower(
        equity: BigDecimal?,
        initialRiskTotal: BigDecimal?,
        imf: BigDecimal?,
    ): BigDecimal? {
        return if (equity != null && initialRiskTotal != null && imf != null) {
            calculateBuyingPower(
                equity,
                initialRiskTotal,
                imf
            )
        } else null
    }

    private fun calculateSubaccountBuyingPower(
        subaccount: IMutableMap<String, Any>,
        configs: IMap<String, Any>?,
        periods: kollections.Set<CalculationPeriod>,
    ) {
        for (period in periods) {
            val quoteBalance = parser.asDouble(value(subaccount, "quoteBalance", period))
            if (quoteBalance != null) {
                val equity =
                    parser.asDecimal(value(subaccount, "equity", period)) ?: Numeric.decimal.ZERO
                val initialRiskTotal =
                    parser.asDecimal(value(subaccount, "initialRiskTotal", period))
                        ?: Numeric.decimal.ZERO
                val imf =
                    parser.asDecimal(configs?.get("initialMarginFraction"))
                        ?: parser.asDecimal(0.05)!!
                set(
                    calculateBuyingPower(equity, initialRiskTotal, imf).doubleValue(false),
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
        equity: BigDecimal,
        initialRiskTotal: BigDecimal,
        imf: BigDecimal,
    ): BigDecimal {
        val buyingPowerFreeCollateral = equity - initialRiskTotal
        return buyingPowerFreeCollateral / (if (imf > Numeric.decimal.ZERO) imf else parser.asDecimal(
            0.05
        )!!)
    }

    private fun key(period: CalculationPeriod): String {
        return period.rawValue
    }

    private fun value(data: IMap<*, *>, key: String, period: CalculationPeriod): Any? {
        val value = data[key]
        val map = parser.asMap(value)
        return if (map != null) {
            map[key(period)]
        } else {
            value
        }
    }

    private fun set(
        value: Any?,
        data: IMutableMap<String, Any>,
        key: String,
        period: CalculationPeriod,
    ) {
        val map: IMutableMap<String, Any> =
            parser.asMap(data[key])?.mutable() ?: iMutableMapOf<String, Any>()
        map.safeSet(key(period), value)
        data[key] = map
    }

}