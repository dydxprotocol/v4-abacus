package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.BlockAndTime
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.validator.trade.TradeAccountStateValidator
import exchange.dydx.abacus.validator.trade.TradeBracketOrdersValidator
import exchange.dydx.abacus.validator.trade.TradeInputDataValidator
import exchange.dydx.abacus.validator.trade.TradeMarketOrderInputValidator
import exchange.dydx.abacus.validator.trade.TradePositionStateValidator
import exchange.dydx.abacus.validator.trade.TradeTriggerPriceValidator

internal class TradeInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol
) :
    BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    private val tradeValidators = listOf<TradeValidatorProtocol>(
        TradeInputDataValidator(localizer, formatter, parser),
        TradeMarketOrderInputValidator(localizer, formatter, parser),
        TradeBracketOrdersValidator(localizer, formatter, parser),
        TradeTriggerPriceValidator(localizer, formatter, parser),
        TradePositionStateValidator(localizer, formatter, parser),
        TradeAccountStateValidator(localizer, formatter, parser),
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
        if (transactionType == "trade" || transactionType == "closePosition") {
            val marketId = parser.asString(transaction["marketId"]) ?: return null
            val change = change(parser, subaccount, transaction)
            val restricted = parser.asBool(user?.get("restricted")) ?: false
            val market = parser.asNativeMap(markets?.get(marketId))
            val errors = mutableListOf<Any>()

            val closeOnlyError =
                validateClosingOnly(
                    parser,
                    subaccount,
                    market,
                    transaction,
                    change,
                    restricted,
                )
            if (closeOnlyError != null) {
                errors.add(closeOnlyError)
            }
            for (validator in tradeValidators) {
                val validatorErrors =
                    validator.validateTrade(
                        staticTyping = staticTyping,
                        internalState = internalState,
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
            return errors
        }
        return null
    }

    private fun change(
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

    private fun validateClosingOnly(
        parser: ParserProtocol,
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
                            null,
                            null,
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
