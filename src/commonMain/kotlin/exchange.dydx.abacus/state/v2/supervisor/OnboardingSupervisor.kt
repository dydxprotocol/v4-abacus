package exchange.dydx.abacus.state.v2.supervisor

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.processor.router.skip.SkipRoutePayloadProcessor
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionCallback
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.state.app.adaptors.V4TransactionErrors
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.manager.CctpChainTokenInfo
import exchange.dydx.abacus.state.manager.CctpConfig
import exchange.dydx.abacus.state.manager.CctpWithdrawState
import exchange.dydx.abacus.state.manager.ExchangeConfig
import exchange.dydx.abacus.state.manager.ExchangeInfo
import exchange.dydx.abacus.state.manager.HumanReadableDepositPayload
import exchange.dydx.abacus.state.manager.HumanReadableFaucetPayload
import exchange.dydx.abacus.state.manager.HumanReadableSubaccountTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableTransferPayload
import exchange.dydx.abacus.state.manager.HumanReadableWithdrawPayload
import exchange.dydx.abacus.state.manager.Platform
import exchange.dydx.abacus.state.manager.RpcConfigs
import exchange.dydx.abacus.state.manager.RpcInfo
import exchange.dydx.abacus.state.manager.StatsigConfig
import exchange.dydx.abacus.state.manager.SystemUtils
import exchange.dydx.abacus.state.manager.pendingCctpWithdraw
import exchange.dydx.abacus.state.model.TradingStateMachine
import exchange.dydx.abacus.state.model.TransferInputField
import exchange.dydx.abacus.state.model.routerChains
import exchange.dydx.abacus.state.model.routerTokens
import exchange.dydx.abacus.state.model.squidRoute
import exchange.dydx.abacus.state.model.squidRouteV2
import exchange.dydx.abacus.state.model.squidStatus
import exchange.dydx.abacus.state.model.squidV2SdkInfo
import exchange.dydx.abacus.state.model.transfer
import exchange.dydx.abacus.utils.AnalyticsUtils
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.SLIPPAGE_PERCENT
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.isAddressValid
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import exchange.dydx.abacus.utils.toNeutronAddress
import exchange.dydx.abacus.utils.toNobleAddress
import exchange.dydx.abacus.utils.toOsmosisAddress
import io.ktor.util.encodeBase64
import kollections.iListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.coroutines.resume

internal class OnboardingSupervisor(
    stateMachine: TradingStateMachine,
    helper: NetworkHelper,
    analyticsUtils: AnalyticsUtils,
    private val configs: OnboardingConfigs,
) : NetworkSupervisor(stateMachine, helper, analyticsUtils) {
    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)

        if (readyToConnect) {
            if (configs.retrieveSquidRoutes) {
                retrieveAssetsFromRouter()
                retrieveDepositExchanges()
            }
        }
    }

    private fun retrieveAssetsFromRouter() {
        if (StatsigConfig.useSkip) {
            CoroutineScope(Dispatchers.Unconfined).launch {
                retrieveSkipTransferChains()
            }
            retrieveSkipTransferTokens()
        } else {
            retrieveTransferAssets()
        }
        retrieveCctpChainIds()
    }



    private suspend fun retrieveSkipTransferChains() = coroutineScope {
        val chainsUrl = helper.configs.skipV1Chains()
        val chainsRequestDeferred = async { helper.getAsync(chainsUrl, null, null).response }

        // web does not need rpc endpoints to be available since web uses wagmi sdk for this
        if (SystemUtils.platform === Platform.android || SystemUtils.platform === Platform.ios) {
            // kick off rpc fetch in parallel with chains fetch for efficiency
            // wait for it to complete before submitting chains fetch response to state machine
            // since processor needs rpc endpoints to be available
            async { retrieveChainRpcEndpoints() }.await()
        }

        chainsRequestDeferred.await()?.let { chainsResponse ->
            update(stateMachine.routerChains(chainsResponse), stateMachine.state)
        }
    }

    private suspend fun retrieveChainRpcEndpoints() = suspendCancellableCoroutine { continuation ->
        val url = "${helper.deploymentUri}/configs/rpc.json"
        helper.get(url) { _, response, _, _ ->
            if (response != null) {
                try {
                    Json.decodeFromString<Map<String, RpcInfo>>(response).let {
                        RpcConfigs.chainIdToRpcMap = it
                    }
                } catch (e: IllegalArgumentException) {
                    Logger.e { "retrieveChainRpcEndpoints IllegalArgumentException error: $e" }
                } catch (e: SerializationException) {
                    Logger.e { "retrieveChainRpcEndpoints SerializationException error: $e" }
                }
            }
            continuation.resume(Unit)
        }
    }

    private fun retrieveSkipTransferTokens() {
        val oldState = stateMachine.state
        val tokensUrl = helper.configs.skipV1Assets()
//            add API key injection for all skip methods
//            val header = iMapOf("authorization" to skipAPIKey)
        helper.get(tokensUrl, null, null) { _, response, httpCode, _ ->
            if (helper.success(httpCode) && response != null) {
                update(stateMachine.routerTokens(response), oldState)
            }
        }
    }

    private fun retrieveTransferAssets() {
        val oldState = stateMachine.state
        val url = helper.configs.squidV2Assets()
        val squidIntegratorId = helper.environment.squidIntegratorId
        if (url != null && squidIntegratorId != null) {
            val header = iMapOf("x-integrator-id" to squidIntegratorId)
            helper.get(url, null, header) { _, response, httpCode, _ ->
                if (helper.success(httpCode) && response != null) {
                    update(stateMachine.squidV2SdkInfo(response), oldState)
                }
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

    private fun retrieveDepositExchanges() {
        val url = "${helper.deploymentUri}/configs/exchanges.json"
        helper.get(url) { _, response, _, _ ->
            if (response != null) {
                val exchanges = mutableListOf<ExchangeInfo>()
                val exchangeInfos: List<JsonElement>? =
                    helper.parser.decodeJsonArray(response)?.toList() as? List<JsonElement>
                for (exchange in exchangeInfos ?: emptyList()) {
                    val exchangeInfo = exchange.jsonObject
                    val name = helper.parser.asString(exchangeInfo["name"])
                    val label = helper.parser.asString(exchangeInfo["label"])
                    val icon = helper.parser.asString(exchangeInfo["icon"])
                    val depositType = helper.parser.asString(exchangeInfo["depositType"])
                    if (name != null && label != null && icon != null && depositType != null) {
                        exchanges.add(ExchangeInfo(name, label, icon, depositType))
                    }
                }
                ExchangeConfig.exchangeList = exchanges
                stateMachine.routerProcessor.exchangeDestinationChainId =
                    helper.configs.nobleChainId()
            }
        }
    }

    private fun retrieveDepositRoute(
        state: PerpetualState?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val isCctp = state?.input?.transfer?.isCctp ?: false
        if (StatsigConfig.useSkip) {
            if (isCctp) {
                retrieveSkipDepositRouteCCTP(
                    state = state,
                    accountAddress = accountAddress,
                    sourceAddress = sourceAddress,
                    subaccountNumber = subaccountNumber,
                )
            } else {
                retrieveSkipDepositRouteNonCCTP(
                    state,
                    accountAddress,
                    sourceAddress,
                    subaccountNumber,
                )
            }
            return
        }
        when (configs.squidVersion) {
            OnboardingConfigs.SquidVersion.V2WithdrawalOnly -> retrieveDepositRouteV1(
                state,
                accountAddress,
                sourceAddress,
                subaccountNumber,
            )

            OnboardingConfigs.SquidVersion.V2, OnboardingConfigs.SquidVersion.V2DepositOnly ->
                if (isCctp) {
                    retrieveDepositRouteV2(
                        state,
                        accountAddress,
                        sourceAddress,
                        subaccountNumber,
                    )
                } else {
                    retrieveDepositRouteV1(
                        state,
                        accountAddress,
                        sourceAddress,
                        subaccountNumber,
                    )
                }
        }
    }

    private fun retrieveSkipDepositRouteNonCCTP(
        state: PerpetualState?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
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
                helper.parser.asInt(stateMachine.routerProcessor.selectedTokenDecimals(tokenAddress = fromTokenDenom, selectedChainId = fromChain))
            if (decimals != null) {
                (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
            } else {
                null
            }
        }
        val osmosisChainId = helper.configs.osmosisChainId()
        val nobleChainId = helper.configs.nobleChainId()
        val neutronChainId = helper.configs.neutronChainId()
        val chainId = helper.environment.dydxChainId ?: return
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom ?: return
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val url = helper.configs.skipV2MsgsDirect()
        if (fromAmount != null && fromAmount > 0) {
            val body: Map<String, Any> = mapOf(
                "amount_in" to fromAmountString,
                "source_asset_denom" to fromTokenDenomForAPIUse,
                "source_asset_chain_id" to fromChain,
                "dest_asset_denom" to nativeChainUSDCDenom,
                "dest_asset_chain_id" to chainId,
                "chain_ids_to_addresses" to mapOf(
                    fromChain to sourceAddress,
                    osmosisChainId to accountAddress.toOsmosisAddress(),
                    nobleChainId to accountAddress.toNobleAddress(),
                    neutronChainId to accountAddress.toNeutronAddress(),
                    chainId to accountAddress,
                ),
                "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
            )

            val oldState = stateMachine.state
            val header = iMapOf(
                "Content-Type" to "application/json",
            )
            helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, headers ->
                if (response != null) {
                    val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                    val oldFromAmount = oldState?.input?.transfer?.size?.size
                    if (currentFromAmount == oldFromAmount) {
                        update(stateMachine.squidRoute(response, subaccountNumber ?: 0, null), oldState)
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
    ) {
//      We have a lot of duplicate code for these deposit/withdrawal route calls
//      It's easier to dedupe now that the url is the same and only the args differ
//      We can consider deduping this at a later point.
        val fromChain = state?.input?.transfer?.chain ?: return
        val fromToken = state.input.transfer.token ?: return
        val fromAmount = helper.parser.asDecimal(state.input.transfer.size?.size)?.let {
            val decimals = helper.parser.asInt(stateMachine.routerProcessor.selectedTokenDecimals(tokenAddress = fromToken, selectedChainId = fromChain))
            if (decimals != null) {
                (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
            } else {
                null
            }
        } ?: return
        if (fromAmount <= 0) return
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val url = helper.configs.skipV2MsgsDirect()
        val toChain = helper.environment.dydxChainId ?: return
        val toToken = helper.environment.tokens["usdc"]?.denom ?: return
        val body: Map<String, Any> = mapOf(
            "amount_in" to fromAmountString,
            "source_asset_denom" to fromToken,
            "source_asset_chain_id" to fromChain,
            "dest_asset_denom" to toToken,
            "dest_asset_chain_id" to toChain,
            "chain_ids_to_addresses" to mapOf(
                fromChain to sourceAddress,
                toChain to accountAddress,
            ),
            "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
            "bridges" to listOf(
                "CCTP",
                "IBC",
            ),
        )
        val oldState = stateMachine.state
        val header = iMapOf(
            "Content-Type" to "application/json",
        )
        helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, headers ->
            if (response != null) {
                val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                val oldFromAmount = oldState?.input?.transfer?.size?.size
                if (currentFromAmount == oldFromAmount) {
                    update(stateMachine.squidRoute(response, subaccountNumber ?: 0, null), oldState)
                }
            } else {
                Logger.e { "retrieveSkipDepositRouteCCTP error, code: $code" }
            }
        }
    }

    private fun retrieveDepositRouteV1(
        state: PerpetualState?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val fromChain = state?.input?.transfer?.chain
        val fromToken = state?.input?.transfer?.token
        val fromAmount = helper.parser.asDecimal(state?.input?.transfer?.size?.size)?.let {
            val decimals =
                helper.parser.asInt(stateMachine.routerProcessor.selectedTokenDecimals(tokenAddress = fromToken, selectedChainId = fromChain))
            if (decimals != null) {
                (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
            } else {
                null
            }
        }
        val chainId = helper.environment.dydxChainId
        val squidIntegratorId = helper.environment.squidIntegratorId
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom
        val fromAmountString = helper.parser.asString(fromAmount)
        val url = helper.configs.squidRoute()
        if (fromChain != null &&
            fromToken != null &&
            fromAmount != null && fromAmount > 0 &&
            fromAmountString != null &&
            chainId != null &&
            nativeChainUSDCDenom != null &&
            url != null &&
            squidIntegratorId != null
        ) {
            val params: IMap<String, String> = iMapOf(
                "fromChain" to fromChain,
                "fromToken" to fromToken,
                "fromAmount" to fromAmountString,
                "toChain" to chainId,
                "toToken" to nativeChainUSDCDenom,
                "toAddress" to accountAddress,
                "slippage" to "1",
                "enableForecall" to "false",
                "fromAddress" to sourceAddress,
            )

            val oldState = stateMachine.state
            val header = iMapOf(
                "x-integrator-id" to squidIntegratorId,
            )
            helper.get(url, params, header) { _, response, code, headers ->
                if (response != null) {
                    val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                    val oldFromAmount = oldState?.input?.transfer?.size?.size
                    val requestId = helper.parser.asString(headers?.get("x-request-id"))
                    if (currentFromAmount == oldFromAmount) {
                        update(stateMachine.squidRoute(response, subaccountNumber ?: 0, requestId), oldState)
                    }
                } else {
                    Logger.e { "retrieveDepositRouteV1 error, code: $code" }
                }
            }
        }
    }

    private fun retrieveDepositRouteV2(
        state: PerpetualState?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val fromChain = state?.input?.transfer?.chain
        val fromToken = state?.input?.transfer?.token
        val fromAmount = helper.parser.asDecimal(state?.input?.transfer?.size?.size)?.let {
            val decimals =
                helper.parser.asInt(stateMachine.routerProcessor.selectedTokenDecimals(tokenAddress = fromToken, selectedChainId = fromChain))
            if (decimals != null) {
                (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
            } else {
                null
            }
        }
        val chainId = helper.environment.dydxChainId
        val squidIntegratorId = helper.environment.squidIntegratorId
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom
        val fromAmountString = helper.parser.asString(fromAmount)
        val nobleAddress = accountAddress.toNobleAddress()
        val url = helper.configs.squidV2Route()
        val toChain = helper.configs.nobleChainId()
        val toToken = helper.configs.nobleDenom
        if (fromChain != null &&
            fromToken != null &&
            fromAmount != null && fromAmount > 0 &&
            fromAmountString != null &&
            nobleAddress != null &&
            chainId != null &&
            nativeChainUSDCDenom != null &&
            url != null &&
            squidIntegratorId != null &&
            toChain != null &&
            toToken != null
        ) {
            val body: Map<String, Any> = mapOf(
                "fromChain" to fromChain,
                "fromToken" to fromToken,
                "fromAddress" to sourceAddress,
                "fromAmount" to fromAmountString,
                "toChain" to toChain,
                "toToken" to toToken,
                "toAddress" to nobleAddress,
                "quoteOnly" to false,
                "enableBoost" to false,
                "slippage" to 1,
                "slippageConfig" to iMapOf<String, Any>(
                    "autoMode" to 1,
                ),
            )
            val oldState = stateMachine.state
            val header = iMapOf(
                "x-integrator-id" to squidIntegratorId,
                "Content-Type" to "application/json",
            )
            helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, headers ->
                if (response != null) {
                    val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                    val oldFromAmount = oldState?.input?.transfer?.size?.size
                    val requestId = helper.parser.asString(headers?.get("x-request-id"))
                    if (currentFromAmount == oldFromAmount) {
                        update(stateMachine.squidRouteV2(response, subaccountNumber ?: 0, requestId), oldState)
                    }
                } else {
                    Logger.e { "retrieveDepositRouteV2 error, code: $code" }
                }
            }
        }
    }

    internal fun transfer(
        data: String?,
        type: TransferInputField?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        helper.ioImplementations.threading?.async(ThreadingType.abacus) {
            val stateResponse = stateMachine.transfer(data, type, subaccountNumber ?: 0)
            didUpdateStateForTransfer(data, type, accountAddress, sourceAddress, subaccountNumber)
            helper.ioImplementations.threading?.async(ThreadingType.main) {
                helper.stateNotification?.stateChanged(
                    stateResponse.state,
                    stateResponse.changes,
                )
            }
        }
    }

    private fun didUpdateStateForTransfer(
        data: String?,
        type: TransferInputField?,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?
    ) {
        val state = stateMachine.state
        if (state?.input?.transfer?.type == TransferType.deposit) {
            if (type == TransferInputField.size) {
                retrieveDepositRoute(state, accountAddress, sourceAddress, subaccountNumber)
            }
        } else if (state?.input?.transfer?.type == TransferType.withdrawal) {
            if (type == TransferInputField.usdcSize ||
                type == TransferInputField.address ||
                type == TransferInputField.chain ||
                type == TransferInputField.exchange ||
                type == TransferInputField.token
            ) {
                val decimals = helper.environment.tokens["usdc"]?.decimals ?: 6

                val usdcSize =
                    helper.parser.asDouble(state.input.transfer.size?.usdcSize)
                        ?: Numeric.double.ZERO
                if (usdcSize > Numeric.double.ZERO) {
                    simulateWithdrawal(decimals, subaccountNumber) { gasFee ->
                        if (gasFee != null) {
                            retrieveWithdrawalRoute(
                                state,
                                decimals,
                                gasFee,
                                accountAddress,
                                sourceAddress,
                                subaccountNumber,
                            )
                        } else {
                            retrieveWithdrawalRoute(
                                state,
                                decimals,
                                Numeric.decimal.ZERO,
                                accountAddress,
                                sourceAddress,
                                subaccountNumber,
                            )
                        }
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
        fetchTransferStatus(hash, fromChainId, toChainId, isCctp)
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
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val isCctp =
            CctpConfig.cctpChainIds?.any { it.isCctpEnabled(state?.input?.transfer) } ?: false
        val isExchange = state?.input?.transfer?.exchange != null
        if (StatsigConfig.useSkip) {
            if (isCctp) {
                retrieveSkipWithdrawalRouteCCTP(
                    state,
                    decimals,
                    gas,
                    accountAddress,
                    sourceAddress,
                    subaccountNumber,
                )
            } else if (isExchange) {
                retrieveSkipWithdrawalRouteExchange(
                    state,
                    decimals,
                    gas,
                    accountAddress,
                    sourceAddress,
                    subaccountNumber,
                )
            } else {
                retrieveSkipWithdrawalRouteNonCCTP(
                    state,
                    decimals,
                    gas,
                    accountAddress,
                    sourceAddress,
                    subaccountNumber,
                )
            }
            return
        }
        when (configs.squidVersion) {
            OnboardingConfigs.SquidVersion.V2DepositOnly -> retrieveWithdrawalRouteV1(
                state,
                decimals,
                gas,
                accountAddress,
                sourceAddress,
                subaccountNumber,
            )

            OnboardingConfigs.SquidVersion.V2, OnboardingConfigs.SquidVersion.V2WithdrawalOnly ->
                if (isCctp) {
                    retrieveWithdrawalRouteV2(
                        state,
                        decimals,
                        gas,
                        accountAddress,
                        sourceAddress,
                        subaccountNumber,
                    )
                } else if (isExchange) {
                    retrieveWithdrawalRouteNoble(
                        state,
                        decimals,
                        gas,
                        accountAddress,
                        sourceAddress,
                        subaccountNumber,
                    )
                } else {
                    retrieveWithdrawalRouteV1(
                        state,
                        decimals,
                        gas,
                        accountAddress,
                        sourceAddress,
                        subaccountNumber,
                    )
                }
        }
    }

    private fun retrieveWithdrawalRouteNoble(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val nobleChain = helper.configs.nobleChainId()
        val nobleToken = helper.configs.nobleDenom
        val toAddress = state?.input?.transfer?.address
        val usdcSize = helper.parser.asDecimal(state?.input?.transfer?.size?.usdcSize)
        val fromAmount = if (usdcSize != null && usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            null
        }
        val chainId = helper.environment.dydxChainId
        val squidIntegratorId = helper.environment.squidIntegratorId
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom
        val fromAmountString = helper.parser.asString(fromAmount)
        val url = helper.configs.squidRoute()
        val fromAddress = accountAddress
        if (nobleChain != null &&
            nobleToken != null &&
            toAddress != null &&
            fromAmount != null &&
            fromAmount > 0 &&
            fromAmountString != null &&
            chainId != null &&
            nativeChainUSDCDenom != null &&
            url != null &&
            squidIntegratorId != null
        ) {
            val params: IMap<String, String> = iMapOf(
                "fromChain" to chainId,
                "fromToken" to nativeChainUSDCDenom,
                "fromAmount" to fromAmountString,
                "fromAddress" to fromAddress,
                "toChain" to nobleChain,
                "toToken" to nobleToken,
                "toAddress" to toAddress,
                "slippage" to "1",
                "enableForecall" to "false",
            )

            val oldState = stateMachine.state
            val header = iMapOf(
                "x-integrator-id" to squidIntegratorId,
            )
            helper.get(url, params, header) { _, response, _, headers ->
                if (response != null) {
                    val requestId = helper.parser.asString(headers?.get("x-request-id"))
                    update(stateMachine.squidRoute(response, subaccountNumber ?: 0, requestId), oldState)
                }
            }
        }
    }

    private fun retrieveWithdrawalRouteV1(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val toChain = state?.input?.transfer?.chain
        val toToken = state?.input?.transfer?.token
        val toAddress = state?.input?.transfer?.address
        val usdcSize = helper.parser.asDecimal(state?.input?.transfer?.size?.usdcSize)
        val fromAmount = if (usdcSize != null && usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            null
        }
        val chainId = helper.environment.dydxChainId
        val squidIntegratorId = helper.environment.squidIntegratorId
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom
        val fromAmountString = helper.parser.asString(fromAmount)
        val url = helper.configs.squidRoute()
        val fromAddress = accountAddress
        if (toChain != null &&
            toToken != null &&
            toAddress != null &&
            fromAmount != null &&
            fromAmount > 0 &&
            fromAmountString != null &&
            chainId != null &&
            nativeChainUSDCDenom != null &&
            url != null &&
            squidIntegratorId != null
        ) {
            val params: IMap<String, String> = iMapOf(
                "fromChain" to chainId,
                "fromToken" to nativeChainUSDCDenom,
                "fromAmount" to fromAmountString,
                "fromAddress" to fromAddress,
                "toChain" to toChain,
                "toToken" to toToken,
                "toAddress" to toAddress,
                "slippage" to "1",
                "enableForecall" to "false",
                "cosmosSignerAddress" to accountAddress.toString(),
            )

            val oldState = stateMachine.state
            val header = iMapOf(
                "x-integrator-id" to squidIntegratorId,
            )
            helper.get(url, params, header) { _, response, _, headers ->
                if (response != null) {
                    val requestId = helper.parser.asString(headers?.get("x-request-id"))
                    update(stateMachine.squidRoute(response, subaccountNumber ?: 0, requestId), oldState)
                }
            }
        }
    }

    private fun retrieveWithdrawalRouteV2(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val toChain = state?.input?.transfer?.chain
        val toToken = state?.input?.transfer?.token
        val toAddress = state?.input?.transfer?.address
        val usdcSize = helper.parser.asDecimal(state?.input?.transfer?.size?.usdcSize)
        val fromAmount = if (usdcSize != null && usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            null
        }
        val chainId = helper.environment.dydxChainId
        val squidIntegratorId = helper.environment.squidIntegratorId
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom
        val fromAmountString = helper.parser.asString(fromAmount)
        val url = helper.configs.squidV2Route()
        val fromAddress = accountAddress.toNobleAddress()
        val fromChain = helper.configs.nobleChainId()
        val fromToken = helper.configs.nobleDenom
        if (toChain != null &&
            toToken != null &&
            toAddress != null &&
            fromAmount != null &&
            fromAmount > 0 &&
            fromAmountString != null &&
            chainId != null &&
            nativeChainUSDCDenom != null &&
            url != null &&
            fromAddress != null &&
            squidIntegratorId != null &&
            fromChain != null &&
            fromToken != null
        ) {
            val body: IMap<String, Any> = iMapOf(
                "fromChain" to fromChain,
                "fromToken" to fromToken,
                "fromAmount" to fromAmountString,
                "fromAddress" to fromAddress,
                "toChain" to toChain,
                "toToken" to toToken,
                "toAddress" to toAddress,
                "quoteOnly" to false,
                "enableBoost" to false,
                "slippage" to 1,
                "slippageConfig" to iMapOf<String, Any>(
                    "autoMode" to 1,
                ),
                // "enableForecall" to "false",
                // "cosmosSignerAddress" to accountAddress.toString(),
            )
            val oldState = stateMachine.state
            val header = iMapOf(
                "x-integrator-id" to squidIntegratorId,
                "Content-Type" to "application/json",
            )
            helper.post(url, header, body.toJsonPrettyPrint()) { url, response, code, headers ->
                if (response != null) {
                    val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                    val oldFromAmount = oldState?.input?.transfer?.size?.size
                    val requestId = helper.parser.asString(headers?.get("x-request-id"))
                    if (currentFromAmount == oldFromAmount) {
                        update(stateMachine.squidRouteV2(response, subaccountNumber ?: 0, requestId), oldState)
                    }
                } else {
                    Logger.e { "retrieveWithdrawalRouteV2 error, code: $code" }
                }
            }
        }
    }

    private fun retrieveSkipWithdrawalRouteExchange(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val toChain = helper.configs.nobleChainId() ?: return
        val toToken = helper.configs.nobleDenom ?: return
        val toAddress = state?.input?.transfer?.address ?: return
        val usdcSize = helper.parser.asDecimal(state?.input?.transfer?.size?.usdcSize) ?: return
        val fromAmount = if (usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            return
        }
        if (fromAmount <= 0) return
        val fromChain = helper.environment.dydxChainId ?: return
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom ?: return
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val url = helper.configs.skipV2MsgsDirect()
        val fromAddress = accountAddress
        val body: Map<String, Any> = mapOf(
            "amount_in" to fromAmountString,
            "source_asset_denom" to nativeChainUSDCDenom,
            "source_asset_chain_id" to fromChain,
            "dest_asset_denom" to toToken,
            "dest_asset_chain_id" to toChain,
            "chain_ids_to_addresses" to mapOf(
                fromChain to fromAddress,
                toChain to toAddress,
            ),
            "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
        )

        val oldState = stateMachine.state
        val header = iMapOf(
            "Content-Type" to "application/json",
        )
        helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, headers ->
            if (response != null) {
                update(stateMachine.squidRoute(response, subaccountNumber ?: 0, null), oldState)
            } else {
                Logger.e { "retrieveSkipWithdrawalRouteExchange error, code: $code" }
            }
        }
    }

    private fun retrieveSkipWithdrawalRouteNonCCTP(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val toChain = state?.input?.transfer?.chain ?: return
        val toAddress = state?.input?.transfer?.address
        val toTokenDenom = state.input.transfer.token ?: return
        val toTokenSkipDenom = stateMachine.routerProcessor.getTokenByDenomAndChainId(
            tokenDenom = toTokenDenom,
            chainId = toChain,
        )?.get("skipDenom")
//        Denoms for tokens on their native chains are returned from the skip API in an incompatible
//        format for our frontend SDKs but are required by the skip API for other API calls.
//        So we prefer the skimDenom and default to the regular denom for API calls.
        val toTokenDenomForAPIUse = toTokenSkipDenom ?: toTokenDenom

        val usdcSize = helper.parser.asDecimal(state?.input?.transfer?.size?.usdcSize) ?: return
        val fromAmount = if (usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            return
        }
        if (fromAmount <= 0) return
        val osmosisChainId = helper.configs.osmosisChainId()
        val nobleChainId = helper.configs.nobleChainId()
        val neutronChainId = helper.configs.neutronChainId()
        val fromChain = helper.environment.dydxChainId ?: return
        val fromToken = helper.environment.tokens["usdc"]?.denom ?: return
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val url = helper.configs.skipV2MsgsDirect()
        val body: Map<String, Any> = mapOf(
            "amount_in" to fromAmountString,
            "source_asset_denom" to fromToken,
            "source_asset_chain_id" to fromChain,
            "dest_asset_denom" to toTokenDenomForAPIUse,
            "dest_asset_chain_id" to toChain,
            "chain_ids_to_addresses" to mapOf(
                fromChain to accountAddress,
                osmosisChainId to accountAddress.toOsmosisAddress(),
                nobleChainId to accountAddress.toNobleAddress(),
                neutronChainId to accountAddress.toNeutronAddress(),
                toChain to toAddress,
            ),
            "allow_multi_tx" to true,
            "allow_unsafe" to true,
            "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
        )
        val header = iMapOf(
            "Content-Type" to "application/json",
        )
        val oldState = stateMachine.state
        helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, headers ->
            if (response != null) {
                update(stateMachine.squidRoute(response, subaccountNumber ?: 0, null), oldState)
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
        sourceAddress: String,
        subaccountNumber: Int?,
    ) {
        val toChain = state?.input?.transfer?.chain ?: return
        val toToken = state.input.transfer.token ?: return
        val toAddress = state.input.transfer.address ?: return
        val usdcSize = helper.parser.asDecimal(state.input.transfer.size?.usdcSize) ?: return
        val fromAmount = if (usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            return
        }
        if (fromAmount <= 0) return
        val fromAmountString = helper.parser.asString(fromAmount) ?: return
        val url = helper.configs.skipV2MsgsDirect()
        val fromAddress = accountAddress.toNobleAddress() ?: return

        val fromChain = helper.configs.nobleChainId()
        val fromToken = helper.configs.nobleDenom
        val body: Map<String, Any> = mapOf(
            "amount_in" to fromAmountString,
            "source_asset_denom" to fromToken,
            "source_asset_chain_id" to fromChain,
            "dest_asset_denom" to toToken,
            "dest_asset_chain_id" to toChain,
            "chain_ids_to_addresses" to mapOf(
                fromChain to fromAddress,
                toChain to toAddress,
            ),
            "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
            "smart_relay" to true,
            "allow_unsafe" to true,
        )
        val oldState = stateMachine.state
        val header = iMapOf(
            "Content-Type" to "application/json",
        )
        helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, _ ->
            if (response != null) {
                val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                val oldFromAmount = oldState?.input?.transfer?.size?.size
                if (currentFromAmount == oldFromAmount) {
                    update(stateMachine.squidRoute(response, subaccountNumber ?: 0, null), oldState)
                }
            } else {
                Logger.e { "retrieveSkipWithdrawalRouteCCTP error, code: $code" }
            }
        }
    }

    private fun fetchTransferStatus(
        hash: String,
        fromChainId: String?,
        toChainId: String?,
        isCctp: Boolean,
    ) {
        val params: IMap<String, String> = iMapOf(
            "transactionId" to hash,
            "fromChainId" to fromChainId,
            "toChainId" to toChainId,
            "bridgeType" to if (isCctp) "cctp" else null,
        ).filterNotNull()
        val url = if (isCctp) helper.configs.squidV2Status() else helper.configs.squidStatus()
        val squidIntegratorId = helper.environment.squidIntegratorId
        if (url != null && squidIntegratorId != null) {
            val oldState = stateMachine.state
            val header = iMapOf(
                "x-integrator-id" to squidIntegratorId,
            )
            helper.get(url, params, header) { _, response, httpCode, _ ->
                if (response != null) {
                    update(stateMachine.squidStatus(response, hash), oldState)
                } else {
                    Logger.e { "fetchTransferStatus error, code: $httpCode" }
                }
            }
        }
    }

    private fun receiveTransferGas(gas: BigDecimal?) {
        val input = stateMachine.input
        val oldFee = helper.parser.asDecimal(helper.parser.value(input, "transfer.fee"))
        if (oldFee != gas) {
            val oldState = stateMachine.state
            val modified = input?.mutable() ?: iMapOf<String, Any>().mutable()
            modified.safeSet("transfer.fee", gas)
            update(StateChanges(iListOf(Changes.input)), oldState)
        }
    }

    private fun transferNobleBalance(accountAddress: String, amount: BigDecimal) {
        val url = helper.configs.squidRoute()
        val fromChain = helper.configs.nobleChainId()
        val fromToken = helper.configs.nobleDenom
        val nobleAddress = accountAddress.toNobleAddress()
        val chainId = helper.environment.dydxChainId
        val squidIntegratorId = helper.environment.squidIntegratorId
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom
        if (url != null &&
            fromChain != null &&
            fromToken != null &&
            nobleAddress != null &&
            chainId != null &&
            nativeChainUSDCDenom != null &&
            squidIntegratorId != null
        ) {
            val params: Map<String, String> = mapOf(
                "fromChain" to fromChain,
                "fromToken" to fromToken,
                "fromAddress" to nobleAddress,
                "fromAmount" to amount.toPlainString(),
                "toChain" to chainId,
                "toToken" to nativeChainUSDCDenom,
                "toAddress" to accountAddress.toString(),
                "slippage" to "1",
                "enableForecall" to "false",
            )
            val header = iMapOf(
                "x-integrator-id" to squidIntegratorId,
            )
            helper.get(url, params, header) { _, response, code, _ ->
                if (response != null) {
                    val json = helper.parser.decodeJsonObject(response)
                    val ibcPayload =
                        helper.parser.asString(
                            helper.parser.value(
                                json,
                                "route.transactionRequest.data",
                            ),
                        )
                    if (ibcPayload != null) {
                        helper.transaction(TransactionType.SendNobleIBC, ibcPayload) {
                            val error = helper.parseTransactionResponse(it)
                            if (error != null) {
                                Logger.e { "transferNobleBalance error: $error" }
                            }
                        }
                    }
                } else {
                    Logger.e { "transferNobleBalance error, code: $code" }
                }
            }
        }
    }

    @Throws(Exception::class)
    fun depositPayload(subaccountNumber: Int?): HumanReadableDepositPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.size ?: throw Exception("size is null")
        return HumanReadableDepositPayload(
            subaccountNumber ?: 0,
            amount,
        )
    }

    fun depositPayloadJson(subaccountNumber: Int?): String {
        return Json.encodeToString(depositPayload(subaccountNumber))
    }

    @Throws(Exception::class)
    fun withdrawPayload(subaccountNumber: Int?): HumanReadableWithdrawPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.usdcSize ?: throw Exception("usdcSize is null")
        return HumanReadableWithdrawPayload(
            subaccountNumber ?: 0,
            amount,
        )
    }

    fun transferNativeTokenPayloadJson(subaccountNumber: Int?): String {
        return Json.encodeToString(transferNativeTokenPayload(subaccountNumber))
    }

    @Throws(Exception::class)
    fun transferNativeTokenPayload(subaccountNumber: Int?): HumanReadableTransferPayload {
        val transfer = stateMachine.state?.input?.transfer ?: throw Exception("Transfer is null")
        val amount = transfer.size?.size ?: throw Exception("size is null")
        val recipient = transfer.address ?: throw Exception("address is null")
        return HumanReadableTransferPayload(
            subaccountNumber ?: 0,
            amount,
            recipient,
        )
    }

    fun withdrawPayloadJson(subaccountNumber: Int?): String {
        return Json.encodeToString(withdrawPayload(subaccountNumber))
    }

    @Throws(Exception::class)
    fun subaccountTransferPayload(subaccountNumber: Int?): HumanReadableSubaccountTransferPayload {
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

    fun faucetPayload(subaccountNumber: Int, amount: Double): HumanReadableFaucetPayload {
        return HumanReadableFaucetPayload(subaccountNumber, amount)
    }

    fun subaccountTransferPayloadJson(subaccountNumber: Int?): String {
        return Json.encodeToString(subaccountTransferPayload(subaccountNumber))
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
            val error = parseTransactionResponse(response)
            helper.send(error, callback, payload)
        }
    }

    private fun commitWithdrawal(subaccountNumber: Int?, callback: TransactionCallback) {
        val payload = withdrawPayload(subaccountNumber)
        val string = Json.encodeToString(payload)

        helper.transaction(TransactionType.Withdraw, string) { response ->
            val error = parseTransactionResponse(response)
            helper.send(error, callback, payload)
        }
    }

    private fun commitTransferOut(subaccountNumber: Int?, callback: TransactionCallback) {
        val payload = subaccountTransferPayload(subaccountNumber)
        val string = Json.encodeToString(payload)

        helper.transaction(TransactionType.SubaccountTransfer, string) { response ->
            val error = parseTransactionResponse(response)
            helper.send(error, callback, payload)
        }
    }

    internal fun commitCCTPWithdraw(
        accountAddress: String,
        subaccountNumber: Int?,
        callback: TransactionCallback
    ) {
        val state = stateMachine.state
        if (state?.input?.transfer?.type == TransferType.withdrawal) {
            val decimals = helper.environment.tokens["usdc"]?.decimals ?: 6

            val usdcSize =
                helper.parser.asDouble(state.input.transfer.size?.usdcSize) ?: Numeric.double.ZERO
            if (usdcSize > Numeric.double.ZERO) {
                simulateWithdrawal(decimals, subaccountNumber) { gasFee ->
                    if (gasFee != null) {
//                        return back and branch from top level method
                        cctpToNoble(
                            state,
                            decimals,
                            gasFee,
                            accountAddress,
                            subaccountNumber,
                            callback,
                        )
                    } else {
//                        return back and branch from top level method
                        cctpToNoble(
                            state,
                            decimals,
                            Numeric.decimal.ZERO,
                            accountAddress,
                            subaccountNumber,
                            callback,
                        )
                    }
                }
            } else {
                helper.send(V4TransactionErrors.error(null, "Invalid usdcSize"), callback)
            }
        } else {
            helper.send(V4TransactionErrors.error(null, "Invalid transfer type"), callback)
        }
    }

    /**
     * Computes whether to serve skip or squid cctpToNoble method
     * The cctpToNoble methods move cctp funds from dydx subaccount wallet to the noble wallet
     */
    private fun cctpToNoble(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        subaccountNumber: Int?,
        callback: TransactionCallback
    ) {
        if (StatsigConfig.useSkip) {
            cctpToNobleSkip(
                state,
                decimals,
                gas,
                accountAddress,
                subaccountNumber,
                callback,
            )
        } else {
            cctpToNobleSquid(
                state,
                decimals,
                gas,
                accountAddress,
                subaccountNumber,
                callback,
            )
        }
    }

    private fun cctpToNobleSquid(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        subaccountNumber: Int?,
        callback: TransactionCallback
    ) {
        val url = helper.configs.squidRoute()
        val nobleChain = helper.configs.nobleChainId()
        val nobleToken = helper.configs.nobleDenom
        val nobleAddress = accountAddress.toNobleAddress()
        val chainId = helper.environment.dydxChainId
        val squidIntegratorId = helper.environment.squidIntegratorId
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom
        val usdcSize = helper.parser.asDecimal(state?.input?.transfer?.size?.usdcSize)
        val fromAmount = if (usdcSize != null && usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            null
        }
        val fromAmountString = helper.parser.asString(fromAmount)

        if (url != null &&
            nobleChain != null &&
            nobleToken != null &&
            nobleAddress != null &&
            chainId != null &&
            nativeChainUSDCDenom != null &&
            squidIntegratorId != null &&
            fromAmountString != null && fromAmount != null && fromAmount > 0
        ) {
            val params: Map<String, String> = mapOf(
                "toChain" to nobleChain,
                "toToken" to nobleToken,
                "toAddress" to nobleAddress,
                "fromAmount" to fromAmountString,
                "fromChain" to chainId,
                "fromToken" to nativeChainUSDCDenom,
                "fromAddress" to accountAddress,
                "slippage" to "1",
                "enableForecall" to "false",
            )
            val header = iMapOf(
                "x-integrator-id" to squidIntegratorId,
            )
            helper.get(url, params, header) { _, response, code, _ ->
                if (response != null) {
                    val json = helper.parser.decodeJsonObject(response)
                    val ibcPayload =
                        helper.parser.asString(
                            helper.parser.value(
                                json,
                                "route.transactionRequest.data",
                            ),
                        )
                    if (ibcPayload != null) {
                        val payload = helper.jsonEncoder.encode(
                            mapOf(
                                "subaccountNumber" to (subaccountNumber ?: 0),
                                "amount" to state?.input?.transfer?.size?.usdcSize,
                                "ibcPayload" to ibcPayload.encodeBase64(),
                            ),
                        )
                        helper.transaction(TransactionType.WithdrawToNobleIBC, payload) {
                            val error = parseTransactionResponse(it)
                            if (error != null) {
                                Logger.e { "withdrawToNobleIBC error: $error" }
                                helper.send(error, callback)
                            } else {
                                pendingCctpWithdraw = CctpWithdrawState(
//                                    we use skip state with squid route
                                    state?.input?.transfer?.requestPayload?.data,
                                    callback,
                                )
                            }
                        }
                    } else {
                        Logger.e { "cctpToNoble error, code: $code" }
                        val error = ParsingError(
                            ParsingErrorType.MissingContent,
                            "Missing squid response",
                        )
                        helper.send(error, callback)
                    }
                } else {
                    Logger.e { "cctpToNoble error, code: $code" }
                    val error = ParsingError(
                        ParsingErrorType.MissingContent,
                        "Missing squid response",
                    )
                    helper.send(error, callback)
                }
            }
        } else {
            val error = ParsingError(
                ParsingErrorType.MissingRequiredData,
                "Missing required data for cctp withdraw",
            )
            helper.send(error, callback)
        }
    }

    @Suppress("ForbiddenComment")
    private fun cctpToNobleSkip(
        state: PerpetualState?,
        decimals: Int,
        gas: BigDecimal,
        accountAddress: String,
        subaccountNumber: Int?,
        callback: TransactionCallback
    ) {
//      We have a lot of duplicate code for these deposit/withdrawal route calls
//      It's easier to dedupe now that the url is the same and only the args differ
//      Consider creating generateArgs fun to reduce code duplication
//      DO-LATER: https://linear.app/dydx/issue/OTE-350/%5Babacus%5D-cleanup
        val url = helper.configs.skipV2MsgsDirect()
        val nobleChain = helper.configs.nobleChainId()
        val nobleToken = helper.configs.nobleDenom
        val nobleAddress = accountAddress.toNobleAddress()
        val chainId = helper.environment.dydxChainId
        val nativeChainUSDCDenom = helper.environment.tokens["usdc"]?.denom
        val usdcSize = helper.parser.asDecimal(state?.input?.transfer?.size?.usdcSize)
        val fromAmount = if (usdcSize != null && usdcSize > gas) {
            ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else {
            null
        }
        val fromAmountString = helper.parser.asString(fromAmount)

        if (
            nobleAddress != null &&
            chainId != null &&
            nativeChainUSDCDenom != null &&
            fromAmountString != null && fromAmount != null && fromAmount > 0
        ) {
            val body: Map<String, Any> = mapOf(
                "amount_in" to fromAmountString,
//                from dydx denom and chain
                "source_asset_denom" to nativeChainUSDCDenom,
                "source_asset_chain_id" to chainId,
//                to noble denom and chain
                "dest_asset_denom" to nobleToken,
                "dest_asset_chain_id" to nobleChain,
                "chain_ids_to_addresses" to mapOf(
                    chainId to accountAddress,
                    nobleChain to nobleAddress,
                ),
                "slippage_tolerance_percent" to SLIPPAGE_PERCENT,
            )

            val header = iMapOf(
                "Content-Type" to "application/json",
            )
            helper.post(url, header, body.toJsonPrettyPrint()) { _, response, code, _ ->
                val json = helper.parser.decodeJsonObject(response)
                if (json != null) {
                    val skipRoutePayloadProcessor = SkipRoutePayloadProcessor(parser = helper.parser)
                    val processedPayload = skipRoutePayloadProcessor.received(existing = mapOf(), payload = json)
                    val ibcPayload = helper.parser.asString(
                        processedPayload.get("data"),
                    )
                    if (ibcPayload != null) {
                        val payload = helper.jsonEncoder.encode(
                            mapOf(
                                "subaccountNumber" to (subaccountNumber ?: 0),
                                "amount" to state?.input?.transfer?.size?.usdcSize,
                                "ibcPayload" to ibcPayload.encodeBase64(),
                            ),
                        )
                        helper.transaction(TransactionType.WithdrawToNobleIBC, payload) {
                            val error = parseTransactionResponse(it)
                            if (error != null) {
                                Logger.e { "withdrawToNobleIBC error: $error" }
                                helper.send(error, callback)
                            } else {
                                pendingCctpWithdraw = CctpWithdrawState(
                                    state?.input?.transfer?.requestPayload?.data,
                                    callback,
                                )
                            }
                        }
                    } else {
                        Logger.e { "cctpToNobleSkip error, code: $code" }
                        val error = ParsingError(
                            ParsingErrorType.MissingContent,
                            "Missing skip response",
                        )
                        helper.send(error, callback)
                    }
                } else {
                    Logger.e { "cctpToNobleSkip error, code: $code" }
                    val error = ParsingError(
                        ParsingErrorType.MissingContent,
                        "Missing skip response",
                    )
                    helper.send(error, callback)
                }
            }
        } else {
            val error = ParsingError(
                ParsingErrorType.MissingRequiredData,
                "Missing required data for cctp skip withdraw",
            )
            helper.send(error, callback)
        }
    }
}
