package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
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
    ): Map<String, Any>? {
        return if (account != null) {
            val subaccounts = parser.asMap(account["subaccounts"]) ?: return account
            var modified = account.mutable()
            for ((subaccountNumber, subaccount) in subaccounts) {
                val parentSubaccountNumber = subaccountNumber.toInt() % 128
                if (parentSubaccountNumber in subaccountNumbers) {
                    val key = "subaccounts.$subaccountNumber"
                    modified.safeSet(key, subaccountCalculator.calculate(parser.asNativeMap(subaccount), configs, markets, price, periods))
                }
            }
            modified = groupSubaccounts(modified)
            modified
        } else {
            null
        }
    }


    private fun groupSubaccounts(existing: Map<String, Any>): MutableMap<String, Any> {
        val modified = existing.mutable()
        val subaccounts = parser.asNativeMap(parser.value(existing, "subaccounts"))
        val subaccountNumbers = subaccounts?.keys?.mapNotNull { parser.asInt(it) }?.sorted()
        if (subaccountNumbers != null) {
            val groupedSubaccounts = mutableMapOf<String, Any>()
            for (subaccountNumber in subaccountNumbers) {
                val subaccount =
                    parser.asNativeMap(parser.value(subaccounts, "$subaccountNumber")) ?: break
                if (subaccountNumber < 128) {
                    // this is a parent subaccount
                    groupedSubaccounts["$subaccountNumber"] = subaccount
                } else {
                    val parentSubaccountNumber = subaccountNumber % 128
                    val parentSubaccount =
                        parser.asNativeMap(parser.value(subaccounts, "$parentSubaccountNumber"))
                    if (parentSubaccount != null) {
                        val openPositions =
                            parser.asNativeMap(parser.value(parentSubaccount, "openPositions"))
                        val modifiedOpenPositions = openPositions?.toMutableMap() ?: mutableMapOf()

                        val childOpenPositions =
                            parser.asNativeMap(parser.value(subaccount, "openPositions"))
                        if (childOpenPositions?.size == 1) {
                            for ((market, childOpenPosition) in childOpenPositions) {
                                val modifiedChildOpenPosition =
                                    parser.asMap(childOpenPosition)?.toMutableMap()
                                modifiedChildOpenPosition?.safeSet(
                                    "quoteBalance",
                                    subaccount["quoteBalance"]
                                )
                                modifiedChildOpenPosition?.safeSet(
                                    "freeCollateral",
                                    subaccount["freeCollateral"]
                                )
                                modifiedChildOpenPosition?.safeSet("equity", subaccount["equity"])
                                modifiedOpenPositions.safeSet(market, modifiedChildOpenPosition)
                            }
                        }
                        val modifiedParentSubaccount = parentSubaccount.toMutableMap()
                        modifiedParentSubaccount.safeSet("openPositions", modifiedOpenPositions)
                        modifiedParentSubaccount.safeSet("equity", sum(
                            parser.asMap(parentSubaccount["equity"]),
                            parser.asMap(subaccount["equity"])
                        ))
                        groupedSubaccounts["$parentSubaccountNumber"] = modifiedParentSubaccount
                    }
                }
            }
            modified.safeSet("groupedSubaccounts", groupedSubaccounts)
        } else {
            modified.safeSet("groupedSubaccounts", null)
        }
        return modified
    }

    private fun sum(value1: Map<String, Any>?, value2: Map<String, Any>?): Map<String, Any> {
        val current1 = parser.asDouble(value1?.get("current"))
        val current2 = parser.asDouble(value2?.get("current"))

        val postOrder1 = parser.asDouble(value1?.get("postOrder"))
        val postOrder2 = parser.asDouble(value2?.get("postOrder"))

        val postAllOrders1 = parser.asDouble(value1?.get("postAllOrders"))
        val postAllOrders2 = parser.asDouble(value2?.get("postAllOrders"))

        val result = mutableMapOf<String, Any>()
        if (current1 != null || current2 != null) {
            result["current"] = (current1 ?: 0.0) + (current2 ?: 0.0)
        }
        if (postOrder1 != null || postOrder2 != null) {
            result["postOrder"] = (postOrder1 ?: 0.0) + (postOrder2 ?: 0.0)
        }
        if (postAllOrders1 != null || postAllOrders2 != null) {
            result["postAllOrders"] = (postAllOrders1 ?: 0.0) + (postAllOrders2 ?: 0.0)
        }
        return result
    }

}
