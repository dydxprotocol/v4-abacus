package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.onAccountOwnerShares
import exchange.dydx.abacus.state.model.onMegaVaultPnl
import exchange.dydx.abacus.state.model.onVaultMarketPnls
import exchange.dydx.abacus.state.model.onVaultMarketPositions
import exchange.dydx.abacus.state.model.onVaultTransferHistory
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.Logger

internal class VaultSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    internal val configs: VaultConfigs,
) : NetworkSupervisor(stateMachine, helper, analyticsUtils) {

    var accountAddress: String? = null
        set(value) {
            if (value != accountAddress) {
                if (indexerConnected) {
                    stopPollingIndexerData()
                    startPollingIndexerData(value)
                }
                if (validatorConnected) {
                    stopPollingValidatorData()
                    startPollingValidatorData(value)
                }
                field = value
            }
        }

    companion object {
        private const val POLLING_DURATION = 20.0
        private const val MEGAVAULT_MODULE_ADDRESS = "dydx18tkxrnrkqc2t0lr3zxr5g6a4hdvqksylxqje4r"
    }

    private var indexerTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private var validatorTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    fun refreshVaultAccount() {
        if (accountAddress != null && validatorConnected) {
            stopPollingValidatorData()
            stopPollingIndexerData()
            startPollingValidatorData(accountAddress)
            startPollingIndexerData(accountAddress)
        }
    }

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)

        if (indexerConnected) {
            startPollingIndexerData(accountAddress)
        } else {
            stopPollingIndexerData()
        }
    }

    override fun didSetValidatorConnected(validatorConnected: Boolean) {
        super.didSetValidatorConnected(validatorConnected)

        if (validatorConnected) {
            startPollingValidatorData(accountAddress)
        } else {
            stopPollingValidatorData()
        }
    }

    private fun startPollingIndexerData(accountAddress: String?) {
        if (!configs.retrieveVault) {
            return
        }

        val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
        indexerTimer = timer.schedule(delay = 1.0, repeat = Companion.POLLING_DURATION) {
            if (readyToConnect) {
                retrieveMegaVaultPnl()
                retrieveVaultMarketPnls()
                retrieveVaultMarketPositions()
                if (accountAddress != null) {
                    retrieveTransferHistory(accountAddress)
                }
            }
            true // Repeat
        }
    }

    private fun stopPollingIndexerData() {
        indexerTimer = null
    }

    private fun startPollingValidatorData(accountAddress: String?) {
        if (!configs.retrieveVault) {
            return
        }

        val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
        validatorTimer = timer.schedule(delay = 1.0, repeat = Companion.POLLING_DURATION) {
            if (readyToConnect) {
                if (accountAddress != null) {
                    retrieveAccountShares(accountAddress)
                }
            }
            true // Repeat
        }
    }

    private fun stopPollingValidatorData() {
        validatorTimer = null
    }

    private fun retrieveMegaVaultPnl() {
        val url = helper.configs.publicApiUrl("vaultHistoricalPnl")
        if (url != null) {
            helper.get(
                url = url,
                params = mapOf("resolution" to "day"),
                headers = null,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    stateMachine.onMegaVaultPnl(response)
                } else {
                    Logger.e {
                        "Failed to retrieve mega vault pnl: $httpCode, $response"
                    }
                }
            }
        }
    }

    private fun retrieveVaultMarketPnls() {
        val url = helper.configs.publicApiUrl("vaultMarketPnls")
        if (url != null) {
            helper.get(
                url = url,
                params = mapOf("resolution" to "day"),
                headers = null,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    stateMachine.onVaultMarketPnls(response)
                } else {
                    Logger.e {
                        "Failed to retrieve vault market pnls: $httpCode, $response"
                    }
                }
            }
        }
    }

    private fun retrieveVaultMarketPositions() {
        val url = helper.configs.publicApiUrl("vaultPositions")
        if (url != null) {
            helper.get(
                url = url,
                params = null,
                headers = null,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    stateMachine.onVaultMarketPositions(response)
                } else {
                    Logger.e {
                        "Failed to retrieve vault market positions: $httpCode, $response"
                    }
                }
            }
        }
    }

    private fun retrieveTransferHistory(accountAddress: String) {
        val url = helper.configs.publicApiUrl("transfers")
        if (url != null) {
            helper.get(
                url = url,
                params = mapOf(
                    "sourceAddress" to accountAddress,
                    "sourceSubaccountNumber" to "0",
                    "recipientAddress" to MEGAVAULT_MODULE_ADDRESS,
                    "recipientSubaccountNumber" to "0",
                ),
                headers = null,
            ) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    stateMachine.onVaultTransferHistory(response)
                } else {
                    Logger.e {
                        "Failed to retrieve transfer history: $httpCode, $response"
                    }
                }
            }
        }
    }

    private fun retrieveAccountShares(accountAddress: String) {
        val payload =
            helper.jsonEncoder.encode(
                mapOf(
                    "address" to accountAddress,
                ),
            )
        helper.transaction(TransactionType.GetMegavaultOwnerShares, payload) { response ->
            val error = helper.parseTransactionResponse(response)
            if (error != null) {
                Logger.e { "getMegavaultOwnerShares error: $error" }
            } else {
                stateMachine.onAccountOwnerShares(response)
            }
        }
    }
}
