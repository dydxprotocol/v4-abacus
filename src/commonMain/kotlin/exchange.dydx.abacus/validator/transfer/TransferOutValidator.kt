package exchange.dydx.abacus.validator.transfer

import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.isAddressValid
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.TransferValidatorProtocol

internal class TransferOutValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TransferValidatorProtocol {
    override fun validateTransfer(
        internalState: InternalState,
        currentBlockAndHeight: BlockAndTime?,
        restricted: Boolean,
        environment: V4Environment?
    ): List<ValidationError>? {
        val transfer = internalState.input.transfer ?: return null
        val address = transfer.address
        val type = transfer.type
        if (type == TransferType.transferOut && !address.isNullOrEmpty() && !address.isAddressValid()) {
            return listOf(
                error(
                    type = ErrorType.error,
                    errorCode = "INVALID_ADDRESS",
                    fields = listOf("address"),
                    actionStringKey = "APP.DIRECT_TRANSFER_MODAL.ADDRESS_FIELD",
                    titleStringKey = "APP.DIRECT_TRANSFER_MODAL.INVALID_ADDRESS_TITLE",
                    textStringKey = "APP.DIRECT_TRANSFER_MODAL.INVALID_ADDRESS_BODY",
                ),
            )
        } else {
            return null
        }
    }

    override fun validateTransferDeprecated(
        wallet: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        transfer: Map<String, Any>,
        configs: Map<String, Any>?,
        currentBlockAndHeight: BlockAndTime?,
        restricted: Boolean,
        environment: V4Environment?
    ): List<Any>? {
        val address = parser.asString(parser.value(transfer, "address"))
        val type = parser.asString(parser.value(transfer, "type"))
        if (type == "TRANSFER_OUT" && !address.isNullOrEmpty() && !address.isAddressValid()) {
            return listOf(
                errorDeprecated(
                    type = "ERROR",
                    errorCode = "INVALID_ADDRESS",
                    fields = listOf("address"),
                    actionStringKey = "APP.DIRECT_TRANSFER_MODAL.ADDRESS_FIELD",
                    titleStringKey = "APP.DIRECT_TRANSFER_MODAL.INVALID_ADDRESS_TITLE",
                    textStringKey = "APP.DIRECT_TRANSFER_MODAL.INVALID_ADDRESS_BODY",
                ),
            )
        } else {
            return null
        }
    }
}
