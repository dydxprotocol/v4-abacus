package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.processor.base.ComparisonOrder
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet


internal fun TradingStateMachine.mergeFills(
    account: Map<String, Any>?,
    subaccountNumbers: List<Int>,
): Map<String, Any> {
    val modifiedAccount = account?.mutable() ?: mutableMapOf()
    for (subaccountNumber in subaccountNumbers) {
        val parentSubaccountNumber = subaccountNumber % NUM_PARENT_SUBACCOUNTS
        val groupedFills = parser.asNativeList(parser.value(account, "groupedSubaccounts.$parentSubaccountNumber.fills"))
        val fills = parser.asNativeList(parser.value(account, "subaccounts.$subaccountNumber.fills"))
        val mergedFills = ParsingHelper.merge(parser, groupedFills, fills, { obj, itemData ->
            // sort by fill datetime
            val time1 = parser.asDatetime(parser.value(obj, "createdAt"))
            val time2 = parser.asDatetime(parser.value(itemData, "createdAt"))
            var order = ParsingHelper.compare(time1, time2, true)
            if (order == ComparisonOrder.same) {
                // among fills with the same block
                // sort by subaccountNumber
                val subaccountNumber1 = parser.asInt(parser.value(obj, "subaccountNumber"))
                val subaccountNumber2 = parser.asInt(parser.value(itemData, "subaccountNumber"))
                order = ParsingHelper.compare(subaccountNumber1 ?: 0, subaccountNumber2 ?: 0, true)
            }
            if (order == ComparisonOrder.same) {
                // among fills with the same block and same subaccountNumber
                // sort by id
                val id1 = parser.asString(parser.value(obj, "id"))
                val id2 = parser.asString(parser.value(itemData, "id"))
                order = ParsingHelper.compare(id1, id2, true)
            }
            order
        }, { _, obj, itemData ->
            itemData
        }, false)

        modifiedAccount.safeSet("groupedSubaccounts.$parentSubaccountNumber.fills", mergedFills)
    }
    return modifiedAccount
}

internal fun TradingStateMachine.mergeTransfers(
    account: Map<String, Any>?,
    subaccountNumbers: List<Int>,
): Map<String, Any> {
    val modifiedAccount = account?.mutable() ?: mutableMapOf()
    for (subaccountNumber in subaccountNumbers) {
        val parentSubaccountNumber = subaccountNumber % NUM_PARENT_SUBACCOUNTS
        val groupedTransfers = parser.asNativeList(parser.value(account, "groupedSubaccounts.$parentSubaccountNumber.transfers"))
        val transfers = parser.asNativeList(parser.value(account, "subaccounts.$subaccountNumber.transfers"))
        val mergedTransfers = ParsingHelper.merge(parser, groupedTransfers, transfers, { obj, itemData ->
            // sort by fill datetime
            val time1 = parser.asDatetime(parser.value(obj, "createdAt"))
            val time2 = parser.asDatetime(parser.value(itemData, "createdAt"))
            var order = ParsingHelper.compare(time1, time2, true)
            if (order == ComparisonOrder.same) {
                // among fills with the same block
                // sort by subaccountNumber
                val subaccountNumber1 = parser.asInt(parser.value(obj, "subaccountNumber"))
                val subaccountNumber2 = parser.asInt(parser.value(itemData, "subaccountNumber"))
                order = ParsingHelper.compare(subaccountNumber1 ?: 0, subaccountNumber2 ?: 0, true)
            }
            if (order == ComparisonOrder.same) {
                // among fills with the same block and same subaccountNumber
                // sort by id
                val id1 = parser.asString(parser.value(obj, "id"))
                val id2 = parser.asString(parser.value(itemData, "id"))
                order = ParsingHelper.compare(id1, id2, true)
            }
            order
        }, { _, obj, itemData ->
            itemData
        }, true)

        modifiedAccount.safeSet("groupedSubaccounts.$parentSubaccountNumber.transfers", mergedTransfers)
    }
    return modifiedAccount
}