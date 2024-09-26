package exchange.dydx.abacus.state.internalstate

import exchange.dydx.abacus.output.input.DepositInputOptions
import exchange.dydx.abacus.output.input.SelectionOption
import exchange.dydx.abacus.output.input.TransferInputChainResource
import exchange.dydx.abacus.output.input.TransferInputResources
import exchange.dydx.abacus.output.input.TransferInputSize
import exchange.dydx.abacus.output.input.TransferInputSummary
import exchange.dydx.abacus.output.input.TransferInputTokenResource
import exchange.dydx.abacus.output.input.TransferOutInputOptions
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.output.input.WithdrawalInputOptions

internal data class InternalTransferInputState(
    var chains: List<SelectionOption>? = null,
    var tokens: List<SelectionOption>? = null,
    var chainResources: Map<String, TransferInputChainResource>? = null,
    var tokenResources: Map<String, TransferInputTokenResource>? = null,
    var evmSwapVenues: List<Any?> = listOf(),

    var type: TransferType? = null,
    var size: TransferInputSize? = null,
    var fastSpeed: Boolean = false,
    var fee: Double? = null,
    var exchange: String? = null,
    var chain: String? = null,
    var token: String? = null,
    var address: String? = null,
    var memo: String? = null,
    var depositOptions: DepositInputOptions? = null,
    var withdrawalOptions: WithdrawalInputOptions? = null,
    var transferOutOptions: TransferOutInputOptions? = null,
    var summary: TransferInputSummary? = null,
    var resources: TransferInputResources? = null,
    var route: Map<String, Any>? = null,
)

internal fun TransferInputSize.Companion.safeCreate(existing: TransferInputSize?): TransferInputSize {
    return existing ?: TransferInputSize(
        size = null,
        usdcSize = null,
    )
}

internal fun DepositInputOptions.Companion.safeCreate(existing: DepositInputOptions?): DepositInputOptions {
    return existing ?: DepositInputOptions(
        needsSize = null,
        needsAddress = null,
        needsFastSpeed = null,
        exchanges = null,
        chains = null,
        assets = null,
    )
}

internal fun WithdrawalInputOptions.Companion.safeCreate(existing: WithdrawalInputOptions?): WithdrawalInputOptions {
    return existing ?: WithdrawalInputOptions(
        needsSize = null,
        needsAddress = null,
        needsFastSpeed = null,
        exchanges = null,
        chains = null,
        assets = null,
    )
}

internal fun TransferOutInputOptions.Companion.safeCreate(existing: TransferOutInputOptions?): TransferOutInputOptions {
    return existing ?: TransferOutInputOptions(
        needsSize = null,
        needsAddress = null,
        chains = null,
        assets = null,
    )
}

internal fun TransferInputResources.Companion.safeCreate(existing: TransferInputResources?): TransferInputResources {
    return existing ?: TransferInputResources(
        chainResources = null,
        tokenResources = null,
    )
}

internal fun TransferInputSummary.Companion.safeCreate(existing: TransferInputSummary?): TransferInputSummary {
    return existing ?: TransferInputSummary(
        usdcSize = null,
        fee = null,
        filled = false,
        slippage = null,
        exchangeRate = null,
        estimatedRouteDurationSeconds = null,
        bridgeFee = null,
        gasFee = null,
        toAmount = null,
        toAmountMin = null,
        toAmountUSDC = null,
        toAmountUSD = null,
        aggregatePriceImpact = null,
    )
}
