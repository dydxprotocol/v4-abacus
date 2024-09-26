package exchange.dydx.abacus.output

import exchange.dydx.abacus.output.account.Account
import exchange.dydx.abacus.output.account.Subaccount
import exchange.dydx.abacus.output.account.SubaccountFill
import exchange.dydx.abacus.output.account.SubaccountFundingPayment
import exchange.dydx.abacus.output.account.SubaccountHistoricalPNL
import exchange.dydx.abacus.output.account.SubaccountTransfer
import exchange.dydx.abacus.output.input.Input
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.Parser
import kollections.JsExport
import kollections.iListOf
import kollections.toIList
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class PerpetualState(
    /*
     * We are handling this explicitly for js
     */
    val assets: IMap<String, Asset>?,
    val marketsSummary: PerpetualMarketSummary?,
    val orderbooks: IMap<String, MarketOrderbook>?,
    val candles: IMap<String, MarketCandles>?,
    val trades: IMap<String, IList<MarketTrade>>?,
    val historicalFundings: IMap<String, IList<MarketHistoricalFunding>>?,
    val wallet: Wallet?,
    val account: Account?,
    val historicalPnl: IMap<String, IList<SubaccountHistoricalPNL>>?,
    val fills: IMap<String, IList<SubaccountFill>>?,
    val transfers: IMap<String, IList<SubaccountTransfer>>?,
    val fundingPayments: IMap<String, IList<SubaccountFundingPayment>>?,
    val configs: Configs?,
    val input: Input?,
    val availableSubaccountNumbers: IList<Int>,
    val transferStatuses: IMap<String, TransferStatus>?,
    val trackStatuses: IMap<String, Boolean>?,
    val restriction: UsageRestriction?,
    val launchIncentive: LaunchIncentive?,
    val compliance: Compliance?,
    val vault: Vault?
) {
    internal companion object {
        fun newState(): PerpetualState {
            return PerpetualState(
                assets = null,
                marketsSummary = null,
                orderbooks = null,
                candles = null,
                trades = null,
                historicalFundings = null,
                wallet = null,
                account = null,
                historicalPnl = null,
                fills = null,
                transfers = null,
                fundingPayments = null,
                configs = null,
                input = null,
                availableSubaccountNumbers = iListOf(),
                transferStatuses = null,
                trackStatuses = null,
                restriction = null,
                launchIncentive = null,
                compliance = null,
                vault = null,
            )
        }
    }

    val parser: ParserProtocol
        get() = Parser()

    fun assetIds(): IList<String>? {
        return assets?.keys?.toIList()
    }

    fun asset(assetId: String): Asset? {
        return assets?.get(assetId)
    }

    fun assetOfMarket(marketId: String): Asset? {
        val assetId = market(marketId)?.assetId ?: return null
        return assets?.get(assetId)
    }

    fun marketIds(): IList<String>? {
        return marketsSummary?.marketIds()
    }

    fun market(marketId: String): PerpetualMarket? {
        return marketsSummary?.market(marketId)
    }

    fun marketOrderbook(marketId: String): MarketOrderbook? {
        return orderbooks?.get(marketId)
    }

    fun marketTrades(marketId: String): IList<MarketTrade>? {
        return trades?.get(marketId)
    }

    fun marketCandles(marketId: String): MarketCandles? {
        return candles?.get(marketId)
    }

    fun historicalFunding(marketId: String): IList<MarketHistoricalFunding>? {
        return historicalFundings?.get(marketId)
    }

    fun subaccount(subaccountNumber: Int): Subaccount? {
        // if useParentSubaccount is false in SubaccountConfigs, groupedSubaccounts will be null
        return account?.groupedSubaccounts?.get("$subaccountNumber")
            ?: account?.subaccounts?.get("$subaccountNumber")
    }

    fun subaccountHistoricalPnl(subaccountNumber: Int): IList<SubaccountHistoricalPNL>? {
        return historicalPnl?.get("$subaccountNumber")
    }

    fun subaccountFills(subaccountNumber: Int): IList<SubaccountFill>? {
        // if useParentSubaccount is false in SubaccountConfigs, groupedSubaccounts will be null
        if (account?.groupedSubaccounts?.get("$subaccountNumber") != null) {
            val groupedSubaccountFills = mutableListOf<SubaccountFill>()
            for ((subaccountNumberKey, subaccountFills) in fills ?: emptyMap()) {
                val subaccountId = parser.asInt(subaccountNumberKey)
                if (subaccountId == null) {
                    Logger.e { "Invalid subaccount number: $subaccountNumber" }
                    continue
                }
                if (subaccountId % NUM_PARENT_SUBACCOUNTS == subaccountNumber) {
                    groupedSubaccountFills.addAll(subaccountFills)
                }
            }

            return groupedSubaccountFills.sortedByDescending { it.createdAtMilliseconds }.toIList()
        }

        return fills?.get("$subaccountNumber")
    }

    fun subaccountTransfers(subaccountNumber: Int): IList<SubaccountTransfer>? {
        return transfers?.get("$subaccountNumber")
    }

    fun subaccountFundingPayments(subaccountNumber: Int): IList<SubaccountFundingPayment>? {
        return fundingPayments?.get("$subaccountNumber")
    }
}
