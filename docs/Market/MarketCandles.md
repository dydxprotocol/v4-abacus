# MarketCandles

Sorted array of [MarketCandle](#MarketCandle)

# MarketCandle

data class MarketCandle(  
&emsp;val startedAtMilliseconds: Double,  
&emsp;val updatedAtMilliseconds: Double?,  
&emsp;val low: Double,  
&emsp;val high: Double,  
&emsp;val open: Double,  
&emsp;val close: Double,  
&emsp;val baseTokenVolume: Double,  
&emsp;val usdVolume: Double  
)

## startedAtMilliseconds

Start time of the candle period

## updatedAtMilliseconds

Updated time

## low

Low price during the period

## high

High price during the period

## open

Open price at the beginning of the period

## close

Close price at the end of the period

## baseTokenVolume

Volume in the unit of the asset

## usdVolume

Volume in USDC