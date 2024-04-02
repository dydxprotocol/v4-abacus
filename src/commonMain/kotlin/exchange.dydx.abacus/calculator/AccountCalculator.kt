package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
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
                val parentSubaccountNumber = subaccountNumber.toInt() % NUM_PARENT_SUBACCOUNTS
                if (parentSubaccountNumber in subaccountNumbers) {
                    val key = "subaccounts.$subaccountNumber"
                    modified.safeSet(
                        key,
                        subaccountCalculator.calculate(
                            parser.asNativeMap(subaccount),
                            configs,
                            markets,
                            price,
                            periods,
                        ),
                    )
                }
            }
            modified = groupSubaccounts(modified, markets)
            modified
        } else {
            null
        }
    }

    private fun groupSubaccounts(
        existing: Map<String, Any>,
        markets: Map<String, Any>?
    ): MutableMap<String, Any> {
        val modified = existing.mutable()
        val subaccounts = parser.asNativeMap(parser.value(existing, "subaccounts"))
        val subaccountNumbers = subaccounts?.keys?.mapNotNull { parser.asInt(it) }?.sorted()
        if (subaccountNumbers != null) {
            val groupedSubaccounts = mutableMapOf<String, Any>()
            for (subaccountNumber in subaccountNumbers) {
                val subaccount =
                    parser.asNativeMap(parser.value(subaccounts, "$subaccountNumber")) ?: break
                if (subaccountNumber < NUM_PARENT_SUBACCOUNTS) {
                    // this is a parent subaccount
                    groupedSubaccounts["$subaccountNumber"] = subaccount
                } else {
                    val parentSubaccountNumber = subaccountNumber % NUM_PARENT_SUBACCOUNTS
                    val parentSubaccount = parser.asNativeMap(
                        parser.value(
                            groupedSubaccounts,
                            "$parentSubaccountNumber",
                        ),
                    ) ?: parser.asNativeMap(parser.value(subaccounts, "$parentSubaccountNumber"))
                        ?: mapOf("subaccountNumber" to parentSubaccountNumber)

                    val childOpenPositions =
                        parser.asNativeMap(parser.value(subaccount, "openPositions"))
                    val modifiedParentSubaccount = if (childOpenPositions != null) {
                        mergeChildOpenPositions(
                            parentSubaccount,
                            subaccountNumber,
                            subaccount,
                            childOpenPositions,
                        )
                    } else {
                        val orders = parser.asNativeMap(parser.value(subaccount, "orders"))
                        if (orders != null) {
                            mergeChildPendingPositions(
                                parentSubaccount,
                                subaccountNumber,
                                subaccount,
                                orders,
                                markets,
                            )
                        } else {
                            parentSubaccount
                        }
                    }
                    groupedSubaccounts["$parentSubaccountNumber"] =
                        sumEquity(modifiedParentSubaccount, subaccount)
                }
            }
            modified.safeSet("groupedSubaccounts", groupedSubaccounts)
        } else {
            modified.safeSet("groupedSubaccounts", null)
        }
        return modified
    }

    private fun mergeChildOpenPositions(
        parentSubaccount: Map<String, Any>,
        childSubaccountNumber: Int,
        childSubaccount: Map<String, Any>,
        childOpenPositions: Map<String, Any>,
    ): Map<String, Any> {
        val openPositions =
            parser.asNativeMap(parser.value(parentSubaccount, "openPositions"))
        val modifiedOpenPositions = openPositions?.toMutableMap() ?: mutableMapOf()
        for ((market, childOpenPosition) in childOpenPositions) {
            val modifiedChildOpenPosition =
                parser.asMap(childOpenPosition)?.toMutableMap()
            modifiedChildOpenPosition?.safeSet(
                "childSubaccountNumber",
                childSubaccountNumber,
            )
            modifiedChildOpenPosition?.safeSet(
                "quoteBalance",
                childSubaccount["quoteBalance"],
            )
            modifiedChildOpenPosition?.safeSet(
                "freeCollateral",
                childSubaccount["freeCollateral"],
            )
            modifiedChildOpenPosition?.safeSet("equity", childSubaccount["equity"])
            modifiedOpenPositions.safeSet(market, modifiedChildOpenPosition)
        }

        val modifiedParentSubaccount = parentSubaccount.toMutableMap()
        modifiedParentSubaccount.safeSet("openPositions", modifiedOpenPositions)
        return modifiedParentSubaccount
    }

    private fun mergeChildPendingPositions(
        parentSubaccount: Map<String, Any>,
        childSubaccountNumber: Int,
        childSubaccount: Map<String, Any>,
        childOrders: Map<String, Any>,
        markets: Map<String, Any>?,
    ): Map<String, Any> {
        // Each empty subaccount should have order for one market only
        // Just in case it has more than one market, we will create
        // two separate pending positions.
        val pendingByMarketId = mutableMapOf<String, Any>()
        for ((orderId, order) in childOrders) {
            val marketId =
                parser.asString(parser.value(order, "marketId")) ?: continue
            val pending =
                pendingByMarketId[marketId] as? MutableMap<String, Any>
            if (pending == null) {
                pendingByMarketId[marketId] = mutableMapOf(
                    "firstOrderId" to orderId,
                    "orderCount" to 1,
                )
            } else {
                pendingByMarketId.safeSet(
                    "orderCount",
                    (parser.asInt(pending["orderCount"]) ?: 0) + 1,
                )
                pendingByMarketId.safeSet(marketId, pending)
            }
        }

        val modifiedPendingPositions = mutableListOf<Any>()
        for ((marketId, pending) in pendingByMarketId) {
            val market = parser.asMap(markets?.get(marketId)) ?: continue
            val assetId = parser.asString(market["assetId"]) ?: continue

            val modifiedPendingPosition = mutableMapOf<String, Any>()
            modifiedPendingPosition.safeSet("assetId", assetId)
            modifiedPendingPosition.safeSet(
                "firstOrderId",
                parser.value(pending, "firstOrderId"),
            )
            modifiedPendingPosition.safeSet(
                "orderCount",
                parser.value(pending, "orderCount"),
            )
            modifiedPendingPosition.safeSet(
                "quoteBalance",
                childSubaccount["quoteBalance"],
            )
            modifiedPendingPosition.safeSet(
                "freeCollateral",
                childSubaccount["freeCollateral"],
            )
            modifiedPendingPosition.safeSet("equity", childSubaccount["equity"])
            modifiedPendingPositions.add(modifiedPendingPosition)
        }
        val modifiedParentSubaccount = parentSubaccount.toMutableMap()
        modifiedParentSubaccount.safeSet(
            "pendingPositions",
            modifiedPendingPositions.sortedWith { pending1, pending2 ->
                val marketId1 = parser.asString(parser.value(pending1, "assetId"))
                val marketId2 = parser.asString(parser.value(pending2, "assetId"))
                if (marketId1 != null && marketId2 != null) {
                    marketId1.compareTo(marketId2)
                } else {
                    0
                }
            },
        )
        return modifiedParentSubaccount
    }

    private fun sumEquity(
        parentSubaccount: Map<String, Any>,
        childSubaccount: Map<String, Any>
    ): Map<String, Any> {
        val modifiedParentSubaccount = parentSubaccount.toMutableMap()
        modifiedParentSubaccount.safeSet(
            "equity",
            sum(
                parser.asMap(parentSubaccount["equity"]),
                parser.asMap(childSubaccount["equity"]),
            ),
        )
        return modifiedParentSubaccount
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
