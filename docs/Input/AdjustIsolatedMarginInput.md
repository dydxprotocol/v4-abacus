# AdjustIsolatedMarginInput

data class AdjustIsolatedMarginInput(  
&emsp;val market: String?,  
&emsp;val type: IsolatedMarginAdjustmentType, // "ADD" or "REMOVE"
&emsp;val amount: String?,
&emsp;val amountPercent: String?,
&emsp;val amountInput: IsolatedMarginInputType?, // "AMOUNT" or "PERCENT"
&emsp;val childSubaccountNumber: Int?,
&emsp;val adjustIsolatedMarginInputOptions: AdjustIsolatedMarginInputOptions?,
&emsp;val summary: AdjustIsolatedMarginInputSummary?,
)

## market

Market

## type

ADD - Add margin to the child's isolated margin account from the parent's cross margin account
REMOVE - Remove margin from the child's isolated margin account to the parent's cross margin account

## amount

Amount of USDC to remove or add

## amountPercent

Percentage of available USDC to remove or add; percent of max possible position (capped by current position leverage relative to market's max leverage)

## amountInput

Which one of the fields are entered by the user:
- AMOUNT 
- PERCENT  

## childSubaccountNumber

Subaccount number for the child whose margin is to be adjusted