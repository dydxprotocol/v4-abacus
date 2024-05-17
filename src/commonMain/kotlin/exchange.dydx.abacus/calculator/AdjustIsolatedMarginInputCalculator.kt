package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class AdjustIsolatedMarginInputCalculator(val parser: ParserProtocol) {
    private val subaccountTransformer = SubaccountTransformer()

    internal fun calculate(
        state: Map<String, Any>,
        parentSubaccountNumber: Int?,
    ): Map<String, Any> {
        val wallet = parser.asNativeMap(state["wallet"])
        val isolatedMarginAdjustment = parser.asNativeMap(state["adjustIsolatedMargin"])
        val childSubaccountNumber = parser.asInt(isolatedMarginAdjustment?.get("ChildSubaccountNumber"))
        val type = parser.asString(isolatedMarginAdjustment?.get("Type"))?.let {
            IsolatedMarginAdjustmentType.valueOf(it)
        } ?: IsolatedMarginAdjustmentType.Add

        return if (wallet != null && isolatedMarginAdjustment != null) {
            val modified = state.mutable()
            val parentTransferDelta = getModifiedTransferDelta(isolatedMarginAdjustment, true)
            val childTransferDelta = getModifiedTransferDelta(isolatedMarginAdjustment, false)

            val walletPostParentSubaccountTransfer =
                subaccountTransformer.applyIsolatedMarginAdjustmentToWallet(
                    wallet,
                    subaccountNumber = parentSubaccountNumber,
                    parentTransferDelta,
                    parser,
                    "postOrder",
                )

            val walletPostChildSubaccountTransfer =
                subaccountTransformer.applyIsolatedMarginAdjustmentToWallet(
                    wallet = walletPostParentSubaccountTransfer,
                    subaccountNumber = childSubaccountNumber,
                    childTransferDelta,
                    parser,
                    "postOrder",
                )

            val modifiedParentSubaccount = parser.asNativeMap(parser.value(walletPostChildSubaccountTransfer, "account.subaccounts.$parentSubaccountNumber"))
            val modifiedChildSubaccount = parser.asNativeMap(parser.value(walletPostChildSubaccountTransfer, "account.subaccounts.$childSubaccountNumber"))
            val modifiedIsolatedMarginAdjustment = finalize(isolatedMarginAdjustment, modifiedParentSubaccount, modifiedChildSubaccount, type)

            modified["adjustIsolatedMargin"] = modifiedIsolatedMarginAdjustment
            modified["wallet"] = walletPostChildSubaccountTransfer
            modified
        } else {
            state
        }
    }

    private fun getModifiedTransferDelta(
        isolatedMarginAdjustment: Map<String, Any>,
        isParentSubaccount: Boolean,
    ): Map<String, Double> {
        val type = parser.asString(isolatedMarginAdjustment["Type"])?.let {
            IsolatedMarginAdjustmentType.valueOf(it)
        } ?: IsolatedMarginAdjustmentType.Add
        val amount = parser.asDouble(isolatedMarginAdjustment["Amount"])

        when (type) {
            IsolatedMarginAdjustmentType.Add -> {
                val multiplier =
                    if (isParentSubaccount) Numeric.double.NEGATIVE else Numeric.double.POSITIVE
                val usdcSize = (amount ?: Numeric.double.ZERO) * multiplier

                return mapOf(
                    "usdcSize" to usdcSize,
                )
            }

            IsolatedMarginAdjustmentType.Remove -> {
                val multiplier =
                    if (isParentSubaccount) Numeric.double.POSITIVE else Numeric.double.NEGATIVE
                val usdcSize = (amount ?: Numeric.double.ZERO) * multiplier

                return mapOf(
                    "usdcSize" to usdcSize,
                )
            }
        }
    }

    private fun summaryForType(
        parentSubaccount: Map<String, Any>?,
        childSubaccount: Map<String, Any>?,
        type: IsolatedMarginAdjustmentType,
    ): Map<String, Any> {
        val summary = mutableMapOf<String, Any>()
        val crossCollateral = parentSubaccount?.get("freeCollateral")
        val crossMarginUsage = parentSubaccount?.get("marginUsage")
        val openPositions = parser.asNativeMap(childSubaccount?.get("openPositions"))
        val marketId = openPositions?.keys?.firstOrNull()
        val positionMargin = childSubaccount?.get("freeCollateral")
        val positionLeverage = parser.value(childSubaccount, "openPositions.$marketId.leverage")
        val liquidationPrice = parser.value(childSubaccount, "openPositions.$marketId.liquidationPrice")

        when (type) {
            IsolatedMarginAdjustmentType.Add -> {
                summary.safeSet("crossFreeCollateral", crossCollateral)
                summary.safeSet("crossMarginUsage", crossMarginUsage)
                summary.safeSet("positionMargin", positionMargin)
                summary.safeSet("positionLeverage", positionLeverage)
                summary.safeSet("liquidationPrice", liquidationPrice)
            }

            IsolatedMarginAdjustmentType.Remove -> {
                summary.safeSet("crossFreeCollateral", crossCollateral)
                summary.safeSet("crossMarginUsage", crossMarginUsage)
                summary.safeSet("positionMargin", positionMargin)
                summary.safeSet("positionLeverage", positionLeverage)
                summary.safeSet("liquidationPrice", liquidationPrice)
            }
        }

        return summary
    }

    private fun amountField(): Map<String, Any> {
        return mapOf(
            "field" to "amount",
            "type" to "double",
        )
    }

    private fun requiredFields(): List<Any> {
        return listOf(
            amountField(),
        )
    }

    private fun calculatedOptionsFromField(fields: List<Any>?): Map<String, Any>? {
        fields?.let {
            val options = mutableMapOf<String, Any>(
                "needsSize" to false,
            )

            for (item in fields) {
                parser.asNativeMap(item)?.let { field ->
                    when (parser.asString(field["field"])) {
                        "amount" -> {
                            options["needsSize"] = true
                        }
                    }
                }
            }

            return options
        }

        return null
    }

    private fun finalize(
        isolatedMarginAdjustment: Map<String, Any>,
        parentSubaccount: Map<String, Any>?,
        childSubaccount: Map<String, Any>?,
        type: IsolatedMarginAdjustmentType,
    ): Map<String, Any> {
        val modified = isolatedMarginAdjustment.mutable()
        val fields = requiredFields()
        modified.safeSet("fields", fields)
        modified.safeSet("options", calculatedOptionsFromField(fields))
        modified.safeSet("summary", summaryForType(parentSubaccount, childSubaccount, type))
        return modified
    }
}
