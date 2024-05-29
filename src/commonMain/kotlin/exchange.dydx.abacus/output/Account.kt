package exchange.dydx.abacus.output

import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderTimeInForce
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.processor.base.ComparisonOrder
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.TokenInfo
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_DURATION
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.typedSafeSet
import kollections.JsExport
import kollections.iMapOf
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kollections.toIList
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days

@JsExport
@Serializable
data class SubaccountHistoricalPNL(
    val equity: Double,
    val totalPnl: Double,
    val netTransfers: Double,
    val createdAtMilliseconds: Double,
) {
    companion object {
        internal fun create(
            existing: SubaccountHistoricalPNL?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): SubaccountHistoricalPNL? {
            Logger.d { "creating Account Historical PNL\n" }
            data?.let {
                val equity = parser.asDouble(data["equity"])
                val totalPnl = parser.asDouble(data["totalPnl"])
                val netTransfers = parser.asDouble(data["netTransfers"])
                val createdAtMilliseconds =
                    parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                if (equity != null && totalPnl != null && netTransfers != null && createdAtMilliseconds != null) {
                    return if (existing?.equity != equity ||
                        existing.totalPnl != totalPnl ||
                        existing.netTransfers != netTransfers ||
                        existing.createdAtMilliseconds != createdAtMilliseconds
                    ) {
                        SubaccountHistoricalPNL(
                            equity,
                            totalPnl,
                            netTransfers,
                            createdAtMilliseconds,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Account Historical PNL not valid" }
            return null
        }

        fun create(
            existing: IList<SubaccountHistoricalPNL>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
            startTime: Instant,
        ): IList<SubaccountHistoricalPNL>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time2 = parser.asDatetime(itemData["createdAt"])
                if (time2 != null && time2 >= startTime) {
                    val time1 = (obj as SubaccountHistoricalPNL).createdAtMilliseconds
                    val time2MS = time2.toEpochMilliseconds().toDouble()
                    ParsingHelper.compare(time1, time2MS ?: 0.0, true)
                } else {
                    null
                }
            }, { _, obj, itemData ->
                obj ?: SubaccountHistoricalPNL.create(
                    null,
                    parser,
                    parser.asMap(itemData),
                )
            }, true, { item ->
                val ms = (item as SubaccountHistoricalPNL).createdAtMilliseconds.toDouble()
                val createdAt = Instant.fromEpochMilliseconds(ms.toLong())
                createdAt >= startTime
            }, { itemData ->
                val createdAt = parser.asDatetime(itemData["createdAt"])
                createdAt != null && createdAt >= startTime
            })?.toIList()
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

@JsExport
@Serializable
enum class PositionSide(val rawValue: String) {
    LONG("LONG"),
    SHORT("SHORT"),
    NONE("NONE");

    companion object {
        operator fun invoke(rawValue: String) =
            PositionSide.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class TradeStatesWithPositionSides(
    val current: PositionSide?,
    val postOrder: PositionSide?,
    val postAllOrders: PositionSide?,
) {
    companion object {
        internal fun create(
            existing: TradeStatesWithPositionSides?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): TradeStatesWithPositionSides {
            val currentString = parser.asString(data?.get("current"))
            val postOrderString = parser.asString(data?.get("postOrder"))
            val postAllOrdersString = parser.asString(data?.get("postAllOrders"))
            val current = if (currentString != null) PositionSide.invoke(currentString) else null
            val postOrder =
                if (postOrderString != null) PositionSide.invoke(postOrderString) else null
            val postAllOrders =
                if (postAllOrdersString != null) PositionSide.invoke(postAllOrdersString) else null
            return if (existing == null ||
                existing.current !== current ||
                existing.postOrder !== postOrder ||
                existing.postAllOrders !== postAllOrders
            ) {
                TradeStatesWithPositionSides(current, postOrder, postAllOrders)
            } else {
                existing
            }
        }
    }

    internal fun asTradingStates(): TradingStates<PositionSide> {
        return object : TradingStates<PositionSide> {
            override val current: PositionSide?
                get() = this@TradeStatesWithPositionSides.current
            override val postOrder: PositionSide?
                get() = this@TradeStatesWithPositionSides.postOrder
            override val postAllOrders: PositionSide?
                get() = this@TradeStatesWithPositionSides.postAllOrders
        }
    }
}

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
) {
    companion object {
        internal fun create(
            existing: SubaccountPosition?,
            parser: ParserProtocol,
            data: Map<String, Any>?,
        ): SubaccountPosition? {
            Logger.d { "creating Account Position\n" }
            data?.let {
                val id = parser.asString(data["id"])
                val assetId = parser.asString(data["assetId"])
                val resources = parser.asMap(data["resources"])?.let {
                    SubaccountPositionResources.create(existing?.resources, parser, it)
                }
                if (id != null && assetId != null && resources !== null) {
                    val entryPrice =
                        TradeStatesWithDoubleValues.create(
                            existing?.entryPrice,
                            parser,
                            parser.asMap(data["entryPrice"]),
                        )
                    val exitPrice = parser.asDouble(data["exitPrice"])
                    val createdAtMilliseconds =
                        parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                    val closedAtMilliseconds =
                        parser.asDatetime(data["closedAt"])?.toEpochMilliseconds()?.toDouble()
                    val netFunding = parser.asDouble(data["netFunding"])
                    val realizedPnl =
                        TradeStatesWithDoubleValues.create(
                            existing?.realizedPnl,
                            parser,
                            parser.asMap(data["realizedPnl"]),
                        )
                    val realizedPnlPercent = TradeStatesWithDoubleValues.create(
                        existing?.realizedPnlPercent,
                        parser,
                        parser.asMap(data["realizedPnlPercent"]),
                    )
                    val unrealizedPnl =
                        TradeStatesWithDoubleValues.create(
                            existing?.unrealizedPnl,
                            parser,
                            parser.asMap(data["unrealizedPnl"]),
                        )
                    val unrealizedPnlPercent = TradeStatesWithDoubleValues.create(
                        existing?.unrealizedPnlPercent,
                        parser,
                        parser.asMap(data["unrealizedPnlPercent"]),
                    )
                    val size =
                        TradeStatesWithDoubleValues.create(
                            existing?.size,
                            parser,
                            parser.asMap(data["size"]),
                        )
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
                    val childSubaccountNumber = parser.asInt(data["childSubaccountNumber"])
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
                        existing.equity !== equity
                    ) {
                        val side = positionSide(size)
                        SubaccountPosition(
                            id,
                            assetId,
                            side,
                            entryPrice,
                            exitPrice,
                            createdAtMilliseconds,
                            closedAtMilliseconds,
                            netFunding,
                            realizedPnl,
                            realizedPnlPercent,
                            unrealizedPnl,
                            unrealizedPnlPercent,
                            size,
                            notionalTotal,
                            valueTotal,
                            initialRiskTotal,
                            adjustedImf,
                            adjustedMmf,
                            leverage,
                            maxLeverage,
                            buyingPower,
                            liquidationPrice,
                            resources,
                            childSubaccountNumber,
                            freeCollateral,
                            marginUsage,
                            quoteBalance,
                            equity,
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
data class SubaccountPendingPosition(
    val assetId: String,
    val marketId: String,
    val firstOrderId: String,
    val orderCount: Int,
    val freeCollateral: TradeStatesWithDoubleValues?,
    val quoteBalance: TradeStatesWithDoubleValues?, // available for isolated market position
    val equity: TradeStatesWithDoubleValues?, // available for isolated market position
) {
    companion object {
        internal fun create(
            existing: SubaccountPendingPosition?,
            parser: ParserProtocol,
            data: Map<String, Any>?,
        ): SubaccountPendingPosition? {
            Logger.d { "creating Account Pending Position\n" }
            data?.let {
                val assetId = parser.asString(data["assetId"]) ?: return null
                val marketId = parser.asString(data["marketId"]) ?: return null
                val firstOrderId = parser.asString(data["firstOrderId"]) ?: return null
                val orderCount = parser.asInt(data["orderCount"]) ?: return null
                val freeCollateral = TradeStatesWithDoubleValues.create(
                    null,
                    parser,
                    parser.asMap(data["freeCollateral"]),
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

                return if (existing?.assetId != assetId ||
                    existing.marketId != marketId ||
                    existing.firstOrderId != firstOrderId ||
                    existing.orderCount != orderCount ||
                    existing.freeCollateral !== freeCollateral ||
                    existing.quoteBalance !== quoteBalance ||
                    existing.equity !== equity
                ) {
                    SubaccountPendingPosition(
                        assetId,
                        marketId,
                        firstOrderId,
                        orderCount,
                        freeCollateral,
                        quoteBalance,
                        equity,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Account Pending Position not valid" }
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

/*
typeStringKey and statusStringKey are set to optional, in case
BE returns new enum values which Abacus doesn't recognize
*/
@JsExport
@Serializable
data class SubaccountOrderResources(
    val sideString: String?,
    val typeString: String?,
    val statusString: String?,
    val timeInForceString: String?,
    val sideStringKey: String,
    val typeStringKey: String?,
    val statusStringKey: String?,
    val timeInForceStringKey: String?,
) {
    companion object {
        internal fun create(
            existing: SubaccountOrderResources?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): SubaccountOrderResources? {
            Logger.d { "creating Account Order Resources\n" }

            data?.let {
                val sideStringKey = parser.asString(data["sideStringKey"])
                val typeStringKey = parser.asString(data["typeStringKey"])
                val statusStringKey = parser.asString(data["statusStringKey"])
                val timeInForceStringKey = parser.asString(data["timeInForceStringKey"])
                if (sideStringKey != null) {
                    return if (existing?.sideStringKey != sideStringKey ||
                        existing.typeStringKey != typeStringKey ||
                        existing.statusStringKey != statusStringKey ||
                        existing.timeInForceStringKey != timeInForceStringKey
                    ) {
                        val sideString = localizer?.localize(sideStringKey)
                        val typeString =
                            if (typeStringKey != null) localizer?.localize(typeStringKey) else null
                        val statusString =
                            if (statusStringKey != null) localizer?.localize(statusStringKey) else null
                        val timeInForceString =
                            if (timeInForceStringKey != null) {
                                localizer?.localize(
                                    timeInForceStringKey,
                                )
                            } else {
                                null
                            }
                        SubaccountOrderResources(
                            sideString,
                            typeString,
                            statusString,
                            timeInForceString,
                            sideStringKey,
                            typeStringKey,
                            statusStringKey,
                            timeInForceStringKey,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Account Order Resources not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class SubaccountOrder(
    val subaccountNumber: Int?,
    val id: String,
    val clientId: Int?,
    val type: OrderType,
    val side: OrderSide,
    val status: OrderStatus,
    val timeInForce: OrderTimeInForce?,
    val marketId: String,
    val clobPairId: Int?,
    val orderFlags: Int?,
    val price: Double,
    val triggerPrice: Double?,
    val trailingPercent: Double?,
    val size: Double,
    val remainingSize: Double?,
    val totalFilled: Double?,
    val goodTilBlock: Int?,
    val goodTilBlockTime: Int?,
    val createdAtHeight: Int?,
    val createdAtMilliseconds: Double?,
    val unfillableAtMilliseconds: Double?,
    val expiresAtMilliseconds: Double?,
    val updatedAtMilliseconds: Double?,
    val postOnly: Boolean,
    val reduceOnly: Boolean,
    val cancelReason: String?,
    val resources: SubaccountOrderResources,
) {
    companion object {
        internal fun create(
            existing: SubaccountOrder?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): SubaccountOrder? {
            Logger.d { "creating Account Order\n" }
            data?.let {
                // TODO: Remove default to 0 for subaccountNumber once new indexer response is consumed. Prevents breaking change
                val subaccountNumber = parser.asInt(data["subaccountNumber"]) ?: 0
                val id = parser.asString(data["id"])
                val clientId = parser.asInt(data["clientId"])
                val marketId = parser.asString(data["marketId"])
                val clobPairId = parser.asInt(data["clobPairId"])
                val orderFlags = parser.asInt(data["orderFlags"])
                val typeString = parser.asString(data["type"])
                val type = if (typeString != null) OrderType.invoke(typeString) else null
                val sideString = parser.asString(data["side"])
                val side = if (sideString != null) OrderSide.invoke(sideString) else null
                val statusString = parser.asString(data["status"])
                val status = if (statusString != null) OrderStatus.invoke(statusString) else null
                val timeInForceString = parser.asString(data["timeInForce"])
                val timeInForce =
                    if (timeInForceString != null) OrderTimeInForce.invoke(timeInForceString) else null
                val price = parser.asDouble(data["price"])
                val size = parser.asDouble(data["size"])
                val createdAtMilliseconds =
                    parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                val resources = parser.asMap(data["resources"])?.let {
                    SubaccountOrderResources.create(existing?.resources, parser, it, localizer)
                }
                if (id != null && marketId != null && type != null && side != null && status != null && price != null && size != null &&
                    resources != null
                ) {
                    val triggerPrice = parser.asDouble(data["triggerPrice"])
                    val trailingPercent = parser.asDouble(data["trailingPercent"])
                    val remainingSize = parser.asDouble(data["remainingSize"])
                    val totalFilled = parser.asDouble(data["totalFilled"])

                    val unfillableAtMilliseconds =
                        parser.asDatetime(data["unfillableAt"])?.toEpochMilliseconds()?.toDouble()
                    val goodTilBlock = parser.asInt(data["goodTilBlock"]);
                    val goodTilBlockTime =
                        parser.asDatetime(data["goodTilBlockTime"])?.epochSeconds?.toInt();
                    val createdAtHeight = parser.asInt(data["createdAtHeight"]);
                    val updatedAtMilliseconds =
                        parser.asDatetime(data["updatedAt"])?.toEpochMilliseconds()?.toDouble();
                    val expiresAtMilliseconds =
                        parser.asDatetime(data["expiresAt"] ?: data["goodTilBlockTime"])
                            ?.toEpochMilliseconds()?.toDouble()
                    val postOnly = parser.asBool(data["postOnly"]) ?: false
                    val reduceOnly = parser.asBool(data["reduceOnly"]) ?: false
                    val cancelReason = parser.asString(data["cancelReason"])

                    return if (
                        existing?.subaccountNumber != subaccountNumber ||
                        existing.id != id ||
                        existing.clientId != clientId ||
                        existing.type !== type ||
                        existing.side !== side ||
                        existing.status !== status ||
                        existing.timeInForce !== timeInForce ||
                        existing.marketId != marketId ||
                        existing.clobPairId != clobPairId ||
                        existing.orderFlags != orderFlags ||
                        existing.price != price ||
                        existing.triggerPrice != triggerPrice ||
                        existing.trailingPercent != trailingPercent ||
                        existing.size != size ||
                        existing.remainingSize != remainingSize ||
                        existing.totalFilled != totalFilled ||
                        existing.goodTilBlock != goodTilBlock ||
                        existing.goodTilBlockTime != goodTilBlockTime ||
                        existing.createdAtMilliseconds != createdAtMilliseconds ||
                        existing.unfillableAtMilliseconds != unfillableAtMilliseconds ||
                        existing.expiresAtMilliseconds != expiresAtMilliseconds ||
                        existing.updatedAtMilliseconds != updatedAtMilliseconds ||
                        existing.postOnly != postOnly ||
                        existing.reduceOnly != reduceOnly ||
                        existing.cancelReason != cancelReason ||
                        existing.resources !== resources
                    ) {
                        SubaccountOrder(
                            subaccountNumber,
                            id,
                            clientId,
                            type,
                            side,
                            status,
                            timeInForce,
                            marketId,
                            clobPairId,
                            orderFlags,
                            price,
                            triggerPrice,
                            trailingPercent,
                            size,
                            remainingSize,
                            totalFilled,
                            goodTilBlock,
                            goodTilBlockTime,
                            createdAtHeight,
                            createdAtMilliseconds,
                            unfillableAtMilliseconds,
                            expiresAtMilliseconds,
                            updatedAtMilliseconds,
                            postOnly,
                            reduceOnly,
                            cancelReason,
                            resources,
                        )
                    } else {
                        existing
                    }
                } else {
                    Logger.d { "Account Order not valid" }
                }
            }
            return null
        }
    }
}

/*
typeStringKey, statusStringKey, iconLocal and indicator are set to optional, in case
BE returns new transfer type enum values or status enum values which Abacus doesn't recognize
*/
@JsExport
@Serializable
data class SubaccountFillResources(
    val sideString: String?,
    val liquidityString: String?,
    val typeString: String?,
    val sideStringKey: String?,
    val liquidityStringKey: String?,
    val typeStringKey: String?,
    val iconLocal: String?,
) {
    companion object {
        internal fun create(
            existing: SubaccountFillResources?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?
        ): SubaccountFillResources? {
            Logger.d { "creating Account Fill Resources\n" }

            data?.let {
                val sideStringKey = parser.asString(data["sideStringKey"])
                val liquidityStringKey = parser.asString(data["liquidityStringKey"])
                val typeStringKey = parser.asString(data["typeStringKey"])
                val iconLocal = parser.asString(data["iconLocal"])
                return if (
                    existing?.sideStringKey != sideStringKey ||
                    existing?.liquidityStringKey != liquidityStringKey ||
                    existing?.typeStringKey != typeStringKey ||
                    existing?.iconLocal != iconLocal
                ) {
                    val sideString =
                        if (sideStringKey != null) localizer?.localize(sideStringKey) else null
                    val liquidityString =
                        if (liquidityStringKey != null) localizer?.localize(liquidityStringKey) else null
                    val typeString =
                        if (typeStringKey != null) localizer?.localize(typeStringKey) else null
                    SubaccountFillResources(
                        sideString,
                        liquidityString,
                        typeString,
                        sideStringKey,
                        liquidityStringKey,
                        typeStringKey,
                        iconLocal,
                    )
                } else {
                    Logger.d { "Account Fill Resources not valid" }
                    existing
                }
            }

            return null
        }
    }
}

@JsExport
@Serializable
enum class FillLiquidity(val rawValue: String) {
    maker("MAKER"),
    taker("TAKER");

    companion object {
        operator fun invoke(rawValue: String) =
            FillLiquidity.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class SubaccountFill(
    val id: String,
    val marketId: String,
    val orderId: String?,
    val side: OrderSide,
    val type: OrderType,
    val liquidity: FillLiquidity,
    val price: Double,
    val size: Double,
    val fee: Double?,
    val createdAtMilliseconds: Double,
    val resources: SubaccountFillResources,
) {
    companion object {
        internal fun create(
            existing: SubaccountFill?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): SubaccountFill? {
            Logger.d { "creating Account Fill\n" }
            data?.let {
                val id = parser.asString(data["id"])
                val marketId = parser.asString(data["marketId"])
                val orderId = parser.asString(data["orderId"])
                val sideString = parser.asString(data["side"])
                val side = if (sideString != null) OrderSide.invoke(sideString) else null
                val liquidityString = parser.asString(data["liquidity"])
                val liquidity =
                    if (liquidityString != null) FillLiquidity.invoke(liquidityString) else null
                val typeString = parser.asString(data["type"])
                val type = if (typeString != null) OrderType.invoke(typeString) else null
                val price = parser.asDouble(data["price"])
                val size = parser.asDouble(data["size"])
                val createdAtMilliseconds =
                    parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                val resources = parser.asMap(data["resources"])?.let {
                    SubaccountFillResources.create(existing?.resources, parser, it, localizer)
                }
                return if (id != null && marketId != null && side != null && type != null && liquidity != null &&
                    price != null && size != null && createdAtMilliseconds != null && resources != null
                ) {
                    val fee = parser.asDouble(data["fee"])
                    if (existing?.id != id ||
                        existing.marketId != marketId ||
                        existing.orderId != orderId ||
                        existing.side !== side ||
                        existing.type !== type ||
                        existing.liquidity !== liquidity ||
                        existing.price != price ||
                        existing.fee != fee ||
                        existing.createdAtMilliseconds != createdAtMilliseconds ||
                        existing.resources !== resources
                    ) {
                        SubaccountFill(
                            id,
                            marketId,
                            orderId,
                            side,
                            type,
                            liquidity,
                            price,
                            size,
                            fee,
                            createdAtMilliseconds,
                            resources,
                        )
                    } else {
                        existing
                    }
                } else {
                    Logger.d { "Account Fill not valid" }
                    null
                }
            }
            return null
        }

        fun create(
            existing: IList<SubaccountFill>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
            localizer: LocalizerProtocol?,
        ): IList<SubaccountFill>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountFill).createdAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()
                        ?.toDouble()
                val id1 = obj.id
                val id2 = parser.asString(itemData["id"])
                if (id1 == id2) {
                    ParsingHelper.compare(time1, time2 ?: 0.0, true)
                } else {
                    ParsingHelper.compare(id1, id2, true)
                }
            }, { _, obj, itemData ->
                obj ?: SubaccountFill.create(null, parser, parser.asMap(itemData), localizer)
            }, true)?.toIList()
        }
    }
}

/*
typeStringKey, statusStringKey, iconLocal and indicator are set to optional, in case
BE returns new transfer type enum values or status enum values which Abacus doesn't recognize
*/
@JsExport
@Serializable
data class SubaccountTransferResources(
    val typeString: String?,
    val statusString: String?,
    val typeStringKey: String?,
    val blockExplorerUrl: String?,
    val statusStringKey: String?,
    val iconLocal: String?,
    val indicator: String?,
) {
    companion object {
        internal fun create(
            existing: SubaccountTransferResources?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol? = null,
        ): SubaccountTransferResources? {
            Logger.d { "creating Account Transfer Resources\n" }

            data?.let {
                val typeStringKey = parser.asString(data["typeStringKey"])
                val blockExplorerUrl = parser.asString(data["blockExplorerUrl"])
                val statusStringKey = parser.asString(data["statusStringKey"])
                val iconLocal = parser.asString(data["iconLocal"])
                val indicator = parser.asString(data["indicator"])
                return if (existing?.typeStringKey != typeStringKey ||
                    existing?.blockExplorerUrl != blockExplorerUrl ||
                    existing?.statusStringKey != statusStringKey ||
                    existing?.iconLocal != iconLocal ||
                    existing?.indicator != indicator
                ) {
                    val typeString =
                        if (typeStringKey != null) localizer?.localize(typeStringKey) else null
                    val statusString =
                        if (statusStringKey != null) localizer?.localize(statusStringKey) else null
                    SubaccountTransferResources(
                        typeString,
                        statusString,
                        typeStringKey,
                        blockExplorerUrl,
                        statusStringKey,
                        iconLocal,
                        indicator,
                    )
                } else {
                    existing
                }
            }

            Logger.d { "Account Transfer Resources not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
enum class TransferRecordType(val rawValue: String) {
    DEPOSIT("DEPOSIT"),
    WITHDRAW("WITHDRAWAL"),
    TRANSFER_IN("TRANSFER_IN"),
    TRANSFER_OUT("TRANSFER_OUT");

    companion object {
        operator fun invoke(rawValue: String?) =
            TransferRecordType.values().firstOrNull { it.rawValue == rawValue }
    }
}

/*
debit and credit info are set depending on the type of transfer
*/
@JsExport
@Serializable
data class SubaccountTransfer(
    val id: String,
    val type: TransferRecordType,
    val asset: String?,
    val amount: Double?,
    val updatedAtBlock: Int?,
    val updatedAtMilliseconds: Double,
    val fromAddress: String?,
    val toAddress: String?,
    val transactionHash: String?,
    val resources: SubaccountTransferResources,
) {
    companion object {
        internal fun create(
            existing: SubaccountTransfer?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): SubaccountTransfer? {
            Logger.d { "creating Account Transfer\n" }
            data?.let {
                val id = parser.asString(data["id"])
                val updatedAt =
                    parser.asDatetime(data["confirmedAt"]) ?: parser.asDatetime(data["createdAt"])
                val updatedAtMilliseconds = updatedAt?.toEpochMilliseconds()?.toDouble()
                val resources = parser.asMap(data["resources"])?.let {
                    SubaccountTransferResources.create(existing?.resources, parser, it)
                }
                if (id != null && updatedAtMilliseconds != null && resources != null) {
                    val type =
                        TransferRecordType.invoke(parser.asString(data["type"])) ?: return null
                    val asset = parser.asString(data["asset"])
                    val amount = parser.asDouble(data["amount"])
                    val fromAddress = parser.asString(data["fromAddress"])
                    val toAddress = parser.asString(data["toAddress"])
                    val updatedAtBlock = parser.asInt(data["updatedAtBlock"])
                    val transactionHash = parser.asString(data["transactionHash"])
                    return if (existing?.id != id ||
                        existing.type !== type ||
                        existing.asset != asset ||
                        existing.amount != amount ||
                        existing.updatedAtBlock != updatedAtBlock ||
                        existing.updatedAtMilliseconds != updatedAtMilliseconds ||
                        existing.fromAddress != fromAddress ||
                        existing.toAddress != toAddress ||
                        existing.transactionHash != transactionHash ||
                        existing.resources !== resources
                    ) {
                        SubaccountTransfer(
                            id,
                            type,
                            asset,
                            amount,
                            updatedAtBlock,
                            updatedAtMilliseconds,
                            fromAddress,
                            toAddress,
                            transactionHash,
                            resources,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Account Transfer not valid" }
            return null
        }

        fun create(
            existing: IList<SubaccountTransfer>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
        ): IList<SubaccountTransfer>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountTransfer).updatedAtMilliseconds
                val time2 =
                    (
                        parser.asDatetime(itemData["confirmedAt"])
                            ?: parser.asDatetime(itemData["createdAt"])
                        )?.toEpochMilliseconds()
                        ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                obj ?: SubaccountTransfer.create(
                    null,
                    parser,
                    parser.asMap(itemData),
                )
            })?.toIList()
        }
    }
}

@JsExport
@Serializable
data class SubaccountFundingPayment(
    val marketId: String,
    val payment: Double,
    val rate: Double,
    val positionSize: Double,
    val price: Double?,
    val effectiveAtMilliSeconds: Double,
) {
    companion object {
        internal fun create(
            existing: SubaccountFundingPayment?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): SubaccountFundingPayment? {
            Logger.d { "creating Account Funding Payment\n" }

            data?.let {
                val marketId = parser.asString(data["marketId"])
                val payment = parser.asDouble(data["payment"])
                val rate = parser.asDouble(data["rate"])
                val positionSize = parser.asDouble(data["positionSize"])
                val price = parser.asDouble(data["price"])
                val effectiveAtMilliSeconds =
                    parser.asDatetime(data["effectiveAt"])?.toEpochMilliseconds()?.toDouble()
                if (marketId != null && payment != null && rate != null && positionSize != null && effectiveAtMilliSeconds != null) {
                    return if (existing?.marketId != marketId ||
                        existing.payment != payment ||
                        existing.rate != rate ||
                        existing.positionSize != positionSize ||
                        existing.price != price ||
                        existing.effectiveAtMilliSeconds != effectiveAtMilliSeconds
                    ) {
                        SubaccountFundingPayment(
                            marketId,
                            payment,
                            rate,
                            positionSize,
                            price,
                            effectiveAtMilliSeconds,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Account Funding Payment not valid" }
            return null
        }

        fun create(
            existing: IList<SubaccountFundingPayment>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
        ): IList<SubaccountFundingPayment>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountFundingPayment).effectiveAtMilliSeconds
                val time2 =
                    parser.asDatetime(itemData["effectiveAt"])?.toEpochMilliseconds()
                        ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, true)
            }, { _, obj, itemData ->
                obj ?: SubaccountFundingPayment.create(
                    null,
                    parser,
                    parser.asMap(itemData),
                )
            })?.toIList()
        }
    }
}

/*
ethereumeAddress is passed in from client. All other fields
are filled when socket v3_accounts channel is subscribed
*/
@JsExport
@Serializable
data class Subaccount(
    val subaccountNumber: Int,
    val positionId: String?,
    val pnlTotal: Double?,
    val pnl24h: Double?,
    val pnl24hPercent: Double?,
    val quoteBalance: TradeStatesWithDoubleValues?,
    val notionalTotal: TradeStatesWithDoubleValues?,
    val valueTotal: TradeStatesWithDoubleValues?,
    val initialRiskTotal: TradeStatesWithDoubleValues?,
    val adjustedImf: TradeStatesWithDoubleValues?,
    val equity: TradeStatesWithDoubleValues?,
    val freeCollateral: TradeStatesWithDoubleValues?,
    val leverage: TradeStatesWithDoubleValues?,
    val marginUsage: TradeStatesWithDoubleValues?,
    val buyingPower: TradeStatesWithDoubleValues?,
    val openPositions: IList<SubaccountPosition>?,
    val pendingPositions: IList<SubaccountPendingPosition>?,
    val orders: IList<SubaccountOrder>?,
    val marginEnabled: Boolean?,
) {
    companion object {
        internal fun create(
            existing: Subaccount?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): Subaccount? {
            Logger.d { "creating Account\n" }

            data?.let {
                val ethereumeAddress = parser.asString(data["ethereumeAddress"])
                val positionId = parser.asString(data["positionId"])
                val pnlTotal = parser.asDouble(data["pnlTotal"])
                val pnl24h = parser.asDouble(data["pnl24h"])
                val pnl24hPercent = parser.asDouble(data["pnl24hPercent"])
                /*
                val historicalPnl = (data["historicalPnl"] as? List<*>)?.let {
                    val historicalPnl = iMutableListOf<AccountHistoricalPNL>()
                    for (i in it.indices) {
                        val itemData = it[i]
                        AccountHistoricalPNL.create(
                            existing?.historicalPnl?.a?.getOrNull(i),
                            parser, itemData as? IMap<*, *>
                        )?.let {
                            historicalPnl.add(it)
                        }
                    }
                    AccountHistoricalPNLs.fromArray(historicalPnl)
                }
                 */
                val subaccountNumber = parser.asInt(data["subaccountNumber"]) ?: 0
                val quoteBalance =
                    TradeStatesWithDoubleValues.create(
                        existing?.quoteBalance,
                        parser,
                        parser.asMap(data["quoteBalance"]),
                    )
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
                val initialRiskTotal =
                    TradeStatesWithDoubleValues.create(
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
                val equity =
                    TradeStatesWithDoubleValues.create(
                        existing?.equity,
                        parser,
                        parser.asMap(data["equity"]),
                    )
                val freeCollateral =
                    TradeStatesWithDoubleValues.create(
                        existing?.freeCollateral,
                        parser,
                        parser.asMap(data["freeCollateral"]),
                    )
                val leverage =
                    TradeStatesWithDoubleValues.create(
                        existing?.leverage,
                        parser,
                        parser.asMap(data["leverage"]),
                    )
                val marginUsage =
                    TradeStatesWithDoubleValues.create(
                        existing?.marginUsage,
                        parser,
                        parser.asMap(data["marginUsage"]),
                    )
                val buyingPower =
                    TradeStatesWithDoubleValues.create(
                        existing?.buyingPower,
                        parser,
                        parser.asMap(data["buyingPower"]),
                    )

                val openPositions = openPositions(
                    existing?.openPositions,
                    parser,
                    parser.asMap(data["openPositions"]),
                )
                val pendingPositions = pendingPositions(
                    existing?.pendingPositions,
                    parser,
                    parser.asList(data["pendingPositions"]),
                )
                val orders =
                    orders(parser, existing?.orders, parser.asMap(data["orders"]), localizer)

                /*
                val transfers = AccountTransfers.fromArray(
                    transfers(parser, existing?.transfers?.a, data["transfers"] as? List<*>)
                )
                val fundingPayments = AccountFundingPayments.fromArray(
                    fundingPayments(
                        parser,
                        existing?.fundingPayments?.a,
                        data["fundingPayments"] as? List<*>
                    ))

                 */
                val marginEnabled = parser.asBool(data["marginEnabled"]) ?: true

                return if (existing?.subaccountNumber != subaccountNumber ||
                    existing.positionId != positionId ||
                    existing.pnlTotal != pnlTotal ||
                    existing.pnl24h != pnl24h ||
                    existing.pnl24hPercent != pnl24hPercent ||
                    existing.quoteBalance !== quoteBalance ||
                    existing.notionalTotal !== notionalTotal ||
                    existing.valueTotal !== valueTotal ||
                    existing.initialRiskTotal !== initialRiskTotal ||
                    existing.adjustedImf !== adjustedImf ||
                    existing.equity !== equity ||
                    existing.freeCollateral !== freeCollateral ||
                    existing.leverage !== leverage ||
                    existing.marginUsage !== marginUsage ||
                    existing.buyingPower !== buyingPower ||
                    existing.openPositions != openPositions ||
                    existing.pendingPositions != pendingPositions ||
                    existing.orders != orders ||
                    existing.marginEnabled != marginEnabled
                ) {
                    Subaccount(
                        subaccountNumber,
                        positionId,
                        pnlTotal,
                        pnl24h,
                        pnl24hPercent,
                        quoteBalance,
                        notionalTotal,
                        valueTotal,
                        initialRiskTotal,
                        adjustedImf,
                        equity,
                        freeCollateral,
                        leverage,
                        marginUsage,
                        buyingPower,
                        openPositions,
                        pendingPositions,
                        orders,
                        marginEnabled,
                    )
                } else {
                    existing
                }
            }
            return null
        }

        private fun openPositions(
            existing: IList<SubaccountPosition>?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): IList<SubaccountPosition>? {
            return ParsingHelper.transform(parser, existing, data, {
                (it as SubaccountPosition).id
            }, { _, _ ->
                // not worth the optimization
                true
            }, { obj1, obj2 ->
                val time1 = (obj1 as SubaccountPosition).createdAtMilliseconds
                val time2 = (obj2 as SubaccountPosition).createdAtMilliseconds
                ParsingHelper.compare(time1 ?: 0.0, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                parser.asMap(itemData)?.let {
                    SubaccountPosition.create(obj as? SubaccountPosition, parser, it)
                }
            })?.toIList()
        }

        private fun pendingPositions(
            existing: IList<SubaccountPendingPosition>?,
            parser: ParserProtocol,
            data: List<*>?,
        ): IList<SubaccountPendingPosition>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val orderId1 = (obj as SubaccountPendingPosition).firstOrderId
                val orderId2 =
                    parser.asString(itemData["firstOrderId"])
                ParsingHelper.compare(orderId1, orderId2, false)
            }, { _, obj, itemData ->
                SubaccountPendingPosition.create(
                    obj as? SubaccountPendingPosition,
                    parser,
                    parser.asMap(itemData),
                )
            }, true)?.toIList()
        }

        private fun orders(
            parser: ParserProtocol,
            existing: IList<SubaccountOrder>?,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): IList<SubaccountOrder>? {
            val orders = ParsingHelper.transform(parser, existing, data, {
                (it as SubaccountOrder).id
            }, { _, _ ->
                // not worth the optimization
                true
            }, { obj1, obj2 ->
                val block1 = block(obj1 as SubaccountOrder)
                val block2 = block(obj2 as SubaccountOrder)
                if (block1 != null || block2 != null) {
                    var result = ParsingHelper.compare(block1 ?: 0, block2 ?: 0, false)
                    if (result == ComparisonOrder.same) {
                        result = ParsingHelper.compare(obj1.id, obj2.id, true)
                    }
                    result
                } else {
                    val time1 = (obj1 as SubaccountOrder).createdAtMilliseconds
                    val time2 = (obj2 as SubaccountOrder).createdAtMilliseconds
                    if (time1 != null) {
                        if (time2 != null) {
                            ParsingHelper.compare(time1, time2, false)
                        } else {
                            ComparisonOrder.ascending
                        }
                    } else {
                        if (time2 != null) {
                            ComparisonOrder.descending
                        } else {
                            ParsingHelper.compare(obj1.id, obj2.id, true)
                        }
                    }
                }
            }, { _, obj, itemData ->
                parser.asMap(itemData)?.let {
                    SubaccountOrder.create(obj as? SubaccountOrder, parser, it, localizer)
                }
            })?.toIList()
            return orders
        }

        private inline fun block(order: SubaccountOrder): Int? {
            return order.createdAtHeight ?: if (order.goodTilBlock != null) {
                order.goodTilBlock - SHORT_TERM_ORDER_DURATION
            } else {
                null
            }
        }

        private fun transfers(
            parser: ParserProtocol,
            existing: IList<SubaccountTransfer>?,
            data: List<*>?,
        ): IList<SubaccountTransfer>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountTransfer).updatedAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["confirmedAt"])?.toEpochMilliseconds()?.toDouble()
                        ?: parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()
                            ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                SubaccountTransfer.create(
                    null,
                    parser,
                    parser.asMap(itemData),
                )
            }, true)?.toIList()
        }

        private fun fundingPayments(
            parser: ParserProtocol,
            existing: IList<SubaccountFundingPayment>?,
            data: List<*>?,
        ): IList<SubaccountFundingPayment>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountFundingPayment).effectiveAtMilliSeconds
                val time2 =
                    parser.asDatetime(itemData["effectiveAt"])?.toEpochMilliseconds()?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, obj, itemData ->
                obj ?: SubaccountFundingPayment.create(null, parser, itemData)
            }, true)?.toIList()
        }
    }
}

@JsExport
@Serializable
data class AccountBalance(
    var denom: String,
    var amount: String,
) {
    companion object {
        internal fun create(
            existing: AccountBalance?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            decimals: Int,
        ): AccountBalance? {
            Logger.d { "creating Account Balance\n" }

            val denom = parser.asString(data["denom"])
            val amount = parser.asDecimal(data["amount"])
            if (denom != null && amount != null) {
                val decimalAmount = amount * Numeric.decimal.TEN.pow(-1 * decimals)
                val decimalAmountString = parser.asString(decimalAmount)!!
                return if (existing?.denom != denom || existing.amount != decimalAmountString) {
                    AccountBalance(denom, decimalAmountString)
                } else {
                    existing
                }
            }
            Logger.d { "Account Balance not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class HistoricalTradingReward(
    val amount: Double,
    val cumulativeAmount: Double,
    val startedAtInMilliseconds: Double,
    val endedAtInMilliseconds: Double,
) {
    internal val startedAt: Instant
        get() = Instant.fromEpochMilliseconds(startedAtInMilliseconds.toLong())
    internal val endedAt: Instant
        get() = Instant.fromEpochMilliseconds(endedAtInMilliseconds.toLong())

    companion object {
        internal fun create(
            amount: Double,
            cumulativeAmount: Double,
            startedAt: Instant,
            endedAt: Instant,
        ): HistoricalTradingReward {
            return HistoricalTradingReward(
                amount,
                cumulativeAmount,
                startedAt.toEpochMilliseconds().toDouble(),
                endedAt.toEpochMilliseconds().toDouble(),
            )
        }

        internal fun create(
            existing: HistoricalTradingReward?,
            parser: ParserProtocol,
            data: Map<*, *>,
            period: String,
        ): HistoricalTradingReward? {
            data?.let {
                val amount = parser.asDouble(data["amount"])
                val cumulativeAmount = parser.asDouble(data["cumulativeAmount"])
                val startedAt = parser.asDatetime(data["startedAt"])
                val endedAt = parser.asDatetime(data["endedAt"])

                if (amount != null && cumulativeAmount != null && startedAt != null) {
                    return if (existing?.amount != amount ||
                        existing.cumulativeAmount != cumulativeAmount ||
                        existing.startedAt != startedAt ||
                        existing.endedAt != endedAt
                    ) {
                        create(
                            amount,
                            cumulativeAmount,
                            startedAt,
                            endedAt ?: getEndedAt(startedAt, period),
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "HistoricalTradingReward not valid" }
            return null
        }

        private fun getEndedAt(startedAt: Instant, period: String): Instant {
            return when (period) {
                "DAILY" -> startedAt.plus(1.days)
                "WEEKLY" -> startedAt.plus(7.days)
                "MONTHLY" -> startedAt.nextMonth()
                else -> startedAt.plus(1.days)
            }
        }
    }
}

@JsExport
@Serializable
data class BlockReward(
    val tradingReward: Double,
    val createdAtMilliseconds: Double,
    val createdAtHeight: Int,
) {
    companion object {
        internal fun create(
            existing: BlockReward?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): BlockReward? {
            data?.let {
                val tradingReward = parser.asDouble(data["tradingReward"])
                val createdAtMilliseconds =
                    parser.asDatetime(data["createdAt"])?.toEpochMilliseconds()?.toDouble()
                val createdAtHeight = parser.asInt(data["createdAtHeight"])

                if (tradingReward != null && createdAtMilliseconds != null && createdAtHeight != null) {
                    return if (existing?.tradingReward != tradingReward ||
                        existing.createdAtMilliseconds != createdAtMilliseconds ||
                        existing.createdAtHeight != createdAtHeight
                    ) {
                        BlockReward(
                            tradingReward,
                            createdAtMilliseconds,
                            createdAtHeight,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "BlockReward not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class DatePeriod(
    val start: Instant,
    val end: Instant,
)

internal fun Instant.nextMonth(): Instant {
    val localTime: LocalDateTime = this.toLocalDateTime(TimeZone.UTC)
    var month = localTime.month
    var year = localTime.year
    var monthNumber = month.number
    monthNumber++
    if (monthNumber >= 12) {
        month = Month.JANUARY
        year += 1
    } else {
        month = Month(monthNumber)
    }
    val nextMonth = LocalDateTime(year, month, 1, 0, 0, 0)
    return nextMonth.toInstant(TimeZone.UTC)
}

internal fun Instant.previousMonth(): Instant {
    val localTime: LocalDateTime = this.toLocalDateTime(TimeZone.UTC)
    var month = localTime.month
    var year = localTime.year
    var monthNumber = month.number
    monthNumber--
    if (monthNumber <= 0) {
        month = Month.DECEMBER
        year -= 1
    } else {
        month = Month(monthNumber)
    }
    val previousMonth = LocalDateTime(year, month, 1, 0, 0, 0)
    return previousMonth.toInstant(TimeZone.UTC)
}

@JsExport
@Serializable
data class TradingRewards(
    val total: Double?,
    val blockRewards: IList<BlockReward>?,
    val historical: IMap<String, IList<HistoricalTradingReward>>?
) {
    companion object {
        internal fun create(
            existing: TradingRewards?,
            parser: ParserProtocol,
            data: Map<String, Any>?
        ): TradingRewards? {
            Logger.d { "creating TradingRewards\n" }
            data?.let {
                val total = parser.asDouble(data["total"])
                val historical = total?.let {
                    createHistoricalTradingRewards(
                        it,
                        existing?.historical,
                        parser.asMap(data["historical"]),
                        parser,
                    )
                }
                val blockRewards = parser.asList(data["blockRewards"])?.map {
                    BlockReward.create(null, parser, parser.asMap(it))
                }?.filterNotNull()?.toIList()

                return if (existing?.total != total ||
                    existing?.blockRewards != blockRewards ||
                    existing?.historical != historical
                ) {
                    TradingRewards(
                        total,
                        blockRewards,
                        historical,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "TradingRewards not valid" }
            return null
        }

        private fun createHistoricalTradingRewards(
            total: Double,
            existing: IMap<String, IList<HistoricalTradingReward>>?,
            data: Map<String, Any>?,
            parser: ParserProtocol,
        ): IMap<String, IList<HistoricalTradingReward>> {
            val objs = iMutableMapOf<String, IList<HistoricalTradingReward>>()
            val periods = setOf("WEEKLY", "DAILY", "MONTHLY")
            for (period in periods) {
                val periodObjs = existing?.get(period)
                val periodData = parser.asList(data?.get(period))
                val rewards =
                    createHistoricalTradingRewardsPerPeriod(periodObjs, periodData, parser, period, total)
                objs.typedSafeSet(period, rewards)
            }
            return objs
        }

        private fun createHistoricalTradingRewardsPerPeriod(
            objs: IList<HistoricalTradingReward>?,
            data: List<Any>?,
            parser: ParserProtocol,
            period: String,
            total: Double,
        ): IList<HistoricalTradingReward> {
            val result = iMutableListOf<HistoricalTradingReward>()
            if (data != null) {
                var objIndex = 0
                var dataIndex = 0
                var lastStart: Double? = null
                var cumulativeAmount: Double = total

                while (objIndex < (objs?.size ?: 0) && dataIndex < data.size) {
                    val obj = objs!![objIndex]
                    val item = parser.asMap(data[dataIndex])
                    val itemStart =
                        parser.asDatetime(item?.get("startedAt"))?.toEpochMilliseconds()?.toDouble()
                    if (item != null && itemStart != null) {
                        val objStart = obj.startedAtInMilliseconds
                        val comparison = ParsingHelper.compare(objStart, itemStart, true)
                        when {
                            (comparison == ComparisonOrder.ascending) -> {
                                // item is newer than obj
                                val modified = item.mutable()
                                modified.safeSet("cumulativeAmount", cumulativeAmount)

                                val synced =
                                    HistoricalTradingReward.create(null, parser, modified, period)
                                addHistoricalTradingRewards(result, synced!!, period, lastStart)
                                result.add(synced)
                                dataIndex++
                                lastStart = synced.startedAtInMilliseconds
                                cumulativeAmount = cumulativeAmount - parser.asDouble(item["amount"])!!
                            }

                            (comparison == ComparisonOrder.descending) -> {
                                // item is older than obj
                                val modified = mapOf(
                                    "amount" to obj.amount,
                                    "cumulativeAmount" to cumulativeAmount,
                                    "startedAt" to obj.startedAt,
                                    "endedAt" to obj.endedAt,
                                )

                                val synced = HistoricalTradingReward.create(obj, parser, modified, period)
                                addHistoricalTradingRewards(result, synced!!, period, lastStart)
                                result.add(synced)
                                objIndex++
                                lastStart = synced.startedAtInMilliseconds
                                cumulativeAmount = cumulativeAmount - obj.amount
                            }

                            else -> {
                                val modified = item.mutable()
                                modified.safeSet("cumulativeAmount", cumulativeAmount)

                                val synced =
                                    HistoricalTradingReward.create(obj, parser, modified, period)
                                addHistoricalTradingRewards(result, synced!!, period, lastStart)
                                result.add(synced)
                                objIndex++
                                dataIndex++
                                lastStart = synced.startedAtInMilliseconds
                                cumulativeAmount = cumulativeAmount - obj.amount
                            }
                        }
                    } else {
                        dataIndex++
                    }
                }
                if (objs != null) {
                    while (objIndex < objs.size) {
                        val obj = objs[objIndex]
                        val modified = mapOf(
                            "amount" to obj.amount,
                            "cumulativeAmount" to cumulativeAmount,
                            "startedAt" to obj.startedAt,
                            "endedAt" to obj.endedAt,
                        )

                        val synced = HistoricalTradingReward.create(obj, parser, modified, period)
                        addHistoricalTradingRewards(result, synced!!, period, lastStart)
                        result.add(synced)
                        objIndex++
                        lastStart = obj.startedAtInMilliseconds
                        cumulativeAmount = cumulativeAmount - obj.amount
                    }
                }
                while (dataIndex < data.size) {
                    val item = parser.asMap(data[dataIndex])
                    val itemStart =
                        parser.asDatetime(item?.get("startedAt"))?.toEpochMilliseconds()?.toDouble()

                    if (item != null && itemStart != null) {
                        val modified = item.mutable()
                        modified.safeSet("cumulativeAmount", cumulativeAmount)

                        val synced = HistoricalTradingReward.create(null, parser, modified, period)
                        addHistoricalTradingRewards(result, synced!!, period, lastStart)
                        result.add(synced)
                        dataIndex++
                        lastStart = synced.startedAtInMilliseconds
                        cumulativeAmount = cumulativeAmount - synced.amount
                    }
                }
            } else {
                result.add(currentPeriodPlaceHolder(period, total))
            }
            return result
        }

        private fun currentPeriodPlaceHolder(period: String, total: Double): HistoricalTradingReward {
            val now = Clock.System.now()
            val thisPeriod = when (period) {
                "DAILY" -> today(now)
                "WEEKLY" -> thisWeek(now)
                "MONTHLY" -> thisMonth(now)
                else -> today(now)
            }
            return HistoricalTradingReward(
                0.0,
                total,
                thisPeriod.start.toEpochMilliseconds().toDouble(),
                thisPeriod.end.toEpochMilliseconds().toDouble(),
            )
        }

        private fun previousPlaceHolder(
            period: String,
            lastStartTime: Instant,
            total: Double,
        ): HistoricalTradingReward {
            return when (period) {
                "DAILY" -> HistoricalTradingReward(
                    0.0,
                    total,
                    lastStartTime.minus(1.days).toEpochMilliseconds().toDouble(),
                    lastStartTime.toEpochMilliseconds().toDouble(),
                )

                "WEEKLY" -> HistoricalTradingReward(
                    0.0,
                    total,
                    lastStartTime.minus(7.days).toEpochMilliseconds().toDouble(),
                    lastStartTime.toEpochMilliseconds().toDouble(),
                )

                "MONTHLY" -> HistoricalTradingReward(
                    0.0,
                    total,
                    lastStartTime.previousMonth().toEpochMilliseconds().toDouble(),
                    lastStartTime.toEpochMilliseconds().toDouble(),
                )

                else -> HistoricalTradingReward(
                    0.0,
                    total,
                    lastStartTime.minus(1.days).toEpochMilliseconds().toDouble(),
                    lastStartTime.toEpochMilliseconds().toDouble(),
                )
            }
        }

        private fun addHistoricalTradingRewards(
            result: IMutableList<HistoricalTradingReward>,
            obj: HistoricalTradingReward,
            period: String,
            lastStart: Double?,
        ): Double {
            var lastStartTime: Instant = if (lastStart == null) {
                val thisPeriod = currentPeriodPlaceHolder(period, obj.cumulativeAmount)
                if (obj.startedAtInMilliseconds < thisPeriod.startedAtInMilliseconds) {
                    result.add(thisPeriod)
                }
                Instant.fromEpochMilliseconds(thisPeriod.startedAtInMilliseconds.toLong())
            } else {
                Instant.fromEpochMilliseconds(lastStart.toLong())
            }
            while (obj.startedAtInMilliseconds < lastStartTime.toEpochMilliseconds().toDouble()) {
                val previous = previousPlaceHolder(period, lastStartTime, obj.cumulativeAmount + obj.amount)
                if (obj.startedAtInMilliseconds < previous.startedAtInMilliseconds) {
                    result.add(previous)
                    lastStartTime =
                        Instant.fromEpochMilliseconds(previous.startedAtInMilliseconds.toLong())
                } else {
                    break
                }
            }
            return lastStartTime.toEpochMilliseconds().toDouble()
        }

        private fun today(now: Instant): DatePeriod {
            val zoned: LocalDateTime = now.toLocalDateTime(TimeZone.UTC)
            val utc = LocalDateTime(zoned.year, zoned.month, zoned.dayOfMonth, 0, 0, 0)
            val start = utc.toInstant(TimeZone.UTC)
            val end = start.plus(1.days)
            return DatePeriod(start, end)
        }

        private fun thisWeek(now: Instant): DatePeriod {
            val zoned: LocalDateTime = now.toLocalDateTime(TimeZone.UTC)
            val utc = LocalDateTime(zoned.year, zoned.month, zoned.dayOfMonth, 0, 0, 0)
            val today = utc.toInstant(TimeZone.UTC)
            val dayOfWeek = utc.dayOfWeek
            val start = when (dayOfWeek) {
                DayOfWeek.MONDAY -> today
                DayOfWeek.TUESDAY -> today.minus(1.days)
                DayOfWeek.WEDNESDAY -> today.minus(2.days)
                DayOfWeek.THURSDAY -> today.minus(3.days)
                DayOfWeek.FRIDAY -> today.minus(4.days)
                DayOfWeek.SATURDAY -> today.minus(5.days)
                DayOfWeek.SUNDAY -> today.minus(6.days)
                else -> {
                    Logger.d { "Invalid day of week" }
                    today
                }
            }
            val end = start.plus(7.days)
            return DatePeriod(start, end)
        }

        private fun thisMonth(now: Instant): DatePeriod {
            val zoned: LocalDateTime = now.toLocalDateTime(TimeZone.UTC)
            val utc = LocalDateTime(zoned.year, zoned.month, 1, 0, 0, 0)
            val start = utc.toInstant(TimeZone.UTC)
            val end = start.nextMonth()
            return DatePeriod(start, end)
        }
    }
}

@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class Account(
    var balances: IMap<String, AccountBalance>?,
    var stakingBalances: IMap<String, AccountBalance>?,
    var subaccounts: IMap<String, Subaccount>?,
    var groupedSubaccounts: IMap<String, Subaccount>?,
    var tradingRewards: TradingRewards?,
    val launchIncentivePoints: LaunchIncentivePoints?,
) {
    companion object {
        internal fun create(
            existing: Account?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            tokensInfo: Map<String, TokenInfo>,
            localizer: LocalizerProtocol?,
        ): Account {
            Logger.d { "creating Account\n" }

            val balances: IMutableMap<String, AccountBalance> =
                iMutableMapOf()
            val balancesData = parser.asMap(data["balances"])
            if (balancesData != null) {
                for ((key, value) in balancesData) {
                    val balanceData = parser.asMap(value) ?: iMapOf()
                    // key is the denom
                    val tokenInfo = findTokenInfo(tokensInfo, key)
                    if (tokenInfo != null) {
                        AccountBalance.create(
                            existing?.balances?.get(key),
                            parser,
                            balanceData,
                            tokenInfo.decimals,
                        )?.let { balance ->
                            balances[key] = balance
                        }
                    }
                }
            }

            val stakingBalances: IMutableMap<String, AccountBalance> =
                iMutableMapOf()
            val stakingBalancesData = parser.asMap(data["stakingBalances"])
            if (stakingBalancesData != null) {
                for ((key, value) in stakingBalancesData) {
                    // key is the denom
                    // It should be chain token denom here
                    val tokenInfo = findTokenInfo(tokensInfo, key)
                    if (tokenInfo != null) {
                        val balanceData = parser.asMap(value) ?: iMapOf()
                        AccountBalance.create(
                            existing?.stakingBalances?.get(key),
                            parser,
                            balanceData,
                            tokenInfo.decimals,
                        )?.let { balance ->
                            stakingBalances[key] = balance
                        }
                    }
                }
            }

            val tradingRewardsData = parser.asMap(data["tradingRewards"])
            val tradingRewards = if (tradingRewardsData != null) {
                TradingRewards.create(existing?.tradingRewards, parser, tradingRewardsData)
            } else {
                null
            }

            val launchIncentivePoints = (parser.asMap(data["launchIncentivePoints"]))?.let {
                LaunchIncentivePoints.create(existing?.launchIncentivePoints, parser, it)
            }

            val subaccounts: IMutableMap<String, Subaccount> =
                iMutableMapOf()

            val subaccountsData = parser.asMap(data["subaccounts"])
            if (subaccountsData != null) {
                for ((key, value) in subaccountsData) {
                    val subaccountData = parser.asMap(value) ?: iMapOf()

                    Subaccount.create(
                        existing?.subaccounts?.get(key),
                        parser,
                        subaccountData,
                        localizer,
                    )
                        ?.let { subaccount ->
                            subaccounts[key] = subaccount
                        }
                }
            }

            val groupedSubaccounts: IMutableMap<String, Subaccount> =
                iMutableMapOf()

            val groupedSubaccountsData = parser.asMap(data["groupedSubaccounts"])
            if (groupedSubaccountsData != null) {
                for ((key, value) in groupedSubaccountsData) {
                    val subaccountData = parser.asMap(value) ?: iMapOf()

                    Subaccount.create(
                        existing?.subaccounts?.get(key),
                        parser,
                        subaccountData,
                        localizer,
                    )
                        ?.let { subaccount ->
                            groupedSubaccounts[key] = subaccount
                        }
                }
            }

            return Account(
                balances,
                stakingBalances,
                subaccounts,
                groupedSubaccounts,
                tradingRewards,
                launchIncentivePoints,
            )
        }

        private fun findTokenInfo(tokensInfo: Map<String, TokenInfo>, denom: String): TokenInfo? {
            return tokensInfo.firstNotNullOfOrNull { if (it.value.denom == denom) it.value else null }
        }
    }
}
