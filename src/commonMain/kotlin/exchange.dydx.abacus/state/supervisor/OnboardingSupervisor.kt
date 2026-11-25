package exchange.dydx.abacus.state.supervisor

import RpcConfigsProcessor
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.processor.router.ChainType
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.state.Changes
import exchange.dydx.abacus.state.StateChanges
import exchange.dydx.abacus.state.machine.TradingStateMachine
import exchange.dydx.abacus.state.machine.TransferInputField
import exchange.dydx.abacus.state.machine.WalletConnectionType
import exchange.dydx.abacus.state.machine.evmSwapVenues
import exchange.dydx.abacus.state.machine.routerChains
import exchange.dydx.abacus.state.machine.routerRoute
import exchange.dydx.abacus.state.machine.routerStatus
import exchange.dydx.abacus.state.machine.routerTokens
import exchange.dydx.abacus.state.machine.routerTrack
import exchange.dydx.abacus.state.machine.transfer
import exchange.dydx.abacus.state.manager.CctpChainTokenInfo
import exchange.dydx.abacus.state.manager.CctpConfig
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.Platform
import exchange.dydx.abacus.state.manager.RpcConfigs
import exchange.dydx.abacus.state.manager.SystemUtils
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.SLIPPAGE_PERCENT
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.isAddressValid
import exchange.dydx.abacus.utils.toInjectiveEvmAddress
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import exchange.dydx.abacus.utils.toNeutronAddress
import exchange.dydx.abacus.utils.toOsmosisAddress
import kollections.iListOf
import kollections.toIMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

private val SMART_SWAP_OPTIONS = mapOf(
    "evm_swaps" to true,
)

private const val IBC_BRIDGE_ID = "IBC"
private const val CCTP_BRIDGE_ID = "CCTP"
private const val AXELAR_BRIDGE_ID = "AXELAR"

internal class OnboardingSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    private val configs: OnboardingConfigs,
) : NetworkSupervisor(stateMachine, helper, analyticsUtils) {

    var walletConnectionType: WalletConnectionType? = WalletConnectionType.Ethereum
        set(value) {
            if (field != value) {
                field = value
                stateMachine.routerProcessor.selectedChainType =
                    if (value == WalletConnectionType.Cosmos) {
                        ChainType.COSMOS
                    } else if (value === WalletConnectionType.Solana) {
                        ChainType.SVM
                    } else {
                        ChainType.EVM
                    }
            }
        }

    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)

        if (readyToConnect) {
            if (configs.retrieveRoutes) {
                retrieveAssetsFromRouter()
            }
        }
    }

    private fun retrieveAssetsFromRouter() {
        CoroutineScope(Dispatchers.Unconfined).launch {
            retrieveSkipTransferChains()
        }
        retrieveSkipTransferTokens()
        retrieveSkipEvmSwapVenues()
        retrieveCctpChainIds()
    }

    private suspend fun retrieveSkipTransferChains() = coroutineScope {
        val chainsUrl = helper.configs.skipV1Chains()
        val chainsRequestDeferred = async { helper.getAsync(chainsUrl, null, null).response }

        // web does not need rpc endpoints to be available since web uses wagmi sdk for this
        if (SystemUtils.platform == Platform.android || SystemUtils.platform == Platform.ios) {
            // Fetch RPC endpoints in parallel with chains fetch for efficiency
            async { updateChainRpcEndpoints() }.await()
        }

        chainsRequestDeferred.await()?.let { chainsResponse ->
            update(stateMachine.routerChains(chainsResponse), stateMachine.state)
        }
    }

    private suspend fun updateChainRpcEndpoints() {
        val url = "${helper.deploymentUri}/configs/rpc.json"
        helper.getAsync(url).response?.let { response ->
            RpcConfigsProcessor(helper.parser, configs.alchemyApiKey).received(response).let { rpcMap ->
                RpcConfigs.chainRpcMap = rpcMap
                stateMachine.internalState.configs.rpcMap = rpcMap
                val oldState = stateMachine.state
                update(StateChanges(iListOf(Changes.configs)), oldState)
            }
        }
    }

    private fun retrieveSkipEvmSwapVenues() {
        helper.get(helper.configs.skipV2Venues, null, null) { _, response, httpCode, _ ->
            if (!helper.success(httpCode) || response == null) {
                Logger.e { "retrieveSkipEVMSwapVenues error, code: $httpCode" }
            } else {
                stateMachine.evmSwapVenues(response)
            }
        }
    }

    private fun retrieveSkipTransferTokens() {
        val oldState = stateMachine.state
        val tokensUrl = helper.configs.skipV1Assets()
        helper.get(tokensUrl, null, null) { _, response, httpCode, _ ->
            if (helper.success(httpCode) && response != null) {
                update(stateMachine.routerTokens(response), oldState)
            }
        }
    }

    private fun retrieveCctpChainIds() {
        val url = "${helper.deploymentUri}/configs/cctp.json"
        helper.get(url) { _, response, _, _ ->
            if (response != null) {
                val chainIds = mutableListOf<CctpChainTokenInfo>()
                val chains: List<JsonElement>? =
                    helper.parser.decodeJsonArray(response)?.toList() as? List<JsonElement>
                for (chain in chains ?: emptyList()) {
                    val chainInfo = chain.jsonObject
                    val chainId = helper.parser.asString(chainInfo["chainId"])
                    val tokenAddress = helper.parser.asString(chainInfo["tokenAddress"])
                    if (chainId != null && tokenAddress != null) {
                        chainIds.add(CctpChainTokenInfo(chainId, tokenAddress))
                    }
                }
                CctpConfig.cctpChainIds = chainIds
            }
        }
    }

    private fun retrieveDepositRoute(
        state: PerpetualState?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
        injectiveAddress: String?,
    ) {
        if (stateMachine.skipGoFast) {
            stateMachine.internalState.input.transfer.goFastRoute = null
            stateMachine.internalState.input.transfer.route = null
            stateMachine.internalState.input.transfer.goFastSummary = null
            stateMachine.internalState.input.transfer.summary = null
            retrieveSkipDepositRouteGoFast(
                state = state,
                accountAddress = accountAddress,
                sourceAddress = sourceAddress,
                subaccountNumber = subaccountNumber,
                injectiveAddress = injectiveAddress,
                goFast = true,
            )
            retrieveSkipDepositRouteGoFast(
                state = state,
                accountAddress = accountAddress,
                sourceAddress = sourceAddress,
                subaccountNumber = subaccountNumber,
                injectiveAddress = injectiveAddress,
                goFast = false,
            )
        } else {
            val isCctp = state?.input?.transfer?.isCctp ?: false
            if (isCctp) {
                retrieveSkipDepositRouteCCTP(
                    state = state,
                    accountAddress = accountAddress,
                    sourceAddress = sourceAddress,
                    subaccountNumber = subaccountNumber,
                    injectiveAddress = injectiveAddress,
                )
            } else {
                retrieveSkipDepositRouteNonCCTP(
                    state = state,
                    accountAddress = accountAddress,
                    sourceAddress = sourceAddress,
                    subaccountNumber = subaccountNumber,
                    injectiveAddress = injectiveAddress,
                )
            }
        }
    }

    // externalChain (deposit source or withdrawal destination) goes last so it wins if it's also a hop
    private fun routeChainAddresses(
        accountAddress: String,
        injectiveAddress: String?,
        externalChain: String,
        externalAddress: String,
        includeOsmosisAndNeutron: Boolean,
    ): Map<String, String?>? {
        val injectiveEvmAddress = injectiveAddress?.toInjectiveEvmAddress() ?: return null
        val dydxChainId = helper.environment.dydxChainId ?: return null
        val addresses = mutableMapOf<String, String?>(
            helper.configs.injectiveChainId() to injectiveAddress,
            helper.configs.injectiveEvmChainId to injectiveEvmAddress,
            dydxChainId to accountAddress,
        )
        if (includeOsmosisAndNeutron) {
            addresses[helper.configs.osmosisChainId()] = accountAddress.toOsmosisAddress()
            addresses[helper.configs.neutronChainId()] = accountAddress.toNeutronAddress()
        }
        addresses[externalChain] = externalAddress
        return addresses
    }

    private fun retrieveSkipDepositRouteGoFast(
        state: PerpetualState?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
        injectiveAddress: String?,
        goFast: Boolean
    ) {
        val fromChain = state?.input?.transfer?.chain ?: return
        val fromTokenDenom = state.input.transfer.token ?: return
        val fromTokenSkipDenom = stateMachine.routerProcessor.getTokenByDenomAndChainId(
            tokenDenom = fromTokenDenom,
            chainId = fromChain,
        )?.get("skipDenom")
//        Denoms for tokens on their native chains are returned from the skip API in an incompatible
//        format for our frontend SDKs but are required by the skip API for other API calls.
//        So we prefer the skimDenom and default to the regular denom for API calls.
        val fromTokenDenomForAPIUse = fromTokenSkipDenom ?: fromTokenDenom

        val decimals = stateMachine.internalState.input.transfer.decimals ?: return
        val fromAmount = helper.parser.asDecimal(state.input.transfer.size?.size)?.let {
            (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        }
        if (fromAmount == null || fromAmount <= 0) {
            return
        }
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val chainAddresses = routeChainAddresses(
            accountAddress = accountAddress,
            injectiveAddress = injectiveAddress,
            externalChain = fromChain,
            externalAddress = sourceAddress,
            includeOsmosisAndNeutron = true,
        ) ?: return

        val body = mutableMapOf(
            "amount_in" to fromAmountString,
            "source_asset_denom" to fromTokenDenomForAPIUse,
            "source_asset_chain_id" to fromChain,
            "dest_asset_denom" to helper.configs.injectiveEvmUsdcAddress,
            "dest_asset_chain_id" to helper.configs.injectiveEvmChainId,
            "chain_ids_to_addresses" to chainAddresses,
            "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
            "allow_unsafe" to true,
            "smart_relay" to true,
            "allow_multi_tx" to true,
            "go_fast" to goFast,
            "smart_swap_options" to mapOf(
                "split_routes" to false,
                "evm_swaps" to true,
            ),
            "bridges" to listOf(
                "IBC",
                "AXELAR",
                "CCTP",
                "GO_FAST",
            ),
        )

        val oldState = stateMachine.state
        val header = iMapOf(
            "Content-Type" to "application/json",
        )
        Logger.ddInfo(body.toIMap(), { "retrieveSkipDepositRouteGoFast payload sending" })
        val url = helper.configs.skipV2MsgsDirect()

        helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, headers ->
            if (response != null) {
                Logger.ddInfo(
                    helper.parser.decodeJsonObject(response),
                    { "retrieveSkipDepositRouteGoFast payload received" },
                )
                val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                val oldFromAmount = oldState?.input?.transfer?.size?.size
                if (currentFromAmount == oldFromAmount) {
                    val change = stateMachine.routerRoute(
                        payload = response,
                        subaccountNumber = subaccountNumber ?: 0,
                        requestId = null,
                        goFast = goFast,
                    )
                    update(change, oldState)
                }
            } else {
                Logger.e { "retrieveSkipDepositRouteGoFast error, code: $code" }
            }
        }
    }

    private fun retrieveSkipDepositRouteNonCCTP(
        state: PerpetualState?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
        injectiveAddress: String?,
    ) {
        val fromChain = state?.input?.transfer?.chain ?: return
        val fromTokenDenom = state.input.transfer.token ?: return
        val fromTokenSkipDenom = stateMachine.routerProcessor.getTokenByDenomAndChainId(
            tokenDenom = fromTokenDenom,
            chainId = fromChain,
        )?.get("skipDenom")
//        Denoms for tokens on their native chains are returned from the skip API in an incompatible
//        format for our frontend SDKs but are required by the skip API for other API calls.
//        So we prefer the skimDenom and default to the regular denom for API calls.
        val fromTokenDenomForAPIUse = fromTokenSkipDenom ?: fromTokenDenom
        val fromAmount = helper.parser.asDecimal(state.input.transfer.size?.size)?.let {
            val decimals =
                helper.parser.asInt(
                    stateMachine.routerProcessor.selectedTokenDecimals(
                        tokenAddress = fromTokenDenom,
                        selectedChainId = fromChain,
                    ),
                )
            if (decimals != null) {
                (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
            } else {
                null
            }
        }
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val url = helper.configs.skipV2MsgsDirect()
        val chainAddresses = routeChainAddresses(
            accountAddress = accountAddress,
            injectiveAddress = injectiveAddress,
            externalChain = fromChain,
            externalAddress = sourceAddress,
            includeOsmosisAndNeutron = true,
        ) ?: return

        val options = mapOf(
            "bridges" to listOf(
                IBC_BRIDGE_ID,
                AXELAR_BRIDGE_ID,
                CCTP_BRIDGE_ID,
            ),
            "smart_swap_options" to SMART_SWAP_OPTIONS,
            "allow_multi_tx" to true,
        )
        if (fromAmount != null && fromAmount > 0) {
            val body: Map<String, Any> = mapOf(
                "amount_in" to fromAmountString,
                "source_asset_denom" to fromTokenDenomForAPIUse,
                "source_asset_chain_id" to fromChain,
                "dest_asset_denom" to helper.configs.injectiveEvmUsdcAddress,
                "dest_asset_chain_id" to helper.configs.injectiveEvmChainId,
                "chain_ids_to_addresses" to chainAddresses,
                "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
            ) + options

            val oldState = stateMachine.state
            val header = iMapOf(
                "Content-Type" to "application/json",
            )
            Logger.ddInfo(body.toIMap(), { "retrieveSkipDepositRouteNonCCTP payload sending" })
            helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, headers ->
                if (response != null) {
                    Logger.ddInfo(
                        helper.parser.decodeJsonObject(response),
                        { "retrieveSkipDepositRouteNonCCTP payload received" },
                    )
                    val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                    val oldFromAmount = oldState?.input?.transfer?.size?.size
                    if (currentFromAmount == oldFromAmount) {
                        update(stateMachine.routerRoute(response, subaccountNumber ?: 0, null, false), oldState)
                    }
                } else {
                    Logger.e { "retrieveSkipDepositRouteNonCCTP error, code: $code" }
                }
            }
        }
    }

    private fun retrieveSkipDepositRouteCCTP(
        state: PerpetualState?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
        injectiveAddress: String?,
    ) {
//      We have a lot of duplicate code for these deposit/withdrawal route calls
//      It's easier to dedupe now that the url is the same and only the args differ
//      We can consider deduping this at a later point.
        val fromChain = state?.input?.transfer?.chain ?: return
        val fromToken = state.input.transfer.token ?: return
        val fromAmount = helper.parser.asDecimal(state.input.transfer.size?.size)?.let {
            val decimals = helper.parser.asInt(
                stateMachine.routerProcessor.selectedTokenDecimals(
                    tokenAddress = fromToken,
                    selectedChainId = fromChain,
                ),
            )
            if (decimals != null) {
                (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
            } else {
                null
            }
        } ?: return
        if (fromAmount <= 0) return
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val url = helper.configs.skipV2MsgsDirect()
        val chainAddresses = routeChainAddresses(
            accountAddress = accountAddress,
            injectiveAddress = injectiveAddress,
            externalChain = fromChain,
            externalAddress = sourceAddress,
            includeOsmosisAndNeutron = false,
        ) ?: return
        val body: Map<String, Any> = mapOf(
            "amount_in" to fromAmountString,
            "source_asset_denom" to fromToken,
            "source_asset_chain_id" to fromChain,
            "dest_asset_denom" to helper.configs.injectiveEvmUsdcAddress,
            "dest_asset_chain_id" to helper.configs.injectiveEvmChainId,
            "chain_ids_to_addresses" to chainAddresses,
            "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
            "allow_multi_tx" to true,
            "bridges" to listOf(
                CCTP_BRIDGE_ID,
                IBC_BRIDGE_ID,
            ),
        )
        val oldState = stateMachine.state
        val header = iMapOf(
            "Content-Type" to "application/json",
        )
        Logger.ddInfo(body.toIMap(), { "retrieveSkipDepositRouteCCTP payload sending" })
        helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, headers ->
            if (response != null) {
                Logger.ddInfo(
                    helper.parser.decodeJsonObject(response),
                    { "retrieveSkipDepositRouteCCTP payload received" },
                )
                val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                val oldFromAmount = oldState?.input?.transfer?.size?.size
                if (currentFromAmount == oldFromAmount) {
                    update(stateMachine.routerRoute(response, subaccountNumber ?: 0, null, false), oldState)
                }
            } else {
                Logger.e { "retrieveSkipDepositRouteCCTP error, code: $code" }
            }
        }
    }

    internal fun transfer(
        data: String?,
        type: TransferInputField?,
        accountAddress: String?,
        sourceAddress: String?,
        subaccountNumber: Int?,
        injectiveAddress: String?,
    ) {
        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse = stateMachine.transfer(
                data = data,
                type = type,
                subaccountNumber = subaccountNumber ?: 0,
                environment = helper.environment,
            )
            if (accountAddress != null && sourceAddress != null) {
                processTransferInput(
                    type = type,
                    accountAddress = accountAddress,
                    sourceAddress = sourceAddress,
                    subaccountNumber = subaccountNumber,
                    injectiveAddress = injectiveAddress,
                )
            }
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                helper.stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    private fun processTransferInput(
        type: TransferInputField?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
        injectiveAddress: String?,
    ) {
        val state = stateMachine.state
        if (state?.input?.transfer?.type == TransferType.deposit) {
            if (type == TransferInputField.size) {
                retrieveDepositRoute(state, accountAddress, sourceAddress, subaccountNumber, injectiveAddress)
            }
        } else if (state?.input?.transfer?.type == TransferType.withdrawal) {
            if (type == TransferInputField.usdcSize ||
                type == TransferInputField.address ||
                type == TransferInputField.chain ||
                type == TransferInputField.token
            ) {
                val decimals = helper.environment.tokens["usdc"]?.decimals ?: 6

                val usdcSize =
                    helper.parser.asDouble(state.input.transfer.size?.usdcSize)
                        ?: Numeric.double.ZERO
                if (usdcSize > Numeric.double.ZERO) {
                    simulateWithdrawal(decimals, subaccountNumber) { gasFee ->
                        retrieveWithdrawalRoute(
                            state = state,
                            decimals = decimals,
                            gas = gasFee ?: Numeric.decimal.ZERO,
                            accountAddress = accountAddress,
                            subaccountNumber = subaccountNumber,
                            injectiveAddress = injectiveAddress,
                        )
                    }
                }
            }
        } else if (state?.input?.transfer?.type == TransferType.transferOut &&
            state.input.transfer.address != null &&
            (state.input.transfer.errors?.count() ?: 0) == 0
        ) {
            if (type == TransferInputField.usdcSize ||
                type == TransferInputField.size ||
                type == TransferInputField.token ||
                type == TransferInputField.address
            ) {
                val token = state.input.transfer.token
                if (token == "usdc") {
                    val decimals = helper.environment.tokens[token]?.decimals ?: 6

                    val usdcSize =
                        helper.parser.asDouble(state.input.transfer.size?.usdcSize)
                            ?: Numeric.double.ZERO
                    if (usdcSize > Numeric.double.ZERO) {
                        simulateWithdrawal(decimals, subaccountNumber) { gasFee ->
                            receiveTransferGas(gasFee)
                        }
                    } else {
                        receiveTransferGas(null)
                    }
                } else if (token == "chain") {
                    val decimals = helper.environment.tokens[token]?.decimals ?: 18

                    val address = state.input.transfer.address
                    val tokenSize =
                        helper.parser.asDouble(state.input.transfer.size?.size)
                            ?: Numeric.double.ZERO
                    if (tokenSize > Numeric.double.ZERO && address.isAddressValid()
                    ) {
                        simulateTransferNativeToken(decimals, subaccountNumber) { gasFee ->
                            receiveTransferGas(gasFee)
                        }
                    } else {
                        receiveTransferGas(null)
                    }
                }
            }
        }
    }

    internal fun transferStatus(
        hash: String,
        fromChainId: String?,
        toChainId: String?,
        isCctp: Boolean,
        requestId: String?,
    ) {
        fetchTransferStatusSkip(hash, fromChainId)
    }

    private fun simulateWithdrawal(
        decimals: Int,
        subaccountNumber: Int?,
        callback: (BigDecimal?) -> Unit
    ) {
        val payload = withdrawPayloadJson(subaccountNumber)

        helper.transaction(
            TransactionType.simulateWithdraw,
            payload,
        ) { response ->
            val error = helper.parseTransactionResponse(response)
            if (error != null) {
                Logger.e { "simulateWithdrawal error: $error" }
                callback(null)
                return@transaction
            }

            val result = helper.parser.decodeJsonObject(response)
            if (result != null) {
                val amountMap =
                    helper.parser.asMap(helper.parser.asList(result["amount"])?.firstOrNull())
                val amount = helper.parser.asDecimal(amountMap?.get("amount"))
                val usdcAmount = amount?.div(Numeric.decimal.TEN.pow(decimals))
                callback(usdcAmount)
            } else {
                callback(null)
            }
        }
    }

    private fun simulateTransferNativeToken(
        decimals: Int,
        subaccountNumber: Int?,
        callback: (BigDecimal?) -> Unit
    ) {
        val payload = transferNativeTokenPayloadJson(subaccountNumber)

        helper.transaction(
            TransactionType.simulateTransferNativeToken,
            payload,
        ) { response ->
            val error = helper.parseTransactionResponse(response)
            if (error != null) {
                Logger.e { "simulateTransferNativeToken error: $error" }
                callback(null)
                return@transaction
            }

            val result = helper.parser.decodeJsonObject(response)
            if (result != null) {
                val amountMap =
                    helper.parser.asMap(helper.parser.asList(result["amount"])?.firstOrNull())
                val amount = helper.parser.asDecimal(amountMap?.get("amount"))
                val tokenAmount = amount?.div(Numeric.decimal.TEN.pow(decimals))
                callback(tokenAmount)
            } else {
                callback(null)
            }
        }
    }

    private fun retrieveWithdrawalRoute(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        subaccountNumber: Int?,
        injectiveAddress: String?,
    ) {
        val isCctp =
            CctpConfig.cctpChainIds?.any { it.isCctpEnabled(state?.input?.transfer) } ?: false
        if (isCctp) {
            retrieveSkipWithdrawalRouteCCTP(
                state,
                decimals,
                gas,
                accountAddress,
                subaccountNumber,
                injectiveAddress,
            )
        } else {
            retrieveSkipWithdrawalRouteNonCCTP(
                state,
                decimals,
                gas,
                accountAddress,
                subaccountNumber,
                injectiveAddress,
            )
        }
    }

    private fun retrieveSkipWithdrawalRouteNonCCTP(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        subaccountNumber: Int?,
        injectiveAddress: String?,
    ) {
        val toChain = state?.input?.transfer?.chain ?: return
        val toAddress = state.input.transfer.address ?: return
        if (toAddress.isBlank()) return
        val toTokenDenom = state.input.transfer.token ?: return
        val toTokenSkipDenom = stateMachine.routerProcessor.getTokenByDenomAndChainId(
            tokenDenom = toTokenDenom,
            chainId = toChain,
        )?.get("skipDenom")
//        Denoms for tokens on their native chains are returned from the skip API in an incompatible
//        format for our frontend SDKs but are required by the skip API for other API calls.
//        So we prefer the skimDenom and default to the regular denom for API calls.
        val toTokenDenomForAPIUse = toTokenSkipDenom ?: toTokenDenom

        val usdcSize = helper.parser.asDecimal(state.input.transfer.size?.usdcSize) ?: return
        val fromAmount = if (usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            return
        }
        if (fromAmount <= 0) return
        val fromChain = helper.environment.dydxChainId ?: return
        val fromToken = helper.environment.tokens["usdc"]?.denom ?: return
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val url = helper.configs.skipV2MsgsDirect()
        val chainAddresses = routeChainAddresses(
            accountAddress = accountAddress,
            injectiveAddress = injectiveAddress,
            externalChain = toChain,
            externalAddress = toAddress,
            includeOsmosisAndNeutron = true,
        ) ?: return

        val body: Map<String, Any> = mapOf(
            "amount_in" to fromAmountString,
            "source_asset_denom" to fromToken,
            "source_asset_chain_id" to fromChain,
            "dest_asset_denom" to toTokenDenomForAPIUse,
            "dest_asset_chain_id" to toChain,
            "chain_ids_to_addresses" to chainAddresses,
            "bridges" to listOf(
                IBC_BRIDGE_ID,
                AXELAR_BRIDGE_ID,
            ),
            "allow_multi_tx" to false,
            "allow_unsafe" to true,
            "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
        )
        val header = iMapOf(
            "Content-Type" to "application/json",
        )
        val oldState = stateMachine.state
        Logger.ddInfo(body.toIMap(), { "retrieveSkipWithdrawalRouteNonCCTP payload sending" })
        helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, headers ->
            if (response != null) {
                Logger.ddInfo(
                    helper.parser.decodeJsonObject(response),
                    { "retrieveSkipWithdrawalRouteNonCCTP payload received" },
                )
                update(stateMachine.routerRoute(response, subaccountNumber ?: 0, null, false), oldState)
            } else {
                Logger.e { "retrieveSkipWithdrawalRouteNonCCTP error, code: $code" }
            }
        }
    }

    private fun retrieveSkipWithdrawalRouteCCTP(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        subaccountNumber: Int?,
        injectiveAddress: String?,
    ) {
        val toChain = state?.input?.transfer?.chain ?: return
        val toToken = state.input.transfer.token ?: return
        val toAddress = state.input.transfer.address ?: return
        if (toAddress.isBlank()) return
        val usdcSize = helper.parser.asDecimal(state.input.transfer.size?.usdcSize) ?: return
        val fromAmount = if (usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            return
        }
        if (fromAmount <= 0) return
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val fromChain = helper.environment.dydxChainId ?: return
        val fromToken = helper.environment.tokens["usdc"]?.denom ?: return
        val url = helper.configs.skipV2MsgsDirect()
        val chainAddresses = routeChainAddresses(
            accountAddress = accountAddress,
            injectiveAddress = injectiveAddress,
            externalChain = toChain,
            externalAddress = toAddress,
            includeOsmosisAndNeutron = false,
        ) ?: return
        val body: Map<String, Any> = mapOf(
            "amount_in" to fromAmountString,
            "source_asset_denom" to fromToken,
            "source_asset_chain_id" to fromChain,
            "dest_asset_denom" to toToken,
            "dest_asset_chain_id" to toChain,
            "chain_ids_to_addresses" to chainAddresses,
            "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
            "smart_relay" to true,
            "allow_unsafe" to true,
            // Mobile only signs the dYdX tx, so Skip has to relay every hop after it
            "allow_multi_tx" to false,
            "bridges" to listOf(
                CCTP_BRIDGE_ID,
                IBC_BRIDGE_ID,
            ),
        )
        val oldState = stateMachine.state
        val header = iMapOf(
            "Content-Type" to "application/json",
        )
        Logger.ddInfo(body.toIMap(), { "retrieveSkipWithdrawalRouteCCTP payload sending" })
        helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, _ ->
            if (response != null) {
                Logger.ddInfo(
                    helper.parser.decodeJsonObject(response),
                    { "retrieveSkipWithdrawalRouteCCTP payload received" },
                )
                val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                val oldFromAmount = oldState?.input?.transfer?.size?.size
                if (currentFromAmount == oldFromAmount) {
                    update(stateMachine.routerRoute(response, subaccountNumber ?: 0, null, false), oldState)
                }
            } else {
                Logger.e { "retrieveSkipWithdrawalRouteCCTP error, code: $code" }
            }
        }
    }

    private fun fetchTransferStatusSkip(
        hash: String,
        fromChainId: String?,
    ) {
        val oldState = stateMachine.state
//        If transfer is not yet tracked, must track first before querying status
        val isTracked = oldState?.trackStatuses?.get(hash) == true
        if (!isTracked) {
            trackTransferSkip(hash = hash, fromChainId = fromChainId)
        } else {
            val params: IMap<String, String> = iMapOf(
                "tx_hash" to hash,
                "chain_id" to fromChainId,
            ).filterNotNull()
            val url = helper.configs.skipV2Status()
            helper.get(url, params) { _, response, httpCode, _ ->
                if (response != null) {
                    update(stateMachine.routerStatus(response, hash), oldState)
                } else {
                    Logger.e { "fetchTransferStatus error, code: $httpCode" }
                }
            }
        }
    }

    private fun trackTransferSkip(
        hash: String,
        fromChainId: String?,
    ) {
        val body: IMap<String, String> = iMapOf(
            "tx_hash" to hash,
            "chain_id" to fromChainId,
        ).filterNotNull()
        val url = helper.configs.skipV2Track()
        val oldState = stateMachine.state
        helper.post(url, null, body.toJsonPrettyPrint()) { _, response, httpCode, _ ->
            if (response != null) {
                update(stateMachine.routerTrack(hash, response), oldState)
                val isTracked = oldState?.trackStatuses?.get(hash) == true
                if (isTracked) {
                    fetchTransferStatusSkip(hash, fromChainId)
                }
            }
        }
    }

    private fun receiveTransferGas(gas: BigDecimal?) {
        val gas = helper.parser.asDouble(gas)
        val oldFee = stateMachine.internalState.input.transfer.fee
        if (oldFee != gas) {
            stateMachine.internalState.input.transfer.fee = gas
            update(StateChanges(iListOf(Changes.input)), stateMachine.state)
        }
    }

    @Throws(Exception::class)
    private fun depositPayload(subaccountNumber: Int?): HumanReadableDepositPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.size ?: throw Exception("size is null")
        return HumanReadableDepositPayload(
            subaccountNumber ?: 0,
            amount,
        )
    }

    @Throws(Exception::class)
    private fun withdrawPayload(subaccountNumber: Int?): HumanReadableWithdrawPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.usdcSize ?: throw Exception("usdcSize is null")
        return HumanReadableWithdrawPayload(
            subaccountNumber ?: 0,
            amount,
        )
    }

    private fun transferNativeTokenPayloadJson(subaccountNumber: Int?): String {
        return Json.encodeToString(transferNativeTokenPayload(subaccountNumber))
    }

    @Throws(Exception::class)
    private fun transferNativeTokenPayload(subaccountNumber: Int?): HumanReadableTransferPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.size ?: throw Exception("size is null")
        val recipient = transfer.address ?: throw Exception("address is null")
        return HumanReadableTransferPayload(
            subaccountNumber ?: 0,
            amount,
            recipient,
        )
    }

    private fun withdrawPayloadJson(subaccountNumber: Int?): String {
        return Json.encodeToString(withdrawPayload(subaccountNumber))
    }

    @Throws(Exception::class)
    private fun subaccountTransferPayload(subaccountNumber: Int?): HumanReadableSubaccountTransferPayload {
        val transfer = stateMachine.state?.input?.transfer ?: error("Transfer is null")
        val size = transfer.size?.size ?: error("size is null")
        val destinationAddress = transfer.address ?: error("destination address is null")
        val accountAddress = helper.parser.asString(
            helper.parser.value(stateMachine.wallet, "walletAddress"),
        ) ?: error("account address is null")

        return HumanReadableSubaccountTransferPayload(
            senderAddress = accountAddress,
            subaccountNumber = subaccountNumber ?: 0,
            amount = size,
            destinationAddress,
            destinationSubaccountNumber = 0,
        )
    }

    internal fun commitTransfer(subaccountNumber: Int?, callback: TransactionCallback) {
        val type = stateMachine.state?.input?.transfer?.type
        when (type) {
            TransferType.deposit -> {
                commitDeposit(subaccountNumber, callback)
            }

            TransferType.withdrawal -> {
                commitWithdrawal(subaccountNumber, callback)
            }

            TransferType.transferOut -> {
                commitTransferOut(subaccountNumber, callback)
            }

            else -> {}
        }
    }

    private fun commitDeposit(subaccountNumber: Int?, callback: TransactionCallback) {
        val payload = depositPayload(subaccountNumber)
        val string = Json.encodeToString(payload)

        helper.transaction(TransactionType.Deposit, string) { response ->
            val error = helper.parseTransactionResponse(response)
            helper.send(error, callback, payload)
        }
    }

    private fun commitWithdrawal(subaccountNumber: Int?, callback: TransactionCallback) {
        val payload = withdrawPayload(subaccountNumber)
        val string = Json.encodeToString(payload)

        helper.transaction(TransactionType.Withdraw, string) { response ->
            val error = helper.parseTransactionResponse(response)
            helper.send(error, callback, payload)
        }
    }

    private fun commitTransferOut(subaccountNumber: Int?, callback: TransactionCallback) {
        val payload = subaccountTransferPayload(subaccountNumber)
        val string = Json.encodeToString(payload)

        helper.transaction(TransactionType.SubaccountTransfer, string) { response ->
            val error = helper.parseTransactionResponse(response)
            helper.send(error, callback, payload)
        }
    }
}
