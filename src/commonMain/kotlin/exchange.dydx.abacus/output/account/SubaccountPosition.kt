package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.output.TradeStatesWithStringValues
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountPosition(
    val id: String,
    val assetId: String,
    val displayId: String,
    val side: TradeStatesWithPositionSides,
    val entryPrice: TradeStatesWithDoubleValues,
    val exitPrice: Double?,
    val createdAtMilliseconds: Double?,
    val closedAtMilliseconds: Double?,
    val netFunding: Double?,
    val realizedPnl: TradeStatesWithDoubleValues,
    val realizedPnlPercent: TradeStatesWithDoubleValues,
    val unrealizedPnl: TradeStatesWithDoubleValues,
    val unrealizedPnlPercent: TradeStatesWithDoubleValues,
    val size: TradeStatesWithDoubleValues,
    val notionalTotal: TradeStatesWithDoubleValues,
    val valueTotal: TradeStatesWithDoubleValues,
    val initialRiskTotal: TradeStatesWithDoubleValues,
    val adjustedImf: TradeStatesWithDoubleValues,
    val adjustedMmf: TradeStatesWithDoubleValues,
    val leverage: TradeStatesWithDoubleValues,
    val maxLeverage: TradeStatesWithDoubleValues,
    val buyingPower: TradeStatesWithDoubleValues,
    val liquidationPrice: TradeStatesWithDoubleValues,
    val resources: SubaccountPositionResources,
    val childSubaccountNumber: Int?,
    val freeCollateral: TradeStatesWithDoubleValues,
    val marginUsage: TradeStatesWithDoubleValues,
    val quoteBalance: TradeStatesWithDoubleValues, // available for isolated market position
    val equity: TradeStatesWithDoubleValues, // available for isolated market position
    val marginMode: MarginMode?,
    val marginValue: TradeStatesWithDoubleValues,
) {
    companion object {
        internal fun create(
            existing: SubaccountPosition?,
            parser: ParserProtocol,
            data: Map<String, Any>?,
            positionId: String?, // e.g., "ETH-USD"
            position: InternalPerpetualPosition?,
            subaccount: InternalSubaccountState?,
        ): SubaccountPosition? {
            Logger.d { "creating Account Position\n" }
            data?.let {
                val id = positionId ?: parser.asString(data["id"]) ?: error("id not found")
                val displayId = if (positionId != null) MarketId.getDisplayId(id) else parser.asString(data["displayId"])
                val assetId = if (positionId != null) MarketId.getAssetId(id) else parser.asString(data["assetId"])
                val resources = position?.resources ?: parser.asMap(data["resources"])?.let {
                    SubaccountPositionResources.create(existing?.resources, parser, it)
                }

                if (displayId !== null && assetId != null && resources !== null) {
                    val childSubaccountNumber = position?.subaccountNumber
                        ?: parser.asInt(data["childSubaccountNumber"])

                    val marginMode = position?.marginMode
                        ?: parser.asString(data["marginMode"])?.let { MarginMode.invoke(it) }

                    val entryPrice = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.entryPrice,
                            postOrder = null,
                            postAllOrders = null,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.entryPrice,
                            parser = parser,
                            data = parser.asMap(data["entryPrice"]),
                        )
                    }

                    val exitPrice = position?.exitPrice
                        ?: parser.asDouble(data["exitPrice"])
                    val createdAtMilliseconds =
                        position?.createdAt?.toEpochMilliseconds()?.toDouble()
                            ?: parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()
                                ?.toDouble()
                    val closedAtMilliseconds = position?.closedAt?.toEpochMilliseconds()?.toDouble()
                        ?: parser.asDatetime(data["closedAt"])?.toEpochMilliseconds()?.toDouble()
                    val netFunding = position?.netFunding ?: parser.asDouble(data["netFunding"])

                    val realizedPnl = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.realizedPnl,
                            postOrder = null,
                            postAllOrders = null,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.realizedPnl,
                            parser = parser,
                            data = parser.asMap(data["realizedPnl"]),
                        )
                    }

                    val realizedPnlPercent =
                        if (position != null) {
                            TradeStatesWithDoubleValues(
                                current = position.calculated[CalculationPeriod.current]?.realizedPnlPercent,
                                postOrder = position.calculated[CalculationPeriod.post]?.realizedPnlPercent,
                                postAllOrders = position.calculated[CalculationPeriod.settled]?.realizedPnlPercent,
                            )
                        } else {
                            TradeStatesWithDoubleValues.create(
                                existing = existing?.realizedPnlPercent,
                                parser = parser,
                                data = parser.asMap(data["realizedPnlPercent"]),
                            )
                        }

                    val unrealizedPnl = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.unrealizedPnl,
                            postOrder = position.calculated[CalculationPeriod.post]?.unrealizedPnl,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.unrealizedPnl,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.unrealizedPnl,
                            parser = parser,
                            data = parser.asMap(data["unrealizedPnl"]),
                        )
                    }

                    val unrealizedPnlPercent = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.unrealizedPnlPercent,
                            postOrder = position.calculated[CalculationPeriod.post]?.unrealizedPnlPercent,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.unrealizedPnlPercent,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.unrealizedPnlPercent,
                            parser = parser,
                            data = parser.asMap(data["unrealizedPnlPercent"]),
                        )
                    }

                    val size = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.size,
                            postOrder = position.calculated[CalculationPeriod.post]?.size,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.size,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.size,
                            parser = parser,
                            data = parser.asMap(data["size"]),
                        )
                    }

                    val notionalTotal = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.notionalTotal,
                            postOrder = position.calculated[CalculationPeriod.post]?.notionalTotal,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.notionalTotal,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.notionalTotal,
                            parser = parser,
                            data = parser.asMap(data["notionalTotal"]),
                        )
                    }

                    val valueTotal = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.valueTotal,
                            postOrder = position.calculated[CalculationPeriod.post]?.valueTotal,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.valueTotal,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.valueTotal,
                            parser = parser,
                            data = parser.asMap(data["valueTotal"]),
                        )
                    }

                    val initialRiskTotal = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.initialRiskTotal,
                            postOrder = position.calculated[CalculationPeriod.post]?.initialRiskTotal,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.initialRiskTotal,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.initialRiskTotal,
                            parser = parser,
                            data = parser.asMap(data["initialRiskTotal"]),
                        )
                    }

                    val adjustedImf = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.adjustedImf,
                            postOrder = position.calculated[CalculationPeriod.post]?.adjustedImf,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.adjustedImf,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.adjustedImf,
                            parser = parser,
                            data = parser.asMap(data["adjustedImf"]),
                        )
                    }

                    val adjustedMmf = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.adjustedMmf,
                            postOrder = position.calculated[CalculationPeriod.post]?.adjustedMmf,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.adjustedMmf,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.adjustedMmf,
                            parser = parser,
                            data = parser.asMap(data["adjustedMmf"]),
                        )
                    }

                    val leverage = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.leverage,
                            postOrder = position.calculated[CalculationPeriod.post]?.leverage,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.leverage,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.leverage,
                            parser = parser,
                            data = parser.asMap(data["leverage"]),
                        )
                    }

                    val maxLeverage = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.maxLeverage,
                            postOrder = position.calculated[CalculationPeriod.post]?.maxLeverage,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.maxLeverage,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.leverage,
                            parser = parser,
                            data = parser.asMap(data["maxLeverage"]),
                        )
                    }

                    val buyingPower = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.buyingPower,
                            postOrder = position.calculated[CalculationPeriod.post]?.buyingPower,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.buyingPower,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.leverage,
                            parser = parser,
                            data = parser.asMap(data["buyingPower"]),
                        )
                    }

                    val liquidationPrice = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.liquidationPrice,
                            postOrder = position.calculated[CalculationPeriod.post]?.liquidationPrice,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.liquidationPrice,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = existing?.liquidationPrice,
                            parser = parser,
                            data = parser.asMap(data["liquidationPrice"]),
                        )
                    }

                    val freeCollateral = if (subaccount != null) {
                        TradeStatesWithDoubleValues(
                            current = subaccount.calculated[CalculationPeriod.current]?.freeCollateral,
                            postOrder = subaccount.calculated[CalculationPeriod.post]?.freeCollateral,
                            postAllOrders = subaccount.calculated[CalculationPeriod.settled]?.freeCollateral,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = null,
                            parser = parser,
                            data = parser.asMap(data["freeCollateral"]),
                        )
                    }

                    val marginUsage = if (subaccount != null) {
                        TradeStatesWithDoubleValues(
                            current = subaccount.calculated[CalculationPeriod.current]?.marginUsage,
                            postOrder = subaccount.calculated[CalculationPeriod.post]?.marginUsage,
                            postAllOrders = subaccount.calculated[CalculationPeriod.settled]?.marginUsage,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = null,
                            parser = parser,
                            data = parser.asMap(data["marginUsage"]),
                        )
                    }

                    val quoteBalance = if (subaccount != null) {
                        TradeStatesWithDoubleValues(
                            current = subaccount.calculated[CalculationPeriod.current]?.quoteBalance,
                            postOrder = subaccount.calculated[CalculationPeriod.post]?.quoteBalance,
                            postAllOrders = subaccount.calculated[CalculationPeriod.settled]?.quoteBalance,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = null,
                            parser = parser,
                            data = parser.asMap(data["quoteBalance"]),
                        )
                    }

                    val equity = if (subaccount != null) {
                        TradeStatesWithDoubleValues(
                            current = subaccount.calculated[CalculationPeriod.current]?.equity,
                            postOrder = subaccount.calculated[CalculationPeriod.post]?.equity,
                            postAllOrders = subaccount.calculated[CalculationPeriod.settled]?.equity,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = null,
                            parser = parser,
                            data = parser.asMap(data["equity"]),
                        )
                    }

                    val marginValue = if (position != null) {
                        TradeStatesWithDoubleValues(
                            current = position.calculated[CalculationPeriod.current]?.marginValue,
                            postOrder = position.calculated[CalculationPeriod.post]?.marginValue,
                            postAllOrders = position.calculated[CalculationPeriod.settled]?.marginValue,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing = null,
                            parser = parser,
                            data = parser.asMap(data["marginValue"]),
                        )
                    }

                    return if (existing?.id != id ||
                        existing.assetId != assetId ||
                        existing.displayId != displayId ||
                        existing.entryPrice !== entryPrice ||
                        existing.exitPrice != exitPrice ||
                        existing.createdAtMilliseconds != createdAtMilliseconds ||
                        existing.closedAtMilliseconds != closedAtMilliseconds ||
                        existing.netFunding != netFunding ||
                        existing.realizedPnl !== realizedPnl ||
                        existing.realizedPnlPercent !== realizedPnlPercent ||
                        existing.unrealizedPnl !== unrealizedPnl ||
                        existing.unrealizedPnlPercent !== unrealizedPnlPercent ||
                        existing.size !== size ||
                        existing.notionalTotal !== notionalTotal ||
                        existing.valueTotal !== valueTotal ||
                        existing.initialRiskTotal !== initialRiskTotal ||
                        existing.adjustedImf !== adjustedImf ||
                        existing.adjustedMmf !== adjustedMmf ||
                        existing.leverage !== leverage ||
                        existing.maxLeverage !== maxLeverage ||
                        existing.buyingPower !== buyingPower ||
                        existing.liquidationPrice !== liquidationPrice ||
                        existing.resources !== resources ||
                        existing.childSubaccountNumber != childSubaccountNumber ||
                        existing.freeCollateral !== freeCollateral ||
                        existing.marginUsage !== marginUsage ||
                        existing.quoteBalance !== quoteBalance ||
                        existing.equity !== equity ||
                        existing.marginMode != marginMode ||
                        existing.marginValue !== marginValue
                    ) {
                        val side = positionSide(size)
                        SubaccountPosition(
                            id = id,
                            assetId = assetId,
                            displayId = displayId,
                            side = side,
                            entryPrice = entryPrice,
                            exitPrice = exitPrice,
                            createdAtMilliseconds = createdAtMilliseconds,
                            closedAtMilliseconds = closedAtMilliseconds,
                            netFunding = netFunding,
                            realizedPnl = realizedPnl,
                            realizedPnlPercent = realizedPnlPercent,
                            unrealizedPnl = unrealizedPnl,
                            unrealizedPnlPercent = unrealizedPnlPercent,
                            size = size,
                            notionalTotal = notionalTotal,
                            valueTotal = valueTotal,
                            initialRiskTotal = initialRiskTotal,
                            adjustedImf = adjustedImf,
                            adjustedMmf = adjustedMmf,
                            leverage = leverage,
                            maxLeverage = maxLeverage,
                            buyingPower = buyingPower,
                            liquidationPrice = liquidationPrice,
                            resources = resources,
                            childSubaccountNumber = childSubaccountNumber,
                            freeCollateral = freeCollateral,
                            marginUsage = marginUsage,
                            quoteBalance = quoteBalance,
                            equity = equity,
                            marginMode = marginMode,
                            marginValue = marginValue,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Account Position not valid" }
            return null
        }

        private fun positionSide(size: TradeStatesWithDoubleValues): TradeStatesWithPositionSides {
            val current = positionSide(size.current)
            val postOrder = positionSide(size.postOrder)
            val postAllOrders = positionSide(size.postAllOrders)
            return TradeStatesWithPositionSides(current, postOrder, postAllOrders)
        }

        private fun positionSide(size: Double?): PositionSide? {
            return if (size != null) {
                if (size > 0) {
                    PositionSide.LONG
                } else if (size < 0) {
                    PositionSide.SHORT
                } else {
                    PositionSide.NONE
                }
            } else {
                null
            }
        }
    }
}

@JsExport
@Serializable
data class SubaccountPositionResources(
    val sideString: TradeStatesWithStringValues,
    val sideStringKey: TradeStatesWithStringValues,
    val indicator: TradeStatesWithStringValues,
) {
    companion object {
        internal fun create(
            existing: SubaccountPositionResources?,
            parser: ParserProtocol,
            data: IMap<*, *>?,
            localizer: LocalizerProtocol? = null,
        ): SubaccountPositionResources? {
            Logger.d { "creating Account Position Resources\n" }
            data?.let {
                val sideStringKey: TradeStatesWithStringValues =
                    TradeStatesWithStringValues.create(
                        existing?.sideStringKey,
                        parser,
                        parser.asMap(data["sideStringKey"]),
                    )
                val sideString: TradeStatesWithStringValues =
                    TradeStatesWithStringValues.create(
                        existing?.sideString,
                        parser,
                        parser.asMap(data["sideStringKey"])?.mapValues {
                            val stringKey = parser.asString(it.value)
                            if (stringKey != null) {
                                localizer?.localize(stringKey)
                            } else {
                                null
                            }
                        },
                    )
                val indicator: TradeStatesWithStringValues =
                    TradeStatesWithStringValues.create(
                        existing?.indicator,
                        parser,
                        parser.asMap(data["indicator"]),
                    )
                return if (existing?.sideStringKey !== sideStringKey ||
                    existing.indicator !== indicator
                ) {
                    SubaccountPositionResources(sideString, sideStringKey, indicator)
                } else {
                    existing
                }
            }
            Logger.d { "Account Position Resources not valid" }
            return null
        }
    }
}
