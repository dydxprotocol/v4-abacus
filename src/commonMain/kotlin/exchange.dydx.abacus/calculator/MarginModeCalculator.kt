package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.protocols.ParserProtocol

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
        if (position != null) {
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
}
