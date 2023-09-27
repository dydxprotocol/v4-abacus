package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.AppVersion
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

class AccountCalculator(val parser: ParserProtocol) {
    private val subaccountCalculator = SubaccountCalculator(parser)
    internal fun calculate(
        account: Map<String, Any>?,
        subaccountNumbers: List<Int>,
        configs: Map<String, Any>?,
        markets: Map<String, Any>?,
        price: Map<String, Any>?,
        periods: Set<CalculationPeriod>,
        version: AppVersion
    ): Map<String, Any>? {
        return if (account != null) {
            val modified = account.mutable()
            for (subaccountNumber in subaccountNumbers) {
                val key = "subaccounts.$subaccountNumber"
                val subaccount = parser.asMap(parser.value(account, key))
                if (subaccount != null) {
                    modified.safeSet(key, subaccountCalculator.calculate(subaccount, configs, markets, price, periods, version))
                }
            }
            modified
        } else null
    }
}