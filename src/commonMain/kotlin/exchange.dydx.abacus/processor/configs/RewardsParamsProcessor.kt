package exchange.dydx.abacus.processor.configs

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalRewardsParamsState
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
    private val rewardsParamsKeyMap = mapOf(
        "string" to mapOf(
            "denom" to "denom",
        ),
        "double" to mapOf(
            "denomExponent" to "denomExponent",
            "feeMultiplierPpm" to "feeMultiplierPpm",
            "marketId" to "marketId",
        ),
    )

    private val tokenPriceMap = mapOf(
        "double" to mapOf(
            "price" to "price",
            "exponent" to "exponent",
        ),
    )

    override fun process(
        payload: OnChainRewardsParamsResponse?,
    ): InternalRewardsParamsState? {
        return if (payload != null) {
            InternalRewardsParamsState(
                denom = payload.params?.denom,
                denomExponent = payload.params?.denomExponent,
                marketId = payload.params?.marketId,
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

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return transform(existing, payload, rewardsParamsKeyMap)
    }

    fun receivedTokenPrice(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        return receivedObject(existing, "tokenPrice", payload) { existing, payload ->
            transform(parser.asNativeMap(existing), parser.asNativeMap(payload), tokenPriceMap)
        }
    }
}
