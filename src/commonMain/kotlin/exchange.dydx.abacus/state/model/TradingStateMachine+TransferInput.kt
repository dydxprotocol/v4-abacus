package exchange.dydx.abacus.state.model

import exchange.dydx.abacus.calculator.TransferInputCalculator
import exchange.dydx.abacus.responses.ParsingError
import exchange.dydx.abacus.responses.StateResponse
import exchange.dydx.abacus.state.changes.Changes
import exchange.dydx.abacus.state.changes.StateChanges
import exchange.dydx.abacus.utils.mutable
import exchange.dydx.abacus.utils.mutableMapOf
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

    exchange("exchange"),
    chain("chain"),
    token("token"),
    address("address"),
    MEMO("memo"),
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

    val input = this.input?.mutable() ?: mutableMapOf()
    input["current"] = "transfer"
    val transfer = parser.asMap(input["transfer"])?.mutable() ?: kotlin.run {
        val transfer = mutableMapOf<String, Any>()
        transfer["type"] = "DEPOSIT"

        val calculator = TransferInputCalculator(parser)
        val params = mutableMapOf<String, Any>()
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
                        transfer.safeSet("memo", null)
                        if (parser.asString(data) == "TRANSFER_OUT") {
                            transfer.safeSet("chain", "chain")
                            transfer.safeSet("token", "usdc")
                        } else {
                            val chainType = routerProcessor.defaultChainId()
                            if (chainType != null) {
                                updateTransferToChainType(transfer, chainType)
                            }
                            transfer.safeSet("token", routerProcessor.defaultTokenAddress(chainType))
                        }
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber),
                    )
                }
                TransferInputField.address.rawValue -> {
                    val address = parser.asString(data)
                    transfer.safeSet(typeText, address)
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber),
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
                        iListOf(subaccountNumber),
                    )
                }
                TransferInputField.usdcSize.rawValue,
                TransferInputField.usdcFee.rawValue -> {
                    transfer.safeSet(typeText, parser.asDouble(data))
                    transfer.safeSet("route", null)
                    transfer.safeSet("requestPayload", null)
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber),
                    )
                }
                TransferInputField.size.rawValue -> {
                    transfer.safeSet("size.size", parser.asDouble(data))
                    transfer.safeSet("route", null)
                    transfer.safeSet("requestPayload", null)
                    if (transfer["type"] == "DEPOSIT") {
                        transfer.safeSet("size.usdcSize", null)
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber),
                    )
                }
                TransferInputField.fastSpeed.rawValue -> {
                    transfer.safeSet(typeText, parser.asBool(data))
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber),
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
                        iListOf(subaccountNumber),
                    )
                }
                TransferInputField.exchange.rawValue -> {
                    val exchange = parser.asString(data)
                    if (exchange != null) {
                        updateTransferExchangeType(transfer, exchange)
                    }
                    changes = StateChanges(
                        iListOf(Changes.wallet, Changes.subaccount, Changes.input),
                        null,
                        iListOf(subaccountNumber),
                    )
                }
                TransferInputField.MEMO.rawValue -> {
                    transfer.safeSet(typeText, parser.asString(data))
                    changes = StateChanges(
                        iListOf(Changes.input),
                        null,
                        iListOf(subaccountNumber),
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

private fun TradingStateMachine.updateTransferToTokenType(transfer: MutableMap<String, Any>, tokenAddress: String) {
    val selectedChainId = transfer["chain"] as? String
    if (transfer["type"] == "TRANSFER_OUT") {
        transfer.safeSet("size.usdcSize", null)
        transfer.safeSet("size.size", null)
    } else {
        transfer.safeSet(
            "resources.tokenSymbol",
            routerProcessor.selectedTokenSymbol(tokenAddress = tokenAddress, selectedChainId = selectedChainId),
        )
        transfer.safeSet(
            "resources.tokenDecimals",
            routerProcessor.selectedTokenDecimals(tokenAddress = tokenAddress, selectedChainId = selectedChainId),
        )
    }
    transfer.safeSet("route", null)
    transfer.safeSet("requestPayload", null)
}

private fun TradingStateMachine.updateTransferToChainType(transfer: MutableMap<String, Any>, chainType: String) {
    val tokenOptions = routerProcessor.tokenOptions(chainType)
    if (transfer["type"] != "TRANSFER_OUT") {
        internalState.transfer.tokens = tokenOptions
        transfer.safeSet("chain", chainType)
        transfer.safeSet("token", routerProcessor.defaultTokenAddress(chainType))
        internalState.transfer.chainResources = routerProcessor.chainResources(chainType)
        internalState.transfer.tokenResources = routerProcessor.tokenResources(chainType)
    }
    transfer.safeSet("exchange", null)
    transfer.safeSet("size.size", null)
    transfer.safeSet("route", null)
    transfer.safeSet("requestPayload", null)
//    needed to pass tests, remove later
    transfer.safeSet(
        "depositOptions.assets",
        tokenOptions,
    )
    transfer.safeSet(
        "withdrawalOptions.assets",
        tokenOptions,
    )
    transfer.safeSet(
        "resources.chainResources",
        routerProcessor.chainResources(chainType),
    )
    transfer.safeSet(
        "resources.tokenResources",
        routerProcessor.tokenResources(chainType),
    )
}

private fun TradingStateMachine.updateTransferExchangeType(transfer: MutableMap<String, Any>, exchange: String) {
    val exchangeDestinationChainId = routerProcessor.exchangeDestinationChainId
    val tokenOptions = routerProcessor.tokenOptions(exchangeDestinationChainId)
    if (transfer["type"] != "TRANSFER_OUT") {
        internalState.transfer.tokens = tokenOptions
        transfer.safeSet("token", routerProcessor.defaultTokenAddress(exchangeDestinationChainId))
        internalState.transfer.tokenResources = routerProcessor.tokenResources(exchangeDestinationChainId)

//        needed to pass tests, remove later
        transfer.safeSet(
            "depositOptions.assets",
            tokenOptions,
        )
        transfer.safeSet(
            "withdrawalOptions.assets",
            tokenOptions,
        )
        transfer.safeSet(
            "resources.tokenResources",
            routerProcessor.tokenResources(exchangeDestinationChainId),
        )
    }
    transfer.safeSet("exchange", exchange)
    transfer.safeSet("chain", null)
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

fun TradingStateMachine.validTransferInput(transfer: Map<String, Any>, typeText: String?): Boolean {
    val option = if (transfer["type"] == "TRANSFER_OUT" && transfer["token"] == "chain") {
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
    } else {
        true
    }
}
