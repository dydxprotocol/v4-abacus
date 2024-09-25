package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.onMegaVaultPnl
import exchange.dydx.abacus.state.model.onVaultMarketPnls
import exchange.dydx.abacus.state.model.onVaultMarketPositions
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import indexer.codegen.IndexerAssetPositionResponseObject
import indexer.codegen.IndexerMegavaultHistoricalPnlResponse
import indexer.codegen.IndexerMegavaultPositionResponse
import indexer.codegen.IndexerPerpetualPositionResponseObject
import indexer.codegen.IndexerPerpetualPositionStatus
import indexer.codegen.IndexerPnlTicksResponseObject
import indexer.codegen.IndexerPositionSide
import indexer.codegen.IndexerVaultHistoricalPnl
import indexer.codegen.IndexerVaultPosition
import indexer.codegen.IndexerVaultsHistoricalPnlResponse
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
        if (configs.useMocks) {
            val mock = IndexerMegavaultHistoricalPnlResponse(
                megavaultPnl = arrayOf(
                    IndexerPnlTicksResponseObject(
                        equity = "10000.0",
                        totalPnl = "1000.0",
                        netTransfers = "0.0",
                        createdAt = Instant.fromEpochMilliseconds(1659465600000).toString(),
                    ),
                    IndexerPnlTicksResponseObject(
                        equity = "5000.0",
                        totalPnl = "500",
                        netTransfers = "0.0",
                        createdAt = Instant.fromEpochMilliseconds(1659379200000).toString(),
                    ),
                ),
            )
            stateMachine.onMegaVaultPnl(Json.encodeToString(mock))
        } else {
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
    }

    private fun retrieveVaultMarketPnls() {
        if (configs.useMocks) {
            val btcHistory = IndexerVaultHistoricalPnl(
                ticker = "BTC-USD",
                historicalPnl = arrayOf(
                    IndexerPnlTicksResponseObject(
                        id = "1",
                        equity = "10500.0",
                        totalPnl = "500.0",
                        netTransfers = "0.0",
                        createdAt = Instant.fromEpochMilliseconds(1659465600000).toString(),
                    ),
                    IndexerPnlTicksResponseObject(
                        id = "2",
                        equity = "10000.0",
                        totalPnl = "0.0",
                        netTransfers = "0.0",
                        createdAt = Instant.fromEpochMilliseconds(1659379200000).toString(),
                    ),
                ),
            )
            val marketPnls = IndexerVaultsHistoricalPnlResponse(
                vaultsPnl = arrayOf(btcHistory),
            )
            stateMachine.onVaultMarketPnls(Json.encodeToString(marketPnls))
        } else {
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
    }

    private fun retrieveVaultMarketPositions() {
        if (configs.useMocks) {
            val btcPosition = IndexerVaultPosition(
                ticker = "BTC-USD",
                assetPosition = IndexerAssetPositionResponseObject(
                    symbol = "USDC",
                    side = IndexerPositionSide.SHORT,
                    size = "40000.0",
                    assetId = "0",
                    subaccountNumber = NUM_PARENT_SUBACCOUNTS,
                ),
                perpetualPosition = IndexerPerpetualPositionResponseObject(
                    market = "BTC-USD",
                    status = IndexerPerpetualPositionStatus.OPEN,
                    side = IndexerPositionSide.LONG,
                    size = "1.0",
                    maxSize = null,
                    entryPrice = "50000.0",
                    realizedPnl = null,
                    createdAt = "2023-08-01T00:00:00Z",
                    createdAtHeight = "1000",
                    sumOpen = null,
                    sumClose = null,
                    netFunding = null,
                    unrealizedPnl = "5000.0",
                    closedAt = null,
                    exitPrice = null,
                    subaccountNumber = NUM_PARENT_SUBACCOUNTS,
                ),
                equity = "15000.0",
            )
            val megaVaultPosition = IndexerMegavaultPositionResponse(
                positions = arrayOf(btcPosition),
            )
            stateMachine.onVaultMarketPositions(Json.encodeToString(megaVaultPosition))
        } else {
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
}
