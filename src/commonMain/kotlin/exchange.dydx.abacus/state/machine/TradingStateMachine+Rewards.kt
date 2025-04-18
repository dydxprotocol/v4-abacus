package exchange.dydx.abacus.state.machine

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.StateChanges
import indexer.models.chain.OnChainRewardsParamsResponse
import indexer.models.chain.OnChainTokenPriceResponse
import kollections.iListOf

internal fun TradingStateMachine.onChainRewardsParams(payload: String): StateChanges {
    val rewardParamsObject = parser.asTypedObject<OnChainRewardsParamsResponse>(payload)
    internalState.rewardsParams = rewardsProcessor.process(rewardParamsObject)

    return StateChanges(iListOf())
}

internal fun TradingStateMachine.onChainRewardTokenPrice(payload: String): StateChanges? {
    val tokenPriceResponse = parser.asTypedObject<OnChainTokenPriceResponse>(payload)
    internalState.rewardsParams = rewardsProcessor.processTokenPrice(
        eixsting = internalState.rewardsParams,
        payload = tokenPriceResponse,
    )

    return StateChanges(iListOf())
}
