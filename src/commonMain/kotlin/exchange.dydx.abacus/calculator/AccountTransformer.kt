package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMapOf

class AccountTransformer() {
    private val subaccountTransformer = SubaccountTransformer()
    internal fun applyTradeToAccount(
        account: IMap<String, Any>?,
        subaccountNumber: Int?,
        trade: IMap<String, Any>,
        parser: ParserProtocol,
        period: String
    ): IMap<String, Any>? {
        val modified = account?.mutable() ?: return null
        val subaccount = if (subaccountNumber != null) parser.asMap(
            parser.value(
                account,
                "subaccounts.$subaccountNumber"
            )
        ) ?: iMapOf() else null
        val modifiedSubaccount =
            subaccountTransformer.applyTradeToSubaccount(subaccount, trade, parser, period)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedSubaccount)
        return modified
    }
}