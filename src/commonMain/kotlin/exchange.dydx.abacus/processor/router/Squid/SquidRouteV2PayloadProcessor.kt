package exchange.dydx.abacus.processor.router.Squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.safeSet

internal class SquidRouteV2PayloadProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            // Transaction request payload
            "route.transactionRequest.routeType" to "routeType",
            "route.transactionRequest.target" to "targetAddress",
            "route.transactionRequest.data" to "data",
            "route.transactionRequest.value" to "value",
            "route.transactionRequest.gasPrice" to "gasPrice",
            "route.transactionRequest.gasLimit" to "gasLimit",
            "route.transactionRequest.maxFeePerGas" to "maxFeePerGas",
            "route.transactionRequest.maxPriorityFeePerGas" to "maxPriorityFeePerGas",
            "route.estimate.fromToken.chainId" to "fromChainId",
            "route.estimate.fromToken.address" to "fromAddress",
            "route.estimate.toToken.chainId" to "toChainId",
            "route.estimate.toToken.address" to "toAddress",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        var state = transform(existing, payload, keyMap)
        state.safeSet("isV2Route", true)
        return state
    }
}
