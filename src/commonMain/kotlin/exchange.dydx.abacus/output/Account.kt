package exchange.dydx.abacus.output

import exchange.dydx.abacus.output.input.*
import exchange.dydx.abacus.processor.base.ComparisonOrder
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.SHORT_TERM_ORDER_DURATION
import kollections.JsExport
import kollections.iMapOf
import kollections.iMutableListOf
import kollections.iMutableMapOf
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
            data: IMap<*, *>?,
        ): SubaccountHistoricalPNL? {
            DebugLogger.log("creating Account Historical PNL\n")
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
                            createdAtMilliseconds
                        )
                    } else {
                        existing
                    }
                }
            }
            DebugLogger.debug("Account Historical PNL not valid")
            return null
        }


        fun create(
            existing: IList<SubaccountHistoricalPNL>?,
            parser: ParserProtocol,
            data: IList<IMap<String, Any>>?,
            startTime: Instant,
        ): IList<SubaccountHistoricalPNL>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time2 = parser.asDatetime(itemData["createdAt"])
                if (time2 != null && time2 >= startTime) {
                    val time1 = (obj as SubaccountHistoricalPNL).createdAtMilliseconds
                    val time2MS = time2.toEpochMilliseconds().toDouble()
                    ParsingHelper.compare(time1, time2MS ?: 0.0, true)
                } else null
            }, { _, itemData ->
                SubaccountHistoricalPNL.create(null, parser, parser.asMap(itemData))
            }, true, { item ->
                val ms = (item as SubaccountHistoricalPNL).createdAtMilliseconds.toDouble()
                val createdAt = Instant.fromEpochMilliseconds(ms.toLong())
                createdAt >= startTime
            }, { itemData ->
                val createdAt = parser.asDatetime(itemData["createdAt"])
                createdAt != null && createdAt >= startTime
            })
        }
    }
}


@JsExport
@Serializable
data class SubaccountPositionResources(
    val sideStringKey: TradeStatesWithStringValues,
    val indicator: TradeStatesWithStringValues,
) {
    companion object {
        internal fun create(
            existing: SubaccountPositionResources?,
            parser: ParserProtocol, data: IMap<*, *>?,
        ): SubaccountPositionResources? {
            DebugLogger.log("creating Account Position Resources\n")
            data?.let {
                val sideStringKey: TradeStatesWithStringValues =
                    TradeStatesWithStringValues.create(
                        existing?.sideStringKey,
                        parser,
                        parser.asMap(data["sideStringKey"])
                    )
                val indicator: TradeStatesWithStringValues =
                    TradeStatesWithStringValues.create(
                        existing?.indicator,
                        parser,
                        parser.asMap(data["indicator"])
                    )
                return if (existing?.sideStringKey !== sideStringKey ||
                    existing.indicator !== indicator
                ) {
                    SubaccountPositionResources(sideStringKey, indicator)
                } else {
                    existing
                }
            }
            DebugLogger.debug("Account Position Resources not valid")
            return null
        }
    }
}

@JsExport
@Serializable
enum class PositionSide(val rawValue: String) {
    LONG("LONG"), SHORT("SHORT"), NONE("NONE");

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
            data: IMap<*, *>?,
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
    val entryPrice: TradeStatesWithDoubleValues?,
    val exitPrice: Double?,
    val createdAtMilliseconds: Double?,
    val closedAtMilliseconds: Double?,
    val netFunding: Double?,
    val realizedPnl: TradeStatesWithDoubleValues?,
    val realizedPnlPercent: TradeStatesWithDoubleValues?,
    val unrealizedPnl: TradeStatesWithDoubleValues?,
    val unrealizedPnlPercent: TradeStatesWithDoubleValues?,
    val size: TradeStatesWithDoubleValues?,
    val notionalTotal: TradeStatesWithDoubleValues?,
    val valueTotal: TradeStatesWithDoubleValues?,
    val initialRiskTotal: TradeStatesWithDoubleValues?,
    val adjustedImf: TradeStatesWithDoubleValues?,
    val adjustedMmf: TradeStatesWithDoubleValues?,
    val leverage: TradeStatesWithDoubleValues?,
    val maxLeverage: TradeStatesWithDoubleValues?,
    val buyingPower: TradeStatesWithDoubleValues?,
    val liquidationPrice: TradeStatesWithDoubleValues?,
    val resources: SubaccountPositionResources,
) {
    companion object {
        internal fun create(
            existing: SubaccountPosition?,
            parser: ParserProtocol,
            data: IMap<String, Any>?,
        ): SubaccountPosition? {
            DebugLogger.log("creating Account Position\n")
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
                            parser.asMap(data["entryPrice"])
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
                            parser.asMap(data["realizedPnl"])
                        )
                    val realizedPnlPercent = TradeStatesWithDoubleValues.create(
                        existing?.realizedPnlPercent,
                        parser,
                        parser.asMap(data["realizedPnlPercent"])
                    )
                    val unrealizedPnl =
                        TradeStatesWithDoubleValues.create(
                            existing?.unrealizedPnl,
                            parser,
                            parser.asMap(data["unrealizedPnl"])
                        )
                    val unrealizedPnlPercent = TradeStatesWithDoubleValues.create(
                        existing?.unrealizedPnlPercent,
                        parser,
                        parser.asMap(data["unrealizedPnlPercent"])
                    )
                    val size =
                        TradeStatesWithDoubleValues.create(
                            existing?.size,
                            parser,
                            parser.asMap(data["size"])
                        )
                    val notionalTotal =
                        TradeStatesWithDoubleValues.create(
                            existing?.notionalTotal,
                            parser,
                            parser.asMap(data["notionalTotal"])
                        )
                    val valueTotal =
                        TradeStatesWithDoubleValues.create(
                            existing?.valueTotal,
                            parser,
                            parser.asMap(data["valueTotal"])
                        )
                    val initialRiskTotal = TradeStatesWithDoubleValues.create(
                        existing?.initialRiskTotal,
                        parser,
                        parser.asMap(data["initialRiskTotal"])
                    )
                    val adjustedImf =
                        TradeStatesWithDoubleValues.create(
                            existing?.adjustedImf,
                            parser,
                            parser.asMap(data["adjustedImf"])
                        )
                    val adjustedMmf =
                        TradeStatesWithDoubleValues.create(
                            existing?.adjustedMmf,
                            parser,
                            parser.asMap(data["adjustedMmf"])
                        )
                    val leverage =
                        TradeStatesWithDoubleValues.create(
                            existing?.leverage,
                            parser,
                            parser.asMap(data["leverage"])
                        )
                    val maxLeverage =
                        TradeStatesWithDoubleValues.create(
                            existing?.leverage,
                            parser,
                            parser.asMap(data["maxLeverage"])
                        )
                    val buyingPower =
                        TradeStatesWithDoubleValues.create(
                            existing?.leverage,
                            parser,
                            parser.asMap(data["buyingPower"])
                        )
                    val liquidationPrice = TradeStatesWithDoubleValues.create(
                        existing?.liquidationPrice,
                        parser,
                        parser.asMap(data["liquidationPrice"])
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
                        existing.resources !== resources
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
                            resources
                        )
                    } else {
                        existing
                    }
                }
            }
            DebugLogger.debug("Account Position not valid")
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
                if (size > 0) PositionSide.LONG
                else if (size < 0) PositionSide.SHORT
                else PositionSide.NONE
            } else null
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
    val sideStringKey: String,
    val typeStringKey: String?,
    val statusStringKey: String?,
    val timeInForceStringKey: String?,
) {
    companion object {
        internal fun create(
            existing: SubaccountOrderResources?,
            parser: ParserProtocol, data: IMap<*, *>?,
        ): SubaccountOrderResources? {
            DebugLogger.log("creating Account Order Resources\n")

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
                        SubaccountOrderResources(
                            sideStringKey,
                            typeStringKey,
                            statusStringKey,
                            timeInForceStringKey
                        )
                    } else {
                        existing
                    }
                }
            }
            DebugLogger.debug("Account Order Resources not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class SubaccountOrder(
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
    val postOnly: Boolean,
    val reduceOnly: Boolean,
    val cancelReason: String?,
    val resources: SubaccountOrderResources,
) {
    companion object {
        internal fun create(
            existing: SubaccountOrder?,
            parser: ParserProtocol, data: IMap<*, *>?,
        ): SubaccountOrder? {
            DebugLogger.log("creating Account Order\n")
            data?.let {
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
                    SubaccountOrderResources.create(existing?.resources, parser, it)
                }
                if (id != null && marketId != null && type != null && side != null && status != null && price != null && size != null
                    && resources != null
                ) {
                    val triggerPrice = parser.asDouble(data["triggerPrice"])
                    val trailingPercent = parser.asDouble(data["trailingPercent"])
                    val remainingSize = parser.asDouble(data["remainingSize"])
                    val totalFilled = parser.asDouble(data["totalFilled"])

                    val unfillableAtMilliseconds =
                        parser.asDatetime(data["unfillableAt"])?.toEpochMilliseconds()?.toDouble()
                    val goodTilBlock = parser.asInt(data["goodTilBlock"]);
                    val goodTilBlockTime = parser.asDatetime(data["goodTilBlockTime"])?.epochSeconds?.toInt();
                    val createdAtHeight = parser.asInt(data["createdAtHeight"]);
                    val expiresAtMilliseconds =
                        parser.asDatetime(data["expiresAt"] ?: data["goodTilBlockTime"])
                            ?.toEpochMilliseconds()?.toDouble()
                    val postOnly = parser.asBool(data["postOnly"]) ?: false
                    val reduceOnly = parser.asBool(data["reduceOnly"]) ?: false
                    val cancelReason = parser.asString(data["cancelReason"])

                    return if (existing?.id != id ||
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
                        existing.postOnly != postOnly ||
                        existing.reduceOnly != reduceOnly ||
                        existing.cancelReason != cancelReason ||
                        existing.resources !== resources
                    ) {
                        SubaccountOrder(
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
                            createdAtMilliseconds?.toDouble(),
                            unfillableAtMilliseconds?.toDouble(),
                            expiresAtMilliseconds?.toDouble(),
                            postOnly,
                            reduceOnly,
                            cancelReason,
                            resources
                        )
                    } else {
                        existing
                    }
                } else {
                    DebugLogger.debug("Account Order not valid")
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
    val sideStringKey: String?,
    val liquidityStringKey: String?,
    val typeStringKey: String?,
    val iconLocal: String?,
) {
    companion object {
        internal fun create(
            existing: SubaccountFillResources?,
            parser: ParserProtocol, data: IMap<*, *>?,
        ): SubaccountFillResources? {
            DebugLogger.log("creating Account Fill Resources\n")

            data?.let {
                val sideStringKey = parser.asString(data["sideStringKey"])
                val liquidityStringKey = parser.asString(data["liquidityStringKey"])
                val typeStringKey = parser.asString(data["typeStringKey"])
                val iconLocal = parser.asString(data["iconLocal"])
                return if (existing?.sideStringKey != sideStringKey ||
                    existing?.liquidityStringKey != liquidityStringKey ||
                    existing?.typeStringKey != typeStringKey ||
                    existing?.iconLocal != iconLocal
                ) {
                    SubaccountFillResources(
                        sideStringKey,
                        liquidityStringKey,
                        typeStringKey,
                        iconLocal
                    )
                } else {
                    DebugLogger.debug("Account Fill Resources not valid")
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
    maker("MAKER"), taker("TAKER");

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
            parser: ParserProtocol, data: IMap<*, *>?,
        ): SubaccountFill? {
            DebugLogger.log("creating Account Fill\n")
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
                    SubaccountFillResources.create(existing?.resources, parser, it)
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
                    )
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
                            resources
                        ) else existing
                } else {
                    DebugLogger.debug("Account Fill not valid")
                    null
                }
            }
            return null
        }


        fun create(
            existing: IList<SubaccountFill>?,
            parser: ParserProtocol,
            data: IList<IMap<String, Any>>?,
        ): IList<SubaccountFill>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountFill).createdAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()
                        ?.toDouble()
                val id1 = (obj as SubaccountFill).id
                val id2 = parser.asString(itemData["id"])
                if (id1 == id2) {
                    ParsingHelper.compare(time1, time2 ?: 0.0, true)
                } else {
                    ParsingHelper.compare(id1, id2, true)
                }
            }, { _, itemData ->
                SubaccountFill.create(null, parser, parser.asMap(itemData))
            }, true)
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
            data: IMap<*, *>?,
        ): SubaccountTransferResources? {
            DebugLogger.log("creating Account Transfer Resources\n")

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
                    SubaccountTransferResources(
                        typeStringKey,
                        blockExplorerUrl,
                        statusStringKey,
                        iconLocal,
                        indicator
                    )
                } else {
                    existing
                }
            }

            DebugLogger.debug("Account Transfer Resources not valid")
            return null
        }
    }
}

@JsExport
@Serializable
enum class TransferRecordType(val rawValue: String) {
    DEPOSIT("DEPOSIT"), WITHDRAW("WITHDRAWAL"), TRANSFER_IN("TRANSFER_IN"), TRANSFER_OUT("TRANSFER_OUT");

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
            parser: ParserProtocol, data: IMap<*, *>?,
        ): SubaccountTransfer? {
            DebugLogger.log("creating Account Transfer\n")
            data?.let {
                val id = parser.asString(data["id"])
                val updatedAt =
                    parser.asDatetime(data["confirmedAt"]) ?: parser.asDatetime(data["createdAt"])
                val updatedAtMilliseconds = updatedAt?.toEpochMilliseconds()?.toDouble()
                val resources = parser.asMap(data["resources"])?.let {
                    SubaccountTransferResources.create(existing?.resources, parser, it)
                }
                if (id != null && updatedAtMilliseconds != null && resources != null) {
                    val type = TransferRecordType.invoke(parser.asString(data["type"])) ?: return null
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
                            resources
                        )
                    } else {
                        existing
                    }
                }
            }
            DebugLogger.debug("Account Transfer not valid")
            return null
        }


        fun create(
            existing: IList<SubaccountTransfer>?,
            parser: ParserProtocol,
            data: IList<IMap<String, Any>>?,
        ): IList<SubaccountTransfer>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountTransfer).updatedAtMilliseconds
                val time2 =
                    (parser.asDatetime(itemData["confirmedAt"])
                        ?: parser.asDatetime(itemData["createdAt"]))?.toEpochMilliseconds()
                        ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, itemData ->
                SubaccountTransfer.create(null, parser, parser.asMap(itemData))
            })
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
            data: IMap<*, *>?,
        ): SubaccountFundingPayment? {
            DebugLogger.log("creating Account Funding Payment\n")

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
                            effectiveAtMilliSeconds
                        )
                    } else {
                        existing
                    }
                }
            }
            DebugLogger.debug("Account Funding Payment not valid")
            return null
        }


        fun create(
            existing: IList<SubaccountFundingPayment>?,
            parser: ParserProtocol,
            data: IList<IMap<String, Any>>?,
        ): IList<SubaccountFundingPayment>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountFundingPayment).effectiveAtMilliSeconds
                val time2 =
                    parser.asDatetime(itemData["effectiveAt"])?.toEpochMilliseconds()
                        ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, true)
            }, { _, itemData ->
                SubaccountFundingPayment.create(null, parser, parser.asMap(itemData))
            })
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
    val orders: IList<SubaccountOrder>?,
    val marginEnabled: Boolean?,
) {
    companion object {
        internal fun create(
            existing: Subaccount?,
            parser: ParserProtocol,
            data: IMap<*, *>?,
        ): Subaccount? {
            DebugLogger.log("creating Account\n")

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
                        parser, parser.asMap(data["quoteBalance"])
                    )
                val notionalTotal =
                    TradeStatesWithDoubleValues.create(
                        existing?.notionalTotal,
                        parser, parser.asMap(data["notionalTotal"])
                    )
                val valueTotal =
                    TradeStatesWithDoubleValues.create(
                        existing?.valueTotal,
                        parser, parser.asMap(data["valueTotal"])
                    )
                val initialRiskTotal =
                    TradeStatesWithDoubleValues.create(
                        existing?.initialRiskTotal,
                        parser,
                        parser.asMap(data["initialRiskTotal"])
                    )
                val adjustedImf =
                    TradeStatesWithDoubleValues.create(
                        existing?.adjustedImf,
                        parser, parser.asMap(data["adjustedImf"])
                    )
                val equity =
                    TradeStatesWithDoubleValues.create(
                        existing?.equity,
                        parser, parser.asMap(data["equity"])
                    )
                val freeCollateral =
                    TradeStatesWithDoubleValues.create(
                        existing?.freeCollateral,
                        parser, parser.asMap(data["freeCollateral"])
                    )
                val leverage =
                    TradeStatesWithDoubleValues.create(
                        existing?.leverage,
                        parser, parser.asMap(data["leverage"])
                    )
                val marginUsage =
                    TradeStatesWithDoubleValues.create(
                        existing?.marginUsage,
                        parser, parser.asMap(data["marginUsage"])
                    )
                val buyingPower =
                    TradeStatesWithDoubleValues.create(
                        existing?.buyingPower,
                        parser, parser.asMap(data["buyingPower"])
                    )

                val openPositions = openPositions(
                        existing?.openPositions,
                        parser,
                        parser.asMap(data["openPositions"])
                    )
                val orders = orders(parser, existing?.orders, parser.asMap(data["orders"]))

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

                return if (existing?.positionId != positionId ||
                    existing?.pnlTotal != pnlTotal ||
                    existing?.pnl24h != pnl24h ||
                    existing?.pnl24hPercent != pnl24hPercent ||
                    existing?.quoteBalance !== quoteBalance ||
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
                        orders,
                        marginEnabled
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
            data: IMap<*, *>?,
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
            }, { _, itemData ->
                parser.asMap(itemData)?.let {
                    SubaccountPosition.create(null, parser, it)
                }
            })
        }

        private fun orders(
            parser: ParserProtocol,
            existing: IList<SubaccountOrder>?,
            data: IMap<*, *>?,
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
                        } else ComparisonOrder.ascending
                    } else {
                        if (time2 != null) ComparisonOrder.descending else {
                            ParsingHelper.compare(obj1.id, obj2.id, true)
                        }
                    }
                }
            }, { _, itemData ->
                parser.asMap(itemData)?.let {
                    SubaccountOrder.create(null, parser, it)
                }
            })
            return orders
        }

        private inline fun block(order: SubaccountOrder): Int? {
            return order.createdAtHeight ?: if (order.goodTilBlock != null) {
                order.goodTilBlock - SHORT_TERM_ORDER_DURATION
            } else null
        }

        private fun transfers(
            parser: ParserProtocol,
            existing: IList<SubaccountTransfer>?,
            data: IList<*>?,
        ): IList<SubaccountTransfer>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountTransfer).updatedAtMilliseconds
                val time2 =
                    parser.asDatetime(itemData["confirmedAt"])?.toEpochMilliseconds()?.toDouble()
                        ?: parser.asDatetime(itemData["createdAt"])?.toEpochMilliseconds()
                            ?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, itemData ->
                SubaccountTransfer.create(null, parser, parser.asMap(itemData))
            }, true)
        }

        private fun fundingPayments(
            parser: ParserProtocol,
            existing: IList<SubaccountFundingPayment>?,
            data: IList<*>?,
        ): IList<SubaccountFundingPayment>? {
            return ParsingHelper.merge(parser, existing, data, { obj, itemData ->
                val time1 = (obj as SubaccountFundingPayment).effectiveAtMilliSeconds
                val time2 =
                    parser.asDatetime(itemData["effectiveAt"])?.toEpochMilliseconds()?.toDouble()
                ParsingHelper.compare(time1, time2 ?: 0.0, false)
            }, { _, itemData ->
                SubaccountFundingPayment.create(null, parser, itemData)
            }, true)
        }
    }
}

@JsExport
@Serializable
data class AccountBalance(
    var denom: String,
    var amount: Double,
) {
    companion object {
        internal fun create(
            existing: AccountBalance?,
            parser: ParserProtocol,
            data: IMap<String, Any>,
        ): AccountBalance? {
            DebugLogger.log("creating Account Balance\n")

            val denom = parser.asString(data["denom"])
            val amount = parser.asDouble(data["amount"])
            if (denom != null && amount != null) {
                return if (existing?.denom != denom || existing.amount != amount) {
                    AccountBalance(denom, amount)
                } else {
                    existing
                }
            }
            DebugLogger.debug("Account Balance not valid")
            return null
        }
    }
}

@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class Account(
    var balances: IMap<String, AccountBalance>?,
    var subaccounts: IMap<String, Subaccount>?,
) {
    companion object {
        internal fun create(
            existing: Account?,
            parser: ParserProtocol,
            data: IMap<String, Any>,
        ): Account {
            DebugLogger.log("creating Account\n")

            val balances: IMutableMap<String, AccountBalance> = iMutableMapOf<String, AccountBalance>()
            val balancesData = parser.asMap(data["balances"])
            if (balancesData != null) {
                for ((key, value) in balancesData) {
                    val balanceData = parser.asMap(value) ?: iMapOf()
                    AccountBalance.create(
                        existing?.balances?.get(key),
                        parser,
                        balanceData
                    )?.let { balance ->
                        balances[key] = balance
                    }
                }
            }

            val subaccounts: IMutableMap<String, Subaccount> =
                iMutableMapOf<String, Subaccount>()

            val subaccountsData = parser.asMap(data["subaccounts"])
            if (subaccountsData != null) {
                for ((key, value) in subaccountsData) {
                    val subaccountData = parser.asMap(value) ?: iMapOf()
                    Subaccount.create(
                        existing?.subaccounts?.get(key),
                        parser,
                        subaccountData
                    )
                        ?.let { subaccount ->
                            subaccounts[key] = subaccount
                        }
                }
            }

            return Account(balances, subaccounts)
        }
    }
}