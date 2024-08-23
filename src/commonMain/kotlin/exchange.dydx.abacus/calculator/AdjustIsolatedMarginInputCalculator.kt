package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.output.input.IsolatedMarginInputType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.MARGIN_COLLATERALIZATION_CHECK_BUFFER
import exchange.dydx.abacus.utils.MAX_LEVERAGE_BUFFER_PERCENT
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kotlin.math.max
import kotlin.math.min

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

        val markets = parser.asNativeMap(state["markets"])
        val marketId = isolatedMarginAdjustment?.get("Market")
        val market = if (marketId != null) parser.asNativeMap(markets?.get(marketId)) else null

        return if (wallet != null && isolatedMarginAdjustment != null && market != null) {
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
            val updatedIsolatedMarginAdjustment = calculateAmounts(isolatedMarginAdjustment, modifiedParentSubaccount, modifiedChildSubaccount, market, type)

            modified["adjustIsolatedMargin"] = finalize(updatedIsolatedMarginAdjustment, modifiedParentSubaccount, modifiedChildSubaccount, type)
            modified["wallet"] = walletPostChildSubaccountTransfer
            modified
        } else {
            state
        }
    }

    private fun calculateAmounts(
        adjustIsolatedMargin: Map<String, Any>,
        parentSubaccount: Map<String, Any>?,
        childSubaccount: Map<String, Any>?,
        market: Map<String, Any>,
        type: IsolatedMarginAdjustmentType,
    ): MutableMap<String, Any> {
        val modified = adjustIsolatedMargin.mutable()
        val inputType = parser.asString(modified["AmountInput"])?.let {
            IsolatedMarginInputType.valueOf(it)
        }

        if (inputType != null) {
            val notionalTotal = parser.asDouble(parser.value(childSubaccount, "notionalTotal.current")) ?: return modified
            val equity = parser.asDouble(parser.value(childSubaccount, "equity.current"))
            val availableCollateralToTransfer = parser.asDouble(parser.value(parentSubaccount, "freeCollateral.current"))

            val baseAmount = when (type) {
                IsolatedMarginAdjustmentType.Add -> availableCollateralToTransfer
                IsolatedMarginAdjustmentType.Remove -> equity
            }

            val amountPercent = parser.asDouble(modified["AmountPercent"])
            val amountValue = parser.asDouble(modified["Amount"])

            val initialMarginFraction =
                parser.asDouble(parser.value(market, "configs.effectiveInitialMarginFraction"))
                    ?: return modified
            val maxMarketLeverage = if (initialMarginFraction <= Numeric.double.ZERO) {
                return modified
            } else {
                Numeric.double.ONE / initialMarginFraction
            }

            when (inputType) {
                IsolatedMarginInputType.Amount -> {
                    if (baseAmount != null && baseAmount > Numeric.double.ZERO && amountValue != null) {
                        when (type) {
                            IsolatedMarginAdjustmentType.Add -> {
                                val percent = amountValue / baseAmount
                                modified.safeSet("AmountPercent", percent.toString())
                            }
                            IsolatedMarginAdjustmentType.Remove -> {
                                val maxRemovableAmount = baseAmount - notionalTotal / (maxMarketLeverage * MAX_LEVERAGE_BUFFER_PERCENT)
                                if (maxRemovableAmount > Numeric.double.ZERO) {
                                    val percent = amountValue / maxRemovableAmount
                                    modified.safeSet("AmountPercent", percent.toString())
                                } else {
                                    modified.safeSet("AmountPercent", null)
                                }
                            }
                        }
                    } else {
                        modified.safeSet("AmountPercent", null)
                    }
                }
                IsolatedMarginInputType.Percent -> {
                    if (baseAmount != null && baseAmount >= Numeric.double.ZERO && amountPercent != null) {
                        when (type) {
                            IsolatedMarginAdjustmentType.Add -> {
                                // The amount to add is a percentage of all add-able margin (your parent subaccount's free collateral)
                                val amount = baseAmount * amountPercent
                                // We leave behind MARGIN_COLLATERALIZATION_CHECK_BUFFER to pass collateralization checks
                                val cappedAmount = min(max(baseAmount - MARGIN_COLLATERALIZATION_CHECK_BUFFER, 0.0), amount)
                                modified.safeSet("Amount", cappedAmount.toString())
                            }
                            IsolatedMarginAdjustmentType.Remove -> {
                                // The amount to remove is a percentage of all remov-able margin (100% puts you at the market's max leveage)
                                // leverage = notional total / equity
                                // marketMaxLeverage = notional total / (currentEquity - amount)
                                // amount = currentEquity - notionalTotal / marketMaxLeverage
                                val amountToRemove = baseAmount - notionalTotal / (maxMarketLeverage * MAX_LEVERAGE_BUFFER_PERCENT)
                                if (amountToRemove >= Numeric.double.ZERO) {
                                    val amount = amountToRemove * amountPercent
                                    modified.safeSet("Amount", amount.toString())
                                } else {
                                    modified.safeSet("Amount", null)
                                }
                            }
                        }
                    } else {
                        modified.safeSet("Amount", null)
                    }
                }
            }
        }
        return modified
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
        val positionMargin = parser.value(childSubaccount, "equity")
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
