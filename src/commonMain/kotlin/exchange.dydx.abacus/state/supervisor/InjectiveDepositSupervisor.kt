package exchange.dydx.abacus.state.supervisor

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.ParsingErrorType
import exchange.dydx.abacus.state.InternalInjectiveBalancesState
import exchange.dydx.abacus.state.manager.InjectiveDepositConfig
import exchange.dydx.abacus.utils.CoroutineTimer
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.INJ_DECIMALS
import exchange.dydx.abacus.utils.InjectiveTx
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.toJson
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

internal data class GasSponsorshipGrant(
    // The address to set as the fee granter of the sponsored transaction
    val granter: String,
    // A transaction whose fee is at most gasLimit * gasPrice fits within the allowance
    val gasLimit: String,
    val gasPrice: String,
)

private data class InjectiveAccount(val accountNumber: Long, val sequence: Long)

private data class InjectiveFee(val amount: String, val gasLimit: Long, val granter: String?)

/**
 * Sends USDC that has arrived on Injective to the dYdX chain over IBC, asking the indexer to
 * sponsor the gas when the Injective account has too little INJ to pay for it.
 * Abacus builds and broadcasts the transaction; the app only provides the public key and signature.
 */
internal class InjectiveDepositSupervisor(
    private val helper: NetworkHelper,
) {
    companion object {
        private const val CONFIRMED = "CONFIRMED"

        // The endpoint already waits for the grant to confirm before answering PENDING, and is rate
        // limited to 5 requests a minute, so a few spaced-out attempts are enough
        private const val MAX_SPONSORSHIP_ATTEMPTS = 4
        private const val SPONSORSHIP_RETRY_DELAY_SECONDS = 5.0
    }

    fun fetchBalances(
        injectiveAddress: String,
        completion: (InternalInjectiveBalancesState?) -> Unit,
    ) {
        val configs = helper.configs
        fetchBalance(injectiveAddress, configs.injectiveUsdcDenom) { usdc ->
            fetchBalance(injectiveAddress, configs.injectiveFeeDenom) { inj ->
                completion(
                    if (usdc != null && inj != null) InternalInjectiveBalancesState(usdc = usdc, inj = inj) else null,
                )
            }
        }
    }

    fun commit(
        dydxAddress: String,
        injectiveAddress: String,
        balances: InternalInjectiveBalancesState?,
        completion: (error: ParsingError?, hash: String?) -> Unit,
    ) {
        if (balances == null || balances.usdc <= BigDecimal.ZERO) {
            completion(error("No USDC found on Injective"), null)
            return
        }

        val configs = helper.configs
        val fee = configs.injectiveIbcTransferFee
        if (balances.inj >= fee) {
            val ownFee = InjectiveFee(
                amount = fee.toPlainString(),
                gasLimit = configs.injectiveIbcTransferGasLimit.longValue(),
                granter = null,
            )
            sendToDydx(dydxAddress, injectiveAddress, balances.usdc, ownFee, completion)
            return
        }

        val notEnoughInjMessage =
            "Not enough INJ to pay for gas. Send at least ${fee.moveDecimalPoint(-INJ_DECIMALS).toPlainString()} INJ to $injectiveAddress."
        if (!InjectiveDepositConfig.gas_sponsorship_enabled) {
            completion(error(notEnoughInjMessage), null)
            return
        }

        requestGasSponsorship(dydxAddress, injectiveAddress) { grant, sponsorshipError ->
            val sponsoredFee = grant?.let { sponsoredFee(it) }
            if (sponsoredFee != null) {
                sendToDydx(dydxAddress, injectiveAddress, balances.usdc, sponsoredFee, completion)
            } else {
                Logger.e { "Injective gas sponsorship failed: ${sponsorshipError?.message}" }
                completion(error(notEnoughInjMessage, sponsorshipError?.stringKey), null)
            }
        }
    }

    private fun fetchBalance(address: String, denom: String, completion: (BigDecimal?) -> Unit) {
        helper.get(helper.configs.injectiveBalanceUrl(address, denom)) { _, response, httpCode, _ ->
            val balance = helper.parser.asMap(helper.parser.decodeJsonObject(response)?.get("balance"))
            val amount = if (helper.success(httpCode)) helper.parser.asString(balance?.get("amount")) else null
            if (amount == null) {
                Logger.d { "Error fetching Injective $denom balance: $response" }
            }
            completion(amount?.let { BigDecimal.parseString(it) })
        }
    }

    // Requests a fee allowance for `injectiveAddress` and calls back once it can be used
    private fun requestGasSponsorship(
        dydxAddress: String,
        injectiveAddress: String,
        attempt: Int = 1,
        callback: (grant: GasSponsorshipGrant?, error: ParsingError?) -> Unit,
    ) {
        val url = helper.configs.publicApiUrl("gasSponsorship")
        if (url == null) {
            callback(null, error("Gas sponsorship is unavailable"))
            return
        }
        val body = mapOf("address" to dydxAddress, "injectiveAddress" to injectiveAddress).toJson()
        val headers = iMapOf("Content-Type" to "application/json")
        helper.post(url, headers, body) { _, response, httpCode, _ ->
            val json = helper.parser.decodeJsonObject(response)
            if (!helper.success(httpCode)) {
                callback(null, sponsorshipError(json, httpCode))
                return@post
            }

            val grant = parseConfirmedGrant(json)
            when {
                grant != null -> callback(grant, null)
                attempt < MAX_SPONSORSHIP_ATTEMPTS -> {
                    val timer = helper.ioImplementations.timer ?: CoroutineTimer.instance
                    timer.schedule(SPONSORSHIP_RETRY_DELAY_SECONDS, null) {
                        requestGasSponsorship(dydxAddress, injectiveAddress, attempt + 1, callback)
                        false
                    }
                }
                else -> callback(
                    null,
                    error("Gas sponsorship is taking longer than expected. Please retry."),
                )
            }
        }
    }

    private fun parseConfirmedGrant(json: IMap<String, Any>?): GasSponsorshipGrant? {
        if (helper.parser.asString(json?.get("status")) != CONFIRMED) {
            return null
        }
        val granter = helper.parser.asString(json?.get("granter")) ?: return null
        val gasLimit = helper.parser.asString(json?.get("gasLimit")) ?: return null
        val gasPrice = helper.parser.asString(json?.get("gasPrice")) ?: return null
        return GasSponsorshipGrant(granter = granter, gasLimit = gasLimit, gasPrice = gasPrice)
    }

    private fun sponsoredFee(grant: GasSponsorshipGrant): InjectiveFee? {
        val gasLimit = grant.gasLimit.toLongOrNull() ?: return null
        val gasPrice = runCatching { BigDecimal.parseString(grant.gasPrice) }.getOrNull() ?: return null
        val amount = (BigDecimal.fromLong(gasLimit) * gasPrice).ceil().toBigInteger().toString()
        return InjectiveFee(amount = amount, gasLimit = gasLimit, granter = grant.granter)
    }

    private fun sponsorshipError(json: IMap<String, Any>?, httpCode: Int): ParsingError {
        val firstError = helper.parser.asMap(helper.parser.asList(json?.get("errors"))?.firstOrNull())
        val code = helper.parser.asString(firstError?.get("code"))
        return error(
            message = helper.parser.asString(firstError?.get("msg")) ?: "Gas sponsorship failed with HTTP $httpCode",
            stringKey = code?.let { "ERRORS.$it" },
        )
    }

    private fun sendToDydx(
        dydxAddress: String,
        injectiveAddress: String,
        amount: BigDecimal,
        fee: InjectiveFee,
        completion: (error: ParsingError?, hash: String?) -> Unit,
    ) {
        val configs = helper.configs
        val body = InjectiveTx.transferBody(
            channel = configs.injectiveToDydxChannel,
            denom = configs.injectiveUsdcDenom,
            amount = amount.toPlainString(),
            sender = injectiveAddress,
            receiver = dydxAddress,
            timeoutTimestampNs = (Clock.System.now() + 10.minutes).toEpochMilliseconds() * 1_000_000,
        )

        helper.getOnChain(QueryType.GetInjectivePublicKey, null) { publicKeyResponse ->
            val publicKey = decodeBase64Field(publicKeyResponse, "publicKey")
            if (publicKey == null) {
                completion(helper.parseTransactionResponse(publicKeyResponse) ?: error("Injective key unavailable"), null)
                return@getOnChain
            }
            fetchAccount(injectiveAddress) { account ->
                if (account == null) {
                    completion(error("Injective account not found"), null)
                    return@fetchAccount
                }
                val authInfo = InjectiveTx.authInfo(
                    publicKey = publicKey,
                    sequence = account.sequence,
                    feeDenom = configs.injectiveFeeDenom,
                    feeAmount = fee.amount,
                    gasLimit = fee.gasLimit,
                    feeGranter = fee.granter,
                )
                val signDoc = InjectiveTx.signDoc(body, authInfo, configs.injectiveChainId(), account.accountNumber)
                signAndBroadcast(body, authInfo, signDoc, completion)
            }
        }
    }

    private fun fetchAccount(address: String, completion: (InjectiveAccount?) -> Unit) {
        helper.get(helper.configs.injectiveAccountUrl(address)) { _, response, httpCode, _ ->
            val parser = helper.parser
            val account = parser.asMap(parser.decodeJsonObject(response)?.get("account"))
            val baseAccount = parser.asMap(account?.get("base_account")) ?: account
            val accountNumber = parser.asString(baseAccount?.get("account_number"))?.toLongOrNull()
            val sequence = parser.asString(baseAccount?.get("sequence"))?.toLongOrNull()
            if (!helper.success(httpCode) || accountNumber == null || sequence == null) {
                Logger.e { "Error fetching Injective account: $response" }
                completion(null)
            } else {
                completion(InjectiveAccount(accountNumber = accountNumber, sequence = sequence))
            }
        }
    }

    private fun signAndBroadcast(
        body: ByteArray,
        authInfo: ByteArray,
        signDoc: ByteArray,
        completion: (error: ParsingError?, hash: String?) -> Unit,
    ) {
        val payload = mapOf("signDoc" to signDoc.encodeBase64()).toJson()
        helper.transaction(TransactionType.SignInjectiveDirect, payload) { signResponse ->
            val signature = decodeBase64Field(signResponse, "signature")
            if (signature == null) {
                val signError = helper.parseTransactionResponse(signResponse) ?: error("Injective signing failed")
                Logger.e { "TransactionType.SignInjectiveDirect error: $signError" }
                completion(signError, null)
                return@transaction
            }

            val txBytes = InjectiveTx.txRaw(body, authInfo, signature).encodeBase64()
            val broadcastBody = mapOf("tx_bytes" to txBytes, "mode" to "BROADCAST_MODE_SYNC").toJson()
            val headers = iMapOf("Content-Type" to "application/json")
            helper.post(helper.configs.injectiveBroadcastUrl(), headers, broadcastBody) { _, response, httpCode, _ ->
                val parser = helper.parser
                val json = parser.decodeJsonObject(response)
                val txResponse = parser.asMap(json?.get("tx_response"))
                val code = parser.asInt(txResponse?.get("code")) ?: 0
                val hash = parser.asString(txResponse?.get("txhash"))
                if (helper.success(httpCode) && code == 0 && hash != null) {
                    completion(null, hash)
                } else {
                    val message = parser.asString(txResponse?.get("raw_log")) ?: parser.asString(json?.get("message"))
                    Logger.e { "Injective broadcast failed: $response" }
                    completion(error(message ?: "Injective transfer failed with HTTP $httpCode"), null)
                }
            }
        }
    }

    private fun decodeBase64Field(response: String?, field: String): ByteArray? {
        val value = helper.parser.asString(helper.parser.decodeJsonObject(response)?.get(field)) ?: return null
        return runCatching { value.decodeBase64Bytes() }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    private fun error(message: String, stringKey: String? = null): ParsingError {
        return ParsingError(
            type = ParsingErrorType.BackendError,
            message = message,
            stringKey = stringKey,
        )
    }
}
