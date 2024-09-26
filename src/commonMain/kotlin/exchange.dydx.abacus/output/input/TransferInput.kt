package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
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
            data: Map<*, *>?,
            internalState: InternalTransferInputState?
        ): DepositInputOptions? {
            Logger.d { "creating Deposit Input Options\n" }

            data?.let {
                val needsSize = parser.asBool(data["needsSize"])
                val needsAddress = parser.asBool(data["needsAddress"])
                val needsFastSpeed = parser.asBool(data["needsFastSpeed"])

                val chains: IList<SelectionOption> = internalState?.chains?.toIList() ?: iListOf()

                val assets: IList<SelectionOption> = internalState?.tokens?.toIList() ?: iListOf()

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
                    existing?.exchanges != exchanges ||
                    existing?.chains != chains ||
                    existing?.assets != assets
                ) {
                    DepositInputOptions(
                        needsSize = needsSize,
                        needsAddress = needsAddress,
                        needsFastSpeed = needsFastSpeed,
                        exchanges = exchanges,
                        chains = chains,
                        assets = assets,
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
            data: Map<*, *>?,
            internalState: InternalTransferInputState?,
        ): WithdrawalInputOptions? {
            Logger.d { "creating Withdrawal Input Options\n" }

            data?.let {
                val needsSize = parser.asBool(data["needsSize"])
                val needsAddress = parser.asBool(data["needsAddress"])
                val needsFastSpeed = parser.asBool(data["needsFastSpeed"])

                val chains: IList<SelectionOption> = internalState?.chains?.toIList() ?: iListOf()

                val assets: IList<SelectionOption> = internalState?.tokens?.toIList() ?: iListOf()

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
                    existing?.exchanges != exchanges ||
                    existing?.chains != chains ||
                    existing?.assets != assets
                ) {
                    WithdrawalInputOptions(
                        needsSize = needsSize,
                        needsAddress = needsAddress,
                        needsFastSpeed = needsFastSpeed,
                        exchanges = exchanges,
                        chains = chains,
                        assets = assets,
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
    val assets: IList<SelectionOption>?,
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
                    type = "chain",
                    string = chainName,
                    stringKey = null,
                    iconUrl = environment.chainLogo,
                )
            } else {
                return null
            }
            val chains: IList<SelectionOption> = iListOf(chainOption)

            val assets: IList<SelectionOption> = environment.tokens.keys.map { key ->
                val token = environment.tokens[key]!!
                SelectionOption(
                    type = key,
                    string = token.name,
                    stringKey = null,
                    iconUrl = token.imageUrl,
                )
            }.toIList()

            return if (existing?.needsSize != needsSize ||
                existing.needsAddress != needsAddress ||
                existing.chains != chains ||
                existing.assets != assets
            ) {
                TransferOutInputOptions(
                    needsSize = needsSize,
                    needsAddress = needsAddress,
                    chains = chains,
                    assets = assets,
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
    val rpc: String? = null,
    val networkName: String? = null,
    val chainId: Int?,
    val iconUrl: String?
)

@JsExport
@Serializable
data class TransferInputTokenResource(
    var name: String?,
    var address: String?,
    var symbol: String?,
    var decimals: Int?,
    var iconUrl: String?
)

@JsExport
@Serializable
data class TransferInputResources(
    var chainResources: IMap<String, TransferInputChainResource>?,
    var tokenResources: IMap<String, TransferInputTokenResource>?,
) {
    companion object {
        internal fun create(
            existing: TransferInputResources?,
            internalState: InternalTransferInputState?,
        ): TransferInputResources? {
            Logger.d { "creating Transfer Input Resources\n" }

            internalState?.let {
                val chainResources: IMap<String, TransferInputChainResource> = internalState.chainResources?.toIMap() ?: iMapOf()
                val tokenResources: IMap<String, TransferInputTokenResource> = internalState.tokenResources?.toIMap() ?: iMapOf()
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
    val allMessages: String?,
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
                val allMessages = parser.asString(data["allMessages"])
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
                    existing?.allMessages != allMessages ||
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
                        allMessages,
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
    val estimatedRouteDurationSeconds: Double?,
    val bridgeFee: Double?,
    val gasFee: Double?,
    val toAmount: Double?,
    val toAmountMin: Double?,
    val toAmountUSDC: Double?,
    val toAmountUSD: Double?,
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
                val estimatedRouteDurationSeconds = parser.asDouble(data["estimatedRouteDurationSeconds"])
                val bridgeFee = parser.asDouble(data["bridgeFee"])
                val gasFee = parser.asDouble(data["gasFee"])
                val toAmount = parser.asDouble(data["toAmount"])
                val toAmountMin = parser.asDouble(data["toAmountMin"])
                val toAmountUSDC = parser.asDouble(data["toAmountUSDC"])
                val toAmountUSD = parser.asDouble(data["toAmountUSD"])
                val aggregatePriceImpact = parser.asDouble(data["aggregatePriceImpact"])

                return if (existing?.usdcSize != usdcSize ||
                    existing?.fee != fee ||
                    existing?.filled != filled ||
                    existing.slippage != slippage ||
                    existing.exchangeRate != exchangeRate ||
                    existing.estimatedRouteDurationSeconds != estimatedRouteDurationSeconds ||
                    existing.bridgeFee != bridgeFee ||
                    existing.gasFee != gasFee ||
                    existing.toAmount != toAmount ||
                    existing.toAmountMin != toAmountMin ||
                    existing.toAmountUSDC != toAmountUSDC ||
                    existing.toAmountUSD != toAmountUSD ||
                    existing.aggregatePriceImpact != aggregatePriceImpact
                ) {
                    TransferInputSummary(
                        usdcSize,
                        fee,
                        filled,
                        slippage,
                        exchangeRate,
                        estimatedRouteDurationSeconds,
                        bridgeFee,
                        gasFee,
                        toAmount,
                        toAmountMin,
                        toAmountUSDC,
                        toAmountUSD,
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
        operator fun invoke(rawValue: String?) =
            entries.firstOrNull { it.rawValue == rawValue }
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
    val memo: String?,
    val depositOptions: DepositInputOptions?,
    val withdrawalOptions: WithdrawalInputOptions?,
    val transferOutOptions: TransferOutInputOptions?,
    val summary: TransferInputSummary?,
    val resources: TransferInputResources?,
    val requestPayload: TransferInputRequestPayload?,
    val errors: String?,
    val errorMessage: String?,
    val warning: String?,
) {
    val isCctp: Boolean
        get() = cctpChainIds?.any { it.isCctpEnabled(this) } ?: false

    companion object {
        internal fun create(
            existing: TransferInput?,
            parser: ParserProtocol,
            data: Map<*, *>?,
            environment: V4Environment?,
            internalState: InternalTransferInputState?,
            staticTyping: Boolean,
        ): TransferInput? {
            Logger.d { "creating Transfer Input\n" }

            if (internalState != null || data != null) {
                val type = if (staticTyping) {
                    internalState?.type
                } else {
                    parser.asString(data?.get("type"))?.let {
                        TransferType.invoke(it)
                    }
                }

                val size = if (staticTyping) {
                    internalState?.size
                } else {
                    TransferInputSize.create(existing?.size, parser, parser.asMap(data?.get("size")))
                }
                val fastSpeed = if (staticTyping) internalState?.fastSpeed ?: false else parser.asBool(data?.get("fastSpeed")) ?: false
                val fee = if (staticTyping) internalState?.fee else parser.asDouble(data?.get("fee"))
                val exchange = if (staticTyping) internalState?.exchange else parser.asString(data?.get("exchange"))
                val chain = if (staticTyping) internalState?.chain else parser.asString(data?.get("chain"))
                val token = if (staticTyping) internalState?.token else parser.asString(data?.get("token"))
                val address = if (staticTyping) internalState?.address else parser.asString(data?.get("address"))
                val memo = if (staticTyping) internalState?.memo else parser.asString(data?.get("memo"))

                var depositOptions: DepositInputOptions? = null
                if (type == TransferType.deposit) {
                    depositOptions = if (staticTyping) {
                        internalState?.depositOptions
                    } else {
                        DepositInputOptions.create(
                            existing = existing?.depositOptions,
                            parser = parser,
                            data = parser.asMap(data?.get("depositOptions")),
                            internalState = internalState,
                        )
                    }
                }

                var withdrawalOptions: WithdrawalInputOptions? = null
                if (type == TransferType.withdrawal) {
                    withdrawalOptions = if (staticTyping) {
                        internalState?.withdrawalOptions
                    } else {
                        WithdrawalInputOptions.create(
                            existing = existing?.withdrawalOptions,
                            parser = parser,
                            data = parser.asMap(data?.get("withdrawalOptions")),
                            internalState = internalState,
                        )
                    }
                }

                var transferOutOptions: TransferOutInputOptions? = null
                if (type == TransferType.transferOut) {
                    transferOutOptions = if (staticTyping) {
                        internalState?.transferOutOptions
                    } else {
                        TransferOutInputOptions.create(
                            existing = existing?.transferOutOptions,
                            parser = parser,
                            data = parser.asMap(data?.get("transferOutOptions")),
                            environment = environment,
                        )
                    }
                }

                val summary = if (staticTyping) {
                    internalState?.summary
                } else {
                    TransferInputSummary.create(
                        existing = existing?.summary,
                        parser = parser,
                        data = parser.asMap(data?.get("summary")),
                    )
                }

                val resources = if (staticTyping) {
                    internalState?.resources
                } else {
                    TransferInputResources.create(
                        existing = existing?.resources,
                        internalState = internalState,
                    )
                }

                val route = if (staticTyping) {
                    internalState?.route
                } else {
                    parser.asMap(data?.get("route"))
                }
                val requestPayload = TransferInputRequestPayload.create(
                    existing = null,
                    parser = parser,
                    data = parser.asMap(route?.get("requestPayload")),
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
                val warning = parser.asString(route?.get("warning"))

                return if (existing?.type !== type ||
                    existing?.size !== size ||
                    existing?.fastSpeed != fastSpeed ||
                    existing.fee != fee ||
                    existing.exchange != exchange ||
                    existing.chain != chain ||
                    existing.token != token ||
                    existing.address != address ||
                    existing.memo != memo ||
                    existing.depositOptions != depositOptions ||
                    existing.withdrawalOptions != withdrawalOptions ||
                    existing.transferOutOptions != transferOutOptions ||
                    existing.summary !== summary ||
                    existing.resources !== resources ||
                    existing.requestPayload !== requestPayload ||
                    existing.errors != errors ||
                    existing.errorMessage != errorMessage ||
                    existing.warning != warning
                ) {
                    TransferInput(
                        type = type,
                        size = size,
                        fastSpeed = fastSpeed,
                        fee = fee,
                        exchange = exchange,
                        chain = chain,
                        token = token,
                        address = address,
                        memo = memo,
                        depositOptions = depositOptions,
                        withdrawalOptions = withdrawalOptions,
                        transferOutOptions = transferOutOptions,
                        summary = summary,
                        resources = resources,
                        requestPayload = requestPayload,
                        errors = errors,
                        errorMessage = errorMessage,
                        warning = warning,
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
