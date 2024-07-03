package exchange.dydx.abacus.state.model

import abs
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.calculator.TradeInputCalculator
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.mutableMapOf
import exchange.dydx.abacus.utils.safeSet
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
        operator fun invoke(rawValue: String) =
            TradeInputField.values().firstOrNull { it.rawValue == rawValue }
    }
}

internal fun TradingStateMachine.tradeInMarket(
    marketId: String,
    subaccountNumber: Int,
): StateResponse {
    val input = this.input?.mutable() ?: mutableMapOf()
    if (parser.asString(parser.value(input, "trade.marketId")) == marketId) {
        if (parser.asString(parser.value(input, "current")) == "trade") {
            return StateResponse(state, StateChanges(iListOf()), null)
        } else {
            input["current"] = "trade"
            input["trade"] = parser.asNativeMap(input["trade"]) ?: mutableMapOf<String, Any>()
            val changes =
                StateChanges(
                    iListOf(Changes.input),
                    null,
                    iListOf(subaccountNumber),
                )

            changes.let {
                update(it)
            }
            return StateResponse(state, changes, null)
        }
    } else {
        val existingTrade = parser.asMap(input["trade"])

        val trade = if (existingTrade != null) {
            val modified = existingTrade.mutable()
            modified["marketId"] = marketId
            // If we changed market, we should also reset the price and size
            modified.safeSet("size", null)
            modified.safeSet("price", null)
            modified
        } else {
            initiateTrade(
                marketId,
                subaccountNumber,
            )
        }.also {
            val existingPosition = MarginCalculator.findExistingPosition(
                parser,
                account,
                marketId,
                subaccountNumber,
            )
            val existingOrder = MarginCalculator.findExistingOrder(
                parser,
                account,
                marketId,
                subaccountNumber,
            )
            if (existingPosition != null) {
                it.safeSet("marginMode", if (existingPosition["equity"] != null) MarginMode.Isolated.rawValue else MarginMode.Cross.rawValue)
                val currentPositionLeverage = parser.asDouble(parser.value(existingPosition, "leverage.current"))?.abs()
                val positionLeverage = if (currentPositionLeverage != null && currentPositionLeverage > 0) currentPositionLeverage else 1.0
                it.safeSet("targetLeverage", positionLeverage)
            } else if (existingOrder != null) {
                val orderMarginMode = if ((parser.asInt(parser.value(existingOrder, "subaccountNumber")) ?: subaccountNumber) == subaccountNumber) MarginMode.Cross.rawValue else MarginMode.Isolated.rawValue
                it.safeSet("marginMode", orderMarginMode)
                it.safeSet("targetLeverage", 1.0)
            } else {
                val marketType = parser.asString(parser.value(marketsSummary, "markets.$marketId.configs.perpetualMarketType"))
                it.safeSet("marginMode", MarginMode.invoke(marketType)?.rawValue)
                it.safeSet("targetLeverage", 1.0)
            }
        }

        input["trade"] = trade
        input["current"] = "trade"
        this.input = input
        val subaccountNumbers =
            MarginCalculator.getChangedSubaccountNumbers(
                parser,
                account,
                subaccountNumber,
                trade,
            )
        val changes =
            StateChanges(
                iListOf(Changes.subaccount, Changes.input),
                null,
                subaccountNumbers,
            )

        changes.let {
            update(it)
        }
        return StateResponse(state, changes, null)
    }
}

private fun TradingStateMachine.initiateTrade(
    marketId: String?,
    subaccountNumber: Int,
): MutableMap<String, Any> {
    val trade = mutableMapOf<String, Any>()
    trade["type"] = "LIMIT"
    trade["side"] = "BUY"
    trade["marketId"] = marketId ?: "ETH-USD"

    val marginMode = MarginCalculator.findExistingMarginMode(parser, account, marketId, subaccountNumber)
        ?: MarginCalculator.findMarketMarginMode(parser, parser.asNativeMap(parser.value(marketsSummary, "markets.$marketId")))

    trade.safeSet("marginMode", marginMode)

    val calculator = TradeInputCalculator(parser, TradeCalculation.trade)
    val params = mutableMapOf<String, Any>()
    params.safeSet("markets", parser.asMap(marketsSummary?.get("markets")))
    params.safeSet("account", account)
    params.safeSet("user", user)
    params.safeSet("trade", trade)
    params.safeSet("rewardsParams", rewardsParams)
    params.safeSet("configs", configs)

    val modified = calculator.calculate(params, subaccountNumber, null)

    return parser.asMap(modified["trade"])?.mutable() ?: trade
}

internal fun TradingStateMachine.initiateClosePosition(
    marketId: String?,
    subaccountNumber: Int,
): MutableMap<String, Any> {
    val trade = mutableMapOf<String, Any>()
    trade["type"] = "MARKET"
    trade["side"] = "BUY"
    trade["marketId"] = marketId ?: "ETH-USD"

    val calculator = TradeInputCalculator(parser, TradeCalculation.closePosition)
    val params = mutableMapOf<String, Any>()
    params.safeSet("markets", parser.asMap(marketsSummary?.get("markets")))
    params.safeSet("account", account)
    params.safeSet("user", user)
    params.safeSet("trade", trade)
    params.safeSet("rewardsParams", rewardsParams)
    params.safeSet("configs", configs)

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

    val input = this.input?.mutable() ?: mutableMapOf()
    input["current"] = "trade"
    val trade =
        parser.asMap(input["trade"])?.mutable() ?: initiateTrade(null, subaccountNumber)

    var sizeChanged = false
    if (typeText != null) {
        if (validTradeInput(trade, typeText)) {
            var subaccountNumbers =
                MarginCalculator.getChangedSubaccountNumbers(
                    parser,
                    account,
                    subaccountNumber,
                    trade,
                )

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
                            subaccountNumbers,
                        )
                    } else {
                        error = ParsingError(
                            ParsingErrorType.MissingRequiredData,
                            "$data is not a valid string",
                        )
                    }
                }

                TradeInputField.size.rawValue,
                TradeInputField.usdcSize.rawValue,
                TradeInputField.leverage.rawValue,
                TradeInputField.targetLeverage.rawValue,
                -> {
                    sizeChanged =
                        (parser.asDouble(data) != parser.asDouble(parser.value(trade, typeText)))
                    trade.safeSet(typeText, parser.asDouble(data))
                    changes = StateChanges(
                        iListOf(Changes.subaccount, Changes.input),
                        null,
                        subaccountNumbers,
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
                        subaccountNumbers,
                    )
                }

                TradeInputField.marginMode.rawValue
                -> {
                    trade.safeSet(typeText, parser.asString(data))
                    val changedSubaccountNumbers =
                        MarginCalculator.getChangedSubaccountNumbers(
                            parser,
                            account,
                            subaccountNumber,
                            trade,
                        )
                    changes = StateChanges(
                        iListOf(Changes.input, Changes.subaccount),
                        null,
                        changedSubaccountNumbers,
                    )
                }

                TradeInputField.timeInForceType.rawValue,
                TradeInputField.goodTilUnit.rawValue,
                TradeInputField.bracketsGoodUntilUnit.rawValue,
                TradeInputField.execution.rawValue,
                TradeInputField.bracketsExecution.rawValue,
                -> {
                    trade.safeSet(typeText, parser.asString(data))
                    changes = StateChanges(
                        iListOf(Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }

                TradeInputField.goodTilDuration.rawValue,
                TradeInputField.bracketsGoodUntilDuration.rawValue,
                -> {
                    trade.safeSet(typeText, parser.asInt(data))
                    changes = StateChanges(
                        iListOf(Changes.input),
                        null,
                        subaccountNumbers,
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
                        subaccountNumbers,
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
            iListOf(subaccountNumber),
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
        TradeInputField.targetLeverage.rawValue -> "options.needsTargetLeverage"

        TradeInputField.goodTilDuration.rawValue -> "options.needsGoodUntil"
        TradeInputField.goodTilUnit.rawValue -> "options.needsGoodUntil"
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
        TradeInputField.marginMode.rawValue -> "options.marginModeOptions"

        else -> null
    }
}

fun TradingStateMachine.validTradeInput(trade: Map<String, Any>, typeText: String?): Boolean {
    val option = this.tradeDataOption(typeText)
    return if (option != null) {
        val value = parser.value(trade, option)
        if (parser.asList(value) != null) {
            true
        } else {
            parser.asBool(value) ?: false
        }
    } else {
        true
    }
}
