package exchange.dydx.abacus.processor.configs

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.output.WithdrawalCapacity
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.QUANTUM_MULTIPLIER
import kotlin.math.pow

@Suppress("UNCHECKED_CAST")
internal class WithdrawalCapacityProcessor(parser: ParserProtocol) : BaseProcessor(parser) {
    override fun received(
        existing: Map<String, Any>?,
        payload: Map<String, Any>
    ): Map<String, Any>? {
        val received = existing?.toMutableMap() ?: mutableMapOf()
        parser.asList(payload["limiterCapacityList"])?.let {
            if (it.size != 2) {
                return null
            }
            var dailyLimit = parser.asDecimal(parser.asMap(it[0])?.get("capacity"))
            var weeklyLimit = parser.asDecimal(parser.asMap(it[1])?.get("capacity"))
            if (dailyLimit != null && weeklyLimit != null) {
                var capacity: BigDecimal?
                if (dailyLimit < weeklyLimit) {
                    capacity = dailyLimit
                } else {
                    capacity = weeklyLimit
                }

                //TODO: move to validator?
                val usdcDecimals = environment?.tokens?.get("usdc")?.decimals ?: 6
                capacity /= BigDecimal.fromDouble(10.0.pow(usdcDecimals))

                parser.asString(capacity)?.let { capacityString ->
                    received["capacity"] = capacityString
                }
            }
        }
        return received
    }
}
