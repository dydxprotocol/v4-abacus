package exchange.dydx.abacus.calculator.v2

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import kollections.toIList

internal class AccountCalculatorV2(
    val parser: ParserProtocol,
    private val useParentSubaccount: Boolean,
) {
    fun calculate(
        account: InternalAccountState,
        subaccountNumbers: List<Int>,
//        configs: Map<String, Any>?,
//        markets: Map<String, Any>?,
//        price: Map<String, Any>?,
//        periods: Set<CalculationPeriod>,
    ): InternalAccountState {
        for ((subaccountNumber, subaccount) in account.subaccounts) {
            val parentSubaccountNumber = subaccountNumber % NUM_PARENT_SUBACCOUNTS
            if (parentSubaccountNumber in subaccountNumbers) {
//                account.subaccounts[subaccountNumber] = subaccountCalculator.calculate(
//                    subaccount,
//                    configs,
//                    markets,
//                    price,
//                    periods,
//                )
            }
        }

        if (useParentSubaccount) {
            return groupSubaccounts(
                account = account,
//                markets = markets,
            )
        } else {
            return account
        }
    }

    private fun groupSubaccounts(
        account: InternalAccountState,
//        markets: Map<String, Any>?
    ): InternalAccountState {
        val subaccounts = account.subaccounts
        val subaccountNumbers = subaccounts.keys.sorted()

        val groupedSubaccounts = mutableMapOf<Int, InternalSubaccountState>()
        for (subaccountNumber in subaccountNumbers) {
            val subaccount = subaccounts[subaccountNumber] ?: continue
            if (subaccountNumber < NUM_PARENT_SUBACCOUNTS) {
                // this is a parent subaccount
                groupedSubaccounts[subaccountNumber] = subaccount
            } else {
                val parentSubaccountNumber = subaccountNumber % NUM_PARENT_SUBACCOUNTS
                var parentSubaccount = groupedSubaccounts[parentSubaccountNumber]
                    ?: subaccounts[parentSubaccountNumber]
                    ?: InternalSubaccountState(subaccountNumber = parentSubaccountNumber)

                // TODO: Add other merges

                parentSubaccount = mergeOrders(
                    parentSubaccount = parentSubaccount,
                    childSubaccount = subaccount,
                )
                groupedSubaccounts[parentSubaccountNumber] = parentSubaccount
            }
        }
        account.groupedSubaccounts = groupedSubaccounts
        return account
    }

    private fun mergeOrders(
        parentSubaccount: InternalSubaccountState,
        childSubaccount: InternalSubaccountState,
    ): InternalSubaccountState {
        val parentOrders = parentSubaccount.orders ?: emptyList()
        val childOrders = childSubaccount.orders ?: emptyList()
        parentSubaccount.orders = (parentOrders + childOrders).toIList()
        return parentSubaccount
    }
}
