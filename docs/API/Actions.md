# trade

When user select a market or position, FE app should call

appStateMachine.setMarket(marketId)

This will retrieve the following data for this market:

    prices
    trades
    orderbook

Then call trade(...) function to make trade the current input.

    fun trade(data: String?, type: [TradeInputField](#TradeInputField)?): AppStateResponse

All trading params are set up using the same function, using different type

When user brings up the trade input panel, use trade(null, null) to make it the active input, without changing data.

Process the response like any other AppStateResponse

The input state is in response.state.input.trade as a [TradeInput](../Input/TradeInput.md)

## data

Data input in string format

# TradeInputField

It is enum which specifies the type of the input

## type

Order type

## side

Order side

## size

Size of the order

## usdcSize

USDC amount of the order

## leverage

Leverage of the order

## limitPrice

Limit price

## triggerPrice

Trigger/stop price

## trailingPercent

Trailing percent for Trailing Stop order

## timeInForceType

TimeInForce

## goodTilDuration

Numeric duration of GoodTil

## goodTilUnit

Time unit of the GoodTil

## execution

Execution option

## reduceOnly

Whether the order is reduce-only

## postOnly

Whether the order is post-only

## bracketsStopLossPrice

Bracket order stop loss price

## bracketsStopLossPercent

Bracket order stop loss percentage

## bracketsStopLossReduceOnly

Whether the stop loss bracket order is reduce only

## bracketsTakeProfitPrice

Bracket order take profit price

## bracketsTakeProfitPercent

Bracket order take profit percentage

## bracketsTakeProfitReduceOnly

Whether the take profit bracket order is reduce only

## bracketsGoodUntilDuration

Bracket orders goodTil numeric duration

## bracketsGoodUntilUnit

Bracket order goodTil time unit

## bracketsExecution

Bracket order execution option

# closePosition

fun closePosition(data: String?, type: [ClosePositionInputField](#ClosePositionInputField)?): AppStateResponse

All closePosition params are set up using the same function, using different type

Process the response like any other AppStateResponse

The input state is in response.state.input.closePosition as a [ClosePositionInput](../Input/ClosePositionInput.md)

## data

Data input in string format

# ClosePositionInputField

## market

The market ID of the position to be close

## size

Size of the order

## percent

Percent of the order relative to the position size. If user entered size directly, the percent is cleared

# transfer

fun transfer(data: String?, type: [TransferInputField](#TransferInputField)?): AppStateResponse

All transfer params are set up using the same function, using different type

Process the response like any other AppStateResponse

The input state is in response.state.input.transfer as a [TransferInput](../Input/TransferInput.md)

## data

Data input in string format

# TransferInputField

## usdcSize

Amount in USDC

## usdcFee

Fee in USDC

## address

Address input, used for transfer

## fastSpeed

Option to use fastSpeed, used by v3 withdrawal

# triggerOrders

fun triggerOrders(data: String?, type: [TriggerOrdersInputField](#TriggerORdersInputField)?): AppStateResponse

# TriggerOrdersInputField
