package exchange.dydx.abacus.output.input

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalTransferInputState
import exchange.dydx.abacus.state.manager.CctpConfig.cctpChainIds
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Logger
import exchange.dydx.abacus.utils.iMapOf
import kollections.JsExport
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
)

@JsExport
@Serializable
data class WithdrawalInputOptions(
    val needsSize: Boolean?,
    val needsAddress: Boolean?,
    val needsFastSpeed: Boolean?,
    val exchanges: IList<SelectionOption>?,
    val chains: IList<SelectionOption>?,
    val assets: IList<SelectionOption>?
)

@JsExport
@Serializable
data class TransferOutInputOptions(
    val needsSize: Boolean?,
    val needsAddress: Boolean?,
    val chains: IList<SelectionOption>?,
    val assets: IList<SelectionOption>?,
)

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
)

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
    val goFastSummary: TransferInputSummary?,
    val resources: TransferInputResources?,
    val requestPayload: TransferInputRequestPayload?,
    val goFastRequestPayload: TransferInputRequestPayload?,
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
            internalState: InternalTransferInputState?,
        ): TransferInput? {
            Logger.d { "creating Transfer Input\n" }

            if (internalState != null) {
                val type = internalState?.type
                val size = internalState?.size

                val fastSpeed = internalState?.fastSpeed ?: false
                val fee = internalState?.fee
                val exchange = internalState?.exchange
                val chain = internalState?.chain
                val token = internalState?.token
                val address = internalState?.address
                val memo = internalState?.memo

                var depositOptions: DepositInputOptions? = null
                if (type == TransferType.deposit) {
                    depositOptions = internalState.depositOptions
                }

                var withdrawalOptions: WithdrawalInputOptions? = null
                if (type == TransferType.withdrawal) {
                    withdrawalOptions = internalState.withdrawalOptions
                }

                var transferOutOptions: TransferOutInputOptions? = null
                if (type == TransferType.transferOut) {
                    transferOutOptions = internalState.transferOutOptions
                }

                val summary = internalState?.summary
                val goFastSummary = internalState?.goFastSummary

                val resources = internalState?.resources

                val route = internalState?.route
                val goFastRoute = internalState?.goFastRoute
                val requestPayload = TransferInputRequestPayload.create(
                    existing = null,
                    parser = parser,
                    data = parser.asMap(route?.get("requestPayload")),
                )
                val goFastRequestPayload = TransferInputRequestPayload.create(
                    existing = null,
                    parser = parser,
                    data = parser.asMap(goFastRoute?.get("requestPayload")),
                )

                val errors = parser.asString(route?.get("errors")) ?: parser.asString(goFastRoute?.get("errors"))
                val errorMessage: String? =
                    if (errors != null) {
                        val errorArray = parser.decodeJsonArray(errors)
                        val firstError = parser.asMap(errorArray?.first())
                        parser.asString(firstError?.get("message"))
                    } else {
                        null
                    }
                val warning = parser.asString(route?.get("warning")) ?: parser.asString(goFastRoute?.get("warning"))

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
                    existing.goFastSummary != goFastSummary ||
                    existing.resources !== resources ||
                    existing.requestPayload !== requestPayload ||
                    existing.goFastRequestPayload != goFastRequestPayload ||
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
                        goFastSummary = goFastSummary,
                        resources = resources,
                        requestPayload = requestPayload,
                        goFastRequestPayload = goFastRequestPayload,
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
