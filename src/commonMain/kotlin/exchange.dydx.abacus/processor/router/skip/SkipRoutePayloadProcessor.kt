package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
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
        COSMOS
    }

//    DO-LATER: https://linear.app/dydx/issue/OTE-350/%5Babacus%5D-cleanup
//    Create custom exceptions for better error handling specificity and expressiveness
    @Suppress("TooGenericExceptionThrown")
    internal fun getTxType(payload: Map<String, Any>): TxType {
        val evm = parser.value(payload, "txs.0.evm_tx")
        val cosmos = parser.value(payload, "txs.0.cosmos_tx")
        if (evm != null) return TxType.EVM
        if (cosmos != null) return TxType.COSMOS
        throw Error("SkipRoutePayloadProcessor: txType is not evm or cosmos")
    }

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val txType = getTxType(payload)
        val modified = transform(existing, payload, keyMap)
        val data = modified["data"]
        if (data != null && txType == TxType.EVM) {
//            skip does not provide the 0x prefix. it's not required but is good for clarity
//            and keeps our typing honest (we typecast this value to evmAddress in web)
            modified.safeSet("data", "0x$data")
        }
        if (txType == TxType.COSMOS) {
            val jsonEncoder = JsonEncoder()
            val msg = parser.asString(parser.value(payload, "txs.0.cosmos_tx.msgs.0.msg"))
            val msgMap = parser.decodeJsonObject(msg)
//            tendermint client rejects msgs that aren't camelcased
            val camelCasedMsgMap = msgMap?.toCamelCaseKeys()
            val msgTypeUrl = parser.value(payload, "txs.0.cosmos_tx.msgs.0.msg_type_url")
            val fullMessage = mapOf(
                "msg" to camelCasedMsgMap,
//                Squid returns the msg payload under the "value" key for noble transfers
                "value" to camelCasedMsgMap,
                "msgTypeUrl" to msgTypeUrl,
//                Squid sometimes returns typeUrl or msgTypeUrl depending on the route version
                "typeUrl" to msgTypeUrl,
            )
            modified.safeSet("data", jsonEncoder.encode(fullMessage))
        }
        return modified
    }
}
