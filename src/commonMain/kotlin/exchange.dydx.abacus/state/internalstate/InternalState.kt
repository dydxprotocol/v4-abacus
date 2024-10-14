package exchange.dydx.abacus.state.internalstate

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.functional.vault.ThirtyDayPnl
import exchange.dydx.abacus.functional.vault.VaultDetails
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
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInputOptions
import exchange.dydx.abacus.output.input.AdjustIsolatedMarginInputSummary
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.IsolatedMarginAdjustmentType
import exchange.dydx.abacus.output.input.IsolatedMarginInputType
import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.Tooltip
import exchange.dydx.abacus.output.input.TradeInputBracket
import exchange.dydx.abacus.output.input.TradeInputBracketSide
import exchange.dydx.abacus.output.input.TradeInputGoodUntil
import exchange.dydx.abacus.output.input.TradeInputMarketOrder
import exchange.dydx.abacus.output.input.TradeInputPrice
import exchange.dydx.abacus.output.input.TradeInputSize
import exchange.dydx.abacus.output.input.TriggerOrderInputSummary
import exchange.dydx.abacus.output.input.TriggerPrice
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import indexer.codegen.IndexerHistoricalBlockTradingReward
import indexer.codegen.IndexerHistoricalTradingRewardAggregation
import indexer.codegen.IndexerPerpetualPositionStatus
import indexer.codegen.IndexerPositionSide
import indexer.codegen.IndexerTransferBetweenResponse
import indexer.models.chain.OnChainAccountVaultResponse
import kotlinx.datetime.Instant

internal data class InternalState(
    var assets: MutableMap<String, Asset> = mutableMapOf(),
    val wallet: InternalWalletState = InternalWalletState(),
    var rewardsParams: InternalRewardsParamsState? = null,
    val launchIncentive: InternalLaunchIncentiveState = InternalLaunchIncentiveState(),
    val configs: InternalConfigsState = InternalConfigsState(),
    val marketsSummary: InternalMarketSummaryState = InternalMarketSummaryState(),
    val input: InternalInputState = InternalInputState(),
    var vault: InternalVaultState? = null,
)

internal data class InternalInputState(
    var trade: InternalTradeInputState = InternalTradeInputState(),
    var closePosition: InternalTradeInputState = InternalTradeInputState(),
    var triggerOrders: InternalTriggerOrdersInputState = InternalTriggerOrdersInputState(),
    var adjustIsolatedMargin: InternalAdjustIsolatedMarginInputState = InternalAdjustIsolatedMarginInputState(),
    var transfer: InternalTransferInputState = InternalTransferInputState(),
    var receiptLines: List<ReceiptLine>? = null,
    var errors: List<ValidationError>? = null,
    var childSubaccountErrors: List<ValidationError>? = null,
) {
    var currentType: InputType? = null
        set(value) {
            if (field != value) {
                receiptLines = null
                errors = null
                childSubaccountErrors = null
                field = value
            }
        }
}

internal data class InternalAdjustIsolatedMarginInputState(
    var market: String? = null,
    var type: IsolatedMarginAdjustmentType? = null,
    var amount: Double? = null,
    var amountPercent: Double? = null,
    var amountInput: IsolatedMarginInputType? = null,
    var childSubaccountNumber: Int? = null,
    var parentSubaccountNumber: Int? = null,
    var options: AdjustIsolatedMarginInputOptions? = null,
    var summary: AdjustIsolatedMarginInputSummary? = null,
)

internal data class InternalTriggerOrdersInputState(
    var marketId: String? = null,
    var size: Double? = null,
    var stopLossOrder: InternalTriggerOrderState? = null,
    var takeProfitOrder: InternalTriggerOrderState? = null,
)

internal data class InternalTriggerOrderState(
    var orderId: String? = null,
    var size: Double? = null,
    var type: OrderType? = null,
    var side: OrderSide? = null,
    var price: TriggerPrice? = null,
    var summary: TriggerOrderInputSummary? = null,
)

internal data class InternalTradeInputState(
    var marketId: String? = null,
    var size: TradeInputSize? = null,
    var sizePercent: Double? = null,
    var price: TradeInputPrice? = null,
    var type: OrderType? = null,
    var side: OrderSide? = null,
    var marginMode: MarginMode? = null,
    var targetLeverage: Double? = null,
    var timeInForce: String? = null,
    var goodTil: TradeInputGoodUntil? = null,
    var execution: String? = null,
    var reduceOnly: Boolean = false,
    var postOnly: Boolean = false,
    var fee: Double? = null,
    var brackets: TradeInputBracket? = null,
    var options: InternalTradeInputOptions = InternalTradeInputOptions(),
    var marketOrder: TradeInputMarketOrder? = null,
    var summary: InternalTradeInputSummary? = null,
) {
    val isBuying: Boolean
        get() = side == OrderSide.Buy || side == null
}

internal data class InternalTradeInputSummary(
    val price: Double?,
    val payloadPrice: Double?,
    val size: Double?,
    val usdcSize: Double?,
    val slippage: Double?,
    val fee: Double?,
    val total: Double?,
    val reward: Double?,
    val filled: Boolean,
    val positionMargin: Double?,
    val positionLeverage: Double?,
    val indexSlippage: Double?,
    val feeRate: Double?,
)

internal data class InternalTradeInputOptions(
    var needsMarginMode: Boolean = false,
    var needsSize: Boolean = false,
    var needsLeverage: Boolean = false,
    var needsBalancePercent: Boolean = false,
    var maxLeverage: Double? = null,
    var needsLimitPrice: Boolean = false,
    var needsTargetLeverage: Boolean = false,
    var needsTriggerPrice: Boolean = false,
    var needsTrailingPercent: Boolean = false,
    var needsGoodUntil: Boolean = false,
    var needsReduceOnly: Boolean = false,
    var needsPostOnly: Boolean = false,
    var needsBrackets: Boolean = false,
    var sideOptions: List<SelectionOption>? = null,
    var orderTypeOptions: List<SelectionOption>? = null,
    var timeInForceOptions: List<SelectionOption>? = null,
    var executionOptions: List<SelectionOption>? = null,
    var marginModeOptions: List<SelectionOption>? = null,
    var goodTilUnitOptions: List<SelectionOption>? = null,
    var reduceOnlyTooltip: Tooltip? = null,
    var postOnlyTooltip: Tooltip? = null,
)

internal data class InternalMarketSummaryState(
    var markets: MutableMap<String, InternalMarketState> = mutableMapOf(),

    var volume24HUSDC: Double? = null,
    var openInterestUSDC: Double? = null,
    var trades24H: Double? = null,
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
) {
    val isWalletConnected: Boolean
        get() = walletAddress != null

    val isAccountConnected: Boolean
        get() = account.subaccounts != null
}

internal data class InternalVaultState(
    val details: VaultDetails? = null,
    val positions: List<InternalVaultPositionState>? = null,
    val pnls: MutableMap<String, ThirtyDayPnl> = mutableMapOf(),
    val transfers: IndexerTransferBetweenResponse? = null,
    val account: OnChainAccountVaultResponse? = null,
)

internal data class InternalVaultPositionState(
    var openPosition: InternalPerpetualPosition? = null,
    var assetPosition: InternalAssetPositionState? = null,
    var equity: Double? = null,
    var ticker: String? = null,
)

internal data class InternalUserState(
    var feeTierId: String? = null,
    var makerFeeRate: Double? = null,
    var takerFeeRate: Double? = null,
    var makerVolume30D: Double? = null,
    var takerVolume30D: Double? = null,

    var restricted: Boolean = false, // TODO: Not being used
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

    // calculated: For parent subaccount, this contains the calculated values of all child subaccounts
    var openPositions: Map<String, InternalPerpetualPosition>? = null,

    // Calculated:
    val calculated: MutableMap<CalculationPeriod, InternalSubaccountCalculated> = mutableMapOf(),
) {
    val isParentSubaccount: Boolean
        get() = subaccountNumber < NUM_PARENT_SUBACCOUNTS

    fun deepCopy(): InternalSubaccountState {
        return InternalSubaccountState(
            fills = fills?.map { it.copy() },
            orders = orders?.map { it.copy() },
            transfers = transfers?.map { it.copy() },
            historicalPNLs = historicalPNLs?.map { it.copy() },
            positions = positions?.map { it.key to it.value.copy() }?.toMap(),
            assetPositions = assetPositions?.map { it.key to it.value.copy() }?.toMap(),
            subaccountNumber = subaccountNumber,
            address = address,
            equity = equity,
            freeCollateral = freeCollateral,
            marginEnabled = marginEnabled,
            updatedAtHeight = updatedAtHeight,
            latestProcessedBlockHeight = latestProcessedBlockHeight,
            pendingPositions = pendingPositions?.map { it.copy() },
            openPositions = openPositions?.map { it.key to it.value.copy() }?.toMap(),
            calculated = calculated.map { it.key to it.value.copy() }.toMap().toMutableMap(),
        )
    }
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
    var displayId: String? = null,
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
    var childSubaccountNumber: Int? = null,
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

internal fun TradeInputSize.Companion.safeCreate(existing: TradeInputSize?): TradeInputSize {
    return existing ?: TradeInputSize(
        size = null,
        usdcSize = null,
        leverage = null,
        balancePercent = null,
        input = null,
    )
}

internal fun TradeInputBracket.Companion.safeCreate(existing: TradeInputBracket?): TradeInputBracket {
    return existing ?: TradeInputBracket(
        stopLoss = null,
        takeProfit = null,
        goodTil = null,
        execution = null,
    )
}

internal fun TradeInputPrice.Companion.safeCreate(existing: TradeInputPrice?): TradeInputPrice {
    return existing ?: TradeInputPrice(
        limitPrice = null,
        triggerPrice = null,
        trailingPercent = null,
    )
}

internal fun TradeInputBracketSide.Companion.safeCreate(existing: TradeInputBracketSide?): TradeInputBracketSide {
    return existing ?: TradeInputBracketSide(
        triggerPrice = null,
        percent = null,
        reduceOnly = false,
    )
}

internal fun TradeInputGoodUntil.Companion.safeCreate(existing: TradeInputGoodUntil?): TradeInputGoodUntil {
    return existing ?: TradeInputGoodUntil(
        duration = null,
        unit = null,
    )
}

internal fun TriggerPrice.Companion.safeCreate(existing: TriggerPrice?): TriggerPrice {
    return existing ?: TriggerPrice(
        limitPrice = null,
        triggerPrice = null,
        percentDiff = null,
        usdcDiff = null,
        input = null,
    )
}
