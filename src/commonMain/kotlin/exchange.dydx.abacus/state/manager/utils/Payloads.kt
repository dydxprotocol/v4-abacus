package exchange.dydx.abacus.state.manager

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
)

data class CancelOrderRecord(
    val subaccountNumber: Int,
    val clientId: Int,
    val timestampInMilliseconds: Double,
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
    val orderId: String,
    val type: String,
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
    val placeOrderPayloads: List<HumanReadablePlaceOrderPayload>,
    val cancelOrderPayloads: List<HumanReadableCancelOrderPayload>,
)

@JsExport
@Serializable
data class HumanReadableSubaccountTransferPayload(
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
