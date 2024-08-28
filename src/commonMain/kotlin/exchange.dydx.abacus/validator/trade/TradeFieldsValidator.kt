package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.ValidationError
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.state.model.TradeInputField
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol
import kotlin.time.Duration

internal class TradeFieldsValidator(
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
        val trade = when (internalState.input.currentType) {
            InputType.TRADE -> internalState.input.trade
            InputType.CLOSE_POSITION -> internalState.input.closePosition
            else -> return null
        }

        val errors = mutableListOf<ValidationError>()
        if (trade.options.needsSize) {
            val size = trade.size?.size ?: 0.0
            val usdcSize = trade.size?.usdcSize ?: 0.0
            if (size == 0.0 && usdcSize == 0.0) {
                errors.add(
                    required(
                        errorCode = "REQUIRED_SIZE",
                        field = if (size == 0.0) TradeInputField.size.rawValue else TradeInputField.usdcSize.rawValue,
                        actionStringKey = "APP.TRADE.ENTER_AMOUNT",
                    ),
                )
            }
        }

        if (trade.options.needsLimitPrice) {
            val limitPrice = trade.price?.limitPrice ?: 0.0
            if (limitPrice == 0.0) {
                errors.add(
                    required(
                        errorCode = "REQUIRED_LIMIT_PRICE",
                        field = TradeInputField.limitPrice.rawValue,
                        actionStringKey = "APP.TRADE.ENTER_LIMIT_PRICE",
                    ),
                )
            }
        }

        if (trade.options.needsTriggerPrice) {
            val triggerPrice = trade.price?.triggerPrice ?: 0.0
            if (triggerPrice == 0.0) {
                errors.add(
                    required(
                        errorCode = "REQUIRED_TRIGGER_PRICE",
                        field = TradeInputField.triggerPrice.rawValue,
                        actionStringKey = "APP.TRADE.ENTER_TRIGGER_PRICE",
                    ),
                )
            }
        }

        if (trade.options.needsTrailingPercent) {
            val trailingPercent = trade.price?.trailingPercent ?: 0.0
            if (trailingPercent == 0.0) {
                errors.add(
                    required(
                        errorCode = "REQUIRED_TRAILING_PERCENT",
                        field = TradeInputField.trailingPercent.rawValue,
                        actionStringKey = "APP.TRADE.ENTER_TRAILING_PERCENT",
                    ),
                )
            }
        }

        if (!trade.options.timeInForceOptions.isNullOrEmpty()) {
            if (trade.timeInForce.isNullOrEmpty()) {
                errors.add(
                    required(
                        errorCode = "REQUIRED_TIME_IN_FORCE",
                        field = TradeInputField.timeInForceType.rawValue,
                        actionStringKey = "APP.TRADE.ENTER_TIME_IN_FORCE",
                    ),
                )
            }
        }

        if (trade.options.needsGoodUntil) {
            val goodTil = trade.goodTil?.timeInterval ?: Duration.ZERO
            if (goodTil == Duration.ZERO) {
                errors.add(
                    required(
                        errorCode = "REQUIRED_GOOD_UNTIL",
                        field = "goodTil",
                        actionStringKey = "APP.TRADE.ENTER_GOOD_UNTIL",
                    ),
                )
            }
        }

        if (!trade.options.executionOptions.isNullOrEmpty()) {
            if (trade.execution.isNullOrEmpty()) {
                errors.add(
                    required(
                        errorCode = "REQUIRED_EXECUTION",
                        field = TradeInputField.execution.rawValue,
                        actionStringKey = "APP.TRADE.ENTER_EXECUTION",
                    ),
                )
            }
        }

        return errors
    }

    override fun validateTradeDeprecated(
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?
    ): List<Any>? {
        return null
    }
}
