package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalRewardsParamsState
import indexer.models.chain.OnChainRewardsParamsResponse
import indexer.models.chain.OnChainTokenPriceResponse

internal interface RewardsParamsProcessorProtocol {
    fun process(
        payload: OnChainRewardsParamsResponse?,
    ): InternalRewardsParamsState?

    fun processTokenPrice(
        eixsting: InternalRewardsParamsState?,
        payload: OnChainTokenPriceResponse?,
    ): InternalRewardsParamsState?
}

internal class RewardsParamsProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), RewardsParamsProcessorProtocol {
    override fun process(
        payload: OnChainRewardsParamsResponse?,
    ): InternalRewardsParamsState? {
        return if (payload != null) {
            InternalRewardsParamsState(
                denom = payload.params?.denom,
                denomExponent = payload.params?.denomExponent,
                marketId = payload.params?.marketId,
                feeMultiplierPpm = payload.params?.feeMultiplierPpm,
            )
        } else {
            null
        }
    }

    override fun processTokenPrice(
        eixsting: InternalRewardsParamsState?,
        payload: OnChainTokenPriceResponse?,
    ): InternalRewardsParamsState? {
        return if (payload != null) {
            eixsting?.copy(
                tokenPrice = parser.asDouble(payload.marketPrice?.price),
                tokenExpoonent = payload.marketPrice?.exponent,
            )
        } else {
            null
        }
    }
}
