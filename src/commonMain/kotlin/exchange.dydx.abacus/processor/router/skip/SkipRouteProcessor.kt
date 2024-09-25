package exchange.dydx.abacus.processor.router.skip

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.StatsigConfig
import exchange.dydx.abacus.utils.SLIPPAGE_PERCENT
import exchange.dydx.abacus.utils.safeSet
import exchange.dydx.abacus.utils.toJson
import exchange.dydx.abacus.utils.toJsonArray
import kotlin.math.pow

@Suppress("ForbiddenComment")
internal class SkipRouteProcessor(internal val parser: ParserProtocol) {
    private val keyMap = mapOf(
        "string" to mapOf(
            "route.usd_amount_out" to "toAmountUSD",
            "route.estimated_amount_out" to "toAmount",
            "route.swap_price_impact_percent" to "aggregatePriceImpact",
            "route.warning" to "warning",
            "route.estimated_route_duration_seconds" to "estimatedRouteDurationSeconds",
//            SQUID PARAMS THAT ARE NOW DEPRECATED:
//            "route.estimate.gasCosts.0.amountUSD" to "gasFee",
//            "route.estimate.exchangeRate" to "exchangeRate",
//            "route.estimate.estimatedRouteDuration" to "estimatedRouteDuration",
//            "route.estimate.toAmountMin" to "toAmountMin",

        ),
    )

    private val transactionTypes = listOf(
        "transfer",
        "axelar_transfer",
        "hyperlane_transfer",
    )

    private fun findFee(payload: Map<String, Any>, key: String): Double? {
        val estimatedFees = parser.asList(parser.value(payload, "route.estimated_fees"))
        val foundFeeObj = estimatedFees?.find {
            parser.asString(parser.asNativeMap(it)?.get("fee_type")) == key
        }
        val feeInUSD = parser.asDouble(parser.asNativeMap(foundFeeObj)?.get("usd_amount"))
        return feeInUSD
    }

    private fun calculateFeesFromSwaps(payload: Map<String, Any>): Double {
        val operations = parser.asList(parser.value(payload, "route.operations")) ?: return 0.0
        var total = 0.0
        operations.forEach { operation ->
            val fee = getFeeFromOperation(parser.asNativeMap(operation))
            total += fee
        }
        return total
    }

    private fun getFeeFromOperation(operationPayload: Map<String, Any>?): Double {
        if (operationPayload == null) return 0.0
        var fee = 0.0
        transactionTypes.forEach { transactionType ->
            val transaction = parser.asNativeMap(operationPayload.get(transactionType))
            if (transaction != null) {
                val usdFeeAmount = parser.asDouble(transaction.get("usd_fee_amount")) ?: 0.0
                fee += usdFeeAmount
            }
        }
        return fee
    }

    fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>,
        decimals: Double?
    ): Map<String, Any> {
        val modified = BaseProcessor(parser).transform(existing, payload, keyMap)

        modified.safeSet("estimatedRouteDurationSeconds", parser.value(payload, "route.estimated_route_duration_seconds"))

        var bridgeFees = findFee(payload, "BRIDGE") ?: 0.0
//        TODO: update web UI to show smart relay fees
//        For now we're just bundling it with the bridge fees
        val smartRelayFees = findFee(payload, "SMART_RELAY") ?: 0.0
        bridgeFees += smartRelayFees
        bridgeFees += calculateFeesFromSwaps(payload)

        val gasFees = findFee(payload, "GAS")

        modified.safeSet("gasFee", gasFees)
        modified.safeSet("bridgeFee", bridgeFees)

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
            modified.safeSet("errors", parser.asString(listOf(payload).toJsonArray()))
        } else {
//          Only bother processing payload if there's no error
            val payloadProcessor = SkipRoutePayloadProcessor(parser)
            modified.safeSet("requestPayload", payloadProcessor.received(null, payload))
        }

        if (modified.get("warning") == null && bridgeFees > StatsigConfig.dc_max_safe_bridge_fees) {
            val fromAmountUSD = parser.asString(parser.value(payload, "route.usd_amount_in"))
            modified.safeSet(
                "warning",
                mapOf(
                    "type" to "BAD_PRICE_WARNING",
                    "message" to "Difference in USD value of route input and output is large ($bridgeFees). Input USD Value: $fromAmountUSD Output USD value: $toAmountUSD",
                ).toJson(),
            )
        }
        return modified
    }
}
