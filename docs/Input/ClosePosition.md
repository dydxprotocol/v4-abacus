# ClosePosition

data class ClosePositionInput(  
&emsp;val type: OrderType?,  
&emsp;val side: OrderSide?,  
&emsp;val marketId: String?,  
&emsp;val size: [ClosePositionInputSize](#ClosePositionInputSize)?,  
&emsp;val fee: Double?,  
&emsp;val marketOrder: TradeInputMarketOrder?,  
&emsp;val summary: TradeInputSummary?  
)

## type

Type of the order (always MARKET)

## side

Side of the order, BUY or SELL

## marketId

MarketId

## size

Size of the order

## fee

Total fee, if any

## marketOrder

[MarketOrder](TradeInput.md#TradeInputMarketOrder)

## summary

[TradeInputSummary](TradeInput.md#TradeInputSummary)

# ClosePositionInputSize

data class ClosePositionInputSize(  
&emsp;val size: Double?,  
&emsp;val usdcSize: Double?,  
&emsp;val percent: Double?,  
&emsp;val input: String?  
)

## size

Size of the order

## usdcSize

USDC amount of the order

## percent

If user clicked a percent button to set the order, the percent value is here

## input

How user enterered the size

