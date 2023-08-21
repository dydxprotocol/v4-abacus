package exchange.dydx.abacus.validator.trade

import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.state.app.helper.Formatter
import exchange.dydx.abacus.utils.IList
import exchange.dydx.abacus.utils.IMap
import exchange.dydx.abacus.utils.Numeric
import exchange.dydx.abacus.utils.values
import exchange.dydx.abacus.validator.BaseInputValidator
import exchange.dydx.abacus.validator.PositionChange
import exchange.dydx.abacus.validator.TradeValidatorProtocol
import kollections.iListOf
import kollections.iMutableListOf

internal class TradeAccountStateValidator(
    localizer: LocalizerProtocol?,
    formatter: Formatter?,
    parser: ParserProtocol,
) :
    BaseInputValidator(localizer, formatter, parser), TradeValidatorProtocol {
    override fun validateTrade(
        subaccount: IMap<String, Any>?,
        market: IMap<String, Any>?,
        trade: IMap<String, Any>,
        change: PositionChange,
        restricted: Boolean,
    ): IList<Any>? {
        return if (subaccount != null) {
            val errors = iMutableListOf<Any>()
            val marginError = validateSubaccountMarginUsage(
                parser,
                subaccount
            )
            if (marginError != null) {
                errors.add(marginError)
            }
            val totalOrderError = validateSubaccountOrders(
                parser,
                subaccount
            )
            if (totalOrderError != null) {
                errors.add(totalOrderError)
            }
            val crossOrdersError = validateSubaccountCrossOrders(
                parser,
                subaccount,
                trade
            )
            if (crossOrdersError != null) {
                errors.add(crossOrdersError)
            }
            val postAllOrdersError = validateSubaccountPostOrders(
                parser,
                subaccount,
                trade,
                change
            )
            if (postAllOrdersError != null) {
                errors.add(postAllOrdersError)
            }
            if (errors.size > 0) errors else null
        } else null
    }

    private fun validateSubaccountMarginUsage(
        parser: ParserProtocol,
        subaccount: IMap<String, Any>,
    ): IMap<String, Any>? {
        /*
        USER_MAX_ORDERS
        In v3, this error comes from backend. Holding off implementation for v4
         */
        return null
    }

    private fun validateSubaccountOrders(
        parser: ParserProtocol,
        subaccount: IMap<String, Any>,
    ): IMap<String, Any>? {
        /*
        INVALID_NEW_ACCOUNT_MARGIN_USAGE
         */
        val equity = parser.asDouble(parser.value(subaccount, "equity.postOrder"))
        val marginUsage = parser.asDouble(parser.value(subaccount, "marginUsage.postOrder"))
        return if (equity != null && (equity == Numeric.double.ZERO || marginUsage == null || marginUsage < Numeric.double.ZERO || marginUsage > Numeric.double.ONE)) {
            error(
                "ERROR",
                "INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                iListOf("size.size"),
                "APP.TRADE.MODIFY_SIZE_FIELD",
                "ERRORS.TRADE_BOX_TITLE.INVALID_NEW_ACCOUNT_MARGIN_USAGE",
                "ERRORS.TRADE_BOX.INVALID_NEW_ACCOUNT_MARGIN_USAGE"
            )
        } else null
    }

    private fun validateSubaccountCrossOrders(
        parser: ParserProtocol,
        subaccount: IMap<String, Any>,
        trade: IMap<String, Any>,
    ): IMap<String, Any>? {
        /*
        ORDER_CROSSES_OWN_ORDER
         */
        return if (fillsExistingOrder(parser, trade, parser.asMap(subaccount["orders"]))) error(
            "ERROR",
            "ORDER_CROSSES_OWN_ORDER",
            iListOf("size.size"),
            "APP.TRADE.MODIFY_SIZE_FIELD",
            "ERRORS.TRADE_BOX_TITLE.ORDER_CROSSES_OWN_ORDER",
            "ERRORS.TRADE_BOX.ORDER_CROSSES_OWN_ORDER"
        )
        else null
    }

    private fun fillsExistingOrder(
        parser: ParserProtocol,
        trade: IMap<String, Any>,
        orders: IMap<String, Any>?,
    ): Boolean {
        if (orders != null) {
            val type = parser.asString(trade["type"]) ?: return false
            val price = parser.asDouble(
                parser.value(
                    trade, if (type == "MARKET")
                        "marketOrder.worstPrice" else "summary.price"
                )
            ) ?: return false
            val marketId = parser.asString(trade["marketId"]) ?: return false
            val side = parser.asString(trade["side"]) ?: return false

            val existing = orders.values().firstOrNull() first@{ item ->
                val order = parser.asMap(item) ?: return@first false
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
        subaccount: IMap<String, Any>,
        trade: IMap<String, Any>,
        change: PositionChange,
    ): IMap<String, Any>? {
        /*
        ORDER_WITH_CURRENT_ORDERS_INVALID
         */
        return if (reducingWithLimit(parser, change, parser.asString(trade["type"])))
            null
        else {
            val positions = parser.asMap(subaccount["openPositions"])
            if (positions != null) {
                var overleveraged = false
                for ((_, value) in positions) {
                    val position = parser.asMap(value)
                    overleveraged = positionOverleveragedPostAllOrders(parser, position)
                    if (overleveraged) {
                        break
                    }
                }
                if (overleveraged) error(
                    "ERROR",
                    "ORDER_WITH_CURRENT_ORDERS_INVALID",
                    null,
                    null,
                    "ERRORS.TRADE_BOX_TITLE.ORDER_WITH_CURRENT_ORDERS_INVALID",
                    "ERRORS.TRADE_BOX.ORDER_WITH_CURRENT_ORDERS_INVALID"
                ) else null
            } else null
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
        position: IMap<String, Any>?,
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
