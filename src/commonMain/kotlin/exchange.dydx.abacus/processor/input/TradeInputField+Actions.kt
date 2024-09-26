package exchange.dydx.abacus.processor.input

import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.TradeInputBracket
import exchange.dydx.abacus.output.input.TradeInputBracketSide
import exchange.dydx.abacus.output.input.TradeInputGoodUntil
import exchange.dydx.abacus.output.input.TradeInputPrice
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.internalstate.safeCreate
import exchange.dydx.abacus.state.model.TradeInputField

//
// Contains the helper functions for each of the trade input fields
//

// Returns the validation action for the trade input field
@Suppress("ExplicitItLambdaParameter")
internal val TradeInputField.validTradeInputAction: ((InternalTradeInputState) -> Boolean)?
    get() = when (this) {
        TradeInputField.type, TradeInputField.side -> null
        TradeInputField.size, TradeInputField.usdcSize, TradeInputField.leverage, TradeInputField.balancePercent -> { state -> state.options.needsSize }
        TradeInputField.limitPrice -> { state -> state.options.needsLimitPrice }
        TradeInputField.triggerPrice -> { state -> state.options.needsTriggerPrice }
        TradeInputField.trailingPercent -> { state -> state.options.needsTrailingPercent }
        TradeInputField.targetLeverage -> { state -> state.options.needsTargetLeverage }
        TradeInputField.goodTilDuration, TradeInputField.goodTilUnit -> { state -> state.options.needsGoodUntil }
        TradeInputField.reduceOnly -> { state -> state.options.needsReduceOnly }
        TradeInputField.postOnly -> { state -> state.options.needsPostOnly }
        TradeInputField.bracketsStopLossPrice,
        TradeInputField.bracketsStopLossPercent,
        TradeInputField.bracketsTakeProfitPrice,
        TradeInputField.bracketsTakeProfitPercent,
        TradeInputField.bracketsGoodUntilDuration,
        TradeInputField.bracketsGoodUntilUnit,
        TradeInputField.bracketsStopLossReduceOnly,
        TradeInputField.bracketsTakeProfitReduceOnly,
        TradeInputField.bracketsExecution -> { state -> state.options.needsBrackets }
        TradeInputField.timeInForceType -> { state -> state.options.timeInForceOptions != null }
        TradeInputField.execution -> { state -> state.options.executionOptions != null }
        TradeInputField.marginMode -> { state -> state.options.marginModeOptions != null }
        TradeInputField.lastInput -> { it -> true }
    }

// Returns the action to read value for the trade input field
internal val TradeInputField.valueAction: ((InternalTradeInputState) -> Any?)?
    get() = when (this) {
        TradeInputField.type -> { state -> state.type }
        TradeInputField.side -> { state -> state.side }

        TradeInputField.marginMode -> { state -> state.marginMode }
        TradeInputField.targetLeverage -> { state -> state.targetLeverage }

        TradeInputField.size -> { state -> state.size?.size }
        TradeInputField.usdcSize -> { state -> state.size?.usdcSize }
        TradeInputField.leverage -> { state -> state.size?.leverage }
        TradeInputField.balancePercent -> { state -> state.size?.balancePercent }

        TradeInputField.lastInput -> { state -> state.size?.input }
        TradeInputField.limitPrice -> { state -> state.price?.limitPrice }
        TradeInputField.triggerPrice -> { state -> state.price?.triggerPrice }
        TradeInputField.trailingPercent -> { state -> state.price?.trailingPercent }

        TradeInputField.timeInForceType -> { state -> state.timeInForce }
        TradeInputField.goodTilDuration -> { state -> state.goodTil?.duration }
        TradeInputField.goodTilUnit -> { state -> state.goodTil?.unit }

        TradeInputField.execution -> { state -> state.execution }
        TradeInputField.reduceOnly -> { state -> state.reduceOnly }
        TradeInputField.postOnly -> { state -> state.postOnly }

        TradeInputField.bracketsStopLossPrice -> { state -> state.brackets?.stopLoss?.triggerPrice }
        TradeInputField.bracketsStopLossPercent -> { state -> state.brackets?.stopLoss?.percent }
        TradeInputField.bracketsStopLossReduceOnly -> { state -> state.brackets?.stopLoss?.reduceOnly }
        TradeInputField.bracketsTakeProfitPrice -> { state -> state.brackets?.takeProfit?.triggerPrice }
        TradeInputField.bracketsTakeProfitPercent -> { state -> state.brackets?.takeProfit?.percent }
        TradeInputField.bracketsTakeProfitReduceOnly -> { state -> state.brackets?.takeProfit?.reduceOnly }
        TradeInputField.bracketsGoodUntilDuration -> { state -> state.brackets?.goodTil?.duration }
        TradeInputField.bracketsGoodUntilUnit -> { state -> state.brackets?.goodTil?.unit }
        TradeInputField.bracketsExecution -> { state -> state.brackets?.execution }
    }

// Returns the write action to update value for the trade input field
internal val TradeInputField.updateValueAction: ((InternalTradeInputState, String?, ParserProtocol) -> Unit)?
    get() = when (this) {
        TradeInputField.type -> { trade, value, parser ->
            trade.type = OrderType.invoke(value)
        }

        TradeInputField.side -> { trade, value, parser ->
            trade.side = OrderSide.invoke(value)
        }

        TradeInputField.lastInput -> { trade, value, parser ->
            trade.size = TradeInputSize.safeCreate(trade.size).copy(input = value)
        }

        TradeInputField.limitPrice -> { trade, value, parser ->
            trade.price = TradeInputPrice.safeCreate(trade.price).copy(limitPrice = parser.asDouble(value))
        }

        TradeInputField.triggerPrice -> { trade, value, parser ->
            trade.price = TradeInputPrice.safeCreate(trade.price).copy(triggerPrice = parser.asDouble(value))
        }

        TradeInputField.trailingPercent -> { trade, value, parser ->
            trade.price = TradeInputPrice.safeCreate(trade.price).copy(trailingPercent = parser.asDouble(value))
        }

        TradeInputField.bracketsStopLossPrice -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.brackets)
            val stopLoss = TradeInputBracketSide.safeCreate(braket.stopLoss)
            trade.brackets =
                braket.copy(stopLoss = stopLoss.copy(triggerPrice = parser.asDouble(value)))
        }

        TradeInputField.bracketsStopLossPercent -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.brackets)
            val stopLoss = TradeInputBracketSide.safeCreate(braket.stopLoss)
            trade.brackets = braket.copy(stopLoss = stopLoss.copy(percent = parser.asDouble(value)))
        }

        TradeInputField.bracketsTakeProfitPrice -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.brackets)
            val takeProfit = TradeInputBracketSide.safeCreate(braket.takeProfit)
            trade.brackets =
                braket.copy(takeProfit = takeProfit.copy(triggerPrice = parser.asDouble(value)))
        }

        TradeInputField.bracketsTakeProfitPercent -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.brackets)
            val takeProfit = TradeInputBracketSide.safeCreate(braket.takeProfit)
            trade.brackets =
                braket.copy(takeProfit = takeProfit.copy(percent = parser.asDouble(value)))
        }

        TradeInputField.marginMode -> { trade, value, parser ->
            trade.marginMode = MarginMode.invoke(value)
        }

        TradeInputField.timeInForceType -> { trade, value, parser ->
            trade.timeInForce = value
        }

        TradeInputField.goodTilUnit -> { trade, value, parser ->
            trade.goodTil = TradeInputGoodUntil.safeCreate(trade.goodTil).copy(unit = value)
        }

        TradeInputField.bracketsGoodUntilUnit -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.brackets)
            trade.brackets = braket.copy(
                goodTil = TradeInputGoodUntil.safeCreate(braket.goodTil).copy(unit = value),
            )
        }

        TradeInputField.execution -> { trade, value, parser ->
            trade.execution = value
        }

        TradeInputField.bracketsExecution -> { trade, value, parser ->
            trade.brackets = TradeInputBracket.safeCreate(trade.brackets).copy(execution = value)
        }

        TradeInputField.goodTilDuration -> { trade, value, parser ->
            trade.goodTil = TradeInputGoodUntil.safeCreate(trade.goodTil)
                .copy(duration = parser.asDouble(value))
        }

        TradeInputField.bracketsGoodUntilDuration -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.brackets)
            trade.brackets = braket.copy(
                goodTil = TradeInputGoodUntil.safeCreate(braket.goodTil)
                    .copy(duration = parser.asDouble(value)),
            )
        }

        TradeInputField.reduceOnly -> { trade, value, parser ->
            trade.reduceOnly = parser.asBool(value) ?: false
        }

        TradeInputField.postOnly -> { trade, value, parser ->
            trade.postOnly = parser.asBool(value) ?: false
        }

        TradeInputField.bracketsStopLossReduceOnly -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.brackets)
            val stopLoss = TradeInputBracketSide.safeCreate(braket.stopLoss)
            trade.brackets =
                braket.copy(stopLoss = stopLoss.copy(reduceOnly = parser.asBool(value) ?: false))
        }

        TradeInputField.bracketsTakeProfitReduceOnly -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.brackets)
            val takeProfit = TradeInputBracketSide.safeCreate(braket.takeProfit)
            trade.brackets = braket.copy(
                takeProfit = takeProfit.copy(
                    reduceOnly = parser.asBool(value) ?: false,
                ),
            )
        }

        TradeInputField.targetLeverage -> { trade, value, parser ->
            trade.targetLeverage = parser.asDouble(value)
        }

        TradeInputField.size -> { trade, value, parser ->
            trade.size = TradeInputSize.safeCreate(trade.size).copy(size = parser.asDouble(value))
        }

        TradeInputField.usdcSize -> { trade, value, parser ->
            trade.size = TradeInputSize.safeCreate(trade.size).copy(usdcSize = parser.asDouble(value))
        }

        TradeInputField.leverage -> { trade, value, parser ->
            trade.size = TradeInputSize.safeCreate(trade.size).copy(leverage = parser.asDouble(value))
        }

        TradeInputField.balancePercent -> { trade, value, parser ->
            trade.size = TradeInputSize.safeCreate(trade.size).copy(balancePercent = parser.asDouble(value))
        }
    }
