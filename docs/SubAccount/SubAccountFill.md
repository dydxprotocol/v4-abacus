# SubaccountFill

data class SubaccountFill(  
&emsp;val id: String,  
&emsp;val marketId: String,  
&emsp;val displayId: String,
&emsp;val orderId: String?,  
&emsp;val side: OrderSide,  
&emsp;val type: OrderType,  
&emsp;val liquidity: FillLiquidity,  
&emsp;val price: Double,  
&emsp;val size: Double,  
&emsp;val fee: Double?,  
&emsp;val createdAtMilliseconds: Double,  
&emsp;val resources: SubaccountFillResources  
)

## id

Fill ID

## marketId

Market ID

## displayId

Displayable market ID.

## orderId

Associated order ID

## side

BUY or SELL

## type

Order type

## liquidity

Taker or Maker

## price

Execution price

## size

Execution size

## fee

Fee charged for this fill

## createdAtMilliseconds

When this fill occured

## resources

UI resources for displaying this fill

# SubaccountFillResources

data class SubaccountFillResources(  
&emsp;val sideStringKey: String?,  
&emsp;val liquidityStringKey: String?,  
&emsp;val typeStringKey: String?,  
&emsp;val iconLocal: String?  
)

## sideStringKey

Localization string key to display the side

## liquidityStringKey

Localization string key to display the liquidity

## typeStringKey

Localization string key to display the type

## iconLocal

Icon used to display the fill