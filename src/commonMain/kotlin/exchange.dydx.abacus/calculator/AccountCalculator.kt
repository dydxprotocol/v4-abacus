package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

class AccountCalculator(val parser: ParserProtocol) {
    private val subaccountCalculator = SubaccountCalculator(parser)

    private val kChildSubaccountMod = NUM_PARENT_SUBACCOUNTS

    internal fun calculate(
        account: Map<String, Any>?,
        subaccountNumbers: List<Int>,
        configs: Map<String, Any>?,
        markets: Map<String, Any>?,
        price: Map<String, Any>?,
        periods: Set<CalculationPeriod>,
    ): Map<String, Any>? {
        return if (account != null) {
            val subaccounts = parser.asMap(account["subaccounts"]) ?: return account
            val modified = account.mutable()
            for ((subaccountNumber, subaccount) in subaccounts) {
                val parentSubaccountNumber = subaccountNumber.toInt() % kChildSubaccountMod
                if (parentSubaccountNumber in subaccountNumbers) {
                    val key = "subaccounts.$subaccountNumber"
                    modified.safeSet(key, subaccountCalculator.calculate(parser.asNativeMap(subaccount), configs, markets, price, periods))
                }
            }
            modified
        } else {
            null
        }
    }
}
