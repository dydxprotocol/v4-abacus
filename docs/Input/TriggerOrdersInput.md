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

Optional size input which overrides the sizes of both the existing take profit and stop loss orders (if non-null).

## stopLossOrder

Stop loss order for the position

## takeProfitOrder

Take profit order for the position

# TriggerOrder

data class TriggerOrderInput(  
&emsp;val type: OrderType?,  
&emsp;val price: [TriggerPrice](#TriggerPrice)?  
)

## type

OrderType. See [SubaccountOrder](../Account.md)

## price

Price information for the order

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
