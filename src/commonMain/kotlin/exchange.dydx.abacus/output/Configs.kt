package exchange.dydx.abacus.output

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class FeeDiscountResources(
    val string: String?,
    val stringKey: String,
) {
    companion object {
        internal fun create(
            existing: FeeDiscountResources?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): FeeDiscountResources? {
            if (data == null) {
                Logger.d { "Fee Discount Resources not valid" }
                return null
            }
            val stringKey = parser.asString(data["stringKey"])
            return if (stringKey != null) {
                if (existing?.stringKey != stringKey) {
                    val string = localizer?.localize(stringKey)
                    FeeDiscountResources(
                        string,
                        stringKey,
                    )
                } else {
                    existing
                }
            } else {
                null
            }
        }
    }
}

@JsExport
@Serializable
data class FeeDiscount(
    val id: String,
    val tier: String,
    val symbol: String,
    val balance: Int,
    val discount: Double?,
    val resources: FeeDiscountResources?
) {
    companion object {
        internal fun create(
            existing: IList<FeeDiscount>?,
            parser: ParserProtocol,
            data: List<*>?,
            localizer: LocalizerProtocol?,
        ): IList<FeeDiscount>? {
            data?.let {
                val feeDiscounts = iMutableListOf<FeeDiscount>()
                for (i in data.indices) {
                    val item = data[i]
                    parser.asMap(item)?.let {
                        val discount = existing?.getOrNull(i)
                        FeeDiscount.create(discount, parser, it, localizer)?.let { feeDiscount ->
                            feeDiscounts.add(feeDiscount)
                        }
                    }
                }
                return feeDiscounts
            }
            Logger.d { "Fee Discounts not valid" }
            return null
        }

        internal fun create(
            existing: FeeDiscount?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): FeeDiscount? {
            data?.let {
                val id = parser.asString(data["id"])
                val tier = parser.asString(data["tier"])
                val symbol = parser.asString(data["symbol"])
                val balance = parser.asInt(data["balance"])
                val discount = parser.asDouble(data["discount"]) ?: 0.0
                val resourcesData = parser.asMap(data["resources"])
                if (id != null && tier != null && symbol != null && balance != null) {
                    val resources =
                        FeeDiscountResources.create(
                            existing?.resources,
                            parser,
                            resourcesData,
                            localizer,
                        )
                    return if (existing?.id != id ||
                        existing.tier != tier ||
                        existing.symbol != symbol ||
                        existing.balance != balance ||
                        existing.discount != discount ||
                        existing.resources !== resources
                    ) {
                        FeeDiscount(id, tier, symbol, balance, discount, resources)
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Fee Discount not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class FeeTierResources(
    val string: String?,
    val stringKey: String
) {
    companion object {
        internal fun create(
            existing: FeeTierResources?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): FeeTierResources? {
            data?.let {
                val stringKey = parser.asString(data["stringKey"])

                return if (stringKey != null) {
                    if (existing?.stringKey != stringKey) {
                        val string = localizer?.localize(stringKey)
                        FeeTierResources(
                            string,
                            stringKey,
                        )
                    } else {
                        existing
                    }
                } else {
                    null
                }
            }
            Logger.d { "Fee Tier Resources not valid" }
            return null
        }
    }
}

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
) {
    companion object {
        internal fun create(
            existing: IList<FeeTier>?,
            parser: ParserProtocol,
            data: List<*>?,
            localizer: LocalizerProtocol?,
        ): IList<FeeTier>? {
            data?.let {
                val feeTiers = iMutableListOf<FeeTier>()
                for (i in data.indices) {
                    val item = data[i]
                    parser.asMap(item)?.let {
                        val tier = existing?.getOrNull(i)
                        FeeTier.create(tier, parser, it, localizer)?.let { feeTier ->
                            feeTiers.add(feeTier)
                        }
                    }
                }
                return feeTiers
            }
            Logger.d { "Fee Tiers not valid" }
            return null
        }

        internal fun create(
            existing: FeeTier?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): FeeTier? {
            data?.let {
                val id = parser.asString(data["id"])
                val tier = parser.asString(data["tier"])
                val symbol = parser.asString(data["symbol"])
                val volume = parser.asInt(data["volume"])
                val makerShare = parser.asDouble(data["makerShare"])
                val totalShare = parser.asDouble(data["totalShare"])
                val maker = parser.asDouble(data["maker"]) ?: 0.0
                val taker = parser.asDouble(data["taker"]) ?: 0.0
                val resourcesData = parser.asMap(data["resources"])
                if (id != null && tier != null && symbol != null && volume != null) {
                    val resources =
                        FeeTierResources.create(
                            existing?.resources,
                            parser,
                            resourcesData,
                            localizer,
                        )
                    return if (existing?.id != id ||
                        existing.tier != tier ||
                        existing.symbol != symbol ||
                        existing.volume != volume ||
                        existing.makerShare != makerShare ||
                        existing.totalShare != totalShare ||
                        existing.maker != maker ||
                        existing.taker != taker ||
                        existing.resources !== resources
                    ) {
                        FeeTier(
                            id,
                            tier,
                            symbol,
                            volume,
                            totalShare,
                            makerShare,
                            maker,
                            taker,
                            resources,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Fee Tier not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class EquityTiers(
    val shortTermOrderEquityTiers: IList<EquityTier>,
    val statefulOrderEquityTiers: IList<EquityTier>,
) {
    companion object {
        internal fun create(
            existing: EquityTiers?,
            parser: ParserProtocol,
            data: Map<*, *>?,
        ): EquityTiers? {
            data?.let {
                val shortTermOrderEquityTiers = parser.asNativeList(data["shortTermOrderEquityTiers"])?.let { tiers ->
                    create(existing?.shortTermOrderEquityTiers, parser, tiers)
                } ?: iListOf()

                val statefulOrderEquityTiers = parser.asNativeList(data["statefulOrderEquityTiers"])?.let { tiers ->
                    create(existing?.statefulOrderEquityTiers, parser, tiers)
                } ?: iListOf()

                return EquityTiers(shortTermOrderEquityTiers, statefulOrderEquityTiers)
            }

            Logger.d { "Equity Tiers not valid" }
            return null
        }

        internal fun create(
            existing: IList<EquityTier>?,
            parser: ParserProtocol,
            data: List<*>?
        ): IList<EquityTier>? {
            data?.let {
                val equityTiers = iMutableListOf<EquityTier>()
                for (i in data.indices) {
                    val item = data[i]
                    val nextItem = data.getOrNull(i + 1)
                    parser.asMap(item)?.let {
                        val tier = existing?.getOrNull(i)
                        val nextTierData = parser.asMap(nextItem)
                        EquityTier.create(tier, parser, it, nextTierData)?.let { equityTier ->
                            equityTiers.add(equityTier)
                        }
                    }
                }
                return equityTiers
            }
            Logger.d { "Equity Tiers not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class EquityTier(
    val requiredTotalNetCollateralUSD: Double,
    val nextLevelRequiredTotalNetCollateralUSD: Double?,
    val maxOrders: Int,
) {
    companion object {
        internal fun create(
            existing: EquityTier?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            nextTierData: Map<*, *>?
        ): EquityTier? {
            data?.let {
                val requiredTotalNetCollateralUSD = parser.asDouble(data["requiredTotalNetCollateralUSD"])
                val nextLevelRequiredTotalNetCollateralUSD = nextTierData?.let {
                    parser.asDouble(nextTierData["requiredTotalNetCollateralUSD"])
                }
                val maxOrders = parser.asInt(data["maxOrders"])

                if (requiredTotalNetCollateralUSD != null && maxOrders != null) {
                    return if (
                        existing?.requiredTotalNetCollateralUSD != requiredTotalNetCollateralUSD ||
                        existing?.nextLevelRequiredTotalNetCollateralUSD != nextLevelRequiredTotalNetCollateralUSD ||
                        existing?.maxOrders != maxOrders
                    ) {
                        EquityTier(
                            requiredTotalNetCollateralUSD,
                            nextLevelRequiredTotalNetCollateralUSD,
                            maxOrders,
                        )
                    } else {
                        existing
                    }
                }
            }
            Logger.d { "Equity Tier not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class WithdrawalGating(
    val withdrawalsAndTransfersUnblockedAtBlock: Int?
) {
    companion object {
        internal fun create(
            existing: WithdrawalGating?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): WithdrawalGating? {
            data?.let {
                val withdrawalsAndTransfersUnblockedAtBlock =
                    parser.asInt(data["withdrawalsAndTransfersUnblockedAtBlock"])
                return if (existing?.withdrawalsAndTransfersUnblockedAtBlock != withdrawalsAndTransfersUnblockedAtBlock) {
                    WithdrawalGating(
                        withdrawalsAndTransfersUnblockedAtBlock,
                    )
                } else {
                    existing
                }
            }
            return null
        }
    }
}

@JsExport
@Serializable
data class WithdrawalCapacity(
    val capacity: String?
) {
    companion object {
        internal fun create(
            existing: WithdrawalCapacity?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): WithdrawalCapacity? {
            return data?.let {
                return parser.asList(data["limiterCapacityList"])?.let {
                    if (it.size != 2) {
                        return null
                    }
                    var dailyLimit = parser.asDecimal(parser.asMap(it[0])?.get("capacity"))
                    var weeklyLimit = parser.asDecimal(parser.asMap(it[1])?.get("capacity"))
                    var capacity: BigDecimal? = null
                    if (dailyLimit != null && weeklyLimit != null) {
                        if (dailyLimit < weeklyLimit) {
                            capacity = dailyLimit
                        } else {
                            capacity = weeklyLimit
                        }
                    }
                    var capacityAsString = parser.asString(capacity)
                    return if (existing?.capacity != capacityAsString) WithdrawalCapacity(capacityAsString) else existing
                }
            }
        }
    }
}

@JsExport
@Serializable
data class NetworkConfigs(
    val api: String?,
    val node: String?
) {
    companion object {
        internal fun create(
            existing: NetworkConfigs?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): NetworkConfigs? {
            data?.let {
                val api = parser.asString(data["api"])
                val node = parser.asString(data["node"])
                return if (existing?.api != api ||
                    existing?.node != node
                ) {
                    NetworkConfigs(
                        api,
                        node,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Network Configs not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class Configs(
    val network: NetworkConfigs?,
    val feeTiers: IList<FeeTier>?,
    val feeDiscounts: IList<FeeDiscount>?,
    val equityTiers: EquityTiers?,
    val withdrawalGating: WithdrawalGating?,
    val withdrawalCapacity: WithdrawalCapacity?,
) {
    companion object {
        internal fun create(
            existing: Configs?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): Configs? {
            if (data != null) {
                val network =
                    NetworkConfigs.create(existing?.network, parser, parser.asMap(data["network"]))

                val feeTiers = FeeTier.create(
                    existing?.feeTiers,
                    parser,
                    parser.asList(data["feeTiers"]),
                    localizer,
                )

                val feeDiscounts = FeeDiscount.create(
                    existing?.feeDiscounts,
                    parser,
                    parser.asList(data["feeDiscounts"]),
                    localizer,
                )

                val equityTiers = EquityTiers.create(
                    existing?.equityTiers,
                    parser,
                    parser.asMap(data["equityTiers"]),
                )

                val withdrawalGating = WithdrawalGating.create(
                    existing?.withdrawalGating,
                    parser,
                    parser.asMap(data["withdrawalGating"]),
                )

                val withdrawalCapacity = WithdrawalCapacity.create(
                    existing?.withdrawalCapacity,
                    parser,
                    parser.asMap(data["withdrawalCapacity"]),
                )

                return if (existing?.network !== network ||
                    existing?.feeTiers != feeTiers ||
                    existing?.feeDiscounts != feeDiscounts ||
                    existing?.equityTiers != equityTiers ||
                    existing?.withdrawalGating != withdrawalGating ||
                    existing?.withdrawalCapacity != withdrawalCapacity
                ) {
                    Configs(
                        network = network,
                        feeTiers = feeTiers,
                        feeDiscounts = feeDiscounts,
                        equityTiers = equityTiers,
                        withdrawalGating = withdrawalGating,
                        withdrawalCapacity = withdrawalCapacity,
                    )
                } else {
                    existing ?: Configs(
                        network = null,
                        feeTiers = null,
                        feeDiscounts = null,
                        equityTiers = null,
                        withdrawalGating = null,
                        withdrawalCapacity = null,
                    )
                }
            }
            Logger.d { "Configs not valid" }
            return null
        }
    }
}
