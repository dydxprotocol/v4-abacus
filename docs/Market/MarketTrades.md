# MarketTrades

A sorted array of [MarketTrade](#MarketTrade)

# MarketTrade

data class MarketTrade(  
&emsp;val side: OrderSide,  
&emsp;val size: Double,  
&emsp;val price: Double,  
&emsp;val liquidation: Boolean,  
&emsp;val createdAtMilliseconds: Double,  
&emsp;val resources: [MarketTradeResources](#MarketTradeResources)  
)

## side

Side of the order, BUY or SELL

## size

Size of the trade

## price

Price of the trade

## liquidation

When it is the result of a liquidation

## createdAtMilliseconds

Timestamp of the trade

## resources

Display information for the trade

# MarketTradeResources

data class MarketTradeResources(  
&emsp;val sideStringKey: String  
)

## sideStringKey

Localization string key to display the side