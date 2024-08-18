package exchange.dydx.abacus.validator

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS

internal class AccountInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    override fun validate(
        internalState: InternalState,
        subaccountNumber: Int?,
        currentBlockAndHeight: BlockAndTime?,
        inputType: InputType,
        environment: V4Environment?,
    ): List<ValidationError>? {
        TODO("Not yet implemented")
    }

    override fun validateDeprecated(
        wallet: Map<String, Any>?,
        user: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        markets: Map<String, Any>?,
        configs: Map<String, Any>?,
        currentBlockAndHeight: BlockAndTime?,
        transaction: Map<String, Any>,
        transactionType: String,
        environment: V4Environment?,
    ): List<Any>? {
        val error = missingWallet(parser, wallet) ?: missingAccount(parser, wallet) ?: checkEquity(
            parser,
            subaccount,
        )
        return if (error != null) listOf(error) else null
    }

    private fun missingWallet(
        parser: ParserProtocol,
        wallet: Map<String, Any>?,
    ): Map<String, Any>? {
        return if (wallet != null) {
            null
        } else {
            errorDeprecated(
                type = "ERROR",
                errorCode = "REQUIRED_WALLET",
                fields = null,
                actionStringKey = "ERRORS.TRADE_BOX_TITLE.CONNECT_WALLET_TO_TRADE",
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.CONNECT_WALLET_TO_TRADE",
                textStringKey = "ERRORS.TRADE_BOX.CONNECT_WALLET_TO_TRADE",
                textParams = null,
                action = "/onboard",
            )
        }
    }

    private fun missingAccount(
        parser: ParserProtocol,
        wallet: Map<String, Any>?,
    ): Map<String, Any>? {
        val account = parser.asNativeMap(wallet?.get("account"))
        return if (account != null) {
            null
        } else {
            errorDeprecated(
                type = "ERROR",
                errorCode = "REQUIRED_ACCOUNT",
                fields = null,
                actionStringKey = "ERRORS.TRADE_BOX_TITLE.DEPOSIT_TO_TRADE",
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.DEPOSIT_TO_TRADE",
                textStringKey = "ERRORS.TRADE_BOX.DEPOSIT_TO_TRADE",
                textParams = null,
                action = "/deposit",
            )
        }
    }

    private fun checkEquity(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
    ): Map<String, Any>? {
        val equity = parser.asDouble(parser.value(subaccount, "equity.current"))
        val subaccountNumber = parser.asInt(subaccount?.get("subaccountNumber"))
        val isChildSubaccountForIsolatedMargin = subaccountNumber == null || subaccountNumber >= NUM_PARENT_SUBACCOUNTS

        return if (equity != null && equity > 0) {
            null
        } else if (isChildSubaccountForIsolatedMargin) {
            // Equity is null when a user is placing an Isolated Margin trade on a childSubaccount
            // subaccountNumber is null when a childSubaccount has not been created yet
            null
        } else {
            errorDeprecated(
                type = "ERROR",
                errorCode = "NO_EQUITY_DEPOSIT_FIRST",
                fields = null,
                actionStringKey = "ERRORS.TRADE_BOX_TITLE.NO_EQUITY_DEPOSIT_FIRST",
                titleStringKey = "ERRORS.TRADE_BOX_TITLE.NO_EQUITY_DEPOSIT_FIRST",
                textStringKey = "ERRORS.TRADE_BOX.NO_EQUITY_DEPOSIT_FIRST",
                textParams = null,
                action = "/deposit",
            )
        }
    }
}
