package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DEFAULT_GAS_LIMIT
import exchange.dydx.abacus.utils.DEFAULT_GAS_PRICE
import exchange.dydx.abacus.utils.JsonEncoder
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.toCamelCaseKeys

// We may later want to split this into one processor per network
// For now we're not since it's just two, but at 3 we will
internal class SkipRoutePayloadProcessor(parser: ParserProtocol) : BaseProcessor(parser) {

    private val keyMap = mapOf(
        "string" to mapOf(
            // Transaction request payload
            "txs.0.evm_tx.to" to "targetAddress",
            "txs.0.evm_tx.data" to "data",
            "txs.0.evm_tx.value" to "value",
            "route.source_asset_chain_id" to "fromChainId",
            "route.source_asset_denom" to "fromAddress",
            "route.dest_asset_chain_id" to "toChainId",
            "route.dest_asset_denom" to "toAddress",
            "txs.0.svm_tx.tx" to "solanaTransaction",
//            SQUID PARAMS THAT ARE NOW DEPRECATED:
//            "route.transactionRequest.routeType" to "routeType",
//            "route.transactionRequest.gasPrice" to "gasPrice",
//            "route.transactionRequest.gasLimit" to "gasLimit",
//            "route.transactionRequest.maxFeePerGas" to "maxFeePerGas",
//            "route.transactionRequest.maxPriorityFeePerGas" to "maxPriorityFeePerGas",
        ),
    )

    enum class TxType {
        EVM,
        COSMOS,
        SOLANA
    }

    //    DO-LATER: https://linear.app/dydx/issue/OTE-350/%5Babacus%5D-cleanup
//    Create custom exceptions for better error handling specificity and expressiveness
    @Suppress("TooGenericExceptionThrown")
    internal fun getTxType(payload: Map<String, Any>): TxType {
        val evm = parser.value(payload, "txs.0.evm_tx")
        val cosmos = parser.value(payload, "txs.0.cosmos_tx")
        val solana = parser.value(payload, "txs.0.svm_tx")
        if (evm != null) return TxType.EVM
        if (cosmos != null) return TxType.COSMOS
        if (solana != null) return TxType.SOLANA
        throw Error("SkipRoutePayloadProcessor: txType is not evm or cosmos")
    }

    private fun jsonEncodePayload(payload: Any): String {
        val jsonEncoder = JsonEncoder()
        return jsonEncoder.encode(payload)
    }

    private fun formatMessage(message: String?, msgTypeUrl: String?): Map<String, Any?> {
        val msgMap = parser.decodeJsonObject(message)
//            tendermint client rejects msgs that aren't camelcased
        val camelCasedMsgMap = msgMap?.toCamelCaseKeys()
        val fullMessage = mapOf(
            "msg" to camelCasedMsgMap,
//                Squid returns the msg payload under the "value" key for noble transfers
            "value" to camelCasedMsgMap,
            "msgTypeUrl" to msgTypeUrl,
//                Squid sometimes returns typeUrl or msgTypeUrl depending on the route version
            "typeUrl" to msgTypeUrl,
        )
        return fullMessage
    }

    private fun formatAllMessages(payload: Map<String, Any>?): List<Map<String, Any?>> {
        val allRawMessages = parser.asList(parser.value(payload, "txs.0.cosmos_tx.msgs"))
        val allFormattedMessages = mutableListOf<Map<String, Any?>>()
        allRawMessages?.forEach {
            val msgObject = parser.asMap(it)
            val msg = parser.asString(msgObject?.get("msg"))
            val msgTypeUrl = parser.asString(msgObject?.get("msg_type_url"))
            allFormattedMessages.add(formatMessage(msg, msgTypeUrl))
        }
        return allFormattedMessages
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val txType = getTxType(payload)
        val modified = transform(existing, payload, keyMap)
        // squid used to provide these, but now we need to hardcode them
        // for the API to work (even though they seem to have default values?): https://ethereum.org/en/developers/docs/apis/json-rpc/#eth_sendtransaction
        modified.safeSet("gasPrice", DEFAULT_GAS_PRICE)
        modified.safeSet("gasLimit", DEFAULT_GAS_LIMIT)
        val data = modified["data"]
        if (data != null && txType == TxType.EVM) {
            modified.safeSet("data", "0x$data")
        }
        if (txType == TxType.COSMOS) {
            val msg = parser.asString(parser.value(payload, "txs.0.cosmos_tx.msgs.0.msg"))
            val msgTypeUrl = parser.asString(parser.value(payload, "txs.0.cosmos_tx.msgs.0.msg_type_url"))
            val formattedMessage = formatMessage(msg, msgTypeUrl)
            val allFormattedMessages = formatAllMessages(payload)
//            save all messages in array
            modified.safeSet("data", jsonEncodePayload(formattedMessage))
            modified.safeSet("allMessages", jsonEncodePayload(allFormattedMessages))
        }
        if (txType == TxType.SOLANA) {
            if (modified["solanaTransaction"] != null) {
                modified.safeSet("data", modified["solanaTransaction"])
                modified.remove("solanaTransaction")

                // These are EVM specific fields and do not make sense for solana
                modified.remove("gasPrice")
                modified.remove("gasLimit")
            }
        }
        return modified
    }
}
