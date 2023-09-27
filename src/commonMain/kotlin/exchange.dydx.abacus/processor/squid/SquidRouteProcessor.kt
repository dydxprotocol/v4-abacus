package exchange.dydx.abacus.processor.squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.safeSet

internal class SquidRouteProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "route.estimate.gasCosts.0.amountUSD" to "gasFee",
            "route.estimate.feeCosts.0.amountUSD" to "bridgeFee",
            "route.params.slippage" to "slippage",
            "route.estimate.exchangeRate" to "exchangeRate",
            "route.estimate.toAmountUSD" to "toAmountUSD",
            "route.estimate.estimatedRouteDuration" to "estimatedRouteDuration",
            "errors" to "errors"
        )
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val modified = transform(existing, payload, keyMap)
        val payloadProcessor = SquidRoutePayloadProcessor(parser)
        modified.safeSet("requestPayload", payloadProcessor.received(null, payload))
        return modified
    }
}