package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.LaunchIncentivePoints
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalAccountBalanceState
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalStakingDelegationState
import exchange.dydx.abacus.state.manager.TokenInfo
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.Numeric
import kollections.JsExport
import kollections.iMapOf
import kollections.iMutableListOf
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
            data: Map<String, Any>,
            tokensInfo: Map<String, TokenInfo>,
            localizer: LocalizerProtocol?,
            staticTyping: Boolean,
            internalState: InternalAccountState,
        ): Account {
            Logger.d { "creating Account\n" }

            val balances: IMutableMap<String, AccountBalance> =
                iMutableMapOf()
            if (staticTyping) {
                internalState.balances?.forEach { (key, value) ->
                    AccountBalance.create(
                        existing = existing?.balances?.get(key),
                        parser = parser,
                        data = emptyMap(),
                        decimals = findTokenInfo(tokensInfo, key)?.decimals ?: 0,
                        internalState = value,
                    )?.let { balance ->
                        balances[key] = balance
                    }
                }
            } else {
                val balancesData = parser.asMap(data["balances"])
                if (balancesData != null) {
                    for ((key, value) in balancesData) {
                        val balanceData = parser.asMap(value) ?: iMapOf()
                        // key is the denom
                        val tokenInfo = findTokenInfo(tokensInfo, key)
                        if (tokenInfo != null) {
                            AccountBalance.create(
                                existing = existing?.balances?.get(key),
                                parser = parser,
                                data = balanceData,
                                decimals = tokenInfo.decimals,
                                internalState = internalState.balances?.get(key),
                            )?.let { balance ->
                                balances[key] = balance
                            }
                        }
                    }
                }
            }

            val stakingBalances = if (staticTyping) {
                processStakingBalance(
                    existing = existing,
                    parser = parser,
                    tokensInfo = tokensInfo,
                    stakingBalances = internalState.stakingBalances,
                )
            } else {
                processStakingBalanceDeprecated(
                    existing = existing,
                    parser = parser,
                    data = data,
                    tokensInfo = tokensInfo,
                )
            }

            val stakingDelegations = if (staticTyping) {
                processStakingDelegations(
                    existing = existing,
                    parser = parser,
                    tokensInfo = tokensInfo,
                    stakingDelegations = internalState.stakingDelegations,
                )
            } else {
                processStakingDelegationsDeprecated(
                    existing = existing,
                    parser = parser,
                    data = data,
                    tokensInfo = tokensInfo,
                )
            }

            val unbondingDelegations = if (staticTyping) {
                internalState.unbondingDelegation?.toIList()
            } else {
                data["unbondingDelegation"] as IList<UnbondingDelegation>?
            }

            val stakingRewards = if (staticTyping) {
                internalState.stakingRewards
            } else {
                data["stakingRewards"] as StakingRewards?
            }

            val tradingRewards = if (staticTyping) {
                TradingRewards.create(
                    existing = existing?.tradingRewards,
                    parser = parser,
                    internalState = internalState.tradingRewards,
                )
            } else {
                val tradingRewardsData = parser.asMap(data["tradingRewards"])
                if (tradingRewardsData != null) {
                    TradingRewards.createDeprecated(
                        existing?.tradingRewards,
                        parser,
                        tradingRewardsData,
                    )
                } else {
                    null
                }
            }

            val launchIncentivePoints = if (staticTyping) {
                val points = internalState.launchIncentivePoints
                if (points.isNotEmpty()) {
                    LaunchIncentivePoints(points = points.toIMap())
                } else {
                    null
                }
            } else {
                (parser.asMap(data["launchIncentivePoints"]))?.let {
                    LaunchIncentivePoints.create(existing?.launchIncentivePoints, parser, it)
                }
            }

            val subaccounts: IMutableMap<String, Subaccount> =
                iMutableMapOf()

            if (staticTyping) {
                internalState.subaccounts.forEach { (key, value) ->
                    Subaccount.create(
                        existing = existing?.subaccounts?.get(key.toString()),
                        parser = parser,
                        data = null,
                        localizer = localizer,
                        staticTyping = staticTyping,
                        internalState = value,
                    )?.let { subaccount ->
                        subaccounts[key.toString()] = subaccount
                    }
                }
            } else {
                val subaccountsData = parser.asMap(data["subaccounts"])
                if (subaccountsData != null) {
                    for ((key, value) in subaccountsData) {
                        val subaccountData = parser.asMap(value) ?: iMapOf()

                        val subaccountNumber = parser.asInt(key) ?: 0
                        Subaccount.create(
                            existing = existing?.subaccounts?.get(key),
                            parser = parser,
                            data = subaccountData,
                            localizer = localizer,
                            staticTyping = staticTyping,
                            internalState = internalState.subaccounts[subaccountNumber],
                        )
                            ?.let { subaccount ->
                                subaccounts[key] = subaccount
                            }
                    }
                }
            }

            val groupedSubaccounts: IMutableMap<String, Subaccount> =
                iMutableMapOf()

            if (staticTyping) {
                internalState.groupedSubaccounts.forEach { (key, value) ->
                    Subaccount.create(
                        existing = existing?.groupedSubaccounts?.get(key.toString()),
                        parser = parser,
                        data = null,
                        localizer = localizer,
                        staticTyping = staticTyping,
                        internalState = value,
                    )?.let { subaccount ->
                        groupedSubaccounts[key.toString()] = subaccount
                    }
                }
            } else {
                val groupedSubaccountsData = parser.asMap(data["groupedSubaccounts"])
                if (groupedSubaccountsData != null) {
                    for ((key, value) in groupedSubaccountsData) {
                        val subaccountData = parser.asMap(value) ?: iMapOf()

                        val subaccountNumber = parser.asInt(key) ?: 0
                        Subaccount.create(
                            existing = existing?.subaccounts?.get(key),
                            parser = parser,
                            data = subaccountData,
                            localizer = localizer,
                            staticTyping = staticTyping,
                            internalState = internalState.subaccounts[subaccountNumber],
                        )
                            ?.let { subaccount ->
                                groupedSubaccounts[key] = subaccount
                            }
                    }
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

        private fun processStakingDelegationsDeprecated(
            existing: Account?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            tokensInfo: Map<String, TokenInfo>,
        ): IMutableList<StakingDelegation> {
            val stakingDelegations: IMutableList<StakingDelegation> =
                iMutableListOf()
            val stakingDelegationsData = parser.asList(data["stakingDelegations"])
            stakingDelegationsData?.forEachIndexed { index, value ->
                val stakingDelegationData = parser.asMap(value) ?: iMapOf()
                val tokenInfo = findTokenInfo(tokensInfo, stakingDelegationData["denom"] as String)
                if (tokenInfo != null) {
                    StakingDelegation.create(
                        existing?.stakingDelegations?.getOrNull(index),
                        parser,
                        stakingDelegationData,
                        tokenInfo.decimals,
                        null,
                    )?.let { stakingDelegation ->
                        stakingDelegations.add(stakingDelegation)
                    }
                }
            }
            return stakingDelegations
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
                        data = emptyMap(),
                        decimals = tokenInfo.decimals,
                        internalState = value,
                    )?.let { balance ->
                        newStakingBalances[key] = balance
                    }
                }
            }
            return newStakingBalances
        }

        private fun processStakingBalanceDeprecated(
            existing: Account?,
            parser: ParserProtocol,
            data: Map<String, Any>,
            tokensInfo: Map<String, TokenInfo>,
        ): IMutableMap<String, AccountBalance> {
            val stakingBalances: IMutableMap<String, AccountBalance> =
                iMutableMapOf()
            val stakingBalancesData = parser.asMap(data["stakingBalances"])
            if (stakingBalancesData != null) {
                for ((key, value) in stakingBalancesData) {
                    // key is the denom
                    // It should be chain token denom here
                    val tokenInfo = findTokenInfo(tokensInfo, key)
                    if (tokenInfo != null) {
                        val balanceData = parser.asMap(value) ?: iMapOf()
                        AccountBalance.create(
                            existing = existing?.stakingBalances?.get(key),
                            parser = parser,
                            data = balanceData,
                            decimals = tokenInfo.decimals,
                            internalState = null,
                        )?.let { balance ->
                            stakingBalances[key] = balance
                        }
                    }
                }
            }
            return stakingBalances
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
