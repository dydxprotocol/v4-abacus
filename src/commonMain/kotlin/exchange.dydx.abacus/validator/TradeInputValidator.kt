package exchange.dydx.abacus.validator

import exchange.dydx.abacus.calculator.CalculationPeriod
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.internalstate.InternalSubaccountState
import exchange.dydx.abacus.state.internalstate.InternalTradeInputState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.TradeValidationPayload
import exchange.dydx.abacus.utils.TradeValidationTracker
import exchange.dydx.abacus.validator.trade.TradeAccountStateValidator
import exchange.dydx.abacus.validator.trade.TradeBracketOrdersValidator
import exchange.dydx.abacus.validator.trade.TradeFieldsValidator
import exchange.dydx.abacus.validator.trade.TradeInputDataValidator
import exchange.dydx.abacus.validator.trade.TradeOrderInputValidator
import exchange.dydx.abacus.validator.trade.TradePositionStateValidator
import exchange.dydx.abacus.validator.trade.TradeResctrictedValidator
import exchange.dydx.abacus.validator.trade.TradeTriggerPriceValidator

internal class TradeInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
    private val tradeValidationTracker: TradeValidationTracker,
) : BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    private val tradeValidators = listOf<TradeValidatorProtocol>(
        TradeFieldsValidator(localizer, formatter, parser),
        TradeResctrictedValidator(localizer, formatter, parser),
        TradeInputDataValidator(localizer, formatter, parser),
        TradeOrderInputValidator(localizer, formatter, parser),
        TradeBracketOrdersValidator(localizer, formatter, parser),
        TradeTriggerPriceValidator(localizer, formatter, parser),
        TradePositionStateValidator(localizer, formatter, parser),
        TradeAccountStateValidator(localizer, formatter, parser),
    )

    override fun validate(
        internalState: InternalState,
        subaccountNumber: Int?,
        currentBlockAndHeight: BlockAndTime?,
        inputType: InputType,
        environment: V4Environment?,
    ): List<ValidationError>? {
        val transactionType = internalState.input.currentType
        if (transactionType != InputType.TRADE && transactionType != InputType.CLOSE_POSITION) {
            return null
        }
        val change = getPositionChange(
            subaccount = internalState.wallet.account.subaccounts[subaccountNumber],
            trade = internalState.input.trade,
        )
        val restricted = internalState.wallet.user?.restricted ?: false

        val errors = mutableListOf<ValidationError>()
        for (validator in tradeValidators) {
            val validatorErrors =
                validator.validateTrade(
                    internalState = internalState,
                    subaccountNumber = subaccountNumber ?: 0,
                    change = change,
                    restricted = restricted,
                    environment = environment,
                )
            if (validatorErrors != null) {
                errors.addAll(validatorErrors)
            }
        }

        val marketId = internalState.input.trade.marketId ?: return errors

        tradeValidationTracker.logValidationResult(
            TradeValidationPayload(
                errors = errors.map { it.code },
                marketId = marketId,
                size = internalState.input.trade.size?.size,
                notionalSize = internalState.input.trade.size?.usdcSize,
            ).apply {
                indexSlippage = internalState.input.trade.summary?.indexSlippage
                orderbookSlippage = internalState.input.trade.summary?.slippage
            },
        )

        return errors
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
        if (transactionType == "trade" || transactionType == "closePosition") {
            val marketId = parser.asString(transaction["marketId"]) ?: return null
            val change = getPositionChangeDeprecated(parser, subaccount, transaction)
            val restricted = parser.asBool(user?.get("restricted")) ?: false
            val market = parser.asNativeMap(markets?.get(marketId))
            val errors = mutableListOf<Any>()

            for (validator in tradeValidators) {
                val validatorErrors =
                    validator.validateTradeDeprecated(
                        subaccount = subaccount,
                        market = market,
                        configs = configs,
                        trade = transaction,
                        change = change,
                        restricted = restricted,
                        environment = environment,
                    )
                if (validatorErrors != null) {
                    errors.addAll(validatorErrors)
                }
            }

            val sizeParent = parser.asNativeMap(transaction["size"])

            tradeValidationTracker.logValidationResult(
                TradeValidationPayload(
                    errors = errors.mapNotNull { (it as? Map<*, *>)?.get("code") as? String },
                    marketId = marketId,
                    size = parser.asDouble(sizeParent?.get("size")),
                    notionalSize = parser.asDouble(sizeParent?.get("usdcSize")),
                ).apply {
                    indexSlippage = parser.asDouble(parser.asNativeMap(transaction.get("summary"))?.get("indexSlippage"))
                    orderbookSlippage = parser.asDouble(parser.asNativeMap(transaction.get("summary"))?.get("slippage"))
                },
            )

            return errors
        }
        return null
    }

    private fun getPositionChange(
        subaccount: InternalSubaccountState?,
        trade: InternalTradeInputState,
    ): PositionChange {
        val marketId = trade.marketId ?: return PositionChange.NONE
        val position = subaccount?.openPositions?.get(marketId) ?: return PositionChange.NONE
        val size = position.calculated[CalculationPeriod.current]?.size ?: Numeric.double.ZERO
        val postOrder = position.calculated[CalculationPeriod.post]?.size ?: Numeric.double.ZERO
        return if (size != Numeric.double.ZERO) {
            if (postOrder != Numeric.double.ZERO) {
                if (size > Numeric.double.ZERO) {
                    if (postOrder > size) {
                        PositionChange.INCREASING
                    } else if (postOrder < Numeric.double.ZERO) {
                        PositionChange.CROSSING
                    } else if (postOrder < size) {
                        PositionChange.DECREASING
                    } else {
                        PositionChange.NONE
                    }
                } else {
                    if (postOrder > size) {
                        PositionChange.DECREASING
                    } else if (postOrder > Numeric.double.ZERO) {
                        PositionChange.CROSSING
                    } else if (postOrder < size) {
                        PositionChange.INCREASING
                    } else {
                        PositionChange.NONE
                    }
                }
            } else {
                PositionChange.CLOSING
            }
        } else {
            if (postOrder != Numeric.double.ZERO) {
                PositionChange.NEW
            } else {
                PositionChange.NONE
            }
        }
    }

    private fun getPositionChangeDeprecated(
        parser: ParserProtocol,
        subaccount: Map<String, Any>?,
        trade: Map<String, Any>,
    ): PositionChange {
        val marketId = parser.asString(trade["marketId"]) ?: return PositionChange.NONE
        val position =
            parser.asNativeMap(parser.value(subaccount, "openPositions.$marketId"))
                ?: return PositionChange.NONE
        val size = parser.asDouble(parser.value(position, "size.current")) ?: Numeric.double.ZERO
        val postOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: Numeric.double.ZERO
        return if (size != Numeric.double.ZERO) {
            if (postOrder != Numeric.double.ZERO) {
                if (size > Numeric.double.ZERO) {
                    if (postOrder > size) {
                        PositionChange.INCREASING
                    } else if (postOrder < Numeric.double.ZERO) {
                        PositionChange.CROSSING
                    } else if (postOrder < size) {
                        PositionChange.DECREASING
                    } else {
                        PositionChange.NONE
                    }
                } else {
                    if (postOrder > size) {
                        PositionChange.DECREASING
                    } else if (postOrder > Numeric.double.ZERO) {
                        PositionChange.CROSSING
                    } else if (postOrder < size) {
                        PositionChange.INCREASING
                    } else {
                        PositionChange.NONE
                    }
                }
            } else {
                PositionChange.CLOSING
            }
        } else {
            if (postOrder != Numeric.double.ZERO) {
                PositionChange.NEW
            } else {
                PositionChange.NONE
            }
        }
    }
}
