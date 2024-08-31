package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.Logger
import indexer.models.chain.OnChainRewardsParamsResponse
import indexer.models.chain.OnChainTokenPriceResponse
import kollections.iListOf
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun TradingStateMachine.onChainRewardsParams(payload: String): StateChanges {
    if (staticTyping) {
        val rewardParamsObject = parser.asTypedObject<OnChainRewardsParamsResponse>(payload)
        internalState.rewardsParams = rewardsProcessor.process(rewardParamsObject)
    } else {
        val json = parser.decodeJsonObject(payload)
        val params = parser.asMap(json?.get("params"))
        rewardsParams =
            if (params != null) {
                rewardsProcessor.received(parser.asMap(rewardsParams), params)
            } else {
                null
            }
    }
    return StateChanges(iListOf())
}

internal fun TradingStateMachine.onChainRewardTokenPrice(payload: String): StateChanges? {
    if (staticTyping) {
        val tokenPriceResponse = parser.asTypedObject<OnChainTokenPriceResponse>(payload)
        internalState.rewardsParams = rewardsProcessor.processTokenPrice(
            eixsting = internalState.rewardsParams,
            payload = tokenPriceResponse,
        )
    } else {
        val json = try {
            Json.parseToJsonElement(payload).jsonObject.toMap()
        } catch (exception: SerializationException) {
            Logger.e {
                "Failed to deserialize onChainRewardTokenPrice: $payload \n" +
                    "Exception: $exception"
            }
            null
        }
        val map = parser.asMap(json)
        val price = parser.asMap(map?.get("marketPrice"))
        rewardsParams =
            if (price != null) {
                rewardsProcessor.receivedTokenPrice(parser.asMap(rewardsParams), price)
            } else {
                null
            }
    }
    return StateChanges(iListOf())
}
