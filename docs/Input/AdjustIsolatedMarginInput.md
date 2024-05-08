# AdjustIsolatedMarginInput

data class AdjustIsolatedMarginInput(  
&emsp;val type: String?, // "ADD" or "REMOVE"
&emsp;val amount: Double?,
&emsp;val childSubaccountNumber: Int?,
)

## type

ADD - Add margin to the child's isolated margin account from the parent's cross margin account
REMOVE - Remove margin from the child's isolated margin account to the parent's cross margin account

## amount

Amount of USDC to remove or add

## childSubaccountNumber

Subaccount number for the child whose margin is to be adjusted

