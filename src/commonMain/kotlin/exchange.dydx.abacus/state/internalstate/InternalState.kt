package exchange.dydx.abacus.state.internalstate

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.LaunchIncentiveSeason
import exchange.dydx.abacus.output.SubaccountFill
import exchange.dydx.abacus.output.SubaccountHistoricalPNL
import exchange.dydx.abacus.output.SubaccountOrder

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
    var balances: Map<String, InternalAccountBalanceState>? = null,
    var stakingBalances: Map<String, InternalAccountBalanceState>? = null,
    var stakingDelegations: List<InternalStakingDelegationState>? = null,
    var subaccounts: MutableMap<Int, InternalSubaccountState> = mutableMapOf(),
    var groupedSubaccounts: MutableMap<Int, InternalSubaccountState> = mutableMapOf(),
)

internal data class InternalSubaccountState(
    var fills: List<SubaccountFill>? = null,
    var orders: List<SubaccountOrder>? = null,
    var historicalPNLs: List<SubaccountHistoricalPNL>? = null,
    var subaccountNumber: Int,
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
