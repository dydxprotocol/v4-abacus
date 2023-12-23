package exchange.dydx.abacus.processor.squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.safeSet

internal class SquidRouteProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "route.estimate.gasCosts.0.amountUSD" to "gasFee",
            "route.params.slippage" to "slippage",
            "route.estimate.exchangeRate" to "exchangeRate",
            "route.estimate.toAmountUSD" to "toAmountUSD",
            "route.estimate.estimatedRouteDuration" to "estimatedRouteDuration",
            "route.estimate.toAmountMin" to "toAmountMin",
            "route.estimate.toAmount" to "toAmount",
            "route.estimate.aggregatePriceImpact" to "aggregatePriceImpact",
            "errors" to "errors"
        )
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        var bridgeFees = 0.0
        parser.asList(parser.value(payload,"route.estimate.feeCosts"))?.map {
            val feeCost = parser.asNativeMap(it)
            if (feeCost !== null) {
                val amountUsd = parser.asDouble(feeCost["amountUSD"])
                if (amountUsd !== null) {
                    bridgeFees += amountUsd
                }
            }
        }

        val modified = transform(existing, payload, keyMap)
        val payloadProcessor = SquidRoutePayloadProcessor(parser)
        modified.safeSet("requestPayload", payloadProcessor.received(null, payload))
        modified.safeSet("bridgeFee", bridgeFees)
        return modified
    }
}