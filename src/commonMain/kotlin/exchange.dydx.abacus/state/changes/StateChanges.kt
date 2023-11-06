package exchange.dydx.abacus.state.changes

import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kollections.iListOf
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
    historicalPnl("historicalPnl"),
    fills("fills"),
    transfers("transfers"),
    fundingPayments("fundingPayments"),
    transferStatuses("transferStatuses"),
    input("input"),
    restriction("restriction"),
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
}