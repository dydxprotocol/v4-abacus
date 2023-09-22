package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.AppVersion
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IMap
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class ClosePositionInputSize(
    val size: Double?,
    val usdcSize: Double?,
    val percent: Double?,
    val input: String?
) {
    companion object {
        internal fun create(
            existing: ClosePositionInputSize?,
            parser: ParserProtocol, data: IMap<*, *>?
        ): ClosePositionInputSize? {
            DebugLogger.log("creating Trade Input Size\n")

            data?.let {
                val size = parser.asDouble(data["size"])
                val usdcSize = parser.asDouble(data["usdcSize"])
                val percent = parser.asDouble(data["percent"])
                val input = parser.asString(data["input"])
                return if (existing?.size != size ||
                    existing?.usdcSize != usdcSize ||
                    existing?.percent != percent ||
                    existing?.input != input
                ) {
                    ClosePositionInputSize(size, usdcSize, percent, input)
                } else {
                    existing
                }
            }
            DebugLogger.debug("Trade Input Size not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class ClosePositionInput(
    val type: OrderType?,
    val side: OrderSide?,
    val marketId: String?,
    val size: ClosePositionInputSize?,
    val price: TradeInputPrice?,
    val fee: Double?,
    val marketOrder: TradeInputMarketOrder?,
    val summary: TradeInputSummary?
) {
    companion object {
        internal fun create(
            existing: ClosePositionInput?,
            parser: ParserProtocol, data: IMap<*, *>?,
            version: AppVersion
        ): ClosePositionInput? {
            DebugLogger.log("creating Close Position Input\n")

            data?.let {
                val type = parser.asString(data["type"])?.let {
                    OrderType.invoke(it)
                }
                val side = parser.asString(data["side"])?.let {
                    OrderSide.invoke(it)
                }
                val marketId = parser.asString(data["marketId"])

                val size = ClosePositionInputSize.create(
                    existing?.size,
                    parser,
                    parser.asMap(data["size"])
                )
                val price =
                    TradeInputPrice.create(existing?.price, parser, parser.asMap(data["price"]))

                val fee = parser.asDouble(data["fee"])

                val marketOrder =
                    TradeInputMarketOrder.create(
                        existing?.marketOrder,
                        parser,
                        parser.asMap(data["marketOrder"])
                    )
                val summary = TradeInputSummary.create(
                    existing?.summary,
                    parser,
                    parser.asMap(data["summary"])
                )

                return if (
                    existing?.type !== type ||
                    existing?.side !==  side ||
                    existing?.marketId != marketId ||
                    existing?.size !== size ||
                    existing?.price !== price ||
                    existing?.fee != fee ||
                    existing?.marketOrder !== marketOrder ||
                    existing?.summary !== summary
                ) {
                    ClosePositionInput(
                        type,
                        side,
                        marketId,
                        size,
                        price,
                        fee,
                        marketOrder,
                        summary
                    )
                } else {
                    existing
                }
            }
            DebugLogger.debug("Close Position Input not valid")
            return null
        }
    }
}
