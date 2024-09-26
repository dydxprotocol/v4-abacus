# structure

marketsSummary in PerpetualState contains summary of the markets, and a dictionary of String to Market

data class PerpetualMarketSummary(  
  val volume24HUSDC: Double?,  
  val openInterestUSDC: Double?,  
  val trades24H: Double?,  
  val markets: Map<String, [PerpetualMarket](#PerpetualMarket)>?  
)

## volume24HUSDC

Last 24-hour volume in USDC

## openInterestUSDC

Open interest in USDC

## trades24H

Number of trades in last 24 hours

## markets

A String to PerpetualMarket dictionary for detailed information for each market. The String key is the marketId

# PerpetualMarket

PerpetualMarket contains detailed information for each perpetual market

data class PerpetualMarket(  
&emsp;val id: String,  
&emsp;val assetId: String,  
&emsp;val market: String?,  
&emsp;val displayId: String?,
&emsp;val indexPrice: Double?,  
&emsp;val oraclePrice: Double? = null,  
&emsp;val marketCaps: Double?,  
&emsp;val priceChange24H: Double?,  
&emsp;val priceChange24HPercent: Double?,  
&emsp;val status: MarketStatus?,  
&emsp;val configs: [MarketConfigs](#MarketConfigs)?,  
&emsp;val perpetual: [MarketPerpetual](#MarketPerpetual)?  
)

## id

Market ID. Other object can reference the market with marketId

## assetId

ID of the associated Asset

## market

Name of the market

## displayId

Displayable market ID.

## indexPrice

Latest index price of the market

## oraclePrice

Latest oracle price of the market

## marketCaps

Market caps

## priceChange24H

Price change over the last 24 hours

## priceChange24HPercent

Price change over the last 24 hours in percentage

## status

A structure to indicate when the market is available for trading

data class MarketStatus(  
&emsp;val canTrade: Boolean,  
&emsp;val canReduce: Boolean  
)

### status.canTrade

The market is available for trading

### status.canReduce

The market is available for trading to reduce position

## configs

Configuration data of the market, with type of [MarketConfigs](#MarketConfigs)

## perpetual

Perpetual specific data for the market, with type of MarketPerpetual

# MarketConfigs
data class MarketConfigs(  
&emsp;val clobPairId: String? = null,  
&emsp;val largeSize: Int? = null,  
&emsp;val stepSize: Double? = null,  
&emsp;val tickSize: Double? = null,  
&emsp;val stepSizeDecimals: Int? = null,  
&emsp;val tickSizeDecimals: Int? = null,  
&emsp;val displayStepSize: Double? = null,  
&emsp;val displayTickSize: Double? = null,  
&emsp;val displayStepSizeDecimals: Int? = null,  
&emsp;val displayTickSizeDecimals: Int? = null,  
&emsp;val minOrderSize: Double? = null,  
&emsp;val initialMarginFraction: Double? = null,  
&emsp;val maintenanceMarginFraction: Double? = null,  
&emsp;val incrementalInitialMarginFraction: Double? = null,  
&emsp;val incrementalPositionSize: Double? = null,  
&emsp;val maxPositionSize: Double? = null,  
&emsp;val baselinePositionSize: Double? = null,  
&emsp;val candleOptions: CandleOptions? = null  
)

## clobPairId

Clob pair ID for V4

## largeSize

What's considered as large order

## stepSize

Step size used to round order size

## tickSize

Tick size used to round price

## stepSizeDecimals

Number of decimals for stepSize

## tickSizeDecimals

Number of decimals for tickSize

## displayStepSize

Step size used to display order size

## displayTickSize

Tick size used to display price

## stepSizeDecimals

Number of decimals for displayStepSize

## tickSizeDecimals

Number of decimals for displayTickSize

## minOrderSize

Minimum order size

## initialMarginFraction

Initial margin fraction limit

## maintenanceMarginFraction

Margin fraction requirements if user already has position

## incrementalInitialMarginFraction

To calculate adjusted margin fraction

## incrementalPositionSize

To calculate adjusted margin fraction, v3: as position size increases by incrementalPositionSize, the margin fraction increases by incrementalInitialMarginFraction

## maxPositionSize

Maximum position size

## baselinePositionSize

Baseline position size

## candleOptions

An array of options for candles, with type CandleOption

data class CandleOption(  
&emsp;val stringKey: String,  
&emsp;val value: String,  
&emsp;val seconds: Int  
)

### candleOptions.stringKey

The string key to display localized text

### candleOptions.value

The value for the option

### candleOptions.seconds

Number of seconds for the candle option. This is used to calculate the duration to fetch the candles

# MarketPerpetual

MarketPerpetual contains unique data related to the perpetual market

data class MarketPerpetual(  
&emsp;val volume24H: Double? = null,  
&emsp;val trades24H: Double? = null,  
&emsp;val volume24HUSDC: Double? = null,  
&emsp;val nextFundingRate: Double? = null,  
&emsp;val nextFundingAtMilliseconds: Double? = null,  
&emsp;val openInterest: Double,  
&emsp;val openInterestUSDC: Double,  
&emsp;val line: Array<Double>?  
)

## volume24H

Trading volume during the last 24 hours

## trades24H

Number of trades over the last 24 hours

## volume24HUSDC

24-hour trading volume in USDC

## nextFundingRate

Next funding rate

## nextFundingAtMilliseconds

When the next funding rate is used

## openInterest

Number of tokens in open interest

## openInterestUSDC

Open interest in USDC

## line

A value array to display small price chart

