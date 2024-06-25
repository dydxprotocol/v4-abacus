package exchange.dydx.abacus.state.manager

import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kotlinx.serialization.Serializable

internal data class Subaccount(
    val address: String,
    val subaccountNumber: Int,
)

data class FaucetRecord(
    val subaccountNumber: Int,
    val amount: Double,
    val timestampInMilliseconds: Double,
)

data class PlaceOrderRecord(
    val subaccountNumber: Int,
    val clientId: Int,
    val timestampInMilliseconds: Double,
    val fromSlTpDialog: Boolean,
    var lastOrderStatus: OrderStatus?,
)

data class CancelOrderRecord(
    val subaccountNumber: Int,
    val clientId: Int,
    val timestampInMilliseconds: Double,
    val fromSlTpDialog: Boolean,
)

@JsExport
@Serializable
data class PlaceOrderMarketInfo(
    val clobPairId: Int,
    val atomicResolution: Int,
    val stepBaseQuantums: Int,
    val quantumConversionExponent: Int,
    val subticksPerTick: Int,
)

@JsExport
@Serializable
data class HumanReadablePlaceOrderPayload(
    val subaccountNumber: Int,
    val marketId: String,
    val clientId: Int,
    val type: String,
    val side: String,
    val price: Double,
    val triggerPrice: Double?,
    val size: Double,
    val reduceOnly: Boolean?,
    val postOnly: Boolean?,
    val timeInForce: String?,
    val execution: String?,
    val goodTilTimeInSeconds: Int?,
    val goodTilBlock: Int?,
    val marketInfo: PlaceOrderMarketInfo? = null,
    val currentHeight: Int? = null,
)

@JsExport
@Serializable
data class HumanReadableCancelOrderPayload(
    val subaccountNumber: Int,
    val type: String,
    val orderId: String,
    val clientId: Int,
    val orderFlags: Int,
    val clobPairId: Int,
    val goodTilBlock: Int?,
    val goodTilBlockTime: Int?,
)

@JsExport
@Serializable
data class HumanReadableTriggerOrdersPayload(
    val marketId: String,
    val positionSize: Double?,
    val placeOrderPayloads: IList<HumanReadablePlaceOrderPayload>,
    val cancelOrderPayloads: IList<HumanReadableCancelOrderPayload>,
)

@JsExport
@Serializable
data class HumanReadableSubaccountTransferPayload(
    val senderAddress: String,
    val subaccountNumber: Int,
    val amount: String,
    val destinationAddress: String,
    val destinationSubaccountNumber: Int,
)

@JsExport
@Serializable
data class HumanReadableFaucetPayload(
    val subaccountNumber: Int,
    val amount: Double,
)

@JsExport
@Serializable
data class HumanReadableDepositPayload(
    val subaccountNumber: Int,
    val amount: String,
)

@JsExport
@Serializable
data class HumanReadableWithdrawPayload(
    val subaccountNumber: Int,
    val amount: String,
)

@JsExport
@Serializable
data class HumanReadableWithdrawIBCPayload(
    val subaccountNumber: Int,
    val amount: String,
    val ibcPayload: String,
)

@JsExport
@Serializable
data class HumanReadableTransferPayload(
    val subaccountNumber: Int,
    val amount: String,
    val recipient: String,
)

@JsExport
@Serializable
data class TransferChainInfo(
    val chainName: String,
    val chainId: String,
    val logoUri: String,
    val chainType: String,
    val isTestnet: Boolean,
)
