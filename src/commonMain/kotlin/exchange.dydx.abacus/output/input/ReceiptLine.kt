package exchange.dydx.abacus.output.input

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class ReceiptLine(val rawValue: String) {
    Equity("EQUITY"),
    BuyingPower("BUYING_POWER"),
    MarginUsage("MARGIN_USAGE"),
    ExpectedPrice("EXPECTED_PRICE"),
    Fee("FEE"),
    Total("TOTAL"),
    WalletBalance("WALLET_BALANCE"),
    BridgeFee("BRIDGE_FEE"),
    ExchangeRate("EXCHANGE_RATE"),
    ExchangeReceived("EXCHANGE_RECEIVED"),
    Slippage("SLIPPAGE"),
    GasFee("GAS_FEES"),
    Reward("REWARD"),
    TransferRouteEstimatedDuration("TRANSFER_ROUTE_ESTIMATE_DURATION"),
    CrossFreeCollateral("CROSS_FREE_COLLATERAL"),
    CrossMarginUsage("CROSS_MARGIN_USAGE"),
    PositionMargin("POSITION_MARGIN"),
    PositionLeverage("POSITION_LEVERAGE"),
    LiquidationPrice("LIQUIDATION_PRICE"),
    TransferFee("TRANSFER_FEE");

    companion object {
        operator fun invoke(rawValue: String) =
            entries.firstOrNull { it.rawValue == rawValue }
    }
}
