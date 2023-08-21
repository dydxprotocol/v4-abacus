package exchange.dydx.abacus.processor.configs

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.iMapOf
import kollections.toIMap

@Suppress("UNCHECKED_CAST")
internal class FeeTierProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val feeTierKeyMap = iMapOf(
        "string" to iMapOf(
            "tier" to "tier",
            "name" to "tier",
            "symbol" to "symbol"
        ),
        "double" to iMapOf(
            "maker" to "maker",
            "taker" to "taker"
        ),
        "int" to iMapOf(
            "volume" to "volume"
        ).toIMap()
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any>? {
        val received = transform(existing, payload, feeTierKeyMap)
        var absoluteVolumeRequirement = parser.asDecimal(payload["absoluteVolumeRequirement"])
        if (absoluteVolumeRequirement == null) {
            val absoluteVolumeRequirementStruct = parser.asMap(payload["absoluteVolumeRequirement"])
            if (absoluteVolumeRequirementStruct != null) {
                absoluteVolumeRequirement = constructVolume(absoluteVolumeRequirementStruct)
            }
        }
        if (absoluteVolumeRequirement != null) {
            received["volume"] = absoluteVolumeRequirement
            received["symbol"] = "≥"

            val makerFeePpm = parser.asDecimal(payload["makerFeePpm"])
            if (makerFeePpm != null) {
                received["maker"] = makerFeePpm / 1000000
            }
            val takerFeePpm = parser.asDecimal(payload["takerFeePpm"])
            if (takerFeePpm != null) {
                received["taker"] = takerFeePpm / 1000000
            }
            val totalVolumeShareRequirementPpm =
                parser.asDecimal(payload["totalVolumeShareRequirementPpm"])
            if (totalVolumeShareRequirementPpm != null) {
                received["totalShare"] = totalVolumeShareRequirementPpm / 1000000
            }
            val makerVolumeShareRequirementPpm =
                parser.asDecimal(payload["makerVolumeShareRequirementPpm"])
            if (makerVolumeShareRequirementPpm != null) {
                received["makerShare"] = makerVolumeShareRequirementPpm / 1000000
            }
        }

        val tier = received["tier"]
        if (tier != null) {
            received["id"] = tier
            received["resources"] = iMapOf("stringKey" to "FEE_TIER.$tier")
        }
        return received
    }

    fun constructVolume(absoluteVolumeRequirement: IMap<String, Any>): BigDecimal {
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