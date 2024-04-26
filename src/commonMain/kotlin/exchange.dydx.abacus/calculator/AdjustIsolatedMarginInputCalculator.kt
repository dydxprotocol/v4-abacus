package exchange.dydx.abacus.calculator

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
        val account = parser.asNativeMap(state["account"])
        val wallet = parser.asNativeMap(state["wallet"])
        val isolatedMarginAdjustment = parser.asNativeMap(state["adjustIsolatedMargin"])
        val childSubaccountNumber = parser.asInt(isolatedMarginAdjustment?.get("childSubaccountNumber"))
        val type = parser.asString(isolatedMarginAdjustment?.get("type"))
        val parentSubaccount = if (parentSubaccountNumber != null) {
            parser.asNativeMap(
                parser.value(
                    account,
                    "subaccounts.$parentSubaccountNumber",
                ),
            )
        } else {
            null
        }
        val childSubaccount = if (childSubaccountNumber != null) {
            parser.asNativeMap(
                parser.value(
                    account,
                    "subaccounts.$childSubaccountNumber",
                ),
            )
        } else {
            null
        }

        return if (wallet != null && isolatedMarginAdjustment != null && type != null) {
            val modified = state.mutable()
            val parentTransferDelta = getModifiedTransferDelta(isolatedMarginAdjustment, true)
            val childTransferDelta = getModifiedTransferDelta(isolatedMarginAdjustment, false)

            val postParentSubaccountTransferWallet =
                subaccountTransformer.applyIsolatedMarginAdjustmentToWallet(
                    wallet,
                    subaccountNumber=parentSubaccountNumber,
                    parentTransferDelta,
                    parser,
                    "postOrder",
                )

            val postChildSubaccountTransferWallet =
                subaccountTransformer.applyIsolatedMarginAdjustmentToWallet(
                    wallet=postParentSubaccountTransferWallet,
                    subaccountNumber=childSubaccountNumber,
                    childTransferDelta,
                    parser,
                    "postOrder",
                )

            modified["summary"] = summaryForType(
                parentSubaccount,
                childSubaccount,
                isolatedMarginAdjustment,
                type,
            )
            modified["wallet"] = postChildSubaccountTransferWallet
            modified
        } else {
            state
        }
    }

    private fun getModifiedTransferDelta(
        isolatedMarginAdjustment: Map<String, Any>,
        isParentSubaccount: Boolean,
        ): Map<String, Double>? {
        val type = parser.asString(isolatedMarginAdjustment["type"])
        val amount = parser.asDouble(isolatedMarginAdjustment["amount"])
        if (type != null) {
            when (type) {
                "ADD" -> {
                    val multiplier =
                        if (isParentSubaccount) Numeric.double.NEGATIVE else Numeric.double.POSITIVE
                    val usdcSize = (amount ?: Numeric.double.ZERO) * multiplier

                    return mapOf(
                        "usdcSize" to usdcSize,
                    )
                }

                "REMOVE" -> {
                    val multiplier =
                        if (isParentSubaccount) Numeric.double.POSITIVE else Numeric.double.NEGATIVE
                    val usdcSize = (amount ?: Numeric.double.ZERO) * multiplier

                    return mapOf(
                        "usdcSize" to usdcSize,
                    )
                }

                else -> {
                    return null
                }
            }
        }

        return null
    }

    private fun summaryForType(
        parentSubaccount: Map<String, Any>?,
        childSubaccount: Map<String, Any>?,
        isolatedMarginAdjustment: Map<String, Any>,
        type: String,
    ): Map<String, Any> {
        val summary = mutableMapOf<String, Any>()
        val amount = parser.asDouble(isolatedMarginAdjustment["amount"])
        val crossCollateral = parser.asDouble(parser.value(parentSubaccount, "freeCollateral.postOrder"))
        val crossLeverage = parser.asDouble(parser.value(parentSubaccount, "leverage.postOrder"))
        val positionMargin = parser.asDouble(parser.value(childSubaccount, "margin.postOrder"))
        val positionLeverage = parser.asDouble(parser.value(childSubaccount, "leverage.postOrder"))

        when (type) {
            "ADD" -> {
                summary.safeSet("crossCollateral", crossCollateral)
                summary.safeSet("crossLeverage", crossLeverage)
                summary.safeSet("positionMargin", positionMargin)
                summary.safeSet("positionLeverage", positionLeverage)
                summary.safeSet("liquidationPrice", amount)
            }

            "REMOVE" -> {
                summary.safeSet("crossCollateral", crossCollateral)
                summary.safeSet("crossLeverage", crossLeverage)
                summary.safeSet("positionMargin", positionMargin)
                summary.safeSet("positionLeverage", positionLeverage)
                summary.safeSet("liquidationPrice", amount)
            }

            else -> {}
        }

        return summary
    }
}
