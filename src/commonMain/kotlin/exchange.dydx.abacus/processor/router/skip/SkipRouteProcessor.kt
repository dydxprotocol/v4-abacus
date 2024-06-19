package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.SLIPPAGE_PERCENT
import exchange.dydx.abacus.utils.safeSet
import kotlin.math.pow

@Suppress("ForbiddenComment")
internal class SkipRouteProcessor(internal val parser: ParserProtocol) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "route.usd_amount_out" to "toAmountUSD",
            "route.estimated_amount_out" to "toAmount",
            "swap_price_impact_percent" to "aggregatePriceImpact",

//            SQUID PARAMS THAT ARE NOW DEPRECATED:
//            "route.estimate.gasCosts.0.amountUSD" to "gasFee",
//            "route.estimate.exchangeRate" to "exchangeRate",
//            "route.estimate.estimatedRouteDuration" to "estimatedRouteDuration",
//            "route.estimate.toAmountMin" to "toAmountMin",

        ),
    )

    private fun findFee(payload: Map<String, Any>, key: String): Double? {
        val estimatedFees = parser.asList(parser.value(payload, "route.estimated_fees"))
        val foundFeeObj = estimatedFees?.find {
            parser.asString(parser.asNativeMap(it)?.get("fee_type")) == key
        }
        val feeInUSD = parser.asDouble(parser.asNativeMap(foundFeeObj)?.get("usd_amount"))
        return feeInUSD
    }

    fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        decimals: Double?
    ): Map<String, Any> {
        val modified = BaseProcessor(parser).transform(existing, payload, keyMap)

        var bridgeFees = findFee(payload, "BRIDGE")
//        TODO: update web UI to show smart relay fees
//        For now we're just bundling it with the bridge fees
        val smartRelayFees = findFee(payload, "SMART_RELAY")
        if (bridgeFees == null) {
            bridgeFees = smartRelayFees
        } else if (smartRelayFees != null) {
            bridgeFees += smartRelayFees
        }
        val gasFees = findFee(payload, "GAS")

        modified.safeSet("gasFees", gasFees)
        modified.safeSet("bridgeFees", bridgeFees)

        val toAmount = parser.asLong(parser.value(payload, "route.estimated_amount_out"))
        if (toAmount != null && decimals != null) {
            modified.safeSet("toAmount", toAmount / 10.0.pow(decimals))
        }
        val toAmountUSD = parser.asDouble(parser.value(payload, "route.usd_amount_out"))
        if (toAmountUSD != null) {
            modified.safeSet("toAmountUSD", toAmountUSD)
        }

//        TODO: Remove slippage.
//        This is just hard coded in our params so we're keeping it to be at parity for now
//        Fast follow squid -> skip migration project to removing max slippage
//        because we already show the actual price impact.
        modified.safeSet("slippage", SLIPPAGE_PERCENT)

        val errorCode = parser.value(payload, "code")
//        if we have an error code, add the payload as a list of errors
//        this allows to match the current errors format.
//        TODO: replace errors with errorMessage once we finish migration
        if (errorCode != null) {
            modified.safeSet("errors", parser.asString(listOf(payload)))
        } else {
//          Only bother processing payload if there's no error
            val payloadProcessor = SkipRoutePayloadProcessor(parser)
            modified.safeSet("requestPayload", payloadProcessor.received(null, payload))
        }
        return modified
    }
}
