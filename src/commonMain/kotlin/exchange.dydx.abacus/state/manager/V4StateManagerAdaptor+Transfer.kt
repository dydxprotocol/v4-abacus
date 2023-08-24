package exchange.dydx.abacus.state.manager

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.protocols.ThreadingType
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.state.modal.squidRoute
import exchange.dydx.abacus.state.modal.squidStatus
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.filterNotNull
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iListOf
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.math.pow

private val dydxTokenDemon = "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5"

internal fun V4StateManagerAdaptor.retrieveDepositRoute(state: PerpetualState?) {
    val fromChain = state?.input?.transfer?.chain
    val fromToken = state?.input?.transfer?.token
    val fromAmount = state?.input?.transfer?.size?.size?.let {
        val decimals = parser.asDouble(stateMachine.squidProcessor.selectedTokenDecimals(fromToken))
        if (decimals != null) {
            (it * 10.0.pow(decimals)).toBigDecimal().toBigInteger()
        } else null
    }
    val chainId = environment.dydxChainId
    val fromAmountString = parser.asString(fromAmount)
    val url = configs.squidRoute()
    if (fromChain != null &&
        fromToken != null &&
        fromAmount != null && fromAmount > 0 &&
        fromAmountString != null &&
        accountAddress != null &&
        chainId != null &&
        url != null &&
        sourceAddress != null
    ) {
        val params: IMap<String, String> = iMapOf<String, String>(
            "fromChain" to fromChain,
            "fromToken" to fromToken,
            "fromAmount" to fromAmountString,
            "toChain" to chainId,
            "toToken" to dydxTokenDemon,
            "toAddress" to accountAddress.toString(),
            "slippage" to "1",
            "enableForecall" to "false",
            "evmFallbackAddress" to sourceAddress.toString(),
        )

        val oldState = stateMachine.state
        get(url, params, null, false, callback = { response, httpCode ->
            if (success(httpCode) && response != null) {
                update(stateMachine.squidRoute(response), oldState)
            }
        })
    }
}

internal fun V4StateManagerAdaptor.simulateWithdrawal(callback: (Double?) -> Unit) {
    val payload = withdrawPayloadJson()

    transaction(
        TransactionType.simulateWithdraw,
        payload) { response ->
        val error = parseTransactionResponse(response)
        if (error != null) {
            callback(null)
            return@transaction
        }

        val result = Json.parseToJsonElement(response).jsonObject.toIMap()
        val amountMap = parser.asMap(parser.asList(result["amount"])?.firstOrNull())
        val amount = parser.asDouble(amountMap?.get("amount"))
        val decimals = 6
        val usdcAmount = amount?.div(10.0.pow(decimals))
        callback(usdcAmount)
    }
}

internal fun V4StateManagerAdaptor.simulateTransferNativeToken(callback: (Double?) -> Unit) {
    val payload = transferNativeTokenPayloadJson()

    transaction(
        TransactionType.simulateTransferNativeToken,
        payload) { response ->
        val error = parseTransactionResponse(response)
        if (error != null) {
            callback(null)
            return@transaction
        }

        val result = Json.parseToJsonElement(response).jsonObject.toIMap()
        val amountMap = parser.asMap(parser.asList(result["amount"])?.firstOrNull())
        val amount = parser.asDouble(amountMap?.get("amount"))
        val decimals = 6
        val usdcAmount = amount?.div(10.0.pow(decimals))
        callback(usdcAmount)
    }
}
internal fun V4StateManagerAdaptor.retrieveWithdrawalRoute(gas: Double) {
    val state = stateMachine.state
    val toChain = state?.input?.transfer?.chain
    val toToken = state?.input?.transfer?.token
    val toAddress = state?.input?.transfer?.address
    val usdcSize = state?.input?.transfer?.size?.usdcSize
    val fromAmount = if (usdcSize != null && usdcSize > gas) {
        val decimals = 6
        ((usdcSize - gas) * 10.0.pow(decimals)).toBigDecimal().toBigInteger()
    } else {
        null
    }
    val chainId = environment.dydxChainId
    val fromAmountString = parser.asString(fromAmount)
    val url = configs.squidRoute()
    if (toChain != null &&
        toToken != null &&
        toAddress != null &&
        fromAmount != null && fromAmount > 0 &&
        fromAmountString != null &&
        accountAddress != null &&
        chainId != null &&
        url != null
    ) {
        val params: IMap<String, String> = iMapOf<String, String>(
            "fromChain" to chainId,
            "fromToken" to dydxTokenDemon,
            "fromAmount" to fromAmountString,
            "toChain" to toChain,
            "toToken" to toToken,
            "toAddress" to toAddress,
            "slippage" to "1",
            "enableForecall" to "false",
            "cosmosSignerAddress" to accountAddress.toString(),
        )

        val oldState = stateMachine.state
        get(url, params, null, false, callback = { response, httpCode ->
            if (success(httpCode) && response != null) {
                update(stateMachine.squidRoute(response), oldState)
            }
        })
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
    if (url != null) {
        val oldState = stateMachine.state
        get(url, params, null, false, callback = { response, httpCode ->
            if (success(httpCode) && response != null) {
                update(stateMachine.squidStatus(response), oldState)
            }
        })
    }
}

internal fun V4StateManagerAdaptor.receiveTransferGas(gas: Double?) {
    val input = stateMachine.input
    val oldFee = parser.asDouble(parser.value(input, "transfer.fee"))
    if (oldFee != gas) {
        val oldState = stateMachine.state
        var modified = input?.mutable() ?: iMapOf<String, Any>().mutable()
        modified.safeSet("transfer.fee", gas)
        update(StateChanges(iListOf(Changes.input)), oldState)
    }
}
