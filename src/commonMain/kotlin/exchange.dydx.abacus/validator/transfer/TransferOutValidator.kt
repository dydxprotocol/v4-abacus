package exchange.dydx.abacus.validator.transfer

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.beth32.Bech32
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.TransferValidatorProtocol
import kollections.iListOf

internal class TransferOutValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TransferValidatorProtocol {
    override fun validateTransfer(
        wallet: IMap<String, Any>?,
        subaccount: IMap<String, Any>?,
        transfer: IMap<String, Any>,
        restricted: Boolean
    ): IList<Any>? {
        val address = parser.asString(parser.value(transfer, "address"))
        val type = parser.asString(parser.value(transfer, "type"))
        if (type == "TRANSFER_OUT" && address != null && !isAddressValid(address)) {
            return iListOf(
                error(
                    "ERROR",
                    "INVALID_ADDRESS",
                    iListOf("address"),
                    "APP.DIRECT_TRANSFER_MODAL.ADDRESS_FIELD",
                    "APP.DIRECT_TRANSFER_MODAL.INVALID_ADDRESS_TITLE",
                    "APP.DIRECT_TRANSFER_MODAL.INVALID_ADDRESS_BODY"
                )
            )
        } else {
            return null
        }
    }

    private fun isAddressValid(address: String): Boolean {
        try {
            val (humanReadablePart, data) = Bech32.decode(address)
            return humanReadablePart == "dydx"
        } catch (e: Exception) {
            return false
        }
    }
}

