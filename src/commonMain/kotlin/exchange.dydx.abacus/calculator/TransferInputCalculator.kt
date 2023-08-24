package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.iListOf

@Suppress("UNCHECKED_CAST")
internal class TransferInputCalculator(val parser: ParserProtocol) {
    private val subaccountTransformer = SubaccountTransformer()
    internal fun calculate(
        state: IMap<String, Any>,
        subaccountNumber: Int?
    ): IMap<String, Any> {
        val wallet = parser.asMap(state["wallet"])
        val transfer = parser.asMap(state["transfer"])
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
                "postOrder"
            )
            modified["wallet"] = modifiedWallet
            modified
        } else state
    }

    private fun finalize(
        transfer: IMap<String, Any>,
        type: String
    ): IMap<String, Any> {
        val modified = transfer.mutable()
        val fields = requiredFields(type)
        modified.safeSet("fields", fields)
        modified.safeSet("options", calculatedOptionsFromFields(fields))
        modified.safeSet("summary", summaryForType(transfer, type))
        return modified
    }

    private fun requiredFields(type: String): IList<Any>? {
        return when (type) {
            "DEPOSIT" -> {
                iListOf(
                    sizeField(),
                    gaslessField()
                )
            }

            "WITHDRAWAL" -> {
                iListOf(
                    sizeField(),
                    speedField()
                )
            }

            "TRANSFER_OUT" -> {
                iListOf(
                    sizeField(),
                    addressField()
                )
            }

            else -> null
        }
    }

    private fun sizeField(): IMap<String, Any> {
        return iMapOf(
            "field" to "size.usdcSize",
            "type" to "double"
        )
    }

    private fun gaslessField(): IMap<String, Any> {
        return iMapOf(
            "field" to "gasless",
            "type" to "bool"
        )
    }

    private fun speedField(): IMap<String, Any> {
        return iMapOf(
            "field" to "fastSpeed",
            "type" to "bool",
            "default" to true
        )
    }

    private fun addressField(): IMap<String, Any> {
        return iMapOf(
            "field" to "address",
            "type" to "string"
        )
    }

    private fun calculatedOptionsFromFields(fields: IList<Any>?): IMap<String, Any>? {
        fields?.let { fields ->
            val options = iMutableMapOf<String, Any>(
                "needsSize" to false,
                "needsGasless" to false,
                "needsFastSpeed" to false,
                "needsAddress" to false
            )
            for (item in fields) {
                parser.asMap(item)?.let { field ->
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

    private fun calculatedOptions(type: String): IMap<String, Any>? {
        val fields = requiredFields(type)
        return calculatedOptionsFromFields(fields)
    }

    private fun summaryForType(
        transfer: IMap<String, Any>,
        type: String
    ): IMap<String, Any> {
        val summary = iMutableMapOf<String, Any>()
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

                val bridgeFee = parser.asDouble(parser.value(transfer, "route.bridgeFee"))
                summary.safeSet("bridgeFee", bridgeFee)

                val gasFee = parser.asDouble(parser.value(transfer, "route.gasFee"))
                summary.safeSet("gasFee", gasFee)
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
