package indexer.models

import indexer.codegen.IndexerPerpetualMarketStatus
import indexer.codegen.IndexerPerpetualMarketType
import kotlinx.serialization.Serializable

@Serializable
data class IndexerCompositeMarketObject(

    // Copied from IndexerPerpetualMarketResponseObject

    val clobPairId: kotlin.String? = null,
    val ticker: kotlin.String? = null,
    val status: IndexerPerpetualMarketStatus? = null,
    val oraclePrice: kotlin.String? = null,
    val priceChange24H: kotlin.String? = null,
    val volume24H: kotlin.String? = null,
    val trades24H: kotlin.Int? = null,
    val nextFundingRate: kotlin.String? = null,
    val initialMarginFraction: kotlin.String? = null,
    val maintenanceMarginFraction: kotlin.String? = null,
    val openInterest: kotlin.String? = null,
    val atomicResolution: kotlin.Int? = null,
    val quantumConversionExponent: kotlin.Int? = null,
    val tickSize: kotlin.String? = null,
    val stepSize: kotlin.String? = null,
    val stepBaseQuantums: kotlin.Int? = null,
    val subticksPerTick: kotlin.Int? = null,
    val marketType: IndexerPerpetualMarketType? = null,
    val openInterestLowerCap: kotlin.String? = null,
    val openInterestUpperCap: kotlin.String? = null,
    val baseOpenInterest: kotlin.String? = null,

    // Additional fields for WS
    val id: kotlin.String? = null,
    val marketId: kotlin.Int? = null,
    val baseAsset: kotlin.String? = null,
    val quoteAsset: kotlin.String? = null,
    val basePositionSize: kotlin.String? = null,
    val incrementalPositionSize: kotlin.String? = null,
    val maxPositionSize: kotlin.String? = null,

    // Unused fields
    val incrementalInitialMarginFraction: kotlin.String? = null,
) {
    fun copyNotNulls(from: IndexerCompositeMarketObject): IndexerCompositeMarketObject {
        return IndexerCompositeMarketObject(
            clobPairId = from.clobPairId ?: clobPairId,
            ticker = from.ticker ?: ticker,
            status = from.status ?: status,
            oraclePrice = from.oraclePrice ?: oraclePrice,
            priceChange24H = from.priceChange24H ?: priceChange24H,
            volume24H = from.volume24H ?: volume24H,
            trades24H = from.trades24H ?: trades24H,
            nextFundingRate = from.nextFundingRate ?: nextFundingRate,
            initialMarginFraction = from.initialMarginFraction ?: initialMarginFraction,
            maintenanceMarginFraction = from.maintenanceMarginFraction ?: maintenanceMarginFraction,
            openInterest = from.openInterest ?: openInterest,
            atomicResolution = from.atomicResolution ?: atomicResolution,
            quantumConversionExponent = from.quantumConversionExponent ?: quantumConversionExponent,
            tickSize = from.tickSize ?: tickSize,
            stepSize = from.stepSize ?: stepSize,
            stepBaseQuantums = from.stepBaseQuantums ?: stepBaseQuantums,
            subticksPerTick = from.subticksPerTick ?: subticksPerTick,
            marketType = from.marketType ?: marketType,
            openInterestLowerCap = from.openInterestLowerCap ?: openInterestLowerCap,
            openInterestUpperCap = from.openInterestUpperCap ?: openInterestUpperCap,
            baseOpenInterest = from.baseOpenInterest ?: baseOpenInterest,
            id = from.id ?: id,
            marketId = from.marketId ?: marketId,
            baseAsset = from.baseAsset ?: baseAsset,
            quoteAsset = from.quoteAsset ?: quoteAsset,
            basePositionSize = from.basePositionSize ?: basePositionSize,
            incrementalPositionSize = from.incrementalPositionSize ?: incrementalPositionSize,
            maxPositionSize = from.maxPositionSize ?: maxPositionSize,
            incrementalInitialMarginFraction = from.incrementalInitialMarginFraction ?: incrementalInitialMarginFraction,
        )
    }
}
