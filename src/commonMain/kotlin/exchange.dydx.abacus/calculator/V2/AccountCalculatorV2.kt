package exchange.dydx.abacus.calculator.v2

import abs
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.MarketConfigs
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalMarketState
import exchange.dydx.abacus.state.internalstate.InternalMarketSummaryState
import exchange.dydx.abacus.state.internalstate.InternalPendingPositionCalculated
import exchange.dydx.abacus.state.internalstate.InternalPerpetualPendingPosition
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import kollections.toIList

internal class AccountCalculatorV2(
    val parser: ParserProtocol,
    private val useParentSubaccount: Boolean,
    private val subaccountCalculator: SubaccountCalculatorV2 = SubaccountCalculatorV2(parser),
) {
    fun calculate(
        account: InternalAccountState,
        subaccountNumbers: List<Int>,
        marketsSummary: InternalMarketSummaryState,
        price: Map<String, Double>?,
        configs: MarketConfigs?,
        periods: Set<CalculationPeriod>,
    ): InternalAccountState {
        for ((subaccountNumber, subaccount) in account.subaccounts) {
            val parentSubaccountNumber = subaccountNumber % NUM_PARENT_SUBACCOUNTS
            if (parentSubaccountNumber in subaccountNumbers) {
                val subAccountState = subaccountCalculator.calculate(
                    subaccount = subaccount,
                    marketsSummary = marketsSummary,
                    price = price,
                    periods = periods,
                    configs = configs,
                )
                if (subAccountState != null) {
                    account.subaccounts[subaccountNumber] = subAccountState
                }
            }
        }

        if (useParentSubaccount) {
            return groupSubaccounts(
                account = account,
                marketsSummary = marketsSummary,
            )
        } else {
            return account
        }
    }

    private fun groupSubaccounts(
        account: InternalAccountState,
        marketsSummary: InternalMarketSummaryState,
    ): InternalAccountState {
        val subaccounts = account.subaccounts
        val subaccountNumbers = subaccounts.keys.sorted()

        // Merge child subaccounts with parent subaccount and store it into groupedSubaccounts
        // We need to copy its entire content not just references, so that the subaccount calculations
        // are not affected by the parent subaccount calculations.
        val groupedSubaccounts = mutableMapOf<Int, InternalSubaccountState>()
        for (subaccountNumber in subaccountNumbers) {
            val subaccount = subaccounts[subaccountNumber] ?: continue
            if (subaccountNumber < NUM_PARENT_SUBACCOUNTS) {
                // this is a parent subaccount..
                groupedSubaccounts[subaccountNumber] = subaccount.deepCopy()
            } else {
                val parentSubaccountNumber = subaccountNumber % NUM_PARENT_SUBACCOUNTS
                var parentSubaccount = groupedSubaccounts[parentSubaccountNumber]
                    ?: subaccounts[parentSubaccountNumber]?.deepCopy()
                    ?: InternalSubaccountState(subaccountNumber = parentSubaccountNumber)

                parentSubaccount = mergeChildOpenPositions(
                    parentSubaccount = parentSubaccount,
                    childSubaccountNumber = subaccountNumber,
                    childSubaccount = subaccount,
                )

                parentSubaccount = mergeChildPendingPositions(
                    parentSubaccount = parentSubaccount,
                    childSubaccount = subaccount,
                    markets = marketsSummary.markets,
                )

                parentSubaccount = mergeOrders(
                    parentSubaccount = parentSubaccount,
                    childSubaccount = subaccount,
                )

                parentSubaccount = sumEquity(
                    parentSubaccount = parentSubaccount,
                    childSubaccount = subaccount,
                )

                groupedSubaccounts[parentSubaccountNumber] = parentSubaccount
            }
        }
        account.groupedSubaccounts = groupedSubaccounts
        return account
    }

    private fun mergeChildOpenPositions(
        parentSubaccount: InternalSubaccountState,
        childSubaccountNumber: Int,
        childSubaccount: InternalSubaccountState,
    ): InternalSubaccountState {
        val parentOpenPositions = parentSubaccount.openPositions
        val modifiedOpenPositions = parentOpenPositions?.toMutableMap() ?: mutableMapOf()
        val childOpenPositions = childSubaccount.openPositions
        for ((market, childOpenPosition) in childOpenPositions ?: emptyMap()) {
            childOpenPosition.childSubaccountNumber = childSubaccountNumber

            modifiedOpenPositions[market] = childOpenPosition
        }
        parentSubaccount.openPositions = modifiedOpenPositions

        return parentSubaccount
    }

    private fun mergeChildPendingPositions(
        parentSubaccount: InternalSubaccountState,
        childSubaccount: InternalSubaccountState,
        markets: Map<String, InternalMarketState>?,
    ): InternalSubaccountState {
        data class PendingMarket(
            var firstOrderId: String,
            var orderCount: Int,
        )

        // Each empty subaccount should have order for one market only
        // Just in case it has more than one market, we will create
        // two separate pending positions.
        val childOpenPositions = childSubaccount.openPositions
        val childOrders = childSubaccount.orders
        val pendingByMarketId = mutableMapOf<String, PendingMarket>()
        for (order in childOrders ?: emptyList()) {
            val marketId = order.marketId ?: continue

            if (childOpenPositions?.containsKey(marketId) == true) {
                val existingPositionCurrentSize =
                    childOpenPositions.get(marketId)?.calculated?.get(CalculationPeriod.current)?.size
                if (existingPositionCurrentSize != null && existingPositionCurrentSize.abs() > 0.0) {
                    continue
                }
            }

            val orderStatus = order.status
            if (!listOf(
                    OrderStatus.Open,
                    OrderStatus.Pending,
                    OrderStatus.Untriggered,
                    OrderStatus.PartiallyFilled,
                ).contains(orderStatus)
            ) {
                continue
            }

            val pending = pendingByMarketId[marketId]
            if (pending == null) {
                pendingByMarketId[marketId] = PendingMarket(
                    firstOrderId = order.id,
                    orderCount = 1,
                )
            } else {
                pending.orderCount += 1
            }
        }

        val modifiedPendingPositions = mutableListOf<InternalPerpetualPendingPosition>()
        for ((marketId, pending) in pendingByMarketId) {
            val market = markets?.get(marketId) ?: continue
            val assetId = market.perpetualMarket?.assetId ?: continue
            val displayId = market.perpetualMarket?.displayId ?: continue

            val calculated =
                mutableMapOf<CalculationPeriod, InternalPendingPositionCalculated>()
            for (period in CalculationPeriod.entries) {
                val childSubaccountCalculated = childSubaccount.calculated[period]
                calculated[period] = InternalPendingPositionCalculated(
                    quoteBalance = childSubaccountCalculated?.quoteBalance,
                    freeCollateral = childSubaccountCalculated?.freeCollateral,
                    equity = childSubaccountCalculated?.equity,
                )
            }
            val pendingPosition = InternalPerpetualPendingPosition(
                assetId = assetId,
                marketId = marketId,
                displayId = displayId,
                firstOrderId = pending.firstOrderId,
                orderCount = pending.orderCount,
                calculated = calculated,
            )
            modifiedPendingPositions.add(pendingPosition)
        }
        var allPendingPositions = parentSubaccount.pendingPositions ?: emptyList()
        allPendingPositions = allPendingPositions + modifiedPendingPositions
        parentSubaccount.pendingPositions = allPendingPositions.sortedWith { a, b ->
            val aMarketId = a.assetId ?: ""
            val bMarketId = b.assetId ?: ""
            aMarketId.compareTo(bMarketId)
        }

        return parentSubaccount
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

    private fun sumEquity(
        parentSubaccount: InternalSubaccountState,
        childSubaccount: InternalSubaccountState,
    ): InternalSubaccountState {
        for (period in CalculationPeriod.entries) {
            val parentEquity = parentSubaccount.calculated[period]?.equity
            val childEquity = childSubaccount.calculated[period]?.equity
            if (parentEquity != null || childEquity != null) {
                parentSubaccount.calculated[period]?.equity = (parentEquity ?: 0.0) + (childEquity ?: 0.0)
            }
        }
        return parentSubaccount
    }
}
