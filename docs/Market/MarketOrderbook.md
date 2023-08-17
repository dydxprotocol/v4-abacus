# MarketOrderbook

Orderbook data for a particular market

data class MarketOrderbook(  
&emsp;val midPrice: Double?,  
&emsp;val spreadPercent: Double?,  
&emsp;val grouping: MarketOrderbookGrouping?,  
&emsp;val asks: OrderbookLines?,  
&emsp;val bids: OrderbookLines?  
)

## midPrice

Calculated midPrice between asks and bids

## spreadPercent

The spread percent relative to the midPrice

## goruping

Grouping information of the orderbook entries under asks and bids

## asks

Sorted array of orderbook entries on the asks side

## bids

Sorted array of orderbook entries on the bids side

# MarketOrderbookGrouping

data class MarketOrderbookGrouping(  
&emsp;val multiplier: OrderbookGrouping,  
&emsp;val tickSize: Double?  
)

## multiplier

none
x10
x100
x1000

## tickSize

The tick size of the orderbook grouping. For example, if the market has a tick size of 0.01, when multiplier is x10, the tick size here is 0.1

# OrderbookLines

Sorted array of [OrderbookLine](#OrderbookLine)

# OrderbookLine

data class OrderbookLine(  
&emsp;val size: Double,  
&emsp;val price: Double,  
&emsp;val depth: Double?  
)

## size

Size of the orderbook entry

## price

Price of the orderbook entry

## depth

Calculated depth up to this entry