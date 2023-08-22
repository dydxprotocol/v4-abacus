package exchange.dydx.abacus.output

import exchange.dydx.abacus.output.input.Input
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.JsExport
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
    val notifications: IList<Notification>?,
) {
    fun assetIds(): IList<String>? {
        return assets?.keys?.toIList()
    }

    fun asset(assetId: String): Asset? {
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
        return account?.subaccounts?.get("$subaccountNumber")
    }

    fun subaccountHistoricalPnl(subaccountNumber: Int): IList<SubaccountHistoricalPNL>? {
        return historicalPnl?.get("$subaccountNumber")
    }

    fun subaccountFills(subaccountNumber: Int): IList<SubaccountFill>? {
        return fills?.get("$subaccountNumber")
    }

    fun subaccountTransfers(subaccountNumber: Int): IList<SubaccountTransfer>? {
        return transfers?.get("$subaccountNumber")
    }

    fun subaccountFundingPayments(subaccountNumber: Int): IList<SubaccountFundingPayment>? {
        return fundingPayments?.get("$subaccountNumber")
    }

    fun transferStatus(hash: String): TransferStatus? {
        return transferStatuses?.get(hash)
    }
}