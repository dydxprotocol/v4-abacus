package exchange.dydx.abacus.processor.router.Squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol

internal class SquidRoutePayloadProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            // Transaction request payload
            "route.transactionRequest.routeType" to "routeType",
            "route.transactionRequest.targetAddress" to "targetAddress",
            "route.transactionRequest.data" to "data",
            "route.transactionRequest.value" to "value",
            "route.transactionRequest.gasPrice" to "gasPrice",
            "route.transactionRequest.gasLimit" to "gasLimit",
            "route.transactionRequest.maxFeePerGas" to "maxFeePerGas",
            "route.transactionRequest.maxPriorityFeePerGas" to "maxPriorityFeePerGas",
            "route.params.fromToken.chainId" to "fromChainId",
            "route.params.fromToken.address" to "fromAddress",
            "route.params.toToken.chainId" to "toChainId",
            "route.params.toToken.address" to "toAddress",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return transform(existing, payload, keyMap)
    }
}
