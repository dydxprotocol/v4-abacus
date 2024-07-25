package exchange.dydx.abacus.processor.wallet.account.staking

import exchange.dydx.abacus.output.account.AccountBalance
import exchange.dydx.abacus.output.account.StakingRewards
import exchange.dydx.abacus.utils.Parser
import indexer.models.chain.OnChainStakingReward
import indexer.models.chain.OnChainStakingRewardAmount
import indexer.models.chain.OnChainStakingRewardsResponse
import kollections.toIList
import kotlin.test.Test
import kotlin.test.assertEquals

class StakingRewardsProcessorTests {
    companion object {
        val payloadMock = OnChainStakingRewardsResponse(
            rewards = listOf(
                OnChainStakingReward(
                    validatorAddress = "validatorAddress1",
                    reward = listOf(
                        OnChainStakingRewardAmount(
                            denom = "denom1",
                            amount = "100.0",
                        ),
                    ),
                ),
                OnChainStakingReward(
                    validatorAddress = "validatorAddress2",
                    reward = listOf(
                        OnChainStakingRewardAmount(
                            denom = "denom1",
                            amount = "100.0",
                        ),
                    ),
                ),
            ),
            total = listOf(
                OnChainStakingRewardAmount(
                    denom = "denom1",
                    amount = "100.0",
                ),
                OnChainStakingRewardAmount(
                    denom = "denom2",
                    amount = "200.0",
                ),
            ),
        )

        val rewardsMock = StakingRewards(
            validators = listOf("validatorAddress1", "validatorAddress2").toIList(),
            totalRewards = listOf(
                AccountBalance("denom1", "100.0"),
                AccountBalance("denom2", "200.0"),
            ).toIList(),
        )
    }

    private val processor = StakingRewardsProcessor(
        parser = Parser(),
    )

    @Test
    fun testProcess() {
        val result = processor.process(null, payloadMock)
        assertEquals(rewardsMock, result)
    }
}
