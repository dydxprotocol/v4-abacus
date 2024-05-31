package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

class AccountTransformer() {
    private val subaccountTransformer = SubaccountTransformer()
    internal fun applyTradeToAccount(
        account: Map<String, Any>?,
        subaccountNumber: Int?,
        trade: Map<String, Any>,
        market: Map<String, Any>?,
        parser: ParserProtocol,
        period: String,
        usePessimisticCollateralCheck: Boolean,
        useOptimisticCollateralCheck: Boolean
    ): Map<String, Any>? {
        val modified = account?.mutable() ?: return null
        val childSubaccountNumber =
            MarginModeCalculator.getChildSubaccountNumberForIsolatedMarginTrade(
                parser,
                account,
                subaccountNumber ?: 0,
                trade,
            )
        if (subaccountNumber == childSubaccountNumber) {
            // CROSS
            val subaccount = parser.asNativeMap(
                parser.value(
                    account,
                    "subaccounts.$subaccountNumber",
                ),
            ) ?: mapOf()
            val modifiedSubaccount =
                subaccountTransformer.applyTradeToSubaccount(
                    subaccount,
                    trade,
                    market,
                    parser,
                    period,
                    usePessimisticCollateralCheck,
                    useOptimisticCollateralCheck,
                )
            modified.safeSet("subaccounts.$subaccountNumber", modifiedSubaccount)
            return modified
        } else {
            val transferAmount = calculateIsolatedMarginTransferAmount(
                parser,
                trade,
            )
            if (transferAmount != null) {
                val subaccount = parser.asNativeMap(
                    parser.value(
                        account,
                        "subaccounts.$subaccountNumber",
                    ),
                ) ?: mapOf()

                val modifiedSubaccount =
                    subaccountTransformer.applyTransferToSubaccount(
                        subaccount,
                        transferAmount * -1.0,
                        parser,
                        period,
                    )
                modified.safeSet("subaccounts.$subaccountNumber", modifiedSubaccount)
            }

            val childSubaccount = parser.asNativeMap(
                parser.value(
                    account,
                    "subaccounts.$childSubaccountNumber",
                ),
            ) ?: mapOf()

            val modifiedChildSubaccount =
                subaccountTransformer.applyTradeToSubaccount(
                    childSubaccount,
                    trade,
                    market,
                    parser,
                    period,
                    usePessimisticCollateralCheck,
                    useOptimisticCollateralCheck,
                    transferAmount,
                )
            modified.safeSet("subaccounts.$childSubaccountNumber", modifiedChildSubaccount)
            return modified
        }
    }

    fun calculateIsolatedMarginTransferAmount(
        parser: ParserProtocol,
        trade: Map<String, Any>,
    ): Double? {
        val targetLeverage = parser.asDouble(trade["targetLeverage"]) ?: return null
        return if (targetLeverage == 0.0) {
            null
        } else {
            val usdcSize = parser.asDouble(parser.value(trade, "size.usdcSize")) ?: return null
            usdcSize / targetLeverage
        }
    }
}
