package exchange.dydx.abacus.state.v2.supervisor

import exchange.dydx.abacus.protocols.LocalTimerProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.state.manager.GasToken
import exchange.dydx.abacus.state.manager.IndexerURIs
import exchange.dydx.abacus.state.manager.NetworkState
import exchange.dydx.abacus.state.manager.StatsigConfig
import exchange.dydx.abacus.state.manager.SystemUtils
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.safeSet
import kotlinx.datetime.Clock

internal interface ConnectionDelegate {
    fun didConnectToIndexer(connectedToIndexer: Boolean)
    fun didConnectToValidator(connectedToValidator: Boolean)
    fun didConnectToSocket(connectedToSocket: Boolean)
    fun processSocketResponse(message: String)
}

internal class ConnectionsSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    private val delegate: ConnectionDelegate,
) : NetworkSupervisor(stateMachine, helper, analyticsUtils), ConnectionStatsDelegate {
    private val connectionStats = ConnectionStats(stateMachine, helper, this)

    override var validatorUrl: String?
        get() = helper.validatorUrl
        set(value) {
            helper.validatorUrl = value
            didSetValidatorUrl(value)
        }

    private val heightPollingDuration = 10.0
    private var heightTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    private var chainTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }

    override var indexerConfig: IndexerURIs?
        get() = helper.configs.indexerConfig
        set(value) {
            if (helper.configs.indexerConfig != value) {
                helper.configs.indexerConfig = value
                didSetIndexerConfig()
            }
        }

    private var indexerTimer: LocalTimerProtocol? = null
        set(value) {
            if (field !== value) {
                field?.cancel()
                field = value
            }
        }
    private val serverPollingDuration = 10.0

    internal val indexerState: NetworkState
        get() = connectionStats.indexerState

    internal val validatorState: NetworkState
        get() = connectionStats.validatorState

    internal var gasToken: GasToken? = null
        set(value) {
            if (field != value) {
                field = value
                updateSelectedGasToken()
            }
        }

    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        if (readyToConnect) {
            bestEffortConnectIndexer()
            bestEffortConnectChain()
        } else {
            chainTimer = null
            heightTimer = null
            indexerTimer = null

            indexerConfig = null
            validatorConnected = false
            socketConnected = false
            disconnectSocket()
        }
    }

    override fun didSetIndexerConnected(indexerConnected: Boolean) {
        super.didSetIndexerConnected(indexerConnected)
        delegate.didConnectToIndexer(indexerConnected)
        if (indexerConnected) {
            retrieveHeights()
            connectSocket()
        } else {
            disconnectSocket()
            reconnectIndexer()
        }
    }

    private fun didSetIndexerConfig() {
        indexerConnected = (indexerConfig != null)
    }

    private fun updateSelectedGasToken() {
        val gasTokenName = gasToken?.name
        if (validatorConnected && gasTokenName != null) {
            helper.ioImplementations.chain?.transaction(
                type = TransactionType.SetSelectedGasDenom,
                paramsInJson = gasTokenName,
            ) { response ->
                if (response != "success") {
                    Logger.e { "Failed to set selected gas token: $response" }
                }
            }
        }
    }

    private fun connectSocket() {
        val webscoketUrl = helper.configs.websocketUrl()
        if (webscoketUrl != null) {
            helper.ioImplementations.webSocket?.connect(webscoketUrl, connected = { connected ->
                if (!connected) {
                    // Do not set socketConnected to true here, wait for the "connected" message
                    socketConnected = false
                }
            }, received = { message ->
                delegate.processSocketResponse(message)
            })
        }
    }

    private fun disconnectSocket() {
        helper.ioImplementations.webSocket?.disconnect()
        delegate.didConnectToSocket(false)
    }

    private fun reconnectIndexer() {
        if (readyToConnect) {
            // Create a timer, to try to connect the chain again
            // Do not repeat. This timer is recreated in bestEffortConnectChain if needed
            val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
            indexerTimer = timer.schedule(serverPollingDuration, null) {
                if (readyToConnect) {
                    bestEffortConnectIndexer()
                }
                false
            }
        }
    }

    private fun bestEffortConnectIndexer() {
        indexerConfig = helper.configs.indexerConfigs?.firstOrNull()
    }

    private fun bestEffortConnectChain() {
        findOptimalNode { url ->
            this.validatorUrl = url
        }
    }

    private fun findOptimalNode(callback: (node: String?) -> Unit) {
        val endpointUrls = helper.configs.validatorUrls()
        if (endpointUrls != null && endpointUrls.size > 1) {
            val param = iMapOf(
                "endpointUrls" to endpointUrls,
                "chainId" to helper.environment.dydxChainId,
            )
            val json = helper.jsonEncoder.encode(param)
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                helper.ioImplementations.chain?.get(QueryType.OptimalNode, json) { result ->
                    if (result != null) {
                        /*
                    response = {
                        "url": "https://...",
                         */
                        val map = helper.parser.decodeJsonObject(result)
                        val node = helper.parser.asString(map?.get("url"))
                        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                            callback(node)
                        }
                    } else {
                        // Not handled by client yet
                        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                            callback(endpointUrls.firstOrNull())
                        }
                    }
                }
            }
        } else {
            val first = helper.parser.asString(endpointUrls?.firstOrNull())
            helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                callback(first)
            }
        }
    }

    private fun connectChain(validatorUrl: String, callback: (successful: Boolean) -> Unit) {
        val indexerUrl = helper.environment.endpoints.indexers?.firstOrNull()?.api ?: return
        val websocketUrl = helper.configs.websocketUrl() ?: return
        val chainId = helper.environment.dydxChainId ?: return
        val faucetUrl = helper.configs.faucetUrl()
        val usdcToken = helper.environment.tokens["usdc"] ?: return
        val chainToken = helper.environment.tokens["chain"] ?: return
        val usdcDenom = usdcToken.denom
        val usdcDecimals = usdcToken.decimals
        val usdcGasDenom = usdcToken.gasDenom
        val chainTokenDenom = chainToken.denom
        val chainTokenDecimals = chainToken.decimals
        val nobleValidator = helper.environment.endpoints.nobleValidator

        val params = mutableMapOf<String, Any>()
        params["indexerUrl"] = indexerUrl
        params["websocketUrl"] = websocketUrl
        params["validatorUrl"] = validatorUrl
        params["chainId"] = chainId
        params.safeSet("faucetUrl", faucetUrl)
        params.safeSet("nobleValidatorUrl", nobleValidator)

        params.safeSet("USDC_DENOM", usdcDenom)
        params.safeSet("USDC_DECIMALS", usdcDecimals)
        params.safeSet("USDC_GAS_DENOM", usdcGasDenom)
        params.safeSet("CHAINTOKEN_DENOM", chainTokenDenom)
        params.safeSet("CHAINTOKEN_DECIMALS", chainTokenDecimals)
        params.safeSet("txnMemo", "dYdX Frontend (${SystemUtils.platform.rawValue})")

        params.safeSet("enableTimestampNonce", StatsigConfig.ff_enable_timestamp_nonce)

        val jsonString = JsonEncoder().encode(params) ?: return

        helper.ioImplementations.threading?.async(ThreadingType.main) {
            helper.ioImplementations.chain?.connectNetwork(
                jsonString,
            ) { response ->
                helper.ioImplementations.threading?.async(ThreadingType.abacus) {
                    if (response != null) {
                        val json = helper.parser.decodeJsonObject(response)
                        helper.ioImplementations.threading?.async(ThreadingType.main) {
                            if (json != null) {
                                val error = json["error"]
                                if (error != null) {
                                    tracking(
                                        eventName = "ConnectNetworkFailed",
                                        params = iMapOf(
                                            "errorMessage" to helper.parser.asString(error),
                                        ),
                                    )
                                }
                                callback(error == null)
                            } else {
                                tracking(
                                    eventName = "ConnectNetworkFailed",
                                    params = iMapOf(
                                        "errorMessage" to "Invalid response: $response",
                                    ),
                                )
                                callback(false)
                            }
                        }
                    } else {
                        helper.ioImplementations.threading?.async(ThreadingType.main) {
                            tracking(
                                eventName = "ConnectNetworkFailed",
                                params = iMapOf(
                                    "errorMessage" to "null response",
                                ),
                            )
                            callback(false)
                        }
                    }
                }
            }
        }
    }

    private fun didSetValidatorUrl(validatorUrl: String?) {
        validatorConnected = false
        if (validatorUrl != null) {
            connectChain(validatorUrl) { successful ->
                validatorConnected = successful
            }
        } else {
            reconnectChain()
        }
    }

    override fun didSetValidatorConnected(validatorConnected: Boolean) {
        super.didSetValidatorConnected(validatorConnected)
        delegate.didConnectToValidator(validatorConnected)
        if (validatorConnected) {
            retrieveHeights()
            updateSelectedGasToken()
        } else {
            reconnectChain()
        }
    }

    override fun didSetSocketConnected(socketConnected: Boolean) {
        super.didSetSocketConnected(socketConnected)
        delegate.didConnectToSocket(socketConnected)
        if (!socketConnected && readyToConnect) {
            connectSocket()
        }
    }

    private fun retrieveHeights() {
        if (indexerConnected && validatorConnected) {
            val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
            heightTimer = timer.schedule(0.0, heightPollingDuration) {
                if (readyToConnect) {
                    getHeights()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun getHeights() {
        // serialize height retrieval. Get indexer height first, then validator height
        // If indexer height is not available, then validator height is not available
        // indexer height no longer triggers api state change
        getIndexerHeight {
            getValidatorHeight()
        }
    }

    private fun getIndexerHeight(callback: (() -> Unit)? = null) {
        val url = helper.configs.publicApiUrl("height")
        if (url != null) {
            connectionStats.indexerState.previousRequestTime =
                connectionStats.indexerState.requestTime
            connectionStats.indexerState.requestTime = Clock.System.now()
            helper.get(url, null, null) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    val json = helper.parser.decodeJsonObject(response)
                    if (json != null) {
                        val height = helper.parser.asInt(json["height"])
                        val time = helper.parser.asDatetime(json["time"])
                        connectionStats.indexerState.updateHeight(height, time)
                    } else {
                        connectionStats.indexerState.updateHeight(null, null)
                    }
                } else {
                    connectionStats.indexerState.updateHeight(null, null)
                }
                callback?.invoke()
                /*
                Indexer height no longer triggers api state change
                 */
            }
        }
    }

    private fun getValidatorHeight() {
        connectionStats.validatorState.requestTime = Clock.System.now()
        helper.getOnChain(QueryType.Height, null) { response ->
            connectionStats.parseHeight(response)
        }
    }

    private fun reconnectChain() {
        if (readyToConnect) {
            // Create a timer, to try to connect the chain again
            // Do not repeat. This timer is recreated in bestEffortConnectChain if needed
            val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
            chainTimer = timer.schedule(serverPollingDuration, null) {
                if (readyToConnect) {
                    bestEffortConnectChain()
                }
                false
            }
        }
    }

    internal fun calculateCurrentHeight(): Int? {
        val latestBlockAndTime =
            connectionStats.validatorState.blockAndTime ?: connectionStats.indexerState.blockAndTime
                ?: return null
        val currentTime = ServerTime.now()
        val lapsedTime = currentTime - latestBlockAndTime.localTime
        return if (lapsedTime.inWholeMilliseconds <= 0L) {
            // This should never happen we use system time, then we don't want to estimate height
            null
        } else {
            val firstBlockAndTime = connectionStats.firstBlockAndTime
            if (firstBlockAndTime == null) {
                // This should never happen, but just in case, assume 1.5s per block
                null
            } else {
                val lapsedBlocks = latestBlockAndTime.block - firstBlockAndTime.block
                if (lapsedBlocks <= 0) {
                    // This should never happen
                    null
                } else {
                    val betweenLastAndFirstBlockTime =
                        latestBlockAndTime.time - firstBlockAndTime.time
                    val averageMillisecondsPerBlock =
                        betweenLastAndFirstBlockTime.inWholeMilliseconds / lapsedBlocks
                    if (averageMillisecondsPerBlock <= 0L) {
                        // This should never happen
                        latestBlockAndTime.block
                    } else {
                        latestBlockAndTime.block + (lapsedTime.inWholeMilliseconds / averageMillisecondsPerBlock).toInt()
                    }
                }
            }
        }
    }
}
