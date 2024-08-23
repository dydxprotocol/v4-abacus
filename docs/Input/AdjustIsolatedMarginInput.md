# AdjustIsolatedMarginInput

data class AdjustIsolatedMarginInput(  
&emsp;val marketId: String?,  
&emsp;val type: IsolatedMarginAdjustmentType, // "ADD" or "REMOVE"
&emsp;val amount: String?,
&emsp;val amountPercent: String?,
&emsp;val amountInput: IsolatedMarginInputType?, // "AMOUNT" or "PERCENT"
&emsp;val childSubaccountNumber: Int?,
&emsp;val adjustIsolatedMarginInputOptions: AdjustIsolatedMarginInputOptions?,
&emsp;val summary: AdjustIsolatedMarginInputSummary?,
)

## marketId

MarketId

## type

ADD - Add margin to the child's isolated margin account from the parent's cross margin account
REMOVE - Remove margin from the child's isolated margin account to the parent's cross margin account

## amount

Amount of USDC to remove or add

## amountPercent

Percentage of available USDC to remove or add; max percentage is calculated from market's max leverage

## amountInput

AMOUNT - USDC amount
PERCENT - percent of max possible position (capped by current position leverage relative to market's max leverage)

## childSubaccountNumber

Subaccount number for the child whose margin is to be adjusted