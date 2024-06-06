package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.safeSet

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

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val modified = transform(existing, payload, keyMap)
        val data = modified.get("data")
        if (data != null) {
//            skip does not provide the 0x prefix. it's not required but is good for clarity
//            and keeps our typing honest (we typecast this value to evmAddress in web)
            modified.safeSet("data", "0x$data")
        }
        return modified
    }
}
