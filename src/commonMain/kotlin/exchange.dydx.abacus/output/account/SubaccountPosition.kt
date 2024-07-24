package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.output.TradeStatesWithStringValues
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPosition
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ParsingHelper
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountPosition(
    val id: String,
    val assetId: String,
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
            internalState: InternalPerpetualPosition?,
        ): SubaccountPosition? {
            Logger.d { "creating Account Position\n" }
            data?.let {
                val id = positionId ?: parser.asString(data["id"])
                val assetId = if (positionId != null) ParsingHelper.assetId(id) else parser.asString(data["assetId"])
                val resources = internalState?.resources ?: parser.asMap(data["resources"])?.let {
                    SubaccountPositionResources.create(existing?.resources, parser, it)
                }
                if (id != null && assetId != null && resources !== null) {
                    val childSubaccountNumber = internalState?.subaccountNumber
                        ?: parser.asInt(data["childSubaccountNumber"])

                    val marginMode = internalState?.marginMode
                        ?: parser.asString(data["marginMode"])?.let { MarginMode.invoke(it) }

                    val entryPrice = if (internalState?.entryPrice != null) {
                        TradeStatesWithDoubleValues(
                            current = internalState.entryPrice,
                            postOrder = null,
                            postAllOrders = null,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing?.entryPrice,
                            parser,
                            parser.asMap(data["entryPrice"]),
                        )
                    }

                    val exitPrice = internalState?.exitPrice
                        ?: parser.asDouble(data["exitPrice"])
                    val createdAtMilliseconds = internalState?.createdAt?.toEpochMilliseconds()?.toDouble()
                        ?: parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                    val closedAtMilliseconds = internalState?.closedAt?.toEpochMilliseconds()?.toDouble()
                        ?: parser.asDatetime(data["closedAt"])?.toEpochMilliseconds()?.toDouble()
                    val netFunding = internalState?.netFunding ?: parser.asDouble(data["netFunding"])

                    val realizedPnl = if (internalState?.realizedPnl != null) {
                        TradeStatesWithDoubleValues(
                            current = internalState.realizedPnl,
                            postOrder = null,
                            postAllOrders = null,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing?.realizedPnl,
                            parser,
                            parser.asMap(data["realizedPnl"]),
                        )
                    }

                    val realizedPnlPercent = TradeStatesWithDoubleValues.create(
                        existing?.realizedPnlPercent,
                        parser,
                        parser.asMap(data["realizedPnlPercent"]),
                    )

                    val unrealizedPnl = if (internalState?.unrealizedPnl != null) {
                        TradeStatesWithDoubleValues(
                            current = internalState.unrealizedPnl,
                            postOrder = null,
                            postAllOrders = null,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing?.unrealizedPnl,
                            parser,
                            parser.asMap(data["unrealizedPnl"]),
                        )
                    }

                    val unrealizedPnlPercent = TradeStatesWithDoubleValues.create(
                        existing?.unrealizedPnlPercent,
                        parser,
                        parser.asMap(data["unrealizedPnlPercent"]),
                    )

                    val size = if (internalState?.size != null) {
                        TradeStatesWithDoubleValues(
                            current = internalState.size,
                            postOrder = null,
                            postAllOrders = null,
                        )
                    } else {
                        TradeStatesWithDoubleValues.create(
                            existing?.size,
                            parser,
                            parser.asMap(data["size"]),
                        )
                    }

                    val notionalTotal =
                        TradeStatesWithDoubleValues.create(
                            existing?.notionalTotal,
                            parser,
                            parser.asMap(data["notionalTotal"]),
                        )
                    val valueTotal =
                        TradeStatesWithDoubleValues.create(
                            existing?.valueTotal,
                            parser,
                            parser.asMap(data["valueTotal"]),
                        )
                    val initialRiskTotal = TradeStatesWithDoubleValues.create(
                        existing?.initialRiskTotal,
                        parser,
                        parser.asMap(data["initialRiskTotal"]),
                    )
                    val adjustedImf =
                        TradeStatesWithDoubleValues.create(
                            existing?.adjustedImf,
                            parser,
                            parser.asMap(data["adjustedImf"]),
                        )
                    val adjustedMmf =
                        TradeStatesWithDoubleValues.create(
                            existing?.adjustedMmf,
                            parser,
                            parser.asMap(data["adjustedMmf"]),
                        )
                    val leverage =
                        TradeStatesWithDoubleValues.create(
                            existing?.leverage,
                            parser,
                            parser.asMap(data["leverage"]),
                        )
                    val maxLeverage =
                        TradeStatesWithDoubleValues.create(
                            existing?.leverage,
                            parser,
                            parser.asMap(data["maxLeverage"]),
                        )
                    val buyingPower =
                        TradeStatesWithDoubleValues.create(
                            existing?.leverage,
                            parser,
                            parser.asMap(data["buyingPower"]),
                        )
                    val liquidationPrice = TradeStatesWithDoubleValues.create(
                        existing?.liquidationPrice,
                        parser,
                        parser.asMap(data["liquidationPrice"]),
                    )
                    val freeCollateral = TradeStatesWithDoubleValues.create(
                        null,
                        parser,
                        parser.asMap(data["freeCollateral"]),
                    )
                    val marginUsage = TradeStatesWithDoubleValues.create(
                        null,
                        parser,
                        parser.asMap(data["marginUsage"]),
                    )
                    val quoteBalance = TradeStatesWithDoubleValues.create(
                        null,
                        parser,
                        parser.asMap(data["quoteBalance"]),
                    )
                    val equity = TradeStatesWithDoubleValues.create(
                        null,
                        parser,
                        parser.asMap(data["equity"]),
                    )
                    val marginValue = TradeStatesWithDoubleValues.create(
                        null,
                        parser,
                        parser.asMap(data["marginValue"]),
                    )

                    return if (existing?.id != id ||
                        existing.assetId != assetId ||
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
