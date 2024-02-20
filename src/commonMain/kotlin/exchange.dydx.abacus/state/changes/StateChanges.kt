package exchange.dydx.abacus.state.changes

import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kollections.iListOf
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class Changes(val rawValue: String) {
    configs("configs"),

    markets("markets"),
    assets("assets"),
    orderbook("orderbook"),
    trades("trades"),
    candles("candles"),
    sparklines("sparklines"),
    historicalFundings("historicalFundings"),

    wallet("wallet"),

    accountBalances("accountBalances"),
    subaccount("subaccount"),
    tradingRewards("tradingRewards"),
    historicalPnl("historicalPnl"),
    fills("fills"),
    transfers("transfers"),
    fundingPayments("fundingPayments"),
    transferStatuses("transferStatuses"),
    input("input"),
    restriction("restriction"),

    launchIncentive("launchIncentive"),
    ;

    companion object {
        operator fun invoke(rawValue: String) =
            Changes.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class StateChanges(
    val changes: IList<Changes>,
    val markets: IList<String>? = null,
    val subaccountNumbers: IList<Int>? = null
) {
    companion object {
        val noChange = StateChanges(iListOf<Changes>())
    }

    fun merge(earlierChanges: StateChanges): StateChanges {
        val mergedChanges = this.changes.toSet().union(earlierChanges.changes.toSet()).toIList()
        val mergedMarkets = this.markets?.toSet()?.union(earlierChanges.markets?.toSet() ?: setOf())
            ?.toIList() ?: earlierChanges.markets
        val mergedSubaccountNumbers =
            this.subaccountNumbers?.toSet()
                ?.union(earlierChanges.subaccountNumbers?.toSet() ?: setOf())
                ?.toIList() ?: earlierChanges.subaccountNumbers
        return StateChanges(
            changes = mergedChanges,
            markets = mergedMarkets,
            subaccountNumbers = mergedSubaccountNumbers
        )
    }
}