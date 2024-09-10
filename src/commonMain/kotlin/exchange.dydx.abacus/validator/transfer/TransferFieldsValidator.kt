package exchange.dydx.abacus.validator.transfer

import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.TransferValidatorProtocol

internal class TransferFieldsValidator(
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
        val errors = mutableListOf<ValidationError>()
        val transfer = internalState.input.transfer
        val type = transfer.type ?: return null
        when (type) {
            TransferType.deposit -> {
                val usdcSize = parser.asDouble(transfer.size?.usdcSize) ?: 0.0
                if (usdcSize <= 0.0) {
                    errors.add(
                        required(
                            errorCode = "REQUIRED_SIZE",
                            field = "size.usdcSize",
                            actionStringKey = "APP.TRADE.ENTER_AMOUNT",
                        ),
                    )
                }
            }
            TransferType.withdrawal -> {
                val usdcSize = parser.asDouble(transfer.size?.usdcSize) ?: 0.0
                if (usdcSize <= 0.0) {
                    errors.add(
                        required(
                            errorCode = "REQUIRED_SIZE",
                            field = "size.usdcSize",
                            actionStringKey = "APP.TRADE.ENTER_AMOUNT",
                        ),
                    )
                }
                if (transfer.address == null) {
                    errors.add(
                        required(
                            errorCode = "REQUIRED_ADDRESS",
                            field = "address",
                            actionStringKey = "APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS",
                        ),
                    )
                }
            }
            TransferType.transferOut -> {
                if (transfer.address == null) {
                    errors.add(
                        required(
                            errorCode = "REQUIRED_ADDRESS",
                            field = "address",
                            actionStringKey = "APP.DIRECT_TRANSFER_MODAL.ENTER_ETH_ADDRESS",
                        ),
                    )
                }
            }
        }

        return errors
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
        return null
    }
}
