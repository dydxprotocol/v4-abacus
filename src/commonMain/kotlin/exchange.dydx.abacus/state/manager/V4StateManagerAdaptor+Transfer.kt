package exchange.dydx.abacus.state.manager

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.modal.squidRoute
import exchange.dydx.abacus.state.modal.squidRouteV2
import exchange.dydx.abacus.state.modal.squidStatus
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import exchange.dydx.abacus.utils.toNobleAddress
import kollections.iListOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

private data class CctpChainTokenInfo(
    val chainId: String,
    val tokenAddress: String,
) {
    fun isCctpEnabled(transferInput: TransferInput?) =
        transferInput?.chain == chainId && transferInput.token == tokenAddress
}

private var cctpChainIds: List<CctpChainTokenInfo>? = null

internal fun V4StateManagerAdaptor.retrieveCctpChainIds() {
    val url = "$deploymentUri/configs/cctp.json"
    get(url) { _, response, _ ->
        if (response != null) {
            var chainIds = mutableListOf<CctpChainTokenInfo>()
            val chains: List<JsonElement>? = parser.decodeJsonArray(response)?.toList() as? List<JsonElement>
            for (chain in chains ?: emptyList()) {
                val chainInfo = chain.jsonObject
                val chainId = parser.asString(chainInfo["chainId"])
                val tokenAddress = parser.asString(chainInfo["tokenAddress"])
                 if (chainId != null && tokenAddress != null) {
                   chainIds.add(CctpChainTokenInfo(chainId, tokenAddress))
               }
            }
            cctpChainIds = chainIds
        }
    }
}

internal fun V4StateManagerAdaptor.retrieveDepositRoute(state: PerpetualState?) {
    val isCctp = cctpChainIds?.any { it.isCctpEnabled(state?.input?.transfer) } ?: false
    when (appConfigs.squidVersion) {
        AppConfigs.SquidVersion.V1, AppConfigs.SquidVersion.V2WithdrawalOnly -> retrieveDepositRouteV1(state)
        AppConfigs.SquidVersion.V2, AppConfigs.SquidVersion.V2DepositOnly ->
            if (isCctp) retrieveDepositRouteV2(state) else retrieveDepositRouteV1(state)
    }
}

private fun V4StateManagerAdaptor.retrieveDepositRouteV1(state: PerpetualState?) {
    val fromChain = state?.input?.transfer?.chain
    val fromToken = state?.input?.transfer?.token
    val fromAmount = parser.asDecimal(state?.input?.transfer?.size?.size)?.let {
        val decimals = parser.asInt(stateMachine.squidProcessor.selectedTokenDecimals(fromToken))
        if (decimals != null) {
            (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else null
    }
    val chainId = environment.dydxChainId
    val squidIntegratorId = environment.squidIntegratorId
    val dydxTokenDemon = environment.tokens["usdc"]?.denom
    val fromAmountString = parser.asString(fromAmount)
    val url = configs.squidRoute()
    if (fromChain != null &&
        fromToken != null &&
        fromAmount != null && fromAmount > 0 &&
        fromAmountString != null &&
        accountAddress != null &&
        chainId != null &&
        dydxTokenDemon != null &&
        url != null &&
        sourceAddress != null &&
        squidIntegratorId != null
    ) {
        val params: IMap<String, String> = iMapOf(
            "fromChain" to fromChain,
            "fromToken" to fromToken,
            "fromAmount" to fromAmountString,
            "toChain" to chainId,
            "toToken" to dydxTokenDemon,
            "toAddress" to accountAddress.toString(),
            "slippage" to "1",
            "enableForecall" to "false",
            "fromAddress" to sourceAddress.toString(),
        )

        val oldState = stateMachine.state
        val header = iMapOf(
            "x-integrator-id" to squidIntegratorId,
        )
        get(url, params, header) { _, response, _ ->
            if (response != null) {
                val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                val oldFromAmount = oldState?.input?.transfer?.size?.size
                if (currentFromAmount == oldFromAmount) {
                    update(stateMachine.squidRoute(response, subaccountNumber), oldState)
                }
            }
        }
    }
}

private fun V4StateManagerAdaptor.retrieveDepositRouteV2(state: PerpetualState?) {
    val fromChain = state?.input?.transfer?.chain
    val fromToken = state?.input?.transfer?.token
    val fromAmount = parser.asDecimal(state?.input?.transfer?.size?.size)?.let {
        val decimals = parser.asInt(stateMachine.squidProcessor.selectedTokenDecimals(fromToken))
        if (decimals != null) {
            (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
        } else null
    }
    val chainId = environment.dydxChainId
    val squidIntegratorId = environment.squidIntegratorId
    val dydxTokenDemon = environment.tokens["usdc"]?.denom
    val fromAmountString = parser.asString(fromAmount)
    val nobleAddress = accountAddress?.toNobleAddress()
    val url = configs.squidV2Route()
    val toChain = configs.nobleChainId()
    val toToken = configs.nobleDenom()
    if (fromChain != null &&
        fromToken != null &&
        fromAmount != null && fromAmount > 0 &&
        fromAmountString != null &&
        nobleAddress != null &&
        chainId != null &&
        dydxTokenDemon != null &&
        url != null &&
        sourceAddress != null &&
        squidIntegratorId != null &&
        toChain != null &&
        toToken != null
    ) {
        val body: Map<String, Any> = mapOf(
            "fromChain" to fromChain,
            "fromToken" to fromToken,
            "fromAddress" to sourceAddress.toString(),
            "fromAmount" to fromAmountString,
            "toChain" to toChain,
            "toToken" to toToken,
            "toAddress" to nobleAddress,
            "quoteOnly" to false,
            "enableBoost" to true,
            "slippage" to 1,
            "slippageConfig" to iMapOf<String, Any>(
                "autoMode" to 1
            ),
        )
        val oldState = stateMachine.state
        val header = iMapOf(
            "x-integrator-id" to squidIntegratorId,
            "Content-Type" to "application/json",
        )
        post(url, header, body.toJsonPrettyPrint()) { response, _ ->
            if (response != null) {
                val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                val oldFromAmount = oldState?.input?.transfer?.size?.size
                if (currentFromAmount == oldFromAmount) {
                    update(stateMachine.squidRouteV2(response, subaccountNumber), oldState)
                }
            }
        }
    }
}


internal fun V4StateManagerAdaptor.simulateWithdrawal(decimals: Int, callback: (BigDecimal?) -> Unit) {
    val payload = withdrawPayloadJson()

    transaction(
        TransactionType.simulateWithdraw,
        payload,
    ) { response ->
        val error = parseTransactionResponse(response)
        if (error != null) {
            callback(null)
            return@transaction
        }

        val result = parser.decodeJsonObject(response)
        if (result != null) {
            val amountMap = parser.asMap(parser.asList(result["amount"])?.firstOrNull())
            val amount = parser.asDecimal(amountMap?.get("amount"))
            val usdcAmount = amount?.div(Numeric.decimal.TEN.pow(decimals))
            callback(usdcAmount)
        } else {
            callback(null)
        }
    }
}

internal fun V4StateManagerAdaptor.simulateTransferNativeToken(decimals: Int, callback: (BigDecimal?) -> Unit) {
    val payload = transferNativeTokenPayloadJson()

    transaction(
        TransactionType.simulateTransferNativeToken,
        payload
    ) { response ->
        val error = parseTransactionResponse(response)
        if (error != null) {
            callback(null)
            return@transaction
        }

        val result = parser.decodeJsonObject(response)
        if (result != null) {
            val amountMap = parser.asMap(parser.asList(result["amount"])?.firstOrNull())
            val amount = parser.asDecimal(amountMap?.get("amount"))
            val tokenAmount = amount?.div(Numeric.decimal.TEN.pow(decimals))
            callback(tokenAmount)
        } else {
            callback(null)
        }
    }
}

internal fun V4StateManagerAdaptor.retrieveWithdrawalRoute(
    state: PerpetualState?,
    decimals: Int,
    gas: BigDecimal,
) {
    val isCctp = cctpChainIds?.any { it.isCctpEnabled(state?.input?.transfer) } ?: false
    when (appConfigs.squidVersion) {
        AppConfigs.SquidVersion.V1, AppConfigs.SquidVersion.V2DepositOnly -> retrieveWithdrawalRouteV1(state, decimals, gas)
        AppConfigs.SquidVersion.V2, AppConfigs.SquidVersion.V2WithdrawalOnly ->
            if (isCctp) retrieveWithdrawalRouteV2(state, decimals, gas) else retrieveWithdrawalRouteV1(state, decimals, gas)
    }
}

internal fun V4StateManagerAdaptor.retrieveWithdrawalRouteV1(
    state: PerpetualState?,
    decimals: Int,
    gas: BigDecimal,
){
    val toChain = state?.input?.transfer?.chain
    val toToken = state?.input?.transfer?.token
    val toAddress = state?.input?.transfer?.address
    val usdcSize = parser.asDecimal(state?.input?.transfer?.size?.usdcSize)
    val fromAmount = if (usdcSize != null && usdcSize > gas) {
        ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
    } else {
        null
    }
    val chainId = environment.dydxChainId
    val squidIntegratorId = environment.squidIntegratorId
    val dydxTokenDemon = environment.tokens["usdc"]?.denom
    val fromAmountString = parser.asString(fromAmount)
    val url = configs.squidRoute()
    val fromAddress = accountAddress
    if (toChain != null &&
        toToken != null &&
        toAddress != null &&
        fromAmount != null && fromAmount > 0 &&
        fromAmountString != null &&
        accountAddress != null &&
        chainId != null &&
        dydxTokenDemon != null &&
        url != null &&
        fromAddress != null &&
        squidIntegratorId != null
    ) {
        val params: IMap<String, String> = iMapOf(
            "fromChain" to chainId,
            "fromToken" to dydxTokenDemon,
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
        get(url, params, header) { _, response, _ ->
            if (response != null) {
                update(stateMachine.squidRoute(response, subaccountNumber), oldState)
            }
        }
    }
}

internal fun V4StateManagerAdaptor.retrieveWithdrawalRouteV2(
    state: PerpetualState?,
    decimals: Int,
    gas: BigDecimal,
) {
    val toChain = state?.input?.transfer?.chain
    val toToken = state?.input?.transfer?.token
    val toAddress = state?.input?.transfer?.address
    val usdcSize = parser.asDecimal(state?.input?.transfer?.size?.usdcSize)
    val fromAmount = if (usdcSize != null && usdcSize > gas) {
        ((usdcSize - gas) * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
    } else {
        null
    }
    val chainId = environment.dydxChainId
    val squidIntegratorId = environment.squidIntegratorId
    val dydxTokenDemon = environment.tokens["usdc"]?.denom
    val fromAmountString = parser.asString(fromAmount)
    val url = configs.squidV2Route()
    val fromAddress = accountAddress?.toNobleAddress()
    val fromChain = configs.nobleChainId()
    val fromToken = configs.nobleDenom()
    if (toChain != null &&
        toToken != null &&
        toAddress != null &&
        fromAmount != null && fromAmount > 0 &&
        fromAmountString != null &&
        accountAddress != null &&
        chainId != null &&
        dydxTokenDemon != null &&
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
            "enableBoost" to true,
            "slippage" to 1,
            "slippageConfig" to iMapOf<String, Any>(
                "autoMode" to 1
            ),
           // "enableForecall" to "false",
           // "cosmosSignerAddress" to accountAddress.toString(),
        )
        val oldState = stateMachine.state
        val header = iMapOf(
            "x-integrator-id" to squidIntegratorId,
            "Content-Type" to "application/json",
        )
        post(url, header, body.toJsonPrettyPrint()) { response, _ ->
            if (response != null) {
                val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                val oldFromAmount = oldState?.input?.transfer?.size?.size
                if (currentFromAmount == oldFromAmount) {
                    update(stateMachine.squidRouteV2(response, subaccountNumber), oldState)
                }
            }
        }
    }
}

internal fun V4StateManagerAdaptor.fetchTransferStatus(
    hash: String,
    fromChainId: String?,
    toChainId: String?,
) {
    val params: IMap<String, String> = iMapOf(
        "transactionId" to hash,
        "fromChainId" to fromChainId,
        "toChainId" to toChainId,
    ).filterNotNull()
    val url = configs.squidStatus()
    val squidIntegratorId = environment.squidIntegratorId
    if (url != null && squidIntegratorId != null) {
        val oldState = stateMachine.state
        val header = iMapOf(
            "x-integrator-id" to squidIntegratorId,
        )
        get(url, params, header) { _, response, httpCode ->
            if (response != null) {
                update(stateMachine.squidStatus(response, hash), oldState)
            }
        }
    }
}

internal fun V4StateManagerAdaptor.receiveTransferGas(gas: BigDecimal?) {
    val input = stateMachine.input
    val oldFee = parser.asDecimal(parser.value(input, "transfer.fee"))
    if (oldFee != gas) {
        val oldState = stateMachine.state
        val modified = input?.mutable() ?: iMapOf<String, Any>().mutable()
        modified.safeSet("transfer.fee", gas)
        update(StateChanges(iListOf(Changes.input)), oldState)
    }
}
