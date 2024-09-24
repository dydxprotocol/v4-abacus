package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.onMegaVaultPnl
import exchange.dydx.abacus.state.model.onVaultMarketPnls
import exchange.dydx.abacus.state.model.onVaultMarketPositions
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer

internal class VaultSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    internal val configs: VaultConfigs,
) : NetworkSupervisor(stateMachine, helper, analyticsUtils) {

    private var indexerTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }
    private val megaVaultPnlPollingDuration = 60.0

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)

        if (!configs.retrieveVault) {
            return
        }

        if (indexerConnected) {
            val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
            indexerTimer = timer.schedule(delay = 0.0, repeat = megaVaultPnlPollingDuration) {
                if (readyToConnect) {
                    retrieveMegaVaultPnl()
                    retrieveVaultMarketPnls()
                    retrieveVaultMarketPositions()
                }
                false
            }
        } else {
            indexerTimer = null
        }
    }

    private fun retrieveMegaVaultPnl() {
        val url = helper.configs.publicApiUrl("vault/megavault/historicalPnl")
        if (url != null) {
            helper.get(
                url = url,
                params = null,
                headers = null,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    stateMachine.onMegaVaultPnl(response)
                }
            }
        }
    }

    private fun retrieveVaultMarketPnls() {
        val url = helper.configs.publicApiUrl("vault/vaults/historicalPnl")
        if (url != null) {
            helper.get(
                url = url,
                params = null,
                headers = null,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    stateMachine.onVaultMarketPnls(response)
                }
            }
        }
    }

    private fun retrieveVaultMarketPositions() {
            val url = helper.configs.publicApiUrl("vault/positions")
        if (url != null) {
            helper.get(
                url = url,
                params = null,
                headers = null,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    stateMachine.onVaultMarketPositions(response)
                }
            }
        }
    }
}
