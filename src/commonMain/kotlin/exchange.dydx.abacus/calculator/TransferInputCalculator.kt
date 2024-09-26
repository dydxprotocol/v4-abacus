package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet

@Suppress("UNCHECKED_CAST")
internal class TransferInputCalculator(val parser: ParserProtocol) {
    private val subaccountTransformer = SubaccountTransformer()
    internal fun calculate(
        state: Map<String, Any>,
        subaccountNumber: Int?
    ): Map<String, Any> {
        val wallet = parser.asNativeMap(state["wallet"])
        val transfer = parser.asNativeMap(state["transfer"])
        val type = parser.asString(transfer?.get("type"))
        return if (wallet != null && transfer != null && type != null) {
            val modifiedTransfer = finalize(transfer, type)
            val modified = state.mutable()
            modified["transfer"] = modifiedTransfer

            val modifiedWallet = subaccountTransformer.applyTransferToWallet(
                wallet,
                subaccountNumber,
                modifiedTransfer,
                parser,
                "postOrder",
            )
            modified["wallet"] = modifiedWallet
            modified
        } else {
            state
        }
    }

    private fun finalize(
        transfer: Map<String, Any>,
        type: String
    ): Map<String, Any> {
        val modified = transfer.mutable()
        val fields = requiredFields(type)
        modified.safeSet("fields", fields)
        modified.safeSet("options", calculatedOptionsFromFields(fields))
        modified.safeSet("summary", summaryForType(transfer, type))
        return modified
    }

    private fun requiredFields(type: String): List<Any>? {
        return when (type) {
            "DEPOSIT" -> {
                listOf(
                    sizeField(),
                    gaslessField(),
                )
            }

            "WITHDRAWAL" -> {
                listOf(
                    sizeField(),
                    speedField(),
                )
            }

            "TRANSFER_OUT" -> {
                listOf(
                    sizeField(),
                    addressField(),
                )
            }

            else -> null
        }
    }

    private fun sizeField(): Map<String, Any> {
        return mapOf(
            "field" to "size.usdcSize",
            "type" to "double",
        )
    }

    private fun gaslessField(): Map<String, Any> {
        return mapOf(
            "field" to "gasless",
            "type" to "bool",
        )
    }

    private fun speedField(): Map<String, Any> {
        return mapOf(
            "field" to "fastSpeed",
            "type" to "bool",
            "default" to true,
        )
    }

    private fun addressField(): Map<String, Any> {
        return mapOf(
            "field" to "address",
            "type" to "string",
        )
    }

    private fun calculatedOptionsFromFields(fields: List<Any>?): Map<String, Any>? {
        fields?.let { fields ->
            val options = mutableMapOf<String, Any>(
                "needsSize" to false,
                "needsGasless" to false,
                "needsFastSpeed" to false,
                "needsAddress" to false,
            )
            for (item in fields) {
                parser.asNativeMap(item)?.let { field ->
                    when (parser.asString(field["field"])) {
                        "size.usdcSize" -> options["needsSize"] = true
                        "gasless" -> options["needsGasless"] = true
                        "fastSpeed" -> options["needsFastSpeed"] = true
                        "address" -> options["needsAddress"] = true
                    }
                }
            }
            return options
        }
        return null
    }

    private fun calculatedOptions(type: String): Map<String, Any>? {
        val fields = requiredFields(type)
        return calculatedOptionsFromFields(fields)
    }

    private fun summaryForType(
        transfer: Map<String, Any>,
        type: String
    ): Map<String, Any> {
        val summary = mutableMapOf<String, Any>()
        when (type) {
            "DEPOSIT" -> {
                val size = parser.asDouble(parser.value(transfer, "size.size"))
                val usdcSize = parser.asDouble(parser.value(transfer, "size.usdcSize"))

                summary.safeSet("size", size)
                summary.safeSet("filled", true)

                val fee = parser.asDouble(parser.value(transfer, "fee"))
                summary.safeSet("fee", fee)

                val slippage = parser.asDouble(parser.value(transfer, "route.slippage"))
                summary.safeSet("slippage", slippage)

                val exchangeRate = parser.asDouble(parser.value(transfer, "route.exchangeRate"))
                summary.safeSet("exchangeRate", exchangeRate)

                val estimatedRouteDurationSeconds =
                    parser.asDouble(parser.value(transfer, "route.estimatedRouteDurationSeconds"))
                summary.safeSet("estimatedRouteDurationSeconds", estimatedRouteDurationSeconds)

                if (usdcSize != null) {
                    summary.safeSet("usdcSize", usdcSize)
                } else if (size != null && exchangeRate != null) {
                    summary.safeSet("usdcSize", exchangeRate * size)
                } else {
                    summary.safeSet("usdcSize", null)
                }

                val bridgeFee = parser.asDouble(parser.value(transfer, "route.bridgeFee"))
                summary.safeSet("bridgeFee", bridgeFee)

                val gasFee = parser.asDouble(parser.value(transfer, "route.gasFee"))
                summary.safeSet("gasFee", gasFee)

                val toAmount = parser.asDouble(parser.value(transfer, "route.toAmount"))
                summary.safeSet("toAmount", toAmount)

                val toAmountMin = parser.asDouble(parser.value(transfer, "route.toAmountMin"))
                summary.safeSet("toAmountMin", toAmountMin)

                val toAmountUSDC = parser.asDouble(parser.value(transfer, "route.toAmountUSDC"))
                summary.safeSet("toAmountUSDC", toAmountUSDC ?: toAmountMin)

                val aggregatePriceImpact =
                    parser.asDouble(parser.value(transfer, "route.aggregatePriceImpact"))
                summary.safeSet(
                    "aggregatePriceImpact",
                    if (aggregatePriceImpact != null) aggregatePriceImpact / 100.0 else null,
                )
            }

            "WITHDRAWAL" -> {
                val size = parser.asDouble(parser.value(transfer, "size.size"))
                val usdcSize = parser.asDouble(parser.value(transfer, "size.usdcSize"))
                val fastSpeed = parser.asBool(parser.value(transfer, "fastSpeed")) ?: false
                summary.safeSet("size", size)
                summary.safeSet("usdcSize", usdcSize)
                summary.safeSet("fastSpeed", fastSpeed)
                summary.safeSet("filled", true)

                val fee = parser.asDouble(parser.value(transfer, "fee"))
                summary.safeSet("fee", fee)

                val slippage = parser.asDouble(parser.value(transfer, "route.slippage"))
                summary.safeSet("slippage", slippage)

                val exchangeRate = parser.asDouble(parser.value(transfer, "route.exchangeRate"))
                summary.safeSet("exchangeRate", exchangeRate)

                val estimatedRouteDurationSeconds =
                    parser.asDouble(parser.value(transfer, "route.estimatedRouteDurationSeconds"))
                summary.safeSet("estimatedRouteDurationSeconds", estimatedRouteDurationSeconds)

                val bridgeFee = parser.asDouble(parser.value(transfer, "route.bridgeFee"))
                summary.safeSet("bridgeFee", bridgeFee)

                val gasFee = parser.asDouble(parser.value(transfer, "route.gasFee"))
                summary.safeSet("gasFee", gasFee)

                val toAmountMin = parser.asDouble(parser.value(transfer, "route.toAmountMin"))
                summary.safeSet("toAmountMin", toAmountMin)

                val toAmount = parser.asDouble(parser.value(transfer, "route.toAmount"))
                summary.safeSet("toAmount", toAmount)

                val toAmountUSDC = parser.asDouble(parser.value(transfer, "route.toAmountUSDC"))
                summary.safeSet("toAmountUSDC", toAmountUSDC)

                val toAmountUSD = parser.asDouble(parser.value(transfer, "route.toAmountUSD"))
                summary.safeSet("toAmountUSD", toAmountUSD)

                val aggregatePriceImpact =
                    parser.asDouble(parser.value(transfer, "route.aggregatePriceImpact"))
                summary.safeSet(
                    "aggregatePriceImpact",
                    if (aggregatePriceImpact != null) aggregatePriceImpact / 100.0 else null,
                )
            }

            "TRANSFER_OUT" -> {
                val address = parser.asString(parser.value(transfer, "address"))
                val size = parser.asDouble(parser.value(transfer, "size.size"))
                val usdcSize = parser.asDouble(parser.value(transfer, "size.usdcSize"))

                summary.safeSet("address", address)
                summary.safeSet("size", size)
                summary.safeSet("usdcSize", usdcSize)
                summary.safeSet("filled", true)
                val fee = parser.asDouble(parser.value(transfer, "fee"))
                summary.safeSet("gasFee", fee)
            }

            else -> {}
        }
        return summary
    }
}
