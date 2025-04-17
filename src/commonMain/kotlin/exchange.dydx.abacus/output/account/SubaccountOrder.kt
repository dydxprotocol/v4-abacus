package exchange.dydx.abacus.output.account

import exchange.dydx.abacus.output.input.MarginMode
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderStatus
import exchange.dydx.abacus.output.input.OrderTimeInForce
import exchange.dydx.abacus.output.input.OrderType
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class SubaccountOrder(
    val subaccountNumber: Int?,
    val id: String,
    val clientId: String?,
    val type: OrderType,
    val side: OrderSide,
    val status: OrderStatus,
    val timeInForce: OrderTimeInForce?,
    val marketId: String,
    val displayId: String,
    val clobPairId: Int?,
    val orderFlags: Int?,
    val price: Double,
    val triggerPrice: Double?,
    val trailingPercent: Double?,
    val size: Double,
    val remainingSize: Double?,
    val totalFilled: Double?,
    val goodTilBlock: Int?,
    val goodTilBlockTime: Int?,
    val createdAtHeight: Int?,
    val createdAtMilliseconds: Double?,
    val unfillableAtMilliseconds: Double?,
    val expiresAtMilliseconds: Double?,
    val updatedAtMilliseconds: Double?,
    val postOnly: Boolean,
    val reduceOnly: Boolean,
    val cancelReason: String?,
    val resources: SubaccountOrderResources,
    val marginMode: MarginMode?
)

/*
typeStringKey and statusStringKey are set to optional, in case
BE returns new enum values which Abacus doesn't recognize
*/
@JsExport
@Serializable
data class SubaccountOrderResources(
    val sideString: String?,
    val typeString: String?,
    val statusString: String?,
    val timeInForceString: String?,
    val sideStringKey: String,
    val typeStringKey: String?,
    val statusStringKey: String?,
    val timeInForceStringKey: String?,
)
