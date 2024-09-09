package exchange.dydx.abacus.calculator

import exchange.dydx.abacus.output.input.InputType
import exchange.dydx.abacus.output.input.OrderType
import exchange.dydx.abacus.output.input.ReceiptLine
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.state.internalstate.InternalInputState
import exchange.dydx.abacus.state.manager.StatsigConfig

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
                    when (input.transfer.type) {
                        TransferType.deposit, TransferType.withdrawal -> {
                            if (StatsigConfig.useSkip) {
                                listOf(
                                    ReceiptLine.Equity,
                                    ReceiptLine.BuyingPower,
                                    ReceiptLine.BridgeFee,
                                    // add these back when supported by Skip
//                            ReceiptLine.ExchangeRate,
//                            ReceiptLine.ExchangeReceived,
//                            ReceiptLine.Fee,
                                    ReceiptLine.Slippage,
                                    ReceiptLine.TransferRouteEstimatedDuration,
                                )
                            } else {
                                listOf(
                                    ReceiptLine.Equity,
                                    ReceiptLine.BuyingPower,
                                    ReceiptLine.ExchangeRate,
                                    ReceiptLine.ExchangeReceived,
                                    ReceiptLine.Fee,
//                                ReceiptLine.BridgeFee,
                                    ReceiptLine.Slippage,
                                    ReceiptLine.TransferRouteEstimatedDuration,
                                )
                            }
                        }

                        TransferType.transferOut -> {
                            listOf(
                                ReceiptLine.Equity,
                                ReceiptLine.MarginUsage,
                                ReceiptLine.Fee,
                            )
                        }

                        else -> {
                            listOf()
                        }
                    }
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
