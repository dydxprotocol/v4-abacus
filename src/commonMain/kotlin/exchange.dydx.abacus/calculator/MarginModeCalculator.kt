package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS

internal object MarginModeCalculator {
    fun findExistingPosition(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        marketId: String?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val position = parser.asNativeMap(
            parser.value(
                account,
                "groupedSubaccounts.$subaccountNumber.openPositions.$marketId",
            ),
        )

        return if (
            position != null &&
            (parser.asDouble(parser.value(position, "size.current")) ?: 0.0) != 0.0
        ) {
            position
        } else {
            null
        }
    }

    fun findExistingOrder(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        marketId: String?,
        subaccountNumber: Int,
    ): Map<String, Any>? {
        val orders = parser.asNativeMap(
            parser.value(
                account,
                "groupedSubaccounts.$subaccountNumber.orders",
            ),
        )
        val order = orders?.entries?.firstOrNull {
            val orderMarketId = parser.asString(parser.value(it.value, "marketId"))
            val orderStatus = parser.asString(parser.value(it.value, "status"))
            orderMarketId == marketId && listOf("OPEN", "PENDING", "UNTRIGGERED", "PARTIALLY_FILLED").contains(orderStatus)
        }

        return if (order != null) order.value as Map<String, Any> else null
    }

    fun findExistingMarginMode(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        marketId: String?,
        subaccountNumber: Int,
    ): String? {
        val position = findExistingPosition(parser, account, marketId, subaccountNumber)
        if (position != null) {
            return if (position["equity"] != null) "ISOLATED" else "CROSS"
        }

        val openOrder = findExistingOrder(parser, account, marketId, subaccountNumber)
        if (openOrder != null) {
            return if ((
                    parser.asInt(
                        parser.value(
                            openOrder,
                            "subaccountNumber",
                        ),
                    ) ?: subaccountNumber
                    ) != subaccountNumber
            ) {
                "ISOLATED"
            } else {
                "CROSS"
            }
        }

        return null
    }

    fun findMarketMarginMode(
        parser: ParserProtocol,
        market: Map<String, Any>?,
    ): String {
        val marginMode = parser.asString(
            parser.value(
                market,
                "configs.perpetualMarketType",
            ),
        )
        return marginMode ?: "CROSS"
    }

    fun selectableMarginModes(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        market: Map<String, Any>?,
        subaccountNumber: Int,
    ): Boolean {
        val marketId = parser.asString(market?.get("id"))
        val existingMarginMode =
            findExistingMarginMode(parser, account, marketId, subaccountNumber)
        return if (existingMarginMode != null) {
            false
        } else if (marketId != null) {
            findMarketMarginMode(parser, market) == "CROSS"
        } else {
            true
        }
    }

    /**
     * @description Get the childSubaccount number that is available for the given marketId
     * @param marketId
     */
    fun getChildSubaccountNumberForIsolatedMarginTrade(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        subaccountNumber: Int,
        tradeInput: Map<String, Any>?
    ): Int {
        val marginMode = parser.asString(tradeInput?.get("marginMode"))
        if (marginMode != "ISOLATED") {
            return subaccountNumber
        }
        val marketId = parser.asString(tradeInput?.get("marketId")) ?: return subaccountNumber
        val subaccounts = parser.asMap(account?.get("subaccounts")) ?: return subaccountNumber

        var lastSubaccountNumber = subaccountNumber
        for ((_, item) in subaccounts) {
            val subaccount = parser.asMap(item) ?: continue
            val childSubaccountNumber =
                parser.asInt(subaccount["subaccountNumber"]) ?: subaccountNumber

            if (childSubaccountNumber % NUM_PARENT_SUBACCOUNTS == subaccountNumber) {
                if (containsMarket(parser, subaccount, marketId)) {
                    return childSubaccountNumber
                } else {
                    if (childSubaccountNumber > lastSubaccountNumber) {
                        lastSubaccountNumber = childSubaccountNumber
                    }
                }
            }
        }
        return lastSubaccountNumber + NUM_PARENT_SUBACCOUNTS
    }

    private fun containsMarket(
        parser: ParserProtocol,
        subaccount: Map<String, Any>,
        marketId: String
    ): Boolean {
        val positionSize = parser.asDouble(parser.value(subaccount, "openPositions.$marketId.size.current"))

        if ((positionSize ?: 0.0) > 0.0) {
            return true
        }
        val orders = parser.asMap(subaccount["orders"])
        val foundOrder = orders?.values?.firstOrNull { item ->
            val order = parser.asMap(item)
            return if (order != null) {
                parser.asString(order["marketId"]) == marketId
            } else {
                false
            }
        }
        return foundOrder != null
    }
}
