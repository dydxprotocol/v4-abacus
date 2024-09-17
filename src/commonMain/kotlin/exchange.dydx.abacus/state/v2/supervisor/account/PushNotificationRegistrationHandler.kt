package exchange.dydx.abacus.state.v2.supervisor.account

import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.abacus.state.v2.supervisor.NetworkHelper
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.toJsonPrettyPrint

internal interface PushNotificationRegistrationHandlerProtocol {
    fun sendPushNotificationToken(token: String, languageCode: String, isKepler: Boolean)
}

internal class PushNotificationRegistrationHandler(
    private val helper: NetworkHelper,
    private val accountAddress: String,
) : PushNotificationRegistrationHandlerProtocol {

    override fun sendPushNotificationToken(token: String, languageCode: String, isKepler: Boolean) {
        getSignedPayload(token, languageCode, isKepler) { payload ->
            if (payload != null) {
                sendPushNotificationToken(payload)
            } else {
                Logger.e { "Push notification token payload signing failed" }
            }
        }
    }

    private fun sendPushNotificationToken(
        payload: Map<String, Any>,
    ) {
        val indexerUrl = helper.configs.indexerConfig?.api ?: return
        val registrationUrl = "$indexerUrl/v4/addresses/$accountAddress/registerToken"
        val header =
            iMapOf(
                "Content-Type" to "application/json",
            )
        helper.post(
            url = registrationUrl,
            headers = header,
            body = payload.toJsonPrettyPrint(),
        ) { _, response, httpCode, _ ->
            if (helper.success(httpCode) && response != null) {
                Logger.d { "Push notification token registered successfully" }
            } else {
                Logger.e { "Push notification token registration failed: $response" }
            }
        }
    }

    private fun getSignedPayload(
        token: String,
        language: String,
        isKepler: Boolean,
        callback: (Map<String, Any>?) -> Unit
    ) {
        val message = "Verify account ownership"
        val payload =
            helper.jsonEncoder.encode(
                mapOf(
                    "message" to message,
                ),
            )
        helper.transaction(
            TransactionType.SignPushNotificationTokenRegistrationPayload,
            payload,
        ) { additionalPayload ->
            val error = helper.parseTransactionResponse(additionalPayload)
            val result = helper.parser.decodeJsonObject(additionalPayload)

            if (error == null && result != null) {
                val signedMessage = helper.parser.asString(result["signedMessage"])
                val publicKey = helper.parser.asString(result["publicKey"])
                val timestamp = helper.parser.asString(result["timestamp"])

                if (signedMessage != null && publicKey != null && timestamp != null) {
                    callback(
                        mapOf(
                            "token" to token,
                            "language" to language,
                            "message" to message,
                            "signedMessage" to signedMessage,
                            "pubKey" to publicKey,
                            "timestamp" to timestamp,
                            "walletIsKeplr" to isKepler,
                        ),
                    )
                } else {
                    Logger.e { "Push notification token payload signing error: Invalid payload" }
                    callback(null)
                }
            } else {
                Logger.e { "Push notification token payload signing error: $error" }
                callback(null)
            }
        }
    }
}
