package exchange.dydx.abacus.state.modal

import exchange.dydx.abacus.calculator.TransferInputCalculator
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.IMutableMap
import exchange.dydx.abacus.utils.iMutableMapOf
import exchange.dydx.abacus.utils.isAddressValid
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.safeSet
import kollections.JsExport
import kollections.iListOf
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class TransferInputField(val rawValue: String) {
    type("type"),

    usdcSize("size.usdcSize"),
    size("size.size"),
    usdcFee("fee"),

    chain("chain"),
    token("token"),
    address("address"),
    fastSpeed("fastSpeed");

    companion object {
        operator fun invoke(rawValue: String) =
            TradeInputField.values().firstOrNull { it.rawValue == rawValue }
    }
}

fun TradingStateMachine.transfer(
    data: String?,
    type: TransferInputField?,
    subaccountNumber: Int = 0
): StateResponse {
    var changes: StateChanges? = null
    var error: ParsingError? = null
    val typeText = type?.rawValue

    val input = this.input?.mutable() ?: iMutableMapOf()
    input["current"] = "transfer"
    val transfer = parser.asMap(input["transfer"])?.mutable() ?: kotlin.run {
        val transfer = iMutableMapOf<String, Any>()
        transfer["type"] = "DEPOSIT"

        val calculator = TransferInputCalculator(parser)
        val params = iMutableMapOf<String, Any>()
        params.safeSet("markets", parser.asMap(marketsSummary?.get("markets")))
        params.safeSet("account", account)
        params.safeSet("user", user)
        params.safeSet("transfer", transfer)
        val modified = calculator.calculate(params, subaccountNumber)

        parser.asMap(modified["transfer"])?.mutable() ?: transfer
    }

    if (typeText != null) {
        if (validTransferInput(transfer, typeText)) {
            when (typeText) {
                TransferInputField.type.rawValue -> {
                    if (transfer["type"] != parser.asString(data)) {
                        transfer.safeSet(typeText, parser.asString(data))
                        transfer.safeSet("size.size", null)
                        transfer.safeSet("size.usdcSize", null)
                        transfer.safeSet("route", null)
                        transfer.safeSet("requestPayload", null)
                        if (parser.asString(data) == "TRANSFER_OUT") {
                            transfer.safeSet("chain", "dydx")
                            transfer.safeSet("token", "usdc")
                        } else {
                            val chainType = squidProcessor.defaultChainId()
                            if (chainType != null) {
                                updateTransferToChainType(transfer, chainType)
                            }
                            transfer.safeSet("token", squidProcessor.defaultTokenAddress(chainType))
                        }
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                }
                TransferInputField.address.rawValue -> {
                    val address = parser.asString(data)
                    transfer.safeSet(typeText, address)
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                    transfer.safeSet("route", null)
                    transfer.safeSet("requestPayload", null)
                }
                TransferInputField.token.rawValue -> {
                    val token = parser.asString(data)
                    transfer.safeSet("size.size", null)
                    transfer.safeSet("size.usdcSize", null)
                    transfer.safeSet(typeText, token)
                    if (token != null) {
                        updateTransferToTokenType(transfer, token)
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                }
                TransferInputField.usdcSize.rawValue,
                TransferInputField.usdcFee.rawValue -> {
                    transfer.safeSet(typeText, parser.asDecimal(data))
                    transfer.safeSet("route", null)
                    transfer.safeSet("requestPayload", null)
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                }
                TransferInputField.size.rawValue -> {
                    transfer.safeSet("size.size", parser.asDecimal(data))
                    transfer.safeSet("route", null)
                    transfer.safeSet("requestPayload", null)
                    if (transfer["type"] == "DEPOSIT") {
                        transfer.safeSet("size.usdcSize", null)
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                }

                TransferInputField.fastSpeed.rawValue -> {
                    transfer.safeSet(typeText, parser.asBool(data))
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                }
                TransferInputField.chain.rawValue -> {
                    val chainType = parser.asString(data)
                    if (chainType != null) {
                        updateTransferToChainType(transfer, chainType)
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber)
                    )
                }
                else -> {}
            }
        } else {
            error = cannotModify(typeText)
        }
    } else {
        changes = StateChanges(iListOf(Changes.wallet, Changes.subaccount, Changes.input), null, iListOf(subaccountNumber))
    }
    input["transfer"] = transfer

    this.input = input
    changes?.let {
        update(it)
    }
    return StateResponse(state, changes, if (error != null) iListOf(error) else null)
}

private fun TradingStateMachine.updateTransferToTokenType(transfer: IMutableMap<String, Any>, token: String) {
    if (transfer["type"] == "TRANSFER_OUT") {
        transfer.safeSet("size.usdcSize", null)
        transfer.safeSet("size.size", null)
    } else {
        transfer.safeSet(
            "resources.tokenSymbol",
            squidProcessor.selectedTokenSymbol(token)
        )
        transfer.safeSet(
            "resources.tokenDecimals",
            squidProcessor.selectedTokenDecimals(token)
        )
    }
    transfer.safeSet("route", null)
    transfer.safeSet("requestPayload", null)
}

private fun TradingStateMachine.updateTransferToChainType(transfer: IMutableMap<String, Any>, chainType: String) {
    val tokenOptions = squidProcessor.tokenOptions(chainType)
    if (transfer["type"] != "TRANSFER_OUT") {
        transfer.safeSet(
            "depositOptions.assets",
            tokenOptions
        )
        transfer.safeSet(
            "withdrawalOptions.assets",
            tokenOptions
        )
        transfer.safeSet("chain", chainType)
        transfer.safeSet("token", squidProcessor.defaultTokenAddress(chainType))
        transfer.safeSet(
            "resources.chainResources",
            squidProcessor.chainResources(chainType)
        )
        transfer.safeSet(
            "resources.tokenResources",
            squidProcessor.tokenResources(chainType)
        )
    }
    transfer.safeSet("size.size", null)
    transfer.safeSet("route", null)
    transfer.safeSet("requestPayload", null)
}

private fun TradingStateMachine.transferDataOptionUsdcSize(typeText: String?): String? {
    return when (typeText) {
        TransferInputField.usdcSize.rawValue -> "options.needsSize"
      //  TransferInputField.address.rawValue -> "options.needsAddress"
        TransferInputField.fastSpeed.rawValue -> "options.needsFastSpeed"

        else -> null
    }
}

private fun TradingStateMachine.transferDataOptionSize(typeText: String?): String? {
    return when (typeText) {
        TransferInputField.size.rawValue -> "options.needsSize"
        //  TransferInputField.address.rawValue -> "options.needsAddress"
        TransferInputField.fastSpeed.rawValue -> "options.needsFastSpeed"

        else -> null
    }
}

fun TradingStateMachine.validTransferInput(transfer: IMap<String, Any>, typeText: String?): Boolean {
    val option = if (transfer["type"] == "TRANSFER_OUT" && transfer["token"] == "dydx") {
        transferDataOptionSize(typeText)
    } else {
        transferDataOptionUsdcSize(typeText)
    }
    return if (option != null) {
        val value = parser.value(transfer, option)
        if (parser.asList(value) != null) {
            true
        } else {
            parser.asBool(value) ?: false
        }
    } else true
}
