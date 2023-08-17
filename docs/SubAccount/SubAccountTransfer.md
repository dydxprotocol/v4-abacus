# SubaccountTransfer

data class SubaccountTransfer(  
&emsp;val id: String,  
&emsp;val debitAsset: String?,  
&emsp;val creditAsset: String?,  
&emsp;val debitAmount: Double?,  
&emsp;val creditAmount: Double?,  
&emsp;val updatedAtMilliseconds: Double,  
&emsp;val fromAddress: String?,  
&emsp;val toAddress: String?,  
&emsp;val resources: [SubaccountTransferResources](#SubaccountTransferResources)  
)

## id

Transfer ID

## debitAsset

Debit asset. Can be one of the supported tokens for deposit, "USDC" for withdraw

## creditAsset

Credit asset. Should be USDC

## updatedAtMilliseconds

When the transfer was updated

## fromAddress

Wallet address of the transfer

## toAddress

Destination address of the transfer

## resources

# SubaccountTransferResources

data class SubaccountTransferResources(  
&emsp;val typeStringKey: String?,  
&emsp;val blockExplorerUrl: String?,  
&emsp;val statusStringKey: String?,  
&emsp;val iconLocal: String?,  
&emsp;val indicator: String?  
)

## typeString

Localization string key for the transfer type

## blockExplorerUrl

URL to see the contract call in block explorer

## statusStringKey

Localization string key for the status

## iconLocal

Icon file to display the transfer

## indicator

Additional indicator image