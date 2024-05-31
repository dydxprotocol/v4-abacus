package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.abacus.utils.NUM_PARENT_SUBACCOUNTS

internal object MarginModeCalculator {
    fun findExistingMarginMode(
        parser: ParserProtocol,
        account: Map<String, Any>?,
        marketId: String?,
        subaccountNumber: Int,
    ): String? {
        val position = parser.asNativeMap(
            parser.value(
                account,
                "groupedSubaccounts.$subaccountNumber.openPositions.$marketId",
            ),
        )
        if (position != null && (
                    parser.asDouble(parser.value(position, "size.current"))
                        ?: 0.0
                    ) != 0.0
        ) {
            return if (position["equity"] == null) {
                "CROSS"
            } else {
                "ISOLATED"
            }
        }
        val order = parser.asNativeMap(
            parser.value(account, "groupedSubaccounts.$subaccountNumber.orders"),
        )?.values?.firstOrNull {
            parser.asString(parser.value(it, "marketId")) == marketId
        }
        if (order != null) {
            return if ((
                        parser.asInt(
                            parser.value(
                                order,
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
        marketId: String?
    ): Int {
        val marketId = marketId ?: return subaccountNumber
        val subaccounts = parser.asMap(account?.get("subaccounts")) ?: return subaccountNumber

        var lastSubaccountNumber = subaccountNumber
        for ((key, item) in subaccounts) {
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
        val openPositions = parser.asMap(subaccount["openPositions"])
        if (openPositions?.containsKey(marketId) ?: false) {
            return true
        }
        val orders = parser.asMap(subaccount["orders"])
        val foundOrder = orders?.firstOrNull { item ->
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
