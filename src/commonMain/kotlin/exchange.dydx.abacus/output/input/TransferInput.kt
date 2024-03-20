package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.manager.CctpConfig.cctpChainIds
import exchange.dydx.abacus.state.manager.ExchangeConfig.exchangeList
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableList
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.iMapOf
import kollections.JsExport
import kollections.iListOf
import kollections.iMutableListOf
import kollections.toIList
import kollections.toIMap
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class DepositInputOptions(
    val needsSize: Boolean?,
    val needsAddress: Boolean?,
    val needsFastSpeed: Boolean?,
    val exchanges: IList<SelectionOption>?,
    val chains: IList<SelectionOption>?,
    val assets: IList<SelectionOption>?
) {
    companion object {
        internal fun create(
            existing: DepositInputOptions?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): DepositInputOptions? {
            Logger.d { "creating Deposit Input Options\n" }

            data?.let {
                val needsSize = parser.asBool(data["needsSize"])
                val needsAddress = parser.asBool(data["needsAddress"])
                val needsFastSpeed = parser.asBool(data["needsFastSpeed"])

                var chains: IMutableList<SelectionOption>? = null
                parser.asList(data["chains"])?.let { data ->
                    chains = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        SelectionOption.create(
                            existing?.chains?.getOrNull(i),
                            parser,
                            parser.asMap(item),
                        )?.let {
                            chains?.add(it)
                        }
                    }
                }

                var assets: IMutableList<SelectionOption>? = null
                parser.asList(data["assets"])?.let { data ->
                    assets = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        SelectionOption.create(
                            existing?.assets?.getOrNull(i),
                            parser,
                            parser.asMap(item),
                        )?.let {
                            assets?.add(it)
                        }
                    }
                }

                var exchanges: IMutableList<SelectionOption>? = null
                exchangeList?.let { data ->
                    exchanges = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        val selection = SelectionOption(item.name, item.label, item.label, item.icon)
                        exchanges?.add(selection)
                    }
                }

                return if (existing?.needsSize != needsSize ||
                    existing?.needsAddress != needsAddress ||
                    existing?.needsFastSpeed != needsFastSpeed ||
                    existing?.chains != chains ||
                    existing?.assets != assets
                ) {
                    DepositInputOptions(
                        needsSize,
                        needsAddress,
                        needsFastSpeed,
                        exchanges,
                        chains,
                        assets,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Transfer Deposit Options not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class WithdrawalInputOptions(
    val needsSize: Boolean?,
    val needsAddress: Boolean?,
    val needsFastSpeed: Boolean?,
    val exchanges: IList<SelectionOption>?,
    val chains: IList<SelectionOption>?,
    val assets: IList<SelectionOption>?
) {
    companion object {
        internal fun create(
            existing: WithdrawalInputOptions?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): WithdrawalInputOptions? {
            Logger.d { "creating Withdrawal Input Options\n" }

            data?.let {
                val needsSize = parser.asBool(data["needsSize"])
                val needsAddress = parser.asBool(data["needsAddress"])
                val needsFastSpeed = parser.asBool(data["needsFastSpeed"])

                var chains: IMutableList<SelectionOption>? = null
                parser.asList(data["chains"])?.let { data ->
                    chains = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        SelectionOption.create(
                            existing?.chains?.getOrNull(i),
                            parser,
                            parser.asMap(item),
                        )?.let {
                            chains?.add(it)
                        }
                    }
                }
                var assets: IMutableList<SelectionOption>? = null
                parser.asList(data["assets"])?.let { data ->
                    assets = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        SelectionOption.create(
                            existing?.assets?.getOrNull(i),
                            parser,
                            parser.asMap(item),
                        )?.let {
                            assets?.add(it)
                        }
                    }
                }

                var exchanges: IMutableList<SelectionOption>? = null
                exchangeList?.let { data ->
                    exchanges = iMutableListOf()
                    for (i in data.indices) {
                        val item = data[i]
                        val selection = SelectionOption(item.name, item.label, item.label, item.icon)
                        exchanges?.add(selection)
                    }
                }

                return if (existing?.needsSize != needsSize ||
                    existing?.needsAddress != needsAddress ||
                    existing?.needsFastSpeed != needsFastSpeed ||
                    existing?.chains != chains ||
                    existing?.assets != assets
                ) {
                    WithdrawalInputOptions(
                        needsSize,
                        needsAddress,
                        needsFastSpeed,
                        exchanges,
                        chains,
                        assets,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Transfer Withdrawal Options not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class TransferOutInputOptions(
    val needsSize: Boolean?,
    val needsAddress: Boolean?,
    val chains: IList<SelectionOption>?,
    val assets: IList<SelectionOption>?
) {
    companion object {
        internal fun create(
            existing: TransferOutInputOptions?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            environment: V4Environment?
        ): TransferOutInputOptions? {
            Logger.d { "creating TransferOut Input Options\n" }

            val needsSize = parser.asBool(data?.get("needsSize")) ?: false
            val needsAddress = parser.asBool(data?.get("needsAddress")) ?: false

            val chainName = environment?.chainName
            val chainOption: SelectionOption = if (chainName != null) {
                SelectionOption(
                    "chain",
                    chainName,
                    null,
                    environment.chainLogo,
                )
            } else {
                return null
            }
            val chains: IList<SelectionOption> = iListOf(chainOption)

            val assets: IList<SelectionOption> = environment.tokens.keys.map { key ->
                val token = environment.tokens[key]!!
                SelectionOption(
                    key,
                    token.name,
                    null,
                    token.imageUrl,
                )
            }.toIList()

            return if (existing?.needsSize != needsSize ||
                existing.needsAddress != needsAddress ||
                existing.chains != chains ||
                existing.assets != assets
            ) {
                TransferOutInputOptions(
                    needsSize,
                    needsAddress,
                    chains,
                    assets,
                )
            } else {
                existing
            }
        }
    }
}

@JsExport
@Serializable
data class TransferInputChainResource(
    val chainName: String?,
    val rpc: String?,
    val networkName: String?,
    val chainId: Int?,
    val iconUrl: String?
) {
    companion object {
        internal fun create(
            existing: TransferInputChainResource?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TransferInputChainResource? {
            Logger.d { "creating Transfer Input Chain Resource\n" }

            data?.let {
                val chainName = parser.asString(data["chainName"])
                val rpc = parser.asString(data["rpc"])
                val networkName = parser.asString(data["networkName"])
                val chainId = parser.asInt(data["chainId"])
                val iconUrl = parser.asString(data["iconUrl"])

                return if (existing?.chainName != chainName ||
                    existing?.rpc != rpc ||
                    existing?.networkName != networkName ||
                    existing?.chainId != chainId ||
                    existing?.iconUrl != iconUrl
                ) {
                    TransferInputChainResource(
                        chainName,
                        rpc,
                        networkName,
                        chainId,
                        iconUrl,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Transfer Input Chain Resource not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class TransferInputTokenResource(
    var name: String?,
    var address: String?,
    var symbol: String?,
    var decimals: Int?,
    var iconUrl: String?
) {
    companion object {
        internal fun create(
            existing: TransferInputTokenResource?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TransferInputTokenResource? {
            Logger.d { "creating Transfer Input Token Resource\n" }

            data?.let {
                val name = parser.asString(data["name"])
                val address = parser.asString(data["address"])
                val symbol = parser.asString(data["symbol"])
                val decimals = parser.asInt(data["decimals"])
                val iconUrl = parser.asString(data["iconUrl"])

                return if (existing?.name != name ||
                    existing?.address != address ||
                    existing?.symbol != symbol ||
                    existing?.decimals != decimals ||
                    existing?.iconUrl != iconUrl
                ) {
                    TransferInputTokenResource(
                        name,
                        address,
                        symbol,
                        decimals,
                        iconUrl,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Transfer Input Token Resource not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class TransferInputResources(
    var chainResources: IMap<String, TransferInputChainResource>?,
    var tokenResources: IMap<String, TransferInputTokenResource>?,
) {
    companion object {
        internal fun create(
            existing: TransferInputResources?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TransferInputResources? {
            Logger.d { "creating Transfer Input Resources\n" }

            data?.let {
                val chainResourcesMap = parser.asMap(data["chainResources"])
                val chainResources: IMap<String, TransferInputChainResource> =
                    chainResourcesMap?.mapValues { entry ->
                        TransferInputChainResource.create(
                            null,
                            parser,
                            parser.asMap(entry.value),
                        ) ?: TransferInputChainResource(null, null, null, null, null)
                    }?.toIMap() ?: iMapOf()

                val tokenResourcesMap = parser.asMap(data["tokenResources"])
                val tokenResources: IMap<String, TransferInputTokenResource> =
                    tokenResourcesMap?.mapValues {
                        TransferInputTokenResource.create(
                            null,
                            parser,
                            parser.asMap(it.value),
                        ) ?: TransferInputTokenResource(null, null, null, null, null)
                    }?.toIMap() ?: iMapOf()

                return if (
                    existing?.chainResources != chainResources ||
                    existing.tokenResources != tokenResources
                ) {
                    TransferInputResources(
                        chainResources,
                        tokenResources,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Transfer Input Resources not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class TransferInputRequestPayload(
    val routeType: String?,
    val targetAddress: String?,
    val data: String?,
    val value: String?,
    val gasLimit: String?,
    val gasPrice: String?,
    val maxFeePerGas: String?,
    val maxPriorityFeePerGas: String?,
    val fromChainId: String?,
    val toChainId: String?,
    val fromAddress: String?,
    val toAddress: String?,
    val isV2Route: Boolean?,
    val requestId: String?,
) {
    companion object {
        internal fun create(
            existing: TransferInputRequestPayload?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TransferInputRequestPayload? {
            Logger.d { "creating Transfer Input Request Payload\n" }

            data?.let {
                val routeType = parser.asString(data["routeType"])
                val targetAddress = parser.asString(data["targetAddress"])
                val dataValue = parser.asString(data["data"])
                val value = parser.asString(data["value"])
                val gasLimit = parser.asString(data["gasLimit"])
                val gasPrice = parser.asString(data["gasPrice"])
                val maxFeePerGas = parser.asString(data["maxFeePerGas"])
                val maxPriorityFeePerGas = parser.asString(data["maxPriorityFeePerGas"])
                val fromChainId = parser.asString(data["fromChainId"])
                val toChainId = parser.asString(data["toChainId"])
                val fromAddress = parser.asString(data["fromAddress"])
                val toAddress = parser.asString(data["toAddress"])
                val isV2Route = parser.asBool(data["isV2Route"])
                val requestId = parser.asString(data["requestId"])

                return if (
                    existing?.routeType != routeType ||
                    existing?.targetAddress != targetAddress ||
                    existing?.data != dataValue ||
                    existing?.value != value ||
                    existing?.gasLimit != gasLimit ||
                    existing?.gasPrice != gasPrice ||
                    existing?.maxFeePerGas != maxFeePerGas ||
                    existing?.maxPriorityFeePerGas != maxPriorityFeePerGas ||
                    existing?.fromChainId != fromChainId ||
                    existing?.toChainId != toChainId ||
                    existing?.fromAddress != fromAddress ||
                    existing?.toAddress != toAddress ||
                    existing?.isV2Route != isV2Route ||
                    existing?.requestId != requestId
                ) {
                    TransferInputRequestPayload(
                        routeType,
                        targetAddress,
                        dataValue,
                        value,
                        gasLimit,
                        gasPrice,
                        maxFeePerGas,
                        maxPriorityFeePerGas,
                        fromChainId,
                        toChainId,
                        fromAddress,
                        toAddress,
                        isV2Route,
                        requestId,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Transfer Input Request Payload not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class TransferInputSummary(
    val usdcSize: Double?,
    val fee: Double?,
    val filled: Boolean,
    val slippage: Double?,
    val exchangeRate: Double?,
    val estimatedRouteDuration: Double?,
    val bridgeFee: Double?,
    val gasFee: Double?,
    val toAmount: Double?,
    val toAmountMin: Double?,
    val toAmountUSDC: Double?,
    val aggregatePriceImpact: Double?,
) {
    companion object {
        internal fun create(
            existing: TransferInputSummary?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TransferInputSummary? {
            Logger.d { "creating Transfer Input Summary\n" }

            data?.let {
                val usdcSize = parser.asDouble(data["usdcSize"])
                val fee = parser.asDouble(data["fee"])
                val filled = parser.asBool(data["filled"]) ?: false
                val slippage = parser.asDouble(data["slippage"])
                val exchangeRate = parser.asDouble(data["exchangeRate"])
                val estimatedRouteDuration = parser.asDouble(data["estimatedRouteDuration"])
                val bridgeFee = parser.asDouble(data["bridgeFee"])
                val gasFee = parser.asDouble(data["gasFee"])
                val toAmount = parser.asDouble(data["toAmount"])
                val toAmountMin = parser.asDouble(data["toAmountMin"])
                val toAmountUSDC = parser.asDouble(data["toAmountUSDC"])
                val aggregatePriceImpact = parser.asDouble(data["aggregatePriceImpact"])

                return if (existing?.usdcSize != usdcSize ||
                    existing?.fee != fee ||
                    existing?.filled != filled ||
                    existing.slippage != slippage ||
                    existing.exchangeRate != exchangeRate ||
                    existing.estimatedRouteDuration != estimatedRouteDuration ||
                    existing.bridgeFee != bridgeFee ||
                    existing.gasFee != gasFee ||
                    existing.toAmount != toAmount ||
                    existing.toAmountMin != toAmountMin ||
                    existing.toAmountUSDC != toAmountUSDC ||
                    existing.aggregatePriceImpact != aggregatePriceImpact
                ) {
                    TransferInputSummary(
                        usdcSize,
                        fee,
                        filled,
                        slippage,
                        exchangeRate,
                        estimatedRouteDuration,
                        bridgeFee,
                        gasFee,
                        toAmount,
                        toAmountMin,
                        toAmountUSDC,
                        aggregatePriceImpact,
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Transfer Input Summary not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
data class TransferInputSize(
    val usdcSize: String?,
    var size: String?
) {
    companion object {
        internal fun create(
            existing: TransferInputSize?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TransferInputSize? {
            Logger.d { "creating Transfer Input Size\n" }

            data?.let {
                val usdcSize = parser.asString(data["usdcSize"])
                val size = parser.asString(data["size"])

                return if (
                    existing?.usdcSize != usdcSize ||
                    existing?.size != size
                ) {
                    TransferInputSize(usdcSize, size)
                } else {
                    existing
                }
            }
            Logger.d { "Transfer Input Size not valid" }
            return null
        }
    }
}

@JsExport
@Serializable
enum class TransferType(val rawValue: String) {
    deposit("DEPOSIT"),
    withdrawal("WITHDRAWAL"),
    transferOut("TRANSFER_OUT");

    companion object {
        operator fun invoke(rawValue: String) =
            TransferType.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
@Serializable
data class TransferInputHyperlink(
    val label: String?,
    val url: String?
) {
    companion object {
        internal fun create(
            existing: TransferInputHyperlink?,
            parser: ParserProtocol,
            data: Map<*, *>?
        ): TransferInputHyperlink? {
            DebugLogger.log("creating Transfer Input Hyperlink\n")

            data?.let {
                // TODO: Changes values for label and url
                val label = "yooo"// parser.asString(data["label"])
                val url = "https://google.com"// parser.asString(data["url"])

                return if (existing?.label != label || existing?.url != url ) {
                    TransferInputHyperlink(label, url)
                } else {
                    existing
                }
            }
            DebugLogger.debug("Transfer Input Hyperlink not valid")
            return null
        }
    }
}

@JsExport
@Serializable
data class TransferInput(
    val type: TransferType?,
    val size: TransferInputSize?,
    val fastSpeed: Boolean,
    val fee: Double?,
    val exchange: String?,
    val chain: String?,
    val token: String?,
    val address: String?,
    val depositOptions: DepositInputOptions?,
    val withdrawalOptions: WithdrawalInputOptions?,
    val transferOutOptions: TransferOutInputOptions?,
    val summary: TransferInputSummary?,
    val resources: TransferInputResources?,
    val requestPayload: TransferInputRequestPayload?,
    val errors: String?,
    val errorMessage: String?,
    val hyperlink: TransferInputHyperlink?
) {
    val isCctp: Boolean
        get() = cctpChainIds?.any { it.isCctpEnabled(this) } ?: false

    companion object {
        internal fun create(
            existing: TransferInput?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            environment: V4Environment?
        ): TransferInput? {
            Logger.d { "creating Transfer Input\n" }

            data?.let {
                val type = parser.asString(data["type"])?.let {
                    TransferType.invoke(it)
                }

                val size =
                    TransferInputSize.create(existing?.size, parser, parser.asMap(data["size"]))
                val fastSpeed = parser.asBool(data["fastSpeed"]) ?: false
                val fee = parser.asDouble(data["fee"])
                val exchange = parser.asString(data["exchange"])
                val chain = parser.asString(data["chain"])
                val token = parser.asString(data["token"])
                val address = parser.asString(data["address"])

                var depositOptions: DepositInputOptions? = null
                if (type == TransferType.deposit) {
                    depositOptions = DepositInputOptions.create(
                        existing?.depositOptions,
                        parser,
                        parser.asMap(data["depositOptions"]),
                    )
                }

                var withdrawalOptions: WithdrawalInputOptions? = null
                if (type == TransferType.withdrawal) {
                    withdrawalOptions = WithdrawalInputOptions.create(
                        existing?.withdrawalOptions,
                        parser,
                        parser.asMap(data["withdrawalOptions"]),
                    )
                }

                var transferOutOptions: TransferOutInputOptions? = null
                if (type == TransferType.transferOut) {
                    transferOutOptions = TransferOutInputOptions.create(
                        existing?.transferOutOptions,
                        parser,
                        parser.asMap(data["transferOutOptions"]),
                        environment,
                    )
                }

                val summary = TransferInputSummary.create(
                    existing?.summary,
                    parser,
                    parser.asMap(data["summary"]),
                )

                val resources = TransferInputResources.create(
                    existing?.resources,
                    parser,
                    parser.asMap(data["resources"]),
                )

                val route = parser.asMap(data["route"])
                val requestPayload = TransferInputRequestPayload.create(
                    null,
                    parser,
                    parser.asMap(route?.get("requestPayload")),
                )

                val errors = parser.asString(route?.get("errors"))

                val errorMessage: String? =
                    if (errors != null) {
                        val errorArray = parser.decodeJsonArray(errors)
                        val firstError = parser.asMap(errorArray?.first())
                        parser.asString(firstError?.get("message"))
                    } else {
                        null
                    }

                val hyperlink = TransferInputHyperlink.create(
                    existing?.hyperlink,
                    parser,
                    parser.asMap(data["hyperlink"]),
                )

                return if (existing?.type !== type ||
                    existing?.size !== size ||
                    existing?.fastSpeed != fastSpeed ||
                    existing.fee != fee ||
                    existing.exchange != exchange ||
                    existing.chain != chain ||
                    existing.token != token ||
                    existing.address != address ||
                    existing.depositOptions != depositOptions ||
                    existing.withdrawalOptions != withdrawalOptions ||
                    existing.transferOutOptions != transferOutOptions ||
                    existing.summary !== summary ||
                    existing.resources !== resources ||
                    existing.requestPayload !== requestPayload ||
                    existing.errors != errors ||
                    existing.errorMessage != errorMessage ||
                    existing.hyperlink != hyperlink
                ) {
                    TransferInput(
                        type,
                        size,
                        fastSpeed,
                        fee,
                        exchange,
                        chain,
                        token,
                        address,
                        depositOptions,
                        withdrawalOptions,
                        transferOutOptions,
                        summary,
                        resources,
                        requestPayload,
                        errors,
                        errorMessage,
                        hyperlink
                    )
                } else {
                    existing
                }
            }
            Logger.d { "Transfer Input not valid" }
            return null
        }
    }
}
