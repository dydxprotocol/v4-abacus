package exchange.dydx.abacus.validator

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.iMapOf
import exchange.dydx.abacus.validator.trade.TradeAccountStateValidator
import exchange.dydx.abacus.validator.trade.TradeBracketOrdersValidator
import exchange.dydx.abacus.validator.trade.TradeInputDataValidator
import exchange.dydx.abacus.validator.trade.TradeMarketOrderInputValidator
import exchange.dydx.abacus.validator.trade.TradePositionStateValidator
import exchange.dydx.abacus.validator.trade.TradeTriggerPriceValidator
import kollections.iListOf
import kollections.iMutableListOf

internal class TradeInputValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol
) :
    BaseInputValidator(localizer, formatter, parser), ValidatorProtocol {
    private val tradeValidators = iListOf<TradeValidatorProtocol>(
        TradeInputDataValidator(localizer, formatter, parser),
        TradeMarketOrderInputValidator(localizer, formatter, parser),
        TradeBracketOrdersValidator(localizer, formatter, parser),
        TradeTriggerPriceValidator(localizer, formatter, parser),
        TradePositionStateValidator(localizer, formatter, parser),
        TradeAccountStateValidator(localizer, formatter, parser),
    )

    override fun validate(
        wallet: IMap<String, Any>?,
        user: IMap<String, Any>?,
        subaccount: IMap<String, Any>?,
        configs: IMap<String, Any>?,
        markets: IMap<String, Any>?,
        transaction: IMap<String, Any>,
        transactionType: String,
    ): IList<Any>? {
        if (transactionType == "trade") {
            val marketId = parser.asString(transaction["marketId"]) ?: return null
            val change = change(parser, subaccount, transaction)
            val restricted = parser.asBool(user?.get("restricted")) ?: false
            val market = parser.asMap(markets?.get(marketId))
            val errors = iMutableListOf<Any>()

            val closeOnlyError =
                validateClosingOnly(
                    parser,
                    subaccount,
                    market,
                    transaction,
                    change,
                    restricted
                )
            if (closeOnlyError != null) {
                errors.add(closeOnlyError)
            }
            for (validator in tradeValidators) {
                val validatorErrors =
                    validator.validateTrade(
                        subaccount,
                        market,
                        configs,
                        transaction,
                        change,
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

    private fun change(
        parser: ParserProtocol,
        subaccount: IMap<String, Any>?,
        trade: IMap<String, Any>,
    ): PositionChange {
        val marketId = parser.asString(trade["marketId"]) ?: return PositionChange.NONE
        val position =
            parser.asMap(parser.value(subaccount, "openPositions.$marketId"))
                ?: return PositionChange.NONE
        val size = parser.asDouble(parser.value(position, "size.current")) ?: Numeric.double.ZERO
        val postOrder =
            parser.asDouble(parser.value(position, "size.postOrder")) ?: Numeric.double.ZERO
        return if (size != Numeric.double.ZERO) {
            if (postOrder != Numeric.double.ZERO) {
                if (size > Numeric.double.ZERO) {
                    if (postOrder > size)
                        PositionChange.INCREASING
                    else if (postOrder < Numeric.double.ZERO)
                        PositionChange.CROSSING
                    else if (postOrder < size)
                        PositionChange.DECREASING
                    else
                        PositionChange.NONE
                } else {
                    if (postOrder > size)
                        PositionChange.DECREASING
                    else if (postOrder > Numeric.double.ZERO)
                        PositionChange.CROSSING
                    else if (postOrder < size)
                        PositionChange.INCREASING
                    else
                        PositionChange.NONE
                }
            } else PositionChange.CLOSING
        } else {
            if (postOrder != Numeric.double.ZERO)
                PositionChange.NEW
            else
                PositionChange.NONE
        }
    }

    private fun validateClosingOnly(
        parser: ParserProtocol,
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
                            null,
                            null,
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
