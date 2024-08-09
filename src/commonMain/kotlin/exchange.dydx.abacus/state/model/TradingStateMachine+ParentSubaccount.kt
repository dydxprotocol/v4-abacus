package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.processor.base.ComparisonOrder
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.ParsingHelper
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

internal fun TradingStateMachine.mergeFills(
    account: InternalAccountState,
    subaccountNumbers: List<Int>,
): InternalAccountState {
    for (subaccountNumber in subaccountNumbers) {
        val parentSubaccountNumber = subaccountNumber % NUM_PARENT_SUBACCOUNTS
        val groupedFills = account.groupedSubaccounts[parentSubaccountNumber]?.fills ?: emptyList()
        val fills = account.subaccounts[subaccountNumber]?.fills ?: emptyList()
        val mergedFills = ParsingHelper.mergeTyped(
            parser = parser,
            existing = groupedFills,
            incoming = fills,
            comparison = { obj, itemData ->
                // sort by fill datetime
                val time1 = obj.createdAtMilliseconds
                val time2 = itemData.createdAtMilliseconds
                var order = ParsingHelper.compare(time1, time2, true)
                if (order == ComparisonOrder.same) {
                    // among fills with the same block
                    // sort by subaccountNumber
                    val subaccountNumber1 = obj.subaccountNumber
                    val subaccountNumber2 = itemData.subaccountNumber
                    order = ParsingHelper.compare(subaccountNumber1 ?: 0, subaccountNumber2 ?: 0, true)
                }
                if (order == ComparisonOrder.same) {
                    // among fills with the same block and same subaccountNumber
                    // sort by id
                    val id1 = obj.id
                    val id2 = itemData.id
                    order = ParsingHelper.compare(id1, id2, true)
                }
                order
            },
            createObject = { _, obj, itemData ->
                itemData
            },
            syncItems = false,
        )

        account.groupedSubaccounts[parentSubaccountNumber]?.fills = mergedFills
    }
    return account
}

internal fun TradingStateMachine.mergeFillsDeprecated(
    account: Map<String, Any>?,
    subaccountNumbers: List<Int>,
): Map<String, Any>? {
    if (subaccountNumbers.isEmpty()) return account
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
    account: InternalAccountState,
    subaccountNumbers: List<Int>,
): InternalAccountState {
    for (subaccountNumber in subaccountNumbers) {
        val parentSubaccountNumber = subaccountNumber % NUM_PARENT_SUBACCOUNTS
        val groupedTransfers = account.groupedSubaccounts[parentSubaccountNumber]?.transfers
        val transfers = account.subaccounts[subaccountNumber]?.transfers
        val mergedTransfers = ParsingHelper.mergeTyped(
            parser = parser,
            existing = groupedTransfers,
            incoming = transfers,
            comparison = { obj, itemData ->
                // sort by fill datetime
                val time1 = obj.updatedAtMilliseconds
                val time2 = itemData.updatedAtMilliseconds
                var order = ParsingHelper.compare(time1, time2, true)
                if (order == ComparisonOrder.same) {
                    // among fills with the same block
                    // sort by id
                    val id1 = obj.id
                    val id2 = itemData.id
                    order = ParsingHelper.compare(id1, id2, true)
                }
                order
            },
            createObject = { _, obj, itemData ->
                itemData
            },
            syncItems = false,
        )
        account.groupedSubaccounts[parentSubaccountNumber]?.transfers = mergedTransfers
    }
    return account
}

internal fun TradingStateMachine.mergeTransfersDeprecated(
    account: Map<String, Any>?,
    subaccountNumbers: List<Int>,
): Map<String, Any>? {
    if (subaccountNumbers.isEmpty()) return account
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
