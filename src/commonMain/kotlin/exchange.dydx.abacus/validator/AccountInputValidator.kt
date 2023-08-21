package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import kollections.iListOf

internal class AccountInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    override fun validate(
        wallet: IMap<String, Any>?,
        user: IMap<String, Any>?,
        subaccount: IMap<String, Any>?,
        markets: IMap<String, Any>?,
        transaction: IMap<String, Any>,
        transactionType: String,
    ): IList<Any>? {
        val error = missingWallet(parser, wallet) ?: missingAccount(parser, wallet) ?: checkEquity(
            parser,
            subaccount
        )
        return if (error != null) iListOf(error) else null
    }

    private fun missingWallet(
        parser: ParserProtocol,
        wallet: IMap<String, Any>?,
    ): IMap<String, Any>? {
        return if (wallet != null) null else {
            error(
                "ERROR",
                "REQUIRED_WALLET",
                null,
                "ERRORS.TRADE_BOX_TITLE.CONNECT_WALLET_TO_TRADE",
                "ERRORS.TRADE_BOX_TITLE.CONNECT_WALLET_TO_TRADE",
                "ERRORS.TRADE_BOX.CONNECT_WALLET_TO_TRADE",
                null,
                "/onboard"
            )
        }
    }

    private fun missingAccount(
        parser: ParserProtocol,
        wallet: IMap<String, Any>?,
    ): IMap<String, Any>? {
        val account = parser.asMap(wallet?.get("account"))
        return if (account != null) null else {
            error(
                "ERROR",
                "REQUIRED_ACCOUNT",
                null,
                "ERRORS.TRADE_BOX_TITLE.DEPOSIT_TO_TRADE",
                "ERRORS.TRADE_BOX_TITLE.DEPOSIT_TO_TRADE",
                "ERRORS.TRADE_BOX.DEPOSIT_TO_TRADE",
                null,
                "/deposit"
            )
        }
    }


    private fun checkEquity(
        parser: ParserProtocol,
        subaccount: IMap<String, Any>?,
    ): IMap<String, Any>? {
        val equity = parser.asDouble(parser.value(subaccount, "equity.current"))
        return if (equity != null && equity > 0) null else {
            error(
                "ERROR",
                "NO_EQUITY_DEPOSIT_FIRST",
                null,
                "ERRORS.TRADE_BOX_TITLE.NO_EQUITY_DEPOSIT_FIRST",
                "ERRORS.TRADE_BOX_TITLE.NO_EQUITY_DEPOSIT_FIRST",
                "ERRORS.TRADE_BOX.NO_EQUITY_DEPOSIT_FIRST",
                null,
                "/deposit"
            )
        }
    }
}
