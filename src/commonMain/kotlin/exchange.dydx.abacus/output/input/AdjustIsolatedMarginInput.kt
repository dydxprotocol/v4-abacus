package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class AdjustIsolatedMarginInputOptions(
    val needsSize: Boolean?,
) {
    companion object {
        internal fun create(
            existing: AdjustIsolatedMarginInputOptions?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): AdjustIsolatedMarginInputOptions? {
            Logger.d { "creating Adjust Isolated Margin Input Options\n" }

            data?.let {
                val needsSize = parser.asBool(data["needsSize"])

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
    val crossMarginUsage: Double?,
    val positionMargin: Double?,
    val positionLeverage: Double?,
    val liquidationPrice: Double?,
) {
    companion object {
        internal fun create(
            existing: AdjustIsolatedMarginInputSummary?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): AdjustIsolatedMarginInputSummary? {
            Logger.d { "creating Adjust Isolated Margin Input Summary\n" }

            data?.let {
                val crossFreeCollateral = parser.asDouble(data["crossFreeCollateral"])
                val crossMarginUsage = parser.asDouble(data["crossMarginUsage"])
                val positionMargin = parser.asDouble(data["positionMargin"])
                val positionLeverage = parser.asDouble(data["positionLeverage"])
                val liquidationPrice = parser.asDouble(data["liquidationPrice"])

                return if (
                    existing?.crossFreeCollateral != crossFreeCollateral ||
                    existing?.crossMarginUsage != crossMarginUsage ||
                    existing?.positionMargin != positionMargin ||
                    existing?.positionLeverage != positionLeverage ||
                    existing?.liquidationPrice != liquidationPrice
                ) {
                    AdjustIsolatedMarginInputSummary(
                        crossFreeCollateral,
                        crossMarginUsage,
                        positionMargin,
                        positionLeverage,
                        liquidationPrice,
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
enum class IsolatedMarginAdjustmentType(val rawValue: String) {
    Add("ADD"),
    Remove("REMOVE");

    companion object {
        operator fun invoke(rawValue: String) =
            IsolatedMarginAdjustmentType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class AdjustIsolatedMarginInput(
    val type: IsolatedMarginAdjustmentType,
    val amount: String?,
    val childSubaccountNumber: Int?,
    val adjustIsolatedMarginInputOptions: AdjustIsolatedMarginInputOptions?,
    val summary: AdjustIsolatedMarginInputSummary?,
    val errors: String?,
    val errorMessage: String?,
    val fee: Double?,
) {
    companion object {
        internal fun create(
            existing: AdjustIsolatedMarginInput?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): AdjustIsolatedMarginInput? {
            Logger.d { "creating Adjust Isolated Margin Input\n" }

            data?.let {
                val type = parser.asString(data["type"])?.let {
                    IsolatedMarginAdjustmentType.invoke(it)
                }?: IsolatedMarginAdjustmentType.Add

                val childSubaccountNumber = parser.asInt(data["childSubaccountNumber"])
                val amount = parser.asString(data["amount"])
                val fee = parser.asDouble(data["fee"])
                val adjustIsolatedMarginInputOptions = AdjustIsolatedMarginInputOptions.create(
                    existing?.adjustIsolatedMarginInputOptions,
                    parser,
                    parser.asMap(data["adjustIsolatedMarginInputOptions"]),
                )
                val summary = AdjustIsolatedMarginInputSummary.create(
                    existing?.summary,
                    parser,
                    parser.asMap(data["summary"]),
                )

                val errors = parser.asString(data["errors"])

                val errorMessage: String? =
                    if (errors != null) {
                        val errorArray = parser.decodeJsonArray(errors)
                        val firstError = parser.asMap(errorArray?.first())
                        parser.asString(firstError?.get("message"))
                    } else {
                        null
                    }

                return if (
                    existing?.type != type ||
                    existing.amount != amount ||
                    existing.childSubaccountNumber != childSubaccountNumber ||
                    existing.adjustIsolatedMarginInputOptions != adjustIsolatedMarginInputOptions ||
                    existing.summary !== summary ||
                    existing.errors !== errors ||
                    existing.errorMessage != errorMessage ||
                    existing.fee != fee
                ) {
                    AdjustIsolatedMarginInput(
                        type,
                        amount,
                        childSubaccountNumber,
                        adjustIsolatedMarginInputOptions,
                        summary,
                        errors,
                        errorMessage,
                        fee,
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
