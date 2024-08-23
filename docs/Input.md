# Input

Input is a generic object representing the input state. There is always only one input active at one time, and the account state is calculated based on the current input

data class Input(
&emsp;val current: String?,
&emsp;val trade: [TradeInput](Input/TradeInput.md)?,
&emsp;val closePosition: [ClosePositionInput](Input/ClosePosition.md)?,
&emsp;val transfer: [TransferInput](Input/TransferInput.md)?,
&emsp;val triggerOrders: [TriggerOrdersInput](Input/TriggerOrdersInput.md)?,
&emsp;val adjustIsolatedMargin: [AdjustIsolatedMarginInput](Input/AdjustIsolatedMarginInput.md)?,
&emsp;val receiptLines: ReceiptLines?,
&emsp;val errors: Array<ValidationError>?
)

## current

Indicating the current input:
trade
transfer
closePosition
triggerOrders
adjustIsolatedMargin

## trade

Trade input object

## closePosition

Close Position input object

## transfer

Transfer state

## triggerOrders

Trigger Order Dialog input object

## adjustIsolatedMargin

Adjust Isolated Margin Dialog input object

## receiptLines

A list of receipt lines to be displayed in the receipt area

## errors

A list of errors or warning for the current input

