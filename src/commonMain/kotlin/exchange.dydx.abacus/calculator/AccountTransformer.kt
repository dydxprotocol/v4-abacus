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
        val targetSubaccountNumber =
            MarginModeCalculator.getChildSubaccountNumberForIsolatedMarginTrade(
                parser,
                account,
                subaccountNumber ?: 0,
                parser.asString(trade["marketId"]),
            )
        val subaccount = parser.asNativeMap(
            parser.value(
                account,
                "subaccounts.$targetSubaccountNumber",
            )
        ) ?: mapOf()
        if (targetSubaccountNumber != subaccountNumber) {
            MarginModeCalculator.getChildSubaccountNumberForIsolatedMarginTrade(
                parser,
                account,
                subaccountNumber ?: 0,
                parser.asString(trade["marketId"]),
            )
        }
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
        modified.safeSet("subaccounts.$targetSubaccountNumber", modifiedSubaccount)
        return modified
    }
}
