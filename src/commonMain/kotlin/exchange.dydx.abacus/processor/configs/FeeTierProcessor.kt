package exchange.dydx.abacus.processor.configs

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER

@Suppress("UNCHECKED_CAST")
internal class FeeTierProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val feeTierKeyMap = mapOf(
        "string" to mapOf(
            "tier" to "tier",
            "name" to "tier",
            "symbol" to "symbol"
        ),
        "double" to mapOf(
            "maker" to "maker",
            "taker" to "taker"
        ),
        "int" to mapOf(
            "volume" to "volume"
        ).toMap()
    )

    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val received = transform(existing, payload, feeTierKeyMap)
        val absoluteVolumeRequirement = parser.asDecimal(payload["absoluteVolumeRequirement"])
        if (absoluteVolumeRequirement != null) {
            received["symbol"] = "≥"
            received["volume"] = absoluteVolumeRequirement / QUANTUM_MULTIPLIER

            val makerFeePpm = parser.asDecimal(payload["makerFeePpm"])
            if (makerFeePpm != null) {
                received["maker"] = makerFeePpm / QUANTUM_MULTIPLIER
            }
            val takerFeePpm = parser.asDecimal(payload["takerFeePpm"])
            if (takerFeePpm != null) {
                received["taker"] = takerFeePpm / QUANTUM_MULTIPLIER
            }
            val totalVolumeShareRequirementPpm =
                parser.asDecimal(payload["totalVolumeShareRequirementPpm"])
            if (totalVolumeShareRequirementPpm != null) {
                received["totalShare"] = totalVolumeShareRequirementPpm / QUANTUM_MULTIPLIER
            }
            val makerVolumeShareRequirementPpm =
                parser.asDecimal(payload["makerVolumeShareRequirementPpm"])
            if (makerVolumeShareRequirementPpm != null) {
                received["makerShare"] = makerVolumeShareRequirementPpm / QUANTUM_MULTIPLIER
            }
        }

        val tier = received["tier"]
        if (tier != null) {
            received["id"] = tier
            received["resources"] = mapOf("stringKey" to "FEE_TIER.$tier")
        }
        return received
    }

    fun constructVolume(absoluteVolumeRequirement: Map<String, Any>): BigDecimal {
        val low = parser.asInt(absoluteVolumeRequirement["low"]) ?: 0
        val high = parser.asInt(absoluteVolumeRequirement["high"]) ?: 0
        val unsigned = parser.asBool(absoluteVolumeRequirement["unsigned"]) ?: false
        return constructDecimalFromLong(low, high, !unsigned)
    }

    fun constructDecimalFromLong(low: Int, high: Int, signed: Boolean): BigDecimal {
        return if (signed) {
            val value = (high.toLong() shl 32) or low.toLong()
            parser.asDecimal(value) ?: Numeric.decimal.ZERO
        } else {
            val negative = (high and 0x80000000.toInt()) != 0
            val value = ((high and 0x7FFFFFFF).toLong() shl 32) or low.toLong()
            (parser.asDecimal(value) ?: Numeric.decimal.ZERO) * if (negative) -1 else 1
        }
    }
}