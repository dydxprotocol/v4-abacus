package exchange.dydx.abacus.validator

import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
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
        wallet: IMap<String, Any>?,
        user: IMap<String, Any>?,
        subaccount: IMap<String, Any>?,
        markets: IMap<String, Any>?,
        transaction: IMap<String, Any>,
        transactionType: String
    ): IList<Any>?
}

interface TradeValidatorProtocol {
    fun validateTrade(
        subaccount: IMap<String, Any>?,
        market: IMap<String, Any>?,
        trade: IMap<String, Any>,
        change: PositionChange,
        restricted: Boolean
    ): IList<Any>?
}

interface TransferValidatorProtocol {
    fun validateTransfer(
        wallet: IMap<String, Any>?,
        subaccount: IMap<String, Any>?,
        transfer: IMap<String, Any>,
        restricted: Boolean
    ): IList<Any>?
}