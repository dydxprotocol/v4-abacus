package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment

internal class AccountInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    override fun validate(
        wallet: Map<String, Any>?,
        user: Map<String, Any>?,
        subaccount: Map<String, Any>?,
        markets: Map<String, Any>?,
        configs: Map<String, Any>?,
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
            error(
                "ERROR",
                "REQUIRED_WALLET",
                null,
                "ERRORS.TRADE_BOX_TITLE.CONNECT_WALLET_TO_TRADE",
                "ERRORS.TRADE_BOX_TITLE.CONNECT_WALLET_TO_TRADE",
                "ERRORS.TRADE_BOX.CONNECT_WALLET_TO_TRADE",
                null,
                "/onboard",
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
            error(
                "ERROR",
                "REQUIRED_ACCOUNT",
                null,
                "ERRORS.TRADE_BOX_TITLE.DEPOSIT_TO_TRADE",
                "ERRORS.TRADE_BOX_TITLE.DEPOSIT_TO_TRADE",
                "ERRORS.TRADE_BOX.DEPOSIT_TO_TRADE",
                null,
                "/deposit",
            )
        }
    }

    private fun checkEquity(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
    ): Map<String, Any>? {
        val equity = parser.asDouble(parser.value(subaccount, "equity.current"))
        return if (equity != null && equity > 0) {
            null
        } else {
            error(
                "ERROR",
                "NO_EQUITY_DEPOSIT_FIRST",
                null,
                "ERRORS.TRADE_BOX_TITLE.NO_EQUITY_DEPOSIT_FIRST",
                "ERRORS.TRADE_BOX_TITLE.NO_EQUITY_DEPOSIT_FIRST",
                "ERRORS.TRADE_BOX.NO_EQUITY_DEPOSIT_FIRST",
                null,
                "/deposit",
            )
        }
    }
}
