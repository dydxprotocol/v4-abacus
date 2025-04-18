package exchange.dydx.abacus.state.supervisor

import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.machine.launchIncentiveSeasons
import exchange.dydx.abacus.state.machine.onChainEquityTiers
import exchange.dydx.abacus.state.machine.onChainFeeTiers
import exchange.dydx.abacus.state.machine.onChainRewardTokenPrice
import exchange.dydx.abacus.state.machine.onChainRewardsParams
import exchange.dydx.abacus.state.machine.onChainWithdrawalCapacity
import exchange.dydx.abacus.state.machine.onChainWithdrawalGating
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.iMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

internal class SystemSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    internal val configs: SystemConfigs,
    private val incentiveSeasonReceived: (String?) -> Unit,
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
            if (configs.retrieveWithdrawSafetyChecks) {
                stateMachine.state?.input?.transfer?.type?.let { transferType ->
                    retrieveWithdrawSafetyChecks(transferType)
                }
            }
        }
    }

    internal fun didSetTransferType(transferType: TransferType) {
        if (stateMachine.featureFlags.withdrawalSafetyEnabled &&
            configs.retrieveWithdrawSafetyChecks &&
            (transferType == TransferType.withdrawal || transferType == TransferType.transferOut)
        ) {
            retrieveWithdrawSafetyChecks(transferType)
        }
    }

    private fun retrieveServerTime() {
        val url = helper.configs.publicApiUrl("time")
        if (url != null) {
            helper.get(url, null, null) { _, response, httpCode, _ ->
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
        val infoUrl = helper.configs.metadataServiceInfo()
        val pricesUrl = helper.configs.metadataServicePrices()
        if (infoUrl != null && pricesUrl != null) {
            val scope = CoroutineScope(Dispatchers.Unconfined)
            scope.launch {
                val deferredInfo = async { helper.postAsync(infoUrl, headers = null, body = null) }
                val deferredPrices =
                    async { helper.postAsync(pricesUrl, headers = null, body = null) }

                val infoResponse = deferredInfo.await()
                val pricesResponse = deferredPrices.await()

                if (infoResponse.response != null && pricesResponse.response != null) {
                    val stateChange = stateMachine.configurations(infoResponse.response, pricesResponse.response, null)
                    update(
                        changes = stateChange,
                        oldState = oldState,
                    )
                } else if (infoResponse.error != null) {
                    Logger.e {
                        "Failed to retrieve day mega vault pnl: ${infoResponse.error}"
                    }
                } else if (pricesResponse.error != null) {
                    Logger.e {
                        "Failed to retrieve hourly mega vault pnl: ${pricesResponse.error}"
                    }
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

            val marketId =
                stateMachine.internalState.rewardsParams?.marketId

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
                url,
                iMapOf(
                    "content-type" to "application/json",
                    "protocol" to "dydx-v4",
                ),
                requestBody,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    val oldState = stateMachine.state
                    update(stateMachine.launchIncentiveSeasons(response), oldState)

                    incentiveSeasonReceived(stateMachine.state?.launchIncentive?.currentSeason)
                }
            }
        }
    }

    fun retrieveWithdrawSafetyChecks(transferType: TransferType) {
        when (transferType) {
            TransferType.withdrawal -> {
                updateWithdrawalCapacity()
                updateWithdrawalGating()
            }

            TransferType.transferOut -> {
                updateWithdrawalGating()
            }

            else -> {
                // do nothing
            }
        }
    }
    private fun updateWithdrawalCapacity() {
        var denom = helper.environment.tokens["usdc"]?.denom
        val params = iMapOf(
            "denom" to denom,
        )
        val paramsInJson = helper.jsonEncoder.encode(params)
        helper.getOnChain(QueryType.GetWithdrawalCapacityByDenom, paramsInJson) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainWithdrawalCapacity(response), oldState)
        }
    }

    private fun updateWithdrawalGating() {
        helper.getOnChain(QueryType.GetWithdrawalAndTransferGatingStatus, null) { response ->
            val oldState = stateMachine.state
            update(stateMachine.onChainWithdrawalGating(response), oldState)
        }
    }
}
