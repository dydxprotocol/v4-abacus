# Structure

PerpetualState represent the complete state of Abacus

data class PerpetualState(  
&emsp;val assets: Map<String, [Asset](Assets.md)>?,  
&emsp;val marketsSummary: [PerpetualMarketSummary](PerpetualMarketSummary.md)?,  
&emsp;val orderbooks: Map<String, [MarketOrderbook](Market/MarketOrderbook.md)>?,  
&emsp;val candles: Map<String, [MarketCandles](Market/MarketCandles.md)>?,  
&emsp;val trades: Map<String, [MarketTrades](Market/MarketTrades.md)>?,  
&emsp;val historicalFundings: Map<String, [MarketHistoricalFundings](Market/MarketHistoricalFundings.md)>?,  
&emsp;val wallet: [Wallet](Wallet.md)?,  
&emsp;val account: [Account](Account.md)?,  
&emsp;val historicalPnl: Map<String, [SubaccountHistoricalPNLs](#SubaccountHistoricalPNLs)>?,  
&emsp;val fills: Map<String, [SubaccountFills](#SubaccountFills)>?,  
&emsp;val transfers: Map<String, [SubaccountTransfers](#SubaccountTransfers)>?,  
&emsp;val fundingPayments: Map<String, [SubaccountFundingPayments](#SubaccountFundingPayment)>?,  
&emsp;val configs: Configs?,  
&emsp;val input: [Input](Input.md)?,  
&emsp;val availableSubaccountNumbers: Array<Int>,
&emsp;val transferStatuses: Map<String, TransferStatus>?
)

# SubaccountHistoricalPNLs

A sorted list of [SubaccountHistoricalPNL](Subaccount/SubaccountHistoricalPNL.md)

# SubaccountFills

A sorted list of [SubaccountFill](Subaccount/SubaccountFill.md)

# SubaccountTransfers

A sorted list of [SubaccountTransfer](Subaccount/SubaccountTransfer.md)

# SubaccountFundingPayments

A sorted list of [SubaccountFundingPayment](Subaccount/SubaccountFundingPayment.md)
