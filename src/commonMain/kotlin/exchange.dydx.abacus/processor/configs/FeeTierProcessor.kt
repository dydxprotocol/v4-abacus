package exchange.dydx.abacus.processor.configs

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
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
            received["resources"] = iMapOf("stringKey" to "FEE_TIER.$tier")
        }
        return received
    }
}