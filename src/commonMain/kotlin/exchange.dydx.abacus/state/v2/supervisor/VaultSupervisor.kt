package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.onVaultPnl
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
    private val vaultPnlPollingDuration = 60.0

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)

        if (!configs.retrieveVault) {
            return
        }

        if (indexerConnected) {
            val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
            indexerTimer = timer.schedule(delay = 0.0, repeat = vaultPnlPollingDuration) {
                if (readyToConnect) {
                    retrieveVaultPnl()
                }
                false
            }
        } else {
            indexerTimer = null
        }
    }

    private fun retrieveVaultPnl() {
        val url = helper.configs.publicApiUrl("vault/megavault/historicalPnl")
        if (url != null) {
            helper.get(
                url = url,
                params = null,
                headers = null,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    stateMachine.onVaultPnl(response)
                }
            }
        }
    }
}
