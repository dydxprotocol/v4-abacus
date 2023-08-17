package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.protocols.DataNotificationProtocol
import exchange.dydx.abacus.protocols.StateNotificationProtocol
import exchange.dydx.abacus.protocols.V3PrivateSignerProtocol
import exchange.dydx.abacus.state.app.V4Environment
import exchange.dydx.abacus.state.app.signer.V3ApiKey
import exchange.dydx.abacus.state.manager.configs.V3StateManagerConfigs
import exchange.dydx.abacus.state.modal.candles
import exchange.dydx.abacus.state.modal.feeDiscounts
import exchange.dydx.abacus.state.modal.feeTiers
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IOImplementations
import exchange.dydx.abacus.utils.ServerTime
import exchange.dydx.abacus.utils.UIImplementations
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iMutableMapOf
import kollections.toIMutableMap

class V3StateManagerAdaptor(
    ioImplementations: IOImplementations,
    uiImplementations: UIImplementations,
    environment: V4Environment,
    override val configs: V3StateManagerConfigs,
    stateNotification: StateNotificationProtocol?,
    dataNotification: DataNotificationProtocol?,
    private val v3Signer: V3PrivateSignerProtocol?,
    private val apiKey: V3ApiKey?,
) : StateManagerAdaptor(
    ioImplementations, uiImplementations, environment, configs, stateNotification, dataNotification
) {
    override fun didSetReadyToConnect(readyToConnect: Boolean) {
        super.didSetReadyToConnect(readyToConnect)
        if (readyToConnect) {
            retrieveFeeDiscounts()
        }
    }

    override fun canConnectTo(subaccountNumber: Int): Boolean {
        return subaccountNumber == 0
    }

    override fun retrieveFeeTiers() {
        val oldState = stateMachine.state
        val url = configs.configsUrl("fee_tiers")
        if (url != null) {
            ioImplementations.rest?.get(url, null, callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.feeTiers(response), oldState)
                }
            })
        }
    }

    private fun retrieveFeeDiscounts() {
        val oldState = stateMachine.state
        val url = configs.configsUrl("fee_discounts")
        if (url != null) {
            ioImplementations.rest?.get(url, null, callback = { response, httpCode ->
                if (success(httpCode) && response != null) {
                    update(stateMachine.feeDiscounts(response), oldState)
                }
            })
        }
    }

    override fun retrieveSubaccounts() {
    }

    override fun subaccountChannelParams(
        accountAddress: String,
        subaccountNumber: Int,
    ): IMap<String, Any> {
        val params = iMapOf("accountNumber" to "$subaccountNumber")
        val channel = configs.subaccountChannel()
        return if (channel != null) {
            transformSignedSocketParams(channel, params)
        } else {
            params
        }
    }


    private fun transformSignedSocketParams(
        channel: String,
        params: IMap<String, Any>,
    ): IMap<String, Any> {
        if (apiKey == null || v3Signer == null) return params

        val timeStamp = ServerTime.now().toString()
        val signingRequest = socketSigningRequest(channel, timeStamp)

        val secret = apiKey.secret
        val signature = v3Signer.sign(signingRequest, secret)

        val modified = params.toIMutableMap()
        modified.safeSet("apiKey", apiKey.key)
        modified.safeSet("passphrase", apiKey.passPhrase)
        modified.safeSet("timestamp", timeStamp)
        modified.safeSet("signature", signature)
        return modified
    }

    private fun socketSigningRequest(channel: String, timeStamp: String): String {
        return signingRequest("GET", transformSocketChannel(channel), null, null, timeStamp)
    }

    private fun signingRequest(
        verb: String,
        path: String,
        params: String?,
        body: String?,
        timeStamp: String,
    ): String {
        val pathAndParams = if (params != null) "$path$params" else path
        return if (body != null) "$timeStamp$verb$pathAndParams$body" else "$timeStamp$verb$pathAndParams"
    }

    private fun transformSocketChannel(channel: String): String {
        return when (channel) {
            "v3_accounts" -> "/ws/accounts"
            else -> channel
        }
    }

    override fun privateHeaders(
        path: String,
        verb: String,
        params: IMap<String, String>?,
        headers: IMap<String, String>?,
        body: String?,
    ): IMap<String, String>? {
        val accountAddress = accountAddress
        if (v3Signer == null || apiKey == null || accountAddress == null) return null

        val timeStamp = ServerTime.now().toString()
        val signingRequest =
            signingRequest(
                "GET",
                path,
                params?.toString(),
                body,
                timeStamp
            )
        val signatureHeader = "dydx-signature"
        val secret = apiKey.secret
        val signature = v3Signer.sign(signingRequest, secret)

        val modified = headers?.mutable() ?: iMutableMapOf()
        val referrer = configs.referrer()
        if (referrer != null) {
            modified["Referer"] = referrer
            modified["Origin"] = referrer
        }
        modified["dydx-ethereum-address"] = accountAddress
        modified["dydx-api-key"] = apiKey.key
        modified["dydx-passphrase"] = apiKey.passPhrase
        modified["dydx-timestamp"] = timeStamp
        if (signature != null) {
            modified[signatureHeader] = signature
        }

        return modified
    }

    override fun parseSparklinesResponse(response: String) {
        val oldState = stateMachine.state
        update(stateMachine.candles(response), oldState)
    }
}