package exchange.dydx.abacus.output

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.Logger
import kollections.JsExport
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
    val feeDiscounts: IList<FeeDiscount>?
) {
    companion object {
        internal fun create(
            existing: Configs?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            localizer: LocalizerProtocol?,
        ): Configs? {
            data?.let {
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
                return if (existing?.network !== network ||
                    existing?.feeTiers != feeTiers ||
                    existing?.feeDiscounts != feeDiscounts
                ) {
                    Configs(
                        network,
                        feeTiers,
                        feeDiscounts,
                    )
                } else {
                    existing ?: Configs(
                        null,
                        null,
                        null,
                    )
                }
            }
            Logger.d { "Configs not valid" }
            return null
        }
    }
}
