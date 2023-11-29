package exchange.dydx.abacus.processor.squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.safeSet
import kotlin.math.pow

internal class SquidRouteV2Processor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "route.estimate.gasCosts.0.amountUsd" to "gasFee",
            "route.estimate.feeCosts.0.amountUSD" to "bridgeFee",
            "route.estimate.aggregateSlippage" to "slippage",
            "route.estimate.exchangeRate" to "exchangeRate",
            "route.estimate.estimatedRouteDuration" to "estimatedRouteDuration",
            "route.estimate.toAmountMin" to "toAmountMin",
            "errors" to "errors",
        )
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        val modified = transform(existing, payload, keyMap)
        val payloadProcessor = SquidRouteV2PayloadProcessor(parser)
        modified.safeSet("requestPayload", payloadProcessor.received(null, payload))
        val toAmount = parser.asLong(parser.value(payload, "route.estimate.toAmount"))
        val decimals = parser.asLong(parser.value(payload, "route.estimate.toToken.decimals"))
        if (toAmount != null && decimals != null) {
            modified.safeSet("toAmountUSD", toAmount / 10.0.pow(decimals.toDouble()))
        }
        return modified
    }
}