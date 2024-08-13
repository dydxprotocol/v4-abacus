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
internal val TradeInputField.validTradeInputAction: ((InternalTradeInputState) -> Boolean)?
    get() = when (this) {
        TradeInputField.type, TradeInputField.side -> null
        TradeInputField.size, TradeInputField.usdcSize, TradeInputField.leverage -> { it -> it.options.needsSize }
        TradeInputField.limitPrice -> { it -> it.options.needsLimitPrice }
        TradeInputField.triggerPrice -> { it -> it.options.needsTriggerPrice }
        TradeInputField.trailingPercent -> { it -> it.options.needsTrailingPercent }
        TradeInputField.targetLeverage -> { it -> it.options.needsTargetLeverage }
        TradeInputField.goodTilDuration, TradeInputField.goodTilUnit -> { it -> it.options.needsGoodUntil }
        TradeInputField.reduceOnly -> { it -> it.options.needsReduceOnly }
        TradeInputField.postOnly -> { it -> it.options.needsPostOnly }
        TradeInputField.bracketsStopLossPrice,
        TradeInputField.bracketsStopLossPercent,
        TradeInputField.bracketsTakeProfitPrice,
        TradeInputField.bracketsTakeProfitPercent,
        TradeInputField.bracketsGoodUntilDuration,
        TradeInputField.bracketsGoodUntilUnit,
        TradeInputField.bracketsStopLossReduceOnly,
        TradeInputField.bracketsTakeProfitReduceOnly,
        TradeInputField.bracketsExecution -> { it -> it.options.needsBrackets }
        TradeInputField.timeInForceType -> { it -> it.options.timeInForceOptions != null }
        TradeInputField.execution -> { it -> it.options.executionOptions != null }
        TradeInputField.marginMode -> { it -> it.options.marginModeOptions != null }
        TradeInputField.lastInput -> { it -> true }
    }

// Returns the action to read value for the trade input field
internal val TradeInputField.valueAction: ((InternalTradeInputState) -> Any?)?
    get() = when (this) {
        TradeInputField.type -> { it -> it.type }
        TradeInputField.side -> { it -> it.side }

        TradeInputField.marginMode -> { it -> it.marginMode }
        TradeInputField.targetLeverage -> { it -> it.targetLeverage }

        TradeInputField.size -> { it -> it.size?.size }
        TradeInputField.usdcSize -> { it -> it.size?.usdcSize }
        TradeInputField.leverage -> { it -> it.size?.leverage }

        TradeInputField.lastInput -> { it -> it.size?.input }
        TradeInputField.limitPrice -> { it -> it.price?.limitPrice }
        TradeInputField.triggerPrice -> { it -> it.price?.triggerPrice }
        TradeInputField.trailingPercent -> { it -> it.price?.trailingPercent }

        TradeInputField.timeInForceType -> { it -> it.timeInForce }
        TradeInputField.goodTilDuration -> { it -> it.goodTil?.duration }
        TradeInputField.goodTilUnit -> { it -> it.goodTil?.unit }

        TradeInputField.execution -> { it -> it.execution }
        TradeInputField.reduceOnly -> { it -> it.reduceOnly }
        TradeInputField.postOnly -> { it -> it.postOnly }

        TradeInputField.bracketsStopLossPrice -> { it -> it.bracket?.stopLoss?.triggerPrice }
        TradeInputField.bracketsStopLossPercent -> { it -> it.bracket?.stopLoss?.percent }
        TradeInputField.bracketsStopLossReduceOnly -> { it -> it.bracket?.stopLoss?.reduceOnly }
        TradeInputField.bracketsTakeProfitPrice -> { it -> it.bracket?.takeProfit?.triggerPrice }
        TradeInputField.bracketsTakeProfitPercent -> { it -> it.bracket?.takeProfit?.percent }
        TradeInputField.bracketsTakeProfitReduceOnly -> { it -> it.bracket?.takeProfit?.reduceOnly }
        TradeInputField.bracketsGoodUntilDuration -> { it -> it.bracket?.goodTil?.duration }
        TradeInputField.bracketsGoodUntilUnit -> { it -> it.bracket?.goodTil?.unit }
        TradeInputField.bracketsExecution -> { it -> it.bracket?.execution }
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
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val stopLoss = TradeInputBracketSide.safeCreate(braket.stopLoss)
            trade.bracket =
                braket.copy(stopLoss = stopLoss.copy(triggerPrice = parser.asDouble(value)))
        }

        TradeInputField.bracketsStopLossPercent -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val stopLoss = TradeInputBracketSide.safeCreate(braket.stopLoss)
            trade.bracket = braket.copy(stopLoss = stopLoss.copy(percent = parser.asDouble(value)))
        }

        TradeInputField.bracketsTakeProfitPrice -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val takeProfit = TradeInputBracketSide.safeCreate(braket.takeProfit)
            trade.bracket =
                braket.copy(takeProfit = takeProfit.copy(triggerPrice = parser.asDouble(value)))
        }

        TradeInputField.bracketsTakeProfitPercent -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val takeProfit = TradeInputBracketSide.safeCreate(braket.takeProfit)
            trade.bracket =
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
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            trade.bracket = braket.copy(
                goodTil = TradeInputGoodUntil.safeCreate(braket.goodTil).copy(unit = value),
            )
        }

        TradeInputField.execution -> { trade, value, parser ->
            trade.execution = value
        }

        TradeInputField.bracketsExecution -> { trade, value, parser ->
            trade.bracket = TradeInputBracket.safeCreate(trade.bracket).copy(execution = value)
        }

        TradeInputField.goodTilDuration -> { trade, value, parser ->
            trade.goodTil = TradeInputGoodUntil.safeCreate(trade.goodTil)
                .copy(duration = parser.asDouble(value))
        }

        TradeInputField.bracketsGoodUntilDuration -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            trade.bracket = braket.copy(
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
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val stopLoss = TradeInputBracketSide.safeCreate(braket.stopLoss)
            trade.bracket =
                braket.copy(stopLoss = stopLoss.copy(reduceOnly = parser.asBool(value) ?: false))
        }

        TradeInputField.bracketsTakeProfitReduceOnly -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val takeProfit = TradeInputBracketSide.safeCreate(braket.takeProfit)
            trade.bracket = braket.copy(
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
    }
