package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.FeeTierResources
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketFeeDiscountState
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
import indexer.models.chain.OnChainFeeDiscountsParams
import indexer.models.chain.OnChainFeeTier

internal interface FeeTiersProcessorProtocol {
    fun process(
        payload: List<OnChainFeeTier>?
    ): List<FeeTier>?

    fun processFeeDiscounts(
        payload: List<OnChainFeeDiscountsParams>?
    ): Map<String, InternalMarketFeeDiscountState>?
}

internal class FeeTiersProcessor(
    parser: ParserProtocol,
    private val localizer: LocalizerProtocol?,
) : BaseProcessor(parser), FeeTiersProcessorProtocol {
    override fun process(
        payload: List<OnChainFeeTier>?
    ): List<FeeTier>? {
        return payload?.mapNotNull {
            val absoluteVolumeRequirement = parser.asDecimal(it.absoluteVolumeRequirement)
            val volume = parser.asInt(absoluteVolumeRequirement?.div(QUANTUM_MULTIPLIER))
            if (volume != null && it.name != null) {
                FeeTier(
                    id = it.name,
                    tier = it.name,
                    symbol = "≥",
                    volume = volume,
                    totalShare = parser.asDouble(
                        parser.asDecimal(it.totalVolumeShareRequirementPpm)
                            ?.div(QUANTUM_MULTIPLIER),
                    ),
                    makerShare = parser.asDouble(
                        parser.asDecimal(it.makerVolumeShareRequirementPpm)
                            ?.div(QUANTUM_MULTIPLIER),
                    ),
                    maker = parser.asDouble(
                        parser.asDecimal(it.makerFeePpm)
                            ?.div(QUANTUM_MULTIPLIER),
                    ),
                    taker = parser.asDouble(
                        parser.asDecimal(it.takerFeePpm)
                            ?.div(QUANTUM_MULTIPLIER),
                    ),
                    resources = FeeTierResources(
                        stringKey = "FEE_TIER.${it.name}",
                        string = localizer?.localize("FEE_TIER.${it.name}"),
                    ),
                )
            } else {
                null
            }
        }
    }

    override fun processFeeDiscounts(
        payload: List<OnChainFeeDiscountsParams>?
    ): Map<String, InternalMarketFeeDiscountState>? {
        val marketFeeDiscounts = mutableMapOf<String, InternalMarketFeeDiscountState>()
        for (feeDiscount in payload ?: emptyList()) {
            val feeDiscountState = InternalMarketFeeDiscountState(
                startTime = feeDiscount.startTime,
                endTime = feeDiscount.endTime,
                chargePercent = feeDiscount.chargePpm?.div(QUANTUM_MULTIPLIER),
            )
            marketFeeDiscounts[feeDiscount.clobPairId.toString()] = feeDiscountState
        }
        return marketFeeDiscounts
    }
}
