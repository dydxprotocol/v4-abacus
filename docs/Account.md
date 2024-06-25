# Account

account in PerpetualState is an object, containing a map of String to Subaccount

data class Account(  
&emsp;var subaccounts: Map<String, Subaccount>?,  
&emsp;var tradingRewards: TradingRewards?  
)

# TradingRewards

Metadata about trading rewards this account has earned. 

data class TradingRewards(  
&emsp;val total: Double?,  
&emsp;val filledHistory: Map<String, IList<HistoricalTradingReward>>?,  
&emsp;val rawHistory: Map<String, IList<HistoricalTradingReward>>?  
)

## filledHistory

Map with keys "WEEKLY", "DAILY", and "MONTHLY" where each value is a list of objects representing the rewards received in every period starting in the current period and going backward. 

Periods with no rewards will be present in the list with a value of 0. 

## rawHistory

Same as filledHistory except all empty (0) rewards periods will be removed from the lists. 

# Subaccount

data class Subaccount(  
&emsp;val subaccountNumber: Int,  
&emsp;val positionId: String?,  
&emsp;val pnlTotal: Double?,  
&emsp;val pnl24h: Double?,  
&emsp;val pnl24hPercent: Double?,  
&emsp;val quoteBalance: TradeStatesWithDoubleValues?,  
&emsp;val notionalTotal: TradeStatesWithDoubleValues?,  
&emsp;val valueTotal: TradeStatesWithDoubleValues?,  
&emsp;val initialRiskTotal: TradeStatesWithDoubleValues?,  
&emsp;val adjustedImf: TradeStatesWithDoubleValues?,  
&emsp;val equity: TradeStatesWithDoubleValues?,  
&emsp;val freeCollateral: TradeStatesWithDoubleValues?,  
&emsp;val leverage: TradeStatesWithDoubleValues?,  
&emsp;val marginUsage: TradeStatesWithDoubleValues?,  
&emsp;val buyingPower: TradeStatesWithDoubleValues?,  
&emsp;val openPositions: [SubaccountPositions](#SubaccountPositions)?,  
&emsp;val orders: [SubaccountOrders](#SubaccountOrders)?,  
&emsp;val marginEnabled: Boolean?  
)

## subaccountNumber

Sequential number for the subaccount

## positionId

Not used

## pnlTotal

Total PNL

## pnl24h

24-hour PNL

## pnl24hPercent

24-hour PNL percentage change

## quoteBalance

Calculated quote balance

## notionalTotal

Notional total of the positions

## valueTotal

Total value of the positions

## initialRiskTotal

Total initial risk

## adjustedImf

Adjusted margin fraction

## equity

Total equity

## freeCollateral

Free collateral

## leverage

Leverage of the subaccount

## marginUsage

Margin usage of the account

## buyingPower

Buying power

## openPositions

All open positions

## orders

Outstanding orders

## marginEnabled

V4 only and should always be true

# SubaccountPositions

An array of [SubaccountPosition](#SubaccountPosition)

# SubaccountPosition

data class SubaccountPosition(  
&emsp;val id: String,  
&emsp;val assetId: String,  
&emsp;val side: TradeStatesWithPositionSides,  
&emsp;val entryPrice: TradeStatesWithDoubleValues?,  
&emsp;val exitPrice: Double?,  
&emsp;val createdAtMilliseconds: Double?,  
&emsp;val closedAtMilliseconds: Double?,  
&emsp;val netFunding: Double?,  
&emsp;val realizedPnl: TradeStatesWithDoubleValues?,  
&emsp;val realizedPnlPercent: TradeStatesWithDoubleValues?,  
&emsp;val unrealizedPnl: TradeStatesWithDoubleValues?,  
&emsp;val unrealizedPnlPercent: TradeStatesWithDoubleValues?,  
&emsp;val size: TradeStatesWithDoubleValues?,  
&emsp;val notionalTotal: TradeStatesWithDoubleValues?,  
&emsp;val valueTotal: TradeStatesWithDoubleValues?,  
&emsp;val initialRiskTotal: TradeStatesWithDoubleValues?,  
&emsp;val adjustedImf: TradeStatesWithDoubleValues?,  
&emsp;val leverage: TradeStatesWithDoubleValues?,  
&emsp;val maxLeverage: TradeStatesWithDoubleValues?,  
&emsp;val buyingPower: TradeStatesWithDoubleValues?,  
&emsp;val liquidationPrice: TradeStatesWithDoubleValues?,  
&emsp;val resources: [SubaccountPositionResources](#SubaccountPositionResources)  
)

## id

ID of the position, it should be the same as marketId

## assetId

Asset ID, such as "ETH"

## side

LONG, SHORT or NONE

## entryPrice

Entry price

## exitPrice

Exit price

## createdAtMilliseconds

When the position was created

## closedAtMilliseconds

When the position was closed

## netFunding

Total funding income or expense

## realizedPnl

Total realized PNL

## realizedPnlPercent

Total realized PNL as percent

## unrealizedPnl

Total unrealized PNL

## unrealizedPnlPercent

Total unrealized PNL as percent

## size

Position size

## notionalTotal

Notional total

## valueTotal

Value total for the position

## initialRiskTotal

Initial risk total

## adjustedImf

Adjusted margin fraction

## leverage

Leverage of the position

## maxLeverage

Max leverage allowed for the position, calculated from adjustedImf

## buyingPower

Buying power for the position

## liquidationPrice

Liquidation price for the position

# SubaccountPositionResources

data class SubaccountPositionResources(  
&emsp;val sideStringKey: TradeStatesWithStringValues,  
&emsp;val indicator: TradeStatesWithStringValues  
)

## sideStringKey

The localization string key to display side

## indicator

The icon resource

# SubaccountOrders

An array of [SubaccountOrder](#SubaccountOrder)

# SubaccountOrder

data class SubaccountOrder(
&emsp;val subaccountNumber: Int,
&emsp;val id: String,  
&emsp;val clientId: Int?,  
&emsp;val type: OrderType,  
&emsp;val side: OrderSide,  
&emsp;val status: OrderStatus,  
&emsp;val timeInForce: OrderTimeInForce?,  
&emsp;val marketId: String,  
&emsp;val clobPairId: Int?,  
&emsp;val price: Double,  
&emsp;val triggerPrice: Double?,  
&emsp;val trailingPercent: Double?,  
&emsp;val size: Double,  
&emsp;val remainingSize: Double?,  
&emsp;val totalFilled: Double?,  
&emsp;val createdAtMilliseconds: Double?,  
&emsp;val unfillableAtMilliseconds: Double?,  
&emsp;val expiresAtMilliseconds: Double?,  
&emsp;val postOnly: Boolean,  
&emsp;val reduceOnly: Boolean,  
&emsp;val cancelReason: String?,  
&emsp;val resources: [SubaccountOrderResources](#SubaccountOrderResources)  
)

## subaccountNumber

The subaccount number that placed the order

## id

Order ID

## clientId

Client assigned ID. Randomly generated in Abacus

## type

Order type

&emsp;market  
&emsp;stopMarket  
&emsp;takeProfitMarket  
&emsp;limit  
&emsp;stopLimit  
&emsp;takeProfitLimit  
&emsp;trailingStop  
&emsp;liquidated  
&emsp;liquidation  

## side

BUY or SELL

## status

Order status

&emsp;cancelled  
&emsp;bestEffortCancelled  
&emsp;filled  
&emsp;open  
&emsp;pending  
&emsp;untriggered  

## timeInForce

Time in force, GTT (Good Til Time) or  IOC (Immediate or Cancel)

## marketId

Market ID

## clobPairId

Clob Pair ID used by dYdX chain

## price

Price

## triggerPrice

Trigger price

## trailingPercent

Trailing percent for Trailing Stop order

## size

Size of the order

## remainingSize

If the order has been partially executed, the remaining size

## totalFilled

If the order has been partially executed, total size filled

## createdAtMilliseconds

When the order was created

## unfillableAtMilliseconds

Order was marked as unfillable

## expiresAtMilliseconds

Expiration time

## postOnly

Order is post-only

## reduceOnly

Order is reduce-only

## cancelReason

If order was canceled, the reason for canncellation

# SubaccountOrderResources

data class SubaccountOrderResources(  
&emsp;val sideStringKey: String,  
&emsp;val typeStringKey: String?,  
&emsp;val statusStringKey: String?,  
&emsp;val timeInForceStringKey: String?  
)

## sideStringKey

Localization string key to display the side

## typeStringKey

Localization string key to display the type

## statusStringKey

Localization string key to display the status

## timeInForceStringKey

Localization string key to display the timeInForce