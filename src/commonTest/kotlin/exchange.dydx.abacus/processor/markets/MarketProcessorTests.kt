package exchange.dydx.abacus.processor.markets

import exchange.dydx.abacus.output.MarketConfigs
import exchange.dydx.abacus.output.MarketConfigsV4
import exchange.dydx.abacus.output.MarketPerpetual
import exchange.dydx.abacus.output.MarketStatus
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.PerpetualMarketType
import exchange.dydx.abacus.utils.Parser
import indexer.codegen.IndexerPerpetualMarketStatus
import indexer.codegen.IndexerPerpetualMarketType
import indexer.models.IndexerCompositeMarketObject
import indexer.models.IndexerWsMarketOraclePriceObject
import kotlin.test.Test
import kotlin.test.assertEquals

class MarketProcessorTests {
    companion object {
        val marketPayloadMock = IndexerCompositeMarketObject(
            clobPairId = "1",
            ticker = "BTC-USD",
            status = IndexerPerpetualMarketStatus.ACTIVE,
            oraclePrice = "20000.0000",
            priceChange24H = "138.180620",
            volume24H = "424522782.317100",
            trades24H = 54584,
            nextFundingRate = "0.0000102743",
            initialMarginFraction = "0.05",
            maintenanceMarginFraction = "0.03",
            openInterest = "5061.9983",
            atomicResolution = 10000,
            quantumConversionExponent = 10,
            tickSize = "0.1",
            stepSize = "0.0001",
            stepBaseQuantums = 10000,
            subticksPerTick = 1,
            marketType = IndexerPerpetualMarketType.CROSS,
            openInterestLowerCap = null,
            openInterestUpperCap = null,
            baseOpenInterest = null,
            id = null,
            marketId = null,
            baseAsset = null,
            quoteAsset = null,
            basePositionSize = null,
            incrementalPositionSize = null,
            maxPositionSize = null,
            incrementalInitialMarginFraction = null,
        )

        val marketUpdatePayloadMock = IndexerCompositeMarketObject(
            incrementalPositionSize = "5",
            basePositionSize = "25",
        )

        val oraclePricePayloadMock = IndexerWsMarketOraclePriceObject(
            oraclePrice = "10000.0000",
        )

        val outputMock = PerpetualMarket(
            id = "BTC-USD",
            assetId = "BTC",
            market = "BTC-USD",
            oraclePrice = 20000.0000,
            marketCaps = null,
            priceChange24H = 138.180620,
            priceChange24HPercent = 0.006957097804400636,
            status = MarketStatus(
                canTrade = true,
                canReduce = true,
            ),
            configs = MarketConfigs(
                initialMarginFraction = 0.05,
                maintenanceMarginFraction = 0.03,
                tickSize = 0.1,
                stepSize = 0.0001,
                incrementalPositionSize = null,
                maxPositionSize = null,
                incrementalInitialMarginFraction = null,
                clobPairId = "1",
                largeSize = null,
                stepSizeDecimals = 4,
                tickSizeDecimals = 1,
                displayStepSize = 0.0001,
                displayTickSize = 0.1,
                displayStepSizeDecimals = 4,
                displayTickSizeDecimals = 1,
                effectiveInitialMarginFraction = 0.05,
                minOrderSize = 0.0001,
                basePositionNotional = null,
                baselinePositionSize = null,
                candleOptions = null,
                perpetualMarketType = PerpetualMarketType.CROSS,
                v4 = MarketConfigsV4(
                    atomicResolution = 10000,
                    quantumConversionExponent = 10,
                    subticksPerTick = 1,
                    clobPairId = 1,
                    stepBaseQuantums = 10000,
                ),
            ),
            perpetual = MarketPerpetual(
                volume24H = 424522782.317100,
                trades24H = 54584.0,
                openInterest = 5061.9983,
                nextFundingRate = 0.0000102743,
                openInterestLowerCap = null,
                openInterestUpperCap = null,
                volume24HUSDC = null,
                nextFundingAtMilliseconds = null,
                openInterestUSDC = 101239966.0,
                line = null,
            ),
        )
    }

    private val processor = MarketProcessor(
        parser = Parser(),
        calculateSparklines = true,
    )

    @Test
    fun testProcess() {
        var output = processor.process("BTC-USD", marketPayloadMock)
        assertEquals(outputMock, output)

        output = processor.process("BTC-USD", marketUpdatePayloadMock)
        assertEquals(
            outputMock.copy(
                configs = outputMock.configs?.copy(
                    incrementalPositionSize = 5.0,
                    baselinePositionSize = 25.0,
                ),
            ),
            output,
        )
    }

    @Test
    fun testProcessOraclePrice() {
        processor.process("BTC-USD", marketPayloadMock)
        val output = processor.processOraclePrice("BTC-USD", oraclePricePayloadMock)
        assertEquals(output?.oraclePrice, 10000.0000)
        assertEquals(output?.priceChange24HPercent, 0.014011676210602023)
    }
}
