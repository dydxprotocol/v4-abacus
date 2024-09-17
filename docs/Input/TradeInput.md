# TradeInput

data class TradeInput(  
&emsp;val type: OrderType?,  
&emsp;val side: OrderSide?,  
&emsp;val marketId: String?,
&emsp;val marginMode: MarginMode?,
&emsp;val targetLeverage: Double?,
&emsp;val size: [TradeInputSize](#TradeInputSize)?,  
&emsp;val price: [TradeInputPrice](#TradeInputPrice)?,  
&emsp;val timeInForce: String?,  
&emsp;val goodTil: [TradeInputGoodUntil](#TradeInputGoodUntil)?,  
&emsp;val execution: String?,  
&emsp;val reduceOnly: Boolean,  
&emsp;val postOnly: Boolean,  
&emsp;val fee: Double?,  
&emsp;val bracket: TradeInputBracket?,  
&emsp;val marketOrder: [TradeInputMarketOrder](#TradeInputMarketOrder)?,  
&emsp;val options: [TradeInputOptions](#TradeInputOptions)?,  
&emsp;val summary: [TradeInputSummary](#TradeInputSummary)?  
)

## type

OrderType. See [SubaccountOrder](../Account.md)

## side

BUY or SELL

## marketId

Selected Market ID

## marginMode

CROSS or ISOLATED

## size

Size information for the trade

## price

Price information for the trade

## timeInForce

Time in force

## execution

Execution option

## reduceOnly

Whether the trade has reduce-only selected

## postOnly

Post-only option

## fee

Calculated fee

## bracket

Bracket orders

## marketOrder

Market order structure calculated from orderbook

## options

The list of input fields required, based on type

## summary

A summary of the order

# TradeInputSize

data class TradeInputSize(  
&emsp;val size: Double?,  
&emsp;val usdcSize: Double?,  
&emsp;val leverage: Double?,  
&emsp;val percent: Double?,  
&emsp;val balancePercent: Double?,  
&emsp;val input: String?  
)

## size

Size of the trade in the token unit

## usdcSize

Size of the trade in USDC

## leverage

Leverage of the order

## percent

Percentage (of existing position) to use for the order. Only used for the close position input.

## balancePercent

Percentage (of available balance / free collateral) to use for the order. Currently only on market orders.

## input

Which one of the fields are entered by the user:
- TradeInput: size, usdcSize, leverage, percent, balancePercent
- ClosePositionInput: size, percent

# TradeInputPrice

data class TradeInputPrice(  
&emsp;val limitPrice: Double?,  
&emsp;val triggerPrice: Double?,  
&emsp;val trailingPercent: Double?  
)

## limitPrice

Entered limit price

## triggerPrice

Entered trigger price

## trailingPercent

Trailing percent for TrailingStop orders

# TradeInputGoodUntil

data class TradeInputGoodUntil(  
&emsp;val duration: Double?,  
&emsp;val unit: String?  
)

## duration

Number of (unit) for the duration

## unit

time unit

# TradeInputBracket

data class TradeInputBracket(  
&emsp;val stopLoss: TradeInputBracketSide?,  
&emsp;val takeProfit: TradeInputBracketSide?,  
&emsp;val goodTil: [TradeInputGoodUntil](#TradeInputGoodUntil)?,  
&emsp;val execution: String?  
)

## stopLoss

Stoploss side of the bracket order

## takeProfit

Take profit side of the bracket order

## goodTil

Good until settings for the bracket orders

## execution

Execution settings for the bracket orders

# TradeInputMarketOrder

data class TradeInputMarketOrder(  
&emsp;val size: Double?,  
&emsp;val usdcSize: Double?,  
&emsp;val balancePercent: Double?,  
&emsp;val price: Double?,  
&emsp;val worstPrice: Double?,  
&emsp;val filled: Boolean,  
&emsp;val orderbook: Array<OrderbookUsage>?  
)

## size

size of the market order

## usdcSize

USDC amount of the market order

## balancePercent

Percentage (of available balance / free collateral) of the market order


## price

Calculated price

## worstPrice

Worst price of the market order, based on order book

## filled

Whether the market order can be filled with the current order book

## orderbook

The orderbook entries taken by the market order

# TradeInputOptions

data class TradeInputOptions(  
&emsp;val needsSize: Boolean,  
&emsp;val needsLeverage: Boolean,  
&emsp;val needsBalancePercent: Boolean,
&emsp;val maxLeverage: Double?,  
&emsp;val needsLimitPrice: Boolean,
&emsp;val needsTargetLeverage: Boolean,
&emsp;val needsTriggerPrice: Boolean,  
&emsp;val needsTrailingPercent: Boolean,  
&emsp;val needsGoodUntil: Boolean,  
&emsp;val needsReduceOnly: Boolean,  
&emsp;val needsPostOnly: Boolean,  
&emsp;val needsBrackets: Boolean,  
&emsp;val typeOptions: Array<SelectionOption>,  
&emsp;val sideOptions: Array<SelectionOption>,  
&emsp;val timeInForceOptions: Array<SelectionOption>?,  
&emsp;val goodTilUnitOptions: Array<SelectionOption>?,  
&emsp;val executionOptions: Array<SelectionOption>?,
&emsp;val marginModeOptions: Array<SelectionOption>?
)

## needsSize

UX should ask user for size

## needsLeverage

UX should ask user for leverage input

## needsBalancePercent

UX should ask user for balance percent input

## maxLeverage

Max leverage for the leverage slider

## needsTargetLeverage
UX should ask user to adjust their target leverage when margin mode is ISOLATED

## needsTriggerPrice

UX should ask user to enter trigger price

## needsLimitPrice

UX should ask user to enter limit price

## needsTrailingPercent

UX should ask user to enter trailing percent

## needsGoodUntil

UX should ask user to enter GoodTil time

## needsReduceOnly

UX should have a check box for reduce-only

## needsPostOnly

UX should have a check box for post-only

## needsBrackets

UX should allow user to enter bracket orders

## typeOptions

Order types available for selection

## sideOptions

Order side available for selection

## timeInForceOptions

TimeInForce options

## goodTilUnitOptions

Time units for goodTil

## executionOptions

Execution options for selection

# TradeInputSummary

data class TradeInputSummary(  
&emsp;val price: Double?,  
&emsp;val size: Double?,  
&emsp;val usdcSize: Double?,  
&emsp;val slippage: Double?,  
&emsp;val fee: Double?,  
&emsp;val total: Double?,  
&emsp;val filled: Boolean
&emsp;val positionMargin: Double?,
&emsp;val positionLeverage: Double?,
)

## price

Price of the trade

## size

Size of the trade

## usdcSize

USDC amount of the trade

## slippage

Slippage of the trade

## fee

Fee

## Total

Total amount of the trade

## filled

Whether this trade can be filled

## positionMargin

The margin value of the position

## positionLeverage

The leverage value of the position