package exchange.dydx.abacus.processor.wallet.account

import exchange.dydx.abacus.output.LaunchIncentivePoint
import exchange.dydx.abacus.processor.base.BaseProcessor
import exchange.dydx.abacus.processor.wallet.account.staking.AccountDelegationsProcessor
import exchange.dydx.abacus.processor.wallet.account.staking.DelegationUnbondingProcessor
import exchange.dydx.abacus.processor.wallet.account.staking.StakingRewardsProcessor
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.protocols.asTypedList
import exchange.dydx.abacus.protocols.asTypedObject
import exchange.dydx.abacus.responses.SocketInfo
import exchange.dydx.abacus.state.InternalAccountState
import exchange.dydx.abacus.state.InternalSubaccountState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.HistoricalTradingRewardsPeriod
import exchange.dydx.abacus.utils.safeSet
import indexer.codegen.IndexerFundingPaymentResponseObject
import indexer.codegen.IndexerHistoricalBlockTradingReward
import indexer.codegen.IndexerHistoricalTradingRewardAggregation
import indexer.codegen.IndexerPnlTicksResponseObject
import indexer.codegen.IndexerTransferResponseObject
import indexer.models.IndexerCompositeFillObject
import indexer.models.chain.OnChainAccountBalanceObject
import indexer.models.chain.OnChainDelegationResponse
import indexer.models.chain.OnChainStakingRewardsResponse
import indexer.models.chain.OnChainUnbondingResponse
import indexer.models.configs.ConfigsLaunchIncentivePoints

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
    private val stakingRewardsProcessor = StakingRewardsProcessor(parser)
    private val unbondingProcessor = DelegationUnbondingProcessor(parser)

    internal fun processAccountBalances(
        existing: InternalAccountState,
        payload: List<OnChainAccountBalanceObject>?,
    ): InternalAccountState {
        existing.balances = balancesProcessor.process(existing.balances, payload)
        return existing
    }

    internal fun processStakingDelegations(
        existing: InternalAccountState,
        payload: OnChainDelegationResponse?,
    ): InternalAccountState {
        existing.stakingBalances = delegationsProcessor.process(existing.stakingBalances, payload)
        existing.stakingDelegations = delegationsProcessor.processDelegations(existing.stakingDelegations, payload)
        return existing
    }

    fun processUnbonding(
        existing: InternalAccountState,
        payload: OnChainUnbondingResponse?,
    ): InternalAccountState {
        existing.unbondingDelegation = unbondingProcessor.process(existing.unbondingDelegation, payload)
        return existing
    }

    fun processStakingRewards(
        existing: InternalAccountState,
        payload: OnChainStakingRewardsResponse?,
    ): InternalAccountState {
        existing.stakingRewards = stakingRewardsProcessor.process(existing.stakingRewards, payload)
        return existing
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

    internal fun processFundingPayments(
        existing: InternalAccountState,
        payload: List<IndexerFundingPaymentResponseObject>?,
        subaccountNumber: Int,
    ): InternalAccountState {
        val subaccount = existing.subaccounts[subaccountNumber] ?: InternalSubaccountState(subaccountNumber = subaccountNumber)
        val newSubaccount = subaccountsProcessor.processFundingPayments(subaccount, payload)
        existing.subaccounts[subaccountNumber] = newSubaccount
        return existing
    }

    internal fun processFills(
        existing: InternalAccountState,
        payload: List<IndexerCompositeFillObject>?,
        subaccountNumber: Int,
    ): InternalAccountState {
        val subaccount = existing.subaccounts[subaccountNumber] ?: InternalSubaccountState(subaccountNumber = subaccountNumber)
        val newSubaccount = subaccountsProcessor.processFills(subaccount, payload)
        existing.subaccounts[subaccountNumber] = newSubaccount
        return existing
    }

    internal fun processTransfers(
        existing: InternalAccountState,
        payload: List<IndexerTransferResponseObject>?,
        subaccountNumber: Int,
    ): InternalAccountState {
        val subaccount = existing.subaccounts[subaccountNumber] ?: InternalSubaccountState(subaccountNumber = subaccountNumber)
        val newSubaccount = subaccountsProcessor.processTransfers(subaccount, payload)
        existing.subaccounts[subaccountNumber] = newSubaccount
        return existing
    }

    internal fun processHistoricalTradingRewards(
        existing: InternalAccountState,
        payload: List<IndexerHistoricalTradingRewardAggregation>?,
        period: HistoricalTradingRewardsPeriod,
    ): InternalAccountState {
        val modifiedHistoricalTradingRewards = tradingRewardsProcessor.processHistoricalTradingRewards(
            existing = existing.tradingRewards.historical[period],
            payload = payload,
        )
        if (modifiedHistoricalTradingRewards != null) {
            existing.tradingRewards.historical[period] = modifiedHistoricalTradingRewards
        } else {
            existing.tradingRewards.historical.remove(period)
        }
        return existing
    }

    fun processAccount(
        internalState: InternalAccountState,
        content: Map<String, Any>?,
    ): InternalAccountState {
        val modified = internalState
        val subaccounts = parser.asNativeList(parser.value(content, "subaccounts"))
        subaccountsProcessor.processSubaccounts(
            internalState = internalState.subaccounts,
            payload = subaccounts,
        )

        internalState.tradingRewards.total = parser.asDouble(parser.value(content, "totalTradingRewards"))

        /* block trading rewards are only sent in subaccounts.0 channel */
        val blockTradingRewards =
            parser.asTypedList<IndexerHistoricalBlockTradingReward>(parser.value(content, "subaccounts.0.tradingRewards"))
        if (blockTradingRewards != null) {
            internalState.tradingRewards.blockRewards.addAll(blockTradingRewards)
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

        val tradingReward = parser.asTypedObject<IndexerHistoricalBlockTradingReward>(content["tradingReward"])
        if (tradingReward != null) {
            existing.tradingRewards.blockRewards.add(tradingReward)
        }

        return account ?: existing
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

            val tradingReward = parser.asTypedObject<IndexerHistoricalBlockTradingReward>(content["tradingReward"])
            if (tradingReward != null) {
                existing.tradingRewards.blockRewards.add(tradingReward)
            }
        }
        return existing
    }

    fun updateHeight(
        existing: InternalAccountState,
        height: BlockAndTime?,
    ): Triple<InternalAccountState, Boolean, List<Int>?> {
        val subaccounts = existing.subaccounts
        if (subaccounts != null) {
            val (modifiedSubaccounts, updated, subaccountIds) = subaccountsProcessor.updateSubaccountsHeight(
                existing = subaccounts,
                height = height,
            )
            if (updated) {
                return Triple(existing, true, subaccountIds)
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

    fun orderCanceled(
        existing: InternalAccountState,
        orderId: String,
        subaccountNumber: Int,
    ): Pair<InternalAccountState, Boolean> {
        val subaccounts = existing.subaccounts
        val (modifiedSubaccounts, updated) = subaccountsProcessor.orderCanceled(
            existing = subaccounts,
            orderId = orderId,
            subaccountNumber = subaccountNumber,
        )
        return Pair(existing, updated)
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
        if (payload != null) {
            existing.launchIncentivePoints[season] = LaunchIncentivePoint(
                incentivePoints = payload.incentivePoints ?: 0.0,
                marketMakingIncentivePoints = payload.marketMakingIncentivePoints ?: 0.0,
            )
        }
        return existing
    }
}
