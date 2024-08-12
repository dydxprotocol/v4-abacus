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
import exchange.dydx.abacus.state.model.TradeInputField.bracketsExecution
import exchange.dydx.abacus.state.model.TradeInputField.bracketsGoodUntilDuration
import exchange.dydx.abacus.state.model.TradeInputField.bracketsGoodUntilUnit
import exchange.dydx.abacus.state.model.TradeInputField.bracketsStopLossPercent
import exchange.dydx.abacus.state.model.TradeInputField.bracketsStopLossPrice
import exchange.dydx.abacus.state.model.TradeInputField.bracketsStopLossReduceOnly
import exchange.dydx.abacus.state.model.TradeInputField.bracketsTakeProfitPercent
import exchange.dydx.abacus.state.model.TradeInputField.bracketsTakeProfitPrice
import exchange.dydx.abacus.state.model.TradeInputField.bracketsTakeProfitReduceOnly
import exchange.dydx.abacus.state.model.TradeInputField.execution
import exchange.dydx.abacus.state.model.TradeInputField.goodTilDuration
import exchange.dydx.abacus.state.model.TradeInputField.goodTilUnit
import exchange.dydx.abacus.state.model.TradeInputField.leverage
import exchange.dydx.abacus.state.model.TradeInputField.limitPrice
import exchange.dydx.abacus.state.model.TradeInputField.marginMode
import exchange.dydx.abacus.state.model.TradeInputField.postOnly
import exchange.dydx.abacus.state.model.TradeInputField.reduceOnly
import exchange.dydx.abacus.state.model.TradeInputField.side
import exchange.dydx.abacus.state.model.TradeInputField.size
import exchange.dydx.abacus.state.model.TradeInputField.targetLeverage
import exchange.dydx.abacus.state.model.TradeInputField.timeInForceType
import exchange.dydx.abacus.state.model.TradeInputField.trailingPercent
import exchange.dydx.abacus.state.model.TradeInputField.triggerPrice
import exchange.dydx.abacus.state.model.TradeInputField.type
import exchange.dydx.abacus.state.model.TradeInputField.usdcSize

//
// Contains the helper functions for each of the trade input fields
//

// Returns the validation action for the trade input field
internal val TradeInputField.validTradeInputAction: ((InternalTradeInputState) -> Boolean)?
    get() = when (this) {
        type, side -> null
        size, usdcSize, leverage -> { it -> it.options.needsSize }
        limitPrice -> { it -> it.options.needsLimitPrice }
        triggerPrice -> { it -> it.options.needsTriggerPrice }
        trailingPercent -> { it -> it.options.needsTrailingPercent }
        targetLeverage -> { it -> it.options.needsTargetLeverage }
        goodTilDuration, goodTilUnit -> { it -> it.options.needsGoodUntil }
        reduceOnly -> { it -> it.options.needsReduceOnly }
        postOnly -> { it -> it.options.needsPostOnly }
        bracketsStopLossPrice,
        bracketsStopLossPercent,
        bracketsTakeProfitPrice,
        bracketsTakeProfitPercent,
        bracketsGoodUntilDuration,
        bracketsGoodUntilUnit,
        bracketsStopLossReduceOnly,
        bracketsTakeProfitReduceOnly,
        bracketsExecution -> { it -> it.options.needsBrackets }
        timeInForceType -> { it -> it.options.timeInForceOptions != null }
        execution -> { it -> it.options.executionOptions != null }
        marginMode -> { it -> it.options.marginModeOptions != null }
        TradeInputField.lastInput -> { it -> true }
    }

// Returns the action to read value for the trade input field
internal val TradeInputField.valueAction: ((InternalTradeInputState) -> Any?)?
    get() = when (this) {
        type -> { it -> it.type }
        side -> { it -> it.side }

        marginMode -> { it -> it.marginMode }
        targetLeverage -> { it -> it.targetLeverage }

        size -> { it -> it.size?.size }
        usdcSize -> { it -> it.size?.usdcSize }
        leverage -> { it -> it.size?.leverage }

        TradeInputField.lastInput -> { it -> it.size?.input }
        limitPrice -> { it -> it.price?.limitPrice }
        triggerPrice -> { it -> it.price?.triggerPrice }
        trailingPercent -> { it -> it.price?.trailingPercent }

        timeInForceType -> { it -> it.timeInForce }
        goodTilDuration -> { it -> it.goodTil?.duration }
        goodTilUnit -> { it -> it.goodTil?.unit }

        execution -> { it -> it.execution }
        reduceOnly -> { it -> it.reduceOnly }
        postOnly -> { it -> it.postOnly }

        bracketsStopLossPrice -> { it -> it.bracket?.stopLoss?.triggerPrice }
        bracketsStopLossPercent -> { it -> it.bracket?.stopLoss?.percent }
        bracketsStopLossReduceOnly -> { it -> it.bracket?.stopLoss?.reduceOnly }
        bracketsTakeProfitPrice -> { it -> it.bracket?.takeProfit?.triggerPrice }
        bracketsTakeProfitPercent -> { it -> it.bracket?.takeProfit?.percent }
        bracketsTakeProfitReduceOnly -> { it -> it.bracket?.takeProfit?.reduceOnly }
        bracketsGoodUntilDuration -> { it -> it.bracket?.goodTil?.duration }
        bracketsGoodUntilUnit -> { it -> it.bracket?.goodTil?.unit }
        bracketsExecution -> { it -> it.bracket?.execution }
    }

// Returns the write action to update value for the trade input field
internal val TradeInputField.updateValueAction: ((InternalTradeInputState, String?, ParserProtocol) -> Unit)?
    get() = when (this) {
        type -> { trade, value, parser -> trade.type = OrderType.invoke(value) }
        side -> { trade, value, parser -> trade.side = OrderSide.invoke(value) }

        TradeInputField.lastInput -> { trade, value, parser ->
            trade.size = TradeInputSize.safeCreate(trade.size).copy(input = value)
        }

        limitPrice -> { trade, value, parser ->
            trade.price =
                TradeInputPrice.safeCreate(trade.price).copy(limitPrice = parser.asDouble(value))
        }

        triggerPrice -> { trade, value, parser ->
            trade.price =
                TradeInputPrice.safeCreate(trade.price).copy(triggerPrice = parser.asDouble(value))
        }

        trailingPercent -> { trade, value, parser ->
            trade.price = TradeInputPrice.safeCreate(trade.price)
                .copy(trailingPercent = parser.asDouble(value))
        }

        bracketsStopLossPrice -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val stopLoss = TradeInputBracketSide.safeCreate(braket.stopLoss)
            trade.bracket =
                braket.copy(stopLoss = stopLoss.copy(triggerPrice = parser.asDouble(value)))
        }

        bracketsStopLossPercent -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val stopLoss = TradeInputBracketSide.safeCreate(braket.stopLoss)
            trade.bracket = braket.copy(stopLoss = stopLoss.copy(percent = parser.asDouble(value)))
        }

        bracketsTakeProfitPrice -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val takeProfit = TradeInputBracketSide.safeCreate(braket.takeProfit)
            trade.bracket =
                braket.copy(takeProfit = takeProfit.copy(triggerPrice = parser.asDouble(value)))
        }

        bracketsTakeProfitPercent -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val takeProfit = TradeInputBracketSide.safeCreate(braket.takeProfit)
            trade.bracket =
                braket.copy(takeProfit = takeProfit.copy(percent = parser.asDouble(value)))
        }

        marginMode -> { trade, value, parser ->
            trade.marginMode = MarginMode.invoke(value)
        }

        timeInForceType -> { trade, value, parser -> trade.timeInForce = value }

        goodTilUnit -> { trade, value, parser ->
            trade.goodTil = TradeInputGoodUntil.safeCreate(trade.goodTil).copy(unit = value)
        }

        bracketsGoodUntilUnit -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            trade.bracket = braket.copy(
                goodTil = TradeInputGoodUntil.safeCreate(braket.goodTil).copy(unit = value),
            )
        }

        execution -> { trade, value, parser -> trade.execution = value }

        bracketsExecution -> { trade, value, parser ->
            trade.bracket = TradeInputBracket.safeCreate(trade.bracket).copy(execution = value)
        }

        goodTilDuration -> { trade, value, parser ->
            trade.goodTil = TradeInputGoodUntil.safeCreate(trade.goodTil)
                .copy(duration = parser.asDouble(value))
        }

        bracketsGoodUntilDuration -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            trade.bracket = braket.copy(
                goodTil = TradeInputGoodUntil.safeCreate(braket.goodTil)
                    .copy(duration = parser.asDouble(value)),
            )
        }

        reduceOnly -> { trade, value, parser -> trade.reduceOnly = parser.asBool(value) ?: false }

        postOnly -> { trade, value, parser -> trade.postOnly = parser.asBool(value) ?: false }

        bracketsStopLossReduceOnly -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val stopLoss = TradeInputBracketSide.safeCreate(braket.stopLoss)
            trade.bracket =
                braket.copy(stopLoss = stopLoss.copy(reduceOnly = parser.asBool(value) ?: false))
        }

        bracketsTakeProfitReduceOnly -> { trade, value, parser ->
            val braket = TradeInputBracket.safeCreate(trade.bracket)
            val takeProfit = TradeInputBracketSide.safeCreate(braket.takeProfit)
            trade.bracket = braket.copy(
                takeProfit = takeProfit.copy(
                    reduceOnly = parser.asBool(value) ?: false,
                ),
            )
        }

        targetLeverage -> { trade, value, parser -> trade.targetLeverage = parser.asDouble(value) }
        size -> { trade, value, parser ->
            trade.size = TradeInputSize.safeCreate(trade.size).copy(size = parser.asDouble(value))
        }

        usdcSize -> { trade, value, parser ->
            trade.size =
                TradeInputSize.safeCreate(trade.size).copy(usdcSize = parser.asDouble(value))
        }

        leverage -> { trade, value, parser ->
            trade.size =
                TradeInputSize.safeCreate(trade.size).copy(leverage = parser.asDouble(value))
        }
    }
