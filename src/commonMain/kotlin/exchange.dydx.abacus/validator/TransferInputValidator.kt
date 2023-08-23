package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.validator.transfer.DepositValidator
import exchange.dydx.abacus.validator.transfer.TransferOutValidator
import kollections.iListOf
import kollections.iMutableListOf

internal class TransferInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    private val transferValidators = iListOf<TransferValidatorProtocol>(
        DepositValidator(localizer, formatter, parser),
        TransferOutValidator(localizer, formatter, parser),
    )

    override fun validate(
        wallet: IMap<String, Any>?,
        user: IMap<String, Any>?,
        subaccount: IMap<String, Any>?,
        markets: IMap<String, Any>?,
        transaction: IMap<String, Any>,
        transactionType: String,
    ): IList<Any>? {
        if (transactionType == "transfer") {
            val errors = iMutableListOf<Any>()
            val restricted = parser.asBool(user?.get("restricted")) ?: false
            for (validator in transferValidators) {
                val validatorErrors =
                    validator.validateTransfer(
                        wallet,
                        subaccount,
                        transaction,
                        restricted
                    )
                if (validatorErrors != null) {
                    errors.addAll(validatorErrors)
                }
            }
            return errors
        }
        return null
    }

    private fun validateClosingOnly(
        subaccount: IMap<String, Any>?,
        market: IMap<String, Any>?,
        trade: IMap<String, Any>,
        change: PositionChange,
        restricted: Boolean,
    ): IMap<String, Any>? {
        val marketId = parser.asMap(market?.get("assetId")) ?: ""
        val canTrade = parser.asBool(parser.value(market, "status.canTrade")) ?: true
        val canReduce = parser.asBool(parser.value(market, "status.canTrade")) ?: true
        return if (canTrade) {
            if (restricted) {
                when (change) {
                    PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING ->
                        error(
                            "ERROR",
                            "RESTRICTED_USER",
                            iListOf("size.size"),
                            "APP.TRADE.MODIFY_SIZE_FIELD",
                            "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_CLOSE_POSITION_ONLY",
                            "ERRORS.TRADE_BOX.MARKET_ORDER_CLOSE_POSITION_ONLY"
                        )

                    else -> null
                }
            } else {
                return null
            }
        } else if (canReduce) {
            when (change) {
                PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING ->
                    error(
                        "ERROR",
                        "CLOSE_ONLY_MARKET",
                        iListOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
                        "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
                        iMapOf(
                            "MARKET" to iMapOf(
                                "value" to marketId,
                                "format" to "string"
                            )
                        )
                    )

                else -> null
            }
        } else error(
            "ERROR",
            "CLOSED_MARKET",
            null,
            null,
            "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
            "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
            iMapOf(
                "MARKET" to iMapOf(
                    "value" to marketId,
                    "format" to "string"
                )
            )
        )
    }
}
