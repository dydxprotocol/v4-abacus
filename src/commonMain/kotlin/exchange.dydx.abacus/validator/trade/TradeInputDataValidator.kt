package exchange.dydx.abacus.validator.trade

import abs
import exchange.dydx.abacus.output.Asset
import exchange.dydx.abacus.output.input.ErrorType
import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderSide
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.InternalMarketState
import exchange.dydx.abacus.state.InternalState
import exchange.dydx.abacus.state.InternalTradeInputState
import exchange.dydx.abacus.state.helper.Formatter
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol
import kotlin.time.Duration.Companion.days

/*
Covers basic check of required fields
Covers checking
LIMIT_MUST_ABOVE_TRIGGER_PRICE
LIMIT_MUST_BELOW_TRIGGER_PRICE
USER_MAX_ORDERS
ORDER_SIZE_BELOW_MIN_SIZE

TODO

LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_HIGHER
LIMIT_PRICE_TRIGGER_PRICE_SLIPPAGE_LOWER

 */

internal class TradeInputDataValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) : BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {

    override fun validateTrade(
        internalState: InternalState,
        subaccountNumber: Int?,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?
    ): List<ValidationError>? {
        val errors = mutableListOf<ValidationError>()

        val trade = when (internalState.input.currentType) {
            InputType.TRADE -> internalState.input.trade
            InputType.CLOSE_POSITION -> internalState.input.closePosition
            else -> return null
        }
        val marketId = trade.marketId ?: return null
        val market = internalState.marketsSummary.markets[marketId]

        validateSize(
            trade = internalState.input.trade,
            market = market,
            assets = internalState.assets,
        )?.let {
            errors.addAll(it)
        }

        validateLimitPrice(
            trade = internalState.input.trade,
        )?.let {
            errors.addAll(it)
        }

        validateTimeInForce(
            trade = internalState.input.trade,
        )?.let {
            errors.addAll(it)
        }

        return errors
    }

    private fun validateSize(
        trade: InternalTradeInputState,
        market: InternalMarketState?,
        assets: Map<String, Asset>?
    ): List<ValidationError>? {
        /*
         ORDER_SIZE_BELOW_MIN_SIZE
         */
        val assetId = market?.perpetualMarket?.assetId ?: return null
        val symbol = assets?.get(assetId)?.displayableAssetId ?: return null
        val size = trade.size?.size ?: return null
        val minOrderSize = market.perpetualMarket?.configs?.minOrderSize ?: return null
        return if (size.abs() < minOrderSize) {
            listOf(
                error(
                    type = ErrorType.error,
                    errorCode = "ORDER_SIZE_BELOW_MIN_SIZE",
                    fields = null,
                    actionStringKey = null,
                    titleStringKey = "ERRORS.TRADE_BOX_TITLE.ORDER_SIZE_BELOW_MIN_SIZE",
                    textStringKey = "ERRORS.TRADE_BOX.ORDER_SIZE_BELOW_MIN_SIZE",
                    textParams = mapOf(
                        "MIN_SIZE" to mapOf(
                            "value" to minOrderSize,
                            "format" to "size",
                        ),
                        "SYMBOL" to mapOf(
                            "value" to symbol,
                            "format" to "string",
                        ),
                    ),
                ),
            )
        } else {
            null
        }
    }

    private fun validateLimitPrice(
        trade: InternalTradeInputState,
    ): List<ValidationError>? {
        /*
        LIMIT_MUST_ABOVE_TRIGGER_PRICE
        LIMIT_MUST_BELOW_TRIGGER_PRICE
         */
        return when (trade.type) {
            OrderType.StopLimit, OrderType.TakeProfitLimit -> {
                if (trade.execution != "IOC") {
                    return null
                }
                val side = trade.side ?: return null
                val limitPrice = trade.price?.limitPrice ?: return null
                val triggerPrice = trade.price?.triggerPrice ?: return null
                if (side == OrderSide.Buy && limitPrice < triggerPrice) {
                    // BUY
                    return listOf(
                        error(
                            type = ErrorType.error,
                            errorCode = "LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                            fields = listOf("price.triggerPrice"),
                            actionStringKey = "APP.TRADE.MODIFY_TRIGGER_PRICE",
                            titleStringKey = "ERRORS.TRADE_BOX_TITLE.LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                            textStringKey = "ERRORS.TRADE_BOX.LIMIT_MUST_ABOVE_TRIGGER_PRICE",
                        ),
                    )
                } else if (side == OrderSide.Sell && limitPrice > triggerPrice) {
                    // SELL
                    return listOf(
                        error(
                            type = ErrorType.error,
                            errorCode = "LIMIT_MUST_BELOW_TRIGGER_PRICE",
                            fields = listOf("price.triggerPrice"),
                            actionStringKey = "APP.TRADE.MODIFY_TRIGGER_PRICE",
                            titleStringKey = "ERRORS.TRADE_BOX_TITLE.LIMIT_MUST_BELOW_TRIGGER_PRICE",
                            textStringKey = "ERRORS.TRADE_BOX.LIMIT_MUST_BELOW_TRIGGER_PRICE",
                        ),
                    )
                } else {
                    null
                }
            }

            else -> return null
        }
    }

    private fun validateTimeInForce(
        trade: InternalTradeInputState,
    ): List<ValidationError>? {
        if (trade.goodTil != null && trade.options.needsGoodUntil) {
            val timeInterval = trade.goodTil?.timeInterval
            if (timeInterval != null && timeInterval > 90.days) {
                return listOf(
                    error(
                        type = ErrorType.error,
                        errorCode = "INVALID_GOOD_TIL",
                        fields = listOf("goodTil"),
                        actionStringKey = "APP.TRADE.MODIFY_GOOD_TIL",
                        titleStringKey = "ERRORS.TRADE_BOX_TITLE.INVALID_GOOD_TIL",
                        textStringKey = "ERRORS.TRADE_BOX.INVALID_GOOD_TIL_MAX_90_DAYS",
                    ),
                )
            }
        }

        return null
    }
}
