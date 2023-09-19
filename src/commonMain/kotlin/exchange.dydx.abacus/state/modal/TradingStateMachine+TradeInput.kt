package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.calculator.TradeInputCalculator
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class TradeInputField(val rawValue: String) {
    type("type"),
    side("side"),

    size("size.size"),
    usdcSize("size.usdcSize"),
    leverage("size.leverage"),

    limitPrice("price.limitPrice"),
    triggerPrice("price.triggerPrice"),
    trailingPercent("price.trailingPercent"),

    timeInForceType("timeInForce"),
    goodUntilDuration("goodUntil.duration"),
    goodUntilUnit("goodUntil.unit"),

    execution("execution"),
    reduceOnly("reduceOnly"),
    postOnly("postOnly"),

    bracketsStopLossPrice("brackets.stopLoss.triggerPrice"),
    bracketsStopLossPercent("brackets.stopLoss.percent"),
    bracketsStopLossReduceOnly("brackets.stopLoss.reduceOnly"),
    bracketsTakeProfitPrice("brackets.takeProfit.triggerPrice"),
    bracketsTakeProfitPercent("brackets.takeProfit.percent"),
    bracketsTakeProfitReduceOnly("brackets.takeProfit.reduceOnly"),
    bracketsGoodUntilDuration("brackets.goodUntil.duration"),
    bracketsGoodUntilUnit("brackets.goodUntil.unit"),
    bracketsExecution("brackets.execution");

    companion object {
        operator fun invoke(rawValue: String) =
            TradeInputField.values().firstOrNull { it.rawValue == rawValue }
    }
}


internal fun TradingStateMachine.tradeInMarket(
    marketId: String,
    subaccountNumber: Int,
): StateResponse {
    if (parser.asString(parser.value(input, "trade.marketId")) == marketId) {
        return StateResponse(state, StateChanges(iListOf()))
    } else {
        val input = this.input?.mutable() ?: iMutableMapOf()
        val existingTrade = parser.asMap(input["trade"])

        val trade = if (existingTrade != null) {
            val modified = existingTrade.mutable()
            modified["marketId"] = marketId
            // If we changed market, we should also reset the price and size
            modified.safeSet("size", null)
            modified.safeSet("price", null)
            modified
        } else initiateTrade(
            marketId,
            subaccountNumber
        )
        input["trade"] = trade
        this.input = input
        val changes =
            StateChanges(
                iListOf(Changes.subaccount, Changes.input),
                null,
                iListOf(subaccountNumber)
            )

        changes.let {
            update(it)
        }
        return StateResponse(state, changes, null)
    }
}

internal fun TradingStateMachine.initiateTrade(
    marketId: String?,
    subaccountNumber: Int,
): IMutableMap<String, Any> {
    val trade = iMutableMapOf<String, Any>()
    trade["type"] = "LIMIT"
    trade["side"] = "BUY"
    trade["marketId"] = marketId ?: "ETH-USD"

    val calculator = TradeInputCalculator(parser, TradeCalculation.trade)
    val params = iMutableMapOf<String, Any>()
    params.safeSet("markets", parser.asMap(marketsSummary?.get("markets")))
    params.safeSet("account", account)
    params.safeSet("user", user)
    params.safeSet("trade", trade)
    params.safeSet("rewardsParams", rewardsParams)

    val modified = calculator.calculate(params, subaccountNumber, null)

    return parser.asMap(modified["trade"])?.mutable() ?: trade
}

internal fun TradingStateMachine.inititiateClosePosition(
    marketId: String?,
    subaccountNumber: Int,
): IMutableMap<String, Any> {
    val trade = iMutableMapOf<String, Any>()
    trade["type"] = "MARKET"
    trade["side"] = "BUY"
    trade["marketId"] = marketId ?: "ETH-USD"

    val calculator = TradeInputCalculator(parser, TradeCalculation.closePosition)
    val params = iMutableMapOf<String, Any>()
    params.safeSet("markets", parser.asMap(marketsSummary?.get("markets")))
    params.safeSet("account", account)
    params.safeSet("user", user)
    params.safeSet("trade", trade)
    params.safeSet("rewardsParams", rewardsParams)

    val modified = calculator.calculate(params, subaccountNumber, null)

    return parser.asMap(modified["trade"])?.mutable() ?: trade
}

fun TradingStateMachine.trade(
    data: String?,
    type: TradeInputField?,
    subaccountNumber: Int,
): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    val typeText = type?.rawValue

    val input = this.input?.mutable() ?: iMutableMapOf()
    input["current"] = "trade"
    val trade =
        parser.asMap(input["trade"])?.mutable() ?: initiateTrade(null, subaccountNumber)

    var sizeChanged = false
    if (typeText != null) {
        if (validTradeInput(trade, typeText)) {
            when (typeText) {
                TradeInputField.type.rawValue, TradeInputField.side.rawValue -> {
                    val text = parser.asString(data)
                    if (text != null) {
                        if (parser.asString(parser.value(trade, "size.input")) == "size.leverage") {
                            trade.safeSet("size.input", "size.size")
                        }
                        trade[typeText] = text
                        changes = StateChanges(
                            iListOf(Changes.subaccount, Changes.input),
                            null,
                            iListOf(subaccountNumber)
                        )
                    } else {
                        error = ParsingError(
                            ParsingErrorType.MissingRequiredData,
                            "$data is not a valid string"
                        )
                    }
                }

                TradeInputField.size.rawValue,
                TradeInputField.usdcSize.rawValue,
                TradeInputField.leverage.rawValue,
                -> {
                    sizeChanged = (parser.asDouble(data) != parser.asDouble(trade[typeText]))
                    trade.safeSet(typeText, parser.asDouble(data))
                    changes = StateChanges(
                        iListOf(Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                }

                TradeInputField.limitPrice.rawValue,
                TradeInputField.triggerPrice.rawValue,
                TradeInputField.trailingPercent.rawValue,
                TradeInputField.bracketsStopLossPrice.rawValue,
                TradeInputField.bracketsStopLossPercent.rawValue,
                TradeInputField.bracketsTakeProfitPrice.rawValue,
                TradeInputField.bracketsTakeProfitPercent.rawValue,
                -> {
                    trade.safeSet(typeText, parser.asDouble(data))
                    changes = StateChanges(
                        iListOf(Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                }

                TradeInputField.timeInForceType.rawValue,
                TradeInputField.goodUntilUnit.rawValue,
                TradeInputField.bracketsGoodUntilUnit.rawValue,
                TradeInputField.execution.rawValue,
                TradeInputField.bracketsExecution.rawValue,
                -> {
                    trade.safeSet(typeText, parser.asString(data))
                    changes = StateChanges(
                        iListOf(Changes.input), null,
                        iListOf(subaccountNumber)
                    )
                }

                TradeInputField.goodUntilDuration.rawValue,
                TradeInputField.bracketsGoodUntilDuration.rawValue,
                -> {
                    trade.safeSet(typeText, parser.asInt(data))
                    changes = StateChanges(
                        iListOf(Changes.input), null,
                        iListOf(subaccountNumber)
                    )
                }


                TradeInputField.reduceOnly.rawValue,
                TradeInputField.postOnly.rawValue,
                TradeInputField.bracketsStopLossReduceOnly.rawValue,
                TradeInputField.bracketsTakeProfitReduceOnly.rawValue,
                -> {
                    trade.safeSet(typeText, parser.asBool(data))
                    changes = StateChanges(
                        iListOf(Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                }

                else -> {}
            }
        } else {
            error = cannotModify(typeText)
        }
    } else {
        changes = StateChanges(
            iListOf(Changes.wallet, Changes.subaccount, Changes.input),
            null,
            iListOf(subaccountNumber)
        )
    }
    if (sizeChanged) {
        when (typeText) {
            TradeInputField.size.rawValue,
            TradeInputField.usdcSize.rawValue,
            TradeInputField.leverage.rawValue,
            -> {
                trade.safeSet("size.input", typeText)
            }

            else -> {}
        }
    }
    input["trade"] = trade
    this.input = input

    changes?.let {
        update(it)
    }
    return StateResponse(state, changes, if (error != null) iListOf(error) else null)
}

fun TradingStateMachine.tradeDataOption(typeText: String?): String? {
    return when (typeText) {
        TradeInputField.type.rawValue,
        TradeInputField.side.rawValue,
        -> null

        TradeInputField.size.rawValue,
        TradeInputField.usdcSize.rawValue,
        TradeInputField.leverage.rawValue,
        -> "options.needsSize"

        TradeInputField.limitPrice.rawValue -> "options.needsLimitPrice"
        TradeInputField.triggerPrice.rawValue -> "options.needsTriggerPrice"
        TradeInputField.trailingPercent.rawValue -> "options.needsTrailingPercent"

        TradeInputField.goodUntilDuration.rawValue -> "options.needsGoodUntil"
        TradeInputField.goodUntilUnit.rawValue -> "options.needsGoodUntil"
        TradeInputField.reduceOnly.rawValue -> "options.needsReduceOnly"
        TradeInputField.postOnly.rawValue -> "options.needsPostOnly"

        TradeInputField.bracketsStopLossPrice.rawValue,
        TradeInputField.bracketsStopLossPercent.rawValue,
        TradeInputField.bracketsTakeProfitPrice.rawValue,
        TradeInputField.bracketsTakeProfitPercent.rawValue,
        TradeInputField.bracketsGoodUntilDuration.rawValue,
        TradeInputField.bracketsGoodUntilUnit.rawValue,
        TradeInputField.bracketsStopLossReduceOnly.rawValue,
        TradeInputField.bracketsTakeProfitReduceOnly.rawValue,
        TradeInputField.bracketsExecution.rawValue,
        -> "options.needsBrackets"

        TradeInputField.timeInForceType.rawValue -> "options.timeInForceOptions"
        TradeInputField.execution.rawValue -> "options.executionOptions"

        else -> null
    }
}

fun TradingStateMachine.validTradeInput(trade: IMap<String, Any>, typeText: String?): Boolean {
    val option = this.tradeDataOption(typeText)
    return if (option != null) {
        val value = parser.value(trade, option)
        if (parser.asList(value) != null) {
            true
        } else {
            parser.asBool(value) ?: false
        }
    } else true
}