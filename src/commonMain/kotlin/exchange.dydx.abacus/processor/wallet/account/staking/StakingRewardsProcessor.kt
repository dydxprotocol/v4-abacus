package exchange.dydx.abacus.processor.wallet.account.staking

import exchange.dydx.abacus.output.account.AccountBalance
import exchange.dydx.abacus.output.account.StakingRewards
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IMutableList
import indexer.models.chain.OnChainStakingRewardsResponse
import kollections.iMutableListOf

internal interface StakingRewardsProcessorProtocol {
    fun process(
        existing: StakingRewards?,
        payload: OnChainStakingRewardsResponse?,
    ): StakingRewards?
}

internal class StakingRewardsProcessor(
    parser: ParserProtocol
) : BaseProcessor(parser), StakingRewardsProcessorProtocol {

    override fun process(
        existing: StakingRewards?,
        payload: OnChainStakingRewardsResponse?,
    ): StakingRewards? {
        if (payload == null) {
            return null
        }

        val rewards = payload.rewards ?: emptyList()
        val total = payload.total ?: emptyList()
        val validators: IMutableList<String> = iMutableListOf()
        val totalRewards: IMutableList<AccountBalance> = iMutableListOf()
        for (validator in rewards) {
            val validatorAddress = validator.validatorAddress ?: continue
            validators.add(validatorAddress)
        }
        for (reward in total) {
            val denom = reward.denom ?: continue
            val amount = reward.amount ?: continue
            totalRewards.add(AccountBalance(denom, amount))
        }
        return StakingRewards(validators, totalRewards)
    }
}
