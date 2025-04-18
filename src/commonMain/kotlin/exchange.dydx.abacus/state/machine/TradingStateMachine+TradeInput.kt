package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.responses.StateResponse
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class TradeInputField(val rawValue: String) {
    type("type"),
    side("side"),
    marginMode("marginMode"),
    targetLeverage("targetLeverage"),

    size("size.size"),
    usdcSize("size.usdcSize"),
    leverage("size.leverage"),
    balancePercent("size.balancePercent"),
    lastInput("size.input"),

    limitPrice("price.limitPrice"),
    triggerPrice("price.triggerPrice"),
    trailingPercent("price.trailingPercent"),

    timeInForceType("timeInForce"),
    goodTilDuration("goodTil.duration"),
    goodTilUnit("goodTil.unit"),

    execution("execution"),
    reduceOnly("reduceOnly"),
    postOnly("postOnly"),

    bracketsStopLossPrice("brackets.stopLoss.triggerPrice"),
    bracketsStopLossPercent("brackets.stopLoss.percent"),
    bracketsStopLossReduceOnly("brackets.stopLoss.reduceOnly"),
    bracketsTakeProfitPrice("brackets.takeProfit.triggerPrice"),
    bracketsTakeProfitPercent("brackets.takeProfit.percent"),
    bracketsTakeProfitReduceOnly("brackets.takeProfit.reduceOnly"),
    bracketsGoodUntilDuration("brackets.goodTil.duration"),
    bracketsGoodUntilUnit("brackets.goodTil.unit"),
    bracketsExecution("brackets.execution");

    companion object {
        operator fun invoke(rawValue: String?) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}

internal fun TradingStateMachine.tradeInMarket(
    marketId: String,
    subaccountNumber: Int,
): StateResponse {
    val changes = tradeInputProcessor.tradeInMarket(
        inputState = internalState.input,
        marketSummaryState = internalState.marketsSummary,
        walletState = internalState.wallet,
        configs = internalState.configs,
        rewardsParams = internalState.rewardsParams,
        marketId = marketId,
        subaccountNumber = subaccountNumber,
    )
    updateStateChanges(changes)
    return StateResponse(state, changes, null)
}

fun TradingStateMachine.trade(
    data: String?,
    type: TradeInputField?,
    subaccountNumber: Int,
): StateResponse {
    val result = tradeInputProcessor.trade(
        inputState = internalState.input,
        walletState = internalState.wallet,
        marketSummaryState = internalState.marketsSummary,
        configs = internalState.configs,
        rewardsParams = internalState.rewardsParams,
        inputType = type,
        inputData = data,
        subaccountNumber = subaccountNumber,
    )
    result.changes?.let {
        updateStateChanges(it)
    }
    return StateResponse(
        state,
        result.changes,
        if (result.error != null) iListOf(result.error) else null,
    )
}
