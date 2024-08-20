package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.state.internalstate.InternalInputState

internal class ReceiptCalculator {
    fun calculate(
        input: InternalInputState,
    ): InternalInputState {
        val receiptLines: List<ReceiptLine>? =
            when (input.currentType) {
                InputType.TRADE -> {
                    val trade = input.trade
                    when (trade.type) {
                        OrderType.Market, OrderType.StopMarket, OrderType.TakeProfitMarket, OrderType.TrailingStop -> {
                            listOf(
                                ReceiptLine.ExpectedPrice,
                                ReceiptLine.LiquidationPrice,
                                ReceiptLine.PositionMargin,
                                ReceiptLine.PositionLeverage,
                                ReceiptLine.Fee,
                                ReceiptLine.Reward,
                            )
                        }

                        else -> {
                            listOf(
                                ReceiptLine.LiquidationPrice,
                                ReceiptLine.PositionMargin,
                                ReceiptLine.PositionLeverage,
                                ReceiptLine.Fee,
                                ReceiptLine.Reward,
                            )
                        }
                    }
                }

                InputType.CLOSE_POSITION -> {
                    listOf(
                        ReceiptLine.BuyingPower,
                        ReceiptLine.MarginUsage,
                        ReceiptLine.ExpectedPrice,
                        ReceiptLine.Fee,
                        ReceiptLine.Reward,
                    )
                }

                InputType.TRANSFER -> {
                    null // TODO when working with transfer
                    /*
                val transfer = parser.asNativeMap(input["transfer"]) ?: return null
                val type = parser.asString(transfer["type"]) ?: return null
                return when (type) {
                    "DEPOSIT", "WITHDRAWAL" -> {
                        if (StatsigConfig.useSkip) {
                            listOf(
                                ReceiptLine.Equity.rawValue,
                                ReceiptLine.BuyingPower.rawValue,
                                ReceiptLine.BridgeFee.rawValue,
                                // add these back when supported by Skip
//                            ReceiptLine.ExchangeRate.rawValue,
//                            ReceiptLine.ExchangeReceived.rawValue,
//                            ReceiptLine.Fee.rawValue,
                                ReceiptLine.Slippage.rawValue,
                                ReceiptLine.TransferRouteEstimatedDuration.rawValue,
                            )
                        } else {
                            listOf(
                                ReceiptLine.Equity.rawValue,
                                ReceiptLine.BuyingPower.rawValue,
                                ReceiptLine.ExchangeRate.rawValue,
                                ReceiptLine.ExchangeReceived.rawValue,
                                ReceiptLine.Fee.rawValue,
//                                ReceiptLine.BridgeFee.rawValue,
                                ReceiptLine.Slippage.rawValue,
                                ReceiptLine.TransferRouteEstimatedDuration.rawValue,
                            )
                        }
                    }

                    "TRANSFER_OUT" -> {
                        listOf(
                            ReceiptLine.Equity.rawValue,
                            ReceiptLine.MarginUsage.rawValue,
                            ReceiptLine.Fee.rawValue,
                        )
                    }

                    else -> {
                        listOf()
                    }
                }
                     */
                }

                InputType.ADJUST_ISOLATED_MARGIN -> {
                    listOf(
                        ReceiptLine.CrossFreeCollateral,
                        ReceiptLine.CrossMarginUsage,
                        ReceiptLine.PositionLeverage,
                        ReceiptLine.PositionMargin,
                        ReceiptLine.LiquidationPrice,
                    )
                }

                else -> null
            }

        input.receiptLines = receiptLines
        return input
    }
}
