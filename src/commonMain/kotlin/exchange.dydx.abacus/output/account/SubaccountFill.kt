package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ParsingHelper
import kollections.JsExport
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountFill(
    val id: String,
    val marketId: String,
    val displayId: String,
    val orderId: String?,
    val subaccountNumber: Int?,
    val marginMode: MarginMode?,
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
        private fun create(
            existing: SubaccountFill?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): SubaccountFill? {
            Logger.d { "creating Account Fill\n" }
            data?.let {
                val id = parser.asString(data["id"])
                val marketId = parser.asString(data["marketId"])
                val displayId = parser.asString(data["displayId"])
                val orderId = parser.asString(data["orderId"])
                val subaccountNumber = parser.asInt(data["subaccountNumber"])
                val marginMode = parser.asString(data["marginMode"])?.let { MarginMode.invoke(it) }
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
                return if (id != null && marketId != null && displayId != null && side != null && type != null && liquidity != null &&
                    price != null && size != null && createdAtMilliseconds != null && resources != null
                ) {
                    val fee = parser.asDouble(data["fee"])
                    if (existing?.id != id ||
                        existing.marketId != marketId ||
                        existing.displayId != displayId ||
                        existing.orderId != orderId ||
                        existing.subaccountNumber != subaccountNumber ||
                        existing.marginMode != marginMode ||
                        existing.side !== side ||
                        existing.type !== type ||
                        existing.liquidity !== liquidity ||
                        existing.price != price ||
                        existing.fee != fee ||
                        existing.createdAtMilliseconds != createdAtMilliseconds ||
                        existing.resources !== resources
                    ) {
                        SubaccountFill(
                            id = id,
                            marketId = marketId,
                            displayId = displayId,
                            orderId = orderId,
                            subaccountNumber = subaccountNumber,
                            marginMode = marginMode,
                            side = side,
                            type = type,
                            liquidity = liquidity,
                            price = price,
                            size = size,
                            fee = fee,
                            createdAtMilliseconds = createdAtMilliseconds,
                            resources = resources,
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

        internal fun create(
            existing: IList<SubaccountFill>?,
            parser: ParserProtocol,
            data: List<Map<String, Any>>?,
            localizer: LocalizerProtocol?,
        ): IList<SubaccountFill>? {
            return ParsingHelper.merge(
                parser = parser,
                existing = existing,
                data = data,
                comparison = { obj, itemData ->
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
                },
                createObject = { _, obj, itemData ->
                    obj ?: SubaccountFill.create(null, parser, parser.asMap(itemData), localizer)
                },
                syncItems = true,
            )?.toIList()
        }

        internal fun merge(
            existing: IList<SubaccountFill>?,
            new: IList<SubaccountFill>?,
        ): IList<SubaccountFill> {
            return ParsingHelper.merge(
                existing = existing,
                new = new,
                comparison = { obj, newItem ->
                    if (obj.id == newItem.id) {
                        ParsingHelper.compare(obj.createdAtMilliseconds, newItem.createdAtMilliseconds, true)
                    } else {
                        ParsingHelper.compare(obj.id, newItem.id, true)
                    }
                },
                syncItems = true,
            ).toIList()
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
