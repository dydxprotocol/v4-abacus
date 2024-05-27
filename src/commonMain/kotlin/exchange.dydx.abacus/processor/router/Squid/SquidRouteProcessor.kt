package exchange.dydx.abacus.processor.router.Squid

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.safeSet
import kotlin.math.pow

internal class SquidRouteProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "route.estimate.gasCosts.0.amountUSD" to "gasFee",
            "route.params.slippage" to "slippage",
            "route.estimate.exchangeRate" to "exchangeRate",
            "route.estimate.toAmountUSD" to "toAmountUSD",
            "route.estimate.toAmount" to "toAmount",
            "route.estimate.estimatedRouteDuration" to "estimatedRouteDuration",
            "route.estimate.toAmountMin" to "toAmountMin",
            "route.estimate.aggregatePriceImpact" to "aggregatePriceImpact",
            "errors" to "errors",
        ),
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any> {
        var bridgeFees = 0.0
        parser.asList(parser.value(payload, "route.estimate.feeCosts"))?.map {
            val feeCost = parser.asNativeMap(it)
            if (feeCost !== null) {
                val amountUsd = parser.asDouble(feeCost["amountUSD"])
                if (amountUsd !== null) {
                    bridgeFees += amountUsd
                }
            }
        }

        val modified = transform(existing, payload, keyMap)

        val decimals = parser.asDouble(parser.value(payload, "route.params.toToken.decimals"))
        val toAmount = parser.asLong(parser.value(payload, "route.estimate.toAmount"))
        if (toAmount != null && decimals != null) {
            modified.safeSet("toAmount", toAmount / 10.0.pow(decimals))
        }
        val toAmountMin = parser.asLong(parser.value(payload, "route.estimate.toAmountMin"))
        if (toAmountMin != null && decimals != null) {
            modified.safeSet("toAmountMin", toAmountMin / 10.0.pow(decimals))
        }

        val toAmountUSD = parser.asLong(parser.value(payload, "route.estimate.toAmountUSD"))
        val udscDecimals = 6
        if (toAmountUSD != null) {
            modified.safeSet("toAmountUSD", toAmountUSD / 10.0.pow(udscDecimals.toDouble()))
        }

        val payloadProcessor = SquidRoutePayloadProcessor(parser)
        modified.safeSet("requestPayload", payloadProcessor.received(null, payload))
        modified.safeSet("bridgeFee", bridgeFees)
        return modified
    }
}
