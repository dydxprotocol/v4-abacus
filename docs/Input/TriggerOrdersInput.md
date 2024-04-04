# TriggerOrdersInput

data class TriggerOrdersInput(  
&emsp;val marketId: String?,  
&emsp;val size: Double?,  
&emsp;val stopLossOrder: [TriggerOrder](#TriggerOrder)?,  
&emsp;val takeProfitOrder: [TriggerOrder](#TriggerOrder)?  
)

## marketId

Selected Market ID

## size

Optional size input which overrides the sizes of both the existing take profit and stop loss order sizes (if non-null).

## stopLossOrder

Stop loss order for the position

## takeProfitOrder

Take profit order for the position

# TriggerOrder

data class TriggerOrderInput(  
&emsp;val orderId: String?,  
&emsp;val size: Double?,
&emsp;val type: OrderType?,  
&emsp;val side: Side?,  
&emsp;val price: [TriggerPrice](#TriggerPrice)?  
&emsp;val summary: [TriggerOrderInputSummary](#TriggerOrderInputSummary)?  
)

## orderId

The associated order ID, if the order already exists

## size

Size of order

## type

OrderType. See [SubaccountOrder](../Account.md)

## side

BUY or SELL

## price

Price information for the order

## summary

A summary of the order

# TriggerPrice

data class TriggerPrice(  
&emsp;val limitPrice: Double?,  
&emsp;val triggerPrice: Double?,  
&emsp;val percentDiff: Double?,  
&emsp;val usdcDiff: Double?,  
&emsp;val input: String?  
)

## limitPrice

Entered limit price

## triggerPrice

Entered trigger price

## percentDiff

Percentage price difference of the trigger price relative to the position's average entry price

## usdcDiff

Usdc price difference of the trigger price relative to the position's average entry price

## input

The price field last modified by the user: triggerPrice, percentDiff, usdcDiff

# TriggerOrderInputSummary

data class TriggerOrderInputSummary(  
&emsp;val price: Double?  
&emsp;val size: Double?  
)

## price

Price of the order

## price

Size of the order
