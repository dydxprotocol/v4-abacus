package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.TradeStatesWithDoubleValues
import exchange.dydx.abacus.output.TradeStatesWithStringValues
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.processor.utils.MarketId
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalPerpetualPosition
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
            positionId: String?, // e.g., "ETH-USD"
            position: InternalPerpetualPosition?,
        ): SubaccountPosition? {
            Logger.d { "creating Account Position\n" }

            val id = positionId ?: error("id not found")
            val displayId = MarketId.getDisplayId(id)
            val assetId = MarketId.getAssetId(id)
            val resources = position?.resources

            if (assetId != null && resources !== null) {
                val childSubaccountNumber = position?.subaccountNumber

                val marginMode = position?.marginMode

                val entryPrice =
                    TradeStatesWithDoubleValues(
                        current = position.entryPrice,
                        postOrder = null,
                        postAllOrders = null,
                    )

                val exitPrice = position?.exitPrice

                val createdAtMilliseconds =
                    position?.createdAt?.toEpochMilliseconds()?.toDouble()
                val closedAtMilliseconds = position?.closedAt?.toEpochMilliseconds()?.toDouble()
                val netFunding = position?.netFunding

                val realizedPnl =
                    TradeStatesWithDoubleValues(
                        current = position.realizedPnl,
                        postOrder = null,
                        postAllOrders = null,
                    )

                val realizedPnlPercent =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.realizedPnlPercent,
                        postOrder = position.calculated[CalculationPeriod.post]?.realizedPnlPercent,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.realizedPnlPercent,
                    )

                val unrealizedPnl =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.unrealizedPnl,
                        postOrder = position.calculated[CalculationPeriod.post]?.unrealizedPnl,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.unrealizedPnl,
                    )

                val unrealizedPnlPercent =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.unrealizedPnlPercent,
                        postOrder = position.calculated[CalculationPeriod.post]?.unrealizedPnlPercent,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.unrealizedPnlPercent,
                    )

                val size =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.size,
                        postOrder = position.calculated[CalculationPeriod.post]?.size,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.size,
                    )

                val notionalTotal =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.notionalTotal,
                        postOrder = position.calculated[CalculationPeriod.post]?.notionalTotal,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.notionalTotal,
                    )

                val valueTotal =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.valueTotal,
                        postOrder = position.calculated[CalculationPeriod.post]?.valueTotal,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.valueTotal,
                    )

                val initialRiskTotal =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.initialRiskTotal,
                        postOrder = position.calculated[CalculationPeriod.post]?.initialRiskTotal,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.initialRiskTotal,
                    )

                val adjustedImf =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.adjustedImf,
                        postOrder = position.calculated[CalculationPeriod.post]?.adjustedImf,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.adjustedImf,
                    )

                val adjustedMmf =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.adjustedMmf,
                        postOrder = position.calculated[CalculationPeriod.post]?.adjustedMmf,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.adjustedMmf,
                    )

                val leverage =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.leverage,
                        postOrder = position.calculated[CalculationPeriod.post]?.leverage,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.leverage,
                    )

                val maxLeverage =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.maxLeverage,
                        postOrder = position.calculated[CalculationPeriod.post]?.maxLeverage,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.maxLeverage,
                    )

                val buyingPower =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.buyingPower,
                        postOrder = position.calculated[CalculationPeriod.post]?.buyingPower,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.buyingPower,
                    )

                val liquidationPrice =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.liquidationPrice,
                        postOrder = position.calculated[CalculationPeriod.post]?.liquidationPrice,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.liquidationPrice,
                    )

                val freeCollateral =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.freeCollateral,
                        postOrder = position.calculated[CalculationPeriod.post]?.freeCollateral,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.freeCollateral,
                    )

                val marginUsage =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.marginUsage,
                        postOrder = position.calculated[CalculationPeriod.post]?.marginUsage,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.marginUsage,
                    )

                val quoteBalance =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.quoteBalance,
                        postOrder = position.calculated[CalculationPeriod.post]?.quoteBalance,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.quoteBalance,
                    )

                val equity =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.equity,
                        postOrder = position.calculated[CalculationPeriod.post]?.equity,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.equity,
                    )

                val marginValue =
                    TradeStatesWithDoubleValues(
                        current = position.calculated[CalculationPeriod.current]?.marginValue,
                        postOrder = position.calculated[CalculationPeriod.post]?.marginValue,
                        postAllOrders = position.calculated[CalculationPeriod.settled]?.marginValue,
                    )

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
