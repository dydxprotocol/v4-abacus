package exchange.dydx.abacus.state.model

import abs
import exchange.dydx.abacus.calculator.MarginCalculator
import exchange.dydx.abacus.calculator.TradeCalculation
import exchange.dydx.abacus.calculator.TradeInputCalculator
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.responses.cannotModify
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.Numeric
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

    internal val tradeDataOption: String?
        get() = when (this) {
            type, side -> null
            size, usdcSize, leverage -> "options.needsSize"
            limitPrice -> "options.needsLimitPrice"
            triggerPrice -> "options.needsTriggerPrice"
            trailingPercent -> "options.needsTrailingPercent"
            targetLeverage -> "options.needsTargetLeverage"
            goodTilDuration, goodTilUnit -> "options.needsGoodUntil"
            reduceOnly -> "options.needsReduceOnly"
            postOnly -> "options.needsPostOnly"
            bracketsStopLossPrice,
            bracketsStopLossPercent,
            bracketsTakeProfitPrice,
            bracketsTakeProfitPercent,
            bracketsGoodUntilDuration,
            bracketsGoodUntilUnit,
            bracketsStopLossReduceOnly,
            bracketsTakeProfitReduceOnly,
            bracketsExecution -> "options.needsBrackets"
            timeInForceType -> "options.timeInForceOptions"
            execution -> "options.executionOptions"
            marginMode -> "options.marginModeOptions"
            else -> null
        }
}

internal fun TradingStateMachine.tradeInMarket(
    marketId: String,
    subaccountNumber: Int,
): StateResponse {
    if (staticTyping) {
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
                updateStateChanges(it)
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
            val existingPosition = MarginCalculator.findExistingPositionDeprecated(
                parser,
                account,
                marketId,
                subaccountNumber,
            )
            val existingOrder = MarginCalculator.findExistingOrderDeprecated(
                parser,
                account,
                marketId,
                subaccountNumber,
            )
            val market = parser.asNativeMap(parser.value(marketsSummary, "markets.$marketId"))
            val imf = parser.asDouble(parser.value(market, "configs.initialMarginFraction")) ?: Numeric.double.ZERO
            val effectiveImf = parser.asDouble(parser.value(market, "configs.effectiveInitialMarginFraction")) ?: Numeric.double.ZERO
            val maxMarketLeverage = if (effectiveImf > Numeric.double.ZERO) {
                Numeric.double.ONE / effectiveImf
            } else if (imf > Numeric.double.ZERO) {
                Numeric.double.ONE / imf
            } else {
                Numeric.double.ONE
            }
            if (existingPosition != null) {
                it.safeSet("marginMode", if (existingPosition["equity"] != null) MarginMode.Isolated.rawValue else MarginMode.Cross.rawValue)
                val currentPositionLeverage = parser.asDouble(parser.value(existingPosition, "leverage.current"))?.abs()
                val positionLeverage = if (currentPositionLeverage != null && currentPositionLeverage > 0) currentPositionLeverage else 1.0
                it.safeSet("targetLeverage", positionLeverage)
            } else if (existingOrder != null) {
                val orderMarginMode = if ((parser.asInt(parser.value(existingOrder, "subaccountNumber")) ?: subaccountNumber) == subaccountNumber) MarginMode.Cross.rawValue else MarginMode.Isolated.rawValue
                it.safeSet("marginMode", orderMarginMode)
                it.safeSet("targetLeverage", maxMarketLeverage)
            } else {
                val marketType = parser.asString(parser.value(marketsSummary, "markets.$marketId.configs.perpetualMarketType"))
                it.safeSet("marginMode", MarginMode.invoke(marketType)?.rawValue)
                it.safeSet("targetLeverage", maxMarketLeverage)
            }
        }

        input["trade"] = trade
        input["current"] = "trade"
        this.input = input
        val subaccountNumbers =
            MarginCalculator.getChangedSubaccountNumbersDeprecated(
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
            updateStateChanges(it)
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

    val marginMode = MarginCalculator.findExistingMarginModeDeprecated(parser, account, marketId, subaccountNumber)
        ?: MarginCalculator.findMarketMarginModeDeprecated(parser, parser.asNativeMap(parser.value(marketsSummary, "markets.$marketId")))

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

fun TradingStateMachine.trade(
    data: String?,
    type: TradeInputField?,
    subaccountNumber: Int,
): StateResponse {
    if (staticTyping) {
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
        return StateResponse(state, result.changes, if (result.error != null) iListOf(result.error) else null)
    }

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
            val subaccountNumbers =
                MarginCalculator.getChangedSubaccountNumbersDeprecated(
                    parser,
                    account,
                    subaccountNumber,
                    trade,
                )

            when (type) {
                TradeInputField.type, TradeInputField.side -> {
                    val text = parser.asString(data)
                    if (text != null) {
                        val sizeInput = TradeInputField.invoke(parser.asString(parser.value(trade, "size.input")))
                        if (sizeInput == TradeInputField.leverage || sizeInput == TradeInputField.balancePercent) {
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

                TradeInputField.lastInput -> {
                    trade.safeSet(typeText, parser.asString(data))
                    changes = StateChanges(
                        iListOf(Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }

                TradeInputField.size,
                TradeInputField.usdcSize,
                TradeInputField.leverage,
                TradeInputField.balancePercent,
                TradeInputField.targetLeverage,
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

                TradeInputField.limitPrice,
                TradeInputField.triggerPrice,
                TradeInputField.trailingPercent,
                TradeInputField.bracketsStopLossPrice,
                TradeInputField.bracketsStopLossPercent,
                TradeInputField.bracketsTakeProfitPrice,
                TradeInputField.bracketsTakeProfitPercent,
                -> {
                    trade.safeSet(typeText, parser.asDouble(data))
                    changes = StateChanges(
                        iListOf(Changes.subaccount, Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }

                TradeInputField.marginMode
                -> {
                    trade.safeSet(typeText, parser.asString(data))
                    val changedSubaccountNumbers =
                        MarginCalculator.getChangedSubaccountNumbersDeprecated(
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

                TradeInputField.timeInForceType,
                TradeInputField.goodTilUnit,
                TradeInputField.bracketsGoodUntilUnit,
                TradeInputField.execution,
                TradeInputField.bracketsExecution,
                -> {
                    trade.safeSet(typeText, parser.asString(data))
                    changes = StateChanges(
                        iListOf(Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }

                TradeInputField.goodTilDuration,
                TradeInputField.bracketsGoodUntilDuration,
                -> {
                    trade.safeSet(typeText, parser.asInt(data))
                    changes = StateChanges(
                        iListOf(Changes.input),
                        null,
                        subaccountNumbers,
                    )
                }

                TradeInputField.reduceOnly,
                TradeInputField.postOnly,
                TradeInputField.bracketsStopLossReduceOnly,
                TradeInputField.bracketsTakeProfitReduceOnly,
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
            error = ParsingError.cannotModify(typeText)
        }
    } else {
        changes = StateChanges(
            iListOf(Changes.wallet, Changes.subaccount, Changes.input),
            null,
            iListOf(subaccountNumber),
        )
    }
    if (sizeChanged) {
        when (type) {
            TradeInputField.size,
            TradeInputField.usdcSize,
            TradeInputField.balancePercent,
            TradeInputField.leverage,
            -> {
                trade.safeSet("size.input", typeText)
            }

            else -> {}
        }
    }
    input["trade"] = trade
    this.input = input

    changes?.let {
        updateStateChanges(it)
    }
    return StateResponse(state, changes, if (error != null) iListOf(error) else null)
}

private fun TradingStateMachine.validTradeInput(trade: Map<String, Any>, typeText: String?): Boolean {
    val option = TradeInputField.invoke(typeText)?.tradeDataOption
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
