package exchange.dydx.abacus.processor.configs

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.base.BaseProcessorProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalWithdrawalCapacityState
import indexer.models.chain.OnChainWithdrawalCapacityResponse
import kotlin.math.pow

internal interface WithdrawalCapacityProcessorProtocol : BaseProcessorProtocol {
    fun process(
        payload: OnChainWithdrawalCapacityResponse?
    ): InternalWithdrawalCapacityState?
}

internal class WithdrawalCapacityProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), WithdrawalCapacityProcessorProtocol {
    override fun process(
        payload: OnChainWithdrawalCapacityResponse?
    ): InternalWithdrawalCapacityState? {
        val maxCapacity: BigDecimal? = if (payload?.limiterCapacityList?.size == 2) {
            val dailyLimit = parser.asDecimal(payload.limiterCapacityList[0].capacity)
            val weeklyLimit = parser.asDecimal(payload.limiterCapacityList[1].capacity)
            if (dailyLimit != null && weeklyLimit != null) {
                if (dailyLimit < weeklyLimit) {
                    dailyLimit
                } else {
                    weeklyLimit
                }
            } else {
                null
            }
        } else {
            null
        }

        return if (maxCapacity != null) {
            val usdcDecimals = environment?.tokens?.get("usdc")?.decimals ?: 6
            val maxWithdrawalCapacity = maxCapacity / BigDecimal.fromDouble(10.0.pow(usdcDecimals))
            InternalWithdrawalCapacityState(
                capacity = parser.asString(maxCapacity),
                maxWithdrawalCapacity = maxWithdrawalCapacity,
            )
        } else {
            null
        }
    }
}
