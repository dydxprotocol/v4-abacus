# SubaccountHistoricalPNL

data class SubaccountHistoricalPNL(  
&emsp;val equity: Double,  
&emsp;val totalPnl: Double,  
&emsp;val netTransfers: Double,  
&emsp;val createdAtMilliseconds: Double  
)

## equity

Equity value at the moment

## totalPnl

Total PNL at the moment

## netTransfers

Total net transfer (deposit and withdraw)

## createdAtMilliseconds

The timestamp of this PNL calculation