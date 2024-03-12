package exchange.dydx.abacus.utils

import kollections.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
enum class RiskLevel(val rawValue: Int) {
    low(0),

    medium(1),
    high(2);

    companion object {
        operator fun invoke(rawValue: Int) =
            RiskLevel.values().firstOrNull { it.rawValue == rawValue }
    }
}

@JsExport
class AbacusHelper {
    companion object {
        fun marginRiskLevel(marginUsage: Double): RiskLevel {
            return if (marginUsage < 0.2) {
                RiskLevel.low
            } else if (marginUsage < 0.4) {
                RiskLevel.medium
            } else {
                RiskLevel.high
            }
        }

        fun leverageRiskLevel(leverage: Double): RiskLevel {
            return if (leverage <= 2) {
                RiskLevel.low
            } else if (leverage <= 5) {
                RiskLevel.medium
            } else {
                RiskLevel.high
            }
        }
    }
}
