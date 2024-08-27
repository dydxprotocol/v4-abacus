package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.utils.Logger
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
            parser: ParserProtocol,
            data: Map<*, *>?
        ): ClosePositionInputSize? {
            Logger.d { "creating Trade Input Size\n" }

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
            Logger.d { "Trade Input Size not valid" }
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
            state: InternalTradeInputState?
        ): ClosePositionInput? {
            if (state == null) {
                return null
            }

            return ClosePositionInput(
                type = state.type,
                side = state.side,
                marketId = state.marketId,
                size = ClosePositionInputSize(
                    size = state.size?.size,
                    usdcSize = state.size?.usdcSize,
                    percent = state.sizePercent,
                    input = state.size?.input,
                ),
                price = state.price,
                fee = state.fee,
                marketOrder = state.marketOrder,
                summary = TradeInputSummary.create(state.summary),
            )
        }

        internal fun create(
            existing: ClosePositionInput?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): ClosePositionInput? {
            Logger.d { "creating Close Position Input\n" }

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
                    parser.asMap(data["size"]),
                )
                val price =
                    TradeInputPrice.create(existing?.price, parser, parser.asMap(data["price"]))

                val fee = parser.asDouble(data["fee"])

                val marketOrder =
                    TradeInputMarketOrder.create(
                        existing?.marketOrder,
                        parser,
                        parser.asMap(data["marketOrder"]),
                    )
                val summary = TradeInputSummary.create(
                    existing?.summary,
                    parser,
                    parser.asMap(data["summary"]),
                )

                return if (
                    existing?.type !== type ||
                    existing?.side !== side ||
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
                        summary,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Close Position Input not valid" }
            return null
        }
    }
}
