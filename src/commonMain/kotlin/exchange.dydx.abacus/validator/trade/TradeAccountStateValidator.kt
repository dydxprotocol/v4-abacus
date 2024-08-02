package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.state.internalstate.InternalState
import exchange.dydx.abacus.state.manager.V4Environment
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol

internal class TradeAccountStateValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    override fun validateTrade(
        staticTyping: Boolean,
        internalState: InternalState,
        subaccount: Map<String, Any>?,
        market: Map<String, Any>?,
        configs: Map<String, Any>?,
        trade: Map<String, Any>,
        change: PositionChange,
        restricted: Boolean,
        environment: V4Environment?,
    ): List<Any>? {
        return if (subaccount != null) {
            val subaccountNumber = parser.asInt(subaccount["subaccountNumber"])
            val isIsolatedMarginTrade = subaccountNumber == null || subaccountNumber >= NUM_PARENT_SUBACCOUNTS

            val errors = mutableListOf<Any>()

            when (isIsolatedMarginTrade) {
                true -> {
                    val crossOrdersError = validateSubaccountCrossOrders(
                        parser,
                        subaccount,
                        trade,
                    )
                    if (crossOrdersError != null) {
                        errors.add(crossOrdersError)
                    }
                    val postAllOrdersError = validateSubaccountPostOrders(
                        parser,
                        subaccount,
                        trade,
                        change,
                    )
                    if (postAllOrdersError != null) {
                        errors.add(postAllOrdersError)
                    }
                }
                false -> {
                    val marginError = validateSubaccountMarginUsage(
                        parser,
                        subaccount,
                        change,
                    )
                    if (marginError != null) {
                        errors.add(marginError)
                    }
                    val crossOrdersError = validateSubaccountCrossOrders(
                        parser,
                        subaccount,
                        trade,
                    )
                    if (crossOrdersError != null) {
                        errors.add(crossOrdersError)
                    }
                    val postAllOrdersError = validateSubaccountPostOrders(
                        parser,
                        subaccount,
                        trade,
                        change,
                    )
                    if (postAllOrdersError != null) {
                        errors.add(postAllOrdersError)
                    }
                }
            }

            if (errors.size > 0) errors else null
        } else {
            null
        }
    }

    private fun validateSubaccountMarginUsage(
        parser: ParserProtocol,
        subaccount: Map<String, Any>,
        change: PositionChange,
    ): Map<String, Any>? {
        /*
        INVALID_NEW_ACCOUNT_MARGIN_USAGE
         */
        return when (change) {
            PositionChange.CLOSING, PositionChange.DECREASING -> null
            else -> {
                val equity = parser.asDouble(parser.value(subaccount, "equity.postOrder"))
                val marginUsage = parser.asDouble(parser.value(subaccount, "marginUsage.postOrder"))
                if (equity != null &&
                    (
                        equity == Numeric.double.ZERO ||
                            marginUsage == null ||
                            marginUsage < Numeric.double.ZERO ||
                            marginUsage > Numeric.double.ONE
                        )
                ) {
                    error(
                        "ERROR",
                        "INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                        listOf("size.size"),
                        "APP.TRADE.MODIFY_SIZE_FIELD",
                        "ERRORS.TRADE_BOX_TITLE.INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                        "ERRORS.TRADE_BOX.INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun validateSubaccountCrossOrders(
        parser: ParserProtocol,
        subaccount: Map<String, Any>,
        trade: Map<String, Any>,
    ): Map<String, Any>? {
        /*
        ORDER_CROSSES_OWN_ORDER
         */
        return if (fillsExistingOrder(
                parser,
                trade,
                parser.asNativeMap(subaccount["orders"]),
            )
        ) {
            error(
                "ERROR",
                "ORDER_CROSSES_OWN_ORDER",
                listOf("size.size"),
                "APP.TRADE.MODIFY_SIZE_FIELD",
                "ERRORS.TRADE_BOX_TITLE.ORDER_CROSSES_OWN_ORDER",
                "ERRORS.TRADE_BOX.ORDER_CROSSES_OWN_ORDER",
            )
        } else {
            null
        }
    }

    private fun fillsExistingOrder(
        parser: ParserProtocol,
        trade: Map<String, Any>,
        orders: Map<String, Any>?,
    ): Boolean {
        if (orders != null) {
            val type = parser.asString(trade["type"]) ?: return false
            val price = parser.asDouble(
                parser.value(
                    trade,
                    if (type == "MARKET") {
                        "marketOrder.worstPrice"
                    } else {
                        "summary.price"
                    },
                ),
            ) ?: return false
            val marketId = parser.asString(trade["marketId"]) ?: return false
            val side = parser.asString(trade["side"]) ?: return false

            val existing = orders.values.firstOrNull first@{ item ->
                val order = parser.asNativeMap(item) ?: return@first false
                val orderPrice = parser.asDouble(order["price"]) ?: return@first false
                val orderType = parser.asString(order["type"]) ?: return@first false
                val orderMarketId = parser.asString(order["marketId"]) ?: return@first false
                val orderStatus = parser.asString(order["status"]) ?: return@first false
                val orderSide = parser.asString(order["side"]) ?: return@first false
                if (orderMarketId == marketId && orderType == "LIMIT" && orderStatus == "OPEN") {
                    when (side) {
                        "BUY" -> {
                            if (orderSide == "SELL" && price >= orderPrice) {
                                val stopPrice =
                                    parser.asDouble(parser.value(trade, "price.triggerPrice"))
                                if (stopPrice != null) {
                                    stopPrice < price
                                } else {
                                    true
                                }
                            } else {
                                false
                            }
                        }

                        "SELL" -> {
                            if (orderSide == "BUY" && price <= orderPrice) {
                                val stopPrice =
                                    parser.asDouble(parser.value(trade, "price.triggerPrice"))
                                if (stopPrice != null) {
                                    stopPrice > price
                                } else {
                                    true
                                }
                            } else {
                                false
                            }
                        }

                        else -> false
                    }
                } else {
                    false
                }
            }
            return existing != null
        } else {
            return false
        }
    }

    private fun validateSubaccountPostOrders(
        parser: ParserProtocol,
        subaccount: Map<String, Any>,
        trade: Map<String, Any>,
        change: PositionChange,
    ): Map<String, Any>? {
        /*
        ORDER_WITH_CURRENT_ORDERS_INVALID
         */
        return if (reducingWithLimit(parser, change, parser.asString(trade["type"]))) {
            null
        } else {
            val positions = parser.asNativeMap(subaccount["openPositions"])
            if (positions != null) {
                var overleveraged = false
                for ((_, value) in positions) {
                    val position = parser.asNativeMap(value)
                    overleveraged = positionOverleveragedPostAllOrders(parser, position)
                    if (overleveraged) {
                        break
                    }
                }
                if (overleveraged) {
                    error(
                        "ERROR",
                        "ORDER_WITH_CURRENT_ORDERS_INVALID",
                        null,
                        null,
                        "ERRORS.TRADE_BOX_TITLE.ORDER_WITH_CURRENT_ORDERS_INVALID",
                        "ERRORS.TRADE_BOX.ORDER_WITH_CURRENT_ORDERS_INVALID",
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun reducingWithLimit(
        parser: ParserProtocol,
        change: PositionChange,
        type: String?,
    ): Boolean {
        return when (change) {
            PositionChange.CLOSING, PositionChange.DECREASING -> true
            else -> false
        } && type == "LIMIT"
    }

    private fun positionOverleveragedPostAllOrders(
        parser: ParserProtocol,
        position: Map<String, Any>?,
    ): Boolean {
        /*
        ORDER_WITH_CURRENT_ORDERS_INVALID
         */
        val leverage =
            parser.asDouble(parser.value(position, "leverage.postAllOrders")) ?: return false
        val adjustedImf =
            parser.asDouble(parser.value(position, "adjustedImf.postAllOrders")) ?: return false
        return if (adjustedImf > Numeric.double.ZERO) (leverage > Numeric.double.ONE / adjustedImf) else true
    }
}
