package exchange.dydx.abacus.processor.squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf

internal class SquidRoutePayloadProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = iMapOf(
        "string" to iMapOf(
            // Transaction request payload
            "route.transactionRequest.routeType" to "routeType",
            "route.transactionRequest.targetAddress" to "targetAddress",
            "route.transactionRequest.data" to "data",
            "route.transactionRequest.value" to "value",
            "route.transactionRequest.gasPrice" to "gasPrice",
            "route.transactionRequest.gasLimit" to "gasLimit",
            "route.transactionRequest.maxFeePerGas" to "maxFeePerGas",
            "route.transactionRequest.maxPriorityFeePerGas" to "maxPriorityFeePerGas"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        return transform(existing, payload, keyMap)
    }
}