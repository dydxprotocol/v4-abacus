package exchange.dydx.abacus.state.internalstate

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.EquityTiers
import exchange.dydx.abacus.output.FeeTier
import exchange.dydx.abacus.output.LaunchIncentivePoint
import exchange.dydx.abacus.output.LaunchIncentiveSeason
import exchange.dydx.abacus.output.MarketCandle
import exchange.dydx.abacus.output.MarketOrderbook
import exchange.dydx.abacus.output.MarketTrade
import exchange.dydx.abacus.output.PerpetualMarket
import exchange.dydx.abacus.output.WithdrawalGating
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
    val configs: InternalConfigsState = InternalConfigsState(),
    val marketsSummary: InternalMarketSummaryState = InternalMarketSummaryState(),
)

internal data class InternalMarketSummaryState(
    var markets: MutableMap<String, InternalMarketState> = mutableMapOf(),
)

internal data class InternalMarketState(
    // recent trades
    var trades: List<MarketTrade>? = null,

    // market details
    var perpetualMarket: PerpetualMarket? = null,

    // raw orderbook
    var rawOrderbook: InternalOrderbook? = null,

    //  orderbook for trade calculations
    var consolidatedOrderbook: InternalOrderbook? = null,

    // grouped orderbook for app display
    var groupedOrderbook: MarketOrderbook? = null,

    // candles: resolution -> candles
    var candles: MutableMap<String, List<MarketCandle>>? = null
)

internal data class InternalOrderbook(
    val asks: List<InternalOrderbookTick>? = null,
    val bids: List<InternalOrderbookTick>? = null,
)

internal data class InternalOrderbookTick(
    val price: Double,
    val size: Double,
)

internal data class InternalConfigsState(
    var equityTiers: EquityTiers? = null,
    var feeTiers: List<FeeTier>? = null,
    var withdrawalGating: WithdrawalGating? = null,
    var withdrawalCapacity: InternalWithdrawalCapacityState? = null,
)

internal data class InternalWithdrawalCapacityState(
    val capacity: String? = null,
    val maxWithdrawalCapacity: BigDecimal? = null,
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
    var equity: Double? = null,
    var freeCollateral: Double? = null,
    var marginEnabled: Boolean? = null,
    var updatedAtHeight: String? = null,
    var latestProcessedBlockHeight: String? = null,

    var pendingPositions: List<InternalPerpetualPendingPosition>? = null,

    // for parent subaccount only.  This contains the consolidated open positions of all child subaccounts
    var childSubaccountOpenPositions: Map<String, InternalPerpetualPosition>? = null,

    // Calculated:
    val calculated: MutableMap<CalculationPeriod, InternalSubaccountCalculated> = mutableMapOf(),
) {
    val isParentSubaccount: Boolean
        get() = subaccountNumber < NUM_PARENT_SUBACCOUNTS

    val openPositions: Map<String, InternalPerpetualPosition>?
        get() = positions?.filterValues { it.status == IndexerPerpetualPositionStatus.OPEN }

    val groupedOpenPositions: Map<String, InternalPerpetualPosition>?
        get() = if (isParentSubaccount) childSubaccountOpenPositions else openPositions
}

internal data class InternalSubaccountCalculated(
    var quoteBalance: Double? = null,
    var notionalTotal: Double? = null,
    var valueTotal: Double? = null,
    var initialRiskTotal: Double? = null,
    var equity: Double? = null,
    var freeCollateral: Double? = null,
    var leverage: Double? = null,
    var marginUsage: Double? = null,
    var buyingPower: Double? = null,
)

internal data class InternalAssetPositionState(
    val symbol: String? = null,
    val side: PositionSide? = null,
    val assetId: String? = null,
    val size: Double? = null,
    val subaccountNumber: Int? = null,
)

internal data class InternalPerpetualPendingPosition(
    val assetId: String? = null,
    val marketId: String? = null,
    val firstOrderId: String? = null,
    val orderCount: Int? = null,

    // calculated
    val calculated: MutableMap<CalculationPeriod, InternalPendingPositionCalculated> = mutableMapOf(),
)

internal data class InternalPendingPositionCalculated(
    val quoteBalance: Double? = null,
    val freeCollateral: Double? = null,
    val equity: Double? = null,
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

    // Calculated:
    val calculated: MutableMap<CalculationPeriod, InternalPositionCalculated> = mutableMapOf(),
) {
    val marginMode: MarginMode?
        get() {
            return if (subaccountNumber != null) {
                if (subaccountNumber >= NUM_PARENT_SUBACCOUNTS) {
                    MarginMode.Isolated
                } else {
                    MarginMode.Cross
                }
            } else {
                null
            }
        }
}

internal data class InternalPositionCalculated(
    var valueTotal: Double? = null,
    var notionalTotal: Double? = null,
    var adjustedImf: Double? = null,
    var adjustedMmf: Double? = null,
    var initialRiskTotal: Double? = null,
    var maxLeverage: Double? = null,
    var unrealizedPnl: Double? = null,
    var unrealizedPnlPercent: Double? = null,
    var marginValue: Double? = null,
    var realizedPnlPercent: Double? = null,
    var leverage: Double? = null,
    var size: Double? = null,
    var liquidationPrice: Double? = null,
    var buyingPower: Double? = null,
)

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
