package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderTimeInForce
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountOrder(
    val subaccountNumber: Int?,
    val id: String,
    val clientId: String?,
    val type: OrderType,
    val side: OrderSide,
    val status: OrderStatus,
    val timeInForce: OrderTimeInForce?,
    val marketId: String,
    val displayId: String,
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
    val marginMode: MarginMode?
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
                val clientId = parser.asString(data["clientId"])
                val marketId = parser.asString(data["marketId"])
                val displayId = parser.asString(data["displayId"])
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
                val marginMode = parser.asString(data["marginMode"])?.let { MarginMode.invoke(it) }
                if (id != null && marketId != null && displayId != null && type != null && side != null && status != null && price != null && size != null &&
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
                        existing.displayId != displayId ||
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
                        existing.resources !== resources ||
                        existing.subaccountNumber != subaccountNumber ||
                        existing.marginMode != marginMode
                    ) {
                        SubaccountOrder(
                            subaccountNumber = subaccountNumber,
                            id = id,
                            clientId = clientId,
                            type = type,
                            side = side,
                            status = status,
                            timeInForce = timeInForce,
                            marketId = marketId,
                            displayId = displayId,
                            clobPairId = clobPairId,
                            orderFlags = orderFlags,
                            price = price,
                            triggerPrice = triggerPrice,
                            trailingPercent = trailingPercent,
                            size = size,
                            remainingSize = remainingSize,
                            totalFilled = totalFilled,
                            goodTilBlock = goodTilBlock,
                            goodTilBlockTime = goodTilBlockTime,
                            createdAtHeight = createdAtHeight,
                            createdAtMilliseconds = createdAtMilliseconds,
                            unfillableAtMilliseconds = unfillableAtMilliseconds,
                            expiresAtMilliseconds = expiresAtMilliseconds,
                            updatedAtMilliseconds = updatedAtMilliseconds,
                            postOnly = postOnly,
                            reduceOnly = reduceOnly,
                            cancelReason = cancelReason,
                            resources = resources,
                            marginMode = marginMode,
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
