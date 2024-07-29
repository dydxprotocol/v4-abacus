package exchange.dydx.abacus.state.internalstate

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.LaunchIncentivePoint
import exchange.dydx.abacus.output.LaunchIncentiveSeason
import exchange.dydx.abacus.output.account.PositionSide
import exchange.dydx.abacus.output.account.StakingRewards
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.output.account.SubaccountOrder
import exchange.dydx.abacus.output.account.SubaccountPositionResources
import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.output.account.UnbondingDelegation
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import indexer.codegen.IndexerHistoricalBlockTradingReward
import indexer.codegen.IndexerHistoricalTradingRewardAggregation
import indexer.codegen.IndexerPerpetualPositionStatus
import indexer.codegen.IndexerPositionSide
import kotlinx.datetime.Instant

internal data class InternalState(
    var assets: MutableMap<String, Asset> = mutableMapOf(),
    val transfer: InternalTransferInputState = InternalTransferInputState(),
    val wallet: InternalWalletState = InternalWalletState(),
    var rewardsParams: InternalRewardsParamsState? = null,
    val launchIncentive: InternalLaunchIncentiveState = InternalLaunchIncentiveState(),
)

internal data class InternalWalletState(
    var account: InternalAccountState = InternalAccountState(),
    var user: InternalUserState? = null,
    var walletAddress: String? = null,
)

internal data class InternalUserState(
    var feeTierId: String? = null,
    var makerFeeRate: Double? = null,
    var takerFeeRate: Double? = null,
    var makerVolume30D: Double? = null,
    var takerVolume30D: Double? = null,
)

internal data class InternalAccountState(
    // token denom -> balance
    var balances: Map<String, InternalAccountBalanceState>? = null,

    // token denom -> staking balance
    var stakingBalances: Map<String, InternalAccountBalanceState>? = null,

    var stakingDelegations: List<InternalStakingDelegationState>? = null,
    var unbondingDelegation: List<UnbondingDelegation>? = null,
    var stakingRewards: StakingRewards? = null,

    // season id -> points
    var launchIncentivePoints: MutableMap<String, LaunchIncentivePoint> = mutableMapOf(),

    // subaccount number -> subaccount state
    var subaccounts: MutableMap<Int, InternalSubaccountState> = mutableMapOf(),

    // subaccount number -> subaccount state
    var groupedSubaccounts: MutableMap<Int, InternalSubaccountState> = mutableMapOf(),

    var tradingRewards: InternalTradingRewardsState = InternalTradingRewardsState(),
)

internal data class InternalTradingRewardsState(
    var historical: MutableMap<HistoricalTradingRewardsPeriod, List<IndexerHistoricalTradingRewardAggregation>> = mutableMapOf(),
    var blockRewards: MutableList<IndexerHistoricalBlockTradingReward> = mutableListOf(),
    var total: Double? = null,
)

internal data class InternalSubaccountState(
    var fills: List<SubaccountFill>? = null,
    var orders: List<SubaccountOrder>? = null,
    var transfers: List<SubaccountTransfer>? = null,
    var historicalPNLs: List<SubaccountHistoricalPNL>? = null,
    var positions: Map<String, InternalPerpetualPosition>? = null,
    var assetPositions: Map<String, InternalAssetPositionState>? = null,
    var subaccountNumber: Int,
    var address: String? = null,
    var equity: String? = null,
    var freeCollateral: String? = null,
    var marginEnabled: Boolean? = null,
    var updatedAtHeight: String? = null,
    var latestProcessedBlockHeight: String? = null,

    // Calculate:
    var quoteBalance: MutableMap<CalculationPeriod, Double?> = mutableMapOf(),

) {
    val openPositions: Map<String, InternalPerpetualPosition>?
        get() {
            return positions?.filterValues { it.status == IndexerPerpetualPositionStatus.OPEN }
        }
}

internal data class InternalAssetPositionState(
    val symbol: String? = null,
    val side: PositionSide? = null,
    val assetId: String? = null,
    val size: Double? = null,
    val subaccountNumber: Int? = null,
)

internal data class InternalPerpetualPosition(
    val market: String? = null,
    val status: IndexerPerpetualPositionStatus? = null,
    val side: IndexerPositionSide? = null,
    val size: Double? = null,
    val maxSize: Double? = null,
    val entryPrice: Double? = null,
    val realizedPnl: Double? = null,
    val createdAt: Instant? = null,
    val createdAtHeight: Double? = null,
    val sumOpen: Double? = null,
    val sumClose: Double? = null,
    val netFunding: Double? = null,
    val unrealizedPnl: Double? = null,
    val closedAt: Instant? = null,
    val exitPrice: Double? = null,
    val subaccountNumber: Int? = null,
    val resources: SubaccountPositionResources? = null,
) {
    val marginMode: MarginMode?
        get() {
            return if (subaccountNumber != null) {
                if (subaccountNumber >= NUM_PARENT_SUBACCOUNTS) {
                    MarginMode.Cross
                } else {
                    MarginMode.Isolated
                }
            } else {
                null
            }
        }
}

internal data class InternalAccountBalanceState(
    val denom: String,
    val amount: BigDecimal,
)

internal data class InternalStakingDelegationState(
    val delegatorAddress: String? = null,
    val validatorAddress: String? = null,
    val shares: BigDecimal? = null,
    val balance: InternalAccountBalanceState,
)

internal data class InternalRewardsParamsState(
    val denom: String? = null,
    val denomExponent: Double? = null,
    val marketId: Double? = null,
    val feeMultiplierPpm: Double? = null,
    val tokenPrice: Double? = null,
    val tokenExpoonent: Double? = null,
)

internal data class InternalLaunchIncentiveState(
    var seasons: List<LaunchIncentiveSeason>? = null,
)
