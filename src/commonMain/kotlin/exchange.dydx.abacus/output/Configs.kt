package exchange.dydx.abacus.output

import exchange.dydx.abacus.state.manager.ChainRpcMap
import exchange.dydx.abacus.utils.IList
import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class FeeDiscountResources(
    val string: String?,
    val stringKey: String,
)

@JsExport
@Serializable
data class FeeDiscount(
    val id: String,
    val tier: String,
    val symbol: String,
    val balance: Int,
    val discount: Double?,
    val resources: FeeDiscountResources?
)

@JsExport
@Serializable
data class FeeTierResources(
    val string: String?,
    val stringKey: String
)

@JsExport
@Serializable
data class FeeTier(
    val id: String,
    val tier: String,
    val symbol: String,
    val volume: Int,
    val totalShare: Double?,
    val makerShare: Double?,
    val maker: Double?,
    val taker: Double?,
    val resources: FeeTierResources?
)

@JsExport
@Serializable
data class EquityTiers(
    val shortTermOrderEquityTiers: IList<EquityTier>,
    val statefulOrderEquityTiers: IList<EquityTier>,
)

@JsExport
@Serializable
data class EquityTier(
    val requiredTotalNetCollateralUSD: Double,
    val nextLevelRequiredTotalNetCollateralUSD: Double?,
    val maxOrders: Int,
)

@JsExport
@Serializable
data class WithdrawalGating(
    val withdrawalsAndTransfersUnblockedAtBlock: Int?
)

@JsExport
@Serializable
data class WithdrawalCapacity(
    val capacity: String?
)

@JsExport
@Serializable
data class NetworkConfigs(
    val api: String?,
    val node: String?
)

@JsExport
@Serializable
data class Configs(
    val network: NetworkConfigs?,
    val feeTiers: IList<FeeTier>?,
    val feeDiscounts: IList<FeeDiscount>?,
    val equityTiers: EquityTiers?,
    val withdrawalGating: WithdrawalGating?,
    val withdrawalCapacity: WithdrawalCapacity?,
    val rpcMap: ChainRpcMap?
)
