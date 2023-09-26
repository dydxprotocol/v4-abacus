package exchange.dydx.abacus.validator

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class PositionChange(val rawValue: String) {
    NONE("None"),
    NEW("New"),
    INCREASING("Increasing"),
    DECREASING("Decreasing"),
    CROSSING("Crossing"),
    CLOSING("Closing");

    companion object {
        operator fun invoke(rawValue: String) =
            PositionChange.values().firstOrNull { it.rawValue == rawValue }
    }
}

interface ValidatorProtocol {
    fun validate(
        wallet: Map<String, Any>?,
        user: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        markets: Map<String, Any>?,
        configs: Map<String, Any>?,
        transaction: Map<String, Any>,
        transactionType: String
    ): List<Any>?
}

interface TradeValidatorProtocol {
    fun validateTrade(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean
    ): List<Any>?
}

interface TransferValidatorProtocol {
    fun validateTransfer(
        wallet: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        transfer: Map<String, Any>,
        restricted: Boolean
    ): List<Any>?
}