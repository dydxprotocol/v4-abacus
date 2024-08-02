package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.transfer.DepositValidator
import exchange.dydx.abacus.validator.transfer.TransferOutValidator
import exchange.dydx.abacus.validator.transfer.WithdrawalCapacityValidator
import exchange.dydx.abacus.validator.transfer.WithdrawalGatingValidator

internal class TransferInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    private val transferValidators = listOf<TransferValidatorProtocol>(
        DepositValidator(localizer, formatter, parser),
        TransferOutValidator(localizer, formatter, parser),
        WithdrawalGatingValidator(localizer, formatter, parser),
        WithdrawalCapacityValidator(localizer, formatter, parser),
    )

    override fun validate(
        staticTyping: Boolean,
        internalState: InternalState,
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
        if (transactionType == "transfer") {
            val errors = mutableListOf<Any>()
            val restricted = parser.asBool(user?.get("restricted")) ?: false
            for (validator in transferValidators) {
                val validatorErrors =
                    validator.validateTransfer(
                        staticTyping = staticTyping,
                        internalState = internalState,
                        wallet = wallet,
                        subaccount = subaccount,
                        transfer = transaction,
                        configs = configs,
                        currentBlockAndHeight = currentBlockAndHeight,
                        restricted = restricted,
                        environment = environment,
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
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
    ): Map<String, Any>? {
        val marketId = parser.asNativeMap(market?.get("assetId")) ?: ""
        val canTrade = parser.asBool(parser.value(market, "status.canTrade")) ?: true
        val canReduce = parser.asBool(parser.value(market, "status.canTrade")) ?: true
        return if (canTrade) {
            if (restricted) {
                when (change) {
                    PositionChange.NEW, PositionChange.INCREASING, PositionChange.CROSSING ->
                        error(
                            "ERROR",
                            "RESTRICTED_USER",
                            listOf("size.size"),
                            "APP.TRADE.MODIFY_SIZE_FIELD",
                            "ERRORS.TRADE_BOX_TITLE.MARKET_ORDER_CLOSE_POSITION_ONLY",
                            "ERRORS.TRADE_BOX.MARKET_ORDER_CLOSE_POSITION_ONLY",
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
                        listOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
                        "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
                        mapOf(
                            "MARKET" to mapOf(
                                "value" to marketId,
                                "format" to "string",
                            ),
                        ),
                    )

                else -> null
            }
        } else {
            error(
                "ERROR",
                "CLOSED_MARKET",
                null,
                null,
                "WARNINGS.TRADE_BOX_TITLE.MARKET_STATUS_CLOSE_ONLY",
                "WARNINGS.TRADE_BOX.MARKET_STATUS_CLOSE_ONLY",
                mapOf(
                    "MARKET" to mapOf(
                        "value" to marketId,
                        "format" to "string",
                    ),
                ),
            )
        }
    }
}
