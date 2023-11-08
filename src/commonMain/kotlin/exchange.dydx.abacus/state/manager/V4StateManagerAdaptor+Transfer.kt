package exchange.dydx.abacus.state.manager

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.modal.squidRoute
import exchange.dydx.abacus.state.modal.squidStatus
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.toJsonPrettyPrint
import kollections.iListOf

internal fun V4StateManagerAdaptor.retrieveDepositRoute(state: PerpetualState?) {
    when (appConfigs.squidVersion) {
        AppConfigs.SquidVersion.V1 -> retrieveDepositRouteV1(state)
        AppConfigs.SquidVersion.V2 -> retrieveDepositRouteV2(state)
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
    val url = "https://testnet.v2.api.squidrouter.com/v2/route" // configs.squidRoute()
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
        val body: Map<String, Any> = mapOf(
            "fromChain" to fromChain,
            "fromToken" to "0x07865c6E87B9F70255377e024ace6630C1Eaa37F", //fromToken,
            "fromAddress" to "0xb13CD07B22BC5A69F8500a1Cb3A1b65618d50B22", //sourceAddress.toString(),
            "fromAmount" to fromAmountString,
            "toChain" to "grand-1", //chainId,
            "toToken" to "uusdc", // dydxTokenDemon,
            "toAddress" to "noble1zqnudqmjrgh9m3ec9yztkrn4ttx7ys64p87kkx", //accountAddress.toString(),
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
                    update(stateMachine.squidRoute(response, subaccountNumber), oldState)
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

internal fun V4StateManagerAdaptor.retrieveWithdrawalRoute(decimals: Int, gas: BigDecimal) {
    val state = stateMachine.state
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
        val params: IMap<String, String> = iMapOf<String, String>(
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
