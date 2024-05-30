# TransferInput

data class TransferInput(  
&emsp;val type: TransferType?,  
&emsp;val size: TransferInputSize?,  
&emsp;val fastSpeed: Boolean,  
&emsp;val fee: Double?,
&emsp;val chain: String?,
&emsp;val address: String?,
&emsp;val memo: String?,
&emsp;val depositOptions: DepositInputOptions?,  
&emsp;val withdrawalOptions: WithdrawalInputOptions?,  
&emsp;val transferOutOptions: TransferOutInputOptions?,
&emsp;val summary: TransferInputSummary?,
&emsp;val resources: TransferInputResources?,
&emsp;val requestPayload: TransferInputRequestPayload?
)

## type

deposit,
withdrawal,
transferOut;

## size

Size of the transfer

## fastSpeed

Only for v3 withdrawal, option to use fast withdrawal

## fee

Total amount of fee, deducted from the size

## chain

Selected chain to perform the transfer

## address

Selected token address of the chain to perform the transfer

## memo

Memo for transfer

## depositOptions

structure of [DepositInputOptions](#DepositInputOptions)

## withdrawalOptions

structure of [WithdrawalInputOptions](#WithdrawalInputOptions)

## transferOutOptions

structure of [TransferOutInputOptions](#TransferOutInputOptions)

## summary

structure of [TransferInputSummary](#TransferInputSummary)

## resources

structure of [TransferInputResources](#TransferInputResources)

## requestPayload

structure of [TransferInputRequestPayload](#TransferInputRequestPayload)

# DepositInputOptions

data class DepositInputOptions(  
&emsp;val needsSize: Boolean?,  
&emsp;val needsAddress: Boolean?,  
&emsp;val needsFastSpeed: Boolean?,  
&emsp;val assets: Array<SelectionOption>?  
)

## needsSize

UX should let user enter the size

## needsAddress

UX should let user enter a wallet address

## needsFastSpeed

UX should let the user choose whether to use fast speed

## assets

Option of assets to choose from

# WithdrawalInputOptions

data class DepositInputOptions(  
&emsp;val needsSize: Boolean?,  
&emsp;val needsAddress: Boolean?,  
&emsp;val needsFastSpeed: Boolean?,  
&emsp;val exchanges: Array<SelectionOption>?  
&emsp;val chains: Array<SelectionOption>?  
&emsp;val assets: Array<SelectionOption>?  
)

## needsSize

UX should let user enter the size

## needsAddress

UX should let user enter a wallet address

## needsFastSpeed

UX should let the user choose whether to use fast speed

## exchanges

Option of exchanges to choose from

## chains

Option of chains to choose from

## assets

Option of assets to choose from

# TransferOutInputOptions

data class TransferOutInputOptions(  
&emsp;val needsSize: Boolean?,  
&emsp;val needsAddress: Boolean?,  
&emsp;val chains: Array<SelectionOption>?,
&emsp;val assets: Array<SelectionOption>?  
)

## needsSize

UX should let user enter the size

## needsAddress

UX should let user enter a wallet address

## chains

Option of chains to choose from

## assets

Option of assets to choose from

# TransferInputSummary

data class TransferInputSummary(  
&emsp;val usdcSize: Double?,  
&emsp;val fee: Double?,  
&emsp;val filled: Boolean  
)

## usdcSize

Amount of USDC

## fee

Fee in USDC

## filled

Whether the transfer transaction can be filled

# TransferInputResources

The chain and token resources of the selected chain and its associated tokens. Use the chainId
and token address of the key to the maps, respectively, to get the resource.

data class TransferInputResources(
&emsp;var chainResources: Map<String, TransferInputChainResource>?,
&emsp;var tokenResources: Map<String, TransferInputTokenResource>?
)

# TransferInputChainResource

The chain resource of the selected chain

data class TransferInputChainResource(
&emsp;val chainName: String?,
&emsp;val rpc: String?,
&emsp;val networkName: String?,
&emsp;val chainId: Int?,
&emsp;val iconUrl: String?
)

# TransferInputTokenResource

The token resource of the selected token

data class TransferInputTokenResource(
&emsp;var name: String?,
&emsp;var address: String?,
&emsp;var symbol: String?,
&emsp;var decimals: Int?,
&emsp;var iconUrl: String?
)

# TransferInputRequestPayload

Payload needed for the client to sign call sendTransaction()

data class TransferInputRequestPayload(
&emsp;val routeType: String?,
&emsp;val targetAddress: String?,
&emsp;val data: String?,
&emsp;val value: String?,
&emsp;val gasLimit: String?,
&emsp;val gasPrice: String?,
&emsp;val maxFeePerGas: String?,
&emsp;val maxPriorityFeePerGas: String?
)
