# trade

When user select a market or position, FE app should call

    appStateMachine.setMarket(marketId)

This will retrieve the following data for this market:

    prices
    trades
    orderbook

Then call the `trade(...)` function to make trade the current input.

    fun trade(data: String?, type: TradeInputField?): AppStateResponse

All trading params are set up using the same function, but with different types.

When user brings up the trade input panel, use `trade(null, null)` to make it the active input, without changing data.

Process the response like any other AppStateResponse.

The input state is in `response.state.input.trade` as a [TradeInput](../Input/TradeInput.md).

### data

Data input in string format

## TradeInputField

An enum specifying the type of trade input

### type

Order type

### side

Order side

### size

Size of the order

### usdcSize

USDC amount of the order

### leverage

Leverage of the order

### limitPrice

Limit price

### triggerPrice

Trigger/stop price

### trailingPercent

Trailing percent for Trailing Stop order

### timeInForceType

TimeInForce

### goodTilDuration

Numeric duration of GoodTil

### goodTilUnit

Time unit of the GoodTil

### execution

Execution option

### reduceOnly

Whether the order is reduce-only

### postOnly

Whether the order is post-only

### bracketsStopLossPrice

Bracket order stop loss price

### bracketsStopLossPercent

Bracket order stop loss percentage

### bracketsStopLossReduceOnly

Whether the stop loss bracket order is reduce only

### bracketsTakeProfitPrice

Bracket order take profit price

### bracketsTakeProfitPercent

Bracket order take profit percentage

### bracketsTakeProfitReduceOnly

Whether the take profit bracket order is reduce only

### bracketsGoodUntilDuration

Bracket orders goodTil numeric duration

### bracketsGoodUntilUnit

Bracket order goodTil time unit

### bracketsExecution

Bracket order execution option

# closePosition

    fun closePosition(data: String?, type: ClosePositionInputField?): AppStateResponse

All closePosition params are set up using the same function, but with different types.

Process the response like any other AppStateResponse.

The input state is in `response.state.input.closePosition` as a [ClosePositionInput](../Input/ClosePositionInput.md).

### data

Data input in string format

## ClosePositionInputField

### market

The market ID of the position to be closed

### size

Size of the order

### percent

Percent of the order relative to the position size. If the user directly edited `size`, `percent` is cleared

# transfer

    fun transfer(data: String?, type: TransferInputField?): AppStateResponse

All transfer params are set up using the same function, but with different types.

Process the response like any other AppStateResponse.

The input state is in `response.state.input.transfer` as a [TransferInput](../Input/TransferInput.md).

### data

Data input in string format

## TransferInputField

### usdcSize

Amount in USDC

### usdcFee

Fee in USDC

### address

Address input, used for transfer

### fastSpeed

Option to use fastSpeed, used by v3 withdrawal

# triggerOrders

    fun triggerOrders(data: String?, TriggerOrdersInputField?): AppStateResponse

All trigger order params are set up using the same function, but with different types.

Process the response like any other AppStateResponse.

The input state is in `response.state.input.triggerOrders` as a [TriggerOrdersInput](../Input/TriggerOrdersInput.md).

### data

Data input in string format

## TriggerOrdersInputField

### size

max(stopLossOrder size, takeProfitOrder size) // xcxc i don't think i ened this

### stopLossOrderType

Stop loss order type

### stopLossLimitPrice

Stop loss order limit price

### stopLossPrice

Stop loss order trigger price

### stopLossPercentDiff

Stop loss order trigger price's percentage difference from the position's average entry price

### stopLossUsdcDiff

Stop loss order trigger price's usdc difference from the position's average entry price

### takeProfitOrderType

Take profit order type

### takeProfitLimitPrice

Take profit order limit price

### takeProfitPrice

Take profit order trigger price

### takeProfitPercentDiff

Take profit order trigger price's percentage difference from the position's average entry price

### takeProfitUsdcDiff

Take profit order trigger price's usdc difference from the position's average entry price