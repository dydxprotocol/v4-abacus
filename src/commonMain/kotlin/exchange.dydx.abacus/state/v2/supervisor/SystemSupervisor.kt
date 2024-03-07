package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.launchIncentiveSeasons
import exchange.dydx.abacus.state.model.onChainEquityTiers
import exchange.dydx.abacus.state.model.onChainFeeTiers
import exchange.dydx.abacus.state.model.onChainRewardTokenPrice
import exchange.dydx.abacus.state.model.onChainRewardsParams
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.iMapOf

internal class SystemSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    internal val configs: SystemConfigs,
) : NetworkSupervisor(stateMachine, helper, analyticsUtils) {
    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        if (readyToConnect) {
            if (configs.retrieveMarketConfigs) {
                // get from web deployment
                retrieveMarketConfigs()
            }
            if (configs.retrieveLaunchIncentiveSeasons) {
                // get from launch incentive endpoints
                retrieveLaunchIncentiveSeasons()
            }
        }
    }

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)

        if (indexerConnected) {
            if (configs.retrieveServerTime) {
                retrieveServerTime()
            }
        }
    }

    override fun didSetValidatorConnected(validatorConnected: Boolean) {
        super.didSetValidatorConnected(validatorConnected)

        if (validatorConnected) {
            if (configs.retrieveEquityTiers) {
                retrieveEquityTiers()
            }
            if (configs.retrieveFeeTiers) {
                retrieveFeeTiers()
            }
            if (configs.retrieveRewardsParams) {
                retrieveRewardsParams()
            }
        }
    }

    override fun didSetSocketConnected(socketConnected: Boolean) {
        if (configs.subscribeToMarkets) {
            marketsChannelSubscription(socketConnected)
        }
    }

    private fun retrieveServerTime() {
        val url = helper.configs.publicApiUrl("time")
        if (url != null) {
            helper.get(url, null, null) { _, response, httpCode ->
                if (helper.success(httpCode) && response != null) {
                    val json = helper.parser.decodeJsonObject(response)
                    val time = helper.parser.asDatetime(json?.get("time"))
                    if (time != null) {
                        ServerTime.overWrite = time
                    }
                }
            }
        }
    }

    private fun retrieveMarketConfigs() {
        val oldState = stateMachine.state
        val url = helper.configs.configsUrl("markets")
        if (url != null) {
            helper.get(url, null, null) { _, response, httpCode ->
                if (helper.success(httpCode) && response != null) {
                    update(
                        // TODO, subaccountNumber required to refresh
                        stateMachine.configurations(response, null, helper.deploymentUri),
                        oldState
                    )
                }
            }
        }
    }

    private fun retrieveEquityTiers() {
        helper.getOnChain(QueryType.EquityTiers, null) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainEquityTiers(response), oldState)
        }
    }

    private fun retrieveFeeTiers() {
        helper.getOnChain(QueryType.FeeTiers, null) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainFeeTiers(response), oldState)
        }
    }

    private fun retrieveRewardsParams() {
        helper.getOnChain(QueryType.RewardsParams, null) { rewardsParams ->
            val oldState = stateMachine.state
            update(stateMachine.onChainRewardsParams(rewardsParams), oldState)

            val json = helper.parser.decodeJsonObject(rewardsParams)
            val marketId = helper.parser.asString(helper.parser.value(json, "params.marketId"))
            val params = iMapOf("marketId" to marketId)
            val paramsInJson = helper.jsonEncoder.encode(params)

            helper.getOnChain(QueryType.GetMarketPrice, paramsInJson) { marketPrice ->
                update(stateMachine.onChainRewardTokenPrice(marketPrice), oldState)
            }
        }
    }

    private fun retrieveLaunchIncentiveSeasons() {
        val url = helper.configs.launchIncentiveUrl("graphql")
        if (url != null) {
            val requestBody =
                "{\"operationName\":\"TradingSeasons\",\"variables\":{},\"query\":\"query TradingSeasons {tradingSeasons {startTimestamp label __typename }}\"}"
            helper.post(
                url, iMapOf(
                    "content-type" to "application/json",
                    "protocol" to "dydx-v4",
                ), requestBody
            ) { _, response, httpCode ->
                if (helper.success(httpCode) && response != null) {
                    val oldState = stateMachine.state
                    update(stateMachine.launchIncentiveSeasons(response), oldState)

//                    retrieveLaunchIncentivePoints()
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun marketsChannelSubscription(subscribe: Boolean = true) {
        val channel = helper.configs.marketsChannel() ?: throw Exception("market is null")
        helper.socket(
            helper.socketAction(subscribe), channel,
            if (subscribe && shouldBatchMarketsChannelData()) {
                iMapOf("batched" to "true")
            } else null
        )
    }

    private fun shouldBatchMarketsChannelData(): Boolean {
        return false
    }
}