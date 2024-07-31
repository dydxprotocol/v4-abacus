package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.account.AccountBalance
import exchange.dydx.abacus.output.account.StakingRewards
import exchange.dydx.abacus.output.account.UnbondingDelegation
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.wallet.account.staking.AccountDelegationsProcessor
import exchange.dydx.abacus.processor.wallet.account.staking.DelegationUnbondingProcessor
import exchange.dydx.abacus.processor.wallet.account.staking.StakingRewardsProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.internalstate.InternalAccountState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerFillResponseObject
import indexer.codegen.IndexerPnlTicksResponseObject
import indexer.codegen.IndexerTransferResponseObject
import indexer.models.chain.OnChainAccountBalanceObject
import indexer.models.chain.OnChainDelegationResponse
import indexer.models.chain.OnChainStakingRewardsResponse
import indexer.models.chain.OnChainUnbondingResponse
import indexer.models.configs.ConfigsLaunchIncentivePoints
import kollections.iMutableListOf

/*
"account": {
      "starkKey": "01de24f8468f0590b80c12c87ae9e247ef3278d4683d875296051495b2ad0100",
      "positionId": "30915",
      "equity": "205935.352966",
      "freeCollateral": "187233.155294",
      "pendingDeposits": "0.000000",
      "pendingWithdrawals": "0.000000",
      "openPositions": {
        "ETH-USD": {
          "market": "ETH-USD",
          "status": "OPEN",
          "side": "LONG",
          "size": "93.57",
          "maxSize": "100",
          "entryPrice": "1091.812076",
          "exitPrice": "1091.236219",
          "unrealizedPnl": "61455.547636",
          "realizedPnl": "-4173.521266",
          "createdAt": "2022-06-30T01:01:10.234Z",
          "closedAt": null,
          "sumOpen": "218.92",
          "sumClose": "125.35",
          "netFunding": "-4101.337527"
        },
        "UNI-USD": {
          "market": "UNI-USD",
          "status": "OPEN",
          "side": "LONG",
          "size": "11548.4",
          "maxSize": "11548.4",
          "entryPrice": "7.065650",
          "exitPrice": "0.000000",
          "unrealizedPnl": "23552.454293",
          "realizedPnl": "142.629215",
          "createdAt": "2022-07-18T20:37:23.893Z",
          "closedAt": null,
          "sumOpen": "11548.4",
          "sumClose": "0",
          "netFunding": "142.629215"
        }
      },
      "accountNumber": "0",
      "id": "dace1648-c854-5aed-9879-88899bf647a3",
      "quoteBalance": "-62697.279528",
      "createdAt": "2021-04-20T18:27:38.698Z"
    },

    to

    "account": {
      "ethereumeAddress": "0xc3ad9aB721765560F05AFA7696D5e167CAD010e7",
      "positionId": "30915",
      "user": {
        "isRegistered": false,
        "email": "johnqh@yahoo.com",
        "username": "johnqh",
        "makerFeeRate": 0.00015,
        "takerFeeRate": 0.0004,
        "makerVolume30D": 0,
        "takerVolume30D": 1483536.2848,
        "fees30D": 626.566513,
        "isEmailVerified": false,
        "country": "CK",
        "favorited": [
          "BTC-USD",
          "CRV-USD",
          "UMA-USD",
          "ETH-USD",
          "RUNE-USD",
          "MKR-USD"
        ],
        "walletId": "METAMASK"
      },
      "pnlTotal": 23.34,
      "pnl24h": 3.34,
      "pnl24hPercent": 0.03,
      "historicalPnl": [
        {
          "equity": 138463.2724,
          "totalPnl": 78334.1124,
          "createdAtMilliseconds": 23099045345,
          "netTransfers": 0
        }
      ],
      "quoteBalance": {
        "current": 2349.234,
        "postOrder": 2349.234,
        "postAllOrders": 2349.234
      },
      "notionalTotal": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 234230.34
      },
      "valueTotal": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 234230.34
      },
      "initialRiskTotal": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 234230.34
      },
      "adjustedImf": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 234230.34
      },
      "equity": {
        "current": 234324,
        "postOrder": 234234,
        "postAllOrders": 798234
      },
      "freeCollateral": {
        "current": 0.03,
        "postOrder": 0.04,
        "postAllOrders": 0.03
      },
      "leverage": {
        "current": 7.64,
        "postOrder": 8.76,
        "postAllOrders": 6.54
      },
      "marginUsage": {
        "current": 0.102,
        "postOrder": 0.105,
        "postAllOrders": 0.093
      },
      "buyingPower": {
        "current": 98243520.45,
        "postOrder": 234899345.34,
        "postAllOrders": 98243520.45
      },
 */

internal class V4AccountProcessor(
    parser: ParserProtocol,
    localizer: LocalizerProtocol?,
) : BaseProcessor(parser) {
    private val subaccountsProcessor = V4SubaccountsProcessor(parser, localizer)
    private val balancesProcessor = AccountBalancesProcessor(parser)
    private val delegationsProcessor = AccountDelegationsProcessor(parser)
    private val tradingRewardsProcessor = AccountTradingRewardsProcessor(parser)
    private val launchIncentivePointsProcessor = LaunchIncentivePointsProcessor(parser)
    private val stakingRewardsProcessor = StakingRewardsProcessor(parser)
    private val unbondingProcessor = DelegationUnbondingProcessor(parser)

    internal fun processAccountBalances(
        existing: InternalAccountState,
        payload: List<OnChainAccountBalanceObject>?,
    ): InternalAccountState {
        existing.balances = balancesProcessor.process(existing.balances, payload)
        return existing
    }

    internal fun receivedAccountBalancesDeprecated(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any> {
        val modified = existing?.mutable() ?: mutableMapOf()
        val balances = parser.asNativeMap(parser.value(existing, "balances"))
        val modifiedBalances = balancesProcessor.receivedBalancesDeprecated(balances, payload)
        modified.safeSet("balances", modifiedBalances)
        return modified
    }

    internal fun processStakingDelegations(
        existing: InternalAccountState,
        payload: OnChainDelegationResponse?,
    ): InternalAccountState {
        existing.stakingBalances = delegationsProcessor.process(existing.stakingBalances, payload)
        existing.stakingDelegations = delegationsProcessor.processDelegations(existing.stakingDelegations, payload)
        return existing
    }

    internal fun receivedDelegationsDeprecated(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any> {
        val modified = existing?.mutable() ?: mutableMapOf()
        val delegations = parser.asNativeMap(parser.value(existing, "stakingBalances"))
        val modifiedStakingBalance = delegationsProcessor.receivedDeprecated(delegations, payload)
        modified.safeSet("stakingBalances", modifiedStakingBalance)
        val modifiedDelegations = delegationsProcessor.receivedDelegationsDeprecated(delegations, payload)
        modified.safeSet("stakingDelegations", modifiedDelegations)
        return modified
    }

    fun processUnbonding(
        existing: InternalAccountState,
        payload: OnChainUnbondingResponse?,
    ): InternalAccountState {
        existing.unbondingDelegation = unbondingProcessor.process(existing.unbondingDelegation, payload)
        return existing
    }

    internal fun receivedUnbonding(
        existing: Map<String, Any>?,
        payload: List<Any>?,
    ): Map<String, Any>? {
        if (payload == null) {
            return existing
        }
        val modified = existing?.mutable() ?: mutableMapOf()
        val unbondingDelegations: IMutableList<UnbondingDelegation> = iMutableListOf()

        for (validator in payload) {
            val validatorAddress = parser.asString(parser.value(validator, "validatorAddress")) ?: continue
            val entries = parser.asList(parser.value(validator, "entries")) ?: continue

            for (entry in entries) {
                val completionTime = parser.asString(parser.value(entry, "completionTime")) ?: continue
                val balance = parser.asString(parser.value(entry, "balance")) ?: continue

                unbondingDelegations.add(UnbondingDelegation(validatorAddress, completionTime, balance))
            }
        }
        modified.safeSet("unbondingDelegation", unbondingDelegations)
        return modified
    }

    fun processStakingRewards(
        existing: InternalAccountState,
        payload: OnChainStakingRewardsResponse?,
    ): InternalAccountState {
        existing.stakingRewards = stakingRewardsProcessor.process(existing.stakingRewards, payload)
        return existing
    }

    internal fun receivedStakingRewardsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any>? {
        val rewards = parser.asList(parser.value(payload, "rewards"))
        val total = parser.asList(parser.value(payload, "total"))
        if (payload == null || rewards == null || total == null) {
            return existing
        }
        val modified = existing?.mutable() ?: mutableMapOf()
        val validators: IMutableList<String> = iMutableListOf()
        val totalRewards: IMutableList<AccountBalance> = iMutableListOf()
        for (validator in rewards) {
            val validatorAddress = parser.asString(parser.value(validator, "validatorAddress")) ?: continue
            validators.add(validatorAddress)
        }
        for (reward in total) {
            val denom = parser.asString(parser.value(reward, "denom")) ?: continue
            val amount = parser.asString(parser.value(reward, "amount")) ?: continue
            totalRewards.add(AccountBalance(denom, amount))
        }
        modified.safeSet("stakingRewards", StakingRewards(validators, totalRewards))
        return modified
    }

    internal fun processHistoricalPnls(
        existing: InternalAccountState,
        payload: List<IndexerPnlTicksResponseObject>?,
        subaccountNumber: Int,
    ): InternalAccountState {
        val subaccount = existing.subaccounts[subaccountNumber] ?: InternalSubaccountState(subaccountNumber = subaccountNumber)
        val newSubaccount = subaccountsProcessor.processsHistoricalPNLs(subaccount, payload)
        existing.subaccounts[subaccountNumber] = newSubaccount
        return existing
    }

    internal fun receivedHistoricalPnlsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountsProcessor.receivedHistoricalPnlsDeprecated(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    internal fun processFills(
        existing: InternalAccountState,
        payload: List<IndexerFillResponseObject>?,
        subaccountNumber: Int,
    ): InternalAccountState {
        val subaccount = existing.subaccounts[subaccountNumber] ?: InternalSubaccountState(subaccountNumber = subaccountNumber)
        val newSubaccount = subaccountsProcessor.processFills(subaccount, payload)
        existing.subaccounts[subaccountNumber] = newSubaccount
        return existing
    }

    internal fun receivedFillsDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
        subaccountNumber: Int,
    ): Map<String, Any> {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountsProcessor.receivedFillsDeprecated(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    fun processTransfers(
        existing: InternalAccountState,
        payload: List<IndexerTransferResponseObject>?,
        subaccountNumber: Int,
    ): InternalAccountState {
        val subaccount = existing.subaccounts[subaccountNumber] ?: InternalSubaccountState(subaccountNumber = subaccountNumber)
        val newSubaccount = subaccountsProcessor.processTransfers(subaccount, payload)
        existing.subaccounts[subaccountNumber] = newSubaccount
        return existing
    }

    internal fun receivedTransfersDeprecated(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
        subaccountNumber: Int,
    ): Map<String, Any> {
        val modified = existing?.mutable() ?: mutableMapOf()
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        val modifiedsubaccount = subaccountsProcessor.receivedTransfersDeprecated(subaccount, payload)
        modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        return modified
    }

    internal fun receivedHistoricalTradingRewards(
        existing: Map<String, Any>?,
        payload: List<Any>?,
        period: String?,
    ): Map<String, Any> {
        val modified = existing?.mutable() ?: mutableMapOf()
        val historicalTradingRewards =
            parser.asNativeList(parser.value(existing, "tradingRewards.historical.$period"))
        val modifiedHistoricalTradingRewards =
            tradingRewardsProcessor.recievedHistoricalTradingRewards(
                historicalTradingRewards,
                payload,
            )
        modified.safeSet("tradingRewards.historical.$period", modifiedHistoricalTradingRewards)
        return modified
    }

    fun processAccount(
        internalState: InternalAccountState,
        content: Map<String, Any>?,
    ): InternalAccountState {
        var modified = internalState
        val subaccounts = parser.asNativeList(parser.value(content, "subaccounts"))
        subaccountsProcessor.processSubaccounts(
            internalState = internalState.subaccounts,
            payload = subaccounts,
        )

        // TODO: Updating the account with the trading rewards
        return modified
    }

    internal fun receivedAccount(
        existing: Map<String, Any>?,
        payload: Map<String, Any>?,
    ): Map<String, Any> {
        var modified = existing?.mutable() ?: mutableMapOf()
        val subaccounts = parser.asNativeMap(parser.value(existing, "subaccounts"))
        val modifiedSubaccounts = subaccountsProcessor.receivedSubaccounts(
            subaccounts,
            parser.asList(payload?.get("subaccounts")),
        )
        modified.safeSet("subaccounts", modifiedSubaccounts)

        val tradingRewards = parser.asNativeMap(parser.value(existing, "tradingRewards"))
        val modifiedTradingRewards = tradingRewardsProcessor.receivedTotalTradingRewards(
            tradingRewards,
            payload?.get("totalTradingRewards"),
        )
        modified.safeSet("tradingRewards", modifiedTradingRewards)

        val test = parser.value(payload, "subaccounts.0.tradingRewards")
        /* block trading rewards are only sent in subaccounts.0 channel */
        val tradingRewardsPayload =
            parser.asNativeList(parser.value(payload, "subaccounts.0.tradingRewards"))
        if (tradingRewardsPayload != null) {
            for (item in tradingRewardsPayload) {
                modified = receivedBlockTradingReward(modified, item)
            }
        }
        return modified
    }

    internal fun processSubscribed(
        existing: InternalAccountState,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): InternalAccountState {
        var account: InternalAccountState? = null

        val subaccountNumber = parser.asInt(parser.value(content, "subaccount.subaccountNumber"))
        if (subaccountNumber != null) {
            val subaccount = existing.subaccounts[subaccountNumber] ?: InternalSubaccountState(subaccountNumber = subaccountNumber)
            val modifiedsubaccount = subaccountsProcessor.processSubscribed(subaccount, content, height)
            existing.subaccounts[subaccountNumber] = modifiedsubaccount
        } else {
            val parentSubaccountNumber =
                parser.asInt(parser.value(content, "subaccount.parentSubaccountNumber"))
            if (parentSubaccountNumber != null) {
                account = processSubscribedParentSubaccount(existing, content, height)
            }
        }

        // TODO: Updating the account with the trading rewards

//        /* block trading rewards are only sent in subaccounts.0 channel */
//        val tradingRewardsPayload =
//            parser.value(content, "tradingReward")
//        if (tradingRewardsPayload != null) {
//            modified = receivedBlockTradingReward(modified, tradingRewardsPayload)
//        }

        return account ?: existing
    }

    internal fun subscribedDeprecated(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any> {
        var modified = existing?.mutable() ?: mutableMapOf()
        val subaccountNumber = parser.asInt(parser.value(content, "subaccount.subaccountNumber"))
        if (subaccountNumber != null) {
            val subaccount =
                parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
            val modifiedsubaccount = subaccountsProcessor.subscribedDeprecated(subaccount, content, height)
            modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        } else {
            val parentSubaccountNumber =
                parser.asInt(parser.value(content, "subaccount.parentSubaccountNumber"))
            if (parentSubaccountNumber != null) {
                modified = subscribedParentSubaccountDeprecated(modified, content, height).mutable()
            }
        }

        /* block trading rewards are only sent in subaccounts.0 channel */
        val tradingRewardsPayload =
            parser.value(content, "tradingReward")
        if (tradingRewardsPayload != null) {
            modified = receivedBlockTradingReward(modified, tradingRewardsPayload)
        }
        return modified
    }

    private fun getContentBySubaccountNumber(
        content: Map<String, Any>,
    ): Map<Int, Map<String, Any>> {
        /*
        We will go through all segments in the content, regroup them based on subaccountNumber,
        and send to subaccountProcessor's existing code
         */
        val contentBySubaccountNumber = mutableMapOf<Int, MutableMap<String, Any>>()
        for ((key, value) in content) {
            val subpayloadList = when (key) {
                "subaccount" -> parser.asNativeList(parser.value(value, "childSubaccounts"))
                else -> parser.asNativeList(value)
            }
            if (subpayloadList != null) {
                // Go through the list, and group by subaccountNumber
                val subPayloadBySubaccount = mutableMapOf<Int, MutableList<Any>>()
                for (subpayload in subpayloadList) {
                    val subaccountNumber =
                        parser.asInt(parser.value(subpayload, "subaccountNumber"))
                    if (subaccountNumber != null) {
                        val list =
                            subPayloadBySubaccount.getOrPut(subaccountNumber) { mutableListOf() }
                        list.add(subpayload)
                    }
                }
                for ((subaccountNumber, subPayloadList) in subPayloadBySubaccount) {
                    val subaccount =
                        contentBySubaccountNumber.getOrPut(subaccountNumber) { mutableMapOf() }
                    if (key == "subaccount") {
                        // There should be a single subaccount object
                        subaccount.safeSet(key, subPayloadList.firstOrNull())
                    } else {
                        subaccount[key] = subPayloadList
                    }
                }
            }
        }
        return contentBySubaccountNumber
    }

    private fun processSubscribedParentSubaccount(
        existing: InternalAccountState,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): InternalAccountState {
        val contentBySubaccountNumber = getContentBySubaccountNumber(content)

        /*
        Now we have a map of subaccountNumber to content, we can send it to subaccountProcessor
         */
        for ((subaccountNumber, subaccountContent) in contentBySubaccountNumber) {
            val subaccount = existing.subaccounts[subaccountNumber] ?: InternalSubaccountState(subaccountNumber = subaccountNumber)
            val modifiedsubaccount =
                subaccountsProcessor.processSubscribed(subaccount, subaccountContent, height)
            existing.subaccounts[subaccountNumber] = modifiedsubaccount
        }
        return existing
    }

    private fun subscribedParentSubaccountDeprecated(
        existing: Map<String, Any>,
        content: Map<String, Any>,
        height: BlockAndTime?,
    ): Map<String, Any> {
        val contentBySubaccountNumber = getContentBySubaccountNumber(content)

        /*
        Now we have a map of subaccountNumber to content, we can send it to subaccountProcessor
         */
        val modified = existing.mutable()
        for ((subaccountNumber, subaccountContent) in contentBySubaccountNumber) {
            val subaccount =
                parser.asNativeMap(parser.value(modified, "subaccounts.$subaccountNumber"))
            val modifiedsubaccount =
                subaccountsProcessor.subscribedDeprecated(subaccount, subaccountContent, height)
            modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
        }
        return modified
    }

    internal fun processChannelData(
        existing: InternalAccountState,
        content: Map<String, Any>,
        info: SocketInfo,
        height: BlockAndTime?,
    ): InternalAccountState {
        val subaccountNumber = info.childSubaccountNumber ?: parser.asInt(
            parser.value(
                content,
                "subaccounts.subaccountNumber",
            ),
        )
            ?: subaccountNumberFromInfo(info)

        if (subaccountNumber != null) {
            val subaccount = existing.subaccounts[subaccountNumber] ?: InternalSubaccountState(
                subaccountNumber = subaccountNumber,
            )
            val modifiedsubaccount =
                subaccountsProcessor.processChannelData(subaccount, content, height)
            existing.subaccounts[subaccountNumber] = modifiedsubaccount

            // TODO: Updating the account with the trading rewards

//            /* block trading rewards are only sent in subaccounts.0 channel */
//            val tradingRewardsPayload = content["tradingReward"]
//            if (tradingRewardsPayload != null) {
//                modified = receivedBlockTradingReward(modified, tradingRewardsPayload)
//            }
//            return modified
        }
        return existing
    }

    @Suppress("FunctionName")
    internal fun channel_data(
        existing: Map<String, Any>?,
        content: Map<String, Any>,
        info: SocketInfo,
        height: BlockAndTime?,
    ): Map<String, Any>? {
        val subaccountNumber = info.childSubaccountNumber ?: parser.asInt(parser.value(content, "subaccounts.subaccountNumber"))
            ?: subaccountNumberFromInfo(info)

        return if (subaccountNumber != null) {
            var modified = existing?.toMutableMap() ?: mutableMapOf()
            val subaccount =
                parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
            val modifiedsubaccount = subaccountsProcessor.channel_dataDeprecated(subaccount, content, height)
            modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)

            /* block trading rewards are only sent in subaccounts.0 channel */
            val tradingRewardsPayload = content["tradingReward"]
            if (tradingRewardsPayload != null) {
                modified = receivedBlockTradingReward(modified, tradingRewardsPayload)
            }
            return modified
        } else {
            existing
        }
    }

    private fun receivedBlockTradingReward(
        existing: Map<String, Any>,
        payload: Any,
    ): MutableMap<String, Any> {
        val modified = existing.mutable()
        val blockRewards =
            parser.asNativeList(parser.value(existing, "tradingRewards.blockRewards"))
        val modifiedTradingRewards = tradingRewardsProcessor.recievedBlockTradingReward(
            blockRewards,
            payload,
        )
        modified.safeSet("tradingRewards.blockRewards", modifiedTradingRewards)
        return modified
    }

    internal fun updateHeight(
        existing: Map<String, Any>,
        height: BlockAndTime?,
    ): Triple<Map<String, Any>, Boolean, List<Int>?> {
        val subaccounts = parser.asNativeMap(parser.value(existing, "subaccounts"))
        if (subaccounts != null) {
            val (modifiedSubaccounts, updated, subaccountIds) = subaccountsProcessor.updateSubaccountsHeight(
                subaccounts,
                height,
            )
            if (updated) {
                val modified = existing.mutable()
                modified.safeSet("subaccounts", modifiedSubaccounts)
                return Triple(modified, true, subaccountIds)
            }
        }
        return Triple(existing, false, null)
    }

    private fun subaccountNumberFromInfo(info: SocketInfo): Int? {
        val id = info.id
        return if (id != null) {
            val elements = id.split("/")
            if (elements.size == 2) {
                parser.asInt(elements.lastOrNull())
            } else {
                null
            }
        } else {
            null
        }
    }

    internal fun received(
        existing: Map<String, Any>,
        subaccountNumber: Int,
        height: BlockAndTime?,
    ): Pair<Map<String, Any>, Boolean> {
        val subaccount = parser.asNativeMap(parser.value(existing, "subaccounts.$subaccountNumber"))
        if (subaccount != null) {
            val (modifiedsubaccount, subaccountUpdated) = subaccountsProcessor.received(
                subaccount,
                height,
            )
            if (subaccountUpdated) {
                val modified = existing.toMutableMap()
                modified.safeSet("subaccounts.$subaccountNumber", modifiedsubaccount)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }

    internal fun orderCanceled(
        existing: Map<String, Any>,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<Map<String, Any>, Boolean> {
        val subaccounts = parser.asNativeMap(parser.value(existing, "subaccounts"))?.mutable()
        if (subaccounts != null) {
            val (modifiedSubaccounts, updated) =
                subaccountsProcessor.orderCanceled(subaccounts, orderId, subaccountNumber)
            if (updated) {
                val modified = existing.mutable()
                modified.safeSet("subaccounts", modifiedSubaccounts)
                return Pair(modified, true)
            }
        }
        return Pair(existing, false)
    }

    override fun accountAddressChanged() {
        super.accountAddressChanged()
        subaccountsProcessor.accountAddress = accountAddress
    }

    fun processLaunchIncentivePoints(
        existing: InternalAccountState,
        season: String,
        payload: ConfigsLaunchIncentivePoints?,
    ): InternalAccountState {
        val points = launchIncentivePointsProcessor.process(
            season = season,
            existing = existing.launchIncentivePoints,
            payload = payload,
        )
        existing.launchIncentivePoints = points
        return existing
    }

    internal fun receivedLaunchIncentivePointDeprecated(
        existing: Map<String, Any>,
        season: String,
        payload: Any,
    ): Map<String, Any> {
        /*
        launchIncentive.{season}...
         */
        val data = parser.asNativeMap(payload) ?: return existing
        val modified = existing.mutable()
        val points = launchIncentivePointsProcessor.received(
            season,
            parser.asNativeMap(existing["launchIncentivePoints"]),
            data,
        )

        modified.safeSet("launchIncentivePoints", points)
        return modified
    }
}
