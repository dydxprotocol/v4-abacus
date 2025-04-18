package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.LaunchIncentivePoints
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalAccountBalanceState
import exchange.dydx.abacus.state.InternalAccountState
import exchange.dydx.abacus.state.InternalStakingDelegationState
import exchange.dydx.abacus.state.manager.TokenInfo
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import kollections.JsExport
import kollections.iMutableMapOf
import kollections.toIList
import kollections.toIMap
import kotlinx.serialization.Serializable

@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable
data class Account(
    var balances: IMap<String, AccountBalance>?,
    var stakingBalances: IMap<String, AccountBalance>?,
    var stakingDelegations: IList<StakingDelegation>?,
    var unbondingDelegation: IList<UnbondingDelegation>?,
    var stakingRewards: StakingRewards?,
    var subaccounts: IMap<String, Subaccount>?,
    var groupedSubaccounts: IMap<String, Subaccount>?,
    var tradingRewards: TradingRewards?,
    val launchIncentivePoints: LaunchIncentivePoints?,
) {
    companion object {
        internal fun create(
            existing: Account?,
            parser: ParserProtocol,
            tokensInfo: Map<String, TokenInfo>,
            internalState: InternalAccountState,
        ): Account {
            Logger.d { "creating Account\n" }

            val balances: IMutableMap<String, AccountBalance> =
                iMutableMapOf()
            internalState.balances?.forEach { (key, value) ->
                AccountBalance.create(
                    existing = existing?.balances?.get(key),
                    parser = parser,
                    decimals = findTokenInfo(tokensInfo, key)?.decimals ?: 0,
                    internalState = value,
                )?.let { balance ->
                    balances[key] = balance
                }
            }

            val stakingBalances =
                processStakingBalance(
                    existing = existing,
                    parser = parser,
                    tokensInfo = tokensInfo,
                    stakingBalances = internalState.stakingBalances,
                )

            val stakingDelegations =
                processStakingDelegations(
                    existing = existing,
                    parser = parser,
                    tokensInfo = tokensInfo,
                    stakingDelegations = internalState.stakingDelegations,
                )

            val unbondingDelegations =
                internalState.unbondingDelegation?.toIList()

            val stakingRewards =
                internalState.stakingRewards

            val tradingRewards =
                TradingRewards.create(
                    existing = existing?.tradingRewards,
                    parser = parser,
                    internalState = internalState.tradingRewards,
                )

            val launchIncentivePoints = {
                val points = internalState.launchIncentivePoints
                if (points.isNotEmpty()) {
                    LaunchIncentivePoints(points = points.toIMap())
                } else {
                    null
                }
            }()

            val subaccounts: IMutableMap<String, Subaccount> =
                iMutableMapOf()

            internalState.subaccounts.forEach { (key, value) ->
                Subaccount.create(
                    existing = existing?.subaccounts?.get(key.toString()),
                    internalState = value,
                )?.let { subaccount ->
                    subaccounts[key.toString()] = subaccount
                }
            }

            val groupedSubaccounts: IMutableMap<String, Subaccount> =
                iMutableMapOf()

            internalState.groupedSubaccounts.forEach { (key, value) ->
                Subaccount.create(
                    existing = existing?.groupedSubaccounts?.get(key.toString()),
                    internalState = value,
                )?.let { subaccount ->
                    groupedSubaccounts[key.toString()] = subaccount
                }
            }

            return Account(
                balances = balances,
                stakingBalances = stakingBalances,
                stakingDelegations = stakingDelegations,
                unbondingDelegation = unbondingDelegations,
                stakingRewards = stakingRewards,
                subaccounts = subaccounts,
                groupedSubaccounts = groupedSubaccounts,
                tradingRewards = tradingRewards,
                launchIncentivePoints = launchIncentivePoints,
            )
        }

        private fun findTokenInfo(tokensInfo: Map<String, TokenInfo>, denom: String): TokenInfo? {
            return tokensInfo.firstNotNullOfOrNull { if (it.value.denom == denom) it.value else null }
        }

        private fun processStakingDelegations(
            existing: Account?,
            parser: ParserProtocol,
            tokensInfo: Map<String, TokenInfo>,
            stakingDelegations: List<InternalStakingDelegationState>?
        ): IList<StakingDelegation>? {
            return stakingDelegations?.mapIndexedNotNull { index, item ->
                val tokenInfo = findTokenInfo(tokensInfo, item.balance.denom)
                if (tokenInfo != null) {
                    StakingDelegation.create(
                        existing = existing?.stakingDelegations?.getOrNull(index),
                        parser = parser,
                        data = emptyMap(),
                        decimals = tokenInfo.decimals,
                        internalState = item,
                    )
                } else {
                    null
                }
            }?.toIList()
        }

        private fun processStakingBalance(
            existing: Account?,
            parser: ParserProtocol,
            tokensInfo: Map<String, TokenInfo>,
            stakingBalances: Map<String, InternalAccountBalanceState>?,
        ): IMap<String, AccountBalance> {
            val newStakingBalances: IMutableMap<String, AccountBalance> =
                iMutableMapOf()
            for ((key, value) in stakingBalances ?: emptyMap()) {
                val tokenInfo = findTokenInfo(tokensInfo, key)
                if (tokenInfo != null) {
                    AccountBalance.create(
                        existing = existing?.stakingBalances?.get(key),
                        parser = parser,
                        decimals = tokenInfo.decimals,
                        internalState = value,
                    )?.let { balance ->
                        newStakingBalances[key] = balance
                    }
                }
            }
            return newStakingBalances
        }
    }
}

@JsExport
@Serializable
data class StakingDelegation(
    var validator: String,
    var amount: String,
) {
    companion object {
        internal fun create(
            existing: StakingDelegation?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            decimals: Int,
            internalState: InternalStakingDelegationState?
        ): StakingDelegation? {
            Logger.d { "creating Staking Delegation\n" }

            val validator = internalState?.validatorAddress ?: parser.asString(data["validator"])
            val amount = internalState?.balance?.amount ?: parser.asDecimal(data["amount"])
            if (validator != null && amount != null) {
                val decimalAmount = amount * Numeric.decimal.TEN.pow(-1 * decimals)
                val decimalAmountString = parser.asString(decimalAmount)!!
                return if (existing?.validator != validator || existing.amount != decimalAmountString) {
                    StakingDelegation(validator, decimalAmountString)
                } else {
                    existing
                }
            }
            Logger.d { "Staking Delegation not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class UnbondingDelegation(
    var validator: String,
    var completionTime: String,
    var balance: String,
)

@JsExport
@Serializable
data class StakingRewards(
    var validators: IList<String>,
    var totalRewards: IList<AccountBalance>,
)
