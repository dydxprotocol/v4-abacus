package exchange.dydx.abacus.processor.wallet.user

import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER

@Suppress("UNCHECKED_CAST")
internal class UserProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    private val userKeyMap = iMapOf(
        "double" to iMapOf(
            "makerFeeRate" to "makerFeeRate",
            "takerFeeRate" to "takerFeeRate",
            "makerVolume30D" to "makerVolume30D",
            "takerVolume30D" to "takerVolume30D",
            "fees30D" to "fees30D"
        )
    )

    private val OnChainUserFeeTierKeyMap = iMapOf(
        "double" to iMapOf(
            "makerFeePpm" to "makerFeePpm",
            "takerFeePpm" to "takerFeePpm"
        ),
        "string" to iMapOf(
            "name" to "feeTierId"
        ),
    )

    private val OnChainUserFeeStatsKeyMap = iMapOf(
        "double" to iMapOf(
            "makerNotional" to "makerNotional",
            "takerNotional" to "takerNotional"
        )
    )

    override fun received(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        return transform(existing, payload, userKeyMap)
    }


    fun receivedOnChainUserFeeTier(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val received = transform(existing, payload, OnChainUserFeeTierKeyMap)
        val makerFeePpm = parser.asDecimal(received["makerFeePpm"])
        if (makerFeePpm != null) {
            received["makerFeeRate"] = makerFeePpm / QUANTUM_MULTIPLIER
        }
        val takerFeePpm = parser.asDecimal(received["takerFeePpm"])
        if (takerFeePpm != null) {
            received["takerFeeRate"] = takerFeePpm / QUANTUM_MULTIPLIER
        }

        return received
    }

    fun receivedOnChainUserStats(
        existing: IMap<String, Any>?,
        payload: IMap<String, Any>
    ): IMap<String, Any> {
        val received = transform(existing, payload, OnChainUserFeeStatsKeyMap)
        val makerNotional = parser.asDecimal(received["makerNotional"])
        if (makerNotional != null) {
            received["makerVolume30D"] = makerNotional / QUANTUM_MULTIPLIER
        }
        val takerNotional = parser.asDecimal(received["takerNotional"])
        if (takerNotional != null) {
            received["takerVolume30D"] = takerNotional / QUANTUM_MULTIPLIER
        }
        return received
    }
}