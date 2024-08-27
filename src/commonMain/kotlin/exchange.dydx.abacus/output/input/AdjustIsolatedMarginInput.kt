package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAdjustIsolatedMarginInputState
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class AdjustIsolatedMarginInputOptions(
    val needsSize: Boolean,
) {
    companion object {
        internal fun create(
            existing: AdjustIsolatedMarginInputOptions?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): AdjustIsolatedMarginInputOptions? {
            Logger.d { "creating Adjust Isolated Margin Input Options\n" }

            data?.let {
                val needsSize = parser.asBool(data["needsSize"]) ?: false

                return if (existing?.needsSize != needsSize
                ) {
                    AdjustIsolatedMarginInputOptions(needsSize)
                } else {
                    existing
                }
            }
            Logger.d { "Adjust Isolated Margin Input Options not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class AdjustIsolatedMarginInputSummary(
    val crossFreeCollateral: Double?,
    val crossFreeCollateralUpdated: Double?,
    val crossMarginUsage: Double?,
    val crossMarginUsageUpdated: Double?,
    val positionMargin: Double?,
    val positionMarginUpdated: Double?,
    val positionLeverage: Double?,
    val positionLeverageUpdated: Double?,
    val liquidationPrice: Double?,
    val liquidationPriceUpdated: Double?,
) {
    companion object {
        internal fun create(
            existing: AdjustIsolatedMarginInputSummary?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): AdjustIsolatedMarginInputSummary? {
            Logger.d { "creating Adjust Isolated Margin Input Summary\n" }

            data?.let {
                val crossFreeCollateral = parser.asDouble(parser.value(data, "crossFreeCollateral.current"))
                val crossFreeCollateralUpdated = parser.asDouble(parser.value(data, "crossFreeCollateral.postOrder"))
                val crossMarginUsage = parser.asDouble(parser.value(data, "crossMarginUsage.current"))
                val crossMarginUsageUpdated = parser.asDouble(parser.value(data, "crossMarginUsage.postOrder"))
                val positionMargin = parser.asDouble(parser.value(data, "positionMargin.current"))
                val positionMarginUpdated = parser.asDouble(parser.value(data, "positionMargin.postOrder"))
                val positionLeverage = parser.asDouble(parser.value(data, "positionLeverage.current"))
                val positionLeverageUpdated = parser.asDouble(parser.value(data, "positionLeverage.postOrder"))
                val liquidationPrice = parser.asDouble(parser.value(data, "liquidationPrice.current"))
                val liquidationPriceUpdated = parser.asDouble(parser.value(data, "liquidationPrice.postOrder"))

                return if (
                    existing?.crossFreeCollateral != crossFreeCollateral ||
                    existing?.crossFreeCollateralUpdated != crossFreeCollateralUpdated ||
                    existing?.crossMarginUsage != crossMarginUsage ||
                    existing?.crossMarginUsageUpdated != crossMarginUsageUpdated ||
                    existing?.positionMargin != positionMargin ||
                    existing?.positionMarginUpdated != positionMarginUpdated ||
                    existing?.positionLeverage != positionLeverage ||
                    existing?.positionLeverageUpdated != positionLeverageUpdated ||
                    existing?.liquidationPrice != liquidationPrice ||
                    existing?.liquidationPriceUpdated != liquidationPriceUpdated
                ) {
                    AdjustIsolatedMarginInputSummary(
                        crossFreeCollateral,
                        crossFreeCollateralUpdated,
                        crossMarginUsage,
                        crossMarginUsageUpdated,
                        positionMargin,
                        positionMarginUpdated,
                        positionLeverage,
                        positionLeverageUpdated,
                        liquidationPrice,
                        liquidationPriceUpdated,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Adjust Isolated Margin Input Summary not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
enum class IsolatedMarginAdjustmentType {
    Add,
    Remove
}

@JsExport
@Serializable
enum class IsolatedMarginInputType {
    Amount,
    Percent
}

@JsExport
@Serializable
data class AdjustIsolatedMarginInput(
    val market: String?,
    val type: IsolatedMarginAdjustmentType,
    val amount: String?,
    val amountPercent: String?,
    val amountInput: IsolatedMarginInputType?,
    val childSubaccountNumber: Int?,
    val adjustIsolatedMarginInputOptions: AdjustIsolatedMarginInputOptions?,
    val summary: AdjustIsolatedMarginInputSummary?
) {
    companion object {
        internal fun create(
            parser: ParserProtocol,
            data: InternalAdjustIsolatedMarginInputState?
        ): AdjustIsolatedMarginInput? {
            return if (data != null) {
                AdjustIsolatedMarginInput(
                    market = data.market,
                    type = data.type ?: IsolatedMarginAdjustmentType.Add,
                    amount = parser.asString(data.amount),
                    amountPercent = parser.asString(data.amountPercent),
                    amountInput = data.amountInput,
                    childSubaccountNumber = data.childSubaccountNumber,
                    adjustIsolatedMarginInputOptions = data.options,
                    summary = data.summary,
                )
            } else {
                null
            }
        }

        internal fun create(
            existing: AdjustIsolatedMarginInput?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): AdjustIsolatedMarginInput? {
            Logger.d { "creating Adjust Isolated Margin Input\n" }

            data?.let {
                val market = parser.asString(data["Market"])

                val type = parser.asString(data["Type"])?.let {
                    IsolatedMarginAdjustmentType.valueOf(it)
                } ?: IsolatedMarginAdjustmentType.Add

                val childSubaccountNumber = parser.asInt(data["ChildSubaccountNumber"])
                val amount = parser.asString(data["Amount"])
                val amountPercent = parser.asString(data["AmountPercent"])
                val amountInput = parser.asString(data["AmountInput"])?.let {
                    IsolatedMarginInputType.valueOf(it)
                }

                val adjustIsolatedMarginInputOptions = AdjustIsolatedMarginInputOptions.create(
                    existing?.adjustIsolatedMarginInputOptions,
                    parser,
                    parser.asMap(data["options"]),
                )
                val summary = AdjustIsolatedMarginInputSummary.create(
                    existing?.summary,
                    parser,
                    parser.asMap(data["summary"]),
                )

                return if (
                    existing?.market != market ||
                    existing?.type != type ||
                    existing.amount != amount ||
                    existing.amountPercent != amountPercent ||
                    existing.amountInput != amountInput ||
                    existing.childSubaccountNumber != childSubaccountNumber ||
                    existing.adjustIsolatedMarginInputOptions != adjustIsolatedMarginInputOptions ||
                    existing.summary !== summary
                ) {
                    AdjustIsolatedMarginInput(
                        market,
                        type,
                        amount,
                        amountPercent,
                        amountInput,
                        childSubaccountNumber,
                        adjustIsolatedMarginInputOptions,
                        summary,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Adjust Isolated Margin Input not valid" }
            return null
        }
    }
}
