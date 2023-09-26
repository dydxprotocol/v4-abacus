package exchange.dydx.abacus.output


import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.DebugLogger
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.JsExport
import kollections.iMutableListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class FeeDiscountResources(
    val stringKey: String?
) {
    companion object {
        internal fun create(
            existing: FeeDiscountResources?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): FeeDiscountResources? {
            if (data == null) {
                DebugLogger.debug("Fee Discount Resources not valid")
                return null
            }
            val stringKey = parser.asString(data["stringKey"])
            return if (existing?.stringKey != stringKey) {
                FeeDiscountResources(
                    stringKey
                )
            } else {
                existing
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
            data: List<*>?
        ): IList<FeeDiscount>? {
            data?.let {
                val feeDiscounts = iMutableListOf<FeeDiscount>()
                for (i in data.indices) {
                    val item = data[i]
                    parser.asMap(item)?.let {
                        val discount = existing?.getOrNull(i)
                        FeeDiscount.create(discount, parser, it)?.let { feeDiscount ->
                            feeDiscounts.add(feeDiscount)
                        }
                    }
                }
                return feeDiscounts
            }
            DebugLogger.debug("Fee Discounts not valid")
            return null
        }

        internal fun create(
            existing: FeeDiscount?,
            parser: ParserProtocol,
            data: Map<*, *>?
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
                        FeeDiscountResources.create(existing?.resources, parser, resourcesData)
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
            DebugLogger.debug("Fee Discount not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class FeeTierResources(
    val stringKey: String?
) {
    companion object {
        internal fun create(
            existing: FeeTierResources?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): FeeTierResources? {
            data?.let {
                val stringKey = parser.asString(data["stringKey"])

                return if (existing?.stringKey != stringKey) {
                    FeeTierResources(
                        stringKey
                    )
                } else {
                    existing
                }
            }
            DebugLogger.debug("Fee Tier Resources not valid")
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
            data: List<*>?
        ): IList<FeeTier>? {
            data?.let {
                val feeTiers = iMutableListOf<FeeTier>()
                for (i in data.indices) {
                    val item = data[i]
                    parser.asMap(item)?.let {
                        val tier = existing?.getOrNull(i)
                        FeeTier.create(tier, parser, it)?.let { feeTier ->
                            feeTiers.add(feeTier)
                        }
                    }
                }
                return feeTiers
            }
            DebugLogger.debug("Fee Tiers not valid")
            return null
        }

        internal fun create(
            existing: FeeTier?,
            parser: ParserProtocol,
            data: Map<*, *>?
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
                        FeeTierResources.create(existing?.resources, parser, resourcesData)
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
                        FeeTier(id, tier, symbol, volume, totalShare, makerShare, maker, taker, resources)
                    } else {
                        existing
                    }
                }
            }
            DebugLogger.debug("Fee Tier not valid")
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
                        node
                    )
                } else {
                    existing
                }
            }
            DebugLogger.debug("Network Configs not valid")
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
            data: Map<*, *>?
        ): Configs? {
            data?.let {
                val network =
                    NetworkConfigs.create(existing?.network, parser, parser.asMap(data["network"]))
                val feeTiers =
                    FeeTier.create(existing?.feeTiers, parser, parser.asList(data["feeTiers"]))
                val feeDiscounts = FeeDiscount.create(
                    existing?.feeDiscounts, parser, parser.asList(
                        data["feeDiscounts"]
                    )
                )
                return if (existing?.network !== network ||
                    existing?.feeTiers != feeTiers ||
                    existing?.feeDiscounts != feeDiscounts
                ) {
                    Configs(
                        network,
                        feeTiers,
                        feeDiscounts
                    )
                } else {
                    existing ?: Configs(
                        null,
                        null,
                        null,
                    )
                }
            }
            DebugLogger.debug("Configs not valid")
            return null
        }
    }
}