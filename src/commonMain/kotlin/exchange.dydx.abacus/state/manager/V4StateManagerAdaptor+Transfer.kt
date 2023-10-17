package exchange.dydx.abacus.state.manager

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import exchange.dydx.abacus.output.PerpetualState
import exchange.dydx.abacus.protocols.ThreadingType
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
import kollections.iListOf
import kollections.toIMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.math.pow

private val dydxTokenDemon = "ibc/8E27BA2D5493AF5636760E354E46004562C46AB7EC0CC4C1CA14E9E20E2545B5"

internal fun V4StateManagerAdaptor.retrieveDepositRoute(state: PerpetualState?) {
    val fromChain = state?.input?.transfer?.chain
    val fromToken = state?.input?.transfer?.token
    val fromAmount = parser.asDecimal(state?.input?.transfer?.size?.size)?.let {
        val decimals = parser.asInt(stateMachine.squidProcessor.selectedTokenDecimals(fromToken))
        if (decimals != null) {
            (it * Numeric.decimal.TEN.pow(decimals)).toBigInteger()
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
            "fromAddress" to sourceAddress.toString(),
        )

        val oldState = stateMachine.state
        get(url, params, null) { _, response, httpCode ->
            if (success(httpCode) && response != null) {
                val currentFromAmount = stateMachine.state?.input?.transfer?.size?.size
                val oldFromAmount = oldState?.input?.transfer?.size?.size
                if (currentFromAmount == oldFromAmount) {
                    update(stateMachine.squidRoute(response, subaccountNumber), oldState)
                }
            }
        }
    }
}

internal fun V4StateManagerAdaptor.simulateWithdrawal(exponents: Int, callback: (BigDecimal?) -> Unit) {
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

        val result = Json.parseToJsonElement(response).jsonObject.toIMap()
        val amountMap = parser.asMap(parser.asList(result["amount"])?.firstOrNull())
        val amount = parser.asDecimal(amountMap?.get("amount"))
        val usdcAmount = amount?.div(Numeric.decimal.TEN.pow(exponents))
        callback(usdcAmount)
    }
}

internal fun V4StateManagerAdaptor.simulateTransferNativeToken(exponents: Int, callback: (BigDecimal?) -> Unit) {
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

        val result = Json.parseToJsonElement(response).jsonObject.toIMap()
        val amountMap = parser.asMap(parser.asList(result["amount"])?.firstOrNull())
        val amount = parser.asDecimal(amountMap?.get("amount"))
        val tokenAmount = amount?.div(Numeric.decimal.TEN.pow(exponents))
        callback(tokenAmount)
    }
}

internal fun V4StateManagerAdaptor.retrieveWithdrawalRoute(exponents: Int, gas: BigDecimal) {
    val state = stateMachine.state
    val toChain = state?.input?.transfer?.chain
    val toToken = state?.input?.transfer?.token
    val toAddress = state?.input?.transfer?.address
    val usdcSize = parser.asDecimal(state?.input?.transfer?.size?.usdcSize)
    val fromAmount = if (usdcSize != null && usdcSize > gas) {
        ((usdcSize - gas) * Numeric.decimal.TEN.pow(exponents)).toBigInteger()
    } else {
        null
    }
    val chainId = environment.dydxChainId
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
        url != null &&
        fromAddress != null
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
        get(url, params, null) { _, response, httpCode ->
            if (success(httpCode) && response != null) {
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
    if (url != null) {
        val oldState = stateMachine.state
        get(url, params, null) { _, response, httpCode ->
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
