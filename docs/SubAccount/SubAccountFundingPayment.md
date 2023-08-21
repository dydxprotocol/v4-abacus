# SubaccountFundingPayment

data class SubaccountFundingPayment(  
&emsp;val marketId: String,  
&emsp;val payment: Double,  
&emsp;val rate: Double,  
&emsp;val positionSize: Double,  
&emsp;val price: Double?,  
&emsp;val effectiveAtMilliSeconds: Double  
)

## marketId

Market ID of the funding payment

## payment

Payment amount, can be negative

## rate

Funding rate

## positionSize

Position size at the moment of payment

## price

price

## effectiveAtMilliSeconds

Timestamp of the funding payment