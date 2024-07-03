package exchange.dydx.abacus.state.internalstate

import indexer.codegen.IndexerPerpetualMarketStatus
import indexer.codegen.IndexerPerpetualMarketType

internal data class InternalStatePerpetualMarkets (
    val markets: Map<String, InternalStatePerpetualMarket>? = null,
)

internal data class InternalStatePerpetualMarket (
    val clobPairId: Int, // "6",
    val ticker: String, // "ADA-USD",
    val status: IndexerPerpetualMarketStatus,//  "ACTIVE",
    val oraclePrice: Double, // "0.408213647",
    val priceChange24H: Double, // "-0.0034423657",
    val volume24H: Double, // "12831.688",
    val trades24H: Int, // 388,
    val nextFundingRate: Double, // "0",
    val initialMarginFraction: Double, // "0.1",
    val maintenanceMarginFraction: Double, // "0.05",
    val openInterest: Double, // "247890",
    val atomicResolution: Int, // -5,
    val quantumConversionExponent: Int, // -9,
    val tickSize: Double, //  "0.0001",
    val stepSize: Double, // "10",
    val stepBaseQuantums: Int, // 1000000,
    val subticksPerTick: Int, // 1000000,
    val marketType: IndexerPerpetualMarketType?,
    val openInterestLowerCap: Double, // "20000000",
    val openInterestUpperCap: Double, // "50000000",
    val baseOpenInterest: Double, // "247890"
)