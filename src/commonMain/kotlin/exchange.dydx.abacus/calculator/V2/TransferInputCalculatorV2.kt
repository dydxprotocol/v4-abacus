package exchange.dydx.abacus.calculator.v2

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.TransferInputSummary
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.internalstate.InternalTransferInputState
import exchange.dydx.abacus.state.internalstate.InternalWalletState

internal class TransferInputCalculatorV2(
    private val parser: ParserProtocol,
    private val subaccountTransformer: SubaccountTransformerV2 = SubaccountTransformerV2(parser),
) {
    fun calculate(
        transfer: InternalTransferInputState,
        wallet: InternalWalletState,
        subaccountNumber: Int?,
    ): InternalTransferInputState {
        if (wallet.isAccountConnected && transfer.type != null) {
            finalize(transfer)

            subaccountTransformer.applyTransferToWallet(
                wallet = wallet,
                subaccountNumber = subaccountNumber,
                transfer = transfer,
                parser = parser,
                period = CalculationPeriod.post,
            )
        }
        return transfer
    }

    private fun finalize(
        transfer: InternalTransferInputState,
    ): InternalTransferInputState {
        transfer.summary = summaryForType(transfer)
        return transfer
    }

    private fun summaryForType(
        transfer: InternalTransferInputState,
    ): TransferInputSummary? {
        val type = transfer.type ?: return null
        when (type) {
            TransferType.deposit -> {
                val size = parser.asDouble(transfer.size?.size)
                var usdcSize = parser.asDouble(transfer.size?.usdcSize)
                val fee = parser.asDouble(transfer.fee)

                val route = transfer.route

                val slippage = parser.asDouble(parser.value(route, "slippage"))
                val exchangeRate = parser.asDouble(parser.value(route, "exchangeRate"))
                val estimatedRouteDurationSeconds = parser.asDouble(parser.value(route, "estimatedRouteDurationSeconds"))
                val bridgeFee = parser.asDouble(parser.value(route, "bridgeFee"))
                val gasFee = parser.asDouble(parser.value(route, "gasFee"))
                val toAmount = parser.asDouble(parser.value(route, "toAmount"))
                val toAmountMin = parser.asDouble(parser.value(route, "toAmountMin"))
                val toAmountUSDC = parser.asDouble(parser.value(route, "toAmountUSDC"))
                val toAmountUSD = parser.asDouble(parser.value(route, "toAmountUSD"))
                var aggregatePriceImpact = parser.asDouble(parser.value(route, "aggregatePriceImpact"))
                aggregatePriceImpact = if (aggregatePriceImpact != null) aggregatePriceImpact / 100.0 else null

                usdcSize = if (usdcSize != null) {
                    usdcSize
                } else if (size != null && exchangeRate != null) {
                    exchangeRate * size
                } else {
                    null
                }

                return TransferInputSummary(
                    filled = true,
                    fee = fee,
                    slippage = slippage,
                    exchangeRate = exchangeRate,
                    estimatedRouteDurationSeconds = estimatedRouteDurationSeconds,
                    usdcSize = usdcSize,
                    bridgeFee = bridgeFee,
                    gasFee = gasFee,
                    toAmount = toAmount,
                    toAmountMin = toAmountMin,
                    toAmountUSDC = toAmountUSDC,
                    toAmountUSD = toAmountUSD,
                    aggregatePriceImpact = aggregatePriceImpact,
                )
            }

            TransferType.withdrawal -> {
                val usdcSize = parser.asDouble(transfer.size?.usdcSize)
                val fee = parser.asDouble(transfer.fee)

                val route = transfer.route

                val slippage = parser.asDouble(parser.value(route, "slippage"))
                val exchangeRate = parser.asDouble(parser.value(route, "exchangeRate"))
                val estimatedRouteDurationSeconds = parser.asDouble(parser.value(route, "estimatedRouteDurationSeconds"))
                val bridgeFee = parser.asDouble(parser.value(route, "bridgeFee"))
                val gasFee = parser.asDouble(parser.value(route, "gasFee"))
                val toAmount = parser.asDouble(parser.value(route, "toAmount"))
                val toAmountMin = parser.asDouble(parser.value(route, "toAmountMin"))
                val toAmountUSDC = parser.asDouble(parser.value(route, "toAmountUSDC"))
                val toAmountUSD = parser.asDouble(parser.value(route, "toAmountUSD"))
                var aggregatePriceImpact = parser.asDouble(parser.value(route, "aggregatePriceImpact"))
                aggregatePriceImpact = if (aggregatePriceImpact != null) aggregatePriceImpact / 100.0 else null

                return TransferInputSummary(
                    filled = true,
                    fee = fee,
                    slippage = slippage,
                    exchangeRate = exchangeRate,
                    estimatedRouteDurationSeconds = estimatedRouteDurationSeconds,
                    usdcSize = usdcSize,
                    bridgeFee = bridgeFee,
                    gasFee = gasFee,
                    toAmount = toAmount,
                    toAmountMin = toAmountMin,
                    toAmountUSDC = toAmountUSDC,
                    toAmountUSD = toAmountUSD,
                    aggregatePriceImpact = aggregatePriceImpact,
                )
            }

            TransferType.transferOut -> {
                val usdcSize = parser.asDouble(transfer.size?.usdcSize)

                return TransferInputSummary(
                    filled = true,
                    gasFee = transfer.fee,
                    usdcSize = usdcSize,
                    fee = null,
                    slippage = null,
                    exchangeRate = null,
                    estimatedRouteDurationSeconds = null,
                    bridgeFee = null,
                    toAmount = null,
                    toAmountMin = null,
                    toAmountUSDC = null,
                    toAmountUSD = null,
                    aggregatePriceImpact = null,
                )
            }
        }
    }
}
